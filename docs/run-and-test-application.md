# Run And Test The Application

This guide explains how to run this Spring Boot application and test it using Swagger UI, Postman, and PowerShell. It also explains what to do when a port is already used by another application such as Grafana.

## Quick Start

Run from the repository root:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

The local profile normally starts on:

```text
http://localhost:8080
```

Open Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Open OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Profile Ports

| Profile | Command | Port |
| --- | --- | ---: |
| default | `.\mvnw.cmd spring-boot:run` | 8080 |
| local | `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"` | 8080 |
| dev | `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"` | 8081 |
| qa | `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=qa"` | 8082 |
| prod | `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"` | 8083 |
| localstack | `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=localstack"` | 8090 |

If the expected port is already used, run the app on a temporary port:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--server.port=18080"
```

Then use:

```text
http://localhost:18080/swagger-ui/index.html
http://localhost:18080/app/v1/test/message
```

## Existing API Endpoints

The current controller is available under:

```text
/app/v1/test
```

Available endpoints:

| Method | URL | Expected Response |
| --- | --- | --- |
| GET | `/app/v1/test/message` | `test` |
| POST | `/app/v1/test?string=hello` | `Hello Post string: hello` |
| GET | `/app/v1/test/cloud` | Cloud/profile description |
| POST | `/app/v1/test/cloud?string=hello` | Cloud/profile echo response |

## Test With Swagger UI

Start the app:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Open:

```text
http://localhost:8080/swagger-ui/index.html
```

If using the temporary port:

```text
http://localhost:18080/swagger-ui/index.html
```

Test in Swagger:

1. Open `Test API` or the available controller section.
2. Expand `GET /app/v1/test/message`.
3. Click `Try it out`.
4. Click `Execute`.
5. Confirm the response body is:

```text
test
```

Test POST:

1. Expand `POST /app/v1/test`.
2. Click `Try it out`.
3. Set query parameter `string` to `hello-swagger`.
4. Click `Execute`.
5. Confirm the response body is:

```text
Hello Post string: hello-swagger
```

## Test With Postman

Create these requests in Postman.

### GET Message

Method:

```text
GET
```

URL:

```text
http://localhost:8080/app/v1/test/message
```

Expected response:

```text
test
```

### POST Message

Method:

```text
POST
```

URL:

```text
http://localhost:8080/app/v1/test?string=hello-postman
```

Expected response:

```text
Hello Post string: hello-postman
```

### GET Cloud Description

Method:

```text
GET
```

URL:

```text
http://localhost:8080/app/v1/test/cloud
```

Expected local response:

```text
Cloud environment: disabled. The active profile uses local application behavior only.
```

### POST Cloud Message

Method:

```text
POST
```

URL:

```text
http://localhost:8080/app/v1/test/cloud?string=cloud-postman
```

Expected local response:

```text
Local profile received: cloud-postman
```

If the app is running on port `18080`, replace `8080` with `18080`.

## Test With PowerShell

GET test:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/app/v1/test/message"
```

POST test:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/app/v1/test?string=hello-powershell"
```

Swagger/OpenAPI check:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/v3/api-docs"
```

Swagger UI HTTP status:

```powershell
(Invoke-WebRequest -Method Get -Uri "http://localhost:8080/swagger-ui/index.html").StatusCode
```

Expected status:

```text
200
```

## Test CORS

This project allows the local browser frontend origin:

```text
http://localhost:5173
```

It intentionally does not allow:

```text
http://localhost:3000
```

because port `3000` commonly points to Grafana login locally.

Allowed-origin preflight test:

```powershell
Invoke-WebRequest `
  -Method Options `
  -Uri "http://localhost:8080/app/v1/test/message" `
  -Headers @{
    "Origin" = "http://localhost:5173"
    "Access-Control-Request-Method" = "GET"
    "Access-Control-Request-Headers" = "X-Request-Id"
  }
```

Expected:

- Status is `200`.
- `Access-Control-Allow-Origin` is `http://localhost:5173`.

Grafana-origin rejection test:

```powershell
Invoke-WebRequest `
  -Method Options `
  -Uri "http://localhost:8080/app/v1/test/message" `
  -Headers @{
    "Origin" = "http://localhost:3000"
    "Access-Control-Request-Method" = "GET"
  }
```

Expected:

```text
403 Forbidden
```

## Troubleshooting

### ERR_CONNECTION_REFUSED

`ERR_CONNECTION_REFUSED` means nothing is listening on the URL and port you opened.

Check ports:

```powershell
foreach ($port in @(3000,5173,8080,18080)) {
  $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
  if ($conn) {
    $proc = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
    Write-Output "port=$port listening=true process=$($proc.ProcessName)"
  } else {
    Write-Output "port=$port listening=false"
  }
}
```

If `5173` is not listening, that is fine unless you are running a frontend dev server. `5173` is only the allowed browser origin for CORS; this Spring Boot app does not start a frontend server there.

### Swagger Or API Returns 404

If `http://localhost:8080/app/v1/test/message` returns `404`, the Java process on port `8080` may not be this project.

Run this project on a temporary port:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--server.port=18080"
```

Then test:

```text
http://localhost:18080/app/v1/test/message
http://localhost:18080/swagger-ui/index.html
```

### Grafana Login Appears

If you see Grafana login, you are probably opening or routing through:

```text
http://localhost:3000
```

This project does not use `3000` as the local frontend origin anymore. Use:

```text
http://localhost:5173
```

only when an actual frontend dev server is running there, or use the Spring Boot app directly:

```text
http://localhost:8080/swagger-ui/index.html
```

## Run Automated Tests

Run the full test suite:

```powershell
.\mvnw.cmd test
```

Expected result:

```text
BUILD SUCCESS
```

The tests verify:

- Spring context loads.
- OpenAPI docs are exposed.
- CORS allows `http://localhost:5173`.
- Unknown origins are rejected.
- Grafana default origin `http://localhost:3000` is rejected.

## Hinglish Summary

Application run karne ke liye repository root se command chalao:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Default local URL:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

API test:

```text
http://localhost:8080/app/v1/test/message
```

Expected response:

```text
test
```

Agar `8080` par koi aur Java app chal rahi hai ya API `404` de rahi hai, to temporary port use karo:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--server.port=18080"
```

Phir URLs:

```text
http://localhost:18080/swagger-ui/index.html
http://localhost:18080/app/v1/test/message
```

Postman me `GET`, `POST` requests bana kar same URLs hit kar sakte ho. Query parameter `string` POST requests me pass karna hai.

CORS ke liye local browser frontend origin:

```text
http://localhost:5173
```

`http://localhost:3000` use mat karo, kyunki local machine par ye commonly Grafana login page hota hai. Agar browser me `ERR_CONNECTION_REFUSED` aaye, iska matlab jis port ko open kar rahe ho us par koi server running nahi hai.

Final check ke liye:

```powershell
.\mvnw.cmd test
```

`BUILD SUCCESS` aana chahiye.
