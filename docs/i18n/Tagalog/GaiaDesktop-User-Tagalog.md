# GAIA Desktop — Gabay ng gumagamit

Ipinapaliwanag ng dokumentong ito kung paano gamitin ang programa. Para sa pagbuo, pagbabago o
pagdaragdag ng bagong wika, tingnan ang [gabay ng developer](../../GaiaDesktop-Developer.md) (sa
Ingles). Orihinal na dokumento: [GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Nilalaman

1. [Ano ito](#ano-ito)
2. [Pagpapatakbo](#pagpapatakbo)
3. [Ang bintana](#ang-bintana)
4. [Mode ng pagsusuri](#mode-ng-pagsusuri)
   - [Input bar at ang token na {GS}](#input-bar-at-ang-token-na-gs)
   - [Pag-scan](#pag-scan)
   - [Pagbubukas ng file](#pagbubukas-ng-file)
   - [Kasaysayan ng mga capture](#kasaysayan-ng-mga-capture)
5. [Mode ng pagbuo](#mode-ng-pagbuo)
   - [Editor ng AI](#editor-ng-ai)
   - [Ang hindi tinatanggap ng editor](#ang-hindi-tinatanggap-ng-editor)
   - [Element string o Digital Link](#element-string-o-digital-link)
   - [Pagbabago ng umiiral na capture](#pagbabago-ng-umiiral-na-capture)
6. [Ang ulat](#ang-ulat)
   - [Pag-export](#pag-export)
7. [Mga opsyon sa pagsusuri](#mga-opsyon-sa-pagsusuri)
   - [Unang pagpapatakbo](#unang-pagpapatakbo)
8. [Mga wika](#mga-wika)
9. [Ang naaalala](#ang-naaalala)
10. [Mga keyboard shortcut](#mga-keyboard-shortcut)
11. [Pag-aayos ng problema](#pag-aayos-ng-problema)
12. [Mga limitasyon](#mga-limitasyon)

---

## Ano ito

Dalawang bagay ang ginagawa ng GAIA Desktop sa datos ng GS1:

- **Pagsusuri** — mag-type ng element string ng GS1 o ng Digital Link URI, o i-paste ito, o
  buksan ito mula sa file, o i-scan ito, at basahin ang resulta bilang buong ulat: ano ang ibig
  sabihin ng bawat Application Identifier (AI), nasaan ang mali, at bakit;
- **Pagbuo** — pagsasama-sama ng mga pares na AI at halaga upang makalikha ng element string o ng
  Digital Link URI.

Lahat ng alam ng programang ito tungkol sa GS1 — ang kahulugan ng bawat AI, bawat panuntunan sa
pagsusuri, bawat mensahe ng error at bawat detalye ng pagsasawika — ay nagmumula sa makina ng
GAIA na tumatakbo sa ilalim nito. Walang idinaragdag na sariling panuntunan ang programa:
tinatanggap nito ang eksaktong tinatanggap ng makina.

---

## Pagpapatakbo

```bash
java -jar gaia-desktop.jar
```

Kailangan ng JAR file na ito ang direktoryong `lib/` na kasamang ipinadadala — panatilihin silang
magkatabi, kung hindi ay hindi babangon ang programa. (Maaari ring bumuo ng JAR file na kumpleto
sa sarili; tingnan ang [gabay ng developer](../../GaiaDesktop-Developer.md#packaging).)

Kailangan ang **Java 21 o mas bago**. Ang distribusyon ay ginagawa para sa isang operating system
at isang arkitektura ng processor, kaya gamitin ang JAR file na ginawa para sa iyong makina.

Upang buksan ang programa sa isang partikular na wika nang hindi binabago ang naka-save na wika:

```bash
java -jar gaia-desktop.jar --language=FR
```

Maaaring code ng katalogo (`FR`, `PCM`, `ARZ`), buong pangalan (`FRENCH`, `WU-CHINESE`), o tag ng
wika (`fr`; para sa Cantonese `zh-HK`, para sa Egyptian Arabic `ar-EG`) ang halaga — walang
pinagkaiba ang malaki at maliit na titik. Sa takbong iyon lamang gumagana ang utos na ito: nananatili
ang naka-save mong wika, kaya hindi mababago ng shortcut na ginawa mo para sa kasamahan kung anong
wika bumubukas ang sarili mong kopya. Ngunit ang pagpili ng wika mula sa *loob* ng programa ay
tunay na pagpili at nase-save gaya ng dati. Ang halagang hindi tumutukoy sa anumang wika ay
binabalewala nang may paunawa sa console, at bumubukas ang programa sa naka-save na wika sa halip
na tumangging tumakbo.

Sa pagsisimula, naglilimbag ang console ng isang linyang naglalaman ng bersyon ng programa, ng
petsa ng pagbuo nito at ng bersyon ng makina. Ang linyang iyon din ang dapat isama sa anumang
ulat ng depekto.

---

## Ang bintana

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

> Tulad ng lahat ng ibang code block sa dokumentong ito, nananatiling Ingles ang mga label ng
> dayagram na ito. Sa screen ay makikita mo ang *Suriin* / *Bumuo*, *Mga capture*,
> *I-save ang ulat*, *Mga opsyon*, *scanner: patay* at *Tungkol*.

**Sa macOS at Linux ay wala talaga ang linya ng scanner sa dayagram na ito** — walang button sa
toolbar, walang listahan ng pagpili ng device, walang menu na *Scanner*, at walang
*scanner: patay* sa status bar. Tingnan ang [Pag-scan](#pag-scan).

Ang switch na **Suriin ⇄ Bumuo** ay nagpapalit lamang ng bar sa ilalim ng toolbar at ng mga
kontrol na may kinalaman sa capture. Ang kasaysayan, ang ulat, ang panel ng mga opsyon at lahat ng
aksyon ng pag-export ay gumagana sa parehong mode, dahil ang matagumpay na pagbuo ay nagiging isa
ring capture lamang.

**Sarado ang panel ng mga opsyon sa unang pagpapatakbo.** Ang mode, ang pagpapakita o pagtatago ng
panel, ang tema at ang scanner na huling ginamit — lahat ay naaalala para sa susunod.

---

## Mode ng pagsusuri

### Input bar at ang token na {GS}

Tinatanggap ng bar na ito ang element string, ang GS1 Digital Link URI, o ang hilaw na scan kasama
ang unlaping AIM Code ID (`]d2…`). Sinisimulan ng <kbd>Enter</kbd> ang pagsusuri.

Ang naghihiwalay sa mga AI na iba-iba ang haba ay ang **FNC1** — ang hindi nakikitang control
character na `0x1D`. Walang text field na kayang hawakan ang character na ito: tahimik itong
nabubura, at ang `01…` + FNC1 + `10LOT` ay dumarating doon bilang `01…10LOT` — isang element string
na nagbago ang kahulugan.

Kaya nga ipinapakita ng programa ang separator bilang nakikitang token na **`{GS}`** at ibinabalik
ito sa dati habang papunta sa parser:

- Inilalagay ng **button na `{GS}`** ang token sa kinaroroonan ng cursor;
- Kapag **ipinaste** ang tunay na mga character ng paghihiwalay — mula sa keyboard, mula sa context
  menu o sa pamamagitan ng pag-drag — kusa silang nagiging `{GS}`, kaya walang nawawala;
- Isinusulat ang token nang eksakto sa paraang isinusulat ito ng ulat, kaya ang halagang kinopya
  mula sa ulat ay maaaring ipaste pabalik nang tuwiran.

Ang mga capture, ang ulat at ang naka-save na kasaysayan — silang tatlo ay may dalang **tunay** na
separator. Ang token ay iyon lamang nakikita mo at itinitipa mo.

### Pag-scan

> **Windows lamang.** Para sa Windows lamang naglalabas ang Zebra ng mga driver ng kanilang
> scanner, kaya sa macOS at Linux ay wala talagang ginagawang kontrol ng scanner ang programa —
> walang button na *Ikonekta ang scanner*, walang listahan ng device, walang menu na *Scanner*,
> walang katayuan ng scanner sa status bar. Walang ipinapakitang malabo, dahil wala talagang
> setting o device na makapagpapagana nito. Lahat ng iba pa, kasama ang pagbubukas ng file na puno
> ng mga naka-save na scan, ay gumagana kahit saan.

Binubuksan ng *Ikonekta ang scanner* ang device na nakasaad sa listahan; ang `ZebraAllScanners`
ang default at bagay sa karamihan ng modelo. Sinusuri ang bawat scan at kusang idinaragdag sa
kasaysayan.

Binabasa ang datos ng scanner nang byte kada byte, kaya ang unlaping AIM Code ID at bawat separator
na FNC1 ay dumarating sa parser sa parehong anyo na ipinadala ng scanner.

Sa mode ng pagbuo ay itinatago ang mga kontrol ng scanner, ngunit patuloy sa paggana ang nakakonektang
scanner: binabago pa rin ito ng *Scanner → Ikonekta / Idiskonekta* (<kbd>⌘K</kbd>), nag-uulat pa
rin ang status bar, at ang scan na dumarating habang bukas ang editor ay nakukuha rin.

Kung walang device na kumokonekta, tingnan ang [Pag-aayos ng problema](#pag-aayos-ng-problema).

### Pagbubukas ng file

Sinusuri ng *File → Magbukas ng text file…* ang **bawat linyang may laman bilang hiwalay na
capture** — ito ang pinakamabilis na paraan upang harapin ang tumpok ng mga scan na ipinadala sa
iyo ng iba. Binabasa ang file nang byte kada byte, kaya ligtas din ang tunay na mga separator sa
loob nito.

### Kasaysayan ng mga capture

Nasa itaas ang pinakabago. Bawat linya ay nagpapakita ng tuldok na wasto / hindi wasto, ng buod na
isang linya, at katabi nila ang oras ng capture, ang pinagmulan nito at ang bilang ng mga AI sa
loob. Lumalabas lamang ang petsa sa mga capture ng mga nakaraang araw; ang mga linya ngayong araw
ay may dalang oras lamang.

Ang buod para sa Digital Link ay ang **Digital Link URI**, at para sa lahat ng iba ay ang **HRI**:
para sa element string, mas madaling basahin ang `(01)09506000134352 (17)261231` kaysa sa
`01095060001343521726123110…`.

Sa pag-right-click sa isang capture ay maaaring kopyahin ang input nito o ang HRI nito, ibalik ito
sa input bar, buksan ito sa editor, alisin ito, o burahin ang buong kasaysayan.

---

## Mode ng pagbuo

### Editor ng AI

Isang talahanayan ng mga pares na AI at halaga. Mag-type ng code ng AI, o pumili mula sa listahang
*Magdagdag ng AI…* mula sa **lahat ng AI na alam ng makina**; habang nagta-type ka, napupuno ang
mga hanay na **Pamagat**, **Format** at **Paglalarawan** sa wikang pinili mo. Hindi inaalok ng
listahan ang AI na tatanggihan sa oras ng pagbuo.

Sa dulo ng talahanayan ay laging may **isang walang lamang row**: magsulat ng kahit ano rito at
lilitaw ang susunod, kaya hindi mo na kailanman kakailanganin ang *Magdagdag ng row* para sa
string na maraming AI.

Kumikilos ang pag-edit gaya ng nararapat sa isang talahanayan: ang <kbd>Enter</kbd>,
<kbd>Tab</kbd>, <kbd>Shift</kbd>+<kbd>Tab</kbd> at ang pag-click sa ibang lugar — lahat sila ay
**nag-iingat** ng isinulat mo; iniiwan ito ng <kbd>Esc</kbd>. Ang <kbd>Tab</kbd> ay lumilipat mula
sa AI patungo sa halaga at pagkatapos sa susunod na row, na nilalaktawan ang mga hanay na hindi
maaaring i-edit.

### Ang hindi tinatanggap ng editor

| | Ang nangyayari |
|---|---|
| AI na hindi kilala ng makina | Namumula ang row habang nagta-type ka; tinatanggihan ang pagbuo nang may paliwanag |
| Ang parehong AI sa ikalawang pagkakataon mula sa listahan | Hindi ito naidaragdag — sinasabi ng mensahe ang pangalan nito, at pinipili ang row na mayroon na nito |
| Ang parehong AI sa ikalawang pagkakataon sa pagta-type nang manu-mano | Minamarkahan ang dalawang row; tinatanggihan ang pagbuo nang may error na `GE-S004` |
| Walang bubuuin | Isang mensahe; walang nabubuong capture |

Isang beses lamang maaaring lumitaw ang AI sa isang element string — kaya nga agad tumatanggi ang
listahan sa halip na hayaan kang subukan ang pagbuong tiyak nang mabibigo.

### Element string o Digital Link

Pinagdurugtong ng **Bumuo ng element string** ang mga row at inilalagay ang separator na FNC1 sa
bawat lugar na hinihingi ng pamantayan. Ang **Bumuo ng Digital Link** ay lumilikha ng URI; upang
ituro ito sa sarili mong resolver sa halip na sa karaniwang `id.gs1.org`, buksan ang
*Target na Digital Link*:

| Patlang | Epekto nito | Ang pag-iwan nitong walang laman |
|---|---|---|
| Domain | ang host ng resolver, gaya ng `example.com` | `id.gs1.org` |
| Prefix ng path | isang bahagi bago ang path ng mga AI, gaya ng `/products` | Walang prefix |

Kapag **nagtagumpay**, kumikilos ang resulta na parang isang scan lamang: sinusuri ito, sumasama
ito sa kasaysayan, ipinapakita ito bilang ulat at nase-save ito. Sinusuri ng pagbuo ang sarili
nitong resulta sa pamamagitan ng pag-parse nito, kaya tiyak na wasto ang ibinabalik nito.

Kapag **nabigo**, walang nakukuhang capture. Nananatili ang dahilan sa ilalim ng mga button ng
pagbuo — isang linya sa bawat suliranin, bawat isa ay may error, ang kaugnay na AI at ang sariling
paliwanag ng makina:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> Ang dalawang panipi sa mensaheng iyon ay hindi galing sa programang ito kundi sa sariling
> katalogo ng error ng makina.

### Pagbabago ng umiiral na capture

Ang pag-right-click sa isang capture → **Bumuo** ay nagdadala nito sa editor ng AI. Baguhin ang
isang halaga, bumuo muli, at lilitaw ang resulta bilang bagong capture — nananatiling buo ang
orihinal na capture.

---

## Ang ulat

Ipinapakita ang piniling capture bilang buong ulat: ang bar ng hatol, ang element string na
hinati-hati sa mga may pangalang “banda”, ang metadata ng pagsusuri, isang card sa bawat AI kasama
ang kahulugan nito, at ang mga error at babala kasama ang gabay sa pagwawasto.

Para sa **Digital Link**, sinasabi rin ng bawat card ng AI ang papel nito sa istruktura —
**primary key**, **key qualifier** o **data attribute** — sa mga kulay na sumusunod sa pagkakasunod
ng paglitaw nila sa URI. Sa Digital Link lamang nakikita ang mga papel na ito: sa payak na element
string ay wala talagang puwesto ang AI sa URI na mailalarawan.

Sumusunod ang ulat sa wika at temang pinili mo.

### Pag-export

| | |
|---|---|
| **I-save ang ulat → Bilang HTML…** (<kbd>⌘S</kbd>) | Isinusulat ang eksaktong nasa screen: isang file na kumpleto sa sarili at bumubukas sa kahit anong browser |
| **I-save ang ulat → Bilang PDF…** (<kbd>⌘P</kbd>) | Nililimbag ang ulat sa pamamagitan ng print dialog ng sistema — piliin doon ang patutunguhan ng PDF |

Dumaraan ang PDF sa print dialog dahil doon pinipili ang lugar at ang pangalan ng file: sa macOS
**PDF ▾ → Save as PDF**, sa Windows **Microsoft Print to PDF**, sa Linux **Print to File**.
Nahahati nang maayos ang mga pahina at nananatiling napipili ang teksto.

Laging maliwanag na tema ang ginagamit ng paglilimbag at ng PDF, kahit madilim na tema ang programa,
at pinananatili nila ang makukulay na marka ng AI at ang bar ng hatol gaya ng dati.

Kung wala talagang naka-configure na printer o patutunguhan ng PDF sa makina mo, sinasabi ito ng
programa at itinuturo ka nito sa pag-export sa HTML, na maaaring ilimbag mula sa kahit anong
browser.

---

## Mga opsyon sa pagsusuri

Tinitipon ng panel sa kanan ang lahat ng opsyon ng makina sa isang lugar. Sa pagbabago ng kahit
ano, **agad na muling sinusuri ang piniling capture**, kaya hindi lamang sa susunod na scan
nakaaapekto ang panel na ito — paraan din ito upang itanong: “Ano ang magiging anyo ng input na
ito sa mga setting na ito?”.

| Opsyon | Mga halaga |
|---|---|
| Mode ng pagsusuri | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — gaano kalalim aabot ang pagsusuri |
| Wika | 35 katalogo ng mensahe |
| Pagkakasunod | `LITTLE` (dd/mm/yyyy), `MIDDLE` (mm/dd/yyyy), `BIG` (yyyy/mm/dd) |
| Pantukoy | `SLASH`, `HYPHEN`, `PERIOD` |
| Buwan | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Taon | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Laktawan ang pagsusuring "requires" | Itinitigil ang pagtanggi sa AI na wala ang kinakailangang kasama nito (`GE-S005`) |
| Laktawan ang pagsusuring "excludes" | Itinitigil ang pagtanggi sa mga pares ng AI na ipinagbabawal ng pamantayan (`GE-S006`) |

Sa ilalim ng panel, ibinabalik ng *Ibalik sa default* ang sariling default na halaga ng makina.

### Unang pagpapatakbo

Hangga't walang na-save pa, kinukuha ng programa ang mga setting nito mula sa **mga panrehiyong
setting ng makina mo** sa halip na sa default ng makina:

- Ang **wika** ay nagmumula sa locale ng sistema, at bumabalik sa Ingles kung saan walang katalogo;
- Ang **mga opsyon sa petsa** ay binabasa mula sa maikling anyo ng petsa ng locale mo: ang
  pagkakasunod ng mga bahagi, ang pantukoy, at kung pinaiikli ba ang buwan at taon o hindi.

> **Tandaan.** Kaya ang locale na sumusulat ng maiikling petsa nang may dalawang-digit na taon ay
> magsisimula rin dito nang may dalawang-digit na taon: naaayon ito sa iba pang bahagi ng sistema
> mo, ngunit naiiba sa sariling apat-na-digit na default ng makina. Palitan ito sa panel, at
> mananatili ang pinili.

---

## Mga wika

Ang pagpili ng wika ay hindi lamang nagpapalit ng mga mensahe ng error kundi ng **buong programa**
— ang mga menu, ang mga button, ang editor, ang panel ng mga opsyon, ang linya ng katayuan at ang
ulat. Binabaligtad ng mga wikang isinusulat mula kanan pakaliwa ang bintana at ang ulat.

Makukuha ang lahat ng 35 wika, sa dalawang patong na saklaw:

| | Saklaw |
|---|---|
| Mga ulat ng pagsusuri at lahat ng mensahe ng error ng makina | **Lahat ng 35 wika** |
| Ang mga menu, button at label ng mismong programa | **Lahat ng 35 wika** |

Ang mga wikang isinusulat mula kanan pakaliwa — Arabic, Egyptian Arabic, Persian at Urdu — ay
binabaligtad ang bintana at ang ulat mula pa sa sandaling bumukas ang programa.

Naaalala ang wika para sa mga susunod na pagkakataon. Para sa pagbubukas sa ibang wika nang isang
beses lamang, tingnan ang [`--language`](#pagpapatakbo).

May mga bagay na sadyang hindi isinasalin sa anumang wika: ang maiikling pangalan ng patlang na
nakalimbag katabi ng hilaw na mga halaga (`format`, `position`, `raw`, `len` …) — mga tagakilala
ang mga ito, hindi salita; ang mga pangalan ng pattern at ng symbology — bokabularyo ito ng GS1 at
ng industriya; at isang mahabang talata mula sa pamantayan, na ang kalahating salin ay mas
makalilito kaysa maglilinaw.

---

## Ang naaalala

Dalawang file sa `.gaia-desktop` sa loob ng iyong home directory:

| File | Ang laman |
|---|---|
| `options.properties` | Lahat ng opsyon sa pagsusuri, ang tema, kung bukas ba ang panel ng mga opsyon, ang scanner na huling ginamit, at kung anong mode iniwan ang bintana |
| `captures.tsv` | Ang kasaysayan ng mga capture, pinakabago muna, hanggang 500 |

Sa tuwing binabago mo ang isang opsyon ay naisusulat ito; sa tuwing nagbabago ang kasaysayan ay
naisusulat ito. Muling isinusulat silang dalawa sa paglabas.

Ang pagbura ng isa sa dalawang file ay nagbabalik lamang sa bahaging iyon sa unang kalagayan nito
at wala nang iba — kaya nga sila pinaghiwalay, upang hindi makasama ng sirang kasaysayan ang iyong
mga setting. Kahit ang file na nasira ng manu-manong pag-edit ay hindi nakamamatay: bawat setting
ay bumabalik sa sariling default nito nang isa-isa, kaya ang halaga ng isang maling linya ay hindi
lalampas sa linyang iyon.

Ang input ang iniingatan ng kasaysayan, hindi ang pagsusuri. Muli itong sinusuri sa pag-load, ibig
sabihin ay sinasalamin ng naibalik na capture ang mga opsyong ipinapatupad **ngayon**, hindi ang
mga ipinapatupad noong ito ay nakuha.

---

## Mga keyboard shortcut

| | |
|---|---|
| <kbd>Enter</kbd> | Suriin ang input bar |
| <kbd>⌘O</kbd> | Magbukas ng text file |
| <kbd>⌘S</kbd> | I-save ang ulat bilang HTML |
| <kbd>⌘P</kbd> | I-save ang ulat bilang PDF (print dialog ng sistema) |
| <kbd>⌘G</kbd> | Maglagay ng token ng separator na FNC1 |
| <kbd>⌘K</kbd> | Ikonekta / Idiskonekta ang scanner (Windows lamang) |
| <kbd>⌘D</kbd> | Palitan ang madilim na tema |
| <kbd>⌘Q</kbd> | Lumabas |

Sa editor: ang <kbd>Tab</kbd> / <kbd>Shift</kbd>+<kbd>Tab</kbd> ay gumagalaw sa pagitan ng mga
patlang na maaaring i-edit, kinukumpirma ng <kbd>Enter</kbd>, kinakansela ng <kbd>Esc</kbd>.

---

## Pag-aayos ng problema

| Palatandaan | Dahilan at lunas |
|---|---|
| Hindi bumabangon ang programa, o nagbibigay ng `NoClassDefFoundError` | Dapat nasa tabi ng JAR file ang direktoryong `lib/`. Panatilihin silang magkatabi, o gamitin ang distribusyong kumpleto sa sarili |
| Sa console sa pagsisimula ay *“Unsupported JavaFX configuration…”* | Inaasahan at walang pinsala — sinasabi lamang ng toolkit kung paano ito na-load. Gumagana ang programa gaya ng dati |
| Mukhang nawala ang separator na ipinaste | Ganoon nga dapat: nagiging nakikitang token na `{GS}` ito, at ang mismong tunay na character ang natatanggap ng parser. Tingnan ang [token na {GS}](#input-bar-at-ang-token-na-gs) |
| Wala talaga ang button na *Ikonekta ang scanner* | Nasa macOS o Linux ka, kung saan walang driver ng scanner. Tingnan ang [Pag-scan](#pag-scan) |
| Hindi kumokonekta ang scanner | Tingnan kung tugma sa modelo mo ang lohikal na pangalan sa listahan, kung nakasaksak ang scanner, at kung walang ibang programang humahawak dito |
| Sinasabi ng pag-export sa PDF na walang patutunguhan | Walang naka-configure na printer o patutunguhan ng PDF sa makina mo. Magdagdag ng isa, o mag-export bilang HTML at ilimbag mula sa browser |
| May lumalabas na tulad ng `act.hri` sa isang item ng menu | May label na nalimutan — mangyaring iulat ito; hindi ito setting kundi depekto noong ginawa ang build |

---

## Mga limitasyon

- **Pag-scan sa Windows lamang**, dahil naroon ang mga driver.
- **Dumaraan ang PDF sa print dialog**, hindi sa save dialog.
- **Domain at prefix ng path lamang ang tinatanggap ng mga target na Digital Link**; walang
  dagdag na query parameter o fragment.
- **Isang beses lamang lumilitaw ang AI** sa isang element string — panuntunan ito ng GS1, hindi
  limitasyon ng programa.
