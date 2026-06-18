# Mundial Ticketing 2026 — UCU BDII

Sistema de ticketing para el Mundial 2026. API REST construida con Java 17 + Spring Boot 3 + MySQL + JDBC puro + JWT.

## Requisitos

- Java 17+
- Maven 3.8+
- MySQL 8.0+ corriendo en Linux (configurado por el equipo de BD)

## Configuración

Antes de correr la app, editar `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mundial_ticketing?...
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
jwt.secret=TU_SECRETO_LARGO
```

## Compilar y correr

```bash
# Compilar
mvn clean package -DskipTests

# Correr
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Estructura del proyecto

```
src/main/java/com/ucu/ticketing/
├── config/         # Configuración de BD y seguridad
├── controller/     # Endpoints REST
├── service/        # Lógica de negocio
├── repository/     # Queries SQL con JdbcTemplate
├── model/          # POJOs que representan las entidades
├── dto/            # Objetos de entrada/salida de la API
├── security/       # JWT: generación, filtro y UserDetails
└── exception/      # Manejo global de errores
```

## Roles

| Rol         | Valor en BD   | Acceso                                      |
|-------------|---------------|---------------------------------------------|
| Administrador| `ADMIN`      | Gestión de estadios, sectores y eventos     |
| Funcionario | `FUNCIONARIO` | Validación de entradas en puerta            |
| Usuario     | `USUARIO`     | Compra, transferencia y consulta de entradas|

## Autenticación

Todas las rutas (excepto `/api/auth/register` y `/api/auth/login`) requieren el header:

```
Authorization: Bearer <token_jwt>
```

## Integrantes

- [Nombre 1] — Base de datos
- [Nombre 2] — Aplicación
- [Nombre 3] — [Área]
