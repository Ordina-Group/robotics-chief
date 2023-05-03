FROM eclipse-temurin:17-jdk as builder

WORKDIR /app
COPY . /app

RUN /app/gradlew --no-daemon installShadowDist

FROM eclipse-temurin:17-jre

EXPOSE 3080:3080

WORKDIR /app

COPY --from=builder /app/build/install/robochief-shadow /app

CMD ["/app/bin/robochief"]
