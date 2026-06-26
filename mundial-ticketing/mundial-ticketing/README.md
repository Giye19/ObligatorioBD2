# Mundial Ticketing 2026 — UCU BDII

Sistema de ticketing para el Mundial 2026.

## Estructura

    mundial-ticketing/
    ├── backend/      # API REST — Java 17 + Spring Boot 3 + JDBC + JWT
    ├── frontend/     # SPA — React + Vite
    └── database/
        ├── script_oblig.sql   # Script de creación de BD + triggers (MySQL 8.0+)
        └── reset_demo.sql     # Reset de datos a estado inicial (3 usuarios + países)

## Requisitos

- Java 17 (el proyecto no compila con versiones más nuevas debido a incompatibilidades de Lombok)
- Node.js 18+
- Acceso a un servidor MySQL 8.0+ con el script `script_oblig.sql` ya ejecutado

## Setup

### 1. Base de datos

Ejecutar `database/script_oblig.sql` en el servidor MySQL para crear las tablas y triggers. Las reglas de negocio críticas (límite de entradas por compra, límite de transferencias, control de capacidad, validación de jurisdicción, prevención de doble validación) están implementadas como triggers, no en el backend.

Para dejar la base en un estado limpio con datos mínimos de prueba, ejecutar además `database/reset_demo.sql`.

### 2. Backend

Las credenciales no están en el repo por seguridad — se configuran como variables de entorno antes de levantar el backend:

```bash
$env:DB_URL = "jdbc:mysql://mysql.reto-ucu.net:50006/CD_Grupo3?useSSL=false&serverTimezone=UTC"
$env:DB_USERNAME = "..."
$env:DB_PASSWORD = "..."
$env:JWT_SECRET = "..."
```


```bash
cd backend
mvn spring-boot:run
```

API en http://localhost:8080

### 3. Frontend

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