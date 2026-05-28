# Architecture JobStream

> Vue d'ensemble technique - Mars 2026

## 1. Vue d'Ensemble

JobStream est une plateforme de recherche d'emploi avec une architecture **event-driven** basée sur Kafka et une approche **API-First**.

```mermaid
flowchart TB
    subgraph Frontend
        F[Angular 21<br/>Signals]
    end

    subgraph Backend
        D[Dashboard Service<br/>8080]
        I[IA Analyzer<br/>8081]
    end

    subgraph Data
        K[Kafka<br/>Topics]
        P[PostgreSQL]
        J[JSearch API<br/>RapidAPI]
    end

    O[openapi.yml<br/>Source de vérité]

    O -.->|DTO generation| F
    O -.->|DTO generation| D

    F <--> D
    D <--> J
    D <--> K
    K <--> I
    I <--> K
    D <--> P
    D -.->|SSE| F
```

---

## 2. Approche API-First

### Principe

**`openapi.yml` est le fichier central** entre Backend et Frontend.

| Action | Outil |
|--------|-------|
| Définir les endpoints | `openapi.yml` |
| Définir les DTOs | `openapi.yml` |
| Générer DTOs Backend | openapi-generator-maven-plugin |
| Générer Client Frontend | openapi-generator-cli |

⚠️ **NE JAMAIS créer de DTOs manuellement**.

### Structure

```
openapi/
└── openapi.yml    # Source de vérité
```

### Endpoints définis

| Méthode | Path | Description |
|---------|------|-------------|
| `GET` | `/api/jobs/search?q=` | Recherche via JSearch |
| `GET` | `/api/jobs` | Liste jobs sauvegardés |
| `POST` | `/api/jobs/{jobId}/save` | Sauvegarder un job |
| `GET` | `/api/jobs/{jobId}` | Détail d'un job |
| `DELETE` | `/api/jobs/{jobId}` | Supprimer un job |
| `GET` | `/api/jobs/stream` | Flux SSE |
| `GET` | `/api/health` | Health check |

### DTOs définis

- `JobDto` - Job brut de l'API
- `SavedJobDto` - Job sauvegardé avec analyse
- `JobAnalysisDto` - Résultat analyse IA
- `JobSearchResponse` - Réponse de recherche
- `JobStatus` - Énumération des statuts

---

## 3. Stack Technique

| Layer | Technology |
|-------|------------|
| Frontend | Angular 21, Signals |
| Backend | Java 21, Spring Boot 3.x |
| Messaging | Apache Kafka 3.x |
| Database | PostgreSQL 15+ |
| Real-time | Server-Sent Events |
| **API Contracts** | **OpenAPI 3.0** (source de vérité) |

---

## 4. Services

### Dashboard Service (8080)
- API REST pour recherche et sauvegarde
- Kafka Producer → `job-offers-raw`
- Kafka Consumer ← `job-offers-analyzed`
- SSE pour updates temps réel
- Génère ses DTOs depuis OpenAPI

### IA Analyzer Service (8081)
- Kafka Consumer ← `job-offers-raw`
- Analyse des jobs (classification, scoring)
- Kafka Producer → `job-offers-analyzed`

---

## 5. Workflow (Smart Cost)

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant D as Dashboard
    participant K as Kafka
    participant I as IA Analyzer
    participant J as JSearch

    U->>F: Recherche job
    F->>D: GET /api/jobs/search?q=...
    D->>J: Appelle API JSearch
    J-->>D: Jobs bruts
    D-->>F: DISCOVERED
    F->>U: Affiche résultats

    U->>F: "Save" sur un job
    F->>D: POST /api/jobs/{id}/save
    D->>P: Persiste (SAVED)
    D->>K: Produce → job-offers-raw

    K->>I: Consomme job-offers-raw
    I->>I: Analyse (score, tags)
    I->>K: Produce → job-offers-analyzed

    K->>D: Consomme job-offers-analyzed
    D->>P: Update analyse
    D-.->F: SSE update
    F->>U: Mise à jour temps réel
```

---

## 6. Topics Kafka

```mermaid
flowchart LR
    subgraph Cluster
        R[job-offers-raw]
        A[job-offers-analyzed]
        DLT[job-offers-dlt]
    end

    D --> R
    R -->|retry| R
    R -->|fail| DLT
    I --> A
    A --> D
```

| Topic | Partitions | Usage |
|-------|------------|-------|
| `job-offers-raw` | 3 | Jobs sauvegardés |
| `job-offers-analyzed` | 3 | Jobs analysés |
| `job-offers-dlt` | 1 | Dead Letter |

---

## 7. États des Jobs

```mermaid
stateDiagram-v2
    [*] --> DISCOVERED
    DISCOVERED --> SAVED
    SAVED --> ANALYZING
    ANALYZING --> ANALYZED
    ANALYZING -->|: retry| ANALYZING
    ANALYZING -->|: fail| DLT
```

| Status | Description |
|--------|-------------|
| `DISCOVERED` | Résultat API, non sauvegardé |
| `SAVED` | Sauvegardé par l'utilisateur |
| `ANALYZING` | En cours d'analyse IA |
| `ANALYZED` | Analyse terminée |

---

## 8. Base de Données

```mermaid
erDiagram
    users ||--o{ jobs : saves
    jobs ||--o| job_analysis : has

    users {
        uuid id PK
        string email
        string name
        timestamp created_at
    }

    jobs {
        uuid id PK
        string external_id
        string title
        string company
        text description
        string location
        int salary_min
        int salary_max
        string status
        timestamp saved_at
        timestamp created_at
        uuid user_id FK
    }

    job_analysis {
        uuid id PK
        uuid job_id FK
        int score
        jsonb tags
        text summary
        timestamp analyzed_at
    }
```

---

## 9. API Externe

| API | Provider | Endpoint |
|-----|----------|----------|
| JSearch | RapidAPI | `https://jsearch.p.rapidapi.com/search` |

Config: variable d'environnement `RAPIDAPI_KEY`

---

## 10. Définition de "Done"

- [ ] Code respecte contrat OpenAPI (depuis `openapi.yml`)
- [ ] Tests intégration Testcontainers passent
- [ ] Documentation Swagger à jour