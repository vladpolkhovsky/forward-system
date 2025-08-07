# Стадия с Node.js
FROM node:22-alpine AS nodejs

COPY ./forward-system-frontend /usr/src/forward-system-fronted
WORKDIR /usr/src/forward-system-fronted

RUN npm install

# Стадия для загрузки Gradle-зависимостей
FROM gradle:8.8.0-jdk21-alpine AS gradle-deps

# Копируем ТОЛЬКО файлы сборки Gradle
COPY ./build.gradle.kts /usr/src/forward-system/
COPY ./settings.gradle.kts /usr/src/forward-system/

# Копируем файлы сборки подпроектов (если есть)
# Пример для фронтенда:
COPY ./forward-system-frontend/build.gradle /usr/src/forward-system/forward-system-frontend/

WORKDIR /usr/src/forward-system
RUN gradle dependencies --no-daemon

# Финальная стадия с JDK
FROM gradle:8.8.0-jdk21-alpine AS build

# Копируем кэш Gradle из стадии зависимостей
COPY --from=gradle-deps /root/.gradle /root/.gradle

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
COPY --from=nodejs /usr/src/forward-system-fronted/node_modules /usr/src/forward-system/forward-system-frontend/node_modules

COPY ./src /usr/src/forward-system/src
COPY ./build.gradle.kts /usr/src/forward-system/build.gradle.kts
COPY ./settings.gradle.kts /usr/src/forward-system/settings.gradle.kts

WORKDIR /usr/src/forward-system

RUN echo Build starts

RUN ls
RUN node -v
RUN npm -v

RUN gradle :forward-system-frontend:build

RUN gradle clean build -x test -x :forward-system-frontend:build -x :forward-system-frontend:install

EXPOSE 8081/tcp
ENV TZ="Europe/Moscow"

RUN echo Run the forward-system

CMD ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "./build/libs/forward-system-0.1.0.jar"]