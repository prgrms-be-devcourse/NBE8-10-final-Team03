# syntax=docker/dockerfile:1.7

# =========================
# 1️⃣ Build Stage (Java 21 JDK 사용)
# =========================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# 의존성 캐시 레이어 (소스 변경 시에도 캐시 유지)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src src

# 테스트 생략(-x test)하고 가볍게 JAR 빌드만 수행
RUN ./gradlew clean build -x test --no-daemon

# =========================
# 2️⃣ Layer Extract Stage
# =========================
FROM eclipse-temurin:21-jdk AS extractor

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Spring Boot layered jar 추출
RUN java -Djarmode=layertools -jar app.jar extract

# =========================
# 3️⃣ Runtime Stage (가벼운 Java 21 JRE 사용)
# =========================
FROM eclipse-temurin:21-jre

ENV LANG=ko_KR.UTF-8
ENV LC_ALL=ko_KR.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8"

WORKDIR /app

# Spring Boot layers (변경 빈도 낮은 순 → 높은 순으로 배치)
COPY --from=extractor /app/dependencies/ ./
COPY --from=extractor /app/spring-boot-loader/ ./
COPY --from=extractor /app/snapshot-dependencies/ ./
COPY --from=extractor /app/application/ ./

# ── t3.micro JVM 최적화 ──
ENTRYPOINT ["java", \
  "-XX:+UseSerialGC", \
  "-XX:MaxRAMPercentage=55", \
  "-XX:InitialRAMPercentage=25", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-XX:+AlwaysPreTouch", \
  "org.springframework.boot.loader.launch.JarLauncher"]