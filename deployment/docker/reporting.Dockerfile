# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
# MNT-2: copy the whole reactor before building.
#
# The previous version copied only the root, core and this module's pom and then ran
# `mvn dependency:go-offline -pl ai-qa-os-reporting -am`. `-am` makes Maven read EVERY module
# declared in the root pom to construct the reactor, so with 22 modules and 3 poms present it
# failed with ProjectBuildingException — the docker build has been broken on main, and any fix
# that re-lists poms rots again the moment a module is added (which is how this broke).
COPY . .
RUN mvn clean package -pl ai-qa-os-reporting -am -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
# MNT-2: patch OS packages in the runtime layer before dropping privileges. Trivy gates the
# publish on CRITICAL/HIGH with `ignore-unfixed: true`, so what it reports are findings that
# HAVE fixes in Alpine — upgrading is the actual remedy, not a suppression.
RUN apk --no-cache upgrade \
 && addgroup -S qaosgroup \
 && adduser -S qaosuser -G qaosgroup
USER qaosuser
WORKDIR /app
COPY --from=build --chown=qaosuser:qaosgroup /app/ai-qa-os-reporting/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]