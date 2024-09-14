# Используем базовый образ с JDK 21
FROM openjdk:21-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файл JAR вашего приложения в контейнер
COPY quartzbot-0.0.1-SNAPSHOT.jar app.jar

# Устанавливаем переменные окружения для подключения к PostgreSQL
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/postgres
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=postgres

# Открываем порт вашего приложения
EXPOSE 8080

# Запускаем приложение
CMD ["java", "-jar", "app.jar"]