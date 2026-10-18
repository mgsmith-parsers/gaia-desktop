# GAIA Desktop — Gebruikershandleiding

Hoe u de applicatie gebruikt. Voor bouwen, wijzigen of een taal toevoegen, zie de
[ontwikkelaarshandleiding](../../GaiaDesktop-Developer.md) (in het Engels). Origineel document:
[GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Inhoudsopgave

1. [Wat het is](#wat-het-is)
2. [Starten](#starten)
3. [Het venster](#het-venster)
4. [Modus Parseren](#modus-parseren)
   - [De invoerbalk en het {GS}-token](#de-invoerbalk-en-het-gs-token)
   - [Scannen](#scannen)
   - [Een bestand openen](#een-bestand-openen)
   - [De vastlegginggeschiedenis](#de-vastlegginggeschiedenis)
5. [Modus Samenstellen](#modus-samenstellen)
   - [De AI-editor](#de-ai-editor)
   - [Wat de editor weigert](#wat-de-editor-weigert)
   - [Elementreeks of Digital Link](#elementreeks-of-digital-link)
   - [Een bestaande vastlegging bewerken](#een-bestaande-vastlegging-bewerken)
6. [Modus Combineren](#modus-combineren)
   - [De strook Combineren](#de-strook-combineren)
   - [Wat Combineren weigert](#wat-combineren-weigert)
   - [Dubbele en conflicten](#dubbele-en-conflicten)
   - [Het combinatierapport](#het-combinatierapport)
7. [Het rapport](#het-rapport)
   - [Exporteren](#exporteren)
8. [Parseeropties](#parseeropties)
   - [Eerste start](#eerste-start)
9. [Talen](#talen)
10. [Wat er wordt onthouden](#wat-er-wordt-onthouden)
11. [Sneltoetsen](#sneltoetsen)
12. [Problemen oplossen](#problemen-oplossen)
13. [Grenzen](#grenzen)

---

## Wat het is

GAIA Desktop doet drie dingen met GS1-gegevens:

- **Parseren** — typ, plak, open of scan een GS1-elementreeks of een Digital Link-URI en lees
  het resultaat als een volledig rapport: wat elke toepassingsidentificatie (AI) betekent, wat
  er mis is en waarom;
- **Samenstellen** — AI-/waardeparen samenvoegen tot een elementreeks of een Digital Link-URI;
- **Combineren** — tot drie GS1-128-streepjescodes van één etiket samenvoegen tot de ene
  elementreeks die een GS1 DataMatrix zou dragen, die als geheel controleren en het symbool
  tekenen.

Alles wat de applicatie van GS1 weet — elke AI-definitie, elke validatieregel, elke foutmelding
en elke gelokaliseerde beschrijving — komt van de GAIA-engine eronder. De applicatie voegt geen
eigen regels toe: wat zij accepteert is precies wat de engine accepteert.

---

## Starten

```bash
java -jar gaia-desktop.jar
```

Het JAR-bestand heeft de map `lib/` nodig die ernaast wordt meegeleverd — houd ze bij elkaar,
anders start de applicatie niet. (Er kan ook één zelfstandig JAR-bestand worden gebouwd; zie de
[ontwikkelaarshandleiding](../../GaiaDesktop-Developer.md#packaging).)

**Java 21 of nieuwer** moet geïnstalleerd zijn. De distributie hoort bij één besturingssysteem
en één processorarchitectuur: gebruik het JAR-bestand dat voor uw machine is gebouwd.

Om de applicatie in een bepaalde taal te openen zonder de opgeslagen taal te wijzigen:

```bash
java -jar gaia-desktop.jar --language=FR
```

De waarde mag de cataloguscode zijn (`FR`, `PCM`, `ARZ`), de volledige naam (`FRENCH`,
`WU-CHINESE`) of een taalcode (`fr`, `zh-HK` voor Kantonees, `ar-EG` voor Egyptisch Arabisch) —
hoofdletters maken niet uit. De instelling geldt alleen voor die run: uw opgeslagen taal blijft
ongemoeid, zodat een snelkoppeling die u voor een collega maakt niet kan veranderen in welke
taal uw eigen exemplaar opent. Een taal *in* de applicatie kiezen is wél een echte keuze en
wordt zoals altijd bewaard. Een waarde die geen taal aanduidt wordt genegeerd, met een notitie
in de console; de applicatie opent dan in de opgeslagen taal in plaats van te weigeren te
starten.

Bij het starten drukt de console één regel af met de versie van de applicatie, de bouwdatum en
de versie van de engine. Dat is de regel die u in een foutmelding moet citeren.

---

## Het venster

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

> Het schema houdt de Engelse opschriften aan, net als alle codeblokken in deze documentatie. Op
> het scherm ziet u *Parseren* / *Samenstellen* / *Combineren*, *Vastleggingen*, *Rapport opslaan*, *Opties*,
> *scanner: uit* en *Over*.

**Op macOS en Linux bestaat de scannerregel niet** — geen schakelaar of apparaatkeuze in de
werkbalk, geen menu *Scanner* en geen *scanner: uit* in de statusbalk. Zie
[Scannen](#scannen).

De schakelaar **Parseren ⇄ Samenstellen** verandert alleen de strook onder de werkbalk en de
bedieningselementen voor vastleggen. De geschiedenis, het rapport, het optiepaneel en de
exportacties dienen beide modi, omdat een geslaagde samenstelling eenvoudigweg nog een
vastlegging wordt. De modus **Combineren** is anders ingedeeld: zie [Modus Combineren](#modus-combineren).

Het optiepaneel is **bij de eerste start gesloten**. De modus, de zichtbaarheid van het paneel,
het thema en de laatst gebruikte scanner worden tussen sessies onthouden.

**Nieuwe versies.** Enkele seconden na het starten, hoogstens één keer per dag, vraagt de
applicatie aan GitHub of er een nieuwere GAIA Desktop is uitgebracht. Zo ja, dan verschijnt er
een koppeling in de statusbalk — *GAIA Desktop 1.1.0 is beschikbaar* — die de releasepagina in uw
browser opent; er wordt niets voor u gedownload of geïnstalleerd. *Deze versie overslaan*
verbergt de melding tot een latere release. De controle blijft stil als er niets nieuws is of
als GitHub niet bereikbaar is. *Help → Controleren op updates…* controleert meteen en meldt
wat er gevonden is; *Help → Automatisch controleren op updates* zet de dagelijkse controle uit en weer
aan.

---

## Modus Parseren

### De invoerbalk en het {GS}-token

De balk accepteert een elementreeks, een GS1 Digital Link-URI of een ruwe scan inclusief het
AIM Code ID-voorvoegsel (`]d2…`). <kbd>Enter</kbd> parseert.

AI's met variabele lengte worden gescheiden door **FNC1** — het onzichtbare stuurteken `0x1D`.
Een tekstveld kan dat teken niet vasthouden: het zou stilzwijgend worden weggehaald, en
`01…` + FNC1 + `10LOT` zou aankomen als `01…10LOT`, een elementreeks met een andere betekenis.

Daarom toont de applicatie het scheidingsteken als het zichtbare token **`{GS}`** en zet het op
weg naar de parser weer terug:

- de **knop `{GS}`** voegt het token in op de plaats van de cursor;
- echte scheidingstekens **plakken** — via het toetsenbord, het contextmenu of slepen — zet ze
  automatisch om in `{GS}`, zodat er niets verloren gaat;
- het token wordt precies zo geschreven als het rapport het schrijft, dus een waarde die uit een
  rapport is gekopieerd kan er ongewijzigd weer in worden geplakt.

De vastlegging, het rapport en de opgeslagen geschiedenis bevatten alle het **echte**
scheidingsteken. Het token is alleen wat u ziet en typt.

### Scannen

> **Alleen Windows.** Zebra levert zijn scannerstuurprogramma's uitsluitend voor Windows; op
> macOS en Linux bouwt de applicatie daarom helemaal geen scannerbediening — geen schakelaar
> *Scanner verbinden*, geen apparaatkeuze, geen menu *Scanner* en geen scannerstatus in de
> statusbalk. Er is niets grijs gemaakt, want er is geen instelling of apparaat dat het zou
> kunnen inschakelen. Al het overige, inclusief het openen van een bestand met opgeslagen scans,
> werkt overal.

*Scanner verbinden* opent het apparaat dat in de keuzelijst staat; `ZebraAllScanners` is de
standaardwaarde en voldoet voor de meeste modellen. Elke scan wordt geparseerd en automatisch
aan de geschiedenis toegevoegd.

Scannergegevens worden byte voor byte gelezen, zodat het AIM Code ID-voorvoegsel en elk
FNC1-scheidingsteken de parser precies zo bereiken als de scanner ze verstuurde.

In de modus Samenstellen is de scannerbediening verborgen, maar een verbonden scanner blijft
werken: *Scanner → Verbinden / Loskoppelen* (<kbd>⌘K</kbd>) schakelt hem nog steeds, de
statusbalk meldt hem nog steeds, en een scan die binnenkomt terwijl de editor openstaat wordt
gewoon vastgelegd. In de modus Combineren blijft de bediening staan, en wordt elke scan aan de combinatie
toegevoegd in plaats van aan de geschiedenis.

Het voor het eerst instellen van een scanner — de Zebra Scanner SDK, de USB-modus van de scanner
en de GS1-instellingen — wordt stap voor stap beschreven in de
[installatiehandleiding voor Zebra-scanners](GaiaDesktop-Zebra-Scanner-Windows-Dutch.md), die in elke zip onder `docs/` zit. Als een apparaat geen verbinding maakt, zie [Problemen oplossen](#problemen-oplossen).

### Een bestand openen

*Bestand → Tekstbestand openen…* parseert **elke niet-lege regel als een eigen vastlegging** —
de snelle manier om een partij scans door te werken die iemand u heeft gestuurd. Het bestand
wordt byte voor byte gelezen, dus ruwe scheidingstekens erin blijven behouden.

### De vastlegginggeschiedenis

De nieuwste bovenaan. Elke regel toont een stip voor geldig / ongeldig, een samenvatting van één
regel, en verder het tijdstip van vastleggen, de herkomst en het aantal AI's. De datum
verschijnt alleen bij vastleggingen van een eerdere dag; de regels van vandaag tonen alleen de
tijd.

De samenvatting is de **Digital Link-URI** bij een Digital Link en anders de **HRI**: bij een
elementreeks leest `(01)09506000134352 (17)261231` prettiger dan
`01095060001343521726123110…`.

Klik met de rechtermuisknop op een vastlegging om de invoer of de HRI te kopiëren, haar terug te
sturen naar de invoerbalk, haar in de editor te openen, haar te verwijderen of de geschiedenis
te wissen.

---

## Modus Samenstellen

### De AI-editor

Een tabel met AI-/waardeparen. Typ een AI-code of kies uit **alle AI's die de engine kent** in
de lijst *Een AI toevoegen…*; de kolommen **Titel**, **Formaat** en **Beschrijving** vullen zich
terwijl u typt, in de gekozen taal. De keuzelijst kan geen AI aanbieden die het samenstellen zou
weigeren.

De tabel houdt altijd **één lege rij onderaan**: typ daarin en de volgende verschijnt, zodat een
reeks met meerdere AI's nooit *Rij toevoegen* nodig heeft.

Bewerken gedraagt zich zoals een tabel zich hoort te gedragen: <kbd>Enter</kbd>, <kbd>Tab</kbd>,
<kbd>Shift</kbd>+<kbd>Tab</kbd> en ergens anders klikken **behouden** alle wat u hebt getypt;
<kbd>Esc</kbd> verwerpt het. <kbd>Tab</kbd> gaat van AI naar Waarde en dan naar de volgende rij,
en slaat de kolommen over die u niet kunt bewerken.

### Wat de editor weigert

| | Wat er gebeurt |
|---|---|
| Een AI die de engine niet herkent | De rij wordt rood terwijl u typt; het samenstellen wordt geweigerd met een uitleg |
| Dezelfde AI twee keer, via de keuzelijst | Wordt niet toegevoegd — een melding noemt hem, en de rij die hem al bevat wordt geselecteerd |
| Dezelfde AI twee keer, met de hand getypt | Beide rijen worden gemarkeerd; het samenstellen wordt geweigerd met fout `GE-S004` |
| Niets om samen te stellen | Een melding; er wordt geen vastlegging gemaakt |

Een AI mag maar één keer in een elementreeks voorkomen — daarom weigert de keuzelijst meteen, in
plaats van een bij voorbaat kansloze samenstelling te laten proberen.

### Elementreeks of Digital Link

**Elementreeks samenstellen** voegt de rijen samen en zet het FNC1-scheidingsteken overal waar
de norm dat vereist. **Digital Link samenstellen** levert een URI op; klap *Digital Link-doel*
uit om hem naar uw eigen resolver te sturen in plaats van naar het canonieke `id.gs1.org`:

| Veld | Effect | Leeg betekent |
|---|---|---|
| Domein | De host van de resolver, bijv. `example.com` | `id.gs1.org` |
| Padvoorvoegsel | Een segment vóór het AI-pad, bijv. `/products` | geen voorvoegsel |

Bij **succes** gedraagt het resultaat zich precies als een scan: geparseerd, aan de geschiedenis
toegevoegd, als rapport weergegeven en opgeslagen. Het samenstellen valideert door de eigen
uitvoer te parseren, dus alles wat het teruggeeft is gegarandeerd geldig.

Bij **mislukking** wordt er niets vastgelegd. De reden blijft onder de samenstelknoppen staan,
één regel per probleem, telkens met de fout, de bijbehorende AI en de eigen uitleg van de engine:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> De dubbele aanhalingstekens in die melding komen uit de foutcatalogus van de engine, niet uit
> deze applicatie.

### Een bestaande vastlegging bewerken

Rechtermuisknop op een vastlegging → **Samenstellen** laadt haar AI's in de editor. Wijzig een
waarde, stel opnieuw samen, en het resultaat komt binnen als een nieuwe vastlegging — de
oorspronkelijke blijft ongemoeid.

---

## Modus Combineren

Oudere etiketten verdelen hun gegevens vaak over **meerdere GS1-128-streepjescodes** — de GTIN in
de ene, de houdbaarheidsdatum en de partij in een andere — omdat één lineaire streepjescode niet
alles kan bevatten. Combineren neemt tot **drie** van die streepjescodes, voegt ze samen tot
de ene elementreeks die een **GS1 DataMatrix** zou dragen, en controleert het resultaat als
geheel. Zodra dat geldig is, tekent de applicatie het DataMatrix-symbool.

Geen van die streepjescodes hoeft op zichzelf volledig te zijn. Een partijnummer zonder zijn GTIN
faalt op zichzelf met `GE-S005`, maar dat is normaal voor één streepjescode uit een set, dus
Combineren controleert het in plaats daarvan op de combinatie.

### De strook Combineren

Overschakelen naar **Combineren** vervangt de invoerbalk door een strook onderaan het
venster. De vastlegginggeschiedenis gaat opzij, omdat scans in de modus Combineren geen
vastleggingen zijn en er nooit in komen. Het rapportgebied toont in plaats daarvan het
combinatierapport:

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

Een streepjescode komt op twee manieren binnen:

- **Scannen.** Onder Windows blijft de scannerbediening in de modus Combineren in de
  werkbalk staan, en gaat elke scan rechtstreeks naar de combinatie in plaats van naar de
  geschiedenis.
- **Typen of plakken** in het veld *Streepjescode* en op <kbd>Enter</kbd> of
  **Toevoegen** drukken. Schrijf FNC1 als **`{GS}`**, net als in de invoerbalk. Een
  streepjescode die zonder AIM Code ID wordt getypt, geldt als GS1-128.

| Knop | Doet |
|---|---|
| **Toevoegen** | Voegt de streepjescode uit het veld toe |
| **Laatste verwijderen** | Verwijdert de meest recente streepjescode (niet beschikbaar als er geen zijn) |
| **Resetten** | Verwijdert alle streepjescodes, leegt het veld en elke weigering, en begint opnieuw |

De statusbalk zegt altijd hoeveel streepjescodes er zijn (*2 van 3 GS1-128-streepjescodes*), of waarom
de laatste is geweigerd.

### Wat Combineren weigert

Een geweigerde streepjescode wordt niet toegevoegd. Het rapport en de statusbalk zeggen allebei
waarom:

| Streepjescode | Geweigerd omdat |
|---|---|
| Een leeg veld | Er is niets om toe te voegen |
| Een vierde streepjescode | Een etiket wordt uit hoogstens drie gecombineerd. Verwijder de laatste, of reset |
| Een andere symbologie, volgens de AIM Code ID | Alleen GS1-128 wordt gecombineerd, dus een GS1 DataMatrix (`]d2`) of een QR Code wordt geweigerd, met de naam erbij |
| AI's die niet te lezen zijn | Een syntaxfout; de foutcode en de melding van de engine worden getoond |
| Helemaal geen AI's | Er is niets om te combineren |
| Een al gescande | Dezelfde elementreeks twee keer, bijvoorbeeld dezelfde streepjescode opnieuw gescand; het rapport zegt welke eerdere streepjescode hij herhaalt |

Combineren weigert alleen wat het helemaal niet kan gebruiken. Al het andere (een verkeerd
controlecijfer, een ongeldige datum, een ontbrekende begeleidende AI) wordt aanvaard en op de
combinatie beoordeeld.

### Dubbele en conflicten

De AI's worden gecombineerd in de volgorde waarin ze zijn gescand. Als twee streepjescodes
dezelfde AI dragen:

- **met dezelfde waarde**, wordt die één keer behouden en vermeld onder
  *Dubbele — één keer behouden*, met de streepjescodes waarin hij is gelezen;
- **met verschillende waarden**, is het een **conflict**. Het oordeel luidt
  *CONFLICT*, de botsende waarden worden per streepjescode vermeld, en **er wordt niets
  gecombineerd** tot het is opgelost. Verwijder de foute streepjescode, of reset.

### Het combinatierapport

Van boven naar beneden:

1. **Het oordeel**: *geen scans*, *CONFLICT*, geldig of ongeldig, en hoeveel
   streepjescodes er zijn.
2. **De scans**: elke streepjescode zoals hij binnenkwam, de symbologie, en de elementreeks die
   overblijft zodra de AIM Code ID is verwijderd. Een probleem dat alleen de combinatie kan
   beslechten, staat bij de streepjescode, gemarkeerd met *beoordeeld op de combinatie*.
3. **Dubbele** en **conflicten**, als die er zijn.
4. **Gecombineerd**: de elementreeks, de HRI en de lengte, daarna de gecombineerde invoer
   als gelabelde band en één kaart per AI, beide weergegeven zoals in het
   [parseerrapport](#het-rapport). Elke fout en waarschuwing over de combinatie wordt daarna vermeld
   onder *Validatie*.
5. **Het GS1 DataMatrix-symbool**, met de HRI eronder. Het wordt alleen getekend als de
   combinatie geldig is. Anders zegt het rapport waarom het niet is getekend, zodat een ongeldig
   symbool niet per ongeluk kan worden afgedrukt.

Bijvoorbeeld een etiket met twee streepjescodes:

| Streepjescode | Bevat | Op zichzelf |
|---|---|---|
| `]C10109506000134352` | (01) GTIN | geldig |
| `]C11726123110LOT-ABC` | (17) houdbaarheidsdatum, (10) partij | `GE-S005` voor beide AI's, beoordeeld op de combinatie |

Samen worden ze `01095060001343521726123110LOT-ABC`, HRI
`(01)09506000134352 (17)261231 (10)LOT-ABC`, wat geldig is, dus het symbool wordt getekend. AI
`17` heeft een vaste lengte, dus daarna is geen FNC1 nodig. Combineren voegt er een in
overal waar de standaard dat vereist.

**Rapport opslaan** exporteert het combinatierapport als HTML of PDF, net als een
parseerrapport (zie [Exporteren](#exporteren)), zodra er één streepjescode is. De naam van het
HTML-bestand begint met `gaia-combine-`.

Het rapport volgt de gekozen taal en het gekozen thema. Een gewijzigde parseeroptie of taal
controleert de streepjescodes die er al zijn opnieuw. Ze overleven ook een wissel naar
Parseren of Samenstellen en terug, maar geen herstart: streepjescodes in de modus
Combineren worden niet opgeslagen.

---

## Het rapport

De geselecteerde vastlegging wordt weergegeven als een volledig rapport: een oordeelsbanner, de
elementreeks uiteengelegd als een gelabelde „band”, de parseermetagegevens, één kaart per AI met
de interpretaties, en de fouten en waarschuwingen met aanwijzingen om ze te verhelpen.

Bij een **Digital Link** draagt elke AI-kaart bovendien haar structurele rol — **primaire
sleutel**, **sleutelkwalificatie** of **gegevensattribuut** — met kleurcodering in de volgorde
waarin ze in de URI voorkomen. Rollen worden alleen bij een Digital Link getoond: in een gewone
elementreeks heeft een AI geen positie in een URI om te beschrijven.

Het rapport volgt de taal en het thema die u hebt gekozen.

### Exporteren

| | |
|---|---|
| **Rapport opslaan → Als HTML…** (<kbd>⌘S</kbd>) | Schrijft precies wat er op het scherm staat: één zelfstandig bestand dat in elke browser opent |
| **Rapport opslaan → Als PDF…** (<kbd>⌘P</kbd>) | Drukt het rapport af via het afdrukvenster van het systeem — kies daar een PDF-bestemming |

PDF loopt via het afdrukvenster omdat daar de bestemming en de bestandsnaam worden gekozen:
**PDF ▾ → Bewaar als pdf** op macOS, **Microsoft Print to PDF** op Windows, **Afdrukken naar
bestand** op Linux. De pagina's worden netjes verdeeld en de tekst blijft selecteerbaar.

Afdrukken en PDF gebruiken altijd het lichte palet, ook wanneer de applicatie in het donkere
thema staat, en behouden de gekleurde AI-labels en de oordeelsbanner.

Heeft uw machine helemaal geen printer of PDF-bestemming ingesteld, dan meldt de applicatie dat
en verwijst zij naar de HTML-export, die vanuit elke browser afgedrukt kan worden.

---

## Parseeropties

Het paneel rechts bevat alle opties van de engine. Iets wijzigen **parseert de geselecteerde
vastlegging meteen opnieuw**; het paneel dient dus ook om te vragen „hoe zou deze invoer er met
deze instellingen uitzien?”, en niet alleen om de volgende scan te beïnvloeden.

| Optie | Waarden |
|---|---|
| Parseermodus | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — hoe diep de analyse gaat |
| Taal | 35 meldingscatalogi |
| Volgorde | `LITTLE` (dd/mm/jjjj), `MIDDLE` (mm/dd/jjjj), `BIG` (jjjj/mm/dd) |
| Scheidingsteken | `SLASH`, `HYPHEN`, `PERIOD` |
| Maand | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Jaar | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| „requires”-controle overslaan | Wijst een AI waarvan de vereiste partner ontbreekt niet meer af (`GE-S005`) |
| „excludes”-controle overslaan | Wijst AI-combinaties die de norm verbiedt niet meer af (`GE-S006`) |

*Standaardwaarden herstellen*, onderaan het paneel, zet de eigen standaardwaarden van de engine
terug.

### Eerste start

Zolang er nog niets is opgeslagen, neemt de applicatie haar instellingen over uit de
**regio-instellingen van uw machine** in plaats van uit de standaardwaarden van de engine:

- de **taal** komt uit de landinstelling van het systeem, met terugval op Engels wanneer er geen
  catalogus voor is;
- de **datumopties** worden afgeleid uit de korte datumnotatie van uw landinstelling: de
  volgorde van de onderdelen, het scheidingsteken en of maand en jaar worden afgekort.

> **Let op.** Een landinstelling die korte datums met een jaartal van twee cijfers schrijft,
> begint hier dus ook met twee cijfers: dat sluit aan bij de rest van uw systeem, maar wijkt af
> van de standaard van de engine, die vier cijfers gebruikt. Wijzig het in het paneel en de
> keuze blijft staan.

---

## Talen

Een taal kiezen verandert **de hele applicatie** — menu's, knoppen, de editor, het optiepaneel,
de statusregel en het rapport — niet alleen de foutmeldingen. Talen die van rechts naar links
worden geschreven spiegelen het venster en het rapport.

Alle 35 talen zijn beschikbaar, met twee lagen dekking:

| | Dekking |
|---|---|
| Parseerrapporten en alle foutmeldingen van de engine | **alle 35 talen** |
| Menu's, knoppen en opschriften van de applicatie zelf | **alle 35 talen** |

De talen van rechts naar links — Arabisch, Egyptisch Arabisch, Perzisch en Urdu — spiegelen het
venster en het rapport vanaf het moment dat de applicatie opent.

De taal wordt tussen sessies onthouden. Om één keer in een andere taal te openen, zie
[`--language`](#starten).

Een paar dingen blijven in alle talen bewust onvertaald: de korte veldnamen naast de ruwe
waarden (`format`, `position`, `raw`, `len` …), die identificatoren zijn en geen woorden; de
namen van de voorbeelden en van de symbologieën, die GS1- en vakterminologie zijn; en één lange
normalinea, waar een halve vertaling zou misleiden.

---

## Wat er wordt onthouden

Twee bestanden in `.gaia-desktop` in uw thuismap:

| Bestand | Bevat |
|---|---|
| `options.properties` | Alle parseeropties, het thema, of het optiepaneel open staat, de laatst gebruikte scanner, de modus waarin het venster is achtergelaten, en de instellingen van de updatecontrole — aan of uit, wanneer die het laatst draaide en elke versie die u wilde overslaan |
| `captures.tsv` | De vastlegginggeschiedenis, de nieuwste bovenaan, met een limiet van 500 items |

De opties worden geschreven zodra u er een wijzigt; de geschiedenis zodra die verandert. Beide
worden bij het afsluiten opnieuw weggeschreven.

Een van beide bestanden verwijderen zet alleen dat deel terug en verder niets — ze worden apart
gehouden zodat een beschadigde geschiedenis u niet uw instellingen kost. Een bestand dat u met
de hand kapot hebt bewerkt is evenmin fataal: elke instelling valt afzonderlijk terug op haar
standaardwaarde, dus één foute regel kost alleen die regel.

De geschiedenis bewaart de invoer, niet de analyse. Die wordt bij het laden opnieuw uitgevoerd,
wat betekent dat een herstelde vastlegging de opties weerspiegelt die **nu** gelden, niet die
golden toen zij werd vastgelegd.

De streepjescodes in de modus Combineren worden niet opgeslagen: na een herstart begint
Combineren leeg.

---

## Sneltoetsen

| | |
|---|---|
| <kbd>Enter</kbd> | De invoerbalk parseren |
| <kbd>⌘O</kbd> | Een tekstbestand openen |
| <kbd>⌘S</kbd> | Het rapport als HTML opslaan |
| <kbd>⌘P</kbd> | Het rapport als PDF opslaan (afdrukvenster van het systeem) |
| <kbd>⌘G</kbd> | Het FNC1-scheidingstoken invoegen |
| <kbd>⌘K</kbd> | De scanner verbinden / loskoppelen (alleen Windows) |
| <kbd>⌘D</kbd> | Het donkere thema omschakelen |
| <kbd>⌘Q</kbd> | Afsluiten |

In de editor: <kbd>Tab</kbd> / <kbd>Shift</kbd>+<kbd>Tab</kbd> verplaatsen tussen bewerkbare
cellen, <kbd>Enter</kbd> bevestigt, <kbd>Esc</kbd> annuleert.

In de modus Combineren: <kbd>Enter</kbd> in het veld *Streepjescode* voegt de
streepjescode toe. Typ daar FNC1 als `{GS}`; <kbd>⌘G</kbd> hoort bij de invoerbalk van de modus
Parseren.

---

## Problemen oplossen

| Verschijnsel | Oorzaak en oplossing |
|---|---|
| De applicatie start niet, of meldt `NoClassDefFoundError` | De map `lib/` moet naast het JAR-bestand staan. Houd ze bij elkaar, of gebruik een zelfstandige distributie |
| *„Unsupported JavaFX configuration…”* in de console bij het starten | Verwacht en onschuldig — de toolkit meldt alleen hoe zij is geladen. De applicatie werkt normaal |
| Een geplakt scheidingsteken lijkt te verdwijnen | Zo bedoeld: het wordt omgezet naar het zichtbare token `{GS}`, en de parser krijgt nog steeds het echte teken. Zie [het {GS}-token](#de-invoerbalk-en-het-gs-token) |
| Er is geen knop *Scanner verbinden* | U werkt op macOS of Linux, waar de scannerstuurprogramma's niet bestaan. Zie [Scannen](#scannen) |
| De scanner maakt geen verbinding | Controleer of de logische naam in de keuzelijst bij uw model past, of de scanner is aangesloten en of geen andere applicatie hem bezet houdt. De [installatiehandleiding voor Zebra-scanners](GaiaDesktop-Zebra-Scanner-Windows-Dutch.md#11-problemen-oplossen) somt elke foutcode op. |
| De PDF-export meldt dat er geen bestemming is | Uw machine heeft geen printer of PDF-bestemming ingesteld. Voeg er een toe, of exporteer naar HTML en druk af vanuit een browser |
| Een menu-item toont zoiets als `act.hri` | Er ontbreekt een opschrift — meld het alstublieft; dit is een fout in de build, geen instelling |

---

## Grenzen

- **Scannen kan alleen op Windows**, omdat de stuurprogramma's dat ook zijn.
- **PDF loopt via het afdrukvenster** in plaats van via een opslagvenster.
- **Digital Link-doelen accepteren een domein en een padvoorvoegsel**, geen extra
  queryparameters of fragment.
- **Een AI mag maar één keer voorkomen** in een elementreeks — dat is de GS1-regel, geen grens
  van de applicatie.
- **Combineren neemt alleen GS1-128-streepjescodes aan, hoogstens drie**, en bewaart ze
  niet tussen sessies.
