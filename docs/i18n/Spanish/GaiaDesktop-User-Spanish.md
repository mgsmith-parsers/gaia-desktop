# GAIA Desktop — Guía del usuario

Cómo usar la aplicación. Para compilarla, modificarla o añadir un idioma, consulte la
[guía del desarrollador](../../GaiaDesktop-Developer.md) (en inglés). Documento original:
[GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Índice

1. [Qué es](#qué-es)
2. [Ejecutarla](#ejecutarla)
3. [La ventana](#la-ventana)
4. [Modo Analizar](#modo-analizar)
   - [La barra de entrada y el token {GS}](#la-barra-de-entrada-y-el-token-gs)
   - [Lectura con lector](#lectura-con-lector)
   - [Abrir un archivo](#abrir-un-archivo)
   - [El historial de capturas](#el-historial-de-capturas)
5. [Modo Componer](#modo-componer)
   - [El editor de AI](#el-editor-de-ai)
   - [Lo que el editor rechaza](#lo-que-el-editor-rechaza)
   - [Cadena de elementos o Digital Link](#cadena-de-elementos-o-digital-link)
   - [Editar una captura existente](#editar-una-captura-existente)
6. [El informe](#el-informe)
   - [Exportar](#exportar)
7. [Opciones de análisis](#opciones-de-análisis)
   - [Primer arranque](#primer-arranque)
8. [Idiomas](#idiomas)
9. [Qué se recuerda](#qué-se-recuerda)
10. [Atajos de teclado](#atajos-de-teclado)
11. [Solución de problemas](#solución-de-problemas)
12. [Límites](#límites)

---

## Qué es

GAIA Desktop hace dos cosas con datos GS1:

- **Analizar** — escriba, pegue, abra o escanee una cadena de elementos GS1 o un URI
  Digital Link y lea el resultado como un informe completo: qué significa cada identificador
  de aplicación (AI), qué está mal y por qué;
- **Componer** — ensamblar pares AI / valor en una cadena de elementos o un URI Digital Link.

Todo lo que la aplicación sabe de GS1 — cada definición de AI, cada regla de validación, cada
mensaje de error y cada descripción localizada — procede del motor GAIA que tiene debajo. La
aplicación no añade reglas propias: lo que acepta es exactamente lo que acepta el motor.

---

## Ejecutarla

```bash
java -jar gaia-desktop.jar
```

El archivo JAR necesita el directorio `lib/` que se distribuye junto a él: manténgalos juntos o
la aplicación no arrancará. (También puede compilarse un JAR autónomo; consulte la
[guía del desarrollador](../../GaiaDesktop-Developer.md#packaging).)

Debe tener instalado **Java 21 o posterior**. La distribución es propia de un sistema operativo
y una arquitectura de procesador, así que use el JAR compilado para su máquina.

Para abrir la aplicación en un idioma concreto, sin cambiar el idioma que tiene guardado:

```bash
java -jar gaia-desktop.jar --language=FR
```

El valor puede ser el código del catálogo (`FR`, `PCM`, `ARZ`), el nombre completo (`FRENCH`,
`WU-CHINESE`) o una etiqueta de idioma (`fr`, `zh-HK` para cantonés, `ar-EG` para árabe
egipcio); las mayúsculas y minúsculas no importan. La preferencia solo dura esa ejecución: su
idioma guardado queda intacto, de modo que un acceso directo creado para un compañero no puede
cambiar el idioma en que se abre su propia copia. Elegir un idioma *dentro* de la aplicación sí
es una elección real y se guarda como siempre. Un valor que no designa ningún idioma se ignora
con una nota en la consola, y la aplicación se abre en el idioma guardado en lugar de negarse a
arrancar.

Al arrancar, la consola imprime una línea con la versión de la aplicación, su fecha de
compilación y la versión del motor. Esa es la línea que conviene citar en un informe de error.

---

## La ventana

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

> El esquema conserva las etiquetas en inglés, como todos los bloques de código de esta
> documentación. En pantalla verá *Analizar* / *Componer*, *Capturas*, *Guardar informe*,
> *Opciones*, *lector: apagado* y *Acerca de*.

**En macOS y Linux no existe la fila del lector**: no hay botón ni selector de dispositivo en la
barra de herramientas, no hay menú *Lector* y no aparece *lector: apagado* en la barra de
estado. Consulte [Lectura con lector](#lectura-con-lector).

El selector **Analizar ⇄ Componer** solo cambia la franja bajo la barra de herramientas y los
controles de captura. El historial, el informe, el panel de opciones y las acciones de
exportación sirven a ambos modos, porque una composición correcta se convierte simplemente en
una captura más.

El panel de opciones está **cerrado en el primer arranque**. El modo, la visibilidad del panel,
el tema y el último lector utilizado se recuerdan de una ejecución a otra.

---

## Modo Analizar

### La barra de entrada y el token {GS}

La barra acepta una cadena de elementos, un URI GS1 Digital Link o una lectura en bruto con su
prefijo AIM Code ID (`]d2…`). <kbd>Intro</kbd> analiza.

Los AI de longitud variable se separan con **FNC1**, el carácter de control invisible `0x1D`.
Un campo de texto no puede contener ese carácter: se eliminaría en silencio, y
`01…` + FNC1 + `10LOT` llegaría como `01…10LOT`, una cadena de elementos que significa otra cosa.

Por eso la aplicación muestra el separador como el token visible **`{GS}`** y lo reconvierte de
camino al analizador:

- el **botón `{GS}`** inserta el token donde está el cursor;
- **pegar** caracteres separadores reales — con el teclado, con el menú contextual o
  arrastrando — los convierte automáticamente en `{GS}`, así que no se pierde nada;
- el token se escribe igual que lo escribe el informe, de modo que un valor copiado de un
  informe puede pegarse de vuelta tal cual.

La captura, el informe y el historial guardado contienen todos el separador **real**. El token
es solo lo que usted ve y escribe.

### Lectura con lector

> **Solo Windows.** Zebra distribuye sus controladores de lector únicamente para Windows, así
> que en macOS y Linux la aplicación no construye ningún control de lectura: no hay botón
> *Conectar lector*, ni selector de dispositivo, ni menú *Lector*, ni estado del lector en la
> barra de estado. Nada aparece atenuado, porque no hay ajuste ni dispositivo que pudiera
> activarlo. Todo lo demás, incluida la apertura de un archivo de lecturas guardadas, funciona
> en cualquier plataforma.

*Conectar lector* abre el dispositivo indicado en el selector; `ZebraAllScanners` es el valor
predeterminado y sirve para la mayoría de los modelos. Cada lectura se analiza y se añade al
historial automáticamente.

Los datos del lector se leen byte a byte, de modo que el prefijo AIM Code ID y cada separador
FNC1 llegan al analizador exactamente como los envió el lector.

Los controles del lector se ocultan en el modo Componer, pero un lector conectado sigue
funcionando: *Lector → Conectar / Desconectar* (<kbd>⌘K</kbd>) lo sigue alternando, la barra de
estado lo sigue indicando y una lectura que llegue con el editor abierto se captura igualmente.

Si un dispositivo no se conecta, consulte [Solución de problemas](#solución-de-problemas).

### Abrir un archivo

*Archivo → Abrir archivo de texto…* analiza **cada línea no vacía como una captura
independiente**, que es la forma rápida de procesar un lote de lecturas que alguien le haya
enviado. El archivo se lee byte a byte, así que los separadores en bruto que contenga
sobreviven.

### El historial de capturas

Las más recientes primero. Cada fila muestra un punto de válido / no válido, un resumen de una
línea y la hora de captura, su procedencia y cuántos AI contiene. La fecha solo aparece en las
capturas de un día anterior; las filas de hoy muestran únicamente la hora.

El resumen es el **URI Digital Link** en el caso de un Digital Link, y el **HRI** en los demás:
para una cadena de elementos, `(01)09506000134352 (17)261231` se lee mejor que
`01095060001343521726123110…`.

Haga clic derecho en una captura para copiar su entrada o su HRI, devolverla a la barra de
entrada, abrirla en el editor, eliminarla o borrar el historial.

---

## Modo Componer

### El editor de AI

Una tabla de pares AI / valor. Escriba un código de AI o elija entre **todos los AI que el motor
conoce** en la lista *Añadir un AI…*; las columnas **Título**, **Formato** y **Descripción** se
rellenan a medida que escribe, en el idioma elegido. El selector no puede ofrecer un AI que el
compositor fuera a rechazar.

La tabla siempre mantiene **una fila vacía al final**: escriba en ella y aparecerá la
siguiente, de modo que una cadena con varios AI nunca necesita *Añadir fila*.

La edición se comporta como debe comportarse una tabla: <kbd>Intro</kbd>, <kbd>Tab</kbd>,
<kbd>Mayús</kbd>+<kbd>Tab</kbd> y hacer clic fuera **conservan** lo que haya escrito;
<kbd>Esc</kbd> lo descarta. <kbd>Tab</kbd> avanza de AI a Valor y a la fila siguiente, saltando
las columnas que no se pueden editar.

### Lo que el editor rechaza

| | Qué ocurre |
|---|---|
| Un AI que el motor no reconoce | La fila se vuelve roja mientras escribe; la composición se rechaza con una explicación |
| El mismo AI dos veces, desde el selector | No se añade: un mensaje lo nombra y se selecciona la fila que ya lo tiene |
| El mismo AI dos veces, escrito a mano | Ambas filas quedan marcadas; la composición se rechaza con el error `GE-S004` |
| Nada que componer | Un mensaje; no se crea ninguna captura |

Un AI solo puede aparecer una vez en una cadena de elementos: por eso el selector rechaza de
entrada en lugar de dejar intentar una composición condenada al fracaso.

### Cadena de elementos o Digital Link

**Componer cadena de elementos** ensambla las filas insertando el separador FNC1 allí donde la
norma lo exige. **Componer Digital Link** produce un URI; despliegue *Destino Digital Link* para
dirigirlo a su propio resolutor en lugar del canónico `id.gs1.org`:

| Campo | Efecto | Vacío significa |
|---|---|---|
| Dominio | El host del resolutor, p. ej. `example.com` | `id.gs1.org` |
| Prefijo de ruta | Un segmento antes de la ruta de los AI, p. ej. `/products` | sin prefijo |

Si **tiene éxito**, el resultado se comporta igual que una lectura: se analiza, se añade al
historial, se representa como informe y se guarda. El compositor valida analizando su propia
salida, así que todo lo que devuelve es válido con seguridad.

Si **falla**, no se captura nada. El motivo permanece bajo los botones de composición, una línea
por problema, cada una con el error, el AI al que corresponde y la explicación propia del motor:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> Las comillas duplicadas de ese mensaje proceden del catálogo de errores del motor, no de esta
> aplicación.

### Editar una captura existente

Clic derecho en una captura → **Componer** carga sus AI en el editor. Cambie un valor, vuelva a
componer y el resultado aparecerá como una captura nueva: la original queda intacta.

---

## El informe

La captura seleccionada se representa como un informe completo: un banner de veredicto, la
cadena de elementos descompuesta en una «cinta» etiquetada, los metadatos del análisis, una
tarjeta por AI con sus interpretaciones, y los errores y avisos con orientación para
corregirlos.

En un **Digital Link**, cada tarjeta de AI lleva además su función estructural — **clave
primaria**, **calificador de clave** o **atributo de datos** — con un código de color según el
orden en que aparecen en el URI. Las funciones solo se muestran en un Digital Link: en una
cadena de elementos normal, un AI no tiene ninguna posición de URI que describir.

El informe sigue el idioma y el tema que haya elegido.

### Exportar

| | |
|---|---|
| **Guardar informe → Como HTML…** (<kbd>⌘S</kbd>) | Escribe exactamente lo que hay en pantalla: un único archivo autónomo que se abre en cualquier navegador |
| **Guardar informe → Como PDF…** (<kbd>⌘P</kbd>) | Imprime el informe a través del diálogo de impresión del sistema; elija ahí un destino PDF |

El PDF pasa por el diálogo de impresión porque es ahí donde se eligen el destino y el nombre del
archivo: **PDF ▾ → Guardar como PDF** en macOS, **Microsoft Print to PDF** en Windows,
**Imprimir en un archivo** en Linux. Las páginas se paginan y el texto sigue siendo
seleccionable.

La impresión y el PDF usan siempre la paleta clara, aunque la aplicación esté en tema oscuro, y
conservan intactas las etiquetas de color de los AI y el banner de veredicto.

Si su máquina no tiene ninguna impresora ni destino PDF configurado, la aplicación lo indica y
le remite a la exportación HTML, que se imprime desde cualquier navegador.

---

## Opciones de análisis

El panel de la derecha reúne todas las opciones del motor. Cambiar cualquiera de ellas
**vuelve a analizar la captura seleccionada de inmediato**, así que el panel sirve además para
preguntar «¿cómo se vería esta entrada con estos ajustes?», y no solo para influir en la
siguiente lectura.

| Opción | Valores |
|---|---|
| Modo de análisis | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — hasta dónde llevar el análisis |
| Idioma | 35 catálogos de mensajes |
| Orden | `LITTLE` (dd/mm/aaaa), `MIDDLE` (mm/dd/aaaa), `BIG` (aaaa/mm/dd) |
| Separador | `SLASH`, `HYPHEN`, `PERIOD` |
| Mes | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Año | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Omitir la comprobación «requires» | Deja de rechazar un AI cuyo acompañante obligatorio falta (`GE-S005`) |
| Omitir la comprobación «excludes» | Deja de rechazar las combinaciones de AI que la norma prohíbe (`GE-S006`) |

*Restablecer valores predeterminados*, al pie del panel, restaura los valores propios del motor.

### Primer arranque

Cuando aún no hay nada guardado, la aplicación toma sus ajustes de la **configuración regional
de su máquina** en lugar de los valores predeterminados del motor:

- el **idioma** procede de la configuración regional del sistema, con retorno al inglés cuando
  no existe catálogo;
- las **opciones de fecha** se deducen del formato de fecha corta de su configuración regional:
  el orden de los componentes, el separador y si el mes y el año se abrevian.

> **Nota.** Por tanto, una configuración regional que escribe las fechas cortas con año de dos
> dígitos empieza aquí también con dos dígitos: coincide con lo que muestra el resto de su
> sistema, pero difiere del valor predeterminado del motor, que es de cuatro. Cámbielo en el
> panel y la elección se mantiene.

---

## Idiomas

Elegir un idioma cambia **toda la aplicación** — menús, botones, editor, panel de opciones,
línea de estado e informe —, no solo los mensajes de error. Los idiomas que se escriben de
derecha a izquierda invierten la ventana y el informe.

Están disponibles los 35 idiomas, con dos capas de cobertura:

| | Cobertura |
|---|---|
| Informes de análisis y mensajes de error del motor | **los 35 idiomas** |
| Menús, botones y etiquetas de la propia aplicación | **los 35 idiomas** |

Los idiomas de derecha a izquierda — árabe, árabe egipcio, persa y urdu — invierten la ventana y
el informe desde el momento en que la aplicación se abre.

El idioma se recuerda entre ejecuciones. Para abrir en otro idioma una sola vez, consulte
[`--language`](#ejecutarla).

Algunas cosas quedan sin traducir en todos los idiomas a propósito: los nombres de campo
abreviados que se imprimen junto a los valores en bruto (`format`, `position`, `raw`, `len` …),
que son identificadores y no palabras; los nombres de las muestras y de las simbologías, que son
términos de GS1 y del sector; y un párrafo largo de la norma en el que una traducción parcial
induciría a error.

---

## Qué se recuerda

Dos archivos en `.gaia-desktop`, dentro de su directorio personal:

| Archivo | Contiene |
|---|---|
| `options.properties` | Todas las opciones de análisis, el tema, si el panel de opciones está abierto, el último lector utilizado y el modo en que se dejó la ventana |
| `captures.tsv` | El historial de capturas, las más recientes primero, con un límite de 500 entradas |

Las opciones se escriben cada vez que cambia una; el historial cada vez que cambia. Ambos se
vuelven a escribir al salir.

Eliminar cualquiera de los dos archivos restablece esa parte y nada más: se mantienen separados
para que un historial dañado no le cueste sus ajustes. Un archivo estropeado al editarlo a mano
tampoco es fatal: cada ajuste vuelve individualmente a su valor predeterminado, así que una
línea incorrecta solo cuesta esa línea.

El historial guarda la entrada, no el análisis. Se vuelve a analizar al cargarlo, lo que
significa que una captura restaurada refleja las opciones vigentes **ahora**, no las que estaban
vigentes cuando se capturó.

---

## Atajos de teclado

| | |
|---|---|
| <kbd>Intro</kbd> | Analizar la barra de entrada |
| <kbd>⌘O</kbd> | Abrir un archivo de texto |
| <kbd>⌘S</kbd> | Guardar el informe como HTML |
| <kbd>⌘P</kbd> | Guardar el informe como PDF (diálogo de impresión del sistema) |
| <kbd>⌘G</kbd> | Insertar el token separador FNC1 |
| <kbd>⌘K</kbd> | Conectar / desconectar el lector (solo Windows) |
| <kbd>⌘D</kbd> | Alternar el tema oscuro |
| <kbd>⌘Q</kbd> | Salir |

En el editor: <kbd>Tab</kbd> / <kbd>Mayús</kbd>+<kbd>Tab</kbd> se mueven entre celdas
editables, <kbd>Intro</kbd> confirma y <kbd>Esc</kbd> cancela.

---

## Solución de problemas

| Síntoma | Causa y solución |
|---|---|
| La aplicación no arranca, o informa de `NoClassDefFoundError` | El directorio `lib/` debe estar junto al JAR. Manténgalos juntos o use una distribución autónoma |
| *«Unsupported JavaFX configuration…»* en la consola al arrancar | Esperado e inofensivo: el kit de herramientas indica cómo se cargó. La aplicación funciona con normalidad |
| Un separador que pegó parece desaparecer | Es lo esperado: se reescribe como el token visible `{GS}` y el analizador sigue recibiendo el carácter real. Consulte [el token {GS}](#la-barra-de-entrada-y-el-token-gs) |
| No hay botón *Conectar lector* | Está en macOS o Linux, donde no existen los controladores del lector. Consulte [Lectura con lector](#lectura-con-lector) |
| El lector no se conecta | Compruebe que el nombre lógico del selector corresponde a su modelo, que el lector está conectado y que ninguna otra aplicación lo está reteniendo |
| La exportación a PDF dice que no hay destino | Su máquina no tiene impresora ni destino PDF configurado. Añada uno, o exporte a HTML e imprima desde un navegador |
| Un elemento de menú muestra algo como `act.hri` | Falta una etiqueta; comuníquelo, por favor: es un defecto de compilación, no un ajuste |

---

## Límites

- **La lectura es solo para Windows**, porque los controladores lo son.
- **El PDF pasa por el diálogo de impresión** y no por uno de guardado.
- **Los destinos Digital Link admiten un dominio y un prefijo de ruta**, pero no parámetros de
  consulta adicionales ni fragmento.
- **Un AI solo puede aparecer una vez** en una cadena de elementos: es la regla de GS1, no un
  límite de la aplicación.
