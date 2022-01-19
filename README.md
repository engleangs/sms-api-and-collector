# SMS Api and Collector
### The combination between FastAPI (Front API), SpringBoot ( Daemon task) via Kafka and Mongodb as Database

![N|Solid](https://cdn-images-1.medium.com/max/800/0*wmA2MbVb89z2Ur8k)



## System components:

- API : FastAPI to act as front web service allow CP to trigger sms api with rest format
- Collector : Spring Boot console app, acting as daemon service keep getting data from Apache Kafka , forwarding to SMSC/Simulator using SMPP protocol and receiving MO data from SMSC/Simulator
- MongoDB : database to store logs and config data
- Apache Kafka : message broker acting as queue forwarding message to consumer and receiving message from producer
- SMPP Simulator : ( credit to selenium ) to act as SMSC on our computer , this is just to be sure that we can develop system without needing to connect to real SMSC
- Sample handler : just provide sample handler web service to simulate CP service.
- Docker : using docker-compose to make it up and running on our computer easily

## Sequencial Diagrams:
MT Flow

![N|Solid](https://cdn-images-1.medium.com/max/800/0*e8-ldgcDtogSwCrw)


MO Flow

![N|Solid](https://cdn-images-1.medium.com/max/800/0*c_yc-PLjT9eKJxGp)


## Features

- Content provider management
- Allow sending SMS via API with basic authentication
- Log MT, MO and DN in MongoDB database
- Call back to DN via http request for DN and MO with sha as signature
- Connecting to SMSC/SMPP Simulator as Receiver, Transmitter and Tranceiver


## Up and running

```sh
git clone https://github.com/engleangs/sms-api-and-collector 
cd project directory
docker-compose up -d
```

This will bring all instance of services up including: Apache kafka , MongoDB, SMPP Collector , FastAPi and Simple Handler

Once done, check the status of all docker image and you can browser on the borwsers:
```sh
http://127.0.0.1:8000/docs
```
It should display the documeation ( Swagger) of RestAPi available in FastAPI.
![N|Solid](https://cdn-images-1.medium.com/max/800/1*kfTjffMn9iZne6qyn6d3bg.png)
Access to MongoDB you sill see some data and try to execute sample API as well as check the docker logs.
Docker images:
![N|Solid](https://cdn-images-1.medium.com/max/800/1*MRGa2yR43VPwm29vLaFk1Q.png)
MongoDB data
![N|Solid](https://cdn-images-1.medium.com/max/800/1*-ahODE-q29b1Yh2x81x_CA.png)


## To Do:
- API :
    - Auth verification : add more secure and complex security like JWT or Sha25 signature verify auth from CP
    - Add log file or database logs for every APi call and other important events
    - Separate api triggering from FastAPI and retry mechanism
    - TPS control using RateLimit or Redis

- SMPP Collector console application:
    - Add more language support for SMS integration
    - Add routing and filtering based on Short Code belong to specific
    - Add more detail error message based on error code from SMPP



