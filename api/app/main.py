from time import sleep
from tkinter.messagebox import NO
from fastapi import FastAPI, Body, HTTPException, status,Header
from fastapi.responses import JSONResponse
from fastapi.encoders import jsonable_encoder
from typing import Final, Optional, List
import motor.motor_asyncio
from kafka import KafkaConsumer,KafkaProducer
import hashlib
import base64
import asyncio
import json
from pkg_resources import get_provider
import requests
from multiprocessing import Process
import app.config as config
from app.model import ContentProviderModel, MoData, MessageData,MessageInput
from datetime import datetime
import concurrent.futures
from loguru import logger
import  nest_asyncio
nest_asyncio.apply()
app = FastAPI()
#logger.info(f"db server url  {config.MONGODB}")
logger.info(f"kafka server  : {config.KAFKA_SERVER}")
client = motor.motor_asyncio.AsyncIOMotorClient(config.MONGODB,authSource="admin" ) #os.environ["MONGODB_URL"]
db = client.smsapi
sms_producers:Optional[KafkaProducer]= None
 
CACHE:Final= {} # Cache to cache some data 


DN_THREAD_POOL = concurrent.futures.ThreadPoolExecutor(5)
MO_THREAD_POOL = concurrent.futures.ThreadPoolExecutor(5)

# Config function 

def get_producer()->KafkaProducer:
    global sms_producers
    if sms_producers is None:
        sms_producers  = KafkaProducer( bootstrap_servers=config.KAFKA_SERVER,value_serializer=lambda m: json.dumps(m).encode('utf8'))
    return sms_producers
    


async def get_short_codes():
    '''
    Get short code set from all cp in DB or CACHE if available
    '''
    if 'short_codes' in CACHE:
        return CACHE['short_code']
    cp_list = await db["content_providers"].find().to_list(1000)
    short_codes  = [   cp["shortCodeList"].split(',') for i,cp in enumerate(cp_list) ]
    items = [ item for  code  in short_codes  for item in code  ]
    CACHE['short_code'] = set(items)
    return CACHE['short_code']

async def get_cp_by_short_code(short_code)->Optional[ContentProviderModel]:
    '''
    Get content provider by input short code 
    '''
    if 'short_code:content_provider' in CACHE:
        if short_code in CACHE['short_code:content_provider']:
            return CACHE['short_code:content_provider'][short_code]
    data = {}
    cp_list = await db["content_providers"].find().to_list(1000)
    for _,cp in enumerate(cp_list):
        short_codes = cp["shortCodeList"].split(",")
        for _, code in enumerate( short_codes):
            data[code] = ContentProviderModel(**cp)
    CACHE['short_code:content_provider']  = data
    if short_code in data :
        return data[ short_code ]
    return None



    
def trigger_mo(url, mo:MoData,cp:ContentProviderModel):
    '''
    Triggering MO( Mobile Originate) event to Content provider MO endpoint

    '''
    request_date =datetime.utcnow().timestamp() if mo.requestDate is None  else mo.requestDate.timestamp()
    signature = get_signature( cp.secreteKey, mo.src)
    p = { 'number': mo.dest, 'originator':mo.src,'content':mo.content,'request_date': request_date , 'signature':signature}
    result = requests.get(url,p,stream=True)
    content = result.content.decode("utf-8") if result.content else ""
    return ( content, result.status_code )


def get_signature(secrete_key:str, number:str)->str:
    '''
    Get signature string combine between number and secrete key
    '''
    return base64.b64encode(hashlib.sha256(''.join([secrete_key,'@',number]).encode('utf-8')).digest()).decode('utf-8')

def trigger_dn(url, cp:ContentProviderModel ,message:MessageData):
    '''
    Triggering DN ( Delivery Notification ) to Content provider DN endpoint
    '''
    signature = get_signature( cp.secreteKey, message.dest)
    dn = { 'number':message.dest, 'originator':message.src, 'content': message.content, 'request_date':datetime.utcnow().timestamp(), 'signature':signature}
    result = requests.get(url, dn)
    content = result.content.decode("utf-8") if result.content else ""
    dn["callBackContent"] = content
    dn['callBackStausCode'] = result.status_code
    res =  db["dn_logs"].insert_one(dn)
    logger.info(f"done called dn ->{url} : {content} : {result.status_code}")
    return dn


