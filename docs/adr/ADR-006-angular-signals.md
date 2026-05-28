# ADR-006: Angular Signals pour la Gestion d'État

## Status
**Accepted** - 2026-03-26

## Context

Angular 21 propose un nouveau modèle de réactivité: les Signals. Nous devons choisir comment gérer l'état dans le frontend.

### Alternatives
1. Signals (nouveau)
2. RxJS BehaviorSubject (traditionnel)
3. NgRx (complet mais complexe)

## Decision

**Angular Signals pour la gestion d'état**

```typescript
@Injectable({ providedIn: 'root' })
export class JobService {
  // Signal pour les jobs
  private jobsSignal = signal<Job[]>([]);
  
  // Computed pour les statistiques
  readonly jobCount = computed(() => this.jobsSignal().length);
  
  // Getter public en lecture seule
  get jobs() {
    return this.jobsSignal.asReadonly();
  }
  
  // Mise à jour
  setJobs(jobs: Job[]): void {
    this.jobsSignal.set(jobs);
  }
}
```

## Rationale

1. **Nouveau standard**: Angular encourage l'utilisation des Signals
2. **Simplicité**: Plus facile à comprendre que RxJS
3. **Performance**: Meilleure détection des changements
4. **Type-safe**: Entièrement typé TypeScript

## Consequences

### Positive
- ✅ Plus simple que RxJS pour les cas simples
- ✅ Meilleures perfs que Zone.js
- ✅ API intuitive
- ✅ Bon intégration avec Angular 21

### Negative
- ⚠️ Moins puissant que RxJS pour les cas complexes
- ⚠️ Team doit apprendre le pattern

### Neutral
- ℹ️ Peut être combiné avec RxJS si besoin

## Alternatives Considered

### Alternative 1: RxJS BehaviorSubject
**Description**: Utiliser des BehaviorSubject pour l'état

**Pros**:
- familier pour les développeurs Angular
- Puissant

**Cons**:
- Plus complexe
- Plus de boilerplate

**Reason Rejected**: Plus complexe que nécessaire

### Alternative 2: NgRx
**Description**: Store Redux pour Angular

**Pros**:
- Complet
- Bon pour grandes applications

**Cons**:
- Trop complexe pour un prototype
-陡峭 d'apprentissage

**Reason Rejected**: Overkill pour notre cas d'usage

## Implementation Notes

- Utiliser `inject()` pour les services
- Signals pour l'état local et global
- RxJS pour les streams HTTP (conversion avec `toSignal`)

## Related Decisions
- ADR-001: API-First avec OpenAPI

## References
- [Angular Signals](https://angular.io/guide/signals)

## Change Log
- 2026-03-26: Initial decision (Status: Accepted)

---
**Decision Maker**: Architecture Team  
**Last Updated**: 2026-03-26