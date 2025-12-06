# 💪 VieGym Backend API

Backend API cho ứng dụng VieGym - Hệ thống quản lý tập luyện và sức khỏe.

## 🛠️ Tech Stack

- **Java 21** (LTS)
- **Spring Boot 3.5.5**
- **PostgreSQL** - Database
- **Spring Security** - Authentication & Authorization
- **JWT** - Token-based authentication
- **OAuth2** - Google login
- **Spring Data JPA** - ORM
- **MapStruct** - Object mapping
- **RabbitMQ** - Message queue
- **WebSocket** - Real-time communication
- **Cloudinary** - Image storage
- **Spring Mail** - Email service
- **Swagger/OpenAPI** - API documentation

## 📋 Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 16+
- RabbitMQ (hoặc CloudAMQP)
- Docker (optional)

## 🚀 Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/yuhhz52/Viegym.git
cd Viegym/viegymapp
```

### 2. Configure Database
Tạo database PostgreSQL:
```sql
CREATE DATABASE viegym;
```

### 3. Configure Application
Copy và chỉnh sửa file cấu hình:
```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

Cập nhật thông tin trong `application-local.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/viegym
    username: postgres
    password: your-password
```

### 4. Run Migration
```bash
psql -U postgres -d viegym -f src/main/resources/db/migration/V1__create_notification_tables.sql
```

### 5. Build & Run
```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Hoặc sử dụng Docker:
```bash
docker build -t viegym-backend .
docker run -p 8080:8080 viegym-backend
```

## 📁 Project Structure

```
viegymapp/
├── src/main/java/com/example/viegymapp/
│   ├── config/          # Configuration classes
│   ├── controller/      # REST Controllers
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # JPA Entities
│   ├── repository/     # JPA Repositories
│   ├── service/        # Business Logic
│   ├── security/       # Security configuration
│   ├── mapper/         # MapStruct mappers
│   ├── exception/      # Custom exceptions
│   └── util/           # Utilities
├── src/main/resources/
│   ├── application.yaml              # Base config
│   ├── application-dev.yml          # Dev config
│   ├── application-local.yml        # Local config
│   ├── application-prod.yml         # Production config
│   └── db/migration/                # Database migrations
└── Dockerfile
```

## 🔑 Environment Variables

Xem file `.env.example` để biết danh sách đầy đủ các biến môi trường cần thiết.

**Core Variables:**
```env
SPRING_PROFILES_ACTIVE=local
DATABASE_URL=jdbc:postgresql://localhost:5432/viegym
JWT_SECRET=your-secret-key
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

## 📚 API Documentation

Sau khi chạy ứng dụng, truy cập Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

### Main Endpoints

**Authentication:**
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập
- `GET /api/auth/google` - Google OAuth login

**Users:**
- `GET /api/users/profile` - Lấy profile
- `PUT /api/users/profile` - Cập nhật profile
- `POST /api/users/avatar` - Upload avatar

**Workouts:**
- `GET /api/workouts` - Danh sách workouts
- `POST /api/workouts` - Tạo workout
- `GET /api/workouts/{id}` - Chi tiết workout

**Exercises:**
- `GET /api/exercises` - Danh sách exercises
- `GET /api/exercises/{id}` - Chi tiết exercise

**Community:**
- `GET /api/community/posts` - Danh sách posts
- `POST /api/community/posts` - Tạo post
- `POST /api/community/posts/{id}/like` - Like post

**Coach:**
- `GET /api/coaches` - Danh sách coaches
- `POST /api/bookings` - Đặt lịch với coach

**WebSocket:**
- `/ws` - WebSocket endpoint
- `/topic/likes/{postId}` - Subscribe like updates
- `/topic/comments/{postId}` - Subscribe comment updates

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run specific test
./mvnw test -Dtest=UserServiceTest

# Run with coverage
./mvnw clean test jacoco:report
```

## 🏗️ Build for Production

```bash
# Build JAR
./mvnw clean package -DskipTests

# Build Docker image
docker build -t viegym-backend:latest .
```

## 🚢 Deployment

### Deploy to Render.com

Chi tiết xem file: **[RENDER_DEPLOYMENT.md](./RENDER_DEPLOYMENT.md)**

Quick steps:
1. Push code to GitHub
2. Tạo PostgreSQL database trên Render
3. Tạo Web Service từ GitHub repo
4. Configure environment variables
5. Deploy!

Hướng dẫn nhanh: **[QUICK_DEPLOY.md](./QUICK_DEPLOY.md)**

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Database Connection Failed
- Kiểm tra PostgreSQL đang chạy
- Kiểm tra username/password
- Kiểm tra database đã tạo chưa

### RabbitMQ Connection Failed
- Kiểm tra RabbitMQ service đang chạy
- Default: localhost:5672

## 📊 Clean Code

Backend đã được clean code với:
- ✅ Removed all `System.out.println`
- ✅ Proper SLF4J logging
- ✅ Structured error handling
- ✅ Production-ready configuration

Chi tiết: [CLEAN_CODE_SUMMARY.md](./CLEAN_CODE_SUMMARY.md)

## 🔐 Security

- JWT-based authentication
- Role-based access control (USER, COACH, ADMIN)
- OAuth2 integration (Google)
- Password encryption with BCrypt
- CORS configuration
- Secure cookies in production

## 📝 Contributing

1. Fork repository
2. Create feature branch: `git checkout -b feature/AmazingFeature`
3. Commit changes: `git commit -m 'Add AmazingFeature'`
4. Push to branch: `git push origin feature/AmazingFeature`
5. Open Pull Request

## 👥 Authors

- **VieGym Team**

## 📄 License

This project is private.

---

**Made with ❤️ by VieGym Team**
