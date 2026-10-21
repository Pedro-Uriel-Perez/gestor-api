# Jenkins LTS con JDK 21 + Maven 3.9.11 instalados para compilar los microservicios en Java 21
FROM jenkins/jenkins:lts-jdk21

ARG MAVEN_VERSION=3.9.11

USER root
# Maven en /opt/maven (se registra en Jenkins como la herramienta "maven-3.9")
RUN curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
      | tar -xz -C /opt \
 && ln -s "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven
USER jenkins

# Plugins: pipelines, git, reportes de pruebas y cobertura, configuración como código
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# La configuración (herramientas, usuario y jobs) se carga desde casc.yaml, sin asistente inicial
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
ENV CASC_JENKINS_CONFIG=/var/jenkins_home/casc.yaml
