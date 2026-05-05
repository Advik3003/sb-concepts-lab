# CORS Complete Step-by-Step Tutorial

This tutorial explains CORS in both English and Hinglish. It covers what CORS is, where to use it, how this Spring Boot project implements it, how to test it, and how to debug cases where a request unexpectedly reaches a Grafana login URL.

## 1. What Is CORS?

CORS means Cross-Origin Resource Sharing.

It is a browser security rule. It controls whether JavaScript running on one origin can call an API on another origin.

An origin is made from three parts:

```text
scheme + host + port
```

Examples:

```text
http://localhost:3000
http://localhost:8080
https://api.example.com
https://grafana.example.com
```

These two are different origins because the ports are different:

```text
Frontend: http://localhost:3000
Backend:  http://localhost:8080
```

These two are also different origins because the hosts are different:

```text
Frontend: https://app.example.com
Backend:  https://api.example.com
```

### Hinglish

CORS ka full form hai Cross-Origin Resource Sharing. Ye browser ka security feature hai. Jab browser me loaded JavaScript kisi dusre origin ke API ko call karta hai, browser check karta hai ki API ne us origin ko allow kiya hai ya nahi.

Origin ka matlab hota hai:

```text
scheme + host + port
```

Example: `http://localhost:3000` aur `http://localhost:8080` same machine par hain, lekin port different hai. Isliye browser ke liye ye cross-origin request hai.

## 2. Why CORS Exists

Browsers protect users from malicious websites. Without CORS, a random website could use the user's browser to call another site where the user is logged in.

Example risk:

1. User is logged in to `https://bank.example.com`.
2. User opens malicious site `https://evil.example.com`.
3. Malicious JavaScript tries to call `https://bank.example.com/api/transfer`.
4. Browser blocks the cross-origin response unless the bank explicitly allows that origin.

CORS does not protect APIs from all clients. It mainly protects browser-based JavaScript access.

### Hinglish

CORS isliye hota hai kyunki browser user ko protect karta hai. Agar CORS na ho to koi bhi malicious website user ke browser se dusri website ke APIs call kar sakti hai, especially jaha user already logged in hai.

Important point: CORS browser enforce karta hai. PowerShell, curl, Postman, backend service, ya server-to-server calls par normally CORS apply nahi hota.

## 3. When CORS Is Needed

CORS is needed when a browser client calls an API from a different origin.

Common cases:

- React app at `http://localhost:3000` calls Spring Boot API at `http://localhost:8080`.
- Vite app at `http://localhost:5173` calls backend API at `http://localhost:8080`.
- Swagger UI hosted at `https://docs.example.com` calls API at `https://api.example.com`.
- Admin portal calls API from another domain.
- Grafana, developer portal, or API gateway redirects requests across different domains.

CORS is not usually needed when:

- Swagger UI and API are served from the same Spring Boot app.
- API is called from PowerShell, curl, Postman, or backend code.
- Browser calls the same scheme, host, and port.

### Hinglish

CORS tab chahiye jab browser me running frontend dusre origin ke backend ko call kare.

Example:

```text
React UI:    http://localhost:3000
Spring API:  http://localhost:8080
```

Yaha CORS chahiye.

Lekin agar Swagger UI aur API dono same app se serve ho rahe hain:

```text
Swagger UI: http://localhost:8080/swagger-ui/index.html
API:        http://localhost:8080/app/v1/test/message
```

To CORS ki zarurat nahi hoti.

## 4. Should I Use CORS By Project Type?

This is the most important decision point. CORS is not a feature that every application needs. CORS is needed mainly when a browser-based client calls your backend from a different origin.

### Quick Decision Table

| Project Type | Should You Configure CORS? | Why |
| --- | --- | --- |
| Backend API called by same-origin web app | Usually no | Browser sees same scheme, host, and port. |
| Backend API called by different-origin React/Angular/Vue app | Yes | Browser blocks cross-origin API responses without CORS. |
| Backend API called by mobile app | Usually no | Native mobile apps do not enforce browser CORS. |
| Backend API called by another backend service | No | Server-to-server calls do not use browser CORS. |
| Backend API called by Postman, curl, or PowerShell | No | These tools are not browsers. |
| Swagger UI served by same Spring Boot app | Usually no | Swagger UI and API are same origin. |
| Swagger UI hosted separately | Yes | Browser loads Swagger UI from one origin and calls API on another. |
| Public API for third-party browser apps | Yes, carefully | Allow only approved origins if authentication or sensitive data exists. |
| Internal microservice API only | Usually no | Services call each other server-to-server. |
| API behind gateway used by browser frontend | Usually yes at gateway or API | The browser-facing layer must return CORS headers. |
| Grafana dashboard calling custom API from browser | Sometimes yes | Needed only if Grafana frontend JavaScript calls your API cross-origin. |

