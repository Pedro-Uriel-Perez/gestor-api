# gestor-api

API REST del Gestor de Proyectos (usuarios, proyectos, tareas, comentarios y asignaciones) con PostgreSQL.

| | Versión |
|---|---|
| Java | **21** |
| Spring Boot | **3.5.16** (Spring Framework 6, Hibernate 6, Jakarta EE) |
| Maven | 3.9 o superior (incluye `mvnw`) |
| Base de datos | PostgreSQL (base `dbgestor`) |
| Puerto | 8081 |

## Requisitos
- JDK 21 (`JAVA_HOME` apuntando a él). El build falla con un mensaje claro si se usa otra versión (maven-enforcer).
- PostgreSQL con usuario `postgres` / `postgres` (o las variables `DB_URL`, `DB_USER`, `DB_PASSWORD`).

## Base de datos
```
psql -U postgres -h localhost -c "CREATE DATABASE dbgestor;"
psql -U postgres -h localhost -d dbgestor -f sql/postgres/01_schema.sql
psql -U postgres -h localhost -d dbgestor -f sql/postgres/02_data.sql
```

## Ejecutar
```
mvnw spring-boot:run
```

## Pruebas y cobertura
```
mvnw test        → 33 pruebas (JUnit 5) · reporte en target/site/jacoco/index.html
```
Colección de Postman con las pruebas de endpoints: `postman/Gestor_Practica1.postman_collection.json`.

## Jenkins (JDK 21 + Maven 3.9.11)
La carpeta `jenkins/` levanta Jenkins en Docker ya configurado (herramientas JDK 21 y Maven, usuario y pipelines de este repositorio y de `gestor-frontend`):
```
cd jenkins
docker compose up -d --build
```
Abrir http://localhost:8090 (usuario `admin`, contraseña `admin123`) y ejecutar el job **gestor-api**. El pipeline está en `Jenkinsfile`.

Frontend que consume esta API: https://github.com/Pedro-Uriel-Perez/gestor-frontend
