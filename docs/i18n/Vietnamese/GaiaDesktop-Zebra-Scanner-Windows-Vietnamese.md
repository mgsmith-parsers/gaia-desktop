# Thiết lập máy quét Zebra — GAIA Desktop cho Windows

Cách cài đặt và cấu hình máy quét mã vạch Zebra để GAIA Desktop đọc mã vạch GS1 trực tiếp từ máy:
mỗi lần quét đều được phân tích và thêm vào lịch sử, giữ nguyên tiền tố AIM Code ID và mọi ký tự
phân cách FNC1. Tài liệu gốc:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

GAIA Desktop được xây dựng và kiểm thử với máy quét ảnh cầm tay **Zebra DS8108** kết nối qua USB.
Các bước áp dụng cho mọi máy quét 2D của Zebra được Scanner SDK của Zebra hỗ trợ; mã vạch thiết lập
và tên menu cho từng mẫu máy nằm trong Quick Reference Guide hoặc Product Reference Guide của chính
mẫu đó.

> **Chỉ dành cho Windows.** Trình điều khiển máy quét của Zebra chỉ có cho Windows, nên trên macOS
> hoặc Linux, GAIA Desktop không tạo điều khiển máy quét nào. Mọi thứ khác trong ứng dụng đều chạy
> trên mọi nền tảng.

---

## Mục lục

