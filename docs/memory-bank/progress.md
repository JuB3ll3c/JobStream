# Progress - Milestones & Decision Log

> Last updated: 2026-03-26

## Milestones

| Date | Milestone | Status |
|------|-----------|--------|
| 2026-03-26 | OpenCode configuration setup | ✅ Done |
| 2026-03-26 | Skills creation (angular, java, docker best practices) | ✅ Done |
| 2026-03-26 | Agent configuration with skills | ✅ Done |
| 2026-03-26 | Memory bank structure | ✅ Done |
| 2026-03-26 | README.md created | ✅ Done |
| 2026-03-26 | Architecture doc created (docs/architecture/overview.md) | ✅ Done |
| 2026-03-26 | Memory Bank updated with Kafka/SSE/Architecture | ✅ Done |

## Architecture Decisions

| Date | Decision | Context |
|------|----------|---------|
| 2026-03-26 | Event-driven with Kafka | Decoupled microservices |
| 2026-03-26 | Dashboard service manages DB | Separation of concerns |
| 2026-03-26 | 2 Kafka topics (raw + analyzed) | Clean data flow |
| 2026-03-26 | SSE for real-time | Simpler than WebSocket |
| 2026-03-26 | OpenAPI as source of truth | Contract-first |
| 2026-03-26 | No API Gateway | Simplification for prototype |
| 2026-03-26 | JSearch API (RapidAPI) | External job search |

## Coding Standards Decisions

| Date | Decision | Context |
|------|----------|---------|
| 2026-03-26 | Angular 21 with Signals | Modern state management |
| 2026-03-26 | Use `inject()` function | Modern DI pattern |
| 2026-03-26 | DTOs from OpenAPI generator | Never create manually |
| 2026-03-26 | Kafka with @RetryableTopic | Resilience |
| 2026-03-26 | acks=all + idempotence | Reliability |

## Next Steps

1. Initialize codebase structure (frontend, services)
2. Set up Docker Compose with Kafka + PostgreSQL
3. Create OpenAPI spec (openapi.yml)
4. Generate DTOs from OpenAPI
5. Implement Dashboard Service
6. Implement IA Analyzer Service
7. Implement Angular Frontend