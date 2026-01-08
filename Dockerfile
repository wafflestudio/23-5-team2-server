# 1. 베이스 이미지 선택 (경량화된 Java 17 이미지)
FROM docker.io/eclipse-temurin:17-alpine
# 2. 작업 디렉토리 설정 (컨테이너 내부의 /app 디렉토리)
WORKDIR /app
# 3. 현재 디렉토리를 내부의 /app 디렉토리로 복사
COPY . /app
# 4. jar 파일 생성
RUN ./gradlew bootJar
# 5. 포트 노출 (컨테이너가 8080 포트를 사용함을 명시)
EXPOSE 8080
# 6. 실행 명령어 (컨테이너 시작 시 실행될 명령어)
ENTRYPOINT exec java $JAVA_OPTS -jar build/libs/team2-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
