# Logging Approach for Local, Dev, QA, Prod, LocalStack, and Microservices

This guide explains a practical logging approach for this Spring Boot project and for future monolithic or microservices applications. The goal is to make logs easy to see locally, useful in shared environments, and searchable in production without exposing secrets.

## Goals

- Use one logging API in code: SLF4J through Spring Boot's default Logback support.
- Write structured logs in shared environments so log platforms can search by fields.
- Keep local logs readable for developers.
- Include enough context to debug requests across services.
- Avoid logging secrets, tokens, passwords, full credentials, or sensitive payloads.
- Centralize logs outside the application in dev, QA, prod, and multi-service deployments.

## Recommended Baseline

Spring Boot already includes SLF4J and Logback through the web starter, so application code should use `org.slf4j.Logger` or Lombok's `@Slf4j` if Lombok is later added.

Example without Lombok:

```java
private static final Logger log = LoggerFactory.getLogger(MyService.class);

log.info("Created order with id={}", orderId);
log.warn("Payment provider returned retryable status for orderId={} status={}", orderId, status);
log.error("Failed to create order for customerId={}", customerId, exception);
```

Use log levels consistently:

| Level | Use For |
| --- | --- |
| `ERROR` | Failures that need action or break a request/job. |
| `WARN` | Recoverable problems, retries, degraded behavior, unusual conditions. |
| `INFO` | Important business or lifecycle events. |
| `DEBUG` | Developer troubleshooting details. |
| `TRACE` | Very detailed diagnostics, usually disabled. |

## Environment Strategy

| Environment | Format | Level | Where to See Logs | Recommended Storage |
| --- | --- | --- | --- | --- |
| local | Human-readable console | `INFO`, package `DEBUG` when needed | IDE terminal or PowerShell | No long-term storage required |
| localstack | Human-readable or JSON console | `INFO`, AWS SDK `WARN` | App terminal plus Docker logs | Optional local file or OpenSearch |
| dev | JSON console | `INFO`, selected package `DEBUG` temporarily | Central log tool and container logs | Short retention, for example 7-14 days |
| qa | JSON console | `INFO` | Central log tool and release verification dashboards | Medium retention, for example 14-30 days |
| prod | JSON console | `INFO`, no broad `DEBUG` | Central log platform with alerts | Longer retention based on compliance |

For containers and cloud deployments, prefer writing logs to `stdout` and `stderr`. Let Docker, Kubernetes, ECS, CloudWatch Agent, Fluent Bit, Filebeat, or another collector ship logs to the central platform.

## Spring Boot Configuration

Keep shared logging defaults in `src/main/resources/application.yaml`, then override only the differences in profile files.

Implemented base configuration in `src/main/resources/application.yaml`:

```yaml
logging:
  level:
    root: INFO
    sb.concepts.lab: INFO
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %-5level [${spring.application.name:application}] [traceId=%X{traceId:-} requestId=%X{requestId:-}] %logger{36} - %msg%n%ex"
```

Implemented local override in `application-local.yaml`:

```yaml
logging:
  level:
    root: INFO
    sb.concepts.lab: DEBUG
```

Implemented dev override in `application-dev.yaml`:

```yaml
logging:
  structured:
    format:
      console: logstash
  level:
    root: INFO
    sb.concepts.lab: INFO
```

Implemented QA override in `application-qa.yaml`:

```yaml
logging:
  structured:
    format:
      console: logstash
  level:
    root: INFO
    sb.concepts.lab: INFO
```

Implemented production override in `application-prod.yaml`:

```yaml
logging:
  structured:
    format:
      console: logstash
  level:
    root: INFO
    sb.concepts.lab: INFO
    org.springframework: WARN
```

Implemented LocalStack override in `application-localstack.yaml`:

```yaml
logging:
  structured:
    format:
      console: logstash
  level:
    root: INFO
    sb.concepts.lab: DEBUG
    software.amazon.awssdk: WARN
```

The `dev`, `qa`, `prod`, and `localstack` profiles use Spring Boot structured console logging with the `logstash` format. This produces JSON-style logs that are easier for central log platforms to parse and search.

