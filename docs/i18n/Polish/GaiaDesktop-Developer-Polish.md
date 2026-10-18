# GAIA Desktop — Podręcznik programisty

Jak moduł jest budowany, zorganizowany i testowany. To, co aplikacja robi widziana z zewnątrz, opisuje
[Podręcznik użytkownika](GaiaDesktop-User-Polish.md). Dokument oryginalny:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Spis treści

1. [Moduł w skrócie](#moduł-w-skrócie)
2. [Budowanie i uruchamianie](#budowanie-i-uruchamianie)
   - [Pakowanie](#pakowanie)
3. [Architektura](#architektura)
   - [Lejek przechwytywania](#lejek-przechwytywania)
   - [Wątki](#wątki)
   - [Przebudowa czy zmiana etykiet](#przebudowa-czy-zmiana-etykiet)
4. [Token {GS}](#token-gs)
5. [Integracja skanera](#integracja-skanera)
   - [Bariera platformy](#bariera-platformy)
   - [Symbologie](#symbologie)
6. [Lokalizacja](#lokalizacja)
   - [Argument `--language`](#argument---language)
   - [Dodawanie języka](#dodawanie-języka)
   - [Co celowo nie jest tłumaczone](#co-celowo-nie-jest-tłumaczone)
7. [Formaty zapisanego stanu](#formaty-zapisanego-stanu)
8. [Testy](#testy)
9. [Rozwiązywanie problemów z kompilacją](#rozwiązywanie-problemów-z-kompilacją)
10. [Znane ograniczenia i otwarte prace](#znane-ograniczenia-i-otwarte-prace)

---

## Moduł w skrócie

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Klient biblioteki rdzenia i nic więcej. Nie zawiera **żadnej wiedzy o GS1**: każda definicja AI, reguła
walidacji, komunikat o błędzie i zlokalizowany opis pochodzi z
[`GaiaParser`](../../../../gaia/docs/GaiaParser.md) i
[`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). W szczególności lista AI nigdy nie jest kopiowana do
tego modułu — selektor w kreatorze czyta rejestr samego rdzenia, więc nie może zaproponować AI, które kreator
by odrzucił.

**Punkty wejścia:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (`Application` JavaFX) oraz
`tools.pantheum.gaia.desktop.Launcher` (`Main-Class` spakowanego jara).

Moduł jest liściem: nic od niego nie zależy, a on nie zależy od żadnego innego modułu dalej w łańcuchu.

---

## Budowanie i uruchamianie

- **Java 21** do uruchamiania i JDK 21 lub nowszy do budowania — moduł kompiluje się z `release=21`. JavaFX
  21 to odpowiadająca linia LTS. Jar rdzenia to nadal bajtkod Javy 11 i działa bez zmian na 21.
- **Maven** w `PATH`. W tym repozytorium nie ma wrappera.
- **Najpierw zainstalowany rdzeń.** Ten moduł rozwiązuje `gaia:1.1.0` z `~/.m2`; nieaktualny lub brakujący
  jar rdzenia to zwykła przyczyna błędu `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` to jedyny argument wiersza poleceń i jedyny sposób wpływania na start poza
`-Dgaia.desktop.home=…`, które przenosi cały profil w inne miejsce.

`mvn javafx:run` to ścieżka deweloperska: wtyczka umieszcza JavaFX na **module path**, więc toolkit startuje
jako moduł nazwany, a `GaiaDesktopApp` można uruchomić bezpośrednio. Spakowany jar tego nie potrafi —
wszystko jest na class path — i dlatego jego `Main-Class` to pośrednik `Launcher`. Podklasa `Application`
jako `Main-Class` w takim układzie kończy się błędem *"JavaFX runtime components are missing"*.

Zawsze używaj `clean` — raz, przed zbudowaniem zestawu platform, ponieważ opróżnia `target/dist/` ze
wszystkich. Dlaczego — patrz [Rozwiązywanie problemów z kompilacją](#rozwiązywanie-problemów-z-kompilacją).

### Pakowanie

Wszystko, co trafia do dystrybucji, jest składane w `target/dist/gaia-desktop-<version>-<platform>/` i nic
więcej, więc katalog można spakować lub skopiować w całości, bez wybierania artefaktów z `target/` obok
`classes/`, `maven-status/` i reszty roboczych plików kompilacji. `${dist.dir}` nazywa go raz; każda wtyczka,
która zapisuje artefakt, wskazuje na niego.

#### Jedna kompilacja na platformę

Jary JavaFX zawierają kod natywny, więc każda dystrybucja jest dla jednej platformy, określonej właściwością
`fx.platform` jako klasyfikator OpenJFX: `mac-aarch64`, `mac`, `win`, `linux` lub `linux-aarch64`. Pięć
profili `fx-*` aktywowanych przez system operacyjny ustawia ją na platformę maszyny budującej, co wystarcza
zwykłemu `mvn package` lub `mvn javafx:run`; `-Dfx.platform=…` ma pierwszeństwo przed nimi, by zbudować
dowolną inną, z dowolnej maszyny — jary JavaFX to tylko pliki do pobrania. `${dist.dir}` to
`target/dist/${fx.platform}`, więc kompilacja jednej platformy nigdy nie nadpisuje innej.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Dwa szczegóły pomu sprawiają, że to działa, i oba wyglądają na zbędne:

- **Każdy moduł JavaFX jest bezpośrednią zależnością** — base, graphics, controls, media i web — każdy z
  `<classifier>${fx.platform}</classifier>` i wykluczeniem `org.openjfx:*`. Pomy OpenJFX wybierają swoje
  przechodnie jary według systemu *maszyny budującej*, więc pozwolenie, by graphics lub base przyszły
  przechodnio, wkłada natywne biblioteki Maca do kompilacji dla Windows obok tych dla Windows.
- **Właściwość to `fx.platform`, nie `javafx.platform`.** Ustawiona z zewnątrz, własna właściwość OpenJFX
  zostaje przez jego pom nadrzędny zdefiniowana na nowo w kategoriach samej siebie; Maven odrzuca ten pom
  jako nieprawidłowy i po cichu pomija jary platformy, co daje kompilację bez JavaFX.

Oba układy zapisują tę samą ścieżkę, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`, i w
każdej kompilacji działa dokładnie jeden z nich. `${jar.phase}`, `assembly.skipAssembly` i `mdep.skip`
sprawiają, że nie zgłaszają do niej roszczeń oba naraz.

| | Domyślny | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — tylko klasy i zasoby tego modułu | ~50 MB — zależności rozpakowane do środka |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 jarów, wymienionych pojedynczo w `Class-Path` manifestu | — |
| Do dystrybucji | jar **i** `lib/` obok niego | jeden plik |
| Także w `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) lub `run.sh` (pozostałe) | to samo |
| Oraz | `readme.html` i `docs/` (każda platforma) | to samo |

Każda dystrybucja zawiera też podręczniki: `docs/` skopiowany z zachowaniem struktury, tak by odnośniki
między podręcznikami a tłumaczeniami nadal działały, bez narzędzi tłumaczeniowych (`check-i18n.py`,
`i18n-manifest.json`); oraz `readme.html` w katalogu głównym, indeks z odnośnikami do
każdego z nich, z `src/main/dist/readme.html` z `@VERSION@` przefiltrowanym do wersji z pomu. Oba zapisuje
krok antrun `pick-launch-script`, który najpierw usuwa `docs/`, żeby usunięty podręcznik nie pozostał.
Odnośniki w readme są pisane ręcznie — przemianowany podręcznik lub tłumaczenie dodane w `docs/` wymaga
również własnego wiersza w nim.

`Class-Path` manifestu i `lib/` to dwie połowy jednej rzeczy: jar wymienia `lib/<artifact>-<version>.jar`
dla każdej zależności czasu uruchomienia, a `copy-dependencies` wypełnia `lib/` dokładnie tymi nazwami. To
właśnie sprawia, że działa `java -jar`.

#### Aplikacja macOS i archiwum dystrybucyjne

Jedno wykonanie antrun, `mac-app-and-zip`, kończy fazę `package`. Wtyczka antrun jest z tego powodu
deklarowana **po** wtyczkach jar i assembly — Maven uruchamia wykonania fazy w kolejności deklaracji
wtyczek — więc jar istnieje, w obu sposobach pakowania, gdy ono działa. (Jej drugie wykonanie,
`pick-launch-script`, jest w `prepare-package` i nie jest tym objęte.)

Dla `mac` i `mac-aarch64` najpierw buduje `GAIA Desktop.app` w katalogu dystrybucji: `Info.plist` (z
`@VERSION@` przefiltrowanym do wersji z pomu), program uruchamiający `Contents/MacOS/gaia-desktop` (z prawem
wykonywania) i `gaia-desktop.icns`, wszystko z `src/main/macos/`, a następnie **przenosi** `gaia-desktop.jar`
i `lib/` do `Contents/Java`, aby pakiet był samodzielny. `run.sh` szuka tam, gdy obok niego nie ma jara.
Program uruchamiający znajduje Javę po wersji (`java_home -v 21+`, potem openjdk z Homebrew — uruchomienia z
Findera nie widzą `JAVA_HOME` ani `PATH`), gdy jej nie ma, pokazuje okno dialogowe przez `osascript`, robi
`cd` do `~/.gaia-desktop`, aby względny katalog `logs/` logbacka był zapisywalny, i przekazuje
`-Xdock:name`/`-Xdock:icon`. macOS kojarzy uruchomioną `java` z pakietem (jego identyfikator pakietu i ikona
w Docku to te aplikacji); pasek menu pokazuje GAIA Desktop.

Następnie, dla każdej platformy, pakuje katalog do `target/dist/gaia-desktop-<version>-<platform>.zip` pod
folderem o tej samej nazwie, za pomocą `<zip>` z Anta: `run.sh` i `Contents/MacOS/**` jako osobny zestaw
plików z `filemode="755"`, inaczej po rozpakowaniu nie dałyby się uruchomić. Zip **nie jest dołączany**: jest
przeznaczony do wydań na GitHubie, a cztery platformy pochodzą z czterech kompilacji, których Maven Central —
jedno niezmienne przesłanie na wersję — nigdy nie przyjąłby jako jednego wydania.

`project.build.outputTimestamp` jest ustawiony na czas kompilacji. W przeciwnym razie archiwizatory w
maven-jar-plugin 3.5 i maven-assembly-plugin 3.8 datują każdy wpis na 1 lutego 1980.

Ikonę aplikacji rysuje `tools/icon/GaiaIcon.java` (nie jest częścią kompilacji; jego nagłówek podaje kroki
`sips`/`iconutil`). Jego wyniki są w repozytorium: `src/main/macos/gaia-desktop.icns` dla pakietu oraz
`src/main/resources/icons/gaia-desktop-{16..256}.png`, które `GaiaDesktopApp` ustawia jako ikonę okna
(Windows i Linux; macOS ignoruje ikony okien).

#### Skrypty uruchomieniowe

`run.sh` i `run.bat` pochodzą z `src/main/scripts/` i są kopiowane do `dist/gaia-desktop-<version>-<platform>/`
bez filtrowania — `run.sh` jest pełen rozwinięć powłoki `${...}`, które filtrowanie Mavena mogłoby swobodnie
przepisać. Krok antrun zostawia potem tylko ten, który uruchamia dana platforma — `run.bat` dla `win`,
`run.sh` dla wszystkich pozostałych — i ustawia bit wykonywania na `run.sh`, ponieważ `copy-resources` go nie
zachowuje. Decyduje na podstawie rozwiązanego `${fx.platform}`, a nie przez profile, ponieważ aktywacja
profilu nie widzi wartości ustawionej przez inny profil: kompilacja dla Linuksa wykonana na Windows
dostałaby inaczej skrypt hosta.

**Nie polegają na manifeście.** Każdy sam buduje class path: `gaia-desktop.jar`, a za nim każdy `.jar` z
`lib/`, wymieniane pojedynczo zamiast zostawiać to symbolowi wieloznacznemu `lib/*`, aby ścieżka ze spacjami
pozostała jednym wpisem, a liczba dodanych elementów była prawdziwa. Uruchamianie z `-cp` jest powodem, dla
którego jawnie wymieniają `tools.pantheum.gaia.desktop.Launcher`: `-cp` nie czyta `Main-Class` z manifestu.
**Nadal** jednak podąża za jego `Class-Path` — jar na `-cp` wnosi jary wymienione w swoim manifeście,
przeszukiwane zaraz po nim — więc każdy jar z `lib/` jest ładowany niezależnie od tego, czy skrypt go
wymienia, a pominięcie jednego w class path skryptu nie trzyma go z dala od JVM. Skrypty i tak je
wymieniają, aby jar z nieaktualnym, ręcznie edytowanym lub niekompletnym manifestem mimo to wystartował. Ten
sam skrypt obsługuje oba sposoby pakowania: jar `-Pfat` nie ma `lib/` do dodania i go nie potrzebuje,
dlatego też pusty `lib/` jest uwagą, a nie błędem.

#### Biblioteki natywne Zebra

Kompilacja nie dostarcza niczego od Zebry — żadnych DLL, żadnych jarów usług. W Windows pochodzą one ze
Scanner SDK firmy Zebra zainstalowanego na komputerze: `run.bat` dodaje jego jary usług `Jpos*.jar` i `bin\`
do class path oraz `bin\` do `PATH`, gdzie `System.loadLibrary` znajduje `CSJPOSScanner.dll` /
`CSJPOSScanner64.dll` (`java.library.path` domyślnie równa się `PATH`). API JavaPOS i jego program ładujący
konfigurację pochodzą zawsze z `lib\javapos-*.jar`, z SDK czy bez.

**`run.bat` nigdy nie dodaje własnego jara JavaPOS z SDK**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`.
Kiedyś to robił, pomijając `lib\javapos-*.jar`, aby uniknąć dwóch kopii API — ale `Class-Path` manifestu i
tak je ładował (patrz wyżej), więc JVM dostawała obie: klasy programu ładującego 4.x z `lib\`, a obok
`jpos/res/jpos.properties` i `jcl.xsd` z `javaPOS114.jar`. Plik właściwości sprawia, że
[`JposConfig`](#integracja-skanera) usuwa się na bok, a ten `jcl.xsd` przyjmuje wersje JavaPOS tylko do 1.10,
więc każdy wpis w `jpos.xml` (1.12–1.14) nie przechodził walidacji, rejestr pozostawał pusty, a każde
połączenie kończyło się błędem 109, zanim w ogóle dotarło do skanera. Wystarczy w ogóle mieć ten jar na class
path, nawet za jarami z `lib\`. Jary usług Zebry działają bez zmian na API 1.14 z `lib\` — otwieranie,
zajmowanie i skanowanie zostały tak sprawdzone na prawdziwym skanerze. Pliki DLL położone ręcznie obok
`run.bat` są nadal uwzględniane: skrypt ustawia wtedy `-Djava.library.path=<dist>;%PATH%`, dopisując `PATH`,
bo właściwość całkowicie zastępuje domyślną ścieżkę wyszukiwania.

`mvn javafx:run` nie robi nic z tego, więc zajęcie skanera z uruchomienia deweloperskiego w Windows wymaga
umieszczenia innymi sposobami jarów SDK na class path i jego `bin\` w `PATH`; w tym celu uruchamiaj z
`dist/`.

Poza class path obsługują to, czego nie robi `java -jar`: ustalają własny katalog, by działały z dowolnego
katalogu roboczego, przedkładają `JAVA_HOME` nad `PATH` i odrzucają JVM starszą niż 11 zdaniem, które podaje
rozwiązanie, zamiast `UnsupportedClassVersionError`. `GAIA_DESKTOP_OPTS` przekazuje opcje JVM. `run.bat`
używa `java.exe`, nie `javaw.exe`, aby konsola pozostała otwarta — Windows to jedyna platforma, na której
działa skaner, a jego stan i błędy JavaPOS trafiają właśnie tam.

`run.bat` wybiera też swoją Javę, JavaFX i usługi Zebry według tego, co znajdzie — dołączony `jdk*\`,
`javafx-sdk*\lib\` na module path, Scanner SDK Zebry pod `ZEBRA_HOME` — pomijając na class path kopie JavaFX
z `lib\`, gdy używa tych z SDK, i przekazuje parser SAX z JDK, proxy i magazyn certyfikatów Windows oraz
`--enable-native-access` na Javie 17 i nowszej. Sekcja README
[What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) zawiera tabelę. Zachowaj
końce wierszy CRLF w pliku: `cmd.exe` potrafi przeoczyć `call :label` w pliku wsadowym z samymi LF. `run.sh`
również przekazuje `--enable-native-access=ALL-UNNAMED` — JavaFX ładuje własne biblioteki natywne z `lib/`,
a przy JavaFX na class path `ALL-UNNAMED` to moduł, który to obejmuje — pomijane, jak w `run.bat`, tylko gdy
nie da się odczytać wersji Javy.

Każdy z układów zawiera biblioteki natywne JavaFX **jednej platformy**, `${fx.platform}`, więc żaden nie jest
przenośny między systemami operacyjnymi ani architekturami. Buduj raz na każdy cel — patrz
[Jedna kompilacja na platformę](#jedna-kompilacja-na-platformę).

> **Eclipse.** Wykonanie `copy-dependencies` zawiera instrukcję `<?m2e ignore?>`. m2e rozwiązuje otwarty
> projekt przestrzeni roboczej do jego katalogu `target/classes` zamiast do jara, a `copy-dependencies`
> odmawia skopiowania niespakowanego artefaktu (MDEP-187), co psułoby automatyczną kompilację za każdym
> razem, gdy projekt `gaia` jest otwarty obok tego. W konsekwencji kompilacja wykonana tylko w IDE nie
> wypełnia `target/dist/gaia-desktop-<version>-<platform>/lib/`; uruchom `mvn package`, zanim użyjesz
> samego jara.

---

## Architektura

| Pakiet | Zawiera |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (okno), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (logika tworzenia bez JavaFX), `AiPair` (jeden wiersz edytora) |
| `desktop.parse` | `ParseOptions` (obserwowalne `ParseConfig`), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — generator raportu, bez JavaFX i z testami jednostkowymi bez toolkitu |
| `desktop.i18n` | `UiStrings` — katalog etykiet |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### Lejek przechwytywania

Każde wejście — wpisane, wklejone, otwarte z pliku, zeskanowane lub utworzone — trafia do parsera jedną
metodą, `MainView.capture(String, ScanSource, String)`.

To jedyna decyzja projektowa, wokół której zbudowano aplikację. Utworzony ciąg nie potrzebuje własnego
raportu, własnej historii ani własnej ścieżki eksportu, ponieważ za lejkiem jest nie do odróżnienia od
zeskanowanego. Wszystko nowe, co wytwarza wejście, powinno przechodzić przez niego, a nie obok.

### Wątki

| Wątek | Wykonuje |
|---|---|
| Wątek aplikacji JavaFX | Całą analizę, renderowanie i interfejs. Gdy tabele AI są rozgrzane, analiza trwa poniżej milisekundy, więc nie potrzebuje wątku roboczego |
| `gaia-scanner` | Każde wywołanie skanera — otwarcie, zajęcie, włączenie, wyłączenie, zwolnienie, zamknięcie. Jeden długo żyjący wątek należący do `ScannerService`; patrz [Integracja skanera](#integracja-skanera) |
| Wątek zdarzeń JavaPOS | Wywołania zwrotne `ScannerService`. `MainView.FxScannerListener` przenosi każde z nich przez `Platform.runLater` |
| Tło | Oczekiwanie na `connect()` / `disconnect()` skanera (zajęcie może trwać do 5 s), odtwarzanie zapisanej historii przy starcie — co jednocześnie rozgrzewa parser — oraz sprawdzanie aktualizacji (`gaia-update-check`, jedno żądanie do GitHuba, wynik przekazywany przez `Platform.runLater`) |

**Nigdy nie zmieniaj zaznaczenia kontrolki ani nie niszcz kontrolki z wnętrza jej własnego zdarzenia
zaznaczenia.** Trzy osobne awarie tej aplikacji wzięły się właśnie z tego: resetowania selektora AI wewnątrz
własnego `clearAndSelect` okna podręcznego (`IndexOutOfBoundsException` z `ListViewBehavior`) oraz
przebudowywania panelu, do którego należy lista wyboru języka, z wnętrza obsługi zdarzeń tej listy. Oba są
teraz odraczane przez `Platform.runLater`, a powody zapisano w obu miejscach wywołania.

### Przebudowa czy zmiana etykiet

Zmiana języka **przebudowuje** obramowanie z katalogu — menu, pasek narzędzi, panel opcji — co jest
bezpieczne, bo nic z tego nie przechowuje stanu: `OptionsPane` jest dwukierunkowym widokiem `ParseOptions`,
więc wartości żyją w modelu, a świeży panel od razu je przejmuje.

Kontrolki przechowujące **dane użytkownika** są polami i zamiast tego otrzymują nowe etykiety na miejscu:
`BuilderPane.retext(UiStrings)` zachowuje wiersze AI, pola Digital Link i zaznaczenie. Przebudowa
wyrzuciłaby pracę w toku — co raz już się zdarzyło.

---

## Token {GS}

`TextField` JavaFX po cichu odrzuca każdy znak poniżej `0x20` (`TextInputControl.filterInput`). Nie
„wyświetla go dziwnie" — usuwa go. Dotyczy to wpisywania, wklejania, `setText` i `insertText`, więc
`setText("01…" + GS + "10LOT")` daje `"01…10LOT"`: ciąg elementów, którego znaczenie po cichu się zmieniło,
bo AI o zmiennej długości połyka teraz to, co po nim następowało.

`desktop.parse.GsToken` to cała odpowiedź. Separator wędruje przez interfejs jako dosłowny tekst `{GS}` i
po drodze do parsera znów staje się `0x1D`. Token nie został tu wymyślony — dokładnie tak
`ASCIIRevealerUtils.printable` z rdzenia wyświetla `0x1D`, więc wartość skopiowaną z raportu można wkleić z
powrotem do paska wprowadzania bez zmian.

Filtr `TextFormatter` na polu wejściowym przechwytuje każdą drogę, którą może przyjść tekst — wpisywanie, ⌘V,
menu kontekstowe, przeciąganie i upuszczanie, `setText` — dlatego przepisywanie odbywa się tam, a nie w
każdej obsłudze zdarzeń.

Ścieżka skanera nigdy nie przechodzi przez `GsToken`: skany docierają do parsera jako bajty.

---

## Integracja skanera

`ScannerService` posiada kontrolkę `Scanner` JavaPOS: otwarcie → zajęcie → włączenie, z ponownym
uzbrajaniem zdarzenia danych po każdym skanie. Logiczne nazwy urządzeń pochodzą z
`src/main/resources/jpos.xml`.

**Każde wywołanie urządzenia działa na jednym wątku, `gaia-scanner`, który żyje tak długo jak usługa** —
nigdy na wątku FX i nigdy na wątku, który kończy się po wywołaniu. Usługa Zebry dociera do skanera przez JNI
poprzez API COM CoreScanner, a COM wiąże to, co przygotowuje, z apartamentem wątku wywołującego: otwórz na
wątku, który potem się kończy, a skaner zostanie zajęty, ale będzie milczał; zamknij na innym wątku, a
deinicjalizowany zostanie niewłaściwy apartament. Zamknięcie na wątku FX to najgorszy przypadek, bo JavaFX
wywołuje tam `OleInitialize` i rejestruje na nim cel upuszczania każdego okna — `CoUninitialize` na nim
zabija przeciąganie i upuszczanie w całej aplikacji. To JavaFX na Windows, a nie konkretna wersja JavaFX.
`connect()` i `disconnect()` przekazują pracę do tego wątku i czekają, więc `MainView` wywołuje obie z
krótko żyjącego wątku w tle.

Dwie zasady chronią przed tym, by źle zachowujące się urządzenie zablokowało połączenie. **Demontaż nigdy nie
kończy się przedwcześnie:** `closeQuietly` wyłącza, zwalnia i zamyka w osobnych krokach, zwalnia tylko wtedy,
gdy zajęcie jest utrzymywane, i zawsze próbuje `close()` — po nieudanym zajęciu `release()` rzuca „not
claimed", a gdy kroki dzieliły jeden `try`, pomijało to zamknięcie i zostawiało urządzenie otwarte w usłudze
Zebry. **Zdarzenie danych jest ponownie uzbrajane w `finally`:** JavaPOS wyłącza je po każdym zdarzeniu,
więc odczyt lub obsługa, która rzuci wyjątek, i tak musi je uzbroić, inaczej skaner milczy aż do ponownego
połączenia. `ScannerLifecycleTest` sprawdza obie zasady na oskryptowanym `jpos.Scanner`.

**`JposConfig` kieruje JavaPOS do dołączonego `jpos.xml`** przed pierwszym otwarciem. Z jarami JavaPOS z
Mavena (`javapos-config-loader` 4.x) i bez `jpos/res/jpos.properties` na class path każde otwarcie kończy się
inaczej błędem *"Service does not exist in loaded JCL registry"* (błąd 109), z trzech osobnych powodów:
menedżer usług wraca do `SimpleRegPopulator`, który czyta zserializowany `jpos.cfg`, a nigdy `jpos.xml`;
program ładujący XML szuka `jpos.xml` tylko w katalogu roboczym, nie w jarze; a Xerces 2.12.2 w `lib/`
odrzuca właściwość JAXP `accessExternalDTD`, którą ustawia program ładujący. `JposConfig` ustawia
`jpos.config.regPopulatorClass` (`JavaxRegPopulator`), `jpos.config.populatorFileURL` (własny
`jar:file:…!/jpos.xml` jara) i `javax.xml.parsers.SAXParserFactory` (tę z JDK) jako właściwości systemowe —
nigdy taką, która jest już ustawiona, więc `-Djpos.config.populatorFileURL=file:…` nadal wybiera inny
`jpos.xml` — i nie ustawia niczego, gdy na class path jest `jpos.properties` albo brak programu ładującego
4.x. `javaPOS114.jar` z Zebra SDK jest właśnie takim przypadkiem i po prostu psuje rejestr, dlatego `run.bat`
trzyma go z dala od class path — patrz [Biblioteki natywne Zebra](#biblioteki-natywne-zebra).
`JposConfigTest` sprawdza od początku do końca, że rejestr zawiera wtedy skanery z pliku.

**Selektor urządzeń jest generowany z tego rejestru**, a nie przechowywany jako lista w kodzie:
`ScannerService.logicalNames()` czyta załadowane wpisy na wątku `gaia-scanner` i zachowuje te, których
kategoria urządzenia to `Scanner` — dwa wpisy wag w `jpos.xml` zostają poza nim — w kolejności
`pickerOrder`: najpierw `ZebraAllScanners`, gdy plik go definiuje, resztę alfabetycznie (rejestr nie
zachowuje kolejności z pliku). Plik bez niego go nie dostaje; gdy nic nie jest załadowane, proponowana jest
tylko wartość domyślna, więc selektor nigdy nie jest pusty. `MainView` od razu pokazuje wartość domyślną i
uzupełnia listę z wątku w tle, zachowując zapisany wybór. Tak więc skaner dodany do `jpos.xml` — albo cały
plik wskazany przez `-Djpos.config.populatorFileURL` — pojawia się w selektorze bez zmiany kodu, a
`JposConfigTest` porównuje selektor z niezależnym odczytem dołączonego pliku. Usługi wymienione w tych
wpisach to klasy Zebry
(`com.zebra.jpos.service.scanner.*Factory`), które nadal muszą być na class path — ze Scanner SDK przez
`run.bat` albo jako jary usług Zebry położone w `lib\`.

Dane ze skanu są dekodowane jako **ISO-8859-1**, aby każdy bajt mapował się 1:1 i przetrwały zarówno
przedrostek AIM Code ID, jak i każdy `0x1D`; raporty zapisywane są w UTF-8.

Nic w usłudze nie składa tekstu dla użytkownika. Zdarzenia wychodzą jako stałe `ScannerListener.Event` wraz
ze zmiennymi wartościami, a `MainView` dostarcza treść z katalogu — inaczej wiersz stanu byłby po angielsku
w aplikacji przetłumaczonej na 35 sposobów. Wyjątkiem są wiersze dziennika, które pozostają po angielsku.

### Bariera platformy

`ScannerService.isSupportedPlatform()` to jeden test `os.name`, a `MainView` pyta go, zanim zbuduje
jakąkolwiek kontrolkę skanera: w macOS i Linuksie nie ma przełącznika, selektora urządzeń, menu Skaner (a
więc i <kbd>⌘K</kbd>) ani etykiety stanu. Nie są ukryte — nigdy nie są tworzone.

Zebra dostarcza swoje usługi JavaPOS tylko dla Windows, a gdzie indziej nic nie zawodzi wcześnie: jary się
rozwiązują i kontrolka się tworzy, a dopiero `open()` w końcu zawodzi na brakującej warstwie natywnej,
kilka sekund i jedno okno dialogowe później.

Zasada dotyczy *usług*, a nie JavaPOS. Dostawca, który dostarczyłby usługę wieloplatformową, działałby tutaj,
a poszerzenie zasady to zmiana wyłącznie w `supportsJavaPos(String)` — metodzie prywatnej dla pakietu i
testowanej na ciągach `os.name`, aby obie odpowiedzi były sprawdzone na każdej maszynie, która buduje.

### Symbologie

`Symbologies` przypisuje wszystkie **78** typów danych skanu JavaPOS `SCAN_SDT_*` do nazw wyświetlanych oraz
do `DataCarrierType` rdzenia tam, gdzie oba słowniki się pokrywają. JavaPOS jest dokładniejszy z nich dwóch —
oddziela UPC-A od EAN-13 i zwykły Data Matrix od GS1 DataMatrix, tam gdzie AIM grupuje całą rodzinę pod
jednym znakiem kodu.

Typ spoza tabeli i tak dostaje użyteczną nazwę, znalezioną przez refleksję po `ScannerConst`, ponieważ
usługi dostawców rzeczywiście zgłaszają wartości spoza listy ze specyfikacji. Test przechodzi po
`ScannerConst` i zawodzi, jeśli któryś zadeklarowany typ nie ma nazwy — co wyłapuje też stałe dodane przez
przyszłe JavaPOS.

Niektóre przypisania nośników celowo pozostawiono jako `UNKNOWN` zamiast zgadywać: zwykły 2 z 5 (AIM
rozróżnia `]R`/`]S` po liczbie kresek start/stop, czego JavaPOS nie zgłasza), czterostanowe kody pocztowe i
TLC-39.

---

## Lokalizacja

Etykiety mieszkają w jednym pliku właściwości na język w `src/main/resources/i18n/ui_<CODE>.properties`, z
tymi samymi kodami co katalogi błędów rdzenia (`EN`, `FR`, `PCM`, …). Istnieje wszystkie 35 i wszystkie 35
mają identyczny zestaw kluczy. Pliki są czytane jako **UTF-8**, a nie ISO-8859-1, które zakłada
`Properties`, aby tłumaczenia były przechowywane czytelnie, a nie jako sekwencje `\uXXXX`.

**Mechanizm zastępczy działa na poziomie klucza, nie pliku** — dzięki temu częściowo przetłumaczony język
jest użyteczny od chwili, gdy pojawi się jego pierwszy klucz. Angielski jest kontraktem: każdy klucz musi w
nim istnieć.

| Warstwa | Klucze | Pokrycie |
|---|---|---|
| Wspólne słownictwo raportu | 31 | wszystkie 35 języków |
| Obramowanie aplikacji — menu, przyciski, kreator, opcje, wiersz stanu, układ raportu, stan skanera, treści okien dialogowych | 114 | wszystkie 35 języków |

Sześć wspólnych terminów (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) nie jest
używanych przez raport tej aplikacji.
Zachowano je celowo; mówi o tym nagłówek pliku.

### Argument `--language`

`GaiaDesktopApp.languageOverride()` czyta `--language=…` przez własne `getParameters().getNamed()` JavaFX,
rozwiązuje go za pomocą `Languages.fromArgument` — kod katalogu, nazwa enuma lub znacznik BCP 47, bez
rozróżniania wielkości liter — i przekazuje do `MainView`.

**Nadpisanie dotyczy uruchomienia, nie pliku.** `MainView` trzyma wczytany język w `savedLanguage` i
zapisuje z powrotem *ten*, dopóki `languageChosenInSession` nie wskaże, że użytkownik sam wybrał język;
wtedy jego wybór utrwala się normalnie. Ten podział to cały projekt: profil kiosku lub skrót demonstracyjny
może otworzyć się po hiszpańsku bez nadpisywania tego, w czym jutro otworzy się właściciel komputera,
podczas gdy prawdziwy wybór dokonany w panelu opcji nadal się utrzymuje.

Wartość niewskazująca żadnego katalogu jest zapisywana w dzienniku wraz z akceptowanymi kodami i
ignorowana. Odmowa startu z powodu literówki w skrócie byłaby gorszą awarią, i z tego samego powodu
`Languages.fromArgument` zwraca `null` zamiast rzucać wyjątek.

### Dodawanie języka

Nie jest potrzebna żadna zmiana kodu.

1. Edytuj `ui_<CODE>.properties` — każdy język ma już swój plik.
2. Przetłumacz wartości. Zostaw na miejscu symbole zastępcze `{0}` / `{1}`.
3. Zbuduj. Dwa testy pilnują kontraktu: jeden zawodzi, jeśli kod prosi o klucz, którego angielski nie
   definiuje, drugi — jeśli zestaw kluczy któregoś katalogu odbiega od angielskiego.

Pierwszy test przeszukuje **źródła** w poszukiwaniu literałów kluczy zamiast używać refleksji po klasach,
ponieważ klucze istnieją tylko jako literały napisowe w miejscach wywołań. Dlatego nowy komunikat trzeba
budować z kluczem wpisanym bezpośrednio — `strings.format("st.scannerError", …)` — a nie odszukiwanym przez
zmienną, inaczej test go nie zobaczy.

### Co celowo nie jest tłumaczone

- zwięzłe nazwy pól pisane czcionką o stałej szerokości obok surowych wartości — `format`, `position`,
  `length`, `raw` i `part` karty elementu, `AI`, `sep`, `pos` i `len` taśmy oraz cztery komórki lokalizujące
  błąd (`AI`, `position`, `level`, `stage`), których wartości i tak są nieprzetłumaczonymi nazwami enumów;
- nazwy przykładowych danych wejściowych, które nazywają konstrukcje GS1, oraz nazwy symbologii, które są
  nazwami własnymi;
- akapit z zaleceniem naprawy dla `GE-C084` — proza normatywna, gdzie półtłumaczenie jest gorsze niż
  angielski;
- zabezpieczenie `ElementBuilder` na nieznane AI, nieosiągalne z interfejsu: edytor sam sprawdza nieznane AI
  i zgłasza je z katalogu;
- wiersze dziennika.

Pozostaje jedna luka: `ScanSource.label()` dostarcza `manual` / `scanner` / `file` / `built` po angielsku do
wiersza historii i nagłówka raportu. Zamknięcie jej wymaga czegoś więcej niż klucza — `HistoryCell` nie ma
`UiStrings`, a fabryka komórek listy jest instalowana raz, więc przy zmianie języka trzeba by też odświeżać
komórki.

---

## Formaty zapisanego stanu

Dwa pliki w `~/.gaia-desktop/`, trzymane osobno, bo zmieniają się w różnym rytmie — uszkodzony lub ręcznie
edytowany dziennik przechwyceń nie powinien kosztować użytkownika jego ustawień. Katalog można zmienić przez
`-Dgaia.desktop.home=…`, i w ten sposób testy trzymają się z dala od prawdziwego katalogu domowego.

**`options.properties`** — zapisywany, gdy zmienia się opcja, oraz przy zamknięciu.

| Klucz | Znaczenie |
|---|---|
| `parse.mode`, `parse.language` | Głębokość potoku i język komunikatów |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Wyświetlanie dat |
| `check.skipRequires`, `check.skipExcludes` | Wyjątki od kontroli strukturalnych |
| `ui.theme` | `LIGHT` lub `DARK` |
| `ui.optionsPane` | Panel otwarty? |
| `ui.scannerDevice` | Ostatnia logiczna nazwa JavaPOS |
| `ui.mode` | `PARSE`, `BUILD` lub `COMBINE` |
| `update.check` | Czy codzienne sprawdzanie aktualizacji jest uruchamiane (domyślnie `true`) |
| `update.lastCheck` | Kiedy ostatnio się uruchomiło, ISO-8601; brak lub nieczytelna wartość oznacza, że sprawdzenie jest należne |
| `update.skipVersion` | Wydanie, dla którego użytkownik wybrał *Pomiń tę wersję*; nie jest już proponowane automatycznie |

Brakujący lub uszkodzony plik nigdy nie jest fatalny: każda wartość wraca do domyślnej osobno, więc jeden
zły wiersz kosztuje tylko ten wiersz. Przy pierwszym uruchomieniu, bez żadnego pliku, opcje są wypełniane
przez `SystemDefaults` z ustawień regionalnych komputera zamiast z wbudowanych wartości domyślnych GAIA —
krótki wzorzec daty dla ustawień regionalnych podaje kolejność składników, separator oraz to, czy miesiąc i
rok są skracane.

Nigdy nie przekazuj `Properties.setProperty` wartości null: rzuca wyjątek, a `persistOptions()` działa z
obsług zdarzeń interfejsu, gdzie wyjątek ucieka na wątek FX. To prawdziwa awaria, którą ta aplikacja już
miała.

**`captures.tsv`** — zapisywany, gdy zmienia się historia, oraz przy zamknięciu. Najnowsze najpierw, z
limitem **500** wpisów.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Przechowywane jest tylko to, czego nie da się obliczyć ponownie. `ParseResult` **nie** jest serializowany —
jest odtwarzany przez ponowną analizę wejścia przy wczytywaniu, więc przywrócona historia odzwierciedla opcje
obowiązujące *teraz*. Wejścia rutynowo zawierają FNC1 i cokolwiek innego wysyła skaner, więc pole wejścia
jest escapowane (`\\`, `\t`, `\n`, `\r` oraz `\xNN` dla wszystkiego innego poniżej `0x20`), a plik jest
czytany i zapisywany jako ISO-8859-1, bajt po bajcie.

Pliki zapisane, zanim zaczęto rejestrować datę, zawierają samą godzinę; nadal się wczytują, z dzisiejszą
datą, bo prawdziwa data nigdy nie została zapisana.

---

## Testy

```bash
(cd gaia-desktop && mvn test)
```

198 testów, celowo **niezależnych od toolkitu JavaFX**, co utrzymuje je szybkimi i bez interfejsu graficznego.
Obejmują generator raportu, logikę tworzenia i wyszukiwanie AI, konwersję `{GS}`, wartości domyślne
wyprowadzone z ustawień regionalnych, oba magazyny (w tym przejście w obie strony z `0x1D` i celowo
uszkodzone pliki), tabelę symbologii, zasadę platformy skanera, zaślepkę wersji oraz katalogi.

Trzy warto znać, bo bronią kontraktów, a nie funkcji:

| Test | Zawodzi, gdy |
|---|---|
| `CatalogueCoverageTest` | Kod prosi o klucz, którego `ui_EN.properties` nie definiuje, albo któryś z 35 plików odbiega od angielskiego zestawu kluczy |
| `SymbologiesTest` | Któraś stała `SCAN_SDT_*` zadeklarowana przez JavaPOS nie ma nazwy wyświetlanej — w tym taka, którą doda aktualizacja JavaPOS |
| `ScannerServiceTest` | Zasada platformy zmienia odpowiedź dla Windows lub dla czegokolwiek innego |

`UiStrings` jest w czasie działania celowo pobłażliwe — nieznany klucz wyświetla się jako sam klucz zamiast
rzucać wyjątek w środku układu — i właśnie dlatego test katalogu musi istnieć.

**Nie jest objęte:** wszystko, co wymaga działającego toolkitu — zachowanie fokusu, przełączanie trybów,
edycja tabel, zmiana języka i obie awarie opisane w [Wątki](#wątki). Sprawdzono je, obsługując prawdziwe
okno, ale regresja w nich nie spowodowałaby niepowodzenia kompilacji. Dodanie TestFX lub Monocle zamknęłoby
tę lukę.

---

## Rozwiązywanie problemów z kompilacją

| Objaw | Przyczyna i rozwiązanie |
|---|---|
| `package tools.pantheum.gaia does not exist` | Rdzeń nie jest zainstalowany. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | `Main-Class` jara musi być `Launcher`, a nie `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* przy starcie | Oczekiwane i nieszkodliwe — JavaFX odnotowuje, że jest na class path. Właśnie dlatego istnieje `Launcher` |
| Jar się nie uruchamia, `NoClassDefFoundError` | Domyślny jar zawiera tylko klasy tego modułu; `dist/lib/` musi leżeć obok. Użyj `-Pfat`, aby mieć jeden samodzielny plik |
| `Artifact has not been packaged yet … MDEP-187` w Eclipse | Rozwiązywanie przestrzeni roboczej m2e. Patrz uwaga w [Pakowanie](#pakowanie) |
| W kompilacji pojawiają się zduplikowane pliki `… 2.properties` | Projekt leży w folderze synchronizowanym z iCloud, a synchronizacja w trakcie kompilacji utworzyła kopie konfliktowe. Zawsze `mvn clean package`; lepiej wyłącz `target/` z synchronizacji |
| Pozycja menu pokazuje klucz w rodzaju `act.hri` | Tego klucza brakuje w `ui_EN.properties`. Kompilacja powinna była to wyłapać — patrz [Testy](#testy) |

---

## Znane ograniczenia i otwarte prace

- **Brak testów interfejsu bez wyświetlacza.** Patrz [Testy](#testy).
- **Artefakty specyficzne dla platformy.** Oba sposoby pakowania zawierają biblioteki natywne JavaFX jednej
  platformy; buduj jeden na każdy cel z `-Dfx.platform` (każda maszyna może zbudować wszystkie). Żaden nie
  zawiera środowiska uruchomieniowego Javy — do tego potrzebny jest `jpackage`, który buduje tylko dla
  systemu, na którym działa.
- **Wejście ze skanera tylko w Windows.** Patrz [Bariera platformy](#bariera-platformy).
- **`ScanSource.label()` nie jest zlokalizowane.** Patrz [Co celowo nie jest
  tłumaczone](#co-celowo-nie-jest-tłumaczone).
- **`UiState.mode` to `String`** (`"PARSE"` / `"BUILD"`), podczas gdy `Theme` obok to enum, więc literał
  pojawia się w kilku miejscach. Enum by je usunął.
- **Cel Digital Link to tylko domena i prefiks ścieżki.** `BuilderDigitalLinkConfig` obsługuje również
  dodatkowe parametry zapytania i fragment; edytor ich nie udostępnia.
- **Wzajemnie wykluczające się pary AI (`GE-S006`) nie są oznaczane w edytorze** tak, jak nieznane i
  zduplikowane AI — tworzenie po prostu się nie udaje.
- **Katalog błędów rdzenia podwaja cudzysłowy** (`value ''09506000134350''`), co pojawia się dosłownie w
  błędach tworzenia i w raporcie. Wygląda to na escapowanie cudzysłowów przez `MessageFormat`, które nie jest
  cofane; poprawka należy do rdzenia.
