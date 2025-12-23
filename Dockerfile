# Multi-stage build для минимального образа
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Копируем только pom.xml для кэширования зависимостей
COPY pom.xml .

# Скачиваем зависимости (кэшируется отдельно)
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем JAR
RUN mvn clean package -DskipTests

# Финальный образ
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем JAR из стадии сборки
COPY --from=build /app/target/*.jar app.jar

# Оптимизация для Spring Boot
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]