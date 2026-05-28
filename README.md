# JobStream — Event-Driven Job Search Platform

> A modern, event-driven job search platform built with Java 25, Spring Boot 4.0, Angular 21, and Apache Kafka.

**JobStream** is a full-stack application that aggregates job listings from the Adzuna API, allows users to search, save, and manage job offers, and enriches them with AI-powered analysis through OpenRouter. The platform uses an **API-First** approach with **OpenAPI 3.0** as the single source of truth for all service contracts, and an **event-driven architecture** powered by a 3-node Apache Kafka cluster for asynchronous, resilient communication between microservices.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Quick Start with Docker](#quick-start-with-docker)
  - [Manual Setup](#manual-setup)
  - [Environment Variables](#environment-variables)
- [API Documentation](#api-documentation)
- [Data Flow](#data-flow)
- [Infrastructure Options](#infrastructure-options)
- [Monitoring](#monitoring)
- [Contributing](#contributing)
- [OpenCode AI-Assisted Development](#opencode-ai-assisted-development)
- [License](#license)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           JOBSTREAM SYSTEM                              │
└─────────────────────────────────────────────────────────────────────────┘

   ┌──────────┐      ┌───────────────┐      ┌───────────────────┐
   │ Frontend │      │  Dashboard    │      │  External APIs    │
   │ Angular  │      │  Service      │      │  (Adzuna)         │
   │ 21       │      │  :8080        │      │                   │
   └────┬─────┘      └──────┬────────┘      └───────────────────┘
        │                   │                        
        │  REST/SSE         │  REST API calls        
        │◄─────────────────►│◄──────────────────────►
        │                   │                        
        │                   │     ┌──────────────────┐
        │                   │     │   PostgreSQL     │
        │                   │────►│   :5432          │
        │                   │     └──────────────────┘
        │                   │                        
        │                   │     ┌──────────────────┐
        │                   │────►│   Kafka Cluster  │
        │                   │     │  3 KRaft Nodes   │
        │                   │     └────────┬─────────┘
        │                   │              │
        │                   │              │
        │                   │     ┌────────▼─────────┐
        │                   │     │  AI Analyzer     │
        │                   │◄────│  Service         │
        │                   │     │  OpenRouter AI   │
        │                   │     └──────────────────┘
        │                   │                        
        │  Real-time SSE    │                        
        │◄─────────────────│                         
        │                   │                        
   ┌────┴─────┐      ┌──────┴────────┐
   │ Grafana  │      │  Kafka UI     │
   │ :3000    │      │  :8085        │
   └──────────┘      └───────────────┘
```

### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Event-Driven (Kafka)** | Asynchronous decoupling of services; enables independent scaling of dashboard and AI analyzer. 3-node KRaft cluster provides fault tolerance. |
| **API-First (OpenAPI 3.0)** | Single source of truth for contracts. DTOs and client code are auto-generated, eliminating manual synchronization errors. |
| **SSE for Real-Time** | Lightweight alternative to WebSockets. Dashboard pushes AI analysis results to the frontend as they complete. |
| **Two Kafka Topics** | Clear separation: raw saved jobs (`job-to-analyzed`) vs. analyzed results (`analyzed-jobs`). Enables independent consumer groups and retry policies. |
| **No API Gateway** | Direct frontend-to-backend communication keeps the stack simple for a single-application deployment. |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | Angular 21 (Signals, Standalone Components), TailwindCSS 4, TypeScript 5.9, Vite |
| **Backend — Dashboard** | Java 25, Spring Boot 4.0, Spring Data JPA, Spring Kafka |
| **Backend — AI Analyzer** | Java 25, Spring Boot 4.0, Spring AI (OpenRouter), Spring Kafka |
| **Messaging** | Apache Kafka 7.6 (Confluent), 3-node KRaft cluster |
| **Database** | PostgreSQL 18 (Alpine) |
| **Real-Time** | Server-Sent Events (SSE) |
| **API Contracts** | OpenAPI 3.0 (source of truth), openapi-generator |
| **Monitoring** | Prometheus + Grafana, Kafka UI, AKHQ |
| **Infrastructure** | Docker Compose, Vagrant + Ansible |

---

## Project Structure

```
job-stream-opencode/
├── codebase/
│   ├── dashboard-service/        # Main REST API + Kafka producer/consumer
│   │   ├── src/main/java/        # Java 25 / Spring Boot 4.0 source
│   │   ├── src/main/resources/   # application.yml configuration
│   │   └── Dockerfile            # Multi-stage Maven + JRE build
│   │
│   ├── ai-analyzer-service/      # AI analysis service
│   │   ├── src/main/java/        # Kafka consumer + OpenRouter AI
│   │   ├── src/main/resources/   # application.yaml configuration
│   │   └── Dockerfile            # Multi-stage Maven + JRE build
│   │
│   ├── jobstream-frontend/       # Angular 21 SPA
│   │   ├── src/                  # Components, services, config
│   │   ├── angular.json          # Angular CLI config
│   │   └── Dockerfile            # Node build + Nginx runtime
│   │
│   └── openapi/
│       └── openapi.yml           # OpenAPI 3.0 specification (source of truth)
│
├── infra/
│   ├── local_with_docker/        # Docker Compose (recommended for dev)
│   │   ├── docker-compose.yml    # All services + monitoring stack
│   │   ├── .env.example          # Environment variable template
│   │   ├── prometheus.yml        # Metrics scraping config
│   │   └── grafana/              # Pre-configured dashboards
│   │
│   ├── local_with_vagrant_ansible/  # VM-based deployment
│   │   └── Vagrantfile             # Multi-VM setup (Kafka ×3, app, DB)
│   │
│   └── ansible/                    # Ansible automation
│       ├── playbooks/playbook.yml  # Main playbook
│       └── roles/                  # Roles: common, kafka, postgres, backend
│
├── docs/
│   ├── memory-bank/             # Project context & decisions
│   │   ├── projectbrief.md
│   │   ├── techContext.md
│   │   ├── systemPatterns.md
│   │   └── activeContext.md
│   ├── architecture/
│   │   └── overview.md          # Detailed architecture documentation
│   ├── adr/                     # Architecture Decision Records
│   │   ├── ADR-001-api-first-openapi.md
│   │   ├── ADR-002-event-driven-kafka.md
│   │   └── ... 11 ADRs total
│   └── user-stories/            # BDD feature files & tasks
│       ├── features/            # 10 Gherkin feature files
│       └── tasks/               # Breakdown by user story
│
└── opencode.jsonc               # OpenCode AI agent configuration
```

---

## Getting Started

### Prerequisites

- **Docker & Docker Compose** (recommended)
- **Java 25** (for local backend development)
- **Node.js 24** (for local frontend development)
- **Maven 3.9+** (build tool)
- **Angular CLI 21** (`npm install -g @angular/cli`)

### Quick Start with Docker

The fastest way to run the entire platform:

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd job-stream-opencode

# 2. Configure environment variables
cp infra/local_with_docker/.env.example infra/local_with_docker/.env
# Edit infra/local_with_docker/.env with your API keys (see below)

# 3. Start all services
cd infra/local_with_docker
docker compose up -d

# 4. Verify everything is running
docker compose ps

# 5. Access the application
# Frontend:   http://localhost
# Backend:    http://localhost:8080/api/health
# Kafka UI:   http://localhost:8085
# AKHQ:       http://localhost:8086
# Grafana:    http://localhost:3000 (admin / admin)
# Prometheus: http://localhost:9090
```

To stop:
```bash
docker compose down
```

### Manual Setup (Local Development)

#### Backend — Dashboard Service

```bash
cd codebase/dashboard-service

# Build
./mvnw clean package

# Run (requires PostgreSQL & Kafka running)
./mvnw spring-boot:run

# Run tests
./mvnw test
```

#### Backend — AI Analyzer Service

```bash
cd codebase/ai-analyzer-service

# Build
./mvnw clean package

# Run (requires Kafka running)
./mvnw spring-boot:run

# Run tests
./mvnw test
```

#### Frontend

```bash
cd codebase/jobstream-frontend

# Install dependencies
npm install

# Start development server (proxies API to :8080)
npm start

# Run tests
npm test

# Build for production
npm run build
```

#### OpenAPI Client Generation

After modifying `codebase/openapi/openapi.yml`, regenerate DTOs:

```bash
# Backend (Maven)
cd codebase/dashboard-service && ./mvnw clean compile

# Frontend
cd codebase/jobstream-frontend && npm run generate-api
```

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `DB_USER` | PostgreSQL user | Yes |
| `DB_PASSWORD` | PostgreSQL password | Yes |
| `DB_NAME` | PostgreSQL database name | Yes |
| `DB_PORT` | PostgreSQL internal port (default: 5432) | No |
| `DB_EXTERNAL_PORT` | PostgreSQL external port (default: 5433) | No |
| `ADZUNA_APP_ID` | Adzuna API application ID | Yes (for Adzuna) |
| `ADZUNA_APP_KEY` | Adzuna API application key | Yes (for Adzuna) |
| `ADZUNA_DEFAULT_COUNTRY` | Default Adzuna country code (default: ch) | No |
| `OPEN_ROUTER_API_KEY` | OpenRouter API key for AI analysis | Yes (for AI) |
| `AI_MODEL` | AI model (default: deepseek/deepseek-v3.2) | No |
| `SPRING_DATASOURCE_URL` | JDBC URL for PostgreSQL | Yes (Docker) |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | Yes (Docker) |
| `GRAFANA_ADMIN_USER` | Grafana admin username (default: admin) | No |
| `GRAFANA_ADMIN_PASSWORD` | Grafana admin password | Yes |

#### Getting API Keys

- **Adzuna**: Register at [developer.adzuna.com](https://developer.adzuna.com/) (free tier available)
- **OpenRouter**: Create an account at [openrouter.ai](https://openrouter.ai/) and generate an API key

---

## API Documentation

### Key Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/jobs/search?q={query}` | Search jobs via external APIs |
| `GET` | `/api/jobs/{externalId}` | Get external job details |
| `GET` | `/api/saved-jobs` | List all saved jobs |
| `POST` | `/api/saved-jobs` | Save a job |
| `GET` | `/api/saved-jobs/{id}` | Get saved job with analysis |
| `DELETE` | `/api/saved-jobs/{id}` | Delete a saved job |
| `POST` | `/api/saved-jobs/{id}/analyze` | Trigger AI analysis |
| `GET` | `/api/jobs/stream` | SSE real-time event stream |
| `GET` | `/api/health` | Health check |

The full OpenAPI specification is available at `codebase/openapi/openapi.yml`.

---

## Data Flow

### Job Search & AI Analysis Pipeline

```
1. SEARCH ──── User searches → Frontend calls Dashboard API
                  ↓
2. DISPLAY ─── Dashboard fetches from Adzuna API
                  ↓
3. SAVE ────── User saves a job → Persisted in PostgreSQL (status: SAVED)
                  ↓
4. PRODUCE ─── Dashboard publishes to Kafka topic `job-to-analyzed`
                  ↓
5. ANALYZE ─── AI Analyzer Service consumes → Calls OpenRouter AI
                  ↓
6. PRODUCE ─── AI Analyzer publishes result to Kafka topic `analyzed-jobs`
                  ↓
7. UPDATE ──── Dashboard consumes → Updates PostgreSQL → Pushes SSE to frontend
```

### Job Lifecycle States

```
 ┌──────────┐    ┌─────────┐    ┌────────────┐    ┌──────────┐
 │DISCOVERED│───►│  SAVED  │───►│ ANALYZING  │───►│ ANALYZED │
 └──────────┘    └─────────┘    └─────┬──────┘    └──────────┘
                                      │
                                      │ (retry)
                                      ▼
                                 ┌─────────┐
                                 │   DLT   │  (dead letter topic after max retries)
                                 └─────────┘
```

### Kafka Topics

| Topic | Partitions | Description |
|-------|-----------|-------------|
| `job-to-analyzed` | 3 | Raw saved jobs awaiting AI analysis |
| `analyzed-jobs` | 3 | Job offers enriched with AI analysis |
| `jobstream-dlt` | 3 | Dead letter topic for failed messages |

---

## Infrastructure Options

### 1. Docker Compose (Recommended for Development)

Single command brings up the entire stack with monitoring.

```bash
cd infra/local_with_docker
docker compose up -d
```

Services included:
- PostgreSQL 18
- Kafka 3-node KRaft cluster (no ZooKeeper dependency)
- Dashboard Service (Spring Boot)
- AI Analyzer Service (Spring Boot)
- Frontend (Nginx + Angular)
- Kafka UI + AKHQ (message inspection)
- Prometheus + Grafana (metrics & dashboards)

### 2. Vagrant + Ansible (Multi-VM)

For a more production-like environment with separate VMs:

```bash
cd infra/local_with_vagrant_ansible
vagrant up
```

This provisions 5 VMs:
- `broker1`, `broker2`, `broker3` — Kafka KRaft nodes
- `app-server` — Backend + Frontend
- `db-server` — PostgreSQL

---

## Monitoring

The platform includes a comprehensive monitoring stack:

| Tool | URL | Purpose |
|------|-----|---------|
| **Grafana** | http://localhost:3000 | Visual dashboards (includes pre-configured JobStream dashboard) |
| **Prometheus** | http://localhost:9090 | Metrics collection & querying |
| **Kafka UI** | http://localhost:8085 | Kafka cluster management & topic inspection |
| **AKHQ** | http://localhost:8086 | Alternative Kafka GUI with topic browsing |

### Actuator Endpoints

- `/actuator/health` — Health check
- `/actuator/info` — Application info
- `/actuator/prometheus` — Prometheus metrics

---

## Contributing

1. **Architecture Decision Records** — All significant decisions are documented in `docs/adr/`. Please follow this pattern for new decisions.
2. **API-First** — Never create DTOs manually. Always update `codebase/openapi/openapi.yml` first.
3. **Testing** — Write integration tests with Testcontainers (Kafka + PostgreSQL). Follow BDD/Gherkin patterns for acceptance tests.
4. **Code Style** — Java follows Spring Boot conventions; Angular follows the project's Prettier configuration.

---

## OpenAgent AI-Assisted Development

This project is configured for **OpenAgents Control (OAC)** — an agentic AI framework built on [OpenCode](https://opencode.ai). It uses **OpenAgent** as the universal primary agent for code, documentation, testing, and workflow coordination.

The `.opencode/` directory contains:

- **Context system** — Project-specific standards loaded automatically by agents before generating code (API-First, event-driven architecture, code quality, etc.)
- **Agent definitions** — Specialized subagents auto-delegated based on task: `ContextScout` (pattern discovery), `CoderAgent` (implementation), `TestEngineer` (TDD), `CodeReviewer` (quality), `ExternalScout` (live docs), `TaskManager` (complex feature breakdown), and more
- **Rules & standards** — Code quality, testing, Git, security conventions
- **Skills** — Reusable workflows and generators (Docker, Angular, Spring Boot, etc.)

To use this project with OpenAgent:

```bash
# Install OpenCode CLI
npm install -g @opencode/cli

# Launch OpenAgent in the project directory
opencode --agent OpenAgent
```

The agentic framework automatically discovers project patterns via `ContextScout`, proposes implementation plans for your approval, and executes with validation — ensuring all generated code follows the project's architecture decisions (API-First with OpenAPI 3.0, event-driven with Kafka) and coding standards.

---

## License

This project is intended for demonstration and portfolio purposes. All external API usage is subject to the respective provider's terms of service.

---

Built with Java 25, Spring Boot 4.0, Angular 21 and Apache Kafka
