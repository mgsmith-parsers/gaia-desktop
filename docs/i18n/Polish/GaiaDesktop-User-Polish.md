# GAIA Desktop — Podręcznik użytkownika

Jak korzystać z aplikacji. Informacje o budowaniu, modyfikowaniu i dodawaniu języków znajdują
się w [podręczniku programisty](../../GaiaDesktop-Developer.md) (po angielsku). Dokument
źródłowy: [GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Spis treści

1. [Czym to jest](#czym-to-jest)
2. [Uruchamianie](#uruchamianie)
3. [Okno](#okno)
4. [Tryb Analizuj](#tryb-analizuj)
   - [Pasek wejścia i token {GS}](#pasek-wejścia-i-token-gs)
   - [Skanowanie](#skanowanie)
   - [Otwieranie pliku](#otwieranie-pliku)
   - [Historia przechwyceń](#historia-przechwyceń)
5. [Tryb Utwórz](#tryb-utwórz)
   - [Edytor AI](#edytor-ai)
   - [Czego edytor nie przyjmuje](#czego-edytor-nie-przyjmuje)
   - [Ciąg elementów albo Digital Link](#ciąg-elementów-albo-digital-link)
   - [Edytowanie istniejącego przechwycenia](#edytowanie-istniejącego-przechwycenia)
6. [Raport](#raport)
   - [Eksport](#eksport)
7. [Opcje analizy](#opcje-analizy)
   - [Pierwsze uruchomienie](#pierwsze-uruchomienie)
8. [Języki](#języki)
9. [Co jest zapamiętywane](#co-jest-zapamiętywane)
10. [Skróty klawiszowe](#skróty-klawiszowe)
11. [Rozwiązywanie problemów](#rozwiązywanie-problemów)
12. [Ograniczenia](#ograniczenia)

---

## Czym to jest

GAIA Desktop robi z danymi GS1 dwie rzeczy:

- **Analizuje** — wpisz, wklej, otwórz lub zeskanuj ciąg elementów GS1 albo URI Digital Link
  i przeczytaj wynik jako pełny raport: co oznacza każdy identyfikator zastosowania (AI), co
  jest nie tak i dlaczego;
- **Tworzy** — składa pary AI / wartość w ciąg elementów albo w URI Digital Link.

Wszystko, co aplikacja wie o GS1 — każda definicja AI, każda reguła sprawdzania, każdy komunikat
o błędzie i każdy przetłumaczony opis — pochodzi z leżącego pod nią silnika GAIA. Aplikacja nie
dokłada własnych reguł: przyjmuje dokładnie to, co przyjmuje silnik.

---

## Uruchamianie

```bash
java -jar gaia-desktop.jar
```

Plik JAR potrzebuje katalogu `lib/` dostarczanego obok niego — trzymaj je razem, inaczej
aplikacja się nie uruchomi. (Można też zbudować pojedynczy, samodzielny plik JAR; zobacz
[podręcznik programisty](../../GaiaDesktop-Developer.md#packaging).)

Wymagana jest **Java 21 lub nowsza**. Dystrybucja jest właściwa dla jednego systemu operacyjnego
i jednej architektury procesora — używaj pliku JAR zbudowanego dla swojego komputera.

Aby otworzyć aplikację w wybranym języku, bez zmieniania języka zapisanego:

```bash
java -jar gaia-desktop.jar --language=FR
```

Wartością może być kod katalogu (`FR`, `PCM`, `ARZ`), pełna nazwa (`FRENCH`, `WU-CHINESE`) albo
znacznik języka (`fr`, `zh-HK` dla kantońskiego, `ar-EG` dla arabskiego egipskiego) — wielkość
liter nie ma znaczenia. Ustawienie działa tylko dla tego uruchomienia: zapisany język pozostaje
nietknięty, więc skrót przygotowany dla współpracownika nie zmieni języka, w którym otwiera się
Twoja własna kopia. Wybór języka *wewnątrz* aplikacji jest natomiast prawdziwym wyborem i
zostaje zapisany jak zawsze. Wartość, która nie wskazuje żadnego języka, jest pomijana wraz
z notatką w konsoli, a aplikacja otwiera się w języku zapisanym, zamiast odmówić uruchomienia.

Przy starcie konsola wypisuje jeden wiersz z wersją aplikacji, datą jej zbudowania i wersją
silnika. To ten wiersz warto zacytować w zgłoszeniu błędu.

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

> Schemat zachowuje angielskie napisy, tak jak wszystkie bloki kodu w tej dokumentacji. Na
> ekranie zobaczysz *Analizuj* / *Utwórz*, *Przechwycenia*, *Zapisz raport*, *Opcje*,
> *skaner: wyłączony* oraz *Informacje*.

**W systemach macOS i Linux wiersza skanera na tym rysunku nie ma** — nie ma przełącznika ani
listy urządzeń na pasku narzędzi, nie ma menu *Skaner*, nie ma *skaner: wyłączony* na pasku
stanu. Zobacz [Skanowanie](#skanowanie).

Przełącznik **Analizuj ⇄ Utwórz** zmienia jedynie pas pod paskiem narzędzi oraz elementy
sterujące przechwytywaniem. Historia, raport, panel opcji i polecenia eksportu służą obu trybom,
ponieważ udane utworzenie staje się po prostu kolejnym przechwyceniem.

Panel opcji jest **zamknięty przy pierwszym uruchomieniu**. Tryb, widoczność panelu, motyw
i ostatnio używany skaner są pamiętane między uruchomieniami.

---

## Tryb Analizuj

### Pasek wejścia i token {GS}

Pasek przyjmuje ciąg elementów, URI GS1 Digital Link albo surowy odczyt wraz z przedrostkiem
AIM Code ID (`]d2…`). <kbd>Enter</kbd> uruchamia analizę.

AI o zmiennej długości rozdziela **FNC1** — niewidoczny znak sterujący `0x1D`. Pole tekstowe nie
jest w stanie go przechować: zostałby po cichu usunięty, a `01…` + FNC1 + `10LOT` dotarłby jako
`01…10LOT`, czyli ciąg elementów o innym znaczeniu.

Dlatego aplikacja pokazuje separator jako widoczny token **`{GS}`** i zamienia go z powrotem
w drodze do analizatora:

- **przycisk `{GS}`** wstawia token w miejscu kursora;
- **wklejenie** prawdziwych znaków separatora — z klawiatury, z menu kontekstowego lub metodą
  przeciągnij i upuść — zamienia je automatycznie na `{GS}`, więc nic nie ginie;
- token zapisywany jest dokładnie tak, jak zapisuje go raport, więc wartość skopiowaną z raportu
  można wkleić z powrotem bez zmian.

Przechwycenie, raport i zapisana historia zawierają **prawdziwy** separator. Token to tylko to,
co widzisz i wpisujesz.

### Skanowanie

> **Tylko Windows.** Zebra dostarcza sterowniki skanera wyłącznie dla systemu Windows, więc
> w macOS i Linuksie aplikacja w ogóle nie tworzy elementów sterujących skanerem — nie ma
> przełącznika *Połącz skaner*, listy urządzeń, menu *Skaner* ani stanu skanera na pasku stanu.
> Nic nie jest wyszarzone, bo nie istnieje ustawienie ani urządzenie, które mogłoby to włączyć.
> Cała reszta, łącznie z otwieraniem pliku z zapisanymi odczytami, działa wszędzie.

*Połącz skaner* otwiera urządzenie wskazane na liście; `ZebraAllScanners` to wartość domyślna
i odpowiada większości modeli. Każdy odczyt jest analizowany i automatycznie dopisywany do
historii.

Dane ze skanera czytane są bajt po bajcie, więc przedrostek AIM Code ID i każdy separator FNC1
docierają do analizatora dokładnie w takiej postaci, w jakiej wysłał je skaner.

W trybie Utwórz elementy sterujące skanerem są ukryte, ale podłączony skaner nadal działa:
*Skaner → Połącz / Odłącz* (<kbd>⌘K</kbd>) wciąż go przełącza, pasek stanu wciąż o nim
informuje, a odczyt, który nadejdzie przy otwartym edytorze, i tak zostanie przechwycony.

Jeśli urządzenie nie chce się połączyć, zobacz
[Rozwiązywanie problemów](#rozwiązywanie-problemów).

### Otwieranie pliku

*Plik → Otwórz plik tekstowy…* analizuje **każdy niepusty wiersz jako osobne przechwycenie** —
to szybki sposób na przerobienie partii odczytów, którą ktoś Ci przysłał. Plik czytany jest bajt
po bajcie, więc zawarte w nim surowe separatory zostają zachowane.

### Historia przechwyceń

Najnowsze na górze. Każdy wiersz pokazuje kropkę poprawności, jednowierszowe podsumowanie oraz
czas przechwycenia, źródło i liczbę AI. Data pojawia się tylko przy przechwyceniach z wcześniej-
szych dni; wiersze z dzisiaj pokazują samą godzinę.

Podsumowaniem jest **URI Digital Link** w przypadku Digital Link, a w pozostałych — **HRI**: dla
ciągu elementów `(01)09506000134352 (17)261231` czyta się lepiej niż
`01095060001343521726123110…`.

Kliknij przechwycenie prawym przyciskiem, aby skopiować jego wejście albo HRI, odesłać je na
pasek wejścia, otworzyć w edytorze, usunąć albo wyczyścić historię.

---

## Tryb Utwórz

### Edytor AI

Tabela par AI / wartość. Wpisz kod AI albo wybierz spośród **wszystkich AI, które zna silnik**,
z listy *Dodaj AI…*; kolumny **Tytuł**, **Format** i **Opis** wypełniają się w trakcie pisania,
w wybranym języku. Lista nie zaproponuje AI, który zostałby odrzucony przy tworzeniu.

Tabela zawsze utrzymuje **jeden pusty wiersz na końcu**: wpisz w nim coś, a pojawi się kolejny —
dzięki temu ciąg z wieloma AI nigdy nie wymaga polecenia *Dodaj wiersz*.

Edycja zachowuje się tak, jak tabela zachowywać się powinna: <kbd>Enter</kbd>, <kbd>Tab</kbd>,
<kbd>Shift</kbd>+<kbd>Tab</kbd> oraz kliknięcie poza komórką **zachowują** to, co wpisałeś;
<kbd>Esc</kbd> odrzuca. <kbd>Tab</kbd> przechodzi z AI do Wartości i dalej do następnego
wiersza, pomijając kolumny, których nie można edytować.

### Czego edytor nie przyjmuje

| | Co się dzieje |
|---|---|
| AI, którego silnik nie rozpoznaje | Wiersz robi się czerwony już w trakcie pisania; utworzenie zostaje odrzucone wraz z wyjaśnieniem |
| Ten sam AI dwa razy, z listy | Nie zostaje dodany — komunikat go wskazuje, a wiersz, który już go zawiera, zostaje zaznaczony |
| Ten sam AI dwa razy, wpisany ręcznie | Oba wiersze zostają oznaczone; utworzenie zostaje odrzucone z błędem `GE-S004` |
| Nie ma czego tworzyć | Komunikat; żadne przechwycenie nie powstaje |

W jednym ciągu elementów dany AI może wystąpić tylko raz — dlatego lista odmawia od razu,
zamiast pozwalać podejmować z góry skazaną próbę.

### Ciąg elementów albo Digital Link

**Utwórz ciąg elementów** składa wiersze, wstawiając separator FNC1 wszędzie tam, gdzie wymaga
tego norma. **Utwórz Digital Link** wytwarza URI; rozwiń *Cel Digital Link*, aby skierować go do
własnego resolwera zamiast do kanonicznego `id.gs1.org`:

| Pole | Działanie | Puste oznacza |
|---|---|---|
| Domena | Host resolwera, np. `example.com` | `id.gs1.org` |
| Prefiks ścieżki | Segment przed ścieżką AI, np. `/products` | brak prefiksu |

Po **powodzeniu** wynik zachowuje się dokładnie jak odczyt ze skanera: zostaje przeanalizowany,
dopisany do historii, przedstawiony jako raport i zapisany. Tworzenie sprawdza się, analizując
własny wynik, więc to, co zwraca, jest na pewno poprawne.

Po **niepowodzeniu** nic nie zostaje przechwycone. Powód pozostaje pod przyciskami tworzenia —
jeden wiersz na problem, każdy z błędem, AI, którego dotyczy, i własnym wyjaśnieniem silnika:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> Podwojone cudzysłowy w tym komunikacie pochodzą z katalogu błędów silnika, a nie z tej
> aplikacji.

### Edytowanie istniejącego przechwycenia

Kliknięcie przechwycenia prawym przyciskiem → **Utwórz** ładuje jego AI do edytora. Zmień
wartość, utwórz ponownie, a wynik pojawi się jako nowe przechwycenie — pierwotne pozostaje
nietknięte.

---

## Raport

Zaznaczone przechwycenie przedstawiane jest jako pełny raport: baner werdyktu, ciąg elementów
rozłożony na opisaną „taśmę”, metadane analizy, po jednej karcie dla każdego AI wraz z jego
interpretacjami oraz błędy i ostrzeżenia ze wskazówkami, jak je usunąć.

Przy **Digital Link** każda karta AI niesie dodatkowo swoją rolę strukturalną — **klucz
podstawowy**, **kwalifikator klucza** albo **atrybut danych** — oznaczoną kolorem w kolejności,
w jakiej występują w URI. Role pokazywane są wyłącznie dla Digital Link: w zwykłym ciągu
elementów AI nie ma żadnej pozycji w URI do opisania.

Raport podąża za wybranym językiem i motywem.

### Eksport

| | |
|---|---|
| **Zapisz raport → Jako HTML…** (<kbd>⌘S</kbd>) | Zapisuje dokładnie to, co jest na ekranie: jeden samodzielny plik, który otworzy się w dowolnej przeglądarce |
| **Zapisz raport → Jako PDF…** (<kbd>⌘P</kbd>) | Drukuje raport przez systemowe okno drukowania — wybierz tam miejsce docelowe PDF |

PDF przechodzi przez okno drukowania, bo to właśnie tam wybiera się miejsce docelowe i nazwę
pliku: **PDF ▾ → Zapisz jako PDF** w macOS, **Microsoft Print to PDF** w Windows, **Drukuj do
pliku** w Linuksie. Strony są podzielone, a tekst pozostaje zaznaczalny.

Wydruk i PDF zawsze używają jasnej palety, nawet gdy aplikacja jest w motywie ciemnym,
i zachowują kolorowe znaczniki AI oraz baner werdyktu.

Jeśli na Twoim komputerze nie skonfigurowano żadnej drukarki ani miejsca docelowego PDF,
aplikacja to zgłosi i wskaże eksport do HTML, który można wydrukować z dowolnej przeglądarki.

---

## Opcje analizy

Panel po prawej zawiera komplet opcji silnika. Zmiana czegokolwiek **natychmiast ponownie
analizuje zaznaczone przechwycenie**, więc panel służy także do pytania „jak to wejście
wyglądałoby przy tych ustawieniach?”, a nie tylko do wpływania na kolejny odczyt.

| Opcja | Wartości |
|---|---|
| Tryb analizy | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — jak głęboko prowadzić analizę |
| Język | 35 katalogów komunikatów |
| Kolejność | `LITTLE` (dd/mm/rrrr), `MIDDLE` (mm/dd/rrrr), `BIG` (rrrr/mm/dd) |
| Separator | `SLASH`, `HYPHEN`, `PERIOD` |
| Miesiąc | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Rok | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Pomiń kontrolę „requires” | Przestaje odrzucać AI, któremu brakuje wymaganego towarzysza (`GE-S005`) |
| Pomiń kontrolę „excludes” | Przestaje odrzucać zestawienia AI, których norma zabrania (`GE-S006`) |

*Przywróć ustawienia domyślne* u dołu panelu przywraca własne wartości domyślne silnika.

### Pierwsze uruchomienie

Dopóki nic nie zostało zapisane, aplikacja bierze ustawienia z **ustawień regionalnych Twojego
komputera**, a nie z wartości domyślnych silnika:

- **język** pochodzi z ustawień regionalnych systemu, z powrotem do angielskiego, gdy nie ma dla
  niego katalogu;
- **opcje daty** odczytywane są z krótkiego formatu daty Twoich ustawień regionalnych: kolejność
  składników, separator oraz to, czy miesiąc i rok są skracane.

> **Uwaga.** Ustawienia regionalne, które zapisują krótkie daty z rokiem dwucyfrowym, również
> tutaj zaczną od dwóch cyfr: zgadza się to z resztą systemu, ale różni od domyślnego ustawienia
> silnika, czyli czterech cyfr. Zmień je w panelu, a wybór zostanie zachowany.

---

## Języki

Wybór języka zmienia **całą aplikację** — menu, przyciski, edytor, panel opcji, wiersz stanu
i raport — a nie tylko komunikaty o błędach. Języki pisane od prawej do lewej odwracają okno
i raport.

Dostępnych jest wszystkie 35 języków, w dwóch warstwach pokrycia:

| | Pokrycie |
|---|---|
| Raporty analizy i wszystkie komunikaty błędów silnika | **wszystkie 35 języków** |
| Menu, przyciski i etykiety samej aplikacji | **wszystkie 35 języków** |

Języki pisane od prawej do lewej — arabski, arabski egipski, perski i urdu — odwracają okno
i raport od chwili otwarcia aplikacji.

Język jest pamiętany między uruchomieniami. Aby otworzyć w innym języku tylko raz, zobacz
[`--language`](#uruchamianie).

Kilka rzeczy celowo pozostaje nieprzetłumaczonych we wszystkich językach: skrócone nazwy pól
drukowane obok wartości surowych (`format`, `position`, `raw`, `len` …), które są
identyfikatorami, a nie słowami; nazwy przykładów i symbolik, będące terminami GS1 i branżowymi;
oraz jeden długi akapit normy, w którym połowiczne tłumaczenie wprowadzałoby w błąd.

---

## Co jest zapamiętywane

Dwa pliki w katalogu `.gaia-desktop` w Twoim katalogu domowym:

| Plik | Zawiera |
|---|---|
| `options.properties` | Wszystkie opcje analizy, motyw, informację, czy panel opcji jest otwarty, ostatnio używany skaner oraz tryb, w którym zostawiono okno |
| `captures.tsv` | Historię przechwyceń, najnowsze na górze, ograniczoną do 500 pozycji |

Opcje zapisywane są przy każdej zmianie; historia przy każdej zmianie historii. Oba pliki
zapisywane są ponownie przy zamykaniu.

Usunięcie któregokolwiek z plików przywraca stan początkowy tylko tej części i niczego więcej —
trzymane są osobno po to, by uszkodzona historia nie kosztowała Cię ustawień. Plik zepsuty
ręczną edycją także nie jest katastrofą: każde ustawienie wraca do wartości domyślnej osobno,
więc jeden błędny wiersz kosztuje tylko ten wiersz.

Historia przechowuje wejście, a nie analizę. Przy wczytywaniu jest ono analizowane ponownie, co
oznacza, że przywrócone przechwycenie odzwierciedla opcje obowiązujące **teraz**, a nie te
z chwili przechwycenia.

---

## Skróty klawiszowe

| | |
|---|---|
| <kbd>Enter</kbd> | Analiza zawartości paska wejścia |
| <kbd>⌘O</kbd> | Otwarcie pliku tekstowego |
| <kbd>⌘S</kbd> | Zapis raportu jako HTML |
| <kbd>⌘P</kbd> | Zapis raportu jako PDF (systemowe okno drukowania) |
| <kbd>⌘G</kbd> | Wstawienie tokenu separatora FNC1 |
| <kbd>⌘K</kbd> | Połączenie / odłączenie skanera (tylko Windows) |
| <kbd>⌘D</kbd> | Przełączenie motywu ciemnego |
| <kbd>⌘Q</kbd> | Zakończenie pracy |

W edytorze: <kbd>Tab</kbd> / <kbd>Shift</kbd>+<kbd>Tab</kbd> przechodzą między komórkami
edytowalnymi, <kbd>Enter</kbd> zatwierdza, <kbd>Esc</kbd> anuluje.

---

## Rozwiązywanie problemów

| Objaw | Przyczyna i rozwiązanie |
|---|---|
| Aplikacja się nie uruchamia albo zgłasza `NoClassDefFoundError` | Katalog `lib/` musi leżeć obok pliku JAR. Trzymaj je razem albo użyj dystrybucji samodzielnej |
| *„Unsupported JavaFX configuration…”* w konsoli przy starcie | Spodziewane i nieszkodliwe — biblioteka odnotowuje jedynie, w jaki sposób została wczytana. Aplikacja działa normalnie |
| Wklejony separator zdaje się znikać | Tak ma być: zostaje zamieniony na widoczny token `{GS}`, a analizator nadal otrzymuje prawdziwy znak. Zobacz [token {GS}](#pasek-wejścia-i-token-gs) |
| Nie ma przycisku *Połącz skaner* | Pracujesz w macOS albo Linuksie, gdzie sterowniki skanera nie istnieją. Zobacz [Skanowanie](#skanowanie) |
| Skaner się nie łączy | Sprawdź, czy nazwa logiczna na liście odpowiada Twojemu modelowi, czy skaner jest podłączony i czy nie zajmuje go inna aplikacja |
| Eksport do PDF zgłasza brak miejsca docelowego | Na komputerze nie skonfigurowano drukarki ani miejsca docelowego PDF. Dodaj je albo wyeksportuj do HTML i wydrukuj z przeglądarki |
| Pozycja menu pokazuje coś w rodzaju `act.hri` | Brakuje etykiety — prosimy o zgłoszenie; to usterka kompilacji, a nie ustawienie |

---

## Ograniczenia

- **Skanowanie działa tylko w systemie Windows**, bo tylko tam działają sterowniki.
- **PDF przechodzi przez okno drukowania**, a nie przez okno zapisu.
- **Cele Digital Link przyjmują domenę i prefiks ścieżki**, ale nie dodatkowe parametry
  zapytania ani fragment.
- **Dany AI może wystąpić tylko raz** w ciągu elementów — to reguła GS1, a nie ograniczenie
  aplikacji.
