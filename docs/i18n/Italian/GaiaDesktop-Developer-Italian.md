# GAIA Desktop — Guida per sviluppatori

Come il modulo viene compilato, organizzato e testato. Per ciò che l'applicazione fa vista dall'esterno,
consulta la [Guida utente](GaiaDesktop-User-Italian.md). Documento originale:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Indice

1. [Il modulo in breve](#il-modulo-in-breve)
2. [Compilare ed eseguire](#compilare-ed-eseguire)
   - [Pacchettizzazione](#pacchettizzazione)
3. [Architettura](#architettura)
   - [L'imbuto di acquisizione](#limbuto-di-acquisizione)
   - [Thread](#thread)
   - [Ricostruire o rietichettare](#ricostruire-o-rietichettare)
4. [Il token {GS}](#il-token-gs)
5. [Integrazione del lettore](#integrazione-del-lettore)
   - [Il vincolo di piattaforma](#il-vincolo-di-piattaforma)
   - [Simbologie](#simbologie)
6. [Localizzazione](#localizzazione)
   - [L'argomento `--language`](#largomento---language)
   - [Aggiungere una lingua](#aggiungere-una-lingua)
   - [Cosa non viene tradotto di proposito](#cosa-non-viene-tradotto-di-proposito)
7. [Formati dello stato salvato](#formati-dello-stato-salvato)
8. [Test](#test)
9. [Risoluzione dei problemi di build](#risoluzione-dei-problemi-di-build)
10. [Limiti noti e lavoro aperto](#limiti-noti-e-lavoro-aperto)

---

## Il modulo in breve

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Un client della libreria principale e nient'altro. Non incorpora **alcuna conoscenza GS1**: ogni
definizione di AI, regola di convalida, messaggio di errore e descrizione localizzata proviene da
[`GaiaParser`](../../../../gaia/docs/GaiaParser.md) e
[`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). In particolare, l'elenco degli AI non viene mai
copiato in questo modulo — il selettore del compositore legge il registro del nucleo stesso, quindi non può
proporre un AI che il compositore rifiuterebbe.

**Punti di ingresso:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (l'`Application` JavaFX) e
`tools.pantheum.gaia.desktop.Launcher` (la `Main-Class` del jar pacchettizzato).

Il modulo è una foglia: niente dipende da esso, ed esso non dipende da nessun altro modulo a valle.

---

## Compilare ed eseguire

- **Java 21** per eseguire, e un JDK 21 o successivo per compilare — il modulo compila con `release=21`.
  JavaFX 21 è la linea LTS corrispondente. Il jar del nucleo è ancora bytecode Java 11 e funziona invariato
  su 21.
- **Maven** nel `PATH`. In questo repository non c'è un wrapper.
- **Prima il nucleo installato.** Questo modulo risolve `gaia:1.1.0` da `~/.m2`; un jar del nucleo
  obsoleto o assente è la causa abituale di `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` è l'unico argomento da riga di comando, e l'unico modo per influire sull'avvio a parte
`-Dgaia.desktop.home=…`, che sposta l'intero profilo altrove.

`mvn javafx:run` è la strada dello sviluppo: il plugin mette JavaFX sul **module path**, così il toolkit si
avvia come modulo con nome e `GaiaDesktopApp` può essere lanciata direttamente. Un jar pacchettizzato non
può farlo — tutto è sul class path —, ed è per questo che la sua `Main-Class` è lo spessore `Launcher`. Una
sottoclasse di `Application` come `Main-Class` in quella disposizione fallisce con *"JavaFX runtime
components are missing"*.

Usa sempre `clean` — una volta, prima di compilare una serie di piattaforme, poiché svuota `target/dist/` di
tutte. Il perché è in [Risoluzione dei problemi di build](#risoluzione-dei-problemi-di-build).

### Pacchettizzazione

Tutto ciò che si distribuisce viene assemblato sotto `target/dist/gaia-desktop-<version>-<platform>/` e
nient'altro, così la cartella può essere compressa o copiata in blocco senza dover scegliere gli artefatti
in `target/` accanto a `classes/`, `maven-status/` e al resto dei file di lavoro della build.
`${dist.dir}` la nomina una volta sola; ogni plugin che scrive un artefatto punta lì.

#### Una build per piattaforma

I jar di JavaFX contengono codice nativo, quindi ogni distribuzione è per una sola piattaforma, indicata
dalla proprietà `fx.platform` come classificatore OpenJFX: `mac-aarch64`, `mac`, `win`, `linux` o
`linux-aarch64`. Cinque profili `fx-*` attivati dal sistema operativo la impostano su quella della macchina
che compila, ed è tutto ciò che serve a un semplice `mvn package` o `mvn javafx:run`; `-Dfx.platform=…` ha
la precedenza su di essi per compilarne qualunque altra, da qualunque macchina — i jar di JavaFX sono solo
download. `${dist.dir}` vale `target/dist/${fx.platform}`, così la build di una piattaforma non sovrascrive
mai quella di un'altra.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Due dettagli del pom lo rendono possibile, ed entrambi sembrano eliminabili:

- **Ogni modulo JavaFX è una dipendenza diretta** — base, graphics, controls, media e web —, ciascuno con
  `<classifier>${fx.platform}</classifier>` e un'esclusione `org.openjfx:*`. I pom di OpenJFX scelgono i
  loro jar transitivi in base al sistema operativo della *macchina che compila*, quindi lasciare che graphics
  o base arrivino in modo transitivo mette i nativi Mac in una build Windows accanto a quelli Windows.
- **La proprietà è `fx.platform`, non `javafx.platform`.** Impostata dall'esterno, la proprietà propria di
  OpenJFX viene ridefinita in funzione di sé stessa dal suo pom padre; Maven rifiuta quel pom come non valido
  e scarta in silenzio i jar di piattaforma, lasciando una build senza JavaFX.

Entrambe le disposizioni scrivono lo stesso percorso,
`target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`, e in ogni build ne viene eseguita
esattamente una. `${jar.phase}`, `assembly.skipAssembly` e `mdep.skip` impediscono che lo rivendichino
entrambe.

| | Predefinita | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — solo le classi e le risorse di questo modulo | ~50 MB — dipendenze scompattate al suo interno |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 jar, nominati uno per uno nel `Class-Path` del manifest | — |
| Da distribuire | il jar **e** `lib/` accanto | un solo file |
| Anche in `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) o `run.sh` (le altre) | lo stesso |
| E | `readme.html` e `docs/` (ogni piattaforma) | lo stesso |

Ogni distribuzione contiene anche le guide: `docs/` copiata mantenendo la struttura, così i collegamenti tra
guide e traduzioni continuano a funzionare, meno gli strumenti di traduzione (`check-i18n.py`,
`i18n-manifest.json`); e `readme.html` alla radice, un indice con un collegamento a ciascuna,
da `src/main/dist/readme.html` con `@VERSION@` filtrato alla versione del pom. Entrambi sono scritti dal passo
antrun `pick-launch-script`, che cancella prima `docs/` perché una guida rimossa non resti. I collegamenti
del readme sono scritti a mano — una guida rinominata o una traduzione aggiunta sotto `docs/` richiede anche
la sua riga lì.

Il `Class-Path` del manifest e `lib/` sono due metà della stessa cosa: il jar nomina
`lib/<artifact>-<version>.jar` per ogni dipendenza di runtime, e `copy-dependencies` riempie `lib/` con
esattamente quei nomi. È ciò che fa funzionare `java -jar`.

#### L'app macOS e lo zip di distribuzione

Un'esecuzione antrun, `mac-app-and-zip`, chiude la fase `package`. Il plugin antrun è dichiarato **dopo** i
plugin jar e assembly per questo motivo — Maven esegue le esecuzioni di una fase nell'ordine di
dichiarazione dei plugin —, così il jar esiste, in entrambe le pacchettizzazioni, quando viene eseguita.
(L'altra sua esecuzione, `pick-launch-script`, è in `prepare-package` e non è coinvolta.)

Per `mac` e `mac-aarch64` costruisce prima `GAIA Desktop.app` nella cartella di distribuzione: `Info.plist`
(con `@VERSION@` filtrato alla versione del pom), il lanciatore `Contents/MacOS/gaia-desktop` (reso
eseguibile) e `gaia-desktop.icns`, tutti da `src/main/macos/`, poi **sposta** `gaia-desktop.jar` e `lib/` in
`Contents/Java`, così il bundle è autonomo. `run.sh` guarda lì quando accanto a sé non c'è nessun jar. Il
lanciatore trova Java per versione (`java_home -v 21+`, poi l'openjdk di Homebrew — gli avvii dal Finder non
vedono né `JAVA_HOME` né `PATH`), mostra una finestra di dialogo tramite `osascript` quando non ce n'è, fa
`cd` in `~/.gaia-desktop` perché la cartella relativa `logs/` di logback sia scrivibile, e passa
`-Xdock:name`/`-Xdock:icon`. macOS associa il `java` in esecuzione al bundle (il suo identificativo di bundle
e la sua icona del Dock sono quelli dell'app); la barra dei menu mostra GAIA Desktop.

Poi, per ogni piattaforma, comprime la cartella in `target/dist/gaia-desktop-<version>-<platform>.zip` sotto
una cartella con lo stesso nome, con lo `<zip>` di Ant: `run.sh` e `Contents/MacOS/**` come insieme di file
separato con `filemode="755"`, altrimenti verrebbero estratti non eseguibili. Lo zip **non è allegato**: è
per le release di GitHub, e le quattro piattaforme vengono da quattro build, che Maven Central — un solo
caricamento immutabile per versione — non potrebbe mai accettare come un'unica release.

`project.build.outputTimestamp` è impostato all'ora della build. Altrimenti gli archiviatori di
maven-jar-plugin 3.5 e maven-assembly-plugin 3.8 datano ogni voce al 1° febbraio 1980.

L'icona dell'app è disegnata da `tools/icon/GaiaIcon.java` (non fa parte della build; la sua intestazione
riporta i passi `sips`/`iconutil`). I suoi risultati sono nel repository: `src/main/macos/gaia-desktop.icns`
per il bundle, e `src/main/resources/icons/gaia-desktop-{16..256}.png`, che `GaiaDesktopApp` imposta come
icona della finestra (Windows e Linux; macOS ignora le icone delle finestre).

#### Gli script di avvio

`run.sh` e `run.bat` vengono da `src/main/scripts/`, copiati in `dist/gaia-desktop-<version>-<platform>/`
senza filtraggio — `run.sh` è pieno di espansioni di shell `${...}` che il filtraggio di Maven sarebbe libero
di riscrivere. Un passo antrun tiene poi solo quello che la piattaforma esegue — `run.bat` per `win`,
`run.sh` per tutte le altre — e imposta il bit di esecuzione su `run.sh`, poiché `copy-resources` non lo
conserva. Decide in base al `${fx.platform}` risolto anziché tramite profili, perché l'attivazione di un
profilo non vede un valore impostato da un altro profilo: una build Linux fatta su Windows riceverebbe
altrimenti lo script dell'host.

**Non si affidano al manifest.** Ciascuno costruisce da sé il class path, `gaia-desktop.jar` seguito da ogni
`.jar` in `lib/`, nominati uno per uno invece di lasciarli a un carattere jolly `lib/*`, perché un percorso
con spazi resti una sola voce e il conteggio di ciò che è stato aggiunto sia reale. L'avvio con `-cp` è il
motivo per cui nominano esplicitamente `tools.pantheum.gaia.desktop.Launcher`: `-cp` non legge la
`Main-Class` del manifest. Segue **invece** ancora il suo `Class-Path` — un jar su `-cp` porta con sé i jar
che il suo manifest nomina, cercati subito dopo —, quindi ogni jar di `lib/` viene caricato che uno script lo
elenchi o no, e ometterne uno dal class path dello script non lo tiene fuori dalla JVM. Gli script li
elencano comunque, così un jar con manifest obsoleto, modificato a mano o incompleto parte lo stesso. Lo
stesso script serve entrambe le pacchettizzazioni: il jar `-Pfat` non ha `lib/` da aggiungere e non ne ha
bisogno, ed è anche per questo che una `lib/` vuota è una nota e non un errore.

#### Le librerie native di Zebra

La build non distribuisce niente di Zebra — né DLL, né jar di servizio. Su Windows provengono dallo Scanner
SDK di Zebra installato sulla macchina: `run.bat` aggiunge i suoi jar di servizio `Jpos*.jar` e `bin\` al
class path e `bin\` al `PATH`, dove `System.loadLibrary` trova `CSJPOSScanner.dll` / `CSJPOSScanner64.dll`
(`java.library.path` vale per impostazione predefinita il `PATH`). L'API JavaPOS e il suo caricatore di
configurazione vengono sempre da `lib\javapos-*.jar`, con o senza SDK.

**`run.bat` non aggiunge mai il jar JavaPOS proprio dell'SDK**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`.
Un tempo lo faceva, lasciando fuori `lib\javapos-*.jar` per evitare due copie dell'API — ma il `Class-Path`
del manifest li caricava comunque (vedi sopra), quindi la JVM riceveva entrambi: le classi del caricatore 4.x
da `lib\`, con accanto `jpos/res/jpos.properties` e `jcl.xsd` di `javaPOS114.jar`. Il file di proprietà fa
farsi da parte [`JposConfig`](#integrazione-del-lettore), e quel `jcl.xsd` accetta versioni di JavaPOS solo
fino alla 1.10, così ogni voce di `jpos.xml` (1.12–1.14) non superava la convalida, il registro restava vuoto
e ogni connessione falliva con l'errore 109 prima ancora di raggiungere il lettore. Basta avere il jar sul
class path perché succeda, anche dietro i jar di `lib\`. I jar di servizio di Zebra funzionano invariati
sull'API 1.14 di `lib\` — apertura, prenotazione e scansione sono verificate così su un lettore reale. Le
DLL lasciate a mano accanto a `run.bat` sono ancora rispettate: lo script imposta allora
`-Djava.library.path=<dist>;%PATH%`, aggiungendo il `PATH` perché la proprietà sostituisce del tutto il
percorso di ricerca predefinito.

`mvn javafx:run` non fa nulla di tutto ciò, quindi prenotare un lettore da un'esecuzione di sviluppo su
Windows richiede di mettere per altre vie i jar dell'SDK sul class path e il suo `bin\` nel `PATH`; per
farlo, avvia da `dist/`.

Oltre al class path, coprono ciò che `java -jar` non fa: risolvere la propria cartella per funzionare da
qualsiasi cartella di lavoro, preferire `JAVA_HOME` al `PATH`, e rifiutare una JVM precedente alla 11 con una
frase che indica il rimedio invece di `UnsupportedClassVersionError`. `GAIA_DESKTOP_OPTS` passa opzioni alla
JVM. `run.bat` usa `java.exe`, non `javaw.exe`, così la console resta aperta — Windows è l'unica piattaforma
su cui il lettore funziona, e il suo stato e gli errori JavaPOS finiscono lì.

`run.bat` sceglie anche il proprio Java, JavaFX e i servizi Zebra in base a ciò che trova — un `jdk*\`
incluso, un `javafx-sdk*\lib\` sul module path, lo Scanner SDK di Zebra in `ZEBRA_HOME` —, lasciando fuori dal
class path le copie di JavaFX di `lib\` quando usa quelle di un SDK, e passa il parser SAX del JDK, i proxy e
l'archivio dei certificati di Windows, e `--enable-native-access` su Java 17 e successivi. La sezione del
README [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) contiene la tabella.
Mantieni i fine riga CRLF del file: `cmd.exe` può non trovare un `call :label` in un file batch con soli LF.
Anche `run.sh` passa `--enable-native-access=ALL-UNNAMED` — JavaFX carica i propri nativi da `lib/`, e con
JavaFX sul class path `ALL-UNNAMED` è il modulo che lo copre —, omesso, come in `run.bat`, solo quando la
versione di Java non si può leggere.

Entrambe le disposizioni contengono i nativi JavaFX **di una sola piattaforma**, `${fx.platform}`, quindi
nessuna è portabile tra sistemi operativi o architetture. Compila una volta per destinazione — vedi
[Una build per piattaforma](#una-build-per-piattaforma).

> **Eclipse.** L'esecuzione `copy-dependencies` porta un'istruzione `<?m2e ignore?>`. m2e risolve un
> progetto aperto del workspace nella sua cartella `target/classes` invece che in un jar, e
> `copy-dependencies` si rifiuta di copiare un artefatto non pacchettizzato (MDEP-187), il che farebbe
> fallire la build automatica ogni volta che il progetto `gaia` è aperto accanto a questo. Di conseguenza,
> una build fatta solo nell'IDE non riempie `target/dist/gaia-desktop-<version>-<platform>/lib/`; esegui
> `mvn package` prima di usare il jar stesso.

---

## Architettura

| Pacchetto | Contiene |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (la finestra), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (logica di composizione senza JavaFX), `AiPair` (una riga dell'editor) |
| `desktop.parse` | `ParseOptions` (`ParseConfig` osservabile), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — il generatore del report, senza JavaFX e con test unitari senza il toolkit |
| `desktop.i18n` | `UiStrings` — il catalogo delle etichette |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### L'imbuto di acquisizione

Ogni input — digitato, incollato, aperto da un file, scansionato o composto — raggiunge il parser attraverso
un unico metodo, `MainView.capture(String, ScanSource, String)`.

È l'unica decisione di progetto attorno a cui è costruita l'app. Una composizione non ha bisogno di un report
proprio, né di una cronologia propria, né di un percorso di esportazione proprio, perché dopo l'imbuto una
stringa composta è indistinguibile da una scansionata. Qualsiasi cosa nuova che produca un input dovrebbe
passare attraverso di esso e non accanto.

### Thread

| Thread | Esegue |
|---|---|
| Thread dell'applicazione JavaFX | Tutto il parsing, il rendering e l'interfaccia. Con le tabelle degli AI già calde il parsing richiede meno di un millisecondo, quindi non serve alcun thread di lavoro |
| `gaia-scanner` | Ogni chiamata al lettore — apertura, prenotazione, abilitazione, disabilitazione, rilascio, chiusura. Un thread di lunga durata di proprietà di `ScannerService`; vedi [Integrazione del lettore](#integrazione-del-lettore) |
| Thread degli eventi JavaPOS | Le callback di `ScannerService`. `MainView.FxScannerListener` passa ciascuna con `Platform.runLater` |
| In background | L'attesa di `connect()` / `disconnect()` del lettore (la prenotazione può richiedere fino a 5 s), il ripristino della cronologia salvata all'avvio — che funge anche da riscaldamento del parser — e il controllo degli aggiornamenti (`gaia-update-check`, una richiesta a GitHub, risultato restituito con `Platform.runLater`) |

**Non modificare mai la selezione di un controllo, né distruggere il controllo, dall'interno del suo stesso
evento di selezione.** Tre crash distinti di questa app sono venuti esattamente da lì: reimpostare il
selettore di AI dentro il `clearAndSelect` stesso del popup (una `IndexOutOfBoundsException` uscita da
`ListViewBehavior`), e ricostruire il pannello che possiede la casella della lingua dal gestore stesso di
quella casella. Entrambi ora sono rinviati con `Platform.runLater`, e i motivi sono annotati in entrambi i
punti di chiamata.

### Ricostruire o rietichettare

Un cambio di lingua **ricostruisce** la cornice dal catalogo — menu, barra degli strumenti, pannello
opzioni —, il che è sicuro perché nessuno di essi possiede stato: `OptionsPane` è una vista bidirezionale di
`ParseOptions`, quindi i valori vivono nel modello e un pannello nuovo li riprende subito.

I controlli che contengono **dati dell'utente** sono campi e vengono invece rietichettati sul posto:
`BuilderPane.retext(UiStrings)` conserva le righe di AI, i campi Digital Link e la selezione. Ricostruirlo
butterebbe via il lavoro in corso — come una volta è successo.

---

## Il token {GS}

Un `TextField` di JavaFX scarta in silenzio ogni carattere sotto `0x20` (`TextInputControl.filterInput`).
Non «lo mostra in modo strano» — lo elimina. Digitazione, incolla, `setText` e `insertText` ne sono tutti
interessati, quindi `setText("01…" + GS + "10LOT")` produce `"01…10LOT"`: una stringa di elementi il cui
significato è cambiato senza avvisare, perché l'AI a lunghezza variabile ora inghiotte ciò che lo seguiva.

`desktop.parse.GsToken` è tutta la risposta. Il separatore attraversa l'interfaccia come testo letterale
`{GS}` e torna `0x1D` sulla via del parser. Il token non è inventato qui — è esattamente come
`ASCIIRevealerUtils.printable` del nucleo mostra `0x1D`, così un valore copiato da un report si incolla di
nuovo tale e quale nella barra di input.

Un filtro `TextFormatter` sul campo di input intercetta ogni via per cui può arrivare testo — digitazione, ⌘V,
il menu contestuale, il trascinamento, `setText` —, ed è per questo che la riscrittura vive lì e non in ogni
gestore.

Il percorso del lettore non passa mai per `GsToken`: le scansioni raggiungono il parser come byte.

---

## Integrazione del lettore

`ScannerService` possiede il controllo `Scanner` di JavaPOS: apertura → prenotazione → abilitazione,
riarmando l'evento dati dopo ogni scansione. I nomi logici dei dispositivi vengono da
`src/main/resources/jpos.xml`.

**Ogni chiamata al dispositivo gira su un solo thread, `gaia-scanner`, che vive quanto il servizio** — mai
sul thread FX, e mai su un thread che termina dopo la chiamata. Il servizio di Zebra raggiunge il lettore
tramite JNI sull'API COM CoreScanner, e COM lega ciò che prepara all'apartment del thread chiamante: apri su
un thread che poi termina e il lettore viene prenotato ma resta muto; chiudi su un altro thread e viene
deinizializzato l'apartment sbagliato. Chiudere sul thread FX è il caso peggiore, perché JavaFX vi chiama
`OleInitialize` e vi registra la destinazione di rilascio di ogni finestra — un `CoUninitialize` su quel
thread elimina il trascinamento in tutta l'app. È JavaFX su Windows, non una sua versione particolare.
`connect()` e `disconnect()` inviano a quel thread e attendono, quindi `MainView` le chiama entrambe da un
thread in background di breve durata.

Due regole impediscono a un dispositivo che si comporta male di bloccare la connessione. **Lo smontaggio non
si ferma mai prima del tempo:** `closeQuietly` disabilita, rilascia e chiude in passi separati, rilascia solo
se la prenotazione è detenuta, e tenta sempre `close()` — dopo una prenotazione fallita, `release()` lancia
«not claimed», e quando i passi condividevano un unico `try` questo saltava la chiusura e lasciava il
dispositivo aperto nel servizio di Zebra. **L'evento dati viene riarmato in un `finally`:** JavaPOS lo
disabilita dopo ogni evento, quindi una lettura o un gestore che lancia un'eccezione deve comunque riarmare,
altrimenti il lettore resta muto fino a una riconnessione. `ScannerLifecycleTest` mette alla prova entrambe
le regole contro un `jpos.Scanner` scriptato.

**`JposConfig` indirizza JavaPOS al `jpos.xml` incluso** prima della prima apertura. Con i jar JavaPOS di
Maven (`javapos-config-loader` 4.x) e senza `jpos/res/jpos.properties` sul class path, altrimenti ogni
apertura fallisce con *"Service does not exist in loaded JCL registry"* (errore 109), per tre motivi
distinti: il gestore dei servizi ripiega su `SimpleRegPopulator`, che legge un `jpos.cfg` serializzato e mai
`jpos.xml`; il caricatore XML cerca `jpos.xml` solo nella cartella di lavoro, non nel jar; e Xerces 2.12.2 in
`lib/` rifiuta la proprietà JAXP `accessExternalDTD` che il caricatore imposta. `JposConfig` imposta
`jpos.config.regPopulatorClass` (`JavaxRegPopulator`), `jpos.config.populatorFileURL` (il
`jar:file:…!/jpos.xml` del jar stesso) e `javax.xml.parsers.SAXParserFactory` (quella del JDK) come proprietà
di sistema — mai una già impostata, così `-Djpos.config.populatorFileURL=file:…` seleziona ancora un altro
`jpos.xml` — e non imposta nulla quando sul class path c'è un `jpos.properties` o manca il caricatore 4.x. Il
`javaPOS114.jar` dello Zebra SDK è uno di questi casi e rompe del tutto il registro, ed è per questo che
`run.bat` lo tiene fuori dal class path — vedi [Le librerie native di Zebra](#le-librerie-native-di-zebra).
`JposConfigTest` verifica da capo a fondo che il registro contenga allora i lettori del file.

**Il selettore di dispositivo è generato da quel registro**, non tenuto come elenco nel codice:
`ScannerService.logicalNames()` legge le voci caricate sul thread `gaia-scanner` e tiene quelle la cui
categoria di dispositivo è `Scanner` — le due voci di bilancia in `jpos.xml` restano fuori —, nell'ordine
`pickerOrder`: `ZebraAllScanners` per primo quando il file lo definisce, il resto in ordine alfabetico (il
registro non mantiene l'ordine del file). A un file che non lo definisce non viene aggiunto; se non si carica
nulla viene proposto solo quello predefinito, così il selettore non è mai vuoto. `MainView` mostra subito il
predefinito e completa l'elenco da un thread in background, mantenendo la scelta salvata. Così un lettore
aggiunto a `jpos.xml` — o un intero file indicato con `-Djpos.config.populatorFileURL` — compare nel selettore
senza modifiche al codice, e `JposConfigTest` confronta il selettore con una lettura indipendente del file
incluso. I servizi che quelle voci nominano sono classi di Zebra
(`com.zebra.jpos.service.scanner.*Factory`), che devono comunque essere sul class path — dallo Scanner SDK
tramite `run.bat`, o come jar di servizio di Zebra lasciati in `lib\`.

I dati scansionati vengono decodificati come **ISO-8859-1** così che ogni byte corrisponda 1:1 e
sopravvivano sia il prefisso AIM Code ID sia ogni `0x1D`; i report sono scritti in UTF-8.

Niente nel servizio compone testo per un utente. Gli eventi escono come costanti di `ScannerListener.Event`
più i valori che variano, e `MainView` fornisce il testo dal catalogo — altrimenti la riga di stato sarebbe
in inglese in un'applicazione tradotta in 35 modi. Le righe di log fanno eccezione e restano in inglese.

### Il vincolo di piattaforma

`ScannerService.isSupportedPlatform()` è un solo test su `os.name`, e `MainView` lo interroga prima di
costruire qualsiasi controllo del lettore: su macOS e Linux non c'è l'interruttore, né il selettore di
dispositivo, né il menu Lettore (e quindi nemmeno <kbd>⌘K</kbd>), né l'etichetta di stato. Non sono
nascosti — non vengono mai costruiti.

Zebra distribuisce i suoi servizi JavaPOS solo per Windows, e altrove niente fallisce presto: i jar si
risolvono e il controllo si costruisce, ed è `open()` che alla fine fallisce per lo strato nativo mancante,
diversi secondi e una finestra di dialogo dopo.

La regola riguarda i *servizi*, non JavaPOS. Un fornitore che distribuisse un servizio multipiattaforma
funzionerebbe qui, e ampliare la regola è una modifica solo a `supportsJavaPos(String)` — che è
package-private e testata contro stringhe di `os.name`, così entrambe le risposte sono coperte su qualunque
macchina esegua la build.

### Simbologie

`Symbologies` associa tutti i **78** tipi di dati di scansione JavaPOS `SCAN_SDT_*` a nomi visualizzati, e al
`DataCarrierType` del nucleo dove i due vocabolari coincidono. JavaPOS è il più fine dei due — separa UPC-A
da EAN-13, e il Data Matrix semplice dal GS1 DataMatrix, dove AIM raggruppa un'intera famiglia sotto un
unico carattere di codice.

Un tipo fuori dalla tabella riceve comunque un nome utilizzabile, trovato per riflessione su `ScannerConst`,
perché i servizi dei fornitori riportano davvero valori fuori dall'elenco della specifica. Un test percorre
`ScannerConst` e fallisce se un tipo dichiarato non ha un nome — il che intercetta anche costanti che un
futuro JavaPOS aggiunga.

Alcune corrispondenze di supporto sono lasciate di proposito a `UNKNOWN` anziché indovinate: il 2 di 5
standard (AIM distingue `]R`/`]S` in base al numero di barre di avvio/arresto, che JavaPOS non riporta), i
codici postali a quattro stati e TLC-39.

---

## Localizzazione

Le etichette vivono in un file di proprietà per lingua sotto
`src/main/resources/i18n/ui_<CODE>.properties`, con gli stessi codici dei cataloghi di errori del nucleo
(`EN`, `FR`, `PCM`, …). Esistono tutti e 35 e tutti e 35 hanno un insieme di chiavi identico. I file sono
letti come **UTF-8**, non come l'ISO-8859-1 che `Properties` presuppone, così le traduzioni sono salvate in
forma leggibile anziché come escape `\uXXXX`.

**Il ripiego avviene per chiave, non per file** — ed è ciò che rende utile una lingua tradotta in parte non
appena arriva la sua prima chiave. L'inglese è il contratto: ogni chiave deve esistere lì.

| Livello | Chiavi | Copertura |
|---|---|---|
| Vocabolario condiviso del report | 31 | tutte le 35 lingue |
| Cornice del desktop — menu, pulsanti, compositore, opzioni, riga di stato, impaginazione del report, stato del lettore, testi delle finestre di dialogo | 114 | tutte le 35 lingue |

Sei dei termini condivisi (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) non sono
usati dal report di questa app.
Sono mantenuti di proposito; l'intestazione del file lo dice.

### L'argomento `--language`

`GaiaDesktopApp.languageOverride()` legge `--language=…` tramite il `getParameters().getNamed()` proprio di
JavaFX, lo risolve con `Languages.fromArgument` — codice di catalogo, nome dell'enum o tag BCP 47, senza
distinzione tra maiuscole e minuscole — e lo passa a `MainView`.

**La sostituzione vale per l'esecuzione, non per il file.** `MainView` conserva in `savedLanguage` la lingua
che ha caricato e riscrive *quella*, finché `languageChosenInSession` non indica che l'utente ha scelto una
lingua da sé; allora la sua scelta persiste normalmente. Questa separazione è tutto il progetto: un profilo
da chiosco o un collegamento dimostrativo può aprirsi in spagnolo senza riscrivere ciò che il proprietario
della macchina aprirà domani, mentre una vera scelta fatta nel pannello opzioni resta.

Un valore che non nomina alcun catalogo viene registrato nel log insieme ai codici accettati e ignorato.
Rifiutarsi di partire per un refuso in un collegamento sarebbe il guasto peggiore, e `Languages.fromArgument`
restituisce `null` invece di lanciare un'eccezione per lo stesso motivo.

### Aggiungere una lingua

Non serve alcuna modifica al codice.

1. Modifica `ui_<CODE>.properties` — ogni lingua ha già un file.
2. Traduci i valori. Lascia al loro posto i segnaposto `{0}` / `{1}`.
3. Compila. Due test fanno rispettare il contratto: uno fallisce se il codice chiede una chiave che
   l'inglese non definisce, l'altro se l'insieme di chiavi di un catalogo si discosta da quello inglese.

Il primo test analizza i **sorgenti** cercando letterali di chiave invece di usare la riflessione sulle
classi, perché le chiavi esistono solo come letterali stringa nei punti di chiamata. Per questo un nuovo
messaggio va costruito con la chiave scritta in linea — `strings.format("st.scannerError", …)` — e non
cercata tramite una variabile, altrimenti il test non la vede.

### Cosa non viene tradotto di proposito

- i nomi di campo laconici in carattere monospaziato accanto ai valori grezzi — `format`, `position`,
  `length`, `raw` e `part` della scheda dell'elemento, `AI`, `sep`, `pos` e `len` del nastro, e le quattro
  celle che localizzano un errore (`AI`, `position`, `level`, `stage`), i cui valori sono comunque nomi di
  enum non tradotti;
- i nomi degli input di esempio, che nominano costrutti GS1, e i nomi delle simbologie, che sono nomi propri;
- il paragrafo di rimedio di `GE-C084` — prosa normativa, dove una traduzione a metà è peggio dell'inglese;
- il controllo di riserva per AI sconosciuti di `ElementBuilder`, irraggiungibile dall'interfaccia: l'editor
  controlla da sé gli AI sconosciuti e li segnala dal catalogo;
- le righe di log.

Resta una lacuna: `ScanSource.label()` fornisce `manual` / `scanner` / `file` / `built` in inglese alla riga
della cronologia e all'intestazione del report. Colmarla richiede più di una chiave — `HistoryCell` non ha
`UiStrings`, e la fabbrica di celle dell'elenco viene installata una sola volta, quindi anche le celle
andrebbero aggiornate a un cambio di lingua.

---

## Formati dello stato salvato

Due file sotto `~/.gaia-desktop/`, tenuti separati perché cambiano con ritmi diversi — un registro delle
acquisizioni corrotto o modificato a mano non dovrebbe costare all'utente le sue impostazioni. Cambia la
cartella con `-Dgaia.desktop.home=…`, ed è così che i test restano fuori dalla vera cartella home.

**`options.properties`** — scritto quando cambia un'opzione, e all'uscita.

| Chiave | Significato |
|---|---|
| `parse.mode`, `parse.language` | Profondità della pipeline e lingua dei messaggi |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Resa delle date |
| `check.skipRequires`, `check.skipExcludes` | Deroghe ai controlli strutturali |
| `ui.theme` | `LIGHT` o `DARK` |
| `ui.optionsPane` | Pannello aperto? |
| `ui.scannerDevice` | Ultimo nome logico JavaPOS |
| `ui.mode` | `PARSE`, `BUILD` o `COMBINE` |
| `update.check` | Se il controllo giornaliero degli aggiornamenti viene eseguito (predefinito `true`) |
| `update.lastCheck` | Quando è stato eseguito l'ultima volta, ISO-8601; se assente o illeggibile, un controllo è dovuto |
| `update.skipVersion` | Una release per cui l'utente ha scelto *Salta questa versione*; non viene riproposta automaticamente |

Un file mancante o danneggiato non è mai fatale: ogni valore ripiega individualmente, così una riga errata
costa solo quella riga. Al primo avvio, senza alcun file, le opzioni vengono inizializzate da `SystemDefaults`
a partire dalle impostazioni regionali della macchina anziché dai valori predefiniti di GAIA — il formato di
data breve della locale dà l'ordine dei componenti, il separatore e se mese e anno sono abbreviati.

Non passare mai un null a `Properties.setProperty`: lancia un'eccezione, e `persistOptions()` viene eseguito
da gestori di eventi dell'interfaccia, dove un'eccezione sfugge sul thread FX. È un crash reale che questa
app ha già avuto.

**`captures.tsv`** — scritto quando cambia la cronologia, e all'uscita. Le più recenti per prime, con un
massimo di **500** voci.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Viene salvato solo ciò che non si può ricalcolare. Il `ParseResult` **non** viene serializzato — viene
ricavato di nuovo analizzando l'input al caricamento, così una cronologia ripristinata riflette le opzioni in
vigore *adesso*. Gli input contengono spesso FNC1 e qualunque altra cosa emetta un lettore, quindi il campo
di input viene sottoposto a escape (`\\`, `\t`, `\n`, `\r`, e `\xNN` per tutto il resto sotto `0x20`) e il
file viene letto e scritto come ISO-8859-1, byte per byte.

I file scritti prima che la data venisse registrata contengono solo un orario; si caricano ancora, datati a
oggi, perché la data vera non è mai stata annotata.

---

## Test

```bash
(cd gaia-desktop && mvn test)
```

198 test, di proposito **indipendenti dal toolkit JavaFX**, il che li mantiene veloci e senza interfaccia
grafica. Coprono il generatore del report, la logica di composizione e le ricerche di AI, la conversione di
`{GS}`, i valori predefiniti derivati dalla locale, entrambi gli archivi (incluso un andata e ritorno con
`0x1D` e file danneggiati di proposito), la tabella delle simbologie, la regola di piattaforma del lettore,
lo stub di versione e i cataloghi.

Tre meritano di essere conosciuti perché difendono contratti più che funzioni:

| Test | Fallisce quando |
|---|---|
| `CatalogueCoverageTest` | Il codice chiede una chiave che `ui_EN.properties` non definisce, o uno dei 35 file si discosta dall'insieme di chiavi inglese |
| `SymbologiesTest` | Una costante `SCAN_SDT_*` dichiarata da JavaPOS non ha un nome visualizzato — inclusa una aggiunta da un aggiornamento di JavaPOS |
| `ScannerServiceTest` | La regola di piattaforma cambia risposta per Windows o per qualsiasi altro sistema |

`UiStrings` è di proposito tollerante a runtime — una chiave sconosciuta viene mostrata come la chiave
stessa invece di lanciare un'eccezione a metà impaginazione —, ed è proprio per questo che il test del
catalogo deve esistere.

**Non coperto:** tutto ciò che richiede un toolkit attivo — il comportamento del focus, il cambio di
modalità, la modifica delle tabelle, il cambio di lingua, e entrambi i crash descritti in [Thread](#thread).
Sono stati verificati pilotando la finestra reale, ma una regressione in essi non farebbe fallire la build.
Aggiungere TestFX o Monocle colmerebbe questa lacuna.

---

## Risoluzione dei problemi di build

| Sintomo | Causa e rimedio |
|---|---|
| `package tools.pantheum.gaia does not exist` | Il nucleo non è installato. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | La `Main-Class` del jar deve essere `Launcher`, non `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* all'avvio | Previsto e innocuo — JavaFX segnala di trovarsi sul class path. È esattamente il motivo per cui esiste `Launcher` |
| Il jar non parte, `NoClassDefFoundError` | Il jar predefinito contiene solo le classi di questo modulo; `dist/lib/` deve stargli accanto. Usa `-Pfat` per un unico file autonomo |
| `Artifact has not been packaged yet … MDEP-187` in Eclipse | Risoluzione del workspace di m2e. Vedi la nota in [Pacchettizzazione](#pacchettizzazione) |
| Compaiono file `… 2.properties` duplicati in una build | Il progetto si trova in una cartella sincronizzata con iCloud e la sincronizzazione ha prodotto copie in conflitto durante la build. Usa sempre `mvn clean package`; meglio ancora, escludi `target/` dalla sincronizzazione |
| Una voce di menu mostra una chiave come `act.hri` | Quella chiave manca in `ui_EN.properties`. La build avrebbe dovuto rilevarlo — vedi [Test](#test) |

---

## Limiti noti e lavoro aperto

- **Nessun test dell'interfaccia senza display.** Vedi [Test](#test).
- **Artefatti specifici per piattaforma.** Entrambe le pacchettizzazioni includono i nativi JavaFX di una
  sola piattaforma; compilane una per destinazione con `-Dfx.platform` (qualunque macchina può compilarle
  tutte). Nessuna include un runtime Java — servirebbe `jpackage`, che compila solo per il sistema operativo
  su cui gira.
- **L'input dal lettore è solo per Windows.** Vedi [Il vincolo di piattaforma](#il-vincolo-di-piattaforma).
- **`ScanSource.label()` non è localizzato.** Vedi [Cosa non viene tradotto di
  proposito](#cosa-non-viene-tradotto-di-proposito).
- **`UiState.mode` è una `String`** (`"PARSE"` / `"BUILD"`) mentre `Theme` accanto è un enum, quindi il
  letterale compare in più punti. Un enum li eliminerebbe.
- **La destinazione Digital Link si limita a dominio e prefisso di percorso.** `BuilderDigitalLinkConfig`
  supporta anche parametri di query aggiuntivi e un frammento; l'editor non li espone.
- **Le coppie di AI mutuamente esclusive (`GE-S006`) non vengono segnalate nell'editor** come accade per gli
  AI sconosciuti e duplicati — la composizione semplicemente fallisce.
- **Il catalogo degli errori del nucleo raddoppia gli apici** (`value ''09506000134350''`), cosa che compare
  tale e quale nei fallimenti di composizione e nel report. Sembra un escape degli apici di `MessageFormat`
  che non viene annullato; la correzione spetta al nucleo.
