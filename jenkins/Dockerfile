# Jenkins LTS con JDK 21 + Maven 3.9.11 instalados para compilar los microservicios en Java 21
FROM jenkins/jenkins:lts-jdk21

ARG MAVEN_VERSION=3.9.11
ARG TRIVY_VERSION=0.76.1

USER root
# Maven en /opt/maven (se registra en Jenkins como la herramienta "maven-3.9")
RUN curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
      | tar -xz -C /opt \
 && ln -s "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven
# Trivy: escáner de vulnerabilidades (dependencias, secretos y configuración)
RUN curl -fsSL "https://github.com/aquasecurity/trivy/releases/download/v${TRIVY_VERSION}/trivy_${TRIVY_VERSION}_Linux-64bit.tar.gz" \
      | tar -xz -C /usr/local/bin trivy
# Pipeline de base de datos: cliente psql para aplicar y verificar los scripts, y SQLFluff para su análisis estático
ARG SQLFLUFF_VERSION=4.5.0
RUN apt-get update \
 && apt-get install -y --no-install-recommends postgresql-client python3 python3-venv \
 && rm -rf /var/lib/apt/lists/* \
 && python3 -m venv /opt/sqlfluff \
 && /opt/sqlfluff/bin/pip install --no-cache-dir "sqlfluff==${SQLFLUFF_VERSION}" \
 && ln -s /opt/sqlfluff/bin/sqlfluff /usr/local/bin/sqlfluff
USER jenkins

# Plugins: pipelines, git, reportes de pruebas y cobertura, configuración como código
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# La configuración (herramientas, usuario y jobs) se carga desde casc.yaml, sin asistente inicial
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
ENV CASC_JENKINS_CONFIG=/var/jenkins_home/casc.yaml

# Docker marca el contenedor como "unhealthy" si Jenkins deja de responder (hallazgo Trivy DS-0026)
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 CMD curl -fs http://localhost:8080/login > /dev/null || exit 1
