FROM openjdk:17

USER root

ENV TZ="Europe/Moscow"

COPY . /usr/src/forward-system

RUN microdnf install --nodocs wget findutils

RUN keytool -importkeystore -srckeystore forward-system.jks -destkeystore forward-system.p12 -deststoretype pkcs12

RUN mkdir -p /root/.postgresql && \
    wget "https://storage.yandexcloud.net/cloud-certs/CA.pem" \
      --output-document /root/.postgresql/root.crt && \
    chmod 0600 /root/.postgresql/root.crt

WORKDIR /usr/src/forward-system

RUN chmod +x gradlew

RUN echo Build starts

RUN ./gradlew build -x test

EXPOSE 8081/tcp

RUN echo Run the forward-system

CMD ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "./build/libs/forward-system-0.1.0.jar"]