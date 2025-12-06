#!/bin/bash

# Render start script for Spring Boot application

echo "🚀 Starting VieGym Backend..."

# Find the JAR file
JAR_FILE=$(find target -name "*.jar" | grep -v "original" | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "❌ JAR file not found!"
    exit 1
fi

echo "📦 Starting application: $JAR_FILE"

# Start the Spring Boot application
exec java -jar \
    -Dspring.profiles.active=prod \
    -Xmx512m \
    -Xms256m \
    "$JAR_FILE"
