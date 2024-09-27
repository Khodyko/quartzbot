# Use the base image with JDK 21
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file of your application into the container
COPY quartzbot-0.0.1-SNAPSHOT.jar app.jar

# Update package lists, install tzdata and curl
RUN apt-get update && \
    apt-get install -y tzdata curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set the timezone environment variable
ENV TZ="Europe/Minsk"

# Optionally, configure tzdata
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Open the port for your application
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]