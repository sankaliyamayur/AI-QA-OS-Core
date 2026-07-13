# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY ai-qa-os-core/pom.xml ai-qa-os-core/
COPY ai-qa-os-agents-runtime/pom.xml ai-qa-os-agents-runtime/
# Pre-fetch dependencies
RUN mvn dependency:go-offline -pl ai-qa-os-agents-runtime -am -B
COPY . .
RUN mvn clean package -pl ai-qa-os-agents-runtime -am -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S qaosgroup && adduser -S qaosuser -G qaosgroup
USER qaosuser
WORKDIR /app
COPY --from=build --chown=qaosuser:qaosgroup /app/ai-qa-os-agents-runtime/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]