# Hệ thống Đấu giá Trực tuyến (LAN-based)

## 1. Mô tả bài toán và Phạm vi hệ thống
Dự án này là một hệ thống đấu giá trực tuyến được thiết kế để hoạt động trong mạng nội bộ (LAN). Hệ thống cho phép người dùng tham gia vào các phiên đấu giá thời gian thực với các vai trò khác nhau như Người mua (Bidder), Người bán (Seller) và Quản trị viên (Admin).

**Phạm vi hệ thống:**
*   **Quản lý người dùng:** Đăng ký, đăng nhập và phân quyền.
*   **Quản lý đấu giá:** Tạo phiên đấu giá với các danh mục hàng hóa đa dạng (Nghệ thuật, Điện tử, Phương tiện, Khác).
*   **Đấu giá thời gian thực:** Cơ chế đặt giá cạnh tranh, xử lý đồng thời (Concurrent Bidding).
*   **Cơ chế chống bắn tỉa (Anti-sniping):** Tự động gia hạn thời gian khi có người đặt giá ở những phút cuối.
*   **Tìm kiếm & Bộ lọc:** Cho phép người dùng nhanh chóng tìm thấy sản phẩm theo từ khóa hoặc danh mục.
*   **Thanh toán:** Hệ thống ví điện tử nội bộ để quản lý số dư và thanh toán khi thắng đấu giá.

## 2. Công nghệ sử dụng và Yêu cầu cài đặt
### Công nghệ:
*   **Ngôn ngữ:** Java 21 (JDK 21).
*   **Giao diện (UI):** JavaFX 21.
*   **Cơ sở dữ liệu:** MySQL 8.0+.
*   **Quản lý dự án:** Maven.
*   **Thư viện hỗ trợ:** HikariCP (Connection Pool), SLF4J (Logging), Gson (JSON).
*   **Kiểm thử (Testing):** JUnit 5, Mockito (Mocking), TestFX (UI Testing).
*   **CI/CD:** GitHub Actions.

### Yêu cầu môi trường:
*   Đã cài đặt **Java JDK 21**.
*   Đã cài đặt **Maven**.
*   Cơ sở dữ liệu **MySQL** đang hoạt động.

## 3. Cấu trúc thư mục chính
*   `src/main/java/com/auction/client`: Chứa các Controller và logic xử lý phía người dùng (UI).
*   `src/main/java/com/auction/server`: Chứa logic Server, Service xử lý nghiệp vụ, DAO (truy cập database) và Manager (quản lý thời gian thực).
*   `src/main/java/com/auction/models`: Các thực thể (Entity) và DTO (Data Transfer Objects) dùng chung giữa Client và Server.
*   `src/main/resources`: Chứa các tệp FXML (giao diện), cấu hình database và file schema SQL.
*   `src/test/java`: Hệ thống các bài kiểm thử unit test và UI test.

## 4. Câu lệnh chạy chương trình (Đa nền tảng)
Dự án sử dụng Maven, nên các lệnh dưới đây hoạt động trên **Windows, Linux và MacOS**.

### Bước 1: Biên dịch và cài đặt thư viện
```bash
mvn clean install
```

### Bước 2: Chạy Server
Mở một cửa sổ dòng lệnh và chạy:
```bash
mvn exec:java -Dexec.mainClass="com.auction.server.ServerApp"
```

### Bước 3: Chạy Client
Mở một cửa sổ dòng lệnh khác (có thể mở nhiều cửa sổ để chạy nhiều client):
```bash
mvn exec:java -Dexec.mainClass="com.auction.ClientLauncher"
```

## 5. Hướng dẫn vận hành hệ thống
Để hệ thống hoạt động chính xác, vui lòng tuân thủ thứ tự sau:

1.  **Cấu hình Database:** Chạy file `src/main/resources/schema.sql` trong MySQL để tạo cấu trúc bảng. Cập nhật thông tin đăng nhập trong `src/main/resources/database.properties`.
2.  **Khởi động Server:** Chạy `ServerApp`. Server sẽ khởi tạo Connection Pool và lắng nghe các kết nối tại cổng 8080.
3.  **Khởi động Client:** Chạy `ClientLauncher`. 
    *   *Lưu ý khi chạy qua LAN:* Nếu bạn muốn bạn bè kết nối, hãy thay đổi `SERVER_HOST` trong `AppConstants.java` thành địa chỉ IP máy tính của bạn (ví dụ: `192.168.1.5`).
4.  **Đăng nhập/Đăng ký:** Sử dụng các tài khoản khác nhau ở các cửa sổ client khác nhau để kiểm tra tính năng đấu giá đồng thời.

## 6. Danh sách chức năng đã hoàn thành
- [x] **Xác thực:** Đăng nhập, Đăng ký (Ràng buộc mật khẩu 6 ký tự).
- [x] **Tạo đấu giá:** Người bán có thể tạo sản phẩm theo danh mục (Art, Electronics, Vehicle, Others) với các thuộc tính riêng biệt.
- [x] **Giao diện danh sách:** Tìm kiếm theo tên/mô tả và lọc theo danh mục sản phẩm.
- [x] **Đấu giá thời gian thực:** Đồng bộ giá hiện tại và người đặt giá cao nhất tức thời qua Socket.
- [x] **Anti-sniping:** Tự động gia hạn 5 phút nếu có người đặt giá trong 5 phút cuối cùng.
- [x] **Xử lý đồng thời:** Khóa (Locking) dữ liệu khi nhiều người cùng đặt giá trên một sản phẩm.
- [x] **Quản lý ví:** Nạp tiền vào tài khoản và trừ tiền khi thanh toán sản phẩm thắng cuộc.
- [x] **Hệ thống Test:** 39 bài test tự động bao quát Model, Service, Factory và UI.
- [x] **CI/CD:** Tự động chạy kiểm thử trên GitHub mỗi khi push code.

## 7. Link to PDF and Video demo:
- https://drive.google.com/drive/folders/1QkRp0HjightooyX07aA6FYpnFHwsVMWw?usp=sharing
