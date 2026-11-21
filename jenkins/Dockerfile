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
 && apt-get install -y --no-install-recommends postgresql-client python3 python3-venv jq unzip \
 && rm -rf /var/lib/apt/lists/* \
 && python3 -m venv /opt/sqlfluff \
 && /opt/sqlfluff/bin/pip install --no-cache-dir "sqlfluff==${SQLFLUFF_VERSION}" \
 && ln -s /opt/sqlfluff/bin/sqlfluff /usr/local/bin/sqlfluff

# ---- Unidad III: IaC, despliegue en Render, SAST y rendimiento ----
# Terraform: aprovisiona la infraestructura en Render (repo gestor-infra)
ARG TERRAFORM_VERSION=1.17.1
RUN curl -fsSL -o /tmp/terraform.zip "https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip" \
 && unzip -q /tmp/terraform.zip terraform -d /usr/local/bin \
 && rm /tmp/terraform.zip
# Ansible (configuración: base de datos, verificación y monitoreo), ansible-lint y Semgrep (SAST)
ARG ANSIBLE_CORE_VERSION=2.21.6
ARG ANSIBLE_LINT_VERSION=26.10.1
ARG SEMGREP_VERSION=1.183.0
RUN python3 -m venv /opt/ansible \
 && /opt/ansible/bin/pip install --no-cache-dir "ansible-core==${ANSIBLE_CORE_VERSION}" "ansible-lint==${ANSIBLE_LINT_VERSION}" \
 && ln -s /opt/ansible/bin/ansible /opt/ansible/bin/ansible-playbook /opt/ansible/bin/ansible-galaxy /opt/ansible/bin/ansible-lint /usr/local/bin/ \
 && python3 -m venv /opt/semgrep \
 && /opt/semgrep/bin/pip install --no-cache-dir "semgrep==${SEMGREP_VERSION}" \
 && ln -s /opt/semgrep/bin/semgrep /usr/local/bin/semgrep
# k6: pruebas de carga contra los servicios desplegados
ARG K6_VERSION=2.3.0
RUN curl -fsSL "https://github.com/grafana/k6/releases/download/v${K6_VERSION}/k6-v${K6_VERSION}-linux-amd64.tar.gz" \
      | tar -xz --strip-components=1 -C /usr/local/bin "k6-v${K6_VERSION}-linux-amd64/k6"
# Cliente de Docker + Compose: Ansible levanta el stack de monitoreo en el Docker del equipo
# (el socket se monta en docker-compose.yml)
ARG DOCKER_VERSION=29.8.0
ARG COMPOSE_VERSION=5.7.0
RUN curl -fsSL "https://download.docker.com/linux/static/stable/x86_64/docker-${DOCKER_VERSION}.tgz" \
      | tar -xz --strip-components=1 -C /usr/local/bin docker/docker \
 && mkdir -p /usr/local/lib/docker/cli-plugins \
 && curl -fsSL -o /usr/local/lib/docker/cli-plugins/docker-compose \
      "https://github.com/docker/compose/releases/download/v${COMPOSE_VERSION}/docker-compose-linux-x86_64" \
 && chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
USER jenkins

# Plugins: pipelines, git, reportes de pruebas y cobertura, configuración como código
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# La configuración (herramientas, usuario y jobs) se carga desde casc.yaml, sin asistente inicial
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
ENV CASC_JENKINS_CONFIG=/var/jenkins_home/casc.yaml

# Docker marca el contenedor como "unhealthy" si Jenkins deja de responder (hallazgo Trivy DS-0026)
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 CMD curl -fs http://localhost:8080/login > /dev/null || exit 1
