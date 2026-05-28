# Active Context - Current Work Focus

> Last updated: 2026-03-26
> Current session: Documentation setup

## Current Task

- [x] Set up OpenCode configuration
- [x] Create memory bank structure
- [x] Create README.md
- [x] Create architecture documentation
- [x] Update Memory Bank (all files)
- [ ] Initialize project structure (codebase/frontend, services)

## Recent Sessions

### Session 2026-03-26
- Initialized OpenCode project
- Created skills: angular-best-practices, java-best-practices, docker-best-practices
- Configured agents with skill permissions
- Created README.md with project overview
- Created architecture documentation (Mermaid diagrams)
- Updated all Memory Bank files:
  - projectbrief.md - Updated with Kafka/SSE architecture
  - productContext.md - Updated with workflow description
  - techContext.md - Added Kafka, SSE, services details
  - systemPatterns.md - Added Kafka and SSE patterns
  - progress.md - Added architecture decisions

## Architecture Summary

```
Frontend (Angular 21)
       │
       ▼
Dashboard Service (8080) ──▶ JSearch API
       │
       ├──▶ Kafka (job-offers-raw)
       │
       ▼
IA Analyzer (8081)
       │
       ▼
Kafka (job-offers-analyzed)
       │
       ▼
Dashboard (consume) ──▶ PostgreSQL
       │
       ▼
SSE ──▶ Frontend
```

## Notes for Next Session

- Project documentation is complete
- Ready for implementation
- Start with Docker Compose (Kafka + PostgreSQL)
- Then implement services and frontend
- Check memory bank before starting new work