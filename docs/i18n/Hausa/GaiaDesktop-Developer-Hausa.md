# GAIA Desktop — Jagorar mai haɓakawa

Yadda ake gina module ɗin, yadda aka tsara shi, da yadda ake gwada shi. Don abin da manhajar take yi idan aka duba
daga waje, duba [Jagorar mai amfani](GaiaDesktop-User-Hausa.md). Asalin takardar:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Abubuwan da ke ciki

1. [Module a taƙaice](#module-a-taƙaice)
2. [Gini da gudanarwa](#gini-da-gudanarwa)
   - [Tattara kunshi](#tattara-kunshi)
3. [Tsarin gini](#tsarin-gini)
   - [Mazurari na shigarwa](#mazurari-na-shigarwa)
   - [Zaren aiki](#zaren-aiki)
   - [Sake ginawa ko sake lakabi](#sake-ginawa-ko-sake-lakabi)
4. [Alamar {GS}](#alamar-gs)
5. [Haɗa sikana](#haɗa-sikana)
   - [Ƙofar dandali](#ƙofar-dandali)
   - [Tsarin lambobin ƙira](#tsarin-lambobin-ƙira)
6. [Daidaita harshe](#daidaita-harshe)
   - [Hujjar `--language`](#hujjar---language)
   - [Ƙara harshe](#ƙara-harshe)
   - [Abin da ba a fassara da gangan ba](#abin-da-ba-a-fassara-da-gangan-ba)
7. [Tsarin bayanan da aka adana](#tsarin-bayanan-da-aka-adana)
8. [Gwaji](#gwaji)
9. [Warware matsalolin gini](#warware-matsalolin-gini)
10. [Iyakokin da aka sani da aikin da ya rage](#iyakokin-da-aka-sani-da-aikin-da-ya-rage)

---

## Module a taƙaice

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Abokin hulɗa ne na babban laburare kawai, ba fiye da haka ba. **Babu wani ilimin GS1** a cikinsa: kowace ma'anar AI,
dokar tantancewa, saƙon kuskure da bayanin da aka fassara suna zuwa ne daga
[`GaiaParser`](../../../../gaia/docs/GaiaParser.md) da [`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md).
Musamman, ba a taɓa kwafar jerin AI zuwa cikin wannan module ba — mai zaɓi na mai ginawa yana karanta rajistar
cibiya da kanta, don haka ba zai iya bayar da AI da mai ginawa zai ƙi ba.

**Wuraren shiga:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (`Application` na JavaFX) da
`tools.pantheum.gaia.desktop.Launcher` (`Main-Class` na jar da aka tattara).

Module ɗin ganye ne: babu abin da ya dogara da shi, kuma shi ma bai dogara da wani module na ƙasa ba.

---

## Gini da gudanarwa

- **Java 21** don gudanarwa, da JDK 21 ko sabo don ginawa — ana harhaɗa module ɗin da `release=21`. JavaFX 21 shi ne
  layin LTS da ya dace. Jar na cibiya har yanzu bytecode ne na Java 11 kuma yana aiki ba tare da canji ba a kan 21.
- **Maven** a kan `PATH`. Babu wrapper a wannan rumbun.
- **Fara shigar da cibiya.** Wannan module yana samo `gaia:1.1.0` daga `~/.m2`; jar na cibiya da ya tsufa ko babu shi
  shi ne sanadin da aka saba na `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` ita ce kaɗai hujjar layin umarni, kuma ita ce kaɗai hanyar da za a shafi farawa ban da
`-Dgaia.desktop.home=…`, wadda ke nuna dukan bayanan martaba zuwa wani wuri.

`mvn javafx:run` ita ce hanyar haɓakawa: plugin ɗin yana sanya JavaFX a kan **hanyar module**, don haka kayan aikin
yana farawa a matsayin module mai suna kuma ana iya ƙaddamar da `GaiaDesktopApp` kai tsaye. Jar da aka tattara ba zai
iya yin haka ba — komai yana kan hanyar aji — shi ya sa `Main-Class` ɗinsa shi ne shim ɗin `Launcher`. Wani ƙaramin ajin
`Application` a matsayin `Main-Class` a irin wannan tsari yana kasawa da *"JavaFX runtime components are missing"*.

Kullum yi amfani da `clean` — sau ɗaya, kafin gina rukunin dandali, domin yana wofintar da `target/dist/` daga dukansu.
Duba [Warware matsalolin gini](#warware-matsalolin-gini) don dalilin.

### Tattara kunshi

Duk abin da za a iya aikawa ana tattara shi ƙarƙashin `target/dist/gaia-desktop-<version>-<platform>/` kuma babu wani abu
dabam, don a iya zip ko kwafar babban fayil ɗin a matsayin rukuni ɗaya ba tare da zaɓar kayayyaki daga `target/` kusa
da `classes/`, `maven-status/` da sauran fayilolin wucin gadi na gini ba. `${dist.dir}` yana ba shi suna sau ɗaya;
kowane plugin da ke rubuta kaya yana nuna shi.

#### Gini ɗaya ga kowane dandali

Jar na JavaFX suna ɗauke da lambar asali, don haka kowane rarrabawa na dandali ɗaya ne, wanda halayyar `fx.platform`
ke ba shi suna a matsayin mai rarrabewa na OpenJFX: `mac-aarch64`, `mac`, `win`, `linux` ko `linux-aarch64`. Bayanan
martaba biyar na `fx-*` da OS ke kunnawa suna saita shi ga dandalin injin da ke ginawa, kuma abin da `mvn package` ko
`mvn javafx:run` na yau da kullum ke buƙata kenan; `-Dfx.platform=…` yana rinjayar su don gina kowane dandali dabam,
daga kowane inji — jar na JavaFX zazzagewa ne kawai. `${dist.dir}` shi ne `target/dist/${fx.platform}`, don haka ginin
dandali ɗaya ba ya taɓa rubuta kan ginin wani.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Cikakkun bayanai biyu na pom ne ke sa wannan aiki, kuma dukansu suna kama da ana iya cire su:

- **Kowane module na JavaFX dogaro ne kai tsaye** — base, graphics, controls, media da web — kowanne da
  `<classifier>${fx.platform}</classifier>` da keɓewar `org.openjfx:*`. Pom na OpenJFX suna zaɓar jar ɗinsu na
  canzawa bisa OS na *injin da ke ginawa*, don haka barin graphics ko base su zo ta hanyar canzawa yana saka kayan
  asali na Mac cikin ginin Windows kusa da na Windows.
- **Halayyar ita ce `fx.platform`, ba `javafx.platform` ba.** Idan aka saita ta daga waje, pom na iyaye na OpenJFX yana
  sake ayyana halayyar OpenJFX da kanta; Maven yana ƙin wannan pom a matsayin mara inganci kuma a ɓoye yana zubar da
  jar na dandali, yana barin gini babu JavaFX a ciki.

Duka tsare-tsaren biyu suna rubuta hanya ɗaya, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`, kuma
ɗaya kawai daga cikinsu ke gudana a kowane gini. `${jar.phase}`, `assembly.skipAssembly` da `mdep.skip` ne ke hana su
duka biyun su mallake ta.

| | Na asali | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — azuzuwa da albarkatun wannan module kawai | ~50 MB — an buɗe dogaro a cikinsa |
| `dist/gaia-desktop-<version>-<platform>/lib/` | jar 16, an ambace su ɗaya bayan ɗaya a `Class-Path` na manifest | — |
| Aika | jar **da** `lib/` a gefensa | fayil ɗaya |
| Haka kuma a `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) ko `run.sh` (sauran) | iri ɗaya |
| Da | `readme.html` da `docs/` (kowane dandali) | iri ɗaya |

Kowane rarrabawa yana ɗauke da jagororin ma: ana kwafar `docs/` tare da tsarinsa, don hanyoyin haɗi tsakanin jagorori
da fassarori su ci gaba da aiki, ban da kayan aikin fassara (`check-i18n.py`, `i18n-manifest.json`);
da `readme.html` a tushe, fihirisa da ke haɗawa zuwa kowannensu, daga `src/main/dist/readme.html` inda ake maye
`@VERSION@` da sigar pom. Matakin antrun na `pick-launch-script` ne ke rubuta su duka, kuma yana goge `docs/` da farko
don jagorar da aka cire kar ta ci gaba da zama. An rubuta hanyoyin haɗi na readme da hannu — jagorar da aka sake wa
suna ko fassarar da aka ƙara ƙarƙashin `docs/` tana buƙatar layinta a can ma.

`Class-Path` na manifest da `lib/` rabi biyu ne na abu ɗaya: jar yana ambaton `lib/<artifact>-<version>.jar` ga kowane
dogaro na lokacin gudana, kuma `copy-dependencies` yana cika `lib/` da daidai waɗannan sunaye. Wannan ne ke sa
`java -jar` ya yi aiki.

#### Manhajar macOS da zip na rarrabawa

Aiwatarwar antrun ɗaya, `mac-app-and-zip`, ita ke ƙare matakin `package`. Saboda haka ne aka ayyana plugin na antrun
**bayan** plugin na jar da assembly — Maven yana gudanar da aiwatarwar mataki bisa tsarin ayyana plugin — don haka jar
yana nan, a kowane tattarawa, lokacin da yake gudana. (Ɗayan aiwatarwarsa, `pick-launch-script`, tana cikin
`prepare-package` kuma ba ta shafu ba.)

Don `mac` da `mac-aarch64` da farko yana gina `GAIA Desktop.app` a cikin babban fayil na rarrabawa: `Info.plist` (inda
aka maye `@VERSION@` da sigar pom), mai ƙaddamarwa `Contents/MacOS/gaia-desktop` (an sa ya zama mai aiwatarwa) da
`gaia-desktop.icns`, duka daga `src/main/macos/`, sannan yana **matsar** da `gaia-desktop.jar` da `lib/` zuwa cikin
`Contents/Java`, don kunshin ya tsaya da kansa. `run.sh` yana duba can lokacin da babu jar a gefensa. Mai ƙaddamarwa
yana neman Java bisa sigar (`java_home -v 21+`, sannan openjdk na Homebrew — ƙaddamarwa daga Finder ba sa ganin
`JAVA_HOME` ko `PATH`), yana nuna akwatin magana ta `osascript` idan babu, yana `cd` zuwa `~/.gaia-desktop` don a iya
rubutu a `logs/` na dangi na logback, kuma yana aika `-Xdock:name`/`-Xdock:icon`. macOS yana haɗa `java` da ke gudana da
kunshin (bundle id da gunkin Dock nasa na manhajar ne); sandar menu tana nuna GAIA Desktop.

Sannan, ga kowane dandali, yana zip babban fayil ɗin zuwa `target/dist/gaia-desktop-<version>-<platform>.zip` ƙarƙashin
babban fayil mai suna iri ɗaya, da `<zip>` na Ant: `run.sh` da `Contents/MacOS/**` a matsayin rukunin fayil dabam da
`filemode="755"`, in ba haka ba ba za su gudana ba bayan an buɗe zip. **Ba a haɗa** zip ɗin ba: na sakin GitHub ne,
kuma dandali huɗu suna zuwa daga gini huɗu, waɗanda Maven Central — loda ɗaya marar canzawa ga kowace siga — ba zai taɓa
iya karɓa a matsayin saki ɗaya ba.

An saita `project.build.outputTimestamp` zuwa lokacin gini. In ba haka ba masu adanawa a maven-jar-plugin 3.5 da
maven-assembly-plugin 3.8 suna sanya ranar 1 ga Fabrairu 1980 a kowace shigarwa.

`tools/icon/GaiaIcon.java` ne ke zana gunkin manhajar (ba ɓangare na gini ba ne; kansa yana da matakan
`sips`/`iconutil`). An adana abubuwan da yake fitarwa a rumbun: `src/main/macos/gaia-desktop.icns` don kunshin, da
`src/main/resources/icons/gaia-desktop-{16..256}.png`, waɗanda `GaiaDesktopApp` ke saitawa a matsayin gunkin taga
(Windows da Linux; macOS yana watsi da gumakan taga).

#### Rubutun farawa

`run.sh` da `run.bat` suna zuwa daga `src/main/scripts/`, ana kwafar su zuwa `dist/gaia-desktop-<version>-<platform>/`
ba tare da tacewa ba — `run.sh` cike yake da faɗaɗawar shell na `${...}` waɗanda tacewar Maven za ta iya sake rubutawa
yadda ta ga dama. Sannan matakin antrun yana ajiye wanda dandalin ke gudanarwa kaɗai — `run.bat` don `win`, `run.sh`
don kowane sauran — kuma yana saita bit na aiwatarwa a kan `run.sh`, domin `copy-resources` ba ya ɗauke shi. Yana
yanke shawara daga `${fx.platform}` da aka warware maimakon ta bayanan martaba, domin kunna bayanin martaba ba zai iya
ganin ƙimar da wani bayanin martaba ya saita ba: in ba haka ba ginin Linux da aka yi a Windows zai sami rubutun mai
masauki.

**Ba sa dogara da manifest**. Kowanne yana gina hanyar aji da kansa, `gaia-desktop.jar` sannan kowane `.jar` a `lib/`,
an ambace su ɗaya bayan ɗaya maimakon a bar wa katin daji na `lib/*`, don hanyar da ke da sarari ta kasance shigarwa ɗaya
kuma ƙidayar abin da aka ƙara ta zama ta gaskiya. Ƙaddamarwa da `-cp` ne dalilin da ya sa suke ambaton
`tools.pantheum.gaia.desktop.Launcher` a fili: `-cp` ba ya karanta `Main-Class` na manifest. **Duk da haka** yana bin
`Class-Path` ɗinsa — jar a kan `-cp` yana kawo jar da manifest ɗinsa ya ambata, waɗanda ake bincika nan da nan bayansa —
don haka kowane jar na `lib/` yana lodawa ko rubutun ya lissafa shi ko bai lissafa ba, kuma barin ɗaya daga hanyar aji
na rubutun ba ya hana shi shiga JVM. Rubutun suna lissafa su duka duk da haka, don jar wanda manifest ɗinsa ya tsufa,
aka gyara da hannu ko ya rasa shigarwa ɗaya ya fara aiki har yanzu. Rubutu ɗaya yana hidimar tattarawa biyun: jar na
`-Pfat` ba shi da `lib/` da za a ƙara kuma ba ya buƙata, shi ya sa `lib/` maras komai sanarwa ce ba kuskure ba.

#### Abubuwan asali na Zebra

Ginin ba ya aika kome na Zebra — babu DLL, babu jar na sabis. A Windows suna zuwa daga Scanner SDK na Zebra da aka
shigar a injin: `run.bat` yana ƙara jar ɗin sabis na `Jpos*.jar` da `bin\` zuwa hanyar aji da `bin\` zuwa `PATH`, inda
`System.loadLibrary` ke samun `CSJPOSScanner.dll` / `CSJPOSScanner64.dll` (`java.library.path` ta asali ita ce `PATH`).
API na JavaPOS da mai lodawa na saitinsa kullum suna zuwa daga `lib\javapos-*.jar`, akwai SDK ko babu.

**`run.bat` ba ya taɓa ƙara jar na JavaPOS na SDK da kansa**, wato `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`. A da
yana yi, yana barin `lib\javapos-*.jar` don guje wa kwafi biyu na API — amma `Class-Path` na manifest ya loda su duk da
haka (duba sama), don haka JVM ta sami duka biyun: azuzuwan mai lodawa na 4.x daga `lib\`, tare da
`jpos/res/jpos.properties` da `jcl.xsd` na `javaPOS114.jar` a gefensu. Fayil ɗin halaye yana sa
[`JposConfig`](#haɗa-sikana) ya ja da baya, kuma wannan `jcl.xsd` yana karɓar sigogin JavaPOS har zuwa 1.10 kawai, don
haka kowace shigarwa a `jpos.xml` (1.12–1.14) ta kasa tantancewa, rajistar ta kasance babu kome, kuma kowace haɗawa ta
kasa da kuskure 109 kafin a ma kai ga sikana. Kasancewar wannan jar a kan hanyar aji kawai ya isa haka, ko da yana bayan
jar na `lib\`. Jar na sabis na Zebra suna aiki ba tare da canji ba a kan API na 1.14 daga `lib\` — an tabbatar da buɗewa,
mallaka da duba a kan sikana ta gaske ta haka. Ana girmama DLL da aka ajiye kusa da `run.bat` da hannu har yanzu: a lokacin
rubutun yana saita `-Djava.library.path=<dist>;%PATH%`, yana ƙara `PATH` a ƙarshe domin halayyar tana maye gurbin hanyar
bincike ta asali gaba ɗaya.

`mvn javafx:run` ba ya yin kome daga cikin waɗannan, don haka mallakar sikana daga gudanarwar haɓakawa a Windows na
buƙatar jar na SDK a kan hanyar aji da `bin\` ɗinsa a kan `PATH` ta wasu hanyoyi; gudanar daga `dist/` don haka.

Bayan hanyar aji suna kula da abin da `java -jar` ba ya yi: warware babban fayil ɗinsu don su yi aiki daga kowane babban
fayil na aiki, fifita `JAVA_HOME` a kan `PATH`, da ƙin JVM kafin 11 da jumlar da ke faɗin magani maimakon
`UnsupportedClassVersionError`. `GAIA_DESKTOP_OPTS` yana wuce zaɓuɓɓukan JVM. `run.bat` yana amfani da `java.exe`, ba
`javaw.exe` ba, don na'ura mai kwakwalwa ta kasance a buɗe — Windows ita ce kaɗai dandalin da sikana ke aiki, kuma
matsayinta da kurakuran JavaPOS suna zuwa can.

`run.bat` kuma yana zaɓar Java, JavaFX da sabis na Zebra daga abin da ya samu — `jdk*\` da aka haɗa, `javafx-sdk*\lib\`
a kan hanyar module, Scanner SDK na Zebra a `ZEBRA_HOME` — yana barin kwafin JavaFX na `lib\` daga hanyar aji lokacin da
yake amfani da na SDK, kuma yana aika mai fassara SAX na JDK, proxy da ma'ajiyar amana na Windows, da
`--enable-native-access` a Java 17+. [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) na
README yana da teburin. Ajiye ƙarshen layi na CRLF na fayil ɗin: `cmd.exe` na iya rasa `call :label` a fayil na batch
mai LF kawai. `run.sh` ma yana aika `--enable-native-access=ALL-UNNAMED` — JavaFX yana loda kayan asalinsa daga `lib/`,
kuma idan JavaFX yana kan hanyar aji `ALL-UNNAMED` ne module da ke rufe shi — ana tsallake shi, kamar a `run.bat`, sai
lokacin da ba a iya karanta sigar Java ba.

Kowane tsari yana ɗauke da kayan asali na JavaFX **na dandali ɗaya**, `${fx.platform}`, don haka babu ɗayansu da za a
iya ɗauka zuwa wani OS ko gine-gine. Gina sau ɗaya ga kowane manufa — duba
[Gini ɗaya ga kowane dandali](#gini-ɗaya-ga-kowane-dandali).

> **Eclipse.** Aiwatarwar `copy-dependencies` tana ɗauke da umarnin `<?m2e ignore?>`. m2e yana warware aikin wurin aiki
> da ke buɗe zuwa babban fayil ɗinsa na `target/classes` maimakon jar, kuma `copy-dependencies` yana ƙin kwafar kayan da
> ba a tattara ba (MDEP-187), wanda zai sa ginin kai tsaye ya kasa duk lokacin da aikin `gaia` yake buɗe kusa da wannan.
> Sakamakon shi ne ginin da aka yi a IDE kawai ba ya cika `target/dist/gaia-desktop-<version>-<platform>/lib/`; gudanar da
> `mvn package` kafin amfani da jar ɗin kansa.

---

## Tsarin gini

| Kunshi | Abin da yake riƙe |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (taga), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (dabarar gini marar JavaFX), `AiPair` (layin edita ɗaya) |
| `desktop.parse` | `ParseOptions` (`ParseConfig` mai lura), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — mai zana rahoto, marar JavaFX kuma ana gwada shi ba tare da kayan aiki ba |
| `desktop.i18n` | `UiStrings` — kundin lakabi |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### Mazurari na shigarwa

Kowace shigarwa — wadda aka buga, liƙa, buɗe daga fayil, duba, ko gina — tana isa ga mai fassara ta hanya ɗaya,
`MainView.capture(String, ScanSource, String)`.

Wannan shi ne kaɗai shawarar ƙira da aka gina manhajar a kanta. Gini ba ya buƙatar rahotonsa, tarihinsa ko hanyar
fitarwa tasa, domin bayan mazurari ba za a iya bambanta igiyar da aka gina da wadda aka duba ba. Duk wani sabon abu da ke
samar da shigarwa ya kamata ya bi ta cikinsa maimakon gefensa.

### Zaren aiki

| Zare | Abin da yake gudanarwa |
|---|---|
| Zaren manhajar JavaFX | Duk fassara, zane da UI. Fassara ba ta kai millisecond ɗaya ba idan teburan AI sun yi zafi, don haka ba ta buƙatar ma'aikaci |
| `gaia-scanner` | Kowane kira zuwa sikana — buɗewa, mallaka, kunnawa, kashewa, sakewa, rufewa. Zare ɗaya mai tsawon rai mallakar `ScannerService`; duba [Haɗa sikana](#haɗa-sikana) |
| Zaren abubuwan JavaPOS | Kiran dawowa na `ScannerService`. `MainView.FxScannerListener` yana ƙetare kowannensu da `Platform.runLater` |
| Bango | Jiran `connect()` / `disconnect()` na sikana (mallaka na iya ɗaukar har zuwa 5 s), maido da tarihin da aka adana a farawa — wanda kuma yake ɗumama mai fassara — da duba sabuntawa (`gaia-update-check`, buƙatar GitHub ɗaya, ana mayar da sakamakon da `Platform.runLater`) |

**Kada ka taɓa canza zaɓin wani sarrafi, ko lalata sarrafin, daga cikin abin zaɓinsa na kansa.** Faɗuwa uku dabam a
wannan manhaja sun fito daga wannan daidai: sake saita mai zaɓin AI a cikin `clearAndSelect` na popup ɗin kansa (wani
`IndexOutOfBoundsException` daga `ListViewBehavior`), da sake gina fanel ɗin da ke da akwatin zaɓin harshe daga cikin
mai sarrafa wannan akwatin. Dukansu yanzu ana jinkirta su da `Platform.runLater`, kuma an rubuta dalilan a wuraren kiran
biyu.

### Sake ginawa ko sake lakabi

Canza harshe yana **sake gina** firam ɗin daga kundin — menus, sandar kayan aiki, fanel ɗin zaɓuɓɓuka — wanda yake da
aminci domin babu ɗayansu da ke riƙe da yanayi: `OptionsPane` kallon hanya biyu ne na `ParseOptions`, don haka ƙimomi
suna cikin samfurin kuma sabon fanel yana ɗauko su nan take.

Sarrafin da ke riƙe da **bayanan mai amfani** filaye ne, kuma ana sake musu lakabi a wurinsu maimakon haka:
`BuilderPane.retext(UiStrings)` yana ajiye layukan AI, filayen Digital Link da zaɓi. Sake gina shi zai watsar da aikin da
ake yi — kamar yadda ya taɓa yi.

---

## Alamar {GS}

`TextField` na JavaFX a ɓoye yana zubar da kowane harafi ƙasa da `0x20` (`TextInputControl.filterInput`). Ba "yana nuna
shi da ban mamaki" ba — yana cire shi. Bugawa, liƙawa, `setText` da `insertText` duka abin ya shafa, don haka
`setText("01…" + GS + "10LOT")` yana bayar da `"01…10LOT"`: igiyar abubuwa wadda ma'anarta ta canza a ɓoye, domin AI mai
tsawo mai canzawa yanzu yana haɗiye abin da ke biye da shi.

`desktop.parse.GsToken` shi ne dukan amsar. Mai rabawa yana tafiya ta cikin UI a matsayin rubutu na zahiri `{GS}` kuma
yana sake zama `0x1D` a hanyar zuwa mai fassara. Ba a ƙirƙiri alamar a nan ba — daidai yadda `ASCIIRevealerUtils.printable`
na cibiya ke nuna `0x1D` ne, don haka ƙimar da aka kwafa daga rahoto tana liƙuwa kai tsaye cikin sandar shigarwa.

Matatar `TextFormatter` a kan filin shigarwa tana kama kowace hanya da rubutu zai iya zuwa — bugawa, ⌘V, menu na mahallin,
ja da sakewa, `setText` — shi ya sa sake rubutun yake can maimakon a kowane mai sarrafawa.

Hanyar sikana ba ta taɓa bi ta `GsToken` ba: dubawa suna isa ga mai fassara a matsayin baiti.

---

## Haɗa sikana

`ScannerService` ne ke da sarrafin `Scanner` na JavaPOS: buɗewa → mallaka → kunnawa, yana sake ɗaura abin bayanai bayan
kowane dubawa. Sunayen na'urori na hankali suna zuwa daga `src/main/resources/jpos.xml`.

**Kowane kiran na'ura yana gudana a kan zare ɗaya, `gaia-scanner`, wanda yake rayuwa muddin sabis ɗin yana rayuwa** —
ba a taɓa a kan zaren FX ba, kuma ba a taɓa a kan zaren da ke ƙarewa bayan kiran ba. Sabis na Zebra yana isa ga sikana
ta JNI a kan CoreScanner COM API, kuma COM yana ɗaure abin da ya shirya ga ɗakin zaren da ya yi kira: buɗe a kan zaren
da zai ƙare daga baya kuma sikana tana mallaka amma tana yin shiru; rufe a kan wani zare kuma ana soke farawar ɗakin da
bai dace ba. Rufewa a kan zaren FX ita ce mafi muni, domin JavaFX yana kiran `OleInitialize` a can kuma yana rajistar
manufar sakewa na kowace taga a kansa — `CoUninitialize` a kansa yana kashe ja da sakewa a dukan manhajar. Wannan JavaFX
ne a Windows, ba wata sigar JavaFX ɗaya ba. `connect()` da `disconnect()` suna miƙa aiki ga wannan zaren kuma suna jira,
don haka `MainView` yana kiran su duka daga zaren bango mai gajeren rai.

Dokoki biyu suna hana na'urar da ke rashin ɗabi'a kulle haɗin. **Wargazawa ba ta taɓa tsayawa da wuri ba:**
`closeQuietly` yana kashewa, sakewa da rufewa a matsayin matakai dabam, yana sakewa ne kawai lokacin da ake riƙe da
mallaka, kuma kullum yana gwada `close()` — bayan mallaka ta kasa, `release()` yana jefa "not claimed", kuma lokacin da
matakan suka raba `try` ɗaya hakan ya tsallake rufewar ya bar na'urar a buɗe a sabis na Zebra. **Ana sake ɗaura abin
bayanai a cikin `finally`:** JavaPOS yana kashe shi bayan kowane abu, don haka karatu ko mai sarrafawa da ya jefa dole
ne ya sake ɗaurawa, in ba haka ba sikana za ta yi shiru har sai an sake haɗawa. `ScannerLifecycleTest` yana gwada duka
biyun a kan `jpos.Scanner` da aka rubuta.

**`JposConfig` yana nuna JavaPOS zuwa `jpos.xml` da aka haɗa** kafin buɗewa ta farko. Da jar na JavaPOS na Maven
(`javapos-config-loader` 4.x) kuma babu `jpos/res/jpos.properties` a kan hanyar aji, in ba haka ba kowace buɗewa tana
kasawa da *"Service does not exist in loaded JCL registry"* (kuskure 109), saboda dalilai uku dabam: manajan sabis yana
komawa ga `SimpleRegPopulator`, wanda ke karanta `jpos.cfg` da aka jera kuma ba ya taɓa karanta `jpos.xml`; mai lodawa
na XML yana neman `jpos.xml` a babban fayil na aiki kawai, ba a cikin jar ba; kuma Xerces 2.12.2 a `lib/` yana ƙin
halayyar JAXP `accessExternalDTD` da mai lodawa ke saitawa. `JposConfig` yana saita `jpos.config.regPopulatorClass`
(`JavaxRegPopulator`), `jpos.config.populatorFileURL` (`jar:file:…!/jpos.xml` na jar ɗin kansa) da
`javax.xml.parsers.SAXParserFactory` (na JDK) a matsayin halayen tsarin — ba a taɓa wanda aka riga aka saita ba, don
haka `-Djpos.config.populatorFileURL=file:…` har yanzu yana iya zaɓar wani `jpos.xml` — kuma ba ya saita kome lokacin da
`jpos.properties` yake kan hanyar aji ko babu mai lodawa na 4.x. `javaPOS114.jar` na Zebra SDK irin wannan lamari ne kuma
yana karya rajistar gaba ɗaya, shi ya sa `run.bat` ke ajiye shi waje da hanyar aji — duba
[Abubuwan asali na Zebra](#abubuwan-asali-na-zebra). `JposConfigTest` yana bincika daga farko zuwa ƙarshe cewa rajistar
tana riƙe da sikanoni na fayil ɗin bayan haka.

**Ana samar da mai zaɓin na'ura daga wannan rajistar**, ba a ajiye shi a matsayin jeri a cikin lamba ba:
`ScannerService.logicalNames()` yana karanta shigarwar da aka loda a kan zaren `gaia-scanner` kuma yana ajiye waɗanda
rukunin na'urarsu `Scanner` ne — shigarwar ma'auni biyu a `jpos.xml` suna zama a waje — a `pickerOrder`:
`ZebraAllScanners` da farko idan fayil ɗin ya ayyana shi, sauran bisa haruffa (rajistar ba ta ajiye tsarin fayil ba).
Fayil ɗin da ba shi da shi ba a ba shi ba; idan babu abin da aka loda ana bayar da na asali kaɗai, don haka mai zaɓin
ba ya taɓa zama babu kome. `MainView` yana nuna na asali nan take kuma yana cika jerin daga zaren bango, yana ajiye zaɓin
da aka adana. Don haka sikana da aka ƙara zuwa `jpos.xml` — ko dukan fayil da aka ambata da
`-Djpos.config.populatorFileURL` — yana bayyana a mai zaɓin ba tare da canjin lamba ba, kuma `JposConfigTest` yana bincika
mai zaɓin a kan karatu mai zaman kansa na fayil ɗin da aka haɗa. Sabis ɗin da waɗannan shigarwa suka ambata azuzuwan Zebra
ne (`com.zebra.jpos.service.scanner.*Factory`), waɗanda har yanzu dole ne su kasance a kan hanyar aji — daga Scanner SDK
ta `run.bat`, ko jar na sabis na Zebra da aka saka a `lib\`.

Ana warware bayanan dubawa a matsayin **ISO-8859-1** don kowane baiti ya yi taswira 1:1 kuma duka kan AIM Code ID da
kowane `0x1D` su tsira; ana rubuta rahotanni a matsayin UTF-8.

Babu abin da ke cikin sabis ɗin da ke tsara rubutu don mai amfani. Abubuwa suna fita a matsayin madawwaman
`ScannerListener.Event` tare da ƙimomin da ke canzawa, kuma `MainView` yana bayar da kalmomi daga kundin — in ba haka ba
layin matsayi zai kasance da Turanci a manhajar da aka fassara ta hanyoyi 35. Layukan log su ne banda kuma suna zama da
Turanci.

### Ƙofar dandali

`ScannerService.isSupportedPlatform()` gwajin `os.name` ɗaya ne, kuma `MainView` yana tambayarsa kafin gina kowane sarrafin
sikana: a macOS da Linux babu maɓallin kunna, babu mai zaɓin na'ura, babu menu na Sikana (don haka babu <kbd>⌘K</kbd>),
kuma babu lakabin yanayi. Ba a ɓoye su ba — ba a taɓa gina su ba.

Zebra tana aika sabis ɗinta na JavaPOS don Windows kaɗai, kuma babu abin da ke kasawa da wuri a wasu wurare: jar suna
warwarewa kuma sarrafin yana ginuwa, kuma `open()` ne a ƙarshe ke kasawa saboda rashin layin asali, bayan ɗaƙiƙu da yawa
da akwatin magana ɗaya.

Dokar game da *sabis* ce, ba game da JavaPOS ba. Mai siyarwa da ke aika sabis na dandali da yawa zai yi aiki a nan, kuma
faɗaɗa dokar canji ne a `supportsJavaPos(String)` kaɗai — wanda yake na kunshi kuma an gwada shi a kan igiyoyin
`os.name` don a rufe amsoshi biyun a kowane inji da ke gudanar da gini.

### Tsarin lambobin ƙira

`Symbologies` yana taswirar duk nau'ukan bayanan dubawa **78** na `SCAN_SDT_*` na JavaPOS zuwa sunayen nuni, da zuwa
`DataCarrierType` na cibiya inda ƙamus biyun suka daidaita. JavaPOS ya fi zurfi a cikin biyun — yana raba UPC-A da EAN-13,
da Data Matrix na yau da kullum da GS1 DataMatrix, inda AIM ke tara dukan iyali ƙarƙashin harafin lamba ɗaya.

Nau'in da ke waje da teburin har yanzu yana samun sunan da za a iya amfani da shi, wanda aka samu ta hanyar tunani a kan
`ScannerConst`, domin sabis na masu siyarwa suna ba da rahoton ƙimomin da ke waje da jerin ƙayyadaddun bayanai da gaske.
Gwaji yana zagaya `ScannerConst` kuma yana kasawa idan kowane nau'in da aka ayyana ba shi da suna — wanda kuma yake kama
madawwaman da JavaPOS na gaba zai ƙara.

An bar wasu taswirar masu ɗauka da gangan a matsayin `UNKNOWN` maimakon hasashe: straight 2 of 5 (AIM yana raba `]R`/`]S`
bisa ƙidayar sandunan farawa/tsayawa, wanda JavaPOS ba ya ba da rahoto), lambobin gidan waya masu yanayi huɗu, da TLC-39.

---

## Daidaita harshe

Lakabi suna zaune a fayil ɗin halaye ɗaya ga kowane harshe ƙarƙashin `src/main/resources/i18n/ui_<CODE>.properties`,
suna amfani da lambobi iri ɗaya da kundin kurakurai na cibiya (`EN`, `FR`, `PCM`, …). Duka 35 suna nan kuma duka 35
suna ɗauke da rukunin maɓallai iri ɗaya. Ana karanta fayilolin a matsayin **UTF-8**, ba ISO-8859-1 da `Properties` ke
ɗauka ba, don haka ana adana fassarori a hanyar da za a iya karantawa maimakon tserewar `\uXXXX`.

**Komawa baya ga kowane maɓalli ne, ba ga kowane fayil ba** — wannan ne ke sa harshen da aka fassara wani ɓangare ya yi
amfani daga lokacin da maɓallinsa na farko ya sauka. Turanci ne yarjejeniyar: dole kowane maɓalli ya kasance a can.

| Layi | Maɓallai | Ɗaukar hoto |
|---|---|---|
| Ƙamus na rahoto da aka raba | 31 | duk harsuna 35 |
| Firam ɗin tebur — menus, maɓallai, mai ginawa, zaɓuɓɓuka, layin matsayi, tsarin rahoto, matsayin sikana, jikin akwatin magana | 114 | duk harsuna 35 |

Shida daga cikin kalmomin da aka raba (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) ba a
amfani da su a rahoton wannan manhaja ba.
An ajiye su da gangan; kan fayil ɗin ya faɗi haka.

### Hujjar `--language`

`GaiaDesktopApp.languageOverride()` yana karanta `--language=…` ta `getParameters().getNamed()` na JavaFX kansa, yana
warware shi da `Languages.fromArgument` — lambar kundi, sunan enum, ko alamar BCP 47, ba tare da kula da manyan ko ƙananan
haruffa ba — kuma yana miƙa shi ga `MainView`.

**Sokewar don gudanarwar ce, ba don fayil ba.** `MainView` yana ajiye harshen da ya loda a `savedLanguage` kuma yana
rubuta *wannan* baya, har sai `languageChosenInSession` ya ce mai amfani ya zaɓi harshe da kansa; sa'an nan zaɓinsa yana
dawwama kamar yadda aka saba. Wannan rabuwar ita ce dukan ƙirar: bayanin martaba na kiosk ko gajeriyar hanya ta demo na
iya buɗewa da Sifaniyanci ba tare da sake rubuta abin da mai injin zai buɗe gobe ba, yayin da zaɓi na gaske da aka yi a
fanel ɗin zaɓuɓɓuka har yanzu yana manne.

Ƙimar da ba ta ambaci wani kundi ba ana rubuta ta a log tare da lambobin da aka karɓa kuma ana watsi da ita. Ƙin farawa
saboda kuskuren bugu a gajeriyar hanya zai zama gazawa mafi muni, kuma `Languages.fromArgument` yana mayar da `null`
maimakon jefawa saboda dalili ɗaya.

### Ƙara harshe

Ba a buƙatar canjin lamba.

1. Gyara `ui_<CODE>.properties` — kowane harshe yana da fayil tuni.
2. Fassara ƙimomin. Bar masu riƙe wuri `{0}` / `{1}` a wurinsu.
3. Gina. Gwaje-gwaje biyu suna tilasta yarjejeniyar: ɗaya yana kasawa idan lambar ta nemi maɓallin da Turanci bai
   ayyana ba, ɗayan idan rukunin maɓallai na kowane kundi ya kauce daga Turanci.

Gwaji na farko yana bincika **tushen lamba** don maɓallai na zahiri maimakon yin tunani a kan azuzuwa, domin maɓallai
suna wanzuwa ne kawai a matsayin igiyoyin zahiri a wuraren kira. Shi ya sa dole a gina sabon saƙo da maɓallin a rubuce
kai tsaye — `strings.format("st.scannerError", …)` — maimakon a nema ta hanyar mai canji, in ba haka ba gwajin ba zai iya
ganinsa ba.

### Abin da ba a fassara da gangan ba

- gajerun sunayen filaye da aka saita a monospace kusa da ƙimomin ɗanye — `format`, `position`, `length`, `raw` da
  `part` na katin abu, `AI`, `sep`, `pos` da `len` na tef, da sel huɗu da ke nuna wurin kuskure (`AI`, `position`,
  `level`, `stage`), waɗanda ƙimominsu sunayen enum ne da ba a fassara ba ko ta yaya;
- sunayen samfurin shigarwa, waɗanda ke ambaton gine-ginen GS1, da sunayen lambobin ƙira, waɗanda sunaye ne na musamman;
- sakin layi na gyara na `GE-C084` — rubutun ma'auni inda rabin fassara ya fi Turanci muni;
- shingen ƙarshe na `ElementBuilder` don AI da ba a sani ba, wanda ba za a iya kaiwa daga UI ba: edita yana duba AI da
  ba a sani ba da kansa kuma yana ba da rahotonsu daga kundin;
- layukan log.

Gibi ɗaya ya rage: `ScanSource.label()` yana bayar da `manual` / `scanner` / `file` / `built` ga layin tarihi da kan
rahoto da Turanci. Rufe shi yana buƙatar fiye da maɓalli — `HistoryCell` ba shi da `UiStrings`, kuma ana shigar da
masana'antar sel na jerin sau ɗaya, don haka dole a sabunta sel lokacin canza harshe ma.

---

## Tsarin bayanan da aka adana

Fayiloli biyu ƙarƙashin `~/.gaia-desktop/`, an raba su domin suna canzawa a lokuta dabam-dabam — log na kamawa da ya
lalace ko aka gyara da hannu bai kamata ya jawo wa mai amfani asarar saitunansa ba. Soke babban fayil ɗin da
`-Dgaia.desktop.home=…`, wanda shi ne yadda gwaje-gwaje ke nisanta daga babban fayil na gida na gaske.

**`options.properties`** — ana rubuta shi lokacin da zaɓi ya canza, da lokacin fita.

| Maɓalli | Ma'ana |
|---|---|
| `parse.mode`, `parse.language` | Zurfin bututun aiki da harshen saƙo |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Nuna kwanan wata |
| `check.skipRequires`, `check.skipExcludes` | Tserewa daga binciken tsari |
| `ui.theme` | `LIGHT` ko `DARK` |
| `ui.optionsPane` | Fanel ɗin a buɗe? |
| `ui.scannerDevice` | Sunan hankali na JavaPOS na ƙarshe |
| `ui.mode` | `PARSE`, `BUILD` ko `COMBINE` |
| `update.check` | Ko duba sabuntawa na yau da kullum yana gudana (na asali `true`) |
| `update.lastCheck` | Lokacin da ya gudana na ƙarshe, ISO-8601; rashinsa ko rashin karantuwa yana nufin lokacin bincike ya yi |
| `update.skipVersion` | Sakin da mai amfani ya zaɓi *Tsallake wannan sigar* gare shi; ba a sake bayar da shi kai tsaye |

Fayil da ya ɓace ko ya lalace ba ya taɓa zama mai kisa: kowace ƙima tana komawa baya da kanta, don haka layi mara kyau
ɗaya yana jawo asarar wannan layin kawai ba wani abu ba. A ƙaddamarwa ta farko, babu fayil ko kaɗan, `SystemDefaults` ne
ke cika zaɓuɓɓuka daga saitunan yanki na injin maimakon na asali na GAIA — gajeren tsarin kwanan wata na wurin yana bayar
da tsarin sassa, mai rabawa, da ko an gajarta wata da shekara.

Kada ka taɓa ba `Properties.setProperty` null: yana jefawa, kuma `persistOptions()` yana gudana daga masu sarrafa abubuwan
UI inda keɓantawa ke tserewa zuwa zaren FX. Wannan faɗuwa ce ta gaske da wannan manhaja ta riga ta samu.

**`captures.tsv`** — ana rubuta shi lokacin da tarihi ya canza, da lokacin fita. Sabo da farko, iyaka **500** shigarwa.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Ana adana abin da ba za a iya sake lissafawa kaɗai. **Ba a** jera `ParseResult` ba — ana sake samun sa ta sake fassara
shigarwar lokacin lodawa, don haka tarihin da aka maido yana nuna zaɓuɓɓukan da ke aiki *yanzu*. Shigarwa yawanci tana
ɗauke da FNC1 da duk abin da sikana ke fitarwa, don haka ana tserar da filin shigarwa (`\\`, `\t`, `\n`, `\r`, da `\xNN`
ga duk sauran ƙasa da `0x20`) kuma ana karanta da rubuta fayil ɗin a matsayin ISO-8859-1, baiti bayan baiti.

Fayilolin da aka rubuta kafin a fara rubuta kwanan wata suna riƙe da lokacin agogo kawai; har yanzu suna lodawa, da kwanan
watan yau, domin ba a taɓa rubuta ainihin kwanan watan ba.

---

## Gwaji

```bash
(cd gaia-desktop && mvn test)
```

Gwaje-gwaje 198, da gangan **ba tare da kayan aikin JavaFX ba**, wanda ke sa su sauri kuma marasa allo. Suna rufe mai zana
rahoto, dabarar gini da neman AI, canjin `{GS}`, na asali da aka samo daga wuri, ma'ajiyoyi biyun (har da tafiya-da-dawowa
mai ɗauke da `0x1D` da fayilolin da aka lalata da gangan), teburin lambobin ƙira, dokar dandalin sikana, kututturen siga,
da kundin.

Uku sun cancanci sani domin suna kare yarjejeniyoyi maimakon ayyuka:

| Gwaji | Yana kasawa lokacin |
|---|---|
| `CatalogueCoverageTest` | Lambar ta nemi maɓallin da `ui_EN.properties` bai ayyana ba, ko kowane daga cikin fayiloli 35 ya kauce daga rukunin maɓallai na Turanci |
| `SymbologiesTest` | Kowane madawwamin `SCAN_SDT_*` da JavaPOS ya ayyana ba shi da sunan nuni — har da wanda haɓaka JavaPOS ya ƙara |
| `ScannerServiceTest` | Dokar dandali ta canza amsarta ga Windows ko ga kowane abu dabam |

`UiStrings` da gangan yana da sauƙin kai a lokacin gudana — maɓallin da ba a sani ba yana nunawa a matsayin maɓallin kansa
maimakon jefawa a tsakiyar tsari — wannan daidai ne dalilin da ya sa dole gwajin kundi ya wanzu.

**Ba a rufe ba:** duk abin da ke buƙatar kayan aiki mai rai — halin mayar da hankali, maɓallin yanayi, gyaran tebur,
canjin harshe, da faɗuwa biyun da aka bayyana ƙarƙashin [Zaren aiki](#zaren-aiki). An tabbatar da waɗannan ta hanyar
tuƙa taga ta gaske, amma koma baya a cikinsu ba zai sa gini ya kasa ba. Ƙara TestFX ko Monocle zai rufe wannan gibin.

---

## Warware matsalolin gini

| Alama | Sanadi da gyara |
|---|---|
| `package tools.pantheum.gaia does not exist` | Ba a shigar da cibiya ba. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | Dole `Main-Class` na jar ya zama `Launcher`, ba `GaiaDesktopApp` ba |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* a farawa | Ana tsammani kuma ba shi da lahani — JavaFX yana lura cewa yana kan hanyar aji. Wannan daidai ne dalilin da ya sa `Launcher` yake wanzuwa |
| Jar ba zai fara ba, `NoClassDefFoundError` | Jar na asali yana ɗauke da azuzuwan wannan module kawai; dole `dist/lib/` ya kasance a gefensa. Yi amfani da `-Pfat` don fayil ɗaya mai cin gashin kansa |
| `Artifact has not been packaged yet … MDEP-187` a Eclipse | Warware wurin aiki na m2e. Duba bayanin ƙarƙashin [Tattara kunshi](#tattara-kunshi) |
| Fayilolin `… 2.properties` kwafi suna bayyana a gini | Aikin yana cikin babban fayil da iCloud ke daidaitawa kuma daidaitawar ta samar da kwafin rikici a tsakiyar gini. Kullum `mvn clean package`; mafi kyau, cire `target/` daga daidaitawa |
| Abun menu yana nuna maɓalli kamar `act.hri` | Wannan maɓallin ya ɓace daga `ui_EN.properties`. Ya kamata gini ya kama shi — duba [Gwaji](#gwaji) |

---

## Iyakokin da aka sani da aikin da ya rage

- **Babu gwaje-gwajen UI marasa allo.** Duba [Gwaji](#gwaji).
- **Kayan da suka keɓanta ga dandali.** Tattarawa biyun suna haɗa kayan asali na JavaFX na dandali ɗaya; gina ɗaya ga
  kowane manufa da `-Dfx.platform` (kowane inji zai iya gina su duka). Babu ɗayansu da ke haɗa lokacin gudana na Java —
  wannan yana buƙatar `jpackage`, wanda yake ginawa ne kawai don OS da yake gudana a kai.
- **Shigarwar sikana Windows ne kawai.** Duba [Ƙofar dandali](#ƙofar-dandali).
- **Ba a daidaita harshen `ScanSource.label()` ba.** Duba [Abin da ba a fassara da gangan
  ba](#abin-da-ba-a-fassara-da-gangan-ba).
- **`UiState.mode` `String` ne** (`"PARSE"` / `"BUILD"`) yayin da `Theme` a gefensa enum ne, don haka na zahirin yana
  bayyana a wurare da dama. Enum zai cire su.
- **Manufar Digital Link yanki ne da kan hanya kawai.** `BuilderDigitalLinkConfig` kuma yana goyan bayan ƙarin sigogin
  tambaya da guntu (fragment); edita ba ya bayyana su.
- **Ba a yi wa nau'i-nau'in AI masu hana juna (`GE-S006`) alama a edita ba** kamar yadda ake yi wa AI da ba a sani ba da
  kwafi — gini kawai yana kasawa.
- **Kundin kurakurai na cibiya yana ninka alamomin ambato** (`value ''09506000134350''`), wanda ke bayyana kamar yadda yake
  a gazawar gini da rahoton. Yana kama da tserewar ambato ta `MessageFormat` da ba a warware ba; gyaran yana cikin cibiya.
