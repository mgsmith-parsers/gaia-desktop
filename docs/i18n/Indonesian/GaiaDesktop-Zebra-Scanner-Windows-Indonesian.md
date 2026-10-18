# Penyiapan pemindai Zebra — GAIA Desktop untuk Windows

Cara memasang dan mengonfigurasi pemindai kode batang Zebra agar GAIA Desktop dapat membaca kode
batang GS1 langsung darinya: setiap pindaian diurai dan ditambahkan ke riwayat, dengan awalan
AIM Code ID dan setiap pemisah FNC1 tetap utuh. Dokumen asli:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

GAIA Desktop dibuat dan diuji dengan pencitra genggam **Zebra DS8108** yang tersambung lewat USB.
Langkah-langkahnya berlaku untuk pemindai 2D Zebra mana pun yang didukung Scanner SDK dari Zebra;
kode batang penyiapan dan nama menu untuk model tertentu ada di Quick Reference Guide atau
Product Reference Guide model itu sendiri.

> **Khusus Windows.** Driver pemindai Zebra hanya tersedia untuk Windows, sehingga GAIA Desktop
> tidak membuat kontrol pemindai di macOS atau Linux. Semua hal lain di aplikasi berjalan di
> semua platform.

---

## Daftar isi

1. [Yang Anda perlukan](#1-yang-anda-perlukan)
2. [Memasang Java](#2-memasang-java)
3. [Memasang Zebra Scanner SDK](#3-memasang-zebra-scanner-sdk)
4. [Memasang 123Scan](#4-memasang-123scan)
5. [Memperbarui firmware pemindai Zebra](#5-memperbarui-firmware-pemindai-zebra)
6. [Mengatur mode USB pemindai Zebra](#6-mengatur-mode-usb-pemindai-zebra)
7. [Mengonfigurasi pemindai Zebra untuk GS1](#7-mengonfigurasi-pemindai-zebra-untuk-gs1)
8. [Memasang dan menjalankan GAIA Desktop](#8-memasang-dan-menjalankan-gaia-desktop)
9. [Menyambungkan pemindai](#9-menyambungkan-pemindai)
10. [Referensi konfigurasi](#10-referensi-konfigurasi)
11. [Pemecahan masalah](#11-pemecahan-masalah)
12. [Pemindai 2D Zebra yang didukung](#12-pemindai-2d-zebra-yang-didukung)

---

## 1. Yang Anda perlukan

| | |
|---|---|
| **Pemindai** | Pemindai 2D Zebra, beserta kabel USB-nya — lihat [Pemindai 2D Zebra yang didukung](#12-pemindai-2d-zebra-yang-didukung) |
| **Komputer** | Windows 10 atau 11, 64-bit |
| **Java** | Java 21 atau yang lebih baru, **64-bit** |
| **Zebra Scanner SDK for Windows** | **64-bit**, dari situs dukungan Zebra — menyediakan driver dan layanan JavaPOS yang dipakai GAIA Desktop |
| **Zebra 123Scan** | Utilitas konfigurasi gratis dari Zebra, dari situs dukungan Zebra — dipakai untuk memperbarui firmware pemindai dan memeriksa pengaturannya |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Opsional** | **Product Reference Guide** (PRG) pemindai, yang memuat kode batang penyiapan |

Bagaimana semuanya terhubung:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop membawa sendiri API JavaPOS dan konfigurasi yang menamai pemindai (`jpos.xml`, di
dalam `gaia-desktop.jar`). Zebra SDK menyediakan sisanya: driver yang berkomunikasi dengan pemindai
dan kelas layanan Zebra yang berada di antara JavaPOS dan driver tersebut.

---

## 2. Memasang Java

GAIA Desktop memerlukan **Java 21 atau yang lebih baru**, dan harus versi **64-bit**, agar cocok
dengan driver Zebra 64-bit.

1. Pasang JDK atau JRE Java 21 untuk Windows x64 — misalnya Eclipse Temurin dari
   adoptium.net. Biarkan penginstal mengatur `JAVA_HOME` jika ditawarkan.
2. Periksa di Command Prompt:

   ```
   java -version
   ```

   Baris pertama harus menunjukkan versi 21 atau lebih baru; Java 64-bit juga mencetak `64-Bit Server VM`.

`run.bat` mencari Java dengan urutan ini: folder `jdk*` di samping `run.bat`, lalu `JAVA_HOME`,
lalu `java` di `PATH`. Versi yang lebih lama dari 21 ditolak dengan pesan yang menjelaskannya.

---

## 3. Memasang Zebra Scanner SDK

1. Unduh **Zebra Scanner SDK for Windows (64-bit)** dari situs dukungan Zebra.
2. Jalankan penginstal. Jika ada pilihan komponen, pastikan komponen **JPOS** (JavaPOS)
   disertakan.
3. Pertahankan lokasi pemasangan bawaan. GAIA Desktop mencari bagian JavaPOS dari SDK di:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   dan mengharapkan jar layanan Zebra (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …)
   serta pustaka native (`CSJPOSScanner64.dll`, …) di folder `bin\`-nya. Jika SDK dipasang di
   tempat lain, lihat [ZEBRA_HOME](#zebra_home).
4. **Mulai ulang Windows** setelah memasang. Driver CoreScanner dari SDK berjalan di latar belakang
   dan harus sudah berjalan sebelum GAIA Desktop bisa menjangkau pemindai; memulai ulang adalah
   cara paling sederhana untuk memastikannya.

---

## 4. Memasang 123Scan

123Scan adalah utilitas gratis Zebra untuk mengonfigurasi pemindainya. Utilitas ini dipakai pada
langkah berikutnya untuk memperbarui firmware pemindai, dan merupakan cara termudah untuk memeriksa
serta mengubah pengaturan pemindai sesudahnya.

1. Unduh **123Scan** dari situs dukungan Zebra dan jalankan penginstalnya.
2. Colokkan pemindai Zebra dan jalankan 123Scan. 123Scan seharusnya menemukan pemindai dan
   menampilkan model, nomor seri, dan versi firmware-nya saat ini. Jika tidak, coba port USB atau
   kabel lain sebelum melanjutkan — GAIA Desktop tidak akan melihat pemindai yang tidak terlihat
   oleh 123Scan.

---

## 5. Memperbarui firmware pemindai Zebra

Perbarui firmware pemindai ke versi terbaru sebelum mengonfigurasinya. Pembaruan firmware
memperbaiki masalah dekode dan menjaga pemindai tetap selaras dengan driver Zebra yang dipasang
oleh SDK.

1. Tutup **GAIA Desktop** dan program lain yang mungkin memakai pemindai: 123Scan memerlukan
   pemindai sepenuhnya selama pembaruan.
2. Di 123Scan, dengan pemindai tersambung, pilih opsi pembaruan firmware-nya. 123Scan membandingkan
   firmware pemindai dengan versi terbaru yang diterbitkan Zebra dan menawarkan untuk mengunduh serta
   memasangnya. Jika komputer tidak punya akses internet, unduh firmware pemindai dari halaman
   dukungannya di situs Zebra dan muat berkasnya ke 123Scan.
3. Mulai pembaruan dan **biarkan pemindai tetap tercolok sampai 123Scan melaporkan bahwa pembaruan
   selesai**. Prosesnya memakan beberapa menit; pemindai akan memulai ulang di akhir. Mencabutnya di
   tengah jalan dapat membuatnya tidak bisa dipakai.
4. Pastikan 123Scan kini menampilkan versi firmware yang baru.

Pembaruan dapat mengembalikan beberapa pengaturan ke nilai bawaan, itulah sebabnya pemindai
dikonfigurasi pada dua langkah berikutnya, setelah firmware.

---

## 6. Mengatur mode USB pemindai Zebra

Inilah langkah yang paling sering terlewat. **Dari pabrik, pemindai Zebra berperan sebagai keyboard
USB** (mode USB HID Keyboard): pemindai "mengetikkan" setiap kode batang ke mana pun fokus berada.
Dalam mode itu *Sambungkan pemindai* di GAIA Desktop tidak dapat menemukannya, dan apa pun yang
diketik ke kolom teks kehilangan pemisah FNC1-nya, yang mengubah makna untai elemen GS1.

Alihkan pemindai ke mode yang langsung dilayani driver Zebra:

| Mode USB pemindai | Nama perangkat di GAIA Desktop | Catatan |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`, atau `ZebraAllScanners` | Disarankan |
| SNAPI (dengan atau tanpa pencitraan) | `ZebraScannerSNAPI`, atau `ZebraAllScanners` | Protokol USB milik Zebra sendiri; juga berfungsi |
| IBM Table-top USB | `ZebraUSBTableTop`, atau `ZebraAllScanners` | Ditujukan untuk pemindai meja |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`, atau `ZebraAllScanners` | |
| USB HID Keyboard (bawaan pabrik) | — | **Tidak akan tersambung** |

`ZebraAllScanners`, nama perangkat bawaan di GAIA Desktop, menerima pemindai dalam mode mana pun
yang didukung, jadi setelah mode diatur, nilai bawaan biasanya langsung berfungsi.

Untuk mengubah mode, pilih salah satu:

- **pindai kode batang penyiapan** untuk mode tersebut — misalnya *IBM Hand-held USB* — di Quick
  Reference Guide atau Product Reference Guide pemindai (bagian USB Host Types / USB Device Type);
  pemindai berbunyi untuk mengonfirmasi. Atau
- **gunakan 123Scan** ([langkah 4](#4-memasang-123scan)): sambungkan pemindai, buka konfigurasinya,
  dan atur jenis host USB.

Setelah mode diubah, Windows mendeteksi ulang pemindai sebagai jenis perangkat lain; tunggu beberapa
detik sebelum menyambungkan.

Untuk mengembalikan pemindai ke mode keyboard nanti — untuk dipakai dengan perangkat lunak lain —
pindai kode batang penyiapan *USB Keyboard (HID)*, atau pulihkan pengaturan pabriknya.

---

## 7. Mengonfigurasi pemindai Zebra untuk GS1

GAIA Desktop mengurai paling baik bila pemindai memberi tahu jenis kode batang yang dibacanya.

### Kirimkan AIM Code ID

Atur **Transmit Code ID Character** ke **AIM Code ID Character**, memakai kode batang penyiapan di
Product Reference Guide (User Preferences) atau di 123Scan. Setiap pindaian lalu diawali dengan
pengenal simbologinya — `]C1` untuk GS1-128, `]d2` untuk GS1 DataMatrix, `]Q3` untuk GS1 QR Code,
`]e0` untuk GS1 DataBar — yang dipakai GAIA Desktop untuk menamai pembawa data dan menerapkan
aturannya.

Tanpa itu, GAIA Desktop tetap mengurai untai elemen dan mengambil simbologi dari pindaian itu
sendiri, tetapi laporan tidak punya AIM Code ID untuk ditampilkan, dan kode batang biasa seperti
EAN-13 lebih sulit dibedakan dari untai elemen GS1.

### Periksa simbologi

Pastikan jenis kode batang yang Anda pakai **diaktifkan**: biasanya GS1-128, GS1 DataMatrix,
GS1 DataBar, GS1 QR Code, dan EAN/UPC. Sebagian besar aktif secara bawaan pada pemindai 2D Zebra;
123Scan menampilkan pengaturan saat ini, dan Product Reference Guide memuat kode batang untuk
mengaktifkan masing-masing.

### Biarkan fitur penguraian GS1 Zebra mati

Firmware terbaru menambahkan fitur GS1 yang bekerja di pemindai itu sendiri — **Label Parse+**,
**GS1 Digital Link**, dan **GS1 Retail POS parsing**. Biarkan semuanya **mati**, seperti saat
pemindai dikirim: fitur-fitur itu memformat ulang atau memendekkan apa yang dikirim pemindai,
padahal GAIA Desktop memerlukan data mentah untuk mengurai dan memvalidasinya. Lihat
[Pemindai 2D Zebra yang didukung](#12-pemindai-2d-zebra-yang-didukung) untuk fungsi masing-masing.

### Jangan pakai awalan dan akhiran

Jangan tambahkan awalan, akhiran, atau tombol Enter ke keluaran pemindai. GAIA Desktop menerima
setiap pindaian sebagai pesan utuh melalui JavaPOS dan tidak memerlukan satu pun dari itu; karakter
tambahan akan ikut masuk ke untai elemen.

---

## 8. Memasang dan menjalankan GAIA Desktop

1. Ekstrak `gaia-desktop-1.0.0-win.zip`, misalnya ke `C:\Tools\`. Isinya satu folder:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Colokkan pemindai.
3. Klik ganda **`run.bat`**. Jendela konsol terbuka di samping aplikasi dan tetap terbuka: status
   pemindai dan galat JavaPOS dilaporkan di sana.
4. Baca baris pertama di konsol. Baris itu menyebutkan apa yang ditemukan `run.bat`:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Jika diakhiri dengan **`Zebra services from none (Zebra Scanner SDK not installed)`**, SDK tidak
   ditemukan di lokasi bawaannya — pasang SDK ([langkah 3](#3-memasang-zebra-scanner-sdk)) atau
   atur [ZEBRA_HOME](#zebra_home). Aplikasi tetap berjalan, tetapi pemindai tidak dapat tersambung.

---

## 9. Menyambungkan pemindai

1. Di bilah alat, **pemilih perangkat** menampilkan nama-nama pemindai yang dikenal GAIA Desktop.
   Biarkan pada **`ZebraAllScanners`**, atau pilih nama untuk mode USB pemindai dari tabel di
   [langkah 6](#6-mengatur-mode-usb-pemindai-zebra).
2. Klik **Sambungkan pemindai**, atau tekan **Ctrl+K** (juga ada di menu **Pemindai**).
3. Bilah status berubah menjadi **`pemindai: ZebraAllScanners`** (atau nama yang Anda pilih), dan
   baris status melaporkan *Pemindai tersambung — ZebraAllScanners*. Tombolnya kini bertuliskan
   **Putuskan pemindai**.
4. Pindai sebuah kode batang GS1. Kode itu muncul di bagian atas riwayat tangkapan dan laporannya
   terbuka. Dengan AIM Code ID aktif, laporan menyebutkan pembawa datanya — *GS1-128*,
   *GS1 DataMatrix* — dan untai elemen mempertahankan setiap pemisah FNC1.

Pemilih perangkat mengingat pilihan Anda di antara sesi. Pemindai tetap tersambung sampai Anda
memutuskannya atau menutup aplikasi. Pindaian juga berfungsi dalam mode **Bangun** dan **Gabung**:
dalam Gabung, setiap pindaian menjadi salah satu kode batang yang digabungkan.

Selama GAIA Desktop memegang pemindai, program lain tidak dapat memakainya, begitu pula sebaliknya:
tutup 123Scan dan perangkat lunak kasir (point-of-sale) atau pemindai lain sebelum menyambungkan.

---

## 10. Referensi konfigurasi

### Nama perangkat

Pemilih perangkat diisi dari `jpos.xml` yang dibundel di `gaia-desktop.jar`. Entri pemindainya:

| Nama perangkat | Jenis pemindai | Dipakai untuk |
|---|---|---|
| `ZebraAllScanners` | apa saja | **Bawaan.** Mode USB apa pun yang didukung |
| `ZebraAllScanners114` | apa saja | Versi JavaPOS 1.14 dari layanan yang sama |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Pemindai dalam mode IBM Hand-held USB |
| `ZebraUSBTableTop` | IBM Table-top USB | Mode table-top |
| `ZebraUSBOPOS` | USB OPOS | Mode USB OPOS |
| `ZebraScannerSNAPI` | SNAPI | Mode SNAPI |
| `ZebraScannerUSB` | USB apa saja | Mode USB apa pun |
| `ZebraScannerSerialSSI` | serial SSI | Pemindai berkabel serial (bukan USB) |
| `ZebraScannerSerialNIXMODB` | serial NIXMODB | Pemindai berkabel serial |
| `ZebraVDI` | VDI | Pemindai yang diteruskan ke desktop virtual |

### ZEBRA_HOME

Jika Zebra SDK dipasang di tempat selain lokasi bawaan, atur `ZEBRA_HOME` ke folder `JPOS`-nya
sebelum menjalankan `run.bat` — misalnya dalam berkas batch kecil di sampingnya:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

`run.bat` lalu mengambil jar layanan Zebra dari `%ZEBRA_HOME%bin` dan menambahkan folder itu ke
`PATH` untuk DLL Zebra.

### jpos.xml milik Anda sendiri

Untuk menambahkan nama perangkat atau mengubah pengaturan sebuah entri, salin `jpos.xml` dari
`gaia-desktop.jar` (berkas itu berformat zip; alat ekstrak apa pun bisa membukanya), sunting
salinannya, dan arahkan GAIA Desktop ke sana:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

Pemilih perangkat kemudian menampilkan pemindai dari berkas itu. Jika lebih dari satu pemindai
Zebra tersambung, sebuah entri dapat diikat ke salah satunya melalui properti `ScannerModel` atau
`SerialNumber`, seperti pada entri `ZebraAllScanners`.

### Opsi lain

| Pengaturan | Caranya |
|---|---|
| Bahasa aplikasi | `run.bat --language=FR` (hanya untuk sesi ini), atau Panel opsi |
| Opsi Java tambahan | `set GAIA_DESKTOP_OPTS=…` sebelum `run.bat` |
| Java bawaan paket | letakkan folder `jdk-21…` di samping `run.bat`; folder ini dipakai sebelum `JAVA_HOME` |

---

## 11. Pemecahan masalah

Galat muncul di kotak dialog saat menyambungkan, di jendela konsol, dan di berkas log
`logs\gaia-desktop.log`, di folder tempat `run.bat` dijalankan (folder GAIA Desktop, bila
`run.bat` diklik ganda). Angka dalam kurung siku di log adalah kode galat JavaPOS.

| Yang Anda lihat | Kemungkinan penyebab | Yang perlu dilakukan |
|---|---|---|
| Tidak ada tombol *Sambungkan pemindai* | Tidak berjalan di Windows | Pemindaian hanya tersedia di Windows |
| 123Scan tidak menemukan pemindai | Kabel, port USB, atau pemindai sedang dipegang program lain | Coba kabel atau port lain; tutup GAIA Desktop dan perangkat lunak pemindai lain. GAIA Desktop tidak akan melihat pemindai yang tidak terlihat oleh 123Scan |
| Konsol: `Zebra services from none (Zebra Scanner SDK not installed)` | SDK tidak berada di lokasi bawaannya | Pasang SDK 64-bit ([langkah 3](#3-memasang-zebra-scanner-sdk)), atau atur [ZEBRA_HOME](#zebra_home) |
| Galat **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Jar layanan Zebra tidak ditemukan | Seperti di atas — periksa baris `Zebra services from` di konsol |
| Galat **109**: *Service does not exist in loaded JCL registry* | Nama perangkat tidak ada di konfigurasi pemindai, atau `javaPOS114.jar` dari SDK masuk ke class path | Pilih nama dari pemilih perangkat; jangan tambahkan `javaPOS114.jar`; jika memakai `jpos.xml` sendiri, pastikan nama itu ada di dalamnya |
| Galat **107** (tidak ada perangkat keras), atau penyambungan gagal padahal pemindai tercolok | Pemindai masih dalam mode keyboard USB, atau driver CoreScanner tidak berjalan | Atur mode USB ([langkah 6](#6-mengatur-mode-usb-pemindai-zebra)); mulai ulang Windows setelah memasang SDK; coba port USB lain |
| Galat **112** (batas waktu) atau **102** (sedang dipakai) saat menyambungkan | Program lain memegang pemindai | Tutup 123Scan dan perangkat lunak pemindai atau kasir lainnya, lalu sambungkan lagi |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` di konsol | DLL Zebra tidak dapat dimuat — sering karena SDK 32-bit dengan Java 64-bit | Pasang SDK 64-bit; pastikan `java -version` melaporkan VM 64-bit |
| Pindaian terketik ke bilah input alih-alih masuk sebagai tangkapan | Pemindai dalam mode keyboard USB | Atur mode USB ([langkah 6](#6-mengatur-mode-usb-pemindai-zebra)) |
| Tersambung, tetapi pindaian tidak masuk | Nama perangkat salah untuk mode pemindai, atau pemindai dicolok ulang saat tersambung | Putuskan, pilih `ZebraAllScanners`, sambungkan lagi |
| Laporan tidak menampilkan AIM Code ID / pembawa data | Pengiriman AIM Code ID mati | Aktifkan *Transmit Code ID Character → AIM Code ID* ([langkah 7](#7-mengonfigurasi-pemindai-zebra-untuk-gs1)) |
| Untai elemen kehilangan pemisahnya, atau AI-nya menyatu | Dipindai dalam mode keyboard, atau diketik manual tanpa `{GS}` | Gunakan mode USB JavaPOS; saat mengetik, sisipkan pemisah dengan tombol `{GS}` |
| Suatu jenis kode batang tidak pernah terbaca | Simbologi itu dinonaktifkan di pemindai | Aktifkan ([langkah 7](#7-mengonfigurasi-pemindai-zebra-untuk-gs1)) |

Jika masalah berlanjut, simpan bersama-sama baris `run.bat:` dari konsol, teks dialog galat, dan
`logs\gaia-desktop.log`: bersama-sama ketiganya menunjukkan Java mana, layanan Zebra mana, dan nama
perangkat mana yang dipakai, serta di titik mana tepatnya penyambungan gagal.

---

## 12. Pemindai 2D Zebra yang didukung

GAIA Desktop memerlukan pemindai yang dapat membaca kode batang tempat data GS1 dicetak — GS1-128,
GS1 DataBar, EAN/UPC, dan, untuk sebagian besar keperluan, kode 2D: **GS1 DataMatrix** dan
**GS1 QR Code**, yang juga merupakan cara URI **GS1 Digital Link** biasanya dicetak. Artinya
diperlukan **pencitra 2D** Zebra: pemindai linear (1D) sama sekali tidak dapat membaca kode
DataMatrix atau QR.

Pemindai itu sendiri tidak perlu memahami GS1 Application Identifiers atau Digital Link.
**GAIA Desktop mengurai keduanya**: aplikasi mengambil pindaian apa adanya seperti yang dibaca
pemindai — AIM Code ID, pemisah FNC1, dan semuanya — lalu memecahnya menjadi AI, atau mengubah URI
Digital Link menjadi AI-nya, sendiri.

### Sudah diuji

| Pemindai | Jenis | Status |
|---|---|---|
| **DS8108** | Pencitra 2D genggam berkabel | **Dibuat dan diuji** dengan GAIA Desktop |

### Diperkirakan berfungsi

Berikut pencitra 2D Zebra yang, seperti DS8108, dijalankan melalui Scanner SDK dan layanan JavaPOS
dari Zebra. Semuanya **belum diuji** dengan GAIA Desktop; siapkan masing-masing seperti dalam
panduan ini, memakai Quick Reference Guide atau Product Reference Guide-nya sendiri untuk kode batang
penyiapan.

| Keluarga | Model (contoh) |
|---|---|
| DS81x8 | DS8108 (berkabel), DS8178 (nirkabel) |
| DS22x8 | DS2208 (berkabel), DS2278 (nirkabel) — serbaguna |
| DS36x8 | DS3608 (berkabel), DS3678 (nirkabel) — tangguh, termasuk varian KD dan XR |
| DS46x8 | DS4608 (berkabel), DS4678 (nirkabel) |
| DS9308 / DS9908 | Pemindai presentasi dan bebas genggam |
| MP7000 / MP7200 | Pemindai bioptik multibidang dan pemindai-timbangan |
| CS6080 | Pemindai pendamping nirkabel |

Pemindai nirkabel berkomunikasi dengan dudukannya, dan dudukan itulah yang dicolokkan ke komputer:
sambungkan dudukan lewat USB dan perlakukan pasangan itu sebagai satu pemindai. Pemindai dari
keluarga Zebra lain mungkin juga berfungsi, jika Scanner SDK dari Zebra mendukungnya dalam salah satu
mode USB di [langkah 6](#6-mengatur-mode-usb-pemindai-zebra).

### Fitur GS1 di pemindai Zebra

Terpisah dari membaca kode batang, beberapa pencitra 2D Zebra dapat memproses data GS1 di pemindai
itu sendiri, dikonfigurasi melalui 123Scan:

| Fitur | Fungsinya | Untuk GAIA Desktop |
|---|---|---|
| **Label Parse+** | Mengurai AI label GS1 di pemindai dan mengirim kolom yang sudah diformat ulang | **Biarkan mati** — GAIA Desktop tidak lagi melihat AI, pemisah FNC1, dan AIM Code ID |
| **GS1 Digital Link** | Untuk produk ritel yang ditandai dengan EAN/UPC 1D sekaligus kode Digital Link 2D: memilih mana yang dikirim | **Biarkan mati** (bawaan) |
| **GS1 Retail POS parsing** | Membaca kode batang ritel 2D dalam sintaks URI Digital Link atau untai elemen | **Biarkan mati** |

Pengaturan **GS1 Digital Link** punya tiga mode. **Mode 1 — GTIN only**, mode awalnya setelah
diaktifkan, hanya mengirim GTIN, dari kode batang mana pun asalnya, sehingga batch, tanggal
kedaluwarsa, atau nomor seri dalam Digital Link tidak akan pernah sampai ke GAIA Desktop.
**Mode 2 — Prioritize 2D** mengirim kode 2D bila terlihat dalam batas waktu prioritas (bawaan
200 ms), jika tidak, kode 1D. **Mode 3 — Corroborate 1D and 2D GTINs Match** mengirim kode 1D, dan
juga kode 2D bila GTIN-nya cocok. Jika pengaturan ini harus aktif demi perangkat lunak lain, pakai
Mode 2, bukan Mode 1. Pada DS8108 ini adalah parameter 2373 (aktif/mati), 2374 (mode), dan 2491
(batas waktu).

Fitur-fitur ini memerlukan **123Scan versi 6 atau lebih baru** dan firmware yang memuatnya. Zebra
telah menambahkan dukungan GS1 Digital Link lewat pembaruan firmware sejak April 2025; tanggal di
bawah berasal dari reseller Zebra, jadi periksa catatan rilis firmware setiap model:

| Keluarga model | Firmware GS1 Digital Link dirilis |
|---|---|
| DS81x8 | 16 April 2025 |
| MP7000 | 21 April 2025 |
| DS9908 | 6 Juni 2025 |
| SP72 / SM72 | 11 Juli 2025 |
| DS46x8 | 28 Juli 2025 |
| MP7200 (Rev-C), DS5502 | 30 Juli 2025 |
| DS9308 | 29 Agustus 2025 |
| DS22x8, DS1000 | 5 September 2025 (hanya Asia-Pasifik) |
| DS36x8 (termasuk KD dan XR), MS47 | 30 September 2025 |
| PL5000, SE4107 | 15 Oktober 2025 |
| CS6080 | 31 Desember 2025 |

Tidak satu pun fitur ini diperlukan untuk membaca kode GS1 Digital Link ke GAIA Desktop: dengan
semuanya mati, pemindai 2D Zebra dengan firmware terkini membaca kode QR Digital Link seperti kode
lainnya, dan GAIA Desktop mengubah URI-nya menjadi AI.

Sumber: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (Agustus 2025), bab 12
"GS1 Digital Link Parameters" dan bab Data Parsing; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (tanggal firmware).
