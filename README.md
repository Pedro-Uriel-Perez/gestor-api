# gestor-api

[![CI/CD gestor-api](https://github.com/Pedro-Uriel-Perez/gestor-api/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/Pedro-Uriel-Perez/gestor-api/actions/workflows/ci-cd.yml)
[![Base de datos](https://github.com/Pedro-Uriel-Perez/gestor-api/actions/workflows/basedatos.yml/badge.svg)](https://github.com/Pedro-Uriel-Perez/gestor-api/actions/workflows/basedatos.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=Pedro-Uriel-Perez_gestor-api&metric=alert_status)](https://sonarcloud.io/project/overview?id=Pedro-Uriel-Perez_gestor-api)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Pedro-Uriel-Perez_gestor-api&metric=coverage)](https://sonarcloud.io/component_measures?id=Pedro-Uriel-Perez_gestor-api&metric=coverage)

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

## CI/CD en la nube (GitHub Actions + SonarQube Cloud)
| Workflow | Qué hace |
|---|---|
| [`ci-cd.yml`](.github/workflows/ci-cd.yml) | Compila y prueba contra un PostgreSQL de servicio (JUnit + JaCoCo) → SonarQube Cloud con Quality Gate → Trivy (dependencias, secretos y Dockerfile, sin `target/`) → Semgrep (SAST) → despliega en Render el mismo commit y comprueba `/actuator/health` |
| [`basedatos.yml`](.github/workflows/basedatos.yml) | SQLFluff → base limpia → migración → re-ejecución → verificación de integridad BD-01 a BD-10 → pruebas de la API contra la base → Trivy |

Las acciones están fijadas por SHA, Semgrep corre desde su imagen fijada por digest y los secretos
(`SONAR_TOKEN`, `RENDER_API_KEY`) son secretos del repositorio.

## Despliegue en la nube (Render) y observabilidad
- `Dockerfile`: imagen con JRE 21, usuario sin privilegios, CDS para arrancar rápido con 0.1 CPU y el
  agente de OpenTelemetry 2.33.1 (verificado con SHA-256), apagado por defecto.
- La infraestructura (PostgreSQL, API y frontend en Render; monitoreo en Grafana Cloud) se crea con Terraform
  y se configura con Ansible desde el repositorio [gestor-infra](https://github.com/Pedro-Uriel-Perez/gestor-infra).
- Variables en Render: `PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `METRICAS_TOKEN`, `SQL_EN_LOGS=false` y el
  grupo `otel-grafana-cloud` (`OTEL_*`), que enciende el agente y lo conecta a Grafana Cloud.
- Monitoreo: trazas, métricas y logs por OTLP; `/actuator/health` (público) y `/actuator/prometheus`
  (exige `Authorization: Bearer <METRICAS_TOKEN>`).

## Jenkins local (alternativa: JDK 21 + Maven 3.9.11)
La carpeta `jenkins/` levanta Jenkins en Docker ya configurado (JCasC) con los mismos pipelines en local:
```
cd jenkins
docker compose up -d --build
```
Abrir http://localhost:8090 (usuario `admin`, contraseña `admin123`). Los secretos van en `jenkins/.env`
(no se versiona). Pipelines:

| Job | Jenkinsfile | Qué hace |
|---|---|---|
| **gestor-api** | `Jenkinsfile` | Compila, pruebas + JaCoCo, SonarQube + Quality Gate, Trivy, Semgrep (SAST), empaqueta y despliega en Render |
| **gestor-frontend** | `Jenkinsfile` del repo gestor-frontend | Lo mismo para el frontend |
| **gestor-basedatos** | `jenkins/basedatos.Jenkinsfile` | Igual que `basedatos.yml` |

Análisis de los scripts SQL en local: `pip install sqlfluff` y `sqlfluff lint sql/postgres` (reglas en `.sqlfluff`).

Frontend que consume esta API: https://github.com/Pedro-Uriel-Perez/gestor-frontend