## Implementation Steps Completed in This Project

1. Added shared logging defaults in `src/main/resources/application.yaml`.
   - Root logs are set to `INFO`.
   - Project package `sb.concepts.lab` is set to `INFO`.
   - Console pattern includes `traceId` and `requestId` from MDC.

2. Added environment-specific logging configuration.
   - `application-local.yaml` uses readable console logs and enables `DEBUG` for the project package.
   - `application-dev.yaml` uses structured `logstash` console logs and `INFO` level.
   - `application-qa.yaml` uses structured `logstash` console logs and `INFO` level.
   - `application-prod.yaml` uses structured `logstash` console logs, keeps the project package at `INFO`, and reduces Spring framework noise with `org.springframework: WARN`.
   - `application-localstack.yaml` uses structured `logstash` console logs, enables project `DEBUG`, and keeps AWS SDK logs at `WARN`.

3. Added request correlation support in `src/main/java/sb/concepts/lab/logging/RequestCorrelationFilter.java`.
   - Reads `X-Correlation-Id` or `X-Request-Id` if the caller sends one.
   - Creates a new UUID request ID if no incoming ID exists.
   - Reads the W3C `traceparent` header when available and extracts `traceId`.
   - Stores `requestId` and `traceId` in SLF4J MDC so every log line can include them.
   - Adds `X-Request-Id` and `X-Correlation-Id` to the HTTP response.
   - Logs request start and completion with method, path, status, and duration.

4. Added application logs in the existing API paths.
   - `TestController` now logs calls to test and cloud endpoints.
   - `DefaultCloudEnvironmentClient` logs default local cloud behavior at `DEBUG`.
   - `LocalStackCloudEnvironmentClient` logs LocalStack environment details without logging secrets.
   - `ProdAwsCloudEnvironmentClient` logs production simulation details without logging secrets.

5. Avoided sensitive logging.
   - Request bodies are not logged.
   - Request parameter values are not logged directly.
   - POST endpoints log only payload length.
   - Access keys and secret keys are not logged.

## How to See Logs Locally

Run the default or local profile from the repository root:

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Logs appear directly in the PowerShell or IDE terminal. During debugging, temporarily increase the project package to `DEBUG` in `application-local.yaml` or pass it as a command-line argument:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--logging.level.sb.concepts.lab=DEBUG"
```

## How to Test Local Logging

Start the application with the `local` profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Wait until the terminal shows that Tomcat started on port `8080`:

```text
Tomcat started on port 8080
Started SbConceptsLabApplication
```

In another PowerShell terminal, call the test APIs with a custom correlation ID:

```powershell
$headers = @{ 'X-Correlation-Id' = 'local-test-123' }

Invoke-RestMethod -Method Get -Uri "http://localhost:8080/app/v1/test/message" -Headers $headers
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/app/v1/test?string=hello-local-logging" -Headers $headers
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/app/v1/test/cloud" -Headers $headers
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/app/v1/test/cloud?string=cloud-local-logging" -Headers $headers
```

Expected API responses:

```text
test
Hello Post string: hello-local-logging
Cloud environment: disabled. The active profile uses local application behavior only.
Local profile received: cloud-local-logging
```

In the application terminal, you should see logs like these:

```text
INFO  [sb-concepts-lab] [traceId=local-test-123 requestId=local-test-123] RequestCorrelationFilter - Request started method=GET path=/app/v1/test/message
DEBUG [sb-concepts-lab] [traceId=local-test-123 requestId=local-test-123] TestController - Handling test message request
INFO  [sb-concepts-lab] [traceId=local-test-123 requestId=local-test-123] RequestCorrelationFilter - Request completed method=GET path=/app/v1/test/message status=200 durationMs=47
INFO  [sb-concepts-lab] [traceId=local-test-123 requestId=local-test-123] TestController - Handling test post request payloadLength=19
INFO  [sb-concepts-lab] [traceId=local-test-123 requestId=local-test-123] TestController - Handling cloud environment description request
DEBUG [sb-concepts-lab] [traceId=local-test-123 requestId=local-test-123] DefaultCloudEnvironmentClient - Describing default local cloud environment
```

What this confirms:

- The app is running with the `local` profile.
- Local logs are readable in the terminal.
- `X-Correlation-Id` is copied into `traceId` and `requestId`.
- Each request logs start and completion.
- Controller and cloud service logs appear with the same correlation values.
- Request parameter values are not logged directly; only safe metadata like `payloadLength` is logged.

If you do not pass `X-Correlation-Id` or `X-Request-Id`, the application creates a UUID automatically and returns it in the response headers.

## How to See Logs with LocalStack

Start LocalStack:

```powershell
docker compose -f docker-compose.localstack.yml up -d
```

Run the app with the `localstack` profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=localstack"
```

