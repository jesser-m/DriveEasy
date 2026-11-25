@echo off
REM Demarre toute la plateforme (Windows) - necessite JDK 21, Maven, Docker
docker compose up -d
call mvn -q clean package -DskipTests
start "service-registry" cmd /c java -jar service-registry\target\service-registry.jar
timeout /t 25 /nobreak
start "config-server" cmd /c java -jar config-server\target\config-server.jar
timeout /t 20 /nobreak
start "vehicule-service" cmd /c java -jar vehicule-service\target\vehicule-service.jar
start "client-service" cmd /c java -jar client-service\target\client-service.jar
start "location-service" cmd /c java -jar location-service\target\location-service.jar
timeout /t 30 /nobreak
start "api-gateway" cmd /c java -jar api-gateway\target\api-gateway.jar
echo Eureka http://localhost:8761 ^| Gateway http://localhost:8080 ^| Zipkin http://localhost:9411
