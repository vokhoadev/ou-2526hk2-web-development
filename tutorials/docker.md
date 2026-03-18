# Tutorials

## 1. Docker — Cơ bản

### Docker là gì?
Docker chạy ứng dụng trong **container** (môi trường cô lập), không cần cài trực tiếp lên máy (ví dụ MySQL chạy trong container thay vì cài MySQL lên OS).

### Kiểm tra Docker đã cài
```bash
docker --version
docker info
```
- Nếu lỗi "Cannot connect to the Docker daemon" → chưa chạy Docker Desktop (hoặc service Docker).

### Lệnh thường dùng
| Lệnh | Mô tả |
|------|--------|
| `docker ps` | Liệt kê container đang chạy |
| `docker ps -a` | Liệt kê tất cả container (kể cả đã dừng) |
| `docker images` | Liệt kê image đã tải |
| `docker stop <container_id hoặc tên>` | Dừng container |
| `docker start <container_id hoặc tên>` | Chạy lại container đã dừng |
| `docker logs <container_id hoặc tên>` | Xem log container |

### Ví dụ với MySQL (chạy 1 container đơn lẻ, không dùng compose)
```bash
# Chạy MySQL 8.0, port 3306, password root
docker run -d --name mysql-dev -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 mysql:8.0

# Xem log
docker logs mysql-dev

# Dừng
docker stop mysql-dev

# Xóa container (sau khi stop)
docker rm mysql-dev
```

---

## 2. Docker Compose — Nhiều service, cấu hình bằng file

### Docker Compose là gì?
Định nghĩa nhiều container (MySQL, app, …) trong file **docker-compose.yml**, chạy bằng một lệnh.

### Kiểm tra
```bash
docker compose version
# hoặc (phiên bản cũ): docker-compose --version
```

### Lệnh cơ bản (tại thư mục có file docker-compose.yml)
| Lệnh | Mô tả |
|------|--------|
| `docker compose up -d` | Chạy tất cả service trong nền (-d = detach) |
| `docker compose down` | Dừng và xóa container (giữ volume) |
| `docker compose down -v` | Dừng và xóa cả **volume** (reset data) |
| `docker compose ps` | Xem trạng thái các service |
| `docker compose logs` | Xem log tất cả service |
| `docker compose logs mysql` | Xem log service tên `mysql` |
| `docker compose logs -f mysql` | Xem log theo thời gian thực |

### Trong project này (springcode)
File **docker-compose.yml** ở thư mục gốc project định nghĩa 1 service: **mysql**.

**Chạy MySQL:**
```bash
cd /path/to/springcode
docker compose up -d
```

**Kiểm tra MySQL đã sẵn sàng:**
```bash
docker compose ps
# Trạng thái "Up (healthy)" là OK.
```

**Dừng MySQL:**
```bash
docker compose down
```

**Dừng và xóa hết data (volume) — dùng khi bị lỗi Restarting / table không tồn tại:**
```bash
docker compose down -v
docker compose up -d
```

---

## 3. Cách đóng (dừng) ứng dụng Java / Spring Boot

### Chạy bằng Maven trong terminal
```bash
mvn spring-boot:run
# hoặc với profile:
mvn spring-boot:run -Dspring-boot.run.profiles=dev-h2
```

**Cách dừng:**
- **Ctrl + C** trong terminal đang chạy → gửi tín hiệu dừng tiến trình → ứng dụng tắt.

### Chạy bằng JAR
```bash
java -jar target/springcode-0.0.1-SNAPSHOT.jar
```
**Dừng:** **Ctrl + C** trong terminal đó.

### Chạy trong IDE (IntelliJ, Eclipse, VS Code / Cursor)
- **Stop** (nút hình vuông đỏ) trên thanh Run/Debug, hoặc
- **Ctrl + F2** (IntelliJ) / **Cmd + F2** (Mac), hoặc
- Đóng tab Run / Debug.

### Process chạy nền (background)
Nếu đã chạy với `&` hoặc trong terminal khác:
```bash
# Tìm process Java của springcode (port 8080)
lsof -i :8080
# hoặc (macOS/Linux)
netstat -anv | grep 8080

# Kill theo PID (thay 12345 bằng PID thực tế)
kill 12345
# Hoặc ép kill:
kill -9 12345
```

### Tóm tắt
| Cách chạy | Cách dừng |
|------------|-----------|
| `mvn spring-boot:run` trong terminal | **Ctrl + C** trong terminal đó |
| `java -jar ...jar` trong terminal | **Ctrl + C** |
| IDE (Run/Debug) | Nút Stop hoặc Ctrl+F2 / Cmd+F2 |
| Process nền | `kill <PID>` (tìm PID bằng `lsof -i :8080`) |
