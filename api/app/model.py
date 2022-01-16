from bson import ObjectId
from typing import Final, Optional, List

from datetime import date, datetime
from pydantic import BaseModel, Field, EmailStr,validator
class PyObjectId(ObjectId):
    @classmethod
    def __get_validators__(cls):
        yield cls.validate

    @classmethod
    def validate(cls, v):
        if not ObjectId.is_valid(v):
            raise ValueError("Invalid objectid")
        return ObjectId(v)

    @classmethod
    def __modify_schema__(cls, field_schema):
        field_schema.update(type="string")

class MoData(BaseModel):
    src:str
    dest:str
    content:Optional[str]
    longMsg:bool 
    requestDate:Optional[datetime]
    lang:int
    callBackResult:Optional[str]
    callBackStatusCode:Optional[int]
    
    @validator("requestDate", pre=True)
    def parse_request_date(cls, value):
        return datetime.fromtimestamp(value / 1e3)
    class Config:
         json_encoders = {ObjectId: str}
       

class MessageData(BaseModel):
    src:str
    dest:str
    content:str
    lang:int

class MessageInput(BaseModel):
    id: PyObjectId = Field(default_factory=PyObjectId, alias="_id")
    src:str
    dest:List
    content:str
    lang:int  
    request_date:Optional[datetime]
    class Config:
        allow_population_by_field_name = True
        arbitrary_types_allowed = True
        json_encoders = {ObjectId: str}
        schema_extra = {
            "example": {
                "src": "1000",
                "dest": ["7711878787","7711878788"],
                "content": "Hello world",
                "lang":1
            }
        }


class ContentProviderModel(BaseModel):
    id: PyObjectId = Field(default_factory=PyObjectId, alias="_id")
    name: Optional[str]
    email :Optional[EmailStr]
    username:Optional[str]
    password:Optional[str]
    moUrl:str
    dnUrl:str
    shortCodeList:str
    secreteKey:str

    class Config:
        allow_population_by_field_name = True
        arbitrary_types_allowed = True
        json_encoders = {ObjectId: str}
        schema_extra = {
            "example": {
                "name": "Content provider x",
                "email": "jdoe@example.com",
                "username": "cp_x",
                "password":"abc123@test",
                "moUrl":"http://localhost/mo.php",
                "dnUrl":"http://localhost/dn.php",
                "shortCodeList":"1000,1001",
                "secreteKey":"2Z0t5cZ4EXsFXKRvCX4vEz27nEAGtzhn"
            }
        }
