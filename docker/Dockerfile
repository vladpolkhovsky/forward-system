FROM openjdk:17

COPY . /usr/src/forward-system

WORKDIR /usr/src/forward-system

RUN chmod +x gradlew

RUN microdnf install findutils

RUN echo Build starts

RUN ./gradlew build

EXPOSE 8081/tcp

RUN echo Run the forward-system

CMD ["java", "-jar", "./build/libs/telegram-bot-0.0.1.jar"]