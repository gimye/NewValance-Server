FROM eclipse-temurin:17-jdk
ARG JAR_FILE=build/libs/*.jar

# 모든 설정 파일 복사
COPY src/main/resources/application*.yml /app/config/
COPY src/main/resources/application-API-key.properties /app/config/

# JAR 복사
COPY ${JAR_FILE} app.jar

# 환경 변수 설정
ENV SPRING_CONFIG_LOCATION=/app/config/
ENV SPRING_PROFILES_ACTIVE=prod

# ✅ 실행 명령 수정 (포트 설정 제거)
ENTRYPOINT ["java", "-Dspring.config.location=/app/config/", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]
