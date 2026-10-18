# GAIA Desktop — Användarhandbok

Hur du använder programmet. För att bygga, ändra eller lägga till ett språk, se
[utvecklarhandboken](../../GaiaDesktop-Developer.md) (på engelska). Originaldokument:
[GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Innehåll

1. [Vad det är](#vad-det-är)
2. [Köra programmet](#köra-programmet)
3. [Fönstret](#fönstret)
4. [Läget Tolka](#läget-tolka)
   - [Inmatningsfältet och {GS}-token](#inmatningsfältet-och-gs-token)
   - [Skanning](#skanning)
   - [Öppna en fil](#öppna-en-fil)
   - [Registreringshistoriken](#registreringshistoriken)
5. [Läget Bygg](#läget-bygg)
   - [AI-redigeraren](#ai-redigeraren)
   - [Vad redigeraren avvisar](#vad-redigeraren-avvisar)
   - [Elementsträng eller Digital Link](#elementsträng-eller-digital-link)
   - [Redigera en befintlig registrering](#redigera-en-befintlig-registrering)
6. [Läget Kombinera](#läget-kombinera)
   - [Remsan Kombinera](#remsan-kombinera)
   - [Vad Kombinera avvisar](#vad-kombinera-avvisar)
   - [Dubbletter och konflikter](#dubbletter-och-konflikter)
   - [Kombinationsrapporten](#kombinationsrapporten)
7. [Rapporten](#rapporten)
   - [Exportera](#exportera)
8. [Tolkningsalternativ](#tolkningsalternativ)
   - [Första starten](#första-starten)
9. [Språk](#språk)
10. [Vad som kommer ihåg](#vad-som-kommer-ihåg)
11. [Tangentbordsgenvägar](#tangentbordsgenvägar)
12. [Felsökning](#felsökning)
13. [Begränsningar](#begränsningar)

---

## Vad det är

GAIA Desktop gör tre saker med GS1-data:

- **Tolkar** — skriv, klistra in, öppna eller skanna en GS1-elementsträng eller en
  Digital Link-URI och läs resultatet som en fullständig rapport: vad varje
  applikationsidentifierare (AI) betyder, vad som är fel och varför;
- **Bygger** — sätter samman AI/värde-par till en elementsträng eller en Digital Link-URI;
- **Kombinera** — slå ihop upp till tre GS1-128-streckkoder från en etikett till den enda
  elementsträng som en GS1 DataMatrix skulle bära, kontrollera den som en helhet och rita
  symbolen.

Allt programmet vet om GS1 — varje AI-definition, varje valideringsregel, varje felmeddelande
och varje översatt beskrivning — kommer från GAIA-motorn under det. Programmet lägger inte till
egna regler: det accepterar exakt det som motorn accepterar.

---

## Köra programmet

```bash
java -jar gaia-desktop.jar
```

JAR-filen behöver katalogen `lib/` som levereras bredvid den — håll ihop dem, annars startar
inte programmet. (Det går också att bygga en fristående JAR-fil; se
[utvecklarhandboken](../../GaiaDesktop-Developer.md#packaging).)

**Java 21 eller senare** måste vara installerat. Distributionen gäller ett visst operativsystem
och en viss processorarkitektur — använd den JAR-fil som byggts för din dator.

För att öppna programmet på ett visst språk utan att ändra det sparade språket:

```bash
java -jar gaia-desktop.jar --language=FR
```

Värdet kan vara katalogkoden (`FR`, `PCM`, `ARZ`), det fullständiga namnet (`FRENCH`,
`WU-CHINESE`) eller en språktagg (`fr`, `zh-HK` för kantonesiska, `ar-EG` för egyptisk arabiska)
— versaler och gemener spelar ingen roll. Valet gäller bara den körningen: ditt sparade språk
lämnas orört, så en genväg du gör åt en kollega kan inte ändra vilket språk din egen kopia
öppnas i. Att välja språk *inne i* programmet är däremot ett riktigt val och sparas som vanligt.
Ett värde som inte pekar ut något språk ignoreras med en notis i konsolen, och programmet öppnas
på det sparade språket i stället för att vägra starta.

Vid start skriver konsolen en rad med programmets version, byggdatum och motorns version. Det är
den raden du bör citera i en felrapport.

---

## Fönstret

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

> Skissen behåller de engelska etiketterna, liksom alla kodblock i den här dokumentationen. På
> skärmen ser du *Tolka* / *Bygg* / *Kombinera*, *Registreringar*, *Spara rapport*, *Alternativ*,
> *skanner: av* och *Om*.

**På macOS och Linux finns inte skannerraden i bilden** — ingen växlingsknapp eller enhetsväljare
i verktygsfältet, ingen *Skanner*-meny och inget *skanner: av* i statusfältet. Se
[Skanning](#skanning).

Växlaren **Tolka ⇄ Bygg** ändrar bara remsan under verktygsfältet och kontrollerna för
registrering. Historiken, rapporten, alternativpanelen och exportkommandona tjänar båda lägena,
eftersom ett lyckat bygge helt enkelt blir ännu en registrering. Läget **Kombinera** är upplagt på ett annat sätt: se [Läget Kombinera](#läget-kombinera).

Alternativpanelen är **stängd vid första starten**. Läget, panelens synlighet, temat och den
senast använda skannern kommer ihåg mellan körningar.

**Nya versioner.** Några sekunder efter starten, högst en gång om dagen, frågar programmet GitHub
om en nyare GAIA Desktop har släppts. Om så är fallet visas en länk i statusfältet —
*GAIA Desktop 1.1.0 finns tillgänglig* — som öppnar versionssidan i din webbläsare; ingenting laddas ned
eller installeras åt dig. *Hoppa över den här versionen* döljer meddelandet till en senare version.
Kontrollen är tyst när det inte finns något nytt eller när den inte når GitHub.
*Hjälp → Sök efter uppdateringar…* kontrollerar direkt och säger vad den hittade;
*Hjälp → Sök efter uppdateringar automatiskt* slår av och på den dagliga kontrollen.

---

## Läget Tolka

### Inmatningsfältet och {GS}-token

Fältet tar emot en elementsträng, en GS1 Digital Link-URI eller en rå skanning inklusive dess
AIM Code ID-prefix (`]d2…`). <kbd>Retur</kbd> tolkar.

AI:er med variabel längd skiljs åt av **FNC1** — det osynliga styrtecknet `0x1D`. Ett textfält
kan inte hålla det tecknet: det skulle tyst plockas bort, och `01…` + FNC1 + `10LOT` skulle komma
fram som `01…10LOT`, en elementsträng med en annan innebörd.

Därför visar programmet avgränsaren som den synliga token **`{GS}`** och gör om den på vägen till
tolken:

- **knappen `{GS}`** infogar token där markören står;
- att **klistra in** riktiga avgränsartecken — med tangentbordet, via snabbmenyn eller genom att
  dra och släppa — gör dem automatiskt till `{GS}`, så ingenting går förlorat;
- token skrivs på precis samma sätt som rapporten skriver den, så ett värde som kopierats ur en
  rapport kan klistras tillbaka oförändrat.

Registreringen, rapporten och den sparade historiken innehåller alla den **riktiga**
avgränsaren. Token är bara det du ser och skriver.

### Skanning

> **Endast Windows.** Zebra levererar sina skannerdrivrutiner enbart för Windows, så på macOS
> och Linux bygger programmet inga skannerkontroller alls — ingen *Anslut skanner*-knapp, ingen
> enhetsväljare, ingen *Skanner*-meny och ingen skannerstatus i statusfältet. Ingenting är
> nedtonat, eftersom det inte finns någon inställning eller enhet som skulle kunna slå på det.
> Allt annat, inklusive att öppna en fil med sparade skanningar, fungerar överallt.

*Anslut skanner* öppnar den enhet som anges i väljaren; `ZebraAllScanners` är standardvärdet och
passar de flesta modeller. Varje skanning tolkas och läggs automatiskt till i historiken.

Skannerdata läses byte för byte, så AIM Code ID-prefixet och varje FNC1-avgränsare når tolken
exakt som skannern skickade dem.

I läget Bygg är skannerkontrollerna dolda, men en ansluten skanner fortsätter att fungera:
*Skanner → Anslut / Koppla från* (<kbd>⌘K</kbd>) växlar den fortfarande, statusfältet rapporterar
den fortfarande, och en skanning som kommer in medan redigeraren är öppen registreras ändå. I läget Kombinera finns kontrollerna kvar, och varje skanning läggs till i kombinationen
i stället för i historiken.

Att ställa in en skanner första gången — Zebra Scanner SDK, skannerns USB-läge och
GS1-inställningarna — beskrivs steg för steg i
[installationsguiden för Zebra-skannrar](GaiaDesktop-Zebra-Scanner-Windows-Swedish.md), som följer med i varje zip under `docs/`. Om en enhet inte ansluter, se [Felsökning](#felsökning).

### Öppna en fil

*Arkiv → Öppna textfil…* tolkar **varje icke-tom rad som en egen registrering**, vilket är det
snabba sättet att beta av en bunt skanningar som någon har skickat dig. Filen läses byte för
byte, så råa avgränsare i den överlever.

### Registreringshistoriken

Nyast överst. Varje rad visar en punkt för giltig/ogiltig, en sammanfattning på en rad samt
tidpunkt, ursprung och antal AI:er. Datumet visas bara för registreringar från en tidigare dag;
dagens rader visar enbart klockslaget.

Sammanfattningen är **Digital Link-URI:n** för en Digital Link, annars **HRI**: för en
elementsträng är `(01)09506000134352 (17)261231` lättare att läsa än
`01095060001343521726123110…`.

Högerklicka på en registrering för att kopiera dess indata eller HRI, skicka tillbaka den till
inmatningsfältet, öppna den i redigeraren, ta bort den eller rensa historiken.

---

## Läget Bygg

### AI-redigeraren

En tabell med AI/värde-par. Skriv en AI-kod eller välj bland **alla AI:er som motorn känner
till** i listan *Lägg till en AI…*; kolumnerna **Titel**, **Format** och **Beskrivning** fylls i
medan du skriver, på det språk du valt. Listan kan inte erbjuda en AI som bygget skulle avvisa.

Tabellen har alltid **en tom rad sist**: skriv i den så dyker nästa upp, så en sträng med flera
AI:er behöver aldrig *Lägg till rad*.

Redigeringen beter sig som en tabell ska: <kbd>Retur</kbd>, <kbd>Tabb</kbd>,
<kbd>Skift</kbd>+<kbd>Tabb</kbd> och att klicka någon annanstans **behåller** allt du skrivit;
<kbd>Esc</kbd> förkastar det. <kbd>Tabb</kbd> går från AI till Värde och vidare till nästa rad,
och hoppar över de kolumner du inte kan redigera.

### Vad redigeraren avvisar

| | Vad som händer |
|---|---|
| En AI som motorn inte känner igen | Raden blir röd medan du skriver; bygget avvisas med en förklaring |
| Samma AI två gånger, från listan | Läggs inte till — ett meddelande namnger den, och raden som redan har den markeras |
| Samma AI två gånger, skriven för hand | Båda raderna markeras; bygget avvisas med felet `GE-S004` |
| Inget att bygga | Ett meddelande; ingen registrering skapas |

En AI får bara förekomma en gång i en elementsträng — därför avvisar listan direkt i stället för
att låta dig försöka med ett bygge som är dömt att misslyckas.

### Elementsträng eller Digital Link

**Bygg elementsträng** fogar samman raderna och sätter in FNC1-avgränsaren överallt där
standarden kräver det. **Bygg Digital Link** ger en URI; fäll ut *Digital Link-mål* för att rikta
den mot din egen resolver i stället för det kanoniska `id.gs1.org`:

| Fält | Effekt | Tomt betyder |
|---|---|---|
| Domän | Resolverns värd, t.ex. `example.com` | `id.gs1.org` |
| Sökvägsprefix | Ett segment före AI-sökvägen, t.ex. `/products` | inget prefix |

Vid **lyckat resultat** beter sig utfallet precis som en skanning: det tolkas, läggs till i
historiken, ritas upp som rapport och sparas. Bygget validerar genom att tolka sin egen utdata,
så allt det lämnar tillbaka är garanterat giltigt.

Vid **misslyckande** registreras ingenting. Orsaken står kvar under byggknapparna, en rad per
problem, var och en med felet, den AI det gäller och motorns egen förklaring:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> De dubblerade citattecknen i det meddelandet kommer från motorns felkatalog, inte från det här
> programmet.

### Redigera en befintlig registrering

Högerklicka på en registrering → **Bygg** läser in dess AI:er i redigeraren. Ändra ett värde,
bygg om, och resultatet landar som en ny registrering — originalet lämnas orört.

---

## Läget Kombinera

Äldre etiketter sprider ofta sina data över **flera GS1-128-streckkoder** — GTIN i en,
bäst före-datum och batch i en annan — eftersom en enda linjär streckkod inte rymmer allt.
Kombinera tar upp till **tre** av de streckkoderna, slår ihop dem till den enda
elementsträng som en **GS1 DataMatrix** skulle bära och kontrollerar resultatet som en helhet.
När det är giltigt ritar programmet DataMatrix-symbolen.

Ingen av de streckkoderna behöver vara komplett för sig. Ett batchnummer utan sitt GTIN
underkänns ensamt med `GE-S005`, men det är normalt för en streckkod i en uppsättning, så
Kombinera kontrollerar det mot kombinationen i stället.

### Remsan Kombinera

Ett byte till **Kombinera** ersätter inmatningsfältet med en remsa längs fönstrets
nederkant. Registreringshistoriken kliver åt sidan, eftersom skanningar i läget Kombinera
inte är registreringar och aldrig hamnar där. Rapportytan visar i stället
kombinationsrapporten:

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

En streckkod kan komma in på två sätt:

- **Skanna den.** I Windows ligger skannerkontrollerna kvar i verktygsfältet i läget
  Kombinera, och varje skanning går direkt in i kombinationen i stället för i historiken.
- **Skriv eller klistra in den** i fältet *Streckkod* och tryck på
  <kbd>Retur</kbd> eller **Lägg till**. Skriv FNC1 som **`{GS}`**, som i inmatningsfältet.
  En streckkod som skrivs utan AIM Code ID tolkas som GS1-128.

| Knapp | Gör |
|---|---|
| **Lägg till** | Lägger till streckkoden i fältet |
| **Ta bort sista** | Tar bort den senaste streckkoden (inte tillgänglig när det inte finns några) |
| **Återställ** | Tar bort alla streckkoder, tömmer fältet och eventuella avvisningar, och börjar om |

Statusfältet säger alltid hur många streckkoder som finns (*2 av 3 GS1-128-streckkoder*), eller varför
den senaste avvisades.

### Vad Kombinera avvisar

En avvisad streckkod läggs inte till. Både rapporten och statusfältet säger varför:

| Streckkod | Avvisas eftersom |
|---|---|
| Ett tomt fält | Det finns inget att lägga till |
| En fjärde streckkod | En etikett kombineras från högst tre. Ta bort den senaste, eller återställ |
| En annan symbologi, enligt dess AIM Code ID | Bara GS1-128 kombineras, så en GS1 DataMatrix (`]d2`) eller en QR Code avvisas, med sitt namn |
| AI som inte går att läsa | Ett syntaxfel; motorns felkod och meddelande visas |
| Inga AI alls | Det finns inget att kombinera |
| En som redan skannats | Samma elementsträng två gånger, till exempel samma streckkod skannad igen; rapporten säger vilken tidigare streckkod den upprepar |

Kombinera avvisar bara det den inte kan använda alls. Allt annat (en felaktig
kontrollsiffra, ett ogiltigt datum, en saknad följe-AI) tas emot och bedöms på kombinationen.

### Dubbletter och konflikter

AI:erna kombineras i den ordning de skannades. När två streckkoder bär samma AI:

- **med samma värde** behålls den en gång och listas under *Dubbletter — behålls en gång*, tillsammans
  med de streckkoder den lästes i;
- **med olika värden** är det en **konflikt**. Utlåtandet lyder *KONFLIKT*, de
  motstridiga värdena listas streckkod för streckkod, och **ingenting kombineras** förrän den är
  löst. Ta bort den felaktiga streckkoden, eller återställ.

### Kombinationsrapporten

Uppifrån och ned:

1. **Utlåtandet**: *inga skanningar*, *KONFLIKT*, giltig eller ogiltig, och hur
   många streckkoder som finns.
2. **Skanningarna**: varje streckkod som den kom in, dess symbologi och den elementsträng som
   återstår när AIM Code ID har tagits bort. Ett problem som bara kombinationen kan avgöra visas
   vid streckkoden, märkt *bedöms på kombinationen*.
3. **Dubbletter** och **konflikter**, om det finns några.
4. **Kombinerad**: elementsträngen, dess HRI och dess längd, sedan den kombinerade
   inmatningen som ett märkt band och ett kort per AI, båda upplagda som i
   [tolkningsrapporten](#rapporten). Varje fel och varje varning om kombinationen listas sedan
   under *Validering*.
5. **GS1 DataMatrix-symbolen**, med sin HRI under. Den ritas bara när kombinationen är giltig.
   Annars säger rapporten varför den inte ritades, så att en ogiltig symbol inte kan skrivas ut
   av misstag.

Till exempel en etikett med två streckkoder:

| Streckkod | Bär | För sig |
|---|---|---|
| `]C10109506000134352` | (01) GTIN | giltig |
| `]C11726123110LOT-ABC` | (17) bäst före-datum, (10) batch | `GE-S005` för båda AI, bedömd på kombinationen |

De slås ihop till `01095060001343521726123110LOT-ABC`, HRI
`(01)09506000134352 (17)261231 (10)LOT-ABC`, som är giltig, så symbolen ritas. AI `17` har fast
längd, så inget FNC1 behövs efter den. Kombinera infogar ett överallt där standarden
kräver det.

**Spara rapport** exporterar kombinationsrapporten som HTML eller PDF, på samma sätt som en
tolkningsrapport (se [Exportera](#exportera)), så snart en streckkod finns. HTML-filens namn
börjar med `gaia-combine-`.

Rapporten följer valt språk och tema. Ett ändrat tolkningsalternativ eller språk kontrollerar
om de streckkoder som redan finns. De överlever också ett byte till Tolka eller
Bygg och tillbaka, men inte en omstart: streckkoder i läget Kombinera sparas
inte.

---

## Rapporten

Den markerade registreringen ritas upp som en fullständig rapport: en utlåtandebanner,
elementsträngen uppdelad i ett märkt ”band”, tolkningens metadata, ett kort per AI med dess
tolkningar, samt fel och varningar med vägledning för att åtgärda dem.

För en **Digital Link** bär varje AI-kort dessutom sin strukturella roll — **primärnyckel**,
**nyckelkvalificerare** eller **dataattribut** — färgkodad i den ordning de förekommer i URI:n.
Roller visas bara för en Digital Link: i en vanlig elementsträng har en AI ingen URI-position att
beskriva.

Rapporten följer det språk och det tema du har valt.

### Exportera

| | |
|---|---|
| **Spara rapport → Som HTML…** (<kbd>⌘S</kbd>) | Skriver exakt det som syns på skärmen: en enda fristående fil som öppnas i vilken webbläsare som helst |
| **Spara rapport → Som PDF…** (<kbd>⌘P</kbd>) | Skriver ut rapporten via systemets utskriftsdialog — välj en PDF-destination där |

PDF går via utskriftsdialogen eftersom det är där destinationen och filnamnet väljs:
**PDF ▾ → Spara som PDF** på macOS, **Microsoft Print to PDF** på Windows, **Skriv till fil** på
Linux. Sidorna bryts korrekt och texten går fortfarande att markera.

Utskrift och PDF använder alltid den ljusa paletten, även när programmet är i mörkt tema, och
behåller de färgade AI-brickorna och utlåtandebannern.

Om datorn inte har någon skrivare eller PDF-destination alls konfigurerad säger programmet det
och hänvisar till HTML-exporten, som kan skrivas ut från vilken webbläsare som helst.

---

## Tolkningsalternativ

Panelen till höger samlar alla motorns alternativ. Att ändra något **tolkar om den markerade
registreringen omedelbart**, så panelen är också ett sätt att fråga ”hur skulle den här indatan
se ut med de här inställningarna?”, inte bara något som påverkar nästa skanning.

| Alternativ | Värden |
|---|---|
| Analysläge | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — hur djupt analysen ska gå |
| Språk | 35 meddelandekataloger |
| Ordning | `LITTLE` (dd/mm/åååå), `MIDDLE` (mm/dd/åååå), `BIG` (åååå/mm/dd) |
| Avgränsare | `SLASH`, `HYPHEN`, `PERIOD` |
| Månad | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| År | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Hoppa över ”requires”-kontrollen | Slutar avvisa en AI vars obligatoriska följeslagare saknas (`GE-S005`) |
| Hoppa över ”excludes”-kontrollen | Slutar avvisa AI-kombinationer som standarden förbjuder (`GE-S006`) |

*Återställ standardvärden* längst ned i panelen återställer motorns egna standardvärden.

### Första starten

Så länge inget är sparat hämtar programmet sina inställningar från **datorns regionala
inställningar** i stället för från motorns standardvärden:

- **språket** kommer från systemets språkinställning, med återgång till engelska när det inte
  finns någon katalog för den;
- **datumalternativen** läses ur den korta datumformatsmallen i dina regionala inställningar:
  delarnas ordning, avgränsaren och om månad och år förkortas.

> **Obs.** En regional inställning som skriver korta datum med tvåsiffrigt år börjar alltså även
> här med två siffror: det stämmer med resten av systemet men skiljer sig från motorns eget
> standardvärde, som är fyrsiffrigt. Ändra det i panelen så blir valet kvar.

---

## Språk

Att välja språk ändrar **hela programmet** — menyer, knappar, redigeraren, alternativpanelen,
statusraden och rapporten — inte bara felmeddelandena. Språk som skrivs från höger till vänster
speglar fönstret och rapporten.

Alla 35 språk är tillgängliga, med två lager av täckning:

| | Täckning |
|---|---|
| Tolkningsrapporter och alla motorns felmeddelanden | **alla 35 språk** |
| Programmets egna menyer, knappar och etiketter | **alla 35 språk** |

Språken som skrivs från höger till vänster — arabiska, egyptisk arabiska, persiska och urdu —
speglar fönstret och rapporten från det ögonblick programmet öppnas.

Språket kommer ihåg mellan körningar. För att öppna på ett annat språk bara en gång, se
[`--language`](#köra-programmet).

Några saker lämnas medvetet oöversatta på alla språk: de korta fältnamnen bredvid råa värden
(`format`, `position`, `raw`, `len` …), som är identifierare och inte ord; namnen på exemplen och
symbologierna, som är GS1- och branschtermer; och ett långt stycke standardtext där en halv
översättning skulle vilseleda.

---

## Vad som kommer ihåg

Två filer i `.gaia-desktop` i din hemkatalog:

| Fil | Innehåller |
|---|---|
| `options.properties` | Alla tolkningsalternativ, temat, om alternativpanelen är öppen, den senast använda skannern, det läge fönstret lämnades i, och uppdateringskontrollens inställningar — på eller av, när den senast kördes och eventuell version du valt att hoppa över |
| `captures.tsv` | Registreringshistoriken, nyast först, med högst 500 poster |

Alternativen skrivs varje gång du ändrar ett; historiken varje gång den ändras. Båda skrivs om
vid avslut.

Att ta bort någon av filerna nollställer bara den delen och inget annat — de hålls isär för att
en trasig historik inte ska kosta dig dina inställningar. En fil som gått sönder vid manuell
redigering är inte heller ödesdiger: varje inställning faller tillbaka var för sig, så en felaktig
rad kostar bara den raden.

Historiken sparar indatan, inte analysen. Den tolkas om vid inläsning, vilket betyder att en
återställd registrering speglar de alternativ som gäller **nu**, inte de som gällde när den
gjordes.

Streckkoderna i läget Kombinera sparas inte: efter en omstart börjar Kombinera
tomt.

---

## Tangentbordsgenvägar

| | |
|---|---|
| <kbd>Retur</kbd> | Tolka inmatningsfältet |
| <kbd>⌘O</kbd> | Öppna en textfil |
| <kbd>⌘S</kbd> | Spara rapporten som HTML |
| <kbd>⌘P</kbd> | Spara rapporten som PDF (systemets utskriftsdialog) |
| <kbd>⌘G</kbd> | Infoga FNC1-avgränsartoken |
| <kbd>⌘K</kbd> | Anslut / koppla från skannern (endast Windows) |
| <kbd>⌘D</kbd> | Växla mörkt tema |
| <kbd>⌘Q</kbd> | Avsluta |

I redigeraren: <kbd>Tabb</kbd> / <kbd>Skift</kbd>+<kbd>Tabb</kbd> flyttar mellan redigerbara
celler, <kbd>Retur</kbd> bekräftar, <kbd>Esc</kbd> avbryter.

I läget Kombinera: <kbd>Retur</kbd> i fältet *Streckkod* lägger till
streckkoden. Där skriver du FNC1 som `{GS}`; <kbd>⌘G</kbd> hör till inmatningsfältet i läget
Tolka.

---

## Felsökning

| Symtom | Orsak och åtgärd |
|---|---|
| Programmet startar inte, eller rapporterar `NoClassDefFoundError` | Katalogen `lib/` måste ligga bredvid JAR-filen. Håll ihop dem, eller använd en fristående distribution |
| *”Unsupported JavaFX configuration…”* i konsolen vid start | Förväntat och harmlöst — verktygslådan noterar bara hur den lästes in. Programmet fungerar normalt |
| En inklistrad avgränsare verkar försvinna | Så ska det vara: den skrivs om till den synliga token `{GS}`, och tolken får fortfarande det riktiga tecknet. Se [{GS}-token](#inmatningsfältet-och-gs-token) |
| Det finns ingen *Anslut skanner*-knapp | Du kör macOS eller Linux, där skannerdrivrutinerna inte finns. Se [Skanning](#skanning) |
| Skannern ansluter inte | Kontrollera att det logiska namnet i väljaren stämmer med din modell, att skannern är ansluten och att inget annat program håller den. [Installationsguiden för Zebra-skannrar](GaiaDesktop-Zebra-Scanner-Windows-Swedish.md#11-felsökning) listar varje felkod. |
| PDF-exporten säger att det inte finns någon destination | Datorn har varken skrivare eller PDF-destination konfigurerad. Lägg till en, eller exportera HTML och skriv ut från en webbläsare |
| Ett menyalternativ visar något i stil med `act.hri` | En etikett saknas — rapportera det gärna; det är ett byggfel, inte en inställning |

---

## Begränsningar

- **Skanning fungerar bara på Windows**, eftersom drivrutinerna gör det.
- **PDF går via utskriftsdialogen** i stället för en spardialog.
- **Digital Link-mål tar emot en domän och ett sökvägsprefix**, inte extra frågeparametrar eller
  ett fragment.
- **En AI får bara förekomma en gång** i en elementsträng — det är GS1:s regel, inte en
  begränsning i programmet.
- **Kombinera tar bara GS1-128-streckkoder, högst tre**, och sparar dem inte mellan
  körningar.
