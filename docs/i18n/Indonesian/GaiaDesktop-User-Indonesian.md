# GAIA Desktop — Panduan Pengguna

Cara menggunakan aplikasi. Untuk membangun, mengubah, atau menambah bahasa, lihat
[panduan pengembang](../../GaiaDesktop-Developer.md) (dalam bahasa Inggris). Dokumen asli:
[GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Daftar isi

1. [Apa ini](#apa-ini)
2. [Menjalankannya](#menjalankannya)
3. [Jendela](#jendela)
4. [Mode Urai](#mode-urai)
   - [Bilah masukan dan token {GS}](#bilah-masukan-dan-token-gs)
   - [Pemindaian](#pemindaian)
   - [Membuka berkas](#membuka-berkas)
   - [Riwayat tangkapan](#riwayat-tangkapan)
5. [Mode Bangun](#mode-bangun)
   - [Editor AI](#editor-ai)
   - [Apa yang ditolak editor](#apa-yang-ditolak-editor)
   - [String elemen atau Digital Link](#string-elemen-atau-digital-link)
   - [Menyunting tangkapan yang ada](#menyunting-tangkapan-yang-ada)
6. [Laporan](#laporan)
   - [Mengekspor](#mengekspor)
7. [Opsi penguraian](#opsi-penguraian)
   - [Peluncuran pertama](#peluncuran-pertama)
8. [Bahasa](#bahasa)
9. [Apa yang diingat](#apa-yang-diingat)
10. [Pintasan papan ketik](#pintasan-papan-ketik)
11. [Pemecahan masalah](#pemecahan-masalah)
12. [Batasan](#batasan)

---

## Apa ini

GAIA Desktop melakukan dua hal dengan data GS1:

- **Mengurai** — ketik, tempel, buka, atau pindai sebuah string elemen GS1 atau URI Digital Link
  lalu baca hasilnya sebagai laporan lengkap: apa arti setiap Application Identifier (AI), apa
  yang salah, dan mengapa;
- **Membangun** — menyusun pasangan AI / nilai menjadi string elemen atau URI Digital Link.

Semua yang aplikasi ini ketahui tentang GS1 — setiap definisi AI, setiap aturan validasi, setiap
pesan galat, dan setiap deskripsi yang dilokalkan — berasal dari mesin GAIA di bawahnya.
Aplikasi tidak menambahkan aturan sendiri: yang diterimanya persis sama dengan yang diterima
mesin.

---

## Menjalankannya

```bash
java -jar gaia-desktop.jar
```

Berkas JAR memerlukan direktori `lib/` yang disertakan di sebelahnya — simpan keduanya bersama,
jika tidak aplikasi tidak akan berjalan. (Berkas JAR mandiri juga dapat dibangun; lihat
[panduan pengembang](../../GaiaDesktop-Developer.md#packaging).)

**Java 21 atau lebih baru** harus terpasang. Distribusinya khusus untuk satu sistem operasi dan
satu arsitektur prosesor — gunakan berkas JAR yang dibangun untuk mesin Anda.

Untuk membuka aplikasi dalam bahasa tertentu, tanpa mengubah bahasa yang tersimpan:

```bash
java -jar gaia-desktop.jar --language=FR
```

Nilainya dapat berupa kode katalog (`FR`, `PCM`, `ARZ`), nama lengkap (`FRENCH`, `WU-CHINESE`),
atau tag bahasa (`fr`, `zh-HK` untuk Kanton, `ar-EG` untuk Arab Mesir) — huruf besar-kecil tidak
berpengaruh. Penetapan ini hanya berlaku untuk sesi tersebut: bahasa yang Anda simpan tetap utuh,
sehingga pintasan yang dibuat untuk rekan kerja tidak dapat mengubah bahasa saat salinan Anda
sendiri dibuka. Memilih bahasa *di dalam* aplikasi tetap merupakan pilihan sungguhan dan
tersimpan seperti biasa. Nilai yang tidak menunjuk bahasa mana pun diabaikan dengan catatan di
konsol, dan aplikasi dibuka dalam bahasa yang tersimpan alih-alih menolak berjalan.

Saat mulai, konsol mencetak satu baris berisi versi aplikasi, tanggal pembangunan, dan versi
mesin. Baris itulah yang sebaiknya dikutip dalam laporan kesalahan.

---

## Jendela

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

> Bagan ini mempertahankan label bahasa Inggris, sama seperti semua blok kode dalam dokumentasi
> ini. Di layar Anda akan melihat *Urai* / *Bangun*, *Tangkapan*, *Simpan laporan*, *Opsi*,
> *pemindai: mati*, dan *Tentang*.

**Di macOS dan Linux baris pemindai pada gambar itu tidak ada** — tidak ada sakelar maupun
pemilih perangkat di bilah alat, tidak ada menu *Pemindai*, dan tidak ada *pemindai: mati* di
bilah status. Lihat [Pemindaian](#pemindaian).

Sakelar **Urai ⇄ Bangun** hanya mengubah jalur di bawah bilah alat serta kendali yang berkaitan
dengan penangkapan. Riwayat, laporan, panel opsi, dan tindakan ekspor melayani kedua mode, sebab
pembangunan yang berhasil hanya menjadi satu tangkapan lagi.

Panel opsi **tertutup pada peluncuran pertama**. Mode, tampak-tidaknya panel, tema, dan pemindai
terakhir yang dipakai diingat di antara sesi.

---

## Mode Urai

### Bilah masukan dan token {GS}

Bilah ini menerima string elemen, URI GS1 Digital Link, atau hasil pindai mentah lengkap dengan
awalan AIM Code ID (`]d2…`). <kbd>Enter</kbd> mengurai.

AI dengan panjang berubah-ubah dipisahkan oleh **FNC1** — karakter kendali tak terlihat `0x1D`.
Bidang teks tidak dapat menyimpan karakter itu: ia akan hilang diam-diam, dan
`01…` + FNC1 + `10LOT` akan tiba sebagai `01…10LOT`, sebuah string elemen yang artinya berbeda.

Karena itu aplikasi menampilkan pemisah sebagai token kasatmata **`{GS}`** dan mengubahnya
kembali dalam perjalanan menuju pengurai:

- **tombol `{GS}`** menyisipkan token di posisi kursor;
- **menempel** karakter pemisah yang sebenarnya — lewat papan ketik, menu konteks, atau seret dan
  lepas — otomatis mengubahnya menjadi `{GS}`, sehingga tidak ada yang hilang;
- token ditulis persis seperti laporan menuliskannya, jadi nilai yang disalin dari laporan dapat
  ditempel kembali apa adanya.

Tangkapan, laporan, dan riwayat yang tersimpan semuanya memuat pemisah **yang sebenarnya**. Token
hanyalah bentuk yang Anda lihat dan ketik.

### Pemindaian

> **Hanya Windows.** Zebra menyediakan penggerak pemindainya khusus untuk Windows, sehingga di
> macOS dan Linux aplikasi sama sekali tidak membangun kendali pemindai — tidak ada sakelar
> *Sambungkan pemindai*, tidak ada pemilih perangkat, tidak ada menu *Pemindai*, dan tidak ada
> status pemindai di bilah status. Tidak ada yang dibuat kelabu, sebab tidak ada pengaturan
> maupun perangkat yang bisa menyalakannya. Selebihnya, termasuk membuka berkas berisi pindaian
> tersimpan, berjalan di mana saja.

*Sambungkan pemindai* membuka perangkat yang disebut pada pemilih; `ZebraAllScanners` adalah
nilai bawaan dan cocok untuk sebagian besar model. Setiap pindaian diurai dan otomatis
ditambahkan ke riwayat.

Data pemindai dibaca bita demi bita, sehingga awalan AIM Code ID dan setiap pemisah FNC1 sampai
ke pengurai persis seperti yang dikirim pemindai.

Dalam mode Bangun kendali pemindai disembunyikan, tetapi pemindai yang tersambung tetap bekerja:
*Pemindai → Sambungkan / Putuskan* (<kbd>⌘K</kbd>) masih mengalihkannya, bilah status masih
melaporkannya, dan pindaian yang datang saat editor terbuka tetap ditangkap.

Jika perangkat tidak mau tersambung, lihat [Pemecahan masalah](#pemecahan-masalah).

### Membuka berkas

*Berkas → Buka berkas teks…* mengurai **setiap baris yang tidak kosong sebagai tangkapan
tersendiri** — cara cepat menggarap sekumpulan pindaian yang dikirimkan seseorang kepada Anda.
Berkas dibaca bita demi bita, jadi pemisah mentah di dalamnya tetap selamat.

### Riwayat tangkapan

Yang terbaru di atas. Tiap baris menampilkan titik sah / tidak sah, ringkasan satu baris, serta
waktu penangkapan, asalnya, dan jumlah AI yang dikandungnya. Tanggal hanya muncul pada tangkapan
dari hari sebelumnya; baris hari ini hanya menampilkan waktu.

Ringkasannya adalah **URI Digital Link** untuk Digital Link, dan **HRI** untuk yang lain: untuk
string elemen, `(01)09506000134352 (17)261231` lebih mudah dibaca daripada
`01095060001343521726123110…`.

Klik kanan sebuah tangkapan untuk menyalin masukan atau HRI-nya, mengirimkannya kembali ke bilah
masukan, membukanya di editor, menghapusnya, atau membersihkan riwayat.

---

## Mode Bangun

### Editor AI

Sebuah tabel pasangan AI / nilai. Ketik kode AI atau pilih dari **semua AI yang dikenal mesin**
pada daftar *Tambah AI…*; kolom **Judul**, **Format**, dan **Deskripsi** terisi seiring Anda
mengetik, dalam bahasa yang dipilih. Daftar itu tidak akan menawarkan AI yang akan ditolak saat
pembangunan.

Tabel selalu menyisakan **satu baris kosong di akhir**: ketik di sana dan baris berikutnya
muncul, sehingga string dengan banyak AI tidak pernah memerlukan *Tambah baris*.

Penyuntingan berperilaku sebagaimana mestinya sebuah tabel: <kbd>Enter</kbd>, <kbd>Tab</kbd>,
<kbd>Shift</kbd>+<kbd>Tab</kbd>, dan mengklik di tempat lain semuanya **mempertahankan** apa yang
Anda ketik; <kbd>Esc</kbd> membuangnya. <kbd>Tab</kbd> berpindah dari AI ke Nilai lalu ke baris
berikutnya, melewati kolom yang tidak dapat disunting.

### Apa yang ditolak editor

| | Apa yang terjadi |
|---|---|
| AI yang tidak dikenali mesin | Baris memerah selagi Anda mengetik; pembangunan ditolak disertai penjelasan |
| AI yang sama dua kali, dari daftar | Tidak ditambahkan — sebuah pesan menyebutkannya, dan baris yang sudah memuatnya dipilih |
| AI yang sama dua kali, diketik manual | Kedua baris ditandai; pembangunan ditolak dengan galat `GE-S004` |
| Tidak ada yang bisa dibangun | Sebuah pesan; tidak ada tangkapan yang dibuat |

Sebuah AI hanya boleh muncul sekali dalam satu string elemen — itulah sebabnya daftar menolak
sejak awal, alih-alih membiarkan Anda mencoba pembangunan yang sudah pasti gagal.

### String elemen atau Digital Link

**Bangun string elemen** merangkai baris-baris itu sambil menyisipkan pemisah FNC1 di setiap
tempat yang diwajibkan standar. **Bangun Digital Link** menghasilkan URI; bentangkan
*Target Digital Link* untuk mengarahkannya ke resolver Anda sendiri alih-alih `id.gs1.org` yang
baku:

| Ruas | Efek | Kosong berarti |
|---|---|---|
| Domain | Host resolver, mis. `example.com` | `id.gs1.org` |
| Awalan jalur | Sebuah segmen sebelum jalur AI, mis. `/products` | tanpa awalan |

Bila **berhasil**, hasilnya berperilaku persis seperti sebuah pindaian: diurai, ditambahkan ke
riwayat, digambar sebagai laporan, dan disimpan. Pembangunan memvalidasi dengan mengurai
keluarannya sendiri, jadi apa pun yang dikembalikannya pasti sah.

Bila **gagal**, tidak ada yang ditangkap. Alasannya tetap berada di bawah tombol pembangunan,
satu baris per masalah, masing-masing menyebut galat, AI yang bersangkutan, dan penjelasan mesin
sendiri:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> Tanda kutip ganda pada pesan itu berasal dari katalog galat mesin, bukan dari aplikasi ini.

### Menyunting tangkapan yang ada

Klik kanan sebuah tangkapan → **Bangun** memuat AI-nya ke dalam editor. Ubah sebuah nilai, bangun
ulang, dan hasilnya hadir sebagai tangkapan baru — yang asli dibiarkan utuh.

---

## Laporan

Tangkapan yang dipilih digambarkan sebagai laporan lengkap: panji putusan, string elemen yang
diurai menjadi “pita” berlabel, metadata penguraian, satu kartu untuk tiap AI beserta
interpretasinya, serta galat dan peringatan lengkap dengan petunjuk perbaikannya.

Untuk **Digital Link**, tiap kartu AI juga membawa peran strukturalnya — **kunci utama**,
**kualifikasi kunci**, atau **atribut data** — dengan kode warna sesuai urutan kemunculannya di
URI. Peran hanya ditampilkan untuk Digital Link: pada string elemen biasa, sebuah AI tidak
memiliki posisi URI yang bisa dijelaskan.

Laporan mengikuti bahasa dan tema yang Anda pilih.

### Mengekspor

| | |
|---|---|
| **Simpan laporan → Sebagai HTML…** (<kbd>⌘S</kbd>) | Menulis persis apa yang tampak di layar: satu berkas mandiri yang terbuka di peramban mana pun |
| **Simpan laporan → Sebagai PDF…** (<kbd>⌘P</kbd>) | Mencetak laporan lewat dialog cetak sistem — pilih tujuan PDF di sana |

PDF melewati dialog cetak karena di sanalah tujuan dan nama berkas dipilih: **PDF ▾ → Simpan
sebagai PDF** di macOS, **Microsoft Print to PDF** di Windows, **Cetak ke berkas** di Linux.
Halaman terbagi rapi dan teksnya tetap dapat diseleksi.

Cetakan dan PDF selalu memakai palet terang, bahkan ketika aplikasi bertema gelap, dan
mempertahankan keping AI berwarna serta panji putusan.

Jika mesin Anda sama sekali tidak memiliki pencetak atau tujuan PDF yang tersiap, aplikasi akan
mengatakannya dan mengarahkan Anda ke ekspor HTML, yang dapat dicetak dari peramban mana pun.

---

## Opsi penguraian

Panel di sebelah kanan memuat seluruh opsi mesin. Mengubah apa pun **langsung mengurai ulang
tangkapan yang sedang dipilih**, sehingga panel ini juga menjadi cara bertanya “seperti apa
masukan ini dengan pengaturan tersebut?”, bukan sekadar memengaruhi pindaian berikutnya.

| Opsi | Nilai |
|---|---|
| Mode analisis | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — seberapa dalam analisis dibawa |
| Bahasa | 35 katalog pesan |
| Urutan | `LITTLE` (dd/mm/yyyy), `MIDDLE` (mm/dd/yyyy), `BIG` (yyyy/mm/dd) |
| Pemisah | `SLASH`, `HYPHEN`, `PERIOD` |
| Bulan | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Tahun | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Lewati pemeriksaan "requires" | Berhenti menolak AI yang pasangan wajibnya tidak ada (`GE-S005`) |
| Lewati pemeriksaan "excludes" | Berhenti menolak pasangan AI yang dilarang standar (`GE-S006`) |

*Setel ulang ke bawaan* di kaki panel mengembalikan nilai bawaan mesin itu sendiri.

### Peluncuran pertama

Selama belum ada yang tersimpan, aplikasi mengambil pengaturannya dari **setelan wilayah mesin
Anda**, bukan dari nilai bawaan mesin GAIA:

- **bahasa** berasal dari lokal sistem, dan kembali ke bahasa Inggris bila tidak ada katalognya;
- **opsi tanggal** dibaca dari format tanggal pendek pada lokal Anda: urutan komponennya,
  pemisahnya, serta apakah bulan dan tahun disingkat.

> **Catatan.** Karena itu lokal yang menulis tanggal pendek dengan tahun dua digit juga akan
> mulai dengan dua digit di sini: hal itu selaras dengan bagian lain sistem Anda, tetapi berbeda
> dari bawaan mesin yang empat digit. Ubah di panel dan pilihan itu bertahan.

---

## Bahasa

Memilih bahasa mengubah **seluruh aplikasi** — menu, tombol, editor, panel opsi, baris status,
dan laporan — bukan hanya pesan galat. Bahasa yang ditulis dari kanan ke kiri membalik jendela
dan laporan.

Seluruh 35 bahasa tersedia, dengan dua lapis cakupan:

| | Cakupan |
|---|---|
| Laporan penguraian dan semua pesan galat mesin | **seluruh 35 bahasa** |
| Menu, tombol, dan label aplikasi sendiri | **seluruh 35 bahasa** |

Bahasa yang ditulis dari kanan ke kiri — Arab, Arab Mesir, Persia, dan Urdu — membalik jendela
dan laporan sejak aplikasi dibuka.

Bahasa diingat di antara sesi. Untuk membuka dalam bahasa lain sekali saja, lihat
[`--language`](#menjalankannya).

Beberapa hal sengaja dibiarkan tanpa terjemahan di semua bahasa: nama ruas ringkas yang dicetak
di samping nilai mentah (`format`, `position`, `raw`, `len` …), yang merupakan pengenal dan bukan
kata; nama contoh dan nama simbologi, yang merupakan istilah GS1 dan industri; serta satu
paragraf panjang dari standar, yang terjemahan setengah jadinya justru menyesatkan.

---

## Apa yang diingat

Dua berkas di dalam `.gaia-desktop` pada direktori rumah Anda:

| Berkas | Berisi |
|---|---|
| `options.properties` | Semua opsi penguraian, tema, apakah panel opsi terbuka, pemindai terakhir yang dipakai, dan mode saat jendela ditinggalkan |
| `captures.tsv` | Riwayat tangkapan, yang terbaru dulu, dibatasi 500 entri |

Opsi ditulis setiap kali Anda mengubah salah satunya; riwayat setiap kali ia berubah. Keduanya
ditulis ulang saat keluar.

Menghapus salah satu berkas hanya menyetel ulang bagian itu dan tidak lebih — keduanya dipisahkan
supaya riwayat yang rusak tidak merenggut pengaturan Anda. Berkas yang rusak karena disunting
manual pun tidak fatal: setiap pengaturan kembali ke nilai bawaannya sendiri-sendiri, sehingga
satu baris keliru hanya merugikan baris itu.

Riwayat menyimpan masukan, bukan hasil analisis. Masukan itu diurai ulang saat dimuat, yang
berarti tangkapan yang dipulihkan mencerminkan opsi yang berlaku **sekarang**, bukan yang berlaku
saat ia ditangkap.

---

## Pintasan papan ketik

| | |
|---|---|
| <kbd>Enter</kbd> | Mengurai isi bilah masukan |
| <kbd>⌘O</kbd> | Membuka berkas teks |
| <kbd>⌘S</kbd> | Menyimpan laporan sebagai HTML |
| <kbd>⌘P</kbd> | Menyimpan laporan sebagai PDF (dialog cetak sistem) |
| <kbd>⌘G</kbd> | Menyisipkan token pemisah FNC1 |
| <kbd>⌘K</kbd> | Menyambungkan / memutuskan pemindai (hanya Windows) |
| <kbd>⌘D</kbd> | Mengalihkan tema gelap |
| <kbd>⌘Q</kbd> | Keluar |

Di dalam editor: <kbd>Tab</kbd> / <kbd>Shift</kbd>+<kbd>Tab</kbd> berpindah antar sel yang dapat
disunting, <kbd>Enter</kbd> menegaskan, <kbd>Esc</kbd> membatalkan.

---

## Pemecahan masalah

| Gejala | Sebab dan solusi |
|---|---|
| Aplikasi tidak berjalan, atau melaporkan `NoClassDefFoundError` | Direktori `lib/` harus berada di sebelah berkas JAR. Simpan bersama, atau gunakan distribusi mandiri |
| *“Unsupported JavaFX configuration…”* di konsol saat mulai | Wajar dan tidak berbahaya — pustaka hanya mencatat bagaimana ia dimuat. Aplikasi berjalan normal |
| Pemisah yang Anda tempel tampak lenyap | Memang begitu: ia ditulis ulang menjadi token kasatmata `{GS}`, dan pengurai tetap menerima karakter yang sebenarnya. Lihat [token {GS}](#bilah-masukan-dan-token-gs) |
| Tidak ada tombol *Sambungkan pemindai* | Anda berada di macOS atau Linux, tempat penggerak pemindai tidak tersedia. Lihat [Pemindaian](#pemindaian) |
| Pemindai tidak mau tersambung | Periksa apakah nama logis pada pemilih cocok dengan model Anda, apakah pemindai terpasang, dan apakah tidak ada aplikasi lain yang menahannya |
| Ekspor PDF menyatakan tidak ada tujuan | Mesin Anda belum menyiapkan pencetak atau tujuan PDF. Tambahkan satu, atau ekspor ke HTML lalu cetak dari peramban |
| Sebuah butir menu menampilkan sesuatu seperti `act.hri` | Ada label yang hilang — mohon laporkan; itu cacat saat pembangunan, bukan sebuah pengaturan |

---

## Batasan

- **Pemindaian hanya untuk Windows**, karena penggeraknya memang begitu.
- **PDF melewati dialog cetak**, bukan dialog simpan.
- **Target Digital Link menerima domain dan awalan jalur**, bukan parameter kueri tambahan atau
  fragmen.
- **Sebuah AI hanya boleh muncul sekali** dalam satu string elemen — itu aturan GS1, bukan
  batasan aplikasi.
