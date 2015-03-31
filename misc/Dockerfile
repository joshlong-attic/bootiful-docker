FROM java
ADD ./build/service.jar /service.jar
ADD ./build/run.sh /run.sh
RUN chmod a+x /run.sh
CMD /run.sh