See application logs in the app terminal. See LocalStack container logs with:

```powershell
docker logs sb-concepts-lab-localstack
docker logs -f sb-concepts-lab-localstack
```

For AWS SDK troubleshooting, keep SDK logs at `WARN` by default and only temporarily raise the specific AWS package to `DEBUG`. Do not leave broad AWS SDK debug logging enabled because it can be noisy and may expose request details.

## How to See Logs in Dev, QA, and Prod

In shared environments, the application should write structured logs to `stdout`. The runtime platform should collect and forward them.

Common options:

| Runtime | Collection Approach | Viewing Tool |
| --- | --- | --- |
| Docker Compose | Docker logging driver or Filebeat | `docker logs`, ELK/OpenSearch |
| Kubernetes | Fluent Bit, Fluentd, or Vector DaemonSet | Grafana Loki, ELK, OpenSearch, Datadog, Splunk |
| AWS ECS | `awslogs` driver or FireLens | CloudWatch Logs, OpenSearch, Datadog, Splunk |
| AWS EKS | Fluent Bit or CloudWatch Container Insights | CloudWatch Logs, OpenSearch, Grafana |
| VM | CloudWatch Agent, Filebeat, or Vector | CloudWatch Logs, ELK/OpenSearch |

Recommended production setup on AWS:

- Application writes JSON logs to `stdout`.
- Platform ships logs to CloudWatch Logs.
- CloudWatch subscription, Firehose, Fluent Bit, or vendor agent forwards logs to OpenSearch, Datadog, Splunk, or another long-term search platform if needed.
- Alerts are created from metrics and key log patterns, not from manually watching logs.
- Logs include service name, environment, version, trace ID, request ID, and error fields.

## Monolithic Application Logging

For a monolith, the best approach is usually simple:

- Use one application log stream.
- Use structured JSON logs in shared environments.
- Add request IDs and user/session-safe identifiers to every request log.
- Keep package-specific log levels so noisy modules can be adjusted without changing the whole app.
- Create dashboards for error count, request count, latency, and key business failures.
- Store logs centrally in dev, QA, and prod.

In a monolith, debugging is easier because one request usually stays inside one process. Correlation IDs are still valuable because they connect access logs, application logs, scheduled jobs, and downstream calls.

Best monolith approach:

1. Local: readable console logs.
2. Shared environments: JSON logs to `stdout`.
3. Central search: CloudWatch Logs, ELK/OpenSearch, Loki, Datadog, or Splunk.
4. Metrics and traces added through Micrometer and OpenTelemetry when the application grows.

## Microservices Logging

For microservices, the best approach is centralized, structured, and correlated logging.

Every service should include these fields in each log event:

| Field | Purpose |
| --- | --- |
| `service` | Which service emitted the log. |
| `environment` | local, dev, qa, prod, or localstack. |
| `version` | Deployed build or image version. |
| `traceId` | Connects logs across service calls. |
| `spanId` | Connects logs to distributed traces. |
| `requestId` | Connects logs for one incoming request. |
| `userId` or `tenantId` | Only when safe and not sensitive. |
| `operation` | Business or technical operation name. |
| `errorCode` | Stable error identifier for searching and alerting. |

Use OpenTelemetry for distributed tracing and Micrometer for metrics. Logs explain what happened, metrics show how often or how badly it is happening, and traces show where time was spent across services.

Best microservices approach:

