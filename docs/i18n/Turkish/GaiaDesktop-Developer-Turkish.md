# GAIA Desktop — Geliştirici Kılavuzu

Modülün nasıl derlendiği, nasıl düzenlendiği ve nasıl test edildiği. Uygulamanın dışarıdan bakıldığında ne
yaptığı için [Kullanıcı Kılavuzu](GaiaDesktop-User-Turkish.md)'na bakın. Özgün belge:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## İçindekiler

1. [Modüle genel bakış](#modüle-genel-bakış)
2. [Derleme ve çalıştırma](#derleme-ve-çalıştırma)
   - [Paketleme](#paketleme)
3. [Mimari](#mimari)
   - [Girdi hunisi](#girdi-hunisi)
   - [Thread kullanımı](#thread-kullanımı)
   - [Yeniden kurma ve yeniden etiketleme](#yeniden-kurma-ve-yeniden-etiketleme)
4. [{GS} belirteci](#gs-belirteci)
5. [Tarayıcı entegrasyonu](#tarayıcı-entegrasyonu)
   - [Platform kapısı](#platform-kapısı)
   - [Sembolojiler](#sembolojiler)
6. [Yerelleştirme](#yerelleştirme)
   - [`--language` argümanı](#--language-argümanı)
   - [Dil ekleme](#dil-ekleme)
   - [Bilerek çevrilmeyenler](#bilerek-çevrilmeyenler)
7. [Saklanan durum biçimleri](#saklanan-durum-biçimleri)
8. [Test](#test)
9. [Derleme sorunlarını giderme](#derleme-sorunlarını-giderme)
10. [Bilinen sınırlamalar ve açık işler](#bilinen-sınırlamalar-ve-açık-işler)

---

## Modüle genel bakış

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Çekirdek kitaplığın bir istemcisi, fazlası değil. İçinde **hiçbir GS1 bilgisi gömülü değildir**: her AI tanımı,
doğrulama kuralı, hata mesajı ve yerelleştirilmiş açıklama [`GaiaParser`](../../../../gaia/docs/GaiaParser.md)
ve [`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md)'dan gelir. Özellikle AI listesi bu modüle asla
kopyalanmaz — oluşturucunun seçicisi çekirdeğin kendi kayıt defterini okur, dolayısıyla oluşturucunun
reddedeceği bir AI'yı sunamaz.

**Giriş noktaları:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (JavaFX `Application`'ı) ve
`tools.pantheum.gaia.desktop.Launcher` (paketlenmiş jar'ın `Main-Class`'ı).

Modül bir yapraktır: hiçbir şey ona bağımlı değildir ve o da başka hiçbir aşağı akış modülüne bağımlı değildir.
Başlangıç noktası olan iki sınıf, bağımlılık olarak alınmak yerine `gaia-demo`'dan **kopyalanmıştır** —
`Symbologies` hâlâ oradaki ikiziyle elle uyumlu tutulur ve ikisi de bunu belirten bir not taşır.

---

## Derleme ve çalıştırma

- Çalıştırmak için **Java 21**, derlemek için JDK 21 veya daha yenisi — modül `release=21` ile derlenir.
  JavaFX 21 buna karşılık gelen LTS hattıdır. Çekirdek jar hâlâ Java 11 bayt kodudur ve 21'de değişmeden
  çalışır.
- `PATH` üzerinde **Maven**. Bu depoda wrapper yoktur.
- **Önce çekirdek kurulmalı.** Bu modül `gaia:1.1.0`'ı `~/.m2`'den çözümler; eski veya eksik bir çekirdek jar,
  `package tools.pantheum.gaia does not exist` hatasının olağan nedenidir.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` tek komut satırı argümanıdır ve tüm profili başka bir yere yönlendiren `-Dgaia.desktop.home=…`
dışında başlangıcı etkilemenin tek yoludur.

`mvn javafx:run` geliştirme yoludur: eklenti JavaFX'i **modül yoluna** koyar, böylece araç takımı adlandırılmış
bir modül olarak başlar ve `GaiaDesktopApp` doğrudan başlatılabilir. Paketlenmiş bir jar bunu yapamaz — her şey
sınıf yolundadır — bu yüzden `Main-Class`'ı `Launcher` ara katmanıdır. Bu düzende `Main-Class` olarak bir
`Application` alt sınıfı *"JavaFX runtime components are missing"* ile başarısız olur.

Her zaman `clean` kullanın — bir dizi platformu derlemeden önce bir kez, çünkü `target/dist/`'i hepsinden
boşaltır. Nedeni için [Derleme sorunlarını giderme](#derleme-sorunlarını-giderme)'ye bakın.

### Paketleme

Gönderilebilecek her şey `target/dist/gaia-desktop-<version>-<platform>/` altında toplanır ve başka hiçbir şey toplanmaz;
böylece dizin, `target/` içinde `classes/`, `maven-status/` ve derlemenin geri kalan geçici dosyalarının
yanından yapıtları ayıklamadan, bir bütün olarak zip'lenebilir veya kopyalanabilir. `${dist.dir}` onu tek bir
yerde adlandırır; yapıt yazan her eklenti ona işaret eder.

#### Platform başına bir derleme

JavaFX jar'ları yerel kod taşır, dolayısıyla her dağıtım tek bir platform içindir ve `fx.platform` özelliği
tarafından bir OpenJFX sınıflandırıcısı olarak adlandırılır: `mac-aarch64`, `mac`, `win`, `linux` veya
`linux-aarch64`. İşletim sistemine göre etkinleşen beş `fx-*` profili onu derleyen makinenin kendi platformuna
ayarlar; sade bir `mvn package` veya `mvn javafx:run` için gereken de yalnızca budur. `-Dfx.platform=…` bunların
önüne geçerek herhangi bir makineden başka herhangi bir platformu derler — JavaFX jar'ları yalnızca
indirmelerdir. `${dist.dir}` `target/dist/${fx.platform}`'dur, dolayısıyla bir platformun derlemesi asla
diğerininkinin üzerine yazmaz.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Bunu çalışır kılan iki pom ayrıntısı vardır ve ikisi de kaldırılabilirmiş gibi görünür:

- **Her JavaFX modülü doğrudan bir bağımlılıktır** — base, graphics, controls, media ve web — her biri
  `<classifier>${fx.platform}</classifier>` ve bir `org.openjfx:*` dışlamasıyla. OpenJFX'in pom'ları geçişli
  jar'larını *derleyen makinenin* işletim sistemine göre seçer; dolayısıyla graphics veya base'in geçişli olarak
  gelmesine izin vermek, bir Windows derlemesine Windows yerel kitaplıklarının yanına Mac olanlarını da koyar.
- **Özellik `javafx.platform` değil, `fx.platform`'dur.** Dışarıdan ayarlandığında OpenJFX'in kendi özelliği,
  üst pom'u tarafından kendisi cinsinden yeniden tanımlanır; Maven o pom'u geçersiz sayıp reddeder ve platform
  jar'larını sessizce düşürür, geriye içinde hiç JavaFX olmayan bir derleme kalır.

Her iki düzen de aynı yolu yazar, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`, ve her derlemede tam
olarak biri çalışır. İkisinin birden o yolu sahiplenmesini engelleyen `${jar.phase}`, `assembly.skipAssembly`
ve `mdep.skip`'tir.

| | Varsayılan | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — yalnızca bu modülün sınıfları ve kaynakları | ~50 MB — bağımlılıklar içine açılmış |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 jar, manifest `Class-Path`'inde tek tek adlandırılmış | — |
| Gönderilen | jar **ve** yanındaki `lib/` | tek dosya |
| `dist/gaia-desktop-<version>-<platform>/` içinde ayrıca | `run.bat` (win) veya `run.sh` (diğerleri) | aynı |
| Ve | `readme.html` ve `docs/` (her platform) | aynı |

Her dağıtım kılavuzları da taşır: `docs/` düzeni korunarak kopyalanır, böylece kılavuzlar ve çeviriler
arasındaki bağlantılar çalışmaya devam eder; çeviri araçları (`check-i18n.py`, `i18n-manifest.json`,
`i18n-plan.md`) hariç tutulur. Kökte de `readme.html` bulunur; her birine bağlantı veren bir dizindir,
`src/main/dist/readme.html`'den gelir ve içindeki `@VERSION@` pom sürümüyle değiştirilir. İkisini de
`pick-launch-script` antrun adımı yazar; bu adım önce `docs/`'u siler, böylece kaldırılmış bir kılavuz geride
kalmaz. Readme'deki bağlantılar elle yazılmıştır — `docs/` altında adı değiştirilen bir kılavuz veya eklenen bir
çeviri orada da kendi satırını gerektirir.

Manifest `Class-Path`'i ve `lib/` tek bir şeyin iki yarısıdır: jar her çalışma zamanı bağımlılığı için
`lib/<artifact>-<version>.jar`'ı adlandırır ve `copy-dependencies` `lib/`'i tam olarak bu adlarla doldurur.
`java -jar`'ın çalışmasını sağlayan budur.

#### macOS uygulaması ve dağıtım zip dosyası

Tek bir antrun yürütmesi, `mac-app-and-zip`, `package` aşamasını bitirir. Bu nedenle antrun eklentisi jar ve
assembly eklentilerinden **sonra** bildirilmiştir — Maven bir aşamanın yürütmelerini eklenti bildirim sırasıyla
çalıştırır — böylece o çalıştığında jar, iki paketlemeden hangisi olursa olsun, zaten vardır. (Diğer yürütmesi
`pick-launch-script` `prepare-package` içindedir ve etkilenmez.)

`mac` ve `mac-aarch64` için önce dağıtım dizininde `GAIA Desktop.app`'i oluşturur: `Info.plist` (`@VERSION@`
pom sürümüyle değiştirilmiş), başlatıcı `Contents/MacOS/gaia-desktop` (çalıştırılabilir yapılmış) ve
`gaia-desktop.icns`, hepsi `src/main/macos/`'tan; ardından `gaia-desktop.jar` ve `lib/`'i `Contents/Java`'ya
**taşır**, böylece paket kendi başına durur. Yanında jar olmadığında `run.sh` oraya bakar. Başlatıcı Java'yı
sürüme göre bulur (`java_home -v 21+`, sonra Homebrew'un openjdk'si — Finder'dan yapılan başlatmalar
`JAVA_HOME` veya `PATH` görmez), bulamazsa `osascript` ile bir iletişim kutusu gösterir, logback'in göreli
`logs/` dizini yazılabilir olsun diye `~/.gaia-desktop`'a `cd` yapar ve `-Xdock:name`/`-Xdock:icon`
geçirir. macOS çalışan `java`'yı paketle ilişkilendirir (paket kimliği ve Dock simgesi uygulamanınkidir); menü
çubuğunda GAIA Desktop yazar.

Ardından her platform için dizini, Ant'ın `<zip>`'iyle, aynı adlı bir klasörün altında
`target/dist/gaia-desktop-<version>-<platform>.zip` olarak sıkıştırır: `run.sh` ve `Contents/MacOS/**`,
`filemode="755"` ile ayrı bir dosya kümesi olarak; aksi halde açıldıklarında çalıştırılamazlar. Zip **iliştirilmez**:
GitHub sürümleri içindir ve dört platform dört derlemeden gelir; sürüm başına tek ve değiştirilemez bir yükleme
kabul eden Maven Central bunları asla tek bir sürüm olarak alamaz.

`project.build.outputTimestamp` derleme zamanına ayarlanır. Aksi halde maven-jar-plugin 3.5 ve
maven-assembly-plugin 3.8'deki arşivleyiciler her girdiye 1 Şubat 1980 tarihini verir.

Uygulama simgesini `tools/icon/GaiaIcon.java` çizer (derlemenin parçası değildir; başlığında `sips`/`iconutil`
adımları vardır). Çıktıları depoya işlenmiştir: paket için `src/main/macos/gaia-desktop.icns` ve
`GaiaDesktopApp`'in pencere simgesi olarak ayarladığı `src/main/resources/icons/gaia-desktop-{16..256}.png`
(Windows ve Linux; macOS pencere simgelerini yok sayar).

#### Başlatma betikleri

`run.sh` ve `run.bat` `src/main/scripts/`'ten gelir ve filtrelenmeden `dist/gaia-desktop-<version>-<platform>/`'e
kopyalanır — `run.sh`, Maven filtrelemesinin keyfince yeniden yazabileceği `${...}` kabuk genişletmeleriyle
doludur. Ardından bir antrun adımı yalnızca platformun çalıştırdığını tutar — `win` için `run.bat`, diğer hepsi
için `run.sh` — ve `copy-resources` taşımadığı için `run.sh` üzerinde çalıştırma bitini ayarlar. Kararı
profiller üzerinden değil, çözümlenmiş `${fx.platform}`'dan verir, çünkü profil etkinleştirmesi başka bir
profilin ayarladığı değeri göremez: aksi halde Windows'ta yapılan bir Linux derlemesi ana makinenin betiğini
alırdı.

Betikler **manifest'e güvenmez**. Her biri sınıf yolunu kendisi kurar: `gaia-desktop.jar`, ardından `lib/`
içindeki her `.jar`, bir `lib/*` joker karakterine bırakılmak yerine tek tek adlandırılarak; böylece boşluk içeren
bir yol tek bir girdi olarak kalır ve eklenenlerin sayısı gerçektir. `-cp` ile başlatmak, betiklerin
`tools.pantheum.gaia.desktop.Launcher`'ı açıkça adlandırmasının nedenidir: `-cp` manifest'in `Main-Class`'ını
okumaz. Ama `Class-Path`'ini **hâlâ** izler — `-cp` üzerindeki bir jar, manifest'inin adlandırdığı jar'ları da
getirir ve bunlar hemen ardından aranır — dolayısıyla bir betik listelese de listelemese de her `lib/` jar'ı
yüklenir ve birini betiğin sınıf yolundan çıkarmak onu JVM'nin dışında tutmaz. Betikler yine de hepsini
listeler, böylece manifest'i eski, elle düzenlenmiş veya bir girdisi eksik olan bir jar da başlar. Aynı betik
iki paketlemeye de hizmet eder: `-Pfat` jar'ının ekleyecek bir `lib/`'i yoktur ve gerek de duymaz; boş bir
`lib/`'in hata değil not olmasının nedeni de budur.

#### Zebra yerel kitaplıkları

Derleme Zebra'ya ait hiçbir şey göndermez — DLL yok, hizmet jar'ı yok. Windows'ta bunlar makineye kurulu Zebra
Scanner SDK'sından gelir: `run.bat`, SDK'nın `Jpos*.jar` hizmet jar'larını ve `bin\`'i sınıf yoluna, `bin\`'i de
`PATH`'e ekler; `System.loadLibrary` orada `CSJPOSScanner.dll` / `CSJPOSScanner64.dll`'i bulur
(`java.library.path` varsayılan olarak `PATH`'tir). JavaPOS API'si ve yapılandırma yükleyicisi, SDK olsun ya da
olmasın, her zaman `lib\javapos-*.jar`'dan gelir.

**`run.bat` SDK'nın kendi JavaPOS jar'ını asla eklemez**, yani `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`'ı.
Bir zamanlar ekliyordu ve API'nin iki kopyasından kaçınmak için `lib\javapos-*.jar`'ı dışarıda bırakıyordu —
ama manifest `Class-Path`'i onları yine de yükledi (yukarıya bakın), böylece JVM ikisini birden aldı: `lib\`'ten
4.x yükleyici sınıfları ve yanlarında `javaPOS114.jar`'ın `jpos/res/jpos.properties` ve `jcl.xsd` dosyaları.
Özellikler dosyası [`JposConfig`](#tarayıcı-entegrasyonu)'in kenara çekilmesine yol açar ve o `jcl.xsd` yalnızca
1.10'a kadarki JavaPOS sürümlerini kabul eder; bu yüzden `jpos.xml` içindeki her girdi (1.12–1.14) doğrulamada
başarısız oldu, kayıt defteri boş kaldı ve tarayıcıya daha ulaşılmadan her bağlantı 109 hatasıyla başarısız
oldu. Bunun için o jar'ın sınıf yolunda bulunması yeterlidir, `lib\`'in jar'larının arkasında olsa bile.
Zebra'nın hizmet jar'ları `lib\`'teki 1.14 API'si üzerinde değişmeden çalışır — açma, sahiplenme ve tarama bu
şekilde gerçek bir tarayıcıyla doğrulanmıştır. `run.bat`'ın yanına elle bırakılan DLL'ler hâlâ dikkate alınır:
betik o zaman `-Djava.library.path=<dist>;%PATH%` ayarlar; `PATH`'i sona ekler çünkü bu özellik varsayılan arama
yolunu tamamen değiştirir.

`mvn javafx:run` bunların hiçbirini yapmaz; dolayısıyla Windows'ta bir geliştirme çalıştırmasından tarayıcı
sahiplenmek, SDK'nın jar'larının sınıf yolunda ve `bin\`'inin `PATH`'te başka yollarla bulunmasını gerektirir;
bunun için `dist/`'ten çalıştırın.

Sınıf yolunun ötesinde betikler `java -jar`'ın yapmadıklarını da üstlenir: herhangi bir çalışma dizininden
çalışabilmek için kendi dizinlerini çözümlemek, `PATH` yerine `JAVA_HOME`'u tercih etmek ve 11 öncesi bir JVM'yi
`UnsupportedClassVersionError` yerine çözümü söyleyen bir cümleyle reddetmek. `GAIA_DESKTOP_OPTS` JVM
seçeneklerini aktarır. `run.bat` `javaw.exe` değil `java.exe` kullanır, böylece konsol açık kalır — tarayıcının
çalıştığı tek platform Windows'tur ve durumu ile JavaPOS hataları oraya yazılır.

`run.bat` ayrıca Java'yı, JavaFX'i ve Zebra hizmetlerini bulduklarından seçer — paketlenmiş bir `jdk*\`, modül
yolunda bir `javafx-sdk*\lib\`, `ZEBRA_HOME`'daki Zebra Scanner SDK'sı — SDK'nınkini kullandığında `lib\`'teki
JavaFX kopyalarını sınıf yolunun dışında bırakır ve JDK'nın SAX ayrıştırıcısını, Windows proxy'lerini ve güven
deposunu, Java 17+ üzerinde de `--enable-native-access`'i geçirir. Tablo README'deki
[What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) bölümündedir. Dosyanın CRLF satır
sonlarını koruyun: `cmd.exe` yalnızca LF içeren bir toplu iş dosyasında bir `call :label`'ı kaçırabilir. `run.sh`
de `--enable-native-access=ALL-UNNAMED` geçirir — JavaFX kendi yerel kitaplıklarını `lib/`'ten yükler ve JavaFX
sınıf yolundayken onu kapsayan modül `ALL-UNNAMED`'dir — `run.bat`'ta olduğu gibi yalnızca Java sürümü
okunamadığında atlanır.

Her iki düzen de **tek bir platformun**, `${fx.platform}`'un, JavaFX yerel kitaplıklarını taşır; dolayısıyla
hiçbiri işletim sistemleri veya mimariler arasında taşınabilir değildir. Her hedef için bir kez derleyin —
[Platform başına bir derleme](#platform-başına-bir-derleme)'ye bakın.

> **Eclipse.** `copy-dependencies` yürütmesi bir `<?m2e ignore?>` talimatı taşır. m2e açık bir çalışma alanı
> projesini bir jar'a değil, `target/classes` klasörüne çözümler ve `copy-dependencies` paketlenmemiş bir yapıtı
> kopyalamayı reddeder (MDEP-187); bu da `gaia` projesi bununla yan yana açık olduğunda otomatik derlemeyi
> başarısız kılardı. Sonuç olarak yalnızca IDE'de yapılan bir derleme
> `target/dist/gaia-desktop-<version>-<platform>/lib/`'i doldurmaz; jar'ın kendisini kullanmadan önce `mvn package`
> çalıştırın.

---

## Mimari

| Paket | İçerik |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (pencere), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (JavaFX'ten bağımsız oluşturma mantığı), `AiPair` (bir düzenleyici satırı) |
| `desktop.parse` | `ParseOptions` (gözlemlenebilir `ParseConfig`), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — rapor oluşturucu, JavaFX'ten bağımsız ve araç takımı olmadan birim testli |
| `desktop.i18n` | `UiStrings` — etiket kataloğu |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### Girdi hunisi

Her girdi — yazılan, yapıştırılan, dosyadan açılan, taranan veya oluşturulan — ayrıştırıcıya tek bir yöntemden,
`MainView.capture(String, ScanSource, String)`'den geçerek ulaşır.

Uygulamanın etrafında kurulduğu tek tasarım kararı budur. Bir oluşturma işleminin kendine ait bir rapora, kendine
ait bir geçmişe veya kendine ait bir dışa aktarma yoluna ihtiyacı yoktur, çünkü huniden sonra oluşturulmuş bir
dize taranmış olandan ayırt edilemez. Girdi üreten yeni herhangi bir şey onun yanından değil, içinden geçmelidir.

### Thread kullanımı

| Thread | Çalıştırdığı |
|---|---|
| JavaFX uygulama thread'i | Tüm ayrıştırma, çizim ve UI. AI tabloları ısındıktan sonra ayrıştırma bir milisaniyenin altındadır, dolayısıyla bir işçiye gerek yoktur |
| `gaia-scanner` | Tarayıcıya yapılan her çağrı — açma, sahiplenme, etkinleştirme, devre dışı bırakma, bırakma, kapatma. `ScannerService`'in sahip olduğu uzun ömürlü tek bir thread; [Tarayıcı entegrasyonu](#tarayıcı-entegrasyonu)'na bakın |
| JavaPOS olay thread'i | `ScannerService` geri çağrıları. `MainView.FxScannerListener` her birini `Platform.runLater` ile aktarır |
| Arka plan | Tarayıcının `connect()` / `disconnect()` işlemini beklemek (sahiplenme 5 saniyeye kadar sürebilir), başlangıçta kaydedilmiş geçmişi geri yüklemek — ki bu aynı zamanda ayrıştırıcıyı ısıtır — ve güncelleme denetimi (`gaia-update-check`, tek bir GitHub isteği, sonuç `Platform.runLater` ile geri gönderilir) |

**Bir denetimin seçimini asla kendi seçim olayının içinden değiştirmeyin, denetimi de oradan yok etmeyin.** Bu
uygulamadaki üç ayrı çökme tam olarak bundan kaynaklandı: AI seçicisini açılır pencerenin kendi
`clearAndSelect`'i içinde sıfırlamak (`ListViewBehavior`'dan çıkan bir `IndexOutOfBoundsException`) ve dil açılır
kutusuna sahip paneli o kutunun kendi işleyicisi içinden yeniden kurmak. İkisi de artık `Platform.runLater` ile
erteleniyor ve nedenleri her iki çağrı noktasında da kayıtlı.

### Yeniden kurma ve yeniden etiketleme

Dil değişikliği arayüz çerçevesini — menüler, araç çubuğu, seçenekler paneli — katalogdan **yeniden kurar**; bu
güvenlidir, çünkü hiçbiri durum tutmaz: `OptionsPane`, `ParseOptions`'ın iki yönlü bir görünümüdür, dolayısıyla
değerler modelde yaşar ve yeni bir panel onları hemen geri alır.

**Kullanıcı verisi** tutan denetimler alanlardır ve bunun yerine yerinde yeniden etiketlenir:
`BuilderPane.retext(UiStrings)` AI satırlarını, Digital Link alanlarını ve seçimi korur. Onu yeniden kurmak
süren işi atardı — bir zamanlar da öyle yapıyordu.

---

## {GS} belirteci

Bir JavaFX `TextField`'ı `0x20`'nin altındaki her karakteri sessizce atar (`TextInputControl.filterInput`).
"Garip görüntüler" değil — siler. Yazma, yapıştırma, `setText` ve `insertText` hepsi etkilenir; dolayısıyla
`setText("01…" + GS + "10LOT")` sonucunda `"01…10LOT"` elde edilir: anlamı sessizce değişmiş bir öğe dizesi,
çünkü değişken uzunluklu AI artık ardından geleni de yutar.

`desktop.parse.GsToken` cevabın tamamıdır. Ayırıcı UI içinde `{GS}` düz metni olarak dolaşır ve ayrıştırıcıya
giderken yeniden `0x1D` olur. Belirteç burada icat edilmemiştir — çekirdeğin `ASCIIRevealerUtils.printable`'ının
`0x1D`'yi gösterdiği biçimin ta kendisidir; bu yüzden bir rapordan kopyalanan değer doğrudan girdi çubuğuna geri
yapıştırılabilir.

Girdi alanındaki bir `TextFormatter` filtresi, metnin gelebileceği her yolu yakalar — yazma, ⌘V, bağlam menüsü,
sürükle-bırak, `setText` — yeniden yazmanın her işleyicide değil de orada durmasının nedeni budur.

Tarayıcı yolu asla `GsToken`'dan geçmez: taramalar ayrıştırıcıya bayt olarak ulaşır.

---

## Tarayıcı entegrasyonu

`ScannerService`, JavaPOS `Scanner` denetiminin sahibidir: açma → sahiplenme → etkinleştirme; her taramadan sonra
veri olayını yeniden kurar. Mantıksal aygıt adları `src/main/resources/jpos.xml`'den gelir.

**Her aygıt çağrısı, hizmet yaşadığı sürece yaşayan tek bir thread'de, `gaia-scanner`'da çalışır** — asla FX
thread'inde değil ve asla çağrıdan sonra sona eren bir thread'de değil. Zebra'nın hizmeti tarayıcıya JNI ile
CoreScanner COM API'si üzerinden ulaşır ve COM, kurduğu şeyi çağıran thread'in apartmanına bağlar: sonra sona eren
bir thread'de açarsanız tarayıcı sahiplenilir ama sessiz kalır; başka bir thread'de kapatırsanız yanlış apartmanın
başlatması geri alınır. FX thread'inde kapatmak en kötü durumdur, çünkü JavaFX orada `OleInitialize` çağırır ve
her pencerenin bırakma hedefini ona göre kaydeder — orada bir `CoUninitialize` uygulama genelinde sürükle-bırakı
öldürür. Bu, belirli bir JavaFX sürümü değil, Windows'taki JavaFX'tir. `connect()` ve `disconnect()` o thread'e
iş gönderip bekler, bu yüzden `MainView` ikisini de kısa ömürlü bir arka plan thread'inden çağırır; desen,
sürükle-bırak hatasının ilk bulunduğu yer olan görüntüleyicinin `JposScannerSession`'ından gelir.

İki kural, sorunlu bir aygıtın bağlantıyı kilitlemesini önler. **Kapatma işlemi asla erken durmaz:**
`closeQuietly` devre dışı bırakma, bırakma ve kapatmayı ayrı adımlar olarak yapar, yalnızca sahiplenme
tutuluyorsa bırakır ve her zaman `close()`'u dener — başarısız bir sahiplenmeden sonra `release()` "not claimed"
fırlatır ve adımlar tek bir `try`'ı paylaştığında bu, kapatmayı atlayıp aygıtı Zebra'nın hizmetinde açık
bırakıyordu. **Veri olayı bir `finally` içinde yeniden kurulur:** JavaPOS onu her olaydan sonra devre dışı
bırakır, dolayısıyla istisna fırlatan bir okuma veya işleyici de yeniden kurmalıdır; aksi halde tarayıcı yeniden
bağlanana kadar sessiz kalır. `ScannerLifecycleTest` ikisini de betiklenmiş bir `jpos.Scanner`'a karşı sınar.

**`JposConfig`, ilk açmadan önce JavaPOS'u paketlenmiş `jpos.xml`'e yönlendirir.** Maven JavaPOS jar'larıyla
(`javapos-config-loader` 4.x) ve sınıf yolunda `jpos/res/jpos.properties` yokken, aksi halde her açma
*"Service does not exist in loaded JCL registry"* (hata 109) ile başarısız olur, üç ayrı nedenle: hizmet
yöneticisi `SimpleRegPopulator`'a geri düşer, bu da serileştirilmiş bir `jpos.cfg` okur ve asla `jpos.xml`
okumaz; XML yükleyicisi `jpos.xml`'i yalnızca çalışma dizininde arar, jar'ın içinde değil; ve `lib/`'teki Xerces
2.12.2, yükleyicinin ayarladığı JAXP `accessExternalDTD` özelliğini reddeder. `JposConfig`,
`jpos.config.regPopulatorClass` (`JavaxRegPopulator`), `jpos.config.populatorFileURL` (jar'ın kendi
`jar:file:…!/jpos.xml`'i) ve `javax.xml.parsers.SAXParserFactory` (JDK'nınki) değerlerini sistem özellikleri
olarak ayarlar — zaten ayarlanmış olanı asla değil, böylece `-Djpos.config.populatorFileURL=file:…` hâlâ başka bir
`jpos.xml` seçebilir — ve sınıf yolunda bir `jpos.properties` varken veya 4.x yükleyici yokken hiçbir şey
ayarlamaz. Zebra SDK'sının `javaPOS114.jar`'ı böyle bir durumdur ve kayıt defterini tamamen bozar; `run.bat`'ın
onu sınıf yolunun dışında tutmasının nedeni budur — [Zebra yerel kitaplıkları](#zebra-yerel-kitaplıkları)'na
bakın. `JposConfigTest`, kayıt defterinin ardından dosyanın tarayıcılarını içerdiğini uçtan uca denetler.

**Aygıt seçicisi o kayıt defterinden üretilir**, kodda bir liste olarak tutulmaz:
`ScannerService.logicalNames()` yüklenmiş girdileri `gaia-scanner` thread'inde okur ve aygıt kategorisi
`Scanner` olanları tutar — `jpos.xml`'deki iki terazi girdisi dışarıda kalır — `pickerOrder` sırasıyla: dosya
tanımlıyorsa önce `ZebraAllScanners`, geri kalanlar alfabetik (kayıt defteri dosya sırasını korumaz). Onu
tanımlamayan bir dosyaya o eklenmez; hiçbir şey yüklenmemişse yalnızca varsayılan sunulur, dolayısıyla seçici
asla boş olmaz. `MainView` varsayılanı hemen gösterir ve kaydedilmiş seçimi koruyarak listeyi bir arka plan
thread'inden doldurur. Böylece `jpos.xml`'e eklenen bir tarayıcı — ya da `-Djpos.config.populatorFileURL` ile
adlandırılan bütün bir dosya — kod değişikliği olmadan seçicide görünür ve `JposConfigTest` seçiciyi paketlenmiş
dosyanın bağımsız bir okumasına karşı denetler. Bu girdilerin adlandırdığı hizmetler Zebra'nın sınıflarıdır
(`com.zebra.jpos.service.scanner.*Factory`) ve yine de sınıf yolunda olmaları gerekir — `run.bat` aracılığıyla
Scanner SDK'sından ya da `lib\`'e bırakılmış Zebra hizmet jar'larından.

Tarama verisi **ISO-8859-1** olarak çözülür; böylece her bayt 1:1 eşlenir ve hem AIM Code ID öneki hem de her
`0x1D` korunur; raporlar UTF-8 olarak yazılır.

Hizmette kullanıcı için metin oluşturan hiçbir şey yoktur. Olaylar `ScannerListener.Event` sabitleri ve değişen
değerler olarak dışarı çıkar ve ifadeyi katalogdan `MainView` sağlar — aksi halde 35 dile çevrilmiş bir
uygulamada durum satırı İngilizce olurdu. Günlük satırları istisnadır ve İngilizce kalır.

### Platform kapısı

`ScannerService.isSupportedPlatform()` tek bir `os.name` testidir ve `MainView` herhangi bir tarayıcı denetimi
kurmadan önce ona sorar: macOS ve Linux'ta ne bir anahtar, ne bir aygıt seçicisi, ne Tarayıcı menüsü (dolayısıyla
<kbd>⌘K</kbd> de yok), ne de bir durum etiketi vardır. Bunlar gizlenmemiştir — hiç oluşturulmazlar.

Zebra JavaPOS hizmetlerini yalnızca Windows için gönderir ve başka yerlerde hiçbir şey erken başarısız olmaz:
jar'lar çözümlenir ve denetim oluşturulur; sonunda eksik yerel katman nedeniyle başarısız olan `open()`'dır,
birkaç saniye ve bir iletişim kutusu sonra.

Kural JavaPOS ile değil, *hizmetlerle* ilgilidir. Platformlar arası bir hizmet gönderen bir satıcı burada
çalışırdı ve kuralı genişletmek yalnızca `supportsJavaPos(String)`'te yapılacak bir değişikliktir — bu yöntem
paket-özeldir ve `os.name` dizelerine karşı test edilir; böylece derlemeyi hangi makine çalıştırırsa çalıştırsın
iki yanıt da kapsanır.

### Sembolojiler

`Symbologies`, JavaPOS'un **78** `SCAN_SDT_*` tarama verisi türünün tümünü görüntüleme adlarına ve iki sözcük
dağarcığının örtüştüğü yerlerde çekirdeğin `DataCarrierType`'ına eşler. İkisi arasında JavaPOS daha ayrıntılıdır
— UPC-A'yı EAN-13'ten ve düz Data Matrix'i GS1 DataMatrix'ten ayırır; AIM ise koca bir aileyi tek bir kod
karakteri altında toplar.

Tablo dışındaki bir tür de `ScannerConst` üzerinde yansıma ile bulunan kullanılabilir bir ad alır, çünkü satıcı
hizmetleri gerçekten de belirtim listesi dışındaki değerleri bildirir. Bir test `ScannerConst`'u dolaşır ve
bildirilmiş herhangi bir türün adı yoksa başarısız olur — bu, gelecekteki bir JavaPOS'un ekleyeceği sabitleri de
yakalar.

Bazı taşıyıcı eşlemeleri tahmin edilmek yerine bilerek `UNKNOWN` bırakılmıştır: düz 2 of 5 (AIM `]R`/`]S`'yi
başlangıç/bitiş çubuğu sayısına göre ayırır, JavaPOS ise bunu bildirmez), dört durumlu posta kodları ve TLC-39.

---

## Yerelleştirme

Etiketler, her dil için bir properties dosyası olarak `src/main/resources/i18n/ui_<CODE>.properties` altında
durur ve çekirdeğin hata kataloglarıyla aynı kodları kullanır (`EN`, `FR`, `PCM`, …). 35'inin tamamı vardır ve
35'inin tamamı özdeş bir anahtar kümesi taşır. Dosyalar, `Properties`'in varsaydığı ISO-8859-1 olarak değil,
**UTF-8** olarak okunur; böylece çeviriler `\uXXXX` kaçışları olarak değil, okunabilir biçimde saklanır.

**Geri dönüş dosya başına değil, anahtar başınadır** — kısmen çevrilmiş bir dilin ilk anahtarı girdiği andan
itibaren işe yaramasını sağlayan budur. Sözleşme İngilizcedir: her anahtar orada bulunmalıdır.

| Katman | Anahtar | Kapsam |
|---|---|---|
| Ortak rapor sözcük dağarcığı, `gaia-demo`'nun `GaiaHtmlReport`'undan alınmış | 31 | 35 dilin tümü |
| Masaüstü çerçevesi — menüler, düğmeler, oluşturucu, seçenekler, durum satırı, rapor düzeni, tarayıcı durumu, iletişim kutusu metinleri | 114 | 35 dilin tümü |

Ortak terimlerden altısı (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) bu uygulamanın
raporunda kullanılmaz. Ortak blok demo sözcük dağarcığının sadık bir kopyası olarak kalsın diye tutulmuşlardır;
dosya başlığı bunu belirtir.

### `--language` argümanı

`GaiaDesktopApp.languageOverride()`, `--language=…`'ı JavaFX'in kendi `getParameters().getNamed()`'i aracılığıyla
okur, `Languages.fromArgument` ile çözümler — katalog kodu, enum adı veya BCP 47 etiketi, büyük/küçük harf
duyarsız — ve `MainView`'e verir.

**Geçersiz kılma dosya için değil, o çalıştırma içindir.** `MainView` yüklediği dili `savedLanguage` içinde tutar
ve `languageChosenInSession` kullanıcının kendisinin bir dil seçtiğini söyleyene kadar *onu* geri yazar; ondan
sonra kullanıcının seçimi her zamanki gibi kalıcı olur. Bu ayrım tasarımın ta kendisidir: bir kiosk profili veya
bir demo kısayolu, makinenin sahibinin yarın açacağı dili yeniden yazmadan İspanyolca açılabilir; seçenekler
panelinde yapılan gerçek bir seçim ise yine kalıcı olur.

Hiçbir kataloğu adlandırmayan bir değer, kabul edilen kodlarla birlikte günlüğe yazılır ve yok sayılır. Bir
kısayoldaki bir yazım hatası yüzünden başlamayı reddetmek daha kötü bir başarısızlık olurdu ve
`Languages.fromArgument`'ın istisna fırlatmak yerine `null` döndürmesi de aynı nedenledir.

### Dil ekleme

Kod değişikliği gerekmez.

1. `ui_<CODE>.properties`'i düzenleyin — her dilin zaten bir dosyası var.
2. Değerleri çevirin. `{0}` / `{1}` yer tutucularını yerinde bırakın.
3. Derleyin. İki test sözleşmeyi uygular: biri kod İngilizcenin tanımlamadığı bir anahtar isterse, diğeri
   herhangi bir kataloğun anahtar kümesi İngilizceden saparsa başarısız olur.

İlk test sınıflar üzerinde yansıma yapmak yerine **kaynak kodda** anahtar sabitlerini tarar, çünkü anahtarlar
yalnızca çağrı noktalarında dize sabitleri olarak bulunur. Bu yüzden yeni bir mesaj bir değişken aracılığıyla
aranmak yerine anahtar satır içinde yazılarak oluşturulmalıdır — `strings.format("st.scannerError", …)` — yoksa
test onu göremez.

### Bilerek çevrilmeyenler

- ham değerlerin yanında eş aralıklı yazıyla gösterilen kısa alan adları — öğe kartındaki `format`, `position`,
  `length`, `raw` ve `part`, şeritteki `AI`, `sep`, `pos` ve `len` ve bir hatanın yerini belirten dört hücre
  (`AI`, `position`, `level`, `stage`); bunların değerleri zaten çevrilmemiş enum adlarıdır;
- GS1 yapılarını adlandıran örnek girdi adları ve özel ad olan semboloji adları;
- `GE-C084` düzeltme paragrafı — yarım bir çevirinin İngilizceden daha kötü olduğu standart metni;
- `ElementBuilder`'ın bilinmeyen AI için son savunma hattı; UI'dan ulaşılamaz: düzenleyici bilinmeyen AI'ları
  kendisi denetler ve katalogdan bildirir;
- günlük satırları.

Bir boşluk kalıyor: `ScanSource.label()`, geçmiş satırına ve rapor başlığına `manual` / `scanner` / `file` /
`built` değerlerini İngilizce olarak sağlar. Bunu kapatmak bir anahtardan fazlasını gerektirir — `HistoryCell`'in
bir `UiStrings`'i yoktur ve listenin hücre fabrikası bir kez kurulur; dolayısıyla dil değiştiğinde hücrelerin de
yenilenmesi gerekir.

---

## Saklanan durum biçimleri

`~/.gaia-desktop/` altında iki dosya; farklı tempolarda değiştikleri için ayrı tutulurlar — bozuk veya elle
düzenlenmiş bir yakalama günlüğü kullanıcının ayarlarına mal olmamalıdır. Dizini `-Dgaia.desktop.home=…` ile
geçersiz kılın; testlerin gerçek ana dizinden uzak durması da böyle sağlanır.

**`options.properties`** — bir seçenek değiştiğinde ve çıkışta yazılır.

| Anahtar | Anlamı |
|---|---|
| `parse.mode`, `parse.language` | İşlem hattı derinliği ve mesaj dili |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Tarih gösterimi |
| `check.skipRequires`, `check.skipExcludes` | Yapısal denetimden kaçışlar |
| `ui.theme` | `LIGHT` veya `DARK` |
| `ui.optionsPane` | Seçenekler paneli açık mı? |
| `ui.scannerDevice` | Son JavaPOS mantıksal adı |
| `ui.mode` | `PARSE`, `BUILD` veya `COMBINE` |
| `update.check` | Günlük güncelleme denetiminin çalışıp çalışmadığı (varsayılan `true`) |
| `update.lastCheck` | En son ne zaman çalıştığı, ISO-8601; yoksa veya okunamıyorsa denetim zamanı gelmiş demektir |
| `update.skipVersion` | Kullanıcının *Bu sürümü atla*'yı seçtiği sürüm; bir daha otomatik olarak sunulmaz |

Eksik veya hasarlı bir dosya asla ölümcül değildir: her değer ayrı ayrı geri döner, dolayısıyla bozuk bir satır
yalnızca o satıra mal olur. İlk başlatmada, hiç dosya yokken, seçenekler GAIA'nın yerleşik varsayılanlarından
değil, `SystemDefaults` tarafından makinenin bölgesel ayarlarından doldurulur — yerel ayarın kısa tarih kalıbı
bileşen sırasını, ayırıcıyı ve ay ile yılın kısaltılıp kısaltılmadığını verir.

`Properties.setProperty`'ye asla null vermeyin: istisna fırlatır ve `persistOptions()` istisnanın FX thread'ine
kaçtığı UI olay işleyicilerinden çalışır. Bu, uygulamanın daha önce yaşadığı gerçek bir çökmedir.

**`captures.tsv`** — geçmiş değiştiğinde ve çıkışta yazılır. En yeni önce, en fazla **500** girdi.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Yalnızca yeniden hesaplanamayan saklanır. `ParseResult` **serileştirilmez** — yüklemede girdi yeniden
ayrıştırılarak yeniden türetilir; böylece geri yüklenen geçmiş *şu anda* geçerli olan seçenekleri yansıtır.
Girdiler sıklıkla FNC1'i ve bir tarayıcının yaydığı başka her şeyi içerir; bu yüzden girdi alanı kaçışlanır
(`\\`, `\t`, `\n`, `\r` ve `0x20`'nin altındaki diğer her şey için `\xNN`) ve dosya ISO-8859-1 olarak, bayt bayt
okunup yazılır.

Tarih kaydedilmeye başlanmadan önce yazılmış dosyalar yalnızca bir saat içerir; yine de yüklenirler ve bugünün
tarihini alırlar, çünkü gerçek tarih hiç yazılmamıştır.

---

## Test

```bash
(cd gaia-desktop && mvn test)
```

96 test, bilerek **JavaFX araç takımından bağımsız**; bu da onları hızlı ve başsız (headless) tutar. Rapor
oluşturucuyu, oluşturma mantığını ve AI aramalarını, `{GS}` dönüşümünü, yerel ayardan türetilen varsayılanları,
iki depoyu da (`0x1D` içeren bir gidiş-dönüş ve bilerek bozulmuş dosyalar dahil), semboloji tablosunu, tarayıcı
platform kuralını, sürüm taslağını ve katalogları kapsarlar.

Üçü bilinmeye değer, çünkü işlevleri değil sözleşmeleri korurlar:

| Test | Başarısız olduğu durum |
|---|---|
| `CatalogueCoverageTest` | Kod, `ui_EN.properties`'in tanımlamadığı bir anahtar ister ya da 35 dosyadan herhangi biri İngilizce anahtar kümesinden sapar |
| `SymbologiesTest` | JavaPOS'un bildirdiği herhangi bir `SCAN_SDT_*` sabitinin görüntüleme adı yoktur — bir JavaPOS yükseltmesinin eklediği de dahil |
| `ScannerServiceTest` | Platform kuralı Windows için ya da başka herhangi bir şey için cevabını değiştirir |

`UiStrings` çalışma zamanında bilerek hoşgörülüdür — bilinmeyen bir anahtar, düzenin ortasında istisna
fırlatmak yerine anahtarın kendisi olarak gösterilir — katalog testinin var olması gerekmesinin nedeni tam olarak
budur.

**Kapsanmayanlar:** canlı bir araç takımı gerektiren her şey — odak davranışı, mod anahtarı, tablo düzenleme, dil
değiştirme ve [Thread kullanımı](#thread-kullanımı) altında anlatılan iki çökme. Bunlar gerçek pencere
kullanılarak doğrulandı, ama bunlardaki bir gerileme derlemeyi başarısız kılmaz. TestFX veya Monocle eklemek bu
boşluğu kapatır.

---

## Derleme sorunlarını giderme

| Belirti | Neden ve çözüm |
|---|---|
| `package tools.pantheum.gaia does not exist` | Çekirdek kurulu değil. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | Jar'ın `Main-Class`'ı `GaiaDesktopApp` değil, `Launcher` olmalıdır |
| Başlangıçta *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* | Beklenen ve zararsız — JavaFX sınıf yolunda olduğunu not ediyor. `Launcher`'ın var olma nedeni tam da budur |
| Jar başlamıyor, `NoClassDefFoundError` | Varsayılan jar yalnızca bu modülün sınıflarını taşır; `dist/lib/` yanında olmalıdır. Kendi kendine yeten tek bir dosya için `-Pfat` kullanın |
| Eclipse'te `Artifact has not been packaged yet … MDEP-187` | m2e çalışma alanı çözümlemesi. [Paketleme](#paketleme) altındaki nota bakın |
| Derlemede yinelenen `… 2.properties` dosyaları görünüyor | Proje iCloud ile eşitlenen bir klasörde ve eşitleme derleme sırasında çakışma kopyaları üretti. Her zaman `mvn clean package`; daha iyisi, `target/`'ı eşitlemenin dışında tutun |
| Bir menü öğesi `act.hri` gibi bir anahtar gösteriyor | O anahtar `ui_EN.properties`'te yok. Derlemenin bunu yakalaması gerekirdi — [Test](#test)'e bakın |

---

## Bilinen sınırlamalar ve açık işler

- **Başsız UI testi yok.** [Test](#test)'e bakın.
- **Platforma özgü yapıtlar.** İki paketleme de tek bir platformun JavaFX yerel kitaplıklarını içerir; her hedef
  için `-Dfx.platform` ile bir tane derleyin (herhangi bir makine hepsini derleyebilir). Hiçbiri bir Java çalışma
  zamanı içermez — bu `jpackage` gerektirir ve o yalnızca üzerinde çalıştığı işletim sistemi için derler.
- **Tarayıcı girdisi yalnızca Windows'tadır.** [Platform kapısı](#platform-kapısı)'na bakın.
- **`ScanSource.label()` yerelleştirilmemiştir.** [Bilerek çevrilmeyenler](#bilerek-çevrilmeyenler)'e bakın.
- **`UiState.mode` bir `String`'dir** (`"PARSE"` / `"BUILD"`), yanındaki `Theme` ise bir enum'dur; bu yüzden
  sabit birkaç yerde geçer. Bir enum bunları ortadan kaldırırdı.
- **Digital Link hedefi yalnızca alan adı ve yol önekidir.** `BuilderDigitalLinkConfig` ek sorgu parametrelerini
  ve bir parçayı da destekler; düzenleyici bunları sunmaz.
- **Karşılıklı olarak dışlayan AI çiftleri (`GE-S006`) düzenleyicide işaretlenmez**; bilinmeyen ve yinelenen
  AI'lar işaretlenir — oluşturma işlemi düpedüz başarısız olur.
- **Çekirdeğin hata kataloğu tırnakları ikiler** (`value ''09506000134350''`); bu, oluşturma hatalarında ve
  raporda olduğu gibi görünür. Geri alınmayan bir `MessageFormat` tırnak kaçışına benziyor; düzeltmenin yeri
  çekirdektir.
