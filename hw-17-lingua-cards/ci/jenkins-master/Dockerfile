FROM jenkins/jenkins:lts-jdk21

# Переключение на root для установки дополнительных пакетов
USER root

RUN apt-get update && \
    apt-get install -y docker.io && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Установка плагинов (можно через jenkins-plugin-cli)
RUN jenkins-plugin-cli --plugins \
    docker-workflow \
    pipeline-stage-view \
    git \
    blueocean \
    htmlpublisher \
    workflow-aggregator

# Возвращаемся к пользователю jenkins
USER jenkins