# GAIA Desktop — Guide du développeur

Comment le module est construit, organisé et testé. Pour ce que fait l'application vue de l'extérieur,
voir le [Guide de l'utilisateur](GaiaDesktop-User-French.md). Version originale :
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Table des matières

1. [Le module en bref](#le-module-en-bref)
2. [Compiler et exécuter](#compiler-et-exécuter)
   - [Empaquetage](#empaquetage)
3. [Architecture](#architecture)
   - [L'entonnoir de capture](#lentonnoir-de-capture)
   - [Fils d'exécution](#fils-dexécution)
   - [Reconstruire ou réétiqueter](#reconstruire-ou-réétiqueter)
4. [Le jeton {GS}](#le-jeton-gs)
5. [Intégration du lecteur](#intégration-du-lecteur)
   - [Le verrou de plateforme](#le-verrou-de-plateforme)
   - [Symbologies](#symbologies)
6. [Localisation](#localisation)
   - [L'argument `--language`](#largument---language)
   - [Ajouter une langue](#ajouter-une-langue)
   - [Ce qui n'est volontairement pas traduit](#ce-qui-nest-volontairement-pas-traduit)
7. [Formats de l'état enregistré](#formats-de-létat-enregistré)
8. [Tests](#tests)
9. [Dépannage de la compilation](#dépannage-de-la-compilation)
10. [Limites connues et travail en cours](#limites-connues-et-travail-en-cours)

---

## Le module en bref

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Un client de la bibliothèque centrale, et rien de plus. Il n'embarque **aucune connaissance GS1** : chaque
définition d'AI, règle de validation, message d'erreur et description localisée vient de
[`GaiaParser`](../../../../gaia/docs/GaiaParser.md) et de
[`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). En particulier, la liste des AI n'est jamais copiée
dans ce module — le sélecteur du compositeur lit le registre propre au cœur, il ne peut donc pas proposer
un AI que le compositeur refuserait.

**Points d'entrée :** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (l'`Application` JavaFX) et
`tools.pantheum.gaia.desktop.Launcher` (la `Main-Class` du jar empaqueté).

Le module est une feuille : rien n'en dépend, et il ne dépend d'aucun autre module en aval.

---

## Compiler et exécuter

- **Java 21** pour exécuter, et un JDK 21 ou plus récent pour compiler — le module compile avec
  `release=21`. JavaFX 21 est la ligne LTS correspondante. Le jar du cœur est encore en bytecode Java 11
  et tourne tel quel sur 21.
- **Maven** dans le `PATH`. Il n'y a pas de wrapper dans ce dépôt.
- **Le cœur installé d'abord.** Ce module résout `gaia:1.1.0` depuis `~/.m2` ; un jar du cœur périmé ou
  absent est la cause habituelle de `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` est le seul argument de ligne de commande, et le seul moyen d'influer sur le démarrage
hormis `-Dgaia.desktop.home=…`, qui place tout le profil ailleurs.

`mvn javafx:run` est la voie de développement : le plugin met JavaFX sur le **module path**, si bien que
la boîte à outils démarre en tant que module nommé et que `GaiaDesktopApp` peut être lancée directement.
Un jar empaqueté ne le peut pas — tout est sur le class path — d'où sa `Main-Class`, la cale `Launcher`.
Une sous-classe d'`Application` comme `Main-Class` dans cette disposition échoue avec *"JavaFX runtime
components are missing"*.

Utilisez toujours `clean` — une fois, avant de compiler une série de plateformes, puisqu'il vide
`target/dist/` de toutes. Voir [Dépannage de la compilation](#dépannage-de-la-compilation) pour la raison.

### Empaquetage

Tout ce qui est livrable est assemblé sous `target/dist/gaia-desktop-<version>-<platform>/`, et rien
d'autre ne l'est : le répertoire peut ainsi être zippé ou copié d'un bloc sans trier les artefacts dans
`target/` à côté de `classes/`, `maven-status/` et du reste des fichiers de travail de la compilation.
`${dist.dir}` le nomme une seule fois ; chaque plugin qui écrit un artefact pointe dessus.

#### Une compilation par plateforme

Les jars JavaFX contiennent du code natif, chaque distribution vise donc une seule plateforme, désignée
par la propriété `fx.platform` sous forme de classificateur OpenJFX : `mac-aarch64`, `mac`, `win`, `linux`
ou `linux-aarch64`. Cinq profils `fx-*` activés selon l'OS la règlent sur celle de la machine qui compile,
ce qui suffit à un simple `mvn package` ou `mvn javafx:run` ; `-Dfx.platform=…` prime sur eux pour
compiler n'importe quelle autre, depuis n'importe quelle machine — les jars JavaFX ne sont que des
téléchargements. `${dist.dir}` vaut `target/dist/${fx.platform}`, si bien que la compilation d'une
plateforme n'écrase jamais celle d'une autre.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Deux détails du pom rendent cela possible, et tous deux semblent superflus :

- **Chaque module JavaFX est une dépendance directe** — base, graphics, controls, media et web —, chacun
  avec `<classifier>${fx.platform}</classifier>` et une exclusion `org.openjfx:*`. Les poms d'OpenJFX
  choisissent leurs jars transitifs selon l'OS de la *machine qui compile* ; laisser graphics ou base
  arriver par transitivité place les natifs Mac dans une compilation Windows, à côté de ceux de Windows.
- **La propriété est `fx.platform`, pas `javafx.platform`.** Définie de l'extérieur, la propriété propre
  à OpenJFX est redéfinie en fonction d'elle-même par son pom parent ; Maven rejette ce pom comme invalide
  et abandonne en silence les jars de plateforme, ce qui laisse une compilation sans JavaFX.

Les deux dispositions écrivent le même chemin, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`,
et une seule s'exécute par compilation. `${jar.phase}`, `assembly.skipAssembly` et `mdep.skip` les
empêchent de le revendiquer toutes les deux.

| | Par défaut | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — uniquement les classes et ressources de ce module | ~50 MB — dépendances décompressées dedans |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 jars, nommés un par un dans le `Class-Path` du manifeste | — |
| À livrer | le jar **et** `lib/` à côté | un seul fichier |
| Aussi dans `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) ou `run.sh` (autres) | idem |
| Et | `readme.html` et `docs/` (toutes plateformes) | idem |

Chaque distribution embarque aussi les guides : `docs/` copié avec sa structure, pour que les liens entre
guides et traductions restent valides, moins l'outillage de traduction (`check-i18n.py`,
`i18n-manifest.json`, `i18n-plan.md`) ; et `readme.html` à la racine, un index qui pointe vers chacun
d'eux, tiré de `src/main/dist/readme.html` avec `@VERSION@` filtré en version du pom. Les deux sont écrits
par l'étape antrun `pick-launch-script`, qui supprime d'abord `docs/` pour qu'un guide retiré ne traîne
pas. Les liens du readme sont écrits à la main — un guide renommé ou une traduction ajoutée sous `docs/`
y demande aussi sa ligne.

Le `Class-Path` du manifeste et `lib/` sont les deux moitiés d'une même chose : le jar nomme
`lib/<artifact>-<version>.jar` pour chaque dépendance d'exécution, et `copy-dependencies` remplit `lib/`
avec exactement ces noms. C'est ce qui fait fonctionner `java -jar`.

#### L'application macOS et l'archive de distribution

Une exécution antrun, `mac-app-and-zip`, termine la phase `package`. Le plugin antrun est déclaré
**après** les plugins jar et assembly pour cette raison — Maven exécute les exécutions d'une phase dans
l'ordre de déclaration des plugins —, si bien que le jar existe, dans l'un ou l'autre empaquetage,
lorsqu'elle s'exécute. (Son autre exécution, `pick-launch-script`, est dans `prepare-package` et n'est
pas concernée.)

Pour `mac` et `mac-aarch64`, elle construit d'abord `GAIA Desktop.app` dans le répertoire de distribution :
`Info.plist` (avec `@VERSION@` filtré en version du pom), le lanceur `Contents/MacOS/gaia-desktop` (rendu
exécutable) et `gaia-desktop.icns`, tous issus de `src/main/macos/`, puis **déplace** `gaia-desktop.jar`
et `lib/` dans `Contents/Java`, pour que le bundle soit autonome. `run.sh` y regarde quand aucun jar n'est
à côté de lui. Le lanceur trouve Java par version (`java_home -v 21+`, puis l'openjdk de Homebrew — un
lancement depuis le Finder ne voit ni `JAVA_HOME` ni `PATH`), affiche une boîte de dialogue via
`osascript` quand il n'y en a pas, fait un `cd` vers `~/.gaia-desktop` pour que le `logs/` relatif de
logback soit inscriptible, et passe `-Xdock:name`/`-Xdock:icon`. macOS associe le `java` en cours au bundle
(son identifiant de bundle et son icône du Dock sont ceux de l'application) ; la barre de menus affiche
GAIA Desktop.

Ensuite, pour chaque plateforme, elle zippe le répertoire vers
`target/dist/gaia-desktop-<version>-<platform>.zip` sous un dossier du même nom, avec le `<zip>` d'Ant :
`run.sh` et `Contents/MacOS/**` forment un ensemble de fichiers séparé avec `filemode="755"`, sans quoi
ils seraient décompressés non exécutables. Le zip n'est **pas attaché** : il est destiné aux releases
GitHub, et les quatre plateformes viennent de quatre compilations, que Maven Central — un seul envoi
immuable par version — ne pourrait jamais accepter comme une seule release.

`project.build.outputTimestamp` est fixé à l'heure de compilation. Sinon, les archiveurs de
maven-jar-plugin 3.5 et maven-assembly-plugin 3.8 datent chaque entrée du 1er février 1980.

L'icône de l'application est dessinée par `tools/icon/GaiaIcon.java` (hors compilation ; son en-tête
donne les étapes `sips`/`iconutil`). Ses sorties sont versionnées : `src/main/macos/gaia-desktop.icns`
pour le bundle, et `src/main/resources/icons/gaia-desktop-{16..256}.png`, que `GaiaDesktopApp` définit
comme icône de fenêtre (Windows et Linux ; macOS ignore les icônes de fenêtre).

#### Les scripts de lancement

`run.sh` et `run.bat` viennent de `src/main/scripts/`, copiés dans `dist/gaia-desktop-<version>-<platform>/`
sans filtrage — `run.sh` regorge d'expansions shell `${...}` que le filtrage Maven serait libre de
réécrire. Une étape antrun ne garde ensuite que celui que la plateforme exécute — `run.bat` pour `win`,
`run.sh` pour toutes les autres — et pose le bit exécutable sur `run.sh`, puisque `copy-resources` ne le
conserve pas. Elle décide d'après le `${fx.platform}` résolu plutôt que par des profils, car l'activation
d'un profil ne voit pas une valeur qu'un autre profil a fixée : une compilation Linux faite sous Windows
recevrait sinon le script de l'hôte.

Ils **ne s'appuient pas sur le manifeste**. Chacun construit lui-même le class path, `gaia-desktop.jar`
suivi de chaque `.jar` de `lib/`, nommés un par un plutôt que laissés à un joker `lib/*`, afin qu'un
chemin contenant des espaces reste une seule entrée et que le décompte de ce qui a été ajouté soit réel.
Le lancement avec `-cp` explique pourquoi ils nomment `tools.pantheum.gaia.desktop.Launcher`
explicitement : `-cp` ne lit pas la `Main-Class` du manifeste. Il suit **en revanche** encore son
`Class-Path` — un jar sur `-cp` apporte les jars que nomme son manifeste, cherchés juste après lui —, si
bien que chaque jar de `lib/` se charge, qu'un script le liste ou non, et qu'en omettre un du class path
du script ne le tient pas hors de la JVM. Les scripts les listent quand même, pour qu'un jar dont le
manifeste est périmé, modifié à la main ou incomplet démarre tout de même. Le
même script sert les deux empaquetages : le jar `-Pfat` n'a pas de `lib/` à ajouter et n'en a pas
besoin, ce qui explique aussi qu'un `lib/` vide soit une remarque et non une erreur.

#### Les bibliothèques natives Zebra

La compilation ne livre rien de Zebra — ni DLL, ni jar de service. Sous Windows, ils viennent du Scanner
SDK de Zebra installé sur la machine : `run.bat` ajoute ses jars de service `Jpos*.jar` et `bin\` au
class path, et `bin\` au `PATH`, où `System.loadLibrary` trouve `CSJPOSScanner.dll` /
`CSJPOSScanner64.dll` (`java.library.path` vaut par défaut le `PATH`). L'API JavaPOS et son chargeur de
configuration viennent toujours de `lib\javapos-*.jar`, SDK ou pas.

**`run.bat` n'ajoute jamais le propre jar JavaPOS du SDK**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`.
Il l'a fait autrefois, en laissant de côté `lib\javapos-*.jar` pour éviter deux copies de l'API — mais le
`Class-Path` du manifeste les chargeait malgré tout (voir plus haut), et la JVM recevait les deux : les
classes du chargeur 4.x depuis `lib\`, avec à côté `jpos/res/jpos.properties` et `jcl.xsd` de
`javaPOS114.jar`. Le fichier de propriétés fait s'effacer [`JposConfig`](#intégration-du-lecteur), et ce
`jcl.xsd` n'accepte les versions de JavaPOS que jusqu'à 1.10 : chaque entrée de `jpos.xml` (1.12–1.14)
échouait à la validation, le registre restait vide, et chaque connexion échouait avec l'erreur 109 avant
même d'atteindre le lecteur. Avoir le jar sur le class path suffit pour cela, même derrière les jars de
`lib\`. Les jars de service de Zebra fonctionnent sans modification sur l'API 1.14 de `lib\` — ouverture,
réservation et lecture sont vérifiées ainsi sur un vrai lecteur. Des DLL déposées à la main à côté de
`run.bat` sont toujours prises en compte : le script fixe alors `-Djava.library.path=<dist>;%PATH%`, en
ajoutant le `PATH` parce que la propriété remplace purement et simplement le chemin de recherche par défaut.

`mvn javafx:run` ne fait rien de tout cela ; réserver un lecteur depuis une exécution de développement
sous Windows demande donc de mettre par d'autres moyens les jars du SDK sur le class path et son `bin\`
dans le `PATH` ; pour cela, lancez depuis `dist/`.

Au-delà du class path, ils couvrent ce que `java -jar` ne fait pas : résoudre leur propre répertoire pour
fonctionner depuis n'importe quel répertoire de travail, préférer `JAVA_HOME` au `PATH`, et refuser une JVM
antérieure à 11 avec une phrase qui nomme le remède au lieu d'une `UnsupportedClassVersionError`.
`GAIA_DESKTOP_OPTS` transmet des options JVM. `run.bat` utilise `java.exe`, pas `javaw.exe`, pour que la
console reste ouverte — Windows est la seule plateforme où le lecteur fonctionne, et son état et les
erreurs JavaPOS y sont affichés.

`run.bat` choisit aussi son Java, son JavaFX et ses services Zebra d'après ce qu'il trouve — un `jdk*\`
fourni, un `javafx-sdk*\lib\` sur le module path, le Scanner SDK de Zebra à `ZEBRA_HOME` —, en laissant
hors du class path les copies de JavaFX de `lib\` quand il utilise celles d'un SDK, et transmet le parseur
SAX du JDK, les proxys et le magasin de certificats de Windows, ainsi que `--enable-native-access` sur Java
17 et plus. La section du README [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows)
en donne le tableau. Conservez les fins de ligne CRLF du fichier : `cmd.exe` peut manquer un `call :label`
dans un fichier batch en LF seul. `run.sh` passe lui aussi `--enable-native-access=ALL-UNNAMED` — JavaFX
charge ses propres natifs depuis `lib/`, et avec JavaFX sur le class path, `ALL-UNNAMED` est le module qui
le couvre —, omis, comme dans `run.bat`, seulement quand la version de Java ne peut pas être lue.

L'une et l'autre disposition embarquent les natifs JavaFX **d'une seule plateforme**, `${fx.platform}` ;
aucune n'est donc portable d'un OS ou d'une architecture à l'autre. Compilez une fois par cible — voir
[Une compilation par plateforme](#une-compilation-par-plateforme).

> **Eclipse.** L'exécution `copy-dependencies` porte une instruction `<?m2e ignore?>`. m2e résout un
> projet ouvert de l'espace de travail vers son dossier `target/classes` plutôt que vers un jar, et
> `copy-dependencies` refuse de copier un artefact non empaqueté (MDEP-187), ce qui ferait échouer la
> compilation automatique chaque fois que le projet `gaia` est ouvert à côté de celui-ci. En conséquence,
> une compilation faite uniquement dans l'IDE ne remplit pas `target/dist/gaia-desktop-<version>-<platform>/lib/` ;
> lancez `mvn package` avant d'utiliser le jar lui-même.

---

## Architecture

| Paquetage | Contient |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (la fenêtre), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (logique de composition sans JavaFX), `AiPair` (une ligne de l'éditeur) |
| `desktop.parse` | `ParseOptions` (`ParseConfig` observable), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — le moteur de rendu du rapport, sans JavaFX et testé unitairement sans la boîte à outils |
| `desktop.i18n` | `UiStrings` — le catalogue des libellés |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### L'entonnoir de capture

Chaque entrée — tapée, collée, ouverte depuis un fichier, lue au lecteur ou composée — atteint
l'analyseur par une seule méthode, `MainView.capture(String, ScanSource, String)`.

C'est la seule décision de conception autour de laquelle l'application est bâtie. Une composition n'a
besoin ni de son propre rapport, ni de son propre historique, ni de son propre chemin d'export, car après
l'entonnoir une chaîne composée est indiscernable d'une chaîne lue. Tout ce qui produit une nouvelle entrée
doit passer par lui plutôt qu'à côté.

### Fils d'exécution

| Fil | Exécute |
|---|---|
| Fil d'application JavaFX | Toute l'analyse, le rendu et l'interface. Une fois les tables d'AI chaudes, l'analyse prend moins d'une milliseconde ; elle n'a donc besoin d'aucun fil de travail |
| `gaia-scanner` | Chaque appel au lecteur — ouverture, réservation, activation, désactivation, libération, fermeture. Un fil durable appartenant à `ScannerService` ; voir [Intégration du lecteur](#intégration-du-lecteur) |
| Fil d'événements JavaPOS | Les rappels de `ScannerService`. `MainView.FxScannerListener` fait passer chacun d'eux avec `Platform.runLater` |
| Arrière-plan | L'attente de `connect()` / `disconnect()` du lecteur (la réservation peut prendre jusqu'à 5 s), la restauration de l'historique enregistré au démarrage — qui sert aussi de préchauffage de l'analyseur — et la vérification des mises à jour (`gaia-update-check`, une requête GitHub, résultat renvoyé avec `Platform.runLater`) |

**Ne modifiez jamais la sélection d'un contrôle, ni ne détruisez le contrôle, depuis son propre événement
de sélection.** Trois plantages distincts de cette application venaient exactement de là : réinitialiser
le sélecteur d'AI à l'intérieur du propre `clearAndSelect` de la fenêtre contextuelle (une
`IndexOutOfBoundsException` sortie de `ListViewBehavior`), et reconstruire le panneau qui possède la liste
déroulante des langues depuis le propre gestionnaire de cette liste. Les deux sont désormais différés avec
`Platform.runLater`, et les raisons sont consignées aux deux points d'appel.

### Reconstruire ou réétiqueter

Un changement de langue **reconstruit** l'habillage à partir du catalogue — menus, barre d'outils, panneau
d'options —, ce qui est sûr car aucun d'eux ne possède d'état : `OptionsPane` est une vue bidirectionnelle
de `ParseOptions`, les valeurs vivent donc dans le modèle et un panneau neuf les reprend aussitôt.

Les contrôles qui contiennent des **données de l'utilisateur** sont des champs, réétiquetés sur place à la
place : `BuilderPane.retext(UiStrings)` conserve les lignes d'AI, les champs Digital Link et la sélection.
Le reconstruire jetterait le travail en cours — ce qu'il a fait autrefois.

---

## Le jeton {GS}

Un `TextField` JavaFX supprime en silence tout caractère inférieur à `0x20`
(`TextInputControl.filterInput`). Il ne « l'affiche pas bizarrement » — il le retire. La saisie, le collage,
`setText` et `insertText` sont tous concernés, si bien que `setText("01…" + GS + "10LOT")` donne
`"01…10LOT"` : une chaîne d'éléments dont le sens a discrètement changé, car l'AI de longueur variable
avale désormais ce qui le suivait.

`desktop.parse.GsToken` est toute la réponse. Le séparateur traverse l'interface sous forme du texte
littéral `{GS}` et redevient `0x1D` en route vers l'analyseur. Le jeton n'est pas inventé ici — c'est
exactement ce en quoi `ASCIIRevealerUtils.printable` du cœur affiche `0x1D`, si bien qu'une valeur copiée
d'un rapport se recolle telle quelle dans la barre de saisie.

Un filtre `TextFormatter` sur le champ de saisie intercepte toutes les voies par lesquelles du texte peut
arriver — saisie, ⌘V, menu contextuel, glisser-déposer, `setText` —, c'est pourquoi la réécriture se fait
là plutôt que dans chaque gestionnaire.

Le chemin du lecteur ne passe jamais par `GsToken` : les lectures atteignent l'analyseur sous forme
d'octets.

---

## Intégration du lecteur

`ScannerService` possède le contrôle `Scanner` de JavaPOS : ouverture → réservation → activation, en
réarmant l'événement de données après chaque lecture. Les noms logiques de périphérique viennent de
`src/main/resources/jpos.xml`.

**Chaque appel au périphérique s'exécute sur un seul fil, `gaia-scanner`, qui vit aussi longtemps que le
service** — jamais sur le fil FX, et jamais sur un fil qui se termine après l'appel. Le service de Zebra
atteint le lecteur par JNI via l'API COM CoreScanner, et COM lie ce qu'il met en place à l'appartement du
fil appelant : ouvrez sur un fil qui se termine ensuite et le lecteur est réservé mais reste muet ;
fermez sur un autre fil et c'est le mauvais appartement qui est désinitialisé. Fermer sur le fil FX est le
pire cas, car JavaFX y appelle `OleInitialize` et y enregistre la cible de dépôt de chaque fenêtre — un
`CoUninitialize` sur ce fil supprime le glisser-déposer dans toute l'application. C'est JavaFX sous
Windows, et pas une version particulière de JavaFX. `connect()` et `disconnect()` soumettent à ce fil et
attendent ; `MainView` les appelle donc toutes deux depuis un fil d'arrière-plan éphémère ; le modèle est
le `JposScannerSession` du visualiseur, où la panne du glisser-déposer a été découverte.

Deux règles empêchent un périphérique capricieux de bloquer la connexion. **Le démontage ne s'arrête
jamais en route :** `closeQuietly` désactive, libère et ferme en étapes séparées, ne libère que si la
réservation est détenue, et tente toujours `close()` — après une réservation échouée, `release()` lève
« not claimed », et lorsque les étapes partageaient un seul `try`, cela sautait la fermeture et laissait le
périphérique ouvert dans le service de Zebra. **L'événement de données est réarmé dans un `finally` :**
JavaPOS le désactive après chaque événement, donc une lecture ou un gestionnaire qui lève une exception
doit tout de même réarmer, sans quoi le lecteur reste muet jusqu'à une reconnexion.
`ScannerLifecycleTest` exerce les deux règles contre un `jpos.Scanner` scénarisé.

**`JposConfig` oriente JavaPOS vers le `jpos.xml` fourni** avant la première ouverture. Avec les jars
JavaPOS de Maven (`javapos-config-loader` 4.x) et sans `jpos/res/jpos.properties` sur le class path, chaque
ouverture échoue sinon avec *"Service does not exist in loaded JCL registry"* (erreur 109), pour trois
raisons distinctes : le gestionnaire de services se rabat sur `SimpleRegPopulator`, qui lit un `jpos.cfg`
sérialisé et jamais `jpos.xml` ; le chargeur XML ne cherche `jpos.xml` que dans le répertoire de travail,
pas dans le jar ; et Xerces 2.12.2 dans `lib/` rejette la propriété JAXP `accessExternalDTD` que le
chargeur définit. `JposConfig` définit `jpos.config.regPopulatorClass` (`JavaxRegPopulator`),
`jpos.config.populatorFileURL` (le `jar:file:…!/jpos.xml` du jar lui-même) et
`javax.xml.parsers.SAXParserFactory` (celle du JDK) comme propriétés système — jamais une déjà définie,
si bien que `-Djpos.config.populatorFileURL=file:…` sélectionne toujours un autre `jpos.xml` — et ne
définit rien quand un `jpos.properties` est sur le class path ou que le chargeur 4.x n'y est pas. Le
`javaPOS114.jar` du Zebra SDK est un tel cas et casse purement le registre ; c'est pourquoi `run.bat` le
tient hors du class path — voir [Les bibliothèques natives Zebra](#les-bibliothèques-natives-zebra).
`JposConfigTest` vérifie de bout en bout que le registre contient alors les lecteurs du fichier.

**Le sélecteur de périphérique est généré à partir de ce registre**, et non tenu en liste dans le code :
`ScannerService.logicalNames()` lit les entrées chargées sur le fil `gaia-scanner` et garde celles dont la
catégorie de périphérique est `Scanner` — les deux entrées de balance de `jpos.xml` restent dehors —, dans
l'ordre `pickerOrder` : `ZebraAllScanners` d'abord quand le fichier la définit, le reste par ordre
alphabétique (le registre ne garde pas l'ordre du fichier). Un fichier qui ne la définit pas ne la reçoit
pas ; si rien n'est chargé, seule la valeur par défaut est proposée, et le sélecteur n'est donc jamais
vide. `MainView` affiche la valeur par défaut tout de suite et complète la liste depuis un fil
d'arrière-plan, en conservant le choix enregistré. Ainsi, un lecteur ajouté à `jpos.xml` — ou tout un
fichier désigné par `-Djpos.config.populatorFileURL` — apparaît dans le sélecteur sans changement de code,
et `JposConfigTest` confronte le sélecteur à une lecture indépendante du fichier fourni. Les services que
ces entrées nomment sont des classes de Zebra
(`com.zebra.jpos.service.scanner.*Factory`), qui doivent encore être sur le class path — depuis le Scanner
SDK via `run.bat`, ou par les jars de service de Zebra déposés dans `lib\`.

Les données lues sont décodées en **ISO-8859-1** pour que chaque octet corresponde 1:1 et que le préfixe
AIM Code ID comme chaque `0x1D` survivent ; les rapports sont écrits en UTF-8.

Rien dans le service ne compose de texte destiné à l'utilisateur. Les événements sortent sous forme de
constantes `ScannerListener.Event` accompagnées des valeurs qui varient, et `MainView` fournit le libellé
depuis le catalogue — sinon la ligne d'état serait en anglais dans une application traduite de 35 façons.
Les lignes de journal font exception et restent en anglais.

### Le verrou de plateforme

`ScannerService.isSupportedPlatform()` est un seul test sur `os.name`, et `MainView` le consulte avant de
construire le moindre contrôle de lecteur : sous macOS et Linux, il n'y a ni interrupteur, ni sélecteur de
périphérique, ni menu Lecteur (et donc pas de <kbd>⌘K</kbd>), ni libellé d'état. Ils ne sont pas cachés —
ils ne sont jamais construits.

Zebra ne livre ses services JavaPOS que pour Windows, et rien n'échoue tôt ailleurs : les jars se résolvent
et le contrôle se construit, et c'est `open()` qui finit par échouer sur la couche native manquante,
plusieurs secondes et une boîte de dialogue plus tard.

La règle porte sur les *services*, pas sur JavaPOS. Un fournisseur qui livrerait un service
multiplateforme fonctionnerait ici, et élargir la règle ne touche que `supportsJavaPos(String)` — qui est
visible au niveau du paquetage et testée contre des chaînes `os.name`, pour que les deux réponses soient
couvertes sur n'importe quelle machine qui compile.

### Symbologies

`Symbologies` fait correspondre les **78** types de données de lecture JavaPOS `SCAN_SDT_*` à des noms
d'affichage, et au `DataCarrierType` du cœur là où les deux vocabulaires concordent. JavaPOS est le plus
fin des deux — il distingue UPC-A d'EAN-13, et Data Matrix simple de GS1 DataMatrix, là où AIM regroupe
toute une famille sous un seul caractère de code.

Un type absent de la table reçoit tout de même un nom utilisable, trouvé par réflexion sur `ScannerConst`,
car les services des fournisseurs signalent bien des valeurs hors de la liste de la spécification. Un
test parcourt `ScannerConst` et échoue si un type déclaré n'a pas de nom — ce qui attrape aussi les
constantes qu'ajouterait un futur JavaPOS.

Certaines correspondances de support sont volontairement laissées à `UNKNOWN` plutôt que devinées : le 2
parmi 5 standard (AIM sépare `]R`/`]S` selon le nombre de barres de départ/arrêt, que JavaPOS ne signale
pas), les codes postaux à quatre états, et TLC-39.

---

## Localisation

Les libellés vivent dans un fichier de propriétés par langue sous
`src/main/resources/i18n/ui_<CODE>.properties`, avec les mêmes codes que les catalogues d'erreurs du cœur
(`EN`, `FR`, `PCM`, …). Les 35 existent et les 35 portent un jeu de clés identique. Les fichiers sont lus
en **UTF-8**, et non en ISO-8859-1 comme le suppose `Properties`, pour que les traductions soient stockées
lisiblement plutôt qu'en échappements `\uXXXX`.

**Le repli se fait clé par clé, pas fichier par fichier** — c'est ce qui rend une langue partiellement
traduite utile dès que sa première clé arrive. L'anglais est le contrat : chaque clé doit y exister.

| Couche | Clés | Couverture |
|---|---|---|
| Vocabulaire de rapport partagé | 31 | les 35 langues |
| Habillage du bureau — menus, boutons, compositeur, options, ligne d'état, mise en page du rapport, état du lecteur, textes des dialogues | 114 | les 35 langues |

Six des termes partagés (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) ne sont
pas utilisés par le rapport de cette application.
Ils sont conservés délibérément ; l'en-tête du fichier le dit.

### L'argument `--language`

`GaiaDesktopApp.languageOverride()` lit `--language=…` via le `getParameters().getNamed()` propre à
JavaFX, le résout avec `Languages.fromArgument` — code de catalogue, nom d'enum ou balise BCP 47, sans
distinction de casse — et le transmet à `MainView`.

**La substitution vaut pour l'exécution, pas pour le fichier.** `MainView` garde la langue qu'il a chargée
dans `savedLanguage` et réécrit *celle-ci*, jusqu'à ce que `languageChosenInSession` indique que
l'utilisateur a lui-même choisi une langue ; son choix persiste alors normalement. Cette séparation est
tout le principe : un profil de borne ou un raccourci de démonstration peut s'ouvrir en espagnol sans
réécrire ce que le propriétaire de la machine ouvrira demain, tandis qu'un vrai choix fait dans le panneau
d'options reste acquis.

Une valeur qui ne désigne aucun catalogue est journalisée avec les codes acceptés, puis ignorée. Refuser
de démarrer à cause d'une faute de frappe dans un raccourci serait la pire panne, et
`Languages.fromArgument` renvoie `null` plutôt que de lever une exception pour la même raison.

### Ajouter une langue

Aucune modification de code n'est nécessaire.

1. Modifiez `ui_<CODE>.properties` — chaque langue a déjà son fichier.
2. Traduisez les valeurs. Laissez en place les paramètres `{0}` / `{1}`.
3. Compilez. Deux tests font respecter le contrat : l'un échoue si le code demande une clé que l'anglais
   ne définit pas, l'autre si le jeu de clés d'un catalogue s'écarte de celui de l'anglais.

Le premier test parcourt les **sources** à la recherche de littéraux de clé plutôt que de faire de la
réflexion sur les classes, car les clés n'existent que sous forme de littéraux de chaîne aux points
d'appel. C'est pourquoi un nouveau message doit être construit avec la clé en ligne —
`strings.format("st.scannerError", …)` — plutôt que recherché via une variable, sans quoi le test ne peut
pas la voir.

### Ce qui n'est volontairement pas traduit

- les noms de champ laconiques affichés en chasse fixe à côté des valeurs brutes — `format`, `position`,
  `length`, `raw` et `part` de la carte d'élément, `AI`, `sep`, `pos` et `len` du ruban, et les quatre
  cellules qui localisent une erreur (`AI`, `position`, `level`, `stage`), dont les valeurs sont de toute
  façon des noms d'enum non traduits ;
- les noms des entrées d'exemple, qui désignent des constructions GS1, et les noms de symbologie, qui sont
  des noms propres ;
- le paragraphe de remédiation `GE-C084` — de la prose normative, où une demi-traduction est pire que
  l'anglais ;
- le garde-fou d'AI inconnu d'`ElementBuilder`, inaccessible depuis l'interface : l'éditeur vérifie
  lui-même les AI inconnus et les signale à partir du catalogue ;
- les lignes de journal.

Une lacune subsiste : `ScanSource.label()` fournit `manual` / `scanner` / `file` / `built` en anglais à la
ligne d'historique et à l'en-tête du rapport. La combler demande plus qu'une clé — `HistoryCell` n'a pas
d'`UiStrings`, et la fabrique de cellules de la liste n'est installée qu'une fois ; les cellules devraient
donc aussi être rafraîchies lors d'un changement de langue.

---

## Formats de l'état enregistré

Deux fichiers sous `~/.gaia-desktop/`, séparés parce qu'ils changent à des rythmes différents — un journal
de captures corrompu ou modifié à la main ne devrait pas coûter ses réglages à l'utilisateur. Changez le
répertoire avec `-Dgaia.desktop.home=…`, ce qui permet aux tests de rester hors du vrai répertoire
personnel.

**`options.properties`** — écrit quand une option change, et à la sortie.

| Clé | Signification |
|---|---|
| `parse.mode`, `parse.language` | Profondeur du pipeline et langue des messages |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Rendu des dates |
| `check.skipRequires`, `check.skipExcludes` | Dérogations aux vérifications structurelles |
| `ui.theme` | `LIGHT` ou `DARK` |
| `ui.optionsPane` | Panneau ouvert ? |
| `ui.scannerDevice` | Dernier nom logique JavaPOS |
| `ui.mode` | `PARSE`, `BUILD` ou `COMBINE` |
| `update.check` | Si la vérification quotidienne des mises à jour s'exécute (par défaut `true`) |
| `update.lastCheck` | Quand elle s'est exécutée pour la dernière fois, en ISO-8601 ; absente ou illisible, une vérification est due |
| `update.skipVersion` | Une version pour laquelle l'utilisateur a choisi *Ignorer cette version* ; elle n'est plus proposée automatiquement |

Un fichier manquant ou abîmé n'est jamais fatal : chaque valeur se replie individuellement, si bien qu'une
ligne fautive ne coûte qu'elle-même. Au premier lancement, sans aucun fichier, les options sont amorcées à
partir des réglages régionaux de la machine par `SystemDefaults` plutôt qu'à partir des valeurs par défaut
de GAIA — le format de date court de la locale donne l'ordre des composants, le séparateur, et si le mois
et l'année sont abrégés.

Ne passez jamais un null à `Properties.setProperty` : il lève une exception, et `persistOptions()`
s'exécute depuis des gestionnaires d'événements de l'interface, où une exception s'échappe sur le fil FX.
C'est un vrai plantage que cette application a déjà connu.

**`captures.tsv`** — écrit quand l'historique change, et à la sortie. Les plus récentes d'abord, plafonné
à **500** entrées.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Seul ce qui ne peut pas être recalculé est stocké. Le `ParseResult` n'est **pas** sérialisé — il est
reconstitué en analysant de nouveau l'entrée au chargement, si bien qu'un historique restauré reflète les
options en vigueur *maintenant*. Les entrées contiennent couramment des FNC1 et tout ce qu'un lecteur peut
émettre ; le champ d'entrée est donc échappé (`\\`, `\t`, `\n`, `\r`, et `\xNN` pour tout le reste
au-dessous de `0x20`), et le fichier est lu et écrit en ISO-8859-1, octet pour octet.

Les fichiers écrits avant que la date ne soit enregistrée contiennent une simple heure ; ils se chargent
toujours, datés d'aujourd'hui, puisque la vraie date n'a jamais été notée.

---

## Tests

```bash
(cd gaia-desktop && mvn test)
```

96 tests, volontairement **indépendants de la boîte à outils JavaFX**, ce qui les garde rapides et sans
interface graphique. Ils couvrent le moteur de rendu du rapport, la logique de composition et les
recherches d'AI, la conversion `{GS}`, les valeurs par défaut issues de la locale, les deux magasins (y
compris un aller-retour contenant `0x1D` et des fichiers volontairement abîmés), la table des
symbologies, la règle de plateforme du lecteur, le bouchon de version, et les catalogues.

Trois méritent d'être connus parce qu'ils défendent des contrats plutôt que des fonctions :

| Test | Échoue quand |
|---|---|
| `CatalogueCoverageTest` | Le code demande une clé que `ui_EN.properties` ne définit pas, ou l'un des 35 fichiers s'écarte du jeu de clés anglais |
| `SymbologiesTest` | Une constante `SCAN_SDT_*` déclarée par JavaPOS n'a pas de nom d'affichage — y compris une qu'ajouterait une mise à niveau de JavaPOS |
| `ScannerServiceTest` | La règle de plateforme change de réponse pour Windows ou pour tout le reste |

`UiStrings` est volontairement tolérant à l'exécution — une clé inconnue s'affiche comme la clé elle-même
au lieu de lever une exception en pleine mise en page —, et c'est précisément pourquoi le test du catalogue
doit exister.

**Non couvert :** tout ce qui demande une boîte à outils active — le comportement du focus, le basculement
de mode, l'édition des tableaux, le changement de langue, et les deux plantages décrits sous
[Fils d'exécution](#fils-dexécution). Ils ont été vérifiés en pilotant la vraie fenêtre, mais une
régression à leur niveau ne ferait pas échouer la compilation. Ajouter TestFX ou Monocle comblerait cette
lacune.

---

## Dépannage de la compilation

| Symptôme | Cause et correctif |
|---|---|
| `package tools.pantheum.gaia does not exist` | Le cœur n'est pas installé. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | La `Main-Class` du jar doit être `Launcher`, pas `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* au démarrage | Attendu et sans conséquence — JavaFX signale qu'il est sur le class path. C'est exactement la raison d'être de `Launcher` |
| Le jar ne démarre pas, `NoClassDefFoundError` | Le jar par défaut ne contient que les classes de ce module ; `dist/lib/` doit être à côté. Utilisez `-Pfat` pour un seul fichier autonome |
| `Artifact has not been packaged yet … MDEP-187` dans Eclipse | Résolution d'espace de travail de m2e. Voir la note sous [Empaquetage](#empaquetage) |
| Des fichiers `… 2.properties` en double apparaissent dans une compilation | Le projet se trouve dans un dossier synchronisé avec iCloud et la synchronisation a produit des copies en conflit pendant la compilation. Faites toujours `mvn clean package` ; mieux, excluez `target/` de la synchronisation |
| Un élément de menu affiche une clé comme `act.hri` | Cette clé manque dans `ui_EN.properties`. La compilation aurait dû l'attraper — voir [Tests](#tests) |

---

## Limites connues et travail en cours

- **Pas de tests d'interface sans affichage.** Voir [Tests](#tests).
- **Artefacts propres à une plateforme.** Les deux empaquetages embarquent les natifs JavaFX d'une seule
  plateforme ; compilez-en un par cible avec `-Dfx.platform` (n'importe quelle machine peut les compiler
  tous). Aucun n'embarque d'environnement d'exécution Java — il faudrait `jpackage`, qui ne compile que
  pour l'OS sur lequel il s'exécute.
- **La lecture au lecteur est réservée à Windows.** Voir [Le verrou de plateforme](#le-verrou-de-plateforme).
- **`ScanSource.label()` n'est pas localisé.** Voir [Ce qui n'est volontairement pas
  traduit](#ce-qui-nest-volontairement-pas-traduit).
- **`UiState.mode` est une `String`** (`"PARSE"` / `"BUILD"`) alors que `Theme`, à côté, est un enum ; le
  littéral apparaît donc à plusieurs endroits. Un enum les supprimerait.
- **La cible Digital Link se limite au domaine et au préfixe de chemin.** `BuilderDigitalLinkConfig`
  prend aussi en charge des paramètres de requête supplémentaires et un fragment ; l'éditeur ne les expose
  pas.
- **Les paires d'AI mutuellement exclusives (`GE-S006`) ne sont pas signalées dans l'éditeur** comme le
  sont les AI inconnus et en double — la composition échoue simplement.
- **Le catalogue d'erreurs du cœur double les apostrophes** (`value ''09506000134350''`), ce qui apparaît
  tel quel dans les échecs de composition et dans le rapport. On dirait un échappement d'apostrophes de
  `MessageFormat` qui n'est pas défait ; le correctif relève du cœur.
