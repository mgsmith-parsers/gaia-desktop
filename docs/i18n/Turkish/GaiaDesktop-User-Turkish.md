# GAIA Desktop — Kullanıcı Kılavuzu

Uygulamanın nasıl kullanılacağını anlatır. Derleme, değiştirme ve dil ekleme için
[geliştirici kılavuzuna](../../GaiaDesktop-Developer.md) (İngilizce) bakın. Özgün belge:
[GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## İçindekiler

1. [Nedir](#nedir)
2. [Çalıştırma](#çalıştırma)
3. [Pencere](#pencere)
4. [Çözümle kipi](#çözümle-kipi)
   - [Girdi çubuğu ve {GS} belirteci](#girdi-çubuğu-ve-gs-belirteci)
   - [Tarama](#tarama)
   - [Dosya açma](#dosya-açma)
   - [Yakalama geçmişi](#yakalama-geçmişi)
5. [Oluştur kipi](#oluştur-kipi)
   - [AI düzenleyicisi](#ai-düzenleyicisi)
   - [Düzenleyicinin kabul etmedikleri](#düzenleyicinin-kabul-etmedikleri)
   - [Öğe dizesi mi, Digital Link mi](#öğe-dizesi-mi-digital-link-mi)
   - [Var olan bir yakalamayı düzenleme](#var-olan-bir-yakalamayı-düzenleme)
6. [Rapor](#rapor)
   - [Dışa aktarma](#dışa-aktarma)
7. [Çözümleme seçenekleri](#çözümleme-seçenekleri)
   - [Programın ilk açılışı](#programın-ilk-açılışı)
8. [Diller](#diller)
9. [Neler hatırlanır](#neler-hatırlanır)
10. [Klavye kısayolları](#klavye-kısayolları)
11. [Sorun giderme](#sorun-giderme)
12. [Sınırlar](#sınırlar)

---

## Nedir

GAIA Desktop, GS1 verileriyle iki iş yapar:

- **Çözümleme** — bir GS1 öğe dizesini ya da Digital Link URI'sini yazın, yapıştırın, açın veya
  tarayın; sonucu eksiksiz bir rapor olarak okuyun: her uygulama tanımlayıcısının (AI) ne
  anlama geldiğini, neyin yanlış olduğunu ve nedenini;
- **Oluşturma** — AI / değer çiftlerini bir öğe dizesine ya da Digital Link URI'sine
  dönüştürme.

Uygulamanın GS1 hakkında bildiği her şey — her AI tanımı, her doğrulama kuralı, her hata iletisi
ve yerelleştirilmiş her açıklama — altındaki GAIA motorundan gelir. Uygulama kendi kurallarını
eklemez: kabul ettiği şey, tam olarak motorun kabul ettiği şeydir.

---

## Çalıştırma

```bash
java -jar gaia-desktop.jar
```

JAR dosyasının yanında dağıtılan `lib/` dizinine ihtiyacı vardır — ikisini birlikte tutun,
yoksa uygulama başlamaz. (Tek başına çalışan bir JAR da derlenebilir;
[geliştirici kılavuzuna](../../GaiaDesktop-Developer.md#packaging) bakın.)

**Java 21 veya üzeri** kurulu olmalıdır. Dağıtım tek bir işletim sistemine ve işlemci mimarisine
özgüdür; kendi makineniz için derlenmiş JAR dosyasını kullanın.

Kayıtlı dili değiştirmeden, uygulamayı belirli bir dilde açmak için:

```bash
java -jar gaia-desktop.jar --language=FR
```

Değer olarak katalog kodu (`FR`, `PCM`, `ARZ`), tam ad (`FRENCH`, `WU-CHINESE`) ya da bir dil
etiketi (`fr`; Kantonca için `zh-HK`, Mısır Arapçası için `ar-EG`) verilebilir — büyük küçük
harf ayrımı yoktur. Bu belirtim yalnızca o çalıştırma için geçerlidir: kayıtlı diliniz olduğu
gibi kalır, dolayısıyla bir meslektaşınız için oluşturduğunuz kısayol, kendi kopyanızın açıldığı
dili değiştiremez. Dili uygulamanın *içinden* seçmek ise gerçek bir seçimdir ve her zamanki gibi
kaydedilir. Hiçbir dili göstermeyen bir değer, konsola bir not düşülerek yok sayılır; uygulama
başlamayı reddetmek yerine kayıtlı dilde açılır.

Açılışta konsol, uygulamanın sürümünü, derleme tarihini ve motorun sürümünü içeren tek bir satır
yazar. Hata bildiriminde alıntılanacak satır budur.

---

## Pencere

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

> Şema, bu belgedeki tüm kod blokları gibi İngilizce etiketleri korur. Ekranda *Çözümle* /
> *Oluştur*, *Yakalamalar*, *Raporu kaydet*, *Seçenekler*, *tarayıcı: kapalı* ve *Hakkında*
> göreceksiniz.

**macOS ve Linux'ta şemadaki tarayıcı satırı yoktur** — araç çubuğunda ne düğme ne aygıt seçimi,
*Tarayıcı* menüsü yok, durum çubuğunda *tarayıcı: kapalı* yok. Bkz. [Tarama](#tarama).

**Çözümle ⇄ Oluştur** anahtarı yalnızca araç çubuğunun altındaki şeridi ve yakalama ile ilgili
denetimleri değiştirir. Geçmiş, rapor, seçenekler paneli ve dışa aktarma eylemleri her iki kipe
de hizmet eder; çünkü başarılı bir oluşturma yalnızca bir yakalama daha demektir.

Seçenekler paneli **ilk açılışta kapalıdır**. Kip, panelin görünürlüğü, tema ve son kullanılan
tarayıcı çalıştırmalar arasında hatırlanır.

---

## Çözümle kipi

### Girdi çubuğu ve {GS} belirteci

Çubuk; bir öğe dizesini, bir GS1 Digital Link URI'sini ya da AIM Code ID önekiyle (`]d2…`)
birlikte ham tarama verisini kabul eder. <kbd>Enter</kbd> çözümlemeyi başlatır.

Değişken uzunluktaki AI'ler **FNC1** ile ayrılır — görünmez denetim karakteri `0x1D`. Bir metin
alanı bu karakteri tutamaz: sessizce yutulur ve `01…` + FNC1 + `10LOT`, `01…10LOT` olarak
ulaşır; yani anlamı değişmiş bir öğe dizesi.

Bu yüzden uygulama ayırıcıyı görünür **`{GS}`** belirteci olarak gösterir ve çözümleyiciye giden
yolda geri çevirir:

- **`{GS}` düğmesi** belirteci imlecin bulunduğu yere ekler;
- gerçek ayırıcı karakterleri **yapıştırmak** — klavyeyle, bağlam menüsüyle ya da sürükleyip
  bırakarak — onları kendiliğinden `{GS}` yapar; hiçbir şey kaybolmaz;
- belirteç, raporun yazdığı biçimde yazılır; böylece rapordan kopyalanan bir değer olduğu gibi
  geri yapıştırılabilir.

Yakalama, rapor ve kayıtlı geçmiş, hepsi **gerçek** ayırıcıyı taşır. Belirteç yalnızca sizin
gördüğünüz ve yazdığınız biçimdir.

### Tarama

> **Yalnızca Windows.** Zebra, tarayıcı sürücülerini yalnızca Windows için sağlar; bu yüzden
> macOS ve Linux'ta uygulama hiçbir tarayıcı denetimi oluşturmaz — *Tarayıcıyı bağla* düğmesi,
> aygıt seçimi, *Tarayıcı* menüsü ve durum çubuğundaki tarayıcı durumu yoktur. Hiçbiri soluk
> gösterilmez; çünkü bunu etkinleştirebilecek bir ayar ya da aygıt yoktur. Kaydedilmiş
> taramaların bulunduğu bir dosyayı açmak dahil geri kalan her şey her yerde çalışır.

*Tarayıcıyı bağla*, seçim kutusunda belirtilen aygıtı açar; `ZebraAllScanners` varsayılan
değerdir ve çoğu modele uyar. Her tarama çözümlenir ve kendiliğinden geçmişe eklenir.

Tarayıcı verisi bayt bayt okunur; böylece AIM Code ID öneki ve her FNC1 ayırıcısı, tarayıcının
gönderdiği biçimde çözümleyiciye ulaşır.

Oluştur kipinde tarayıcı denetimleri gizlenir, ancak bağlı bir tarayıcı çalışmayı sürdürür:
*Tarayıcı → Bağla / Bağlantıyı kes* (<kbd>⌘K</kbd>) hâlâ onu değiştirir, durum çubuğu hâlâ
bildirir ve düzenleyici açıkken gelen bir tarama yine yakalanır.

Bir aygıt bağlanmıyorsa [Sorun giderme](#sorun-giderme) bölümüne bakın.

### Dosya açma

*Dosya → Metin dosyası aç…*, **boş olmayan her satırı ayrı bir yakalama olarak** çözümler; bu,
birinin size gönderdiği tarama yığınını hızlıca işlemenin yoludur. Dosya bayt bayt okunur, bu
yüzden içindeki ham ayırıcılar korunur.

### Yakalama geçmişi

En yeniler üstte. Her satırda geçerli / geçersiz noktası, tek satırlık bir özet, ayrıca yakalama
saati, kaynağı ve içerdiği AI sayısı bulunur. Tarih yalnızca önceki günlere ait yakalamalarda
görünür; bugünün satırlarında yalnızca saat vardır.

Özet, Digital Link için **Digital Link URI'si**, diğer durumlarda **HRI**'dir: bir öğe dizesinde
`(01)09506000134352 (17)261231`, `01095060001343521726123110…` ifadesinden daha okunaklıdır.

Bir yakalamaya sağ tıklayarak girdisini veya HRI'sini kopyalayabilir, girdi çubuğuna geri
gönderebilir, düzenleyicide açabilir, kaldırabilir ya da geçmişi temizleyebilirsiniz.

---

## Oluştur kipi

### AI düzenleyicisi

AI / değer çiftlerinden oluşan bir tablo. Bir AI kodu yazın ya da *AI ekle…* listesinden
**motorun bildiği tüm AI'ler** arasından seçin; **Başlık**, **Biçim** ve **Açıklama** sütunları
siz yazdıkça, seçtiğiniz dilde dolar. Liste, oluşturmanın reddedeceği bir AI'yi sunmaz.

Tablonun sonunda her zaman **bir boş satır** durur: oraya yazın, bir sonrakisi belirir; böylece
birden çok AI içeren bir dize için *Satır ekle* hiç gerekmez.

Düzenleme, bir tablonun davranması gerektiği gibi davranır: <kbd>Enter</kbd>, <kbd>Tab</kbd>,
<kbd>Shift</kbd>+<kbd>Tab</kbd> ve başka bir yere tıklamak, yazdığınızı **korur**;
<kbd>Esc</kbd> atar. <kbd>Tab</kbd>, AI → Değer → sonraki satır sırasıyla ilerler ve
değiştirilemeyen sütunları atlar.

### Düzenleyicinin kabul etmedikleri

| | Ne olur |
|---|---|
| Motorun tanımadığı bir AI | Siz yazarken satır kırmızıya döner; oluşturma bir açıklamayla reddedilir |
| Listeden aynı AI'yi ikinci kez | Eklenmez — bir ileti onu belirtir ve zaten içeren satır seçilir |
| Elle yazılarak aynı AI ikinci kez | Her iki satır da işaretlenir; oluşturma `GE-S004` hatasıyla reddedilir |
| Oluşturulacak bir şey yok | Bir ileti; hiçbir yakalama oluşturulmaz |

Bir AI, bir öğe dizesinde yalnızca bir kez bulunabilir; bu yüzden liste, baştan başarısız olacak
bir oluşturmaya izin vermek yerine hemen reddeder.

### Öğe dizesi mi, Digital Link mi

**Öğe dizesi oluştur**, standardın gerektirdiği her yere FNC1 ayırıcısını koyarak satırları
birleştirir. **Digital Link oluştur** bir URI üretir; onu ölçünlü `id.gs1.org` yerine kendi
çözümleyicinize yöneltmek için *Digital Link hedefi* bölümünü açın:

| Alan | Etkisi | Boş bırakılırsa |
|---|---|---|
| Alan adı | Çözümleyici konağı, örn. `example.com` | `id.gs1.org` |
| Yol öneki | AI yolundan önceki bir parça, örn. `/products` | önek yok |

**Başarı** durumunda sonuç tam olarak bir tarama gibi davranır: çözümlenir, geçmişe eklenir,
rapor olarak çizilir ve kaydedilir. Oluşturma kendi çıktısını çözümleyerek doğrular; dolayısıyla
geri verdiği her şey kesinlikle geçerlidir.

**Başarısızlık** durumunda hiçbir şey yakalanmaz. Nedeni oluşturma düğmelerinin altında kalır;
her sorun için bir satır, her biri hatayı, ait olduğu AI'yi ve motorun kendi açıklamasını
gösterir:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> Bu iletideki ikilenmiş tırnaklar bu uygulamadan değil, motorun hata kataloğundan gelir.

### Var olan bir yakalamayı düzenleme

Bir yakalamaya sağ tıklayıp → **Oluştur** dediğinizde AI'leri düzenleyiciye yüklenir. Bir değeri
değiştirip yeniden oluşturun; sonuç yeni bir yakalama olarak gelir — özgün olan olduğu gibi
kalır.

---

## Rapor

Seçilen yakalama eksiksiz bir rapor olarak çizilir: karar başlığı, etiketli bir "şerit" hâlinde
ayrıştırılmış öğe dizesi, çözümleme üstverisi, her AI için yorumlarını taşıyan birer kart ve
düzeltme yönergeleriyle birlikte hatalar ile uyarılar.

**Digital Link** için her AI kartı ayrıca yapısal rolünü taşır — **birincil anahtar**, **anahtar
niteleyici** ya da **veri özniteliği** — URI'de göründükleri sıraya göre renklendirilmiş olarak.
Roller yalnızca Digital Link'te gösterilir: düz bir öğe dizesinde bir AI'nin betimlenecek bir
URI konumu yoktur.

Rapor, seçtiğiniz dili ve temayı izler.

### Dışa aktarma

| | |
|---|---|
| **Raporu kaydet → HTML olarak…** (<kbd>⌘S</kbd>) | Ekranda ne varsa tam olarak onu yazar: her tarayıcıda açılan, kendi kendine yeten tek bir dosya |
| **Raporu kaydet → PDF olarak…** (<kbd>⌘P</kbd>) | Raporu sistemin yazdırma penceresi üzerinden yazdırır — PDF hedefini orada seçin |

PDF'in yazdırma penceresinden geçmesinin nedeni, hedefin ve dosya adının orada seçilmesidir:
macOS'ta **PDF ▾ → PDF Olarak Kaydet**, Windows'ta **Microsoft Print to PDF**, Linux'ta
**Dosyaya yazdır**. Sayfalar düzgün bölünür ve metin seçilebilir kalır.

Yazdırma ve PDF çıktısı, uygulama koyu temada olsa bile her zaman açık paleti kullanır; renkli
AI etiketlerini ve karar başlığını olduğu gibi korur.

Makinenizde hiç yazıcı ya da PDF hedefi tanımlı değilse uygulama bunu bildirir ve her tarayıcıdan
yazdırılabilen HTML dışa aktarımına yönlendirir.

---

## Çözümleme seçenekleri

Sağdaki panel motorun tüm seçeneklerini toplar. Herhangi birini değiştirmek **seçili yakalamayı
anında yeniden çözümler**; böylece panel yalnızca bir sonraki taramayı etkilemekle kalmaz, "bu
ayarlarla bu girdi nasıl görünürdü?" sorusunu sormanın da yoludur.

| Seçenek | Değerler |
|---|---|
| Ayrıştırma modu | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — çözümlemenin ne kadar derine ineceği |
| Dil | 35 ileti kataloğu |
| Sıra | `LITTLE` (gg/aa/yyyy), `MIDDLE` (aa/gg/yyyy), `BIG` (yyyy/aa/gg) |
| Ayırıcı | `SLASH`, `HYPHEN`, `PERIOD` |
| Ay | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Yıl | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| "requires" denetimini atla | Zorunlu eşi eksik olan bir AI'yi artık geri çevirmez (`GE-S005`) |
| "excludes" denetimini atla | Standardın yasakladığı AI eşleşmelerini artık geri çevirmez (`GE-S006`) |

Panelin altındaki *Varsayılanlara sıfırla*, motorun kendi varsayılanlarını geri getirir.

### Programın ilk açılışı

Henüz kaydedilmiş bir şey yokken uygulama, ayarlarını motorun varsayılanlarından değil
**makinenizin bölgesel ayarlarından** alır:

- **dil**, sistemin yerel ayarından gelir; buna karşılık gelen katalog yoksa İngilizceye döner;
- **tarih seçenekleri**, yerel ayarınızın kısa tarih biçiminden okunur: bileşenlerin sırası,
  ayırıcı ve ayın ile yılın kısaltılıp kısaltılmadığı.

> **Not.** Dolayısıyla kısa tarihleri iki haneli yılla yazan bir yerel ayar burada da iki haneli
> yılla başlar: bu, sisteminizin geri kalanıyla uyuşur ama motorun dört haneli varsayılanından
> ayrılır. Panelden değiştirin, seçim kalıcı olur.

---

## Diller

Bir dil seçmek yalnızca hata iletilerini değil, **uygulamanın tamamını** değiştirir — menüler,
düğmeler, düzenleyici, seçenekler paneli, durum satırı ve rapor. Sağdan sola yazılan diller
pencereyi ve raporu çevirir.

35 dilin tamamı kullanılabilir, iki katmanlı kapsamla:

| | Kapsam |
|---|---|
| Çözümleme raporları ve motorun tüm hata iletileri | **35 dilin tamamı** |
| Uygulamanın kendi menüleri, düğmeleri ve etiketleri | **35 dilin tamamı** |

Sağdan sola yazılan diller — Arapça, Mısır Arapçası, Farsça ve Urduca — uygulamanın açıldığı
andan başlayarak pencereyi ve raporu çevirir.

Dil, çalıştırmalar arasında hatırlanır. Yalnızca bir kez başka bir dilde açmak için
[`--language`](#çalıştırma) bölümüne bakın.

Birkaç şey tüm dillerde bilerek çevrilmeden bırakılır: ham değerlerin yanına yazdırılan kısa alan
adları (`format`, `position`, `raw`, `len` …) — bunlar sözcük değil, tanımlayıcıdır; örnek adları
ve simgeleme adları — bunlar GS1 ve sektör terimleridir; ve yarım bir çevirinin yanıltacağı uzun
bir standart paragrafı.

---

## Neler hatırlanır

Ev dizininizdeki `.gaia-desktop` içinde iki dosya:

| Dosya | İçeriği |
|---|---|
| `options.properties` | Tüm çözümleme seçenekleri, tema, seçenekler panelinin açık olup olmadığı, son kullanılan tarayıcı ve pencerenin bırakıldığı kip |
| `captures.tsv` | Yakalama geçmişi, en yeniler üstte, en çok 500 kayıt |

Seçenekler her değiştirdiğinizde, geçmiş her değiştiğinde yazılır. İkisi de çıkışta yeniden
yazılır.

Dosyalardan birini silmek yalnızca o bölümü sıfırlar, başka hiçbir şeyi değil — bozuk bir
geçmişin size ayarlarınıza mal olmaması için ayrı tutulurlar. Elle düzenlenip bozulmuş bir dosya
da yıkıcı değildir: her ayar tek tek kendi varsayılanına döner, dolayısıyla hatalı bir satır
yalnızca o satır kadar zarar verir.

Geçmiş, çözümlemeyi değil girdiyi saklar. Yüklenirken yeniden çözümlenir; yani geri yüklenen bir
yakalama, yakalandığı andaki değil **şu anda** geçerli olan seçenekleri yansıtır.

---

## Klavye kısayolları

| | |
|---|---|
| <kbd>Enter</kbd> | Girdi çubuğunu çözümle |
| <kbd>⌘O</kbd> | Metin dosyası aç |
| <kbd>⌘S</kbd> | Raporu HTML olarak kaydet |
| <kbd>⌘P</kbd> | Raporu PDF olarak kaydet (sistem yazdırma penceresi) |
| <kbd>⌘G</kbd> | FNC1 ayırıcı belirtecini ekle |
| <kbd>⌘K</kbd> | Tarayıcıyı bağla / bağlantıyı kes (yalnızca Windows) |
| <kbd>⌘D</kbd> | Koyu temayı değiştir |
| <kbd>⌘Q</kbd> | Çık |

Düzenleyicide: <kbd>Tab</kbd> / <kbd>Shift</kbd>+<kbd>Tab</kbd> değiştirilebilir hücreler
arasında gezinir, <kbd>Enter</kbd> onaylar, <kbd>Esc</kbd> vazgeçer.

---

## Sorun giderme

| Belirti | Nedeni ve çözümü |
|---|---|
| Uygulama başlamıyor ya da `NoClassDefFoundError` bildiriyor | `lib/` dizini JAR dosyasının yanında olmalıdır. Birlikte tutun ya da kendi kendine yeten bir dağıtım kullanın |
| Açılışta konsolda *"Unsupported JavaFX configuration…"* | Beklenen ve zararsız — araç takımı yalnızca nasıl yüklendiğini bildiriyor. Uygulama normal çalışır |
| Yapıştırdığınız ayırıcı yok oluyor gibi | Beklenen davranış: görünür `{GS}` belirtecine dönüştürülür, çözümleyici yine gerçek karakteri alır. Bkz. [{GS} belirteci](#girdi-çubuğu-ve-gs-belirteci) |
| *Tarayıcıyı bağla* düğmesi yok | macOS ya da Linux kullanıyorsunuz; oralarda tarayıcı sürücüleri yok. Bkz. [Tarama](#tarama) |
| Tarayıcı bağlanmıyor | Seçim kutusundaki mantıksal adın modelinizle eşleştiğini, tarayıcının takılı olduğunu ve başka bir uygulamanın onu tutmadığını denetleyin |
| PDF dışa aktarımı hedef olmadığını söylüyor | Makinenizde yazıcı ya da PDF hedefi tanımlı değil. Bir tane ekleyin ya da HTML olarak dışa aktarıp tarayıcıdan yazdırın |
| Bir menü ögesi `act.hri` gibi bir şey gösteriyor | Eksik bir etiket — lütfen bildirin; bu bir ayar değil, derleme kusurudur |

---

## Sınırlar

- **Tarama yalnızca Windows'ta** çalışır, çünkü sürücüler öyledir.
- **PDF, kaydetme penceresinden değil yazdırma penceresinden** geçer.
- **Digital Link hedefleri yalnızca alan adı ile yol öneki** kabul eder; ek sorgu değiştirgeleri
  ya da parça kabul etmez.
- **Bir AI yalnızca bir kez** bulunabilir bir öğe dizesinde — bu, uygulamanın değil GS1'in
  kuralıdır.
