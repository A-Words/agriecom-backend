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
- SPRING_PROFILES_ACTIVE (default: empty; set to `dev` or `prod`)
- SPRING_LIQUIBASE_ENABLED (default: true)
- SPRING_LIQUIBASE_CHANGELOG (default: classpath:db/changelog/db.changelog-master.yml)
- SECURITY_JWT_SECRET (default: change-me-please-32-bytes-minimum-change-me)
- SECURITY_JWT_EXPIRATION_MS (default: 86400000)
- SECURITY_JWT_COOKIE_NAME (default: AUTH_TOKEN)

### Run (PowerShell)
```powershell
$env:SERVER_PORT='8080'
$env:SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/agriecom'
$env:SPRING_DATASOURCE_USERNAME='postgres'
$env:SPRING_DATASOURCE_PASSWORD='postgres'
$env:SPRING_REDIS_HOST='localhost'
$env:SPRING_REDIS_PORT='6379'
 $env:SPRING_PROFILES_ACTIVE='dev' # 或 prod
./mvnw.cmd spring-boot:run
```

### Endpoints
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Health: `GET /api/v1/health`
- Connectivity: `GET /api/v1/connectivity`
 - Auth Register: `POST /api/v1/auth/register`
 - Auth Login: `POST /api/v1/auth/login`
 - Auth Logout: `POST /api/v1/auth/logout`
 - Auth Me: `GET /api/v1/auth/me`
- Products List (分页/排序/筛选): `GET /api/v1/products`
- Products Search: `GET /api/v1/products/search`
- Product Detail (Redis 缓存): `GET /api/v1/products/{id}`
- Create Order: `POST /api/v1/orders`
- My Orders: `GET /api/v1/my-orders`
- Cancel Order: `PUT /api/v1/my-orders/{id}/cancel`
- Shop Orders: `GET /api/v1/my-shop/orders`
- Ship Order: `PUT /api/v1/my-shop/orders/{id}/ship`
- Cart Detail: `GET /api/v1/cart`
- Cart Add Item: `POST /api/v1/cart/items`
- Cart Update Item: `PUT /api/v1/cart/items/{productId}`
- Cart Remove Item: `DELETE /api/v1/cart/items/{productId}`
- Cart Clear: `POST /api/v1/cart/clear`

## Profiles
- `application-dev.yml`: 开发环境，JPA 默认 `validate`（交由 Liquibase 管理）；显示 SQL；暴露健康详情。
- `application-prod.yml`: 生产环境，JPA `validate`，关闭 SQL 日志。
- 使用 `SPRING_PROFILES_ACTIVE=dev|prod` 切换。

## Database Migrations (Liquibase)
- 主入口：`db/changelog/db.changelog-master.yml`
- 变更集：
	- `001-init-products.yml`：`products` 表
	- `002-auth-tables.yml`：`users`、`roles`、`user_roles`
- 启停：`SPRING_LIQUIBASE_ENABLED=true|false`

## Auth (JWT)
- 登录成功后在 HttpOnly Cookie（默认名 `AUTH_TOKEN`）下发 JWT。
- 配置项：`SECURITY_JWT_SECRET`、`SECURITY_JWT_EXPIRATION_MS`、`SECURITY_JWT_COOKIE_NAME`。
- 开放端点：Swagger、`/api/v1/health`、`/api/v1/connectivity`、`/api/v1/auth/**`；其余 `/api/**` 需携带 JWT。

### Quick Test (with default swagger)
1. 注册：`POST /api/v1/auth/register`，Body：`{"username":"alice","password":"alice123"}`
2. 登录：`POST /api/v1/auth/login`，成功后浏览器会存下 HttpOnly Cookie
3. 查看当前用户：`GET /api/v1/auth/me`

## Troubleshooting
- Postgres 认证失败（FATAL: password authentication failed）：请检查 `SPRING_DATASOURCE_USERNAME/PASSWORD` 与数据库一致。
- Liquibase 想先关闭排障：`SPRING_LIQUIBASE_ENABLED=false`。
- Swagger 无法访问：确认 `springdoc.swagger-ui.path=/swagger-ui.html`，访问 `http://localhost:8080/swagger-ui.html`。
