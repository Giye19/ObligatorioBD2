# Mundial Ticketing 2026 — UCU BDII

Sistema de ticketing para el Mundial 2026.

## Estructura

    mundial-ticketing/
    ├── backend/      # API REST — Java 17 + Spring Boot 3 + JDBC + JWT
    ├── frontend/     # SPA — React + Vite
    └── database/
        └── script_oblig.sql   # Script de creación de BD + triggers (MySQL 8.0+)

## Setup

### Base de datos

La app se conecta a la base del reto UCU (`mysql.reto-ucu.net`). Las credenciales no están en el repo por seguridad — se configuran como variables de entorno antes de levantar el backend:

```bash
$env:DB_URL = "jdbc:mysql://mysql.reto-ucu.net:50006/CD_GrupoX?useSSL=false&serverTimezone=UTC"
$env:DB_USERNAME = "..."
$env:DB_PASSWORD = "..."
$env:JWT_SECRET = "..."
```

### Backend

```bash
cd backend
mvn spring-boot:run
```

API en http://localhost:8080

### Frontend

```bash
cd frontend
npm install
npm run dev
```

App en http://localhost:5173

## Usuarios de prueba

| Mail | Password | Rol |
|------|----------|-----|
| admin@ucu.edu.uy | admin123 | ADMIN |
| usuario@test.com | user123  | USUARIO |
| func@test.com    | func123  | FUNCIONARIO |

