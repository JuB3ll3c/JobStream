# Tech Context - Technology Stack

> Last updated: 2026-03-26

## Stack

| Layer | Technology | Version |
|-------|------------|---------|
| Frontend | Angular | 21 |
| Language | TypeScript | 5.x |
| State | Angular Signals | - |
| UI | Stitch (MCP) | - |
| Testing | Vitest | - |

| Layer | Technology | Version |
|-------|------------|---------|
| Backend | Java | 21 |
| Framework | Spring Boot | 3.x |
| Messaging | Apache Kafka | 3.x |
| Database | PostgreSQL | 15+ |
| Real-time | Server-Sent Events | - |
| Build | Maven | - |

| Layer | Technology | Version |
|-------|------------|---------|
| Container | Docker | - |
| CI/CD | GitHub Actions | - |

## API-First Approach

**`openapi.yml` est la source de vérité** pour les contrats API entre Backend et Frontend.

| Fichier | Description |
|---------|-------------|
| `openapi/openapi.yml` | Spécification OpenAPI 3.0 |

### Génération des DTOs

- **Backend**: openapi-generator-maven-plugin
- **Frontend**: openapi-generator-cli (TypeScript client)

⚠️ **NE JAMAIS créer de DTOs manuellement**.

## Project Structure

```
job-stream-opencode/
├── codebase/
│   ├── frontend/              # Angular 21 app
│   ├── dashboard-service/     # Spring Boot - REST API + Kafka
│   └── ia-analyzer-service/   # Spring Boot - Kafka Consumer
├── openapi/
│   └── openapi.yml            # Source de vérité
└── docker/
```

## Services

### Dashboard Service (Port 8080)
- REST API (recherche, sauvegarde jobs)
- Kafka Producer → `job-offers-raw`
- Kafka Consumer ← `job-offers-analyzed`
- SSE pour updates temps réel
- **DTOs générés depuis OpenAPI**

### IA Analyzer Service (Port 8081)
- Kafka Consumer ← `job-offers-raw`
- Analyse (classification, scoring, tags)
- Kafka Producer → `job-offers-analyzed`

## Kafka Topics

| Topic | Partitions | Usage |
|-------|------------|-------|
| `job-offers-raw` | 3 | Jobs sauvegardés en attente d'analyse |
| `job-offers-analyzed` | 3 | Jobs analysés par l'IA |
| `job-offers-dlt` | 1 | Dead Letter Topic |

## Endpoints (définis dans OpenAPI)

| Méthode | Path | Description |
|---------|------|-------------|
| `GET` | `/api/jobs/search?q=` | Recherche via JSearch |
| `GET` | `/api/jobs` | Liste jobs sauvegardés |
| `POST` | `/api/jobs/{jobId}/save` | Sauvegarder un job |
| `GET` | `/api/jobs/{jobId}` | Détail d'un job |
| `DELETE` | `/api/jobs/{jobId}` | Supprimer un job |
| `GET` | `/api/jobs/stream` | Flux SSE |
| `GET` | `/api/health` | Health check |

## Dependencies

### Frontend
- @angular/core: ^21.0.0
- @angular/material
- rxjs
- @angular/forms

### Backend
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-kafka
- postgresql driver
- openapi-generator-maven-plugin

## External API

| API | Provider | Endpoint |
|-----|----------|----------|
| JSearch | RapidAPI | `https://jsearch.p.rapidapi.com/search` |

Requires: `RAPIDAPI_KEY` environment variable

## Tools

- OpenCode (AI coding agent)
- Serena (code understanding MCP)
- Prettier (formatting)
- ESLint (linting)

## Coding Standards

- **API-First**: OpenAPI comme source de vérité
- DTOs: Generated from OpenAPI (NEVER create manually)
- Kafka Producer: `acks=all`, idempotence enabled
- Angular: Use `inject()` and Signals
- Tests: Testcontainers for integration tests