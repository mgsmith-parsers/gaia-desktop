# GAIA Desktop — Mwongozo wa msanidi

Jinsi moduli inavyojengwa, inavyopangwa na inavyojaribiwa. Kwa kile programu inachofanya ukiitazama kutoka nje, angalia
[Mwongozo wa mtumiaji](GaiaDesktop-User-Swahili.md). Hati asilia:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Yaliyomo

1. [Moduli kwa muhtasari](#moduli-kwa-muhtasari)
2. [Kujenga na kuendesha](#kujenga-na-kuendesha)
   - [Ufungashaji](#ufungashaji)
3. [Usanifu](#usanifu)
   - [Faneli ya ingizo](#faneli-ya-ingizo)
   - [Nyuzi](#nyuzi)
   - [Kujenga upya dhidi ya kuweka lebo upya](#kujenga-upya-dhidi-ya-kuweka-lebo-upya)
4. [Tokeni ya {GS}](#tokeni-ya-gs)
5. [Muunganisho wa skana](#muunganisho-wa-skana)
   - [Lango la jukwaa](#lango-la-jukwaa)
   - [Simbolojia](#simbolojia)
6. [Ujanibishaji](#ujanibishaji)
   - [Hoja ya `--language`](#hoja-ya---language)
   - [Kuongeza lugha](#kuongeza-lugha)
   - [Kisichotafsiriwa kwa makusudi](#kisichotafsiriwa-kwa-makusudi)
7. [Miundo ya hali iliyohifadhiwa](#miundo-ya-hali-iliyohifadhiwa)
8. [Majaribio](#majaribio)
9. [Kutatua matatizo ya ujenzi](#kutatua-matatizo-ya-ujenzi)
10. [Mipaka inayojulikana na kazi iliyobaki](#mipaka-inayojulikana-na-kazi-iliyobaki)

---

## Moduli kwa muhtasari

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Ni mteja wa maktaba ya msingi na si zaidi. Haina **maarifa yoyote ya GS1** ndani yake: kila ufafanuzi wa AI, kanuni ya
uthibitishaji, ujumbe wa hitilafu na maelezo yaliyojanibishwa yanatoka kwa [`GaiaParser`](../../../../gaia/docs/GaiaParser.md)
na [`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). Hasa, orodha ya AI hainakiliwi kamwe ndani ya moduli hii —
kichaguzi cha mjenzi kinasoma sajili ya msingi yenyewe, kwa hivyo hakiwezi kutoa AI ambayo mjenzi angeikataa.

**Sehemu za kuingia:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (`Application` ya JavaFX) na
`tools.pantheum.gaia.desktop.Launcher` (`Main-Class` ya jar iliyofungashwa).

Moduli ni jani: hakuna kinachoitegemea, na yenyewe haitegemei moduli nyingine yoyote ya chini.

---

## Kujenga na kuendesha

- **Java 21** kuendesha, na JDK 21 au mpya zaidi kujenga — moduli inakusanywa kwa `release=21`. JavaFX 21 ndiyo mstari
  wa LTS unaolingana. Jar ya msingi bado ni bytecode ya Java 11 na inaendeshwa bila kubadilishwa kwenye 21.
- **Maven** kwenye `PATH`. Hakuna wrapper katika hazina hii.
- **Sakinisha msingi kwanza.** Moduli hii inapata `gaia:1.1.0` kutoka `~/.m2`; jar ya msingi iliyopitwa na wakati au
  isiyokuwepo ndiyo sababu ya kawaida ya `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` ndiyo hoja pekee ya mstari wa amri, na njia pekee ya kuathiri uanzishaji isipokuwa
`-Dgaia.desktop.home=…`, inayoelekeza wasifu mzima mahali pengine.

`mvn javafx:run` ndiyo njia ya uendelezaji: programu-jalizi inaweka JavaFX kwenye **njia ya moduli**, kwa hivyo zana
zinaanza kama moduli yenye jina na `GaiaDesktopApp` inaweza kuzinduliwa moja kwa moja. Jar iliyofungashwa haiwezi
kufanya hivyo — kila kitu kiko kwenye njia ya madarasa — ndiyo sababu `Main-Class` yake ni shim ya `Launcher`. Darasa
dogo la `Application` kama `Main-Class` katika mpangilio huo linashindwa kwa *"JavaFX runtime components are missing"*.

Tumia `clean` kila mara — mara moja, kabla ya kujenga kundi la majukwaa, kwa sababu inaondoa yote kutoka `target/dist/`.
Angalia [Kutatua matatizo ya ujenzi](#kutatua-matatizo-ya-ujenzi) kwa sababu yake.

### Ufungashaji

Kila kinachoweza kusafirishwa hukusanywa chini ya `target/dist/gaia-desktop-<version>-<platform>/` na hakuna kingine,
kwa hivyo saraka inaweza kufungwa zip au kunakiliwa kama seti moja bila kuchagua vitu kutoka `target/` kando ya
`classes/`, `maven-status/` na faili nyingine za muda za ujenzi. `${dist.dir}` inaipa jina mara moja; kila
programu-jalizi inayoandika kitu inaielekea.

#### Ujenzi mmoja kwa kila jukwaa

Jar za JavaFX hubeba msimbo asilia, kwa hivyo kila usambazaji ni wa jukwaa moja, linalopewa jina na sifa `fx.platform`
kama kiainishi cha OpenJFX: `mac-aarch64`, `mac`, `win`, `linux` au `linux-aarch64`. Wasifu tano wa `fx-*` unaowashwa
kulingana na OS huiweka kwenye jukwaa la mashine inayojenga, na hilo ndilo tu `mvn package` au `mvn javafx:run` ya
kawaida inahitaji; `-Dfx.platform=…` inazishinda ili kujenga jukwaa lingine lolote, kutoka mashine yoyote — jar za
JavaFX ni vipakuliwa tu. `${dist.dir}` ni `target/dist/${fx.platform}`, kwa hivyo ujenzi wa jukwaa moja kamwe
haufuti ujenzi wa jingine.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Maelezo mawili ya pom ndiyo yanayofanya hili lifanye kazi, na yote mawili yanaonekana kana kwamba yanaweza kuondolewa:

- **Kila moduli ya JavaFX ni utegemezi wa moja kwa moja** — base, graphics, controls, media na web — kila moja ikiwa na
  `<classifier>${fx.platform}</classifier>` na utengaji wa `org.openjfx:*`. Pom za OpenJFX huchagua jar zao za
  kupitishwa kulingana na OS ya *mashine inayojenga*, kwa hivyo kuacha graphics au base zifike kwa kupitishwa huingiza
  maktaba asilia za Mac katika ujenzi wa Windows kando ya zile za Windows.
- **Sifa ni `fx.platform`, si `javafx.platform`.** Ikiwekwa kutoka nje, sifa ya OpenJFX yenyewe hufafanuliwa upya na
  pom yake mzazi kwa kujitumia; Maven hukataa pom hiyo kuwa batili na kwa kimya huacha jar za jukwaa, na kubaki na
  ujenzi usio na JavaFX hata kidogo.

Mipangilio yote miwili huandika njia moja, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`, na mmoja
tu kati yao huendeshwa katika kila ujenzi. `${jar.phase}`, `assembly.skipAssembly` na `mdep.skip` ndizo zinazozuia yote
mawili yasiidai.

| | Chaguo-msingi | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — madarasa na rasilimali za moduli hii tu | ~50 MB — tegemezi zimefunguliwa ndani yake |
| `dist/gaia-desktop-<version>-<platform>/lib/` | jar 17, zimetajwa moja moja kwenye `Class-Path` ya manifest | — |
| Kinachosafirishwa | jar **na** `lib/` kando yake | faili moja |
| Pia ndani ya `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) au `run.sh` (nyingine) | vivyo hivyo |
| Na | `readme.html` na `docs/` (kila jukwaa) | vivyo hivyo |

Kila usambazaji pia hubeba miongozo: `docs/` inanakiliwa pamoja na mpangilio wake, ili viungo kati ya miongozo na
tafsiri viendelee kufanya kazi, bila zana za tafsiri (`check-i18n.py`, `i18n-manifest.json`); na
`readme.html` kwenye mzizi, faharasa inayounganisha kwa kila mmoja, kutoka `src/main/dist/readme.html` ambapo
`@VERSION@` hubadilishwa kuwa toleo la pom. Zote mbili huandikwa na hatua ya antrun ya `pick-launch-script`, inayofuta
`docs/` kwanza ili mwongozo ulioondolewa usibaki. Viungo vya readme vimeandikwa kwa mkono — mwongozo uliobadilishwa
jina au tafsiri iliyoongezwa chini ya `docs/` inahitaji mstari wake huko pia.

`Class-Path` ya manifest na `lib/` ni nusu mbili za kitu kimoja: jar inataja `lib/<artifact>-<version>.jar` kwa kila
utegemezi wa wakati wa kuendesha, na `copy-dependencies` hujaza `lib/` kwa majina hayo hasa. Hicho ndicho kinachofanya
`java -jar` ifanye kazi.

#### Programu ya macOS na zip ya usambazaji

Utekelezaji mmoja wa antrun, `mac-app-and-zip`, unamaliza awamu ya `package`. Kwa sababu hiyo programu-jalizi ya antrun
imetangazwa **baada ya** programu-jalizi za jar na assembly — Maven huendesha utekelezaji wa awamu kwa mpangilio wa
kutangazwa kwa programu-jalizi — kwa hivyo jar ipo, katika ufungashaji wowote, inapoendeshwa. (Utekelezaji wake mwingine,
`pick-launch-script`, uko katika `prepare-package` na hauathiriwi.)

Kwa `mac` na `mac-aarch64` kwanza inajenga `GAIA Desktop.app` katika saraka ya usambazaji: `Info.plist` (pamoja na
`@VERSION@` kubadilishwa kuwa toleo la pom), kizinduzi `Contents/MacOS/gaia-desktop` (kilichofanywa kitekelezeke) na
`gaia-desktop.icns`, vyote kutoka `src/main/macos/`, kisha **inahamisha** `gaia-desktop.jar` na `lib/` hadi
`Contents/Java`, ili kifurushi kisimame chenyewe. `run.sh` hutazama huko wakati hakuna jar kando yake. Kizinduzi hutafuta
Java kwa toleo (`java_home -v 21+`, kisha openjdk ya Homebrew — uzinduzi kutoka Finder hauoni `JAVA_HOME` wala
`PATH`), huonyesha kidirisha kupitia `osascript` kisipopata, hufanya `cd` hadi `~/.gaia-desktop` ili `logs/` ya
uhusiano ya logback iandikike, na hupitisha `-Xdock:name`/`-Xdock:icon`. macOS huhusisha `java` inayoendeshwa na
kifurushi (bundle id na ikoni ya Dock ni za programu); upau wa menyu unaonyesha GAIA Desktop.

Kisha, kwa kila jukwaa, inafunga saraka kwa zip hadi `target/dist/gaia-desktop-<version>-<platform>.zip` chini ya
folda yenye jina hilo hilo, kwa `<zip>` ya Ant: `run.sh` na `Contents/MacOS/**` kama seti ya faili tofauti yenye
`filemode="755"`, la sivyo hazingeweza kuendeshwa baada ya kufunguliwa. Zip **haiambatishwi**: ni kwa matoleo ya GitHub,
na majukwaa manne yanatoka kwa ujenzi minne, ambayo Maven Central — upakiaji mmoja usiobadilika kwa kila toleo — kamwe
haingeweza kuipokea kama toleo moja.

`project.build.outputTimestamp` huwekwa kwa wakati wa ujenzi. La sivyo, vihifadhi katika maven-jar-plugin 3.5 na
maven-assembly-plugin 3.8 huipa kila ingizo tarehe 1 Februari 1980.

Ikoni ya programu inachorwa na `tools/icon/GaiaIcon.java` (si sehemu ya ujenzi; kichwa chake kina hatua za
`sips`/`iconutil`). Matokeo yake yamehifadhiwa kwenye hazina: `src/main/macos/gaia-desktop.icns` kwa kifurushi, na
`src/main/resources/icons/gaia-desktop-{16..256}.png`, ambazo `GaiaDesktopApp` huziweka kama ikoni ya dirisha (Windows
na Linux; macOS hupuuza ikoni za dirisha).

#### Hati za kuanzisha

`run.sh` na `run.bat` zinatoka `src/main/scripts/`, zinanakiliwa hadi `dist/gaia-desktop-<version>-<platform>/` bila
kuchujwa — `run.sh` imejaa upanuzi wa shell wa `${...}` ambao uchujaji wa Maven ungeweza kuuandika upya utakavyo. Kisha
hatua ya antrun inabakiza ile tu ambayo jukwaa linaendesha — `run.bat` kwa `win`, `run.sh` kwa mengine yote — na
kuweka biti ya utekelezaji kwenye `run.sh`, kwa sababu `copy-resources` haiibebi. Inaamua kutoka kwa `${fx.platform}`
iliyotatuliwa badala ya kupitia wasifu, kwa sababu uwashaji wa wasifu hauwezi kuona thamani iliyowekwa na wasifu
mwingine: la sivyo ujenzi wa Linux uliofanywa kwenye Windows ungepata hati ya mwenyeji.

**Hazitegemei manifest**. Kila moja hujenga njia ya madarasa yenyewe, `gaia-desktop.jar` ikifuatwa na kila `.jar`
katika `lib/`, zimetajwa moja moja badala ya kuachiwa kadi pori ya `lib/*` ili njia yenye nafasi ibaki ingizo moja na
hesabu ya vilivyoongezwa iwe halisi. Kuzindua kwa `-cp` ndiyo sababu zinataja `tools.pantheum.gaia.desktop.Launcher`
waziwazi: `-cp` haisomi `Main-Class` ya manifest. Lakini **bado** inafuata `Class-Path` yake — jar iliyo kwenye `-cp`
huleta jar ambazo manifest yake inazitaja, zinazotafutwa mara baada yake — kwa hivyo kila jar ya `lib/` hupakiwa iwe
hati inaiorodhesha au la, na kuacha moja kwenye njia ya madarasa ya hati hakuiweki nje ya JVM. Hati bado
huziorodhesha zote, ili jar ambayo manifest yake imepitwa na wakati, imehaririwa kwa mkono au imepungukiwa ingizo moja
bado ianze. Hati moja inahudumia ufungashaji wote miwili: jar ya `-Pfat` haina `lib/` ya kuongeza na haihitaji, ndiyo
sababu pia `lib/` tupu ni dokezo badala ya hitilafu.

#### Maktaba asilia za Zebra

Ujenzi hausafirishi chochote cha Zebra — hakuna DLL, hakuna jar za huduma. Kwenye Windows zinatoka kwa Scanner SDK ya
Zebra iliyosakinishwa kwenye mashine: `run.bat` huongeza jar zake za huduma `Jpos*.jar` na `bin\` kwenye njia ya
madarasa na `bin\` kwenye `PATH`, ambapo `System.loadLibrary` hupata `CSJPOSScanner.dll` / `CSJPOSScanner64.dll`
(`java.library.path` kwa chaguo-msingi ni `PATH`). API ya JavaPOS na kipakiaji chake cha usanidi daima hutoka
`lib\javapos-*.jar`, kuwe na SDK au la.

**`run.bat` kamwe haiongezi jar ya JavaPOS ya SDK yenyewe**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`. Iliwahi
kufanya hivyo, ikiacha `lib\javapos-*.jar` ili kuepuka nakala mbili za API — lakini `Class-Path` ya manifest
iliyapakia vyovyote vile (angalia juu), kwa hivyo JVM ilipata zote mbili: madarasa ya kipakiaji ya 4.x kutoka `lib\`,
pamoja na `jpos/res/jpos.properties` na `jcl.xsd` za `javaPOS114.jar` kando yake. Faili ya sifa hufanya
[`JposConfig`](#muunganisho-wa-skana) ijiweke kando, na `jcl.xsd` hiyo inakubali matoleo ya JavaPOS hadi 1.10 tu, kwa
hivyo kila ingizo katika `jpos.xml` (1.12–1.14) lilishindwa uthibitishaji, sajili ilibaki tupu, na kila muunganisho
ulishindwa kwa hitilafu 109 kabla skana haijafikiwa kabisa. Kuwepo kwa jar hiyo kwenye njia ya madarasa kunatosha kwa
hilo, hata ikiwa nyuma ya jar za `lib\`. Jar za huduma za Zebra zinaendeshwa bila kubadilishwa kwenye API ya 1.14
kutoka `lib\` — kufungua, kudai na kuskani kumethibitishwa dhidi ya skana halisi kwa njia hiyo. DLL zilizowekwa kwa mkono
kando ya `run.bat` bado zinaheshimiwa: hati basi huweka `-Djava.library.path=<dist>;%PATH%`, ikiongeza `PATH` mwishoni
kwa sababu sifa hiyo inabadilisha kabisa njia ya utafutaji ya chaguo-msingi.

`mvn javafx:run` haifanyi lolote kati ya haya, kwa hivyo kudai skana kutoka uendeshaji wa uendelezaji kwenye Windows
kunahitaji jar za SDK kwenye njia ya madarasa na `bin\` yake kwenye `PATH` kwa njia nyingine; endesha kutoka `dist/`
kwa hilo.

Zaidi ya njia ya madarasa zinashughulikia kile ambacho `java -jar` haifanyi: kutatua saraka yao wenyewe ili zifanye kazi
kutoka saraka yoyote ya kazi, kupendelea `JAVA_HOME` kuliko `PATH`, na kukataa JVM ya kabla ya 11 kwa sentensi
inayotaja suluhisho badala ya `UnsupportedClassVersionError`. `GAIA_DESKTOP_OPTS` hupitisha chaguo za JVM. `run.bat`
hutumia `java.exe`, si `javaw.exe`, ili dirisha la amri libaki wazi — Windows ndilo jukwaa pekee ambapo skana
inaendeshwa, na hali yake na hitilafu za JavaPOS huenda huko.

`run.bat` pia huchagua Java, JavaFX na huduma za Zebra kutoka kwa kile inachopata — `jdk*\` iliyofungashwa,
`javafx-sdk*\lib\` kwenye njia ya moduli, Scanner SDK ya Zebra katika `ZEBRA_HOME` — ikiacha nakala za JavaFX za
`lib\` nje ya njia ya madarasa inapotumia za SDK, na hupitisha kichanganuzi cha SAX cha JDK, proksi na hifadhi ya imani
ya Windows, na `--enable-native-access` kwenye Java 17+. Sehemu ya
[What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) ya README ina jedwali. Hifadhi miisho ya
mistari ya CRLF ya faili: `cmd.exe` inaweza kukosa `call :label` katika faili ya batch yenye LF pekee. `run.sh` pia
hupitisha `--enable-native-access=ALL-UNNAMED` — JavaFX hupakia maktaba zake asilia kutoka `lib/`, na JavaFX ikiwa
kwenye njia ya madarasa `ALL-UNNAMED` ndiyo moduli inayoifunika — inarukwa, kama katika `run.bat`, tu wakati toleo la
Java haliwezi kusomwa.

Kila mpangilio hubeba maktaba asilia za JavaFX **za jukwaa moja**, `${fx.platform}`, kwa hivyo hakuna unaoweza
kuhamishwa kati ya OS au usanifu tofauti. Jenga mara moja kwa kila lengo — angalia
[Ujenzi mmoja kwa kila jukwaa](#ujenzi-mmoja-kwa-kila-jukwaa).

> **Eclipse.** Utekelezaji wa `copy-dependencies` unabeba agizo la `<?m2e ignore?>`. m2e hutatua mradi wa nafasi ya
> kazi ulio wazi hadi folda yake ya `target/classes` badala ya jar, na `copy-dependencies` inakataa kunakili kitu
> ambacho hakijafungashwa (MDEP-187), jambo ambalo lingefanya ujenzi wa kiotomatiki ushindwe kila mara mradi wa `gaia`
> ukiwa wazi kando ya huu. Matokeo yake ni kwamba ujenzi wa IDE pekee haujazi
> `target/dist/gaia-desktop-<version>-<platform>/lib/`; endesha `mvn package` kabla ya kutumia jar yenyewe.

---

## Usanifu

| Kifurushi | Kinachoshikilia |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (dirisha), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (mantiki ya kutengeneza bila JavaFX), `AiPair` (mstari mmoja wa kihariri) |
| `desktop.parse` | `ParseOptions` (`ParseConfig` inayoweza kutazamwa), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — kionyeshi cha ripoti, bila JavaFX na kinajaribiwa kwa vitengo bila zana |
| `desktop.i18n` | `UiStrings` — katalogi ya lebo |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### Faneli ya ingizo

Kila ingizo — lililoandikwa, kubandikwa, kufunguliwa kutoka faili, kuskaniwa, au kutengenezwa — hufika kwa kichanganuzi
kupitia mbinu moja, `MainView.capture(String, ScanSource, String)`.

Huo ndio uamuzi pekee wa muundo ambao programu nzima imejengwa juu yake. Kutengeneza hakuhitaji ripoti yake, historia
yake wala njia yake ya kuhamisha, kwa sababu baada ya faneli mfuatano uliotengenezwa hauwezi kutofautishwa na
uliyoskaniwa. Chochote kipya kinachozalisha ingizo kinapaswa kupita ndani yake badala ya kando yake.

### Nyuzi

| Uzi | Unachoendesha |
|---|---|
| Uzi wa programu ya JavaFX | Uchanganuzi wote, uonyeshaji na UI. Uchanganuzi huchukua chini ya milisekunde moja jedwali za AI zikishapashwa, kwa hivyo hauhitaji mfanyakazi |
| `gaia-scanner` | Kila mwito kwa skana — kufungua, kudai, kuwasha, kuzima, kuachilia, kufunga. Uzi mmoja wa muda mrefu unaomilikiwa na `ScannerService`; angalia [Muunganisho wa skana](#muunganisho-wa-skana) |
| Uzi wa matukio ya JavaPOS | Mirejesho ya `ScannerService`. `MainView.FxScannerListener` huvusha kila mmoja kwa `Platform.runLater` |
| Usuli | Kusubiri `connect()` / `disconnect()` ya skana (kudai kunaweza kuchukua hadi sekunde 5), kurejesha historia iliyohifadhiwa wakati wa kuanza — ambako pia hupasha kichanganuzi — na ukaguzi wa masasisho (`gaia-update-check`, ombi moja la GitHub, matokeo hurudishwa kwa `Platform.runLater`) |

**Kamwe usibadilishe uteuzi wa kidhibiti, au kuharibu kidhibiti, kutoka ndani ya tukio lake lenyewe la uteuzi.** Kuanguka
mara tatu tofauti katika programu hii kulitokana na hilo hasa: kuweka upya kichaguzi cha AI ndani ya `clearAndSelect` ya
dirisha ibukizi lenyewe (`IndexOutOfBoundsException` kutoka `ListViewBehavior`), na kujenga upya paneli inayomiliki
kisanduku cha lugha kutoka ndani ya kishughulikiaji cha kisanduku hicho chenyewe. Vyote viwili sasa vinaahirishwa kwa
`Platform.runLater`, na sababu zimeandikwa kwenye sehemu zote mbili za mwito.

### Kujenga upya dhidi ya kuweka lebo upya

Kubadilisha lugha **hujenga upya** fremu kutoka katalogi — menyu, upau wa zana, paneli ya machaguo — jambo ambalo ni salama
kwa sababu hakuna kati yao kinachomiliki hali: `OptionsPane` ni mwonekano wa pande mbili wa `ParseOptions`, kwa hivyo
thamani zinaishi katika modeli na paneli mpya huzichukua tena mara moja.

Vidhibiti vinavyoshikilia **data ya mtumiaji** ni sehemu, na badala yake huwekwa lebo upya mahali vilipo:
`BuilderPane.retext(UiStrings)` huhifadhi mistari ya AI, sehemu za Digital Link na uteuzi. Kuijenga upya kungetupa kazi
inayoendelea — kama ilivyowahi kutokea.

---

## Tokeni ya {GS}

`TextField` ya JavaFX hutupa kwa kimya kila herufi iliyo chini ya `0x20` (`TextInputControl.filterInput`). Si "kuionyesha
kwa ajabu" — inaiondoa. Kuandika, kubandika, `setText` na `insertText` vyote vinaathirika, kwa hivyo
`setText("01…" + GS + "10LOT")` hutoa `"01…10LOT"`: mfuatano wa vipengele ambao maana yake imebadilika kimya kimya,
kwa sababu AI ya urefu unaobadilika sasa inameza kinachoifuata.

`desktop.parse.GsToken` ndilo jibu lote. Kitenganishi husafiri kupitia UI kama maandishi halisi `{GS}` na huwa `0x1D`
tena njiani kuelekea kichanganuzi. Tokeni hii haikubuniwa hapa — ndivyo hasa `ASCIIRevealerUtils.printable` ya msingi
inavyoonyesha `0x1D`, kwa hivyo thamani iliyonakiliwa kutoka ripoti inabandikwa moja kwa moja kurudi kwenye upau wa
ingizo.

Kichujio cha `TextFormatter` kwenye sehemu ya ingizo hukamata kila njia ambayo maandishi yanaweza kufika — kuandika, ⌘V,
menyu ya muktadha, kuburuta na kuachilia, `setText` — ndiyo sababu uandikaji upya uko hapo badala ya katika kila
kishughulikiaji.

Njia ya skana kamwe haipiti kupitia `GsToken`: skani hufika kwa kichanganuzi kama baiti.

---

## Muunganisho wa skana

`ScannerService` inamiliki kidhibiti cha `Scanner` cha JavaPOS: kufungua → kudai → kuwasha, ikiwasha tena tukio la data
baada ya kila skani. Majina ya kimantiki ya vifaa yanatoka `src/main/resources/jpos.xml`.

**Kila mwito wa kifaa unaendeshwa kwenye uzi mmoja, `gaia-scanner`, unaoishi muda wote huduma inapoishi** — kamwe si
kwenye uzi wa FX, na kamwe si kwenye uzi unaoisha baada ya mwito. Huduma ya Zebra hufikia skana kupitia JNI juu ya
CoreScanner COM API, na COM hufunga kile inachoandaa kwa apartment ya uzi unaoita: fungua kwenye uzi utakaoisha baadaye
na skana inadaiwa lakini inakaa kimya; funga kwenye uzi mwingine na apartment isiyo sahihi ndiyo inayoondolewa uanzishaji.
Kufunga kwenye uzi wa FX ndiyo hali mbaya zaidi, kwa sababu JavaFX huita `OleInitialize` hapo na kusajili lengo la
kuachilia la kila dirisha dhidi yake — `CoUninitialize` moja juu yake huua kuburuta na kuachilia katika programu nzima.
Hii ni JavaFX kwenye Windows, si toleo moja maalum la JavaFX. `connect()` na `disconnect()` hukabidhi kazi kwa uzi huo
na kusubiri, kwa hivyo `MainView` huziita zote mbili kutoka uzi wa usuli wa muda mfupi.

Kanuni mbili huzuia kifaa kinachotenda vibaya kisikwamishe muunganisho. **Kubomoa kamwe hakusimami mapema:**
`closeQuietly` huzima, huachilia na hufunga kama hatua tofauti, huachilia tu wakati udai umeshikiliwa, na daima hujaribu
`close()` — baada ya udai kushindwa, `release()` hutupa "not claimed", na hatua zilipokuwa zikishiriki `try` moja hilo
liliruka kufunga na kuacha kifaa kikiwa wazi katika huduma ya Zebra. **Tukio la data huwashwa tena ndani ya `finally`:**
JavaPOS hulizima baada ya kila tukio, kwa hivyo usomaji au kishughulikiaji kinachotupa lazima bado kiwashe tena, la
sivyo skana hukaa kimya hadi muunganisho upya. `ScannerLifecycleTest` hujaribu zote mbili dhidi ya `jpos.Scanner`
iliyoandikiwa hati.

**`JposConfig` huelekeza JavaPOS kwa `jpos.xml` iliyofungashwa** kabla ya kufungua kwa mara ya kwanza. Kwa jar za
JavaPOS za Maven (`javapos-config-loader` 4.x) na bila `jpos/res/jpos.properties` kwenye njia ya madarasa, la sivyo
kila kufungua kunashindwa kwa *"Service does not exist in loaded JCL registry"* (hitilafu 109), kwa sababu tatu tofauti:
msimamizi wa huduma hurudi kwa `SimpleRegPopulator`, inayosoma `jpos.cfg` iliyosanifishwa na kamwe haisomi `jpos.xml`;
kipakiaji cha XML hutafuta `jpos.xml` katika saraka ya kazi pekee, si ndani ya jar; na Xerces 2.12.2 iliyo katika `lib/`
inakataa sifa ya JAXP `accessExternalDTD` ambayo kipakiaji huiweka. `JposConfig` huweka
`jpos.config.regPopulatorClass` (`JavaxRegPopulator`), `jpos.config.populatorFileURL` (`jar:file:…!/jpos.xml` ya jar
yenyewe) na `javax.xml.parsers.SAXParserFactory` (ya JDK) kama sifa za mfumo — kamwe si ile iliyokwisha kuwekwa, kwa
hivyo `-Djpos.config.populatorFileURL=file:…` bado inaweza kuchagua `jpos.xml` nyingine — na haiweki chochote wakati
`jpos.properties` iko kwenye njia ya madarasa au kipakiaji cha 4.x hakipo. `javaPOS114.jar` ya Zebra SDK ni hali kama
hiyo na huvunja sajili kabisa, ndiyo sababu `run.bat` huiweka nje ya njia ya madarasa — angalia
[Maktaba asilia za Zebra](#maktaba-asilia-za-zebra). `JposConfigTest` hukagua mwanzo hadi mwisho kwamba baadaye sajili
inashikilia skana za faili.

**Kichaguzi cha kifaa hutengenezwa kutoka sajili hiyo**, hakihifadhiwi kama orodha katika msimbo:
`ScannerService.logicalNames()` husoma maingizo yaliyopakiwa kwenye uzi wa `gaia-scanner` na kubakiza yale ambayo aina
ya kifaa ni `Scanner` — maingizo mawili ya mizani katika `jpos.xml` hubaki nje — katika `pickerOrder`:
`ZebraAllScanners` kwanza faili inapoifafanua, mengine kwa alfabeti (sajili haihifadhi mpangilio wa faili). Faili
isiyo nayo haipewi; kisipopakiwa chochote chaguo-msingi pekee hutolewa, kwa hivyo kichaguzi kamwe si tupu. `MainView`
huonyesha chaguo-msingi mara moja na kujaza orodha kutoka uzi wa usuli, ikihifadhi chaguo lililohifadhiwa. Kwa hivyo skana
iliyoongezwa kwenye `jpos.xml` — au faili nzima iliyotajwa kwa `-Djpos.config.populatorFileURL` — inaonekana katika
kichaguzi bila mabadiliko ya msimbo, na `JposConfigTest` hukagua kichaguzi dhidi ya usomaji huru wa faili
iliyofungashwa. Huduma ambazo maingizo hayo yanazitaja ni madarasa ya Zebra (`com.zebra.jpos.service.scanner.*Factory`),
ambayo bado lazima yawe kwenye njia ya madarasa — kutoka Scanner SDK kupitia `run.bat`, au jar za huduma za Zebra
zilizowekwa ndani ya `lib\`.

Data ya skani husimbuliwa kama **ISO-8859-1** ili kila baiti ipangwe 1:1 na kiambishi cha AIM Code ID pamoja na kila
`0x1D` vibaki salama; ripoti huandikwa kama UTF-8.

Hakuna chochote katika huduma kinachotunga maandishi kwa ajili ya mtumiaji. Matukio hutoka kama vipeo vya
`ScannerListener.Event` pamoja na thamani zinazobadilika, na `MainView` hutoa maneno kutoka katalogi — la sivyo mstari
wa hali ungekuwa kwa Kiingereza katika programu iliyotafsiriwa kwa njia 35. Mistari ya kumbukumbu ndiyo isiyofuata kanuni
na inabaki kwa Kiingereza.

### Lango la jukwaa

`ScannerService.isSupportedPlatform()` ni jaribio moja la `os.name`, na `MainView` huiuliza kabla ya kujenga kidhibiti
chochote cha skana: kwenye macOS na Linux hakuna swichi, hakuna kichaguzi cha kifaa, hakuna menyu ya Skana (na kwa hivyo
hakuna <kbd>⌘K</kbd>), na hakuna lebo ya hali. Havijafichwa — kamwe havijengwi.

Zebra husafirisha huduma zake za JavaPOS kwa Windows pekee, na kwingineko hakuna kinachoshindwa mapema: jar
hutatuliwa na kidhibiti hujengwa, na ni `open()` ndiyo hatimaye hushindwa kwa kukosa tabaka asilia, sekunde kadhaa na
kidirisha kimoja baadaye.

Kanuni hii inahusu *huduma*, si JavaPOS. Muuzaji anayesafirisha huduma ya majukwaa mengi angefanya kazi hapa, na
kupanua kanuni ni badiliko katika `supportsJavaPos(String)` pekee — ambayo ni package-private na imejaribiwa dhidi ya
mifuatano ya `os.name` ili majibu yote mawili yafunikwe kwenye mashine yoyote inayoendesha ujenzi.

### Simbolojia

`Symbologies` hupanga aina zote **78** za data ya skani za `SCAN_SDT_*` za JavaPOS kwa majina ya kuonyesha, na kwa
`DataCarrierType` ya msingi pale misamiati miwili inapolingana. JavaPOS ndiyo yenye undani zaidi kati ya hizo mbili —
inatenganisha UPC-A na EAN-13, na Data Matrix ya kawaida na GS1 DataMatrix, ilhali AIM huweka familia nzima chini ya
herufi moja ya msimbo.

Aina iliyo nje ya jedwali bado hupata jina linalotumika, linalopatikana kwa uakisi juu ya `ScannerConst`, kwa sababu
huduma za wauzaji kweli huripoti thamani zilizo nje ya orodha ya vipimo. Jaribio hupitia `ScannerConst` na kushindwa
ikiwa aina yoyote iliyotangazwa haina jina — jambo ambalo pia hukamata vipeo ambavyo JavaPOS ya baadaye itaongeza.

Baadhi ya mipangilio ya vibebaji imeachwa kwa makusudi kama `UNKNOWN` badala ya kukisiwa: straight 2 of 5 (AIM
hutenganisha `]R`/`]S` kwa idadi ya pau za kuanza/kusimama, jambo ambalo JavaPOS haliripoti), misimbo ya posta ya hali
nne, na TLC-39.

---

## Ujanibishaji

Lebo zinaishi katika faili moja ya sifa kwa kila lugha chini ya `src/main/resources/i18n/ui_<CODE>.properties`,
zikitumia misimbo ile ile ya katalogi za hitilafu za msingi (`EN`, `FR`, `PCM`, …). Zote 35 zipo na zote 35 zina seti
sawa kabisa ya funguo. Faili husomwa kama **UTF-8**, si ISO-8859-1 ambayo `Properties` hudhani, kwa hivyo tafsiri
huhifadhiwa kwa namna inayosomeka badala ya kutoroka kwa `\uXXXX`.

**Kurudi nyuma ni kwa kila funguo, si kwa kila faili** — ndicho kinachofanya lugha iliyotafsiriwa kwa sehemu iwe na
manufaa tangu funguo yake ya kwanza inapofika. Kiingereza ndio mkataba: kila funguo lazima iwepo hapo.

| Tabaka | Funguo | Ufikiaji |
|---|---|---|
| Msamiati wa ripoti unaoshirikiwa | 31 | lugha zote 35 |
| Fremu ya eneo-kazi — menyu, vitufe, mjenzi, machaguo, mstari wa hali, mpangilio wa ripoti, hali ya skana, maudhui ya vidirisha | 114 | lugha zote 35 |

Istilahi sita zinazoshirikiwa (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) hazitumiki na
ripoti ya programu hii.
Zinabakizwa kwa makusudi; kichwa cha faili kinasema hivyo.

### Hoja ya `--language`

`GaiaDesktopApp.languageOverride()` husoma `--language=…` kupitia `getParameters().getNamed()` ya JavaFX yenyewe,
huitatua kwa `Languages.fromArgument` — msimbo wa katalogi, jina la enum, au lebo ya BCP 47, bila kujali herufi kubwa au
ndogo — na kuikabidhi kwa `MainView`.

**Ubatilishaji ni kwa uendeshaji huo, si kwa faili.** `MainView` huhifadhi lugha iliyopakia katika `savedLanguage` na
kuandika *hiyo* tena, hadi `languageChosenInSession` iseme kwamba mtumiaji alichagua lugha mwenyewe; kisha chaguo lake
hudumu kama kawaida. Mgawanyiko huo ndio muundo wote: wasifu wa kioski au njia ya mkato ya demo inaweza kufungua kwa
Kihispania bila kuandika upya kile ambacho mwenye mashine atafungua nacho kesho, ilhali chaguo halisi lililofanywa katika
paneli ya machaguo bado hudumu.

Thamani isiyotaja katalogi yoyote huandikwa kwenye kumbukumbu pamoja na misimbo inayokubalika na kupuuzwa. Kukataa kuanza
kwa sababu ya kosa la kuandika katika njia ya mkato kungekuwa kushindwa kubaya zaidi, na `Languages.fromArgument`
hurudisha `null` badala ya kutupa kwa sababu hiyo hiyo.

### Kuongeza lugha

Hakuna badiliko la msimbo linalohitajika.

1. Hariri `ui_<CODE>.properties` — kila lugha tayari ina faili.
2. Tafsiri thamani. Acha vishika-nafasi `{0}` / `{1}` mahali pake.
3. Jenga. Majaribio mawili hutekeleza mkataba: moja hushindwa ikiwa msimbo unaomba funguo ambayo Kiingereza hakijaifafanua,
   jingine ikiwa seti ya funguo ya katalogi yoyote inapotoka kutoka Kiingereza.

Jaribio la kwanza huchanganua **msimbo chanzo** kutafuta funguo halisi badala ya kuakisi juu ya madarasa, kwa sababu
funguo zipo tu kama mifuatano halisi kwenye sehemu za mwito. Ndiyo sababu ujumbe mpya lazima ujengwe huku funguo
ikiandikwa moja kwa moja — `strings.format("st.scannerError", …)` — badala ya kutafutwa kupitia kigezo, la sivyo jaribio
haliwezi kuiona.

### Kisichotafsiriwa kwa makusudi

- majina mafupi ya sehemu yanayowekwa kwa herufi za upana sawa kando ya thamani ghafi — `format`, `position`, `length`,
  `raw` na `part` za kadi ya kipengele, `AI`, `sep`, `pos` na `len` za utepe, na seli nne zinazoonyesha mahali pa
  hitilafu (`AI`, `position`, `level`, `stage`), ambazo thamani zake ni majina ya enum yasiyotafsiriwa hata hivyo;
- majina ya sampuli za ingizo, yanayotaja miundo ya GS1, na majina ya simbolojia, ambayo ni nomino maalum;
- aya ya urekebishaji ya `GE-C084` — maandishi ya viwango ambapo tafsiri nusu ni mbaya kuliko Kiingereza;
- kizuizi cha mwisho cha `ElementBuilder` kwa AI isiyojulikana, kisichoweza kufikiwa kutoka UI: kihariri hukagua AI
  zisizojulikana chenyewe na kuziripoti kutoka katalogi;
- mistari ya kumbukumbu.

Pengo moja linabaki: `ScanSource.label()` hutoa `manual` / `scanner` / `file` / `built` kwa mstari wa historia na kichwa
cha ripoti kwa Kiingereza. Kulifunga kunahitaji zaidi ya funguo — `HistoryCell` haina `UiStrings`, na kiwanda cha seli
za orodha husakinishwa mara moja, kwa hivyo seli zingelazimika kuonyeshwa upya lugha inapobadilishwa pia.

---

## Miundo ya hali iliyohifadhiwa

Faili mbili chini ya `~/.gaia-desktop/`, zimetenganishwa kwa sababu hubadilika kwa kasi tofauti — kumbukumbu ya
unasaji iliyoharibika au iliyohaririwa kwa mkono haipaswi kumgharimu mtumiaji mipangilio yake. Batilisha saraka kwa
`-Dgaia.desktop.home=…`, ndivyo majaribio yanavyokaa mbali na saraka halisi ya nyumbani.

**`options.properties`** — huandikwa chaguo linapobadilika, na wakati wa kutoka.

| Funguo | Maana |
|---|---|
| `parse.mode`, `parse.language` | Kina cha mtiririko na lugha ya ujumbe |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Uonyeshaji wa tarehe |
| `check.skipRequires`, `check.skipExcludes` | Kuepuka ukaguzi wa muundo |
| `ui.theme` | `LIGHT` au `DARK` |
| `ui.optionsPane` | Paneli ya machaguo iko wazi? |
| `ui.scannerDevice` | Jina la mwisho la kimantiki la JavaPOS |
| `ui.mode` | `PARSE`, `BUILD` au `COMBINE` |
| `update.check` | Kama ukaguzi wa kila siku wa masasisho unaendeshwa (chaguo-msingi `true`) |
| `update.lastCheck` | Ulipoendeshwa mara ya mwisho, ISO-8601; kutokuwepo au kutosomeka kunamaanisha ukaguzi umefika wakati |
| `update.skipVersion` | Toleo ambalo mtumiaji alichagua *Ruka toleo hili*; halitolewi tena kiotomatiki |

Faili iliyokosekana au kuharibika kamwe si ya kuua: kila thamani hurudi nyuma peke yake, kwa hivyo mstari mmoja mbaya
unagharimu mstari huo tu na si kingine. Katika uzinduzi wa kwanza, bila faili kabisa, machaguo hujazwa kutoka mipangilio
ya kikanda ya mashine na `SystemDefaults` badala ya chaguo-msingi zilizojengwa ndani ya GAIA — mtindo mfupi wa tarehe wa
eneo hutoa mpangilio wa vipengele, kitenganishi, na kama mwezi na mwaka vimefupishwa.

Kamwe usiipe `Properties.setProperty` null: inatupa, na `persistOptions()` huendeshwa kutoka vishughulikiaji vya matukio
ya UI ambapo hitilafu-pekee hutorokea kwenye uzi wa FX. Hicho ni kuanguka halisi ambacho programu hii imekwisha kupata.

**`captures.tsv`** — huandikwa historia inapobadilika, na wakati wa kutoka. Mpya zaidi kwanza, kikomo cha maingizo
**500**.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Kinachohifadhiwa ni kile tu kisichoweza kuhesabiwa upya. `ParseResult` **haisanifishwi** — hupatikana upya kwa
kuchanganua ingizo tena wakati wa kupakia, kwa hivyo historia iliyorejeshwa huakisi machaguo yanayotumika *sasa*. Maingizo
kwa kawaida huwa na FNC1 na chochote kingine ambacho skana hutoa, kwa hivyo sehemu ya ingizo hutoroshwa (`\\`, `\t`,
`\n`, `\r`, na `\xNN` kwa kila kitu kingine chini ya `0x20`) na faili husomwa na kuandikwa kama ISO-8859-1, baiti kwa
baiti.

Faili zilizoandikwa kabla tarehe haijaanza kurekodiwa zina saa tu; bado zinapakiwa, zikiwa na tarehe ya leo, kwa sababu
tarehe halisi haikuwahi kuandikwa.

---

## Majaribio

```bash
(cd gaia-desktop && mvn test)
```

Majaribio 198, kwa makusudi **bila zana za JavaFX**, jambo linalowafanya kuwa ya haraka na bila skrini. Yanafunika kionyeshi
cha ripoti, mantiki ya kutengeneza na utafutaji wa AI, ubadilishaji wa `{GS}`, chaguo-msingi zinazotokana na eneo, hifadhi
zote mbili (pamoja na safari ya kwenda na kurudi yenye `0x1D` na faili zilizoharibiwa kwa makusudi), jedwali la
simbolojia, kanuni ya jukwaa la skana, kishina cha toleo, na katalogi.

Matatu yanafaa kujulikana kwa sababu yanalinda mikataba badala ya kazi:

| Jaribio | Hushindwa wakati |
|---|---|
| `CatalogueCoverageTest` | Msimbo unaomba funguo ambayo `ui_EN.properties` haijaifafanua, au faili yoyote kati ya 35 inapotoka kutoka seti ya funguo ya Kiingereza |
| `SymbologiesTest` | Kipeo chochote cha `SCAN_SDT_*` ambacho JavaPOS hutangaza hakina jina la kuonyesha — pamoja na kile ambacho uboreshaji wa JavaPOS huongeza |
| `ScannerServiceTest` | Kanuni ya jukwaa inabadilisha jibu lake kwa Windows au kwa kitu kingine chochote |

`UiStrings` kwa makusudi husamehe wakati wa kuendesha — funguo isiyojulikana huonyeshwa kama funguo yenyewe badala ya
kutupa katikati ya mpangilio — ndiyo hasa sababu jaribio la katalogi lazima liwepo.

**Hayafunikwi:** chochote kinachohitaji zana hai — tabia ya umakini, swichi ya hali, uhariri wa jedwali, kubadilisha
lugha, na kuanguka mara mbili kulikoelezwa chini ya [Nyuzi](#nyuzi). Hivyo vilithibitishwa kwa kuendesha dirisha
halisi, lakini kurudi nyuma ndani yake hakungefanya ujenzi ushindwe. Kuongeza TestFX au Monocle kungefunga pengo hilo.

---

## Kutatua matatizo ya ujenzi

| Dalili | Sababu na suluhisho |
|---|---|
| `package tools.pantheum.gaia does not exist` | Msingi haujasakinishwa. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | `Main-Class` ya jar lazima iwe `Launcher`, si `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* wakati wa kuanza | Inatarajiwa na haina madhara — JavaFX inaeleza kwamba iko kwenye njia ya madarasa. Ndiyo hasa sababu `Launcher` ipo |
| Jar haianzi, `NoClassDefFoundError` | Jar ya chaguo-msingi hubeba madarasa ya moduli hii tu; `dist/lib/` lazima iwe kando yake. Tumia `-Pfat` kwa faili moja inayojitosheleza |
| `Artifact has not been packaged yet … MDEP-187` katika Eclipse | Utatuzi wa nafasi ya kazi wa m2e. Angalia dokezo chini ya [Ufungashaji](#ufungashaji) |
| Faili za `… 2.properties` zinazojirudia zinaonekana katika ujenzi | Mradi uko katika folda inayolinganishwa na iCloud na ulinganishaji ulizalisha nakala zinazokinzana katikati ya ujenzi. Daima `mvn clean package`; bora zaidi, ondoa `target/` kwenye ulinganishaji |
| Kipengee cha menyu kinaonyesha funguo kama `act.hri` | Funguo hiyo haipo katika `ui_EN.properties`. Ujenzi ulipaswa kuikamata — angalia [Majaribio](#majaribio) |

---

## Mipaka inayojulikana na kazi iliyobaki

- **Hakuna majaribio ya UI bila skrini.** Angalia [Majaribio](#majaribio).
- **Vitu mahususi kwa jukwaa.** Ufungashaji wote miwili hufungasha maktaba asilia za JavaFX za jukwaa moja; jenga moja kwa
  kila lengo kwa `-Dfx.platform` (mashine yoyote inaweza kuzijenga zote). Hakuna unaofungasha mazingira ya kuendesha
  Java — hilo linahitaji `jpackage`, inayojenga tu kwa OS inayoendeshwa juu yake.
- **Ingizo la skana ni la Windows pekee.** Angalia [Lango la jukwaa](#lango-la-jukwaa).
- **`ScanSource.label()` haijajanibishwa.** Angalia [Kisichotafsiriwa kwa makusudi](#kisichotafsiriwa-kwa-makusudi).
- **`UiState.mode` ni `String`** (`"PARSE"` / `"BUILD"`) ilhali `Theme` kando yake ni enum, kwa hivyo thamani halisi
  hiyo inaonekana mahali kadhaa. Enum ingeziondoa.
- **Lengo la Digital Link ni kikoa na kiambishi cha njia pekee.** `BuilderDigitalLinkConfig` pia inaauni vigezo vya
  ziada vya hoja na kipande (fragment); kihariri hakivionyeshi.
- **Jozi za AI zinazokinzana (`GE-S006`) haziwekewi alama katika kihariri** jinsi AI zisizojulikana na zinazojirudia
  zinavyowekewa — kutengeneza hushindwa tu.
- **Katalogi ya hitilafu ya msingi huongeza alama za nukuu mara mbili** (`value ''09506000134350''`), jambo linaloonekana
  kama lilivyo katika kushindwa kwa kutengeneza na katika ripoti. Inaonekana kama utoroshaji wa nukuu wa `MessageFormat`
  usioondolewa; marekebisho ni ya msingi.
