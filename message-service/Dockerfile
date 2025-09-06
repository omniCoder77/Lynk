FROM eclipse-temurin:21-jdk AS builder

ARG GRADLE_USER_HOME=/root/.gradle
ENV GRADLE_USER_HOME=$GRADLE_USER_HOME

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle

RUN --mount=type=cache,target=${GRADLE_USER_HOME} \
    --mount=type=cache,target=${GRADLE_USER_HOME}/wrapper \
    chmod +x ./gradlew && ./gradlew --version --no-daemon

COPY settings.gradle.kts ./
COPY *.lockfile ./
COPY build.gradle.kts ./

RUN --mount=type=cache,target=${GRADLE_USER_HOME} \
    --mount=type=cache,target=${GRADLE_USER_HOME}/wrapper \
    ./gradlew dependencies --no-daemon --console=plain

COPY src ./src

RUN --mount=type=cache,target=${GRADLE_USER_HOME} \
    --mount=type=cache,target=${GRADLE_USER_HOME}/wrapper \
    ./gradlew build --no-daemon -x test --console=plain

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app
COPY keystore.jks ./

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]