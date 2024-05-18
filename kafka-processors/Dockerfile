FROM openjdk:17
WORKDIR /app
ADD target/humidity-processor.jar /app/humidity-processor.jar
ENTRYPOINT ["java", "-jar", "/app/humidity-processor.jar"]
