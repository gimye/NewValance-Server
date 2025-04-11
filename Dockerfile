FROM eclipse-temurin:17-jdk
ARG JAR_FILE=build/libs/*.jar

# 설정 파일 복사
COPY src/main/resources/application.yml /app/config/
COPY src/main/resources/application-prod.yml /app/config/
COPY src/main/resources/application-API-key.properties /app/config/

# 빌드된 JAR 복사
COPY ${JAR_FILE} app.jar

# 실행 명령: prod 프로파일 + 설정 파일 경로 지정
ENTRYPOINT ["java", "-Dspring.config.location=/app/config/", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]
