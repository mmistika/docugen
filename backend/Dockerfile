FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies --no-daemon

COPY . .
RUN --mount=type=cache,target=/root/.gradle ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN apk add --no-cache \
    curl \
    chromium-headless-shell \
    nss \
    freetype \
    harfbuzz \
    fontconfig \
    font-inter \
    font-liberation \
    font-dejavu \
    ttf-freefont \
    font-noto-emoji \
    nodejs

COPY --from=build /app/build/libs/*.jar app.jar

ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
ENV PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH=/usr/bin/chromium-headless-shell
ENV PLAYWRIGHT_NODEJS_PATH=/usr/bin/node

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
