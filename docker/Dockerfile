FROM openjdk:17-alpine

USER root

# Установка Node.js 22 из edge-репозитория
RUN apk add --update --no-cache \
    --repository=https://dl-cdn.alpinelinux.org/alpine/edge/main \
    nodejs npm

# Проверка версий
RUN node -v && npm -v

#Подготовка сертификата yandex
RUN keytool -importkeystore -srckeystore forward-system.jks -destkeystore forward-system.p12 -deststoretype pkcs12
RUN mkdir -p /root/.postgresql && \
    wget "https://storage.yandexcloud.net/cloud-certs/CA.pem" \
      --output-document /root/.postgresql/root.crt && \
    chmod 0600 /root/.postgresql/root.crt

COPY . /usr/src/forward-system
WORKDIR /usr/src/forward-system

RUN echo Build starts
RUN ls
RUN node -v
RUN npm -v
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

EXPOSE 8081/tcp
ENV TZ="Europe/Moscow"

RUN echo Run the forward-system

CMD ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "./build/libs/forward-system-0.1.0.jar"]