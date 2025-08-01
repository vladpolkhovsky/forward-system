FROM openjdk:17

USER root

RUN wget -fsSL https://deb.nodesource.com/setup_22.x | bash -
RUN microdnf install -y --nodocs wget findutils nodejs
RUN npm install
RUN keytool -importkeystore -srckeystore forward-system.jks -destkeystore forward-system.p12 -deststoretype pkcs12
RUN mkdir -p /root/.postgresql && \
    wget "https://storage.yandexcloud.net/cloud-certs/CA.pem" \
      --output-document /root/.postgresql/root.crt && \
    chmod 0600 /root/.postgresql/root.crt

COPY . /usr/src/forward-system
WORKDIR /usr/src/forward-system

RUN ls
RUN sed -i -e 's/\r$//' ./gradlew
RUN chmod +x ./gradlew
RUN echo Build starts
RUN ./gradlew build -x test

EXPOSE 8081/tcp
ENV TZ="Europe/Moscow"

RUN echo Run the forward-system

CMD ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "./build/libs/forward-system-0.1.0.jar"]