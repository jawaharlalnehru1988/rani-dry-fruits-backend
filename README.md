# Fruits Spring Boot API

Spring Boot REST API implementation for the Django `fruits` module.

## Implemented API

- `GET /api/fruits-gallery`
- `GET /api/fruits-gallery/{id}`
- `POST /api/fruits-gallery`
- `PUT /api/fruits-gallery/{id}` (partial update allowed, same as current Django behavior)
- `DELETE /api/fruits-gallery/{id}`

## JSON Contract

Write payload:

```json
{
  "name": "Apple",
  "price": 100,
  "discountPercentage": 5,
  "description": "Fresh apples",
  "imagePath": ["/uploads/apple-1.jpg", "/uploads/apple-2.jpg"]
}
```

Read payload:

```json
{
  "id": 1,
  "name": "Apple",
  "price": 100.00,
  "discountPercentage": 5.00,
  "description": "Fresh apples",
  "imagePath": ["/uploads/apple-1.jpg", "/uploads/apple-2.jpg"],
  "created_at": "2026-04-19T12:45:00",
  "updated_at": "2026-04-19T12:45:00"
}
```

## Database

Mapped to existing tables:

- `fruits_gallery`
- `fruits_gallery_images`

Default DB connection in `src/main/resources/application.yml`:

- host: `localhost`
- port: `5432`
- db: `demoappdb`
- user: `demoappuser`

Override with env vars:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `ADMIN_USERNAME`
- `ADMIN_PASSWORD`
- `ADMIN_SESSION_TTL_MINUTES`

Copy the example file and adjust values for your server:

```bash
cp .env.example .env
```

## Admin Login API

- `POST /api/admin/auth/login`
- `GET /api/admin/auth/me`
- `POST /api/admin/auth/logout`

Login payload:

```json
{
  "username": "Ganeshan",
  "password": "RaniGanesan123"
}
```

For protected write APIs, send the token from login in the `Authorization` header:

```http
Authorization: Bearer <token>
```

## Run

```bash
cd /var/www/fruits-springboot-api
mvn spring-boot:run
```

Runs on port `8084` by default.

## Build

```bash
cd /var/www/fruits-springboot-api
mvn clean package
```

## Deployment Notes

For full deployment steps with systemd and nginx reverse proxy, see `DEPLOYMENT.md`.
# rani-dry-fruits-backend
