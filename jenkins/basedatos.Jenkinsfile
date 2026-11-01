// Pipeline de BASE DE DATOS (PostgreSQL de dbgestor), priorizando la calidad y el análisis del código SQL:
// análisis estático con SQLFluff → base limpia → migración → re-ejecución → verificación de integridad →
// pruebas de la API contra la base nueva → escaneo de secretos con Trivy
pipeline {
    agent any

    tools {
        jdk 'jdk-21'
        maven 'maven-3.9'
    }

    environment {
        // Servidor PostgreSQL del pipeline (contenedor postgres-ci) y base propia de este pipeline,
        // para no interferir con la base dbgestor que usa el pipeline del backend
        PGHOST = 'postgres-ci'
        PGUSER = 'postgres'
        PGPASSWORD = 'postgres'
        BD = 'dbgestor_ci'
        DB_URL = 'jdbc:postgresql://postgres-ci:5432/dbgestor_ci?stringtype=unspecified'
        DB_USER = 'postgres'
        DB_PASSWORD = 'postgres'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Versiones') {
            steps {
                sh 'psql --version'
                sh 'sqlfluff --version'
                sh 'psql -d postgres -tAc "SELECT version()"'
            }
        }
        stage('Análisis estático SQL (SQLFluff)') {
            steps {
                // Reglas en .sqlfluff (dialecto postgres). Falla si algún script tiene hallazgos.
                sh '''
                    sqlfluff lint sql/postgres --format json --write-output sqlfluff-reporte.json || true
                    sqlfluff lint sql/postgres -v > sqlfluff-reporte.txt; resultado=$?
                    cat sqlfluff-reporte.txt
                    exit $resultado
                '''
            }
        }
        stage('Base de datos limpia') {
            steps {
                sh '''
                    psql -d postgres -v ON_ERROR_STOP=1 -c "DROP DATABASE IF EXISTS $BD WITH (FORCE)"
                    psql -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE $BD"
                '''
            }
        }
        stage('Migración (esquema y datos)') {
            steps {
                sh '''
                    psql -d $BD -v ON_ERROR_STOP=1 -f sql/postgres/01_schema.sql
                    psql -d $BD -v ON_ERROR_STOP=1 -f sql/postgres/02_data.sql
                '''
            }
        }
        stage('Re-ejecución de scripts') {
            steps {
                // Los scripts deben poder aplicarse otra vez sin errores (idempotentes)
                sh '''
                    psql -d $BD -v ON_ERROR_STOP=1 -q -f sql/postgres/01_schema.sql
                    psql -d $BD -v ON_ERROR_STOP=1 -q -f sql/postgres/02_data.sql
                '''
            }
        }
        stage('Verificación de integridad') {
            steps {
                // BD-01 a BD-10: tablas, llaves primarias y foráneas, índices, huérfanos, datos, secuencia y controles negativos
                sh '''
                    psql -d $BD -v ON_ERROR_STOP=1 -f sql/postgres/05_verificacion_integridad.sql > verificacion-bd.txt 2>&1; resultado=$?
                    cat verificacion-bd.txt
                    exit $resultado
                '''
            }
        }
        stage('Pruebas de la API contra la base') {
            steps {
                // Pruebas de integración JU-26 a JU-32 (conexión, login, altas y fechas) sobre la base recién creada
                sh 'mvn -B test -Dtest=ConexionPostgresTest -Dsurefire.failIfNoSpecifiedTests=false'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Escaneo de secretos (Trivy)') {
            steps {
                // Busca contraseñas o tokens expuestos en los scripts SQL y la configuración (Dockerfile), sin carpetas target
                sh '''
                    trivy fs --scanners secret,misconfig --skip-dirs target --skip-dirs .mvn --format table --output trivy-basedatos.txt .
                    cat trivy-basedatos.txt
                    trivy fs --scanners secret,misconfig --skip-dirs target --skip-dirs .mvn --severity HIGH,CRITICAL --exit-code 1 --quiet .
                '''
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'sqlfluff-reporte.*, verificacion-bd.txt, trivy-basedatos.txt', allowEmptyArchive: true
        }
    }
}
