# ADR-004: Server-Sent Events (SSE) pour le Temps Réel

## Status
**Accepted** - 2026-03-26

## Context

Quand l'analyse IA est terminée, le frontend doit être notifié pour mettre à jour l'affichage. Nous avons besoin d'une solution de temps réel.

### Alternatives
1. WebSockets
2. Server-Sent Events (SSE)
3. Polling HTTP

## Decision

**Server-Sent Events (SSE)**

```typescript
// Backend
@GetMapping(value = "/api/jobs/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<JobUpdateEvent>> stream() {
    return sseService.getUpdates();
}
```

```typescript
// Frontend
connect(): Observable<JobUpdateEvent> {
  return new Observable(observer => {
    const eventSource = new EventSource('/api/jobs/stream');
    eventSource.onmessage = event => observer.next(JSON.parse(event.data));
  });
}
```

## Rationale

1. **Simplicité**: Plus simple que WebSockets
2. **Unidirectionnel**: Parfait pour notre cas (serveur → client)
3. **HTTP/2**: Compatible avec HTTP/2
4. **Reconnection automatique**: Le browser gère les reconnexions

## Consequences

### Positive
- ✅ Plus simple à implémenter que WebSockets
- ✅ Format JSON simple
- ✅ Support natif dans les navigateurs
- ✅ Reconnexion automatique

### Negative
- ⚠️ Unidirectionnel uniquement
- ⚠️ Limité à un onglet par connexion (par défaut)

### Neutral
- ℹ️ Nécessite une connexion persistante

## Alternatives Considered

### Alternative 1: WebSockets
**Description**: Communication bidirectionnelle

**Pros**:
- Bidirectionnel
- Plus performant pour beaucoup de messages

**Cons**:
- Plus complexe
- Overkill pour notre cas d'usage

**Reason Rejected**: Pas besoin de bidirectionnel, SSE suffit

### Alternative 2: Polling
**Description**: Requêtes HTTP régulières

**Pros**:
- Très simple

**Cons**:
- Inefficient
- Latence

**Reason Rejected**: Pas assez réactif

## Related Decisions
- ADR-002: Architecture Event-Driven avec Apache Kafka

## References
- [MDN: Server-Sent Events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)

## Change Log
- 2026-03-26: Initial decision (Status: Accepted)

---
**Decision Maker**: Architecture Team  
**Last Updated**: 2026-03-26