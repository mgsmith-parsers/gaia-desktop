# GAIA Desktop — Jagorar mai amfani

Wannan takarda tana bayyana yadda ake amfani da manhajar. Don ginata, canza ta ko ƙara sabon
harshe, duba [jagorar mai haɓakawa](../../GaiaDesktop-Developer.md) (da Turanci). Asalin
takardar: [GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Abin da ke ciki

1. [Menene wannan](#menene-wannan)
2. [Gudanarwa](#gudanarwa)
3. [Taga](#taga)
4. [Yanayin bincike](#yanayin-bincike)
   - [Layin shigarwa da alamar {GS}](#layin-shigarwa-da-alamar-gs)
   - [Sikanin](#sikanin)
   - [Buɗe fayil](#buɗe-fayil)
   - [Tarihin rikodi](#tarihin-rikodi)
5. [Yanayin gina](#yanayin-gina)
   - [Editan AI](#editan-ai)
   - [Abin da edita ba ya karɓa](#abin-da-edita-ba-ya-karɓa)
   - [Igiyar abubuwa ko Digital Link](#igiyar-abubuwa-ko-digital-link)
   - [Gyara rikodin da yake nan](#gyara-rikodin-da-yake-nan)
6. [Rahoto](#rahoto)
   - [Fitarwa](#fitarwa)
7. [Zaɓuɓɓukan bincike](#zaɓuɓɓukan-bincike)
   - [Gudanarwa ta farko](#gudanarwa-ta-farko)
8. [Harsuna](#harsuna)
9. [Abin da ake tunawa](#abin-da-ake-tunawa)
10. [Gajerun hanyoyin madannai](#gajerun-hanyoyin-madannai)
11. [Warware matsaloli](#warware-matsaloli)
12. [Iyaka](#iyaka)

---

## Menene wannan

GAIA Desktop yana yin abubuwa biyu da bayanan GS1:

- **Bincike** — rubuta igiyar abubuwa ta GS1 ko URI na Digital Link, ko liƙa ta, ko buɗe ta daga
  fayil, ko sikanta ta, sa'an nan karanta sakamakon a matsayin cikakken rahoto: ma'anar kowane
  mai gano aikace-aikace (AI), inda kuskuren yake, da kuma dalilin sa;
- **Gina** — haɗa ma'auratan AI da ƙima don samar da igiyar abubuwa ko URI na Digital Link.

Duk abin da manhajar ta sani game da GS1 — ma'anar kowane AI, kowace ƙa'idar bincike, kowane
saƙon kuskure da kowane cikakken bayanin gida — daga injin GAIA da ke gudana ƙarƙashinta yake
zuwa. Manhajar ba ta ƙara wata ƙa'ida daga kanta ba: tana karɓar daidai abin da injin yake
karɓa.

---

## Gudanarwa

```bash
java -jar gaia-desktop.jar
```

Wannan fayil ɗin JAR yana buƙatar babban fayil ɗin `lib/` da ake aika tare da shi — ka ajiye su
tare, in ba haka ba manhajar ba za ta tashi ba. (Ana kuma iya gina fayil ɗin JAR mai tsayawa da
kansa; duba [jagorar mai haɓakawa](../../GaiaDesktop-Developer.md#packaging).)

Ana buƙatar **Java 21 ko sabo**. Ana gina rabon don tsarin aiki guda ɗaya da gine-ginen na'ura
guda ɗaya, don haka ka yi amfani da fayil ɗin JAR da aka gina don na'urarka.

Don buɗe manhajar da wani harshe ba tare da canza harshen da aka ajiye ba:

```bash
java -jar gaia-desktop.jar --language=FR
```

Ƙimar tana iya zama lambar kundi (`FR`, `PCM`, `ARZ`), cikakken suna (`FRENCH`, `WU-CHINESE`),
ko tambarin harshe (`fr`; don Kanton `zh-HK`, don Larabcin Masar `ar-EG`) — babu bambanci tsakanin
manyan haruffa da ƙanana. Wannan umarni yana aiki ga wannan gudanarwar kaɗai: harshenka da aka
ajiye ya kasance kamar yadda yake, don haka gajerar hanya da ka yi wa abokin aiki ba za ta iya
canza harshen da kwafinka kake buɗewa da shi ba. Amma zaɓar harshe daga *cikin* manhajar zaɓi ne
na gaskiya kuma ana ajiye shi kamar kullum. Ƙimar da ba ta nuna wani harshe ba ana yin watsi da
ita tare da lura a cikin na'urar wasiƙa, kuma manhajar tana buɗewa da harshen da aka ajiye maimakon
ta ƙi gudanarwa.

Da farawa, na'urar wasiƙa tana buga layi ɗaya mai ɗauke da sigar manhajar, ranar gina ta da sigar
injin. Wannan layin ne ya kamata a haɗa a kowane rahoton matsala.

---

## Taga

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

> Kamar sauran shingayen lamba a wannan takarda, wannan zane yana riƙe da lakabin Turanci. A kan
> allon za ka ga *Tantance* / *Gina*, *Rikodi*, *Ajiye rahoto*, *Zaɓuɓɓuka*, *Sikana: kashe* da
> *Game da*.

**A kan macOS da Linux layin sikana na wannan zane ba ya nan ko kaɗan** — babu maɓalli a mashayar
kayan aiki, babu jerin zaɓen na'ura, babu menu na *Sikana*, kuma babu *Sikana: kashe* a mashayar
matsayi. Duba [Sikanin](#sikanin).

Maɓallin **Tantance ⇄ Gina** yana canza mashayar da ke ƙarƙashin mashayar kayan aiki da kayan
aikin da suka shafi rikodi kaɗai. Tarihi, rahoto, fanel ɗin zaɓuɓɓuka da dukan ayyukan fitarwa
suna aiki a yanayi biyun, domin gina mai nasara ma ya zama rikodi guda kawai.

Fanel ɗin zaɓuɓɓuka **a rufe yake a gudanarwa ta farko**. Yanayin, ganin fanel ko ɓoye shi, jigon
da sikanan da aka yi amfani da shi na ƙarshe — duk ana tuna su don lokaci na gaba.

---

## Yanayin bincike

### Layin shigarwa da alamar {GS}

Wannan layi yana karɓar igiyar abubuwa, URI na GS1 Digital Link, ko ɗanyen sikan tare da
gabatarwar AIM Code ID (`]d2…`). <kbd>Enter</kbd> yana fara bincike.

Abin da ke raba AI masu tsawon canzawa shi ne **FNC1** — haruffan sarrafawa marar ganuwa `0x1D`.
Babu wani filin rubutu da zai iya riƙe wannan harafi: ana share shi shiru, sai `01…` + FNC1 +
`10LOT` ya isa can a matsayin `01…10LOT` — wato igiyar abubuwa da ma'anarta ta canza.

Saboda haka manhajar tana nuna mai rabawa a matsayin alamar da ake gani **`{GS}`** kuma tana
mayar da shi a hanyarsa zuwa mai tantancewa:

- **Maɓallin `{GS}`** yana saka alamar a inda mai nuni yake;
- Idan aka **liƙa** haruffan rabawa na gaskiya — daga madannai, daga menu na mahallin ko ta jan
  abu — suna juyawa da kansu zuwa `{GS}`, don haka babu abin da ke ɓacewa;
- Ana rubuta alamar daidai yadda rahoton ke rubuta ta, don haka ƙimar da aka kwafa daga rahoto
  ana iya liƙa ta kai tsaye.

Rikodi, rahoto da tarihin da aka ajiye — dukansu uku suna ɗauke da mai rabawa na **gaskiya**.
Alamar kawai ita ce abin da kake gani da rubutawa.

### Sikanin

> **Windows kaɗai.** Zebra tana bayar da direbobin sikana nata don Windows kaɗai, don haka a kan
> macOS da Linux manhajar ba ta gina wani kayan aikin sikana ko kaɗan — babu maɓallin *Haɗa
> sikana*, babu jerin na'urori, babu menu na *Sikana*, babu matsayin sikana a mashayar matsayi.
> Babu abin da ake nunawa a raunane, domin babu saiti ko na'urar da za ta kunna shi. Sauran dukan
> abubuwa, har da buɗe fayil cike da sikanan da aka ajiye, suna aiki ko'ina.

*Haɗa sikana* yana buɗe na'urar da aka ambata a jerin; `ZebraAllScanners` shi ne ƙimar tsoho kuma
ya dace da yawancin samfura. Ana tantance kowane sikan kuma ana ƙara shi cikin tarihi da kansa.

Ana karanta bayanan sikana bait bayan bait, don haka gabatarwar AIM Code ID da kowane mai rabawa
na FNC1 suna isa ga mai tantancewa daidai kamar yadda sikanan ya aika su.

A yanayin gina ana ɓoye kayan aikin sikana, amma sikanan da aka haɗa yana ci gaba da aiki:
*Sikana → Haɗa / Cire* (<kbd>⌘K</kbd>) yana nan yana canza shi, mashayar matsayi tana nan tana
sanarwa, kuma sikanan da ya iso yayin da edita a buɗe yake ana rikodin sa.

Idan babu na'urar da ta haɗu, duba [Warware matsaloli](#warware-matsaloli).

### Buɗe fayil

*Fayil → Buɗe fayil ɗin rubutu…* yana tantance **kowane layi maras kufai a matsayin rikodi na
kansa** — wannan ita ce hanya mafi sauri ta magance tarin sikanan da wani ya aiko maka. Ana
karanta fayil ɗin bait bayan bait, don haka masu rabawa na gaskiya da ke cikinsa suna nan
lafiya.

### Tarihin rikodi

Sabbin sama. Kowane layi yana nuna maki ingantacce / marar inganci, taƙaitawa ta layi ɗaya, kuma
tare da su lokacin rikodi, tushensa da adadin AI da ke cikinsa. Ranar tana bayyana kawai a kan
rikodin kwanakin baya; layukan yau suna ɗauke da lokaci kaɗai.

Taƙaitawar Digital Link ita ce **URI na Digital Link**, kuma ga sauran duka ita ce **HRI**: ga
igiyar abubuwa, karanta `(01)09506000134352 (17)261231` ya fi sauƙi fiye da
`01095060001343521726123110…`.

Da danna maɓallin dama a kan rikodi ana iya kwafa shigarwarsa ko HRI ɗinsa, mayar da shi zuwa
layin shigarwa, buɗe shi a edita, cire shi, ko share dukan tarihi.

---

## Yanayin gina

### Editan AI

Tebur na ma'auratan AI da ƙima. Rubuta lambar AI, ko zaɓa daga jerin *Ƙara AI…* daga cikin
**dukan AI da injin ya sani**; yayin da kake rubutawa, ginshiƙan **Take**, **Tsari** da
**Bayani** suna cikawa da harshen da ka zaɓa. Jerin ba ya bayar da AI da za a ƙi shi lokacin
gina.

A ƙarshen tebur koyaushe akwai **layi ɗaya maras kome**: rubuta wani abu a ciki sai na gaba ya
bayyana, don haka ba za ka taɓa buƙatar *Ƙara layi* ga igiya mai AI da yawa ba.

Gyara yana nuna hali kamar yadda ya kamata tebur ya yi: <kbd>Enter</kbd>, <kbd>Tab</kbd>,
<kbd>Shift</kbd>+<kbd>Tab</kbd> da danna wani wuri — duk suna **riƙe** abin da ka rubuta;
<kbd>Esc</kbd> yana barin sa. <kbd>Tab</kbd> yana tafiya daga AI zuwa ƙima sa'an nan zuwa layi na
gaba, yana tsallake ginshiƙan da ba a gyarawa.

### Abin da edita ba ya karɓa

| | Abin da ke faruwa |
|---|---|
| AI da injin bai sani ba | Layin yana yin ja yayin da kake rubutawa; ana ƙin gina tare da bayani |
| Wannan AI a karo na biyu daga jerin | Ba a ƙara shi ba — saƙon yana faɗi sunansa, kuma ana zaɓen layin da ya riga ya ɗauke shi |
| Wannan AI a karo na biyu da rubutu da hannu | Ana yiwa layukan biyu alama; ana ƙin gina da kuskuren `GE-S004` |
| Babu abin da za a gina | Saƙo ɗaya; ba a samar da wani rikodi ba |

AI yana iya bayyana sau ɗaya kaɗai a cikin igiyar abubuwa — saboda haka jerin yana ƙi nan take
maimakon ya bar ka ka gwada gina da ya riga ya faɗi.

### Igiyar abubuwa ko Digital Link

**Gina igiyar abubuwa** yana haɗa layukan tare kuma yana sanya mai rabawa na FNC1 duk inda
ƙa'idar take buƙata. **Gina Digital Link** yana samar da URI; don tura shi zuwa mai warwarenka
maimakon `id.gs1.org` na yau da kullum, buɗe *Manufar Digital Link*:

| Filin | Tasirinsa | Barinsa fanko yana nufin |
|---|---|---|
| Yanki | mai masaukin mai warware, kamar `example.com` | `id.gs1.org` |
| Gabatarwar hanya | wani sashe kafin hanyar AI, kamar `/products` | Babu gabatarwa |

Da **nasara**, sakamakon yana nuna hali daidai kamar sikan: ana tantance shi, yana shiga tarihi,
ana nuna shi a matsayin rahoto kuma ana ajiye shi. Gina yana duba sakamakonsa ta hanyar tantance
shi, don haka abin da yake mayarwa lallai ingantacce ne.

Da **faɗuwa**, ba a rikodin komai. Dalilin yana zama ƙarƙashin maɓallan gina — layi ɗaya ga
kowace matsala, kowanne da kuskuren, AI da abin ya shafa da bayanin injin da kansa:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> Alamun ambato biyu a cikin wannan saƙo ba daga wannan manhaja suke ba, daga jerin kurakuran
> injin da kansa suke.

### Gyara rikodin da yake nan

Danna maɓallin dama a kan rikodi → **Gina** yana kawo shi cikin editan AI. Canza wata ƙima, sake
gina, sai sakamakon ya bayyana a matsayin sabon rikodi — rikodin asali ya kasance kamar yadda
yake.

---

## Rahoto

Ana nuna rikodin da aka zaɓa a matsayin cikakken rahoto: mashayar hukunci, igiyar abubuwa da aka
raba zuwa “mashaya” masu suna, bayanan bincike na sama, katin ɗaya ga kowane AI tare da ma'anarsa,
da kurakurai da gargaɗi tare da jagora zuwa gyara.

Ga **Digital Link**, kowane katin AI yana faɗi matsayinsa na tsari kuma — **maɓallin farko**,
**mai iyakance maɓalli** ko **halayen bayanai** — da launuka bisa tsarin bayyanarsu a cikin URI.
Waɗannan matsayin suna bayyana a Digital Link kaɗai: a cikin igiyar abubuwa mai sauƙi AI ba shi
da wuri a URI da za a bayyana ko kaɗan.

Rahoton yana bin harshe da jigon da ka zaɓa.

### Fitarwa

| | |
|---|---|
| **Ajiye rahoto → A matsayin HTML…** (<kbd>⌘S</kbd>) | Yana rubuta abin da ke kan allo daidai: fayil mai tsayawa da kansa da ke buɗewa a kowane mai bincike |
| **Ajiye rahoto → A matsayin PDF…** (<kbd>⌘P</kbd>) | Yana buga rahoton ta akwatin buga na tsarin — zaɓi inda PDF zai je a can |

PDF yana wucewa ta akwatin buga domin a can ake zaɓen wuri da sunan fayil: a kan macOS
**PDF ▾ → Save as PDF**, a kan Windows **Microsoft Print to PDF**, a kan Linux **Print to File**.
Ana raba shafuka yadda ya kamata kuma rubutun yana nan ana iya zaɓensa.

Buga da PDF suna amfani da jigon haske koyaushe, ko da manhajar tana cikin jigo mai duhu, kuma
suna riƙe alamun AI masu launi da mashayar hukunci kamar yadda suke.

Idan babu wata na'urar buga ko wurin PDF a kan na'urarka, manhajar tana faɗa maka kuma tana
nuna maka fitarwar HTML, wadda ake iya buga ta daga kowane mai bincike.

---

## Zaɓuɓɓukan bincike

Fanel ɗin dama yana tattara dukan zaɓuɓɓukan injin a wuri ɗaya. Da canza wani abu, **ana sake
tantance rikodin da aka zaɓa nan take**, don haka wannan fanel ba ya shafar sikan na gaba kaɗai —
hanya ce kuma ta tambaya: “Yaya wannan shigarwa za ta kasance da waɗannan saitunan?”.

| Zaɓi | Ƙimomi |
|---|---|
| Yanayin bincike | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — zurfin da bincike zai kai |
| Harshe | Kundin saƙo 35 |
| Tsari | `LITTLE` (dd/mm/yyyy), `MIDDLE` (mm/dd/yyyy), `BIG` (yyyy/mm/dd) |
| Mai raba | `SLASH`, `HYPHEN`, `PERIOD` |
| Wata | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Shekara | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Tsallake binciken "requires" | Yana daina ƙin AI da abokinsa na wajibi ba ya nan (`GE-S005`) |
| Tsallake binciken "excludes" | Yana daina ƙin ma'auratan AI da ƙa'idar ta hana (`GE-S006`) |

A ƙasan fanel, *Mayar da tsoffin saituna* yana dawo da ƙimomin tsoho na injin da kansa.

### Gudanarwa ta farko

Muddin ba a ajiye komai ba tukuna, manhajar tana ɗaukar saitunanta daga **saitunan yankin
na'urarka** maimakon tsoffin injin:

- **Harshe** yana zuwa daga wurin tsarin, kuma yana komawa Turanci duk inda babu kundi;
- **Zaɓuɓɓukan rana** ana karanta su daga gajerin tsarin ranar wurinka: tsarin sassan, mai
  rabawa, da ko ana rubuta wata da shekara a taƙaice ko a'a.

> **Ka lura.** Don haka wurin da ke rubuta gajerun ranaku da shekara mai lamba biyu zai fara a
> nan ma da shekara mai lamba biyu: wannan ya dace da sauran tsarinka, amma ya bambanta da tsoho
> na injin da kansa mai lamba huɗu. Canza shi a fanel, sai zaɓin ya kasance.

---

## Harsuna

Zaɓen harshe ba ya canza saƙonnin kuskure kaɗai, yana canza **dukan manhajar** — menu, maɓallai,
edita, fanel ɗin zaɓuɓɓuka, layin matsayi da rahoto. Harsunan da ake rubutawa daga dama zuwa hagu
suna juya taga da rahoto.

Dukan harsuna 35 suna nan, da rufi mai matakai biyu:

| | Rufi |
|---|---|
| Rahotannin bincike da dukan saƙonnin kuskuren injin | **Dukan harsuna 35** |
| Menu, maɓallai da lakabin manhajar da kanta | **Dukan harsuna 35** |

Harsunan da ake rubutawa daga dama zuwa hagu — Larabci, Larabcin Masar, Farisanci da Urdu — suna
juya taga da rahoto tun daga lokacin da manhajar ta buɗe.

Ana tuna harshe don lokutan gaba. Don buɗewa da wani harshe sau ɗaya kaɗai, duba
[`--language`](#gudanarwa).

Akwai abubuwan da ba a fassara su da gangan a kowane harshe: gajerun sunayen filaye da ake buga
tare da ɗanyen ƙimomi (`format`, `position`, `raw`, `len` …) — waɗannan masu ganowa ne ba
kalmomi ba; sunayen sifofi da alamomi — waɗannan kalmomin GS1 da masana'antu ne; da doguwar sakin
layi daga ƙa'idar, wadda fassarar rabi za ta rikitar da fiye da yadda za ta bayyana.

---

## Abin da ake tunawa

Fayiloli biyu a cikin `.gaia-desktop` a cikin babban fayil ɗinka na gida:

| Fayil | Abin da ke ciki |
|---|---|
| `options.properties` | Dukan zaɓuɓɓukan bincike, jigo, ko fanel ɗin zaɓuɓɓuka a buɗe yake, sikanan da aka yi amfani da shi na ƙarshe, da yanayin da aka bar taga a ciki |
| `captures.tsv` | Tarihin rikodi, sabbi da farko, iyaka 500 |

Duk lokacin da ka canza wani zaɓi ana rubuta shi; duk lokacin da tarihi ya canza ana rubuta shi.
Ana sake rubuta su biyun lokacin fita.

Share ɗaya daga cikin fayilolin biyu yana dawo da wannan sashe kaɗai zuwa halinsa na farko kuma
babu wani abu — saboda haka aka raba su, don kada tarihi da ya lalace ya ja saitunanka tare da
shi. Har fayil da gyara da hannu ya lalata ba abin kisa ba ne: kowane saiti yana komawa tsohonsa
shi kaɗai, don haka farashin layi ɗaya mara kyau bai wuce wannan layi ba.

Tarihi yana ajiye shigarwa ba bincike ba. Ana sake tantance shi lokacin ɗaukewa, wato rikodin da
aka dawo da shi yana nuna zaɓuɓɓukan da ke aiki **yanzu**, ba waɗanda suke aiki lokacin da aka
rikodin sa ba.

---

## Gajerun hanyoyin madannai

| | |
|---|---|
| <kbd>Enter</kbd> | Tantance layin shigarwa |
| <kbd>⌘O</kbd> | Buɗe fayil ɗin rubutu |
| <kbd>⌘S</kbd> | Ajiye rahoto a matsayin HTML |
| <kbd>⌘P</kbd> | Ajiye rahoto a matsayin PDF (akwatin buga na tsarin) |
| <kbd>⌘G</kbd> | Saka alamar mai raba FNC1 |
| <kbd>⌘K</kbd> | Haɗa / Cire sikana (Windows kaɗai) |
| <kbd>⌘D</kbd> | Sauya jigo mai duhu |
| <kbd>⌘Q</kbd> | Fita |

A cikin edita: <kbd>Tab</kbd> / <kbd>Shift</kbd>+<kbd>Tab</kbd> suna motsawa tsakanin filayen da
ake gyarawa, <kbd>Enter</kbd> yana tabbatarwa, <kbd>Esc</kbd> yana sokewa.

---

## Warware matsaloli

| Alama | Dalili da magani |
|---|---|
| Manhajar ba ta tashi ba, ko tana ba da `NoClassDefFoundError` | Dole babban fayil ɗin `lib/` ya kasance kusa da fayil ɗin JAR. Ka ajiye su tare, ko ka yi amfani da rabon mai tsayawa da kansa |
| A na'urar wasiƙa lokacin farawa *“Unsupported JavaFX configuration…”* | Ana tsammaninsa kuma ba shi da illa — kayan aikin kawai yana faɗi yadda aka ɗauke shi. Manhajar tana aiki kamar yadda aka saba |
| Mai rabawa da ka liƙa kamar ya ɓace | Haka ya kamata: yana juyawa zuwa alamar da ake gani `{GS}`, kuma mai tantancewa yana samun ainihin harafin. Duba [alamar {GS}](#layin-shigarwa-da-alamar-gs) |
| Maɓallin *Haɗa sikana* ba ya nan ko kaɗan | Kana kan macOS ko Linux, inda babu direban sikana. Duba [Sikanin](#sikanin) |
| Sikanan ba ya haɗuwa | Duba ko sunan da ke jerin ya dace da samfurinka, ko sikanan a haɗe yake, kuma ko wata manhaja ba ta riƙe shi ba |
| Fitarwar PDF tana cewa babu inda za a je | Babu na'urar buga ko wurin PDF a na'urarka. Ƙara ɗaya, ko fitar da HTML ka buga daga mai bincike |
| A wani abu na menu ana ganin wani abu kamar `act.hri` | Lakabi ya ɓace — don Allah ka bayar da rahoto; wannan ba saiti ba ne, matsala ce ta lokacin gina |

---

## Iyaka

- **Sikanin a kan Windows kaɗai**, domin direbobin a can suke.
- **PDF yana wucewa ta akwatin buga**, ba ta akwatin ajiyewa ba.
- **Manufofin Digital Link suna karɓar yanki da gabatarwar hanya kaɗai**; babu ƙarin sigogin
  tambaya ko sassa.
- **AI yana bayyana sau ɗaya kaɗai** a cikin igiyar abubuwa — wannan ƙa'idar GS1 ce, ba iyakar
  manhajar ba.
