# Runtime Profiles and LocalStack Guide

This project uses Spring Boot profiles to keep environment-specific settings separate from the application code. The current application exposes a small test API, so these profiles are intentionally lightweight and ready for later upgrades.

## Profile Files

| Profile | File | Port | Purpose |
| --- | --- | ---: | --- |
| default | `src/main/resources/application.yaml` | 8080 | Shared base configuration used by every run. |
| local | `src/main/resources/application-local.yaml` | 8080 | Daily local development on a workstation. |
| dev | `src/main/resources/application-dev.yaml` | 8081 | Shared development or early integration checks. |
| qa | `src/main/resources/application-qa.yaml` | 8082 | QA verification before production-like runs. |
| prod | `src/main/resources/application-prod.yaml` | 8083 | Production simulation with AWS-style settings and no real AWS calls. |
| localstack | `src/main/resources/application-localstack.yaml` | 8090 | Local AWS-compatible testing with Docker LocalStack. |

Spring always loads `application.yaml` first. When a profile is active, Spring overlays the matching `application-{profile}.yaml` file on top of the base configuration.

## Run the Application

Run these commands from the repository root.

### Default Profile

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts on port `8080`.

### Local Profile

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

The application starts on port `8080`.

### Dev Profile

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

The application starts on port `8081`.

### QA Profile

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=qa"
```

The application starts on port `8082`.

### Prod Profile

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

The application starts on port `8083`.

The `prod` profile also loads dummy AWS cloud values:

```yaml
app:
  aws:
    region: us-east-1
    account-id: "123456789012"
    s3:
      endpoint: aws-managed
      bucket-prefix: sb-concepts-lab-prod
```

These values are intentionally safe placeholders. They let the code behave as if it is running in an AWS-backed environment while avoiding any real cloud calls or credentials.

## Test the Existing API

The current controller is available under `/app/v1/test`.

Use the port for whichever profile is running.

### GET Test

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/app/v1/test/message"
```

Expected response:

```text
test
```

### POST Test

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/app/v1/test?string=local-profile"
```

Expected response:

```text
Hello Post string: local-profile
```

For `dev`, `qa`, `prod`, or `localstack`, replace the port in the URL with the profile port from the table.

### Cloud Simulation GET Test

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8083/app/v1/test/cloud"
```

Expected response when the `prod` profile is active:

```text
Cloud environment: aws-prod-simulation, account: 123456789012, region: us-east-1, s3 endpoint: aws-managed, bucket prefix: sb-concepts-lab-prod
```

### Cloud Simulation POST Test

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8083/app/v1/test/cloud?string=prod-s3-test"
```

Expected response when the `prod` profile is active:

```text
AWS production simulation accepted message for S3 workflow: prod-s3-test
```

## Run with LocalStack

LocalStack gives the project a local AWS-compatible endpoint. The current code does not call S3 yet, but this profile and Docker service create a clean path for the next upgrade.

Start LocalStack:

```powershell
docker compose -f docker-compose.localstack.yml up -d
```

Run the application with the `localstack` profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=localstack"
```

The application starts on port `8090`, and the profile exposes these future AWS settings:

```yaml
app:
  aws:
    region: us-east-1
    access-key: test
    secret-key: test
    s3:
      endpoint: http://localhost:4566
      bucket-prefix: sb-concepts-lab
```

Test the current API through the LocalStack profile:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8090/app/v1/test/message"
Invoke-RestMethod -Method Post -Uri "http://localhost:8090/app/v1/test?string=localstack-profile"
Invoke-RestMethod -Method Get -Uri "http://localhost:8090/app/v1/test/cloud"
Invoke-RestMethod -Method Post -Uri "http://localhost:8090/app/v1/test/cloud?string=localstack-s3-test"
```

Stop LocalStack when finished:

```powershell
docker compose -f docker-compose.localstack.yml down
```

## Recommended Upgrade Path

When the S3-like feature is added, keep the same profile structure:

- Use `local` for filesystem-backed local S3 behavior.
- Use `localstack` for AWS SDK integration against `http://localhost:4566`.
- Keep `dev`, `qa`, and `prod` ready for environment-specific storage values.
- Keep test APIs simple with `GET` and `POST` until the project intentionally expands the API contract.

## Hinglish Summary

Is project me Spring Boot profiles ka use environment-specific configuration separate rakhne ke liye hota hai. `application.yaml` base configuration hai, aur `application-local.yaml`, `application-dev.yaml`, `application-qa.yaml`, `application-prod.yaml`, aur `application-localstack.yaml` uske upar profile-specific values apply karte hain.

- `local` profile daily development ke liye hai. Isme app normally port `8080` par run hoti hai.
- `dev` shared development testing ke liye hai, jaha team integration checks kar sakti hai.
- `qa` release verification ke liye hai, jaha behavior stable aur repeatable hona chahiye.
- `prod` real production jaisa simulation deta hai, lekin abhi safe dummy AWS-style values use karta hai.
- `localstack` local machine par AWS-compatible testing ke liye hai, jaha Docker LocalStack endpoint `http://localhost:4566` use hota hai.

Run karte time profile command me pass karo:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=localstack"
```

LocalStack use karne ke liye pehle Docker service start karo:

```powershell
docker compose -f docker-compose.localstack.yml up -d
```

Short recommendation: local development ke liye `local`, AWS-like local testing ke liye `localstack`, team testing ke liye `dev` aur `qa`, aur production simulation ke liye `prod` profile use karo. Real secrets kabhi YAML me hard-code mat karo.
