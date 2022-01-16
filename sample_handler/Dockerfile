FROM php:8.0-apache
WORKDIR /var/www/html
COPY index.html index.html
RUN echo "" > dn.log
RUN echo "" > mo.log
RUN chown www-data:www-data dn.log 
RUN chown www-data:www-data mo.log

COPY dn.php dn.php
COPY mo.php mo.php
EXPOSE 80
