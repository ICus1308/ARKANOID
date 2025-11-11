# Arkanoid Game - Dự án Lập trình Hướng Đối tượng

## 👥 Tác giả

**Group [Số nhóm] - Class [Mã lớp]**

- [Họ tên 1] - [MSSV 1]
- [Họ tên 2] - [MSSV 2]
- [Họ tên 3] - [MSSV 3]
- [Họ tên 4] - [MSSV 4]

**Giảng viên:** [Tên giảng viên]
**Học kỳ:** [HK1/HK2 - Năm học]

---

## 📖 Mô tả

Đây là một trò chơi **Arkanoid** kinh điển được phát triển bằng Java, là dự án cuối khóa của khóa học **Lập trình Hướng Đối tượng**. Dự án này trình bày việc triển khai các nguyên tắc và mẫu thiết kế OOP.

### ✨ Các tính năng chính:

- ☕ Trò chơi được phát triển bằng **Java 17+** với **JavaFX** cho GUI
- 🎯 Triển khai các nguyên tắc cốt lõi của OOP: **Đóng gói**, **Kế thừa**, **Đa hình** và **Trừu tượng hóa**
- 🏗️ Áp dụng nhiều **mẫu thiết kế**: Singleton, Factory Method, Strategy, Observer và State
- ⚡ Có tính năng **đa luồng** cho trải nghiệm chơi game mượt mà và giao diện người dùng phản hồi nhanh
- 🎨 Bao gồm **hiệu ứng âm thanh**, **hình ảnh động** và **hệ thống tăng sức mạnh**
- 💾 Hỗ trợ chức năng **lưu/tải trò chơi** và **hệ thống bảng xếp hạng**

### 🎮 Cơ chế trò chơi:

- 🏓 Kiểm soát mái chèo để ném bóng và phá hủy các viên gạch
- 🎁 Thu thập sức mạnh cho các khả năng đặc biệt
- 📈 Tiến triển qua nhiều cấp độ với độ khó tăng dần
- 🏆 Ghi điểm và cạnh tranh trên bảng xếp hạng

---

## 📊 Sơ đồ UML

### Biểu đồ lớp

<img width="5554" height="11468" alt="UML" src="https://github.com/user-attachments/assets/fed5e8ff-ae55-43b5-96ee-d61862b3a37c" />

