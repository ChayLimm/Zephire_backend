
> *"Your work is seen."*

**SokHr** is an intelligent HR assistance platform backend built with Spring Boot. It streamlines the recruitment pipeline — from candidate applications and CV uploads to AI-powered job-matching and automated email communications.

---

## Features

| Module | Description |
|---|---|
| ** Authentication** | JWT-based login & registration with role-based access (`ADMIN`, `HR`) |
| ** Candidate Management** | Full CRUD with approval workflow (`PENDING → APPROVED / REJECTED`) |
| ** CV Upload & Parsing** | Upload PDF resumes to Cloudflare R2; extract text with Apache PDFBox |
| ** AI-Powered Matching** | Match candidates to job descriptions using OpenRouter / Ollama LLMs |
| ** AI Chat** | Conversational AI assistant for HR queries with persistent chat history |
| ** Email Automation** | Bulk email delivery via Brevo SMTP with status tracking |
| ** Job Descriptions** | Create and manage JDs; trigger AI candidate matching |
| ** Public Application** | Public-facing endpoint for candidates to self-apply |
| ** Health Check** | Built-in health checker endpoint for monitoring |

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Framework** | Spring Boot 3.3.5 |
| **Language** | Java 21 |
| **Database** | PostgreSQL (JPA / Hibernate) |
| **Security** | Spring Security + JWT (jjwt 0.12.6) |
| **AI / LLM** | OpenRouter API, Ollama |
| **File Storage** | Cloudflare R2 (S3-compatible, AWS SDK v2) |
| **Email** | Spring Mail + Brevo SMTP |
| **PDF Parsing** | Apache PDFBox 3.0.1 |
| **Serialization** | Jackson (with JSR-310 date/time support) |
| **Build** | Maven |
| **Containerization** | Docker (multi-stage build) |

---

## Project Structure

```
src/main/java/com/example/HrAssistance/
├── SokHrApplication.java          # Application entry point
├── config/
│   ├── AppConfig.java                 # App-wide configuration
│   └── R2Config.java                  # Cloudflare R2 (S3) config
├── controller/
│   ├── AuthController.java            # Login & registration
│   ├── CandidateController.java       # Candidate CRUD & approval
│   ├── ChatController.java            # AI chat endpoints
│   ├── EmailController.java           # Bulk email & tracking
│   ├── HealthCheckerController.java   # Health check
│   ├── JobDescriptionController.java  # JD management & matching
│   ├── PublicController.java          # Public candidate application
│   └── TestController.java            # Dev/test endpoints
├── enums/                             # Domain enums
│   ├── CandidateSource.java
│   ├── CandidateStatus.java           # PENDING, APPROVED, REJECTED
│   ├── Department.java                # TELLER, IT, HR, MARKETING, etc.
│   ├── EmailType.java
│   ├── MessageRole.java
│   ├── PriorityCategory.java
│   ├── PriorityStatus.java
│   ├── RecognitionValue.java
│   └── Role.java                      # ADMIN, HR
├── model/                             # JPA entities
│   ├── Candidate.java
│   ├── ChatMessage.java
│   ├── Email.java
│   ├── JobDescription.java
│   ├── MatchResult.java
│   ├── User.java
│   └── dto/                           # Request & Response DTOs
├── repositories/                      # Spring Data JPA repositories
├── security/
│   ├── JwtAuthenticationFilter.java   # JWT filter chain
│   └── SecurityConfig.java            # Security configuration
├── seeders/
│   └── DataSeeder.java                # Initial data seeding
└── service/
    ├── AIService.java                 # AI service interface
    ├── AuthService.java
    ├── CandidateService.java
    ├── ChatService.java
    ├── EmailService.java
    ├── JobDescriptionService.java
    ├── JwtService.java
    ├── PdfService.java
    ├── StorageService.java
    └── impl/                          # Service implementations
        ├── OpenRouterAIServiceImpl.java
        ├── OllamaServiceImpl.java
        └── ...
```

---

## Getting Started

### Prerequisites

- **Java 21** (or later)
- **Maven 3.9+**
- **PostgreSQL** (running instance)
- **Docker** (optional, for containerized deployment)

