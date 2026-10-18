# GAIA Desktop — Guía del desarrollador

Cómo se compila, organiza y prueba el módulo. Para lo que hace la aplicación vista desde fuera, consulte
la [Guía del usuario](GaiaDesktop-User-Spanish.md). Versión original:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Índice

1. [El módulo de un vistazo](#el-módulo-de-un-vistazo)
2. [Compilar y ejecutar](#compilar-y-ejecutar)
   - [Empaquetado](#empaquetado)
3. [Arquitectura](#arquitectura)
   - [El embudo de captura](#el-embudo-de-captura)
   - [Hilos](#hilos)
   - [Reconstruir o reetiquetar](#reconstruir-o-reetiquetar)
4. [El token {GS}](#el-token-gs)
5. [Integración del lector](#integración-del-lector)
   - [La barrera de plataforma](#la-barrera-de-plataforma)
   - [Simbologías](#simbologías)
6. [Localización](#localización)
   - [El argumento `--language`](#el-argumento---language)
   - [Añadir un idioma](#añadir-un-idioma)
   - [Lo que deliberadamente no se traduce](#lo-que-deliberadamente-no-se-traduce)
7. [Formatos del estado guardado](#formatos-del-estado-guardado)
8. [Pruebas](#pruebas)
9. [Solución de problemas de compilación](#solución-de-problemas-de-compilación)
10. [Limitaciones conocidas y trabajo pendiente](#limitaciones-conocidas-y-trabajo-pendiente)

---

## El módulo de un vistazo

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Un cliente de la biblioteca principal y nada más. No incorpora **ningún conocimiento de GS1**: cada
definición de AI, regla de validación, mensaje de error y descripción localizada procede de
[`GaiaParser`](../../../../gaia/docs/GaiaParser.md) y
[`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). En particular, la lista de AI nunca se copia en este
módulo — el selector del compositor lee el propio registro del núcleo, así que no puede ofrecer un AI que
el compositor rechazaría.

**Puntos de entrada:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (la `Application` de JavaFX) y
`tools.pantheum.gaia.desktop.Launcher` (la `Main-Class` del jar empaquetado).

El módulo es una hoja: nada depende de él, y él no depende de ningún otro módulo posterior.

---

## Compilar y ejecutar

- **Java 21** para ejecutar, y un JDK 21 o posterior para compilar — el módulo compila con `release=21`.
  JavaFX 21 es la línea LTS correspondiente. El jar del núcleo sigue siendo bytecode de Java 11 y se
  ejecuta sin cambios en 21.
- **Maven** en el `PATH`. No hay wrapper en este repositorio.
- **El núcleo instalado primero.** Este módulo resuelve `gaia:1.1.0` desde `~/.m2`; un jar del núcleo
  obsoleto o ausente es la causa habitual de `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` es el único argumento de línea de comandos, y la única forma de influir en el arranque aparte
de `-Dgaia.desktop.home=…`, que lleva todo el perfil a otro lugar.

`mvn javafx:run` es la vía de desarrollo: el plugin pone JavaFX en el **module path**, de modo que el
toolkit arranca como módulo con nombre y `GaiaDesktopApp` puede lanzarse directamente. Un jar empaquetado
no puede hacerlo — todo está en el class path —, y por eso su `Main-Class` es el adaptador `Launcher`. Una
subclase de `Application` como `Main-Class` en esa disposición falla con *"JavaFX runtime components are
missing"*.

Use siempre `clean` — una vez, antes de compilar un conjunto de plataformas, ya que vacía `target/dist/`
de todas ellas. Consulte [Solución de problemas de compilación](#solución-de-problemas-de-compilación)
para saber por qué.

### Empaquetado

Todo lo distribuible se reúne bajo `target/dist/gaia-desktop-<version>-<platform>/` y nada más, de modo que
el directorio puede comprimirse o copiarse como un conjunto sin tener que escoger artefactos de `target/`
junto a `classes/`, `maven-status/` y el resto de los archivos de trabajo de la compilación. `${dist.dir}`
lo nombra una sola vez; cada plugin que escribe un artefacto apunta a él.

#### Una compilación por plataforma

Los jars de JavaFX llevan código nativo, así que cada distribución es para una sola plataforma, designada
por la propiedad `fx.platform` como clasificador de OpenJFX: `mac-aarch64`, `mac`, `win`, `linux` o
`linux-aarch64`. Cinco perfiles `fx-*` activados por sistema operativo la fijan en la de la máquina que
compila, que es todo lo que necesita un simple `mvn package` o `mvn javafx:run`; `-Dfx.platform=…` tiene
prioridad sobre ellos para compilar cualquier otra, desde cualquier máquina — los jars de JavaFX son solo
descargas. `${dist.dir}` es `target/dist/${fx.platform}`, así que la compilación de una plataforma nunca
sobrescribe la de otra.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Dos detalles del pom hacen que esto funcione, y ambos parecen prescindibles:

- **Cada módulo de JavaFX es una dependencia directa** — base, graphics, controls, media y web —, cada
  uno con `<classifier>${fx.platform}</classifier>` y una exclusión `org.openjfx:*`. Los poms de OpenJFX
  eligen sus jars transitivos según el sistema operativo de la *máquina que compila*, así que dejar que
  graphics o base lleguen de forma transitiva mete los nativos de Mac en una compilación de Windows, junto
  a los de Windows.
- **La propiedad es `fx.platform`, no `javafx.platform`.** Si se define desde fuera, la propiedad propia de
  OpenJFX queda redefinida en función de sí misma por su pom padre; Maven rechaza ese pom como no válido y
  descarta en silencio los jars de plataforma, lo que deja una compilación sin JavaFX.

Las dos disposiciones escriben la misma ruta, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`,
y solo una se ejecuta en cada compilación. `${jar.phase}`, `assembly.skipAssembly` y `mdep.skip` son lo que
impide que ambas la reclamen.

| | Predeterminado | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — solo las clases y recursos de este módulo | ~50 MB — dependencias desempaquetadas dentro |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 jars, nombrados uno a uno en el `Class-Path` del manifiesto | — |
| Se distribuye | el jar **y** `lib/` a su lado | un solo archivo |
| También en `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) o `run.sh` (las demás) | lo mismo |
| Y | `readme.html` y `docs/` (todas las plataformas) | lo mismo |

Toda distribución lleva además las guías: `docs/` copiado conservando su estructura, para que los enlaces
entre guías y traducciones sigan funcionando, menos las herramientas de traducción (`check-i18n.py`,
`i18n-manifest.json`, `i18n-plan.md`); y `readme.html` en la raíz, un índice que enlaza a cada una de
ellas, a partir de `src/main/dist/readme.html` con `@VERSION@` filtrado a la versión del pom. Ambos los
escribe el paso antrun `pick-launch-script`, que primero borra `docs/` para que una guía eliminada no
perdure. Los enlaces del readme están escritos a mano — una guía renombrada o una traducción añadida bajo
`docs/` necesita también su fila allí.

El `Class-Path` del manifiesto y `lib/` son dos mitades de una misma cosa: el jar nombra
`lib/<artifact>-<version>.jar` para cada dependencia de ejecución, y `copy-dependencies` llena `lib/` con
exactamente esos nombres. Eso es lo que hace funcionar `java -jar`.

#### La aplicación de macOS y el zip de distribución

Una ejecución de antrun, `mac-app-and-zip`, cierra la fase `package`. El plugin antrun se declara
**después** de los plugins jar y assembly por esa razón — Maven ejecuta las ejecuciones de una fase en el
orden de declaración de los plugins —, así que el jar existe, en cualquiera de los dos empaquetados, cuando
se ejecuta. (Su otra ejecución, `pick-launch-script`, está en `prepare-package` y no se ve afectada.)

Para `mac` y `mac-aarch64` primero construye `GAIA Desktop.app` en el directorio de distribución:
`Info.plist` (con `@VERSION@` filtrado a la versión del pom), el lanzador `Contents/MacOS/gaia-desktop`
(hecho ejecutable) y `gaia-desktop.icns`, todo desde `src/main/macos/`, y luego **mueve**
`gaia-desktop.jar` y `lib/` a `Contents/Java`, para que el bundle sea autónomo. `run.sh` busca allí
cuando no hay ningún jar a su lado. El lanzador encuentra Java por versión (`java_home -v 21+`, después el
openjdk de Homebrew — un arranque desde Finder no ve ni `JAVA_HOME` ni `PATH`), muestra un diálogo mediante
`osascript` cuando no lo hay, hace `cd` a `~/.gaia-desktop` para que el `logs/` relativo de logback se
pueda escribir, y pasa `-Xdock:name`/`-Xdock:icon`. macOS asocia el `java` en ejecución con el bundle (su
identificador de bundle y su icono del Dock son los de la aplicación); la barra de menús muestra
GAIA Desktop.

Después, para cada plataforma, comprime el directorio en `target/dist/gaia-desktop-<version>-<platform>.zip`
bajo una carpeta del mismo nombre, con el `<zip>` de Ant: `run.sh` y `Contents/MacOS/**` como un conjunto de
archivos aparte con `filemode="755"`, o se descomprimirían sin poder ejecutarse. El zip **no se adjunta**:
es para las releases de GitHub, y las cuatro plataformas salen de cuatro compilaciones, que Maven Central —
una única subida inmutable por versión — nunca podría aceptar como una sola release.

`project.build.outputTimestamp` se fija a la hora de compilación. De lo contrario, los archivadores de
maven-jar-plugin 3.5 y maven-assembly-plugin 3.8 fechan cada entrada el 1 de febrero de 1980.

El icono de la aplicación lo dibuja `tools/icon/GaiaIcon.java` (no forma parte de la compilación; su
cabecera indica los pasos de `sips`/`iconutil`). Sus resultados están en el repositorio:
`src/main/macos/gaia-desktop.icns` para el bundle, y `src/main/resources/icons/gaia-desktop-{16..256}.png`,
que `GaiaDesktopApp` establece como icono de ventana (Windows y Linux; macOS ignora los iconos de ventana).

#### Los scripts de arranque

`run.sh` y `run.bat` vienen de `src/main/scripts/`, copiados en `dist/gaia-desktop-<version>-<platform>/`
sin filtrar — `run.sh` está lleno de expansiones de shell `${...}` que el filtrado de Maven podría
reescribir libremente. Después, un paso de antrun conserva solo el que ejecuta la plataforma — `run.bat`
para `win`, `run.sh` para todas las demás — y activa el bit de ejecución en `run.sh`, ya que
`copy-resources` no lo conserva. Decide a partir del `${fx.platform}` resuelto y no mediante perfiles,
porque la activación de un perfil no ve un valor que otro perfil haya fijado: una compilación para Linux
hecha en Windows recibiría si no el script del anfitrión.

**No dependen del manifiesto.** Cada uno construye el class path por sí mismo, `gaia-desktop.jar` seguido
de cada `.jar` de `lib/`, nombrados uno a uno en lugar de dejarlos a un comodín `lib/*`, para que una ruta
con espacios siga siendo una sola entrada y el recuento de lo añadido sea real. Lanzar con `-cp` es la razón
por la que nombran `tools.pantheum.gaia.desktop.Launcher` explícitamente: `-cp` no lee la `Main-Class` del
manifiesto. **Sí** sigue en cambio su `Class-Path` — un jar en `-cp` trae los jars que nombra su
manifiesto, buscados justo después de él —, así que cada jar de `lib/` se carga lo liste o no un script, y
omitir uno del class path del script no lo deja fuera de la JVM. Los scripts los listan de todos modos,
para que un jar cuyo manifiesto esté obsoleto, editado a mano o incompleto arranque igualmente. El
mismo script sirve para los dos empaquetados: el jar `-Pfat` no tiene `lib/` que añadir ni lo necesita, y
por eso también un `lib/` vacío es un aviso y no un error.

#### Las bibliotecas nativas de Zebra

La compilación no distribuye nada de Zebra — ni DLL, ni jars de servicio. En Windows proceden del Scanner
SDK de Zebra instalado en la máquina: `run.bat` añade sus jars de servicio `Jpos*.jar` y `bin\` al class
path y `bin\` al `PATH`, donde `System.loadLibrary` encuentra `CSJPOSScanner.dll` / `CSJPOSScanner64.dll`
(`java.library.path` toma por defecto el `PATH`). La API de JavaPOS y su cargador de configuración vienen
siempre de `lib\javapos-*.jar`, con SDK o sin él.

**`run.bat` nunca añade el propio jar de JavaPOS del SDK**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`.
Lo hacía antes, dejando fuera `lib\javapos-*.jar` para evitar dos copias de la API — pero el `Class-Path`
del manifiesto los cargaba igualmente (véase más arriba), así que la JVM recibía ambos: las clases del
cargador 4.x desde `lib\`, con `jpos/res/jpos.properties` y `jcl.xsd` de `javaPOS114.jar` a su lado. El
archivo de propiedades hace que [`JposConfig`](#integración-del-lector) se aparte, y ese `jcl.xsd` solo
acepta versiones de JavaPOS hasta la 1.10, así que cada entrada de `jpos.xml` (1.12–1.14) fallaba la
validación, el registro quedaba vacío y cada conexión fallaba con el error 109 antes siquiera de llegar al
lector. Basta con tener el jar en el class path para que ocurra, incluso detrás de los jars de `lib\`. Los
jars de servicio de Zebra funcionan sin cambios sobre la API 1.14 de `lib\` — apertura, reserva y lectura
se han verificado así con un lector real. Las DLL dejadas a mano junto a `run.bat` se siguen respetando: el
script fija entonces `-Djava.library.path=<dist>;%PATH%`, añadiendo el `PATH` porque la propiedad sustituye
por completo la ruta de búsqueda predeterminada.

`mvn javafx:run` no hace nada de esto, así que reservar un lector desde una ejecución de desarrollo en
Windows requiere poner por otros medios los jars del SDK en el class path y su `bin\` en el `PATH`; para
eso, ejecute desde `dist/`.

Más allá del class path, cubren lo que `java -jar` no hace: resolver su propio directorio para funcionar
desde cualquier directorio de trabajo, preferir `JAVA_HOME` al `PATH`, y rechazar una JVM anterior a la 11
con una frase que indica el remedio en lugar de `UnsupportedClassVersionError`. `GAIA_DESKTOP_OPTS`
transmite opciones de la JVM. `run.bat` usa `java.exe`, no `javaw.exe`, para que la consola siga abierta —
Windows es la única plataforma donde funciona el lector, y su estado y los errores de JavaPOS van allí.

`run.bat` también elige su Java, su JavaFX y sus servicios de Zebra según lo que encuentra — un `jdk*\`
incluido, un `javafx-sdk*\lib\` en el module path, el Scanner SDK de Zebra en `ZEBRA_HOME` —, dejando fuera
del class path las copias de JavaFX de `lib\` cuando usa las de un SDK, y pasa el analizador SAX del JDK,
los proxies y el almacén de certificados de Windows, y `--enable-native-access` en Java 17 y posteriores.
La sección del README [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) tiene
la tabla. Conserve los finales de línea CRLF del archivo: `cmd.exe` puede pasar por alto un `call :label` en
un archivo batch solo con LF. `run.sh` también pasa `--enable-native-access=ALL-UNNAMED` — JavaFX carga sus
propios nativos desde `lib/`, y con JavaFX en el class path `ALL-UNNAMED` es el módulo que lo cubre —, y lo
omite, como `run.bat`, solo cuando no puede leerse la versión de Java.

Cualquiera de las dos disposiciones lleva los nativos de JavaFX **de una sola plataforma**,
`${fx.platform}`, así que ninguna es portable entre sistemas operativos o arquitecturas. Compile una vez por
destino — consulte [Una compilación por plataforma](#una-compilación-por-plataforma).

> **Eclipse.** La ejecución `copy-dependencies` lleva una instrucción `<?m2e ignore?>`. m2e resuelve un
> proyecto abierto del espacio de trabajo a su carpeta `target/classes` en lugar de a un jar, y
> `copy-dependencies` se niega a copiar un artefacto sin empaquetar (MDEP-187), lo que haría fallar la
> compilación automática cada vez que el proyecto `gaia` estuviera abierto junto a este. En consecuencia,
> una compilación solo desde el IDE no llena `target/dist/gaia-desktop-<version>-<platform>/lib/`; ejecute
> `mvn package` antes de usar el propio jar.

---

## Arquitectura

| Paquete | Contiene |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (la ventana), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (lógica de composición sin JavaFX), `AiPair` (una fila del editor) |
| `desktop.parse` | `ParseOptions` (`ParseConfig` observable), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — el generador del informe, sin JavaFX y con pruebas unitarias sin el toolkit |
| `desktop.i18n` | `UiStrings` — el catálogo de etiquetas |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### El embudo de captura

Toda entrada — escrita, pegada, abierta desde un archivo, leída con el lector o compuesta — llega al
analizador por un único método, `MainView.capture(String, ScanSource, String)`.

Esa es la única decisión de diseño en torno a la cual se construye la aplicación. Una composición no
necesita informe propio, ni historial propio, ni vía de exportación propia, porque tras el embudo una
cadena compuesta es indistinguible de una leída. Cualquier cosa nueva que produzca una entrada debe pasar
por él y no por al lado.

### Hilos

| Hilo | Ejecuta |
|---|---|
| Hilo de aplicación de JavaFX | Todo el análisis, el renderizado y la interfaz. Con las tablas de AI ya calientes el análisis tarda menos de un milisegundo, así que no necesita ningún hilo de trabajo |
| `gaia-scanner` | Cada llamada al lector — abrir, reservar, habilitar, deshabilitar, liberar, cerrar. Un hilo de larga vida propiedad de `ScannerService`; consulte [Integración del lector](#integración-del-lector) |
| Hilo de eventos de JavaPOS | Las devoluciones de llamada de `ScannerService`. `MainView.FxScannerListener` pasa cada una con `Platform.runLater` |
| Segundo plano | La espera de `connect()` / `disconnect()` del lector (la reserva puede tardar hasta 5 s), la restauración del historial guardado al arrancar — que sirve también de calentamiento del analizador — y la comprobación de actualizaciones (`gaia-update-check`, una petición a GitHub, con el resultado devuelto mediante `Platform.runLater`) |

**Nunca modifique la selección de un control, ni destruya el control, desde dentro de su propio evento de
selección.** Tres fallos distintos de esta aplicación vinieron exactamente de eso: restablecer el selector
de AI dentro del propio `clearAndSelect` de la ventana emergente (una `IndexOutOfBoundsException` desde
`ListViewBehavior`), y reconstruir el panel que contiene el desplegable de idioma desde el propio
controlador de ese desplegable. Ambos se difieren ahora con `Platform.runLater`, y los motivos constan en
los dos puntos de llamada.

### Reconstruir o reetiquetar

Un cambio de idioma **reconstruye** el armazón a partir del catálogo — menús, barra de herramientas, panel
de opciones —, lo cual es seguro porque nada de ello guarda estado: `OptionsPane` es una vista
bidireccional de `ParseOptions`, de modo que los valores viven en el modelo y un panel nuevo los recoge de
inmediato.

Los controles que contienen **datos del usuario** son campos y se reetiquetan en su sitio:
`BuilderPane.retext(UiStrings)` conserva las filas de AI, los campos de Digital Link y la selección.
Reconstruirlo descartaría el trabajo en curso — cosa que una vez hizo.

---

## El token {GS}

Un `TextField` de JavaFX descarta en silencio todo carácter por debajo de `0x20`
(`TextInputControl.filterInput`). No «lo muestra de forma rara»: lo elimina. Escribir, pegar, `setText` e
`insertText` se ven todos afectados, así que `setText("01…" + GS + "10LOT")` da `"01…10LOT"`: una cadena de
elementos cuyo significado ha cambiado sin avisar, porque el AI de longitud variable se traga ahora lo que
le seguía.

`desktop.parse.GsToken` es toda la respuesta. El separador recorre la interfaz como el texto literal `{GS}`
y vuelve a ser `0x1D` camino del analizador. El token no se inventa aquí — es exactamente como
`ASCIIRevealerUtils.printable` del núcleo muestra `0x1D`, así que un valor copiado de un informe se pega
tal cual en la barra de entrada.

Un filtro `TextFormatter` en el campo de entrada atrapa todas las vías por las que puede llegar texto —
escritura, ⌘V, el menú contextual, arrastrar y soltar, `setText` —, y por eso la reescritura vive ahí y no
en cada controlador.

La vía del lector nunca pasa por `GsToken`: las lecturas llegan al analizador como bytes.

---

## Integración del lector

`ScannerService` posee el control `Scanner` de JavaPOS: abrir → reservar → habilitar, rearmando el evento de
datos tras cada lectura. Los nombres lógicos de dispositivo proceden de `src/main/resources/jpos.xml`.

**Cada llamada al dispositivo se ejecuta en un solo hilo, `gaia-scanner`, que vive tanto como el
servicio** — nunca en el hilo FX, y nunca en un hilo que termine tras la llamada. El servicio de Zebra
llega al lector mediante JNI sobre la API COM de CoreScanner, y COM vincula lo que prepara al apartamento
del hilo que llama: abra en un hilo que luego termine y el lector se reserva pero queda en silencio; cierre
en otro hilo y se desinicializa el apartamento equivocado. Cerrar en el hilo FX es el peor caso, porque
JavaFX llama allí a `OleInitialize` y registra en él el destino de colocación de cada ventana — un
`CoUninitialize` en ese hilo inutiliza arrastrar y soltar en toda la aplicación. Esto es JavaFX en Windows,
no una versión concreta de JavaFX. `connect()` y `disconnect()` envían la tarea a ese hilo y esperan, así
que `MainView` llama a ambas desde un hilo en segundo plano de corta duración.

Dos reglas impiden que un dispositivo que se porta mal bloquee la conexión. **El desmontaje nunca se
detiene antes de tiempo:** `closeQuietly` deshabilita, libera y cierra en pasos separados, libera solo si
se tiene la reserva, y siempre intenta `close()` — tras una reserva fallida, `release()` lanza «not
claimed», y cuando los pasos compartían un único `try` eso se saltaba el cierre y dejaba el dispositivo
abierto en el servicio de Zebra. **El evento de datos se rearma en un `finally`:** JavaPOS lo deshabilita
tras cada evento, así que una lectura o un controlador que lance una excepción debe rearmarlo igualmente, o
el lector queda en silencio hasta reconectar. `ScannerLifecycleTest` pone a prueba ambas reglas contra un
`jpos.Scanner` guionizado.

**`JposConfig` orienta JavaPOS hacia el `jpos.xml` incluido** antes de la primera apertura. Con los jars de
JavaPOS de Maven (`javapos-config-loader` 4.x) y sin `jpos/res/jpos.properties` en el class path, toda
apertura falla en otro caso con *"Service does not exist in loaded JCL registry"* (error 109), por tres
razones distintas: el gestor de servicios recurre a `SimpleRegPopulator`, que lee un `jpos.cfg` serializado
y nunca `jpos.xml`; el cargador XML busca `jpos.xml` solo en el directorio de trabajo, no dentro del jar; y
Xerces 2.12.2 en `lib/` rechaza la propiedad JAXP `accessExternalDTD` que fija el cargador. `JposConfig`
fija `jpos.config.regPopulatorClass` (`JavaxRegPopulator`), `jpos.config.populatorFileURL` (el propio
`jar:file:…!/jpos.xml` del jar) y `javax.xml.parsers.SAXParserFactory` (la del JDK) como propiedades del
sistema — nunca una que ya esté fijada, así que `-Djpos.config.populatorFileURL=file:…` sigue seleccionando
otro `jpos.xml` — y no fija nada cuando hay un `jpos.properties` en el class path o no está el cargador 4.x.
El `javaPOS114.jar` del Zebra SDK es uno de esos casos y rompe el registro sin más, y por eso `run.bat` lo
mantiene fuera del class path — consulte [Las bibliotecas nativas de Zebra](#las-bibliotecas-nativas-de-zebra).
`JposConfigTest` comprueba de extremo a extremo que el registro contiene entonces los lectores del archivo.

**El selector de dispositivo se genera a partir de ese registro**, no se mantiene como una lista en el
código: `ScannerService.logicalNames()` lee las entradas cargadas en el hilo `gaia-scanner` y conserva
aquellas cuya categoría de dispositivo es `Scanner` — las dos entradas de báscula de `jpos.xml` quedan
fuera —, en el orden `pickerOrder`: `ZebraAllScanners` primero cuando el archivo lo define, el resto por
orden alfabético (el registro no conserva el orden del archivo). A un archivo que no lo define no se le
añade; si no se carga nada, se ofrece solo el predeterminado, así que el selector nunca está vacío.
`MainView` muestra el predeterminado de inmediato y completa la lista desde un hilo en segundo plano,
conservando la elección guardada. Así, un lector añadido a `jpos.xml` — o todo un archivo indicado con
`-Djpos.config.populatorFileURL` — aparece en el selector sin cambiar código, y `JposConfigTest` contrasta
el selector con una lectura independiente del archivo incluido. Los servicios que nombran esas entradas son
clases de Zebra
(`com.zebra.jpos.service.scanner.*Factory`), que aún deben estar en el class path — desde el Scanner SDK
mediante `run.bat`, o con los jars de servicio de Zebra dejados en `lib\`.

Los datos leídos se decodifican como **ISO-8859-1** para que cada byte corresponda 1:1 y sobrevivan tanto
el prefijo AIM Code ID como cada `0x1D`; los informes se escriben en UTF-8.

Nada en el servicio compone texto para un usuario. Los eventos salen como constantes de
`ScannerListener.Event` más los valores que varían, y `MainView` pone el texto desde el catálogo — de lo
contrario la línea de estado estaría en inglés en una aplicación traducida de 35 maneras. Las líneas de
registro son la excepción y siguen en inglés.

### La barrera de plataforma

`ScannerService.isSupportedPlatform()` es una sola prueba sobre `os.name`, y `MainView` la consulta antes de
construir cualquier control del lector: en macOS y Linux no hay interruptor, ni selector de dispositivo, ni
menú Lector (y por tanto tampoco <kbd>⌘K</kbd>), ni etiqueta de estado. No están ocultos — nunca se
construyen.

Zebra distribuye sus servicios JavaPOS solo para Windows, y en otros sistemas nada falla pronto: los jars se
resuelven y el control se construye, y es `open()` el que finalmente falla por la capa nativa ausente,
varios segundos y un diálogo después.

La regla trata de los *servicios*, no de JavaPOS. Un fabricante que distribuyera un servicio
multiplataforma funcionaría aquí, y ampliar la regla es un cambio solo en `supportsJavaPos(String)` — que
es privado de paquete y se prueba contra cadenas de `os.name`, para que ambas respuestas queden cubiertas
en cualquier máquina que compile.

### Simbologías

`Symbologies` asigna los **78** tipos de datos de lectura `SCAN_SDT_*` de JavaPOS a nombres visibles, y al
`DataCarrierType` del núcleo donde los dos vocabularios coinciden. JavaPOS es el más fino de los dos —
separa UPC-A de EAN-13, y Data Matrix simple de GS1 DataMatrix, donde AIM agrupa toda una familia bajo un
único carácter de código.

Un tipo fuera de la tabla recibe igualmente un nombre utilizable, encontrado por reflexión sobre
`ScannerConst`, porque los servicios de los fabricantes sí informan valores fuera de la lista de la
especificación. Una prueba recorre `ScannerConst` y falla si algún tipo declarado no tiene nombre — lo que
además detecta constantes que añada un futuro JavaPOS.

Algunas correspondencias de soporte se dejan deliberadamente en `UNKNOWN` en lugar de adivinarlas: el 2 de 5
estándar (AIM separa `]R`/`]S` según el número de barras de inicio/parada, que JavaPOS no informa), los
códigos postales de cuatro estados y TLC-39.

---

## Localización

Las etiquetas viven en un archivo de propiedades por idioma bajo
`src/main/resources/i18n/ui_<CODE>.properties`, con los mismos códigos que los catálogos de errores del
núcleo (`EN`, `FR`, `PCM`, …). Existen los 35 y los 35 tienen un conjunto de claves idéntico. Los archivos
se leen como **UTF-8**, no como el ISO-8859-1 que supone `Properties`, para que las traducciones se guarden
de forma legible y no como escapes `\uXXXX`.

**El respaldo es por clave, no por archivo** — lo que permite que un idioma traducido en parte sea útil en
cuanto llega su primera clave. El inglés es el contrato: toda clave debe existir allí.

| Capa | Claves | Cobertura |
|---|---|---|
| Vocabulario de informe compartido | 31 | los 35 idiomas |
| Armazón de escritorio — menús, botones, compositor, opciones, línea de estado, maquetación del informe, estado del lector, textos de diálogo | 114 | los 35 idiomas |

Seis de los términos compartidos (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`)
no los usa el informe de esta aplicación.
Se conservan a propósito; la cabecera del archivo lo dice.

### El argumento `--language`

`GaiaDesktopApp.languageOverride()` lee `--language=…` mediante el propio `getParameters().getNamed()` de
JavaFX, lo resuelve con `Languages.fromArgument` — código de catálogo, nombre del enum o etiqueta BCP 47,
sin distinguir mayúsculas — y lo pasa a `MainView`.

**La anulación es para la ejecución, no para el archivo.** `MainView` guarda en `savedLanguage` el idioma
que cargó y escribe de vuelta *ese*, hasta que `languageChosenInSession` indica que el usuario eligió un
idioma por sí mismo; entonces su elección persiste con normalidad. Esa separación es todo el diseño: un
perfil de quiosco o un acceso directo de demostración puede abrirse en español sin reescribir lo que el
dueño de la máquina abrirá mañana, mientras que una elección real hecha en el panel de opciones se mantiene.

Un valor que no nombra ningún catálogo se registra junto con los códigos aceptados y se ignora. Negarse a
arrancar por una errata en un acceso directo sería el fallo peor, y `Languages.fromArgument` devuelve
`null` en lugar de lanzar una excepción por la misma razón.

### Añadir un idioma

No hace falta cambiar código.

1. Edite `ui_<CODE>.properties` — todos los idiomas tienen ya un archivo.
2. Traduzca los valores. Deje en su sitio los marcadores `{0}` / `{1}`.
3. Compile. Dos pruebas hacen cumplir el contrato: una falla si el código pide una clave que el inglés no
   define, la otra si el conjunto de claves de algún catálogo se aparta del inglés.

La primera prueba recorre las **fuentes** en busca de literales de clave en lugar de usar reflexión sobre
las clases, porque las claves solo existen como literales de cadena en los puntos de llamada. Por eso un
mensaje nuevo debe construirse con la clave escrita en línea — `strings.format("st.scannerError", …)` — y
no buscada mediante una variable, o la prueba no puede verla.

### Lo que deliberadamente no se traduce

- los escuetos nombres de campo en monoespaciado junto a los valores en bruto — `format`, `position`,
  `length`, `raw` y `part` de la tarjeta de elemento, `AI`, `sep`, `pos` y `len` de la cinta, y las cuatro
  celdas que localizan un error (`AI`, `position`, `level`, `stage`), cuyos valores son de todos modos
  nombres de enum sin traducir;
- los nombres de las entradas de ejemplo, que nombran construcciones GS1, y los nombres de simbología, que
  son nombres propios;
- el párrafo de corrección de `GE-C084` — prosa de normas, donde una traducción a medias es peor que el
  inglés;
- la red de seguridad de AI desconocido de `ElementBuilder`, inalcanzable desde la interfaz: el editor
  comprueba por sí mismo los AI desconocidos y los informa desde el catálogo;
- las líneas de registro.

Queda un hueco: `ScanSource.label()` proporciona `manual` / `scanner` / `file` / `built` en inglés a la fila
del historial y a la cabecera del informe. Cerrarlo requiere más que una clave — `HistoryCell` no tiene
`UiStrings`, y la fábrica de celdas de la lista se instala una sola vez, así que también habría que
refrescar las celdas al cambiar de idioma.

---

## Formatos del estado guardado

Dos archivos bajo `~/.gaia-desktop/`, separados porque cambian a ritmos distintos — un registro de capturas
corrupto o editado a mano no debería costarle al usuario sus ajustes. Cambie el directorio con
`-Dgaia.desktop.home=…`, que es como las pruebas se mantienen fuera del directorio personal real.

**`options.properties`** — se escribe cuando cambia una opción, y al salir.

| Clave | Significado |
|---|---|
| `parse.mode`, `parse.language` | Profundidad del proceso e idioma de los mensajes |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Representación de fechas |
| `check.skipRequires`, `check.skipExcludes` | Excepciones a las comprobaciones estructurales |
| `ui.theme` | `LIGHT` o `DARK` |
| `ui.optionsPane` | ¿Panel abierto? |
| `ui.scannerDevice` | Último nombre lógico de JavaPOS |
| `ui.mode` | `PARSE`, `BUILD` o `COMBINE` |
| `update.check` | Si se ejecuta la comprobación diaria de actualizaciones (por defecto `true`) |
| `update.lastCheck` | Cuándo se ejecutó por última vez, en ISO-8601; si falta o no se puede leer, toca comprobar |
| `update.skipVersion` | Una versión para la que el usuario eligió *Omitir esta versión*; no se vuelve a ofrecer automáticamente |

Un archivo ausente o dañado nunca es fatal: cada valor recurre a su respaldo por separado, así que una
línea mala solo cuesta esa línea. En el primer arranque, sin ningún archivo, las opciones se inicializan a
partir de la configuración regional de la máquina mediante `SystemDefaults` y no de los valores
predeterminados de GAIA — el formato de fecha corto de la configuración regional da el orden de los
componentes, el separador y si el mes y el año van abreviados.

Nunca pase un null a `Properties.setProperty`: lanza una excepción, y `persistOptions()` se ejecuta desde
controladores de eventos de la interfaz, donde una excepción escapa al hilo FX. Es un fallo real que esta
aplicación ya ha tenido.

**`captures.tsv`** — se escribe cuando cambia el historial, y al salir. Las más recientes primero, con un
máximo de **500** entradas.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Solo se guarda lo que no puede recalcularse. El `ParseResult` **no** se serializa — se vuelve a obtener
analizando de nuevo la entrada al cargar, de modo que un historial restaurado refleja las opciones vigentes
*ahora*. Las entradas contienen a menudo FNC1 y cualquier otra cosa que emita un lector, así que el campo de
entrada se escapa (`\\`, `\t`, `\n`, `\r`, y `\xNN` para todo lo demás por debajo de `0x20`) y el archivo
se lee y escribe en ISO-8859-1, byte a byte.

Los archivos escritos antes de que se registrara la fecha contienen solo una hora; siguen cargándose,
fechados hoy, porque la fecha real nunca se anotó.

---

## Pruebas

```bash
(cd gaia-desktop && mvn test)
```

198 pruebas, deliberadamente **independientes del toolkit de JavaFX**, lo que las mantiene rápidas y sin
interfaz gráfica. Cubren el generador del informe, la lógica de composición y las búsquedas de AI, la
conversión de `{GS}`, los valores predeterminados derivados de la configuración regional, ambos almacenes
(incluido un viaje de ida y vuelta con `0x1D` y archivos dañados a propósito), la tabla de simbologías, la
regla de plataforma del lector, el stub de versión y los catálogos.

Tres merecen conocerse porque defienden contratos en lugar de funciones:

| Prueba | Falla cuando |
|---|---|
| `CatalogueCoverageTest` | El código pide una clave que `ui_EN.properties` no define, o alguno de los 35 archivos se aparta del conjunto de claves inglés |
| `SymbologiesTest` | Alguna constante `SCAN_SDT_*` que declara JavaPOS no tiene nombre visible — incluida una que añada una actualización de JavaPOS |
| `ScannerServiceTest` | La regla de plataforma cambia de respuesta para Windows o para cualquier otro sistema |

`UiStrings` es deliberadamente tolerante en ejecución — una clave desconocida se muestra como la propia
clave en lugar de lanzar una excepción a mitad de la maquetación —, y precisamente por eso tiene que existir
la prueba del catálogo.

**Sin cubrir:** todo lo que necesite un toolkit en marcha — el comportamiento del foco, el cambio de modo,
la edición de tablas, el cambio de idioma, y los dos fallos descritos en [Hilos](#hilos). Se verificaron
manejando la ventana real, pero una regresión en ellos no haría fallar la compilación. Añadir TestFX o
Monocle cerraría ese hueco.

---

## Solución de problemas de compilación

| Síntoma | Causa y solución |
|---|---|
| `package tools.pantheum.gaia does not exist` | El núcleo no está instalado. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | La `Main-Class` del jar debe ser `Launcher`, no `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* al arrancar | Esperado e inofensivo — JavaFX avisa de que está en el class path. Justo por eso existe `Launcher` |
| El jar no arranca, `NoClassDefFoundError` | El jar predeterminado lleva solo las clases de este módulo; `dist/lib/` debe estar a su lado. Use `-Pfat` para un único archivo autónomo |
| `Artifact has not been packaged yet … MDEP-187` en Eclipse | Resolución del espacio de trabajo de m2e. Consulte la nota en [Empaquetado](#empaquetado) |
| Aparecen archivos `… 2.properties` duplicados en una compilación | El proyecto está en una carpeta sincronizada con iCloud y la sincronización creó copias en conflicto durante la compilación. Haga siempre `mvn clean package`; mejor aún, excluya `target/` de la sincronización |
| Un elemento de menú muestra una clave como `act.hri` | Falta esa clave en `ui_EN.properties`. La compilación debería haberlo detectado — consulte [Pruebas](#pruebas) |

---

## Limitaciones conocidas y trabajo pendiente

- **No hay pruebas de interfaz sin pantalla.** Consulte [Pruebas](#pruebas).
- **Artefactos específicos de cada plataforma.** Ambos empaquetados incluyen los nativos de JavaFX de una
  sola plataforma; compile uno por destino con `-Dfx.platform` (cualquier máquina puede compilarlos todos).
  Ninguno incluye un entorno de ejecución de Java — para eso hace falta `jpackage`, que solo compila para el
  sistema operativo en el que se ejecuta.
- **La entrada por lector es solo para Windows.** Consulte [La barrera de plataforma](#la-barrera-de-plataforma).
- **`ScanSource.label()` no está localizado.** Consulte [Lo que deliberadamente no se
  traduce](#lo-que-deliberadamente-no-se-traduce).
- **`UiState.mode` es un `String`** (`"PARSE"` / `"BUILD"`) mientras que `Theme`, a su lado, es un enum,
  así que el literal aparece en varios sitios. Un enum los eliminaría.
- **El destino de Digital Link se limita al dominio y al prefijo de ruta.** `BuilderDigitalLinkConfig`
  admite además parámetros de consulta adicionales y un fragmento; el editor no los ofrece.
- **Los pares de AI mutuamente excluyentes (`GE-S006`) no se señalan en el editor** como sí ocurre con los
  AI desconocidos y duplicados — la composición simplemente falla.
- **El catálogo de errores del núcleo duplica las comillas** (`value ''09506000134350''`), lo que aparece
  tal cual en los fallos de composición y en el informe. Parece un escape de comillas de `MessageFormat` que
  no se deshace; la corrección corresponde al núcleo.
