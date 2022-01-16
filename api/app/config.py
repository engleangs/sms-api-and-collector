from typing import Final
from os import getenv
MONGODB:Final= getenv("MONGO_URL", "mongodb://root:rootpassword@localhost:27017/db_sms?retryWrites=true&w=majority")
KAFKA_SERVER:Final= getenv("KAFKA_URL",'127.0.0.1:29092')
KAFKA_MO_TOPIC="sms_receiving"
KAFKA_MT_TOPIC="sms_api_send"