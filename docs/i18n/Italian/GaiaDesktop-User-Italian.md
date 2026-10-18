# GAIA Desktop — Guida dell'utente

Come usare l'applicazione. Per compilarla, modificarla o aggiungere una lingua, vedere la
[guida dello sviluppatore](../../GaiaDesktop-Developer.md) (in inglese). Documento originale:
[GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Indice

1. [Che cos'è](#che-cosè)
2. [Avviarla](#avviarla)
3. [La finestra](#la-finestra)
4. [Modalità Analizza](#modalità-analizza)
   - [La barra di input e il token {GS}](#la-barra-di-input-e-il-token-gs)
   - [Lettura con lettore](#lettura-con-lettore)
   - [Aprire un file](#aprire-un-file)
   - [La cronologia delle acquisizioni](#la-cronologia-delle-acquisizioni)
5. [Modalità Comporre](#modalità-comporre)
   - [L'editor di AI](#leditor-di-ai)
   - [Che cosa rifiuta l'editor](#che-cosa-rifiuta-leditor)
   - [Stringa di elementi o Digital Link](#stringa-di-elementi-o-digital-link)
   - [Modificare un'acquisizione esistente](#modificare-unacquisizione-esistente)
6. [Modalità Combina](#modalità-combina)
   - [La fascia Combina](#la-fascia-combina)
   - [Che cosa rifiuta Combina](#che-cosa-rifiuta-combina)
   - [Duplicati e conflitti](#duplicati-e-conflitti)
   - [Il rapporto di combinazione](#il-rapporto-di-combinazione)
7. [Il rapporto](#il-rapporto)
   - [Esportare](#esportare)
8. [Opzioni di analisi](#opzioni-di-analisi)
   - [Primo avvio](#primo-avvio)
9. [Lingue](#lingue)
10. [Che cosa viene ricordato](#che-cosa-viene-ricordato)
11. [Scorciatoie da tastiera](#scorciatoie-da-tastiera)
12. [Risoluzione dei problemi](#risoluzione-dei-problemi)
13. [Limiti](#limiti)

---

## Che cos'è

GAIA Desktop fa tre cose con i dati GS1:

- **Analizza** — digitate, incollate, aprite o scansionate una stringa di elementi GS1 o un URI
  Digital Link e leggete il risultato come rapporto completo: che cosa significa ogni
  identificatore di applicazione (AI), che cosa non va e perché;
- **Comporre** — assemblare coppie AI / valore in una stringa di elementi o in un URI
  Digital Link;
- **Combina** — riunire fino a tre codici a barre GS1-128 della stessa etichetta
  nell'unica stringa di elementi che porterebbe un GS1 DataMatrix, verificarla nel suo insieme
  e disegnare il simbolo.

Tutto ciò che l'applicazione sa del GS1 — ogni definizione di AI, ogni regola di convalida, ogni
messaggio di errore e ogni descrizione localizzata — proviene dal motore GAIA sottostante.
L'applicazione non aggiunge regole proprie: accetta esattamente ciò che accetta il motore.

---

## Avviarla

```bash
java -jar gaia-desktop.jar
```

Il file JAR ha bisogno della cartella `lib/` distribuita accanto a esso: teneteli insieme,
altrimenti l'applicazione non si avvia. (È possibile anche compilare un JAR autonomo; vedere la
[guida dello sviluppatore](../../GaiaDesktop-Developer.md#packaging).)

Deve essere installato **Java 21 o successivo**. La distribuzione è specifica per un sistema
operativo e un'architettura di processore: usate il JAR compilato per la vostra macchina.

Per aprire l'applicazione in una lingua specifica, senza cambiare la lingua salvata:

```bash
java -jar gaia-desktop.jar --language=FR
```

Il valore può essere il codice del catalogo (`FR`, `PCM`, `ARZ`), il nome completo (`FRENCH`,
`WU-CHINESE`) o un tag di lingua (`fr`, `zh-HK` per il cantonese, `ar-EG` per l'arabo egiziano);
maiuscole e minuscole non contano. L'impostazione vale solo per quell'esecuzione: la lingua
salvata resta intatta, quindi un collegamento creato per un collega non può cambiare la lingua
in cui si apre la vostra copia. Scegliere una lingua *dentro* l'applicazione è invece una scelta
vera e viene salvata come sempre. Un valore che non indica alcuna lingua viene ignorato con una
nota nella console e l'applicazione si apre nella lingua salvata anziché rifiutarsi di partire.

All'avvio la console stampa una riga con la versione dell'applicazione, la data di compilazione
e la versione del motore. È la riga da citare in una segnalazione di errore.

---

## La finestra

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

> Lo schema mantiene le etichette in inglese, come tutti i blocchi di codice di questa
> documentazione. Sullo schermo vedrete *Analizza* / *Comporre* / *Combina*, *Acquisizioni*,
> *Salva rapporto*, *Opzioni*, *lettore: spento* e *Informazioni*.

**Su macOS e Linux la riga del lettore non esiste**: nessun interruttore né selettore di
dispositivo nella barra degli strumenti, nessun menu *Lettore*, nessun *lettore: spento* nella
barra di stato. Vedere [Lettura con lettore](#lettura-con-lettore).

Il selettore **Analizza ⇄ Comporre** cambia solo la fascia sotto la barra degli strumenti e i
comandi di acquisizione. La cronologia, il rapporto, il pannello delle opzioni e le azioni di
esportazione servono entrambe le modalità, perché una composizione riuscita diventa
semplicemente un'altra acquisizione. La modalità **Combina** è disposta diversamente: vedere [Modalità Combina](#modalità-combina).

Il pannello delle opzioni è **chiuso al primo avvio**. La modalità, la visibilità del pannello,
il tema e l'ultimo lettore usato vengono ricordati da un'esecuzione all'altra.

**Nuove versioni.** Pochi secondi dopo l'avvio, al massimo una volta al giorno, l'applicazione
chiede a GitHub se è stata pubblicata una versione più recente di GAIA Desktop. Se sì, nella
barra di stato compare un collegamento — *È disponibile GAIA Desktop 1.1.0* — che apre la pagina
della versione nel browser; non viene scaricato né installato nulla al posto vostro.
*Salta questa versione* nasconde l'avviso fino a una versione successiva. Il controllo resta
silenzioso quando non c'è nulla di nuovo o quando GitHub non è raggiungibile.
*Aiuto → Verifica aggiornamenti…* controlla subito e riferisce l'esito;
*Aiuto → Verifica automaticamente gli aggiornamenti* disattiva e riattiva il controllo quotidiano.

---

## Modalità Analizza

### La barra di input e il token {GS}

La barra accetta una stringa di elementi, un URI GS1 Digital Link o una scansione grezza con il
suo prefisso AIM Code ID (`]d2…`). <kbd>Invio</kbd> avvia l'analisi.

Gli AI di lunghezza variabile sono separati da **FNC1**, il carattere di controllo invisibile
`0x1D`. Un campo di testo non può contenerlo: verrebbe eliminato in silenzio e
`01…` + FNC1 + `10LOT` arriverebbe come `01…10LOT`, una stringa di elementi che significa
un'altra cosa.

Perciò l'applicazione mostra il separatore come token visibile **`{GS}`** e lo riconverte lungo
il percorso verso l'analizzatore:

- il **pulsante `{GS}`** inserisce il token dove si trova il cursore;
- **incollare** veri caratteri separatori — da tastiera, dal menu contestuale o trascinandoli —
  li trasforma automaticamente in `{GS}`, quindi non si perde nulla;
- il token è scritto esattamente come lo scrive il rapporto, così un valore copiato da un
  rapporto può essere incollato di nuovo così com'è.

L'acquisizione, il rapporto e la cronologia salvata contengono tutti il separatore **vero**. Il
token è soltanto ciò che vedete e digitate.

### Lettura con lettore

> **Solo Windows.** Zebra distribuisce i driver del lettore soltanto per Windows: su macOS e
> Linux l'applicazione non costruisce alcun comando di lettura — nessun interruttore *Connetti
> lettore*, nessun selettore di dispositivo, nessun menu *Lettore* e nessuno stato del lettore
> nella barra di stato. Nulla è disattivato in grigio, perché non esiste impostazione o
> dispositivo che possa abilitarlo. Tutto il resto, compresa l'apertura di un file di scansioni
> salvate, funziona ovunque.

*Connetti lettore* apre il dispositivo indicato nel selettore; `ZebraAllScanners` è il valore
predefinito e va bene per la maggior parte dei modelli. Ogni scansione viene analizzata e
aggiunta automaticamente alla cronologia.

I dati del lettore sono letti byte per byte, così il prefisso AIM Code ID e ogni separatore FNC1
arrivano all'analizzatore esattamente come il lettore li ha inviati.

In modalità Comporre i comandi del lettore sono nascosti, ma un lettore connesso continua a
funzionare: *Lettore → Connetti / Disconnetti* (<kbd>⌘K</kbd>) lo commuta ancora, la barra di
stato lo segnala ancora e una scansione che arriva con l'editor aperto viene comunque acquisita. In modalità Combina i comandi restano, e ogni scansione viene aggiunta alla
combinazione invece che alla cronologia.

La prima configurazione di un lettore — lo Zebra Scanner SDK, la modalità USB del lettore e le
impostazioni GS1 — è descritta passo per passo nella
[guida alla configurazione del lettore Zebra](GaiaDesktop-Zebra-Scanner-Windows-Italian.md), inclusa in ogni zip sotto `docs/`. Se un dispositivo non si connette, vedere
[Risoluzione dei problemi](#risoluzione-dei-problemi).

### Aprire un file

*File → Apri file di testo…* analizza **ogni riga non vuota come acquisizione a sé**: è il modo
rapido di lavorare su un lotto di scansioni che qualcuno vi ha inviato. Il file è letto byte per
byte, quindi i separatori grezzi al suo interno sopravvivono.

### La cronologia delle acquisizioni

Le più recenti per prime. Ogni riga mostra un punto valido / non valido, un riepilogo su una
riga e poi l'ora di acquisizione, la provenienza e quanti AI contiene. La data compare solo
sulle acquisizioni di un giorno precedente; le righe di oggi mostrano solo l'ora.

Il riepilogo è l'**URI Digital Link** per un Digital Link e l'**HRI** negli altri casi: per una
stringa di elementi, `(01)09506000134352 (17)261231` si legge meglio di
`01095060001343521726123110…`.

Fate clic con il tasto destro su un'acquisizione per copiarne l'input o l'HRI, rimandarla alla
barra di input, aprirla nell'editor, rimuoverla o cancellare la cronologia.

---

## Modalità Comporre

### L'editor di AI

Una tabella di coppie AI / valore. Digitate un codice AI oppure sceglietelo fra **tutti gli AI
che il motore conosce** nell'elenco *Aggiungi un AI…*; le colonne **Titolo**, **Formato** e
**Descrizione** si riempiono man mano che digitate, nella lingua scelta. Il selettore non può
proporre un AI che la composizione rifiuterebbe.

La tabella mantiene sempre **una riga vuota in fondo**: digitate lì e compare la successiva,
così una stringa con più AI non ha mai bisogno di *Aggiungi riga*.

La modifica si comporta come deve comportarsi una tabella: <kbd>Invio</kbd>, <kbd>Tab</kbd>,
<kbd>Maiusc</kbd>+<kbd>Tab</kbd> e un clic altrove **conservano** tutti quanto avete digitato;
<kbd>Esc</kbd> lo scarta. <kbd>Tab</kbd> passa da AI a Valore e poi alla riga successiva,
saltando le colonne non modificabili.

### Che cosa rifiuta l'editor

| | Che cosa succede |
|---|---|
| Un AI che il motore non riconosce | La riga diventa rossa mentre digitate; la composizione è rifiutata con una spiegazione |
| Lo stesso AI due volte, dal selettore | Non viene aggiunto: un messaggio lo nomina e viene selezionata la riga che già lo contiene |
| Lo stesso AI due volte, digitato a mano | Entrambe le righe vengono marcate; la composizione è rifiutata con l'errore `GE-S004` |
| Niente da comporre | Un messaggio; non viene creata alcuna acquisizione |

Un AI può comparire una sola volta in una stringa di elementi: per questo il selettore rifiuta
subito, invece di lasciar tentare una composizione destinata a fallire.

### Stringa di elementi o Digital Link

**Componi stringa di elementi** assembla le righe inserendo il separatore FNC1 ovunque la norma
lo richieda. **Componi Digital Link** produce un URI; espandete *Destinazione Digital Link* per
indirizzarlo al vostro resolver anziché al canonico `id.gs1.org`:

| Campo | Effetto | Vuoto significa |
|---|---|---|
| Dominio | L'host del resolver, per es. `example.com` | `id.gs1.org` |
| Prefisso percorso | Un segmento prima del percorso degli AI, per es. `/products` | nessun prefisso |

In caso di **successo** il risultato si comporta esattamente come una scansione: analizzato,
aggiunto alla cronologia, rappresentato come rapporto e salvato. La composizione convalida
analizzando il proprio output, quindi ciò che restituisce è certamente valido.

In caso di **fallimento** non viene acquisito nulla. Il motivo resta sotto i pulsanti di
composizione, una riga per problema, ciascuna con l'errore, l'AI a cui appartiene e la
spiegazione propria del motore:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> Le virgolette doppie in quel messaggio provengono dal catalogo degli errori del motore, non da
> questa applicazione.

### Modificare un'acquisizione esistente

Clic destro su un'acquisizione → **Comporre** carica i suoi AI nell'editor. Cambiate un valore,
ricomponete e il risultato arriva come nuova acquisizione: l'originale resta intatta.

---

## Modalità Combina

Le etichette meno recenti distribuiscono spesso i loro dati su **più codici a barre GS1-128** —
il GTIN in uno, la data di scadenza e il lotto in un altro — perché un solo codice a barre
lineare non può contenere tutto. Combina prende fino a **tre** di questi codici a barre,
li riunisce nell'unica stringa di elementi che porterebbe un **GS1 DataMatrix** e verifica il
risultato nel suo insieme. Quando è valido, l'applicazione disegna il simbolo DataMatrix.

Nessuno di questi codici a barre deve essere completo da solo. Un numero di lotto senza il suo
GTIN fallisce da solo con `GE-S005`, ma è normale per un codice a barre di un insieme, quindi
Combina lo verifica sulla combinazione.

### La fascia Combina

Passando a **Combina**, la barra di input viene sostituita da una fascia lungo il bordo
inferiore della finestra. La cronologia delle acquisizioni si fa da parte, perché le scansioni
della modalità Combina non sono acquisizioni e non vi entrano mai. L'area del rapporto
mostra invece il rapporto di combinazione:

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

Un codice a barre può entrare in due modi:

- **Scansionandolo.** Su Windows i comandi del lettore restano nella barra degli strumenti in
  modalità Combina, e ogni scansione va direttamente nella combinazione anziché nella
  cronologia.
- **Digitandolo o incollandolo** nel campo *Codice a barre* e premendo
  <kbd>Invio</kbd> o **Aggiungi**. Scrivete FNC1 come **`{GS}`**, come nella barra di
  input. Un codice a barre digitato senza AIM Code ID è considerato GS1-128.

| Pulsante | Effetto |
|---|---|
| **Aggiungi** | Aggiunge il codice a barre del campo |
| **Rimuovi l'ultimo** | Toglie il codice a barre più recente (non disponibile quando non ce ne sono) |
| **Reimposta** | Toglie tutti i codici a barre, svuota il campo e ogni rifiuto, e ricomincia |

La barra di stato indica sempre quanti codici a barre sono presenti (*2 di 3 codici a barre GS1-128*),
oppure perché l'ultimo è stato rifiutato.

### Che cosa rifiuta Combina

Un codice a barre rifiutato non viene aggiunto. Il rapporto e la barra di stato dicono entrambi
perché:

| Codice a barre | Rifiutato perché |
|---|---|
| Un campo vuoto | Non c'è nulla da aggiungere |
| Un quarto codice a barre | Un'etichetta si combina al massimo da tre. Togliete l'ultimo, oppure reimpostate |
| Un'altra simbologia, secondo il suo AIM Code ID | Si combina solo il GS1-128, quindi un GS1 DataMatrix (`]d2`) o un QR Code viene rifiutato, con il suo nome |
| AI che non si possono leggere | Un errore di sintassi; vengono mostrati il codice di errore e il messaggio del motore |
| Nessun AI | Non c'è nulla da combinare |
| Uno già scansionato | La stessa stringa di elementi due volte, per esempio lo stesso codice a barre scansionato di nuovo; il rapporto indica quale codice a barre precedente ripete |

Combina rifiuta solo ciò che non può usare affatto. Tutto il resto (una cifra di
controllo errata, una data sbagliata, un AI compagno mancante) viene accettato e giudicato sulla
combinazione.

### Duplicati e conflitti

Gli AI vengono combinati nell'ordine in cui sono stati scansionati. Quando due codici a barre
portano lo stesso AI:

- **con lo stesso valore**, viene mantenuto una volta ed elencato sotto *Duplicati — mantenuti una volta*,
  con i codici a barre in cui è stato letto;
- **con valori diversi**, è un **conflitto**. Il verdetto riporta *CONFLITTO*, i
  valori in disaccordo sono elencati codice a barre per codice a barre, e **non si combina
  nulla** finché non viene risolto. Togliete il codice a barre responsabile, oppure reimpostate.

### Il rapporto di combinazione

Dall'alto in basso:

1. **Il verdetto**: *nessuna scansione*, *CONFLITTO*, valido o non valido, e quanti
   codici a barre sono presenti.
2. **Le scansioni**: ogni codice a barre così come è arrivato, la sua simbologia e la stringa di
   elementi che resta tolto l'AIM Code ID. Un problema che solo la combinazione può risolvere è
   mostrato sul codice a barre, contrassegnato *giudicato sulla combinazione*.
3. **Duplicati** e **conflitti**, se ce ne sono.
4. **Combinazione**: la stringa di elementi, il suo HRI e la sua lunghezza, poi l'input
   combinato come nastro etichettato e una scheda per ogni AI, entrambi presentati come nel
   [rapporto di analisi](#il-rapporto). Ogni errore e ogni avviso sulla combinazione è poi elencato
   sotto *Convalida*.
5. **Il simbolo GS1 DataMatrix**, con il suo HRI sotto. Viene disegnato solo quando la
   combinazione è valida. Altrimenti il rapporto spiega perché non è stato disegnato, così che un
   simbolo non valido non possa essere stampato per errore.

Per esempio, un'etichetta con due codici a barre:

| Codice a barre | Contiene | Da solo |
|---|---|---|
| `]C10109506000134352` | (01) GTIN | valido |
| `]C11726123110LOT-ABC` | (17) data di scadenza, (10) lotto | `GE-S005` per entrambi gli AI, giudicato sulla combinazione |

Si combinano in `01095060001343521726123110LOT-ABC`, HRI
`(01)09506000134352 (17)261231 (10)LOT-ABC`, che è valido, quindi il simbolo viene disegnato.
L'AI `17` ha lunghezza fissa, quindi dopo di esso non serve alcun FNC1. Combina ne
inserisce uno ovunque lo standard lo richieda.

**Salva rapporto** esporta il rapporto di combinazione in HTML o PDF, come un rapporto di
analisi (vedere [Esportare](#esportare)), non appena è presente un codice a barre. Il nome del
file HTML inizia con `gaia-combine-`.

Il rapporto segue la lingua e il tema scelti. Cambiare un'opzione di analisi o la lingua
riverifica i codici a barre già presenti. Sopravvivono anche a un passaggio a Analizza o
Comporre e ritorno, ma non a un riavvio: i codici a barre della modalità Combina
non vengono salvati.

---

## Il rapporto

L'acquisizione selezionata è rappresentata come rapporto completo: un banner di verdetto, la
stringa di elementi scomposta in un «nastro» etichettato, i metadati dell'analisi, una scheda
per ogni AI con le sue interpretazioni, e gli errori e gli avvisi con indicazioni per
correggerli.

Per un **Digital Link**, ogni scheda AI porta anche il proprio ruolo strutturale — **chiave
primaria**, **qualificatore di chiave** o **attributo di dati** — con un codice colore
nell'ordine in cui compaiono nell'URI. I ruoli sono mostrati solo per un Digital Link: in una
semplice stringa di elementi un AI non ha alcuna posizione nell'URI da descrivere.

Il rapporto segue la lingua e il tema che avete scelto.

### Esportare

| | |
|---|---|
| **Salva rapporto → Come HTML…** (<kbd>⌘S</kbd>) | Scrive esattamente ciò che è a schermo: un unico file autonomo che si apre in qualsiasi browser |
| **Salva rapporto → Come PDF…** (<kbd>⌘P</kbd>) | Stampa il rapporto attraverso la finestra di stampa del sistema — scegliete lì una destinazione PDF |

Il PDF passa dalla finestra di stampa perché è lì che si scelgono destinazione e nome del file:
**PDF ▾ → Salva come PDF** su macOS, **Microsoft Print to PDF** su Windows, **Stampa su file**
su Linux. Le pagine sono impaginate e il testo resta selezionabile.

La stampa e il PDF usano sempre la palette chiara, anche quando l'applicazione è in tema scuro,
e conservano intatti i riquadri colorati degli AI e il banner di verdetto.

Se la vostra macchina non ha alcuna stampante né destinazione PDF configurata, l'applicazione lo
segnala e rimanda all'esportazione HTML, che si stampa da qualunque browser.

---

## Opzioni di analisi

Il pannello a destra raccoglie tutte le opzioni del motore. Cambiarne una qualsiasi
**rianalizza subito l'acquisizione selezionata**: il pannello serve quindi anche a chiedere
«come apparirebbe questo input con queste impostazioni?», e non solo a influire sulla scansione
successiva.

| Opzione | Valori |
|---|---|
| Modalità di analisi | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — fin dove spingere l'analisi |
| Lingua | 35 cataloghi di messaggi |
| Ordine | `LITTLE` (gg/mm/aaaa), `MIDDLE` (mm/gg/aaaa), `BIG` (aaaa/mm/gg) |
| Separatore | `SLASH`, `HYPHEN`, `PERIOD` |
| Mese | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Anno | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Ignora il controllo «requires» | Smette di rifiutare un AI a cui manca il compagno obbligatorio (`GE-S005`) |
| Ignora il controllo «excludes» | Smette di rifiutare gli abbinamenti di AI che la norma vieta (`GE-S006`) |

*Ripristina valori predefiniti*, in fondo al pannello, riporta ai valori propri del motore.

### Primo avvio

Quando non c'è ancora nulla di salvato, l'applicazione prende le impostazioni dalle
**impostazioni internazionali della vostra macchina** anziché dai valori predefiniti del motore:

- la **lingua** viene dalle impostazioni locali del sistema, con ripiego sull'inglese quando non
  esiste un catalogo;
- le **opzioni della data** sono ricavate dal formato di data breve delle vostre impostazioni
  locali: l'ordine dei componenti, il separatore e se mese e anno sono abbreviati.

> **Nota.** Un'impostazione locale che scrive le date brevi con l'anno a due cifre parte quindi
> anche qui con due cifre: coincide con il resto del sistema, ma differisce dal valore
> predefinito del motore, che è a quattro. Cambiatelo nel pannello e la scelta resta.

---

## Lingue

Scegliere una lingua cambia **l'intera applicazione** — menu, pulsanti, editor, pannello delle
opzioni, riga di stato e rapporto —, non solo i messaggi di errore. Le lingue che si scrivono da
destra a sinistra ribaltano la finestra e il rapporto.

Sono disponibili tutte e 35 le lingue, con due livelli di copertura:

| | Copertura |
|---|---|
| Rapporti di analisi e messaggi di errore del motore | **tutte e 35 le lingue** |
| Menu, pulsanti ed etichette dell'applicazione | **tutte e 35 le lingue** |

Le lingue da destra a sinistra — arabo, arabo egiziano, persiano e urdu — ribaltano la finestra e
il rapporto fin dal momento in cui l'applicazione si apre.

La lingua viene ricordata da un'esecuzione all'altra. Per aprire in un'altra lingua una volta
sola, vedere [`--language`](#avviarla).

Alcune cose restano volutamente non tradotte in tutte le lingue: i nomi di campo abbreviati
stampati accanto ai valori grezzi (`format`, `position`, `raw`, `len` …), che sono
identificatori e non parole; i nomi degli esempi e delle simbologie, che sono termini GS1 e di
settore; e un lungo paragrafo normativo, dove una traduzione parziale sarebbe fuorviante.

---

## Che cosa viene ricordato

Due file in `.gaia-desktop`, dentro la vostra cartella home:

| File | Contiene |
|---|---|
| `options.properties` | Tutte le opzioni di analisi, il tema, se il pannello delle opzioni è aperto, l'ultimo lettore usato, la modalità in cui è stata lasciata la finestra, e le impostazioni del controllo degli aggiornamenti — attivo o no, quando è stato eseguito l'ultima volta e ogni versione che avete scelto di saltare |
| `captures.tsv` | La cronologia delle acquisizioni, le più recenti per prime, con un limite di 500 voci |

Le opzioni sono scritte ogni volta che ne cambiate una; la cronologia ogni volta che cambia.
Entrambi vengono riscritti all'uscita.

Eliminare uno dei due file ripristina quella parte e nient'altro: sono tenuti separati perché
una cronologia danneggiata non vi costi le impostazioni. Nemmeno un file rovinato modificandolo
a mano è fatale: ogni impostazione ricade singolarmente sul proprio valore predefinito, quindi
una riga sbagliata costa solo quella riga.

La cronologia salva l'input, non l'analisi. Viene rianalizzato al caricamento: un'acquisizione
ripristinata riflette quindi le opzioni in vigore **adesso**, non quelle in vigore al momento
dell'acquisizione.

I codici a barre presenti in modalità Combina non vengono salvati: dopo un riavvio,
Combina riparte vuota.

---

## Scorciatoie da tastiera

| | |
|---|---|
| <kbd>Invio</kbd> | Analizzare la barra di input |
| <kbd>⌘O</kbd> | Aprire un file di testo |
| <kbd>⌘S</kbd> | Salvare il rapporto come HTML |
| <kbd>⌘P</kbd> | Salvare il rapporto come PDF (finestra di stampa del sistema) |
| <kbd>⌘G</kbd> | Inserire il token separatore FNC1 |
| <kbd>⌘K</kbd> | Connettere / disconnettere il lettore (solo Windows) |
| <kbd>⌘D</kbd> | Attivare o disattivare il tema scuro |
| <kbd>⌘Q</kbd> | Uscire |

Nell'editor: <kbd>Tab</kbd> / <kbd>Maiusc</kbd>+<kbd>Tab</kbd> spostano fra le celle
modificabili, <kbd>Invio</kbd> conferma, <kbd>Esc</kbd> annulla.

In modalità Combina: <kbd>Invio</kbd> nel campo *Codice a barre* aggiunge il
codice a barre. Lì scrivete FNC1 come `{GS}`; <kbd>⌘G</kbd> appartiene alla barra di input della
modalità Analizza.

---

## Risoluzione dei problemi

| Sintomo | Causa e rimedio |
|---|---|
| L'applicazione non si avvia, o segnala `NoClassDefFoundError` | La cartella `lib/` deve trovarsi accanto al JAR. Teneteli insieme, oppure usate una distribuzione autonoma |
| *«Unsupported JavaFX configuration…»* nella console all'avvio | Atteso e innocuo: il toolkit segnala soltanto come è stato caricato. L'applicazione funziona normalmente |
| Un separatore incollato sembra sparire | È il comportamento previsto: viene riscritto come token visibile `{GS}` e l'analizzatore riceve comunque il carattere vero. Vedere [il token {GS}](#la-barra-di-input-e-il-token-gs) |
| Non c'è il pulsante *Connetti lettore* | Siete su macOS o Linux, dove i driver del lettore non esistono. Vedere [Lettura con lettore](#lettura-con-lettore) |
| Il lettore non si connette | Verificate che il nome logico nel selettore corrisponda al vostro modello, che il lettore sia collegato e che nessun'altra applicazione lo stia occupando. La [guida alla configurazione del lettore Zebra](GaiaDesktop-Zebra-Scanner-Windows-Italian.md#11-risoluzione-dei-problemi) elenca ogni codice di errore. |
| L'esportazione in PDF dice che non c'è destinazione | La vostra macchina non ha stampanti né destinazioni PDF configurate. Aggiungetene una, oppure esportate in HTML e stampate da un browser |
| Una voce di menu mostra qualcosa come `act.hri` | Manca un'etichetta: segnalatelo, è un difetto di compilazione, non un'impostazione |

---

## Limiti

- **La lettura è solo per Windows**, perché lo sono i driver.
- **Il PDF passa dalla finestra di stampa** anziché da una di salvataggio.
- **Le destinazioni Digital Link accettano un dominio e un prefisso percorso**, non parametri di
  query aggiuntivi né un frammento.
- **Un AI può comparire una sola volta** in una stringa di elementi: è la regola GS1, non un
  limite dell'applicazione.
- **Combina accetta solo codici a barre GS1-128, al massimo tre**, e non li conserva
  tra un avvio e l'altro.
