# Maven CI/CD DevSecOps — Setup Guide

A complete setup guide for building a DevSecOps CI/CD pipeline using Jenkins, Docker, Trivy, SonarQube, Nexus, and AWS ECR.

---

## Table of Contents

- [Jenkins Server](#jenkins-server)
- [Jenkins Installation](#jenkins-installation)
- [Jenkins Initial Admin Password](#jenkins-initial-admin-password)
- [Trivy Installation](#trivy-installation)
- [Docker Installation](#docker-installation)
- [Jenkins Plugins to Install](#jenkins-plugins-to-install)
- [SonarQube Setup](#sonarqube-setup)
- [AWS IAM Setup](#aws-iam-setup)
- [Jenkins Credentials](#jenkins-credentials)
- [Jenkins Tools Configuration](#jenkins-tools-configuration)
- [Jenkins System Configuration](#jenkins-system-configuration)
- [Nexus Configuration in Jenkins Pipeline](#nexus-configuration-in-jenkins-pipeline)
- [AWS ECR Repository](#aws-ecr-repository)
- [Clean-up](#clean-up)

---

## Jenkins Server

| Spec | Value |
|---|---|
| Instance Type | `c5.xlarge` (4 vCPU, 8 GB RAM) |
| Storage | 30 GB EBS |
| OS | Ubuntu/Debian-like environment with sudo privileges |

### Security Group — Ports to Enable

| Service | Port |
|---|---|
| HTTP | 80 |
| HTTPS | 443 |
| SSH | 22 |
| Jenkins | 8080 |
| Nexus | 8081 |

---

## Jenkins Installation

```bash
#!/bin/bash

set -e

echo "===== Updating system ====="
apt update -y

echo "===== Installing Java (required for Jenkins) ====="
apt install -y fontconfig openjdk-21-jre

echo "===== Creating keyrings directory ====="
mkdir -p /etc/apt/keyrings

echo "===== Adding Jenkins GPG key ====="
wget -O /etc/apt/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian-stable/jenkins.io-2026.key

echo "===== Adding Jenkins repository ====="
echo "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" \
  > /etc/apt/sources.list.d/jenkins.list

echo "===== Updating package list ====="
apt update -y

echo "===== Installing Jenkins ====="
apt install -y jenkins

echo "===== Enabling & Starting Jenkins ====="
systemctl enable jenkins
systemctl start jenkins

echo "===== Checking Jenkins status ====="
systemctl status jenkins --no-pager

echo "===== Jenkins Installation Completed ====="
echo "Access Jenkins at: http://<EC2-PUBLIC-IP>:8080"
echo "Get admin password using:"
echo "cat /var/lib/jenkins/secrets/initialAdminPassword"
```

---

## Jenkins Initial Admin Password

```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

---

## Trivy Installation

```bash
sudo apt-get update
sudo apt-get install -y wget gnupg

wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | \
  gpg --dearmor | \
  sudo tee /usr/share/keyrings/trivy.gpg > /dev/null

echo "deb [signed-by=/usr/share/keyrings/trivy.gpg] https://aquasecurity.github.io/trivy-repo/deb generic main" | \
  sudo tee /etc/apt/sources.list.d/trivy.list

sudo apt-get update
sudo apt-get install -y trivy
```

---

## Docker Installation

### Add Docker's Official GPG Key

```bash
sudo apt-get update
sudo apt-get install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
```

### Add the Repository to Apt Sources

```bash
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
```

### Install Docker

```bash
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

### Add User to Docker Group

> Log out / in, or run `newgrp`, to apply the group change.

```bash
sudo usermod -aG docker $USER
newgrp docker
docker ps
```

### Grant Jenkins Access to Docker (if needed)

```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

### Check Docker Status

```bash
sudo systemctl status docker
```

---

## Jenkins Plugins to Install

- Eclipse Temurin Installer Plugin
- Email Extension Plugin
- SonarQube Scanner for Jenkins
- OWASP Dependency-Check Plugin
- Pipeline: Stage View Plugin
- NodeJS
- Nexus Artifact Uploader
- Pipeline Maven Integration
- Pipeline Utility Steps
- Amazon Web Services SDK :: All
- Amazon ECR
- Pipeline: AWS Steps
- Docker Pipeline
- CloudBees Docker Build and Publish
- OWASP ZAP

---

## SonarQube Setup

### Run SonarQube in a Docker Container

Image used: `sonarqube:25.10.0.114319-community`

```bash
docker run -d --name sonarqube \
  -p 9000:9000 \
  -v sonarqube_data:/opt/sonarqube/data \
  -v sonarqube_logs:/opt/sonarqube/logs \
  -v sonarqube_extensions:/opt/sonarqube/extensions \
  sonarqube:25.9.0.112764-community
```

---

## AWS IAM Setup

1. Create a normal AWS IAM account: `jenkins`
2. Attach policy: `AmazonEC2ContainerRegistryFullAccess`
3. Create security credentials (access key/secret key are shown **only once** — save them securely)

---

## Jenkins Credentials

| Purpose | ID | Type | Notes |
|---|---|---|---|
| Email | `mail-cred` | Username/app password | |
| SonarQube | `sonar-token` | Secret text | From SonarQube application |
| Nexus | `nexuslogin` | Username/app password | |
| AWS | `awscreds` | Username/app password | Secret key / access key |

**Webhook example:**
```
http://<jenkins-ip>:8080/sonarqube-webhook/
```

---

## Jenkins Tools Configuration

| Tool | Configuration |
|---|---|
| JDK | `JDK17`, `JDK21` |
| SonarQube Scanner | `sonar-scanner` |
| NodeJS | `node16` |
| Dependency-Check | `dp-check` |
| Maven | `MAVEN3` |

---

## Jenkins System Configuration

### SonarQube Servers

| Field | Value |
|---|---|
| Name | `sonar-server` |
| URL | `http://sonar-ip-address:9000` |
| Credentials | Add from Jenkins credentials |

### Extended E-mail Notification

| Field | Value |
|---|---|
| SMTP server | `smtp.gmail.com` |
| SMTP Port | `465` |
| Use SSL | Yes |
| Default user e-mail suffix | `@gmail.com` |

### E-mail Notification

| Field | Value |
|---|---|
| SMTP server | `smtp.gmail.com` |
| Default user e-mail suffix | `@gmail.com` |
| Use SMTP Authentication | Yes |
| User Name | `example@gmail.com` |
| Password | Use credentials |
| Use TLS | Yes |
| SMTP Port | `587` |
| Reply-To Address | `example@gmail.com` |

---

## Nexus Configuration in Jenkins Pipeline

Update the following details in the Jenkins pipeline:

- IP address
- Port
- Artifact repo ID: `vprofile-release`

**Steps to create the repository in Nexus:**

1. Go to the Nexus Server
2. Navigate to **Settings → Repositories → Create Repository**
3. Select **maven2 (hosted)**
4. Name it `vprofile-repo`
5. Click **Create Repository**

---

## AWS ECR Repository

Create a repository in ECR:

```
vprofileappimg
```

---

## Clean-up

After the pipeline/demo is complete, clean up the following resources to avoid unnecessary costs:

- [ ] 2x EC2 instances
- [ ] ECR repository
- [ ] Delete the IAM `jenkins` user
