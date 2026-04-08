# AGENTS.md - CheersAI-Nexus Development Guide

High-signal guidance for AI agents working in this repository. 

## Project Overview

CheersAI-Nexus is a microservices management platform with:
- **Frontend**: Vue 3 + TypeScript + Vite + Element Plus + Pinia
- **Backend**: Spring Boot 3.5.x microservices (Java 21) with MyBatis-Flex ORM
- **Database**: PostgreSQL 16 with Flyway migrations
- **Infrastructure**: Redis 7, Nginx, systemd services

## Frontend (nexus-frontend/)

### Tech Stack
- Vue 3 (Composition API) + TypeScript
- Vite 5 (build tool)
- Element Plus 2.7.8 (UI components)
- Pinia 2.1.7 (state management)
- Vue Router 4
- Axios 1.6.8

### Key Patterns

**API Layer** (`src/api/`)
```typescript
// Each domain has its own file (auth.ts, users.ts, etc.)
// Use request utility with unwrapApiData helper
import request from '../utils/request'
import { unwrapApiData } from '../utils/api'

export async function fetchSomething() {
  const response = await request.get('/api/v1/...')
  return unwrapApiData<ReturnType>(response.data)
}
```

**State Management** (`src/store/modules/`)
- Use Composition API style with `defineStore`
- Stores are modular (auth.ts, etc.)
- LocalStorage for token persistence

**HTTP Client** (`src/utils/request.ts`)
- Centralized Axios instance with interceptors
- Auto-attaches JWT tokens from localStorage
- Auto-refresh on 401/403 with refresh token
- Base URL from `VITE_API_BASE_URL` env var

**Type Definitions** (`src/types/`)
- Co-locate types with their domain

### Dev Commands
```bash
cd nexus-frontend
npm install
npm run dev          # Dev server (Vite)
npm run build        # Production build
npm run type-check   # TypeScript check only
```

### Vite Proxy Config
Development proxy routes in `vite.config.ts`:
- `/api/v1/auth` → `:8081`
- `/api/v1/users` → `:8082`
- `/api/v1/feedbacks` → `:8083`
- `/api/v1/products` → `:8084`
- `/api/v1/plans` → `:8085`
- `/api/v1/subscriptions` → `:8085`
- `/api/v1/audit` → `:8087`

## Backend (nexus-backend/)

### Tech Stack
- Java 21 (BellSoft JDK recommended)
- Spring Boot 3.5.11
- Spring Cloud 2025.0.1 (OpenFeign)
- MyBatis-Flex 1.11.6 (ORM)
- PostgreSQL 42.7.3
- Flyway 11.7.2 (migrations)
- JJWT 0.12.6 (authentication)
- Lombok 1.18.44
- Hutool 5.8.28 (utilities)

### Module Structure

Parent POM manages 8 modules:
```
nexus-backend/
├── common/              # Shared utilities, Result wrapper
├── auth/                # Port 8082 - JWT auth, login/register
├── user-management/     # Port 8083 - User CRUD
├── feedback/            # Port 8084 - Feedback system
├── product/             # Port 8085 - Product management
├── membership/          # Port 8086 - Plans/subscriptions
├── auditlog/            # Port 8087 - Audit logging
└── system-config/       # System configuration
```

### Key Patterns

**Controller Pattern**
```java
@RestController
@RequestMapping("/api/v1/domain")
@RequiredArgsConstructor
public class XxxController {
    private final XxxService xxxService;
    
    @PostMapping("/action")
    public Result<ReturnType> action(@Valid @RequestBody RequestDto dto) {
        return Result.success(xxxService.action(dto));
    }
}
```

**Service Pattern**
- Interface + Implementation (e.g., `AuthService` + `AuthServiceImpl`)
- Use constructor injection with `@RequiredArgsConstructor`

**Result Wrapper** (`common` module)
```java
Result.success(data)     // 200 OK with data
Result.success()         // 200 OK empty
Result.error(message)    // 500 error
Result.error(code, msg)  // Custom code
```

**Entity Pattern** (MyBatis-Flex)
```java
@Table("table_name")
@Data
public class Entity {
    @Id(keyType = KeyType.Auto)
    private Long id;
    // fields...
}
```
- MyBatis-Flex generates `TableDef` classes for type-safe queries
- Generated files in `target/generated-sources/annotations/`

### Database Migrations

Each service has its own migrations in `src/main/resources/db/migration/`:
- `V1__create_xxx_table.sql`
- `V2__alter_xxx_add_column.sql`

Flyway runs automatically on startup. Never modify existing migrations that have run in production.

### Dev Commands

```bash
cd nexus-backend

# Build all modules
mvn clean package -DskipTests

# Run single service (auth must start first for token validation)
java -jar auth/target/nexus-auth.jar
java -jar user-management/target/nexus-user-management.jar
# etc...

# Run tests for single module
cd auth && mvn test
```

## Environment Variables

Required for production (see `deployment/config/.env.example`):
- `DB_PASSWORD` - PostgreSQL password
- `REDIS_PASSWORD` - Redis auth (if enabled)
- `JWT_SECRET` - Min 32 characters
- `ALIYUN_*` - SMS service credentials
- `LOG_LEVEL` - INFO/DEBUG/etc.

## Deployment

See `deployment/` directory:
- `scripts/build.sh` or `build.ps1` - Build frontend + backend
- `scripts/deploy.sh` - Deploy to server
- `nginx/nexus.conf` - Nginx reverse proxy config
- `systemd/*.service` - Service unit files

## Critical Conventions

1. **Always start auth service first** - Other services validate tokens against it
2. **Never skip MyBatis-Flex processor** - Required for `TableDef` generation
3. **Use Result wrapper** - All API responses must use `Result<T>`
4. **Migration naming** - Follow Flyway convention: `V{version}__{description}.sql`
5. **API versioning** - All endpoints prefixed with `/api/v1/`
6. **Port allocation** - Fixed ports 8082-8087 for microservices

## Testing

- **Backend**: JUnit 5 with Spring Boot Test (sparse coverage)
- **Frontend**: No test framework configured yet

## Documentation References

- [Deployment Guide](./deployment/README.md)
- [Operations Manual](./deployment/docs/OPERATIONS_MANUAL.md)
- [PRD](./CheersAI-Nexus运营管理平台PRD_v2.1.md)
