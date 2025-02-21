FROM jenkins/jenkins:lts

# Switch to root for installation tasks
USER root

# Install necessary dependencies
RUN apt-get update && apt-get install -y git maven ca-certificates curl gnupg2

# Install Docker for the Jenkins container
RUN apt-get update \
    && apt-get install -y ca-certificates curl gnupg \
    && install -m 0755 -d /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/ubuntu/gpg | tee /etc/apt/keyrings/docker.asc > /dev/null \
    && chmod a+r /etc/apt/keyrings/docker.asc \
    && echo "deb [arch=amd64] https://download.docker.com/linux/ubuntu focal stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null \
    && apt-get update \
    && apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin


# Add the Jenkins user to the Docker group
RUN usermod -aG docker jenkins

# Switch back to the Jenkins user
USER jenkins

# Install Jenkins plugins
RUN jenkins-plugin-cli --plugins \
    workflow-aggregator \
    git \
    ansicolor

# Expose Jenkins default port
EXPOSE 8080