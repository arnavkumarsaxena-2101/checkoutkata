# 🛒 Checkout Kata Service

A modern **Spring Boot 3 (Java 17)** microservice implementing a **checkout pricing engine** with per-SKU promotional rules — e.g. “3 for 130”.

This service exposes REST APIs to:
- 🏷️ Manage Products and Pricing Rules (`/api/v1/admin/**`)
- 💰 Calculate checkout totals (`/api/v1/checkout/price`)

It’s fully containerized, database-driven (PostgreSQL + Flyway), and uses Testcontainers for consistent, isolated testing.

---

## 🚀 Tech Stack

| Layer | Technology |
|-------|-------------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.x |
| Build Tool | Maven |
| Database | PostgreSQL 16 (Flyway for migrations) |
| Testing | JUnit 5, Mockito, Testcontainers |
| API Docs | Springdoc OpenAPI 2 (Swagger UI) |
| Packaging | WAR (Tomcat 10 / Jakarta EE) |
| Logging | Logback + MDC (`requestId`, `method`, `path`) |
| Code Style | Spotless (Google Java Format) |

---

## ⚙️ Local Development Setup

### 1️⃣ Prerequisites
Make sure you have:
- **Java 17+** → `java -version`
- **Maven 3.9+** → `mvn -version`
- **Docker** (for Testcontainers and/or Postgres)
- **IntelliJ IDEA** (recommended) with Lombok plugin

### 2️⃣ Clone the Repository

```bash
git clone https://github.com/<your-org>/checkoutkata.git
cd checkoutkata
```

### 3️⃣ Run with Local PostgreSQL

Run PostgreSQL using Docker:
```bash
docker run --rm --name checkout-pg   -e POSTGRES_DB=checkout   -e POSTGRES_USER=checkout   -e POSTGRES_PASSWORD=checkout   -p 5432:5432 postgres:16
```

Then start the application:
```bash
mvn spring-boot:run
```

Once started:
👉 **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🧪 Running Tests

### Unit Tests
Run all unit tests:
```bash
mvn test
```

### Integration Tests (Testcontainers)
The tests spin up a **temporary PostgreSQL container** automatically.  
You’ll see output like:
```
Container postgres:16 started (JDBC URL: jdbc:postgresql://localhost:32785/test)
```

---

## 🐳 Run in Docker (Prod-like Environment)

### Build and Package
```bash
mvn clean package -DskipTests
docker build -t checkoutkata-war .
```

### Run the Container
```bash
docker run --rm -p 8080:8080   -e JDBC_URL='jdbc:postgresql://<host>:5432/checkout'   -e DB_USER='checkout'   -e DB_PASSWORD='checkout'   -e SPRING_PROFILES_ACTIVE=prod   checkoutkata-war
```

Access it at:  
👉 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🧩 API Documentation

| Type | URL |
|------|-----|
| Swagger UI | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |
| OpenAPI JSON | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |
| OpenAPI YAML | [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml) |

---

## 🧰 Developer Commands

| Task | Command |
|------|----------|
| Clean + Build | `mvn clean package` |
| Run app | `mvn spring-boot:run` |
| Run tests | `mvn test` |
| Run tests with debug logs | `mvn test -Dlogging.level.root=DEBUG` |
| Apply code formatting | `mvn spotless:apply` |
| Run via Docker | `docker build -t checkoutkata-war . && docker run -p 8080:8080 checkoutkata-war` |

---

## 📁 Project Structure

```
checkoutkata/
├── src/
│   ├── main/java/com/product/service/checkoutkata/
│   │   ├── api/                # REST controllers
│   │   ├── config/             # OpenAPI + logging config
│   │   ├── domain/             # JPA entities & enums
│   │   ├── dto/                # Request/Response DTOs
│   │   ├── repo/               # JPA repositories
│   │   ├── service/            # Business logic & pricing engine
│   │   └── CheckoutkataApplication.java
│   ├── main/resources/
│   │   ├── application.yml
│   │   ├── application-prod.yml
│   │   └── db/migration/       # Flyway scripts
│   └── test/java/...           # Unit + integration tests
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 🌐 Environment Variables

| Variable | Description | Example |
|-----------|-------------|----------|
| `JDBC_URL` | JDBC connection string | `jdbc:postgresql://neon.tech/checkoutkata?sslmode=require` |
| `DB_USER` | Database username | `checkout` |
| `DB_PASSWORD` | Database password | `checkout` |
| `SPRING_PROFILES_ACTIVE` | Spring profile | `prod` |
| `SPRINGDOC_SERVER-URL` | (Optional) Public URL for OpenAPI docs | `https://your-app.example.com` |

---

## 🧱 Key Design Highlights

- ✅ **Transactional boundaries** in `CatalogService` & `CheckoutService`
- ✅ **Flyway migrations** ensure consistent schema
- ✅ **RequestIdFilter** injects `requestId`, `method`, and `path` into logs for tracing
- ✅ **Spotless** for consistent formatting across team
- ✅ **Swagger UI** integrated for easy API exploration
- ✅ **Testcontainers** → reproducible, isolated test environments
- ✅ **Centralized exception handling** via `GlobalExceptionHandler`

---

## 🧭 Troubleshooting

| Issue | Root Cause | Resolution |
|-------|-------------|-------------|
| `Connection refused: localhost:32777` | Test DB not ready | Ensure Docker is running; restart tests |
| `/swagger-ui` shows 404 | Wrong path | Use `/swagger-ui/index.html` instead |
| `Mapped port can only be obtained after the container is started` | Testcontainers race | Use static container pattern or `@Testcontainers` annotation |
| `Whitelabel Error Page` | Missing context path mapping | Rename WAR → `ROOT.war` or set `server.servlet.context-path=/` |
| `management.health.group.readiness.include` unresolved | Spring Boot Actuator version mismatch | Remove or update to Boot 3.3+ |

---

## 👥 Contributors

| Name | Role | Contact |
|------|------|----------|
| **Kumar Arnav** | Developer / Architect | [arnavkumarsaxena@live.com](mailto:arnavkumarsaxena@live.com) |

---

## 📜 License

This project is released into the public domain under [The Unlicense](http://unlicense.org).  
You are free to use, modify, and distribute it for any purpose without restriction.
