#!/bin/bash
# Full production build: builds React then packages Spring Boot JAR with static files embedded

echo "=== Building React frontend ==="
cd frontend
npm install
npm run build
cd ..

echo "=== Copying frontend build to Spring Boot static ==="
rm -rf backend/src/main/resources/static/*
cp -r frontend/dist/. backend/src/main/resources/static/

echo "=== Building Spring Boot JAR ==="
cd backend
mvn clean package -DskipTests
cd ..

echo "=== Build complete ==="
echo "Run: java -jar backend/target/*.jar"