1. Each service logs JSON to `stdout`.
2. A sidecar, daemon, or platform collector ships logs centrally.
3. All incoming requests receive or create a correlation ID.
4. Correlation IDs are propagated through HTTP headers and message metadata.
5. Logs, metrics, and traces share the same `traceId`.
6. Dashboards and alerts are built per service and per business flow.

## Correlation IDs

Use a request filter to read an incoming request ID header, create one if missing, and place it in the logging MDC.

Common headers:

- `X-Request-Id`
- `X-Correlation-Id`
- W3C `traceparent` for distributed tracing

For microservices, propagate the same ID to downstream HTTP calls, async messages, and scheduled work triggered by a request. In Spring Boot, this can be implemented with a servlet filter plus client interceptors, or through OpenTelemetry instrumentation.

## What Not to Log

Never log:

- Passwords, access keys, secret keys, tokens, OTPs, or API keys.
- Full authorization headers.
- Full credit card, bank, national ID, or personal health data.
- Raw request and response bodies unless explicitly approved and redacted.
- Full connection strings containing credentials.

Prefer stable identifiers and redacted values:

```text
Good: userId=12345 operation=createOrder status=FAILED errorCode=PAYMENT_TIMEOUT
Bad: full request body with card number, password, or token
```

## Recommended Tools

Good open-source or common platform choices:

| Need | Tools |
| --- | --- |
| Local viewing | IDE terminal, PowerShell, `docker logs` |
| Central logs | Grafana Loki, ELK, OpenSearch, CloudWatch Logs |
| Enterprise search | Datadog, Splunk, New Relic |
| Metrics | Micrometer, Prometheus, CloudWatch Metrics |
| Traces | OpenTelemetry, Jaeger, Tempo, AWS X-Ray |
| Log shipping | Fluent Bit, Vector, Filebeat, CloudWatch Agent |

For this project, a strong next step would be:

1. Keep console logs readable for `local`.
2. Add JSON Logback configuration for `dev`, `qa`, `prod`, and optionally `localstack`.
3. Add request correlation with MDC.
4. Add OpenTelemetry when there are multiple services or external dependencies to trace.

## Final Recommendation

For a monolithic application, use Spring Boot with SLF4J/Logback, readable local logs, JSON logs in shared environments, and one central log platform.

For a microservices application, use the same base approach but make correlation mandatory. Centralized JSON logs, distributed tracing, metrics, and shared `traceId` values are the best way to debug requests that cross service boundaries.

## Hinglish Summary

Logging ka main purpose ye hai ki application me kya ho raha hai, error kaha aa raha hai, aur request ka flow kaise chal raha hai, ye easily samajh aaye.

- Local environment me logs simple aur readable console format me rakho. Developer PowerShell ya IDE terminal me directly logs dekh sakta hai.
- LocalStack me application logs terminal me dikhenge, aur LocalStack service ke logs `docker logs sb-concepts-lab-localstack` se dekh sakte ho.
- Dev, QA, aur Prod environments me logs JSON format me `stdout` par likhna best hai, phir Docker, Kubernetes, ECS, Fluent Bit, CloudWatch Agent, ya Filebeat jaise tools un logs ko central platform me bhejte hain.
- Prod me broad `DEBUG` logs enable nahi karne chahiye. Sirf `INFO`, `WARN`, aur `ERROR` logs rakho, aur debugging ke liye temporary package-specific `DEBUG` use karo.
- Secrets, passwords, tokens, access keys, authorization headers, aur sensitive request/response body kabhi log nahi karni chahiye.

Monolithic application ke liye best approach simple hai: ek central log stream, readable local logs, shared environments me JSON logs, aur CloudWatch, ELK/OpenSearch, Loki, Datadog, ya Splunk jaisa central logging tool.

Microservices application ke liye best approach hai centralized structured logging with correlation. Har service ke logs me `service`, `environment`, `version`, `traceId`, `requestId`, aur `errorCode` jaise fields hone chahiye. Isse ek request multiple services me travel kare tab bhi uska complete flow trace karna easy hota hai.

Short recommendation: local me readable logs rakho, dev/qa/prod me JSON logs central platform par bhejo, microservices me correlation ID aur distributed tracing mandatory rakho.
