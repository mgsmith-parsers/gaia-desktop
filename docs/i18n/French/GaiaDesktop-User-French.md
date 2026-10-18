# GAIA Desktop — Guide de l'utilisateur

Comment utiliser l'application. Pour la compiler, la modifier ou ajouter une langue, voir le
[guide du développeur](../../GaiaDesktop-Developer.md) (en anglais). Version originale :
[GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Table des matières

1. [Ce que c'est](#ce-que-cest)
2. [Lancer l'application](#lancer-lapplication)
3. [La fenêtre](#la-fenêtre)
4. [Mode Analyser](#mode-analyser)
   - [La barre de saisie et le jeton {GS}](#la-barre-de-saisie-et-le-jeton-gs)
   - [Lecture avec un lecteur](#lecture-avec-un-lecteur)
   - [Ouvrir un fichier](#ouvrir-un-fichier)
   - [L'historique des captures](#lhistorique-des-captures)
5. [Mode Composer](#mode-composer)
   - [L'éditeur d'AI](#léditeur-dai)
   - [Ce que l'éditeur refuse](#ce-que-léditeur-refuse)
   - [Chaîne d'éléments ou Digital Link](#chaîne-déléments-ou-digital-link)
   - [Modifier une capture existante](#modifier-une-capture-existante)
6. [Mode Combiner](#mode-combiner)
   - [La bande Combiner](#la-bande-combiner)
   - [Ce que Combiner refuse](#ce-que-combiner-refuse)
   - [Doublons et conflits](#doublons-et-conflits)
   - [Le rapport de combinaison](#le-rapport-de-combinaison)
7. [Le rapport](#le-rapport)
   - [Exporter](#exporter)
8. [Options d'analyse](#options-danalyse)
   - [Premier lancement](#premier-lancement)
9. [Langues](#langues)
10. [Ce qui est mémorisé](#ce-qui-est-mémorisé)
11. [Raccourcis clavier](#raccourcis-clavier)
12. [Dépannage](#dépannage)
13. [Limites](#limites)

---

## Ce que c'est

GAIA Desktop fait trois choses avec des données GS1 :

- **Analyser** — saisissez, collez, ouvrez ou scannez une chaîne d'éléments GS1 ou un URI
  Digital Link, et lisez le résultat sous forme de rapport complet : ce que signifie chaque
  identifiant d'application (AI), ce qui ne va pas, et pourquoi ;
- **Composer** — assembler des paires AI / valeur en une chaîne d'éléments ou un URI
  Digital Link ;
- **Combiner** — réunir jusqu'à trois codes-barres GS1-128 d'une même étiquette en la
  chaîne d'éléments unique que porterait un GS1 DataMatrix, la vérifier comme un tout, et
  dessiner le symbole.

Tout ce que l'application sait du GS1 — chaque définition d'AI, chaque règle de validation,
chaque message d'erreur et chaque description localisée — vient du moteur GAIA sous-jacent.
L'application n'ajoute aucune règle : ce qu'elle accepte est exactement ce que le moteur
accepte.

---

## Lancer l'application

```bash
java -jar gaia-desktop.jar
```

Le fichier JAR a besoin du répertoire `lib/` livré à côté de lui — gardez les deux ensemble,
sinon l'application ne démarrera pas. (Il est possible de construire un JAR autonome ; voir le
[guide du développeur](../../GaiaDesktop-Developer.md#packaging).)

**Java 21 ou plus récent** doit être installé. La distribution est propre à un système
d'exploitation et à une architecture de processeur : utilisez le JAR construit pour votre
machine.

Pour ouvrir l'application dans une langue donnée, sans modifier la langue enregistrée :

```bash
java -jar gaia-desktop.jar --language=FR
```

La valeur peut être le code du catalogue (`FR`, `PCM`, `ARZ`), le nom complet (`FRENCH`,
`WU-CHINESE`) ou une étiquette de langue (`fr`, `zh-HK` pour le cantonais, `ar-EG` pour l'arabe
égyptien) — la casse n'a pas d'importance. Le remplacement ne vaut que pour cette exécution :
votre langue enregistrée reste intacte, si bien qu'un raccourci créé pour un collègue ne peut
pas changer la langue dans laquelle votre propre copie s'ouvre. Choisir une langue *dans*
l'application reste un vrai choix, enregistré comme d'habitude. Une valeur qui ne désigne
aucune langue est ignorée, avec une note dans la console : l'application s'ouvre dans la langue
enregistrée plutôt que de refuser de démarrer.

Au démarrage, la console affiche une ligne indiquant la version de l'application, sa date de
compilation et la version du moteur. C'est la ligne à citer dans un rapport de bogue.

---

## La fenêtre

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

> Le schéma garde les libellés anglais, comme tous les blocs de code de cette documentation.
> À l'écran, vous verrez *Analyser* / *Composer* / *Combiner*, *Captures*, *Enregistrer le rapport*,
> *Options*, *lecteur : arrêté* et *À propos*.

**Sur macOS et Linux, la rangée du lecteur n'existe pas** — ni bouton, ni sélecteur d'appareil
dans la barre d'outils, pas de menu *Lecteur*, pas de *lecteur : arrêté* dans la barre d'état.
Voir [Lecture avec un lecteur](#lecture-avec-un-lecteur).

Le sélecteur **Analyser ⇄ Composer** ne change que la bande située sous la barre d'outils et
les commandes de capture. L'historique, le rapport, le panneau d'options et les actions
d'export servent les deux modes, car une composition réussie devient simplement une capture de
plus. Le mode **Combiner** est disposé autrement : voir [Mode Combiner](#mode-combiner).

Le panneau d'options est **fermé au premier lancement**. Le mode, la visibilité du panneau, le
thème et le dernier lecteur utilisé sont mémorisés d'une exécution à l'autre.

**Nouvelles versions.** Quelques secondes après son démarrage, au plus une fois par jour,
l'application demande à GitHub si une version plus récente de GAIA Desktop a été publiée. Si
c'est le cas, un lien apparaît dans la barre d'état — *GAIA Desktop 1.1.0 est disponible* — qui ouvre
la page de la version dans votre navigateur ; rien n'est téléchargé ni installé à votre place.
*Ignorer cette version* masque l'avis jusqu'à une version ultérieure. La vérification reste
silencieuse quand il n'y a rien de nouveau ou quand GitHub est injoignable.
*Aide → Rechercher des mises à jour…* vérifie immédiatement et indique le résultat ;
*Aide → Rechercher automatiquement les mises à jour* désactive ou réactive la vérification quotidienne.

---

## Mode Analyser

### La barre de saisie et le jeton {GS}

La barre accepte une chaîne d'éléments, un URI GS1 Digital Link, ou une lecture brute avec son
préfixe AIM Code ID (`]d2…`). <kbd>Entrée</kbd> lance l'analyse.

Les AI de longueur variable sont séparés par **FNC1** — le caractère de contrôle invisible
`0x1D`. Un champ de texte ne peut pas contenir ce caractère : il serait supprimé sans bruit, et
`01…` + FNC1 + `10LOT` arriverait sous la forme `01…10LOT`, une chaîne d'éléments qui ne veut
plus dire la même chose.

L'application affiche donc le séparateur sous la forme du jeton visible **`{GS}`** et le
reconvertit sur le chemin de l'analyseur :

- le **bouton `{GS}`** insère le jeton à l'emplacement du curseur ;
- **coller** de vrais caractères séparateurs — au clavier, par le menu contextuel ou par
  glisser-déposer — les transforme automatiquement en `{GS}` : rien n'est perdu ;
- le jeton s'écrit exactement comme le rapport l'écrit, de sorte qu'une valeur copiée depuis un
  rapport peut être recollée telle quelle.

La capture, le rapport et l'historique enregistré contiennent tous le **vrai** séparateur. Le
jeton n'est que ce que vous voyez et saisissez.

### Lecture avec un lecteur

> **Windows uniquement.** Zebra ne fournit ses pilotes de lecteur que pour Windows : sur macOS
> et Linux, l'application ne construit aucune commande de lecture — pas de bouton *Connecter le
> lecteur*, pas de sélecteur d'appareil, pas de menu *Lecteur*, et rien sur le lecteur dans la
> barre d'état. Rien n'est grisé, car aucun réglage ni aucun appareil ne pourrait l'activer.
> Tout le reste, y compris l'ouverture d'un fichier de lectures enregistrées, fonctionne
> partout.

*Connecter le lecteur* ouvre l'appareil désigné dans le sélecteur ; `ZebraAllScanners` est la
valeur par défaut et convient à la plupart des modèles. Chaque lecture est analysée et ajoutée
à l'historique automatiquement.

Les données du lecteur sont lues octet par octet : le préfixe AIM Code ID et chaque séparateur
FNC1 parviennent à l'analyseur exactement tels que le lecteur les a envoyés.

Les commandes du lecteur sont masquées en mode Composer, mais un lecteur connecté continue de
fonctionner : *Lecteur → Connecter / Déconnecter* (<kbd>⌘K</kbd>) le bascule toujours, la barre
d'état l'indique toujours, et une lecture qui arrive pendant que l'éditeur est ouvert est
capturée quand même. En mode Combiner, les commandes restent affichées, et chaque lecture est ajoutée à la
combinaison au lieu de l'historique.

La première mise en service d'un lecteur — le Zebra Scanner SDK, le mode USB du lecteur et les
réglages GS1 — est décrite pas à pas dans le
[guide de configuration du lecteur Zebra](GaiaDesktop-Zebra-Scanner-Windows-French.md), fourni dans chaque archive zip sous
`docs/`. Si un appareil ne se connecte pas, voir [Dépannage](#dépannage).

### Ouvrir un fichier

*Fichier → Ouvrir un fichier texte…* analyse **chaque ligne non vide comme une capture
distincte**, ce qui est la façon rapide de traiter un lot de lectures qu'on vous a envoyé. Le
fichier est lu octet par octet : les séparateurs bruts qu'il contient survivent.

### L'historique des captures

Les plus récentes en premier. Chaque ligne montre un point valide / invalide, un résumé d'une
ligne, puis l'heure de capture, la provenance et le nombre d'AI. La date n'apparaît que sur les
captures d'un jour antérieur — les lignes du jour n'affichent que l'heure.

Le résumé est l'**URI Digital Link** pour un Digital Link, et le **HRI** sinon : pour une chaîne
d'éléments, `(01)09506000134352 (17)261231` se lit mieux que
`01095060001343521726123110…`.

Faites un clic droit sur une capture pour copier son entrée ou son HRI, la renvoyer vers la
barre de saisie, l'ouvrir dans l'éditeur, la supprimer ou effacer l'historique.

---

## Mode Composer

### L'éditeur d'AI

Un tableau de paires AI / valeur. Saisissez un code AI ou choisissez parmi **tous les AI que le
moteur connaît** dans la liste *Ajouter un AI…* ; les colonnes **Titre**, **Format** et
**Description** se remplissent au fur et à mesure de la saisie, dans la langue choisie. Le
sélecteur ne peut pas proposer un AI que le compositeur refuserait.

Le tableau garde toujours **une ligne vide à la fin** : saisissez-y quelque chose et la suivante
apparaît, de sorte qu'une chaîne à plusieurs AI n'a jamais besoin d'*Ajouter une ligne*.

L'édition se comporte comme un tableau le doit : <kbd>Entrée</kbd>, <kbd>Tab</kbd>,
<kbd>Maj</kbd>+<kbd>Tab</kbd> et un clic ailleurs **conservent** tous ce que vous avez saisi ;
<kbd>Échap</kbd> l'abandonne. <kbd>Tab</kbd> passe de AI à Valeur puis à la ligne suivante, en
sautant les colonnes non modifiables.

### Ce que l'éditeur refuse

| | Ce qui se passe |
|---|---|
| Un AI que le moteur ne reconnaît pas | La ligne devient rouge pendant la saisie ; la composition est refusée avec une explication |
| Le même AI deux fois, depuis le sélecteur | Non ajouté — un message le nomme et la ligne qui le contient déjà est sélectionnée |
| Le même AI deux fois, saisi à la main | Les deux lignes sont marquées ; la composition est refusée avec l'erreur `GE-S004` |
| Rien à composer | Un message ; aucune capture n'est créée |

Un AI ne peut apparaître qu'une seule fois dans une chaîne d'éléments : c'est pourquoi le
sélecteur refuse d'emblée plutôt que de laisser tenter une composition vouée à l'échec.

### Chaîne d'éléments ou Digital Link

**Composer la chaîne d'éléments** assemble les lignes en insérant le séparateur FNC1 partout où
la norme l'exige. **Composer le Digital Link** produit un URI ; dépliez *Cible Digital Link*
pour le diriger vers votre propre résolveur au lieu du `id.gs1.org` canonique :

| Champ | Effet | Vide signifie |
|---|---|---|
| Domaine | L'hôte du résolveur, p. ex. `example.com` | `id.gs1.org` |
| Préfixe de chemin | Un segment avant le chemin des AI, p. ex. `/products` | aucun préfixe |

En cas de **réussite**, le résultat se comporte exactement comme une lecture : analysé, ajouté à
l'historique, rendu sous forme de rapport et enregistré. Le compositeur valide en analysant sa
propre sortie : tout ce qu'il renvoie est donc garanti valide.

En cas d'**échec**, rien n'est capturé. La raison reste sous les boutons de composition, une
ligne par problème, chacune nommant l'erreur, l'AI concerné et l'explication propre au moteur :

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> Les guillemets doublés dans ce message viennent du catalogue d'erreurs du moteur, pas de cette
> application.

### Modifier une capture existante

Clic droit sur une capture → **Composer** charge ses AI dans l'éditeur. Modifiez une valeur,
recomposez, et le résultat arrive comme une nouvelle capture — l'originale reste intacte.

---

## Mode Combiner

Les étiquettes plus anciennes répartissent souvent leurs données sur **plusieurs codes-barres
GS1-128** — le GTIN dans l'un, la date de péremption et le lot dans un autre — parce qu'un seul
code-barres linéaire ne peut pas tout contenir. Le mode Combiner prend jusqu'à **trois**
de ces codes-barres, les réunit en la chaîne d'éléments unique que porterait un **GS1
DataMatrix**, et vérifie le résultat comme un tout. Une fois celui-ci valide, l'application
dessine le symbole DataMatrix.

Aucun de ces codes-barres n'a besoin d'être complet à lui seul. Un numéro de lot sans son GTIN
échoue seul avec `GE-S005`, mais c'est normal pour un code-barres d'un ensemble : le mode
Combiner le vérifie donc sur la combinaison.

### La bande Combiner

Passer en mode **Combiner** remplace la barre de saisie par une bande en bas de la
fenêtre. L'historique des captures s'efface, car les lectures du mode Combiner ne sont pas
des captures et n'y entrent jamais. La zone du rapport affiche à la place le rapport de
combinaison :

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

Un code-barres peut arriver de deux façons :

- **En le lisant.** Sous Windows, les commandes du lecteur restent dans la barre d'outils en mode
  Combiner, et chaque lecture va directement dans la combinaison plutôt que dans
  l'historique.
- **En le saisissant ou en le collant** dans le champ *Code-barres*, puis en
  appuyant sur <kbd>Entrée</kbd> ou sur **Ajouter**. Écrivez le FNC1 sous la forme
  **`{GS}`**, comme dans la barre de saisie. Un code-barres saisi sans AIM Code ID est considéré
  comme un GS1-128.

| Bouton | Effet |
|---|---|
| **Ajouter** | Ajoute le code-barres du champ |
| **Retirer le dernier** | Retire le code-barres le plus récent (indisponible quand il n'y en a aucun) |
| **Réinitialiser** | Retire tous les codes-barres, vide le champ et efface tout refus, puis repart de zéro |

La barre d'état indique toujours combien de codes-barres sont retenus
(*2 code-barres GS1-128 sur 3*), ou pourquoi le dernier a été refusé.

### Ce que Combiner refuse

Un code-barres refusé n'est pas ajouté. Le rapport et la barre d'état disent tous deux pourquoi :

| Code-barres | Refusé parce que |
|---|---|
| Un champ vide | Il n'y a rien à ajouter |
| Un quatrième code-barres | Une étiquette se combine à partir de trois au plus. Retirez le dernier, ou réinitialisez |
| Une autre symbologie, d'après son AIM Code ID | Seul le GS1-128 se combine : un GS1 DataMatrix (`]d2`) ou un QR Code est refusé, avec son nom |
| Des AI illisibles | Une erreur de syntaxe ; le code d'erreur et le message du moteur sont affichés |
| Aucun AI | Il n'y a rien à combiner |
| Un code-barres déjà lu | La même chaîne d'éléments deux fois, par exemple le même code-barres lu de nouveau ; le rapport indique quel code-barres précédent il répète |

Le mode Combiner ne refuse que ce qu'il ne peut pas utiliser du tout. Tout le reste (une
clé de contrôle fausse, une date incorrecte, un AI compagnon manquant) est accepté et jugé sur la
combinaison.

### Doublons et conflits

Les AI sont combinés dans l'ordre de lecture. Quand deux codes-barres portent le même AI :

- **avec la même valeur**, il est conservé une fois et listé sous *Doublons — conservés une fois*, avec
  les codes-barres où il a été lu ;
- **avec des valeurs différentes**, c'est un **conflit**. Le verdict indique *CONFLIT*,
  les valeurs en désaccord sont listées code-barres par code-barres, et **rien n'est combiné**
  tant que le conflit n'est pas résolu. Retirez le code-barres fautif, ou réinitialisez.

### Le rapport de combinaison

De haut en bas :

1. **Le verdict** : *aucun scan*, *CONFLIT*, valide ou non valide, et le
   nombre de codes-barres retenus.
2. **Les lectures** : chaque code-barres tel qu'il est arrivé, sa symbologie, et la chaîne
   d'éléments qui reste une fois l'AIM Code ID retiré. Un problème que seule la combinaison peut
   trancher est signalé sur le code-barres, avec la mention *jugé sur la combinaison*.
3. **Les doublons** et **les conflits**, s'il y en a.
4. **Combinaison** : la chaîne d'éléments, son HRI et sa longueur, puis l'entrée combinée
   sous forme de bande étiquetée et une carte par AI, présentées comme dans le
   [rapport d'analyse](#le-rapport). Chaque erreur et chaque avertissement sur la combinaison est
   ensuite listé sous *Validation*.
5. **Le symbole GS1 DataMatrix**, avec son HRI en dessous. Il n'est dessiné que si la
   combinaison est valide. Sinon, le rapport explique pourquoi il ne l'a pas été, afin qu'un
   symbole non valide ne puisse pas être imprimé par erreur.

Par exemple, une étiquette à deux codes-barres :

| Code-barres | Contient | Seul |
|---|---|---|
| `]C10109506000134352` | (01) GTIN | valide |
| `]C11726123110LOT-ABC` | (17) date de péremption, (10) lot | `GE-S005` pour les deux AI, jugé sur la combinaison |

Ils se combinent en `01095060001343521726123110LOT-ABC`, HRI
`(01)09506000134352 (17)261231 (10)LOT-ABC`, qui est valide : le symbole est donc dessiné.
L'AI `17` est de longueur fixe, aucun FNC1 n'est donc nécessaire après lui. Le mode
Combiner en insère un partout où la norme l'exige.

**Enregistrer le rapport** exporte le rapport de combinaison en HTML ou en PDF, comme un rapport
d'analyse (voir [Exporter](#exporter)), dès qu'un code-barres est retenu. Le nom du fichier HTML
commence par `gaia-combine-`.

Le rapport suit la langue et le thème choisis. Changer une option d'analyse ou la langue
revérifie les codes-barres déjà retenus. Ils survivent aussi à un passage en mode
Analyser ou Composer puis retour, mais pas à un redémarrage : les codes-barres du
mode Combiner ne sont pas enregistrés.

---

## Le rapport

La capture sélectionnée est rendue sous forme de rapport complet : une bannière de verdict, la
chaîne d'éléments décomposée en une « bande » étiquetée, les métadonnées d'analyse, une carte
par AI avec ses interprétations, et les erreurs et avertissements accompagnés de conseils de
correction.

Pour un **Digital Link**, chaque carte d'AI porte en plus son rôle structurel — **clé
primaire**, **qualificateur de clé** ou **attribut de données** — avec un code couleur suivant
leur ordre d'apparition dans l'URI. Les rôles ne sont affichés que pour un Digital Link : dans
une simple chaîne d'éléments, un AI n'a aucune position d'URI à décrire.

Le rapport suit la langue et le thème que vous avez choisis.

### Exporter

| | |
|---|---|
| **Enregistrer le rapport → En HTML…** (<kbd>⌘S</kbd>) | Écrit exactement ce qui est à l'écran : un fichier autonome qui s'ouvre dans n'importe quel navigateur |
| **Enregistrer le rapport → En PDF…** (<kbd>⌘P</kbd>) | Imprime le rapport via la boîte de dialogue d'impression du système — choisissez-y une destination PDF |

Le PDF passe par la boîte de dialogue d'impression parce que c'est là que se choisissent la
destination et le nom du fichier : **PDF ▾ → Enregistrer au format PDF** sur macOS, **Microsoft
Print to PDF** sous Windows, **Imprimer dans un fichier** sous Linux. Les pages sont paginées et
le texte reste sélectionnable.

L'impression et le PDF utilisent toujours la palette claire, même quand l'application est en
thème sombre, et conservent les pastilles d'AI colorées et la bannière de verdict.

Si votre machine n'a aucune imprimante ni destination PDF configurée, l'application le signale
et vous oriente vers l'export HTML, qui s'imprime depuis n'importe quel navigateur.

---

## Options d'analyse

Le panneau de droite regroupe toutes les options du moteur. Modifier quoi que ce soit
**relance l'analyse de la capture sélectionnée immédiatement** : le panneau sert donc aussi à
demander « à quoi ressemblerait cette entrée avec ces réglages ? », et pas seulement à influer
sur la lecture suivante.

| Option | Valeurs |
|---|---|
| Mode d'analyse | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — jusqu'où pousser l'analyse |
| Langue | 35 catalogues de messages |
| Ordre | `LITTLE` (jj/mm/aaaa), `MIDDLE` (mm/jj/aaaa), `BIG` (aaaa/mm/jj) |
| Séparateur | `SLASH`, `HYPHEN`, `PERIOD` |
| Mois | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Année | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Ignorer le contrôle « requires » | N'écarte plus un AI dont le compagnon obligatoire est absent (`GE-S005`) |
| Ignorer le contrôle « excludes » | N'écarte plus les associations d'AI que la norme interdit (`GE-S006`) |

*Rétablir les valeurs par défaut*, au bas du panneau, restaure les valeurs par défaut du moteur.

### Premier lancement

Quand rien n'est encore enregistré, l'application prend ses réglages dans les **paramètres
régionaux de votre machine** plutôt que dans les valeurs par défaut du moteur :

- la **langue** vient des paramètres régionaux du système, avec repli sur l'anglais lorsqu'il
  n'existe pas de catalogue ;
- les **options de date** sont déduites du format de date court de vos paramètres régionaux :
  l'ordre des composants, le séparateur, et le fait que le mois et l'année soient abrégés.

> **Remarque.** Des paramètres régionaux qui écrivent les dates courtes avec une année à deux
> chiffres démarrent donc ici avec une année à deux chiffres : cela correspond au reste de votre
> système, mais diffère de la valeur par défaut du moteur, qui est à quatre chiffres.
> Changez-la dans le panneau et le choix est conservé.

---

## Langues

Choisir une langue change **toute l'application** — menus, boutons, éditeur, panneau d'options,
barre d'état et rapport — pas seulement les messages d'erreur. Les langues qui s'écrivent de
droite à gauche inversent la fenêtre et le rapport.

Les 35 langues sont disponibles, avec deux niveaux de couverture :

| | Couverture |
|---|---|
| Rapports d'analyse et messages d'erreur du moteur | **les 35 langues** |
| Menus, boutons et libellés de l'application | **les 35 langues** |

Les langues de droite à gauche — arabe, arabe égyptien, persan et ourdou — inversent la fenêtre
et le rapport dès l'ouverture de l'application.

La langue est mémorisée d'une exécution à l'autre. Pour n'ouvrir qu'une fois dans une autre
langue, voir [`--language`](#lancer-lapplication).

Quelques éléments restent volontairement non traduits partout : les noms de champs abrégés
imprimés à côté des valeurs brutes (`format`, `position`, `raw`, `len` …), qui sont des
identifiants plutôt que des mots ; les noms des exemples et des symbologies, qui sont des termes
GS1 et de la profession ; et un long paragraphe normatif où une traduction partielle induirait
en erreur.

---

## Ce qui est mémorisé

Deux fichiers dans `.gaia-desktop`, à l'intérieur de votre répertoire personnel :

| Fichier | Contient |
|---|---|
| `options.properties` | Toutes les options d'analyse, le thème, l'ouverture du panneau d'options, le dernier lecteur utilisé, le mode dans lequel la fenêtre a été laissée, et les réglages de la vérification des mises à jour — activée ou non, sa dernière exécution, et toute version que vous avez choisi d'ignorer |
| `captures.tsv` | L'historique des captures, les plus récentes en premier, limité à 500 entrées |

Les options sont écrites dès que vous en changez une ; l'historique dès qu'il change. Les deux
sont réécrits à la fermeture.

Supprimer l'un des fichiers réinitialise cette partie et rien d'autre — les deux sont séparés
pour qu'un historique abîmé ne vous coûte pas vos réglages. Un fichier abîmé par une
modification manuelle n'est pas fatal non plus : chaque réglage retombe individuellement sur sa
valeur par défaut, si bien qu'une ligne fautive ne coûte que cette ligne.

L'historique stocke l'entrée, pas l'analyse. Elle est ré-analysée au chargement, ce qui signifie
qu'une capture restaurée reflète les options en vigueur **maintenant**, et non celles en vigueur
au moment de la capture.

Les codes-barres retenus en mode Combiner ne sont pas enregistrés : après un
redémarrage, le mode Combiner repart vide.

---

## Raccourcis clavier

| | |
|---|---|
| <kbd>Entrée</kbd> | Analyser la barre de saisie |
| <kbd>⌘O</kbd> | Ouvrir un fichier texte |
| <kbd>⌘S</kbd> | Enregistrer le rapport en HTML |
| <kbd>⌘P</kbd> | Enregistrer le rapport en PDF (boîte de dialogue d'impression) |
| <kbd>⌘G</kbd> | Insérer le jeton séparateur FNC1 |
| <kbd>⌘K</kbd> | Connecter / déconnecter le lecteur (Windows uniquement) |
| <kbd>⌘D</kbd> | Basculer le thème sombre |
| <kbd>⌘Q</kbd> | Quitter |

Dans l'éditeur : <kbd>Tab</kbd> / <kbd>Maj</kbd>+<kbd>Tab</kbd> déplacent d'une cellule
modifiable à l'autre, <kbd>Entrée</kbd> valide, <kbd>Échap</kbd> annule.

En mode Combiner : <kbd>Entrée</kbd> dans le champ *Code-barres* ajoute le
code-barres. Là, saisissez le FNC1 sous la forme `{GS}` ; <kbd>⌘G</kbd> appartient à la barre de
saisie du mode Analyser.

---

## Dépannage

| Symptôme | Cause et solution |
|---|---|
| L'application ne démarre pas, ou signale `NoClassDefFoundError` | Le répertoire `lib/` doit se trouver à côté du JAR. Gardez-les ensemble, ou utilisez une distribution autonome |
| *« Unsupported JavaFX configuration… »* dans la console au démarrage | Attendu et sans gravité — la boîte à outils signale simplement comment elle a été chargée. L'application fonctionne normalement |
| Un séparateur collé semble disparaître | Attendu : il est réécrit sous la forme du jeton visible `{GS}`, et l'analyseur reçoit toujours le vrai caractère. Voir [le jeton {GS}](#la-barre-de-saisie-et-le-jeton-gs) |
| Il n'y a pas de bouton *Connecter le lecteur* | Vous êtes sur macOS ou Linux, où les pilotes du lecteur n'existent pas. Voir [Lecture avec un lecteur](#lecture-avec-un-lecteur) |
| Le lecteur ne se connecte pas | Vérifiez que le nom logique du sélecteur correspond à votre modèle, que le lecteur est branché, et qu'aucune autre application ne le retient. Le [guide de configuration du lecteur Zebra](GaiaDesktop-Zebra-Scanner-Windows-French.md#11-dépannage) détaille chaque code d'erreur. |
| L'export PDF indique qu'il n'y a pas de destination | Votre machine n'a ni imprimante ni destination PDF configurée. Ajoutez-en une, ou exportez en HTML et imprimez depuis un navigateur |
| Un élément de menu affiche quelque chose comme `act.hri` | Un libellé manquant — merci de le signaler ; c'est un défaut de compilation, pas un réglage |

---

## Limites

- **La lecture est réservée à Windows**, parce que les pilotes le sont.
- **Le PDF passe par la boîte de dialogue d'impression** plutôt que par une boîte
  d'enregistrement.
- **Les cibles Digital Link acceptent un domaine et un préfixe de chemin**, mais pas de
  paramètres de requête supplémentaires ni de fragment.
- **Un AI ne peut apparaître qu'une fois** dans une chaîne d'éléments — c'est la règle GS1, pas
  une limite de l'application.
- **Le mode Combiner n'accepte que des codes-barres GS1-128, trois au plus**, et ne les
  conserve pas d'une session à l'autre.
