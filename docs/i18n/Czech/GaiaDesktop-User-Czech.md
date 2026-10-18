# GAIA Desktop — Uživatelská příručka

Jak aplikaci používat. O sestavení, úpravách a přidání jazyka pojednává
[příručka pro vývojáře](../../GaiaDesktop-Developer.md) (anglicky). Původní dokument:
[GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Obsah

1. [Co to je](#co-to-je)
2. [Spuštění](#spuštění)
3. [Okno](#okno)
4. [Režim Analýza](#režim-analýza)
   - [Vstupní řádek a token {GS}](#vstupní-řádek-a-token-gs)
   - [Snímání](#snímání)
   - [Otevření souboru](#otevření-souboru)
   - [Historie záznamů](#historie-záznamů)
5. [Režim Sestavení](#režim-sestavení)
   - [Editor AI](#editor-ai)
   - [Co editor odmítá](#co-editor-odmítá)
   - [Řetězec prvků, nebo Digital Link](#řetězec-prvků-nebo-digital-link)
   - [Úprava existujícího záznamu](#úprava-existujícího-záznamu)
6. [Report](#report)
   - [Export](#export)
7. [Možnosti analýzy](#možnosti-analýzy)
   - [První spuštění](#první-spuštění)
8. [Jazyky](#jazyky)
9. [Co se pamatuje](#co-se-pamatuje)
10. [Klávesové zkratky](#klávesové-zkratky)
11. [Řešení potíží](#řešení-potíží)
12. [Omezení](#omezení)

---

## Co to je

GAIA Desktop dělá s daty GS1 dvě věci:

- **Analyzuje** — napište, vložte, otevřete nebo naskenujte řetězec prvků GS1 či URI
  Digital Link a přečtěte si výsledek jako úplný report: co který aplikační identifikátor (AI)
  znamená, co je špatně a proč;
- **Sestavuje** — skládá dvojice AI / hodnota do řetězce prvků nebo do URI Digital Link.

Všechno, co aplikace o GS1 ví — každou definici AI, každé pravidlo kontroly, každou chybovou
zprávu i každý lokalizovaný popis — přebírá od jádra GAIA pod sebou. Aplikace nepřidává vlastní
pravidla: přijímá přesně to, co přijímá jádro.

---

## Spuštění

```bash
java -jar gaia-desktop.jar
```

Soubor JAR potřebuje adresář `lib/`, který se dodává vedle něj — nechte je pohromadě, jinak se
aplikace nespustí. (Lze sestavit i jediný samostatný soubor JAR; viz
[příručka pro vývojáře](../../GaiaDesktop-Developer.md#packaging).)

Musí být nainstalována **Java 21 nebo novější**. Distribuce je vázaná na jeden operační systém a
jednu architekturu procesoru — použijte soubor JAR sestavený pro váš počítač.

Chcete-li aplikaci otevřít v určitém jazyce, aniž byste změnili jazyk uložený:

```bash
java -jar gaia-desktop.jar --language=FR
```

Hodnotou může být kód katalogu (`FR`, `PCM`, `ARZ`), celý název (`FRENCH`, `WU-CHINESE`) nebo
jazyková značka (`fr`, `zh-HK` pro kantonštinu, `ar-EG` pro egyptskou arabštinu) — na velikosti
písmen nezáleží. Volba platí jen pro toto spuštění: uložený jazyk zůstává nedotčen, takže
zástupce vytvořený pro kolegu nezmění jazyk, v němž se otevírá vaše vlastní kopie. Vybrat jazyk
*uvnitř* aplikace je naproti tomu skutečná volba a ukládá se jako obvykle. Hodnota, která
neoznačuje žádný jazyk, se s poznámkou v konzoli ignoruje a aplikace se otevře v uloženém
jazyce, místo aby odmítla nastartovat.

Při startu vypíše konzole jeden řádek s verzí aplikace, datem sestavení a verzí jádra. Právě ten
patří do hlášení chyby.

---

## Okno

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

> Schéma ponechává anglické popisky, stejně jako všechny bloky kódu v této dokumentaci. Na
> obrazovce uvidíte *Analýza* / *Sestavení*, *Záznamy*, *Uložit report*, *Možnosti*,
> *skener: vypnuto* a *O aplikaci*.

**V macOS a Linuxu řádek se skenerem na tomto obrázku vůbec není** — na panelu nástrojů není
přepínač ani výběr zařízení, chybí nabídka *Skener* i *skener: vypnuto* ve stavovém řádku. Viz
[Snímání](#snímání).

Přepínač **Analýza ⇄ Sestavení** mění pouze pruh pod panelem nástrojů a ovládací prvky pro
snímání. Historie, report, panel možností i příkazy exportu slouží oběma režimům, protože úspěšné
sestavení se prostě stane dalším záznamem.

Panel možností je **při prvním spuštění zavřený**. Režim, viditelnost panelu, motiv i naposledy
použitý skener se mezi spuštěními pamatují.

---

## Režim Analýza

### Vstupní řádek a token {GS}

Řádek přijímá řetězec prvků, URI GS1 Digital Link nebo surová data ze snímání včetně předpony
AIM Code ID (`]d2…`). <kbd>Enter</kbd> spustí analýzu.

AI proměnné délky odděluje **FNC1** — neviditelný řídicí znak `0x1D`. Textové pole tento znak
neudrží: byl by tiše odstraněn a `01…` + FNC1 + `10LOT` by dorazilo jako `01…10LOT`, tedy jako
řetězec prvků s jiným významem.

Aplikace proto zobrazuje oddělovač jako viditelný token **`{GS}`** a cestou k analyzátoru jej
převádí zpět:

- **tlačítko `{GS}`** vloží token na pozici kurzoru;
- **vložení** skutečných oddělovacích znaků — klávesnicí, z místní nabídky nebo přetažením — je
  automaticky změní na `{GS}`, takže se nic neztratí;
- token se zapisuje přesně tak, jak jej zapisuje report, takže hodnotu zkopírovanou z reportu
  lze vložit zpět beze změny.

Záznam, report i uložená historie obsahují **skutečný** oddělovač. Token je jen to, co vidíte a
píšete.

### Snímání

> **Pouze Windows.** Zebra dodává ovladače skeneru jen pro Windows, a proto v macOS a Linuxu
> aplikace žádné ovládací prvky skeneru nevytváří — není přepínač *Připojit skener*, není výběr
> zařízení, není nabídka *Skener* ani stav skeneru ve stavovém řádku. Nic není zašedlé, protože
> neexistuje nastavení ani zařízení, které by to mohlo zapnout. Vše ostatní, včetně otevření
> souboru s uloženými snímky, funguje všude.

*Připojit skener* otevře zařízení uvedené ve výběru; `ZebraAllScanners` je výchozí hodnota a
vyhovuje většině modelů. Každé snímání se analyzuje a automaticky přidá do historie.

Data ze skeneru se čtou bajt po bajtu, takže předpona AIM Code ID i každý oddělovač FNC1
dorazí k analyzátoru přesně tak, jak je skener odeslal.

V režimu Sestavení jsou ovládací prvky skeneru skryté, ale připojený skener dál pracuje:
*Skener → Připojit / Odpojit* (<kbd>⌘K</kbd>) jej stále přepíná, stavový řádek o něm stále
informuje a snímek, který dorazí při otevřeném editoru, se stejně zaznamená.

Pokud se zařízení nepřipojí, viz [Řešení potíží](#řešení-potíží).

### Otevření souboru

*Soubor → Otevřít textový soubor…* analyzuje **každý neprázdný řádek jako samostatný záznam** —
je to rychlý způsob, jak zpracovat dávku snímků, kterou vám někdo poslal. Soubor se čte bajt po
bajtu, takže surové oddělovače v něm přežijí.

### Historie záznamů

Nejnovější nahoře. Každý řádek ukazuje tečku platný / neplatný, jednořádkové shrnutí a dále čas
záznamu, původ a počet AI. Datum se objevuje jen u záznamů z dřívějších dnů; dnešní řádky
ukazují pouze čas.

Shrnutím je u Digital Link **URI Digital Link**, jinak **HRI**: u řetězce prvků se
`(01)09506000134352 (17)261231` čte lépe než `01095060001343521726123110…`.

Klepnutím pravým tlačítkem na záznam zkopírujete jeho vstup nebo HRI, vrátíte jej do vstupního
řádku, otevřete v editoru, odeberete jej nebo vymažete historii.

---

## Režim Sestavení

### Editor AI

Tabulka dvojic AI / hodnota. Napište kód AI nebo vyberte ze **všech AI, které jádro zná**, v
seznamu *Přidat AI…*; sloupce **Název**, **Formát** a **Popis** se během psaní vyplňují ve
zvoleném jazyce. Seznam nenabídne AI, které by sestavení odmítlo.

Na konci tabulky zůstává vždy **jeden prázdný řádek**: napište do něj a objeví se další, takže
řetězec s několika AI nikdy nepotřebuje *Přidat řádek*.

Úpravy se chovají tak, jak se tabulka chovat má: <kbd>Enter</kbd>, <kbd>Tab</kbd>,
<kbd>Shift</kbd>+<kbd>Tab</kbd> i klepnutí jinam **zachovají** napsané; <kbd>Esc</kbd> je
zahodí. <kbd>Tab</kbd> jde z AI na Hodnotu a dál na následující řádek a přeskakuje sloupce,
které upravovat nelze.

### Co editor odmítá

| | Co se stane |
|---|---|
| AI, které jádro nezná | Řádek zčervená už během psaní; sestavení je odmítnuto s vysvětlením |
| Totéž AI podruhé, ze seznamu | Nepřidá se — zpráva je pojmenuje a vybere se řádek, který je už obsahuje |
| Totéž AI podruhé, napsané ručně | Označí se oba řádky; sestavení je odmítnuto s chybou `GE-S004` |
| Není co sestavit | Zpráva; žádný záznam nevznikne |

AI se smí v řetězci prvků vyskytnout jen jednou — proto seznam odmítne hned, místo aby nechal
zkusit předem odsouzené sestavení.

### Řetězec prvků, nebo Digital Link

**Sestavit řetězec prvků** spojí řádky a vloží oddělovač FNC1 všude tam, kde to norma vyžaduje.
**Sestavit Digital Link** vytvoří URI; rozbalte *Cíl Digital Link*, chcete-li jej nasměrovat na
vlastní resolver místo kanonického `id.gs1.org`:

| Pole | Účinek | Prázdné znamená |
|---|---|---|
| Doména | Hostitel resolveru, např. `example.com` | `id.gs1.org` |
| Předpona cesty | Úsek před cestou AI, např. `/products` | bez předpony |

Při **úspěchu** se výsledek chová přesně jako snímek: je analyzován, přidán do historie,
vykreslen jako report a uložen. Sestavení se ověřuje tím, že analyzuje svůj vlastní výstup —
cokoli vrátí, je tedy zaručeně platné.

Při **neúspěchu** se nic nezaznamená. Důvod zůstane pod tlačítky sestavení, jeden řádek na každý
problém, vždy s chybou, příslušným AI a vlastním vysvětlením jádra:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> Zdvojené uvozovky v této zprávě pocházejí z katalogu chyb jádra, nikoli z této aplikace.

### Úprava existujícího záznamu

Klepnutí pravým tlačítkem na záznam → **Sestavení** načte jeho AI do editoru. Změňte hodnotu,
sestavte znovu a výsledek přibude jako nový záznam — původní zůstane nedotčen.

---

## Report

Vybraný záznam se vykreslí jako úplný report: pruh verdiktu, řetězec prvků rozložený do
popsaného „pásu“, metadata analýzy, jedna karta na každé AI s jeho interpretacemi a chyby i
varování s návodem k nápravě.

U **Digital Link** nese každá karta AI navíc svou strukturní roli — **primární klíč**,
**kvalifikátor klíče** nebo **datový atribut** — barevně odlišenou v pořadí, v jakém se objevují
v URI. Role se zobrazují jen u Digital Link: v prostém řetězci prvků nemá AI žádnou pozici v
URI, kterou by šlo popsat.

Report se řídí jazykem a motivem, které jste zvolili.

### Export

| | |
|---|---|
| **Uložit report → Jako HTML…** (<kbd>⌘S</kbd>) | Zapíše přesně to, co je na obrazovce: jediný samostatný soubor, který se otevře v libovolném prohlížeči |
| **Uložit report → Jako PDF…** (<kbd>⌘P</kbd>) | Vytiskne report přes systémové okno tisku — cíl PDF vyberte tam |

PDF prochází oknem tisku proto, že se tam vybírá cíl i název souboru: v macOS
**PDF ▾ → Uložit jako PDF**, ve Windows **Microsoft Print to PDF**, v Linuxu **Tisk do souboru**.
Stránky se správně rozdělí a text zůstane vybratelný.

Tisk i PDF používají vždy světlou paletu, i když je aplikace v tmavém motivu, a zachovávají
barevné štítky AI i pruh verdiktu.

Nemá-li počítač nastavenou žádnou tiskárnu ani cíl PDF, aplikace to oznámí a odkáže na export do
HTML, který se dá vytisknout z libovolného prohlížeče.

---

## Možnosti analýzy

Panel vpravo shromažďuje všechny možnosti jádra. Změna kterékoli z nich **okamžitě znovu
analyzuje vybraný záznam**, takže panel slouží i k otázce „jak by tento vstup vypadal s tímhle
nastavením?“, nejen k ovlivnění dalšího snímku.

| Možnost | Hodnoty |
|---|---|
| Režim analýzy | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — jak hluboko analýzu vést |
| Jazyk | 35 katalogů zpráv |
| Pořadí | `LITTLE` (dd/mm/rrrr), `MIDDLE` (mm/dd/rrrr), `BIG` (rrrr/mm/dd) |
| Oddělovač | `SLASH`, `HYPHEN`, `PERIOD` |
| Měsíc | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Rok | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Přeskočit kontrolu „requires“ | Přestane odmítat AI, jemuž chybí povinný protějšek (`GE-S005`) |
| Přeskočit kontrolu „excludes“ | Přestane odmítat kombinace AI, které norma zakazuje (`GE-S006`) |

*Obnovit výchozí* v dolní části panelu vrátí vlastní výchozí hodnoty jádra.

### První spuštění

Dokud není nic uloženo, bere aplikace nastavení z **místních nastavení vašeho počítače**, nikoli
z výchozích hodnot jádra:

- **jazyk** pochází z národního prostředí systému, s návratem k angličtině, není-li pro ně
  katalog;
- **možnosti data** se odvozují z krátkého formátu data vašeho národního prostředí: pořadí
  složek, oddělovač a to, zda se měsíc a rok zkracují.

> **Poznámka.** Národní prostředí, které píše krátká data s dvouciferným rokem, tedy i zde začne
> dvouciferným rokem: odpovídá to zbytku systému, ale liší se to od výchozí hodnoty jádra, která
> je čtyřciferná. Změňte ji v panelu a volba zůstane.

---

## Jazyky

Volba jazyka změní **celou aplikaci** — nabídky, tlačítka, editor, panel možností, stavový řádek
i report — nejen chybové zprávy. Jazyky psané zprava doleva překlopí okno i report.

K dispozici je všech 35 jazyků, ve dvou vrstvách pokrytí:

| | Pokrytí |
|---|---|
| Reporty analýzy a všechny chybové zprávy jádra | **všech 35 jazyků** |
| Nabídky, tlačítka a popisky samotné aplikace | **všech 35 jazyků** |

Jazyky psané zprava doleva — arabština, egyptská arabština, perština a urdština — překlápějí okno
i report od okamžiku, kdy se aplikace otevře.

Jazyk se mezi spuštěními pamatuje. Chcete-li otevřít v jiném jazyce jen jednou, viz
[`--language`](#spuštění).

Několik věcí zůstává ve všech jazycích záměrně nepřeloženo: stručné názvy polí vytištěné vedle
surových hodnot (`format`, `position`, `raw`, `len` …), což jsou identifikátory, nikoli slova;
názvy ukázek a symbolik, což jsou termíny GS1 a oboru; a jeden dlouhý odstavec normy, kde by
poloviční překlad mátl.

---

## Co se pamatuje

Dva soubory v adresáři `.gaia-desktop` ve vašem domovském adresáři:

| Soubor | Obsahuje |
|---|---|
| `options.properties` | Všechny možnosti analýzy, motiv, zda je panel možností otevřený, naposledy použitý skener a režim, v němž bylo okno opuštěno |
| `captures.tsv` | Historii záznamů, nejnovější nahoře, nejvýše 500 položek |

Možnosti se zapisují při každé změně, historie při každé své změně. Oba soubory se znovu zapíší
při ukončení.

Smazání kteréhokoli ze souborů vrátí do výchozího stavu jen tuto část a nic jiného — jsou
oddělené proto, aby vás poškozená historie nepřipravila o nastavení. Ani soubor pokažený ruční
úpravou není fatální: každé nastavení se vrací k výchozí hodnotě zvlášť, takže jeden chybný
řádek stojí jen ten řádek.

Historie ukládá vstup, nikoli výsledek analýzy. Při načtení se analyzuje znovu, což znamená, že
obnovený záznam odráží možnosti platné **nyní**, ne ty, které platily při jeho pořízení.

---

## Klávesové zkratky

| | |
|---|---|
| <kbd>Enter</kbd> | Analyzovat obsah vstupního řádku |
| <kbd>⌘O</kbd> | Otevřít textový soubor |
| <kbd>⌘S</kbd> | Uložit report jako HTML |
| <kbd>⌘P</kbd> | Uložit report jako PDF (systémové okno tisku) |
| <kbd>⌘G</kbd> | Vložit token oddělovače FNC1 |
| <kbd>⌘K</kbd> | Připojit / odpojit skener (jen Windows) |
| <kbd>⌘D</kbd> | Přepnout tmavý motiv |
| <kbd>⌘Q</kbd> | Ukončit |

V editoru: <kbd>Tab</kbd> / <kbd>Shift</kbd>+<kbd>Tab</kbd> přecházejí mezi upravitelnými
buňkami, <kbd>Enter</kbd> potvrzuje, <kbd>Esc</kbd> ruší.

---

## Řešení potíží

| Projev | Příčina a náprava |
|---|---|
| Aplikace se nespustí nebo hlásí `NoClassDefFoundError` | Adresář `lib/` musí ležet vedle souboru JAR. Nechte je pohromadě, nebo použijte samostatnou distribuci |
| *„Unsupported JavaFX configuration…“* v konzoli při startu | Očekávané a neškodné — knihovna jen uvádí, jak byla načtena. Aplikace běží normálně |
| Vložený oddělovač jako by zmizel | Tak to má být: je přepsán na viditelný token `{GS}` a analyzátor stále dostává skutečný znak. Viz [token {GS}](#vstupní-řádek-a-token-gs) |
| Chybí tlačítko *Připojit skener* | Jste v macOS nebo Linuxu, kde ovladače skeneru neexistují. Viz [Snímání](#snímání) |
| Skener se nepřipojí | Ověřte, že logický název ve výběru odpovídá vašemu modelu, že je skener připojen a že jej nedrží jiná aplikace |
| Export do PDF hlásí, že není žádný cíl | Počítač nemá nastavenou tiskárnu ani cíl PDF. Přidejte jej, nebo exportujte do HTML a vytiskněte z prohlížeče |
| Položka nabídky ukazuje něco jako `act.hri` | Chybí popisek — nahlaste to prosím; jde o vadu sestavení, ne o nastavení |

---

## Omezení

- **Snímání funguje jen ve Windows**, protože jen tam existují ovladače.
- **PDF prochází oknem tisku**, nikoli oknem uložení.
- **Cíle Digital Link přijímají doménu a předponu cesty**, nikoli další parametry dotazu ani
  fragment.
- **AI se smí vyskytnout jen jednou** v řetězci prvků — to je pravidlo GS1, ne omezení aplikace.
