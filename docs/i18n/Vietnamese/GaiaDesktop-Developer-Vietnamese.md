# GAIA Desktop — Hướng dẫn dành cho nhà phát triển

Mô-đun được xây dựng, tổ chức và kiểm thử như thế nào. Để biết ứng dụng làm gì khi nhìn từ bên ngoài, xem
[Hướng dẫn sử dụng](GaiaDesktop-User-Vietnamese.md). Tài liệu gốc:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Mục lục

1. [Mô-đun trong nháy mắt](#mô-đun-trong-nháy-mắt)
2. [Xây dựng và chạy](#xây-dựng-và-chạy)
   - [Đóng gói](#đóng-gói)
3. [Kiến trúc](#kiến-trúc)
   - [Phễu đầu vào](#phễu-đầu-vào)
   - [Luồng](#luồng)
   - [Dựng lại và gắn nhãn lại](#dựng-lại-và-gắn-nhãn-lại)
4. [Token {GS}](#token-gs)
5. [Tích hợp máy quét](#tích-hợp-máy-quét)
   - [Cổng nền tảng](#cổng-nền-tảng)
   - [Symbology](#symbology)
6. [Bản địa hóa](#bản-địa-hóa)
   - [Đối số `--language`](#đối-số---language)
   - [Thêm một ngôn ngữ](#thêm-một-ngôn-ngữ)
   - [Những gì cố ý không dịch](#những-gì-cố-ý-không-dịch)
7. [Định dạng trạng thái lưu trữ](#định-dạng-trạng-thái-lưu-trữ)
8. [Kiểm thử](#kiểm-thử)
9. [Khắc phục sự cố khi xây dựng](#khắc-phục-sự-cố-khi-xây-dựng)
10. [Các hạn chế đã biết và việc còn mở](#các-hạn-chế-đã-biết-và-việc-còn-mở)

---

## Mô-đun trong nháy mắt

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Một ứng dụng khách của thư viện lõi, không hơn. Nó **không chứa bất kỳ kiến thức GS1 nào**: mọi định nghĩa AI, quy tắc
kiểm tra hợp lệ, thông báo lỗi và mô tả bản địa hóa đều đến từ [`GaiaParser`](../../../../gaia/docs/GaiaParser.md) và
[`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). Đặc biệt, danh sách AI không bao giờ được sao chép vào mô-đun
này — bộ chọn của trình tạo đọc chính sổ đăng ký của lõi, nên nó không thể đưa ra một AI mà trình tạo sẽ từ chối.

**Điểm vào:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (`Application` của JavaFX) và
`tools.pantheum.gaia.desktop.Launcher` (`Main-Class` của jar đã đóng gói).

Mô-đun là một nút lá: không có gì phụ thuộc vào nó, và nó không phụ thuộc vào mô-đun hạ nguồn nào khác.

---

## Xây dựng và chạy

- **Java 21** để chạy, và JDK 21 trở lên để xây dựng — mô-đun được biên dịch với `release=21`. JavaFX 21 là dòng LTS
  tương ứng. Jar lõi vẫn là bytecode Java 11 và chạy không cần thay đổi trên 21.
- **Maven** trên `PATH`. Kho mã này không có wrapper.
- **Cài đặt lõi trước.** Mô-đun này lấy `gaia:1.1.0` từ `~/.m2`; một jar lõi cũ hoặc không có là nguyên nhân thường
  gặp của `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` là đối số dòng lệnh duy nhất, và là cách duy nhất để tác động đến quá trình khởi động ngoài
`-Dgaia.desktop.home=…`, vốn trỏ toàn bộ hồ sơ sang nơi khác.

`mvn javafx:run` là đường phát triển: plugin đặt JavaFX trên **module path**, nên bộ công cụ khởi động như một mô-đun
có tên và `GaiaDesktopApp` có thể được khởi chạy trực tiếp. Một jar đã đóng gói không làm được vậy — mọi thứ đều nằm
trên class path — đó là lý do `Main-Class` của nó là lớp đệm `Launcher`. Trong bố cục đó, một lớp con của
`Application` làm `Main-Class` sẽ thất bại với *"JavaFX runtime components are missing"*.

Luôn dùng `clean` — một lần, trước khi xây dựng một nhóm nền tảng, vì nó làm trống `target/dist/` khỏi tất cả chúng.
Xem [Khắc phục sự cố khi xây dựng](#khắc-phục-sự-cố-khi-xây-dựng) để biết lý do.

### Đóng gói

Mọi thứ có thể phát hành đều được tập hợp dưới `target/dist/gaia-desktop-<version>-<platform>/` và không có gì khác, để
thư mục có thể được nén zip hoặc sao chép như một bộ mà không phải nhặt các sản phẩm ra khỏi `target/` bên cạnh
`classes/`, `maven-status/` và phần còn lại của tệp tạm trong quá trình xây dựng. `${dist.dir}` đặt tên cho nó một lần;
mọi plugin ghi sản phẩm đều trỏ vào đó.

#### Mỗi nền tảng một bản dựng

Các jar JavaFX mang mã gốc, nên mỗi bản phân phối dành cho một nền tảng, được đặt tên bởi thuộc tính `fx.platform` dưới
dạng bộ phân loại OpenJFX: `mac-aarch64`, `mac`, `win`, `linux` hoặc `linux-aarch64`. Năm hồ sơ `fx-*` được kích hoạt
theo hệ điều hành sẽ đặt nó thành nền tảng của chính máy đang xây dựng, và đó là tất cả những gì một lệnh `mvn package`
hoặc `mvn javafx:run` thông thường cần; `-Dfx.platform=…` được ưu tiên hơn chúng để xây dựng bất kỳ nền tảng nào khác,
từ bất kỳ máy nào — các jar JavaFX chỉ là tệp tải về. `${dist.dir}` là `target/dist/${fx.platform}`, nên bản dựng của
một nền tảng không bao giờ ghi đè lên bản dựng của nền tảng khác.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Hai chi tiết trong pom làm điều đó hoạt động, và cả hai đều trông như có thể xóa bỏ:

- **Mọi mô-đun JavaFX đều là phụ thuộc trực tiếp** — base, graphics, controls, media và web — mỗi cái có
  `<classifier>${fx.platform}</classifier>` và một loại trừ `org.openjfx:*`. Các pom của OpenJFX chọn jar bắc cầu
  theo hệ điều hành của *máy đang xây dựng*, nên để graphics hoặc base đến theo kiểu bắc cầu sẽ đưa mã gốc của Mac vào
  bản dựng Windows bên cạnh mã gốc của Windows.
- **Thuộc tính là `fx.platform`, không phải `javafx.platform`.** Khi được đặt từ bên ngoài, thuộc tính riêng của
  OpenJFX bị pom cha định nghĩa lại theo chính nó; Maven từ chối pom đó là không hợp lệ và lặng lẽ bỏ các jar nền tảng,
  để lại một bản dựng không có JavaFX.

Cả hai bố cục ghi cùng một đường dẫn, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`, và đúng một trong
hai chạy trong mỗi lần xây dựng. `${jar.phase}`, `assembly.skipAssembly` và `mdep.skip` là thứ ngăn cả hai cùng chiếm
nó.

| | Mặc định | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — chỉ các lớp và tài nguyên của mô-đun này | ~50 MB — các phụ thuộc được giải nén vào trong |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 16 jar, liệt kê từng cái trong `Class-Path` của manifest | — |
| Phát hành | jar **và** `lib/` bên cạnh | một tệp |
| Cũng trong `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) hoặc `run.sh` (các nền tảng khác) | như trên |
| Và | `readme.html` và `docs/` (mọi nền tảng) | như trên |

Mỗi bản phân phối cũng mang theo các hướng dẫn: `docs/` được sao chép giữ nguyên bố cục, để các liên kết giữa hướng dẫn
và bản dịch vẫn hoạt động, trừ công cụ dịch (`check-i18n.py`, `i18n-manifest.json`); và `readme.html`
ở thư mục gốc, một trang mục lục liên kết đến từng hướng dẫn, lấy từ `src/main/dist/readme.html` với `@VERSION@` được
thay bằng phiên bản trong pom. Cả hai đều được ghi bởi bước antrun `pick-launch-script`, bước này xóa `docs/` trước để
hướng dẫn đã bị gỡ bỏ không còn sót lại. Các liên kết trong readme được viết tay — một hướng dẫn được đổi tên hoặc một
bản dịch được thêm dưới `docs/` cũng cần có dòng của nó ở đó.

`Class-Path` của manifest và `lib/` là hai nửa của cùng một thứ: jar liệt kê `lib/<artifact>-<version>.jar` cho mọi
phụ thuộc thời gian chạy, và `copy-dependencies` lấp đầy `lib/` bằng đúng những tên đó. Đó là điều khiến
`java -jar` hoạt động.

#### Ứng dụng macOS và tệp zip phân phối

Một lần thực thi antrun, `mac-app-and-zip`, kết thúc pha `package`. Vì lý do đó plugin antrun được khai báo **sau** các
plugin jar và assembly — Maven chạy các lần thực thi của một pha theo thứ tự khai báo plugin — nên jar đã tồn tại, ở
cả hai kiểu đóng gói, khi nó chạy. (Lần thực thi còn lại của nó, `pick-launch-script`, nằm trong `prepare-package` và
không bị ảnh hưởng.)

Với `mac` và `mac-aarch64`, trước tiên nó dựng `GAIA Desktop.app` trong thư mục phân phối: `Info.plist` (với
`@VERSION@` được thay bằng phiên bản trong pom), trình khởi chạy `Contents/MacOS/gaia-desktop` (được đặt quyền thực
thi) và `gaia-desktop.icns`, tất cả từ `src/main/macos/`, rồi **di chuyển** `gaia-desktop.jar` và `lib/` vào
`Contents/Java`, để gói ứng dụng tự đứng được. `run.sh` tìm ở đó khi không có jar bên cạnh. Trình khởi chạy tìm Java
theo phiên bản (`java_home -v 21+`, rồi openjdk của Homebrew — khởi chạy từ Finder không thấy `JAVA_HOME` hay `PATH`),
hiện hộp thoại qua `osascript` khi không có, `cd` tới `~/.gaia-desktop` để thư mục tương đối `logs/` của logback ghi
được, và truyền `-Xdock:name`/`-Xdock:icon`. macOS gắn tiến trình `java` đang chạy với gói ứng dụng (bundle id và biểu
tượng Dock là của ứng dụng); thanh menu hiển thị GAIA Desktop.

Sau đó, với mọi nền tảng, nó nén thư mục thành `target/dist/gaia-desktop-<version>-<platform>.zip` dưới một thư mục cùng
tên, bằng `<zip>` của Ant: `run.sh` và `Contents/MacOS/**` là một tập tệp riêng với `filemode="755"`, nếu không chúng sẽ
không chạy được sau khi giải nén. Tệp zip **không được đính kèm**: nó dành cho các bản phát hành GitHub, và bốn nền tảng
đến từ bốn lần xây dựng, mà Maven Central — mỗi phiên bản một lần tải lên bất biến — không bao giờ có thể nhận như một
bản phát hành.

`project.build.outputTimestamp` được đặt thành thời điểm xây dựng. Nếu không, bộ lưu trữ trong maven-jar-plugin 3.5 và
maven-assembly-plugin 3.8 sẽ ghi ngày 1 tháng 2 năm 1980 cho mọi mục.

Biểu tượng ứng dụng được vẽ bởi `tools/icon/GaiaIcon.java` (không thuộc quá trình xây dựng; phần đầu tệp có các bước
`sips`/`iconutil`). Kết quả của nó được đưa vào kho: `src/main/macos/gaia-desktop.icns` cho gói ứng dụng, và
`src/main/resources/icons/gaia-desktop-{16..256}.png`, mà `GaiaDesktopApp` đặt làm biểu tượng cửa sổ (Windows và
Linux; macOS bỏ qua biểu tượng cửa sổ).

#### Các tập lệnh khởi chạy

`run.sh` và `run.bat` đến từ `src/main/scripts/`, được sao chép vào `dist/gaia-desktop-<version>-<platform>/` mà không lọc
— `run.sh` đầy các phép mở rộng shell `${...}` mà bộ lọc của Maven có thể tùy ý viết lại. Sau đó một bước antrun chỉ
giữ lại tập lệnh mà nền tảng chạy — `run.bat` cho `win`, `run.sh` cho mọi nền tảng khác — và đặt bit thực thi trên
`run.sh`, vì `copy-resources` không giữ nó. Nó quyết định dựa trên `${fx.platform}` đã phân giải thay vì qua các hồ sơ,
vì việc kích hoạt hồ sơ không thấy được giá trị do hồ sơ khác đặt: nếu không, một bản dựng Linux làm trên Windows sẽ
nhận tập lệnh của máy chủ.

Chúng **không dựa vào manifest**. Mỗi tập lệnh tự dựng class path, `gaia-desktop.jar` theo sau là mọi `.jar` trong
`lib/`, liệt kê từng cái thay vì để cho ký tự đại diện `lib/*`, để đường dẫn chứa khoảng trắng vẫn là một mục duy nhất
và số lượng đã thêm là thật. Khởi chạy bằng `-cp` là lý do chúng nêu rõ `tools.pantheum.gaia.desktop.Launcher`: `-cp`
không đọc `Main-Class` của manifest. Nó **vẫn** tuân theo `Class-Path` của manifest — một jar trên `-cp` kéo theo các
jar mà manifest của nó liệt kê, được tìm ngay sau nó — nên mọi jar trong `lib/` đều được nạp dù tập lệnh có liệt kê hay
không, và bỏ một jar khỏi class path của tập lệnh không giữ nó ngoài JVM. Dù vậy các tập lệnh vẫn liệt kê tất cả, để
một jar có manifest cũ, bị sửa tay hoặc thiếu một mục vẫn khởi động được. Cùng một tập lệnh phục vụ cả hai kiểu đóng
gói: jar `-Pfat` không có `lib/` để thêm và cũng không cần, đó cũng là lý do một `lib/` trống chỉ là một ghi chú chứ
không phải lỗi.

#### Thư viện gốc của Zebra

Bản dựng không phát hành bất cứ thứ gì của Zebra — không DLL, không jar dịch vụ. Trên Windows chúng đến từ Scanner SDK
của Zebra được cài trên máy: `run.bat` thêm các jar dịch vụ `Jpos*.jar` và `bin\` của nó vào class path và `bin\` vào
`PATH`, nơi `System.loadLibrary` tìm thấy `CSJPOSScanner.dll` / `CSJPOSScanner64.dll` (`java.library.path` mặc định là
`PATH`). API JavaPOS và trình nạp cấu hình của nó luôn đến từ `lib\javapos-*.jar`, dù có SDK hay không.

**`run.bat` không bao giờ thêm jar JavaPOS riêng của SDK**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`. Trước đây
nó từng thêm, đồng thời bỏ `lib\javapos-*.jar` để tránh hai bản sao của API — nhưng `Class-Path` của manifest vẫn nạp
chúng (xem ở trên), nên JVM nhận cả hai: các lớp trình nạp 4.x từ `lib\`, cùng với `jpos/res/jpos.properties` và
`jcl.xsd` của `javaPOS114.jar` bên cạnh. Tệp thuộc tính khiến [`JposConfig`](#tích-hợp-máy-quét) đứng sang một bên, và
`jcl.xsd` đó chỉ chấp nhận các phiên bản JavaPOS đến 1.10, nên mọi mục trong `jpos.xml` (1.12–1.14) đều không qua
kiểm tra hợp lệ, sổ đăng ký vẫn trống, và mọi lần kết nối đều thất bại với lỗi 109 trước khi chạm tới máy quét. Chỉ
cần jar đó có mặt trên class path là đủ gây ra điều này, kể cả khi nằm sau các jar của `lib\`. Các jar dịch vụ của Zebra
chạy không cần thay đổi trên API 1.14 từ `lib\` — mở, chiếm giữ và quét đã được kiểm chứng với một máy quét thật theo
cách đó. Các DLL đặt thủ công bên cạnh `run.bat` vẫn được tôn trọng: khi đó tập lệnh đặt
`-Djava.library.path=<dist>;%PATH%`, nối thêm `PATH` vì thuộc tính này thay thế hoàn toàn đường dẫn tìm kiếm mặc định.

`mvn javafx:run` không làm bất kỳ điều gì trong số này, nên để chiếm giữ máy quét từ một lần chạy phát triển trên Windows
cần đặt các jar của SDK lên class path và `bin\` của nó lên `PATH` bằng cách khác; để làm vậy, hãy chạy từ `dist/`.

Ngoài class path, chúng đảm nhận những gì `java -jar` không làm: tự phân giải thư mục của mình để hoạt động từ bất kỳ
thư mục làm việc nào, ưu tiên `JAVA_HOME` hơn `PATH`, và từ chối JVM trước 11 bằng một câu nêu cách khắc phục thay vì
`UnsupportedClassVersionError`. `GAIA_DESKTOP_OPTS` truyền các tùy chọn JVM qua. `run.bat` dùng `java.exe`, không phải
`javaw.exe`, nên cửa sổ console vẫn mở — Windows là nền tảng duy nhất mà máy quét chạy, và trạng thái cùng các lỗi
JavaPOS của nó được ghi ra đó.

`run.bat` cũng chọn Java, JavaFX và các dịch vụ Zebra từ những gì nó tìm thấy — một `jdk*\` đi kèm, một
`javafx-sdk*\lib\` trên module path, Scanner SDK của Zebra tại `ZEBRA_HOME` — bỏ các bản sao JavaFX trong `lib\` khỏi
class path khi dùng JavaFX của SDK, và truyền trình phân tích SAX của JDK, proxy và kho tin cậy của Windows, cùng
`--enable-native-access` trên Java 17+. Mục [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows)
của README có bảng chi tiết. Hãy giữ kiểu xuống dòng CRLF của tệp: `cmd.exe` có thể bỏ sót một `call :label` trong tệp
batch chỉ dùng LF. `run.sh` cũng truyền `--enable-native-access=ALL-UNNAMED` — JavaFX nạp mã gốc của chính nó từ
`lib/`, và khi JavaFX nằm trên class path thì `ALL-UNNAMED` là mô-đun bao trùm nó — chỉ bỏ qua, như trong `run.bat`,
khi không đọc được phiên bản Java.

Cả hai bố cục đều mang mã gốc JavaFX **của một nền tảng**, `${fx.platform}`, nên không bố cục nào di chuyển được giữa
các hệ điều hành hay kiến trúc. Hãy xây dựng một lần cho mỗi đích — xem [Mỗi nền tảng một bản dựng](#mỗi-nền-tảng-một-bản-dựng).

> **Eclipse.** Lần thực thi `copy-dependencies` mang một chỉ thị `<?m2e ignore?>`. m2e phân giải một dự án đang mở
> trong workspace thành thư mục `target/classes` của nó thay vì một jar, và `copy-dependencies` từ chối sao chép một
> sản phẩm chưa được đóng gói (MDEP-187), điều sẽ làm hỏng lần tự động xây dựng mỗi khi dự án `gaia` được mở cạnh dự
> án này. Hệ quả là một bản dựng chỉ trong IDE sẽ không lấp đầy `target/dist/gaia-desktop-<version>-<platform>/lib/`; hãy
> chạy `mvn package` trước khi dùng chính tệp jar.

---

## Kiến trúc

| Gói | Chứa |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (cửa sổ), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (logic tạo không phụ thuộc JavaFX), `AiPair` (một hàng trình soạn thảo) |
| `desktop.parse` | `ParseOptions` (`ParseConfig` có thể quan sát), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — bộ kết xuất báo cáo, không phụ thuộc JavaFX và được kiểm thử đơn vị không cần bộ công cụ |
| `desktop.i18n` | `UiStrings` — danh mục nhãn |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### Phễu đầu vào

Mọi đầu vào — gõ, dán, mở từ tệp, quét hoặc tạo — đều đến bộ phân tích qua một phương thức duy nhất,
`MainView.capture(String, ScanSource, String)`.

Đó là quyết định thiết kế duy nhất mà toàn bộ ứng dụng xoay quanh. Một lần tạo không cần báo cáo riêng, lịch sử riêng
hay đường xuất riêng, vì sau phễu, một chuỗi được tạo không thể phân biệt với một chuỗi được quét. Bất cứ thứ gì mới tạo
ra đầu vào đều nên đi qua phễu thay vì đi vòng bên cạnh.

### Luồng

| Luồng | Chạy |
|---|---|
| Luồng ứng dụng JavaFX | Mọi việc phân tích, kết xuất và UI. Phân tích mất dưới một mili giây khi các bảng AI đã được làm nóng, nên không cần luồng phụ |
| `gaia-scanner` | Mọi lời gọi vào máy quét — mở, chiếm giữ, bật, tắt, nhả, đóng. Một luồng sống lâu duy nhất thuộc `ScannerService`; xem [Tích hợp máy quét](#tích-hợp-máy-quét) |
| Luồng sự kiện JavaPOS | Các callback của `ScannerService`. `MainView.FxScannerListener` chuyển từng cái sang bằng `Platform.runLater` |
| Nền | Chờ `connect()` / `disconnect()` của máy quét (việc chiếm giữ có thể mất tới 5 giây), khôi phục lịch sử đã lưu khi khởi động — đồng thời làm nóng bộ phân tích — và kiểm tra cập nhật (`gaia-update-check`, một yêu cầu GitHub, kết quả được gửi lại bằng `Platform.runLater`) |

**Không bao giờ thay đổi lựa chọn của một điều khiển, hay hủy điều khiển đó, từ bên trong sự kiện lựa chọn của chính
nó.** Ba sự cố sập riêng biệt trong ứng dụng này đến từ đúng điều đó: đặt lại bộ chọn AI bên trong `clearAndSelect` của
chính cửa sổ bật lên (một `IndexOutOfBoundsException` từ `ListViewBehavior`), và dựng lại bảng chứa hộp chọn ngôn ngữ
từ bên trong trình xử lý của chính hộp chọn đó. Cả hai giờ đều được hoãn lại bằng `Platform.runLater`, và lý do được
ghi lại ở cả hai chỗ gọi.

### Dựng lại và gắn nhãn lại

Việc đổi ngôn ngữ **dựng lại** khung giao diện từ danh mục — menu, thanh công cụ, bảng tùy chọn — điều này an toàn vì
không phần nào trong đó giữ trạng thái: `OptionsPane` là một khung nhìn hai chiều của `ParseOptions`, nên các giá trị
nằm trong mô hình và một bảng mới lấy lại chúng ngay.

Các điều khiển giữ **dữ liệu người dùng** là các trường, và thay vào đó được gắn nhãn lại tại chỗ:
`BuilderPane.retext(UiStrings)` giữ nguyên các hàng AI, các trường Digital Link và lựa chọn. Dựng lại nó sẽ vứt bỏ công
việc đang làm dở — điều đã từng xảy ra.

---

## Token {GS}

Một `TextField` của JavaFX lặng lẽ loại bỏ mọi ký tự dưới `0x20` (`TextInputControl.filterInput`). Không phải "hiển thị
kỳ lạ" — mà là xóa. Gõ, dán, `setText` và `insertText` đều bị ảnh hưởng, nên `setText("01…" + GS + "10LOT")` cho ra
`"01…10LOT"`: một chuỗi phần tử mà ý nghĩa đã âm thầm thay đổi, vì AI độ dài thay đổi giờ nuốt luôn phần theo sau nó.

`desktop.parse.GsToken` là toàn bộ câu trả lời. Ký tự phân cách đi qua UI dưới dạng văn bản nguyên văn `{GS}` và trở
lại thành `0x1D` trên đường tới bộ phân tích. Token này không phải được phát minh ở đây — nó chính xác là cách
`ASCIIRevealerUtils.printable` của lõi hiển thị `0x1D`, nên một giá trị sao chép từ báo cáo có thể dán thẳng trở lại
thanh nhập.

Một bộ lọc `TextFormatter` trên trường nhập bắt mọi đường mà văn bản có thể đến — gõ, ⌘V, menu ngữ cảnh, kéo và thả,
`setText` — đó là lý do việc viết lại nằm ở đó thay vì ở từng trình xử lý.

Đường của máy quét không bao giờ đi qua `GsToken`: các lần quét đến bộ phân tích dưới dạng byte.

---

## Tích hợp máy quét

`ScannerService` sở hữu điều khiển `Scanner` của JavaPOS: mở → chiếm giữ → bật, kích hoạt lại sự kiện dữ liệu sau mỗi
lần quét. Tên thiết bị logic đến từ `src/main/resources/jpos.xml`.

**Mọi lời gọi thiết bị đều chạy trên một luồng, `gaia-scanner`, sống lâu như dịch vụ** — không bao giờ trên luồng FX,
và không bao giờ trên một luồng kết thúc sau lời gọi. Dịch vụ của Zebra tiếp cận máy quét qua JNI thông qua CoreScanner
COM API, và COM gắn những gì nó thiết lập với apartment của luồng gọi: mở trên một luồng sau đó kết thúc thì máy quét
được chiếm giữ nhưng im lặng; đóng trên một luồng khác thì apartment sai bị hủy khởi tạo. Đóng trên luồng FX là trường
hợp tệ nhất, vì JavaFX gọi `OleInitialize` ở đó và đăng ký đích thả của mọi cửa sổ dựa trên nó — một `CoUninitialize`
trên luồng đó làm hỏng kéo và thả trên toàn ứng dụng. Đây là JavaFX trên Windows, không phải một phiên bản JavaFX cụ
thể nào. `connect()` và `disconnect()` gửi việc tới luồng đó rồi chờ, nên `MainView` gọi cả hai từ một luồng nền ngắn
hạn.

Hai quy tắc giữ cho một thiết bị trục trặc không làm kẹt kết nối. **Quá trình tháo dỡ không bao giờ dừng sớm:**
`closeQuietly` tắt, nhả và đóng thành các bước riêng biệt, chỉ nhả khi đang giữ quyền chiếm giữ, và luôn thử `close()`
— sau một lần chiếm giữ thất bại, `release()` ném ra "not claimed", và khi các bước dùng chung một `try` thì điều đó bỏ
qua bước đóng và để thiết bị mở trong dịch vụ của Zebra. **Sự kiện dữ liệu được kích hoạt lại trong một `finally`:**
JavaPOS tắt nó sau mỗi sự kiện, nên một lần đọc hay trình xử lý ném ngoại lệ vẫn phải kích hoạt lại, nếu không máy quét
sẽ im lặng cho đến khi kết nối lại. `ScannerLifecycleTest` kiểm tra cả hai với một `jpos.Scanner` được lập kịch bản.

**`JposConfig` trỏ JavaPOS tới `jpos.xml` đi kèm** trước lần mở đầu tiên. Với các jar JavaPOS từ Maven
(`javapos-config-loader` 4.x) và không có `jpos/res/jpos.properties` trên class path, nếu không thì mọi lần mở đều thất
bại với *"Service does not exist in loaded JCL registry"* (lỗi 109), vì ba lý do riêng biệt: trình quản lý dịch vụ lùi
về `SimpleRegPopulator`, vốn đọc một `jpos.cfg` tuần tự hóa và không bao giờ đọc `jpos.xml`; trình nạp XML chỉ tìm
`jpos.xml` trong thư mục làm việc, không tìm bên trong jar; và Xerces 2.12.2 trong `lib/` từ chối thuộc tính JAXP
`accessExternalDTD` mà trình nạp đặt. `JposConfig` đặt `jpos.config.regPopulatorClass` (`JavaxRegPopulator`),
`jpos.config.populatorFileURL` (`jar:file:…!/jpos.xml` của chính jar) và `javax.xml.parsers.SAXParserFactory` (của
JDK) làm thuộc tính hệ thống — không bao giờ đè lên cái đã được đặt, nên `-Djpos.config.populatorFileURL=file:…` vẫn
chọn được một `jpos.xml` khác — và không đặt gì khi có một `jpos.properties` trên class path hoặc không có trình nạp
4.x. `javaPOS114.jar` của Zebra SDK là một trường hợp như vậy và làm hỏng hoàn toàn sổ đăng ký, đó là lý do `run.bat`
giữ nó ngoài class path — xem [Thư viện gốc của Zebra](#thư-viện-gốc-của-zebra). `JposConfigTest` kiểm tra đầu cuối rằng
sau đó sổ đăng ký chứa các máy quét trong tệp.

**Bộ chọn thiết bị được sinh ra từ sổ đăng ký đó**, không được giữ như một danh sách trong mã:
`ScannerService.logicalNames()` đọc các mục đã nạp trên luồng `gaia-scanner` và giữ những mục có loại thiết bị là
`Scanner` — hai mục cân trong `jpos.xml` bị loại ra — theo `pickerOrder`: `ZebraAllScanners` đứng đầu khi tệp định
nghĩa nó, phần còn lại theo thứ tự chữ cái (sổ đăng ký không giữ thứ tự trong tệp). Tệp không có nó thì không được thêm
vào; khi không nạp được gì thì chỉ đưa ra mặc định, nên bộ chọn không bao giờ trống. `MainView` hiển thị mặc định ngay
và điền danh sách từ một luồng nền, giữ nguyên lựa chọn đã lưu. Vì vậy một máy quét được thêm vào `jpos.xml` — hoặc cả
một tệp được chỉ định bằng `-Djpos.config.populatorFileURL` — xuất hiện trong bộ chọn mà không cần thay đổi mã, và
`JposConfigTest` đối chiếu bộ chọn với một lần đọc độc lập tệp đi kèm. Các dịch vụ mà những mục đó chỉ tới là các lớp
của Zebra (`com.zebra.jpos.service.scanner.*Factory`), vẫn phải có trên class path — từ Scanner SDK qua `run.bat`, hoặc
các jar dịch vụ của Zebra thả vào `lib\`.

Dữ liệu quét được giải mã theo **ISO-8859-1** để mỗi byte ánh xạ 1:1 và cả tiền tố AIM Code ID lẫn mọi `0x1D` đều còn
nguyên; báo cáo được ghi dưới dạng UTF-8.

Không có gì trong dịch vụ soạn văn bản cho người dùng. Các sự kiện được phát ra dưới dạng hằng `ScannerListener.Event`
cùng các giá trị thay đổi, và `MainView` cung cấp câu chữ từ danh mục — nếu không thì dòng trạng thái sẽ là tiếng Anh
trong một ứng dụng được dịch ra 35 thứ tiếng. Các dòng nhật ký là ngoại lệ và giữ nguyên tiếng Anh.

### Cổng nền tảng

`ScannerService.isSupportedPlatform()` là một phép thử `os.name`, và `MainView` hỏi nó trước khi dựng bất kỳ điều khiển
máy quét nào: trên macOS và Linux không có công tắc, không có bộ chọn thiết bị, không có menu Máy quét (và vì thế không
có <kbd>⌘K</kbd>), và không có nhãn trạng thái. Chúng không bị ẩn — chúng không bao giờ được tạo ra.

Zebra chỉ phát hành các dịch vụ JavaPOS cho Windows, và ở nơi khác không có gì thất bại sớm: các jar phân giải được và
điều khiển được tạo, rồi chính `open()` cuối cùng thất bại vì thiếu lớp mã gốc, sau vài giây và một hộp thoại.

Quy tắc là về *các dịch vụ*, không phải về JavaPOS. Một nhà cung cấp phát hành dịch vụ đa nền tảng sẽ hoạt động ở đây,
và mở rộng quy tắc chỉ là thay đổi `supportsJavaPos(String)` — phương thức package-private và được kiểm thử với các
chuỗi `os.name` để cả hai câu trả lời đều được bao phủ trên bất kỳ máy nào chạy bản dựng.

### Symbology

`Symbologies` ánh xạ toàn bộ **78** kiểu dữ liệu quét `SCAN_SDT_*` của JavaPOS sang tên hiển thị, và sang
`DataCarrierType` của lõi khi hai bộ từ vựng khớp nhau. JavaPOS là bên chi tiết hơn — nó tách UPC-A khỏi EAN-13, và
Data Matrix thường khỏi GS1 DataMatrix, trong khi AIM gộp cả một họ dưới một ký tự mã.

Một kiểu nằm ngoài bảng vẫn có một tên dùng được, tìm bằng phản chiếu trên `ScannerConst`, vì các dịch vụ của nhà cung
cấp thực sự báo các giá trị ngoài danh sách đặc tả. Một bài kiểm thử duyệt `ScannerConst` và thất bại nếu có kiểu đã
khai báo nào không có tên — điều này cũng bắt được các hằng mà một JavaPOS tương lai thêm vào.

Một số ánh xạ vật mang được cố ý để `UNKNOWN` thay vì đoán: straight 2 of 5 (AIM tách `]R`/`]S` theo số vạch bắt
đầu/kết thúc, điều mà JavaPOS không báo), các mã bưu chính bốn trạng thái, và TLC-39.

---

## Bản địa hóa

Nhãn nằm trong một tệp properties cho mỗi ngôn ngữ dưới `src/main/resources/i18n/ui_<CODE>.properties`, dùng cùng mã
với các danh mục lỗi của lõi (`EN`, `FR`, `PCM`, …). Cả 35 tệp đều có và cả 35 đều mang cùng một tập khóa. Các tệp được
đọc dưới dạng **UTF-8**, không phải ISO-8859-1 mà `Properties` giả định, nên bản dịch được lưu ở dạng đọc được thay vì
các chuỗi thoát `\uXXXX`.

**Dự phòng theo từng khóa, không theo từng tệp** — điều giúp một ngôn ngữ mới dịch một phần trở nên hữu ích ngay khi
khóa đầu tiên của nó có mặt. Tiếng Anh là hợp đồng: mọi khóa đều phải có ở đó.

| Tầng | Số khóa | Phạm vi |
|---|---|---|
| Từ vựng báo cáo dùng chung | 31 | cả 35 ngôn ngữ |
| Khung máy tính để bàn — menu, nút, trình tạo, tùy chọn, dòng trạng thái, bố cục báo cáo, trạng thái máy quét, nội dung hộp thoại | 114 | cả 35 ngôn ngữ |

Sáu thuật ngữ dùng chung (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) không được báo cáo
của ứng dụng này sử dụng.
Chúng được giữ lại có chủ ý; phần đầu tệp có nói như vậy.

### Đối số `--language`

`GaiaDesktopApp.languageOverride()` đọc `--language=…` qua `getParameters().getNamed()` của chính JavaFX, phân giải nó
bằng `Languages.fromArgument` — mã danh mục, tên enum hoặc thẻ BCP 47, không phân biệt hoa thường — và chuyển cho
`MainView`.

**Việc ghi đè dành cho lần chạy, không dành cho tệp.** `MainView` giữ ngôn ngữ nó đã nạp trong `savedLanguage` và ghi
lại *ngôn ngữ đó*, cho đến khi `languageChosenInSession` cho biết người dùng đã tự chọn một ngôn ngữ; khi đó lựa chọn
của họ được lưu như bình thường. Sự tách biệt đó là toàn bộ thiết kế: một hồ sơ kiosk hay một lối tắt demo có thể mở
bằng tiếng Tây Ban Nha mà không ghi đè ngôn ngữ chủ máy sẽ mở vào ngày mai, trong khi một lựa chọn thật sự trong bảng
tùy chọn vẫn được giữ.

Một giá trị không trỏ tới danh mục nào sẽ được ghi nhật ký cùng các mã được chấp nhận và bị bỏ qua. Từ chối khởi động
chỉ vì một lỗi gõ trong lối tắt sẽ là thất bại tệ hơn, và `Languages.fromArgument` trả về `null` thay vì ném ngoại lệ
cũng vì lý do đó.

### Thêm một ngôn ngữ

Không cần thay đổi mã.

1. Sửa `ui_<CODE>.properties` — mọi ngôn ngữ đều đã có tệp.
2. Dịch các giá trị. Giữ nguyên các chỗ giữ `{0}` / `{1}`.
3. Xây dựng. Hai bài kiểm thử thực thi hợp đồng: một bài thất bại nếu mã yêu cầu một khóa mà tiếng Anh không định
   nghĩa, bài kia thất bại nếu tập khóa của bất kỳ danh mục nào lệch khỏi tiếng Anh.

Bài kiểm thử đầu tiên quét **mã nguồn** để tìm các literal khóa thay vì phản chiếu trên các lớp, vì khóa chỉ tồn tại dưới
dạng literal chuỗi tại các chỗ gọi. Đó là lý do một thông báo mới phải được dựng với khóa viết trực tiếp —
`strings.format("st.scannerError", …)` — thay vì tra cứu qua một biến, nếu không bài kiểm thử không thấy được nó.

### Những gì cố ý không dịch

- các tên trường ngắn gọn hiển thị bằng phông đơn cách bên cạnh giá trị thô — `format`, `position`, `length`, `raw` và
  `part` của thẻ phần tử, `AI`, `sep`, `pos` và `len` của băng, và bốn ô định vị lỗi (`AI`, `position`, `level`,
  `stage`), mà giá trị của chúng vốn là tên enum không dịch;
- tên các đầu vào mẫu, vốn gọi tên các cấu trúc GS1, và tên symbology, vốn là danh từ riêng;
- đoạn khắc phục của `GE-C084` — văn bản tiêu chuẩn mà một bản dịch nửa vời còn tệ hơn tiếng Anh;
- chốt chặn cuối cho AI không xác định của `ElementBuilder`, không thể chạm tới từ UI: trình soạn thảo tự kiểm tra AI
  không xác định và báo chúng từ danh mục;
- các dòng nhật ký.

Còn một khoảng trống: `ScanSource.label()` cung cấp `manual` / `scanner` / `file` / `built` cho hàng lịch sử và tiêu đề
báo cáo bằng tiếng Anh. Lấp nó cần nhiều hơn một khóa — `HistoryCell` không có `UiStrings`, và nhà máy ô của danh sách
chỉ được cài một lần, nên các ô cũng sẽ phải được làm mới khi đổi ngôn ngữ.

---

## Định dạng trạng thái lưu trữ

Hai tệp dưới `~/.gaia-desktop/`, tách riêng vì chúng thay đổi theo nhịp khác nhau — một nhật ký thu nhận bị hỏng hoặc
bị sửa tay không nên làm người dùng mất cài đặt. Ghi đè thư mục bằng `-Dgaia.desktop.home=…`, đó cũng là cách các bài
kiểm thử tránh xa thư mục nhà thật.

**`options.properties`** — được ghi khi một tùy chọn thay đổi, và khi thoát.

| Khóa | Ý nghĩa |
|---|---|
| `parse.mode`, `parse.language` | Độ sâu đường ống và ngôn ngữ thông báo |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Cách hiển thị ngày |
| `check.skipRequires`, `check.skipExcludes` | Bỏ qua kiểm tra cấu trúc |
| `ui.theme` | `LIGHT` hoặc `DARK` |
| `ui.optionsPane` | Bảng tùy chọn có mở không? |
| `ui.scannerDevice` | Tên logic JavaPOS gần nhất |
| `ui.mode` | `PARSE`, `BUILD` hoặc `COMBINE` |
| `update.check` | Có chạy kiểm tra cập nhật hằng ngày không (mặc định `true`) |
| `update.lastCheck` | Lần chạy gần nhất, ISO-8601; không có hoặc không đọc được nghĩa là đến hạn kiểm tra |
| `update.skipVersion` | Bản phát hành mà người dùng đã chọn *Bỏ qua phiên bản này*; không tự động đề xuất lại |

Tệp bị thiếu hoặc hỏng không bao giờ gây lỗi nghiêm trọng: mỗi giá trị dự phòng riêng lẻ, nên một dòng hỏng chỉ mất
dòng đó và không gì khác. Ở lần khởi chạy đầu tiên, khi chưa có tệp nào, các tùy chọn được khởi tạo từ cài đặt vùng
của máy bởi `SystemDefaults` thay vì từ mặc định dựng sẵn của GAIA — mẫu ngày ngắn của locale cho biết thứ tự các thành
phần, ký tự phân cách, và tháng với năm có được viết tắt hay không.

Không bao giờ đưa null cho `Properties.setProperty`: nó ném ngoại lệ, và `persistOptions()` chạy từ các trình xử lý sự
kiện UI, nơi ngoại lệ thoát ra luồng FX. Đó là một sự cố sập thật mà ứng dụng này đã từng gặp.

**`captures.tsv`** — được ghi khi lịch sử thay đổi, và khi thoát. Mới nhất trước, tối đa **500** mục.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Chỉ những gì không thể tính lại mới được lưu. `ParseResult` **không** được tuần tự hóa — nó được suy ra lại bằng cách
phân tích lại đầu vào khi nạp, nên lịch sử được khôi phục phản ánh các tùy chọn có hiệu lực *hiện tại*. Đầu vào thường
xuyên chứa FNC1 và bất cứ thứ gì khác mà máy quét phát ra, nên trường đầu vào được thoát ký tự (`\\`, `\t`, `\n`, `\r`,
và `\xNN` cho mọi ký tự khác dưới `0x20`) và tệp được đọc và ghi dưới dạng ISO-8859-1, từng byte một.

Các tệp được ghi trước khi ngày được lưu lại chỉ có giờ; chúng vẫn nạp được, mang ngày hôm nay, vì ngày thật chưa bao
giờ được ghi xuống.

---

## Kiểm thử

```bash
(cd gaia-desktop && mvn test)
```

198 bài kiểm thử, cố ý **không phụ thuộc bộ công cụ JavaFX**, giúp chúng nhanh và chạy không giao diện. Chúng bao phủ bộ
kết xuất báo cáo, logic tạo và tra cứu AI, chuyển đổi `{GS}`, các mặc định suy ra từ locale, cả hai kho lưu trữ (gồm
một vòng khứ hồi chứa `0x1D` và các tệp cố ý làm hỏng), bảng symbology, quy tắc nền tảng của máy quét, phần giữ chỗ
phiên bản, và các danh mục.

Ba bài đáng biết vì chúng bảo vệ hợp đồng chứ không phải chức năng:

| Bài kiểm thử | Thất bại khi |
|---|---|
| `CatalogueCoverageTest` | Mã yêu cầu một khóa mà `ui_EN.properties` không định nghĩa, hoặc bất kỳ tệp nào trong 35 tệp lệch khỏi tập khóa tiếng Anh |
| `SymbologiesTest` | Bất kỳ hằng `SCAN_SDT_*` nào JavaPOS khai báo không có tên hiển thị — kể cả hằng do một bản nâng cấp JavaPOS thêm vào |
| `ScannerServiceTest` | Quy tắc nền tảng đổi câu trả lời cho Windows hoặc cho bất kỳ hệ nào khác |

`UiStrings` cố ý dễ dãi khi chạy — một khóa không xác định được hiển thị chính là khóa đó thay vì ném ngoại lệ giữa chừng
bố cục — đó chính xác là lý do bài kiểm thử danh mục phải tồn tại.

**Chưa bao phủ:** bất cứ thứ gì cần bộ công cụ đang chạy — hành vi tiêu điểm, công tắc chế độ, chỉnh sửa bảng, đổi ngôn
ngữ, và cả hai sự cố sập mô tả trong [Luồng](#luồng). Những điều đó đã được kiểm chứng bằng cách thao tác cửa sổ thật,
nhưng một lỗi hồi quy ở đó sẽ không làm bản dựng thất bại. Thêm TestFX hoặc Monocle sẽ lấp khoảng trống đó.

---

## Khắc phục sự cố khi xây dựng

| Triệu chứng | Nguyên nhân và cách sửa |
|---|---|
| `package tools.pantheum.gaia does not exist` | Lõi chưa được cài. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | `Main-Class` của jar phải là `Launcher`, không phải `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* khi khởi động | Được dự kiến và vô hại — JavaFX ghi nhận rằng nó nằm trên class path. Đó chính là lý do `Launcher` tồn tại |
| Jar không khởi động, `NoClassDefFoundError` | Jar mặc định chỉ chứa các lớp của mô-đun này; `dist/lib/` phải nằm cạnh nó. Dùng `-Pfat` để có một tệp tự chứa duy nhất |
| `Artifact has not been packaged yet … MDEP-187` trong Eclipse | Phân giải workspace của m2e. Xem ghi chú trong [Đóng gói](#đóng-gói) |
| Các tệp `… 2.properties` trùng lặp xuất hiện trong bản dựng | Dự án nằm trong thư mục đồng bộ iCloud và việc đồng bộ đã tạo bản sao xung đột giữa lúc xây dựng. Luôn dùng `mvn clean package`; tốt hơn nữa, loại `target/` khỏi đồng bộ |
| Một mục menu hiển thị một khóa như `act.hri` | Khóa đó thiếu trong `ui_EN.properties`. Lẽ ra bản dựng đã phải bắt được — xem [Kiểm thử](#kiểm-thử) |

---

## Các hạn chế đã biết và việc còn mở

- **Không có kiểm thử UI không giao diện.** Xem [Kiểm thử](#kiểm-thử).
- **Sản phẩm riêng theo nền tảng.** Cả hai kiểu đóng gói đều kèm mã gốc JavaFX của một nền tảng; hãy xây dựng một bản cho
  mỗi đích bằng `-Dfx.platform` (bất kỳ máy nào cũng xây dựng được tất cả). Không kiểu nào kèm môi trường chạy Java —
  điều đó cần `jpackage`, vốn chỉ xây dựng cho hệ điều hành mà nó đang chạy.
- **Đầu vào máy quét chỉ có trên Windows.** Xem [Cổng nền tảng](#cổng-nền-tảng).
- **`ScanSource.label()` chưa được bản địa hóa.** Xem [Những gì cố ý không dịch](#những-gì-cố-ý-không-dịch).
- **`UiState.mode` là một `String`** (`"PARSE"` / `"BUILD"`) trong khi `Theme` bên cạnh là một enum, nên literal đó
  xuất hiện ở nhiều chỗ. Một enum sẽ loại bỏ chúng.
- **Đích Digital Link chỉ gồm tên miền và tiền tố đường dẫn.** `BuilderDigitalLinkConfig` còn hỗ trợ tham số truy vấn bổ
  sung và một fragment; trình soạn thảo không cung cấp chúng.
- **Các cặp AI loại trừ lẫn nhau (`GE-S006`) không được đánh dấu trong trình soạn thảo** như AI không xác định và AI
  trùng lặp — việc tạo cứ thế thất bại.
- **Danh mục lỗi của lõi nhân đôi dấu nháy** (`value ''09506000134350''`), điều này xuất hiện nguyên văn trong các lỗi
  tạo và trong báo cáo. Nó trông giống cơ chế thoát dấu nháy của `MessageFormat` không được hoàn tác; việc sửa thuộc về
  lõi.
