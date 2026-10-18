# GAIA Desktop — Hướng dẫn người dùng

Cách sử dụng ứng dụng. Về việc biên dịch, sửa đổi hay thêm ngôn ngữ, xem
[hướng dẫn cho lập trình viên](../../GaiaDesktop-Developer.md) (tiếng Anh). Tài liệu gốc:
[GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Mục lục

1. [Đây là gì](#đây-là-gì)
2. [Chạy ứng dụng](#chạy-ứng-dụng)
3. [Cửa sổ](#cửa-sổ)
4. [Chế độ Phân tích](#chế-độ-phân-tích)
   - [Thanh nhập và mã {GS}](#thanh-nhập-và-mã-gs)
   - [Quét mã](#quét-mã)
   - [Mở tệp](#mở-tệp)
   - [Lịch sử bản ghi](#lịch-sử-bản-ghi)
5. [Chế độ Tạo](#chế-độ-tạo)
   - [Trình soạn AI](#trình-soạn-ai)
   - [Những gì trình soạn từ chối](#những-gì-trình-soạn-từ-chối)
   - [Chuỗi phần tử hay Digital Link](#chuỗi-phần-tử-hay-digital-link)
   - [Sửa một bản ghi đã có](#sửa-một-bản-ghi-đã-có)
6. [Báo cáo](#báo-cáo)
   - [Xuất ra tệp](#xuất-ra-tệp)
7. [Tùy chọn phân tích](#tùy-chọn-phân-tích)
   - [Lần chạy đầu tiên](#lần-chạy-đầu-tiên)
8. [Ngôn ngữ](#ngôn-ngữ)
9. [Những gì được ghi nhớ](#những-gì-được-ghi-nhớ)
10. [Phím tắt](#phím-tắt)
11. [Khắc phục sự cố](#khắc-phục-sự-cố)
12. [Giới hạn](#giới-hạn)

---

## Đây là gì

GAIA Desktop làm hai việc với dữ liệu GS1:

- **Phân tích** — gõ, dán, mở hoặc quét một chuỗi phần tử GS1 hay một URI Digital Link rồi đọc
  kết quả dưới dạng báo cáo đầy đủ: mỗi mã định danh ứng dụng (AI) có nghĩa gì, chỗ nào sai và
  vì sao;
- **Tạo** — ghép các cặp AI / giá trị thành một chuỗi phần tử hoặc một URI Digital Link.

Mọi hiểu biết của ứng dụng về GS1 — từng định nghĩa AI, từng quy tắc kiểm tra, từng thông báo
lỗi và từng phần mô tả đã bản địa hóa — đều đến từ bộ máy GAIA bên dưới. Ứng dụng không thêm quy
tắc riêng nào: nó chấp nhận đúng những gì bộ máy chấp nhận.

---

## Chạy ứng dụng

```bash
java -jar gaia-desktop.jar
```

Tệp JAR cần thư mục `lib/` được phát hành kèm bên cạnh — hãy giữ hai thứ đi cùng nhau, nếu không
ứng dụng sẽ không khởi động. (Cũng có thể dựng một tệp JAR độc lập; xem
[hướng dẫn cho lập trình viên](../../GaiaDesktop-Developer.md#packaging).)

Máy phải cài **Java 21 trở lên**. Bản phát hành gắn với một hệ điều hành và một kiến trúc bộ xử
lý cụ thể, vì vậy hãy dùng tệp JAR được dựng cho máy của bạn.

Để mở ứng dụng bằng một ngôn ngữ nhất định mà không đổi ngôn ngữ đã lưu:

```bash
java -jar gaia-desktop.jar --language=FR
```

Giá trị có thể là mã danh mục (`FR`, `PCM`, `ARZ`), tên đầy đủ (`FRENCH`, `WU-CHINESE`), hoặc
thẻ ngôn ngữ (`fr`; `zh-HK` cho tiếng Quảng Đông, `ar-EG` cho tiếng Ả Rập Ai Cập) — không phân
biệt chữ hoa chữ thường. Chỉ định này chỉ có hiệu lực cho lần chạy đó: ngôn ngữ đã lưu của bạn
vẫn nguyên vẹn, nên một lối tắt tạo cho đồng nghiệp không thể đổi ngôn ngữ mà bản sao của chính
bạn mở lên. Chọn ngôn ngữ *bên trong* ứng dụng mới là lựa chọn thật sự và được lưu như thường
lệ. Giá trị không trỏ tới ngôn ngữ nào sẽ bị bỏ qua kèm một ghi chú trong bảng điều khiển, và
ứng dụng mở bằng ngôn ngữ đã lưu thay vì từ chối khởi động.

Khi khởi động, bảng điều khiển in ra một dòng nêu phiên bản ứng dụng, ngày dựng và phiên bản bộ
máy. Đó chính là dòng nên trích dẫn trong một báo cáo lỗi.

---

## Cửa sổ

```
┌────────────────────────────────────────────────────────────────────┐
│ menu bar                                                           │
├────────────────────────────────────────────────────────────────────┤
│ [Parse|Build]  … mode-specific controls …   Save report   Options  │
├────────────────────────────────────────────────────────────────────┤
│ input bar (Parse)   or   AI editor (Build)                         │
├──────────────────┬─────────────────────────────────────────────────┤
│ Captures         │ Parse report                    │ Options       │
│ (newest first)   │                                 │ panel         │
├──────────────────┴─────────────────────────────────────────────────┤
│ status …                                     scanner: off  [About] │
└────────────────────────────────────────────────────────────────────┘
```

> Sơ đồ giữ nguyên nhãn tiếng Anh, giống như mọi khối mã trong tài liệu này. Trên màn hình bạn
> sẽ thấy *Phân tích* / *Tạo*, *Bản ghi*, *Lưu báo cáo*, *Tùy chọn*, *máy quét: tắt* và
> *Giới thiệu*.

**Trên macOS và Linux, hàng liên quan đến máy quét trong hình không tồn tại** — không có nút bật
tắt hay ô chọn thiết bị trên thanh công cụ, không có trình đơn *Máy quét*, cũng không có
*máy quét: tắt* trên thanh trạng thái. Xem [Quét mã](#quét-mã).

Nút chuyển **Phân tích ⇄ Tạo** chỉ thay đổi dải nằm dưới thanh công cụ và các điều khiển liên
quan tới việc thu nhận. Lịch sử, báo cáo, bảng tùy chọn và các lệnh xuất phục vụ cả hai chế độ,
bởi một lần tạo thành công chẳng qua là thêm một bản ghi nữa.

Bảng tùy chọn **đóng trong lần chạy đầu tiên**. Chế độ, việc bảng có hiện hay không, giao diện và
máy quét dùng lần cuối đều được nhớ giữa các lần chạy.

---

## Chế độ Phân tích

### Thanh nhập và mã {GS}

Thanh này nhận một chuỗi phần tử, một URI GS1 Digital Link, hoặc dữ liệu quét thô kèm tiền tố
AIM Code ID (`]d2…`). <kbd>Enter</kbd> bắt đầu phân tích.

Các AI có độ dài thay đổi được ngăn cách bằng **FNC1** — ký tự điều khiển vô hình `0x1D`. Ô văn
bản không thể giữ ký tự đó: nó sẽ bị nuốt lặng lẽ, và `01…` + FNC1 + `10LOT` sẽ đến dưới dạng
`01…10LOT`, một chuỗi phần tử mang nghĩa khác hẳn.

Vì vậy ứng dụng hiển thị dấu ngăn cách thành mã nhìn thấy được **`{GS}`** rồi chuyển ngược lại
trên đường tới bộ phân tích:

- **nút `{GS}`** chèn mã tại vị trí con trỏ;
- **dán** các ký tự ngăn cách thật — bằng bàn phím, bằng trình đơn ngữ cảnh hay bằng cách kéo
  thả — sẽ tự động biến chúng thành `{GS}`, nên không mất gì cả;
- mã được viết đúng như cách báo cáo viết, nên một giá trị chép từ báo cáo có thể dán ngược lại
  nguyên vẹn.

Bản ghi, báo cáo và lịch sử đã lưu đều chứa dấu ngăn cách **thật**. Mã chỉ là thứ bạn nhìn thấy
và gõ vào.

### Quét mã

> **Chỉ Windows.** Zebra chỉ cung cấp trình điều khiển máy quét cho Windows, nên trên macOS và
> Linux ứng dụng hoàn toàn không dựng điều khiển máy quét nào — không nút *Kết nối máy quét*,
> không ô chọn thiết bị, không trình đơn *Máy quét*, không trạng thái máy quét trên thanh trạng
> thái. Chúng không bị làm mờ, mà đơn giản là không tồn tại, vì chẳng có thiết lập hay thiết bị
> nào có thể bật chúng lên. Mọi thứ còn lại, kể cả mở một tệp chứa dữ liệu quét đã lưu, đều chạy
> ở mọi nơi.

*Kết nối máy quét* mở thiết bị được nêu trong ô chọn; `ZebraAllScanners` là giá trị mặc định và
hợp với phần lớn các mẫu máy. Mỗi lần quét đều được phân tích và tự động thêm vào lịch sử.

Dữ liệu máy quét được đọc theo từng byte, nên tiền tố AIM Code ID và mọi dấu ngăn cách FNC1 đến
được bộ phân tích đúng như máy quét đã gửi.

Ở chế độ Tạo, các điều khiển máy quét bị ẩn đi, nhưng máy quét đang kết nối vẫn hoạt động:
*Máy quét → Kết nối / Ngắt kết nối* (<kbd>⌘K</kbd>) vẫn bật tắt được, thanh trạng thái vẫn báo,
và một lần quét đến trong khi trình soạn đang mở vẫn được ghi lại.

Nếu thiết bị không kết nối được, xem [Khắc phục sự cố](#khắc-phục-sự-cố).

### Mở tệp

*Tệp → Mở tệp văn bản…* phân tích **mỗi dòng không rỗng thành một bản ghi riêng** — đây là cách
nhanh để xử lý một loạt dữ liệu quét ai đó gửi cho bạn. Tệp được đọc theo từng byte, nên các dấu
ngăn cách thô bên trong vẫn còn nguyên.

### Lịch sử bản ghi

Mới nhất ở trên. Mỗi dòng hiển thị một chấm hợp lệ / không hợp lệ, một dòng tóm tắt, cùng thời
điểm ghi, nguồn gốc và số AI chứa trong đó. Ngày tháng chỉ xuất hiện ở các bản ghi từ ngày trước;
các dòng của hôm nay chỉ hiện giờ.

Phần tóm tắt là **URI Digital Link** đối với Digital Link, và là **HRI** trong các trường hợp
khác: với một chuỗi phần tử, `(01)09506000134352 (17)261231` dễ đọc hơn
`01095060001343521726123110…`.

Nhấn chuột phải vào một bản ghi để chép phần nhập hoặc HRI của nó, gửi nó lại thanh nhập, mở nó
trong trình soạn, xóa nó, hoặc xóa toàn bộ lịch sử.

---

## Chế độ Tạo

### Trình soạn AI

Một bảng gồm các cặp AI / giá trị. Hãy gõ mã AI, hoặc chọn trong **tất cả những AI mà bộ máy
biết** ở danh sách *Thêm một AI…*; các cột **Tiêu đề**, **Định dạng** và **Mô tả** tự điền trong
lúc bạn gõ, theo ngôn ngữ bạn đã chọn. Danh sách không thể đưa ra một AI mà khâu tạo sẽ từ chối.

Bảng luôn giữ **một dòng trống ở cuối**: gõ vào đó thì dòng kế tiếp hiện ra, nên một chuỗi nhiều
AI chẳng bao giờ cần tới *Thêm dòng*.

Việc soạn thảo hành xử đúng như một bảng nên hành xử: <kbd>Enter</kbd>, <kbd>Tab</kbd>,
<kbd>Shift</kbd>+<kbd>Tab</kbd> và bấm ra chỗ khác đều **giữ lại** thứ bạn đã gõ; <kbd>Esc</kbd>
bỏ đi. <kbd>Tab</kbd> đi từ AI sang Giá trị rồi sang dòng kế, bỏ qua những cột không sửa được.

### Những gì trình soạn từ chối

| | Điều gì xảy ra |
|---|---|
| Một AI mà bộ máy không nhận ra | Dòng đó đỏ lên ngay khi bạn gõ; việc tạo bị từ chối kèm lời giải thích |
| Cùng một AI lần thứ hai, từ danh sách | Không được thêm — một thông báo nêu tên nó, và dòng đã chứa nó được chọn |
| Cùng một AI lần thứ hai, gõ tay | Cả hai dòng bị đánh dấu; việc tạo bị từ chối với lỗi `GE-S004` |
| Không có gì để tạo | Một thông báo; không bản ghi nào được lập |

Một AI chỉ được xuất hiện một lần trong một chuỗi phần tử — vì vậy danh sách từ chối ngay, thay
vì để bạn thử một lần tạo chắc chắn thất bại.

### Chuỗi phần tử hay Digital Link

**Tạo chuỗi phần tử** ghép các dòng lại, chèn dấu ngăn cách FNC1 ở mọi chỗ mà tiêu chuẩn đòi hỏi.
**Tạo Digital Link** sinh ra một URI; hãy mở *Đích Digital Link* để trỏ nó tới bộ phân giải của
riêng bạn thay vì `id.gs1.org` chuẩn:

| Trường | Tác dụng | Để trống nghĩa là |
|---|---|---|
| Tên miền | Máy chủ phân giải, ví dụ `example.com` | `id.gs1.org` |
| Tiền tố đường dẫn | Một đoạn đứng trước đường dẫn AI, ví dụ `/products` | không có tiền tố |

Khi **thành công**, kết quả hành xử y hệt một lần quét: được phân tích, thêm vào lịch sử, dựng
thành báo cáo và lưu lại. Khâu tạo tự kiểm tra bằng cách phân tích chính đầu ra của mình, nên
những gì nó trả về chắc chắn hợp lệ.

Khi **thất bại**, không có gì được ghi lại. Lý do nằm ngay dưới các nút tạo, mỗi vấn đề một dòng,
mỗi dòng nêu lỗi, AI liên quan và lời giải thích của chính bộ máy:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> Cặp dấu nháy lặp trong thông báo đó đến từ danh mục lỗi của bộ máy, không phải từ ứng dụng này.

### Sửa một bản ghi đã có

Nhấn chuột phải vào một bản ghi → **Tạo** sẽ nạp các AI của nó vào trình soạn. Đổi một giá trị,
tạo lại, và kết quả xuất hiện như một bản ghi mới — bản gốc vẫn nguyên.

---

## Báo cáo

Bản ghi đang chọn được dựng thành một báo cáo đầy đủ: dải kết luận, chuỗi phần tử được tách thành
một “dải băng” có nhãn, siêu dữ liệu phân tích, mỗi AI một thẻ kèm các diễn giải, cùng các lỗi và
cảnh báo có hướng dẫn khắc phục.

Với **Digital Link**, mỗi thẻ AI còn mang vai trò cấu trúc của nó — **khóa chính**,
**bổ nghĩa khóa** hay **thuộc tính dữ liệu** — được tô màu theo thứ tự chúng xuất hiện trong URI.
Vai trò chỉ hiện với Digital Link: trong một chuỗi phần tử thường, AI không có vị trí URI nào để
mô tả.

Báo cáo đi theo ngôn ngữ và giao diện mà bạn đã chọn.

### Xuất ra tệp

| | |
|---|---|
| **Lưu báo cáo → Dạng HTML…** (<kbd>⌘S</kbd>) | Ghi đúng những gì đang có trên màn hình: một tệp độc lập, mở được bằng bất kỳ trình duyệt nào |
| **Lưu báo cáo → Dạng PDF…** (<kbd>⌘P</kbd>) | In báo cáo qua hộp thoại in của hệ thống — hãy chọn đích PDF ở đó |

PDF đi qua hộp thoại in vì chính ở đó bạn chọn đích và tên tệp: **PDF ▾ → Lưu dưới dạng PDF**
trên macOS, **Microsoft Print to PDF** trên Windows, **In ra tệp** trên Linux. Các trang được
chia đúng và chữ vẫn chọn được.

Bản in và bản PDF luôn dùng bảng màu sáng, kể cả khi ứng dụng đang ở giao diện tối, đồng thời
giữ nguyên các thẻ AI có màu và dải kết luận.

Nếu máy của bạn hoàn toàn chưa cấu hình máy in hay đích PDF nào, ứng dụng sẽ nói vậy và hướng bạn
sang bản xuất HTML, thứ có thể in từ bất kỳ trình duyệt nào.

---

## Tùy chọn phân tích

Bảng bên phải tập hợp toàn bộ tùy chọn của bộ máy. Đổi bất cứ thứ gì cũng **phân tích lại ngay
bản ghi đang chọn**, nên bảng này còn là cách để hỏi “với các thiết lập này thì dữ liệu vào trông
ra sao?”, chứ không chỉ ảnh hưởng tới lần quét kế tiếp.

| Tùy chọn | Giá trị |
|---|---|
| Chế độ phân tích | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — phân tích sâu tới đâu |
| Ngôn ngữ | 35 danh mục thông báo |
| Thứ tự | `LITTLE` (dd/mm/yyyy), `MIDDLE` (mm/dd/yyyy), `BIG` (yyyy/mm/dd) |
| Dấu phân cách | `SLASH`, `HYPHEN`, `PERIOD` |
| Tháng | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Năm | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Bỏ qua kiểm tra "requires" | Thôi từ chối một AI thiếu mất AI bắt buộc đi kèm (`GE-S005`) |
| Bỏ qua kiểm tra "excludes" | Thôi từ chối những cặp AI mà tiêu chuẩn cấm (`GE-S006`) |

*Đặt lại mặc định* ở chân bảng khôi phục các giá trị mặc định của chính bộ máy.

### Lần chạy đầu tiên

Khi chưa có gì được lưu, ứng dụng lấy thiết lập từ **cấu hình vùng của máy bạn** thay vì từ giá
trị mặc định của bộ máy:

- **ngôn ngữ** lấy theo bản địa của hệ thống, và lùi về tiếng Anh khi không có danh mục tương
  ứng;
- **các tùy chọn ngày** được đọc từ định dạng ngày ngắn trong bản địa của bạn: thứ tự các thành
  phần, dấu phân cách, và việc tháng cùng năm có viết tắt hay không.

> **Lưu ý.** Vì vậy một bản địa viết ngày ngắn với năm hai chữ số thì ở đây cũng bắt đầu bằng năm
> hai chữ số: điều đó khớp với phần còn lại của hệ thống, nhưng khác mặc định bốn chữ số của bộ
> máy. Đổi trong bảng thì lựa chọn ấy được giữ lại.

---

## Ngôn ngữ

Chọn một ngôn ngữ sẽ đổi **toàn bộ ứng dụng** — trình đơn, nút bấm, trình soạn, bảng tùy chọn,
dòng trạng thái và báo cáo — chứ không chỉ các thông báo lỗi. Những ngôn ngữ viết từ phải sang
trái sẽ lật cửa sổ và báo cáo.

Cả 35 ngôn ngữ đều dùng được, với hai lớp phủ sóng:

| | Phạm vi |
|---|---|
| Báo cáo phân tích và mọi thông báo lỗi của bộ máy | **cả 35 ngôn ngữ** |
| Trình đơn, nút bấm và nhãn của chính ứng dụng | **cả 35 ngôn ngữ** |

Các ngôn ngữ viết từ phải sang trái — Ả Rập, Ả Rập Ai Cập, Ba Tư và Urdu — lật cửa sổ và báo cáo
ngay từ lúc ứng dụng mở lên.

Ngôn ngữ được nhớ giữa các lần chạy. Để mở bằng một ngôn ngữ khác chỉ một lần, xem
[`--language`](#chạy-ứng-dụng).

Vài thứ cố ý không dịch trong mọi ngôn ngữ: những tên trường viết tắt in cạnh giá trị thô
(`format`, `position`, `raw`, `len` …) — chúng là mã định danh chứ không phải từ ngữ; tên các ví
dụ và tên các hệ mã vạch — đó là thuật ngữ GS1 và của ngành; cùng một đoạn dài trích tiêu chuẩn,
nơi bản dịch nửa vời sẽ gây hiểu lầm.

---

## Những gì được ghi nhớ

Hai tệp trong `.gaia-desktop` nằm ở thư mục cá nhân của bạn:

| Tệp | Chứa |
|---|---|
| `options.properties` | Mọi tùy chọn phân tích, giao diện, việc bảng tùy chọn có mở hay không, máy quét dùng lần cuối, và chế độ khi đóng cửa sổ |
| `captures.tsv` | Lịch sử bản ghi, mới nhất trước, giới hạn 500 mục |

Tùy chọn được ghi mỗi khi bạn đổi một mục; lịch sử được ghi mỗi khi nó thay đổi. Cả hai đều được
ghi lại lần nữa khi thoát.

Xóa một trong hai tệp chỉ đặt lại phần đó và không gì khác — chúng được để riêng để một lịch sử
hỏng không làm bạn mất thiết lập. Tệp hỏng do sửa tay cũng không phải tai họa: từng thiết lập lùi
về mặc định riêng lẻ, nên một dòng sai chỉ tốn đúng dòng đó.

Lịch sử lưu dữ liệu vào, chứ không lưu kết quả phân tích. Nó được phân tích lại khi nạp, nghĩa là
một bản ghi khôi phục phản ánh các tùy chọn đang có hiệu lực **lúc này**, không phải các tùy chọn
lúc nó được ghi.

---

## Phím tắt

| | |
|---|---|
| <kbd>Enter</kbd> | Phân tích nội dung thanh nhập |
| <kbd>⌘O</kbd> | Mở một tệp văn bản |
| <kbd>⌘S</kbd> | Lưu báo cáo dạng HTML |
| <kbd>⌘P</kbd> | Lưu báo cáo dạng PDF (hộp thoại in của hệ thống) |
| <kbd>⌘G</kbd> | Chèn mã ngăn cách FNC1 |
| <kbd>⌘K</kbd> | Kết nối / ngắt kết nối máy quét (chỉ Windows) |
| <kbd>⌘D</kbd> | Bật tắt giao diện tối |
| <kbd>⌘Q</kbd> | Thoát |

Trong trình soạn: <kbd>Tab</kbd> / <kbd>Shift</kbd>+<kbd>Tab</kbd> di chuyển giữa các ô sửa
được, <kbd>Enter</kbd> xác nhận, <kbd>Esc</kbd> hủy.

---

## Khắc phục sự cố

| Hiện tượng | Nguyên nhân và cách xử lý |
|---|---|
| Ứng dụng không khởi động, hoặc báo `NoClassDefFoundError` | Thư mục `lib/` phải nằm cạnh tệp JAR. Hãy giữ chúng cùng nhau, hoặc dùng bản phát hành độc lập |
| *“Unsupported JavaFX configuration…”* trong bảng điều khiển lúc khởi động | Bình thường và vô hại — bộ công cụ chỉ ghi nhận cách nó được nạp. Ứng dụng vẫn chạy bình thường |
| Dấu ngăn cách bạn dán vào có vẻ biến mất | Đúng như thiết kế: nó được viết lại thành mã nhìn thấy được `{GS}`, còn bộ phân tích vẫn nhận ký tự thật. Xem [mã {GS}](#thanh-nhập-và-mã-gs) |
| Không có nút *Kết nối máy quét* | Bạn đang dùng macOS hoặc Linux, nơi không có trình điều khiển máy quét. Xem [Quét mã](#quét-mã) |
| Máy quét không kết nối được | Hãy kiểm tra tên lô-gic trong ô chọn có khớp với mẫu máy của bạn không, máy quét đã cắm chưa, và có ứng dụng nào khác đang giữ nó không |
| Xuất PDF báo là không có đích | Máy bạn chưa cấu hình máy in hay đích PDF nào. Hãy thêm một cái, hoặc xuất HTML rồi in từ trình duyệt |
| Một mục trình đơn hiện ra thứ gì đó như `act.hri` | Thiếu một nhãn — xin hãy báo lại; đó là lỗi khi dựng bản, không phải một thiết lập |

---

## Giới hạn

- **Quét mã chỉ có trên Windows**, vì trình điều khiển cũng vậy.
- **PDF đi qua hộp thoại in** chứ không phải hộp thoại lưu.
- **Đích Digital Link chỉ nhận tên miền và tiền tố đường dẫn**, không nhận tham số truy vấn phụ
  hay đoạn phân mảnh.
- **Một AI chỉ được xuất hiện một lần** trong một chuỗi phần tử — đó là quy tắc GS1, không phải
  giới hạn của ứng dụng.
