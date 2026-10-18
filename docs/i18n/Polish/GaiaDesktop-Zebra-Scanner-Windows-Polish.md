# Konfiguracja skanera Zebra — GAIA Desktop dla Windows

Jak zainstalować i skonfigurować skaner kodów kreskowych Zebra, aby GAIA Desktop odczytywał z
niego kody GS1 bezpośrednio: każdy odczyt jest analizowany i dopisywany do historii, z
nienaruszonym prefiksem AIM Code ID i każdym separatorem FNC1. Wersja oryginalna:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

GAIA Desktop został opracowany i przetestowany z ręcznym skanerem obrazowym **Zebra DS8108**
podłączonym przez USB. Kroki dotyczą każdego skanera 2D Zebra obsługiwanego przez Scanner SDK firmy
Zebra; kody konfiguracyjne i nazwy menu konkretnego modelu znajdują się w jego własnym Quick
Reference Guide lub Product Reference Guide.

> **Tylko Windows.** Sterowniki skanerów Zebra istnieją tylko dla Windows, dlatego w systemach
> macOS i Linux GAIA Desktop nie tworzy żadnych elementów sterujących skanerem. Cała reszta aplikacji
> działa na każdej platformie.

---

## Spis treści

1. [Czego potrzebujesz](#1-czego-potrzebujesz)
2. [Instalacja Javy](#2-instalacja-javy)
3. [Instalacja Zebra Scanner SDK](#3-instalacja-zebra-scanner-sdk)
4. [Instalacja 123Scan](#4-instalacja-123scan)
5. [Aktualizacja oprogramowania układowego skanera Zebra](#5-aktualizacja-oprogramowania-układowego-skanera-zebra)
6. [Ustawienie trybu USB skanera Zebra](#6-ustawienie-trybu-usb-skanera-zebra)
7. [Konfiguracja skanera Zebra dla GS1](#7-konfiguracja-skanera-zebra-dla-gs1)
8. [Instalacja i uruchomienie GAIA Desktop](#8-instalacja-i-uruchomienie-gaia-desktop)
9. [Podłączanie skanera](#9-podłączanie-skanera)
10. [Informacje o konfiguracji](#10-informacje-o-konfiguracji)
11. [Rozwiązywanie problemów](#11-rozwiązywanie-problemów)
12. [Obsługiwane skanery 2D Zebra](#12-obsługiwane-skanery-2d-zebra)

---

## 1. Czego potrzebujesz

| | |
|---|---|
| **Skaner** | Skaner 2D Zebra z kablem USB — zob. [Obsługiwane skanery 2D Zebra](#12-obsługiwane-skanery-2d-zebra) |
| **Komputer** | Windows 10 lub 11, 64-bitowy |
| **Java** | Java 21 lub nowsza, **64-bitowa** |
| **Zebra Scanner SDK dla Windows** | **64-bitowy**, ze strony wsparcia Zebra — dostarcza sterownik i usługi JavaPOS, których używa GAIA Desktop |
| **Zebra 123Scan** | Bezpłatne narzędzie konfiguracyjne Zebra, ze strony wsparcia — do aktualizacji oprogramowania układowego skanera i sprawdzania jego ustawień |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Opcjonalnie** | **Product Reference Guide** (PRG) skanera, zawierający kody konfiguracyjne |

Jak elementy do siebie pasują:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop zawiera samo API JavaPOS oraz konfigurację, która nadaje skanerowi nazwę (`jpos.xml`,
wewnątrz `gaia-desktop.jar`). Resztę dostarcza SDK Zebra: sterownik, który komunikuje się ze
skanerem, oraz klasy usług Zebra pośredniczące między JavaPOS a tym sterownikiem.

---

## 2. Instalacja Javy

GAIA Desktop wymaga **Javy 21 lub nowszej**, w wersji **64-bitowej**, zgodnej z 64-bitowymi
sterownikami Zebra.

1. Zainstaluj JDK lub JRE Java 21 dla Windows x64 — na przykład Eclipse Temurin z adoptium.net.
   Pozwól instalatorowi ustawić `JAVA_HOME`, jeśli to zaproponuje.
2. Sprawdź to w wierszu poleceń:

   ```
   java -version
   ```

   Pierwszy wiersz powinien pokazywać wersję 21 lub nowszą; 64-bitowa Java wyświetla też
   `64-Bit Server VM`.

`run.bat` szuka Javy w tej kolejności: folder `jdk*` obok `run.bat`, potem `JAVA_HOME`, potem
`java` w `PATH`. Wszystko starsze niż 21 odrzuca z komunikatem, który to wyjaśnia.

---

## 3. Instalacja Zebra Scanner SDK

1. Pobierz **Zebra Scanner SDK dla Windows (64-bitowy)** ze strony wsparcia Zebra.
2. Uruchom instalator. Jeśli pozwala wybrać składniki, upewnij się, że składnik **JPOS** (JavaPOS)
   jest uwzględniony.
3. Pozostaw domyślną lokalizację instalacji. GAIA Desktop szuka części JavaPOS SDK w:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   i oczekuje plików jar usług Zebra (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …)
   oraz bibliotek natywnych (`CSJPOSScanner64.dll`, …) w jej folderze `bin\`. Jeśli SDK jest
   zainstalowany gdzie indziej, zob. [ZEBRA_HOME](#zebra_home).
4. Po instalacji **uruchom ponownie Windows**. Sterownik CoreScanner z SDK działa w tle i musi być
   uruchomiony, zanim GAIA Desktop będzie mógł dotrzeć do skanera; ponowne uruchomienie to
   najprostszy sposób, by mieć pewność.

---

## 4. Instalacja 123Scan

123Scan to bezpłatne narzędzie Zebra do konfigurowania jej skanerów. W następnym kroku służy do
aktualizacji oprogramowania układowego skanera, a potem jest najprostszym sposobem na sprawdzanie i
zmianę ustawień skanera.

1. Pobierz **123Scan** ze strony wsparcia Zebra i uruchom instalator.
2. Podłącz skaner Zebra i uruchom 123Scan. Program powinien znaleźć skaner i pokazać jego model,
   numer seryjny i bieżącą wersję oprogramowania układowego. Jeśli tak się nie stanie, przed dalszymi
   krokami spróbuj innego portu USB lub kabla — GAIA Desktop nie zobaczy skanera, którego nie widzi
   123Scan.

---

## 5. Aktualizacja oprogramowania układowego skanera Zebra

Przed konfiguracją zaktualizuj oprogramowanie układowe (firmware) skanera do najnowszej wersji.
Aktualizacje usuwają problemy z dekodowaniem i utrzymują zgodność skanera ze sterownikiem Zebra
zainstalowanym przez SDK.

1. Zamknij **GAIA Desktop** i każdy inny program, który może używać skanera: podczas aktualizacji
   123Scan potrzebuje skanera na wyłączność.
2. W 123Scan, przy podłączonym skanerze, wybierz opcję aktualizacji oprogramowania układowego.
   123Scan porównuje firmware skanera z najnowszym opublikowanym przez Zebrę i proponuje jego
   pobranie i instalację. Jeśli komputer nie ma dostępu do Internetu, pobierz firmware skanera z jego
   strony wsparcia w serwisie Zebra i wczytaj plik do 123Scan.
3. Rozpocznij aktualizację i **nie odłączaj skanera, dopóki 123Scan nie zgłosi, że skończył**. Trwa
   to kilka minut; na końcu skaner uruchamia się ponownie. Odłączenie w trakcie może sprawić, że
   skaner przestanie działać.
4. Sprawdź, czy 123Scan pokazuje teraz nową wersję oprogramowania układowego.

Aktualizacja może przywrócić niektóre ustawienia do wartości domyślnych; dlatego skaner konfiguruje
się w dwóch kolejnych krokach, po aktualizacji firmware.

---

## 6. Ustawienie trybu USB skanera Zebra

To krok najczęściej pomijany. **Fabrycznie skaner Zebra zachowuje się jak klawiatura USB** (tryb
USB HID Keyboard): „wpisuje” każdy kod kreskowy tam, gdzie jest fokus. W tym trybie
*Połącz skaner* w GAIA Desktop nie może go znaleźć, a wszystko, co zostanie wpisane w pole
tekstowe, traci separatory FNC1 — co zmienia znaczenie ciągu elementów GS1.

Przełącz skaner w tryb, w którym sterownik Zebra komunikuje się z nim bezpośrednio:

| Tryb USB skanera | Nazwa urządzenia w GAIA Desktop | Uwagi |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld` lub `ZebraAllScanners` | Zalecany |
| SNAPI (z obrazowaniem lub bez) | `ZebraScannerSNAPI` lub `ZebraAllScanners` | Własny protokół USB firmy Zebra; również działa |
| IBM Table-top USB | `ZebraUSBTableTop` lub `ZebraAllScanners` | Przeznaczony dla skanerów stacjonarnych |
| USB OPOS (Hand-held) | `ZebraUSBOPOS` lub `ZebraAllScanners` | |
| USB HID Keyboard (ustawienie fabryczne) | — | **Nie połączy się** |

`ZebraAllScanners`, domyślna nazwa urządzenia w GAIA Desktop, akceptuje skaner w każdym z
obsługiwanych trybów, więc po ustawieniu trybu wartość domyślna zwykle wystarcza.

Tryb zmienia się na jeden z dwóch sposobów:

- **zeskanuj kod konfiguracyjny** trybu — na przykład *IBM Hand-held USB* — z Quick Reference Guide
  lub Product Reference Guide skanera (sekcja USB Host Types / USB Device Type); skaner potwierdza
  sygnałem dźwiękowym. Albo
- **użyj 123Scan** ([krok 4](#4-instalacja-123scan)): podłącz skaner, otwórz jego konfigurację i
  ustaw typ hosta USB.

Po zmianie trybu Windows ponownie wykrywa skaner jako inny rodzaj urządzenia; odczekaj kilka sekund
przed połączeniem.

Aby później przywrócić skaner do trybu klawiatury — dla innego oprogramowania — zeskanuj kod
konfiguracyjny *USB Keyboard (HID)* albo przywróć ustawienia fabryczne.

---

## 7. Konfiguracja skanera Zebra dla GS1

GAIA Desktop analizuje najlepiej, gdy skaner informuje go, jaki rodzaj kodu kreskowego odczytał.

### Przesyłanie AIM Code ID

Ustaw **Transmit Code ID Character** na **AIM Code ID Character** — kodem konfiguracyjnym z Product
Reference Guide (User Preferences) lub w 123Scan. Każdy odczyt zaczyna się wtedy od identyfikatora
symboliki — `]C1` dla GS1-128, `]d2` dla GS1 DataMatrix, `]Q3` dla GS1 QR Code, `]e0` dla
GS1 DataBar — którego GAIA Desktop używa, by rozpoznać nośnik danych i zastosować jego reguły.

Bez tego GAIA Desktop nadal analizuje ciąg elementów i bierze symbolikę z samego odczytu, ale raport
nie ma AIM Code ID do pokazania, a prosty kod kreskowy, taki jak EAN-13, trudniej odróżnić od ciągu
elementów GS1.

### Sprawdzenie symbolik

Upewnij się, że rodzaje kodów kreskowych, z którymi pracujesz, są **włączone**: zwykle GS1-128,
GS1 DataMatrix, GS1 DataBar, GS1 QR Code i EAN/UPC. W skanerach 2D Zebra większość jest domyślnie
włączona; 123Scan pokazuje bieżące ustawienia, a Product Reference Guide zawiera kod włączający
każdą z nich.

### Funkcje analizy GS1 firmy Zebra pozostaw wyłączone

Nowsze oprogramowanie układowe dodaje funkcje GS1 działające w samym skanerze — **Label Parse+**,
**GS1 Digital Link** i **GS1 Retail POS parsing**. Pozostaw je **wyłączone**, tak jak skaner jest
dostarczany: przeformatowują lub skracają to, co skaner wysyła, a GAIA Desktop potrzebuje surowych
danych, by je przeanalizować i zweryfikować. Co robi każda z nich — zob.
[Obsługiwane skanery 2D Zebra](#12-obsługiwane-skanery-2d-zebra).

### Bez prefiksów i sufiksów

Nie dodawaj do danych wyjściowych skanera prefiksu, sufiksu ani klawisza Enter. GAIA Desktop
otrzymuje każdy odczyt jako kompletny komunikat przez JavaPOS i nie potrzebuje żadnego z nich;
dodatkowe znaki trafiłyby do ciągu elementów.

---

## 8. Instalacja i uruchomienie GAIA Desktop

1. Rozpakuj `gaia-desktop-1.0.0-win.zip`, na przykład do `C:\Tools\`. Zawiera jeden folder:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Podłącz skaner.
3. Kliknij dwukrotnie **`run.bat`**. Obok aplikacji otwiera się okno konsoli, które pozostaje
   otwarte: stan skanera i błędy JavaPOS są zgłaszane właśnie tam.
4. Przeczytaj pierwszy wiersz w konsoli. Mówi, co znalazł `run.bat`:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Jeśli kończy się na **`Zebra services from none (Zebra Scanner SDK not installed)`**, SDK nie
   został znaleziony w domyślnej lokalizacji — zainstaluj go
   ([krok 3](#3-instalacja-zebra-scanner-sdk)) lub ustaw [ZEBRA_HOME](#zebra_home). Aplikacja i tak
   się uruchomi, ale skaner nie będzie mógł się połączyć.

---

## 9. Podłączanie skanera

1. Na pasku narzędzi **lista urządzeń** pokazuje nazwy skanerów znane GAIA Desktop. Pozostaw na niej
   **`ZebraAllScanners`** albo wybierz nazwę odpowiadającą trybowi USB skanera z tabeli w
   [kroku 6](#6-ustawienie-trybu-usb-skanera-zebra).
2. Kliknij **Połącz skaner** lub naciśnij **Ctrl+K** (dostępne też w menu **Skaner**).
3. Pasek stanu zmienia się na **`skaner: ZebraAllScanners`** (lub wybraną nazwę), a wiersz
   stanu zgłasza *Skaner podłączony — ZebraAllScanners*. Przycisk ma teraz napis
   **Odłącz skaner**.
4. Zeskanuj kod kreskowy GS1. Pojawia się na początku historii przechwyceń i otwiera się jego raport.
   Przy włączonym AIM Code ID raport podaje nośnik danych — *GS1-128*, *GS1 DataMatrix* — a ciąg
   elementów zachowuje każdy separator FNC1.

Lista urządzeń zapamiętuje wybór między uruchomieniami. Skaner pozostaje podłączony, dopóki go nie
odłączysz lub nie zamkniesz aplikacji. Odczyty działają też w trybach **Utwórz** i
**Połącz**: w trybie Połącz każdy odczyt staje się jednym z łączonych kodów
kreskowych.

Dopóki GAIA Desktop trzyma skaner, inne programy nie mogą go używać, i odwrotnie: przed połączeniem
zamknij 123Scan i wszelkie inne oprogramowanie kasowe lub do skanowania.

---

## 10. Informacje o konfiguracji

### Nazwy urządzeń

Lista urządzeń jest wypełniana z pliku `jpos.xml` dołączonego do `gaia-desktop.jar`. Jego wpisy
skanerów:

| Nazwa urządzenia | Typ skanera | Do użycia z |
|---|---|---|
| `ZebraAllScanners` | dowolny | **Domyślna.** Dowolny obsługiwany tryb USB |
| `ZebraAllScanners114` | dowolny | Wersja JavaPOS 1.14 tej samej usługi |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Skaner w trybie IBM Hand-held USB |
| `ZebraUSBTableTop` | IBM Table-top USB | Tryb stacjonarny |
| `ZebraUSBOPOS` | USB OPOS | Tryb USB OPOS |
| `ZebraScannerSNAPI` | SNAPI | Tryb SNAPI |
| `ZebraScannerUSB` | dowolny USB | Dowolny tryb USB |
| `ZebraScannerSerialSSI` | szeregowy SSI | Skanery z kablem szeregowym (nie USB) |
| `ZebraScannerSerialNIXMODB` | szeregowy NIXMODB | Skanery z kablem szeregowym |
| `ZebraVDI` | VDI | Skanery przekierowane do pulpitu wirtualnego |

### ZEBRA_HOME

Jeśli SDK Zebra jest zainstalowany w innym miejscu niż domyślne, ustaw `ZEBRA_HOME` na jego folder
`JPOS` przed uruchomieniem `run.bat` — na przykład w małym pliku wsadowym obok niego:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

`run.bat` bierze wtedy pliki jar usług Zebra z `%ZEBRA_HOME%bin` i dodaje ten folder do `PATH` dla
bibliotek DLL Zebra.

### Własny jpos.xml

Aby dodać nazwę urządzenia lub zmienić ustawienia wpisu, skopiuj `jpos.xml` z `gaia-desktop.jar`
(to plik zip; otworzy go każde narzędzie do rozpakowywania), zmodyfikuj kopię i wskaż ją
GAIA Desktop:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

Lista urządzeń pokazuje wtedy skanery z tego pliku. Gdy podłączonych jest kilka skanerów Zebra,
wpis można powiązać z jednym z nich przez jego właściwość `ScannerModel` lub `SerialNumber`, jak we
wpisie `ZebraAllScanners`.

### Inne opcje

| Ustawienie | Jak |
|---|---|
| Język aplikacji | `run.bat --language=FR` (tylko dla tego uruchomienia) lub Panel opcji |
| Dodatkowe opcje Javy | `set GAIA_DESKTOP_OPTS=…` przed `run.bat` |
| Dołączona Java | umieść folder `jdk-21…` obok `run.bat`; jest używany przed `JAVA_HOME` |

---

## 11. Rozwiązywanie problemów

Błędy pojawiają się w oknie dialogowym przy łączeniu, w oknie konsoli oraz w pliku dziennika
`logs\gaia-desktop.log` w folderze, z którego uruchomiono `run.bat` (w folderze GAIA Desktop, gdy
`run.bat` uruchamia się dwukrotnym kliknięciem). Liczba w nawiasach kwadratowych w dzienniku to kod
błędu JavaPOS.

| Co widzisz | Prawdopodobna przyczyna | Co zrobić |
|---|---|---|
| Brak przycisku *Połącz skaner* | Aplikacja nie działa w systemie Windows | Skanowanie jest dostępne tylko w Windows |
| 123Scan nie znajduje skanera | Kabel, port USB lub inny program trzyma skaner | Spróbuj innego kabla lub portu; zamknij GAIA Desktop i inne oprogramowanie do skanowania. GAIA Desktop nie zobaczy skanera, którego nie widzi 123Scan |
| Konsola: `Zebra services from none (Zebra Scanner SDK not installed)` | SDK nie znajduje się w domyślnej lokalizacji | Zainstaluj 64-bitowy SDK ([krok 3](#3-instalacja-zebra-scanner-sdk)) lub ustaw [ZEBRA_HOME](#zebra_home) |
| Błąd **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Nie znaleziono plików jar usług Zebra | Jak wyżej — sprawdź wiersz `Zebra services from` w konsoli |
| Błąd **109**: *Service does not exist in loaded JCL registry* | Nazwy urządzenia nie ma w konfiguracji skanera albo `javaPOS114.jar` z SDK trafił do class path | Wybierz nazwę z listy urządzeń; nie dodawaj `javaPOS114.jar`; jeśli używasz własnego `jpos.xml`, sprawdź, czy ta nazwa w nim jest |
| Błąd **107** (brak sprzętu) lub połączenie nie udaje się mimo podłączonego skanera | Skaner jest wciąż w trybie klawiatury USB albo sterownik CoreScanner nie działa | Ustaw tryb USB ([krok 6](#6-ustawienie-trybu-usb-skanera-zebra)); uruchom ponownie Windows po instalacji SDK; spróbuj innego portu USB |
| Błąd **112** (przekroczony czas) lub **102** (zajęty) przy łączeniu | Inny program trzyma skaner | Zamknij 123Scan i wszelkie inne oprogramowanie do skanowania lub kasowe, potem połącz ponownie |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` w konsoli | Nie można załadować bibliotek DLL Zebra — często 32-bitowy SDK z 64-bitową Javą | Zainstaluj 64-bitowy SDK; sprawdź, czy `java -version` zgłasza 64-bitową maszynę wirtualną |
| Odczyty są wpisywane w pasek wprowadzania zamiast trafiać jako przechwycenia | Skaner jest w trybie klawiatury USB | Ustaw tryb USB ([krok 6](#6-ustawienie-trybu-usb-skanera-zebra)) |
| Połączono, ale odczyty nie docierają | Niewłaściwa nazwa urządzenia dla trybu skanera albo skaner podłączono ponownie w trakcie połączenia | Odłącz, wybierz `ZebraAllScanners`, połącz ponownie |
| Raport nie pokazuje AIM Code ID ani nośnika danych | Przesyłanie AIM Code ID jest wyłączone | Włącz *Transmit Code ID Character → AIM Code ID* ([krok 7](#7-konfiguracja-skanera-zebra-dla-gs1)) |
| W ciągu elementów brakuje separatorów albo AI zlewają się ze sobą | Zeskanowano w trybie klawiatury albo wpisano ręcznie bez `{GS}` | Użyj trybu USB JavaPOS; przy wpisywaniu wstawiaj separatory przyciskiem `{GS}` |
| Jakiś rodzaj kodu kreskowego nigdy nie jest odczytywany | Ta symbolika jest w skanerze wyłączona | Włącz ją ([krok 7](#7-konfiguracja-skanera-zebra-dla-gs1)) |

Jeśli problem nie ustępuje, zachowaj razem wiersz `run.bat:` z konsoli, treść okna błędu i
`logs\gaia-desktop.log`: razem pokazują, jakiej Javy, jakich usług Zebra i jakiej nazwy urządzenia
użyto oraz gdzie dokładnie połączenie się nie powiodło.

---

## 12. Obsługiwane skanery 2D Zebra

GAIA Desktop potrzebuje skanera, który odczytuje kody kreskowe, w których drukowane są dane GS1 —
GS1-128, GS1 DataBar, EAN/UPC oraz, w większości zastosowań, kody 2D: **GS1 DataMatrix** i
**GS1 QR Code**, w którym zwykle drukuje się też URI **GS1 Digital Link**. Oznacza to skaner
obrazowy **2D** Zebra: skaner liniowy (1D) w ogóle nie odczyta kodów DataMatrix ani QR.

Skaner nie musi sam rozumieć identyfikatorów zastosowań GS1 (Application Identifiers) ani Digital
Link. **GAIA Desktop analizuje jedno i drugie**: przyjmuje odczyt tak, jak skaner go odczytał — z AIM
Code ID i separatorami FNC1 — i dzieli go na AI albo sam przekształca URI Digital Link w jego AI.

### Przetestowany

| Skaner | Typ | Stan |
|---|---|---|
| **DS8108** | Przewodowy ręczny skaner obrazowy 2D | **Opracowany i przetestowany** z GAIA Desktop |

### Powinny działać

To skanery obrazowe 2D Zebra, które, tak jak DS8108, obsługuje się przez Scanner SDK i usługi
JavaPOS firmy Zebra. **Nie zostały przetestowane** z GAIA Desktop; skonfiguruj każdy z nich jak w tym
przewodniku, korzystając z jego własnego Quick Reference Guide lub Product Reference Guide dla kodów
konfiguracyjnych.

| Rodzina | Modele (przykłady) |
|---|---|
| DS81x8 | DS8108 (przewodowy), DS8178 (bezprzewodowy) |
| DS22x8 | DS2208 (przewodowy), DS2278 (bezprzewodowy) — ogólnego przeznaczenia |
| DS36x8 | DS3608 (przewodowy), DS3678 (bezprzewodowy) — wzmocnione, w tym warianty KD i XR |
| DS46x8 | DS4608 (przewodowy), DS4678 (bezprzewodowy) |
| DS9308 / DS9908 | Skanery prezentacyjne i bezdotykowe (hands-free) |
| MP7000 / MP7200 | Wielopłaszczyznowe skanery bioptyczne i skanery z wagą |
| CS6080 | Bezprzewodowy skaner towarzyszący |

Skaner bezprzewodowy komunikuje się ze swoją podstawką, a do komputera podłącza się podstawkę:
podłącz podstawkę przez USB i traktuj tę parę jak jeden skaner. Skanery z innych rodzin Zebra też
mogą działać, jeśli Scanner SDK firmy Zebra obsługuje je w jednym z trybów USB z
[kroku 6](#6-ustawienie-trybu-usb-skanera-zebra).

### Funkcje GS1 wbudowane w skanery Zebra

Niezależnie od odczytu kodów kreskowych niektóre skanery obrazowe 2D Zebra potrafią przetwarzać dane
GS1 w samym skanerze, konfigurowane przez 123Scan:

| Funkcja | Co robi | Dla GAIA Desktop |
|---|---|---|
| **Label Parse+** | Analizuje AI etykiety GS1 w skanerze i wysyła przeformatowane pola | **Pozostaw wyłączoną** — GAIA Desktop nie widziałby już AI, separatorów FNC1 ani AIM Code ID |
| **GS1 Digital Link** | Dla towarów detalicznych oznaczonych zarówno kodem 1D EAN/UPC, jak i kodem 2D Digital Link: wybiera, który wysłać | **Pozostaw wyłączoną** (domyślnie) |
| **GS1 Retail POS parsing** | Odczytuje detaliczne kody 2D w składni URI Digital Link lub ciągu elementów | **Pozostaw wyłączoną** |

Ustawienie **GS1 Digital Link** ma trzy tryby. **Mode 1 — GTIN only**, tryb, od którego zaczyna po
włączeniu, wysyła tylko GTIN, niezależnie od kodu, z którego pochodzi, więc partia, data ważności czy
numer seryjny z Digital Link nigdy nie dotarłyby do GAIA Desktop. **Mode 2 — Prioritize 2D** wysyła
kod 2D, jeśli zostanie wykryty w czasie priorytetu (domyślnie 200 ms), w przeciwnym razie kod 1D.
**Mode 3 — Corroborate 1D and 2D GTINs Match** wysyła kod 1D, a także 2D, gdy jego GTIN się zgadza.
Jeśli ustawienie musi być włączone dla innego oprogramowania, użyj Mode 2, a nie Mode 1. W DS8108 są
to parametry 2373 (wł./wył.), 2374 (tryb) i 2491 (limit czasu).

Te funkcje wymagają **123Scan w wersji 6 lub nowszej** i oprogramowania układowego, które je zawiera.
Zebra dodaje obsługę GS1 Digital Link przez aktualizacje firmware od kwietnia 2025; poniższe daty
pochodzą od sprzedawcy Zebra, więc sprawdź informacje o wydaniu firmware każdego modelu:

| Rodzina modeli | Firmware z GS1 Digital Link wydany |
|---|---|
| DS81x8 | 16 kwietnia 2025 |
| MP7000 | 21 kwietnia 2025 |
| DS9908 | 6 czerwca 2025 |
| SP72 / SM72 | 11 lipca 2025 |
| DS46x8 | 28 lipca 2025 |
| MP7200 (Rev-C), DS5502 | 30 lipca 2025 |
| DS9308 | 29 sierpnia 2025 |
| DS22x8, DS1000 | 5 września 2025 (tylko region Azji i Pacyfiku) |
| DS36x8 (w tym KD i XR), MS47 | 30 września 2025 |
| PL5000, SE4107 | 15 października 2025 |
| CS6080 | 31 grudnia 2025 |

Żadna z tych funkcji nie jest potrzebna, by wczytywać kody GS1 Digital Link do GAIA Desktop: gdy są
wyłączone, skaner 2D Zebra z aktualnym oprogramowaniem układowym odczytuje kod QR Digital Link jak
każdy inny, a GAIA Desktop przekształca URI w jego AI.

Źródła: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (sierpień 2025), rozdział 12
„GS1 Digital Link Parameters” i rozdział Data Parsing; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (daty firmware).
