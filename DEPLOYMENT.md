# Deployment Guide

This guide describes a simple production setup on Linux using Java 21, Maven, PostgreSQL, systemd, and nginx.

## 1. Prerequisites

- Java 21 installed
- Maven installed
- PostgreSQL reachable from the server
- nginx installed (optional, recommended)

## 2. Build

```bash
cd /var/www/fruits-springboot-api
mvn clean package
```

Expected jar:

- target/fruits-springboot-api-0.0.1-SNAPSHOT.jar

## 3. Environment Variables

Create and edit a runtime env file:

```bash
sudo mkdir -p /etc/fruits-api
sudo cp /var/www/fruits-springboot-api/.env.example /etc/fruits-api/fruits-api.env
sudo nano /etc/fruits-api/fruits-api.env
```

Recommended production changes:

- Use strong DB credentials
- Change ADMIN_USERNAME and ADMIN_PASSWORD
- Reduce token TTL if needed

## 4. Run with systemd

Create service file:

```bash
sudo nano /etc/systemd/system/fruits-api.service
```

Paste:

```ini
[Unit]
Description=Fruits Spring Boot API
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/var/www/fruits-springboot-api
EnvironmentFile=/etc/fruits-api/fruits-api.env
ExecStart=/usr/bin/java -jar /var/www/fruits-springboot-api/target/fruits-springboot-api-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Enable and start:

```bash
sudo systemctl daemon-reload
sudo systemctl enable fruits-api
sudo systemctl restart fruits-api
sudo systemctl status fruits-api
```

## 5. nginx Reverse Proxy (optional)

Example site config:

```nginx
server {
    listen 80;
    server_name api.example.com;

    location / {
        proxy_pass http://127.0.0.1:8084;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Then:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

## 6. Health Check

```bash
curl http://127.0.0.1:8084/api/fruits-gallery
```

## 7. Admin Login Check

```bash
curl -X POST http://127.0.0.1:8084/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"Ganeshan","password":"RaniGanesan123"}'
```

Use the returned token in `Authorization: Bearer <token>` for POST/PUT/DELETE endpoints.
