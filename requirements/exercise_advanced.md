# Bài tập nâng cao — Hệ thống đặt hàng đa bảng 

Sinh viên triển khai trên nền project **springcode** (Spring Boot, JPA, JWT, Security). Mọi mục trong tài liệu này đều **bắt buộc** trừ khi giảng viên có phiên bản đề riêng.

---

## 1. Mục tiêu

- Hoàn thiện **cửa hàng + đơn hàng + đánh giá** với nhiều bảng, transaction, phân quyền theo resource.
- Áp dụng **Events, cache, lịch, AOP/Auditing, quan sát, OpenAPI, kiểm thử + JaCoCo ≥ 80%**, và tài liệu nộp đủ mục §9.

---

## 2. Tác nhân & bối cảnh

- **ADMIN**: quản lý danh mục & sản phẩm; xem mọi đơn; cập nhật trạng thái đơn theo **state machine**; có quyền xóa review (nếu quy định trong README bài tập).
- **USER** (đã đăng nhập): xem sản phẩm; CRUD địa chỉ của mình; đặt hàng nhiều dòng; chỉ xem/sửa đơn của mình; review sản phẩm đã mua (một user — một review / sản phẩm).
- **Khách** (chưa đăng nhập): chỉ **GET** danh sách & chi tiết sản phẩm **đang bán** (read-only).

---

## 3. Mô hình dữ liệu

| Bảng | Cột / ràng buộc gợi ý | Quan hệ |
|------|------------------------|---------|
| `categories` | id, name, **slug** (unique, indexed) | 1-N → `products` |
| `products` | id, category_id (FK), name, description (optional), **price** (`BigDecimal`), **stock** (int ≥ 0), **active**, created_at | N-1 `categories` |
| `addresses` | id, **user_id** (FK users), recipient_name, line1, city, phone, **is_default** (mỗi user tối đa một default — xử lý ở service) | N-1 `users` |
| `orders` | id, user_id, **status** (enum `STRING`: ví dụ `PENDING`, `CONFIRMED`, `SHIPPED`, `CANCELLED`, `COMPLETED`), **total_amount**, snapshot địa chỉ giao (text/JSON), created_at, updated_at | N-1 `users` |
| `order_items` | id, order_id, product_id, **quantity**, **unit_price** (snapshot), line_total (tính hoặc lưu) | N-1 `orders`, N-1 `products` |
| `reviews` | id, user_id, product_id, **rating** (1–5), comment (optional, max length), created_at, **unique (user_id, product_id)** | N-1 `users`, N-1 `products` |
| `order_audit_logs` | id, order_id (FK), actor_user_id (nullable nếu hệ thống), event_type, payload_tóm_tắt (text/JSON), created_at | N-1 `orders` (và optional N-1 users) |

**Quy tắc nghiệp vụ:**

- Chỉ **review** khi user đã **mua** sản phẩm đó trong ít nhất một đơn ở trạng thái **hoàn thành** (định nghĩa rõ trong README bài tập).
- **Đặt hàng**: một **transaction** — tạo `order` + `order_items`, **trừ stock**; thiếu hàng → **rollback toàn bộ**.
- **Đổi trạng thái đơn**: state machine có giải thích; **hủy** sau khi đã trừ kho → **hoàn stock** trong cùng giao dịch cập nhật trạng thái.
- **Race stock**: chọn và mô tả một chiến lược (JPQL điều kiện / pessimistic lock / tương đương) trong README bài tập.

---

## 4. API & REST

- CRUD **category** / **product**: chỉ `ROLE_ADMIN`; validation đầy đủ (giá > 0, stock ≥ 0, slug unique, …).
- GET sản phẩm **công khai**: **phân trang**, lọc `categoryId`, `active`, tìm theo tên (contains, không phân biệt hoa thường).
- GET chi tiết sản phẩm: có **điểm TB rating** + **số review** (tránh N+1).
- **Địa chỉ**: USER CRUD địa chỉ của mình; `is_default=true` thì các địa chỉ khác của user → `false`.
- **Đặt hàng**: POST `addressId` + `[{ productId, quantity }]`, 201 + body đơn; lỗi hết hàng rõ ràng; **idempotency key** (header hoặc field) để tránh trùng đơn khi client gửi lại — ghi rõ cách lưu/kiểm tra trong README.
- **Đơn hàng**: USER chỉ đơn của mình; ADMIN list tất cả (phân trang) + cập nhật status.
- **Review**: POST/GET (phân trang theo product); USER sửa/xóa review của mình; ADMIN xóa bất kỳ (nếu đã nêu ở §2).
- **OpenAPI**: tích hợp **springdoc** (hoặc tương đương), có UI `/swagger-ui` (hoặc path cấu hình chuẩn), liệt kê security scheme JWT.

