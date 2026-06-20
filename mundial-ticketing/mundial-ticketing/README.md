# Mundial Ticketing 2026 — UCU BDII

Sistema de ticketing para el Mundial 2026.

## Estructura del repositorio

```
mundial-ticketing/
├── backend/      # API REST — Java 17 + Spring Boot 3 + JDBC + JWT
├── frontend/     # SPA — React + Vite
└── database/
    └── schema.sql   # Script de creación de BD (MySQL 8.0+)
```

## Setup rápido

### 1. Base de datos
```sql
-- En MySQL Workbench o consola:
source database/schema.sql
```

**Usuarios de prueba:**
| Mail | Password | Rol |
|------|----------|-----|
| admin@ucu.edu.uy | admin123 | ADMIN |
| usuario@test.com | user123  | USUARIO |
| func@test.com    | func123  | FUNCIONARIO |

### 2. Backend
```bash
cd backend
# Editar src/main/resources/application.properties con tus credenciales MySQL
mvn spring-boot:run
# API en http://localhost:8080
```

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
# App en http://localhost:5173
```

## Integrantes
- [Nombre 1] — Base de datos
- [Nombre 2] — Aplicación
- [Nombre 3] — [Área]