1. [Những gì bạn cần](#1-những-gì-bạn-cần)
2. [Cài đặt Java](#2-cài-đặt-java)
3. [Cài đặt Zebra Scanner SDK](#3-cài-đặt-zebra-scanner-sdk)
4. [Cài đặt 123Scan](#4-cài-đặt-123scan)
5. [Cập nhật firmware máy quét Zebra](#5-cập-nhật-firmware-máy-quét-zebra)
6. [Đặt chế độ USB cho máy quét Zebra](#6-đặt-chế-độ-usb-cho-máy-quét-zebra)
7. [Cấu hình máy quét Zebra cho GS1](#7-cấu-hình-máy-quét-zebra-cho-gs1)
8. [Cài đặt và khởi động GAIA Desktop](#8-cài-đặt-và-khởi-động-gaia-desktop)
9. [Kết nối máy quét](#9-kết-nối-máy-quét)
10. [Tham chiếu cấu hình](#10-tham-chiếu-cấu-hình)
11. [Khắc phục sự cố](#11-khắc-phục-sự-cố)
12. [Các máy quét 2D Zebra được hỗ trợ](#12-các-máy-quét-2d-zebra-được-hỗ-trợ)

---

## 1. Những gì bạn cần

| | |
|---|---|
| **Máy quét** | Một máy quét 2D Zebra, kèm cáp USB — xem [Các máy quét 2D Zebra được hỗ trợ](#12-các-máy-quét-2d-zebra-được-hỗ-trợ) |
| **Máy tính** | Windows 10 hoặc 11, 64-bit |
| **Java** | Java 21 trở lên, **64-bit** |
| **Zebra Scanner SDK for Windows** | **64-bit**, từ trang hỗ trợ của Zebra — cung cấp trình điều khiển và các dịch vụ JavaPOS mà GAIA Desktop sử dụng |
| **Zebra 123Scan** | Tiện ích cấu hình miễn phí của Zebra, từ trang hỗ trợ của Zebra — dùng để cập nhật firmware máy quét và kiểm tra cài đặt của máy |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Tùy chọn** | **Product Reference Guide** (PRG) của máy quét, chứa các mã vạch thiết lập |

Các thành phần kết nối với nhau thế nào:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop tự mang theo API JavaPOS và cấu hình đặt tên cho máy quét (`jpos.xml`, bên trong
`gaia-desktop.jar`). Zebra SDK cung cấp phần còn lại: trình điều khiển giao tiếp với máy quét và các
lớp dịch vụ của Zebra nằm giữa JavaPOS và trình điều khiển đó.

---

## 2. Cài đặt Java

GAIA Desktop cần **Java 21 trở lên**, và phải là bản **64-bit** để khớp với trình điều khiển Zebra
64-bit.

1. Cài đặt JDK hoặc JRE Java 21 cho Windows x64 — ví dụ Eclipse Temurin từ
   adoptium.net. Hãy để trình cài đặt đặt `JAVA_HOME` nếu nó đề nghị.
2. Kiểm tra trong Command Prompt:

   ```
   java -version
   ```

   Dòng đầu tiên phải hiển thị phiên bản 21 trở lên; Java 64-bit còn in ra `64-Bit Server VM`.

`run.bat` tìm Java theo thứ tự sau: thư mục `jdk*` bên cạnh `run.bat`, rồi `JAVA_HOME`, rồi `java`
trên `PATH`. Phiên bản nào cũ hơn 21 sẽ bị từ chối kèm thông báo giải thích.

---

## 3. Cài đặt Zebra Scanner SDK

1. Tải **Zebra Scanner SDK for Windows (64-bit)** từ trang hỗ trợ của Zebra.
2. Chạy trình cài đặt. Nếu có lựa chọn thành phần, hãy đảm bảo thành phần **JPOS** (JavaPOS) được
   chọn.
3. Giữ nguyên vị trí cài đặt mặc định. GAIA Desktop tìm phần JavaPOS của SDK tại:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   và mong đợi các tệp jar dịch vụ của Zebra (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …)
   cùng các thư viện gốc (`CSJPOSScanner64.dll`, …) nằm trong thư mục `bin\` của nó. Nếu SDK được
   cài ở nơi khác, xem [ZEBRA_HOME](#zebra_home).
4. **Khởi động lại Windows** sau khi cài đặt. Trình điều khiển CoreScanner của SDK chạy nền và phải
   đang chạy trước khi GAIA Desktop có thể truy cập máy quét; khởi động lại là cách đơn giản nhất để
   chắc chắn điều đó.

---

## 4. Cài đặt 123Scan

123Scan là tiện ích miễn phí của Zebra để cấu hình máy quét của hãng. Tiện ích này được dùng ở bước
tiếp theo để cập nhật firmware máy quét, và là cách dễ nhất để kiểm tra và thay đổi cài đặt của máy
về sau.

1. Tải **123Scan** từ trang hỗ trợ của Zebra và chạy trình cài đặt.
2. Cắm máy quét Zebra và khởi động 123Scan. Tiện ích sẽ tìm thấy máy quét và hiển thị mẫu máy, số sê-ri
   và phiên bản firmware hiện tại. Nếu không, hãy thử cổng USB hoặc cáp khác trước khi tiếp tục —
   GAIA Desktop sẽ không thấy máy quét mà 123Scan không thấy.

---

## 5. Cập nhật firmware máy quét Zebra

Hãy đưa firmware của máy quét lên phiên bản mới nhất trước khi cấu hình. Bản cập nhật firmware sửa các
lỗi giải mã và giữ máy quét đồng bộ với trình điều khiển Zebra do SDK cài đặt.

1. Đóng **GAIA Desktop** và mọi chương trình khác có thể đang dùng máy quét: 123Scan cần độc chiếm
   máy quét trong suốt quá trình cập nhật.
2. Trong 123Scan, khi máy quét đang được kết nối, chọn tùy chọn cập nhật firmware của máy. 123Scan so
   sánh firmware của máy quét với bản mới nhất Zebra phát hành và đề nghị tải về và cài đặt. Nếu máy tính
   không có internet, hãy tải firmware của máy quét từ trang hỗ trợ của nó trên trang web Zebra và nạp
   tệp vào 123Scan.
3. Bắt đầu cập nhật và **giữ máy quét cắm cho đến khi 123Scan báo đã hoàn tất**. Quá trình mất vài
   phút; máy quét sẽ khởi động lại ở cuối. Rút máy giữa chừng có thể khiến nó không dùng được nữa.
4. Kiểm tra rằng 123Scan giờ hiển thị phiên bản firmware mới.

Bản cập nhật có thể đưa một số cài đặt về mặc định, đó là lý do máy quét được cấu hình ở hai bước tiếp
theo, sau firmware.

---

## 6. Đặt chế độ USB cho máy quét Zebra

Đây là bước hay bị bỏ sót nhất. **Khi xuất xưởng, máy quét Zebra là một bàn phím USB** (chế độ USB HID
Keyboard): nó "gõ" từng mã vạch vào nơi đang có tiêu điểm. Ở chế độ đó, *Kết nối máy quét* của
GAIA Desktop không tìm thấy nó, và mọi thứ gõ vào ô văn bản đều mất ký tự phân cách FNC1, làm thay đổi
ý nghĩa của một chuỗi phần tử GS1.

Chuyển máy quét sang chế độ mà trình điều khiển Zebra giao tiếp trực tiếp:

| Chế độ USB của máy quét | Tên thiết bị trong GAIA Desktop | Ghi chú |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`, hoặc `ZebraAllScanners` | Khuyên dùng |
| SNAPI (có hoặc không có chụp ảnh) | `ZebraScannerSNAPI`, hoặc `ZebraAllScanners` | Giao thức USB riêng của Zebra; cũng hoạt động |
| IBM Table-top USB | `ZebraUSBTableTop`, hoặc `ZebraAllScanners` | Dành cho máy quét đặt bàn |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`, hoặc `ZebraAllScanners` | |
| USB HID Keyboard (mặc định của nhà sản xuất) | — | **Sẽ không kết nối** |

`ZebraAllScanners`, tên thiết bị mặc định trong GAIA Desktop, chấp nhận máy quét ở bất kỳ chế độ được
hỗ trợ nào, nên khi đã đặt chế độ, giá trị mặc định thường hoạt động ngay.

Để đổi chế độ, hãy:

- **quét mã vạch thiết lập** cho chế độ đó — chẳng hạn *IBM Hand-held USB* — trong Quick Reference
  Guide hoặc Product Reference Guide của máy quét (mục USB Host Types / USB Device Type); máy quét kêu bíp
  để xác nhận. Hoặc
- **dùng 123Scan** ([bước 4](#4-cài-đặt-123scan)): kết nối máy quét, mở cấu hình của nó và đặt loại
  máy chủ USB.

Sau khi đổi chế độ, Windows nhận diện lại máy quét như một loại thiết bị khác; hãy chờ vài giây trước
khi kết nối.

Để đưa máy quét về chế độ bàn phím sau này — để dùng với phần mềm khác — hãy quét mã vạch thiết lập
*USB Keyboard (HID)*, hoặc khôi phục cài đặt gốc.

---

## 7. Cấu hình máy quét Zebra cho GS1

GAIA Desktop phân tích tốt nhất khi máy quét cho nó biết loại mã vạch vừa đọc.

### Gửi AIM Code ID

Đặt **Transmit Code ID Character** thành **AIM Code ID Character**, dùng mã vạch thiết lập trong
Product Reference Guide (User Preferences) hoặc trong 123Scan. Khi đó mỗi lần quét bắt đầu bằng mã
nhận dạng ký hiệu học — `]C1` cho GS1-128, `]d2` cho GS1 DataMatrix, `]Q3` cho GS1 QR Code, `]e0` cho
GS1 DataBar — mà GAIA Desktop dùng để gọi tên vật mang dữ liệu và áp dụng quy tắc tương ứng.

Không có nó, GAIA Desktop vẫn phân tích chuỗi phần tử và lấy ký hiệu học từ chính lần quét, nhưng báo
cáo không có AIM Code ID để hiển thị, và một mã vạch thông thường như EAN-13 khó phân biệt hơn với
chuỗi phần tử GS1.

### Kiểm tra các ký hiệu học

Đảm bảo các loại mã vạch bạn làm việc đã được **bật**: thường là GS1-128, GS1 DataMatrix,
GS1 DataBar, GS1 QR Code và EAN/UPC. Phần lớn được bật mặc định trên máy quét 2D của Zebra; 123Scan
hiển thị cài đặt hiện tại, và Product Reference Guide có mã vạch để bật từng loại.

### Tắt các tính năng phân tích GS1 của Zebra

Firmware mới thêm các tính năng GS1 chạy ngay trên máy quét — **Label Parse+**, **GS1 Digital Link**
và **GS1 Retail POS parsing**. Hãy để chúng **tắt**, đúng như khi máy quét xuất xưởng: chúng định dạng
lại hoặc rút gọn những gì máy quét gửi, trong khi GAIA Desktop cần dữ liệu thô để phân tích và kiểm
tra. Xem [Các máy quét 2D Zebra được hỗ trợ](#12-các-máy-quét-2d-zebra-được-hỗ-trợ) để biết từng tính
năng làm gì.

### Không dùng tiền tố và hậu tố

Đừng thêm tiền tố, hậu tố hay phím Enter vào dữ liệu ra của máy quét. GAIA Desktop nhận mỗi lần quét
như một thông điệp hoàn chỉnh qua JavaPOS và không cần bất kỳ thứ nào trong số đó; ký tự thừa sẽ lọt
vào chuỗi phần tử.

---

## 8. Cài đặt và khởi động GAIA Desktop

1. Giải nén `gaia-desktop-1.0.0-win.zip`, ví dụ vào `C:\Tools\`. Bên trong có một thư mục:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Cắm máy quét.
3. Nhấp đúp **`run.bat`**. Một cửa sổ console mở ra bên cạnh ứng dụng và luôn mở: trạng thái máy quét
   và lỗi JavaPOS được báo ở đó.
4. Đọc dòng đầu tiên trong console. Dòng này cho biết `run.bat` đã tìm thấy gì:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Nếu dòng kết thúc bằng **`Zebra services from none (Zebra Scanner SDK not installed)`**, nghĩa là
   không tìm thấy SDK ở vị trí mặc định — hãy cài đặt nó ([bước 3](#3-cài-đặt-zebra-scanner-sdk)) hoặc
   đặt [ZEBRA_HOME](#zebra_home). Ứng dụng vẫn khởi động, nhưng máy quét không thể kết nối.

---

## 9. Kết nối máy quét

1. Trên thanh công cụ, **bộ chọn thiết bị** liệt kê các tên máy quét mà GAIA Desktop biết. Hãy để ở
   **`ZebraAllScanners`**, hoặc chọn tên ứng với chế độ USB của máy quét trong bảng ở
   [bước 6](#6-đặt-chế-độ-usb-cho-máy-quét-zebra).
2. Nhấp **Kết nối máy quét**, hoặc nhấn **Ctrl+K** (cũng có trong menu **Máy quét**).
3. Thanh trạng thái đổi thành **`máy quét: ZebraAllScanners`** (hoặc tên bạn đã chọn), và dòng trạng
   thái báo *Đã kết nối máy quét — ZebraAllScanners*. Nút giờ hiển thị **Ngắt kết nối máy quét**.
4. Quét một mã vạch GS1. Mã xuất hiện ở đầu lịch sử ghi nhận và báo cáo của nó mở ra. Khi AIM Code ID
   được bật, báo cáo gọi tên vật mang dữ liệu — *GS1-128*, *GS1 DataMatrix* — và chuỗi phần tử giữ
   nguyên mọi ký tự phân cách FNC1.

Bộ chọn thiết bị nhớ lựa chọn của bạn giữa các lần chạy. Máy quét vẫn kết nối cho đến khi bạn ngắt kết
nối hoặc đóng ứng dụng. Việc quét cũng hoạt động ở chế độ **Tạo** và **Kết hợp**: trong Kết hợp, mỗi
lần quét trở thành một trong các mã vạch được kết hợp.

Khi GAIA Desktop đang giữ máy quét, các chương trình khác không thể dùng nó, và ngược lại: hãy đóng
123Scan và mọi phần mềm bán hàng (point-of-sale) hay phần mềm máy quét khác trước khi kết nối.

---

## 10. Tham chiếu cấu hình

### Tên thiết bị

Bộ chọn thiết bị được điền từ `jpos.xml` đi kèm trong `gaia-desktop.jar`. Các mục máy quét trong đó:

| Tên thiết bị | Loại máy quét | Dùng với |
|---|---|---|
| `ZebraAllScanners` | bất kỳ | **Mặc định.** Mọi chế độ USB được hỗ trợ |
| `ZebraAllScanners114` | bất kỳ | Phiên bản JavaPOS 1.14 của cùng dịch vụ |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Máy quét ở chế độ IBM Hand-held USB |
| `ZebraUSBTableTop` | IBM Table-top USB | Chế độ đặt bàn |
| `ZebraUSBOPOS` | USB OPOS | Chế độ USB OPOS |
| `ZebraScannerSNAPI` | SNAPI | Chế độ SNAPI |
| `ZebraScannerUSB` | USB bất kỳ | Mọi chế độ USB |
| `ZebraScannerSerialSSI` | nối tiếp SSI | Máy quét dùng cáp nối tiếp (không phải USB) |
| `ZebraScannerSerialNIXMODB` | nối tiếp NIXMODB | Máy quét dùng cáp nối tiếp |
| `ZebraVDI` | VDI | Máy quét được chuyển tiếp vào máy tính ảo |

### ZEBRA_HOME

Nếu Zebra SDK được cài ở nơi khác vị trí mặc định, hãy đặt `ZEBRA_HOME` trỏ tới thư mục `JPOS` của
nó trước khi chạy `run.bat` — ví dụ trong một tệp batch nhỏ đặt cạnh nó:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

Khi đó `run.bat` lấy các jar dịch vụ của Zebra từ `%ZEBRA_HOME%bin` và thêm thư mục đó vào `PATH`
cho các DLL của Zebra.

### jpos.xml của riêng bạn

Để thêm tên thiết bị hoặc đổi cài đặt của một mục, hãy sao chép `jpos.xml` ra khỏi
`gaia-desktop.jar` (đó là tệp zip; mọi công cụ giải nén đều mở được), sửa bản sao, rồi trỏ
GAIA Desktop tới đó:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

Bộ chọn thiết bị khi đó liệt kê các máy quét trong tệp này. Khi có nhiều máy quét Zebra được gắn, một
mục có thể được gắn với một máy cụ thể qua thuộc tính `ScannerModel` hoặc `SerialNumber`, như trong
mục `ZebraAllScanners`.

### Tùy chọn khác

| Cài đặt | Cách làm |
|---|---|
| Ngôn ngữ của ứng dụng | `run.bat --language=FR` (chỉ lần chạy này), hoặc Bảng tùy chọn |
| Tùy chọn Java bổ sung | `set GAIA_DESKTOP_OPTS=…` trước `run.bat` |
| Java đi kèm | đặt thư mục `jdk-21…` cạnh `run.bat`; nó được dùng trước `JAVA_HOME` |

---

## 11. Khắc phục sự cố

Lỗi xuất hiện trong hộp thoại khi kết nối, trong cửa sổ console, và trong tệp nhật ký
`logs\gaia-desktop.log`, trong thư mục nơi `run.bat` được khởi chạy (là thư mục GAIA Desktop khi
nhấp đúp `run.bat`). Số trong dấu ngoặc vuông trong nhật ký là mã lỗi JavaPOS.

| Bạn thấy gì | Nguyên nhân có thể | Cách xử lý |
|---|---|---|
| Không có nút *Kết nối máy quét* | Không chạy trên Windows | Chức năng quét chỉ có trên Windows |
| 123Scan không tìm thấy máy quét | Cáp, cổng USB, hoặc máy quét đang bị chương trình khác giữ | Thử cáp hoặc cổng khác; đóng GAIA Desktop và phần mềm máy quét khác. GAIA Desktop sẽ không thấy máy quét mà 123Scan không thấy |
| Console: `Zebra services from none (Zebra Scanner SDK not installed)` | SDK không ở vị trí mặc định | Cài SDK 64-bit ([bước 3](#3-cài-đặt-zebra-scanner-sdk)), hoặc đặt [ZEBRA_HOME](#zebra_home) |
| Lỗi **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Không tìm thấy jar dịch vụ của Zebra | Như trên — kiểm tra dòng `Zebra services from` trong console |
| Lỗi **109**: *Service does not exist in loaded JCL registry* | Tên thiết bị không có trong cấu hình máy quét, hoặc `javaPOS114.jar` của SDK lọt vào class path | Chọn một tên từ bộ chọn thiết bị; đừng thêm `javaPOS114.jar`; nếu dùng `jpos.xml` riêng, kiểm tra tên có trong đó |
| Lỗi **107** (không có phần cứng), hoặc kết nối thất bại dù máy quét đã cắm | Máy quét vẫn ở chế độ bàn phím USB, hoặc trình điều khiển CoreScanner không chạy | Đặt chế độ USB ([bước 6](#6-đặt-chế-độ-usb-cho-máy-quét-zebra)); khởi động lại Windows sau khi cài SDK; thử cổng USB khác |
| Lỗi **112** (hết thời gian) hoặc **102** (đã bị chiếm) khi kết nối | Chương trình khác đang giữ máy quét | Đóng 123Scan và mọi phần mềm máy quét hay bán hàng khác, rồi kết nối lại |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` trong console | Không nạp được DLL của Zebra — thường do SDK 32-bit với Java 64-bit | Cài SDK 64-bit; kiểm tra `java -version` báo VM 64-bit |
| Lần quét bị gõ vào thanh nhập thay vì đến dưới dạng bản ghi nhận | Máy quét đang ở chế độ bàn phím USB | Đặt chế độ USB ([bước 6](#6-đặt-chế-độ-usb-cho-máy-quét-zebra)) |
| Đã kết nối nhưng lần quét không đến | Sai tên thiết bị cho chế độ của máy quét, hoặc máy quét bị cắm lại khi đang kết nối | Ngắt kết nối, chọn `ZebraAllScanners`, kết nối lại |
| Báo cáo không hiển thị AIM Code ID / vật mang dữ liệu | Việc gửi AIM Code ID đang tắt | Bật *Transmit Code ID Character → AIM Code ID* ([bước 7](#7-cấu-hình-máy-quét-zebra-cho-gs1)) |
| Chuỗi phần tử thiếu ký tự phân cách, hoặc các AI dính liền nhau | Quét ở chế độ bàn phím, hoặc gõ tay không có `{GS}` | Dùng chế độ USB JavaPOS; khi gõ, chèn ký tự phân cách bằng nút `{GS}` |
| Một loại mã vạch không bao giờ được đọc | Ký hiệu học đó bị tắt trên máy quét | Bật nó lên ([bước 7](#7-cấu-hình-máy-quét-zebra-cho-gs1)) |

Nếu sự cố vẫn còn, hãy giữ lại cùng nhau dòng `run.bat:` trong console, nội dung hộp thoại lỗi và
`logs\gaia-desktop.log`: kết hợp lại, chúng cho thấy Java nào, dịch vụ Zebra nào và tên thiết bị nào
đã được dùng, và kết nối thất bại chính xác ở đâu.

---

## 12. Các máy quét 2D Zebra được hỗ trợ

GAIA Desktop cần một máy quét đọc được các mã vạch dùng để in dữ liệu GS1 — GS1-128, GS1 DataBar,
EAN/UPC, và với phần lớn mục đích là các mã 2D: **GS1 DataMatrix** và **GS1 QR Code**, cũng là cách
một URI **GS1 Digital Link** thường được in. Nghĩa là cần một **máy quét ảnh 2D** của Zebra: máy quét
tuyến tính (1D) hoàn toàn không đọc được mã DataMatrix hay QR.

Bản thân máy quét không cần hiểu GS1 Application Identifiers hay Digital Link.
**GAIA Desktop tự phân tích cả hai**: ứng dụng lấy lần quét đúng như máy quét đọc — AIM Code ID, ký tự
phân cách FNC1 và tất cả — rồi tự tách thành các AI, hoặc chuyển URI Digital Link thành các AI của nó.

### Đã kiểm thử

| Máy quét | Loại | Tình trạng |
|---|---|---|
| **DS8108** | Máy quét ảnh 2D cầm tay có dây | **Được xây dựng và kiểm thử** với GAIA Desktop |

### Dự kiến hoạt động

Đây là các máy quét ảnh 2D của Zebra, giống DS8108, được điều khiển qua Scanner SDK và các dịch vụ
JavaPOS của Zebra. Chúng **chưa được kiểm thử** với GAIA Desktop; hãy thiết lập từng máy như trong
hướng dẫn này, dùng Quick Reference Guide hoặc Product Reference Guide riêng của máy cho mã vạch
thiết lập.

| Dòng máy | Mẫu (ví dụ) |
|---|---|
| DS81x8 | DS8108 (có dây), DS8178 (không dây) |
| DS22x8 | DS2208 (có dây), DS2278 (không dây) — đa dụng |
| DS36x8 | DS3608 (có dây), DS3678 (không dây) — chắc chắn, gồm cả biến thể KD và XR |
| DS46x8 | DS4608 (có dây), DS4678 (không dây) |
| DS9308 / DS9908 | Máy quét trình diện và rảnh tay |
| MP7000 / MP7200 | Máy quét bioptic đa mặt phẳng và máy quét kiêm cân |
| CS6080 | Máy quét đồng hành không dây |

Máy quét không dây giao tiếp với đế sạc của nó, và đế mới là thứ cắm vào máy tính: kết nối đế qua USB
và coi cặp này như một máy quét. Máy quét thuộc các dòng Zebra khác cũng có thể hoạt động, nếu Scanner
SDK của Zebra hỗ trợ chúng ở một trong các chế độ USB tại [bước 6](#6-đặt-chế-độ-usb-cho-máy-quét-zebra).

### Tính năng GS1 ngay trên máy quét Zebra

Ngoài việc đọc mã vạch, một số máy quét ảnh 2D của Zebra có thể xử lý dữ liệu GS1 ngay trên máy quét,
được cấu hình qua 123Scan:

| Tính năng | Chức năng | Với GAIA Desktop |
|---|---|---|
| **Label Parse+** | Phân tích các AI của nhãn GS1 ngay trên máy quét và gửi các trường đã định dạng lại | **Để tắt** — GAIA Desktop sẽ không còn thấy AI, ký tự phân cách FNC1 và AIM Code ID |
| **GS1 Digital Link** | Với sản phẩm bán lẻ mang cả mã EAN/UPC 1D và mã Digital Link 2D: chọn mã nào được gửi | **Để tắt** (mặc định) |
| **GS1 Retail POS parsing** | Đọc mã vạch bán lẻ 2D theo cú pháp URI Digital Link hoặc chuỗi phần tử | **Để tắt** |

Cài đặt **GS1 Digital Link** có ba chế độ. **Mode 1 — GTIN only**, chế độ khởi đầu khi được bật, chỉ
gửi GTIN, bất kể từ mã vạch nào, nên lô, hạn sử dụng hay số sê-ri trong Digital Link sẽ không bao giờ
đến được GAIA Desktop. **Mode 2 — Prioritize 2D** gửi mã 2D nếu thấy nó trong thời gian chờ ưu tiên
(mặc định 200 ms), nếu không thì gửi mã 1D. **Mode 3 — Corroborate 1D and 2D GTINs Match** gửi mã 1D,
và cả mã 2D khi GTIN của nó khớp. Nếu buộc phải bật cài đặt này cho phần mềm khác, hãy dùng Mode 2,
không phải Mode 1. Trên DS8108, đây là các tham số 2373 (bật/tắt), 2374 (chế độ) và 2491 (thời gian
chờ).

Dùng các tính năng này cần **123Scan phiên bản 6 trở lên** và firmware có chứa chúng. Zebra bổ sung hỗ
trợ GS1 Digital Link qua các bản cập nhật firmware từ tháng 4 năm 2025; các ngày dưới đây lấy từ một
đại lý Zebra, vì vậy hãy kiểm tra ghi chú phát hành firmware của từng mẫu máy:

| Dòng máy | Ngày phát hành firmware GS1 Digital Link |
|---|---|
| DS81x8 | 16 tháng 4 năm 2025 |
| MP7000 | 21 tháng 4 năm 2025 |
| DS9908 | 6 tháng 6 năm 2025 |
| SP72 / SM72 | 11 tháng 7 năm 2025 |
| DS46x8 | 28 tháng 7 năm 2025 |
| MP7200 (Rev-C), DS5502 | 30 tháng 7 năm 2025 |
| DS9308 | 29 tháng 8 năm 2025 |
| DS22x8, DS1000 | 5 tháng 9 năm 2025 (chỉ khu vực châu Á – Thái Bình Dương) |
| DS36x8 (gồm cả KD và XR), MS47 | 30 tháng 9 năm 2025 |
| PL5000, SE4107 | 15 tháng 10 năm 2025 |
| CS6080 | 31 tháng 12 năm 2025 |

Không tính năng nào trong số này là cần thiết để đọc mã GS1 Digital Link vào GAIA Desktop: khi tắt
chúng, máy quét 2D của Zebra với firmware hiện hành đọc mã QR Digital Link như mọi mã khác, và
GAIA Desktop chuyển URI thành các AI của nó.

Nguồn: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (tháng 8 năm 2025), chương 12
"GS1 Digital Link Parameters" và chương Data Parsing; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (ngày phát hành firmware).
