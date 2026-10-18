# GAIA Desktop — Panduan Pengembang

Bagaimana modul ini dibangun, disusun, dan diuji. Untuk apa yang dilakukan aplikasi dilihat dari luar,
lihat [Panduan Pengguna](GaiaDesktop-User-Indonesian.md). Dokumen asli:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Daftar Isi

1. [Sekilas tentang modul](#sekilas-tentang-modul)
2. [Membangun dan menjalankan](#membangun-dan-menjalankan)
   - [Pengemasan](#pengemasan)
3. [Arsitektur](#arsitektur)
   - [Corong masukan](#corong-masukan)
   - [Thread](#thread)
   - [Bangun ulang versus label ulang](#bangun-ulang-versus-label-ulang)
4. [Token {GS}](#token-gs)
5. [Integrasi pemindai](#integrasi-pemindai)
   - [Gerbang platform](#gerbang-platform)
   - [Simbologi](#simbologi)
6. [Lokalisasi](#lokalisasi)
   - [Argumen `--language`](#argumen---language)
   - [Menambahkan bahasa](#menambahkan-bahasa)
   - [Yang sengaja tidak diterjemahkan](#yang-sengaja-tidak-diterjemahkan)
7. [Format status tersimpan](#format-status-tersimpan)
8. [Pengujian](#pengujian)
9. [Pemecahan masalah build](#pemecahan-masalah-build)
10. [Keterbatasan yang diketahui dan pekerjaan terbuka](#keterbatasan-yang-diketahui-dan-pekerjaan-terbuka)

---

## Sekilas tentang modul

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Klien dari pustaka inti, tidak lebih. Modul ini tidak menanamkan **pengetahuan GS1 apa pun**: setiap definisi
AI, aturan validasi, pesan kesalahan, dan deskripsi terlokalisasi berasal dari
[`GaiaParser`](../../../../gaia/docs/GaiaParser.md) dan
[`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). Secara khusus, daftar AI tidak pernah disalin ke dalam
modul ini — pemilih di builder membaca registri milik inti sendiri, sehingga tidak mungkin menawarkan AI yang
akan ditolak builder.

**Titik masuk:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (`Application` JavaFX) dan
`tools.pantheum.gaia.desktop.Launcher` (`Main-Class` dari jar yang dikemas).

Modul ini adalah daun: tidak ada yang bergantung padanya, dan modul ini tidak bergantung pada modul hilir lain.

---

## Membangun dan menjalankan

- **Java 21** untuk menjalankan, dan JDK 21 atau yang lebih baru untuk membangun — modul dikompilasi dengan
  `release=21`. JavaFX 21 adalah lini LTS yang sepadan. Jar inti masih berupa bytecode Java 11 dan berjalan
  tanpa perubahan di 21.
- **Maven** di `PATH`. Tidak ada wrapper di repo ini.
- **Inti dipasang lebih dulu.** Modul ini mengambil `gaia:1.1.0` dari `~/.m2`; jar inti yang usang atau tidak
  ada adalah penyebab umum `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` adalah satu-satunya argumen baris perintah, dan satu-satunya cara memengaruhi start-up selain
`-Dgaia.desktop.home=…`, yang mengarahkan seluruh profil ke tempat lain.

`mvn javafx:run` adalah jalur pengembangan: plugin menaruh JavaFX di **module path**, sehingga toolkit dimulai
sebagai modul bernama dan `GaiaDesktopApp` dapat diluncurkan langsung. Jar yang dikemas tidak bisa begitu —
semuanya berada di class path — itulah sebabnya `Main-Class`-nya adalah shim `Launcher`. Subkelas
`Application` sebagai `Main-Class` dalam tata letak itu gagal dengan *"JavaFX runtime components are
missing"*.

Selalu gunakan `clean` — sekali, sebelum membangun sekumpulan platform, karena perintah itu mengosongkan
`target/dist/` dari semuanya. Lihat [Pemecahan masalah build](#pemecahan-masalah-build) untuk alasannya.

### Pengemasan

Semua yang dapat dikirim dirakit di bawah `target/dist/gaia-desktop-<version>-<platform>/` dan tidak ada yang lain, sehingga
direktori itu dapat di-zip atau disalin sebagai satu set tanpa memilah artefak dari `target/` di samping
`classes/`, `maven-status/`, dan sisa berkas sementara build. `${dist.dir}` menamainya satu kali; setiap plugin
yang menulis artefak menunjuk ke sana.

#### Satu build per platform

Jar JavaFX membawa kode native, sehingga setiap distribusi hanya untuk satu platform, dinamai oleh properti
`fx.platform` sebagai classifier OpenJFX: `mac-aarch64`, `mac`, `win`, `linux`, atau `linux-aarch64`. Lima
profil `fx-*` yang diaktifkan menurut OS mengaturnya ke platform mesin yang membangun, dan itu saja yang
dibutuhkan `mvn package` atau `mvn javafx:run` biasa; `-Dfx.platform=…` mengungguli profil-profil itu untuk
membangun platform lain mana pun, dari mesin mana pun — jar JavaFX hanyalah unduhan. `${dist.dir}` adalah
`target/dist/${fx.platform}`, sehingga build satu platform tidak pernah menimpa build platform lain.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Dua detail pom membuat hal itu berhasil, dan keduanya tampak bisa dihapus:

- **Setiap modul JavaFX adalah dependensi langsung** — base, graphics, controls, media, dan web — masing-masing
  dengan `<classifier>${fx.platform}</classifier>` dan pengecualian `org.openjfx:*`. Pom OpenJFX memilih jar
  transitifnya berdasarkan OS *mesin yang membangun*, sehingga membiarkan graphics atau base datang secara
  transitif akan memasukkan native Mac ke dalam build Windows di samping native Windows.
- **Propertinya `fx.platform`, bukan `javafx.platform`.** Jika diatur dari luar, properti milik OpenJFX
  sendiri didefinisikan ulang oleh pom induknya dalam bentuk dirinya sendiri; Maven menolak pom itu sebagai
  tidak valid dan diam-diam membuang jar platform, menyisakan build tanpa JavaFX sama sekali.

Kedua tata letak menulis jalur yang sama, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`, dan tepat satu
di antaranya berjalan per build. `${jar.phase}`, `assembly.skipAssembly`, dan `mdep.skip` adalah yang mencegah
keduanya sama-sama mengklaimnya.

| | Bawaan | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — hanya kelas dan sumber daya modul ini | ~50 MB — dependensi dibongkar ke dalamnya |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 jar, disebut satu per satu di `Class-Path` manifest | — |
| Dikirim | jar **dan** `lib/` di sebelahnya | satu berkas |
| Juga di `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) atau `run.sh` (lainnya) | sama |
| Dan | `readme.html` dan `docs/` (setiap platform) | sama |

Setiap distribusi juga membawa panduan: `docs/` disalin dengan tata letaknya dipertahankan, sehingga tautan
antara panduan dan terjemahan tetap berfungsi, tanpa perkakas terjemahan (`check-i18n.py`,
`i18n-manifest.json`, `i18n-plan.md`); dan `readme.html` di akar, indeks yang menautkan ke masing-masing,
dari `src/main/dist/readme.html` dengan `@VERSION@` diganti versi pom. Keduanya ditulis oleh langkah antrun
`pick-launch-script`, yang menghapus `docs/` lebih dulu agar panduan yang dihapus tidak tertinggal. Tautan di
readme ditulis tangan — panduan yang diganti namanya atau terjemahan yang ditambahkan di bawah `docs/` juga
memerlukan barisnya di sana.

`Class-Path` manifest dan `lib/` adalah dua belahan dari satu hal: jar menyebut
`lib/<artifact>-<version>.jar` untuk setiap dependensi runtime, dan `copy-dependencies` mengisi `lib/` dengan
tepat nama-nama itu. Itulah yang membuat `java -jar` berfungsi.

#### Aplikasi macOS dan zip distribusi

Satu eksekusi antrun, `mac-app-and-zip`, mengakhiri fase `package`. Karena alasan itulah plugin antrun
dideklarasikan **setelah** plugin jar dan assembly — Maven menjalankan eksekusi dalam satu fase menurut urutan
deklarasi plugin — sehingga jar sudah ada, dalam pengemasan mana pun, ketika eksekusi itu berjalan. (Eksekusi
lainnya, `pick-launch-script`, berada di `prepare-package` dan tidak terpengaruh.)

Untuk `mac` dan `mac-aarch64`, eksekusi ini pertama-tama membangun `GAIA Desktop.app` di direktori
distribusi: `Info.plist` (dengan `@VERSION@` diganti versi pom), peluncur `Contents/MacOS/gaia-desktop`
(dibuat dapat dieksekusi), dan `gaia-desktop.icns`, semuanya dari `src/main/macos/`, lalu **memindahkan**
`gaia-desktop.jar` dan `lib/` ke `Contents/Java`, sehingga bundel dapat berdiri sendiri. `run.sh` memeriksa
di sana bila tidak ada jar di sebelahnya. Peluncur mencari Java berdasarkan versi (`java_home -v 21+`, lalu
openjdk dari Homebrew — peluncuran dari Finder tidak melihat `JAVA_HOME` atau `PATH`), menampilkan dialog
melalui `osascript` bila tidak ada, melakukan `cd` ke `~/.gaia-desktop` agar `logs/` relatif milik logback
dapat ditulisi, dan meneruskan `-Xdock:name`/`-Xdock:icon`. macOS mengaitkan `java` yang berjalan dengan
bundel (bundle id dan ikon Dock-nya milik aplikasi); bilah menu menampilkan GAIA Desktop.

Kemudian, untuk setiap platform, eksekusi ini men-zip direktori menjadi
`target/dist/gaia-desktop-<version>-<platform>.zip` di bawah folder bernama sama, dengan `<zip>` milik Ant:
`run.sh` dan `Contents/MacOS/**` sebagai file set terpisah dengan `filemode="755"`, karena jika tidak keduanya
tidak dapat dijalankan setelah diekstrak. Zip ini **tidak dilampirkan**: zip ini untuk rilis GitHub, dan empat
platform berasal dari empat build, yang tidak akan pernah bisa diterima Maven Central — satu unggahan tetap
per versi — sebagai satu rilis.

`project.build.outputTimestamp` diatur ke waktu build. Jika tidak, pengarsip di maven-jar-plugin 3.5 dan
maven-assembly-plugin 3.8 memberi tanggal 1 Februari 1980 pada setiap entri.

Ikon aplikasi digambar oleh `tools/icon/GaiaIcon.java` (bukan bagian dari build; kepalanya memuat langkah
`sips`/`iconutil`). Keluarannya di-commit: `src/main/macos/gaia-desktop.icns` untuk bundel, dan
`src/main/resources/icons/gaia-desktop-{16..256}.png`, yang ditetapkan `GaiaDesktopApp` sebagai ikon jendela
(Windows dan Linux; macOS mengabaikan ikon jendela).

#### Skrip peluncur

`run.sh` dan `run.bat` berasal dari `src/main/scripts/`, disalin ke `dist/gaia-desktop-<version>-<platform>/` tanpa
filter — `run.sh` penuh ekspansi shell `${...}` yang bebas ditulis ulang oleh filter Maven. Sebuah langkah
antrun kemudian hanya menyimpan skrip yang dijalankan platform itu — `run.bat` untuk `win`, `run.sh` untuk
yang lain — dan mengatur bit eksekusi pada `run.sh`, karena `copy-resources` tidak membawanya. Keputusan
diambil dari `${fx.platform}` yang sudah terselesaikan, bukan melalui profil, karena aktivasi profil tidak
dapat melihat nilai yang diatur profil lain: kalau tidak, build Linux yang dibuat di Windows akan mendapat
skrip milik host.

Skrip-skrip ini **tidak bergantung pada manifest**. Masing-masing menyusun class path sendiri,
`gaia-desktop.jar` diikuti setiap `.jar` di `lib/`, disebut satu per satu alih-alih diserahkan ke wildcard
`lib/*` agar jalur yang mengandung spasi tetap menjadi satu entri dan hitungan yang ditambahkan nyata.
Peluncuran dengan `-cp` adalah alasan skrip menyebut `tools.pantheum.gaia.desktop.Launcher` secara eksplisit:
`-cp` tidak membaca `Main-Class` manifest. Namun ia **tetap** mengikuti `Class-Path`-nya — jar di `-cp`
membawa jar yang disebut manifest-nya, yang dicari tepat setelahnya — sehingga setiap jar `lib/` dimuat entah
skrip mencantumkannya atau tidak, dan menghilangkan satu dari class path skrip tidak menjauhkannya dari JVM.
Skrip tetap mencantumkan semuanya, agar jar yang manifest-nya usang, disunting tangan, atau kurang satu entri
tetap bisa dijalankan. Skrip yang sama melayani kedua pengemasan: jar `-Pfat` tidak punya `lib/` untuk
ditambahkan dan tidak membutuhkannya, yang juga menjadi alasan `lib/` yang kosong hanya berupa catatan,
bukan kesalahan.

#### Pustaka native Zebra

Build tidak mengirim apa pun milik Zebra — tidak ada DLL, tidak ada jar layanan. Di Windows semuanya berasal
dari Scanner SDK Zebra yang terpasang di mesin: `run.bat` menambahkan jar layanan `Jpos*.jar` dan `bin\` ke
class path dan `bin\` ke `PATH`, tempat `System.loadLibrary` menemukan `CSJPOSScanner.dll` /
`CSJPOSScanner64.dll` (`java.library.path` secara bawaan sama dengan `PATH`). API JavaPOS dan pemuat
konfigurasinya selalu berasal dari `lib\javapos-*.jar`, dengan atau tanpa SDK.

**`run.bat` tidak pernah menambahkan jar JavaPOS milik SDK sendiri**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`.
Dulu ia melakukannya, sambil menghilangkan `lib\javapos-*.jar` untuk menghindari dua salinan API — tetapi
`Class-Path` manifest tetap memuatnya (lihat di atas), sehingga JVM mendapat keduanya: kelas pemuat 4.x dari
`lib\`, dengan `jpos/res/jpos.properties` dan `jcl.xsd` milik `javaPOS114.jar` di sebelahnya. Berkas properti
itu membuat [`JposConfig`](#integrasi-pemindai) menyingkir, dan `jcl.xsd` itu hanya menerima versi JavaPOS
hingga 1.10, sehingga setiap entri di `jpos.xml` (1.12–1.14) gagal validasi, registri tetap kosong, dan setiap
koneksi gagal dengan kesalahan 109 sebelum pemindai sempat dijangkau. Keberadaan jar itu di class path saja
sudah cukup untuk itu, bahkan di belakang jar `lib\`. Jar layanan Zebra berjalan tanpa perubahan di atas API
1.14 dari `lib\` — open, claim, dan pindai telah diverifikasi dengan pemindai sungguhan dengan cara itu. DLL
yang ditaruh manual di samping `run.bat` tetap dihormati: skrip lalu mengatur
`-Djava.library.path=<dist>;%PATH%`, menambahkan `PATH` karena properti itu sepenuhnya menggantikan jalur
pencarian bawaan.

`mvn javafx:run` tidak melakukan semua ini, sehingga klaim pemindai dari run pengembangan di Windows
membutuhkan jar SDK di class path dan `bin\`-nya di `PATH` dengan cara lain; jalankan dari `dist/` untuk itu.

Selain class path, skrip-skrip ini menangani yang tidak dilakukan `java -jar`: menyelesaikan direktorinya
sendiri agar berfungsi dari direktori kerja mana pun, mengutamakan `JAVA_HOME` daripada `PATH`, dan menolak
JVM sebelum 11 dengan kalimat yang menyebut solusinya alih-alih `UnsupportedClassVersionError`.
`GAIA_DESKTOP_OPTS` meneruskan opsi JVM. `run.bat` menggunakan `java.exe`, bukan `javaw.exe`, sehingga konsol
tetap terbuka — Windows adalah satu-satunya platform tempat pemindai berjalan, dan status serta kesalahan
JavaPOS-nya ditampilkan di sana.

`run.bat` juga memilih Java, JavaFX, dan layanan Zebra dari apa yang ditemukannya — `jdk*\` yang dibundel,
`javafx-sdk*\lib\` di module path, Scanner SDK Zebra di `ZEBRA_HOME` — menyingkirkan salinan JavaFX di
`lib\` dari class path ketika ia menggunakan milik SDK, dan meneruskan parser SAX milik JDK, proxy dan trust
store Windows, serta `--enable-native-access` di Java 17+. Bagian
[What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) di README memuat tabelnya.
Pertahankan akhir baris CRLF berkas itu: `cmd.exe` dapat melewatkan `call :label` di berkas batch yang hanya
memakai LF. `run.sh` juga meneruskan `--enable-native-access=ALL-UNNAMED` — JavaFX memuat native-nya sendiri
dari `lib/`, dan dengan JavaFX di class path `ALL-UNNAMED` adalah modul yang mencakupnya — dilewati, seperti di
`run.bat`, hanya bila versi Java tidak dapat dibaca.

Kedua tata letak membawa native JavaFX **dari satu platform**, `${fx.platform}`, sehingga tidak ada yang
portabel lintas OS atau arsitektur. Bangun sekali per target — lihat
[Satu build per platform](#satu-build-per-platform).

> **Eclipse.** Eksekusi `copy-dependencies` membawa instruksi `<?m2e ignore?>`. m2e menyelesaikan proyek
> workspace yang terbuka ke folder `target/classes`-nya alih-alih ke jar, dan `copy-dependencies` menolak
> menyalin artefak yang belum dikemas (MDEP-187), yang akan menggagalkan auto-build setiap kali proyek `gaia`
> terbuka di samping proyek ini. Akibatnya, build yang hanya di IDE tidak mengisi
> `target/dist/gaia-desktop-<version>-<platform>/lib/`; jalankan `mvn package` sebelum memakai jar itu sendiri.

---

## Arsitektur

| Paket | Berisi |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (jendela), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (logika build bebas JavaFX), `AiPair` (satu baris editor) |
| `desktop.parse` | `ParseOptions` (`ParseConfig` yang dapat diamati), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — perender laporan, bebas JavaFX dan diuji unit tanpa toolkit |
| `desktop.i18n` | `UiStrings` — katalog label |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### Corong masukan

Setiap masukan — diketik, ditempel, dibuka dari berkas, dipindai, atau dibangun — mencapai parser melalui
satu metode, `MainView.capture(String, ScanSource, String)`.

Itulah satu-satunya keputusan desain yang menjadi poros aplikasi. Sebuah build tidak membutuhkan laporan
sendiri, riwayat sendiri, atau jalur ekspor sendiri, karena setelah melewati corong, string yang dibangun
tidak dapat dibedakan dari string yang dipindai. Apa pun yang baru yang menghasilkan masukan sebaiknya
melewati corong ini, bukan memutarinya.

### Thread

| Thread | Menjalankan |
|---|---|
| Thread aplikasi JavaFX | Semua parsing, rendering, dan UI. Parsing di bawah satu milidetik setelah tabel AI hangat, sehingga tidak perlu worker |
| `gaia-scanner` | Setiap panggilan ke pemindai — open, claim, enable, disable, release, close. Satu thread berumur panjang milik `ScannerService`; lihat [Integrasi pemindai](#integrasi-pemindai) |
| Thread peristiwa JavaPOS | Callback `ScannerService`. `MainView.FxScannerListener` memindahkan masing-masing dengan `Platform.runLater` |
| Latar belakang | Menunggu `connect()` / `disconnect()` pemindai (klaim bisa memakan hingga 5 detik), memulihkan riwayat tersimpan saat start-up — yang sekaligus menjadi pemanasan parser — dan pemeriksaan pembaruan (`gaia-update-check`, satu permintaan GitHub, hasilnya dikirim kembali dengan `Platform.runLater`) |

**Jangan pernah mengubah pilihan sebuah kontrol, atau menghancurkan kontrol itu, dari dalam peristiwa pilihannya
sendiri.** Tiga crash terpisah di aplikasi ini berasal tepat dari hal itu: mengatur ulang pemilih AI di dalam
`clearAndSelect` milik popup itu sendiri (sebuah `IndexOutOfBoundsException` dari `ListViewBehavior`), dan
membangun ulang panel pemilik combo bahasa dari dalam handler combo itu sendiri. Keduanya kini ditunda dengan
`Platform.runLater`, dan alasannya dicatat di kedua tempat pemanggilan.

### Bangun ulang versus label ulang

Perubahan bahasa **membangun ulang** kerangka antarmuka dari katalog — menu, toolbar, panel opsi — yang aman
karena tidak satu pun memiliki status: `OptionsPane` adalah tampilan dua arah dari `ParseOptions`, sehingga
nilai-nilainya tinggal di model dan panel baru langsung mengambilnya kembali.

Kontrol yang menyimpan **data pengguna** adalah field, dan diberi label ulang di tempat:
`BuilderPane.retext(UiStrings)` mempertahankan baris AI, field Digital Link, dan pilihan. Membangunnya ulang
akan membuang pekerjaan yang sedang berlangsung — seperti yang pernah terjadi.

---

## Token {GS}

`TextField` JavaFX diam-diam membuang setiap karakter di bawah `0x20` (`TextInputControl.filterInput`). Bukan
"menampilkannya dengan aneh" — menghapusnya. Mengetik, menempel, `setText`, dan `insertText` semuanya
terpengaruh, sehingga `setText("01…" + GS + "10LOT")` menghasilkan `"01…10LOT"`: string elemen yang maknanya
diam-diam berubah, karena AI berpanjang variabel kini menelan apa yang mengikutinya.

`desktop.parse.GsToken` adalah seluruh jawabannya. Pemisah berjalan melalui UI sebagai teks harfiah `{GS}` dan
kembali menjadi `0x1D` dalam perjalanan ke parser. Token ini bukan ciptaan di sini — ini tepat bentuk yang
dipakai `ASCIIRevealerUtils.printable` milik inti untuk merender `0x1D`, sehingga nilai yang disalin dari
laporan dapat langsung ditempel kembali ke bilah masukan.

Filter `TextFormatter` pada field masukan menangkap setiap jalur yang dapat dilalui teks — mengetik, ⌘V, menu
konteks, seret dan lepas, `setText` — itulah sebabnya penulisan ulang ditempatkan di sana alih-alih di setiap
handler.

Jalur pemindai tidak pernah melewati `GsToken`: hasil pindai mencapai parser sebagai byte.

---

## Integrasi pemindai

`ScannerService` memiliki kontrol `Scanner` JavaPOS: open → claim → enable, mempersenjatai kembali peristiwa
data setelah setiap pindaian. Nama perangkat logis berasal dari `src/main/resources/jpos.xml`.

**Setiap panggilan perangkat berjalan di satu thread, `gaia-scanner`, yang hidup selama layanan hidup** —
tidak pernah di thread FX, dan tidak pernah di thread yang berakhir setelah panggilan. Layanan Zebra
menjangkau pemindai melalui JNI lewat CoreScanner COM API, dan COM mengikat apa yang disiapkannya ke
apartemen thread pemanggil: buka di thread yang kemudian berakhir, maka pemindai terklaim tetapi tetap diam;
tutup di thread lain, maka apartemen yang salah di-uninitialize. Menutup di thread FX adalah kasus terburuk,
karena JavaFX memanggil `OleInitialize` di sana dan mendaftarkan target drop setiap jendela padanya — sebuah
`CoUninitialize` di sana mematikan seret dan lepas di seluruh aplikasi. Ini perilaku JavaFX di Windows, bukan
satu versi JavaFX tertentu. `connect()` dan `disconnect()` mengirim ke thread itu dan menunggu, sehingga
`MainView` memanggil keduanya dari thread latar belakang berumur pendek; polanya berasal dari
`JposScannerSession` milik viewer, tempat kegagalan seret dan lepas pertama kali ditemukan.

Dua aturan mencegah perangkat yang berulah mengunci koneksi. **Pembongkaran tidak pernah berhenti di tengah
jalan:** `closeQuietly` menjalankan disable, release, dan close sebagai langkah terpisah, hanya melakukan
release bila klaim sedang dipegang, dan selalu mencoba `close()` — setelah klaim gagal, `release()` melempar
"not claimed", dan ketika langkah-langkah itu berbagi satu `try`, hal itu melewatkan close dan membiarkan
perangkat terbuka di layanan Zebra. **Peristiwa data dipersenjatai kembali di dalam `finally`:** JavaPOS
menonaktifkannya setelah setiap peristiwa, sehingga pembacaan atau handler yang melempar pengecualian tetap
harus mempersenjatai kembali, atau pemindai akan diam sampai koneksi ulang. `ScannerLifecycleTest` menguji
keduanya terhadap `jpos.Scanner` yang diskenariokan.

**`JposConfig` mengarahkan JavaPOS ke `jpos.xml` yang dibundel** sebelum open pertama. Dengan jar JavaPOS dari
Maven (`javapos-config-loader` 4.x) dan tanpa `jpos/res/jpos.properties` di class path, setiap open akan gagal
dengan *"Service does not exist in loaded JCL registry"* (kesalahan 109), karena tiga alasan terpisah:
service manager kembali ke `SimpleRegPopulator`, yang membaca `jpos.cfg` terserialisasi dan tidak pernah
`jpos.xml`; pemuat XML mencari `jpos.xml` hanya di direktori kerja, bukan di dalam jar; dan Xerces 2.12.2 di
`lib/` menolak properti JAXP `accessExternalDTD` yang diatur pemuat. `JposConfig` mengatur
`jpos.config.regPopulatorClass` (`JavaxRegPopulator`), `jpos.config.populatorFileURL` (`jar:file:…!/jpos.xml`
milik jar sendiri), dan `javax.xml.parsers.SAXParserFactory` (milik JDK) sebagai properti sistem — tidak
pernah yang sudah diatur, sehingga `-Djpos.config.populatorFileURL=file:…` tetap dapat memilih `jpos.xml` lain
— dan tidak mengatur apa pun bila ada `jpos.properties` di class path atau pemuat 4.x tidak ada.
`javaPOS114.jar` milik Zebra SDK adalah kasus seperti itu dan merusak registri sepenuhnya, itulah sebabnya
`run.bat` menjauhkannya dari class path — lihat [Pustaka native Zebra](#pustaka-native-zebra).
`JposConfigTest` memeriksa dari ujung ke ujung bahwa registri kemudian memuat pemindai dari berkas itu.

**Pemilih perangkat dibuat dari registri itu**, bukan disimpan sebagai daftar di kode:
`ScannerService.logicalNames()` membaca entri yang dimuat di thread `gaia-scanner` dan menyimpan yang kategori
perangkatnya `Scanner` — dua entri timbangan di `jpos.xml` tetap di luar — dalam `pickerOrder`:
`ZebraAllScanners` lebih dulu bila berkas mendefinisikannya, sisanya menurut abjad (registri tidak menyimpan
urutan berkas). Berkas yang tidak mendefinisikannya tidak diberi entri itu; bila tidak ada yang dimuat, hanya
bawaan yang ditawarkan, sehingga pemilih tidak pernah kosong. `MainView` langsung menampilkan bawaan dan
mengisi daftar dari thread latar belakang, sambil mempertahankan pilihan tersimpan. Jadi pemindai yang
ditambahkan ke `jpos.xml` — atau seluruh berkas yang disebut dengan `-Djpos.config.populatorFileURL` — muncul
di pemilih tanpa perubahan kode, dan `JposConfigTest` memeriksa pemilih terhadap pembacaan independen atas
berkas yang dibundel. Layanan yang disebut entri-entri itu adalah kelas Zebra
(`com.zebra.jpos.service.scanner.*Factory`), yang tetap harus berada di class path — dari Scanner SDK melalui
`run.bat`, atau jar layanan Zebra yang ditaruh di `lib\`.

Data pindaian didekodekan sebagai **ISO-8859-1** sehingga setiap byte dipetakan 1:1 dan baik awalan AIM Code ID
maupun setiap `0x1D` tetap utuh; laporan ditulis sebagai UTF-8.

Tidak ada bagian layanan yang menyusun teks untuk pengguna. Peristiwa dikirim sebagai konstanta
`ScannerListener.Event` ditambah nilai-nilai yang berubah, dan `MainView` menyediakan kata-katanya dari
katalog — kalau tidak, baris status akan berbahasa Inggris di aplikasi yang diterjemahkan ke 35 bahasa. Baris
log adalah pengecualian dan tetap berbahasa Inggris.

### Gerbang platform

`ScannerService.isSupportedPlatform()` adalah satu uji `os.name`, dan `MainView` menanyakannya sebelum
membangun kontrol pemindai apa pun: di macOS dan Linux tidak ada toggle, tidak ada pemilih perangkat, tidak ada
menu Pemindai (dan karenanya tidak ada <kbd>⌘K</kbd>), dan tidak ada label status. Semua itu tidak
disembunyikan — semua itu tidak pernah dibuat.

Zebra mengirim layanan JavaPOS-nya hanya untuk Windows, dan di tempat lain tidak ada yang gagal lebih awal: jar
terselesaikan dan kontrol terbentuk, dan `open()`-lah yang akhirnya gagal karena lapisan native yang hilang,
beberapa detik dan satu dialog kemudian.

Aturan ini tentang *layanan*, bukan tentang JavaPOS. Vendor yang mengirim layanan lintas platform akan
berfungsi di sini, dan memperluas aturan adalah perubahan pada `supportsJavaPos(String)` saja — yang bersifat
package-private dan diuji terhadap string `os.name` sehingga kedua jawaban tercakup di mesin mana pun yang
menjalankan build.

### Simbologi

`Symbologies` memetakan semua **78** tipe data pindai `SCAN_SDT_*` JavaPOS ke nama tampilan, dan ke
`DataCarrierType` milik inti bila kedua kosakata selaras. JavaPOS adalah yang lebih halus dari keduanya — ia
memisahkan UPC-A dari EAN-13, dan Data Matrix biasa dari GS1 DataMatrix, sementara AIM mengelompokkan satu
keluarga utuh di bawah satu karakter kode.

Tipe di luar tabel tetap mendapat nama yang dapat dipakai, ditemukan melalui refleksi atas `ScannerConst`,
karena layanan vendor memang melaporkan nilai di luar daftar spesifikasi. Sebuah uji menelusuri
`ScannerConst` dan gagal bila ada tipe yang dideklarasikan tanpa nama — yang juga menangkap konstanta yang
ditambahkan JavaPOS di masa depan.

Beberapa pemetaan carrier sengaja dibiarkan `UNKNOWN` alih-alih ditebak: straight 2 of 5 (AIM memisahkan
`]R`/`]S` berdasarkan jumlah batang start/stop, yang tidak dilaporkan JavaPOS), kode pos empat-keadaan, dan
TLC-39.

---

## Lokalisasi

Label disimpan dalam satu berkas properties per bahasa di bawah
`src/main/resources/i18n/ui_<CODE>.properties`, menggunakan kode yang sama dengan katalog kesalahan inti
(`EN`, `FR`, `PCM`, …). Ke-35 berkas ada dan ke-35 berkas membawa set kunci yang identik. Berkas dibaca sebagai
**UTF-8**, bukan ISO-8859-1 yang diasumsikan `Properties`, sehingga terjemahan disimpan dalam bentuk yang dapat
dibaca alih-alih sebagai escape `\uXXXX`.

**Fallback berlaku per kunci, bukan per berkas** — itulah yang membuat bahasa yang baru sebagian diterjemahkan
berguna sejak kunci pertamanya masuk. Bahasa Inggris adalah kontraknya: setiap kunci harus ada di sana.

| Lapisan | Kunci | Cakupan |
|---|---|---|
| Kosakata laporan bersama | 31 | semua 35 bahasa |
| Kerangka desktop — menu, tombol, builder, opsi, baris status, tata letak laporan, status pemindai, isi dialog | 114 | semua 35 bahasa |

Enam istilah bersama (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) tidak dipakai oleh
laporan aplikasi ini.
Istilah-istilah itu sengaja dipertahankan; kepala berkas menyatakan hal itu.

### Argumen `--language`

`GaiaDesktopApp.languageOverride()` membaca `--language=…` melalui `getParameters().getNamed()` milik JavaFX
sendiri, menyelesaikannya dengan `Languages.fromArgument` — kode katalog, nama enum, atau tag BCP 47, tanpa
membedakan huruf besar-kecil — dan menyerahkannya ke `MainView`.

**Penimpaan ini untuk run tersebut, bukan untuk berkas.** `MainView` menyimpan bahasa yang dimuatnya di
`savedLanguage` dan menulis *bahasa itu* kembali, sampai `languageChosenInSession` menyatakan bahwa pengguna
memilih bahasa sendiri; setelah itu pilihannya disimpan seperti biasa. Pemisahan itulah seluruh desainnya:
profil kios atau pintasan demo dapat dibuka dalam bahasa Spanyol tanpa menulis ulang bahasa yang akan dibuka
pemilik mesin besok, sementara pilihan sungguhan yang dibuat di panel opsi tetap bertahan.

Nilai yang tidak menyebut katalog mana pun dicatat di log bersama kode-kode yang diterima lalu diabaikan.
Menolak untuk mulai karena salah ketik di sebuah pintasan adalah kegagalan yang lebih buruk, dan
`Languages.fromArgument` mengembalikan `null` alih-alih melempar pengecualian karena alasan yang sama.

### Menambahkan bahasa

Tidak perlu perubahan kode.

1. Sunting `ui_<CODE>.properties` — setiap bahasa sudah punya berkas.
2. Terjemahkan nilainya. Biarkan placeholder `{0}` / `{1}` di tempatnya.
3. Bangun. Dua uji menegakkan kontrak: satu gagal bila kode meminta kunci yang tidak didefinisikan bahasa
   Inggris, yang lain bila set kunci katalog mana pun menyimpang dari bahasa Inggris.

Uji pertama memindai **kode sumber** untuk mencari literal kunci alih-alih melakukan refleksi atas kelas,
karena kunci hanya ada sebagai literal string di tempat pemanggilan. Itulah sebabnya pesan baru harus dibangun
dengan kunci ditulis langsung — `strings.format("st.scannerError", …)` — alih-alih dicari melalui variabel,
atau uji tidak dapat melihatnya.

### Yang sengaja tidak diterjemahkan

- nama field singkat yang ditampilkan dalam huruf monospace di samping nilai mentah — `format`, `position`,
  `length`, `raw`, dan `part` pada kartu elemen, `AI`, `sep`, `pos`, dan `len` pada pita, dan empat sel yang
  menunjukkan lokasi kesalahan (`AI`, `position`, `level`, `stage`), yang nilainya memang nama enum yang tidak
  diterjemahkan;
- nama contoh masukan, yang menamai konstruksi GS1, dan nama simbologi, yang merupakan nama diri;
- paragraf remediasi `GE-C084` — prosa standar yang terjemahan setengah-setengahnya lebih buruk daripada
  bahasa Inggris;
- penahan terakhir AI tak dikenal milik `ElementBuilder`, yang tidak terjangkau dari UI: editor memeriksa
  sendiri AI yang tidak dikenal dan melaporkannya dari katalog;
- baris log.

Satu celah tersisa: `ScanSource.label()` memasok `manual` / `scanner` / `file` / `built` ke baris riwayat dan
kepala laporan dalam bahasa Inggris. Menutupnya membutuhkan lebih dari sekadar kunci — `HistoryCell` tidak
punya `UiStrings`, dan cell factory daftar dipasang sekali, sehingga sel juga harus disegarkan saat bahasa
berganti.

---

## Format status tersimpan

Dua berkas di bawah `~/.gaia-desktop/`, dipisahkan karena berubah dengan ritme berbeda — log tangkapan yang
rusak atau disunting tangan tidak boleh membuat pengguna kehilangan pengaturannya. Timpa direktorinya dengan
`-Dgaia.desktop.home=…`, yang menjadi cara uji tetap berada di luar direktori home yang sebenarnya.

**`options.properties`** — ditulis ketika sebuah opsi berubah, dan saat keluar.

| Kunci | Arti |
|---|---|
| `parse.mode`, `parse.language` | Kedalaman pipeline dan bahasa pesan |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Tampilan tanggal |
| `check.skipRequires`, `check.skipExcludes` | Pengecualian pemeriksaan struktural |
| `ui.theme` | `LIGHT` atau `DARK` |
| `ui.optionsPane` | Panel opsi terbuka? |
| `ui.scannerDevice` | Nama logis JavaPOS terakhir |
| `ui.mode` | `PARSE`, `BUILD`, atau `COMBINE` |
| `update.check` | Apakah pemeriksaan pembaruan harian berjalan (bawaan `true`) |
| `update.lastCheck` | Kapan terakhir berjalan, ISO-8601; tidak ada atau tidak terbaca berarti pemeriksaan jatuh tempo |
| `update.skipVersion` | Rilis yang dipilih pengguna dengan *Lewati versi ini*; tidak ditawarkan lagi secara otomatis |

Berkas yang hilang atau rusak tidak pernah fatal: setiap nilai jatuh kembali secara terpisah, sehingga satu
baris rusak hanya mengorbankan baris itu dan tidak lebih. Pada peluncuran pertama, tanpa berkas sama sekali,
opsi diisi dari pengaturan regional mesin oleh `SystemDefaults` alih-alih dari bawaan GAIA — pola tanggal
pendek locale memberikan urutan komponen, pemisah, dan apakah bulan dan tahun disingkat.

Jangan pernah memberi `Properties.setProperty` sebuah null: ia melempar pengecualian, dan `persistOptions()`
berjalan dari handler peristiwa UI tempat pengecualian lolos ke thread FX. Itu crash nyata yang sudah pernah
dialami aplikasi ini.

**`captures.tsv`** — ditulis ketika riwayat berubah, dan saat keluar. Terbaru lebih dulu, dibatasi
**500** entri.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Hanya yang tidak dapat dihitung ulang yang disimpan. `ParseResult` **tidak** diserialisasi — ia diturunkan
ulang dengan mem-parsing masukan lagi saat dimuat, sehingga riwayat yang dipulihkan mencerminkan opsi yang
berlaku *sekarang*. Masukan rutin mengandung FNC1 dan apa pun lainnya yang dipancarkan pemindai, sehingga field
masukan di-escape (`\\`, `\t`, `\n`, `\r`, dan `\xNN` untuk semua karakter lain di bawah `0x20`) dan berkas
dibaca serta ditulis sebagai ISO-8859-1, byte demi byte.

Berkas yang ditulis sebelum tanggal dicatat hanya memuat jam; berkas itu tetap dimuat, bertanggal hari ini,
karena tanggal sebenarnya tidak pernah dituliskan.

---

## Pengujian

```bash
(cd gaia-desktop && mvn test)
```

96 uji, sengaja **bebas dari toolkit JavaFX**, sehingga cepat dan headless. Uji-uji ini mencakup perender
laporan, logika build dan pencarian AI, konversi `{GS}`, bawaan yang diturunkan dari locale, kedua penyimpanan
(termasuk round trip yang mengandung `0x1D` dan berkas yang sengaja dirusak), tabel simbologi, aturan platform
pemindai, stub versi, dan katalog.

Tiga di antaranya patut diketahui karena menjaga kontrak, bukan fungsi:

| Uji | Gagal ketika |
|---|---|
| `CatalogueCoverageTest` | Kode meminta kunci yang tidak didefinisikan `ui_EN.properties`, atau salah satu dari 35 berkas menyimpang dari set kunci bahasa Inggris |
| `SymbologiesTest` | Ada konstanta `SCAN_SDT_*` yang dideklarasikan JavaPOS tanpa nama tampilan — termasuk yang ditambahkan oleh upgrade JavaPOS |
| `ScannerServiceTest` | Aturan platform berganti jawaban untuk Windows atau untuk yang lain |

`UiStrings` sengaja pemaaf saat runtime — kunci yang tidak dikenal dirender sebagai kunci itu sendiri alih-alih
melempar pengecualian di tengah tata letak — itulah tepatnya mengapa uji katalog harus ada.

**Tidak tercakup:** apa pun yang membutuhkan toolkit hidup — perilaku fokus, toggle mode, penyuntingan tabel,
peralihan bahasa, dan kedua crash yang dijelaskan di bawah [Thread](#thread). Semua itu diverifikasi dengan
mengoperasikan jendela sungguhan, tetapi regresi di sana tidak akan menggagalkan build. Menambahkan TestFX atau
Monocle akan menutup celah itu.

---

## Pemecahan masalah build

| Gejala | Penyebab dan perbaikan |
|---|---|
| `package tools.pantheum.gaia does not exist` | Inti belum dipasang. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | `Main-Class` jar harus `Launcher`, bukan `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* saat start-up | Diharapkan dan tidak berbahaya — JavaFX mencatat bahwa ia berada di class path. Persis itulah alasan `Launcher` ada |
| Jar tidak mau mulai, `NoClassDefFoundError` | Jar bawaan hanya membawa kelas modul ini; `dist/lib/` harus berada di sebelahnya. Gunakan `-Pfat` untuk satu berkas mandiri |
| `Artifact has not been packaged yet … MDEP-187` di Eclipse | Resolusi workspace m2e. Lihat catatan di bawah [Pengemasan](#pengemasan) |
| Berkas duplikat `… 2.properties` muncul dalam build | Proyek berada di folder yang disinkronkan iCloud dan sinkronisasi menghasilkan salinan konflik di tengah build. Selalu `mvn clean package`; lebih baik lagi, kecualikan `target/` dari sinkronisasi |
| Item menu menampilkan kunci seperti `act.hri` | Kunci itu tidak ada di `ui_EN.properties`. Build seharusnya menangkapnya — lihat [Pengujian](#pengujian) |

---

## Keterbatasan yang diketahui dan pekerjaan terbuka

- **Tidak ada uji UI headless.** Lihat [Pengujian](#pengujian).
- **Artefak khusus platform.** Kedua pengemasan membundel native JavaFX satu platform; bangun satu per target
  dengan `-Dfx.platform` (mesin mana pun dapat membangun semuanya). Tidak ada yang membundel runtime Java —
  itu membutuhkan `jpackage`, yang hanya membangun untuk OS tempat ia berjalan.
- **Masukan pemindai hanya untuk Windows.** Lihat [Gerbang platform](#gerbang-platform).
- **`ScanSource.label()` tidak dilokalisasi.** Lihat [Yang sengaja tidak
  diterjemahkan](#yang-sengaja-tidak-diterjemahkan).
- **`UiState.mode` adalah `String`** (`"PARSE"` / `"BUILD"`) sementara `Theme` di sebelahnya adalah enum,
  sehingga literal itu muncul di beberapa tempat. Sebuah enum akan menghilangkannya.
- **Target Digital Link hanya domain dan awalan jalur.** `BuilderDigitalLinkConfig` juga mendukung parameter
  kueri tambahan dan fragmen; editor tidak menampilkannya.
- **Pasangan AI yang saling eksklusif (`GE-S006`) tidak ditandai di editor** seperti AI yang tidak dikenal dan
  duplikat — build langsung gagal begitu saja.
- **Katalog kesalahan inti menggandakan tanda kutip** (`value ''09506000134350''`), yang muncul apa adanya di
  kegagalan build dan laporan. Tampaknya ini escape tanda kutip `MessageFormat` yang tidak dikembalikan;
  perbaikannya tempatnya di inti.
