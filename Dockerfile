FROM maven:3.9.9-amazoncorretto-21 AS builder

RUN mkdir -p /usr/src/app

COPY src /usr/src/app/src
COPY pom.xml /usr/src/app

RUN mvn -f /usr/src/app/pom.xml clean install -DskipTests -Dpmd.skip -Dcheckstyle.skip

FROM maven:3.9.9-amazoncorretto-21
COPY --from=builder /usr/src/app/target/*.jar /usr/app/gateway.jar
COPY start.sh /usr/app/start.sh

RUN chmod +x /usr/app/start.sh

ENTRYPOINT ["/usr/app/start.sh"]