### Backend Applications

For a pure backend application, CORS is not automatically required. A backend service does not need CORS just because it exposes REST APIs.

Use CORS in a backend only when that backend is called directly from browser JavaScript on a different origin.

Use CORS:

```text
React UI:  http://localhost:3000
Backend:   http://localhost:8080
```

Do not use CORS only for:

```text
Service A -> Service B
Postman -> Backend
PowerShell -> Backend
Mobile App -> Backend
Batch Job -> Backend
```

Those clients are not browser JavaScript clients, so CORS is not the deciding security control.

### Web Applications

Web applications are the most common place where CORS matters.

Use CORS when:

- Frontend and backend run on different local ports.
- Frontend and backend use different domains.
- Swagger UI is hosted separately from the API.
- Browser JavaScript sends `Authorization`, `Content-Type: application/json`, or custom headers.

Do not need CORS when:

- Frontend and backend are served from the same origin.
- A reverse proxy makes frontend and backend look same-origin to the browser.

Example no-CORS production shape:

```text
https://app.example.com/          -> frontend
https://app.example.com/api/**    -> backend through reverse proxy
```

Example CORS-required production shape:

```text
https://app.example.com           -> frontend
https://api.example.com           -> backend
```

### Mobile Applications

Native Android and iOS apps do not enforce browser CORS. They can call APIs directly without CORS headers.

For mobile apps, focus more on:

- Authentication.
- Authorization.
- TLS/HTTPS.
- Token handling.
- API rate limiting.
- Certificate pinning if required.

Do not add broad CORS rules only because a mobile app calls the API. CORS will not secure or enable native mobile calls.

### Desktop Applications

Native desktop applications usually do not need CORS. Electron apps may need more careful thinking because they can behave like browser-based apps depending on how requests are made.

Use CORS if the desktop app loads a browser page and browser JavaScript calls your API cross-origin.

Do not rely on CORS as the main security control for desktop apps.

### Server-To-Server And Microservices

Microservices normally do not need CORS between each other.

Example:

```text
order-service -> payment-service
user-service  -> notification-service
```

These are backend-to-backend calls. Use service authentication, network security, mTLS, IAM roles, or API gateway policies instead of CORS.

### API Gateway Or Reverse Proxy

If a browser frontend calls APIs through a gateway, CORS can be handled at one of two places:

1. At the API gateway or reverse proxy.
2. At the backend application.

Professional recommendation:

- Prefer handling CORS at the gateway when many backend services share one browser-facing API domain.
- Handle CORS in the backend when the backend is directly browser-facing.
- Avoid duplicate conflicting CORS headers from both gateway and backend.

### Grafana And Dashboards

Grafana itself is a web application. If a Grafana panel plugin or browser-side dashboard JavaScript calls your API from the user's browser, then CORS may be needed on your API.

But if Grafana backend queries a data source server-to-server, CORS is usually not needed.

If your API request reaches Grafana login, first check routing and authentication. That usually means the request went to the Grafana host or gateway route, not directly to the Spring Boot API.

### Hinglish

Sabse important clarity ye hai: CORS har backend project me blindly apply nahi karna chahiye. CORS tab use karo jab browser-based frontend alag origin se backend API call kar raha ho.

Backend application me CORS tab chahiye:

- React/Angular/Vue frontend different port/domain par hai.
- Swagger UI API se alag domain par hosted hai.
- Browser se API call ho rahi hai aur origin different hai.

Backend application me CORS normally nahi chahiye:

- API ko Postman, curl, PowerShell se test kar rahe ho.
- API ko mobile app call kar rahi hai.
- API ko dusra backend service call kar raha hai.
- Microservices ek dusre ko call kar rahe hain.

Web application me CORS common hai kyunki browser enforce karta hai. Agar frontend `http://localhost:3000` par hai aur backend `http://localhost:8080` par hai, to CORS chahiye. Agar reverse proxy dono ko same origin bana deta hai, to CORS ki zarurat nahi hoti.

Mobile app me CORS normally nahi chahiye. Android/iOS browser CORS policy enforce nahi karte jaise web browser karta hai. Mobile ke liye authentication, token security, HTTPS, rate limit, aur authorization important hain.

Microservices me CORS nahi chahiye, kyunki service-to-service call browser se nahi hoti. Waha mTLS, IAM, service auth, network policy, ya API gateway security use karo.

