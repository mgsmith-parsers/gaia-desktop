# GAIA Desktop — Gabay ng developer

Kung paano bina-build, inaayos at sinusubok ang module. Para sa kung ano ang ginagawa ng programa kapag tiningnan
mula sa labas, tingnan ang [Gabay ng gumagamit](GaiaDesktop-User-Tagalog.md). Orihinal na dokumento:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Talaan ng nilalaman

1. [Ang module sa isang sulyap](#ang-module-sa-isang-sulyap)
2. [Pag-build at pagpapatakbo](#pag-build-at-pagpapatakbo)
   - [Packaging](#packaging)
3. [Arkitektura](#arkitektura)
   - [Ang funnel ng input](#ang-funnel-ng-input)
   - [Threading](#threading)
   - [Rebuild laban sa re-label](#rebuild-laban-sa-re-label)
4. [Ang {GS} token](#ang-gs-token)
5. [Integrasyon ng scanner](#integrasyon-ng-scanner)
   - [Ang platform gate](#ang-platform-gate)
   - [Mga symbology](#mga-symbology)
6. [Lokalisasyon](#lokalisasyon)
   - [Ang argumentong `--language`](#ang-argumentong---language)
   - [Pagdaragdag ng wika](#pagdaragdag-ng-wika)
   - [Ang sadyang hindi isinasalin](#ang-sadyang-hindi-isinasalin)
7. [Mga format ng naka-imbak na estado](#mga-format-ng-naka-imbak-na-estado)
8. [Pagsubok](#pagsubok)
9. [Pag-aayos ng problema sa build](#pag-aayos-ng-problema-sa-build)
10. [Mga kilalang limitasyon at natitirang trabaho](#mga-kilalang-limitasyon-at-natitirang-trabaho)

---

## Ang module sa isang sulyap

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Isang client ng core library at wala nang iba. Wala itong dalang **anumang kaalaman sa GS1**: bawat depinisyon ng AI,
patakaran sa pag-validate, mensahe ng error at naka-localize na paglalarawan ay galing sa
[`GaiaParser`](../../../../gaia/docs/GaiaParser.md) at [`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). Lalo
na, hindi kailanman kinokopya ang listahan ng AI sa module na ito — binabasa ng picker ng builder ang sariling registry
ng core, kaya hindi ito makapag-aalok ng AI na tatanggihan ng builder.

**Mga entry point:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (ang JavaFX `Application`) at
`tools.pantheum.gaia.desktop.Launcher` (ang `Main-Class` ng naka-package na jar).

Isang dahon ang module: walang umaasa rito, at hindi ito umaasa sa ibang downstream module.

---

## Pag-build at pagpapatakbo

- **Java 21** para patakbuhin, at JDK 21 o mas bago para mag-build — kino-compile ang module gamit ang `release=21`.
  Ang JavaFX 21 ang katugmang LTS na linya. Java 11 bytecode pa rin ang core jar at tumatakbo nang walang pagbabago sa
  21.
- **Maven** sa `PATH`. Walang wrapper sa repo na ito.
- **I-install muna ang core.** Kinukuha ng module na ito ang `gaia:1.1.0` mula sa `~/.m2`; ang luma o nawawalang core
  jar ang karaniwang sanhi ng `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

Ang `--language` ang tanging argumento sa command line, at ang tanging paraan para maimpluwensiyahan ang pagsisimula
bukod sa `-Dgaia.desktop.home=…`, na itinuturo ang buong profile sa ibang lugar.

Ang `mvn javafx:run` ang daan para sa development: inilalagay ng plugin ang JavaFX sa **module path**, kaya nagsisimula
ang toolkit bilang may-pangalang module at maaaring ilunsad nang direkta ang `GaiaDesktopApp`. Hindi iyon magagawa ng
naka-package na jar — nasa class path ang lahat — kaya ang `Main-Class` nito ay ang shim na `Launcher`. Ang subclass ng
`Application` bilang `Main-Class` sa ganoong layout ay pumapalya na may *"JavaFX runtime components are missing"*.

Laging gamitin ang `clean` — isang beses, bago mag-build ng isang hanay ng mga platform, dahil inaalisan nito ang
`target/dist/` ng lahat ng iyon. Tingnan ang [Pag-aayos ng problema sa build](#pag-aayos-ng-problema-sa-build) para sa
dahilan.

### Packaging

Lahat ng maipapadala ay tinitipon sa ilalim ng `target/dist/gaia-desktop-<version>-<platform>/` at wala nang iba, kaya ang
direktoryo ay maaaring i-zip o kopyahin bilang isang set nang hindi pumipili ng mga artefact mula sa `target/` sa tabi
ng `classes/`, `maven-status/` at ang iba pang pansamantalang file ng build. Pinangangalanan ito ng `${dist.dir}` nang
isang beses; dito tumuturo ang bawat plugin na sumusulat ng artefact.

#### Isang build bawat platform

May native code ang mga JavaFX jar, kaya bawat distribution ay para sa iisang platform, na pinangangalanan ng property
na `fx.platform` bilang OpenJFX classifier: `mac-aarch64`, `mac`, `win`, `linux` o `linux-aarch64`. Limang `fx-*`
profile na pinapagana ayon sa OS ang nagtatakda nito sa sariling platform ng makinang nagbi-build, at iyon lang ang
kailangan ng karaniwang `mvn package` o `mvn javafx:run`; nangingibabaw sa kanila ang `-Dfx.platform=…` para mag-build
ng anumang ibang platform, mula sa anumang makina — mga download lang ang mga JavaFX jar. Ang `${dist.dir}` ay
`target/dist/${fx.platform}`, kaya hindi kailanman pinapatungan ng build ng isang platform ang build ng iba.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Dalawang detalye ng pom ang nagpapagana nito, at pareho silang mukhang puwedeng alisin:

- **Direktang dependency ang bawat JavaFX module** — base, graphics, controls, media at web — bawat isa ay may
  `<classifier>${fx.platform}</classifier>` at isang `org.openjfx:*` exclusion. Pinipili ng mga pom ng OpenJFX ang
  kanilang transitive na mga jar ayon sa OS ng *makinang nagbi-build*, kaya kapag hinayaang dumating nang transitive
  ang graphics o base, napapasok ang mga native ng Mac sa isang Windows build katabi ng mga native ng Windows.
- **Ang property ay `fx.platform`, hindi `javafx.platform`.** Kapag itinakda mula sa labas, ang sariling property ng
  OpenJFX ay muling dine-define ng parent pom nito gamit ang sarili nito; tinatanggihan ng Maven ang pom na iyon bilang
  hindi valid at tahimik na tinatanggal ang mga platform jar, kaya natitira ang build na walang JavaFX.

Iisang path ang isinusulat ng dalawang layout, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`, at eksaktong
isa lang sa kanila ang tumatakbo bawat build. Ang `${jar.phase}`, `assembly.skipAssembly` at `mdep.skip` ang pumipigil
sa kanilang dalawa na angkinin ito.

| | Default | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — mga class at resource lang ng module na ito | ~50 MB — mga dependency na binuksan sa loob nito |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 jar, isa-isang pinangalanan sa `Class-Path` ng manifest | — |
| Ipadala | ang jar **at** ang `lib/` sa tabi nito | isang file |
| Nasa `dist/gaia-desktop-<version>-<platform>/` din | `run.bat` (win) o `run.sh` (iba pa) | pareho |
| At | `readme.html` at `docs/` (bawat platform) | pareho |

Dala rin ng bawat distribution ang mga gabay: kinokopya ang `docs/` nang buo ang layout nito, kaya gumagana pa rin ang
mga link sa pagitan ng mga gabay at salin, maliban sa mga tool sa pagsasalin (`check-i18n.py`, `i18n-manifest.json`,
`i18n-plan.md`); at ang `readme.html` sa root, isang index na naka-link sa bawat isa, mula sa
`src/main/dist/readme.html` na ang `@VERSION@` ay pinapalitan ng bersyon ng pom. Pareho silang isinusulat ng antrun
step na `pick-launch-script`, na unang binubura ang `docs/` para hindi maiwan ang gabay na inalis na. Mano-manong
isinulat ang mga link ng readme — ang gabay na pinalitan ng pangalan o saling idinagdag sa ilalim ng `docs/` ay
nangangailangan din ng sariling hilera roon.

Ang `Class-Path` ng manifest at ang `lib/` ay dalawang kalahati ng iisang bagay: pinangangalanan ng jar ang
`lib/<artifact>-<version>.jar` para sa bawat runtime dependency, at pinupuno ng `copy-dependencies` ang `lib/` ng
eksaktong mga pangalang iyon. Iyon ang nagpapagana sa `java -jar`.

#### Ang macOS app at ang distribution zip

Isang antrun execution, ang `mac-app-and-zip`, ang nagtatapos sa phase na `package`. Dahil dito, idineklara ang antrun
plugin **pagkatapos** ng mga plugin na jar at assembly — pinapatakbo ng Maven ang mga execution ng isang phase ayon sa
pagkakasunod ng deklarasyon ng plugin — kaya umiiral na ang jar, sa alinmang packaging, kapag tumakbo ito. (Ang iba
pang execution nito, ang `pick-launch-script`, ay nasa `prepare-package` at hindi naaapektuhan.)

Para sa `mac` at `mac-aarch64`, una nitong binubuo ang `GAIA Desktop.app` sa direktoryo ng distribution: ang
`Info.plist` (na ang `@VERSION@` ay pinapalitan ng bersyon ng pom), ang launcher na `Contents/MacOS/gaia-desktop`
(ginawang executable) at ang `gaia-desktop.icns`, lahat mula sa `src/main/macos/`, saka **inililipat** ang
`gaia-desktop.jar` at `lib/` sa `Contents/Java`, kaya nakatatayo nang mag-isa ang bundle. Doon tumitingin ang `run.sh`
kapag walang jar sa tabi nito. Hinahanap ng launcher ang Java ayon sa bersyon (`java_home -v 21+`, pagkatapos ang
openjdk ng Homebrew — hindi nakikita ng mga paglunsad mula sa Finder ang `JAVA_HOME` o `PATH`), nagpapakita ng dialog sa
pamamagitan ng `osascript` kapag wala, nag-`cd` sa `~/.gaia-desktop` para masulatan ang relatibong `logs/` ng logback,
at ipinapasa ang `-Xdock:name`/`-Xdock:icon`. Iniuugnay ng macOS ang tumatakbong `java` sa bundle (sa app ang bundle id
at icon nito sa Dock); GAIA Desktop ang nakasulat sa menu bar.

Pagkatapos, para sa bawat platform, zina-zip nito ang direktoryo sa `target/dist/gaia-desktop-<version>-<platform>.zip`
sa ilalim ng folder na may parehong pangalan, gamit ang `<zip>` ng Ant: ang `run.sh` at `Contents/MacOS/**` bilang
hiwalay na file set na may `filemode="755"`, kung hindi ay hindi sila tatakbo pagka-unzip. **Hindi ikinakabit** ang zip:
para ito sa mga release sa GitHub, at ang apat na platform ay galing sa apat na build, na hindi kailanman matatanggap
ng Maven Central — isang hindi mababagong upload bawat bersyon — bilang iisang release.

Itinatakda ang `project.build.outputTimestamp` sa oras ng build. Kung hindi, nilalagyan ng mga archiver sa
maven-jar-plugin 3.5 at maven-assembly-plugin 3.8 ng petsang 1 Pebrero 1980 ang bawat entry.

Ang icon ng app ay iginuguhit ng `tools/icon/GaiaIcon.java` (hindi bahagi ng build; nasa header nito ang mga hakbang
na `sips`/`iconutil`). Naka-check in ang mga output nito: `src/main/macos/gaia-desktop.icns` para sa bundle, at
`src/main/resources/icons/gaia-desktop-{16..256}.png`, na itinatakda ng `GaiaDesktopApp` bilang icon ng window
(Windows at Linux; binabalewala ng macOS ang mga icon ng window).

#### Ang mga launch script

Ang `run.sh` at `run.bat` ay galing sa `src/main/scripts/`, kinokopya sa `dist/gaia-desktop-<version>-<platform>/` nang
hindi sinasala — puno ang `run.sh` ng mga shell expansion na `${...}` na malayang maisusulat muli ng pagsasala ng Maven.
Pagkatapos ay itinatabi lang ng isang antrun step ang pinapatakbo ng platform — `run.bat` para sa `win`, `run.sh` para
sa lahat ng iba — at itinatakda ang executable bit sa `run.sh`, dahil hindi ito dinadala ng `copy-resources`. Nagpapasya
ito mula sa resolved na `${fx.platform}` sa halip na sa mga profile, dahil hindi nakikita ng pag-activate ng profile ang
halagang itinakda ng ibang profile: kung hindi, makukuha ng Linux build na ginawa sa Windows ang script ng host.

**Hindi sila umaasa sa manifest**. Bawat isa ay bumubuo ng sariling class path, `gaia-desktop.jar` na sinusundan ng
bawat `.jar` sa `lib/`, na isa-isang pinangalanan sa halip na iiwan sa wildcard na `lib/*` para manatiling iisang entry
ang path na may espasyo at totoo ang bilang ng naidagdag. Ang paglunsad gamit ang `-cp` ang dahilan kung bakit tahasan
nilang pinangangalanan ang `tools.pantheum.gaia.desktop.Launcher`: hindi binabasa ng `-cp` ang `Main-Class` ng manifest.
Gayunman, sinusunod pa rin **nito** ang `Class-Path` nito — dinadala ng jar sa `-cp` ang mga jar na pinangalanan ng
manifest nito, na hinahanap kaagad pagkatapos nito — kaya naglo-load ang bawat jar sa `lib/` ilista man ito ng script o
hindi, at ang pag-alis ng isa sa class path ng script ay hindi naglalayo rito sa JVM. Inililista pa rin ng mga script
ang lahat, kaya nagsisimula pa rin ang jar na luma, mano-manong inedit o kulang ng isang entry ang manifest. Iisang
script ang nagsisilbi sa dalawang packaging: walang `lib/` na maidaragdag ang jar ng `-Pfat` at hindi rin nito
kailangan, kaya ang walang-lamang `lib/` ay isang paalala at hindi error.

#### Ang mga native ng Zebra

Walang ipinapadala ang build na pag-aari ng Zebra — walang DLL, walang service jar. Sa Windows, galing ang mga iyon sa
Scanner SDK ng Zebra na naka-install sa makina: idinadagdag ng `run.bat` ang mga service jar nitong `Jpos*.jar` at ang
`bin\` sa class path at ang `bin\` sa `PATH`, kung saan nahahanap ng `System.loadLibrary` ang `CSJPOSScanner.dll` /
`CSJPOSScanner64.dll` (ang default ng `java.library.path` ay ang `PATH`). Laging galing sa `lib\javapos-*.jar` ang
JavaPOS API at ang configuration loader nito, may SDK man o wala.

**Hindi kailanman idinadagdag ng `run.bat` ang sariling JavaPOS jar ng SDK**, ang
`External\JavaPOS-1.14.0-Dist\javaPOS114.jar`. Ginawa nito iyon dati, at iniwan ang `lib\javapos-*.jar` para maiwasan
ang dalawang kopya ng API — pero ni-load pa rin sila ng `Class-Path` ng manifest (tingnan sa itaas), kaya nakuha ng JVM
ang pareho: ang mga loader class na 4.x mula sa `lib\`, katabi ang `jpos/res/jpos.properties` at `jcl.xsd` ng
`javaPOS114.jar`. Pinatatabi ng properties file ang [`JposConfig`](#integrasyon-ng-scanner), at ang `jcl.xsd` na iyon ay
tumatanggap lang ng mga bersyon ng JavaPOS hanggang 1.10, kaya pumalya sa pag-validate ang bawat entry sa `jpos.xml`
(1.12–1.14), nanatiling walang laman ang registry, at bawat connect ay pumalya na may error 109 bago pa man maabot ang
scanner. Sapat na ang pagkakaroon ng jar na iyon sa class path para mangyari iyon, kahit nasa likod ng mga jar ng
`lib\`. Tumatakbo nang walang pagbabago ang mga service jar ng Zebra sa 1.14 API mula sa `lib\` — na-verify ang open,
claim at scan laban sa totoong scanner sa ganoong paraan. Iginagalang pa rin ang mga DLL na mano-manong inilagay sa
tabi ng `run.bat`: itinatakda noon ng script ang `-Djava.library.path=<dist>;%PATH%`, idinudugtong ang `PATH` dahil
tuluyang pinapalitan ng property ang default na search path.

Walang ginagawa sa mga ito ang `mvn javafx:run`, kaya ang pag-claim ng scanner mula sa development run sa Windows ay
nangangailangang mailagay ang mga jar ng SDK sa class path at ang `bin\` nito sa `PATH` sa ibang paraan; patakbuhin mula
sa `dist/` para doon.

Bukod sa class path, inaasikaso nila ang hindi ginagawa ng `java -jar`: tinutukoy ang sarili nilang direktoryo para
gumana mula sa anumang working directory, mas pinipili ang `JAVA_HOME` kaysa `PATH`, at tinatanggihan ang JVM na mas
luma sa 11 gamit ang pangungusap na nagsasabi ng lunas sa halip na `UnsupportedClassVersionError`. Ipinapasa ng
`GAIA_DESKTOP_OPTS` ang mga opsyon ng JVM. Gumagamit ang `run.bat` ng `java.exe`, hindi `javaw.exe`, kaya nananatiling
bukas ang console — Windows ang tanging platform kung saan tumatakbo ang scanner, at doon napupunta ang status nito at
ang mga error ng JavaPOS.

Pinipili rin ng `run.bat` ang Java, JavaFX at mga serbisyo ng Zebra mula sa nahahanap nito — isang naka-bundle na
`jdk*\`, isang `javafx-sdk*\lib\` sa module path, ang Scanner SDK ng Zebra sa `ZEBRA_HOME` — iniiwan sa labas ng class
path ang mga kopya ng JavaFX sa `lib\` kapag gamit nito ang sa SDK, at ipinapasa ang SAX parser ng JDK, ang mga proxy at
trust store ng Windows, at ang `--enable-native-access` sa Java 17+. Nasa
[What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) ng README ang talahanayan. Panatilihin
ang CRLF na line ending ng file: maaaring malaktawan ng `cmd.exe` ang isang `call :label` sa batch file na LF lang.
Ipinapasa rin ng `run.sh` ang `--enable-native-access=ALL-UNNAMED` — nilo-load ng JavaFX ang sarili nitong mga native
mula sa `lib/`, at kapag nasa class path ang JavaFX, ang `ALL-UNNAMED` ang module na sumasaklaw dito — nilalaktawan,
gaya sa `run.bat`, lamang kapag hindi mabasa ang bersyon ng Java.

Dala ng alinmang layout ang mga native ng JavaFX **ng iisang platform**, `${fx.platform}`, kaya hindi madadala ang
alinman sa iba't ibang OS o arkitektura. Mag-build nang isang beses bawat target — tingnan ang
[Isang build bawat platform](#isang-build-bawat-platform).

> **Eclipse.** May dalang instruksiyong `<?m2e ignore?>` ang execution na `copy-dependencies`. Nire-resolve ng m2e ang
> bukas na workspace project sa folder nitong `target/classes` sa halip na sa jar, at tumatangging kopyahin ng
> `copy-dependencies` ang artifact na hindi pa naka-package (MDEP-187), na magpapapalya sa auto-build tuwing bukas ang
> project na `gaia` katabi nito. Ang resulta, hindi pinupuno ng build na sa IDE lang ang
> `target/dist/gaia-desktop-<version>-<platform>/lib/`; patakbuhin ang `mvn package` bago gamitin ang jar mismo.

---

## Arkitektura

| Package | Laman |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (ang window), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (lohika ng pagbuo na walang JavaFX), `AiPair` (isang hilera ng editor) |
| `desktop.parse` | `ParseOptions` (napagmamasdang `ParseConfig`), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — ang renderer ng ulat, walang JavaFX at may unit test nang walang toolkit |
| `desktop.i18n` | `UiStrings` — ang katalogo ng mga label |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### Ang funnel ng input

Bawat input — itinipa, idinikit, binuksan mula sa file, ini-scan, o binuo — ay umaabot sa parser sa pamamagitan ng
iisang method, `MainView.capture(String, ScanSource, String)`.

Iyon ang tanging desisyon sa disenyo na pinagbatayan ng buong app. Hindi kailangan ng pagbuo ng sariling ulat, sariling
kasaysayan o sariling daan sa pag-export, dahil pagkatapos ng funnel, hindi na maipagkakaiba ang binuong string sa
ini-scan. Anumang bagong lumilikha ng input ay dapat dumaan dito sa halip na sa tabi nito.

### Threading

| Thread | Pinapatakbo |
|---|---|
| JavaFX application thread | Lahat ng parsing, rendering at UI. Wala pang isang millisecond ang parsing kapag mainit na ang mga talahanayan ng AI, kaya hindi nito kailangan ng worker |
| `gaia-scanner` | Bawat tawag sa scanner — open, claim, enable, disable, release, close. Isang pangmatagalang thread na pag-aari ng `ScannerService`; tingnan ang [Integrasyon ng scanner](#integrasyon-ng-scanner) |
| JavaPOS event thread | Mga callback ng `ScannerService`. Itinatawid ng `MainView.FxScannerListener` ang bawat isa gamit ang `Platform.runLater` |
| Background | Paghihintay sa `connect()` / `disconnect()` ng scanner (maaaring tumagal ng hanggang 5 s ang claim), pagbabalik ng naka-save na kasaysayan sa pagsisimula — na nagsisilbi ring pampainit sa parser — at ang pagsuri ng update (`gaia-update-check`, isang request sa GitHub, ibinabalik ang resulta gamit ang `Platform.runLater`) |

**Huwag kailanman baguhin ang pagpili ng isang control, o sirain ang control, mula sa loob ng sarili nitong selection
event.** Tatlong magkakahiwalay na crash sa app na ito ang nagmula mismo roon: ang pag-reset ng AI picker sa loob ng
sariling `clearAndSelect` ng popup (isang `IndexOutOfBoundsException` mula sa `ListViewBehavior`), at ang muling
pagbuo ng panel na may-ari ng language combo mula sa loob ng sariling handler ng combo na iyon. Pareho na silang
ipinagpapaliban gamit ang `Platform.runLater`, at nakatala ang mga dahilan sa dalawang call site.

### Rebuild laban sa re-label

Ang pagpapalit ng wika ay **muling bumubuo** ng balangkas mula sa katalogo — mga menu, toolbar, panel ng mga opsyon —
na ligtas dahil walang may-ari ng estado sa mga iyon: ang `OptionsPane` ay dalawang-direksiyong view ng `ParseOptions`,
kaya nasa model ang mga halaga at agad itong muling kinukuha ng bagong panel.

Ang mga control na may hawak na **data ng gumagamit** ay mga field, at muling nilalagyan ng label sa kinalalagyan nila
sa halip: pinananatili ng `BuilderPane.retext(UiStrings)` ang mga hilera ng AI, ang mga field ng Digital Link at ang
pagpili. Ang muling pagbuo rito ay magtatapon ng kasalukuyang ginagawa — na nangyari na noon.

---

## Ang {GS} token

Tahimik na itinatapon ng JavaFX na `TextField` ang bawat character na mas mababa sa `0x20`
(`TextInputControl.filterInput`). Hindi "kakaiba ang pagpapakita" — binubura ito. Apektado ang pagta-type, pagdidikit,
`setText` at `insertText`, kaya ang `setText("01…" + GS + "10LOT")` ay nagbibigay ng `"01…10LOT"`: isang element
string na tahimik na nagbago ang kahulugan, dahil nilulunok na ngayon ng variable-length na AI ang sumusunod dito.

Ang `desktop.parse.GsToken` ang buong sagot. Naglalakbay ang separator sa UI bilang literal na tekstong `{GS}` at
nagiging `0x1D` muli papunta sa parser. Hindi ito inimbento rito — eksaktong ganito ipinapakita ng
`ASCIIRevealerUtils.printable` ng core ang `0x1D`, kaya ang halagang kinopya mula sa ulat ay direktang maidirikit muli sa
input bar.

Hinuhuli ng isang `TextFormatter` filter sa input field ang bawat daan na maaaring pagdatingan ng teksto — pagta-type,
⌘V, ang context menu, drag and drop, `setText` — kaya naroon ang muling pagsulat sa halip na sa bawat handler.

Hindi kailanman dumaraan sa `GsToken` ang daan ng scanner: umaabot sa parser ang mga scan bilang mga byte.

---

## Integrasyon ng scanner

Pag-aari ng `ScannerService` ang JavaPOS na control na `Scanner`: open → claim → enable, muling inaarmas ang data event
pagkatapos ng bawat scan. Galing sa `src/main/resources/jpos.xml` ang mga lohikal na pangalan ng device.

**Tumatakbo ang bawat tawag sa device sa iisang thread, `gaia-scanner`, na nabubuhay hangga't buhay ang serbisyo** —
hindi kailanman sa FX thread, at hindi kailanman sa thread na nagtatapos pagkatapos ng tawag. Naaabot ng serbisyo ng
Zebra ang scanner sa pamamagitan ng JNI sa ibabaw ng CoreScanner COM API, at itinatali ng COM ang inihahanda nito sa
apartment ng tumatawag na thread: mag-open sa thread na magtatapos pagkatapos, at maki-claim ang scanner pero mananatiling
tahimik; mag-close sa ibang thread, at maling apartment ang mauuninitialize. Ang pag-close sa FX thread ang
pinakamasama, dahil tinatawag doon ng JavaFX ang `OleInitialize` at nirerehistro ang drop target ng bawat window laban
dito — pinapatay ng isang `CoUninitialize` doon ang drag and drop sa buong app. Ito ang JavaFX sa Windows, hindi isang
partikular na bersyon ng JavaFX. Nagsusumite ang `connect()` at `disconnect()` sa thread na iyon at naghihintay, kaya
tinatawag silang dalawa ng `MainView` mula sa panandaliang background thread.

Dalawang patakaran ang pumipigil sa maling-asal na device na barahan ang koneksiyon. **Hindi kailanman humihinto nang
maaga ang teardown:** ginagawa ng `closeQuietly` ang disable, release at close bilang magkakahiwalay na hakbang, nagre-
release lang kapag hawak ang claim, at laging sinusubukan ang `close()` — pagkatapos ng palyadong claim, naghahagis ang
`release()` ng "not claimed", at noong iisang `try` ang pinagsasaluhan ng mga hakbang, nalalaktawan nito ang close at
naiiwang bukas ang device sa serbisyo ng Zebra. **Muling inaarmas ang data event sa loob ng `finally`:** dini-disable
ito ng JavaPOS pagkatapos ng bawat event, kaya ang read o handler na naghahagis ay kailangan pa ring muling mag-armas,
kung hindi ay tatahimik ang scanner hanggang mag-reconnect. Sinusubok ng `ScannerLifecycleTest` ang dalawa laban sa
isang naka-script na `jpos.Scanner`.

**Itinuturo ng `JposConfig` ang JavaPOS sa naka-bundle na `jpos.xml`** bago ang unang open. Sa mga JavaPOS jar ng Maven
(`javapos-config-loader` 4.x) at walang `jpos/res/jpos.properties` sa class path, kung hindi ay pumapalya ang bawat
open na may *"Service does not exist in loaded JCL registry"* (error 109), sa tatlong magkakahiwalay na dahilan: bumabalik
ang service manager sa `SimpleRegPopulator`, na nagbabasa ng serialized na `jpos.cfg` at hindi kailanman ng `jpos.xml`;
hinahanap lang ng XML loader ang `jpos.xml` sa working directory, hindi sa loob ng jar; at tinatanggihan ng Xerces
2.12.2 sa `lib/` ang JAXP property na `accessExternalDTD` na itinatakda ng loader. Itinatakda ng `JposConfig` ang
`jpos.config.regPopulatorClass` (`JavaxRegPopulator`), `jpos.config.populatorFileURL` (ang sariling
`jar:file:…!/jpos.xml` ng jar) at `javax.xml.parsers.SAXParserFactory` (ang sa JDK) bilang mga system property —
hindi kailanman ang nakatakda na, kaya makapipili pa rin ang `-Djpos.config.populatorFileURL=file:…` ng ibang
`jpos.xml` — at walang itinatakda kapag may `jpos.properties` sa class path o kapag wala ang 4.x loader. Ganoong kaso
ang `javaPOS114.jar` ng Zebra SDK at tuluyan nitong sinisira ang registry, kaya inilalayo ito ng `run.bat` sa class path
— tingnan ang [Ang mga native ng Zebra](#ang-mga-native-ng-zebra). Sinusuri ng `JposConfigTest` mula simula hanggang
dulo na hawak na ng registry pagkatapos ang mga scanner ng file.

**Binubuo ang device picker mula sa registry na iyon**, hindi iniingatan bilang listahan sa code: binabasa ng
`ScannerService.logicalNames()` ang mga naka-load na entry sa thread na `gaia-scanner` at itinatabi ang mga ang
kategorya ng device ay `Scanner` — nananatili sa labas ang dalawang entry ng timbangan sa `jpos.xml` — sa
`pickerOrder`: una ang `ZebraAllScanners` kapag dine-define ito ng file, ang iba ay ayon sa alpabeto (hindi iniingatan
ng registry ang pagkakasunod sa file). Hindi ito ibinibigay sa file na walang ganito; kapag walang na-load, ang default
lang ang iniaalok, kaya hindi kailanman walang laman ang picker. Ipinapakita kaagad ng `MainView` ang default at
pinupuno ang listahan mula sa background thread, pinananatili ang naka-save na pinili. Kaya ang scanner na idinagdag sa
`jpos.xml` — o isang buong file na pinangalanan gamit ang `-Djpos.config.populatorFileURL` — ay lumilitaw sa picker nang
walang pagbabago sa code, at sinusuri ng `JposConfigTest` ang picker laban sa hiwalay na pagbasa ng naka-bundle na file.
Ang mga serbisyong pinangangalanan ng mga entry na iyon ay mga class ng Zebra
(`com.zebra.jpos.service.scanner.*Factory`), na kailangan pa ring nasa class path — mula sa Scanner SDK sa pamamagitan
ng `run.bat`, o mga service jar ng Zebra na inilagay sa `lib\`.

Dine-decode ang data ng scan bilang **ISO-8859-1** para ang bawat byte ay tumapat nang 1:1 at mabuhay ang AIM Code ID
prefix at bawat `0x1D`; isinusulat ang mga ulat bilang UTF-8.

Walang bahagi ng serbisyo na bumubuo ng teksto para sa gumagamit. Lumalabas ang mga event bilang mga constant na
`ScannerListener.Event` kasama ang mga halagang nagbabago, at ang `MainView` ang nagbibigay ng pananalita mula sa
katalogo — kung hindi ay Ingles ang status line sa isang programang isinalin sa 35 paraan. Ang mga linya ng log ang
eksepsiyon at nananatiling Ingles.

### Ang platform gate

Ang `ScannerService.isSupportedPlatform()` ay iisang pagsubok sa `os.name`, at tinatanong ito ng `MainView` bago bumuo
ng anumang control ng scanner: sa macOS at Linux walang toggle, walang device picker, walang Scanner menu (kaya wala ring
<kbd>⌘K</kbd>), at walang label ng estado. Hindi sila itinatago — hindi sila kailanman nabubuo.

Ipinapadala ng Zebra ang mga serbisyo nitong JavaPOS para sa Windows lamang, at walang pumapalya nang maaga sa ibang
lugar: nare-resolve ang mga jar at nabubuo ang control, at ang `open()` ang sa wakas ay pumapalya dahil sa nawawalang
native layer, ilang segundo at isang dialog pagkalipas.

Ang patakaran ay tungkol sa mga *serbisyo*, hindi sa JavaPOS. Gagana rito ang vendor na nagpapadala ng cross-platform na
serbisyo, at ang pagpapalawak ng patakaran ay pagbabago sa `supportsJavaPos(String)` lamang — na package-private at
sinubok laban sa mga string ng `os.name` kaya nasasaklaw ang dalawang sagot sa anumang makinang nagpapatakbo ng build.

### Mga symbology

Itinutugma ng `Symbologies` ang lahat ng **78** na uri ng scan-data na `SCAN_SDT_*` ng JavaPOS sa mga pangalang
ipinapakita, at sa `DataCarrierType` ng core kung saan nagtutugma ang dalawang bokabularyo. Mas pino ang JavaPOS sa
dalawa — hinihiwalay nito ang UPC-A sa EAN-13, at ang payak na Data Matrix sa GS1 DataMatrix, samantalang pinagsasama ng
AIM ang buong pamilya sa ilalim ng iisang code character.

Ang uring wala sa talahanayan ay nakakakuha pa rin ng magagamit na pangalan, na hinahanap sa pamamagitan ng reflection
sa `ScannerConst`, dahil talagang nag-uulat ang mga serbisyo ng vendor ng mga halagang wala sa listahan ng spec. Isang
test ang dumaraan sa `ScannerConst` at pumapalya kung may idineklarang uri na walang pangalan — na humuhuli rin sa mga
constant na idaragdag ng susunod na JavaPOS.

Ilang carrier mapping ang sadyang iniwang `UNKNOWN` sa halip na hulaan: straight 2 of 5 (hinahati ng AIM ang `]R`/`]S`
ayon sa bilang ng start/stop bar, na hindi iniuulat ng JavaPOS), ang mga four-state postal code, at TLC-39.

---

## Lokalisasyon

Nakatira ang mga label sa iisang properties file bawat wika sa ilalim ng
`src/main/resources/i18n/ui_<CODE>.properties`, gamit ang parehong mga code ng mga katalogo ng error ng core (`EN`,
`FR`, `PCM`, …). Umiiral ang lahat ng 35 at magkakapareho ang hanay ng key ng lahat ng 35. Binabasa ang mga file bilang
**UTF-8**, hindi ang ISO-8859-1 na ipinapalagay ng `Properties`, kaya naiimbak ang mga salin nang nababasa sa halip na
bilang mga escape na `\uXXXX`.

**Bawat key ang fallback, hindi bawat file** — iyon ang nagpapakinabang sa bahagyang naisaling wika mula sa sandaling
dumating ang unang key nito. Ingles ang kontrata: dapat umiiral doon ang bawat key.

| Layer | Mga key | Saklaw |
|---|---|---|
| Pinagsasaluhang bokabularyo ng ulat | 31 | lahat ng 35 wika |
| Balangkas ng desktop — mga menu, button, builder, opsyon, status line, layout ng ulat, estado ng scanner, nilalaman ng dialog | 114 | lahat ng 35 wika |

Anim sa mga pinagsasaluhang termino (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) ang hindi
ginagamit ng ulat ng app na ito.
Sadyang itinatabi sila; sinasabi ito ng header ng file.

### Ang argumentong `--language`

Binabasa ng `GaiaDesktopApp.languageOverride()` ang `--language=…` sa pamamagitan ng sariling
`getParameters().getNamed()` ng JavaFX, nire-resolve ito gamit ang `Languages.fromArgument` — code ng katalogo, pangalan
ng enum, o BCP 47 tag, hindi sensitibo sa malaki o maliit na titik — at ibinibigay sa `MainView`.

**Para sa run ang override, hindi para sa file.** Itinatago ng `MainView` ang wikang ni-load nito sa `savedLanguage` at
*iyon* ang isinusulat pabalik, hanggang sabihin ng `languageChosenInSession` na ang gumagamit mismo ang pumili ng wika;
pagkatapos ay normal na nase-save ang pinili nila. Ang paghihiwalay na iyon ang buong disenyo: ang kiosk profile o demo
shortcut ay makapagbubukas sa Espanyol nang hindi binabago ang bubuksan ng may-ari ng makina bukas, habang nananatili pa
rin ang totoong pagpiling ginawa sa panel ng mga opsyon.

Ang halagang walang pinangangalanang katalogo ay itinatala sa log kasama ang mga tinatanggap na code at binabalewala. Ang
pagtangging magsimula dahil sa isang typo sa shortcut ang mas masamang kabiguan, at sa parehong dahilan nagbabalik ang
`Languages.fromArgument` ng `null` sa halip na maghagis.

### Pagdaragdag ng wika

Walang kailangang pagbabago sa code.

1. I-edit ang `ui_<CODE>.properties` — may file na ang bawat wika.
2. Isalin ang mga halaga. Iwan sa lugar ang mga placeholder na `{0}` / `{1}`.
3. Mag-build. Dalawang test ang nagpapatupad ng kontrata: pumapalya ang isa kapag humingi ang code ng key na hindi
   dine-define ng Ingles, ang isa pa kapag lumihis sa Ingles ang hanay ng key ng anumang katalogo.

Ini-scan ng unang test ang **source** para sa mga literal na key sa halip na mag-reflect sa mga class, dahil umiiral
lang ang mga key bilang mga string literal sa mga call site. Kaya kailangang buuin ang bagong mensahe na nakasulat
nang direkta ang key — `strings.format("st.scannerError", …)` — sa halip na hanapin sa pamamagitan ng variable, kung
hindi ay hindi ito makikita ng test.

### Ang sadyang hindi isinasalin

- ang maiikling pangalan ng field na nakalagay sa monospace sa tabi ng mga hilaw na halaga — ang `format`, `position`,
  `length`, `raw` at `part` ng element card, ang `AI`, `sep`, `pos` at `len` ng tape, at ang apat na cell na tumutukoy
  sa lokasyon ng error (`AI`, `position`, `level`, `stage`), na ang mga halaga ay hindi isinaling pangalan ng enum
  naman;
- ang mga pangalan ng sample input, na nagpapangalan sa mga GS1 construct, at ang mga pangalan ng symbology, na mga
  pangngalang pantangi;
- ang talata ng remediation ng `GE-C084` — prosa ng pamantayan kung saan mas masama ang kalahating salin kaysa Ingles;
- ang huling panangga ng `ElementBuilder` para sa hindi kilalang AI, na hindi maaabot mula sa UI: ang editor mismo ang
  sumusuri sa mga hindi kilalang AI at iniuulat ang mga ito mula sa katalogo;
- mga linya ng log.

May isang puwang pang natitira: ibinibigay ng `ScanSource.label()` ang `manual` / `scanner` / `file` / `built` sa
hilera ng kasaysayan at sa masthead ng ulat sa Ingles. Higit pa sa isang key ang kailangan para isara ito — walang
`UiStrings` ang `HistoryCell`, at isang beses lang ini-install ang cell factory ng listahan, kaya kailangan ding i-refresh
ang mga cell kapag nagpalit ng wika.

---

## Mga format ng naka-imbak na estado

Dalawang file sa ilalim ng `~/.gaia-desktop/`, pinaghiwalay dahil magkaiba ang bilis ng kanilang pagbabago — hindi dapat
mawalan ng mga setting ang gumagamit dahil sa sirang o mano-manong inedit na capture log. I-override ang direktoryo gamit
ang `-Dgaia.desktop.home=…`, na siyang paraan para lumayo ang mga test sa totoong home directory.

**`options.properties`** — isinusulat kapag nagbago ang isang opsyon, at sa paglabas.

| Key | Kahulugan |
|---|---|
| `parse.mode`, `parse.language` | Lalim ng pipeline at wika ng mensahe |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Pagpapakita ng petsa |
| `check.skipRequires`, `check.skipExcludes` | Mga pag-iwas sa structural check |
| `ui.theme` | `LIGHT` o `DARK` |
| `ui.optionsPane` | Bukas ba ang panel? |
| `ui.scannerDevice` | Huling lohikal na pangalan ng JavaPOS |
| `ui.mode` | `PARSE`, `BUILD` o `COMBINE` |
| `update.check` | Kung tumatakbo ang araw-araw na pagsuri ng update (default `true`) |
| `update.lastCheck` | Kailan ito huling tumakbo, ISO-8601; kung wala o hindi mabasa, nangangahulugang dapat nang sumuri |
| `update.skipVersion` | Isang release na pinilian ng gumagamit ng *Laktawan ang bersyong ito*; hindi na awtomatikong iaalok muli |

Hindi kailanman nakamamatay ang nawawala o sirang file: bawat halaga ay nagfa-fallback nang hiwalay, kaya ang isang
masamang linya ay iyon lang ang nawawala at wala nang iba. Sa unang paglunsad, nang walang anumang file, pinupunan ang
mga opsyon mula sa mga regional setting ng makina ng `SystemDefaults` sa halip na mula sa mga built-in na default ng
GAIA — ibinibigay ng maikling date pattern ng locale ang pagkakasunod ng mga bahagi, ang separator, at kung pinaikli ang
buwan at taon.

Huwag kailanman bigyan ng null ang `Properties.setProperty`: naghahagis ito, at tumatakbo ang `persistOptions()` mula sa
mga UI event handler kung saan tumatakas ang exception papunta sa FX thread. Totoong crash iyon na naranasan na ng app
na ito.

**`captures.tsv`** — isinusulat kapag nagbago ang kasaysayan, at sa paglabas. Pinakabago muna, hanggang **500** na
entry.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Iniimbak lang ang hindi na makakalkula muli. **Hindi** sine-serialize ang `ParseResult` — muli itong kinukuha sa
pamamagitan ng muling pag-parse ng input sa pag-load, kaya ang naibalik na kasaysayan ay sumasalamin sa mga opsyong
umiiral *ngayon*. Karaniwang may FNC1 ang mga input at anumang iba pang inilalabas ng scanner, kaya ine-escape ang input
field (`\\`, `\t`, `\n`, `\r`, at `\xNN` para sa lahat ng iba pang mas mababa sa `0x20`) at binabasa at isinusulat ang
file bilang ISO-8859-1, byte por byte.

Ang mga file na isinulat bago itinala ang petsa ay may oras ng orasan lamang; naglo-load pa rin sila, may petsa ngayong
araw, dahil hindi kailanman naisulat ang totoong petsa.

---

## Pagsubok

```bash
(cd gaia-desktop && mvn test)
```

198 na test, sadyang **walang JavaFX toolkit**, na nagpapanatiling mabilis at headless sa kanila. Saklaw nila ang
renderer ng ulat, ang lohika ng pagbuo at mga paghahanap ng AI, ang conversion ng `{GS}`, ang mga default na hango sa
locale, ang dalawang store (kasama ang round trip na may `0x1D` at mga sadyang sinirang file), ang talahanayan ng
symbology, ang patakaran sa platform ng scanner, ang version stub, at ang mga katalogo.

Tatlo ang dapat malaman dahil ipinagtatanggol nila ang mga kontrata sa halip na mga function:

| Test | Pumapalya kapag |
|---|---|
| `CatalogueCoverageTest` | Humingi ang code ng key na hindi dine-define ng `ui_EN.properties`, o lumihis ang alinman sa 35 file sa hanay ng key ng Ingles |
| `SymbologiesTest` | May constant na `SCAN_SDT_*` na idineklara ng JavaPOS na walang pangalang ipinapakita — kasama ang idinagdag ng upgrade ng JavaPOS |
| `ScannerServiceTest` | Nagbago ng sagot ang patakaran sa platform para sa Windows o para sa anumang iba |

Sadyang mapagpatawad ang `UiStrings` sa runtime — ipinapakita ang hindi kilalang key bilang ang key mismo sa halip na
maghagis sa gitna ng layout — kaya eksaktong kailangang umiiral ang test ng katalogo.

**Hindi saklaw:** anumang nangangailangan ng buhay na toolkit — gawi ng focus, ang mode toggle, pag-edit ng
talahanayan, ang pagpapalit ng wika, at ang dalawang crash na inilarawan sa ilalim ng [Threading](#threading).
Na-verify ang mga iyon sa pamamagitan ng pagpapatakbo ng totoong window, pero hindi papalyahin ang build ng regression sa
kanila. Maisasara ang puwang na iyon sa pagdaragdag ng TestFX o Monocle.

---

## Pag-aayos ng problema sa build

| Sintomas | Sanhi at lunas |
|---|---|
| `package tools.pantheum.gaia does not exist` | Hindi naka-install ang core. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | Ang `Main-Class` ng jar ay dapat `Launcher`, hindi `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* sa pagsisimula | Inaasahan at hindi nakapipinsala — sinasabi lang ng JavaFX na nasa class path ito. Iyon mismo ang dahilan kung bakit umiiral ang `Launcher` |
| Ayaw magsimula ng jar, `NoClassDefFoundError` | Mga class lang ng module na ito ang dala ng default na jar; kailangang nasa tabi nito ang `dist/lib/`. Gamitin ang `-Pfat` para sa iisang self-contained na file |
| `Artifact has not been packaged yet … MDEP-187` sa Eclipse | Workspace resolution ng m2e. Tingnan ang tala sa ilalim ng [Packaging](#packaging) |
| May lumilitaw na dobleng `… 2.properties` na file sa build | Nasa folder na sini-sync ng iCloud ang project at gumawa ang sync ng mga kopyang nagkakasalungat sa gitna ng build. Laging `mvn clean package`; mas mabuti pa, alisin ang `target/` sa sync |
| Nagpapakita ang menu item ng key tulad ng `act.hri` | Nawawala ang key na iyon sa `ui_EN.properties`. Dapat ay nahuli ito ng build — tingnan ang [Pagsubok](#pagsubok) |

---

## Mga kilalang limitasyon at natitirang trabaho

- **Walang headless na UI test.** Tingnan ang [Pagsubok](#pagsubok).
- **Mga artifact na partikular sa platform.** Pareho ang dalawang packaging na nagbu-bundle ng mga native ng JavaFX ng
  iisang platform; mag-build ng isa bawat target gamit ang `-Dfx.platform` (kayang i-build ng anumang makina ang lahat).
  Walang nagbu-bundle ng Java runtime — kailangan doon ang `jpackage`, na nagbi-build lang para sa OS na pinapatakbuhan
  nito.
- **Windows lang ang input ng scanner.** Tingnan ang [Ang platform gate](#ang-platform-gate).
- **Hindi naka-localize ang `ScanSource.label()`.** Tingnan ang [Ang sadyang hindi
  isinasalin](#ang-sadyang-hindi-isinasalin).
- **Ang `UiState.mode` ay isang `String`** (`"PARSE"` / `"BUILD"`) habang enum ang `Theme` sa tabi nito, kaya lumilitaw
  ang literal sa ilang lugar. Aalisin sila ng isang enum.
- **Domain at path prefix lang ang target ng Digital Link.** Sinusuportahan din ng `BuilderDigitalLinkConfig` ang mga
  dagdag na query parameter at isang fragment; hindi ito inilalantad ng editor.
- **Hindi minamarkahan sa editor ang magkasalungat na pares ng AI (`GE-S006`)** gaya ng hindi kilala at dobleng AI —
  basta na lang pumapalya ang pagbuo.
- **Dinodoble ng katalogo ng error ng core ang mga panipi** (`value ''09506000134350''`), na lumilitaw nang ganoon sa
  mga palya ng pagbuo at sa ulat. Mukha itong quote-escaping ng `MessageFormat` na hindi ina-unescape; sa core nabibilang
  ang pag-aayos.
