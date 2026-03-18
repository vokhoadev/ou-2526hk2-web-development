# Lab: RESTful API (GET, POST, PUT, PATCH, DELETE)

    Cấu trúc chuẩn:
    /springcode
    |-- controller 
    |   |-- UserController.java
    |
    |-- service
    |   |-- UserService.java # implementation
    |   |-- IUserService.java # interface
    |
    |-- repository
    |   |-- UserRepository.java 
    |
    |-- model
    |   |-- User.java
    |
    |-- dto
    |   |-- UserRequest.java
    |   |-- UserResponse.java

# Lab: DB

## Profiles

| Profile       | DB    | Mô tả |
|---------------|-------|--------|
| **dev-h2**    | H2    | In-memory, có H2 Console. Mặc định khi chạy `mvn spring-boot:run`. |
| **dev-mysql** | MySQL | Cần MySQL (ví dụ `docker compose up -d`). User: springcode / root. |

Chạy với MySQL (phải bật MySQL trước, không sẽ lỗi *Communications link failure*):
```bash
docker compose up -d
# Đợi vài giây cho MySQL sẵn sàng (hoặc: docker compose ps → status Up (healthy))
mvn spring-boot:run -Dspring-boot.run.profiles=dev-mysql
```

Nếu container MySQL **Restarting** liên tục (lỗi `Table 'mysql.user' doesn't exist`): volume cũ không tương thích. Xóa volume và chạy lại:
```bash
docker compose down -v
docker compose up -d
```

Nếu không dùng MySQL, chạy mặc định H2: `mvn spring-boot:run` (profile dev-h2).

---

## Tutorials

Hướng dẫn chi tiết: **[tutorials/README.md](tutorials/README.md)**

- **Docker** — Cài đặt, kiểm tra, lệnh cơ bản (`docker ps`, `docker logs`, …), chạy MySQL bằng `docker run`.
- **Docker Compose** — Dùng file `docker-compose.yml`, lệnh `up` / `down` / `ps` / `logs`, xử lý volume khi lỗi.
- **Đóng ứng dụng Java** — Dừng app chạy bằng Maven, JAR, IDE (Ctrl+C, nút Stop, kill process).
