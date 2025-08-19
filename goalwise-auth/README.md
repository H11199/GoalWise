# Goalwise Auth (Spring Boot) — JWT + Vonage Verify (REST)

## Run (dev, H2)
1. Set env vars (real values from Vonage):
```
export JWT_SECRET='a-very-long-256-bit-secret'
export VONAGE_API_KEY='your_key'
export VONAGE_API_SECRET='your_secret'
```
2. Build & run:
```
./mvnw spring-boot:run
```
3. H2 console: `http://localhost:8080/h2-console` (JDBC: `jdbc:h2:mem:gwdb`)

## Endpoints
- `POST /auth/register` → `{email,password,phone}` → returns `{requestId}`
- `POST /auth/login` → `{email,password}` → returns `{requestId}`
- `POST /auth/otp/start` → `{email?, purpose, channel?}` → returns `{requestId}`
- `POST /auth/otp/verify` → `{requestId, code}` → returns `{jwt}`

Use E.164 phone format, e.g., `+9198xxxxxx`.

## Notes
- Uses Vonage Verify v2 REST API via Basic Auth.
- Dev DB is H2 (in-memory). Switch to Postgres by editing `application.yml`.
- JWT is HS256 signed; set a strong `JWT_SECRET`.