async def check_auth(auth_token:str, cp : ContentProviderModel)->bool:
    '''Verify basic authentication '''
    comb = base64.b64decode(auth_token).decode('utf-8')
    try:
        username,password = comb.split(':')
        return username == cp.username and password == cp.password
    except:
        return False
    return False


def consume_mo(topic):
    '''
    Consuming MO ( Mobile Originate ) from Kafka
    ''' 
    logger.debug("Consuming mo")
    sleep(3) # just to let docker ready to serve for kafka
    try:
         consumer = KafkaConsumer(topic,bootstrap_servers=config.KAFKA_SERVER)
         for message in consumer:
            data = message.value.decode('utf-8')
            logger.info(f"receiving MO trigger {message.topic} key={message.key} value= {data}")
            mo = MoData.parse_raw(data)
            res = get_cp_by_short_code( mo.dest)
            cp =  asyncio.get_event_loop().run_until_complete(res) 
            if cp :
                try:
                  (content, status_code) = trigger_mo(cp.moUrl, mo,cp)
                  mo.callBackResult = content
                  mo.callBackStatusCode = status_code
                except Exception as e :
                    logger.error(f"error calling back to : {cp.moUrl}  : {e}")
            else:
                logger.warning("Not found")
                mo.callBackResult = "no_found_cp"
            mo_data  = jsonable_encoder(mo)
            db["mo_logs"].insert_one(mo_data)
        
    except Exception as e:
         logger.error(f"Error!! {e}")






@app.post("/cp",response_description="Add New Content Provider", response_model=ContentProviderModel)
async def create_cp(cp: ContentProviderModel = Body(...)):    
    cp = jsonable_encoder(cp)
    new_cp = await db["content_providers"].insert_one(cp)
    created_cp = await db["content_providers"].find_one({"_id": new_cp.inserted_id})
    CACHE.clear()
    return JSONResponse(status_code=status.HTTP_201_CREATED, content=created_cp)


@app.get('/cp',response_description='List all content providers', response_model=List[ContentProviderModel])
async def get_cp():
    cp_list = await db["content_providers"].find().to_list(length=None)
    return cp_list

@app.post("/send",response_description="Submit Sms")
async def submit_sms(auth_header: str = Header(...), sms_input:MessageInput= Body(...)):
    #auth_header = request.headers.get('Authorization')
    if not auth_header:
          raise HTTPException(status_code=493, detail=f"Unauthorized")
    cp = await get_cp_by_short_code( sms_input.src)
    if not cp :
        raise HTTPException(status_code=493, detail=f"Unauthorized")
    auth_check = await check_auth(auth_header,cp)
    if not auth_check:
        raise HTTPException(status_code=493, detail=f"Unauthorized")
    sms_input.request_date = datetime.now()
    message_data  = [ { 'dest':dest, 'src':sms_input.src, 'lang':sms_input.lang, 'content': sms_input.content} for dest in sms_input.dest]
    sms_input = jsonable_encoder(sms_input)
    await db["mt_submits"].insert_one(sms_input)
    for _,msg in enumerate(message_data):
        get_producer().send( config.KAFKA_MT_TOPIC, msg)
        if cp.dnUrl:
            msg_data = MessageData(**msg)
            DN_THREAD_POOL.submit(trigger_dn, cp.dnUrl, cp, msg_data)

    return {'success':True, 'message':'Success'}


consumer_thread = Process(target= consume_mo,args=( config.KAFKA_MO_TOPIC,))
@app.on_event("startup")
async def startup():
    consumer_thread.start()    
    logger.info("demo")
    

@app.on_event("shutdown")
def shutdown_event():
    DN_THREAD_POOL.shutdown()
    MO_THREAD_POOL.shutdown()
    consumer_thread.kill()

