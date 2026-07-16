# 1. Build the frontend app
FROM node:22.17.0-alpine AS frontend-build
WORKDIR /app/frontend

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend .
RUN npm run build

# 2. Copy the frontend dist into the backend's static resources, then build the backend
FROM eclipse-temurin:25-jdk AS backend-build
WORKDIR /app/backend

COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY backend/src src
COPY --from=frontend-build /app/frontend/dist src/main/resources/static

# 3. Build the backend
RUN ./mvnw package -DskipTests -B

# 4. Create the final runtime image
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]