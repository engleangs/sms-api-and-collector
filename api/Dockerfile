FROM python:3.9
WORKDIR /code
COPY   ./ /code/
RUN apt-get update
#RUN apt-get install -y iputils-ping
RUN pip install --no-cache-dir --upgrade -r /code/requirements.txt
ENV MONGO_URL=mongodb://root:rootpassword@mongodb_container:27017/db_sms?retryWrites=true&w=majority
ENV KAFKA_URL=kafka:29092
CMD ["uvicorn","app.main:app", "--host","0.0.0.0", "--port", "80"]