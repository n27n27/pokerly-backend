# Pokerly Backend

- Spring Boot 3.5.7 / Java 17 / Gradle
- DB: MariaDB (Docker compose)
- JPA: ddl-auto=validate (스키마는 SQL/Flyway로 관리)

## Dev

1. Docker Desktop 실행
2. `docker compose up -d`
3. `./gradlew bootRun`
4. http://localhost:8080/actuator/health 로 확인

## Production (EC2)

- 위치: `/srv/pokerly`
- 구조:
  - `/srv/pokerly/backend` (git clone)
  - `/srv/pokerly/frontend` (프런트 빌드 결과)
  - `/srv/pokerly/docker-compose.yml`
  - `/srv/pokerly/.env`
  - `/srv/pokerly/deploy_backend.sh`

### 배포 방법

1. `ssh ubuntu@pokerly.kr`
2. `cd /srv/pokerly`
3. `./deploy_backend.sh` (git pull → 이미지 빌드 → 컨테이너 재시작 자동)

## Docker Compose (요약)

- MariaDB
- Backend (8080 → host 9000)

## Nginx (요약)

- 정적 파일: `/srv/pokerly/frontend`
- API 프록시: `/api → http://127.0.0.1:9000`

## Useful commands

- `docker compose ps`
- `docker compose logs backend --tail=100`
- `docker compose restart backend`
