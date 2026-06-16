# HAM-48 Deployment Configuration

## Production Build Commands

### Backend

```bash
cd medilink-backend
./mvnw.cmd clean package -DskipTests
```

Artifact: `target/medilink-backend-0.0.1-SNAPSHOT.jar`

Run:
```bash
java -jar target/medilink-backend-0.0.1-SNAPSHOT.jar
```

### Frontend

```bash
cd medilink-frontend
npm run build -- --configuration=production
```

Output: `dist/medilink-frontend/`

Serve via nginx or any static file server.

## Required Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `MEDILINK_JWT_SECRET` | **Yes** | 256-bit JWT signing secret (production only) |
| `MEDILINK_MAIL_HOST` | Yes | SMTP server host |
| `MEDILINK_MAIL_PORT` | Yes | SMTP server port |
| `MEDILINK_MAIL_USERNAME` | If auth | SMTP username |
| `MEDILINK_MAIL_PASSWORD` | If auth | SMTP password |
| `MEDILINK_MAIL_AUTH` | Yes | `true` or `false` |
| `MEDILINK_MAIL_STARTTLS` | Yes | `true` or `false` |
| `MEDILINK_NOTIFICATION_FROM` | Yes | Sender email address |
| `SPRING_DATASOURCE_URL` | Yes | JDBC URL (default: `jdbc:mysql://localhost:3306/medilink`) |
| `SPRING_DATASOURCE_USERNAME` | Yes | MySQL username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | MySQL password |

## Production Verification

```bash
# Backend health check
curl http://localhost:8080/api/v1/health

# Expected: {"success":true,"data":{"status":"UP"}}

# Backend Swagger UI
open http://localhost:8080/api/swagger-ui.html
```

## Deployment Checklist

- [ ] MySQL database created and migrations applied
- [ ] All environment variables set (see table above)
- [ ] JWT secret generated (256-bit random string)
- [ ] SMTP server configured and reachable
- [ ] Frontend built for production (`npm run build -- --configuration=production`)
- [ ] Backend built for production (`mvnw.cmd clean package`)
- [ ] CORS origin configured for production domain
- [ ] Health check endpoint returns 200
- [ ] Admin seed account created
- [ ] SSL/HTTPS configured on reverse proxy (nginx)
- [ ] Database backups configured

## Build Verification

```bash
# Verify backend builds
cd medilink-backend
./mvnw.cmd clean package -DskipTests
ls -la target/*.jar

# Verify frontend builds
cd medilink-frontend
npm run build -- --configuration=production
ls -la dist/medilink-frontend/
```
