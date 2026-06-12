# Docugen

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-A9254C?style=flat-square)](LICENSE)
[![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)

Docugen is a document generation and template management system. It allows organizations to define dynamic templates, configure custom schemas for input validation, and generate print-accurate PDF files via a headless Chromium browser.

---

## Technical Specifications

* **Backend Framework:** Spring Boot 4.0 (Java 25)
* **Frontend Framework:** Vue.js 3, Vite, TypeScript
* **Rendering Engine:** Playwright (Chromium Headless)
* **Database:** PostgreSQL 18
* **Authentication:** OAuth2 Resource Server (Auth0 integration)
* **Development Environment:** Docker, Docker Compose

---

## Environment Configuration

The application requires two configuration files to separate local development runtimes from containerized orchestration. Because these files contain credentials, they are ignored by version control. Reference templates are provided in the repository:

1. **Root `.env.example` (for Docker Compose):**
   * Configures database credentials, Spring profile overrides, and passes Auth0 environment variables to the frontend Docker container build arguments.
   * To use, copy this to a local `.env` file in the root directory.

2. **Frontend `frontend/.env.local.example` (for Local Frontend Development):**
   * Used by Vite during local frontend development (when running `npm run dev` outside of Docker).
   * To use, copy this to `frontend/.env.local`.

---

## Getting Started

### Prerequisites
* Docker and Docker Compose
* Node.js v20+ (for local frontend development)
* JDK 25 (for local backend development)

### Local Deployment (Docker Compose)

1. **Prepare Environment Files:**
   Copy the provided templates to configure your environment:
   ```bash
   cp .env.example .env
   cp frontend/.env.local.example frontend/.env.local
   ```

2. **Launch the Application Stack:**
   ```bash
   docker compose up --build
   ```
   This command starts the database, the Spring Boot API, and the Vue.js frontend served via Nginx.

3. **Access the Application:**
   Open your browser and navigate to the frontend web interface:
   ```
   http://localhost:5173
   ```
> [!NOTE]
> The PostgreSQL container automatically initializes the schema structure (including tables, indexes, enums, triggers, and permissions) using the SQL script located at `backend/src/main/resources/db_init_tables.sql` (mounted directly as a read-only volume in `docker-compose.yml`).
---

## Local Development (Without Docker)

If you wish to run the services locally outside of Docker for active code changes and hot-reloading, follow these steps.

### 1. Database Infrastructure
The backend and frontend require the database to be running. You can run only the database container:
```bash
docker compose up -d db
```

### 2. Backend Setup
Ensure you have JDK 25 installed. 
1. The backend reads environment configuration from your local system or IDE environment. 
2. Execute the Gradle boot task to start the backend API:
   ```bash
   cd backend
   ./gradlew bootRun
   ```
   The backend API will start at `http://localhost:8080`.

### 3. Frontend Setup
Ensure you have Node.js installed.
1. Verify `frontend/.env.local` is present in the frontend directory.
2. Run npm setup and start commands:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   The frontend hot-reloading development server will start at `http://localhost:5173`.

---

## License

This project is licensed under the GNU Affero General Public License v3. See the [LICENSE](LICENSE) file for the full license text.
