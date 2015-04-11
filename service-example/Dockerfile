FROM java
ADD ./target/service.jar /service.jar
ADD ./target/run.sh /run.sh
ENV SPRING_PROFILES_ACTIVE lattice
RUN chmod a+x /run.sh
CMD /run.sh