---

## 5. Bảo mật

- Phân tách **permitAll** (GET sản phẩm công khai) / **authenticated** / **hasRole('ADMIN')`.
- **Object-level authorization** trên `order`, `address`, `review` (owner hoặc ADMIN); không tin `userId` từ client.
- **Method security**: ít nhất **một** phương thức service quan trọng gắn `@PreAuthorize` (hoặc tương đương) **và** vẫn kiểm tra owner khi cần.
- **Vô hiệu hóa JWT sau logout**: **refresh token + rotation** **hoặc** denylist/blacklist token access cho đến hết hạn — chọn **một** cách, mô tả trong README và có API logout/refresh tương ứng.
- Không trả `password_hash` / dữ liệu nhạy cảm trong JSON.

---

## 6. JPA & hiệu năng

- `@ManyToOne` / `@OneToMany` đúng; `FetchType` có lý do; **không** cascade bừa bãi trên aggregate lớn.
- Xử lý **N+1** ở ít nhất một luồng list (EntityGraph / join fetch / projection).
- Xử lý **LazyInitializationException** bằng DTO/query trong phạm vi transaction — không phụ thuộc OSIV để “vá”.
- **Spring Data JPA Auditing**: bật auditing; ít nhất **hai** entity nghiệp vụ mới có `@CreatedDate` / `@LastModifiedDate` (và `@CreatedBy`/`@LastModifiedBy` nếu phù hợp với Security).

---

## 7. Giao dịch & đồng thời

- `@Transactional` rõ ràng ở tầng service cho đặt hàng và đổi trạng thái (kèm hoàn kho khi hủy).
- Đáp ứng **race condition stock** như §3.

---

## 8. Lỗi & hợp đồng phản hồi

- Dùng thống nhất `ResourceNotFoundException`, `DuplicateResourceException`, validation → JSON theo pattern `ApiError` / `FieldErrorDetail` hiện có **hoặc** **RFC 9457 Problem Details** — **chọn một** phong cách cho toàn API mới và ghi trong README.
- Vi phạm unique → HTTP & message rõ (409/400).

---

## 9. Sự kiện, AOP, lịch, cache, tích hợp, quan sát

### 9.1 Application Events

- Sau **đặt hàng thành công**, publish **`OrderPlacedEvent`** (hoặc tên tương đương).
- Dùng **`@TransactionalEventListener`(phase = AFTER_COMMIT)** để ghi **`order_audit_logs`** (event_type, order_id, actor, payload tóm tắt).
- Có **test** chứng minh listener chạy sau commit (ví dụ `@SpringBootTest` + assert số dòng audit, hoặc mock/spy hợp lệ).

### 9.2 AOP

- Một **`@Aspect`** có mục đích rõ: đo thời gian service/controller **hoặc** log/audit lớp gọi — **không** nhồi logic nghiệp vụ vào aspect.

### 9.3 Lịch

- **`@Scheduled`**: một job có mục đích nghiệp vụ (ví dụ nhắc/hủy đơn `PENDING` quá hạn, hoặc tác vụ bảo trì có mô tả) — ghi rõ cron/fixedDelay trong README.

### 9.4 Cache

- **`@Cacheable`** trên ít nhất một endpoint đọc nhiều (danh mục hoặc chi tiết sản phẩm); **`@CacheEvict`/`@CachePut`** khi ADMIN sửa dữ liệu liên quan.

### 9.5 Webhook hoặc SSE

- **Webhook**: khi đổi trạng thái đơn, gọi HTTP ra URL cấu hình (ký HMAC hoặc secret header, timeout, log kết quả) **hoặc**
- **SSE**: endpoint đẩy sự kiện trạng thái đơn đơn giản cho demo.

### 9.6 Độ tin cậy tích hợp

- Trong README: **một đoạn** mô tả rủi ro “commit DB xong mà gửi webhook/message lỗi” và cách xử lý (outbox, retry, idempotency phía nhận — chọn hướng, có thể chỉ thiết kế nếu chưa code full outbox).

### 9.7 Quan sát

- **Spring Boot Actuator**: bật **`/actuator/health`**; các endpoint actuator khác **phải** được bảo vệ (không public nguy hiểm).
- **Logging**: không log JWT/password; log có `orderId` khi tạo đơn (INFO).

---

## 10. Kiểm thử & coverage

- **JUnit 5**; có **unit test** (mock) **và** **`@DataJpaTest`** **và** **`@WebMvcTest` hoặc `@SpringBootTest`** cho luồng có ý nghĩa (không chỉ `contextLoads` rỗng).
- **JaCoCo**: `mvn test` → `target/site/jacoco/index.html`; **LINE ≥ 80%** theo rule trong `pom` (loại trừ `entity`, `dto`, `config`, `SpringcodeApplication` trừ khi GV chỉnh).
- **`mvn -Penforce-coverage verify`** phải **pass** trước khi nộp.
- Seed **data.sql** / `@Sql` / initializer: category, product, user mẫu trên H2.

---

## 11. Triển khai & script

- **Docker**: `Dockerfile` (hoặc `compose`) build/run được ứng dụng 

---

## 12. Tài liệu nộp 

File **`README-BAI-TAP.md`** (hoặc mục tương đương trong README chính) gồm:

- ERD (ảnh hoặc link).
- **Ma trận endpoint** (method, path, role).
- **State machine** đơn hàng.
- **Race stock** + **idempotency đặt hàng** + **cách vô hiệu hóa JWT/refresh**.
- Lệnh: `mvn test`, `mvn -Penforce-coverage verify`, cách mở Swagger, cách chạy Docker hoặc script API.
- Ảnh hoặc đính kèm báo cáo **JaCoCo** tổng quan.

---

## 13. Checklist tổng hợp

### Dữ liệu & JPA

- [ ] Đủ bảng §3, auditing trên ≥ 2 entity mới
- [ ] `BigDecimal` tiền; thời gian + JSON nhất quán; snapshot địa chỉ & `unit_price`

### Hiệu năng & transaction

- [ ] Tránh N+1; phân trang + sort an toàn
- [ ] Đặt hàng + trừ kho atomic; hủy hoàn kho; xử lý race stock

### REST & tài liệu API

- [ ] Đủ API §4; OpenAPI/Swagger; idempotency đặt hàng
- [ ] Lỗi thống nhất (ApiError **hoặc** Problem Details)

### Bảo mật

- [ ] JWT + phân quyền URL + owner check
- [ ] `@PreAuthorize` trên service (≥ 1 chỗ); logout/refresh hoặc denylist
- [ ] Actuator health bật, endpoint nhạy cảm được bảo vệ

### Nâng cao

- [ ] Events + `@TransactionalEventListener` AFTER_COMMIT + `order_audit_logs` + test
- [ ] Một `@Aspect` đúng mục đích
- [ ] `@Scheduled` có mô tả
- [ ] Cache + evict khi admin sửa
- [ ] Webhook **hoặc** SSE
- [ ] Đoạn README về độ tin cậy tích hợp (§9.6)

### Kiểm thử & giao nộp

- [ ] Unit + DataJpa + WebMvc hoặc SpringBootTest có ý nghĩa
- [ ] JaCoCo ≥ 80% + `enforce-coverage` pass
- [ ] Docker + README §12

---

## 14. Thứ tự triển khai khuyến nghị

1. ERD → entity + repository + seed + auditing.
2. API public sản phẩm (phân trang, lọc, cache).
3. CRUD admin category/product + security + cache evict.
4. Address + owner + `@PreAuthorize` nơi cần.
5. Đặt hàng + transaction + stock + idempotency + race strategy.
6. Events + audit log + listener AFTER_COMMIT + test.
7. Đơn hàng admin + state machine + hoàn kho khi hủy + webhook/SSE.
8. Review + rule đã mua.
9. AOP + scheduled job + Actuator + refresh/denylist logout.
10. OpenAPI + Docker + chỉnh lỗi Problem Details/ApiError thống nhất.
11. Bổ sung test đến **coverage ≥ 80%** → `mvn -Penforce-coverage verify` → hoàn thiện README-BAI-TAP + minh chứng JaCoCo.

---