> **Ghi chú:** Có thể sử dụng IntelliJ để tạo Sơ đồ lớp: [Hướng dẫn video](https://www.youtube.com/watch?v=yCkTqNxZkbY)

Sơ đồ UML hoàn chỉnh có sẵn trong thư mục `docs/uml/`

---

## 🏗️ Triển khai Mẫu thiết kế

### 1. Mẫu Singleton

**Được sử dụng trong:** `GameManager`, `AudioManager`, `ResourceLoader`

**Mục đích:** Đảm bảo chỉ có một phiên bản tồn tại trong toàn bộ ứng dụng.

**Ví dụ triển khai:**

```java
public class GameManager {
    private static GameManager instance;

    private GameManager() {
        // Private constructor
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
}
```

### 2. Mẫu Factory Method

**Được sử dụng trong:** `PowerUpFactory`, `BrickFactory`

**Mục đích:** Tạo đối tượng mà không chỉ định lớp cụ thể của chúng.

### 3. Mẫu Strategy

**Được sử dụng trong:** `MovementStrategy`, `CollisionStrategy`

**Mục đích:** Định nghĩa một họ các thuật toán và làm cho chúng có thể hoán đổi cho nhau.

### 4. Mẫu Observer

**Được sử dụng trong:** `GameEventListener`, `ScoreObserver`

**Mục đích:** Thông báo tự động cho các đối tượng khi có thay đổi trạng thái.

### 5. Mẫu State

**Được sử dụng trong:** `GameState` (Playing, Paused, GameOver)

**Mục đích:** Cho phép đối tượng thay đổi hành vi khi trạng thái nội bộ thay đổi.

---

## 🧵 Triển khai Đa luồng

Trò chơi sử dụng nhiều luồng để đảm bảo hiệu suất mượt mà:

| Luồng | Mục đích | FPS/Tần suất |
|-------|----------|--------------|
| **Game Loop Thread** | Cập nhật logic trò chơi | 60 FPS |
| **Rendering Thread** | Xử lý render đồ họa (JavaFX Application Thread) | 60 FPS |
| **Audio Thread Pool** | Phát hiệu ứng âm thanh không đồng bộ | Theo sự kiện |
| **I/O Thread** | Xử lý các hoạt động lưu/tải mà không chặn UI | Theo yêu cầu |

---

## 🚀 Cài đặt

### Yêu cầu hệ thống:

- **Java:** 17 trở lên
- **Maven:** 3.9+
- **IDE:** IntelliJ IDEA / Eclipse / NetBeans

### Các bước cài đặt:

1. **Sao chép dự án từ kho lưu trữ:**
   ```bash
   git clone [repository-url]
   cd demo
   ```

2. **Mở dự án trong IDE:**
   - Mở IntelliJ IDEA
   - File → Open → Chọn thư mục dự án

3. **Cài đặt dependencies:**
   ```bash
   mvn clean install
   ```

4. **Chạy dự án:**
   ```bash
   mvn javafx:run
   ```

   Hoặc chạy class `MainLauncher` từ IDE

---

## 🎮 Cách sử dụng

### ⌨️ Kiểm soát

| Phím | Hành động |
|------|-----------|
| `←` hoặc `A` | Di chuyển mái chèo sang trái |
| `→` hoặc `D` | Di chuyển mái chèo sang phải |
| `SPACE` | Phóng bóng / Bắn tia laser |
| `P` hoặc `ESC` | Tạm dừng trò chơi |
| `R` | Khởi động lại trò chơi |
| `Q` | Thoát về menu |

### 📋 Hướng dẫn chơi

1. **Bắt đầu trò chơi:** Nhấp vào "Trò chơi mới" từ menu chính
2. **Điều khiển mái chèo:** Sử dụng phím mũi tên hoặc A/D để di chuyển sang trái và phải
3. **Ném bóng:** Nhấn phím `SPACE` để ném bóng từ mái chèo
4. **Phá hủy gạch:** Ném bóng để đập và phá hủy gạch
5. **Thu thập vật phẩm tăng sức mạnh:** Bắt vật phẩm tăng sức mạnh rơi xuống để có được khả năng đặc biệt
6. **Tránh làm mất bóng:** Giữ cho bóng không rơi xuống dưới mái chèo
7. **Hoàn thành cấp độ:** Phá hủy tất cả các viên gạch có thể phá hủy để tiến lên cấp độ tiếp theo

---

## 🎁 Tăng sức mạnh (Power-ups)

| Biểu tượng | Tên | Tác dụng | Thời gian |
|------------|-----|----------|-----------|
| 🟦 | **Mở rộng mái chèo** | Tăng chiều rộng mái chèo | 10 giây |
| 🟥 | **Mái chèo co lại** | Giảm chiều rộng mái chèo | 10 giây |
| ⚡ | **Bóng nhanh** | Tăng tốc độ bóng lên 30% | 8 giây |
| 🐌 | **Bóng chậm** | Giảm tốc độ bóng đi 30% | 8 giây |
| 🎯 | **Nhiều bóng** | Sinh ra thêm 2 quả bóng | Vĩnh viễn |
| 🔫 | **Súng laser** | Bắn tia laser để phá hủy gạch | 15 giây |
| 🧲 | **Nam châm** | Bóng dính vào mái chèo, phóng với SPACE | 12 giây |
| 🛡️ | **Khiên** | Bảo vệ khỏi mất một mạng sống | 1 lần |
| 🔥 | **Quả cầu lửa** | Quả bóng đi qua các viên gạch | 12 giây |

---

## 🏆 Hệ thống tính điểm

| Hành động | Điểm |
|-----------|------|
| Gạch thường (1 hit) | 100 điểm |
| Gạch mạnh (2 hits) | 300 điểm |
| Gạch nổ | 500 điểm + gạch gần đó |
| Thu thập power-up | 50 điểm |
| **Combo nhân điểm:** | |
| 5 gạch liên tiếp | x2 |
| 10 gạch liên tiếp | x3 |
| 15+ gạch liên tiếp | x4 |

---

## 🖼️ Ảnh chụp màn hình

### Menu chính
![Menu chính](https://via.placeholder.com/800x600?text=Main+Menu)

### Gameplay
![Gameplay](https://via.placeholder.com/800x600?text=Gameplay)

### Preview Tuần 1

https://github.com/user-attachments/assets/7a21ead9-b33f-4a71-8542-327b96e43b82

### Preview Tuần 2

https://github.com/user-attachments/assets/9f58c691-46c4-4651-9a3d-10abe993b4d0

### Power-ups trong hành động
![Power-ups](https://via.placeholder.com/800x600?text=Power-ups)

### Bảng xếp hạng
![Leaderboard](https://via.placeholder.com/800x600?text=Leaderboard)

---

## 🎬 Video giới thiệu

[![Video Demo](https://img.youtube.com/vi/VIDEO_ID/0.jpg)](https://www.youtube.com/watch?v=VIDEO_ID)

> Video đầy đủ về gameplay có sẵn trong `docs/demo/gameplay.mp4`

---

## 🔮 Những cải tiến trong tương lai

### 📅 Các tính năng đã lên kế hoạch

#### Các chế độ chơi bổ sung
- ⏱️ Chế độ tấn công theo thời gian
- ♾️ Chế độ sinh tồn với vô số cấp độ
- 👥 Chế độ nhiều người chơi hợp tác

#### Gameplay được cải tiến
- 👹 Trận chiến với boss ở cuối thế giới
- 🎁 Nhiều loại power-up hơn (đóng băng thời gian, tường chắn, v.v.)
- 🏅 Hệ thống thành tích và huy chương
- 🎨 Hệ thống skin tùy chỉnh cho paddle và ball

#### Cải tiến kỹ thuật
- 🎨 Di chuyển sang LibGDX hoặc JavaFX nâng cao để có đồ họa tốt hơn
- ✨ Thêm hiệu ứng hạt và animation nâng cao
- 🤖 Triển khai chế độ đối thủ AI
- 🌐 Thêm bảng xếp hạng trực tuyến với cơ sở dữ liệu backend
- 📱 Hỗ trợ mobile (Android/iOS)

---

## 🛠️ Công nghệ được sử dụng

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| **Java** | 17+ | Ngôn ngữ cốt lõi |
| **JavaFX** | 19.0.2 | Framework GUI |
| **Maven** | 3.9+ | Công cụ build và quản lý dependencies |
| **Jackson** | 2.15.0 | Xử lý JSON cho save/load |
| **JUnit** | 5.9.0 | Unit testing |

---

## 📁 Cấu trúc dự án

```
demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── gameconfig/          # Cấu hình game
│   │   │   ├── gamemanager/         # Quản lý logic game
│   │   │   │   ├── core/            # Core game loop
│   │   │   │   ├── manager/         # Managers (Audio, Resource, etc.)
│   │   │   │   └── ui/              # UI components
│   │   │   ├── gameobject/          # Game objects
│   │   │   │   ├── ball/            # Ball classes
│   │   │   │   ├── brick/           # Brick classes
│   │   │   │   ├── paddle/          # Paddle classes
│   │   │   │   └── powerup/         # Power-up classes
│   │   │   ├── main/                # Main launcher
│   │   │   └── userinterface/       # UI screens
│   │   └── resources/               # Assets (images, sounds, levels)
│   └── test/                        # Unit tests
├── docs/                            # Documentation
├── pom.xml                          # Maven configuration
└── README.md                        # This file
```

---

## 🧪 Testing

### Chạy tests:

```bash
mvn test
```

### Test coverage:

```bash
mvn jacoco:report
```

Báo cáo coverage có sẵn trong `target/site/jacoco/index.html`

---

## 📜 Giấy phép

Dự án này được phát triển **chỉ nhằm mục đích giáo dục**.

### ⚠️ Chính trực học thuật

> Mã nguồn này được cung cấp để **tham khảo**. Vui lòng tuân thủ chính sách chính trực học thuật của trường bạn. Không sao chép trực tiếp mà không hiểu rõ code.

---

## 📝 Ghi chú

- 📚 Trò chơi được phát triển như một phần của chương trình giảng dạy **Lập trình Hướng đối tượng với Java**
- 👨‍💻 Tất cả mã đều được các thành viên trong nhóm viết dưới sự hướng dẫn của giảng viên
- 🎨 Một số tài sản (hình ảnh, âm thanh) có thể được sử dụng cho mục đích giáo dục theo luật sử dụng hợp lý
- 🎯 Dự án chứng minh ứng dụng thực tế của các khái niệm OOP và mẫu thiết kế

---

## 🤝 Đóng góp

Nếu bạn muốn đóng góp cho dự án:

1. Fork dự án
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Mở Pull Request

---

