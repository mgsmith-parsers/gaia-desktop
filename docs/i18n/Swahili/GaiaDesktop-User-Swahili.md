# GAIA Desktop — Mwongozo wa mtumiaji

Hati hii inaeleza jinsi ya kutumia programu. Kwa kuijenga, kuibadilisha au kuongeza lugha mpya,
angalia [mwongozo wa msanidi](../../GaiaDesktop-Developer.md) (kwa Kiingereza). Hati asilia:
[GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Yaliyomo

1. [Hii ni nini](#hii-ni-nini)
2. [Kuendesha](#kuendesha)
3. [Dirisha](#dirisha)
4. [Hali ya uchanganuzi](#hali-ya-uchanganuzi)
   - [Upau wa ingizo na tokeni {GS}](#upau-wa-ingizo-na-tokeni-gs)
   - [Kuskani](#kuskani)
   - [Kufungua faili](#kufungua-faili)
   - [Historia ya rekodi](#historia-ya-rekodi)
5. [Hali ya kutengeneza](#hali-ya-kutengeneza)
   - [Kihariri cha AI](#kihariri-cha-ai)
   - [Kile ambacho kihariri hakikubali](#kile-ambacho-kihariri-hakikubali)
   - [Mfuatano wa elementi au Digital Link](#mfuatano-wa-elementi-au-digital-link)
   - [Kuhariri rekodi iliyopo](#kuhariri-rekodi-iliyopo)
6. [Hali ya kuunganisha](#hali-ya-kuunganisha)
   - [Upau wa kuunganisha](#upau-wa-kuunganisha)
   - [Kile ambacho kuunganisha hakukubali](#kile-ambacho-kuunganisha-hakukubali)
   - [Nakala na migongano](#nakala-na-migongano)
   - [Ripoti ya muunganiko](#ripoti-ya-muunganiko)
7. [Ripoti](#ripoti)
   - [Uhamishaji](#uhamishaji)
8. [Machaguo ya uchanganuzi](#machaguo-ya-uchanganuzi)
   - [Kuendesha kwa mara ya kwanza](#kuendesha-kwa-mara-ya-kwanza)
9. [Lugha](#lugha)
10. [Kile kinachokumbukwa](#kile-kinachokumbukwa)
11. [Njia za mkato za kibodi](#njia-za-mkato-za-kibodi)
12. [Kutatua matatizo](#kutatua-matatizo)
13. [Mipaka](#mipaka)

---

## Hii ni nini

GAIA Desktop hufanya mambo matatu na data ya GS1:

- **Uchanganuzi** — andika mfuatano wa elementi wa GS1 au URI ya Digital Link, au ubandike, au
  uifungue kutoka faili, au uiskani, kisha usome matokeo kama ripoti kamili: kila kitambulisho
  cha programu (AI) kinamaanisha nini, hitilafu iko wapi, na kwa nini;
- **Kutengeneza** — kuunganisha jozi za AI na thamani ili kupata mfuatano wa elementi au URI ya
  Digital Link;
- **Unganisha** — kuunganisha hadi misimbopau mitatu ya GS1-128 ya lebo moja kuwa mfuatano
  mmoja wa elementi ambao GS1 DataMatrix ingebeba, kuukagua kwa ujumla, na kuchora alama.

Kila kitu ambacho programu hii inajua kuhusu GS1 — maana ya kila AI, kila kanuni ya ukaguzi, kila
ujumbe wa hitilafu na kila undani wa lugha — hutoka kwenye injini ya GAIA inayoendesha chini yake.
Programu haiongezi kanuni zake mwenyewe: inakubali hasa kile ambacho injini inakubali.

---

## Kuendesha

```bash
java -jar gaia-desktop.jar
```

Faili hii ya JAR inahitaji saraka ya `lib/` inayosafirishwa pamoja nayo — ziweke pamoja, la
sivyo programu haitaanza. (Faili ya JAR inayojitosheleza pia inaweza kujengwa; angalia
[mwongozo wa msanidi](../../GaiaDesktop-Developer.md#packaging).)

Inahitajika **Java 21 au mpya zaidi**. Usambazaji hujengwa kwa mfumo mmoja wa uendeshaji na
usanifu mmoja wa kichakataji, kwa hivyo tumia faili ya JAR iliyojengwa kwa mashine yako.

Kufungua programu kwa lugha fulani bila kubadilisha lugha iliyohifadhiwa:

```bash
java -jar gaia-desktop.jar --language=FR
```

Thamani inaweza kuwa msimbo wa katalogi (`FR`, `PCM`, `ARZ`), jina kamili (`FRENCH`,
`WU-CHINESE`), au lebo ya lugha (`fr`; kwa Kikantoni `zh-HK`, kwa Kiarabu cha Misri `ar-EG`) —
herufi kubwa na ndogo hazileti tofauti. Amri hii inatumika kwa mzunguko huo tu: lugha yako
iliyohifadhiwa inabaki kama ilivyo, kwa hivyo njia ya mkato unayomtengenezea mwenzako haiwezi
kubadilisha lugha ambayo nakala yako mwenyewe hufunguka nayo. Lakini kuchagua lugha *ndani* ya
programu ni chaguo halisi na huhifadhiwa kama kawaida. Thamani isiyoashiria lugha yoyote hupuuzwa
kwa dokezo kwenye konsoli, na programu hufunguka kwa lugha iliyohifadhiwa badala ya kukataa
kuendesha.

Wakati wa kuanza, konsoli huchapisha mstari mmoja wenye toleo la programu, tarehe ya kuijenga na
toleo la injini. Mstari huohuo ndio unaopaswa kuambatishwa katika ripoti yoyote ya tatizo.

---

## Dirisha

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

> Kama vitalu vingine vyote vya msimbo katika hati hii, mchoro huu unabaki na lebo za Kiingereza.
> Kwenye skrini utaona *Changanua* / *Tengeneza* / *Unganisha*, *Rekodi*, *Hifadhi ripoti*, *Machaguo*,
> *skana: imezimwa* na *Kuhusu*.

**Kwenye macOS na Linux mstari wa skana wa mchoro huu haupo kabisa** — hakuna kitufe kwenye upau
wa vidhibiti, hakuna orodha ya kuchagua kifaa, hakuna menyu ya *Skana*, wala *skana: imezimwa*
kwenye upau wa hali. Angalia [Kuskani](#kuskani).

Kibadilishi cha **Changanua ⇄ Tengeneza** hubadilisha tu upau ulio chini ya upau wa vidhibiti na
vidhibiti vinavyohusu rekodi. Historia, ripoti, paneli ya machaguo na vitendo vyote vya uhamishaji
hufanya kazi katika hali zote mbili, kwa sababu utengenezaji uliofaulu nao huwa rekodi nyingine
tu. Hali ya **Unganisha** ina mpangilio tofauti: angalia [Hali ya kuunganisha](#hali-ya-kuunganisha).

Paneli ya machaguo **imefungwa wakati wa kuendesha kwa mara ya kwanza**. Hali, kuonekana au
kutoonekana kwa paneli, mandhari na skana iliyotumika mara ya mwisho — vyote hukumbukwa kwa mara
ijayo.

**Matoleo mapya.** Sekunde chache baada ya kuanza, mara moja kwa siku zaidi, programu huiuliza GitHub
kama GAIA Desktop mpya zaidi imetolewa. Ikiwa imetolewa, kiungo huonekana kwenye upau wa hali —
*GAIA Desktop 1.1.0 inapatikana* — ambacho hufungua ukurasa wa toleo kwenye kivinjari chako; hakuna
kinachopakuliwa wala kusakinishwa kwa niaba yako. *Ruka toleo hili* huficha arifa hadi toleo la
baadaye. Ukaguzi hubaki kimya wakati hakuna jipya au GitHub haifikiki.
*Msaada → Angalia masasisho…* hukagua papo hapo na kueleza kilichopatikana;
*Msaada → Angalia masasisho kiotomatiki* huzima na kuwasha tena ukaguzi wa kila siku.

---

## Hali ya uchanganuzi

### Upau wa ingizo na tokeni {GS}

Upau huu hukubali mfuatano wa elementi, URI ya GS1 Digital Link, au skani ghafi yenye kiambishi
cha AIM Code ID (`]d2…`). <kbd>Enter</kbd> huanzisha uchanganuzi.

Kinachotenganisha AI zenye urefu unaobadilika ni **FNC1** — herufi ya udhibiti isiyoonekana
`0x1D`. Hakuna sehemu ya maandishi inayoweza kushikilia herufi hii: hufutwa kimyakimya, na
`01…` + FNC1 + `10LOT` hufika huko kama `01…10LOT` — yaani mfuatano wa elementi wenye maana
iliyobadilika.

Ndiyo maana programu huonyesha kitenganishi kama tokeni inayoonekana **`{GS}`** na kukirudisha
kilivyo kikiwa njiani kwenda kwa kichanganuzi:

- **Kitufe cha `{GS}`** huweka tokeni pale ulipo mshale;
- Unapo**bandika** herufi halisi za kutenganisha — kutoka kibodi, kutoka menyu ya muktadha au kwa
  kuburuta na kuachia — hujigeuza zenyewe kuwa `{GS}`, kwa hivyo hakuna kinachopotea;
- Tokeni huandikwa hasa jinsi ripoti inavyoiandika, kwa hivyo thamani iliyonakiliwa kutoka ripoti
  inaweza kubandikwa moja kwa moja.

Rekodi, ripoti na historia iliyohifadhiwa — vyote vitatu hubeba kitenganishi **halisi**. Tokeni ni
kile tu unachokiona na kukiandika.

### Kuskani

> **Windows pekee.** Zebra hutoa viendeshi vya skana zake kwa Windows pekee, kwa hivyo kwenye
> macOS na Linux programu haitengenezi kidhibiti chochote cha skana — hakuna kitufe cha
> *Unganisha skana*, hakuna orodha ya vifaa, hakuna menyu ya *Skana*, hakuna hali ya skana kwenye
> upau wa hali. Hakuna kinachoonyeshwa kikiwa kimefifia, kwa sababu hakuna mpangilio wala kifaa
> kinachoweza kukiwasha. Kila kitu kingine, ikiwa ni pamoja na kufungua faili iliyojaa skani
> zilizohifadhiwa, hufanya kazi kila mahali.

*Unganisha skana* hufungua kifaa kilichotajwa kwenye orodha; `ZebraAllScanners` ndiyo thamani
chaguo-msingi na inafaa kwa modeli nyingi. Kila skani huchanganuliwa na kuongezwa kwenye historia
yenyewe.

Data ya skana husomwa baiti kwa baiti, kwa hivyo kiambishi cha AIM Code ID na kila kitenganishi
cha FNC1 hufika kwa kichanganuzi kwa sura ileile skana ilivyovituma.

Katika hali ya kutengeneza vidhibiti vya skana hufichwa, lakini skana iliyounganishwa huendelea
kufanya kazi: *Skana → Unganisha / Tenganisha* (<kbd>⌘K</kbd>) bado huibadilisha, upau wa hali
bado huarifu, na skani inayowasili wakati kihariri kimefunguliwa nayo hurekodiwa. Katika hali ya Unganisha vidhibiti hivi hubaki, na kila skani huongezwa kwenye muunganiko badala
ya historia.

Kuweka skana kwa mara ya kwanza — Zebra Scanner SDK, hali ya USB ya skana, na mipangilio ya GS1 —
kumeelezwa hatua kwa hatua katika [mwongozo wa kuweka skana ya Zebra](GaiaDesktop-Zebra-Scanner-Windows-Swahili.md), unaokuja katika kila
zip chini ya `docs/`. Ikiwa hakuna kifaa kinachounganishwa, angalia [Kutatua matatizo](#kutatua-matatizo).

### Kufungua faili

*Faili → Fungua faili ya maandishi…* huchanganua **kila mstari usio tupu kama rekodi yake
mwenyewe** — hii ndiyo njia ya haraka zaidi ya kushughulikia rundo la skani ambazo mtu amekutumia.
Faili husomwa baiti kwa baiti, kwa hivyo vitenganishi halisi vilivyomo ndani yake navyo hubaki
salama.

### Historia ya rekodi

Mpya juu. Kila mstari huonyesha nukta ya halali / batili, muhtasari wa mstari mmoja, na pembeni
yake wakati wa rekodi, chanzo chake na idadi ya AI zilizomo. Tarehe huonekana tu kwenye rekodi za
siku zilizopita; mistari ya leo hubeba wakati pekee.

Muhtasari kwa Digital Link ni **URI ya Digital Link**, na kwa kingine chochote ni **HRI**: kwa
mfuatano wa elementi, kusoma `(01)09506000134352 (17)261231` ni rahisi kuliko
`01095060001343521726123110…`.

Kwa kubofya kulia rekodi unaweza kunakili ingizo lake au HRI yake, kuirudisha kwenye upau wa
ingizo, kuifungua katika kihariri, kuiondoa, au kufuta historia yote.

---

## Hali ya kutengeneza

### Kihariri cha AI

Jedwali la jozi za AI na thamani. Andika msimbo wa AI, au chagua kutoka orodha ya
*Ongeza AI…* miongoni mwa **AI zote ambazo injini inazijua**; unapoandika, safu za **Kichwa**,
**Umbizo** na **Maelezo** hujazwa kwa lugha uliyochagua. Orodha haitoi AI ambayo itakataliwa
wakati wa kutengeneza.

Mwishoni mwa jedwali daima kuna **safu moja tupu**: andika kitu ndani yake na inayofuata
huonekana, kwa hivyo hutahitaji kamwe *Ongeza safu* kwa mfuatano wenye AI nyingi.

Kuhariri hufanya kama jedwali linavyopaswa kufanya: <kbd>Enter</kbd>, <kbd>Tab</kbd>,
<kbd>Shift</kbd>+<kbd>Tab</kbd> na kubofya mahali pengine — vyote **huhifadhi** ulichoandika;
<kbd>Esc</kbd> hukiacha. <kbd>Tab</kbd> husogea kutoka AI hadi thamani kisha hadi safu inayofuata,
ikiruka safu zisizohirikika.

### Kile ambacho kihariri hakikubali

| | Kinachotokea |
|---|---|
| AI ambayo injini haiijui | Safu hugeuka nyekundu unapoandika; kutengeneza hukataliwa kwa maelezo |
| AI ileile mara ya pili kutoka orodha | Haiongezwi — ujumbe hutaja jina lake, na safu iliyonayo tayari huchaguliwa |
| AI ileile mara ya pili kwa kuandika kwa mkono | Safu zote mbili hutiwa alama; kutengeneza hukataliwa kwa hitilafu `GE-S004` |
| Hakuna cha kutengeneza | Ujumbe mmoja; hakuna rekodi inayotengenezwa |

AI huweza kutokea mara moja tu katika mfuatano wa elementi — ndiyo maana orodha hukataa papo hapo
badala ya kukuacha ujaribu utengenezaji ambao tayari umeshindwa.

### Mfuatano wa elementi au Digital Link

**Tengeneza mfuatano wa elementi** huunganisha safu pamoja na kuweka kitenganishi cha FNC1 kila
mahali kiwango kinapohitaji. **Tengeneza Digital Link** hutoa URI; ili kuielekeza kwa kisuluhishi
chako badala ya `id.gs1.org` cha kawaida, fungua *Lengo la Digital Link*:

| Sehemu | Athari yake | Kuiacha tupu kunamaanisha |
|---|---|---|
| Kikoa | mwenyeji wa kisuluhishi, kama `example.com` | `id.gs1.org` |
| Kiambishi cha njia | sehemu kabla ya njia ya AI, kama `/products` | Hakuna kiambishi |

Ikiwa **imefaulu**, matokeo hufanya kama skani hasa: huchanganuliwa, hujiunga na historia,
huonyeshwa kama ripoti na huhifadhiwa. Utengenezaji hukagua matokeo yake kwa kuyachanganua, kwa
hivyo kile kinachorudishwa lazima kiwe halali.

Ikiwa **imeshindwa**, hakuna kinachorekodiwa. Sababu hubaki chini ya vitufe vya kutengeneza —
mstari mmoja kwa kila tatizo, kila mmoja ukiwa na hitilafu, AI inayohusika na maelezo ya injini
yenyewe:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> Alama za nukuu mbili katika ujumbe huo hazitoki kwenye programu hii, bali kwenye katalogi ya
> hitilafu ya injini yenyewe.

### Kuhariri rekodi iliyopo

Kubofya kulia rekodi → **Tengeneza** huileta kwenye kihariri cha AI. Badilisha thamani, tengeneza
tena, na matokeo huonekana kama rekodi mpya — rekodi asilia hubaki kama ilivyo.

---

## Hali ya kuunganisha

Lebo za zamani mara nyingi hugawa data zao katika **misimbopau kadhaa ya GS1-128** — GTIN katika
mmoja, tarehe ya kuisha na bechi katika mwingine — kwa sababu msimbopau mmoja wa mstari hauwezi kubeba
kila kitu. Hali ya Unganisha huchukua hadi misimbopau **mitatu** ya aina hiyo, kuiunganisha kuwa
mfuatano mmoja wa elementi ambao **GS1 DataMatrix** ingebeba, na kukagua matokeo kwa ujumla. Matokeo
yakiwa halali, programu huchora alama ya DataMatrix.

Hakuna msimbopau kati ya hiyo unaohitaji kukamilika peke yake. Nambari ya bechi bila GTIN yake hushindwa
peke yake kwa `GE-S005`, lakini hilo ni kawaida kwa msimbopau mmoja wa seti, kwa hiyo hali ya
Unganisha huikagua kwenye muunganiko.

### Upau wa kuunganisha

Kuhamia **Unganisha** hubadilisha upau wa ingizo kwa upau ulio kando ya chini ya dirisha.
Historia ya rekodi hukaa pembeni, kwa sababu skani za hali ya Unganisha si rekodi na kamwe
haziingii humo. Eneo la ripoti huonyesha ripoti ya muunganiko badala yake:

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

Kuna njia mbili za kuingiza msimbopau:

- **Kuuskani.** Kwenye Windows, vidhibiti vya skana hubaki kwenye upau wa vidhibiti katika hali ya
  Unganisha, na kila skani huenda moja kwa moja kwenye muunganiko, si kwenye historia.
- **Kuuandika au kuubandika** katika sehemu ya *Msimbopau* kisha kubonyeza
  <kbd>Enter</kbd> au **Ongeza**. Andika FNC1 kama **`{GS}`**, kama kwenye upau wa ingizo.
  Msimbopau ulioandikwa bila AIM Code ID huchukuliwa kuwa GS1-128.

| Kitufe | Kazi |
|---|---|
| **Ongeza** | Huongeza msimbopau ulio katika sehemu |
| **Ondoa wa mwisho** | Huondoa msimbopau wa hivi karibuni zaidi (haupatikani wakati hakuna) |
| **Weka upya** | Huondoa misimbopau yote, husafisha sehemu na kukataliwa kokote, na kuanza upya |

Upau wa hali daima huonyesha misimbopau mingapi imeshikiliwa (*Misimbopau 2 kati ya 3 ya GS1-128*), au kwa nini
wa mwisho ulikataliwa.

### Kile ambacho kuunganisha hakukubali

Msimbopau uliokataliwa hauongezwi. Ripoti na upau wa hali vyote hueleza sababu:

| Msimbopau | Hukataliwa kwa sababu |
|---|---|
| Sehemu tupu | Hakuna cha kuongeza |
| Msimbopau wa nne | Lebo huunganishwa kutoka mitatu zaidi. Ondoa wa mwisho, au weka upya |
| Alama nyingine, kwa AIM Code ID yake | GS1-128 pekee ndiyo huunganishwa, kwa hiyo GS1 DataMatrix (`]d2`) au QR Code hukataliwa, pamoja na jina lake |
| AI zisizosomeka | Hitilafu ya sintaksia; msimbo wa hitilafu na ujumbe wa injini huonyeshwa |
| Hakuna AI kabisa | Hakuna cha kuunganisha |
| Uliokwisha kuskaniwa | Mfuatano uleule wa elementi mara mbili, kwa mfano msimbopau uleule umeskaniwa tena; ripoti hueleza unarudia msimbopau upi wa awali |

Hali ya Unganisha hukataa tu kile isichoweza kutumia kabisa. Kila kitu kingine (tarakimu hundi
isiyo sahihi, tarehe batili, AI mwandamani inayokosekana) hukubaliwa na kupimwa kwenye muunganiko.

### Nakala na migongano

AI huunganishwa kwa mpangilio zilivyoskaniwa. Misimbopau miwili inapobeba AI ileile:

- **yenye thamani ileile**, huwekwa mara moja na kuorodheshwa chini ya *Nakala — zimewekwa mara moja*,
  pamoja na misimbopau ilimosomwa;
- **yenye thamani tofauti**, huo ni **mgongano**. Uamuzi husema *MGONGANO*, thamani
  zinazogongana huorodheshwa msimbopau kwa msimbopau, na **hakuna kinachounganishwa** hadi utatuliwe.
  Ondoa msimbopau wenye kosa, au weka upya.

### Ripoti ya muunganiko

Kutoka juu hadi chini:

1. **Uamuzi**: *hakuna skani*, *MGONGANO*, halali au batili, na misimbopau mingapi
   imeshikiliwa.
2. **Skani**: kila msimbopau kama ulivyofika, alama yake, na mfuatano wa elementi unaobaki baada ya
   kuondoa AIM Code ID. Tatizo ambalo muunganiko pekee unaweza kuamua huonyeshwa kwenye msimbopau huo,
   likiwa na alama *inapimwa kwenye muunganiko*.
3. **Nakala** na **migongano**, ikiwa ipo.
4. **Muunganiko**: mfuatano wa elementi, HRI yake na urefu wake, kisha ingizo lililounganishwa
   kama utepe wenye lebo na kadi moja kwa kila AI, vyote vimepangwa kama katika
   [ripoti ya uchanganuzi](#ripoti). Kisha kila hitilafu na onyo juu ya muunganiko huorodheshwa
   chini ya *Uthibitisho*.
5. **Alama ya GS1 DataMatrix**, pamoja na HRI yake chini. Huchorwa tu muunganiko ukiwa halali. Vinginevyo
   ripoti hueleza kwa nini haikuchorwa, ili alama batili isichapishwe kwa makosa.

Kwa mfano, lebo yenye misimbopau miwili:

| Msimbopau | Hubeba | Peke yake |
|---|---|---|
| `]C10109506000134352` | (01) GTIN | halali |
| `]C11726123110LOT-ABC` | (17) tarehe ya kuisha, (10) bechi | `GE-S005` kwa AI zote mbili, inapimwa kwenye muunganiko |

Hiyo huunganika kuwa `01095060001343521726123110LOT-ABC`, HRI
`(01)09506000134352 (17)261231 (10)LOT-ABC`, ambao ni halali, kwa hiyo alama huchorwa. AI `17` ina urefu
maalum, kwa hiyo hakuna FNC1 inayohitajika baada yake. Hali ya Unganisha huingiza moja popote
kiwango kinapotaka.

Mara tu msimbopau mmoja unaposhikiliwa, **Hifadhi ripoti** husafirisha ripoti ya muunganiko kama HTML
au PDF, sawa na ripoti ya uchanganuzi (angalia [Kusafirisha](#uhamishaji)). Jina la faili ya HTML huanza
na `gaia-combine-`.

Ripoti hufuata lugha na mandhari uliyochagua. Kubadilisha chaguo la uchanganuzi au lugha hukagua upya
misimbopau iliyoshikiliwa. Hudumu pia baada ya kuhamia Changanua au Tengeneza na kurudi,
lakini si baada ya kuanzisha upya: misimbopau ya hali ya Unganisha haihifadhiwi.

---

## Ripoti

Rekodi iliyochaguliwa huonyeshwa kama ripoti kamili: upau wa uamuzi, mfuatano wa elementi
uliogawanywa katika “mikanda” yenye majina, metadata ya uchanganuzi, kadi moja kwa kila AI pamoja
na maana yake, na hitilafu na maonyo pamoja na mwongozo wa kurekebisha.

Kwa **Digital Link**, kila kadi ya AI hutaja pia jukumu lake la kimuundo — **ufunguo msingi**,
**kifafanuzi cha ufunguo** au **sifa ya data** — kwa rangi zinazofuata mpangilio wanaotokea nao
katika URI. Majukumu haya huonekana katika Digital Link pekee: katika mfuatano wa elementi wa
kawaida AI haina nafasi yoyote katika URI ya kuelezwa.

Ripoti hufuata lugha na mandhari uliyochagua.

### Uhamishaji

| | |
|---|---|
| **Hifadhi ripoti → Kama HTML…** (<kbd>⌘S</kbd>) | Huandika hasa kile kilicho kwenye skrini: faili inayojitosheleza inayofunguka katika kivinjari chochote |
| **Hifadhi ripoti → Kama PDF…** (<kbd>⌘P</kbd>) | Huchapisha ripoti kupitia kisanduku cha uchapishaji cha mfumo — chagua lengo la PDF hapo |

PDF hupitia kisanduku cha uchapishaji kwa sababu mahali na jina la faili huchaguliwa hapo: kwenye
macOS **PDF ▾ → Save as PDF**, kwenye Windows **Microsoft Print to PDF**, kwenye Linux
**Print to File**. Kurasa hugawanywa ipasavyo na maandishi hubaki yanayoweza kuteuliwa.

Uchapishaji na PDF hutumia mandhari angavu daima, hata kama programu iko katika mandhari meusi, na
huhifadhi alama za AI zenye rangi na upau wa uamuzi kama zilivyo.

Ikiwa mashine yako haina printa wala lengo la PDF lililosanidiwa, programu hukuambia hivyo na
kukuelekeza kwenye uhamishaji wa HTML, unaoweza kuchapishwa kutoka kivinjari chochote.

---

## Machaguo ya uchanganuzi

Paneli ya kulia hukusanya machaguo yote ya injini mahali pamoja. Ukibadilisha kitu chochote,
**rekodi iliyochaguliwa huchanganuliwa upya papo hapo**, kwa hivyo paneli hii haiathiri skani
inayofuata pekee — pia ni njia ya kuuliza: “Ingizo hili litaonekanaje kwa mipangilio hii?”.

| Chaguo | Thamani |
|---|---|
| Hali ya uchanganuzi | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — uchanganuzi uende kwa kina kiasi gani |
| Lugha | katalogi 35 za ujumbe |
| Mpangilio | `LITTLE` (dd/mm/yyyy), `MIDDLE` (mm/dd/yyyy), `BIG` (yyyy/mm/dd) |
| Kitenganishi | `SLASH`, `HYPHEN`, `PERIOD` |
| Mwezi | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Mwaka | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Ruka ukaguzi wa "requires" | Huacha kukataa AI ambayo mwenzake wa lazima hayupo (`GE-S005`) |
| Ruka ukaguzi wa "excludes" | Huacha kukataa jozi za AI ambazo kiwango kinazikataza (`GE-S006`) |

Chini ya paneli, *Rejesha chaguo-msingi* hurudisha thamani chaguo-msingi za injini yenyewe.

### Kuendesha kwa mara ya kwanza

Maadamu hakuna kilichohifadhiwa bado, programu huchukua mipangilio yake kutoka **mipangilio ya
eneo ya mashine yako** badala ya chaguo-msingi za injini:

- **Lugha** hutoka kwenye eneo la mfumo, na hurudi Kiingereza pale ambapo hakuna katalogi;
- **Machaguo ya tarehe** husomwa kutoka umbizo fupi la tarehe la eneo lako: mpangilio wa sehemu,
  kitenganishi, na iwapo mwezi na mwaka huandikwa kwa kifupi au la.

> **Zingatia.** Kwa hivyo eneo linaloandika tarehe fupi kwa mwaka wa tarakimu mbili litaanza hapa
> nalo kwa mwaka wa tarakimu mbili: hii inalingana na sehemu nyingine za mfumo wako, lakini
> inatofautiana na chaguo-msingi la injini lenyewe la tarakimu nne. Libadilishe kwenye paneli, na
> chaguo litadumu.

---

## Lugha

Kuchagua lugha hakubadilishi ujumbe wa hitilafu pekee, bali **programu nzima** — menyu, vitufe,
kihariri, paneli ya machaguo, mstari wa hali na ripoti. Lugha zinazoandikwa kutoka kulia kwenda
kushoto hugeuza dirisha na ripoti.

Lugha zote 35 zinapatikana, kwa ufunikaji wa tabaka mbili:

| | Ufunikaji |
|---|---|
| Ripoti za uchanganuzi na ujumbe wote wa hitilafu wa injini | **Lugha zote 35** |
| Menyu, vitufe na lebo za programu yenyewe | **Lugha zote 35** |

Lugha zinazoandikwa kutoka kulia kwenda kushoto — Kiarabu, Kiarabu cha Misri, Kiajemi na Kiurdu —
hugeuza dirisha na ripoti tangu wakati ule programu inapofunguka.

Lugha hukumbukwa kwa mara zijazo. Kufungua kwa lugha nyingine mara moja tu, angalia
[`--language`](#kuendesha).

Kuna vitu ambavyo havitafsiriwi kwa makusudi katika lugha yoyote: majina mafupi ya sehemu
yanayochapishwa pamoja na thamani ghafi (`format`, `position`, `raw`, `len` …) — hivi ni
vitambulisho, si maneno; majina ya mifumo na alama — hivi ni msamiati wa GS1 na wa sekta; na
aya ndefu moja kutoka kwa kiwango, ambayo tafsiri ya nusu ingechanganya zaidi kuliko kufafanua.

---

## Kile kinachokumbukwa

Faili mbili katika `.gaia-desktop` ndani ya saraka yako ya nyumbani:

| Faili | Kilichomo |
|---|---|
| `options.properties` | Machaguo yote ya uchanganuzi, mandhari, iwapo paneli ya machaguo imefunguliwa, skana iliyotumika mara ya mwisho, hali ambayo dirisha liliachwa nayo, na mipangilio ya ukaguzi wa masasisho — umewashwa au la, ulipoendeshwa mara ya mwisho, na toleo lolote ulilochagua kuruka |
| `captures.tsv` | Historia ya rekodi, mpya kwanza, kikomo 500 |

Kila unapobadilisha chaguo huandikwa; kila historia inapobadilika huandikwa. Zote mbili huandikwa
upya wakati wa kutoka.

Kufuta mojawapo ya faili hizi hurudisha sehemu hiyo pekee katika hali yake ya awali na si kitu
kingine — ndiyo maana zimetenganishwa, ili historia iliyoharibika isivute mipangilio yako pamoja
nayo. Hata faili iliyoharibiwa kwa kuhaririwa kwa mkono si janga: kila mpangilio hurudi kwenye
chaguo-msingi lake peke yake, kwa hivyo gharama ya mstari mmoja mbovu haizidi mstari huo.

Historia huhifadhi ingizo, si uchanganuzi. Huchanganuliwa upya wakati wa kupakia, yaani rekodi
iliyorejeshwa huakisi machaguo yanayotumika **sasa**, si yale yaliyokuwa yakitumika wakati
ilirekodiwa.

Misimbopau iliyoshikiliwa katika hali ya Unganisha haihifadhiwi: baada ya kuanzisha upya, hali ya
Unganisha huanza tupu.

---

## Njia za mkato za kibodi

| | |
|---|---|
| <kbd>Enter</kbd> | Changanua upau wa ingizo |
| <kbd>⌘O</kbd> | Fungua faili ya maandishi |
| <kbd>⌘S</kbd> | Hifadhi ripoti kama HTML |
| <kbd>⌘P</kbd> | Hifadhi ripoti kama PDF (kisanduku cha uchapishaji cha mfumo) |
| <kbd>⌘G</kbd> | Ingiza tokeni ya kitenganishi cha FNC1 |
| <kbd>⌘K</kbd> | Unganisha / Tenganisha skana (Windows pekee) |
| <kbd>⌘D</kbd> | Badilisha mandhari meusi |
| <kbd>⌘Q</kbd> | Ondoka |

Katika kihariri: <kbd>Tab</kbd> / <kbd>Shift</kbd>+<kbd>Tab</kbd> husogea kati ya sehemu
zinazohirikika, <kbd>Enter</kbd> huthibitisha, <kbd>Esc</kbd> hughairi.

Katika hali ya Unganisha: <kbd>Enter</kbd> katika sehemu ya *Msimbopau* huongeza
msimbopau. Hapo, andika FNC1 kama `{GS}`; <kbd>⌘G</kbd> ni ya upau wa ingizo wa hali ya
Changanua.

---

## Kutatua matatizo

| Dalili | Sababu na suluhisho |
|---|---|
| Programu haianzi, au inatoa `NoClassDefFoundError` | Saraka ya `lib/` lazima iwe kando ya faili ya JAR. Ziweke pamoja, au tumia usambazaji unaojitosheleza |
| Kwenye konsoli wakati wa kuanza *“Unsupported JavaFX configuration…”* | Inatarajiwa na haina madhara — kifaa cha zana kinaeleza tu jinsi kilivyopakiwa. Programu hufanya kazi kama kawaida |
| Kitenganishi ulichobandika kinaonekana kimepotea | Ndivyo inavyopaswa kuwa: hubadilika kuwa tokeni inayoonekana `{GS}`, na kichanganuzi hupokea herufi halisi yenyewe. Angalia [tokeni ya {GS}](#upau-wa-ingizo-na-tokeni-gs) |
| Kitufe cha *Unganisha skana* hakipo kabisa | Uko kwenye macOS au Linux, ambako hakuna kiendeshi cha skana. Angalia [Kuskani](#kuskani) |
| Skana haiunganishi | Angalia kama jina la kimantiki lililo kwenye orodha linalingana na modeli yako, kama skana imechomekwa, na kama hakuna programu nyingine iliyoishikilia. [Mwongozo wa kuweka skana ya Zebra](GaiaDesktop-Zebra-Scanner-Windows-Swahili.md#11-utatuzi-wa-matatizo) huorodhesha kila msimbo wa hitilafu. |
| Uhamishaji wa PDF unasema hakuna lengo | Hakuna printa wala lengo la PDF lililosanidiwa kwenye mashine yako. Ongeza moja, au hamisha kama HTML na uchapishe kutoka kivinjari |
| Katika kipengee cha menyu kinaonekana kitu kama `act.hri` | Lebo imesahaulika — tafadhali toa taarifa; hili si mpangilio, ni kasoro ya wakati wa kujenga |

---

## Mipaka

- **Kuskani kwenye Windows pekee**, kwa sababu viendeshi vipo huko.
- **PDF hupitia kisanduku cha uchapishaji**, si kisanduku cha kuhifadhi.
- **Malengo ya Digital Link huchukua kikoa na kiambishi cha njia pekee**; hakuna vigezo vya
  hoja vya ziada wala vipande.
- **AI hutokea mara moja tu** katika mfuatano wa elementi — hii ni kanuni ya GS1, si kikomo cha
  programu.
- **Hali ya Unganisha hukubali misimbopau ya GS1-128 pekee, mitatu zaidi**, na haiihifadhi
  kati ya uendeshaji mmoja na mwingine.
