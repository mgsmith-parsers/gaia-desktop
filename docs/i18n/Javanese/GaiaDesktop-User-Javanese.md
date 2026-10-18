# GAIA Desktop — Pandhuan panganggo

Dokumen iki nerangake carane nganggo aplikasi. Kanggo mbangun, ngowahi utawa nambah basa anyar,
delengen [pandhuan pangembang](../../GaiaDesktop-Developer.md) (nganggo basa Inggris). Dokumen
asli: [GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Isi

1. [Iki apa](#iki-apa)
2. [Mlakokake](#mlakokake)
3. [Jendhela](#jendhela)
4. [Mode analisis](#mode-analisis)
   - [Kolom input lan token {GS}](#kolom-input-lan-token-gs)
   - [Mindai](#mindai)
   - [Mbukak berkas](#mbukak-berkas)
   - [Riwayat rekaman](#riwayat-rekaman)
5. [Mode gawe](#mode-gawe)
   - [Panyunting AI](#panyunting-ai)
   - [Sing ora ditampa panyunting](#sing-ora-ditampa-panyunting)
   - [String elemen utawa Digital Link](#string-elemen-utawa-digital-link)
   - [Ngowahi rekaman sing wis ana](#ngowahi-rekaman-sing-wis-ana)
6. [Mode gabung](#mode-gabung)
   - [Wilah gabung](#wilah-gabung)
   - [Sing ora ditampa gabung](#sing-ora-ditampa-gabung)
   - [Dobel lan pasulayan](#dobel-lan-pasulayan)
   - [Laporan gabungan](#laporan-gabungan)
7. [Laporan](#laporan)
   - [Ekspor](#ekspor)
8. [Pilihan analisis](#pilihan-analisis)
   - [Mlakokake sepisanan](#mlakokake-sepisanan)
9. [Basa](#basa)
10. [Sing diélingi](#sing-diélingi)
11. [Trabasan keyboard](#trabasan-keyboard)
12. [Ngatasi masalah](#ngatasi-masalah)
13. [Watesan](#watesan)

---

## Iki apa

GAIA Desktop nindakake telung prakara marang data GS1:

- **Analisis** — ketikna string elemen GS1 utawa URI Digital Link, utawa tempelna, utawa bukaken
  saka berkas, utawa pindaien, banjur wacanen asile minangka laporan jangkep: saben Application
  Identifier (AI) iku tegese apa, kaluputane ana ing ngendi, lan apa sababe;
- **Gawe** — nggandhengake pasangan AI lan nilai supaya dadi string elemen utawa URI Digital Link;
- **Gabung** — nggabungake nganti telung kode bar GS1-128 saka siji label dadi siji string
  elemen sing bakal digawa GS1 DataMatrix, mriksa kanthi wutuh, lan nggambar simbole.

Kabeh sing dimangerteni aplikasi iki bab GS1 — teges saben AI, saben aturan pamriksan, saben pesen
kaluputan lan saben rincian basa — kabeh saka mesin GAIA sing mlaku ing sangisore. Aplikasi ora
nambah aturan dhewe: sing ditampa persis padha karo sing ditampa mesin.

---

## Mlakokake

```bash
java -jar gaia-desktop.jar
```

Berkas JAR iki butuh direktori `lib/` sing dikirim bareng — simpenen jejer, yen ora aplikasi ora
bakal mlaku. (Berkas JAR sing jangkep dhewe uga bisa digawe; delengen
[pandhuan pangembang](../../GaiaDesktop-Developer.md#packaging).)

Kudu ana **Java 21 utawa sing luwih anyar**. Distribusi digawe kanggo siji sistem operasi lan siji
arsitektur prosesor, mula gunakna berkas JAR sing digawe kanggo mesinmu.

Kanggo mbukak aplikasi nganggo basa tartamtu tanpa ngowahi basa sing wis disimpen:

```bash
java -jar gaia-desktop.jar --language=FR
```

Nilaine bisa arupa kode katalog (`FR`, `PCM`, `ARZ`), jeneng lengkap (`FRENCH`, `WU-CHINESE`),
utawa tag basa (`fr`; kanggo Kanton `zh-HK`, kanggo Arab Mesir `ar-EG`) — aksara gedhe lan cilik
ora mbedakake. Prentah iki mung ditrapake kanggo lakon iku wae: basa sing wis disimpen tetep kaya
mau, mula trabasan sing kokgawekake kanca ora bisa ngowahi basa sing dienggo mbukak salinanmu
dhewe. Nanging milih basa saka *njero* aplikasi iku pilihan tenanan lan disimpen kaya adate. Nilai
sing ora nuduhake basa apa wae bakal ora digatekake kanthi cathetan ing konsol, lan aplikasi tetep
mbukak nganggo basa sing wis disimpen tinimbang nolak mlaku.

Nalika wiwitan, konsol nyithak sabaris sing ngemot versi aplikasi, tanggal panggawene lan versi
mesin. Baris iku uga sing kudu disertakake ing saben laporan masalah.

---

## Jendhela

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

> Kaya kabeh blok kode liyane ing dokumen iki, gambar iki tetep nganggo label basa Inggris. Ing
> layar sing bakal kokdeleng yaiku *Urai* / *Gawe* / *Gabung*, *Rekaman*, *Simpen laporan*, *Pilihan*,
> *scanner: mati* lan *Bab*.

**Ing macOS lan Linux baris scanner ing gambar iki pancen ora ana** — ora ana tombol ing wilah
piranti, ora ana dhaftar pamilihan piranti, ora ana menu *Scanner*, lan ora ana *scanner: mati*
ing wilah status. Delengen [Mindai](#mindai).

Saklar **Urai ⇄ Gawe** mung ngganti wilah ing sangisore wilah piranti lan kontrol sing gegayutan
karo rekaman. Riwayat, laporan, panel pilihan lan kabeh tumindak ekspor mlaku ing rong mode
kasebut, amarga panggawean sing kasil iya mung dadi rekaman liyane. Mode **Gabung** tata letake beda: delengen [Mode gabung](#mode-gabung).

Panel pilihan **ketutup nalika dilakokake sepisanan**. Mode, katon utawa ora katone panel, tema
lan scanner sing pungkasan dienggo — kabeh diélingi kanggo mengko.

**Versi anyar.** Sawetara detik sawise diwiwiti, paling akeh sepisan sedina, aplikasi takon marang
GitHub apa ana GAIA Desktop sing luwih anyar wis dirilis. Yen ana, ing wilah status metu pranala —
*GAIA Desktop 1.1.0 wis kasedhiya* — sing mbukak kaca rilis ing panjelajahmu; ora ana sing diundhuh utawa
dipasang kanggo kowe. *Lewati versi iki* ndhelikake kabar iku nganti rilis sabanjure. Pamriksan iki
meneng wae yen ora ana sing anyar utawa GitHub ora bisa digayuh.
*Pitulung → Priksa nganyari…* langsung mriksa lan ngandhani asile;
*Pitulung → Priksa nganyari kanthi otomatis* mateni lan nguripake maneh pamriksan saben dina.

---

## Mode analisis

### Kolom input lan token {GS}

Kolom iki nampa string elemen, URI GS1 Digital Link, utawa asil pindai mentah karo ater-ater AIM
Code ID (`]d2…`). <kbd>Enter</kbd> miwiti analisis.

Sing misahake AI kanthi dawa sing owah-owahan yaiku **FNC1** — aksara kontrol sing ora katon
`0x1D`. Ora ana kolom teks sing bisa ngemot aksara iki: aksara mau ilang tanpa swara, banjur
`01…` + FNC1 + `10LOT` tekan kana dadi `01…10LOT` — yaiku string elemen sing tegese wis owah.

Mula aplikasi nuduhake pamisah minangka token sing katon **`{GS}`** lan mbalekake maneh nalika
lumaku menyang pangurai:

- **Tombol `{GS}`** nglebokake token ing panggonan kursor;
- Nalika aksara pamisah sing asli **ditempelake** — saka keyboard, saka menu konteks utawa kanthi
  disered — aksara mau dhewe malih dadi `{GS}`, mula ora ana sing ilang;
- Token ditulis persis kaya sing ditulis laporan, mula nilai sing disalin saka laporan bisa
  ditempelake bali langsung.

Rekaman, laporan lan riwayat sing disimpen — katelune ngemot pamisah sing **asli**. Token mung
sing kokdeleng lan kokketik.

### Mindai

> **Mung Windows.** Zebra mung nyedhiyakake panggerak scanner kanggo Windows, mula ing macOS lan
> Linux aplikasi pancen ora nggawe kontrol scanner apa wae — ora ana tombol *Sambungake scanner*,
> ora ana dhaftar piranti, ora ana menu *Scanner*, ora ana status scanner ing wilah status. Ora
> ana sing ditampilake surem, amarga pancen ora ana setelan utawa piranti sing bisa nguripake.
> Kabeh liyane, kalebu mbukak berkas sing kebak asil pindai simpenan, mlaku ing ngendi wae.

*Sambungake scanner* mbukak piranti sing disebut ing dhaftar; `ZebraAllScanners` iku nilai standar
lan cocog kanggo akeh-akehe model. Saben asil pindai diurai lan dhewe mlebu menyang riwayat.

Data scanner diwaca bait mbaka bait, mula ater-ater AIM Code ID lan saben pamisah FNC1 tekan
pangurai persis kaya nalika dikirim scanner.

Ing mode gawe kontrol scanner didhelikake, nanging scanner sing kasambung tetep mlaku:
*Scanner → Sambungake / Pedhot* (<kbd>⌘K</kbd>) isih ngganti, wilah status isih ngabari, lan asil
pindai sing teka nalika panyunting menga uga direkam. Ing mode Gabung kontrol iki tetep ana, lan saben pindaian ditambahake menyang gabungan, dudu
menyang riwayat.

Nyetel scanner sepisanan — Zebra Scanner SDK, mode USB scanner, lan setelan GS1 — dijlentrehake langkah
saben langkah ing [pandhuan nyetel scanner Zebra](GaiaDesktop-Zebra-Scanner-Windows-Javanese.md), sing ana ing saben zip ing sangisore
`docs/`. Yen ora ana piranti sing nyambung, delengen [Ngatasi masalah](#ngatasi-masalah).

### Mbukak berkas

*Berkas → Mbukak berkas teks…* ngurai **saben baris sing ora kosong minangka rekaman dhewe** —
iki dalan paling cepet kanggo ngrampungake tumpukan asil pindai sing dikirimake wong marang kowe.
Berkas diwaca bait mbaka bait, mula pamisah asli sing ana ing njerone uga tetep slamet.

### Riwayat rekaman

Sing anyar ing ndhuwur. Saben baris nuduhake titik valid / ora valid, ringkesan sabaris, lan ing
sandhinge wektu rekaman, sumbere lan cacahe AI ing njerone. Tanggal mung metu ing rekaman dina-dina
kepungkur; baris dina iki mung ngemot wektu.

Ringkesan kanggo Digital Link yaiku **URI Digital Link**, lan kanggo liya-liyane yaiku **HRI**:
kanggo string elemen, maca `(01)09506000134352 (17)261231` luwih gampang tinimbang
`01095060001343521726123110…`.

Kanthi klik tengen ing sawijining rekaman, inputé utawa HRI-ne bisa disalin, dibalekake menyang
kolom input, dibukak ing panyunting, dibusak, utawa kabeh riwayat diresiki.

---

## Mode gawe

### Panyunting AI

Tabel isi pasangan AI lan nilai. Ketikna kode AI, utawa pilihen saka dhaftar *Tambah AI…* saka
antarane **kabeh AI sing dimangerteni mesin**; nalika kokketik, kolom **Irah-irahan**, **Format**
lan **Katrangan** kebak nganggo basa sing kokpilih. Dhaftar ora nawakake AI sing bakal ditolak
nalika digawe.

Ing pungkasane tabel tansah ana **sabaris kosong**: tulisen apa wae ing kono banjur sing candhake
metu, mula kowe ora bakal tau butuh *Tambah baris* kanggo string sing akeh AI-ne.

Panyuntingan tumindak kaya sing kudune ditindakake tabel: <kbd>Enter</kbd>, <kbd>Tab</kbd>,
<kbd>Shift</kbd>+<kbd>Tab</kbd> lan klik ing panggonan liya — kabeh **nyimpen** sing wis kokketik;
<kbd>Esc</kbd> nyingkur. <kbd>Tab</kbd> lumaku saka AI menyang nilai banjur menyang baris candhake,
ngliwati kolom sing ora bisa disunting.

### Sing ora ditampa panyunting

| | Sing kedadeyan |
|---|---|
| AI sing ora dikenal mesin | Baris dadi abang nalika kokketik; panggawean ditolak kanthi katrangan |
| AI sing padha kaping pindho saka dhaftar | Ora ditambahake — pesen nyebutake jenenge, lan baris sing wis ngemot dipilih |
| AI sing padha kaping pindho kanthi diketik dhewe | Rong baris kaparingan tandha; panggawean ditolak kanthi kaluputan `GE-S004` |
| Ora ana sing digawe | Siji pesen; ora ana rekaman sing digawe |

AI mung bisa metu sepisan ing sawijining string elemen — mula dhaftar langsung nolak tinimbang
ngeculake kowe nyoba panggawean sing pancen wis mesthi gagal.

### String elemen utawa Digital Link

**Gawe string elemen** nggandheng baris-baris lan nyelehake pamisah FNC1 ing saben panggonan sing
dikarepake standar. **Gawe Digital Link** ngasilake URI; supaya ngarah menyang panyawismu dhewe
tinimbang `id.gs1.org` sing lumrah, bukaken *Target Digital Link*:

| Kolom | Pangaribawane | Yen dikosongake |
|---|---|---|
| Domain | inang panyawis, kayata `example.com` | `id.gs1.org` |
| Ater-ater path | bagean sadurunge path AI, kayata `/products` | Tanpa ater-ater |

Yen **kasil**, asile tumindak persis kaya asil pindai: diurai, mlebu riwayat, ditampilake minangka
laporan lan disimpen. Panggawean mriksa asile dhewe kanthi ngurai maneh, mula sing dibalekake mesthi
valid.

Yen **gagal**, ora ana sing direkam. Sababe kari ing sangisore tombol panggawean — sabaris kanggo
saben masalah, saben-sabene ngemot kaluputan, AI sing gegayutan lan katrangane mesin dhewe:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> Tandha petik pindho ing pesen kuwi dudu saka aplikasi iki, nanging saka katalog kaluputane mesin
> dhewe.

### Ngowahi rekaman sing wis ana

Klik tengen ing rekaman → **Gawe** nggawa rekaman mau menyang panyunting AI. Owahana sawijining
nilai, gawenen maneh, lan asile metu minangka rekaman anyar — rekaman asline tetep kaya mau.

---

## Mode gabung

Label-label lawas kerep mbagi datane menyang **sawetara kode bar GS1-128** — GTIN ing siji, tanggal
kadaluwarsa lan batch ing liyane — amarga siji kode bar linear ora cukup kanggo kabeh. Mode
Gabung njupuk nganti **telu** kode bar kaya mangkono, nggabungake dadi siji string elemen sing
bakal digawa **GS1 DataMatrix**, lan mriksa asile kanthi wutuh. Sawise asile sah, aplikasi nggambar simbol
DataMatrix.

Ora ana siji-sijia kode bar iku sing kudu jangkep dhewe. Nomer batch tanpa GTIN-e gagal dhewe kanthi
`GE-S005`, nanging iku lumrah kanggo siji kode bar saka sakumpulan, mula mode Gabung mriksa
ing gabungan.

### Wilah gabung

Ngalih menyang **Gabung** ngganti kolom input nganggo wilah ing pinggir ngisor jendhela.
Riwayat rekaman nyingkir, amarga pindaian mode Gabung dudu rekaman lan ora tau mlebu ing kono.
Bageyan laporan nuduhake laporan gabungan minangka gantine:

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

Ana rong cara nglebokake kode bar:

- **Mindai.** Ing Windows, kontrol scanner tetep ana ing wilah piranti ing mode Gabung, lan
  saben pindaian langsung mlebu gabungan, dudu riwayat.
- **Ngetik utawa nempelake** ing kolom *Kode bar* banjur mencet <kbd>Enter</kbd> utawa
  **Tambah**. Tulisen FNC1 dadi **`{GS}`**, padha kaya ing kolom input. Kode bar sing diketik
  tanpa AIM Code ID dianggep GS1-128.

| Tombol | Gunane |
|---|---|
| **Tambah** | Nambahake kode bar sing ana ing kolom |
| **Busak pungkasan** | Mbusak kode bar sing paling anyar (ora kasedhiya yen durung ana) |
| **Setel ulang** | Mbusak kabeh kode bar, ngresiki kolom lan panolakan apa wae, lan miwiti maneh |

Wilah status tansah ngandhani pira kode bar sing dicekel (*2 saka 3 kode bar GS1-128*), utawa kenapa sing
pungkasan ditolak.

### Sing ora ditampa gabung

Kode bar sing ditolak ora ditambahake. Laporan lan wilah status padha ngandhani sababe:

| Kode bar | Ditolak amarga |
|---|---|
| Kolom kosong | Ora ana sing ditambahake |
| Kode bar kaping papat | Siji label digabung saka paling akeh telu. Busaken sing pungkasan, utawa setel ulang |
| Simbologi liya, miturut AIM Code ID-ne | Mung GS1-128 sing digabung, mula GS1 DataMatrix (`]d2`) utawa QR Code ditolak, kanthi jenenge |
| AI sing ora bisa diwaca | Kasalahan sintaksis; kode kasalahan lan pesen mesin ditampilake |
| Ora ana AI babar pisan | Ora ana sing digabung |
| Sing wis tau dipindai | String elemen sing padha kaping pindho, upamane kode bar sing padha dipindai maneh; laporan ngandhani kode bar sadurunge endi sing dibaleni |

Mode Gabung mung nolak apa sing babar pisan ora bisa dienggo. Liyane kabeh (digit priksa sing
salah, tanggal sing ora bener, AI pendamping sing ilang) ditampa lan ditaksir ing gabungan.

### Dobel lan pasulayan

AI digabung miturut urutan pindaian. Yen loro kode bar nggawa AI sing padha:

- **kanthi nilai sing padha**, AI iku disimpen sapisan lan dicathet ing sangisore
  *Dobel — disimpen sapisan*, bebarengan karo kode bar sing diwaca;
- **kanthi nilai sing beda**, iku **pasulayan**. Putusane muni *PASULAYAN*, nilai sing
  pasulayan dicathet saben kode bar, lan **ora ana sing digabung** nganti rampung. Busaken kode bar sing
  salah, utawa setel ulang.

### Laporan gabungan

Saka dhuwur mudhun:

1. **Putusan**: *durung ana pindaian*, *PASULAYAN*, sah utawa ora sah, lan pira kode bar
   sing dicekel.
2. **Pindaian**: saben kode bar kaya tekane, simbologine, lan string elemen sing kari sawise AIM Code ID
   dicopot. Masalah sing mung bisa diputus dening gabungan dituduhake ing kode bar iku, ditandhani
   *ditaksir ing gabungan*.
3. **Dobel** lan **pasulayan**, yen ana.
4. **Asil gabungan**: string elemen, HRI-ne lan dawane, banjur input gabungan minangka pita
   mawa label lan siji kertu saben AI, loro-lorone ditata kaya ing [laporan analisis](#laporan).
   Sabanjure saben kasalahan lan pepéling ing gabungan dicathet ing sangisore
   *Validasi*.
5. **Simbol GS1 DataMatrix**, kanthi HRI-ne ing ngisor. Mung digambar yen gabungane sah. Yen ora,
   laporan ngandhani kenapa ora digambar, supaya simbol sing ora sah ora kecithak kanthi ora sengaja.

Contone, label kanthi rong kode bar:

| Kode bar | Nggawa | Dhewe |
|---|---|---|
| `]C10109506000134352` | (01) GTIN | sah |
| `]C11726123110LOT-ABC` | (17) tanggal kadaluwarsa, (10) batch | `GE-S005` kanggo loro AI, ditaksir ing gabungan |

Kode bar iku digabung dadi `01095060001343521726123110LOT-ABC`, HRI
`(01)09506000134352 (17)261231 (10)LOT-ABC`, sing sah, mula simbole digambar. AI `17` dawane tetep,
mula ora perlu FNC1 sawise. Mode Gabung nglebokake siji ing ngendi wae standar mbutuhake.

Sanalika ana siji kode bar sing dicekel, **Simpen laporan** ngekspor laporan gabungan dadi HTML utawa
PDF, padha kaya laporan analisis (delengen [Ngekspor](#ekspor)). Jeneng berkas HTML-e diwiwiti
`gaia-combine-`.

Laporan ngetutake basa lan tema sing dipilih. Ngganti pilihan analisis utawa basa mriksa maneh kode bar
sing dicekel. Kode bar iku uga tetep ana sawise ngalih menyang Urai utawa Gawe lan
bali, nanging ora sawise diwiwiti ulang: kode bar mode Gabung ora disimpen.

---

## Laporan

Rekaman sing dipilih ditampilake minangka laporan jangkep: wilah putusan, string elemen sing
dipérang dadi “pita” sing ana jenenge, metadata analisis, sakartu kanggo saben AI sarta tegese, lan
kaluputan sarta pènget bebarengan karo tuntunan mbenerake.

Kanggo **Digital Link**, saben kartu AI uga nyebutake peran strukturale — **kunci utama**,
**panentu kunci** utawa **atribut data** — nganggo werna sing ngetutake urutan tekane ing URI.
Peran-peran iki mung katon ing Digital Link: ing string elemen lumrah, AI pancen ora duwe papan
ing URI sing bisa diterangake.

Laporan ngetutake basa lan tema sing kokpilih.

### Ekspor

| | |
|---|---|
| **Simpen laporan → Minangka HTML…** (<kbd>⌘S</kbd>) | Nulis persis sing ana ing layar: berkas sing jangkep dhewe lan bisa dibukak ing browser apa wae |
| **Simpen laporan → Minangka PDF…** (<kbd>⌘P</kbd>) | Nyithak laporan liwat kothak cithak sistem — pilihen tujuan PDF ing kono |

PDF liwat kothak cithak amarga panggonan lan jeneng berkas dipilih ing kono: ing macOS
**PDF ▾ → Save as PDF**, ing Windows **Microsoft Print to PDF**, ing Linux **Print to File**.
Kaca dipérang kanthi bener lan teks tetep bisa dipilih.

Cithak lan PDF tansah nganggo tema padhang, senajan aplikasi lagi ana ing tema peteng, lan tetep
ngemot tandha AI sing werna-werna sarta wilah putusan kaya mau.

Yen ing mesinmu pancen durung ana printer utawa tujuan PDF, aplikasi ngandhani lan nuduhake dalan
menyang ekspor HTML, sing bisa dicithak saka browser apa wae.

---

## Pilihan analisis

Panel sisih tengen nglumpukake kabeh pilihan mesin ing sapapan. Yen ana sing diowahi, **rekaman
sing dipilih langsung diurai maneh**, mula panel iki ora mung mangaribawani pindai candhake — iki
uga dalan kanggo takon: “Input iki bakale kaya apa nganggo setelan iki?”.

| Pilihan | Nilai |
|---|---|
| Mode analisis | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — sepira jerone analisis lumaku |
| Basa | 35 katalog pesen |
| Urutan | `LITTLE` (dd/mm/yyyy), `MIDDLE` (mm/dd/yyyy), `BIG` (yyyy/mm/dd) |
| Pamisah | `SLASH`, `HYPHEN`, `PERIOD` |
| Sasi | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Taun | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Lewati pamriksan "requires" | Mandhegake panolakan AI sing kanca wajibe ora ana (`GE-S005`) |
| Lewati pamriksan "excludes" | Mandhegake panolakan pasangan AI sing dilarang standar (`GE-S006`) |

Ing sangisore panel, *Balekake menyang standar* mbalekake nilai standar mesin dhewe.

### Mlakokake sepisanan

Sasuwene durung ana sing disimpen, aplikasi njupuk setelane saka **setelan wilayah mesinmu**
tinimbang saka standar mesin:

- **Basa** teka saka lokal sistem, lan bali menyang basa Inggris ing ngendi katalog ora ana;
- **Pilihan tanggal** diwaca saka wangun tanggal cendhak lokalmu: urutan bagean, pamisahe, lan
  apa sasi sarta taun ditulis cekak apa ora.

> **Gatekna.** Mula lokal sing nulis tanggal cendhak nganggo taun rong angka bakal miwiti ing kene
> uga nganggo taun rong angka: iki cocog karo bagean liya sistemmu, nanging beda karo standar mesin
> dhewe sing patang angka. Owahana ing panel, lan pilihan mau bakal tetep.

---

## Basa

Milih basa ora mung ngganti pesen kaluputan, nanging **aplikasi sakabehe** — menu, tombol,
panyunting, panel pilihan, baris status lan laporan. Basa sing ditulis saka tengen ngiwa mbalik
jendhela lan laporan.

Kabeh 35 basa cumawis, kanthi jangkoan rong lapis:

| | Jangkoan |
|---|---|
| Laporan analisis lan kabeh pesen kaluputan mesin | **Kabeh 35 basa** |
| Menu, tombol lan label aplikasi dhewe | **Kabeh 35 basa** |

Basa sing ditulis saka tengen ngiwa — Arab, Arab Mesir, Persia lan Urdu — mbalik jendhela lan
laporan wiwit sanalika aplikasi mbukak.

Basa diélingi kanggo mengko. Kanggo mbukak nganggo basa liya mung sepisan, delengen
[`--language`](#mlakokake).

Ana barang-barang sing pancen sengaja ora dijarwakake ing basa apa wae: jeneng kolom cekak sing
dicithak jejer nilai mentah (`format`, `position`, `raw`, `len` …) — iki tandha pangenal, dudu
tembung; jeneng pola lan simbologi — iki tembung tekniking GS1 lan industri; sarta sak paragraf
dawa saka standar, sing jarwan setengah malah luwih mbingungake tinimbang mbeningake.

---

## Sing diélingi

Rong berkas ing `.gaia-desktop` sajrone direktori omahmu:

| Berkas | Isine |
|---|---|
| `options.properties` | Kabeh pilihan analisis, tema, panel pilihan menga apa ora, scanner sing pungkasan dienggo, jendhela ditinggal ing mode apa, lan setelan pamriksan nganyari — urip utawa mati, kapan pungkasan mlaku, lan versi apa wae sing kokpilih dilewati |
| `captures.tsv` | Riwayat rekaman, sing anyar dhisik, wates 500 |

Saben kowe ngowahi sawijining pilihan, pilihan mau ditulis; saben riwayat owah, riwayat mau
ditulis. Nalika metu, karo-karone ditulis maneh.

Mbusak salah siji saka rong berkas mau mung mbalekake bagean iku menyang kaanan wiwitane lan ora
liya — mula karo-karone dipisah, supaya riwayat sing rusak ora nggawa lunga setelanmu pisan.
Sanajan berkas sing rusak marga disunting dhewe uga ora mateni: saben setelan bali menyang
standare dhewe-dhewe, mula regane sabaris sing salah ora luwih saka baris iku.

Riwayat nyimpen input, dudu analisise. Nalika dimuat, input mau diurai maneh, tegese rekaman sing
dibalekake nggambarake pilihan sing lumaku **saiki**, dudu sing lumaku nalika rekaman mau dijupuk.

Kode bar sing dicekel ing mode Gabung ora disimpen: sawise diwiwiti ulang, mode
Gabung wiwit kosong.

---

## Trabasan keyboard

| | |
|---|---|
| <kbd>Enter</kbd> | Urai kolom input |
| <kbd>⌘O</kbd> | Mbukak berkas teks |
| <kbd>⌘S</kbd> | Simpen laporan minangka HTML |
| <kbd>⌘P</kbd> | Simpen laporan minangka PDF (kothak cithak sistem) |
| <kbd>⌘G</kbd> | Lebokake token pamisah FNC1 |
| <kbd>⌘K</kbd> | Sambungake / Pedhot scanner (mung Windows) |
| <kbd>⌘D</kbd> | Ganti tema peteng |
| <kbd>⌘Q</kbd> | Metu |

Ing panyunting: <kbd>Tab</kbd> / <kbd>Shift</kbd>+<kbd>Tab</kbd> lumaku ing antarane kolom sing
bisa disunting, <kbd>Enter</kbd> mesthekake, <kbd>Esc</kbd> mbatalake.

Ing mode Gabung: <kbd>Enter</kbd> ing kolom *Kode bar* nambahake kode bar.
Ing kono, ketiken FNC1 dadi `{GS}`; <kbd>⌘G</kbd> kagungane kolom input mode Urai.

---

## Ngatasi masalah

| Tandha | Sabab lan tambane |
|---|---|
| Aplikasi ora mlaku, utawa metu `NoClassDefFoundError` | Direktori `lib/` kudu ana ing sandhinge berkas JAR. Simpenen jejer, utawa gunakna distribusi sing jangkep dhewe |
| Ing konsol nalika wiwitan metu *“Unsupported JavaFX configuration…”* | Wis samesthine lan ora mbebayani — piranti mung ngandhakake carane dimuat. Aplikasi mlaku kaya adate |
| Pamisah sing ditempelake katon ilang | Pancen kudune ngono: pamisah mau malih dadi token sing katon `{GS}`, lan pangurai nampa aksara asline. Delengen [token {GS}](#kolom-input-lan-token-gs) |
| Tombol *Sambungake scanner* pancen ora ana | Kowe ana ing macOS utawa Linux, sing ora ana panggerak scanner. Delengen [Mindai](#mindai) |
| Scanner ora nyambung | Priksanen apa jeneng logis ing dhaftar cocog karo modelmu, apa scanner wis dicolokake, lan apa ora ana aplikasi liya sing nyekel. [Pandhuan nyetel scanner Zebra](GaiaDesktop-Zebra-Scanner-Windows-Javanese.md#11-ngatasi-masalah) nyathet saben kode kasalahan. |
| Ekspor PDF kandha ora ana tujuan | Ing mesinmu durung ana printer utawa tujuan PDF. Tambahna siji, utawa ekspor minangka HTML banjur cithaken saka browser |
| Ing salah sawijining item menu katon kaya `act.hri` | Ana label sing kliwatan — mangga laporna; iki dudu setelan, nanging cacad nalika mbangun |

---

## Watesan

- **Mindai mung ing Windows**, amarga panggerake ana ing kono.
- **PDF liwat kothak cithak**, dudu kothak simpen.
- **Target Digital Link mung nampa domain lan ater-ater path**; ora ana parameter pitakon
  tambahan utawa fragmen.
- **AI mung metu sepisan** ing sawijining string elemen — iki aturan GS1, dudu watesan aplikasi.
- **Mode Gabung mung nampa kode bar GS1-128, paling akeh telu**, lan ora nyimpen ing antarane
  saben mlaku.
