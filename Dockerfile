FROM java
ADD ./build/service.jar /service.jar
ADD ./build/run.sh /run.sh
RUN chmod 755 /run.sh