Grafana ke case me agar browser-side panel API ko call kar raha hai aur API different origin par hai, to CORS chahiye ho sakta hai. Lekin agar request Grafana login par ja rahi hai, to pehle routing, reverse proxy, gateway path, aur authentication check karo. Ye mostly CORS issue nahi, balki wrong URL ya redirect issue hota hai.

Simple rule:

```text
Browser + different origin + API response read karna hai = CORS chahiye.
Non-browser client = CORS normally nahi chahiye.
Same-origin browser call = CORS normally nahi chahiye.
```

## 5. Simple Request vs Preflight Request

For some cross-origin calls, the browser sends the real request directly. For other calls, the browser first sends an `OPTIONS` request. This is called a preflight request.

The browser sends preflight when the request uses things like:

- Non-simple methods such as `PUT`, `PATCH`, or `DELETE`.
- Custom headers such as `Authorization`, `X-Request-Id`, or `X-Correlation-Id`.
- JSON content type such as `application/json`.

Preflight asks the API:

```text
Can origin http://localhost:3000 send a GET request with header X-Request-Id?
```

If the API returns correct CORS headers, the browser sends the real request.

### Hinglish

Preflight ek permission check hai. Browser pehle `OPTIONS` request bhejta hai aur API se poochta hai:

```text
Kya http://localhost:3000 ko GET/POST/PUT call karne ki permission hai?
Kya X-Request-Id ya Authorization header bhejne ki permission hai?
```

Agar API allow karti hai tab browser actual request bhejta hai. Agar allow nahi karti to browser request block kar deta hai.

## 6. Important CORS Headers

Common CORS response headers:

| Header | Purpose |
| --- | --- |
| `Access-Control-Allow-Origin` | Which browser origin can read the response. |
| `Access-Control-Allow-Methods` | Which HTTP methods are allowed. |
| `Access-Control-Allow-Headers` | Which request headers are allowed. |
| `Access-Control-Expose-Headers` | Which response headers JavaScript can read. |
| `Access-Control-Allow-Credentials` | Whether cookies/auth credentials are allowed. |
| `Access-Control-Max-Age` | How long browser can cache preflight result. |

### Hinglish

In headers ka simple meaning:

- `Access-Control-Allow-Origin`: kaunsa frontend allowed hai.
- `Access-Control-Allow-Methods`: kaunse methods allowed hain, jaise `GET`, `POST`.
- `Access-Control-Allow-Headers`: frontend kaunse headers bhej sakta hai.
- `Access-Control-Expose-Headers`: frontend response me kaunse headers read kar sakta hai.
- `Access-Control-Allow-Credentials`: cookies ya browser credentials allow hain ya nahi.
- `Access-Control-Max-Age`: preflight result browser kitni der cache karega.

## 7. CORS In This Project

This project implements CORS using these files:

```text
src/main/java/sb/concepts/lab/config/CorsConfig.java
src/main/java/sb/concepts/lab/config/CorsProperties.java
src/main/resources/application.yaml
src/main/resources/application-dev.yaml
src/main/resources/application-qa.yaml
src/main/resources/application-prod.yaml
src/main/resources/application-localstack.yaml
```

The main CORS settings are in `application.yaml`:

```yaml
app:
  cors:
    allowed-origins:
      - http://localhost:3000
      - http://localhost:5173
    allowed-methods:
      - GET
      - POST
      - PUT
      - PATCH
      - DELETE
      - OPTIONS
    allowed-headers:
      - Authorization
      - Content-Type
      - X-Request-Id
      - X-Correlation-Id
    exposed-headers:
      - X-Request-Id
      - X-Correlation-Id
    allow-credentials: false
    max-age: 3600
```

`CorsConfig.java` applies these rules to:

```text
/app/**
/v3/api-docs/**
```

This means:

- Browser clients from allowed origins can call application APIs under `/app/**`.
- External Swagger UI can fetch `/v3/api-docs/**`.
- Response headers `X-Request-Id` and `X-Correlation-Id` are exposed to browser JavaScript.

### Hinglish

Is project me CORS hard-code nahi kiya gaya. Values YAML se aati hain through `app.cors`.

Main paths:

- `/app/**`: application APIs ke liye.
- `/v3/api-docs/**`: Swagger/OpenAPI JSON ke liye.

Allowed local origins:

- `http://localhost:3000`
- `http://localhost:5173`

Iska matlab agar React ya Vite app in ports se API call karegi to CORS allow hoga.

## 8. How To Implement CORS Step By Step

### Step 1: Decide The Allowed Origins

Write down exact browser origins.

Local examples:

```text
http://localhost:3000
http://localhost:5173
```

Environment examples:

```text
https://dev.example.com
https://qa.example.com
https://app.example.com
```

Do not use `*` in production unless the API is intentionally public and credential-free.

### Step 2: Decide The API Paths

For this project:

```text
/app/**
/v3/api-docs/**
```

Do not enable CORS for every path unless needed.

### Step 3: Decide Methods And Headers

Common API methods:

```text
GET, POST, PUT, PATCH, DELETE, OPTIONS
```

Common headers:

```text
Authorization
Content-Type
X-Request-Id
X-Correlation-Id
```

### Step 4: Add YAML Configuration

Add or update:

```yaml
app:
  cors:
    allowed-origins:
      - http://localhost:3000
      - http://localhost:5173
    allowed-methods:
      - GET
      - POST
      - OPTIONS
    allowed-headers:
      - Authorization
      - Content-Type
      - X-Request-Id
    exposed-headers:
      - X-Request-Id
      - X-Correlation-Id
    allow-credentials: false
    max-age: 3600
```

### Step 5: Add Spring Boot Config

Use `WebMvcConfigurer`:

```java
@Configuration
public class CorsConfig {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/app/**")
                        .allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new))
                        .allowedMethods(corsProperties.getAllowedMethods().toArray(String[]::new))
                        .allowedHeaders(corsProperties.getAllowedHeaders().toArray(String[]::new))
                        .exposedHeaders(corsProperties.getExposedHeaders().toArray(String[]::new))
                        .allowCredentials(corsProperties.isAllowCredentials())
                        .maxAge(corsProperties.getMaxAge());
            }
        };
    }
}
```

### Hinglish

Implementation steps simple hain:

1. Pehle decide karo kaunsa frontend origin API call karega.
2. Sirf required API paths par CORS enable karo.
3. Required methods aur headers allow karo.
4. Values YAML me rakho, Java me hard-code mat karo.
5. Spring Boot me `WebMvcConfigurer` se CORS mapping add karo.

## 9. How To Test CORS

### Test 1: Start The Application

Run local profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

If port `8080` is busy, run on another port:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--server.port=18080"
```

### Test 2: Confirm The API Works

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/app/v1/test/message"
```

Expected response:

```text
test
```

### Test 3: Test CORS Preflight

```powershell
Invoke-WebRequest `
  -Method Options `
  -Uri "http://localhost:8080/app/v1/test/message" `
  -Headers @{
    "Origin" = "http://localhost:3000"
    "Access-Control-Request-Method" = "GET"
    "Access-Control-Request-Headers" = "X-Request-Id"
  }
```

Expected result:

- Status should be `200`.
- Response should include `Access-Control-Allow-Origin`.
- Value should be `http://localhost:3000`.
- Response should include allowed methods and headers.

### Test 4: Test Unknown Origin

```powershell
Invoke-WebRequest `
  -Method Options `
  -Uri "http://localhost:8080/app/v1/test/message" `
  -Headers @{
    "Origin" = "https://unknown.example.com"
    "Access-Control-Request-Method" = "GET"
  }
```

Expected result:

```text
403 Forbidden
```

This confirms that CORS is not open to every origin.

### Test 5: Test Swagger OpenAPI CORS

```powershell
Invoke-WebRequest `
  -Method Options `
  -Uri "http://localhost:8080/v3/api-docs" `
  -Headers @{
    "Origin" = "http://localhost:3000"
    "Access-Control-Request-Method" = "GET"
  }
```

Expected:

- Status `200`.
- `Access-Control-Allow-Origin` should be `http://localhost:3000`.

### Test 6: Test In Browser

Browser testing is the most important because CORS is a browser rule.

Open browser developer tools:

1. Go to the Network tab.
2. Trigger the API call from frontend or Swagger UI.
3. Look for `OPTIONS` request.
4. Check response headers.
5. Confirm the actual `GET` or `POST` request happens after preflight.

### Hinglish

CORS test karne ke liye PowerShell useful hai, lekin final test browser me hi karna chahiye. Kyunki CORS browser enforce karta hai.

Testing flow:

1. App start karo.
2. API direct call karke confirm karo ki backend working hai.
3. `OPTIONS` preflight request bhejo.
4. Allowed origin check karo.
5. Unknown origin se test karo, usko reject hona chahiye.
6. Browser DevTools me Network tab se real behavior verify karo.

## 10. Why Request May Reach Grafana Login URL

If your test reaches a Grafana login URL, that is usually not a pure CORS success or failure. It means the request is being routed or redirected to Grafana before or during the API call.

Possible reasons:

- You are calling the Grafana URL instead of the Spring Boot API URL.
- Reverse proxy route is wrong.
- API gateway routes `/app/**` or `/v3/api-docs/**` to Grafana by mistake.
- Grafana is protecting the route and redirects unauthenticated users to `/login`.
- The browser is following a redirect from API domain to Grafana domain.
- The frontend base URL is configured as Grafana instead of API.
- Swagger UI server URL points to Grafana instead of Spring Boot.

Example wrong setup:

```text
Frontend calls: https://grafana.example.com/app/v1/test/message
Expected API:   https://api.example.com/app/v1/test/message
```

Example wrong Swagger server:

```text
servers:
  - url: https://grafana.example.com
```

Correct server should point to the API:

```text
servers:
  - url: https://api.example.com
```

### How To Debug Grafana Redirect

Use PowerShell without automatically hiding the URL details:

```powershell
Invoke-WebRequest `
  -Method Get `
  -Uri "http://localhost:8080/app/v1/test/message" `
  -MaximumRedirection 0
```

If a redirect happens, inspect:

```text
StatusCode
Location header
Final URL in browser Network tab
```

In browser DevTools:

1. Open Network tab.
2. Click the failed request.
3. Check Request URL.
4. Check Status Code.
5. Check Response Headers.
6. Look for `Location: .../login`.
7. Confirm whether the redirect target is Grafana.

### Hinglish

Agar request Grafana login URL tak pahunch rahi hai, to iska matlab ye ho sakta hai ki request galat route par ja rahi hai. Ye sirf CORS ka issue nahi hota.

Common reasons:

- API URL ki jagah Grafana URL call ho raha hai.
- Reverse proxy ya gateway route galat configured hai.
- Swagger UI me server URL Grafana ka set hai.
- Grafana unauthenticated request ko login page par redirect kar raha hai.
- Frontend environment variable me API base URL galat hai.

Debug ka simple tareeka:

- Browser DevTools me actual Request URL check karo.
- Response me `Location` header check karo.
- Dekho redirect API se Grafana login par ja raha hai ya direct Grafana call ho raha hai.
- API call hamesha Spring Boot API host par honi chahiye, Grafana host par nahi.

## 11. CORS vs Authentication

CORS and authentication are different.

CORS answers:

```text
Can this browser origin read this API response?
```

Authentication answers:

```text
Who is the user?
```

Authorization answers:

```text
Is this user allowed to perform this action?
```

If Grafana login appears, that is usually authentication or routing behavior, not only CORS.

### Hinglish

CORS, authentication, aur authorization alag cheezein hain.

- CORS: kya ye frontend origin API response read kar sakta hai?
- Authentication: user kaun hai?
- Authorization: user ko ye action karne ki permission hai ya nahi?

Grafana login dikhna mostly auth ya routing issue hota hai.

## 12. Common Mistakes

- Adding `*` for all origins in production.
- Allowing credentials with wildcard origin.
- Testing only with PowerShell and not browser.
- Forgetting that different ports are different origins.
- Allowing `/app/**` but forgetting `/v3/api-docs/**` for external Swagger UI.
- Missing `Authorization` in allowed headers.
- Calling the wrong base URL.
- Proxy routing API paths to Grafana or another tool.
- Thinking CORS replaces authentication.

### Hinglish

Common mistakes:

- Production me `*` origin allow kar dena.
- Browser test na karna.
- `localhost:3000` aur `localhost:8080` ko same origin samajhna.
- Swagger ke liye `/v3/api-docs/**` allow na karna.
- API base URL galat configure karna.
- Grafana ya proxy route ko API route samajh lena.

## 13. Professional Checklist

Before saying CORS is working, confirm:

- API works directly.
- Preflight from allowed origin returns `200`.
- Preflight from unknown origin returns `403`.
- Response has `Access-Control-Allow-Origin`.
- Required methods are present.
- Required headers are present.
- Browser Network tab shows successful preflight and actual request.
- Request URL is the API URL, not Grafana login URL.
- No production config allows unnecessary local origins.

### Hinglish Final Summary

CORS browser ka security rule hai. Jab frontend aur backend alag origin par hote hain tab CORS required hota hai. Is project me CORS `app.cors` YAML properties aur `CorsConfig` se implement hai. `/app/**` APIs aur `/v3/api-docs/**` OpenAPI docs ke liye CORS rules apply hote hain.

Testing ke liye PowerShell se preflight check kar sakte ho, lekin final confirmation browser DevTools me karo. Agar request Grafana login URL par ja rahi hai, to pehle URL, redirect, reverse proxy, gateway, Swagger server URL, aur frontend API base URL check karo. Grafana login usually routing ya authentication issue hota hai, direct CORS issue nahi.