### 1. Clone the Repository

```bash
git clone https://github.com/ChayLimm/Zephire_backend.git
cd Zephire_backend
```

### 2. Configure Environment Variables

Create a `.env` file in the project root (or set system environment variables):

```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/kudosflow_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password

# AI (OpenRouter)
OPENROUTER_BASE_URL=https://openrouter.ai/api/v1/chat/completions
OPENROUTER_MODEL=nvidia/nemotron-3-super-120b-a12b:free
OPENROUTER_TOKEN=your_openrouter_token

# Email (Brevo SMTP)
MAIL_USERNAME=your_brevo_username
MAIL_PASSWORD=your_brevo_password

# Cloudflare R2 Storage
R2_ACCESS_KEY=your_access_key
R2_SECRET_KEY=your_secret_key
R2_ACCOUNT_ID=your_account_id
R2_BUCKET_NAME=your_bucket_name
R2_PUBLIC_DOMAIN=https://your-r2-public-domain.r2.dev
```

### 3. Run Locally

```bash
./mvnw spring-boot:run
```

The server starts at **http://localhost:8080** by default.

### 4. Run with Docker

```bash
docker build -t wingpulse-backend .
docker run -p 8080:8080 --env-file .env wingpulse-backend
```

---

## API Reference

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/login` | Login with credentials |
| `POST` | `/api/auth/register` | Register a new user |

### Candidates

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/candidates/upload` | Upload a candidate CV |
| `GET` | `/api/candidates` | List all candidates |
| `GET` | `/api/candidates/{id}` | Get candidate by ID |
| `PATCH` | `/api/candidates/{id}` | Update candidate details |
| `DELETE` | `/api/candidates/{id}` | Delete a candidate |
| `GET` | `/api/candidates/{id}/preview` | Preview candidate PDF (redirect) |
| `GET` | `/api/candidates/pending` | List pending candidates |
| `PUT` | `/api/candidates/{id}/approve` | Approve a candidate |
| `PUT` | `/api/candidates/{id}/reject` | Reject a candidate |

### Job Descriptions

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/jd/match` | AI-match candidates to a JD |
| `GET` | `/api/jd` | List all job descriptions |
| `GET` | `/api/jd/{id}` | Get JD by ID |
| `DELETE` | `/api/jd/{id}` | Delete a JD |

### AI Chat

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/chat` | Send a message to AI |
| `GET` | `/api/chat/history` | Get full chat history |
| `GET` | `/api/chat/history/{id}` | Get history by job ID |
| `GET` | `/api/chat/history/candidate/{candidateId}` | Get history by candidate |
| `DELETE` | `/api/chat/history` | Clear chat history |

### Email

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/email/send-bulk` | Send bulk emails |
| `GET` | `/api/email` | List all emails |
| `PATCH` | `/api/email/{id}/status` | Update email status |
| `GET` | `/api/email/candidate/{candidateId}` | Get emails by candidate |

### Public

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/public/apply` | Public candidate application |

---

## Security

- All `/api/**` endpoints are protected by **JWT authentication** (except `/api/auth/**` and `/api/public/**`).
- Roles: `ADMIN` and `HR` for role-based access control.
- JWT tokens are validated on every request via `JwtAuthenticationFilter`.

---

## AI Integration

SokHr supports two AI backends:

1. **OpenRouter** — Cloud-based LLM API (default: `nvidia/nemotron-3-super-120b-a12b:free`)
2. **Ollama** — Local LLM inference

Used for:
- **CV-to-JD Matching** — Automatically rank and score candidates against job descriptions
- **AI Chat Assistant** — Interactive HR assistant powered by LLM

---

## Deployment

The project includes a multi-stage **Dockerfile** for production-ready containerization:

```dockerfile
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/HrAssistance-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Development

```bash
# Build the project
./mvnw clean package

# Run tests
./mvnw test

# Run in development mode
./mvnw spring-boot:run
```

---

## License

This project is developed as a **capstone project**.

---

<p align="center">
  Built with ❤️ by the <strong>SokHr</strong> team
</p>

