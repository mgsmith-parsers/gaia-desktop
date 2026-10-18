# GAIA Desktop — Pandhuan pangembang

Carane modul iki dibangun, ditata lan diuji. Kanggo apa sing ditindakake aplikasi yen dideleng saka njaba, delengen
[Pandhuan panganggo](GaiaDesktop-User-Javanese.md). Dokumen asli:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Isi

1. [Modul sakklebatan](#modul-sakklebatan)
2. [Mbangun lan mlakokake](#mbangun-lan-mlakokake)
   - [Pambungkusan](#pambungkusan)
3. [Arsitektur](#arsitektur)
   - [Corong input](#corong-input)
   - [Thread](#thread)
   - [Mbangun maneh tinimbang menehi label maneh](#mbangun-maneh-tinimbang-menehi-label-maneh)
4. [Token {GS}](#token-gs)
5. [Integrasi scanner](#integrasi-scanner)
   - [Gerbang platform](#gerbang-platform)
   - [Simbologi](#simbologi)
6. [Lokalisasi](#lokalisasi)
   - [Argumen `--language`](#argumen---language)
   - [Nambah basa](#nambah-basa)
   - [Sing sengaja ora diterjemahake](#sing-sengaja-ora-diterjemahake)
7. [Format kahanan sing disimpen](#format-kahanan-sing-disimpen)
8. [Pangujian](#pangujian)
9. [Ndandani masalah build](#ndandani-masalah-build)
10. [Watesan sing wis dingerteni lan gawean sing isih ana](#watesan-sing-wis-dingerteni-lan-gawean-sing-isih-ana)

---

## Modul sakklebatan

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Klien saka pustaka inti, ora luwih. Modul iki ora ngemot **kawruh GS1 apa wae**: saben definisi AI, aturan validasi,
pesen kesalahan lan katrangan sing dilokalisasi asale saka [`GaiaParser`](../../../../gaia/docs/GaiaParser.md) lan
[`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). Mligine, dhaptar AI ora tau disalin menyang modul iki — pamilih
ing builder maca registri duweke inti dhewe, mula ora bisa nawakake AI sing bakal ditolak builder.

**Titik mlebu:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (`Application` JavaFX) lan
`tools.pantheum.gaia.desktop.Launcher` (`Main-Class` saka jar sing wis dibungkus).

Modul iki godhong: ora ana sing gumantung marang modul iki, lan modul iki ora gumantung marang modul hilir liyane.

---

## Mbangun lan mlakokake

- **Java 21** kanggo mlakokake, lan JDK 21 utawa sing luwih anyar kanggo mbangun — modul dikompilasi nganggo
  `release=21`. JavaFX 21 yaiku jalur LTS sing cocog. Jar inti isih bytecode Java 11 lan mlaku tanpa owah ing 21.
- **Maven** ing `PATH`. Ora ana wrapper ing repo iki.
- **Pasang inti dhisik.** Modul iki njupuk `gaia:1.1.0` saka `~/.m2`; jar inti sing wis lawas utawa ora ana iku sing
  biasane dadi sababe `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` iku siji-sijine argumen baris prentah, lan siji-sijine cara kanggo mangaruhi wiwitan saliyane
`-Dgaia.desktop.home=…`, sing ngarahake kabeh profil menyang panggonan liya.

`mvn javafx:run` yaiku dalan pangembangan: plugin nyelehake JavaFX ing **module path**, mula toolkit diwiwiti minangka
modul sing duwe jeneng lan `GaiaDesktopApp` bisa diluncurake langsung. Jar sing wis dibungkus ora bisa kaya mangkono —
kabeh ana ing class path — mula `Main-Class`-e yaiku shim `Launcher`. Subkelas `Application` minangka `Main-Class` ing
tata letak kuwi gagal kanthi *"JavaFX runtime components are missing"*.

Tansah gunakake `clean` — sepisan, sadurunge mbangun sakumpulan platform, amarga printah iku ngosongake `target/dist/`
saka kabeh platform. Delengen [Ndandani masalah build](#ndandani-masalah-build) kanggo alesane.

### Pambungkusan

Kabeh sing bisa dikirim diklumpukake ing ngisor `target/dist/gaia-desktop-<version>-<platform>/` lan ora ana liyane, supaya
direktori iku bisa di-zip utawa disalin dadi siji set tanpa milih artefak saka `target/` ing sandhinge `classes/`,
`maven-status/` lan berkas sauntara build liyane. `${dist.dir}` menehi jeneng sepisan; saben plugin sing nulis artefak
nuding menyang kono.

#### Siji build saben platform

Jar JavaFX nggawa kode native, mula saben distribusi mung kanggo siji platform, sing dijenengi dening properti
`fx.platform` minangka classifier OpenJFX: `mac-aarch64`, `mac`, `win`, `linux` utawa `linux-aarch64`. Lima profil
`fx-*` sing aktif miturut OS nyetel menyang platform mesin sing mbangun dhewe, lan mung iku sing dibutuhake
`mvn package` utawa `mvn javafx:run` biasa; `-Dfx.platform=…` ngalahake profil-profil iku kanggo mbangun platform liya
apa wae, saka mesin apa wae — jar JavaFX mung undhuhan. `${dist.dir}` yaiku `target/dist/${fx.platform}`, mula build
siji platform ora tau nimpa build platform liyane.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Ana rong rincian pom sing ndadekake iki mlaku, lan kekarone katon kaya bisa dibusak:

- **Saben modul JavaFX iku dependensi langsung** — base, graphics, controls, media lan web — saben siji nganggo
  `<classifier>${fx.platform}</classifier>` lan pangecualian `org.openjfx:*`. Pom OpenJFX milih jar transitif miturut
  OS *mesin sing mbangun*, mula yen graphics utawa base dijarke teka kanthi transitif, native Mac bakal mlebu ing build
  Windows ing sandhinge native Windows.
- **Propertine `fx.platform`, dudu `javafx.platform`.** Yen disetel saka njaba, properti duweke OpenJFX dhewe
  didefinisikake maneh dening pom induke nganggo awake dhewe; Maven nolak pom iku amarga ora valid lan kanthi meneng
  mbuwang jar platform, nyisakake build sing babar pisan ora ana JavaFX-e.

Kekarone tata letak nulis path sing padha, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`, lan pas siji
wae sing mlaku saben build. `${jar.phase}`, `assembly.skipAssembly` lan `mdep.skip` sing nyegah kekarone padha
ngaku-aku.

| | Gawan | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — mung kelas lan sumber daya modul iki | ~50 MB — dependensi dibukak ing njerone |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 jar, dijenengi siji-siji ing `Class-Path` manifest | — |
| Sing dikirim | jar **lan** `lib/` ing sandhinge | siji berkas |
| Uga ing `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) utawa `run.sh` (liyane) | padha |
| Lan | `readme.html` lan `docs/` (saben platform) | padha |

Saben distribusi uga nggawa pandhuan: `docs/` disalin kanthi tata letake tetep, mula pranala antarane pandhuan lan
terjemahan isih mlaku, kajaba piranti terjemahan (`check-i18n.py`, `i18n-manifest.json`); lan
`readme.html` ing oyod, indeks sing nyambung menyang saben pandhuan, saka `src/main/dist/readme.html` kanthi `@VERSION@`
diganti versi pom. Kekarone ditulis dening langkah antrun `pick-launch-script`, sing mbusak `docs/` dhisik supaya
pandhuan sing wis dibusak ora keri. Pranala readme ditulis tangan — pandhuan sing diganti jenenge utawa terjemahan sing
ditambahake ing ngisor `docs/` uga butuh barise ing kana.

`Class-Path` manifest lan `lib/` iku rong separo saka barang siji: jar nyebut `lib/<artifact>-<version>.jar` kanggo saben
dependensi runtime, lan `copy-dependencies` ngisi `lib/` nganggo jeneng-jeneng iku persis. Iku sing ndadekake
`java -jar` mlaku.

#### Aplikasi macOS lan zip distribusi

Siji eksekusi antrun, `mac-app-and-zip`, mungkasi fase `package`. Amarga iku plugin antrun diumumake **sawise** plugin
jar lan assembly — Maven mlakokake eksekusi siji fase miturut urutan pangumuman plugin — mula jar wis ana, ing
pambungkusan apa wae, nalika eksekusi iki mlaku. (Eksekusi liyane, `pick-launch-script`, ana ing `prepare-package` lan ora
kena pengaruh.)

Kanggo `mac` lan `mac-aarch64`, sepisanan nggawe `GAIA Desktop.app` ing direktori distribusi: `Info.plist` (kanthi
`@VERSION@` diganti versi pom), launcher `Contents/MacOS/gaia-desktop` (digawe bisa dieksekusi) lan `gaia-desktop.icns`,
kabeh saka `src/main/macos/`, banjur **mindhah** `gaia-desktop.jar` lan `lib/` menyang `Contents/Java`, supaya bundel
bisa ngadeg dhewe. `run.sh` nggoleki ing kana yen ora ana jar ing sandhinge. Launcher nggoleki Java miturut versi
(`java_home -v 21+`, banjur openjdk saka Homebrew — peluncuran saka Finder ora weruh `JAVA_HOME` utawa `PATH`),
nuduhake dialog liwat `osascript` yen ora ana, `cd` menyang `~/.gaia-desktop` supaya `logs/` relatif duweke logback bisa
ditulisi, lan ngirim `-Xdock:name`/`-Xdock:icon`. macOS nggandhengake `java` sing lagi mlaku karo bundel (bundle id lan
ikon Dock-e duweke aplikasi); bar menu nuduhake GAIA Desktop.

Banjur, kanggo saben platform, direktori di-zip dadi `target/dist/gaia-desktop-<version>-<platform>.zip` ing ngisor
folder sing jenenge padha, nganggo `<zip>` saka Ant: `run.sh` lan `Contents/MacOS/**` dadi file set dhewe nganggo
`filemode="755"`, yen ora, kekarone ora bisa dilakokake sawise dibukak. Zip iki **ora dilampirake**: iki kanggo rilis
GitHub, lan papat platform asale saka papat build, sing ora bakal bisa ditampa Maven Central — siji unggahan sing ora
bisa diowahi saben versi — minangka siji rilis.

`project.build.outputTimestamp` disetel menyang wektu build. Yen ora, pengarsip ing maven-jar-plugin 3.5 lan
maven-assembly-plugin 3.8 menehi tanggal 1 Februari 1980 marang saben entri.

Ikon aplikasi digambar dening `tools/icon/GaiaIcon.java` (dudu bagean saka build; sirahe ngemot langkah
`sips`/`iconutil`). Asile wis di-commit: `src/main/macos/gaia-desktop.icns` kanggo bundel, lan
`src/main/resources/icons/gaia-desktop-{16..256}.png`, sing disetel `GaiaDesktopApp` minangka ikon jendhela (Windows
lan Linux; macOS ora nggatekake ikon jendhela).

#### Skrip peluncur

`run.sh` lan `run.bat` asale saka `src/main/scripts/`, disalin menyang `dist/gaia-desktop-<version>-<platform>/` tanpa
disaring — `run.sh` kebak ekspansi shell `${...}` sing bisa ditulis ulang sakarepe dening saringan Maven. Banjur siji
langkah antrun mung nyimpen skrip sing dilakokake platform iku — `run.bat` kanggo `win`, `run.sh` kanggo liyane — lan
nyetel bit eksekusi ing `run.sh`, amarga `copy-resources` ora nggawa bit iku. Langkah iki mutusake saka `${fx.platform}`
sing wis diresolusi tinimbang liwat profil, amarga aktivasi profil ora bisa ndeleng nilai sing disetel profil liya: yen
ora, build Linux sing digawe ing Windows bakal entuk skrip duweke host.

Skrip-skrip iki **ora gumantung marang manifest**. Saben skrip nyusun class path dhewe, `gaia-desktop.jar` diterusake
saben `.jar` ing `lib/`, dijenengi siji-siji tinimbang dipasrahake marang wildcard `lib/*`, supaya path sing ana spasine
tetep dadi siji entri lan cacah sing ditambahake iku nyata. Peluncuran nganggo `-cp` iku sing dadi alesane skrip nyebut
`tools.pantheum.gaia.desktop.Launcher` kanthi cetha: `-cp` ora maca `Main-Class` manifest. Nanging **tetep** ngetutake
`Class-Path`-e — jar ing `-cp` nggawa jar sing dijenengi manifest-e, sing digoleki langsung sawise jar iku — mula saben
jar ing `lib/` dimuat, didhaptar skrip utawa ora, lan ngilangi siji saka class path skrip ora ndadekake jar iku ana ing
njaba JVM. Skrip tetep ndhaptar kabeh, supaya jar sing manifest-e lawas, disunting tangan utawa kurang siji entri tetep
bisa diwiwiti. Skrip sing padha ngladeni kekarone pambungkusan: jar `-Pfat` ora duwe `lib/` kanggo ditambahake lan ora
butuh, mula `lib/` sing kosong mung cathetan, dudu kesalahan.

#### Native Zebra

Build ora ngirim apa wae duweke Zebra — ora ana DLL, ora ana jar layanan. Ing Windows kabeh iku asale saka Scanner SDK
Zebra sing dipasang ing mesin: `run.bat` nambahake jar layanan `Jpos*.jar` lan `bin\` menyang class path lan `bin\`
menyang `PATH`, ing kono `System.loadLibrary` nemokake `CSJPOSScanner.dll` / `CSJPOSScanner64.dll` (gawane
`java.library.path` yaiku `PATH`). API JavaPOS lan pamuat konfigurasine tansah asale saka `lib\javapos-*.jar`, ana SDK
utawa ora.

**`run.bat` ora tau nambahake jar JavaPOS duweke SDK dhewe**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`. Biyen
nate, karo ninggalake `lib\javapos-*.jar` supaya ora ana rong salinan API — nanging `Class-Path` manifest tetep
ngemotake (delengen ing ndhuwur), mula JVM entuk kekarone: kelas pamuat 4.x saka `lib\`, karo `jpos/res/jpos.properties`
lan `jcl.xsd` duweke `javaPOS114.jar` ing sandhinge. Berkas properti iku ndadekake [`JposConfig`](#integrasi-scanner)
mundur, lan `jcl.xsd` iku mung nampa versi JavaPOS nganti 1.10, mula saben entri ing `jpos.xml` (1.12–1.14) gagal
validasi, registri tetep kosong, lan saben sambungan gagal kanthi kesalahan 109 sadurunge scanner kegayuh. Cukup anane
jar iku ing class path wis njalari mangkono, sanajan ana ing mburine jar `lib\`. Jar layanan Zebra mlaku tanpa owah ing
API 1.14 saka `lib\` — open, claim lan pindai wis diverifikasi nganggo scanner temenan kanthi cara iku. DLL sing
diselehake kanthi tangan ing sandhinge `run.bat` isih diajeni: skrip banjur nyetel `-Djava.library.path=<dist>;%PATH%`,
nambahake `PATH` ing pungkasan amarga properti iku ngganti path panggolekan gawan sakabehe.

`mvn javafx:run` ora nindakake siji wae saka iki, mula ngeklaim scanner saka run pangembangan ing Windows butuh jar SDK
ing class path lan `bin\`-e ing `PATH` kanthi cara liya; mlakokake saka `dist/` kanggo iku.

Saliyane class path, skrip-skrip iki nangani apa sing ora ditindakake `java -jar`: nemtokake direktorine dhewe supaya
mlaku saka direktori kerja apa wae, ngutamakake `JAVA_HOME` tinimbang `PATH`, lan nolak JVM sadurunge 11 nganggo ukara
sing nyebutake cara ndandani tinimbang `UnsupportedClassVersionError`. `GAIA_DESKTOP_OPTS` nerusake pilihan JVM.
`run.bat` nganggo `java.exe`, dudu `javaw.exe`, supaya konsol tetep kebukak — Windows iku siji-sijine platform sing
mlakokake scanner, lan status lan kesalahan JavaPOS-e metu ing kana.

`run.bat` uga milih Java, JavaFX lan layanan Zebra saka apa sing ditemokake — `jdk*\` sing dibundel, `javafx-sdk*\lib\`
ing module path, Scanner SDK Zebra ing `ZEBRA_HOME` — ninggalake salinan JavaFX ing `lib\` saka class path yen nganggo
duweke SDK, lan ngirim parser SAX duweke JDK, proxy lan trust store Windows, lan `--enable-native-access` ing Java 17+.
Bagean [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) ing README ngemot tabele. Jaga
pungkasan baris CRLF ing berkas iku: `cmd.exe` bisa nglewati `call :label` ing berkas batch sing mung LF. `run.sh` uga
ngirim `--enable-native-access=ALL-UNNAMED` — JavaFX ngemot native-e dhewe saka `lib/`, lan yen JavaFX ana ing class path,
`ALL-UNNAMED` iku modul sing nyakup — dilewati, kaya ing `run.bat`, mung yen versi Java ora bisa diwaca.

Kekarone tata letak nggawa native JavaFX **saka siji platform**, `${fx.platform}`, mula ora ana sing bisa digawa antar OS
utawa arsitektur. Bangun sepisan saben target — delengen [Siji build saben platform](#siji-build-saben-platform).

> **Eclipse.** Eksekusi `copy-dependencies` nggawa instruksi `<?m2e ignore?>`. m2e ngresolusi proyek workspace sing
> kebukak menyang folder `target/classes`-e tinimbang menyang jar, lan `copy-dependencies` ora gelem nyalin artefak sing
> durung dibungkus (MDEP-187), sing bakal nggagalake auto-build saben proyek `gaia` kebukak ing sandhinge proyek iki.
> Akibate, build sing mung ing IDE ora ngisi `target/dist/gaia-desktop-<version>-<platform>/lib/`; lakokake `mvn package`
> sadurunge nganggo jar dhewe.

---

## Arsitektur

| Paket | Isine |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (jendhela), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (logika gawe tanpa JavaFX), `AiPair` (siji baris editor) |
| `desktop.parse` | `ParseOptions` (`ParseConfig` sing bisa diamati), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — perender laporan, tanpa JavaFX lan diuji unit tanpa toolkit |
| `desktop.i18n` | `UiStrings` — katalog label |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### Corong input

Saben input — sing diketik, ditempel, dibukak saka berkas, dipindai, utawa digawe — tekan parser liwat siji metode,
`MainView.capture(String, ScanSource, String)`.

Iku siji-sijine keputusan desain sing dadi pathokan aplikasi. Gawe ora butuh laporan dhewe, riwayat dhewe utawa dalan
ekspor dhewe, amarga sawise liwat corong, string sing digawe ora bisa dibedakake karo sing dipindai. Apa wae sing anyar
sing ngasilake input kudu liwat corong iki, ora mubeng ing sandhinge.

### Thread

| Thread | Sing dilakokake |
|---|---|
| Thread aplikasi JavaFX | Kabeh parsing, rendering lan UI. Parsing kurang saka siji milidetik yen tabel AI wis anget, mula ora butuh worker |
| `gaia-scanner` | Saben panggilan menyang scanner — open, claim, enable, disable, release, close. Siji thread umur dawa duweke `ScannerService`; delengen [Integrasi scanner](#integrasi-scanner) |
| Thread acara JavaPOS | Callback `ScannerService`. `MainView.FxScannerListener` nyabrangake saben siji nganggo `Platform.runLater` |
| Latar mburi | Ngenteni `connect()` / `disconnect()` scanner (claim bisa nganti 5 detik), mbalekake riwayat sing disimpen nalika wiwitan — sing uga dadi pemanasan parser — lan priksa nganyari (`gaia-update-check`, siji panjaluk GitHub, asile dikirim bali nganggo `Platform.runLater`) |

**Aja tau ngowahi pilihan sawijining kontrol, utawa ngrusak kontrol iku, saka njero acara pilihane dhewe.** Telung crash
sing beda-beda ing aplikasi iki asale persis saka iku: ngreset pamilih AI ing njero `clearAndSelect` duweke popup dhewe
(`IndexOutOfBoundsException` saka `ListViewBehavior`), lan mbangun maneh panel sing duwe combo basa saka njero handler
combo iku dhewe. Kekarone saiki ditundha nganggo `Platform.runLater`, lan alesane dicathet ing kekarone panggonan
panggilan.

### Mbangun maneh tinimbang menehi label maneh

Ngganti basa **mbangun maneh** kerangka saka katalog — menu, toolbar, panel pilihan — lan iku aman amarga ora ana siji
wae sing nyekel kahanan: `OptionsPane` iku tampilan rong arah saka `ParseOptions`, mula nilai-nilaine manggon ing model
lan panel anyar langsung njupuk maneh.

Kontrol sing nyekel **data panganggo** iku field, lan diwenehi label maneh ing panggonane tinimbang dibangun maneh:
`BuilderPane.retext(UiStrings)` njaga baris AI, field Digital Link lan pilihan. Mbangun maneh bakal mbuwang gawean sing
lagi dilakoni — kaya sing nate kedadeyan.

---

## Token {GS}

`TextField` JavaFX kanthi meneng mbuwang saben karakter ing ngisor `0x20` (`TextInputControl.filterInput`). Dudu
"nampilake kanthi aneh" — mbusak. Ngetik, nempel, `setText` lan `insertText` kabeh kena, mula
`setText("01…" + GS + "10LOT")` ngasilake `"01…10LOT"`: string elemen sing tegese owah kanthi meneng, amarga AI kanthi
dawa sing bisa owah saiki nguntal apa sing ana ing mburine.

`desktop.parse.GsToken` iku jawaban sakabehe. Pemisah lumaku ing UI minangka teks harfiah `{GS}` lan dadi `0x1D` maneh
ing dalan menyang parser. Token iki ora diciptakake ing kene — iki persis cara `ASCIIRevealerUtils.printable` duweke inti
nampilake `0x1D`, mula nilai sing disalin saka laporan bisa langsung ditempel bali ing bar input.

Saringan `TextFormatter` ing field input nyekel saben dalan sing bisa diliwati teks — ngetik, ⌘V, menu konteks, seret
lan culke, `setText` — mula panulisan ulang ana ing kono tinimbang ing saben handler.

Dalan scanner ora tau liwat `GsToken`: asil pindai tekan parser minangka byte.

---

## Integrasi scanner

`ScannerService` duwe kontrol `Scanner` JavaPOS: open → claim → enable, nguripake maneh acara data sawise saben pindai.
Jeneng piranti logis asale saka `src/main/resources/jpos.xml`.

**Saben panggilan piranti mlaku ing siji thread, `gaia-scanner`, sing urip sajrone layanan urip** — ora tau ing thread FX,
lan ora tau ing thread sing rampung sawise panggilan. Layanan Zebra nggayuh scanner liwat JNI ing ndhuwur CoreScanner COM
API, lan COM naleni apa sing disiyapake menyang apartemen thread sing nyeluk: open ing thread sing banjur rampung, scanner
bakal ke-claim nanging meneng wae; close ing thread liya, apartemen sing salah sing di-uninitialize. Close ing thread FX iku
kasus paling ala, amarga JavaFX nyeluk `OleInitialize` ing kono lan ndhaptarake target drop saben jendhela marang iku —
siji `CoUninitialize` ing kono mateni seret lan culke ing sakabehe aplikasi. Iki JavaFX ing Windows, dudu siji versi JavaFX
tartamtu. `connect()` lan `disconnect()` ngirim gawean menyang thread iku lan ngenteni, mula `MainView` nyeluk kekarone
saka thread latar mburi sing umure cendhak.

Rong aturan nyegah piranti sing tumindak ora bener supaya ora nyendhat sambungan. **Pambongkaran ora tau mandheg luwih
dhisik:** `closeQuietly` nindakake disable, release lan close dadi langkah dhewe-dhewe, mung release yen claim lagi
dicekel, lan tansah nyoba `close()` — sawise claim gagal, `release()` mbuwang "not claimed", lan nalika langkah-langkah
iku nganggo `try` bebarengan, iku nglewati close lan ninggalake piranti kebukak ing layanan Zebra. **Acara data diuripake
maneh ing njero `finally`:** JavaPOS mateni acara iku sawise saben acara, mula maca utawa handler sing mbuwang pangecualian
tetep kudu nguripake maneh, yen ora scanner bakal meneng nganti disambungake maneh. `ScannerLifecycleTest` nguji kekarone
marang `jpos.Scanner` sing wis ditulis skripe.

**`JposConfig` ngarahake JavaPOS menyang `jpos.xml` sing dibundel** sadurunge open pisanan. Nganggo jar JavaPOS saka Maven
(`javapos-config-loader` 4.x) lan ora ana `jpos/res/jpos.properties` ing class path, yen ora mangkono saben open bakal
gagal kanthi *"Service does not exist in loaded JCL registry"* (kesalahan 109), amarga telung alesan dhewe-dhewe: service
manager bali menyang `SimpleRegPopulator`, sing maca `jpos.cfg` sing diserialisasi lan ora tau maca `jpos.xml`; pamuat
XML mung nggoleki `jpos.xml` ing direktori kerja, ora ing njero jar; lan Xerces 2.12.2 ing `lib/` nolak properti JAXP
`accessExternalDTD` sing disetel pamuat. `JposConfig` nyetel `jpos.config.regPopulatorClass` (`JavaxRegPopulator`),
`jpos.config.populatorFileURL` (`jar:file:…!/jpos.xml` duweke jar dhewe) lan `javax.xml.parsers.SAXParserFactory`
(duweke JDK) minangka properti sistem — ora tau sing wis disetel, mula `-Djpos.config.populatorFileURL=file:…` isih bisa
milih `jpos.xml` liyane — lan ora nyetel apa-apa yen ana `jpos.properties` ing class path utawa pamuat 4.x ora ana.
`javaPOS114.jar` duweke Zebra SDK iku kasus kaya mangkono lan ngrusak registri babar pisan, mula `run.bat` nyingkirake
saka class path — delengen [Native Zebra](#native-zebra). `JposConfigTest` mriksa saka wiwitan nganti pungkasan yen
registri banjur nyekel scanner-scanner saka berkas iku.

**Pamilih piranti digawe saka registri iku**, ora disimpen minangka dhaptar ing kode: `ScannerService.logicalNames()` maca
entri sing wis dimuat ing thread `gaia-scanner` lan nyimpen sing kategori pirantine `Scanner` — rong entri timbangan ing
`jpos.xml` tetep ing njaba — miturut `pickerOrder`: `ZebraAllScanners` dhisik yen berkas nemtokake, liyane miturut
abjad (registri ora njaga urutan berkas). Berkas sing ora duwe ora diwenehi; yen ora ana sing dimuat mung gawan sing
ditawakake, mula pamilih ora tau kosong. `MainView` langsung nuduhake gawan lan ngisi dhaptar saka thread latar mburi,
njaga pilihan sing wis disimpen. Mula scanner sing ditambahake menyang `jpos.xml` — utawa sakabehe berkas sing dijenengi
nganggo `-Djpos.config.populatorFileURL` — katon ing pamilih tanpa owah kode, lan `JposConfigTest` mriksa pamilih marang
wacan mandhiri saka berkas sing dibundel. Layanan sing dijenengi entri-entri iku yaiku kelas Zebra
(`com.zebra.jpos.service.scanner.*Factory`), sing isih kudu ana ing class path — saka Scanner SDK liwat `run.bat`, utawa
jar layanan Zebra sing diselehake ing `lib\`.

Data pindai didekode minangka **ISO-8859-1** supaya saben byte dipetakake 1:1 lan awalan AIM Code ID uga saben `0x1D`
tetep wutuh; laporan ditulis minangka UTF-8.

Ora ana bagean layanan sing nyusun teks kanggo panganggo. Acara metu minangka konstanta `ScannerListener.Event` ditambah
nilai sing owah, lan `MainView` nyedhiyakake tembung saka katalog — yen ora, baris status bakal nganggo basa Inggris ing
aplikasi sing diterjemahake dadi 35 basa. Baris log iku pangecualian lan tetep nganggo basa Inggris.

### Gerbang platform

`ScannerService.isSupportedPlatform()` iku siji uji `os.name`, lan `MainView` takon marang iku sadurunge mbangun kontrol
scanner apa wae: ing macOS lan Linux ora ana toggle, ora ana pamilih piranti, ora ana menu Scanner (mula uga ora ana
<kbd>⌘K</kbd>), lan ora ana label kahanan. Kabeh iku ora didhelikake — ora tau digawe.

Zebra ngirim layanan JavaPOS-e mung kanggo Windows, lan ing panggonan liya ora ana sing gagal luwih dhisik: jar kaleksanan
lan kontrol digawe, lan `open()` sing pungkasane gagal amarga lapisan native ora ana, sawetara detik lan siji dialog
sabanjure.

Aturan iki babagan *layanan*, dudu babagan JavaPOS. Vendor sing ngirim layanan lintas platform bakal mlaku ing kene, lan
ngembangake aturan iku mung owah ing `supportsJavaPos(String)` — sing package-private lan diuji marang string `os.name`
supaya kekarone jawaban kacakup ing mesin apa wae sing mlakokake build.

### Simbologi

`Symbologies` metakake kabeh **78** jinis data pindai `SCAN_SDT_*` JavaPOS menyang jeneng tampilan, lan menyang
`DataCarrierType` duweke inti ing panggonan kekarone kosakata cocog. JavaPOS iku sing luwih alus saka kekarone — misahake
UPC-A saka EAN-13, lan Data Matrix biasa saka GS1 DataMatrix, dene AIM nglumpukake sakabehe kulawarga ing ngisor siji
karakter kode.

Jinis sing ana ing njaba tabel tetep entuk jeneng sing bisa dienggo, ditemokake liwat refleksi ing `ScannerConst`, amarga
layanan vendor pancen nglaporake nilai ing njaba dhaptar spesifikasi. Siji uji mlaku liwat `ScannerConst` lan gagal yen
ana jinis sing diumumake nanging ora duwe jeneng — sing uga nyekel konstanta sing bakal ditambahake JavaPOS mbesuk.

Sawetara pemetaan carrier sengaja ditinggal `UNKNOWN` tinimbang dikira-kira: straight 2 of 5 (AIM misahake `]R`/`]S`
miturut cacah bar start/stop, sing ora dilaporake JavaPOS), kode pos papat kahanan, lan TLC-39.

---

## Lokalisasi

Label manggon ing siji berkas properties saben basa ing ngisor `src/main/resources/i18n/ui_<CODE>.properties`, nganggo
kode sing padha karo katalog kesalahan inti (`EN`, `FR`, `PCM`, …). Kabeh 35 ana lan kabeh 35 nggawa set kunci sing
padha persis. Berkas diwaca minangka **UTF-8**, dudu ISO-8859-1 sing dianggep `Properties`, mula terjemahan disimpen
kanthi wujud sing bisa diwaca tinimbang escape `\uXXXX`.

**Fallback iku saben kunci, dudu saben berkas** — iku sing ndadekake basa sing lagi diterjemahake sabagean wis migunani
wiwit kunci pisanane teka. Basa Inggris iku kontrake: saben kunci kudu ana ing kana.

| Lapisan | Kunci | Jangkoan |
|---|---|---|
| Kosakata laporan bebarengan | 31 | kabeh 35 basa |
| Kerangka desktop — menu, tombol, builder, pilihan, baris status, tata letak laporan, status scanner, isi dialog | 114 | kabeh 35 basa |

Enem istilah bebarengan (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) ora dienggo laporan
aplikasi iki.
Istilah-istilah iku sengaja disimpen; sirah berkas nyebutake bab iku.

### Argumen `--language`

`GaiaDesktopApp.languageOverride()` maca `--language=…` liwat `getParameters().getNamed()` duweke JavaFX dhewe, ngresolusi
nganggo `Languages.fromArgument` — kode katalog, jeneng enum, utawa tag BCP 47, ora mbedakake aksara gedhe-cilik — lan
masrahake menyang `MainView`.

**Penimpaan iki kanggo run, dudu kanggo berkas.** `MainView` nyimpen basa sing dimuat ing `savedLanguage` lan nulis *basa
iku* bali, nganti `languageChosenInSession` ngandhani yen panganggo milih basa dhewe; sawise iku pilihane disimpen kaya
biasane. Pamisahan iku sakabehe desain: profil kios utawa trabasan demo bisa mbukak nganggo basa Spanyol tanpa nulis
ulang basa sing bakal dibukak sing duwe mesin sesuk, dene pilihan temenan sing digawe ing panel pilihan tetep lestari.

Nilai sing ora nyebut katalog apa wae dicathet ing log bebarengan karo kode-kode sing ditampa lan ora digatekake. Nolak
miwiti amarga salah ketik ing trabasan bakal dadi kegagalan sing luwih ala, lan `Languages.fromArgument` mbalekake `null`
tinimbang mbuwang pangecualian amarga alesan sing padha.

### Nambah basa

Ora perlu owah kode.

1. Sunting `ui_<CODE>.properties` — saben basa wis duwe berkas.
2. Terjemahake nilai-nilaine. Jarke placeholder `{0}` / `{1}` ing panggonane.
3. Bangun. Rong uji ngetrapake kontrak: siji gagal yen kode njaluk kunci sing ora ditemtokake basa Inggris, sijine maneh
   yen set kunci katalog apa wae nyimpang saka basa Inggris.

Uji pisanan mindai **kode sumber** kanggo nggoleki literal kunci tinimbang nindakake refleksi ing kelas, amarga kunci mung
ana minangka literal string ing panggonan panggilan. Mula pesen anyar kudu digawe kanthi kunci ditulis langsung —
`strings.format("st.scannerError", …)` — tinimbang digoleki liwat variabel, yen ora uji ora bisa ndeleng.

### Sing sengaja ora diterjemahake

- jeneng field cekak sing ditampilake nganggo aksara monospace ing sandhinge nilai mentah — `format`, `position`,
  `length`, `raw` lan `part` ing kertu elemen, `AI`, `sep`, `pos` lan `len` ing pita, lan papat sel sing nuduhake
  panggonan kesalahan (`AI`, `position`, `level`, `stage`), sing nilai-nilaine pancen jeneng enum sing ora diterjemahake;
- jeneng conto input, sing menehi jeneng konstruksi GS1, lan jeneng simbologi, sing iku jeneng diri;
- paragraf remediasi `GE-C084` — prosa standar sing terjemahan setengah-setengah luwih ala tinimbang basa Inggris;
- pengaman pungkasan AI sing ora dikenal duweke `ElementBuilder`, sing ora bisa digayuh saka UI: editor mriksa AI sing ora
  dikenal dhewe lan nglaporake saka katalog;
- baris log.

Isih ana siji celah: `ScanSource.label()` nyedhiyakake `manual` / `scanner` / `file` / `built` kanggo baris riwayat lan
sirah laporan nganggo basa Inggris. Nutup celah iki butuh luwih saka siji kunci — `HistoryCell` ora duwe `UiStrings`, lan
cell factory dhaptar mung dipasang sepisan, mula sel uga kudu dianyari nalika basa diganti.

---

## Format kahanan sing disimpen

Rong berkas ing ngisor `~/.gaia-desktop/`, dipisah amarga owahe kanthi irama sing beda — log tangkepan sing rusak utawa
disunting tangan ora kena ndadekake panganggo kelangan setelane. Timpa direktori nganggo `-Dgaia.desktop.home=…`, iku cara
uji tetep adoh saka direktori omah sing temenan.

**`options.properties`** — ditulis nalika pilihan owah, lan nalika metu.

| Kunci | Teges |
|---|---|
| `parse.mode`, `parse.language` | Jerone pipeline lan basa pesen |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Tampilan tanggal |
| `check.skipRequires`, `check.skipExcludes` | Uwal saka pamriksan struktural |
| `ui.theme` | `LIGHT` utawa `DARK` |
| `ui.optionsPane` | Panel pilihan kebukak? |
| `ui.scannerDevice` | Jeneng logis JavaPOS pungkasan |
| `ui.mode` | `PARSE`, `BUILD` utawa `COMBINE` |
| `update.check` | Apa priksa nganyari saben dina mlaku (gawan `true`) |
| `update.lastCheck` | Kapan pungkasan mlaku, ISO-8601; ora ana utawa ora bisa diwaca tegese wis wayahe priksa |
| `update.skipVersion` | Rilis sing dipilih panganggo kanthi *Lewati versi iki*; ora ditawakake maneh kanthi otomatis |

Berkas sing ilang utawa rusak ora tau fatal: saben nilai fallback dhewe-dhewe, mula siji baris sing ala mung ngorbanake
baris iku lan ora liyane. Ing peluncuran pisanan, nalika babar pisan ora ana berkas, pilihan diisi saka setelan regional
mesin dening `SystemDefaults` tinimbang saka gawan bawaan GAIA — pola tanggal cekak locale menehi urutan komponen,
pemisah, lan apa sasi lan taun dicekak.

Aja tau menehi null marang `Properties.setProperty`: iku mbuwang pangecualian, lan `persistOptions()` mlaku saka handler
acara UI sing pangecualiane uwal menyang thread FX. Iku crash temenan sing wis nate dialami aplikasi iki.

**`captures.tsv`** — ditulis nalika riwayat owah, lan nalika metu. Sing paling anyar dhisik, diwatesi **500** entri.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Mung sing ora bisa dietung maneh sing disimpen. `ParseResult` **ora** diserialisasi — dijupuk maneh kanthi parsing input
maneh nalika dimuat, mula riwayat sing dibalekake nggambarake pilihan sing laku *saiki*. Input asring ngemot FNC1 lan apa
wae liyane sing diwetokake scanner, mula field input di-escape (`\\`, `\t`, `\n`, `\r`, lan `\xNN` kanggo kabeh liyane
ing ngisor `0x20`) lan berkas diwaca lan ditulis minangka ISO-8859-1, byte saben byte.

Berkas sing ditulis sadurunge tanggal dicathet mung ngemot jam; tetep bisa dimuat, kanthi tanggal dina iki, amarga tanggal
temenan ora tau ditulis.

---

## Pangujian

```bash
(cd gaia-desktop && mvn test)
```

198 uji, sengaja **tanpa toolkit JavaFX**, sing ndadekake cepet lan headless. Uji-uji iki nyakup perender laporan, logika
gawe lan panggolekan AI, konversi `{GS}`, gawan sing diturunake saka locale, kekarone panyimpenan (kalebu round trip sing
ngemot `0x1D` lan berkas sing sengaja dirusak), tabel simbologi, aturan platform scanner, stub versi, lan katalog-katalog.

Telu sing perlu dingerteni amarga njaga kontrak, dudu fungsi:

| Uji | Gagal nalika |
|---|---|
| `CatalogueCoverageTest` | Kode njaluk kunci sing ora ditemtokake `ui_EN.properties`, utawa salah siji saka 35 berkas nyimpang saka set kunci basa Inggris |
| `SymbologiesTest` | Ana konstanta `SCAN_SDT_*` sing diumumake JavaPOS ora duwe jeneng tampilan — kalebu sing ditambahake upgrade JavaPOS |
| `ScannerServiceTest` | Aturan platform ngganti jawabane kanggo Windows utawa kanggo apa wae liyane |

`UiStrings` sengaja gampang ngapura nalika runtime — kunci sing ora dikenal ditampilake minangka kunci iku dhewe tinimbang
mbuwang pangecualian ing tengah tata letak — persis iku alesane uji katalog kudu ana.

**Ora kacakup:** apa wae sing butuh toolkit urip — prilaku fokus, toggle mode, nyunting tabel, ngganti basa, lan kekarone
crash sing diterangake ing [Thread](#thread). Kabeh iku diverifikasi kanthi nglakokake jendhela temenan, nanging regresi
ing kono ora bakal nggagalake build. Nambah TestFX utawa Monocle bakal nutup celah iku.

---

## Ndandani masalah build

| Gejala | Sabab lan ndandani |
|---|---|
| `package tools.pantheum.gaia does not exist` | Inti durung dipasang. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | `Main-Class` jar kudu `Launcher`, dudu `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* nalika wiwitan | Wis dikarepake lan ora mbebayani — JavaFX nyathet yen ana ing class path. Persis iku alesane `Launcher` ana |
| Jar ora gelem miwiti, `NoClassDefFoundError` | Jar gawan mung nggawa kelas modul iki; `dist/lib/` kudu ana ing sandhinge. Gunakake `-Pfat` kanggo siji berkas mandhiri |
| `Artifact has not been packaged yet … MDEP-187` ing Eclipse | Resolusi workspace m2e. Delengen cathetan ing ngisor [Pambungkusan](#pambungkusan) |
| Berkas `… 2.properties` dobel katon ing build | Proyek ana ing folder sing disinkronake iCloud lan sinkronisasi nggawe salinan konflik ing tengah build. Tansah `mvn clean package`; luwih becik maneh, singkirake `target/` saka sinkronisasi |
| Item menu nuduhake kunci kaya `act.hri` | Kunci iku ora ana ing `ui_EN.properties`. Build kudune wis nyekel — delengen [Pangujian](#pangujian) |

---

## Watesan sing wis dingerteni lan gawean sing isih ana

- **Ora ana uji UI headless.** Delengen [Pangujian](#pangujian).
- **Artefak khusus platform.** Kekarone pambungkusan mbundel native JavaFX siji platform; bangun siji saben target nganggo
  `-Dfx.platform` (mesin apa wae bisa mbangun kabeh). Ora ana sing mbundel runtime Java — iku butuh `jpackage`, sing mung
  mbangun kanggo OS panggonane mlaku.
- **Input scanner mung ing Windows.** Delengen [Gerbang platform](#gerbang-platform).
- **`ScanSource.label()` durung dilokalisasi.** Delengen [Sing sengaja ora diterjemahake](#sing-sengaja-ora-diterjemahake).
- **`UiState.mode` iku `String`** (`"PARSE"` / `"BUILD"`) dene `Theme` ing sandhinge iku enum, mula literal iku katon ing
  sawetara panggonan. Enum bakal ngilangi kabeh iku.
- **Target Digital Link mung domain lan awalan path.** `BuilderDigitalLinkConfig` uga ndhukung parameter query tambahan
  lan fragment; editor ora nampilake.
- **Pasangan AI sing saling nyingkirake (`GE-S006`) ora ditandhani ing editor** kaya AI sing ora dikenal lan dobel —
  gawe mung langsung gagal.
- **Katalog kesalahan inti nggandakake tandha petik** (`value ''09506000134350''`), sing katon apa anane ing kegagalan
  gawe lan laporan. Katon kaya escape petik `MessageFormat` sing ora dibalekake; ndandani kuwi panggonane ing inti.
