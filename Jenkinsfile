// Pipeline de la API REST: compila, prueba y empaqueta con JDK 21 y Maven 3.9 configurados en Jenkins
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
                // 'SonarQube' es el nombre del servidor en Manage Jenkins > System > SonarQube servers
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn -B sonar:sonar'
                }
            }
        }
        stage('Quality Gate') {
            steps {
                // SonarQube responde por webhook (http://jenkins-gestor:8080/sonarqube-webhook/)
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Empaquetar') {
            steps {
                sh 'mvn -B package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }
}
