# Jenkins CI/CD Pipeline With Docker

This guide explains how to run a Jenkins CI/CD pipeline for this Spring Boot Maven project using Docker.

The repository now includes:

| File | Purpose |
| --- | --- |
| `Jenkinsfile` | Jenkins pipeline that tests, packages, builds a Docker image, and smoke-tests the container. |
| `Dockerfile` | Runtime image for the Spring Boot application. |
| `Dockerfile.jenkins` | Jenkins image with Docker CLI installed. |
| `docker-compose.jenkins.yml` | Local Jenkins server running in Docker. |
| `.dockerignore` | Keeps build context small and avoids copying generated files. |

## Pipeline Flow

The `Jenkinsfile` runs these stages:

1. `Checkout`: pulls the repository source.
2. `Test`: runs `./mvnw -B clean test`.
3. `Package`: runs `./mvnw -B -DskipTests package` and archives the jar.
4. `Build Docker Image`: builds `sb-concepts-lab:<build-number>`.
5. `Smoke Test Docker Image`: starts the image, checks `/actuator/health`, and verifies `/app/v1/test/message`.

Expected smoke-test response:

```text
test
```

## Prerequisites

Install these on the machine where you run Jenkins:

| Tool | Why |
| --- | --- |
| Docker Desktop or Docker Engine | Runs Jenkins and builds application images. |
| Git | Lets Jenkins clone this repository. |
| Java 17 | Required by the app if running outside Docker. |

For Windows with Docker Desktop:

1. Start Docker Desktop.
2. Use Linux containers.
3. Make sure Docker commands work:

```powershell
docker version
docker compose version
```

## Approach 1: Run Jenkins In Docker

This is the easiest local setup.

From the repository root:

```powershell
docker compose -f docker-compose.jenkins.yml up -d --build
```

Open Jenkins:

```text
http://localhost:8085
```

Get the initial admin password:

```powershell
docker exec sb-concepts-lab-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

During setup:

1. Install suggested plugins.
2. Create an admin user.
3. Keep the Jenkins URL as `http://localhost:8085/`.

Recommended plugins:

| Plugin | Purpose |
| --- | --- |
| Pipeline | Runs `Jenkinsfile`. |
| Git | Clones Git repositories. |
| Docker Pipeline | Useful for Docker-based pipeline extensions. |
| JUnit | Displays test results. |

## Create A Jenkins Pipeline Job

Use this when the repository is local or in GitHub.

1. Open Jenkins.
2. Click `New Item`.
3. Enter `sb-concepts-lab`.
4. Select `Pipeline`.
5. In `Pipeline`, select `Pipeline script from SCM`.
6. Choose `Git`.
7. Enter the repository URL.
8. Set branch, for example `*/main`.
9. Set script path to:

```text
Jenkinsfile
```

10. Save.
11. Click `Build Now`.

## Local Repository URL Options

If Jenkins runs in Docker, `D:\CursorWorkSpace\sb-concepts-lab` is not automatically visible inside the Jenkins container.

Use one of these options:

| Option | When To Use |
| --- | --- |
| Push to GitHub and use the GitHub URL | Best general approach. |
| Mount the repo into Jenkins container | Good for local-only experiments. |
| Use a bare local Git repository served over HTTP/SSH | Useful for advanced offline setups. |

For day-to-day usage, push this repository to GitHub and configure the Jenkins job with the GitHub clone URL.

## Approach 2: Test Docker Build Locally Without Jenkins

Run the full Maven test suite:

```powershell
.\mvnw.cmd clean test
```

Package the application:

```powershell
.\mvnw.cmd -DskipTests package
```

Build the Docker image:

```powershell
docker build -t sb-concepts-lab:local .
```

Run the container:

```powershell
docker run --rm -d `
  --name sb-concepts-lab-local `
  -e SPRING_PROFILES_ACTIVE=local `
  -e SERVER_PORT=8080 `
  -p 18080:8080 `
  sb-concepts-lab:local
```

`SERVER_PORT=8080` keeps the container listening on Docker's internal port `8080`, even when a Spring profile uses a different local workstation port.

Test health:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:18080/actuator/health"
```

