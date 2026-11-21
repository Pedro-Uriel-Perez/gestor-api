// Pipeline BACKEND (API REST) con JDK 21 y Maven 3.9, priorizando la calidad (CI/CD):
// compila → pruebas (JUnit + JaCoCo) → SonarQube + Quality Gate → escaneo de vulnerabilidades (Trivy)
// → SAST (Semgrep) → empaqueta → despliega en Render solo si todo lo anterior pasó
pipeline {
    agent any

    tools {
        jdk 'jdk-21'          // Manage Jenkins > Tools > JDK
        maven 'maven-3.9'     // Manage Jenkins > Tools > Maven
    }

    environment {
        // Base de datos de pruebas (contenedor postgres-ci de jenkins/docker-compose.yml)
        DB_URL = 'jdbc:postgresql://postgres-ci:5432/dbgestor?stringtype=unspecified'
        DB_USER = 'postgres'
        DB_PASSWORD = 'postgres'
    }

    options {
        timestamps()
    }

    stages {
        stage('Versiones') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
            }
        }
        stage('Compilar') {
            steps {
                sh 'mvn -B clean compile'
            }
        }
        stage('Pruebas') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco execPattern: 'target/jacoco.exec', classPattern: 'target/classes', sourcePattern: 'src/main/java'
                }
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {    // Manage Jenkins > System > SonarQube servers
                    sh 'mvn -B sonar:sonar'
                }
            }
        }
        stage('Quality Gate') {
            steps {
                // SonarQube avisa el resultado por webhook; si el Quality Gate no pasa, el pipeline se detiene
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Escaneo de vulnerabilidades (Trivy)') {
            steps {
                // Dependencias del pom.xml, secretos y Dockerfile; se excluyen las carpetas target y .mvn.
                // --offline-scan usa las dependencias ya descargadas por Maven (~/.m2) en lugar de consultar Maven Central.
                // Los hallazgos revisados y aceptados están justificados en .trivyignore.
                // El pipeline falla si queda alguna vulnerabilidad HIGH o CRITICAL sin atender.
                sh '''
                    trivy fs --offline-scan --scanners vuln,secret,misconfig --skip-dirs target --skip-dirs .mvn \
                         --ignorefile .trivyignore --format table --output trivy-gestor-api.txt .
                    cat trivy-gestor-api.txt
                    trivy fs --offline-scan --scanners vuln,secret,misconfig --skip-dirs target --skip-dirs .mvn \
                         --ignorefile .trivyignore --severity HIGH,CRITICAL --exit-code 1 --quiet .
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-gestor-api.txt', allowEmptyArchive: true
                }
            }
        }
        stage('SAST (Semgrep)') {
            steps {
                // Reglas de seguridad para Java/Spring, OWASP Top 10, secretos, JavaScript y Dockerfile.
                // Los hallazgos revisados se justifican en el código con "nosemgrep"; si queda alguno, falla.
                sh '''
                    semgrep scan --metrics=off --config p/java --config p/secrets --config p/dockerfile \
                        --config p/owasp-top-ten --config p/security-audit --config p/javascript \
                        --exclude target --text-output=semgrep-gestor-api.txt \
                        --sarif-output=semgrep-gestor-api.sarif --error .
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: 'semgrep-gestor-api.*', allowEmptyArchive: true
                }
            }
        }
        stage('Empaquetar') {
            steps {
                sh 'mvn -B package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        stage('Desplegar en Render') {
            environment {
                RENDER_API_KEY = credentials('render-api-key')
                SERVICIO = 'gestor-api'
            }
            steps {
                // Render construye la imagen con el Dockerfile del mismo commit que pasó todas las etapas
                // (el auto-deploy está apagado en Terraform). Después se verifica que responda UP.
                sh '''#!/bin/bash
                    set -euo pipefail
                    API=https://api.render.com/v1
                    AUTH="Authorization: Bearer $RENDER_API_KEY"
                    ID=$(curl -fsS -H "$AUTH" "$API/services?name=$SERVICIO&limit=1" | jq -r '.[0].service.id')
                    URL=$(curl -fsS -H "$AUTH" "$API/services/$ID" | jq -r '.serviceDetails.url')
                    DEPLOY=$(jq -nc --arg c "$GIT_COMMIT" '{commitId: $c}' \
                        | curl -fsS -X POST -H "$AUTH" -H 'Content-Type: application/json' -d @- "$API/services/$ID/deploys" \
                        | jq -r '.id')
                    echo "Despliegue $DEPLOY del commit $GIT_COMMIT en $SERVICIO ($URL)"
                    ESTADO=""
                    for i in $(seq 1 90); do
                        ESTADO=$(curl -fsS -H "$AUTH" "$API/services/$ID/deploys/$DEPLOY" | jq -r '.status')
                        echo "  $(date +%H:%M:%S) $ESTADO"
                        case "$ESTADO" in live) break ;; *failed|canceled|deactivated) exit 1 ;; esac
                        sleep 20
                    done
                    [ "$ESTADO" = live ] || { echo "El despliegue no terminó a tiempo"; exit 1; }
                    curl -fsS --retry 20 --retry-delay 15 --retry-all-errors "$URL/actuator/health"; echo
                '''
            }
        }
    }
}
