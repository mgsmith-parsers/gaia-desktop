# Configuración de un lector Zebra — GAIA Desktop para Windows

Cómo instalar y configurar un lector de códigos de barras Zebra para que GAIA Desktop lea
directamente los códigos GS1: cada lectura se analiza y se añade al historial, con el prefijo
AIM Code ID y cada separador FNC1 intactos. Versión original:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

GAIA Desktop se desarrolló y probó con un generador de imágenes portátil **Zebra DS8108**
conectado por USB. Los pasos sirven para cualquier lector 2D de Zebra compatible con el Scanner SDK
de Zebra; los códigos de barras de configuración y los nombres de menú de un modelo concreto están
en su propia Quick Reference Guide o Product Reference Guide.

> **Solo Windows.** Los controladores de los lectores Zebra solo existen para Windows, así que en
> macOS o Linux GAIA Desktop no crea ningún control de lector. Todo lo demás de la aplicación
> funciona en cualquier plataforma.

---

## Índice

1. [Lo que necesita](#1-lo-que-necesita)
2. [Instalar Java](#2-instalar-java)
3. [Instalar el Zebra Scanner SDK](#3-instalar-el-zebra-scanner-sdk)
4. [Instalar 123Scan](#4-instalar-123scan)
5. [Actualizar el firmware del lector Zebra](#5-actualizar-el-firmware-del-lector-zebra)
6. [Configurar el modo USB del lector Zebra](#6-configurar-el-modo-usb-del-lector-zebra)
7. [Configurar el lector Zebra para GS1](#7-configurar-el-lector-zebra-para-gs1)
8. [Instalar e iniciar GAIA Desktop](#8-instalar-e-iniciar-gaia-desktop)
9. [Conectar el lector](#9-conectar-el-lector)
10. [Referencia de configuración](#10-referencia-de-configuración)
11. [Solución de problemas](#11-solución-de-problemas)
12. [Lectores 2D de Zebra compatibles](#12-lectores-2d-de-zebra-compatibles)

---

## 1. Lo que necesita

| | |
|---|---|
| **Lector** | Un lector 2D de Zebra, con su cable USB — consulte [Lectores 2D de Zebra compatibles](#12-lectores-2d-de-zebra-compatibles) |
| **Ordenador** | Windows 10 u 11, 64 bits |
| **Java** | Java 21 o posterior, **64 bits** |
| **Zebra Scanner SDK para Windows** | **64 bits**, desde el sitio de soporte de Zebra — proporciona el controlador y los servicios JavaPOS que usa GAIA Desktop |
| **Zebra 123Scan** | La utilidad de configuración gratuita de Zebra, desde su sitio de soporte — sirve para actualizar el firmware del lector y comprobar su configuración |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Opcional** | La **Product Reference Guide** (PRG) del lector, que contiene los códigos de barras de configuración |

Cómo encajan las piezas:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

GAIA Desktop incluye la propia API JavaPOS y la configuración que da nombre al lector
(`jpos.xml`, dentro de `gaia-desktop.jar`). El SDK de Zebra aporta el resto: el controlador que
se comunica con el lector y las clases de servicio de Zebra situadas entre JavaPOS y ese
controlador.

---

## 2. Instalar Java

GAIA Desktop necesita **Java 21 o posterior**, en versión de **64 bits**, para coincidir con los
controladores de Zebra de 64 bits.

1. Instale un JDK o JRE de Java 21 para Windows x64 — por ejemplo Eclipse Temurin, de
   adoptium.net. Deje que el instalador defina `JAVA_HOME` si lo ofrece.
2. Compruébelo en un símbolo del sistema:

   ```
   java -version
   ```

   La primera línea debe mostrar la versión 21 o posterior; un Java de 64 bits también muestra
   `64-Bit Server VM`.

`run.bat` busca Java en este orden: una carpeta `jdk*` junto a `run.bat`, después `JAVA_HOME` y
después `java` en el `PATH`. Rechaza cualquier versión anterior a la 21 con un mensaje que lo
indica.

---

## 3. Instalar el Zebra Scanner SDK

1. Descargue el **Zebra Scanner SDK para Windows (64 bits)** desde el sitio de soporte de Zebra.
2. Ejecute el instalador. Si ofrece elegir componentes, asegúrese de incluir el componente
   **JPOS** (JavaPOS).
3. Mantenga la ubicación de instalación predeterminada. GAIA Desktop busca la parte JavaPOS del
   SDK en:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   y espera los jar de servicio de Zebra (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`,
   …) y las bibliotecas nativas (`CSJPOSScanner64.dll`, …) en su carpeta `bin\`. Si el SDK está
   instalado en otro lugar, consulte [ZEBRA_HOME](#zebra_home).
4. **Reinicie Windows** después de la instalación. El controlador CoreScanner del SDK se ejecuta en
   segundo plano y debe estar activo antes de que GAIA Desktop pueda acceder al lector; reiniciar
   es la forma más sencilla de asegurarse.

---

## 4. Instalar 123Scan

123Scan es la utilidad gratuita de Zebra para configurar sus lectores. Se usa en el paso
siguiente para actualizar el firmware del lector, y después es la forma más sencilla de comprobar y
cambiar su configuración.

1. Descargue **123Scan** desde el sitio de soporte de Zebra y ejecute el instalador.
2. Conecte el lector Zebra e inicie 123Scan. Debería encontrar el lector y mostrar su modelo, su
   número de serie y la versión actual de su firmware. Si no lo hace, pruebe otro puerto USB u otro
   cable antes de continuar — GAIA Desktop no verá un lector que 123Scan no ve.

---

## 5. Actualizar el firmware del lector Zebra

Actualice el firmware del lector a la última versión antes de configurarlo. Las actualizaciones
corrigen problemas de decodificación y mantienen el lector en sintonía con el controlador de Zebra
instalado por el SDK.

1. Cierre **GAIA Desktop** y cualquier otro programa que pueda estar usando el lector: 123Scan
   necesita el lector en exclusiva durante la actualización.
2. En 123Scan, con el lector conectado, elija su opción de actualización de firmware. 123Scan
   compara el firmware del lector con la última versión publicada por Zebra y ofrece descargarla e
   instalarla. Si el ordenador no tiene acceso a Internet, descargue el firmware del lector desde su
   página de soporte en el sitio de Zebra y cargue el archivo en 123Scan.
3. Inicie la actualización y **deje el lector conectado hasta que 123Scan indique que ha
   terminado**. Tarda varios minutos; el lector se reinicia al final. Desconectarlo a mitad de
   proceso puede dejarlo inservible.
4. Compruebe que 123Scan muestra ahora la nueva versión del firmware.

Una actualización puede devolver algunos ajustes a sus valores predeterminados; por eso el lector
se configura en los dos pasos siguientes, después del firmware.

---

## 6. Configurar el modo USB del lector Zebra

Es el paso que más se olvida. **De fábrica, un lector Zebra se comporta como un teclado USB** (modo
USB HID Keyboard): «escribe» cada código de barras donde esté el foco. En ese modo,
*Conectar lector* de GAIA Desktop no puede encontrarlo, y todo lo que se escribe en un campo de
texto pierde sus separadores FNC1, lo que cambia el significado de una cadena de elementos GS1.

Cambie el lector a un modo con el que el controlador de Zebra se comunique directamente:

| Modo USB del lector | Nombre de dispositivo en GAIA Desktop | Notas |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`, o `ZebraAllScanners` | Recomendado |
| SNAPI (con o sin imagen) | `ZebraScannerSNAPI`, o `ZebraAllScanners` | El protocolo USB propio de Zebra; también funciona |
| IBM Table-top USB | `ZebraUSBTableTop`, o `ZebraAllScanners` | Pensado para lectores de sobremesa |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`, o `ZebraAllScanners` | |
| USB HID Keyboard (valor de fábrica) | — | **No se conectará** |

`ZebraAllScanners`, el nombre de dispositivo predeterminado de GAIA Desktop, acepta un lector en
cualquiera de los modos compatibles, así que, una vez configurado el modo, el valor predeterminado
suele bastar.

Para cambiar el modo, haga una de estas dos cosas:

- **lea el código de barras de configuración** del modo — *IBM Hand-held USB*, por ejemplo — en la
  Quick Reference Guide o la Product Reference Guide del lector (sección USB Host Types / USB Device
  Type); el lector pita para confirmarlo. O
- **use 123Scan** ([paso 4](#4-instalar-123scan)): conecte el lector, abra su configuración y
  defina el tipo de host USB.

Tras cambiar el modo, Windows vuelve a detectar el lector como otro tipo de dispositivo; espere
unos segundos antes de conectarlo.

Para devolver más adelante el lector al modo teclado — para otro software — lea el código de
barras de configuración *USB Keyboard (HID)*, o restablezca sus valores de fábrica.

---

## 7. Configurar el lector Zebra para GS1

GAIA Desktop analiza mejor cuando el lector le indica qué tipo de código de barras ha leído.

### Transmitir el AIM Code ID

Configure **Transmit Code ID Character** como **AIM Code ID Character**, con el código de barras de
configuración de la Product Reference Guide (User Preferences) o en 123Scan. Cada lectura empieza
entonces con su identificador de simbología — `]C1` para GS1-128, `]d2` para GS1 DataMatrix, `]Q3`
para GS1 QR Code, `]e0` para GS1 DataBar —, que GAIA Desktop usa para identificar el portador de
datos y aplicarle sus reglas.

Sin él, GAIA Desktop sigue analizando la cadena de elementos y toma la simbología de la propia
lectura, pero el informe no tiene AIM Code ID que mostrar, y un código de barras sencillo como un
EAN-13 se distingue peor de una cadena de elementos GS1.

### Comprobar las simbologías

Asegúrese de que los tipos de código de barras que usa están **activados**: normalmente GS1-128,
GS1 DataMatrix, GS1 DataBar, GS1 QR Code y EAN/UPC. La mayoría vienen activados de serie en los
lectores 2D de Zebra; 123Scan muestra la configuración actual, y la Product Reference Guide tiene un
código de barras para activar cada uno.

### Dejar desactivadas las funciones de análisis GS1 de Zebra

El firmware reciente añade funciones GS1 que trabajan en el propio lector — **Label Parse+**,
**GS1 Digital Link** y **GS1 Retail POS parsing**. Déjelas **desactivadas**, como vienen de
fábrica: reformatean o acortan lo que envía el lector, y GAIA Desktop necesita los datos sin
procesar para analizarlos y validarlos. Consulte
[Lectores 2D de Zebra compatibles](#12-lectores-2d-de-zebra-compatibles) para saber qué hace cada una.

### No añadir prefijos ni sufijos

No añada prefijo, sufijo ni tecla Intro a la salida del lector. GAIA Desktop recibe cada lectura
como un mensaje completo a través de JavaPOS y no necesita ninguno; los caracteres de más acabarían
en la cadena de elementos.

---

## 8. Instalar e iniciar GAIA Desktop

1. Descomprima `gaia-desktop-1.0.0-win.zip`, por ejemplo en `C:\Tools\`. Contiene una sola
   carpeta:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Conecte el lector.
3. Haga doble clic en **`run.bat`**. Se abre una ventana de consola junto a la aplicación y
   permanece abierta: el estado del lector y los errores de JavaPOS se muestran allí.
4. Lea la primera línea de la consola. Indica lo que ha encontrado `run.bat`:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Si termina en **`Zebra services from none (Zebra Scanner SDK not installed)`**, el SDK no se ha
   encontrado en su ubicación predeterminada — instálelo ([paso 3](#3-instalar-el-zebra-scanner-sdk))
   o defina [ZEBRA_HOME](#zebra_home). La aplicación se inicia igualmente, pero el lector no puede
   conectarse.

---

## 9. Conectar el lector

1. En la barra de herramientas, el **selector de dispositivo** muestra los nombres de lector que
   conoce GAIA Desktop. Déjelo en **`ZebraAllScanners`**, o elija el nombre que corresponda al modo
   USB del lector en la tabla del [paso 6](#6-configurar-el-modo-usb-del-lector-zebra).
2. Haga clic en **Conectar lector**, o pulse **Ctrl+K** (también en el menú **Lector**).
3. La barra de estado cambia a **`lector: ZebraAllScanners`** (o el nombre elegido), y la
   línea de estado indica *Lector conectado — ZebraAllScanners*. El botón muestra ahora
   **Desconectar lector**.
4. Lea un código de barras GS1. Aparece al principio del historial de capturas y se abre su
   informe. Con el AIM Code ID activado, el informe nombra el portador de datos — *GS1-128*,
   *GS1 DataMatrix* — y la cadena de elementos conserva cada separador FNC1.

El selector de dispositivo recuerda su elección entre sesiones. El lector sigue conectado hasta que
lo desconecte o cierre la aplicación. Las lecturas también funcionan en los modos
**Componer** y **Combinar**: en Combinar, cada lectura pasa a ser uno de los
códigos de barras que se combinan.

Mientras GAIA Desktop tiene el lector, los demás programas no pueden usarlo, y viceversa: cierre
123Scan y cualquier otro software de punto de venta o de lectura antes de conectar.

---

## 10. Referencia de configuración

### Nombres de dispositivo

El selector de dispositivo se rellena a partir del `jpos.xml` incluido en `gaia-desktop.jar`. Sus
entradas de lector:

| Nombre de dispositivo | Tipo de lector | Uso |
|---|---|---|
| `ZebraAllScanners` | cualquiera | **Predeterminado.** Cualquier modo USB compatible |
| `ZebraAllScanners114` | cualquiera | La versión JavaPOS 1.14 del mismo servicio |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Un lector en modo IBM Hand-held USB |
| `ZebraUSBTableTop` | IBM Table-top USB | Modo de sobremesa |
| `ZebraUSBOPOS` | USB OPOS | Modo USB OPOS |
| `ZebraScannerSNAPI` | SNAPI | Modo SNAPI |
| `ZebraScannerUSB` | cualquier USB | Cualquier modo USB |
| `ZebraScannerSerialSSI` | serie SSI | Lectores con cable serie (no USB) |
| `ZebraScannerSerialNIXMODB` | serie NIXMODB | Lectores con cable serie |
| `ZebraVDI` | VDI | Lectores redirigidos a un escritorio virtual |

### ZEBRA_HOME

Si el SDK de Zebra está instalado en otro lugar distinto del predeterminado, defina `ZEBRA_HOME`
con su carpeta `JPOS` antes de iniciar `run.bat` — por ejemplo en un pequeño archivo por lotes
junto a él:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

`run.bat` toma entonces los jar de servicio de Zebra de `%ZEBRA_HOME%bin` y añade esa carpeta al
`PATH` para las DLL de Zebra.

### Su propio jpos.xml

Para añadir un nombre de dispositivo o cambiar la configuración de una entrada, copie `jpos.xml`
fuera de `gaia-desktop.jar` (es un zip; cualquier herramienta de descompresión lo abre), edite la
copia e indíquesela a GAIA Desktop:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

El selector de dispositivo muestra entonces los lectores de ese archivo. Con más de un lector Zebra
conectado, una entrada puede vincularse a uno de ellos mediante su propiedad `ScannerModel` o
`SerialNumber`, como en la entrada `ZebraAllScanners`.

### Otras opciones

| Ajuste | Cómo |
|---|---|
| Idioma de la aplicación | `run.bat --language=FR` (solo para esta sesión), o el Panel de opciones |
| Opciones de Java adicionales | `set GAIA_DESKTOP_OPTS=…` antes de `run.bat` |
| Un Java incluido | coloque una carpeta `jdk-21…` junto a `run.bat`; se usa antes que `JAVA_HOME` |

---

## 11. Solución de problemas

Los errores aparecen en un cuadro de diálogo al conectar, en la ventana de consola y en el archivo
de registro `logs\gaia-desktop.log`, en la carpeta desde la que se inició `run.bat` (la carpeta de
GAIA Desktop, si se hizo doble clic en `run.bat`). El número entre corchetes del registro es el
código de error de JavaPOS.

| Lo que ve | Causa probable | Qué hacer |
|---|---|---|
| No hay botón *Conectar lector* | No se ejecuta en Windows | La lectura solo existe en Windows |
| 123Scan no encuentra el lector | Cable, puerto USB, u otro programa retiene el lector | Pruebe otro cable o puerto; cierre GAIA Desktop y otros programas de lectura. GAIA Desktop no verá un lector que 123Scan no ve |
| Consola: `Zebra services from none (Zebra Scanner SDK not installed)` | El SDK no está en su ubicación predeterminada | Instale el SDK de 64 bits ([paso 3](#3-instalar-el-zebra-scanner-sdk)), o defina [ZEBRA_HOME](#zebra_home) |
| Error **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | No se encontraron los jar de servicio de Zebra | Como arriba — compruebe la línea `Zebra services from` de la consola |
| Error **109**: *Service does not exist in loaded JCL registry* | El nombre de dispositivo no está en la configuración del lector, o el `javaPOS114.jar` del SDK acabó en el class path | Elija un nombre del selector de dispositivo; no añada `javaPOS114.jar`; si usa su propio `jpos.xml`, compruebe que el nombre está en él |
| Error **107** (sin hardware), o la conexión falla con el lector conectado | El lector sigue en modo teclado USB, o el controlador CoreScanner no está en marcha | Configure el modo USB ([paso 6](#6-configurar-el-modo-usb-del-lector-zebra)); reinicie Windows tras instalar el SDK; pruebe otro puerto USB |
| Error **112** (tiempo de espera) o **102** (reclamado) al conectar | Otro programa tiene el lector | Cierre 123Scan y cualquier otro software de lectura o de punto de venta, y vuelva a conectar |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` en la consola | No se pueden cargar las DLL de Zebra — a menudo un SDK de 32 bits con un Java de 64 bits | Instale el SDK de 64 bits; compruebe que `java -version` indica una VM de 64 bits |
| Las lecturas se escriben en la barra de entrada en lugar de llegar como capturas | El lector está en modo teclado USB | Configure el modo USB ([paso 6](#6-configurar-el-modo-usb-del-lector-zebra)) |
| Conectado, pero las lecturas no llegan | Nombre de dispositivo incorrecto para el modo del lector, o el lector se volvió a enchufar estando conectado | Desconecte, elija `ZebraAllScanners` y vuelva a conectar |
| El informe no muestra AIM Code ID ni portador de datos | La transmisión del AIM Code ID está desactivada | Active *Transmit Code ID Character → AIM Code ID* ([paso 7](#7-configurar-el-lector-zebra-para-gs1)) |
| A una cadena de elementos le faltan los separadores, o los AI aparecen pegados | Se leyó en modo teclado, o se escribió a mano sin `{GS}` | Use un modo USB de JavaPOS; al escribir, inserte los separadores con el botón `{GS}` |
| Un tipo de código de barras nunca se lee | Esa simbología está desactivada en el lector | Actívela ([paso 7](#7-configurar-el-lector-zebra-para-gs1)) |

Si un problema persiste, conserve juntos la línea `run.bat:` de la consola, el texto del cuadro de
error y `logs\gaia-desktop.log`: entre los tres muestran qué Java, qué servicios de Zebra y qué
nombre de dispositivo se usaron, y dónde exactamente falló la conexión.

---

## 12. Lectores 2D de Zebra compatibles

GAIA Desktop necesita un lector capaz de leer los códigos de barras en los que se imprimen los
datos GS1 — GS1-128, GS1 DataBar, EAN/UPC y, para la mayoría de los usos, los códigos 2D:
**GS1 DataMatrix** y **GS1 QR Code**, que es también la forma habitual de imprimir una URI **GS1
Digital Link**. Eso significa un **generador de imágenes 2D** de Zebra: un lector lineal (1D) no
puede leer en absoluto códigos DataMatrix ni QR.

El lector no necesita entender por sí mismo los Application Identifiers de GS1 ni el Digital Link.
**GAIA Desktop analiza ambos**: toma la lectura tal como la leyó el lector — con AIM Code ID y
separadores FNC1 — y la divide en AI, o convierte él mismo una URI Digital Link en sus AI.

### Probado

| Lector | Tipo | Estado |
|---|---|---|
| **DS8108** | Generador de imágenes 2D portátil con cable | **Desarrollado y probado** con GAIA Desktop |

### Deberían funcionar

Son generadores de imágenes 2D de Zebra que, como el DS8108, funcionan con el Scanner SDK y los
servicios JavaPOS de Zebra. **No se han probado** con GAIA Desktop; configure cada uno como en esta
guía, con su propia Quick Reference Guide o Product Reference Guide para los códigos de barras de
configuración.

| Familia | Modelos (ejemplos) |
|---|---|
| DS81x8 | DS8108 (con cable), DS8178 (inalámbrico) |
| DS22x8 | DS2208 (con cable), DS2278 (inalámbrico) — uso general |
| DS36x8 | DS3608 (con cable), DS3678 (inalámbrico) — robustos, incluidas las variantes KD y XR |
| DS46x8 | DS4608 (con cable), DS4678 (inalámbrico) |
| DS9308 / DS9908 | Lectores de presentación y de manos libres |
| MP7000 / MP7200 | Lectores biópticos multiplano y lectores-báscula |
| CS6080 | Lector complementario inalámbrico |

Un lector inalámbrico se comunica con su base, y es la base la que se conecta al ordenador:
conecte la base por USB y trate el conjunto como un solo lector. Lectores de otras familias de
Zebra también pueden funcionar, si el Scanner SDK de Zebra los admite en alguno de los modos USB
del [paso 6](#6-configurar-el-modo-usb-del-lector-zebra).

### Funciones GS1 integradas en los lectores Zebra

Aparte de leer los códigos de barras, algunos generadores de imágenes 2D de Zebra pueden procesar
datos GS1 en el propio lector, configurados con 123Scan:

| Función | Qué hace | Para GAIA Desktop |
|---|---|---|
| **Label Parse+** | Analiza los AI de una etiqueta GS1 en el lector y envía campos reformateados | **Dejar desactivada** — GAIA Desktop ya no vería los AI, los separadores FNC1 ni el AIM Code ID |
| **GS1 Digital Link** | Para productos minoristas marcados con un EAN/UPC 1D y un código Digital Link 2D: elige cuál enviar | **Dejar desactivada** (valor predeterminado) |
| **GS1 Retail POS parsing** | Lee códigos de barras 2D minoristas en sintaxis de URI Digital Link o de cadena de elementos | **Dejar desactivada** |

El ajuste **GS1 Digital Link** tiene tres modos. **Mode 1 — GTIN only**, el modo con el que arranca
en cuanto se activa, envía solo el GTIN, venga del código de barras que venga, así que un lote, una
fecha de caducidad o un número de serie del Digital Link nunca llegarían a GAIA Desktop.
**Mode 2 — Prioritize 2D** envía el código 2D si se ve dentro del tiempo de prioridad (200 ms de
forma predeterminada), y si no, el 1D. **Mode 3 — Corroborate 1D and 2D GTINs Match** envía el 1D,
y también el 2D cuando su GTIN coincide. Si el ajuste tiene que estar activado para otro software,
use el Mode 2, no el Mode 1. En el DS8108 son los parámetros 2373 (activado/desactivado), 2374
(modo) y 2491 (tiempo de espera).

Estas funciones requieren **123Scan versión 6 o posterior** y un firmware que las incluya. Zebra
viene añadiendo compatibilidad con GS1 Digital Link mediante actualizaciones de firmware desde abril
de 2025; las fechas siguientes proceden de un distribuidor de Zebra, así que compruebe las notas de
versión del firmware de cada modelo:

| Familia de modelos | Firmware con GS1 Digital Link publicado |
|---|---|
| DS81x8 | 16 de abril de 2025 |
| MP7000 | 21 de abril de 2025 |
| DS9908 | 6 de junio de 2025 |
| SP72 / SM72 | 11 de julio de 2025 |
| DS46x8 | 28 de julio de 2025 |
| MP7200 (Rev-C), DS5502 | 30 de julio de 2025 |
| DS9308 | 29 de agosto de 2025 |
| DS22x8, DS1000 | 5 de septiembre de 2025 (solo Asia-Pacífico) |
| DS36x8 (incluidos KD y XR), MS47 | 30 de septiembre de 2025 |
| PL5000, SE4107 | 15 de octubre de 2025 |
| CS6080 | 31 de diciembre de 2025 |

Ninguna de estas funciones es necesaria para leer códigos GS1 Digital Link en GAIA Desktop: con
ellas desactivadas, un lector 2D de Zebra con el firmware actual lee un código QR Digital Link como
cualquier otro, y GAIA Desktop convierte la URI en sus AI.

Fuentes: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (agosto de 2025), capítulo 12
«GS1 Digital Link Parameters» y capítulo Data Parsing; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (fechas de firmware).