Test API:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:18080/app/v1/test/message"
```

Expected API response:

```text
test
```

Stop the container:

```powershell
docker stop sb-concepts-lab-local
```

## Approach 3: Run Jenkins Directly On The Host

Use this if Jenkins is installed as a Windows or Linux service instead of running in Docker.

Required tools on the Jenkins host:

| Tool | Required Version |
| --- | --- |
| Java | 17 |
| Docker | Any supported recent version |
| Git | Any supported recent version |

For a Linux Jenkins agent, the existing `Jenkinsfile` can run as-is because it uses `sh`.

For a Windows-only Jenkins agent, convert `sh` steps to `bat` or `powershell`. Example:

```groovy
bat 'mvnw.cmd -B clean test'
bat 'mvnw.cmd -B -DskipTests package'
bat 'docker build -t %APP_IMAGE% .'
```

The Docker smoke test can also be converted to PowerShell using `Invoke-RestMethod`.

## Approach 4: Push Image To Docker Hub Or Private Registry

The current pipeline builds and tests a local image only. To publish the image, add a registry stage after `Build Docker Image` and before deployment.

Example Docker Hub stage:

```groovy
stage('Push Docker Image') {
    steps {
        withCredentials([usernamePassword(
            credentialsId: 'docker-hub',
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_PASSWORD'
        )]) {
            sh '''
                echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USER" --password-stdin
                docker tag "$APP_IMAGE" "$DOCKER_USER/sb-concepts-lab:$BUILD_NUMBER"
                docker tag "$APP_IMAGE" "$DOCKER_USER/sb-concepts-lab:latest"
                docker push "$DOCKER_USER/sb-concepts-lab:$BUILD_NUMBER"
                docker push "$DOCKER_USER/sb-concepts-lab:latest"
            '''
        }
    }
}
```

Create Jenkins credentials:

1. Open `Manage Jenkins`.
2. Open `Credentials`.
3. Add `Username with password`.
4. Set ID to `docker-hub`.
5. Use your Docker Hub username and access token.

## Approach 5: Deploy With Docker Compose

For a simple server deployment, create a deployment compose file on the target server:

```yaml
services:
  app:
    image: your-docker-user/sb-concepts-lab:latest
    container_name: sb-concepts-lab
    environment:
      SPRING_PROFILES_ACTIVE: prod
    ports:
      - "8083:8080"
    restart: unless-stopped
```

Deployment command:

```powershell
docker compose pull
docker compose up -d
```

In Jenkins, this is usually done through SSH to the target server after the image is pushed to a registry.

## Approach 6: GitHub Webhook Trigger

Use this after the Jenkins job works manually.

1. Install Jenkins GitHub plugin if needed.
2. In the Jenkins job, enable `GitHub hook trigger for GITScm polling`.
3. In GitHub repository settings, add a webhook:

```text
http://<jenkins-host>/github-webhook/
```

4. Content type: `application/json`.
5. Trigger: `Just the push event`.

If Jenkins runs only on your laptop, GitHub cannot reach it unless you expose it through a tunnel such as ngrok.

## Test The Complete Jenkins Setup

Manual verification checklist:

1. Jenkins starts at `http://localhost:8085`.
2. Jenkins can clone the repository.
3. `Test` stage shows passing JUnit results.
4. `Package` stage archives `target/sb-concepts-lab-0.0.1-SNAPSHOT.jar`.
5. Docker image builds successfully.
6. Smoke test passes against `/actuator/health`.
7. Smoke test confirms `/app/v1/test/message` returns `test`.

Local command equivalent:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd -DskipTests package
docker build -t sb-concepts-lab:local .
docker run --rm -d --name sb-concepts-lab-local -e SPRING_PROFILES_ACTIVE=local -e SERVER_PORT=8080 -p 18080:8080 sb-concepts-lab:local
Invoke-RestMethod -Method Get -Uri "http://localhost:18080/actuator/health"
Invoke-RestMethod -Method Get -Uri "http://localhost:18080/app/v1/test/message"
docker stop sb-concepts-lab-local
```

## Troubleshooting

### Docker Permission Denied In Jenkins

If Jenkins says it cannot access Docker:

```text
permission denied while trying to connect to the Docker daemon socket
```

For the included Dockerized Jenkins setup, `docker-compose.jenkins.yml` runs the container as `root` and mounts:

```text
/var/run/docker.sock:/var/run/docker.sock
```

Restart Jenkins after Docker Desktop starts:

```powershell
docker compose -f docker-compose.jenkins.yml restart
```

### Port 8080 Is Already Used

The Jenkins container maps Jenkins to host port `8085`, not `8080`, to avoid conflicts with the Spring Boot app.

Use:

```text
http://localhost:8085
```

The app smoke test maps the app to:

```text
http://localhost:18080
```

### Jenkins Cannot Find `Jenkinsfile`

Confirm the Jenkins job uses:

```text
Pipeline script from SCM
```

and script path:

```text
Jenkinsfile
```

### Jenkins Container Cannot See A Local Windows Path

Use a Git remote such as GitHub, or mount your workspace explicitly into the Jenkins container. A Git remote is cleaner and closer to real CI/CD.

### Maven Wrapper Permission On Linux

If Linux Jenkins cannot execute `mvnw`, run this once and commit the permission change:

```bash
chmod +x mvnw
```

On Windows, use:

```powershell
.\mvnw.cmd clean test
```

## Hinglish Summary

Jenkins pipeline ke liye important files add ho gaye hain:

```text
Jenkinsfile
Dockerfile
Dockerfile.jenkins
docker-compose.jenkins.yml
```

Jenkins local Docker me start karne ke liye:

```powershell
docker compose -f docker-compose.jenkins.yml up -d --build
```

Browser me open karo:

```text
http://localhost:8085
```

Pipeline run hone par Jenkins Maven tests chalata hai, jar banata hai, Docker image build karta hai, container start karta hai, aur health/API smoke test karta hai.
