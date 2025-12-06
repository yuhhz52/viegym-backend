#!/bin/bash

# Render build script for Spring Boot application

echo "🔨 Starting build process..."

# Install Maven if not available (Render usually has it)
if ! command -v mvn &> /dev/null; then
    echo "Maven not found, installing..."
    curl -fsSL https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz -o maven.tar.gz
    tar -xzf maven.tar.gz
    export PATH=$PWD/apache-maven-3.9.9/bin:$PATH
fi

# Clean and build the application
echo "📦 Building application..."
./mvnw clean package -DskipTests

echo "✅ Build completed successfully!"
