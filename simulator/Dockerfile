FROM openjdk:8
WORKDIR  /smppsim
COPY ./conf /smppsim/conf
COPY ./docs /smppsim/docs
COPY ./lib /smppsim/lib
COPY ./www /smppsim/www
COPY ./deliver_messages.csv /smppsim/
COPY ./smppsim.jar /smppsim/
EXPOSE 2775 88
CMD ["java", "-Djava.net.preferIPv4Stack=true", "-Djava.util.logging.config.file=/smppsim/conf/logging.properties", "-jar", "/smppsim/smppsim.jar", "/smppsim/conf/smppsim.props"]