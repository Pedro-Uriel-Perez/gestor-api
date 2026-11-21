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
mvnw test        → 40 pruebas (JUnit 5) · reporte en target/site/jacoco/index.html
```
Colección de Postman con las pruebas de endpoints: `postman/Gestor_Practica1.postman_collection.json`.

## Despliegue en la nube (Render) y monitoreo
- `Dockerfile`: imagen con JRE 21, usuario sin privilegios y CDS para arrancar rápido con 0.1 CPU.
- La infraestructura (PostgreSQL, API y frontend en Render) se crea con Terraform y se configura con Ansible
  desde el repositorio [gestor-infra](https://github.com/Pedro-Uriel-Perez/gestor-infra).
- Variables en Render: `PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `METRICAS_TOKEN` y `SQL_EN_LOGS=false`.
- Monitoreo: `/actuator/health` (público) y `/actuator/prometheus` (exige `Authorization: Bearer <METRICAS_TOKEN>`).

## Jenkins (JDK 21 + Maven 3.9.11)
La carpeta `jenkins/` levanta Jenkins en Docker ya configurado (herramientas JDK 21 y Maven, usuario y pipelines de este repositorio y de `gestor-frontend`):
```
cd jenkins
docker compose up -d --build
```
Abrir http://localhost:8090 (usuario `admin`, contraseña `admin123`). Los secretos van en `jenkins/.env`
(no se versiona): `SONAR_TOKEN`, `RENDER_API_KEY`, `RENDER_OWNER_ID` y `GRAFANA_ADMIN_PASSWORD`. Pipelines:

| Job | Jenkinsfile | Qué hace |
|---|---|---|
| **gestor-api** | `Jenkinsfile` | Compila, pruebas + JaCoCo, SonarQube + Quality Gate, Trivy, Semgrep (SAST), empaqueta y despliega en Render |
| **gestor-frontend** | `Jenkinsfile` del repo gestor-frontend | Lo mismo para el frontend |
| **gestor-basedatos** | `jenkins/basedatos.Jenkinsfile` | Análisis de los scripts SQL con SQLFluff, base limpia, migración, re-ejecución, verificación de integridad (`sql/postgres/05_verificacion_integridad.sql`), pruebas de la API contra la base y escaneo de secretos |
| **gestor-infra** | `Jenkinsfile` del repo gestor-infra | Terraform (Render) + Ansible (base de datos, verificación y monitoreo) |
| **gestor-rendimiento** | `rendimiento/Jenkinsfile` del repo gestor-infra | Prueba de carga con k6 |

Análisis de los scripts SQL en local: `pip install sqlfluff` y `sqlfluff lint sql/postgres` (reglas en `.sqlfluff`).

Frontend que consume esta API: https://github.com/Pedro-Uriel-Perez/gestor-frontend
