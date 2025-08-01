plugins {
	java
	id("org.springframework.boot") version "3.3.1"
	id("io.spring.dependency-management") version "1.1.5"
}

group = "by.forward"
version = "0.1.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.security:spring-security-messaging")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.retry:spring-retry:2.0.11")
	implementation("org.springframework:spring-aspects:6.2.5")
	implementation("org.springframework.boot:spring-boot-starter-web")

	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	implementation("org.apache.commons:commons-lang3:3.12.0")
	implementation("org.apache.commons:commons-collections4:4.4")
	implementation("nl.martijndwars:web-push:5.1.1")
	implementation("org.mapstruct:mapstruct:1.6.3")
	implementation("org.bouncycastle:bcpkix-jdk18on:1.76")

	implementation("org.telegram:telegrambots-springboot-longpolling-starter:7.2.1")
	implementation("org.telegram:telegrambots-longpolling:7.10.0")
	implementation("org.telegram:telegrambots-client:7.2.1")

	implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.529"))
	implementation("com.amazonaws:aws-java-sdk-s3")

	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("com.h2database:h2")

	compileOnly("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
}

tasks.withType<Test> {
	//useJUnitPlatform()
}

tasks.build {
	dependsOn(project("forward-system-frontend").tasks.build)
}