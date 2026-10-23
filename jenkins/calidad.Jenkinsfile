// Pipeline de calidad y análisis de código de los dos microservicios:
// pruebas + cobertura JaCoCo → SonarQube + Quality Gate → escaneo de vulnerabilidades con Trivy (sin carpetas target)
pipeline {
    agent any

    tools {
        jdk 'jdk-21'
        maven 'maven-3.9'
    }

    environment {
        // Base de datos de pruebas de la API (contenedor postgres-ci)
        DB_URL = 'jdbc:postgresql://postgres-ci:5432/dbgestor?stringtype=unspecified'
        DB_USER = 'postgres'
        DB_PASSWORD = 'postgres'
    }

    options {
        timestamps()
        skipDefaultCheckout()   // los dos repositorios se descargan en la etapa Checkout
    }

    stages {
        stage('Checkout') {
            steps {
                // Clon parcial y superficial: solo el último commit y sin la carpeta de imágenes del frontend
                // (gifs y videos de ~300 MB que no se necesitan para compilar, probar ni analizar el código).
                retry(3) {
                    sh '''
                        rm -rf gestor-api gestor-frontend
                        git clone --depth 1 https://github.com/Pedro-Uriel-Perez/gestor-api.git gestor-api
                        git clone --depth 1 --filter=blob:none --no-checkout https://github.com/Pedro-Uriel-Perez/gestor-frontend.git gestor-frontend
                        cd gestor-frontend
                        git sparse-checkout set --no-cone '/*' '!/src/main/resources/static/Images/'
                        git checkout main
                        git log --oneline -1
                    '''
                }
                sh 'cd gestor-api && git log --oneline -1'
            }
        }
        stage('Pruebas y cobertura') {
            steps {
                dir('gestor-api') {
                    sh 'mvn -B clean test'
                }
                dir('gestor-frontend') {
                    sh 'mvn -B -Pci clean test'
                }
            }
            post {
                always {
                    junit 'gestor-*/target/surefire-reports/*.xml'
                    jacoco execPattern: 'gestor-*/target/jacoco.exec', classPattern: 'gestor-*/target/classes', sourcePattern: 'gestor-*/src/main/java'
                }
            }
        }
        stage('SonarQube Analysis: gestor-api') {
            steps {
                dir('gestor-api') {
                    withSonarQubeEnv('SonarQube') {
                        sh 'mvn -B sonar:sonar'
                    }
                }
            }
        }
        stage('Quality Gate: gestor-api') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('SonarQube Analysis: gestor-frontend') {
            steps {
                dir('gestor-frontend') {
                    withSonarQubeEnv('SonarQube') {
                        sh 'mvn -B -Pci sonar:sonar'
                    }
                }
            }
        }
        stage('Quality Gate: gestor-frontend') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Escaneo de vulnerabilidades (Trivy)') {
            steps {
                // Se analizan el código fuente, el pom.xml (dependencias), secretos y Dockerfiles.
                // Se excluyen las carpetas target (compilados) y .mvn; los hallazgos revisados y aceptados están en .trivyignore.
                // --offline-scan usa las dependencias ya descargadas por Maven (~/.m2) en lugar de consultar Maven Central.
                // El pipeline falla si queda alguna vulnerabilidad HIGH o CRITICAL sin atender.
                script {
                    for (repo in ['gestor-api', 'gestor-frontend']) {
                        dir(repo) {
                            sh """
                                trivy fs --offline-scan --scanners vuln,secret,misconfig --skip-dirs target --skip-dirs .mvn \
                                     --ignorefile .trivyignore --format table --output ../trivy-${repo}.txt .
                                cat ../trivy-${repo}.txt
                                trivy fs --offline-scan --scanners vuln,secret,misconfig --skip-dirs target --skip-dirs .mvn \
                                     --ignorefile .trivyignore --severity HIGH,CRITICAL --exit-code 1 --quiet .
                            """
                        }
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-*.txt', allowEmptyArchive: true
                }
            }
        }
    }
}
