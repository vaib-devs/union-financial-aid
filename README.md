# union-financial-aid

Digital system for submitting, storing and verifying student applications
for financial assistance from the student union.

## Overview

- **Student site** – fill in the application, attach documents, track the status.
- **Staff interface** – review the queue, check documents, approve / request clarification / reject.
- **Backend** – Spring Boot REST API, PostgreSQL.

## Tech stack

| Part      | Technology                                  |
|-----------|---------------------------------------------|
| Backend   | Java 21, Spring Boot, Spring Data JPA, Spring Security |
| Database  | PostgreSQL                                  |
| Frontend  | _to be decided_                             |
| Infra     | Docker Compose, GitHub Actions              |

## Repository structure

```
backend/     Spring Boot application
frontend/    Student site and staff interface
.github/     CI workflows, issue and PR templates
```

## Getting started

### Prerequisites
- JDK 21
- Maven
- Docker and Docker Compose
- Git

### Run locally

1. Clone the repository:
   ```bash
   git clone https://github.com/<org>/union-financial-aid.git
   cd union-financial-aid
   ```
2. Create your local environment file:
   ```bash
   cp .env.example .env
   ```
3. Start the database:
   ```bash
   docker compose up -d
   ```
4. Start the backend:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
5. The API is available at `http://localhost:8080`.

6. To launch the application:
```bash
cd frontend/desktop
./mvnw clean javafx:run
   ```

> Note: Spring does not read `.env` by itself. Set the same variables in your
> IDE run configuration or rely on the defaults in `application.yml`.

### Run tests
```bash
cd backend
./mvnw verify
```

## Team

@ArtRassian - team-lead
@Griga07 - backend