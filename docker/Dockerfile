# Стадия с Node.js
FROM node:22-alpine AS nodejs

# Финальная стадия с JDK
FROM gradle:8.8.0-jdk21-alpine

# Копируем всю среду Node.js
COPY --from=nodejs /usr/local /usr/local

# Копируем зависимости C++
COPY --from=nodejs /lib/ /lib/
COPY --from=nodejs /usr/lib/ /usr/lib/

# Проверка версии
RUN node -v && npm -v

#Подготовка сертификата yandex
RUN keytool -importkeystore -srckeystore forward-system.jks -destkeystore forward-system.p12 -deststoretype pkcs12
RUN mkdir -p /root/.postgresql && \
    wget "https://storage.yandexcloud.net/cloud-certs/CA.pem" \
      --output-document /root/.postgresql/root.crt && \
    chmod 0600 /root/.postgresql/root.crt

# Установка необходимых библиотек
RUN apk add --no-cache libc6-compat gcompat

COPY ./forward-system-frontend /usr/src/forward-system/forward-system-frontend
COPY ./src /usr/src/forward-system/src
COPY ./build.gradle.kts /usr/src/forward-system/build.gradle.kts
COPY ./settings.gradle.kts /usr/src/forward-system/settings.gradle.kts

WORKDIR /usr/src/forward-system

RUN echo Build starts

RUN ls
RUN node -v
RUN npm -v

RUN gradle :forward-system-frontend:install
RUN gradle :forward-system-frontend:build

RUN gradle clean build -x test -x :forward-system-frontend:build -x :forward-system-frontend:install --refresh-dependencies

EXPOSE 8081/tcp
ENV TZ="Europe/Moscow"

RUN echo Run the forward-system

CMD ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "./build/libs/forward-system-0.1.0.jar"]