# Configurazione di un lettore Zebra — GAIA Desktop per Windows

Come installare e configurare un lettore di codici a barre Zebra perché GAIA Desktop legga
direttamente i codici GS1: ogni scansione viene analizzata e aggiunta alla cronologia, con il
prefisso AIM Code ID e ogni separatore FNC1 intatti. Versione originale:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

GAIA Desktop è stato sviluppato e testato con un imager portatile **Zebra DS8108** collegato via
USB. I passaggi valgono per qualsiasi lettore 2D Zebra supportato dallo Scanner SDK di Zebra; i
codici a barre di configurazione e i nomi dei menu di un modello specifico si trovano nella sua
Quick Reference Guide o Product Reference Guide.

> **Solo Windows.** I driver dei lettori Zebra esistono solo per Windows, quindi su macOS o Linux
> GAIA Desktop non crea alcun comando del lettore. Tutto il resto dell'applicazione funziona su
> ogni piattaforma.

---

## Indice

1. [Cosa serve](#1-cosa-serve)
2. [Installare Java](#2-installare-java)
3. [Installare lo Zebra Scanner SDK](#3-installare-lo-zebra-scanner-sdk)
4. [Installare 123Scan](#4-installare-123scan)
5. [Aggiornare il firmware del lettore Zebra](#5-aggiornare-il-firmware-del-lettore-zebra)
6. [Impostare la modalità USB del lettore Zebra](#6-impostare-la-modalità-usb-del-lettore-zebra)
7. [Configurare il lettore Zebra per GS1](#7-configurare-il-lettore-zebra-per-gs1)
8. [Installare e avviare GAIA Desktop](#8-installare-e-avviare-gaia-desktop)
9. [Collegare il lettore](#9-collegare-il-lettore)
10. [Riferimento di configurazione](#10-riferimento-di-configurazione)
11. [Risoluzione dei problemi](#11-risoluzione-dei-problemi)
12. [Lettori 2D Zebra supportati](#12-lettori-2d-zebra-supportati)

---

## 1. Cosa serve

| | |
|---|---|
| **Lettore** | Un lettore 2D Zebra, con il suo cavo USB — vedere [Lettori 2D Zebra supportati](#12-lettori-2d-zebra-supportati) |
| **Computer** | Windows 10 o 11, 64 bit |
| **Java** | Java 21 o successivo, **64 bit** |
| **Zebra Scanner SDK per Windows** | **64 bit**, dal sito di assistenza Zebra — fornisce il driver e i servizi JavaPOS usati da GAIA Desktop |
| **Zebra 123Scan** | L'utilità di configurazione gratuita di Zebra, dal suo sito di assistenza — serve ad aggiornare il firmware del lettore e a verificarne le impostazioni |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Facoltativo** | La **Product Reference Guide** (PRG) del lettore, che contiene i codici a barre di configurazione |

Come si collegano i pezzi:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop include l'API JavaPOS e la configurazione che dà il nome al lettore (`jpos.xml`,
dentro `gaia-desktop.jar`). L'SDK Zebra fornisce il resto: il driver che comunica con il lettore e
le classi di servizio Zebra che stanno tra JavaPOS e quel driver.

---

## 2. Installare Java

GAIA Desktop richiede **Java 21 o successivo**, nella versione a **64 bit**, per corrispondere ai
driver Zebra a 64 bit.

1. Installate un JDK o JRE Java 21 per Windows x64 — ad esempio Eclipse Temurin da adoptium.net.
   Lasciate che il programma di installazione imposti `JAVA_HOME`, se lo propone.
2. Verificate in un prompt dei comandi:

   ```
   java -version
   ```

   La prima riga deve indicare la versione 21 o successiva; un Java a 64 bit mostra anche
   `64-Bit Server VM`.

`run.bat` cerca Java in quest'ordine: una cartella `jdk*` accanto a `run.bat`, poi `JAVA_HOME`,
poi `java` nel `PATH`. Rifiuta qualsiasi versione precedente alla 21, con un messaggio che lo dice.

---

## 3. Installare lo Zebra Scanner SDK

1. Scaricate lo **Zebra Scanner SDK per Windows (64 bit)** dal sito di assistenza Zebra.
2. Avviate il programma di installazione. Se offre una scelta di componenti, assicuratevi che il
   componente **JPOS** (JavaPOS) sia incluso.
3. Mantenete il percorso di installazione predefinito. GAIA Desktop cerca la parte JavaPOS
   dell'SDK in:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   e si aspetta i jar di servizio di Zebra (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`,
   …) e le librerie native (`CSJPOSScanner64.dll`, …) nella sua cartella `bin\`. Se l'SDK è
   installato altrove, vedere [ZEBRA_HOME](#zebra_home).
4. **Riavviate Windows** dopo l'installazione. Il driver CoreScanner dell'SDK funziona in
   background e deve essere attivo prima che GAIA Desktop possa raggiungere il lettore; un riavvio è
   il modo più semplice per esserne certi.

---

## 4. Installare 123Scan

123Scan è l'utilità gratuita di Zebra per configurare i suoi lettori. Si usa nel passaggio
successivo per aggiornare il firmware del lettore, e in seguito è il modo più semplice per
verificarne e modificarne le impostazioni.

1. Scaricate **123Scan** dal sito di assistenza Zebra e avviate il programma di installazione.
2. Collegate il lettore Zebra e avviate 123Scan. Dovrebbe trovare il lettore e mostrarne il modello,
   il numero di serie e la versione attuale del firmware. Altrimenti, provate un'altra porta USB o un
   altro cavo prima di proseguire — GAIA Desktop non vedrà un lettore che 123Scan non vede.

---

## 5. Aggiornare il firmware del lettore Zebra

Portate il firmware del lettore all'ultima versione prima di configurarlo. Gli aggiornamenti del
firmware correggono problemi di decodifica e mantengono il lettore allineato al driver Zebra
installato dall'SDK.

1. Chiudete **GAIA Desktop** e qualsiasi altro programma che possa usare il lettore: durante
   l'aggiornamento 123Scan ha bisogno del lettore in esclusiva.
2. In 123Scan, con il lettore collegato, scegliete l'opzione di aggiornamento del firmware. 123Scan
   confronta il firmware del lettore con l'ultimo pubblicato da Zebra e propone di scaricarlo e
   installarlo. Se il computer non ha accesso a Internet, scaricate il firmware del lettore dalla
   sua pagina di assistenza sul sito Zebra e caricate il file in 123Scan.
3. Avviate l'aggiornamento e **lasciate il lettore collegato finché 123Scan non segnala che è
   terminato**. Richiede alcuni minuti; alla fine il lettore si riavvia. Scollegarlo a metà può
   renderlo inutilizzabile.
4. Verificate che 123Scan mostri ora la nuova versione del firmware.

Un aggiornamento può riportare alcune impostazioni ai valori predefiniti; per questo il lettore
viene configurato nei due passaggi successivi, dopo il firmware.

---

## 6. Impostare la modalità USB del lettore Zebra

È il passaggio più spesso dimenticato. **Appena uscito dalla confezione, un lettore Zebra si
comporta come una tastiera USB** (modalità USB HID Keyboard): «digita» ogni codice a barre dove si
trova il focus. In questa modalità *Connetti lettore* di GAIA Desktop non riesce a trovarlo, e tutto
ciò che viene digitato in un campo di testo perde i separatori FNC1, cambiando il significato di
una stringa di elementi GS1.

Passate il lettore a una modalità con cui il driver Zebra comunica direttamente:

| Modalità USB del lettore | Nome del dispositivo in GAIA Desktop | Note |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`, oppure `ZebraAllScanners` | Consigliata |
| SNAPI (con o senza imaging) | `ZebraScannerSNAPI`, oppure `ZebraAllScanners` | Il protocollo USB proprio di Zebra; funziona anch'esso |
| IBM Table-top USB | `ZebraUSBTableTop`, oppure `ZebraAllScanners` | Pensata per lettori da banco |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`, oppure `ZebraAllScanners` | |
| USB HID Keyboard (impostazione di fabbrica) | — | **Non si collega** |

`ZebraAllScanners`, il nome di dispositivo predefinito di GAIA Desktop, accetta un lettore in
qualsiasi delle modalità supportate: una volta impostata la modalità, il valore predefinito di
solito basta.

Per cambiare modalità, in alternativa:

- **scansionate il codice a barre di configurazione** della modalità — ad esempio *IBM Hand-held
  USB* — nella Quick Reference Guide o nella Product Reference Guide del lettore (sezione USB Host
  Types / USB Device Type); il lettore emette un bip di conferma. Oppure
- **usate 123Scan** ([passaggio 4](#4-installare-123scan)): collegate il lettore, aprite la sua
  configurazione e impostate il tipo di host USB.

Dopo il cambio di modalità, Windows rileva di nuovo il lettore come un altro tipo di dispositivo;
attendete qualche secondo prima di collegarvi.

Per riportare in seguito il lettore in modalità tastiera — per un altro software — scansionate il
codice a barre di configurazione *USB Keyboard (HID)*, oppure ripristinate le impostazioni di
fabbrica.

---

## 7. Configurare il lettore Zebra per GS1

GAIA Desktop analizza meglio quando il lettore gli dice che tipo di codice a barre ha letto.

### Trasmettere l'AIM Code ID

Impostate **Transmit Code ID Character** su **AIM Code ID Character**, con il codice a barre di
configurazione della Product Reference Guide (User Preferences) o in 123Scan. Ogni scansione inizia
allora con il suo identificatore di simbologia — `]C1` per GS1-128, `]d2` per GS1 DataMatrix, `]Q3`
per GS1 QR Code, `]e0` per GS1 DataBar —, che GAIA Desktop usa per riconoscere il supporto dati e
applicarne le regole.

Senza di esso, GAIA Desktop analizza comunque la stringa di elementi e ricava la simbologia dalla
scansione stessa, ma il report non ha un AIM Code ID da mostrare, e un codice a barre semplice come
un EAN-13 si distingue meno facilmente da una stringa di elementi GS1.

### Verificare le simbologie

Assicuratevi che i tipi di codice a barre che usate siano **abilitati**: di solito GS1-128,
GS1 DataMatrix, GS1 DataBar, GS1 QR Code ed EAN/UPC. La maggior parte è attiva per impostazione
predefinita sui lettori 2D Zebra; 123Scan mostra le impostazioni attuali, e la Product Reference
Guide contiene un codice a barre per abilitare ciascuna.

### Lasciare disattivate le funzioni di analisi GS1 di Zebra

I firmware recenti aggiungono funzioni GS1 che lavorano nel lettore stesso — **Label Parse+**,
**GS1 Digital Link** e **GS1 Retail POS parsing**. Lasciatele **disattivate**, come alla consegna:
riformattano o accorciano ciò che il lettore invia, e GAIA Desktop ha bisogno dei dati grezzi per
analizzarli e convalidarli. Per cosa fa ciascuna, vedere
[Lettori 2D Zebra supportati](#12-lettori-2d-zebra-supportati).

### Niente prefissi né suffissi

Non aggiungete prefisso, suffisso né tasto Invio all'uscita del lettore. GAIA Desktop riceve ogni
scansione come messaggio completo tramite JavaPOS e non ne ha bisogno; i caratteri in più
finirebbero nella stringa di elementi.

---

## 8. Installare e avviare GAIA Desktop

1. Estraete `gaia-desktop-1.0.0-win.zip`, ad esempio in `C:\Tools\`. Contiene una sola cartella:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Collegate il lettore.
3. Fate doppio clic su **`run.bat`**. Accanto all'applicazione si apre una finestra della console
   che resta aperta: lo stato del lettore e gli errori JavaPOS vengono segnalati lì.
4. Leggete la prima riga della console. Dice che cosa ha trovato `run.bat`:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Se termina con **`Zebra services from none (Zebra Scanner SDK not installed)`**, l'SDK non è
   stato trovato nel percorso predefinito — installatelo
   ([passaggio 3](#3-installare-lo-zebra-scanner-sdk)) o impostate [ZEBRA_HOME](#zebra_home).
   L'applicazione si avvia comunque, ma il lettore non può collegarsi.

---

## 9. Collegare il lettore

1. Nella barra degli strumenti, il **selettore del dispositivo** elenca i nomi di lettore che GAIA
   Desktop conosce. Lasciatelo su **`ZebraAllScanners`**, oppure scegliete il nome per la modalità
   USB del lettore dalla tabella del [passaggio 6](#6-impostare-la-modalità-usb-del-lettore-zebra).
2. Fate clic su **Connetti lettore**, oppure premete **Ctrl+K** (anche nel menu
   **Lettore**).
3. La barra di stato diventa **`lettore: ZebraAllScanners`** (o il nome scelto), e la riga
   di stato segnala *Lettore connesso — ZebraAllScanners*. Il pulsante ora riporta
   **Disconnetti lettore**.
4. Scansionate un codice a barre GS1. Compare in cima alla cronologia delle acquisizioni e si apre
   il suo report. Con l'AIM Code ID attivo, il report indica il supporto dati — *GS1-128*,
   *GS1 DataMatrix* — e la stringa di elementi conserva ogni separatore FNC1.

Il selettore del dispositivo ricorda la scelta tra un avvio e l'altro. Il lettore resta collegato
finché non lo scollegate o chiudete l'applicazione. Le scansioni funzionano anche nelle modalità
**Comporre** e **Combina**: in Combina, ogni scansione diventa uno dei
codici a barre da combinare.

Finché GAIA Desktop tiene il lettore, gli altri programmi non possono usarlo, e viceversa: chiudete
123Scan e qualsiasi altro software di cassa o di lettura prima di collegarvi.

---

## 10. Riferimento di configurazione

### Nomi dei dispositivi

Il selettore del dispositivo viene riempito dal `jpos.xml` incluso in `gaia-desktop.jar`. Le sue
voci di lettore:

| Nome del dispositivo | Tipo di lettore | Da usare con |
|---|---|---|
| `ZebraAllScanners` | qualsiasi | **Predefinito.** Qualsiasi modalità USB supportata |
| `ZebraAllScanners114` | qualsiasi | La versione JavaPOS 1.14 dello stesso servizio |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Un lettore in modalità IBM Hand-held USB |
| `ZebraUSBTableTop` | IBM Table-top USB | Modalità da banco |
| `ZebraUSBOPOS` | USB OPOS | Modalità USB OPOS |
| `ZebraScannerSNAPI` | SNAPI | Modalità SNAPI |
| `ZebraScannerUSB` | qualsiasi USB | Qualsiasi modalità USB |
| `ZebraScannerSerialSSI` | seriale SSI | Lettori con cavo seriale (non USB) |
| `ZebraScannerSerialNIXMODB` | seriale NIXMODB | Lettori con cavo seriale |
| `ZebraVDI` | VDI | Lettori inoltrati a un desktop virtuale |

### ZEBRA_HOME

Se l'SDK Zebra è installato in un percorso diverso da quello predefinito, impostate `ZEBRA_HOME`
sulla sua cartella `JPOS` prima di avviare `run.bat` — ad esempio in un piccolo file batch accanto
a esso:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

`run.bat` prende allora i jar di servizio di Zebra da `%ZEBRA_HOME%bin` e aggiunge quella cartella
al `PATH` per le DLL Zebra.

### Un jpos.xml personalizzato

Per aggiungere un nome di dispositivo o modificare le impostazioni di una voce, copiate `jpos.xml`
fuori da `gaia-desktop.jar` (è uno zip; qualsiasi strumento di estrazione lo apre), modificate la
copia e indicatela a GAIA Desktop:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

Il selettore del dispositivo elenca allora i lettori di quel file. Con più lettori Zebra collegati,
una voce può essere legata a uno di essi tramite la sua proprietà `ScannerModel` o `SerialNumber`,
come nella voce `ZebraAllScanners`.

### Altre opzioni

| Impostazione | Come |
|---|---|
| Lingua dell'applicazione | `run.bat --language=FR` (solo per questo avvio), oppure il Pannello opzioni |
| Opzioni Java aggiuntive | `set GAIA_DESKTOP_OPTS=…` prima di `run.bat` |
| Un Java incluso | mettete una cartella `jdk-21…` accanto a `run.bat`; viene usata prima di `JAVA_HOME` |

---

## 11. Risoluzione dei problemi

Gli errori compaiono in una finestra di dialogo durante il collegamento, nella finestra della
console e nel file di log `logs\gaia-desktop.log`, nella cartella da cui è stato avviato `run.bat`
(la cartella di GAIA Desktop, se si fa doppio clic su `run.bat`). Il numero tra parentesi quadre
nel log è il codice di errore JavaPOS.

| Cosa vedete | Causa probabile | Cosa fare |
|---|---|---|
| Non c'è il pulsante *Connetti lettore* | Non è in esecuzione su Windows | La lettura esiste solo su Windows |
| 123Scan non trova il lettore | Cavo, porta USB, oppure un altro programma tiene il lettore | Provate un altro cavo o un'altra porta; chiudete GAIA Desktop e gli altri software di lettura. GAIA Desktop non vedrà un lettore che 123Scan non vede |
| Console: `Zebra services from none (Zebra Scanner SDK not installed)` | L'SDK non è nel percorso predefinito | Installate l'SDK a 64 bit ([passaggio 3](#3-installare-lo-zebra-scanner-sdk)), oppure impostate [ZEBRA_HOME](#zebra_home) |
| Errore **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | I jar di servizio di Zebra non sono stati trovati | Come sopra — controllate la riga `Zebra services from` della console |
| Errore **109**: *Service does not exist in loaded JCL registry* | Il nome del dispositivo non è nella configurazione del lettore, oppure il `javaPOS114.jar` dell'SDK è finito nel class path | Scegliete un nome dal selettore del dispositivo; non aggiungete `javaPOS114.jar`; se usate un `jpos.xml` personalizzato, verificate che il nome ci sia |
| Errore **107** (nessun hardware), oppure il collegamento fallisce con il lettore collegato | Il lettore è ancora in modalità tastiera USB, oppure il driver CoreScanner non è in esecuzione | Impostate la modalità USB ([passaggio 6](#6-impostare-la-modalità-usb-del-lettore-zebra)); riavviate Windows dopo aver installato l'SDK; provate un'altra porta USB |
| Errore **112** (timeout) o **102** (già acquisito) al collegamento | Un altro programma tiene il lettore | Chiudete 123Scan e qualsiasi altro software di lettura o di cassa, poi ricollegatevi |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` nella console | Le DLL Zebra non possono essere caricate — spesso un SDK a 32 bit con un Java a 64 bit | Installate l'SDK a 64 bit; verificate che `java -version` indichi una VM a 64 bit |
| Le scansioni vengono digitate nella barra di input invece di arrivare come acquisizioni | Il lettore è in modalità tastiera USB | Impostate la modalità USB ([passaggio 6](#6-impostare-la-modalità-usb-del-lettore-zebra)) |
| Collegato, ma le scansioni non arrivano | Nome di dispositivo sbagliato per la modalità del lettore, oppure il lettore è stato ricollegato durante la connessione | Scollegate, scegliete `ZebraAllScanners`, ricollegatevi |
| Il report non mostra l'AIM Code ID né il supporto dati | La trasmissione dell'AIM Code ID è disattivata | Attivate *Transmit Code ID Character → AIM Code ID* ([passaggio 7](#7-configurare-il-lettore-zebra-per-gs1)) |
| A una stringa di elementi mancano i separatori, oppure gli AI sono attaccati | Scansionata in modalità tastiera, oppure digitata a mano senza `{GS}` | Usate una modalità USB JavaPOS; digitando, inserite i separatori con il pulsante `{GS}` |
| Un tipo di codice a barre non viene mai letto | Quella simbologia è disattivata sul lettore | Attivatela ([passaggio 7](#7-configurare-il-lettore-zebra-per-gs1)) |

Se un problema persiste, conservate insieme la riga `run.bat:` della console, il testo della
finestra di errore e `logs\gaia-desktop.log`: insieme mostrano quale Java, quali servizi Zebra e
quale nome di dispositivo sono stati usati, e dove esattamente il collegamento è fallito.

---

## 12. Lettori 2D Zebra supportati

GAIA Desktop ha bisogno di un lettore in grado di leggere i codici a barre in cui sono stampati i
dati GS1 — GS1-128, GS1 DataBar, EAN/UPC e, per la maggior parte degli usi, i codici 2D:
**GS1 DataMatrix** e **GS1 QR Code**, che è anche la forma abituale di stampa di un URI **GS1
Digital Link**. Serve quindi un **imager 2D** Zebra: un lettore lineare (1D) non può leggere affatto
codici DataMatrix o QR.

Il lettore non deve capire da sé gli Application Identifier GS1 né il Digital Link.
**GAIA Desktop analizza entrambi**: prende la scansione così come il lettore l'ha letta — con
AIM Code ID e separatori FNC1 — e la divide in AI, oppure converte da sé un URI Digital Link nei
suoi AI.

### Testato

| Lettore | Tipo | Stato |
|---|---|---|
| **DS8108** | Imager 2D portatile con cavo | **Sviluppato e testato** con GAIA Desktop |

### Dovrebbero funzionare

Sono imager 2D Zebra che, come il DS8108, vengono gestiti tramite lo Scanner SDK e i servizi
JavaPOS di Zebra. **Non sono stati testati** con GAIA Desktop; configurate ciascuno come in questa
guida, con la sua Quick Reference Guide o Product Reference Guide per i codici a barre di
configurazione.

| Famiglia | Modelli (esempi) |
|---|---|
| DS81x8 | DS8108 (con cavo), DS8178 (senza fili) |
| DS22x8 | DS2208 (con cavo), DS2278 (senza fili) — uso generico |
| DS36x8 | DS3608 (con cavo), DS3678 (senza fili) — rinforzati, incluse le varianti KD e XR |
| DS46x8 | DS4608 (con cavo), DS4678 (senza fili) |
| DS9308 / DS9908 | Lettori da presentazione e a mani libere |
| MP7000 / MP7200 | Lettori bioptici multipiano e lettori-bilancia |
| CS6080 | Lettore companion senza fili |

Un lettore senza fili comunica con la sua base, ed è la base a collegarsi al computer: collegate la
base via USB e trattate la coppia come un unico lettore. Anche lettori di altre famiglie Zebra
possono funzionare, se lo Scanner SDK di Zebra li supporta in una delle modalità USB del
[passaggio 6](#6-impostare-la-modalità-usb-del-lettore-zebra).

### Funzioni GS1 integrate nei lettori Zebra

A parte la lettura dei codici a barre, alcuni imager 2D Zebra possono elaborare dati GS1 nel lettore
stesso, configurati tramite 123Scan:

| Funzione | Cosa fa | Per GAIA Desktop |
|---|---|---|
| **Label Parse+** | Analizza gli AI di un'etichetta GS1 nel lettore e invia campi riformattati | **Lasciare disattivata** — GAIA Desktop non vedrebbe più gli AI, i separatori FNC1 né l'AIM Code ID |
| **GS1 Digital Link** | Per prodotti al dettaglio marcati sia con un EAN/UPC 1D sia con un codice Digital Link 2D: sceglie quale inviare | **Lasciare disattivata** (predefinito) |
| **GS1 Retail POS parsing** | Legge codici a barre 2D al dettaglio in sintassi URI Digital Link o stringa di elementi | **Lasciare disattivata** |

L'impostazione **GS1 Digital Link** ha tre modalità. **Mode 1 — GTIN only**, la modalità da cui
parte una volta attivata, invia solo il GTIN, da qualunque codice a barre provenga: un lotto, una
data di scadenza o un numero di serie del Digital Link non raggiungerebbero mai GAIA Desktop.
**Mode 2 — Prioritize 2D** invia il codice 2D se viene visto entro il tempo di priorità (200 ms per
impostazione predefinita), altrimenti l'1D. **Mode 3 — Corroborate 1D and 2D GTINs Match** invia
l'1D, e anche il 2D quando il suo GTIN corrisponde. Se l'impostazione deve restare attiva per un
altro software, usate il Mode 2, non il Mode 1. Sul DS8108 sono i parametri 2373 (attivo/disattivo),
2374 (modalità) e 2491 (timeout).

Queste funzioni richiedono **123Scan versione 6 o successiva** e un firmware che le includa. Zebra
aggiunge il supporto per GS1 Digital Link tramite aggiornamenti del firmware da aprile 2025; le date
seguenti provengono da un rivenditore Zebra, quindi verificate le note di rilascio del firmware di
ciascun modello:

| Famiglia di modelli | Firmware con GS1 Digital Link pubblicato |
|---|---|
| DS81x8 | 16 aprile 2025 |
| MP7000 | 21 aprile 2025 |
| DS9908 | 6 giugno 2025 |
| SP72 / SM72 | 11 luglio 2025 |
| DS46x8 | 28 luglio 2025 |
| MP7200 (Rev-C), DS5502 | 30 luglio 2025 |
| DS9308 | 29 agosto 2025 |
| DS22x8, DS1000 | 5 settembre 2025 (solo Asia-Pacifico) |
| DS36x8 (inclusi KD e XR), MS47 | 30 settembre 2025 |
| PL5000, SE4107 | 15 ottobre 2025 |
| CS6080 | 31 dicembre 2025 |

Nessuna di queste funzioni è necessaria per leggere codici GS1 Digital Link in GAIA Desktop: con
le funzioni disattivate, un lettore 2D Zebra con il firmware attuale legge un codice QR Digital Link
come qualsiasi altro, e GAIA Desktop converte l'URI nei suoi AI.

Fonti: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (agosto 2025), capitolo 12
«GS1 Digital Link Parameters» e capitolo Data Parsing; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (date del firmware).
