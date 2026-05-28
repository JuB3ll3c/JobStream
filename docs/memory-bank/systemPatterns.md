# System Patterns - Architecture Patterns

> Last updated: 2026-03-26

## API-First Pattern

### Principe
**`openapi.yml` est la source de vérité** pour tous les contrats API.

```
openapi/openapi.yml  ───▶  Backend DTOs (Java)
        │
        └──▶  Frontend Client (TypeScript)
```

### Workflow

1. Définir les endpoints et DTOs dans `openapi.yml`
2. Générer les DTOs Backend avec openapi-generator-maven-plugin
3. Générer le client Frontend avec openapi-generator-cli
4. **NE JAMAIS modifier les fichiers générés manuellement**

### Configuration Maven Backend

```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <version>7.0.1</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <inputSpec>${project.basedir}/../openapi/openapi.yml</inputSpec>
                <generatorName>spring</generatorName>
                <modelPackage>com.jobstream.api.dto</modelPackage>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Frontend Patterns

### Component Architecture
- Standalone components (no NgModules)
- Signal-based reactivity (input/output signals)
- Feature-based directory structure
- Lazy-loaded routes

### Service Pattern
```typescript
@Injectable({ providedIn: 'root' })
export class MyService {
  private readonly http = inject(HttpClient);
  // ...
}
```

### State Management
- Angular Signals (signal, computed, effect)
- Service-based state with signals

## Backend Patterns

### Layered Architecture
```
Controller -> Service -> Repository -> Entity
```

### DTO Pattern (Java Records)
⚠️ DTOs générés automatiquement depuis OpenAPI - **NE PAS créer manuellement**

### Repository Pattern
```java
public interface UserRepository extends JpaRepository<User, String> {}
```

## Event-Driven Patterns

### Kafka Producer
- `acks: all` for reliability
- Idempotence enabled
- JSON serialization

### Kafka Consumer with @RetryableTopic
```java
@RetryableTopic(
    attempts = "4",
    backoff = @Backoff(delay = 1000, multiplier = 2.0),
    dltTopicSuffix = "-dlt"
)
@KafkaListener(topics = "job-offers-raw")
public void consume(ConsumerRecord<String, JobOfferEvent> record) {
    // process
}
```

### Dead Letter Topic (DLT)
- Messages failed after all retries → `-dlt` topic
- Manual intervention for investigation

## Real-Time Patterns

### Server-Sent Events (SSE)
```java
@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<JobUpdateEvent>> stream() {
    return sseService.getUpdates();
}
```

### Frontend SSE Listener
```typescript
connect(): Observable<JobUpdateEvent> {
  return new Observable(observer => {
    const eventSource = new EventSource('/api/jobs/stream');
    eventSource.onmessage = event => observer.next(JSON.parse(event.data));
  });
}
```

## REST Endpoints

| Méthode | Path | Description |
|---------|------|-------------|
| `GET` | `/api/jobs/search?q=` | Recherche via JSearch |
| `GET` | `/api/jobs` | Liste jobs sauvegardés |
| `POST` | `/api/jobs/{jobId}/save` | Sauvegarder un job |
| `GET` | `/api/jobs/{jobId}` | Détail d'un job |
| `DELETE` | `/api/jobs/{jobId}` | Supprimer un job |
| `GET` | `/api/jobs/stream` | Flux SSE |
| `GET` | `/api/health` | Health check |

## Error Handling

- Global exception handler (@RestControllerAdvice)
- Typed exceptions
- Consistent error response format

## Container Patterns

### Multi-Stage Dockerfile
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS builder
COPY . .
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
COPY --from=builder /target/*.jar app.jar
```

### Non-root User
- Always run as non-root
- Use Alpine base images

## Testing Patterns

### Integration Tests with Testcontainers
- Kafka container
- PostgreSQL container
- Isolated test environment