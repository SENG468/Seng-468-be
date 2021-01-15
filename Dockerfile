FROM openjdk:11-jre-slim

ADD --chown=root:root /build/libs/stocktrade-1.0.jar /app/

WORKDIR /app/
CMD ["java", "-jar", "/app/stocktrade-1.0.jar"]