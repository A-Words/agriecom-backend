# Agriecom Backend

## Quick Start

### Env Vars (copy to `.env` when using Compose)
- SPRING_APPLICATION_NAME (default: agriecom-backend)
- SERVER_PORT (default: 8080)
- SPRING_DATASOURCE_URL (default: jdbc:postgresql://localhost:5432/agriecom)
- SPRING_DATASOURCE_USERNAME (default: postgres)
- SPRING_DATASOURCE_PASSWORD (default: postgres)
- SPRING_JPA_HIBERNATE_DDL_AUTO (default: validate)
- SPRING_REDIS_HOST (default: localhost)
- SPRING_REDIS_PORT (default: 6379)
- SPRING_REDIS_PASSWORD (default: empty)
- SPRINGDOC_API_DOCS_PATH (default: /v3/api-docs)
- SPRINGDOC_SWAGGER_UI_PATH (default: /swagger-ui.html)

### Run (PowerShell)
```powershell
$env:SERVER_PORT='8080'
$env:SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/agriecom'
$env:SPRING_DATASOURCE_USERNAME='postgres'
$env:SPRING_DATASOURCE_PASSWORD='postgres'
$env:SPRING_REDIS_HOST='localhost'
$env:SPRING_REDIS_PORT='6379'
./mvnw.cmd spring-boot:run
```

### Endpoints
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Health: `GET /api/v1/health`
- Connectivity: `GET /api/v1/connectivity`
