# DriveEasy - Backend (microservices Spring Boot / Spring Cloud)

Plateforme de location de vehicules - Licence Informatique et Multimedia (LIM3 B/C).
Architecture : Eureka, Config Server, 3 microservices metier, API Gateway, Zipkin. Donnees **en RAM uniquement** (aucune base de donnees).

## Prerequis
JDK 21, Maven 3.9+, Docker (pour Zipkin), curl (pour `start-all.sh`).

## Composants et ports
| Composant | Port | Role |
|---|---|---|
| service-registry (Eureka) | 8761 | Annuaire des services |
| config-server | 8888 | Configuration centralisee (dossier `config-server/src/main/resources/config-repo`) |
| vehicule-service | 8081 | Parc automobile |
| client-service | 8082 | Clients, eligibilite |
| location-service | 8083 | Cycle de vie des locations, prix, statistiques |
| api-gateway | 8080 | Point d'entree unique (`/api/vehicules/**`, `/api/clients/**`, `/api/locations/**`) |
| Zipkin (Docker) | 9411 | Tracage distribue |

## Lancement pas a pas
Option rapide : `./start-all.sh` (Linux/macOS/Git Bash) ou `start-all.bat` (Windows).

Option manuelle (ordre obligatoire) :
```bash
docker compose up -d                       # Zipkin
mvn clean package -DskipTests              # compile les 6 modules
java -jar service-registry/target/service-registry.jar    # 1. attendre http://localhost:8761
java -jar config-server/target/config-server.jar          # 2. attendre le demarrage (port 8888)
java -jar vehicule-service/target/vehicule-service.jar    # 3. (en parallele)
java -jar client-service/target/client-service.jar
java -jar location-service/target/location-service.jar
java -jar api-gateway/target/api-gateway.jar              # 4.
```
Verification : http://localhost:8761 (tous les services en UP), puis `requests.http` ou Postman sur `http://localhost:8080/api/...`.
Tests unitaires : `mvn test`.

## Config Server : dossier local ou depot GitHub
Par defaut le Config Server lit `config-repo/` (profil `native`). Pour externaliser sur GitHub :
1. Creer un depot `driveeasy-config` et y copier le contenu de `config-server/src/main/resources/config-repo/`.
2. Lancer le Config Server avec `CONFIG_PROFILE=git CONFIG_GIT_URI=https://github.com/<compte>/driveeasy-config`.

Fichiers : `application.yml` (Eureka, Actuator, Zipkin communs), `application-dev.yml` (echantillonnage 1.0), `application-prod.yml` (0.1), puis un fichier par service.
Profil actif par defaut : `dev` (changer avec `SPRING_PROFILES_ACTIVE=prod`).

## Jeux de donnees (RAM, recharges a chaque demarrage)
- 8 vehicules (id 1-8, le 8 est en MAINTENANCE).
- 5 clients : id 1 STANDARD, id 2 FIDELE, id 3 VIP, id 4 trop jeune (non eligible), id 5 permis expire (non eligible).

## Choix de conception
- Controller -> Repository pour vehicule-service et client-service ; location-service ajoute une couche `service` pour l'orchestration.
- Prix = jours x prix journalier x **coefficient de la categorie du vehicule** (ECONOMIQUE 1.0, BERLINE 1.2, SUV 1.3, LUXE 1.5, configurable) - remise fidelite (FIDELE 5 %, VIP 10 %). Penalite = 1,5 x prix journalier x jours de retard.
- Coherence : les verifications (eligibilite, disponibilite, chevauchement) precedent toute ecriture ; au depart et au retour, une compensation remet le vehicule dans son etat precedent si l'operation echoue.
- Si un service distant est indisponible, l'API repond **503** (en plus des codes 200, 201, 204, 400, 404, 409, 500).
- `DELETE /clients/{id}` interroge location-service (`GET /locations/clients/{id}/actives/count`) pour refuser la suppression d'un client ayant une location active.
- Endpoint supplementaire `GET /locations/disponibilites?dateDebut&dateFin` : vehicules libres (statut + absence de reservation qui chevauche), utile pour l'assistant "Nouvelle location".
- Filtre personnalise de la gateway : `X-Gateway-Request-Id` + journal (methode, chemin, statut, duree).
- Tracage : Micrometer Tracing (Brave) + Zipkin dans tous les services et la gateway. Une requete `GET /api/locations/{id}` ou `POST /api/locations` traverse gateway -> location-service -> client-service / vehicule-service.

## Captures pour le rapport
Eureka (http://localhost:8761), Postman (requests.http), Zipkin (http://localhost:9411 -> "Run Query" apres quelques requetes).
