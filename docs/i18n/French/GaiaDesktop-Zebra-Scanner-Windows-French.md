# Configuration d'un lecteur Zebra — GAIA Desktop pour Windows

Comment installer et configurer un lecteur de codes-barres Zebra pour que GAIA Desktop lise
directement les codes GS1 : chaque lecture est analysée et ajoutée à l'historique, avec le
préfixe AIM Code ID et chaque séparateur FNC1 intacts. Version originale :
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

GAIA Desktop a été développé et testé avec un imageur portable **Zebra DS8108** connecté en USB.
Les étapes valent pour tout lecteur 2D Zebra pris en charge par le Scanner SDK de Zebra ; les
codes-barres de configuration et les noms de menus d'un modèle donné se trouvent dans son propre
Quick Reference Guide ou Product Reference Guide.

> **Windows uniquement.** Les pilotes des lecteurs Zebra n'existent que pour Windows : sous macOS
> ou Linux, GAIA Desktop ne crée aucune commande de lecteur. Tout le reste de l'application
> fonctionne sur toutes les plateformes.

---

## Table des matières

1. [Ce qu'il vous faut](#1-ce-quil-vous-faut)
2. [Installer Java](#2-installer-java)
3. [Installer le Zebra Scanner SDK](#3-installer-le-zebra-scanner-sdk)
4. [Installer 123Scan](#4-installer-123scan)
5. [Mettre à jour le micrologiciel du lecteur Zebra](#5-mettre-à-jour-le-micrologiciel-du-lecteur-zebra)
6. [Régler le mode USB du lecteur Zebra](#6-régler-le-mode-usb-du-lecteur-zebra)
7. [Configurer le lecteur Zebra pour GS1](#7-configurer-le-lecteur-zebra-pour-gs1)
8. [Installer et lancer GAIA Desktop](#8-installer-et-lancer-gaia-desktop)
9. [Connecter le lecteur](#9-connecter-le-lecteur)
10. [Référence de configuration](#10-référence-de-configuration)
11. [Dépannage](#11-dépannage)
12. [Lecteurs 2D Zebra pris en charge](#12-lecteurs-2d-zebra-pris-en-charge)

---

## 1. Ce qu'il vous faut

| | |
|---|---|
| **Lecteur** | Un lecteur 2D Zebra, avec son câble USB — voir [Lecteurs 2D Zebra pris en charge](#12-lecteurs-2d-zebra-pris-en-charge) |
| **Ordinateur** | Windows 10 ou 11, 64 bits |
| **Java** | Java 21 ou plus récent, **64 bits** |
| **Zebra Scanner SDK pour Windows** | **64 bits**, depuis le site d'assistance de Zebra — il fournit le pilote et les services JavaPOS qu'utilise GAIA Desktop |
| **Zebra 123Scan** | L'utilitaire de configuration gratuit de Zebra, depuis son site d'assistance — sert à mettre à jour le micrologiciel du lecteur et à vérifier ses réglages |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Facultatif** | Le **Product Reference Guide** (PRG) du lecteur, qui contient les codes-barres de configuration |

Comment les éléments s'articulent :

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop embarque lui-même l'API JavaPOS et la configuration qui nomme le lecteur
(`jpos.xml`, dans `gaia-desktop.jar`). Le SDK Zebra fournit le reste : le pilote qui dialogue avec
le lecteur et les classes de service Zebra placées entre JavaPOS et ce pilote.

---

## 2. Installer Java

GAIA Desktop a besoin de **Java 21 ou plus récent**, et en version **64 bits**, pour correspondre
aux pilotes Zebra 64 bits.

1. Installez un JDK ou un JRE Java 21 pour Windows x64 — par exemple Eclipse Temurin depuis
   adoptium.net. Laissez l'installateur définir `JAVA_HOME` s'il le propose.
2. Vérifiez-le dans une invite de commandes :

   ```
   java -version
   ```

   La première ligne doit indiquer la version 21 ou ultérieure ; un Java 64 bits affiche aussi
   `64-Bit Server VM`.

`run.bat` cherche Java dans cet ordre : un dossier `jdk*` à côté de `run.bat`, puis `JAVA_HOME`,
puis `java` dans le `PATH`. Il refuse toute version antérieure à 21, avec un message qui le dit.

---

## 3. Installer le Zebra Scanner SDK

1. Téléchargez le **Zebra Scanner SDK pour Windows (64 bits)** depuis le site d'assistance de
   Zebra.
2. Lancez l'installateur. S'il propose un choix de composants, assurez-vous que le composant
   **JPOS** (JavaPOS) est inclus.
3. Gardez l'emplacement d'installation par défaut. GAIA Desktop cherche la partie JavaPOS du SDK
   dans :

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   et attend les jars de service de Zebra (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`,
   …) et les bibliothèques natives (`CSJPOSScanner64.dll`, …) dans son dossier `bin\`. Si le SDK
   est installé ailleurs, voir [ZEBRA_HOME](#zebra_home).
4. **Redémarrez Windows** après l'installation. Le pilote CoreScanner du SDK tourne en arrière-plan
   et doit être actif avant que GAIA Desktop puisse joindre le lecteur ; un redémarrage est le
   moyen le plus simple de s'en assurer.

---

## 4. Installer 123Scan

123Scan est l'utilitaire gratuit de Zebra pour configurer ses lecteurs. Il sert à l'étape
suivante pour mettre à jour le micrologiciel du lecteur, et c'est ensuite le moyen le plus simple
de vérifier et de modifier ses réglages.

1. Téléchargez **123Scan** depuis le site d'assistance de Zebra et lancez l'installateur.
2. Branchez le lecteur Zebra et démarrez 123Scan. Il doit trouver le lecteur et afficher son
   modèle, son numéro de série et la version actuelle de son micrologiciel. Sinon, essayez un autre
   port USB ou un autre câble avant d'aller plus loin — GAIA Desktop ne verra pas un lecteur que
   123Scan ne voit pas.

---

## 5. Mettre à jour le micrologiciel du lecteur Zebra

Mettez le micrologiciel du lecteur à la dernière version avant de le configurer. Les mises à jour
corrigent des problèmes de décodage et gardent le lecteur en phase avec le pilote Zebra installé
par le SDK.

1. Fermez **GAIA Desktop** et tout autre programme susceptible d'utiliser le lecteur : 123Scan a
   besoin du lecteur pour lui seul pendant la mise à jour.
2. Dans 123Scan, lecteur connecté, choisissez son option de mise à jour du micrologiciel. 123Scan
   compare le micrologiciel du lecteur avec la dernière version publiée par Zebra et propose de la
   télécharger et de l'installer. Si l'ordinateur n'a pas accès à Internet, téléchargez le
   micrologiciel du lecteur depuis sa page d'assistance sur le site de Zebra et chargez le fichier
   dans 123Scan.
3. Lancez la mise à jour et **laissez le lecteur branché jusqu'à ce que 123Scan indique qu'elle est
   terminée**. Elle prend plusieurs minutes ; le lecteur redémarre à la fin. Le débrancher en cours
   de route peut le rendre inutilisable.
4. Vérifiez que 123Scan affiche maintenant la nouvelle version du micrologiciel.

Une mise à jour peut remettre certains réglages à leurs valeurs par défaut ; c'est pourquoi le
lecteur est configuré dans les deux étapes suivantes, après le micrologiciel.

---

## 6. Régler le mode USB du lecteur Zebra

C'est l'étape la plus souvent oubliée. **À la sortie de l'emballage, un lecteur Zebra se comporte
comme un clavier USB** (mode USB HID Keyboard) : il « tape » chaque code-barres dans ce qui a le
focus. Dans ce mode, *Connecter le lecteur* de GAIA Desktop ne peut pas le trouver, et tout ce qui est
tapé dans un champ de texte perd ses séparateurs FNC1, ce qui change le sens d'une chaîne
d'éléments GS1.

Passez le lecteur dans un mode avec lequel le pilote Zebra dialogue directement :

| Mode USB du lecteur | Nom d'appareil dans GAIA Desktop | Remarques |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`, ou `ZebraAllScanners` | Recommandé |
| SNAPI (avec ou sans imagerie) | `ZebraScannerSNAPI`, ou `ZebraAllScanners` | Le protocole USB propre à Zebra ; fonctionne aussi |
| IBM Table-top USB | `ZebraUSBTableTop`, ou `ZebraAllScanners` | Prévu pour les lecteurs de comptoir |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`, ou `ZebraAllScanners` | |
| USB HID Keyboard (réglage d'usine) | — | **Ne se connectera pas** |

`ZebraAllScanners`, le nom d'appareil par défaut de GAIA Desktop, accepte un lecteur dans
n'importe lequel des modes pris en charge : une fois le mode réglé, la valeur par défaut suffit
généralement.

Pour changer de mode, au choix :

- **lisez le code-barres de configuration** du mode — *IBM Hand-held USB*, par exemple — dans le
  Quick Reference Guide ou le Product Reference Guide du lecteur (section USB Host Types / USB
  Device Type) ; le lecteur bipe pour confirmer. Ou
- **utilisez 123Scan** ([étape 4](#4-installer-123scan)) : connectez le lecteur, ouvrez sa
  configuration et réglez le type d'hôte USB.

Après un changement de mode, Windows redétecte le lecteur comme un autre type d'appareil ;
laissez-lui quelques secondes avant de vous connecter.

Pour remettre plus tard le lecteur en mode clavier — pour un autre logiciel — lisez le
code-barres de configuration *USB Keyboard (HID)*, ou rétablissez ses réglages d'usine.

---

## 7. Configurer le lecteur Zebra pour GS1

GAIA Desktop analyse au mieux quand le lecteur lui indique quel type de code-barres il a lu.

### Transmettre l'AIM Code ID

Réglez **Transmit Code ID Character** sur **AIM Code ID Character**, avec le code-barres de
configuration du Product Reference Guide (User Preferences) ou dans 123Scan. Chaque lecture
commence alors par son identifiant de symbologie — `]C1` pour GS1-128, `]d2` pour GS1 DataMatrix,
`]Q3` pour GS1 QR Code, `]e0` pour GS1 DataBar — dont GAIA Desktop se sert pour nommer le support
de données et lui appliquer ses règles.

Sans lui, GAIA Desktop analyse quand même la chaîne d'éléments et tire la symbologie de la lecture
elle-même, mais le rapport n'a pas d'AIM Code ID à afficher, et un code-barres simple comme un
EAN-13 se distingue moins facilement d'une chaîne d'éléments GS1.

### Vérifier les symbologies

Assurez-vous que les types de codes-barres que vous utilisez sont **activés** : en général
GS1-128, GS1 DataMatrix, GS1 DataBar, GS1 QR Code et EAN/UPC. La plupart le sont par défaut sur
les lecteurs 2D Zebra ; 123Scan affiche les réglages actuels, et le Product Reference Guide
contient un code-barres pour activer chacun d'eux.

### Laisser désactivées les fonctions d'analyse GS1 de Zebra

Les micrologiciels récents ajoutent des fonctions GS1 qui travaillent dans le lecteur lui-même —
**Label Parse+**, **GS1 Digital Link** et **GS1 Retail POS parsing**. Laissez-les
**désactivées**, comme à la livraison : elles reformatent ou raccourcissent ce que le lecteur
envoie, et GAIA Desktop a besoin des données brutes pour les analyser et les valider. Voir
[Lecteurs 2D Zebra pris en charge](#12-lecteurs-2d-zebra-pris-en-charge) pour le rôle de chacune.

### Ne pas ajouter de préfixe ni de suffixe

N'ajoutez ni préfixe, ni suffixe, ni touche Entrée à la sortie du lecteur. GAIA Desktop reçoit
chaque lecture comme un message complet par JavaPOS et n'en a pas besoin ; des caractères en trop
se retrouveraient dans la chaîne d'éléments.

---

## 8. Installer et lancer GAIA Desktop

1. Décompressez `gaia-desktop-1.0.0-win.zip`, par exemple dans `C:\Tools\`. Il contient un seul
   dossier :

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Branchez le lecteur.
3. Double-cliquez sur **`run.bat`**. Une fenêtre de console s'ouvre à côté de l'application et
   reste ouverte : l'état du lecteur et les erreurs JavaPOS y sont signalés.
4. Lisez la première ligne de la console. Elle indique ce que `run.bat` a trouvé :

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Si elle se termine par **`Zebra services from none (Zebra Scanner SDK not installed)`**, le SDK
   n'a pas été trouvé à son emplacement par défaut — installez-le
   ([étape 3](#3-installer-le-zebra-scanner-sdk)) ou définissez [ZEBRA_HOME](#zebra_home).
   L'application démarre quand même, mais le lecteur ne peut pas se connecter.

---

## 9. Connecter le lecteur

1. Dans la barre d'outils, le **sélecteur d'appareil** liste les noms de lecteurs que connaît
   GAIA Desktop. Laissez-le sur **`ZebraAllScanners`**, ou choisissez le nom correspondant au mode
   USB du lecteur dans le tableau de l'[étape 6](#6-régler-le-mode-usb-du-lecteur-zebra).
2. Cliquez sur **Connecter le lecteur**, ou appuyez sur **Ctrl+K** (également dans le menu
   **Lecteur**).
3. La barre d'état passe à **`lecteur : ZebraAllScanners`** (ou le nom que vous avez
   choisi), et la ligne d'état indique *Lecteur connecté — ZebraAllScanners*. Le bouton
   affiche maintenant **Déconnecter le lecteur**.
4. Lisez un code-barres GS1. Il apparaît en haut de l'historique des captures et son rapport
   s'ouvre. Avec l'AIM Code ID activé, le rapport nomme le support de données — *GS1-128*,
   *GS1 DataMatrix* — et la chaîne d'éléments garde chaque séparateur FNC1.

Le sélecteur d'appareil retient votre choix d'une session à l'autre. Le lecteur reste connecté
jusqu'à ce que vous le déconnectiez ou fermiez l'application. Les lectures fonctionnent aussi dans
les modes **Composer** et **Combiner** : en mode Combiner, chaque lecture
devient l'un des codes-barres combinés.

Tant que GAIA Desktop tient le lecteur, les autres programmes ne peuvent pas l'utiliser, et
inversement : fermez 123Scan et tout autre logiciel de caisse ou de lecture avant de vous
connecter.

---

## 10. Référence de configuration

### Noms d'appareil

Le sélecteur d'appareil est rempli à partir du `jpos.xml` embarqué dans `gaia-desktop.jar`. Ses
entrées de lecteur :

| Nom d'appareil | Type de lecteur | À utiliser avec |
|---|---|---|
| `ZebraAllScanners` | tous | **Par défaut.** Tout mode USB pris en charge |
| `ZebraAllScanners114` | tous | La version JavaPOS 1.14 du même service |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Un lecteur en mode IBM Hand-held USB |
| `ZebraUSBTableTop` | IBM Table-top USB | Mode comptoir (table-top) |
| `ZebraUSBOPOS` | USB OPOS | Mode USB OPOS |
| `ZebraScannerSNAPI` | SNAPI | Mode SNAPI |
| `ZebraScannerUSB` | tout USB | Tout mode USB |
| `ZebraScannerSerialSSI` | série SSI | Lecteurs à câble série (pas USB) |
| `ZebraScannerSerialNIXMODB` | série NIXMODB | Lecteurs à câble série |
| `ZebraVDI` | VDI | Lecteurs redirigés vers un bureau virtuel |

### ZEBRA_HOME

Si le SDK Zebra est installé ailleurs qu'à l'emplacement par défaut, définissez `ZEBRA_HOME` sur
son dossier `JPOS` avant de lancer `run.bat` — par exemple dans un petit fichier de commandes
placé à côté :

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

`run.bat` prend alors les jars de service de Zebra dans `%ZEBRA_HOME%bin` et ajoute ce dossier au
`PATH` pour les DLL Zebra.

### Votre propre jpos.xml

Pour ajouter un nom d'appareil ou modifier les réglages d'une entrée, copiez `jpos.xml` hors de
`gaia-desktop.jar` (c'est un zip ; n'importe quel outil de décompression l'ouvre), modifiez la
copie, et indiquez-la à GAIA Desktop :

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

Le sélecteur d'appareil liste alors les lecteurs de ce fichier. Avec plusieurs lecteurs Zebra
branchés, une entrée peut être liée à l'un d'eux par sa propriété `ScannerModel` ou
`SerialNumber`, comme dans l'entrée `ZebraAllScanners`.

### Autres options

| Réglage | Comment |
|---|---|
| Langue de l'application | `run.bat --language=FR` (pour cette session seulement), ou le Panneau d'options |
| Options Java supplémentaires | `set GAIA_DESKTOP_OPTS=…` avant `run.bat` |
| Un Java embarqué | placez un dossier `jdk-21…` à côté de `run.bat` ; il est utilisé avant `JAVA_HOME` |

---

## 11. Dépannage

Les erreurs apparaissent dans une boîte de dialogue lors de la connexion, dans la fenêtre de
console et dans le fichier journal `logs\gaia-desktop.log`, dans le dossier d'où `run.bat` a été
lancé (le dossier de GAIA Desktop, quand on double-clique sur `run.bat`). Le nombre entre crochets
dans le journal est le code d'erreur JavaPOS.

| Ce que vous voyez | Cause probable | Que faire |
|---|---|---|
| Il n'y a pas de bouton *Connecter le lecteur* | L'application ne tourne pas sous Windows | La lecture n'existe que sous Windows |
| 123Scan ne trouve pas le lecteur | Câble, port USB, ou lecteur retenu par un autre programme | Essayez un autre câble ou port ; fermez GAIA Desktop et les autres logiciels de lecture. GAIA Desktop ne verra pas un lecteur que 123Scan ne voit pas |
| Console : `Zebra services from none (Zebra Scanner SDK not installed)` | Le SDK n'est pas à son emplacement par défaut | Installez le SDK 64 bits ([étape 3](#3-installer-le-zebra-scanner-sdk)), ou définissez [ZEBRA_HOME](#zebra_home) |
| Erreur **104** : *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Les jars de service de Zebra sont introuvables | Comme ci-dessus — vérifiez la ligne `Zebra services from` de la console |
| Erreur **109** : *Service does not exist in loaded JCL registry* | Le nom d'appareil n'est pas dans la configuration du lecteur, ou le `javaPOS114.jar` du SDK s'est retrouvé dans le class path | Choisissez un nom dans le sélecteur d'appareil ; n'ajoutez pas `javaPOS114.jar` ; si vous utilisez votre propre `jpos.xml`, vérifiez que le nom y figure |
| Erreur **107** (pas de matériel), ou la connexion échoue avec le lecteur branché | Le lecteur est encore en mode clavier USB, ou le pilote CoreScanner ne tourne pas | Réglez le mode USB ([étape 6](#6-régler-le-mode-usb-du-lecteur-zebra)) ; redémarrez Windows après l'installation du SDK ; essayez un autre port USB |
| Erreur **112** (délai dépassé) ou **102** (réservé) à la connexion | Un autre programme tient le lecteur | Fermez 123Scan et tout autre logiciel de lecture ou de caisse, puis reconnectez-vous |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` dans la console | Les DLL Zebra ne peuvent pas être chargées — souvent un SDK 32 bits avec un Java 64 bits | Installez le SDK 64 bits ; vérifiez que `java -version` signale une VM 64 bits |
| Les lectures se tapent dans la barre de saisie au lieu d'arriver comme captures | Le lecteur est en mode clavier USB | Réglez le mode USB ([étape 6](#6-régler-le-mode-usb-du-lecteur-zebra)) |
| Connecté, mais les lectures n'arrivent pas | Mauvais nom d'appareil pour le mode du lecteur, ou lecteur rebranché pendant la connexion | Déconnectez, choisissez `ZebraAllScanners`, reconnectez |
| Le rapport n'affiche ni AIM Code ID ni support de données | La transmission de l'AIM Code ID est désactivée | Activez *Transmit Code ID Character → AIM Code ID* ([étape 7](#7-configurer-le-lecteur-zebra-pour-gs1)) |
| Une chaîne d'éléments a perdu ses séparateurs, ou des AI se suivent sans séparation | Lu en mode clavier, ou tapé à la main sans `{GS}` | Utilisez un mode USB JavaPOS ; à la saisie, insérez les séparateurs avec le bouton `{GS}` |
| Un type de code-barres n'est jamais lu | Cette symbologie est désactivée sur le lecteur | Activez-la ([étape 7](#7-configurer-le-lecteur-zebra-pour-gs1)) |

Si un problème persiste, gardez ensemble la ligne `run.bat:` de la console, le texte de la boîte
de dialogue d'erreur et `logs\gaia-desktop.log` : à eux trois, ils montrent quel Java, quels
services Zebra et quel nom d'appareil ont été utilisés, et où exactement la connexion a échoué.

---

## 12. Lecteurs 2D Zebra pris en charge

GAIA Desktop a besoin d'un lecteur capable de lire les codes-barres dans lesquels les données GS1
sont imprimées — GS1-128, GS1 DataBar, EAN/UPC et, pour la plupart des usages, les codes 2D :
**GS1 DataMatrix** et **GS1 QR Code**, qui est aussi la forme habituelle d'une URI **GS1 Digital
Link**. Il faut donc un **imageur 2D** Zebra : un lecteur linéaire (1D) ne lit pas du tout les
codes DataMatrix ni QR.

Le lecteur n'a pas besoin de comprendre lui-même les Application Identifiers GS1 ni le Digital
Link. **GAIA Desktop analyse les deux** : il prend la lecture telle que le lecteur l'a lue —
AIM Code ID, séparateurs FNC1 compris — et la découpe en AI, ou convertit lui-même une URI Digital
Link en ses AI.

### Testé

| Lecteur | Type | État |
|---|---|---|
| **DS8108** | Imageur 2D portable filaire | **Développé et testé** avec GAIA Desktop |

### Devrait fonctionner

Ce sont des imageurs 2D Zebra qui, comme le DS8108, sont pilotés par le Scanner SDK et les
services JavaPOS de Zebra. Ils **n'ont pas été testés** avec GAIA Desktop ; configurez chacun
comme dans ce guide, avec son propre Quick Reference Guide ou Product Reference Guide pour les
codes-barres de configuration.

| Famille | Modèles (exemples) |
|---|---|
| DS81x8 | DS8108 (filaire), DS8178 (sans fil) |
| DS22x8 | DS2208 (filaire), DS2278 (sans fil) — usage général |
| DS36x8 | DS3608 (filaire), DS3678 (sans fil) — durcis, y compris les variantes KD et XR |
| DS46x8 | DS4608 (filaire), DS4678 (sans fil) |
| DS9308 / DS9908 | Lecteurs de présentation et mains libres |
| MP7000 / MP7200 | Lecteurs bioptiques multiplans et lecteurs-balances |
| CS6080 | Lecteur compagnon sans fil |

Un lecteur sans fil dialogue avec sa base, et c'est la base qui se branche sur l'ordinateur :
connectez la base en USB et traitez la paire comme un seul lecteur. Des lecteurs d'autres familles
Zebra peuvent fonctionner aussi, si le Scanner SDK de Zebra les prend en charge dans l'un des modes
USB de l'[étape 6](#6-régler-le-mode-usb-du-lecteur-zebra).

### Fonctions GS1 intégrées aux lecteurs Zebra

Indépendamment de la lecture des codes-barres, certains imageurs 2D Zebra peuvent traiter les
données GS1 dans le lecteur, avec une configuration par 123Scan :

| Fonction | Ce qu'elle fait | Pour GAIA Desktop |
|---|---|---|
| **Label Parse+** | Analyse les AI d'une étiquette GS1 dans le lecteur et envoie des champs reformatés | **Laisser désactivée** — GAIA Desktop ne verrait plus les AI, les séparateurs FNC1 ni l'AIM Code ID |
| **GS1 Digital Link** | Pour les produits de distribution marqués à la fois d'un EAN/UPC 1D et d'un code Digital Link 2D : choisit lequel envoyer | **Laisser désactivée** (par défaut) |
| **GS1 Retail POS parsing** | Lit les codes-barres 2D de distribution en syntaxe URI Digital Link ou chaîne d'éléments | **Laisser désactivée** |

Le réglage **GS1 Digital Link** a trois modes. **Mode 1 — GTIN only**, le mode actif dès qu'il est
activé, n'envoie que le GTIN, quel que soit le code-barres d'où il provient : un lot, une date de
péremption ou un numéro de série du Digital Link n'atteindrait donc jamais GAIA Desktop.
**Mode 2 — Prioritize 2D** envoie le code 2D s'il est vu dans le délai de priorité (200 ms par
défaut), sinon le 1D. **Mode 3 — Corroborate 1D and 2D GTINs Match** envoie le 1D, et le 2D aussi
quand son GTIN correspond. Si le réglage doit rester actif pour un autre logiciel, utilisez le
Mode 2, pas le Mode 1. Sur le DS8108, ce sont les paramètres 2373 (activé/désactivé), 2374 (mode)
et 2491 (délai).

Ces fonctions nécessitent **123Scan version 6 ou plus récente** et un micrologiciel qui les
inclut. Zebra ajoute la prise en charge de GS1 Digital Link par mise à jour du micrologiciel depuis
avril 2025 ; les dates ci-dessous proviennent d'un revendeur Zebra, vérifiez donc les notes de
version du micrologiciel de chaque modèle :

| Famille de modèles | Micrologiciel GS1 Digital Link publié |
|---|---|
| DS81x8 | 16 avril 2025 |
| MP7000 | 21 avril 2025 |
| DS9908 | 6 juin 2025 |
| SP72 / SM72 | 11 juillet 2025 |
| DS46x8 | 28 juillet 2025 |
| MP7200 (Rev-C), DS5502 | 30 juillet 2025 |
| DS9308 | 29 août 2025 |
| DS22x8, DS1000 | 5 septembre 2025 (Asie-Pacifique uniquement) |
| DS36x8 (y compris KD et XR), MS47 | 30 septembre 2025 |
| PL5000, SE4107 | 15 octobre 2025 |
| CS6080 | 31 décembre 2025 |

Aucune de ces fonctions n'est nécessaire pour lire des codes GS1 Digital Link dans GAIA Desktop :
quand elles sont désactivées, un lecteur 2D Zebra au micrologiciel à jour lit un code QR Digital
Link comme n'importe quel autre, et GAIA Desktop convertit l'URI en ses AI.

Sources : Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (août 2025), chapitre 12
« GS1 Digital Link Parameters » et chapitre Data Parsing ; Zebra, *123Scan release notes* ;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (dates des micrologiciels).
