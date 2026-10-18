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
6. [Mode Gabung](#mode-gabung)
   - [Jalur Gabung](#jalur-gabung)
   - [Apa yang ditolak Gabung](#apa-yang-ditolak-gabung)
   - [Duplikat dan pertentangan](#duplikat-dan-pertentangan)
   - [Laporan gabungan](#laporan-gabungan)
7. [Laporan](#laporan)
   - [Mengekspor](#mengekspor)
8. [Opsi penguraian](#opsi-penguraian)
   - [Peluncuran pertama](#peluncuran-pertama)
9. [Bahasa](#bahasa)
10. [Apa yang diingat](#apa-yang-diingat)
11. [Pintasan papan ketik](#pintasan-papan-ketik)
12. [Pemecahan masalah](#pemecahan-masalah)
13. [Batasan](#batasan)

---

## Apa ini

GAIA Desktop melakukan tiga hal dengan data GS1:

- **Mengurai** — ketik, tempel, buka, atau pindai sebuah string elemen GS1 atau URI Digital Link
  lalu baca hasilnya sebagai laporan lengkap: apa arti setiap Application Identifier (AI), apa
  yang salah, dan mengapa;
- **Membangun** — menyusun pasangan AI / nilai menjadi string elemen atau URI Digital Link;
- **Gabung** — menggabungkan hingga tiga kode batang GS1-128 dari satu label menjadi
  satu string elemen yang akan dibawa sebuah GS1 DataMatrix, memeriksanya secara utuh, dan
  menggambar simbolnya.

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
│ [Parse|Build|Combine]  … mode-specific controls …  Save   Options  │
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
> ini. Di layar Anda akan melihat *Urai* / *Bangun* / *Gabung*, *Tangkapan*, *Simpan laporan*, *Opsi*,
> *pemindai: mati*, dan *Tentang*.

**Di macOS dan Linux baris pemindai pada gambar itu tidak ada** — tidak ada sakelar maupun
pemilih perangkat di bilah alat, tidak ada menu *Pemindai*, dan tidak ada *pemindai: mati* di
bilah status. Lihat [Pemindaian](#pemindaian).

Sakelar **Urai ⇄ Bangun** hanya mengubah jalur di bawah bilah alat serta kendali yang berkaitan
dengan penangkapan. Riwayat, laporan, panel opsi, dan tindakan ekspor melayani kedua mode, sebab
pembangunan yang berhasil hanya menjadi satu tangkapan lagi. Mode **Gabung** ditata secara berbeda: lihat [Mode Gabung](#mode-gabung).

Panel opsi **tertutup pada peluncuran pertama**. Mode, tampak-tidaknya panel, tema, dan pemindai
terakhir yang dipakai diingat di antara sesi.

**Versi baru.** Beberapa detik setelah dijalankan, paling banyak sekali sehari, aplikasi bertanya
kepada GitHub apakah GAIA Desktop yang lebih baru telah dirilis. Jika ya, sebuah tautan muncul di
bilah status — *GAIA Desktop 1.1.0 tersedia* — yang membuka halaman rilis di peramban Anda;
tidak ada yang diunduh atau dipasang untuk Anda. *Lewati versi ini* menyembunyikan
pemberitahuan itu sampai rilis berikutnya. Pemeriksaan ini diam saja bila tidak ada yang baru
atau bila GitHub tidak terjangkau. *Bantuan → Periksa pembaruan…* langsung memeriksa dan
melaporkan hasilnya; *Bantuan → Periksa pembaruan secara otomatis* mematikan dan menghidupkan kembali
pemeriksaan harian.

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
melaporkannya, dan pindaian yang datang saat editor terbuka tetap ditangkap. Dalam mode Gabung kendali tetap ada, dan setiap pindaian ditambahkan ke gabungan,
bukan ke riwayat.

Penyiapan pemindai untuk pertama kali — Zebra Scanner SDK, mode USB pemindai, dan pengaturan
GS1 — dijelaskan langkah demi langkah dalam
[panduan penyiapan pemindai Zebra](GaiaDesktop-Zebra-Scanner-Windows-Indonesian.md), yang disertakan di setiap zip di bawah `docs/`. Jika perangkat tidak mau tersambung, lihat [Pemecahan masalah](#pemecahan-masalah).

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

## Mode Gabung

Label lama sering membagi datanya ke **beberapa kode batang GS1-128** — GTIN di satu kode,
tanggal kedaluwarsa dan batch di kode lain — karena satu kode batang linear tidak dapat memuat
semuanya. Gabung mengambil hingga **tiga** kode batang tersebut, menggabungkannya
menjadi satu string elemen yang akan dibawa sebuah **GS1 DataMatrix**, dan memeriksa hasilnya
secara utuh. Setelah hasilnya valid, aplikasi menggambar simbol DataMatrix.

Tidak satu pun dari kode batang itu perlu lengkap dengan sendirinya. Nomor batch tanpa GTIN-nya
gagal sendirian dengan `GE-S005`, tetapi itu wajar bagi satu kode batang dari satu set, sehingga
Gabung memeriksanya pada gabungan.

### Jalur Gabung

Beralih ke **Gabung** mengganti bilah masukan dengan jalur di sepanjang bagian bawah
jendela. Riwayat tangkapan menyingkir, karena pindaian mode Gabung bukan tangkapan dan
tidak pernah masuk ke sana. Area laporan menampilkan laporan gabungan sebagai gantinya:

```
┌────────────────────────────────────────────────────────────────────┐
│ [Parse|Build|Combine]   (scanner controls, on Windows)   Options   │
├────────────────────────────────────────────────────────────────────┤
│ Combine report                                   │ Options panel   │
├────────────────────────────────────────────────────────────────────┤
│ [Reset] [Remove Last] │ Barcode: [ …………………………………………… ] [Add]       │
├────────────────────────────────────────────────────────────────────┤
│ 2 of 3 GS1-128 barcodes                                   [About]  │
└────────────────────────────────────────────────────────────────────┘
```

Ada dua cara memasukkan kode batang:

- **Memindainya.** Di Windows, kendali pemindai tetap ada di bilah alat dalam mode
  Gabung, dan setiap pindaian langsung masuk ke gabungan, bukan ke riwayat.
- **Mengetik atau menempelkannya** ke kolom *Kode batang* lalu menekan
  <kbd>Enter</kbd> atau **Tambah**. Tulis FNC1 sebagai **`{GS}`**, sama seperti di
  bilah masukan. Kode batang yang diketik tanpa AIM Code ID dianggap GS1-128.

| Tombol | Fungsi |
|---|---|
| **Tambah** | Menambahkan kode batang di kolom |
| **Hapus terakhir** | Menghapus kode batang terbaru (tidak tersedia bila belum ada) |
| **Setel ulang** | Menghapus semua kode batang, mengosongkan kolom dan penolakan apa pun, lalu mulai lagi |

Bilah status selalu menyebutkan berapa kode batang yang tersimpan (*2 dari 3 kode batang GS1-128*),
atau mengapa yang terakhir ditolak.

### Apa yang ditolak Gabung

Kode batang yang ditolak tidak ditambahkan. Laporan dan bilah status sama-sama menyebutkan
alasannya:

| Kode batang | Ditolak karena |
|---|---|
| Kolom kosong | Tidak ada yang ditambahkan |
| Kode batang keempat | Satu label digabung dari paling banyak tiga. Hapus yang terakhir, atau setel ulang |
| Simbologi lain, menurut AIM Code ID-nya | Hanya GS1-128 yang digabung, jadi GS1 DataMatrix (`]d2`) atau QR Code ditolak, dengan namanya |
| AI yang tidak dapat dibaca | Galat sintaksis; kode galat dan pesan mesin ditampilkan |
| Tidak ada AI sama sekali | Tidak ada yang digabung |
| Yang sudah dipindai | String elemen yang sama dua kali, misalnya kode batang yang sama dipindai lagi; laporan menyebutkan kode batang sebelumnya yang diulanginya |

Gabung hanya menolak apa yang sama sekali tidak dapat dipakainya. Selebihnya (digit
pemeriksa yang salah, tanggal yang keliru, AI pendamping yang hilang) diterima dan dinilai pada
gabungan.

### Duplikat dan pertentangan

AI digabung sesuai urutan pemindaiannya. Bila dua kode batang membawa AI yang sama:

- **dengan nilai yang sama**, AI itu disimpan sekali dan dicantumkan di bawah
  *Duplikat — disimpan sekali*, bersama kode batang tempat ia dibaca;
- **dengan nilai berbeda**, itu sebuah **pertentangan**. Putusannya berbunyi
  *BERTENTANGAN*, nilai yang bertentangan dicantumkan per kode batang, dan **tidak ada
  yang digabung** sampai pertentangan itu diselesaikan. Hapus kode batang yang salah, atau setel
  ulang.

### Laporan gabungan

Dari atas ke bawah:

1. **Putusan**: *belum ada pindaian*, *BERTENTANGAN*, valid atau tidak valid, dan berapa
   kode batang yang tersimpan.
2. **Pindaian**: setiap kode batang sebagaimana datangnya, simbologinya, dan string elemen yang
   tersisa setelah AIM Code ID dilepas. Masalah yang hanya dapat diputuskan oleh gabungan
   ditampilkan pada kode batang itu, ditandai *dinilai pada gabungan*.
3. **Duplikat** dan **pertentangan**, jika ada.
4. **Hasil gabungan**: string elemen, HRI-nya, dan panjangnya, lalu masukan gabungan sebagai
   pita berlabel dan satu kartu per AI, keduanya ditata seperti dalam
   [laporan penguraian](#laporan). Setiap galat dan peringatan pada gabungan kemudian
   dicantumkan di bawah *Validasi*.
5. **Simbol GS1 DataMatrix**, dengan HRI-nya di bawah. Simbol hanya digambar bila gabungan valid.
   Jika tidak, laporan menyebutkan mengapa simbol tidak digambar, agar simbol yang tidak valid
   tidak tercetak secara tidak sengaja.

Contohnya, label dengan dua kode batang:

| Kode batang | Membawa | Sendirian |
|---|---|---|
| `]C10109506000134352` | (01) GTIN | valid |
| `]C11726123110LOT-ABC` | (17) tanggal kedaluwarsa, (10) batch | `GE-S005` untuk kedua AI, dinilai pada gabungan |

Keduanya bergabung menjadi `01095060001343521726123110LOT-ABC`, HRI
`(01)09506000134352 (17)261231 (10)LOT-ABC`, yang valid, sehingga simbol digambar. AI `17`
berpanjang tetap, jadi tidak perlu FNC1 setelahnya. Gabung menyisipkan satu di mana
pun standar mengharuskannya.

**Simpan laporan** mengekspor laporan gabungan sebagai HTML atau PDF, sama seperti laporan
penguraian (lihat [Mengekspor](#mengekspor)), begitu ada satu kode batang. Nama berkas HTML-nya
diawali `gaia-combine-`.

Laporan mengikuti bahasa dan tema yang dipilih. Mengubah opsi penguraian atau bahasa memeriksa
ulang kode batang yang sudah tersimpan. Kode batang itu juga bertahan saat beralih ke
Urai atau Bangun dan kembali, tetapi tidak saat aplikasi dimulai ulang: kode
batang mode Gabung tidak disimpan.

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
| `options.properties` | Semua opsi penguraian, tema, apakah panel opsi terbuka, pemindai terakhir yang dipakai, mode saat jendela ditinggalkan, dan pengaturan pemeriksaan pembaruan — aktif atau tidak, kapan terakhir dijalankan, dan versi apa pun yang Anda pilih untuk dilewati |
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

Kode batang yang tersimpan dalam mode Gabung tidak disimpan: setelah dimulai ulang,
Gabung kosong kembali.

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

Dalam mode Gabung: <kbd>Enter</kbd> di kolom *Kode batang* menambahkan
kode batang. Di sana, ketik FNC1 sebagai `{GS}`; <kbd>⌘G</kbd> milik bilah masukan mode
Urai.

---

## Pemecahan masalah

| Gejala | Sebab dan solusi |
|---|---|
| Aplikasi tidak berjalan, atau melaporkan `NoClassDefFoundError` | Direktori `lib/` harus berada di sebelah berkas JAR. Simpan bersama, atau gunakan distribusi mandiri |
| *“Unsupported JavaFX configuration…”* di konsol saat mulai | Wajar dan tidak berbahaya — pustaka hanya mencatat bagaimana ia dimuat. Aplikasi berjalan normal |
| Pemisah yang Anda tempel tampak lenyap | Memang begitu: ia ditulis ulang menjadi token kasatmata `{GS}`, dan pengurai tetap menerima karakter yang sebenarnya. Lihat [token {GS}](#bilah-masukan-dan-token-gs) |
| Tidak ada tombol *Sambungkan pemindai* | Anda berada di macOS atau Linux, tempat penggerak pemindai tidak tersedia. Lihat [Pemindaian](#pemindaian) |
| Pemindai tidak mau tersambung | Periksa apakah nama logis pada pemilih cocok dengan model Anda, apakah pemindai terpasang, dan apakah tidak ada aplikasi lain yang menahannya. [Panduan penyiapan pemindai Zebra](GaiaDesktop-Zebra-Scanner-Windows-Indonesian.md#11-pemecahan-masalah) mencantumkan setiap kode galat. |
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
- **Gabung hanya menerima kode batang GS1-128, paling banyak tiga**, dan tidak
  menyimpannya antarsesi.
