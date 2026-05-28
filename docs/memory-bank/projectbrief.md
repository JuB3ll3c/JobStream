# Project Brief - Project Scope

> Last updated: 2026-03-26

## Project Overview

| Field | Value |
|-------|-------|
| **Name** | JobStream |
| **Type** | Web Application (Prototype) |
| **Stack** | Angular 21 + Java 21 + Spring Boot 3.x |
| **Architecture** | Event-Driven (Microservices + Kafka) |
| **Mode** | Web Spike / Prototype |

## Scope

### In Scope
- Angular 21 frontend with Signals
- Spring Boot microservices (Dashboard + IA Analyzer)
- Apache Kafka for inter-service communication
- PostgreSQL database
- Server-Sent Events (SSE) for real-time updates
- Docker containerization
- OpenAPI 3.0 contracts
- JSearch API integration (RapidAPI)

### Out of Scope (MVP)
- User authentication
- Complex analytics
- WebSocket (SSE is sufficient)
- AI/ML features (simulated analysis)
- API Gateway (direct frontend → service)

## Architecture Summary

```
Frontend (Angular 21) ──▶ Dashboard Service ──▶ JSearch API
       │                       │
       │ SSE                   ▼
       │                  PostgreSQL
       │
       ▼                  Kafka Cluster
       │              (job-offers-raw)
       │                       │
       ▼                       ▼
       │              IA Analyzer Service
       │              (job-offers-analyzed)
       │
       ◀───────────────────────
            (SSE update)
```

## Non-Functional Requirements

### Performance
- Page load < 3 seconds
- API response < 500ms

### Architecture
- Event-driven with Kafka
- Topic-based decoupling
- Dead Letter Topic for failed messages

### Accessibility
- WCAG 2.1 AA compliance (mandatory)

### Security
- HTTPS only
- Input validation
- SQL injection prevention
- No hardcoded secrets (environment variables)

## Job States

| State | Description |
|-------|-------------|
| `DISCOVERED` | Job from API, not saved |
| `SAVED` | User saved the job |
| `ANALYZING` | IA analysis in progress |
| `ANALYZED` | Analysis complete |

## Success Criteria

1. ✅ Code respects OpenAPI contracts
2. ✅ Tests integration (Testcontainers) pass
3. ✅ Swagger/OpenAPI documentation up to date
4. ✅ UI is accessible (WCAG 2.1 AA)

## Timeline

- **Mode**: Prototype/Spike
- **Goal**: Validate architecture and tech stack
- **Scope**: Core functionality only