# Zebra tarayıcı kurulumu — Windows için GAIA Desktop

GAIA Desktop'un GS1 barkodlarını doğrudan okuyabilmesi için bir Zebra barkod tarayıcısının nasıl
kurulup yapılandırılacağı: her tarama ayrıştırılır ve geçmişe eklenir; AIM Code ID öneki ve her
FNC1 ayırıcısı olduğu gibi korunur. Özgün belge:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

GAIA Desktop, USB ile bağlanan bir **Zebra DS8108** el tipi görüntüleyici ile geliştirildi ve
test edildi. Adımlar, Zebra Scanner SDK'nın desteklediği her Zebra 2D tarayıcı için geçerlidir;
belirli bir modelin ayar barkodları ve menü adları o modelin kendi Quick Reference Guide veya
Product Reference Guide belgesinde yer alır.

> **Yalnızca Windows.** Zebra tarayıcı sürücüleri yalnızca Windows için vardır; bu yüzden macOS
> ve Linux'ta GAIA Desktop hiçbir tarayıcı denetimi oluşturmaz. Uygulamanın geri kalanı her
> platformda çalışır.

---

## İçindekiler

1. [Neler gerekir](#1-neler-gerekir)
2. [Java'yı kurma](#2-javayı-kurma)
3. [Zebra Scanner SDK'yı kurma](#3-zebra-scanner-sdkyı-kurma)
4. [123Scan'i kurma](#4-123scani-kurma)
5. [Zebra tarayıcının ürün yazılımını güncelleme](#5-zebra-tarayıcının-ürün-yazılımını-güncelleme)
6. [Zebra tarayıcının USB modunu ayarlama](#6-zebra-tarayıcının-usb-modunu-ayarlama)
7. [Zebra tarayıcıyı GS1 için yapılandırma](#7-zebra-tarayıcıyı-gs1-için-yapılandırma)
8. [GAIA Desktop'u kurma ve başlatma](#8-gaia-desktopu-kurma-ve-başlatma)
9. [Tarayıcıyı bağlama](#9-tarayıcıyı-bağlama)
10. [Yapılandırma başvurusu](#10-yapılandırma-başvurusu)
11. [Sorun giderme](#11-sorun-giderme)
12. [Desteklenen Zebra 2D tarayıcılar](#12-desteklenen-zebra-2d-tarayıcılar)

---

## 1. Neler gerekir

| | |
|---|---|
| **Tarayıcı** | USB kablosuyla bir Zebra 2D tarayıcı — bkz. [Desteklenen Zebra 2D tarayıcılar](#12-desteklenen-zebra-2d-tarayıcılar) |
| **Bilgisayar** | Windows 10 veya 11, 64 bit |
| **Java** | Java 21 veya daha yenisi, **64 bit** |
| **Windows için Zebra Scanner SDK** | **64 bit**, Zebra'nın destek sitesinden — GAIA Desktop'un kullandığı sürücüyü ve JavaPOS hizmetlerini sağlar |
| **Zebra 123Scan** | Zebra'nın ücretsiz yapılandırma aracı, destek sitesinden — tarayıcının ürün yazılımını güncellemek ve ayarlarını denetlemek için kullanılır |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **İsteğe bağlı** | Ayar barkodlarını içeren, tarayıcının **Product Reference Guide** (PRG) belgesi |

Parçaların birbirine nasıl bağlandığı:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop, JavaPOS API'sinin kendisini ve tarayıcıyı adlandıran yapılandırmayı
(`gaia-desktop.jar` içindeki `jpos.xml`) kendisi getirir. Gerisini Zebra'nın SDK'sı sağlar:
tarayıcıyla konuşan sürücü ve JavaPOS ile bu sürücü arasında duran Zebra hizmet sınıfları.

---

## 2. Java'yı kurma

GAIA Desktop **Java 21 veya daha yenisini** gerektirir ve 64 bit Zebra sürücüleriyle uyumlu
olması için **64 bit** sürüm olmalıdır.

1. Windows x64 için bir Java 21 JDK'sı veya JRE'si kurun — örneğin adoptium.net'ten Eclipse
   Temurin. Kurulum programı `JAVA_HOME` değişkenini ayarlamayı önerirse izin verin.
2. Bir Komut İstemi'nde denetleyin:

   ```
   java -version
   ```

   İlk satır 21 veya sonraki bir sürümü göstermelidir; 64 bit bir Java ayrıca
   `64-Bit Server VM` yazar.

`run.bat` Java'yı şu sırayla arar: `run.bat` yanındaki bir `jdk*` klasörü, sonra `JAVA_HOME`,
sonra `PATH` üzerindeki `java`. 21'den eski her şeyi bunu belirten bir iletiyle reddeder.

---

## 3. Zebra Scanner SDK'yı kurma

1. Zebra'nın destek sitesinden **Windows için Zebra Scanner SDK'yı (64 bit)** indirin.
2. Kurulum programını çalıştırın. Bileşen seçimi sunuyorsa **JPOS** (JavaPOS) bileşeninin dahil
   olduğundan emin olun.
3. Varsayılan kurulum konumunu koruyun. GAIA Desktop, SDK'nın JavaPOS bölümünü şurada arar:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   ve Zebra'nın hizmet JAR'larının (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …) ve
   yerel kitaplıkların (`CSJPOSScanner64.dll`, …) bunun `bin\` klasöründe olmasını bekler. SDK
   başka bir yere kuruluysa bkz. [ZEBRA_HOME](#zebra_home).
4. Kurulumdan sonra **Windows'u yeniden başlatın**. SDK'nın CoreScanner sürücüsü arka planda
   çalışır ve GAIA Desktop tarayıcıya ulaşmadan önce çalışıyor olmalıdır; yeniden başlatmak bundan
   emin olmanın en kolay yoludur.

---

## 4. 123Scan'i kurma

123Scan, Zebra'nın kendi tarayıcılarını yapılandırmaya yarayan ücretsiz aracıdır. Bir sonraki
adımda tarayıcının ürün yazılımını güncellemek için kullanılır; sonrasında da tarayıcının
ayarlarını denetleyip değiştirmenin en kolay yoludur.

1. Zebra'nın destek sitesinden **123Scan**'i indirin ve kurulum programını çalıştırın.
2. Zebra tarayıcıyı takın ve 123Scan'i başlatın. Tarayıcıyı bulmalı ve modelini, seri numarasını
   ve geçerli ürün yazılımı sürümünü göstermelidir. Göstermiyorsa devam etmeden önce başka bir USB
   bağlantı noktası veya kablo deneyin — 123Scan'in göremediği bir tarayıcıyı GAIA Desktop da
   göremez.

---

## 5. Zebra tarayıcının ürün yazılımını güncelleme

Yapılandırmadan önce tarayıcının ürün yazılımını en son sürüme getirin. Ürün yazılımı
güncellemeleri kod çözme sorunlarını giderir ve tarayıcıyı SDK'nın kurduğu Zebra sürücüsüyle
uyumlu tutar.

1. **GAIA Desktop**'u ve tarayıcıyı kullanıyor olabilecek diğer tüm programları kapatın:
   güncelleme sırasında 123Scan'in tarayıcıyı tek başına kullanması gerekir.
2. Tarayıcı bağlıyken 123Scan'de ürün yazılımı güncelleme seçeneğini seçin. 123Scan tarayıcının
   ürün yazılımını Zebra'nın yayımladığı en son sürümle karşılaştırır ve indirip kurmayı önerir.
   Bilgisayarın internet erişimi yoksa ürün yazılımını Zebra'nın sitesindeki tarayıcı destek
   sayfasından indirip dosyayı 123Scan'e yükleyin.
3. Güncellemeyi başlatın ve **123Scan bittiğini bildirene kadar tarayıcıyı takılı bırakın**.
   Birkaç dakika sürer; sonunda tarayıcı yeniden başlar. Yarıda çıkarmak tarayıcıyı kullanılamaz
   hâle getirebilir.
4. 123Scan'in artık yeni ürün yazılımı sürümünü gösterdiğini denetleyin.

Bir güncelleme bazı ayarları varsayılanlarına döndürebilir; bu yüzden tarayıcı, ürün yazılımından
sonra, sonraki iki adımda yapılandırılır.

---

## 6. Zebra tarayıcının USB modunu ayarlama

En sık atlanan adım budur. **Fabrika çıkışında bir Zebra tarayıcı bir USB klavyedir** (USB HID
Keyboard modu): her barkodu odağın olduğu yere "yazar". Bu modda GAIA Desktop'taki
*Tarayıcıyı bağla* komutu tarayıcıyı bulamaz ve bir metin alanına yazılan her şey FNC1
ayırıcılarını kaybeder; bu da bir GS1 eleman dizisinin anlamını değiştirir.

Tarayıcıyı, Zebra sürücüsünün doğrudan konuştuğu bir moda geçirin:

| Tarayıcının USB modu | GAIA Desktop'taki cihaz adı | Notlar |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld` veya `ZebraAllScanners` | Önerilir |
| SNAPI (görüntülemeli veya görüntülemesiz) | `ZebraScannerSNAPI` veya `ZebraAllScanners` | Zebra'nın kendi USB protokolü; o da çalışır |
| IBM Table-top USB | `ZebraUSBTableTop` veya `ZebraAllScanners` | Masaüstü tarayıcılar için |
| USB OPOS (Hand-held) | `ZebraUSBOPOS` veya `ZebraAllScanners` | |
| USB HID Keyboard (fabrika ayarı) | — | **Bağlanmaz** |

GAIA Desktop'un varsayılan cihaz adı olan `ZebraAllScanners`, desteklenen modların herhangi
birindeki tarayıcıyı kabul eder; bu yüzden mod ayarlandıktan sonra varsayılan genellikle olduğu
gibi çalışır.

Modu değiştirmek için ikisinden birini yapın:

- tarayıcının Quick Reference Guide veya Product Reference Guide belgesindeki (USB Host Types /
  USB Device Type bölümü) mod için — örneğin *IBM Hand-held USB* — **ayar barkodunu tarayın**;
  tarayıcı onaylamak için bip sesi çıkarır; ya da
- **123Scan'i kullanın** ([4. adım](#4-123scani-kurma)): tarayıcıyı bağlayın, yapılandırmasını
  açın ve USB ana bilgisayar türünü ayarlayın.

Mod değiştikten sonra Windows tarayıcıyı farklı türde bir cihaz olarak yeniden algılar; bağlanmadan
önce birkaç saniye bekleyin.

Tarayıcıyı daha sonra — başka yazılımlarla kullanmak için — klavye moduna döndürmek isterseniz
*USB Keyboard (HID)* ayar barkodunu tarayın veya fabrika ayarlarını geri yükleyin.

---

## 7. Zebra tarayıcıyı GS1 için yapılandırma

GAIA Desktop, tarayıcı hangi tür barkodu okuduğunu söylediğinde en iyi şekilde ayrıştırır.

### AIM Code ID'yi gönderme

Product Reference Guide'daki (User Preferences) ayar barkoduyla veya 123Scan'de
**Transmit Code ID Character** değerini **AIM Code ID Character** olarak ayarlayın. Böylece her
tarama semboloji tanımlayıcısıyla başlar — GS1-128 için `]C1`, GS1 DataMatrix için `]d2`,
GS1 QR Code için `]Q3`, GS1 DataBar için `]e0` — GAIA Desktop bunu veri taşıyıcısını adlandırmak
ve onun kurallarını uygulamak için kullanır.

Bu olmadan da GAIA Desktop eleman dizisini ayrıştırır ve sembolojiyi taramanın kendisinden alır;
ancak raporda gösterilecek bir AIM Code ID olmaz ve EAN-13 gibi sıradan bir barkodu bir GS1 eleman
dizisinden ayırt etmek zorlaşır.

### Sembolojileri denetleme

Çalıştığınız barkod türlerinin **etkin** olduğundan emin olun: genellikle GS1-128,
GS1 DataMatrix, GS1 DataBar, GS1 QR Code ve EAN/UPC. Zebra'nın 2D tarayıcılarında çoğu varsayılan
olarak açıktır; 123Scan geçerli ayarları gösterir ve Product Reference Guide her birini etkinleştiren
bir barkod içerir.

### Zebra'nın GS1 ayrıştırma özelliklerini kapalı bırakma

Yeni ürün yazılımları tarayıcının kendisinde çalışan GS1 özellikleri ekler — **Label Parse+**,
**GS1 Digital Link** ve **GS1 Retail POS parsing**. Tarayıcı nasıl geldiyse öyle, **kapalı**
bırakın: tarayıcının gönderdiğini yeniden biçimlendirir veya kısaltırlar; GAIA Desktop ise
ayrıştırmak ve doğrulamak için ham veriye ihtiyaç duyar. Her birinin ne yaptığı için bkz.
[Desteklenen Zebra 2D tarayıcılar](#12-desteklenen-zebra-2d-tarayıcılar).

### Önek ve sonek eklememe

Tarayıcının çıktısına önek, sonek veya Enter tuşu eklemeyin. GAIA Desktop her taramayı JavaPOS
üzerinden eksiksiz bir ileti olarak alır ve bunların hiçbirine ihtiyaç duymaz; fazladan karakterler
eleman dizisine karışır.

---

## 8. GAIA Desktop'u kurma ve başlatma

1. `gaia-desktop-1.0.0-win.zip` dosyasını örneğin `C:\Tools\` içine açın. Tek bir klasör içerir:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Tarayıcıyı takın.
3. **`run.bat`** dosyasına çift tıklayın. Uygulamanın yanında bir konsol penceresi açılır ve açık
   kalır: tarayıcı durumu ve JavaPOS hataları orada bildirilir.
4. Konsoldaki ilk satırı okuyun. `run.bat`'in ne bulduğunu söyler:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Satır **`Zebra services from none (Zebra Scanner SDK not installed)`** ile bitiyorsa SDK
   varsayılan konumunda bulunamamıştır — kurun ([3. adım](#3-zebra-scanner-sdkyı-kurma)) veya
   [ZEBRA_HOME](#zebra_home) değişkenini ayarlayın. Uygulama yine de başlar, ancak tarayıcı
   bağlanamaz.

---

## 9. Tarayıcıyı bağlama

1. Araç çubuğundaki **cihaz seçici**, GAIA Desktop'un bildiği tarayıcı adlarını listeler.
   **`ZebraAllScanners`** üzerinde bırakın veya [6. adım](#6-zebra-tarayıcının-usb-modunu-ayarlama)
   tablosundan tarayıcının USB moduna uygun adı seçin.
2. **Tarayıcıyı bağla** düğmesine tıklayın veya **Ctrl+K** tuşlarına basın (**Tarayıcı**
   menüsünde de vardır).
3. Durum çubuğu **`tarayıcı: ZebraAllScanners`** (veya seçtiğiniz ad) olarak değişir ve
   durum satırı *Tarayıcı bağlandı* iletisini gösterir. Düğme artık
   **Tarayıcı bağlantısını kes** olarak görünür.
4. Bir GS1 barkodu tarayın. Yakalama geçmişinin en üstünde görünür ve raporu açılır. AIM Code ID
   açıkken rapor veri taşıyıcısını adlandırır — *GS1-128*, *GS1 DataMatrix* — ve eleman dizisi her
   FNC1 ayırıcısını korur.

Cihaz seçici seçiminizi oturumlar arasında hatırlar. Tarayıcı, bağlantısını kesene veya uygulamayı
kapatana kadar bağlı kalır. Taramalar **Oluştur** ve **Birleştir** modlarında da
çalışır: Birleştir modunda her tarama birleştirilen barkodlardan biri olur.

GAIA Desktop tarayıcıyı tuttuğu sürece başka programlar onu kullanamaz; tersi de geçerlidir:
bağlanmadan önce 123Scan'i ve diğer tüm satış noktası (POS) veya tarayıcı yazılımlarını kapatın.

---

## 10. Yapılandırma başvurusu

### Cihaz adları

Cihaz seçici, `gaia-desktop.jar` içinde gelen `jpos.xml` dosyasından doldurulur. İçindeki tarayıcı
girdileri:

| Cihaz adı | Tarayıcı türü | Ne ile kullanılır |
|---|---|---|
| `ZebraAllScanners` | herhangi | **Varsayılan.** Desteklenen herhangi bir USB modu |
| `ZebraAllScanners114` | herhangi | Aynı hizmetin JavaPOS 1.14 sürümü |
| `ZebraUSBHandHeld` | IBM Hand-held USB | IBM Hand-held USB modundaki bir tarayıcı |
| `ZebraUSBTableTop` | IBM Table-top USB | Table-top modu |
| `ZebraUSBOPOS` | USB OPOS | USB OPOS modu |
| `ZebraScannerSNAPI` | SNAPI | SNAPI modu |
| `ZebraScannerUSB` | herhangi bir USB | Herhangi bir USB modu |
| `ZebraScannerSerialSSI` | seri SSI | Seri kablolu tarayıcılar (USB değil) |
| `ZebraScannerSerialNIXMODB` | seri NIXMODB | Seri kablolu tarayıcılar |
| `ZebraVDI` | VDI | Sanal bir masaüstüne aktarılan tarayıcılar |

### ZEBRA_HOME

Zebra SDK varsayılandan başka bir yere kuruluysa `run.bat`'i başlatmadan önce `ZEBRA_HOME`
değişkenini onun `JPOS` klasörüne ayarlayın — örneğin yanına koyacağınız küçük bir toplu iş
dosyasında:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

`run.bat` bu durumda Zebra'nın hizmet JAR'larını `%ZEBRA_HOME%bin` içinden alır ve Zebra DLL'leri
için bu klasörü `PATH`'e ekler.

### Kendi jpos.xml dosyanız

Bir cihaz adı eklemek veya bir girdinin ayarlarını değiştirmek için `jpos.xml` dosyasını
`gaia-desktop.jar` dışına kopyalayın (bu bir zip dosyasıdır; her arşiv aracı açar), kopyayı
düzenleyin ve GAIA Desktop'u ona yönlendirin:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

Cihaz seçici bu durumda o dosyadaki tarayıcıları listeler. Birden fazla Zebra tarayıcı bağlıysa,
`ZebraAllScanners` girdisinde olduğu gibi bir girdi `ScannerModel` veya `SerialNumber` özelliği
ile bunlardan birine bağlanabilir.

### Diğer seçenekler

| Ayar | Nasıl |
|---|---|
| Uygulamanın dili | `run.bat --language=FR` (yalnızca bu çalıştırma için) veya Seçenekler paneli |
| Ek Java seçenekleri | `run.bat`'ten önce `set GAIA_DESKTOP_OPTS=…` |
| Birlikte gelen bir Java | `run.bat` yanına bir `jdk-21…` klasörü koyun; `JAVA_HOME`'dan önce kullanılır |

---

## 11. Sorun giderme

Hatalar bağlanırken bir iletişim kutusunda, konsol penceresinde ve `run.bat`'in başlatıldığı
klasördeki (`run.bat`'e çift tıklandığında GAIA Desktop klasörü) `logs\gaia-desktop.log` günlük
dosyasında görünür. Günlükteki köşeli parantez içindeki sayı JavaPOS hata kodudur.

| Gördüğünüz | Olası neden | Ne yapmalı |
|---|---|---|
| *Tarayıcıyı bağla* düğmesi yok | Windows'ta çalışmıyor | Tarama yalnızca Windows'ta vardır |
| 123Scan tarayıcıyı bulamıyor | Kablo, USB bağlantı noktası veya tarayıcıyı tutan başka bir program | Başka bir kablo veya bağlantı noktası deneyin; GAIA Desktop'u ve diğer tarayıcı yazılımlarını kapatın. 123Scan'in göremediği bir tarayıcıyı GAIA Desktop da göremez |
| Konsol: `Zebra services from none (Zebra Scanner SDK not installed)` | SDK varsayılan konumunda değil | 64 bit SDK'yı kurun ([3. adım](#3-zebra-scanner-sdkyı-kurma)) veya [ZEBRA_HOME](#zebra_home) değişkenini ayarlayın |
| Hata **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Zebra'nın hizmet JAR'ları bulunamadı | Yukarıdaki gibi — konsoldaki `Zebra services from` satırını denetleyin |
| Hata **109**: *Service does not exist in loaded JCL registry* | Cihaz adı tarayıcı yapılandırmasında yok veya SDK'nın `javaPOS114.jar` dosyası class path'e girmiş | Cihaz seçiciden bir ad seçin; `javaPOS114.jar` eklemeyin; kendi `jpos.xml` dosyanızı kullanıyorsanız adın içinde olduğunu denetleyin |
| Hata **107** (donanım yok) veya tarayıcı takılıyken bağlantı başarısız | Tarayıcı hâlâ USB klavye modunda veya CoreScanner sürücüsü çalışmıyor | USB modunu ayarlayın ([6. adım](#6-zebra-tarayıcının-usb-modunu-ayarlama)); SDK kurulumundan sonra Windows'u yeniden başlatın; başka bir USB bağlantı noktası deneyin |
| Bağlanırken hata **112** (zaman aşımı) veya **102** (tutuluyor) | Tarayıcıyı başka bir program tutuyor | 123Scan'i ve diğer tüm tarayıcı veya POS yazılımlarını kapatıp yeniden bağlanın |
| Konsolda `UnsatisfiedLinkError` … `CSJPOSScanner64` | Zebra DLL'leri yüklenemiyor — çoğunlukla 64 bit Java ile 32 bit SDK | 64 bit SDK'yı kurun; `java -version` komutunun 64 bit VM bildirdiğini denetleyin |
| Taramalar yakalama olarak gelmek yerine giriş çubuğuna yazılıyor | Tarayıcı USB klavye modunda | USB modunu ayarlayın ([6. adım](#6-zebra-tarayıcının-usb-modunu-ayarlama)) |
| Bağlı, ama taramalar gelmiyor | Tarayıcının moduna göre yanlış cihaz adı veya tarayıcı bağlıyken yeniden takılmış | Bağlantıyı kesin, `ZebraAllScanners` seçin, yeniden bağlanın |
| Raporda ne AIM Code ID ne de veri taşıyıcısı görünüyor | AIM Code ID gönderimi kapalı | *Transmit Code ID Character → AIM Code ID* ayarını açın ([7. adım](#7-zebra-tarayıcıyı-gs1-için-yapılandırma)) |
| Bir eleman dizisinde ayırıcılar eksik veya AI'lar birbirine karışıyor | Klavye modunda tarandı veya `{GS}` olmadan elle yazıldı | Bir JavaPOS USB modu kullanın; elle yazarken ayırıcıları `{GS}` düğmesiyle ekleyin |
| Bir barkod türü hiç okunmuyor | O semboloji tarayıcıda devre dışı | Etkinleştirin ([7. adım](#7-zebra-tarayıcıyı-gs1-için-yapılandırma)) |

Sorun sürerse konsoldaki `run.bat:` satırını, hata iletişim kutusunun metnini ve
`logs\gaia-desktop.log` dosyasını birlikte saklayın: üçü birlikte hangi Java'nın, hangi Zebra
hizmetlerinin ve hangi cihaz adının kullanıldığını ve bağlantının tam olarak nerede başarısız
olduğunu gösterir.

---

## 12. Desteklenen Zebra 2D tarayıcılar

GAIA Desktop, GS1 verilerinin basıldığı barkodları okuyabilen bir tarayıcıya ihtiyaç duyar —
GS1-128, GS1 DataBar, EAN/UPC ve çoğu kullanım için 2D kodlar: **GS1 DataMatrix** ve
**GS1 QR Code**; bir **GS1 Digital Link** URI'si de genellikle bu şekilde basılır. Bu da bir Zebra
**2D görüntüleyici** demektir: doğrusal (1D) bir tarayıcı DataMatrix veya QR kodlarını hiç okuyamaz.

Tarayıcının GS1 uygulama tanımlayıcılarını veya Digital Link'i kendisinin anlaması gerekmez.
**GAIA Desktop ikisini de ayrıştırır**: taramayı tarayıcının okuduğu hâliyle — AIM Code ID, FNC1
ayırıcıları ve her şeyiyle — alır ve kendisi AI'lara böler ya da bir Digital Link URI'sini AI'larına
dönüştürür.

### Test edildi

| Tarayıcı | Tür | Durum |
|---|---|---|
| **DS8108** | Kablolu el tipi 2D görüntüleyici | GAIA Desktop ile **geliştirildi ve test edildi** |

### Çalışması beklenenler

Bunlar, DS8108 gibi Zebra'nın Scanner SDK'sı ve JavaPOS hizmetleri üzerinden sürülen Zebra 2D
görüntüleyicileridir. GAIA Desktop ile **test edilmediler**; her birini bu kılavuzdaki gibi
yapılandırın ve ayar barkodları için kendi Quick Reference Guide veya Product Reference Guide
belgesini kullanın.

| Aile | Modeller (örnekler) |
|---|---|
| DS81x8 | DS8108 (kablolu), DS8178 (kablosuz) |
| DS22x8 | DS2208 (kablolu), DS2278 (kablosuz) — genel amaçlı |
| DS36x8 | DS3608 (kablolu), DS3678 (kablosuz) — dayanıklı, KD ve XR çeşitleri dahil |
| DS46x8 | DS4608 (kablolu), DS4678 (kablosuz) |
| DS9308 / DS9908 | Sunum ve eller serbest tarayıcılar |
| MP7000 / MP7200 | Çok düzlemli bioptik tarayıcılar ve tarayıcı-teraziler |
| CS6080 | Kablosuz yardımcı tarayıcı |

Kablosuz bir tarayıcı beşiğiyle konuşur ve bilgisayara takılan beşiktir: beşiği USB ile bağlayın ve
ikisini tek bir tarayıcı olarak ele alın. Diğer Zebra ailelerindeki tarayıcılar da, Zebra'nın
Scanner SDK'sı onları [6. adım](#6-zebra-tarayıcının-usb-modunu-ayarlama)'daki USB modlarından birinde
destekliyorsa çalışabilir.

### Zebra tarayıcıların yerleşik GS1 özellikleri

Barkodları okumaktan ayrı olarak, bazı Zebra 2D görüntüleyiciler 123Scan ile yapılandırılarak GS1
verilerini tarayıcının kendisinde işleyebilir:

| Özellik | Ne yapar | GAIA Desktop için |
|---|---|---|
| **Label Parse+** | Bir GS1 etiketinin AI'larını tarayıcıda ayrıştırır ve yeniden biçimlendirilmiş alanlar gönderir | **Kapalı bırakın** — GAIA Desktop artık AI'ları, FNC1 ayırıcılarını ve AIM Code ID'yi göremez |
| **GS1 Digital Link** | Hem 1D EAN/UPC hem de 2D Digital Link koduyla işaretlenmiş perakende ürünlerde hangisinin gönderileceğini seçer | **Kapalı bırakın** (varsayılan) |
| **GS1 Retail POS parsing** | Digital Link URI veya eleman dizisi sözdizimindeki perakende 2D barkodları okur | **Kapalı bırakın** |

**GS1 Digital Link** ayarının üç modu vardır. **Mode 1 — GTIN only**, açıldığında başladığı mod,
hangi barkoddan gelirse gelsin yalnızca GTIN'i gönderir; dolayısıyla Digital Link'teki parti, son
kullanma tarihi veya seri numarası GAIA Desktop'a hiç ulaşmaz. **Mode 2 — Prioritize 2D**, 2D kodu
öncelik süresi içinde (varsayılan 200 ms) görürse onu, aksi hâlde 1D'yi gönderir. **Mode 3 —
Corroborate 1D and 2D GTINs Match** 1D'yi gönderir, GTIN'i eşleşirse 2D'yi de gönderir. Ayarın
başka bir yazılım için açık kalması gerekiyorsa Mode 1'i değil, Mode 2'yi kullanın. DS8108'de
bunlar 2373 (aç/kapat), 2374 (mod) ve 2491 (süre) parametreleridir.

Bu özellikler **123Scan sürüm 6 veya daha yenisini** ve bunları içeren bir ürün yazılımını
gerektirir. Zebra, Nisan 2025'ten bu yana ürün yazılımı güncellemeleriyle GS1 Digital Link desteği
ekliyor; aşağıdaki tarihler bir Zebra bayisinden geliyor, bu nedenle her modelin ürün yazılımı
sürüm notlarını denetleyin:

| Model ailesi | GS1 Digital Link ürün yazılımının yayımlanması |
|---|---|
| DS81x8 | 16 Nisan 2025 |
| MP7000 | 21 Nisan 2025 |
| DS9908 | 6 Haziran 2025 |
| SP72 / SM72 | 11 Temmuz 2025 |
| DS46x8 | 28 Temmuz 2025 |
| MP7200 (Rev-C), DS5502 | 30 Temmuz 2025 |
| DS9308 | 29 Ağustos 2025 |
| DS22x8, DS1000 | 5 Eylül 2025 (yalnızca Asya-Pasifik) |
| DS36x8 (KD ve XR dahil), MS47 | 30 Eylül 2025 |
| PL5000, SE4107 | 15 Ekim 2025 |
| CS6080 | 31 Aralık 2025 |

GS1 Digital Link kodlarını GAIA Desktop'a okutmak için bu özelliklerin hiçbiri gerekmez: kapalıyken,
güncel ürün yazılımlı bir Zebra 2D tarayıcı bir Digital Link QR kodunu diğerleri gibi okur ve GAIA
Desktop URI'yi AI'larına dönüştürür.

Kaynaklar: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (Ağustos 2025), 12. bölüm
"GS1 Digital Link Parameters" ve Data Parsing bölümü; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (ürün yazılımı tarihleri).
