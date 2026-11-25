# DriveEasy - Frontend (Angular 22)

Application web pour les agents d'agence de DriveEasy. Elle consomme l'API REST **uniquement via l'API Gateway** (`http://localhost:8080/api`), jamais directement les microservices.

## Prerequis
- Node.js `^22.22.3` ou `^24.15.0` (exigence d'Angular 22) et npm
- Le backend demarre (voir le README de `driveeasy-backend`) : Eureka, Config Server, 3 microservices, API Gateway sur le port 8080

## Installation et lancement
```bash
npm install
npm start          # ng serve -> http://localhost:4200
npm run build      # build de production dans dist/
```
L'URL de la gateway se change dans `src/app/core/api.ts`. La gateway autorise l'origine `http://localhost:4200` (CORS).

## Architecture
```
src/app/
  app.ts / app.config.ts / app.routes.ts   shell (menu), providers (HttpClient + interceptors), routes lazy-loaded
  core/
    api.ts                    URL de la gateway
    models/models.ts          interfaces TypeScript (miroir des DTO du backend)
    services/                 VehiculeService, ClientService, LocationService, ToastService, LoadingService
    interceptors/             loadingInterceptor (barre de chargement), errorInterceptor (ApiError -> notification)
    guards/                   unsavedChangesGuard (canDeactivate), retourAllowedGuard (canActivate)
    utils/date-utils.ts
  shared/                     Pagination, StatusBadge, ToastContainer
  pages/
    dashboard/ vehicules/ clients/ locations/
```
Standalone components, signaux, nouveau control flow (`@if`, `@for`), formulaires reactifs, aucune dependance de charts (graphiques en CSS).

## Ecrans
| Ecran | Route |
|---|---|
| Tableau de bord | `/dashboard` |
| Catalogue des vehicules (filtres, tri, pagination) | `/vehicules` |
| Formulaire vehicule | `/vehicules/new`, `/vehicules/:id/edit` |
| Liste des clients (recherche, badge, alerte permis) | `/clients` |
| Fiche client (eligibilite, historique) | `/clients/:id` |
| Nouvelle location (assistant en 4 etapes) | `/locations/new` |
| Liste des locations (depart / retour / annulation) | `/locations` |
| Retour de vehicule (penalites, recapitulatif imprimable) | `/locations/:id/retour` |

Captures d'ecran : dossier `docs/screenshots/` (generees avec un backend simule).
