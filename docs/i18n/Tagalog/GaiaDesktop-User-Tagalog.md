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
6. [Mode ng pagsasama](#mode-ng-pagsasama)
   - [Ang bar ng pagsasama](#ang-bar-ng-pagsasama)
   - [Ang hindi tinatanggap ng pagsasama](#ang-hindi-tinatanggap-ng-pagsasama)
   - [Mga duplicate at salungatan](#mga-duplicate-at-salungatan)
   - [Ang ulat ng pagsasama](#ang-ulat-ng-pagsasama)
7. [Ang ulat](#ang-ulat)
   - [Pag-export](#pag-export)
8. [Mga opsyon sa pagsusuri](#mga-opsyon-sa-pagsusuri)
   - [Unang pagpapatakbo](#unang-pagpapatakbo)
9. [Mga wika](#mga-wika)
10. [Ang naaalala](#ang-naaalala)
11. [Mga keyboard shortcut](#mga-keyboard-shortcut)
12. [Pag-aayos ng problema](#pag-aayos-ng-problema)
13. [Mga limitasyon](#mga-limitasyon)

---

## Ano ito

Tatlong bagay ang ginagawa ng GAIA Desktop sa datos ng GS1:

- **Pagsusuri** — mag-type ng element string ng GS1 o ng Digital Link URI, o i-paste ito, o
  buksan ito mula sa file, o i-scan ito, at basahin ang resulta bilang buong ulat: ano ang ibig
  sabihin ng bawat Application Identifier (AI), nasaan ang mali, at bakit;
- **Pagbuo** — pagsasama-sama ng mga pares na AI at halaga upang makalikha ng element string o ng
  Digital Link URI;
- **Pagsamahin** — pagsasama ng hanggang tatlong GS1-128 barcode mula sa iisang label tungo sa
  iisang element string na dadalhin ng isang GS1 DataMatrix, pagsuri nito nang buo, at pagguhit ng
  simbolo.

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

> Tulad ng lahat ng ibang code block sa dokumentong ito, nananatiling Ingles ang mga label ng
> dayagram na ito. Sa screen ay makikita mo ang *Suriin* / *Bumuo* / *Pagsamahin*, *Mga capture*,
> *I-save ang ulat*, *Mga opsyon*, *scanner: patay* at *Tungkol*.

**Sa macOS at Linux ay wala talaga ang linya ng scanner sa dayagram na ito** — walang button sa
toolbar, walang listahan ng pagpili ng device, walang menu na *Scanner*, at walang
*scanner: patay* sa status bar. Tingnan ang [Pag-scan](#pag-scan).

Ang switch na **Suriin ⇄ Bumuo** ay nagpapalit lamang ng bar sa ilalim ng toolbar at ng mga
kontrol na may kinalaman sa capture. Ang kasaysayan, ang ulat, ang panel ng mga opsyon at lahat ng
aksyon ng pag-export ay gumagana sa parehong mode, dahil ang matagumpay na pagbuo ay nagiging isa
ring capture lamang. Iba ang ayos ng mode na **Pagsamahin**: tingnan ang [Mode ng pagsasama](#mode-ng-pagsasama).

**Sarado ang panel ng mga opsyon sa unang pagpapatakbo.** Ang mode, ang pagpapakita o pagtatago ng
panel, ang tema at ang scanner na huling ginamit — lahat ay naaalala para sa susunod.

**Mga bagong bersyon.** Ilang segundo matapos magsimula, hindi hihigit sa isang beses sa isang araw,
tinatanong ng application ang GitHub kung may inilabas nang mas bagong GAIA Desktop. Kung mayroon,
lalabas sa status bar ang isang link — *Available na ang GAIA Desktop 1.1.0* — na nagbubukas ng pahina ng
release sa iyong browser; walang dina-download o ini-install para sa iyo. Itinatago ng
*Laktawan ang bersyong ito* ang abiso hanggang sa susunod na release. Tahimik ang pagsusuri kapag walang bago
o kapag hindi maabot ang GitHub. Agad na sumusuri at nag-uulat ang
*Tulong → Tingnan kung may update…*; pinapatay at muling binubuhay ng
*Tulong → Awtomatikong tingnan kung may update* ang araw-araw na pagsusuri.

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
rin ang status bar, at ang scan na dumarating habang bukas ang editor ay nakukuha rin. Sa mode na Pagsamahin, nananatili ang mga kontrol na ito, at idinaragdag ang bawat scan sa
pagsasama sa halip na sa kasaysayan.

Ang unang pag-set up ng scanner — ang Zebra Scanner SDK, ang USB mode ng scanner, at ang mga setting ng
GS1 — ay ipinapaliwanag nang hakbang-hakbang sa
[gabay sa pag-set up ng Zebra scanner](GaiaDesktop-Zebra-Scanner-Windows-Tagalog.md), na kasama sa bawat zip sa ilalim ng `docs/`. Kung walang device na kumokonekta, tingnan ang [Pag-aayos ng problema](#pag-aayos-ng-problema).

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

## Mode ng pagsasama

Madalas na hinahati ng mga lumang label ang kanilang datos sa **ilang GS1-128 barcode** — ang GTIN sa
isa, ang petsa ng pagkapaso at ang batch sa iba — dahil hindi kayang dalhin ng iisang linear barcode
ang lahat. Kumukuha ang mode na Pagsamahin ng hanggang **tatlo** sa mga barcode na iyon,
pinagsasama ang mga ito tungo sa iisang element string na dadalhin ng isang **GS1 DataMatrix**, at
sinusuri ang resulta nang buo. Kapag wasto na ito, iginuguhit ng application ang simbolong DataMatrix.

Hindi kailangang kumpleto nang mag-isa ang alinman sa mga barcode na iyon. Ang batch number na walang
sariling GTIN ay bumabagsak nang mag-isa sa `GE-S005`, pero normal iyon para sa isang barcode ng isang
set, kaya sinusuri ito ng mode na Pagsamahin sa pagsasama.

### Ang bar ng pagsasama

Kapag lumipat sa **Pagsamahin**, napapalitan ang input bar ng isang bar sa ibabang gilid ng
bintana. Tumatabi ang kasaysayan ng mga capture, dahil hindi capture ang mga scan sa mode na
Pagsamahin at hindi kailanman pumapasok doon. Ang ulat ng pagsasama ang ipinapakita ng bahagi ng
ulat sa halip:

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

May dalawang paraan para magpasok ng barcode:

- **I-scan ito.** Sa Windows, nananatili sa toolbar ang mga kontrol ng scanner sa mode na
  Pagsamahin, at diretso sa pagsasama ang bawat scan, hindi sa kasaysayan.
- **I-type o i-paste ito** sa field na *Barcode* at pindutin ang <kbd>Enter</kbd> o
  ang **Idagdag**. Isulat ang FNC1 bilang **`{GS}`**, gaya sa input bar. Ang barcode na
  ini-type nang walang AIM Code ID ay itinuturing na GS1-128.

| Button | Ginagawa |
|---|---|
| **Idagdag** | Idinaragdag ang barcode na nasa field |
| **Alisin ang huli** | Inaalis ang pinakabagong barcode (hindi magagamit kapag wala pa) |
| **I-reset** | Inaalis ang lahat ng barcode, nililinis ang field at anumang pagtanggi, at nagsisimulang muli |

Laging sinasabi ng status bar kung ilang barcode ang hawak (*2 sa 3 GS1-128 barcode*), o kung bakit
tinanggihan ang huli.

### Ang hindi tinatanggap ng pagsasama

Hindi idinaragdag ang tinanggihang barcode. Parehong sinasabi ng ulat at ng status bar ang dahilan:

| Barcode | Tinanggihan dahil |
|---|---|
| Walang lamang field | Walang idaragdag |
| Ikaapat na barcode | Pinagsasama ang isang label mula sa hanggang tatlo lamang. Alisin ang huli, o i-reset |
| Ibang symbology, ayon sa AIM Code ID nito | GS1-128 lamang ang pinagsasama, kaya tinatanggihan ang GS1 DataMatrix (`]d2`) o QR Code, kasama ang pangalan nito |
| Mga AI na hindi mabasa | Error sa syntax; ipinapakita ang error code at mensahe ng engine |
| Walang anumang AI | Walang pagsasamahin |
| Na-scan na | Ang parehong element string nang dalawang beses, halimbawa ang parehong barcode na na-scan ulit; sinasabi ng ulat kung aling naunang barcode ang inuulit nito |

Tinatanggihan lamang ng mode na Pagsamahin ang hindi talaga nito magagamit. Ang lahat ng iba pa
(maling check digit, maling petsa, nawawalang kasamang AI) ay tinatanggap at hinuhusgahan sa pagsasama.

### Mga duplicate at salungatan

Pinagsasama ang mga AI ayon sa pagkakasunod ng pag-scan. Kapag may parehong AI ang dalawang barcode:

- **na may parehong halaga**, isang beses itong itinatago at inililista sa ilalim ng
  *Mga duplicate — itinago nang isang beses*, kasama ang mga barcode kung saan ito nabasa;
- **na may magkaibang halaga**, isa itong **salungatan**. Ang hatol ay *SALUNGATAN*,
  inililista ang nagbabanggaang halaga bawat barcode, at **walang pinagsasama** hanggang malutas ito.
  Alisin ang may-maling barcode, o i-reset.

### Ang ulat ng pagsasama

Mula itaas hanggang ibaba:

1. **Ang hatol**: *walang scan*, *SALUNGATAN*, wasto o hindi wasto, at kung ilang
   barcode ang hawak.
2. **Ang mga scan**: bawat barcode kung paano ito dumating, ang symbology nito, at ang element string na
   natitira kapag inalis ang AIM Code ID. Ang problemang tanging pagsasama lamang ang makapagpapasya ay
   ipinapakita sa barcode na iyon, may markang *hinuhusgahan sa pagsasama*.
3. **Mga duplicate** at **salungatan**, kung mayroon.
4. **Pinagsama**: ang element string, ang HRI at haba nito, pagkatapos ang pinagsamang input
   bilang tape na may label at isang card bawat AI, parehong nakaayos gaya sa
   [ulat ng pagsusuri](#ang-ulat). Pagkatapos ay inililista sa ilalim ng *Pagsusuri* ang
   bawat error at babala sa pagsasama.
5. **Ang simbolong GS1 DataMatrix**, kasama ang HRI nito sa ilalim. Iginuguhit lamang ito kapag wasto
   ang pagsasama. Kung hindi, sinasabi ng ulat kung bakit hindi ito iginuhit, para hindi ma-print nang
   hindi sinasadya ang isang di-wastong simbolo.

Halimbawa, isang label na may dalawang barcode:

| Barcode | Dala | Mag-isa |
|---|---|---|
| `]C10109506000134352` | (01) GTIN | wasto |
| `]C11726123110LOT-ABC` | (17) petsa ng pagkapaso, (10) batch | `GE-S005` para sa dalawang AI, hinuhusgahan sa pagsasama |

Napagsasama ang mga ito tungo sa `01095060001343521726123110LOT-ABC`, HRI
`(01)09506000134352 (17)261231 (10)LOT-ABC`, na wasto, kaya iginuguhit ang simbolo. Nakapirmi ang haba
ng AI `17`, kaya hindi kailangan ng FNC1 pagkatapos nito. Naglalagay ang mode na Pagsamahin ng isa
saanman ito hinihingi ng pamantayan.

Sa sandaling may hawak nang isang barcode, ine-export ng **I-save ang ulat** ang ulat ng pagsasama
bilang HTML o PDF, gaya ng isang ulat ng pagsusuri (tingnan ang [Pag-export](#pag-export)). Nagsisimula sa
`gaia-combine-` ang pangalan ng HTML file.

Sinusunod ng ulat ang napiling wika at tema. Kapag binago ang isang opsyon sa pagsusuri o ang wika,
muling sinusuri ang mga barcode na hawak. Nananatili rin ang mga ito kapag lumipat sa Suriin o
Bumuo at bumalik, pero hindi pagkatapos mag-restart: hindi sine-save ang mga barcode ng mode na
Pagsamahin.

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
| `options.properties` | Lahat ng opsyon sa pagsusuri, ang tema, kung bukas ba ang panel ng mga opsyon, ang scanner na huling ginamit, kung anong mode iniwan ang bintana, at ang mga setting ng pagsusuri ng update — bukas o patay, kailan ito huling tumakbo, at anumang bersyong pinili mong laktawan |
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

Hindi sine-save ang mga barcode na hawak sa mode na Pagsamahin: pagkatapos mag-restart,
nagsisimulang walang laman ang mode na Pagsamahin.

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

Sa mode na Pagsamahin: idinaragdag ng <kbd>Enter</kbd> sa field na *Barcode* ang
barcode. Doon, i-type ang FNC1 bilang `{GS}`; ang <kbd>⌘G</kbd> ay para sa input bar ng mode na
Suriin.

---

## Pag-aayos ng problema

| Palatandaan | Dahilan at lunas |
|---|---|
| Hindi bumabangon ang programa, o nagbibigay ng `NoClassDefFoundError` | Dapat nasa tabi ng JAR file ang direktoryong `lib/`. Panatilihin silang magkatabi, o gamitin ang distribusyong kumpleto sa sarili |
| Sa console sa pagsisimula ay *“Unsupported JavaFX configuration…”* | Inaasahan at walang pinsala — sinasabi lamang ng toolkit kung paano ito na-load. Gumagana ang programa gaya ng dati |
| Mukhang nawala ang separator na ipinaste | Ganoon nga dapat: nagiging nakikitang token na `{GS}` ito, at ang mismong tunay na character ang natatanggap ng parser. Tingnan ang [token na {GS}](#input-bar-at-ang-token-na-gs) |
| Wala talaga ang button na *Ikonekta ang scanner* | Nasa macOS o Linux ka, kung saan walang driver ng scanner. Tingnan ang [Pag-scan](#pag-scan) |
| Hindi kumokonekta ang scanner | Tingnan kung tugma sa modelo mo ang lohikal na pangalan sa listahan, kung nakasaksak ang scanner, at kung walang ibang programang humahawak dito. Inililista ng [gabay sa pag-set up ng Zebra scanner](GaiaDesktop-Zebra-Scanner-Windows-Tagalog.md#11-pag-aayos-ng-problema) ang bawat error code. |
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
- **GS1-128 barcode lamang ang tinatanggap ng mode na Pagsamahin, tatlo ang pinakamarami**, at
  hindi nito itinatago ang mga ito sa pagitan ng mga pagpapatakbo.
