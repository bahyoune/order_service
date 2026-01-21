# ----------- BUILD IMAGE -----------
FROM eclipse-temurin:21-jre-alpine

# Create app directory
WORKDIR /app

# Copy jar
COPY target/OrderService-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8082

# Run app
ENTRYPOINT ["java","-jar","app.jar"]

