# Stage 1: Build frontend
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/ ./
RUN npm install && npm run build && ls -la dist/ && ls -la dist/assets/

# Stage 2: Build backend
FROM maven:3.9-eclipse-temurin-17-alpine AS backend-build
WORKDIR /app
COPY backend/pom.xml ./backend/pom.xml
COPY backend/src ./backend/src
RUN rm -rf ./backend/src/main/resources/static && mkdir -p ./backend/src/main/resources/static
COPY --from=frontend-build /app/frontend/dist/ ./backend/src/main/resources/static/
RUN ls -la ./backend/src/main/resources/static/ && ls -la ./backend/src/main/resources/static/assets/
RUN cd backend && mvn clean package -DskipTests

# Stage 3: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]