#!/usr/bin/env bash
# Demarre toute la plateforme dans l'ordre recommande (Linux / macOS / Git Bash)
set -e
mkdir -p logs
docker compose up -d
mvn -q clean package -DskipTests

wait_for() { echo -n "Attente de $1 (port $2) "; until curl -sf "http://localhost:$2/actuator/health" >/dev/null; do echo -n "."; sleep 2; done; echo " OK"; }
run() { java -jar "$1/target/$1.jar" > "logs/$1.log" 2>&1 & }

run service-registry; wait_for service-registry 8761
run config-server;    wait_for config-server 8888
run vehicule-service; run client-service; run location-service
wait_for vehicule-service 8081; wait_for client-service 8082; wait_for location-service 8083
run api-gateway;      wait_for api-gateway 8080
echo "Tout est demarre -> Eureka http://localhost:8761 | Gateway http://localhost:8080 | Zipkin http://localhost:9411"
