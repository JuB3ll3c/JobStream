# Repository Guidelines

## Project Structure & Module Organization

JobStream is an API-first full-stack application. `api/` contains the Java 25 Spring Boot service; production code lives under `api/src/main/java/com/jobstream/api`, tests mirror it in `api/src/test/java`, and runtime SQL/YAML files are in `api/src/main/resources`. `front/` is the Angular 22 client, with application code in `front/src/app`, global styles in `front/src/styles.scss`, and static assets in `front/public`. The shared contract is `openapi/openapi.yaml`. Treat it as the source of truth: backend DTOs/interfaces and `front/src/app/generated/` are generated artifacts, not hand-edited code.

## Build, Test, and Development Commands

- `docker compose up -d postgres` starts PostgreSQL 18 on local port `5433`.
- `cd api && ./mvnw spring-boot:run` generates server types and runs the API at `/api`.
- `cd api && ./mvnw verify` compiles, runs JUnit tests, and writes JaCoCo output under `target/site/jacoco/`.
- `cd front && npm ci` installs the locked frontend dependencies.
- `cd front && npm start` regenerates the API client and serves Angular at `http://localhost:4200` with the development proxy.
- `cd front && npm test` runs Vitest unit tests; `npm run build` creates a production build.

The API requires `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `ADZUNA_APP_ID`, `ADZUNA_APP_KEY`, and `JWT_SECRET`. Never commit real credentials or `.env` files.

## Coding Style & Naming Conventions

Use four-space indentation for Java and two spaces for TypeScript, HTML, SCSS, and YAML. Frontend files follow Angular kebab-case conventions (`auth-service.ts`, `login.spec.ts`); classes and Java types use PascalCase, while methods and variables use camelCase. Keep controllers thin, business rules in services, persistence in repositories, and conversions in MapStruct mappers. Frontend formatting follows `front/.editorconfig` and Prettier; TypeScript uses single quotes.

## Agent Implementation Workflow

Agents may implement code only in small, explicitly user-approved slices. Before editing, restate the intended outcome, list the files expected to change, including any generated artifacts, and state what is outside scope. Wait for approval. If the scope or file list must change, stop and seek renewed approval. Do not prepare or implement later slices.

Obtain explicit approval before adding a dependency, introducing a database migration or schema change, introducing a material architectural change, or departing from existing project patterns.

After implementing the approved slice, run the relevant tests and report their commands and results. Then stop and summarize the changed files, completed behavior, and remaining work without starting another slice. Never create a Git commit unless explicitly authorized.

## Testing Guidelines

JUnit 5, Spring test utilities, and Testcontainers cover the backend; Docker must be available for PostgreSQL integration tests. Name Java tests `*Test.java`. Place Angular tests beside their subjects as `*.spec.ts`. Add tests for success, validation, authorization, and error paths. No numeric coverage threshold is configured, but avoid reducing coverage for changed behavior.

## Commit & Pull Request Guidelines

History generally uses short, imperative type prefixes such as `feat:`, `fix:`, `test:`, and `refact:`. Keep each commit focused and describe the observable change. Pull requests should summarize scope, identify contract or configuration changes, link relevant issues, list commands run, and include screenshots for UI changes. Update `openapi/openapi.yaml` first whenever an endpoint shape changes.
