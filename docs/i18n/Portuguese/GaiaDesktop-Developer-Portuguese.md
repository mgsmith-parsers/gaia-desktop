# GAIA Desktop — Guia do programador

Como o módulo é compilado, organizado e testado. Para o que a aplicação faz vista de fora, consulte o
[Guia do utilizador](GaiaDesktop-User-Portuguese.md). Documento original:
[GaiaDesktop-Developer.md](../../GaiaDesktop-Developer.md).

## Índice

1. [O módulo num relance](#o-módulo-num-relance)
2. [Compilar e executar](#compilar-e-executar)
   - [Empacotamento](#empacotamento)
3. [Arquitetura](#arquitetura)
   - [O funil de captura](#o-funil-de-captura)
   - [Threads](#threads)
   - [Reconstruir ou reetiquetar](#reconstruir-ou-reetiquetar)
4. [O token {GS}](#o-token-gs)
5. [Integração do leitor](#integração-do-leitor)
   - [A barreira de plataforma](#a-barreira-de-plataforma)
   - [Simbologias](#simbologias)
6. [Localização](#localização)
   - [O argumento `--language`](#o-argumento---language)
   - [Adicionar um idioma](#adicionar-um-idioma)
   - [O que deliberadamente não é traduzido](#o-que-deliberadamente-não-é-traduzido)
7. [Formatos do estado guardado](#formatos-do-estado-guardado)
8. [Testes](#testes)
9. [Resolução de problemas de compilação](#resolução-de-problemas-de-compilação)
10. [Limitações conhecidas e trabalho em aberto](#limitações-conhecidas-e-trabalho-em-aberto)

---

## O módulo num relance

```
tools.pantheum:gaia-desktop:1.0.0   Java 21 · JavaFX 21
        └── depends on tools.pantheum:gaia:1.1.0
```

Um cliente da biblioteca principal e nada mais. Não incorpora **qualquer conhecimento de GS1**: cada
definição de AI, regra de validação, mensagem de erro e descrição localizada vem de
[`GaiaParser`](../../../../gaia/docs/GaiaParser.md) e
[`GaiaBuilder`](../../../../gaia/docs/GaiaBuilder.md). Em particular, a lista de AI nunca é copiada para
este módulo — o seletor do compositor lê o próprio registo do núcleo, pelo que não pode oferecer um AI que o
compositor rejeitaria.

**Pontos de entrada:** `tools.pantheum.gaia.desktop.GaiaDesktopApp` (a `Application` JavaFX) e
`tools.pantheum.gaia.desktop.Launcher` (a `Main-Class` do jar empacotado).

O módulo é uma folha: nada depende dele, e ele não depende de nenhum outro módulo a jusante.

---

## Compilar e executar

- **Java 21** para executar, e um JDK 21 ou mais recente para compilar — o módulo compila com
  `release=21`. O JavaFX 21 é a linha LTS correspondente. O jar do núcleo continua a ser bytecode Java 11 e
  corre sem alterações em 21.
- **Maven** no `PATH`. Não há wrapper neste repositório.
- **O núcleo instalado primeiro.** Este módulo resolve `gaia:1.1.0` a partir de `~/.m2`; um jar do núcleo
  desatualizado ou ausente é a causa habitual de `package tools.pantheum.gaia does not exist`.

```bash
(cd gaia && mvn -q install -DskipTests)      # publish the core
(cd gaia-desktop && mvn javafx:run)          # run from source
(cd gaia-desktop && mvn clean package)       # target/dist/gaia-desktop-<version>-<platform>/ — jar (~290 KB), lib/, run.sh (run.bat on win)
(cd gaia-desktop && mvn clean -Pfat package) # one self-contained jar, ~50 MB
(cd gaia-desktop && mvn package -Dfx.platform=win)   # another platform; see Packaging
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar
java -jar gaia-desktop/target/dist/gaia-desktop-1.0.0-mac-aarch64/gaia-desktop.jar --language=FR
```

`--language` é o único argumento de linha de comandos, e a única forma de influenciar o arranque além de
`-Dgaia.desktop.home=…`, que coloca todo o perfil noutro lado.

`mvn javafx:run` é o caminho de desenvolvimento: o plugin põe o JavaFX no **module path**, pelo que o
toolkit arranca como módulo com nome e `GaiaDesktopApp` pode ser lançada diretamente. Um jar empacotado não o
consegue — está tudo no class path —, e é por isso que a sua `Main-Class` é o adaptador `Launcher`. Uma
subclasse de `Application` como `Main-Class` nessa disposição falha com *"JavaFX runtime components are
missing"*.

Use sempre `clean` — uma vez, antes de compilar um conjunto de plataformas, já que esvazia `target/dist/` de
todas. Veja porquê em [Resolução de problemas de compilação](#resolução-de-problemas-de-compilação).

### Empacotamento

Tudo o que é distribuível é reunido em `target/dist/gaia-desktop-<version>-<platform>/` e mais nada, pelo
que a pasta pode ser comprimida ou copiada como um conjunto sem ter de escolher artefactos em `target/` ao
lado de `classes/`, `maven-status/` e do resto dos ficheiros de trabalho da compilação. `${dist.dir}`
nomeia-a uma vez; cada plugin que escreve um artefacto aponta para ela.

#### Uma compilação por plataforma

Os jars do JavaFX contêm código nativo, pelo que cada distribuição é para uma só plataforma, indicada pela
propriedade `fx.platform` como classificador do OpenJFX: `mac-aarch64`, `mac`, `win`, `linux` ou
`linux-aarch64`. Cinco perfis `fx-*` ativados pelo sistema operativo definem-na como a da máquina que
compila, o que é tudo o que um simples `mvn package` ou `mvn javafx:run` precisa; `-Dfx.platform=…` tem
precedência sobre eles para compilar qualquer outra, a partir de qualquer máquina — os jars do JavaFX são
apenas transferências. `${dist.dir}` é `target/dist/${fx.platform}`, pelo que a compilação de uma plataforma
nunca substitui a de outra.

```bash
(cd gaia-desktop && mvn -q clean package)    # this machine's platform, with the tests
for p in win linux mac mac-aarch64; do (cd gaia-desktop && mvn -q package -DskipTests -Dfx.platform=$p); done
```

Dois pormenores do pom fazem isto funcionar, e ambos parecem dispensáveis:

- **Cada módulo do JavaFX é uma dependência direta** — base, graphics, controls, media e web —, cada um com
  `<classifier>${fx.platform}</classifier>` e uma exclusão `org.openjfx:*`. Os poms do OpenJFX escolhem os
  seus jars transitivos pelo sistema operativo da *máquina que compila*, por isso deixar graphics ou base
  chegar de forma transitiva põe os nativos do Mac numa compilação Windows ao lado dos do Windows.
- **A propriedade é `fx.platform`, não `javafx.platform`.** Definida de fora, a propriedade própria do
  OpenJFX é redefinida em função de si mesma pelo seu pom pai; o Maven rejeita esse pom como inválido e
  descarta em silêncio os jars de plataforma, deixando uma compilação sem JavaFX.

As duas disposições escrevem o mesmo caminho, `target/dist/gaia-desktop-<version>-<platform>/gaia-desktop.jar`,
e em cada compilação corre exatamente uma delas. `${jar.phase}`, `assembly.skipAssembly` e `mdep.skip` são
o que impede que ambas o reclamem.

| | Predefinição | `-Pfat` |
|---|---|---|
| `gaia-desktop.jar` | ~290 KB — apenas as classes e recursos deste módulo | ~50 MB — dependências descompactadas lá dentro |
| `dist/gaia-desktop-<version>-<platform>/lib/` | 17 jars, nomeados um a um no `Class-Path` do manifesto | — |
| A distribuir | o jar **e** `lib/` ao lado | um único ficheiro |
| Também em `dist/gaia-desktop-<version>-<platform>/` | `run.bat` (win) ou `run.sh` (as outras) | o mesmo |
| E | `readme.html` e `docs/` (todas as plataformas) | o mesmo |

Cada distribuição leva também os guias: `docs/` copiada mantendo a estrutura, para que as ligações entre
guias e traduções continuem a funcionar, sem as ferramentas de tradução (`check-i18n.py`,
`i18n-manifest.json`, `i18n-plan.md`); e `readme.html` na raiz, um índice com ligações para cada um, a
partir de `src/main/dist/readme.html` com `@VERSION@` filtrado para a versão do pom. Ambos são escritos pelo
passo antrun `pick-launch-script`, que apaga primeiro `docs/` para que um guia removido não fique para trás.
As ligações do readme são escritas à mão — um guia renomeado ou uma tradução adicionada em `docs/` precisa
também da sua linha lá.

O `Class-Path` do manifesto e `lib/` são duas metades da mesma coisa: o jar nomeia
`lib/<artifact>-<version>.jar` para cada dependência de execução, e `copy-dependencies` preenche `lib/` com
exatamente esses nomes. É isso que faz `java -jar` funcionar.

#### A aplicação macOS e o zip de distribuição

Uma execução do antrun, `mac-app-and-zip`, encerra a fase `package`. O plugin antrun é declarado **depois**
dos plugins jar e assembly por essa razão — o Maven corre as execuções de uma fase pela ordem de declaração
dos plugins —, pelo que o jar existe, em qualquer dos empacotamentos, quando ela corre. (A sua outra
execução, `pick-launch-script`, está em `prepare-package` e não é afetada.)

Para `mac` e `mac-aarch64` começa por construir `GAIA Desktop.app` na pasta de distribuição: `Info.plist`
(com `@VERSION@` filtrado para a versão do pom), o lançador `Contents/MacOS/gaia-desktop` (tornado
executável) e `gaia-desktop.icns`, tudo a partir de `src/main/macos/`, e depois **move** `gaia-desktop.jar`
e `lib/` para `Contents/Java`, para que o bundle seja autónomo. `run.sh` procura lá quando não há nenhum jar
ao seu lado. O lançador encontra o Java pela versão (`java_home -v 21+`, depois o openjdk do Homebrew — os
arranques a partir do Finder não veem `JAVA_HOME` nem `PATH`), mostra uma caixa de diálogo através de
`osascript` quando não há nenhum, faz `cd` para `~/.gaia-desktop` para que a pasta relativa `logs/` do
logback seja gravável, e passa `-Xdock:name`/`-Xdock:icon`. O macOS associa o `java` em execução ao bundle
(o seu identificador de bundle e o ícone da Dock são os da aplicação); a barra de menus mostra GAIA Desktop.

Depois, para cada plataforma, comprime a pasta em `target/dist/gaia-desktop-<version>-<platform>.zip` sob
uma pasta com o mesmo nome, com o `<zip>` do Ant: `run.sh` e `Contents/MacOS/**` como um conjunto de
ficheiros à parte com `filemode="755"`, ou seriam descomprimidos sem poderem ser executados. O zip **não é
anexado**: destina-se às releases do GitHub, e as quatro plataformas vêm de quatro compilações, que o Maven
Central — um único envio imutável por versão — nunca poderia aceitar como uma só release.

`project.build.outputTimestamp` é definido para a hora da compilação. Caso contrário, os arquivadores do
maven-jar-plugin 3.5 e do maven-assembly-plugin 3.8 datam cada entrada de 1 de fevereiro de 1980.

O ícone da aplicação é desenhado por `tools/icon/GaiaIcon.java` (não faz parte da compilação; o seu
cabeçalho indica os passos de `sips`/`iconutil`). Os seus resultados estão no repositório:
`src/main/macos/gaia-desktop.icns` para o bundle, e `src/main/resources/icons/gaia-desktop-{16..256}.png`, que
`GaiaDesktopApp` define como ícone da janela (Windows e Linux; o macOS ignora ícones de janela).

#### Os scripts de arranque

`run.sh` e `run.bat` vêm de `src/main/scripts/`, copiados para `dist/gaia-desktop-<version>-<platform>/` sem
filtragem — `run.sh` está cheio de expansões de shell `${...}` que a filtragem do Maven poderia reescrever à
vontade. Um passo do antrun mantém depois só o que a plataforma executa — `run.bat` para `win`, `run.sh` para
todas as outras — e ativa o bit de execução em `run.sh`, já que `copy-resources` não o conserva. Decide a
partir do `${fx.platform}` resolvido e não através de perfis, porque a ativação de um perfil não vê um valor
que outro perfil definiu: uma compilação Linux feita em Windows receberia de outro modo o script do
anfitrião.

**Não dependem do manifesto.** Cada um constrói o class path por si, `gaia-desktop.jar` seguido de cada
`.jar` em `lib/`, nomeados um a um em vez de deixados a um caráter universal `lib/*`, para que um caminho com
espaços continue a ser uma só entrada e a contagem do que foi adicionado seja real. Lançar com `-cp` é a
razão por que nomeiam `tools.pantheum.gaia.desktop.Launcher` explicitamente: `-cp` não lê a `Main-Class` do
manifesto. **Segue**, no entanto, o seu `Class-Path` — um jar em `-cp` traz os jars que o seu manifesto
nomeia, procurados logo a seguir —, pelo que cada jar de `lib/` é carregado quer um script o liste quer não,
e deixar um fora do class path do script não o mantém fora da JVM. Os scripts listam-nos mesmo assim, para
que um jar cujo manifesto esteja desatualizado, editado à mão ou incompleto arranque na mesma. O
mesmo script serve ambos os empacotamentos: o jar `-Pfat` não tem `lib/` para adicionar nem precisa, e é
também por isso que uma `lib/` vazia é uma nota e não um erro.

#### As bibliotecas nativas da Zebra

A compilação não distribui nada da Zebra — nem DLL, nem jars de serviço. No Windows vêm do Scanner SDK da
Zebra instalado na máquina: `run.bat` acrescenta os seus jars de serviço `Jpos*.jar` e `bin\` ao class path
e `bin\` ao `PATH`, onde `System.loadLibrary` encontra `CSJPOSScanner.dll` / `CSJPOSScanner64.dll`
(`java.library.path` usa por omissão o `PATH`). A API JavaPOS e o seu carregador de configuração vêm sempre
de `lib\javapos-*.jar`, com ou sem SDK.

**`run.bat` nunca acrescenta o próprio jar JavaPOS do SDK**, `External\JavaPOS-1.14.0-Dist\javaPOS114.jar`.
Já o fez, deixando de fora `lib\javapos-*.jar` para evitar duas cópias da API — mas o `Class-Path` do
manifesto carregava-os de qualquer forma (ver acima), pelo que a JVM recebia ambos: as classes do carregador
4.x de `lib\`, com `jpos/res/jpos.properties` e `jcl.xsd` de `javaPOS114.jar` ao lado. O ficheiro de
propriedades faz [`JposConfig`](#integração-do-leitor) pôr-se de lado, e esse `jcl.xsd` só aceita versões do
JavaPOS até à 1.10, pelo que cada entrada de `jpos.xml` (1.12–1.14) falhava a validação, o registo ficava
vazio e cada ligação falhava com o erro 109 antes mesmo de chegar ao leitor. Basta ter o jar no class path
para isso, mesmo atrás dos jars de `lib\`. Os jars de serviço da Zebra correm sem alterações sobre a API 1.14
de `lib\` — abrir, reservar e ler foram verificados assim com um leitor real. As DLL deixadas à mão ao lado
de `run.bat` continuam a ser respeitadas: o script define então `-Djava.library.path=<dist>;%PATH%`,
acrescentando o `PATH` porque a propriedade substitui por completo o caminho de pesquisa predefinido.

`mvn javafx:run` não faz nada disto, pelo que reservar um leitor numa execução de desenvolvimento em
Windows exige pôr por outros meios os jars do SDK no class path e o seu `bin\` no `PATH`; para isso, execute
a partir de `dist/`.

Para além do class path, cobrem o que `java -jar` não faz: resolver a sua própria pasta para funcionarem a
partir de qualquer pasta de trabalho, preferir `JAVA_HOME` ao `PATH`, e rejeitar uma JVM anterior à 11 com
uma frase que indica o remédio em vez de `UnsupportedClassVersionError`. `GAIA_DESKTOP_OPTS` passa opções à
JVM. `run.bat` usa `java.exe`, não `javaw.exe`, para que a consola fique aberta — o Windows é a única
plataforma onde o leitor funciona, e o seu estado e os erros do JavaPOS vão para lá.

`run.bat` escolhe também o seu Java, JavaFX e serviços Zebra conforme o que encontra — um `jdk*\` incluído, um
`javafx-sdk*\lib\` no module path, o Scanner SDK da Zebra em `ZEBRA_HOME` —, deixando fora do class path as
cópias do JavaFX de `lib\` quando usa as de um SDK, e passa o analisador SAX do JDK, os proxies e o
repositório de certificados do Windows, e `--enable-native-access` em Java 17 ou mais recente. A secção do
README [What `run.bat` adds on Windows](../../../README.md#what-runbat-adds-on-windows) tem a tabela.
Mantenha os fins de linha CRLF do ficheiro: o `cmd.exe` pode falhar um `call :label` num ficheiro batch só
com LF. `run.sh` também passa `--enable-native-access=ALL-UNNAMED` — o JavaFX carrega os seus próprios
nativos de `lib/`, e com o JavaFX no class path `ALL-UNNAMED` é o módulo que o abrange —, omitido, como em
`run.bat`, apenas quando não é possível ler a versão do Java.

Qualquer das disposições leva os nativos do JavaFX **de uma só plataforma**, `${fx.platform}`, pelo que
nenhuma é portável entre sistemas operativos ou arquiteturas. Compile uma vez por destino — veja
[Uma compilação por plataforma](#uma-compilação-por-plataforma).

> **Eclipse.** A execução `copy-dependencies` tem uma instrução `<?m2e ignore?>`. O m2e resolve um projeto
> aberto do espaço de trabalho para a sua pasta `target/classes` em vez de um jar, e `copy-dependencies`
> recusa-se a copiar um artefacto não empacotado (MDEP-187), o que faria falhar a compilação automática
> sempre que o projeto `gaia` estivesse aberto ao lado deste. A consequência é que uma compilação feita só
> no IDE não preenche `target/dist/gaia-desktop-<version>-<platform>/lib/`; execute `mvn package` antes de
> usar o próprio jar.

---

## Arquitetura

| Pacote | Contém |
|---|---|
| `desktop` | `GaiaDesktopApp`, `Launcher`, `Theme`, `GaiaDesktopVersion` |
| `desktop.ui` | `MainView` (a janela), `BuilderPane`, `OptionsPane`, `HistoryCell`, `EditCell` |
| `desktop.build` | `ElementBuilder` (lógica de composição sem JavaFX), `AiPair` (uma linha do editor) |
| `desktop.parse` | `ParseOptions` (`ParseConfig` observável), `SystemDefaults`, `Languages`, `GsToken` |
| `desktop.scanner` | `ScannerService` (JavaPOS), `ScannerListener`, `Symbologies` |
| `desktop.html` | `GaiaDesktopReport` — o gerador do relatório, sem JavaFX e com testes unitários sem o toolkit |
| `desktop.i18n` | `UiStrings` — o catálogo de etiquetas |
| `desktop.store` | `AppPaths`, `OptionsStore`, `CaptureStore` |

### O funil de captura

Cada entrada — escrita, colada, aberta a partir de um ficheiro, lida pelo leitor ou composta — chega ao
analisador por um único método, `MainView.capture(String, ScanSource, String)`.

Essa é a única decisão de conceção em torno da qual a aplicação é construída. Uma composição não precisa de
relatório próprio, nem de histórico próprio, nem de caminho de exportação próprio, porque depois do funil uma
cadeia composta é indistinguível de uma lida. Qualquer coisa nova que produza uma entrada deve passar por ele
e não ao lado dele.

### Threads

| Thread | Executa |
|---|---|
| Thread da aplicação JavaFX | Toda a análise, a renderização e a interface. Com as tabelas de AI já quentes a análise demora menos de um milissegundo, pelo que não precisa de nenhum thread de trabalho |
| `gaia-scanner` | Cada chamada ao leitor — abrir, reservar, ativar, desativar, libertar, fechar. Um thread de longa duração pertencente a `ScannerService`; veja [Integração do leitor](#integração-do-leitor) |
| Thread de eventos JavaPOS | As callbacks de `ScannerService`. `MainView.FxScannerListener` passa cada uma com `Platform.runLater` |
| Segundo plano | A espera por `connect()` / `disconnect()` do leitor (a reserva pode demorar até 5 s), a reposição do histórico guardado no arranque — que serve também de aquecimento do analisador — e a verificação de atualizações (`gaia-update-check`, um pedido ao GitHub, com o resultado devolvido via `Platform.runLater`) |

**Nunca altere a seleção de um controlo, nem destrua o controlo, a partir do seu próprio evento de
seleção.** Três falhas distintas desta aplicação vieram exatamente daí: repor o seletor de AI dentro do
próprio `clearAndSelect` da janela pop-up (uma `IndexOutOfBoundsException` vinda de `ListViewBehavior`), e
reconstruir o painel que contém a caixa de idioma a partir do próprio handler dessa caixa. Ambos são agora
adiados com `Platform.runLater`, e os motivos estão registados nos dois pontos de chamada.

### Reconstruir ou reetiquetar

Uma mudança de idioma **reconstrói** a moldura a partir do catálogo — menus, barra de ferramentas, painel de
opções —, o que é seguro porque nada disso guarda estado: `OptionsPane` é uma vista bidirecional de
`ParseOptions`, pelo que os valores vivem no modelo e um painel novo volta a apanhá-los de imediato.

Os controlos que contêm **dados do utilizador** são campos e são antes reetiquetados no lugar:
`BuilderPane.retext(UiStrings)` mantém as linhas de AI, os campos de Digital Link e a seleção. Reconstruí-lo
deitaria fora trabalho em curso — o que já aconteceu uma vez.

---

## O token {GS}

Um `TextField` do JavaFX descarta em silêncio todos os carateres abaixo de `0x20`
(`TextInputControl.filterInput`). Não «o mostra de forma estranha» — remove-o. Escrever, colar, `setText` e
`insertText` são todos afetados, pelo que `setText("01…" + GS + "10LOT")` dá `"01…10LOT"`: uma cadeia de
elementos cujo significado mudou sem aviso, porque o AI de comprimento variável engole agora o que se lhe
seguia.

`desktop.parse.GsToken` é toda a resposta. O separador atravessa a interface como o texto literal `{GS}` e
volta a ser `0x1D` a caminho do analisador. O token não foi inventado aqui — é exatamente a forma como
`ASCIIRevealerUtils.printable` do núcleo mostra `0x1D`, pelo que um valor copiado de um relatório se cola tal
e qual na barra de entrada.

Um filtro `TextFormatter` no campo de entrada apanha todas as vias por que o texto pode chegar — escrita,
⌘V, o menu de contexto, arrastar e largar, `setText` —, e é por isso que a reescrita vive aí e não em cada
handler.

O caminho do leitor nunca passa por `GsToken`: as leituras chegam ao analisador como bytes.

---

## Integração do leitor

`ScannerService` detém o controlo `Scanner` do JavaPOS: abrir → reservar → ativar, rearmando o evento de
dados após cada leitura. Os nomes lógicos dos dispositivos vêm de `src/main/resources/jpos.xml`.

**Cada chamada ao dispositivo corre num só thread, `gaia-scanner`, que vive tanto como o serviço** — nunca
no thread FX, e nunca num thread que termine depois da chamada. O serviço da Zebra chega ao leitor por JNI
através da API COM do CoreScanner, e o COM liga o que prepara ao apartment do thread que chama: abra num
thread que depois termina e o leitor é reservado mas fica em silêncio; feche noutro thread e é
desinicializado o apartment errado. Fechar no thread FX é o pior caso, porque o JavaFX chama aí
`OleInitialize` e regista nele o destino de largada de cada janela — um `CoUninitialize` nesse thread acaba
com o arrastar e largar em toda a aplicação. Isto é o JavaFX em Windows, não uma versão concreta do JavaFX.
`connect()` e `disconnect()` submetem a esse thread e esperam, pelo que `MainView` chama ambas a partir de
um thread de segundo plano de curta duração.

Duas regras impedem que um dispositivo que se porta mal bloqueie a ligação. **O desmantelamento nunca pára
antes do tempo:** `closeQuietly` desativa, liberta e fecha em passos separados, só liberta quando a reserva
está detida, e tenta sempre `close()` — depois de uma reserva falhada, `release()` lança «not claimed», e
quando os passos partilhavam um único `try` isso saltava o fecho e deixava o dispositivo aberto no serviço
da Zebra. **O evento de dados é rearmado num `finally`:** o JavaPOS desativa-o após cada evento, pelo que uma
leitura ou um handler que lance uma exceção tem mesmo assim de rearmar, ou o leitor fica em silêncio até uma
nova ligação. `ScannerLifecycleTest` põe ambas as regras à prova contra um `jpos.Scanner` guionizado.

**`JposConfig` aponta o JavaPOS para o `jpos.xml` incluído** antes da primeira abertura. Com os jars do
JavaPOS do Maven (`javapos-config-loader` 4.x) e sem `jpos/res/jpos.properties` no class path, toda a
abertura falha de outro modo com *"Service does not exist in loaded JCL registry"* (erro 109), por três
razões distintas: o gestor de serviços recorre a `SimpleRegPopulator`, que lê um `jpos.cfg` serializado e
nunca `jpos.xml`; o carregador XML só procura `jpos.xml` na pasta de trabalho, não dentro do jar; e o Xerces
2.12.2 em `lib/` rejeita a propriedade JAXP `accessExternalDTD` que o carregador define. `JposConfig` define
`jpos.config.regPopulatorClass` (`JavaxRegPopulator`), `jpos.config.populatorFileURL` (o próprio
`jar:file:…!/jpos.xml` do jar) e `javax.xml.parsers.SAXParserFactory` (a do JDK) como propriedades de
sistema — nunca uma já definida, pelo que `-Djpos.config.populatorFileURL=file:…` continua a selecionar
outro `jpos.xml` — e não define nada quando há um `jpos.properties` no class path ou o carregador 4.x não
está lá. O `javaPOS114.jar` do Zebra SDK é um desses casos e estraga o registo de vez, e é por isso que
`run.bat` o mantém fora do class path — veja [As bibliotecas nativas da Zebra](#as-bibliotecas-nativas-da-zebra).
`JposConfigTest` verifica de ponta a ponta que o registo contém então os leitores do ficheiro.

**O seletor de dispositivo é gerado a partir desse registo**, não mantido como uma lista no código:
`ScannerService.logicalNames()` lê as entradas carregadas no thread `gaia-scanner` e mantém aquelas cuja
categoria de dispositivo é `Scanner` — as duas entradas de balança de `jpos.xml` ficam de fora —, pela ordem
`pickerOrder`: `ZebraAllScanners` primeiro quando o ficheiro o define, o resto por ordem alfabética (o
registo não guarda a ordem do ficheiro). Um ficheiro sem ele não o recebe; sem nada carregado, só o
predefinido é oferecido, pelo que o seletor nunca está vazio. `MainView` mostra de imediato o predefinido e
completa a lista a partir de um thread de segundo plano, mantendo a escolha guardada. Assim, um leitor
acrescentado a `jpos.xml` — ou todo um ficheiro indicado com `-Djpos.config.populatorFileURL` — aparece no
seletor sem alterar código, e `JposConfigTest` compara o seletor com uma leitura independente do ficheiro
incluído. Os serviços que essas entradas nomeiam são classes da Zebra
(`com.zebra.jpos.service.scanner.*Factory`), que continuam a ter de estar no class path — a partir do Scanner
SDK via `run.bat`, ou como jars de serviço da Zebra deixados em `lib\`.

Os dados lidos são descodificados como **ISO-8859-1** para que cada byte corresponda 1:1 e sobrevivam tanto
o prefixo AIM Code ID como cada `0x1D`; os relatórios são escritos em UTF-8.

Nada no serviço compõe texto para um utilizador. Os eventos saem como constantes de
`ScannerListener.Event` mais os valores que variam, e `MainView` fornece o texto a partir do catálogo — caso
contrário a linha de estado estaria em inglês numa aplicação traduzida de 35 maneiras. As linhas de registo
são a exceção e ficam em inglês.

### A barreira de plataforma

`ScannerService.isSupportedPlatform()` é um único teste a `os.name`, e `MainView` consulta-o antes de
construir qualquer controlo do leitor: em macOS e Linux não há interruptor, nem seletor de dispositivo, nem
menu Leitor (e portanto nem <kbd>⌘K</kbd>), nem etiqueta de estado. Não estão escondidos — nunca são
construídos.

A Zebra só distribui os seus serviços JavaPOS para Windows, e noutros sistemas nada falha cedo: os jars
resolvem-se e o controlo constrói-se, e é `open()` que acaba por falhar por falta da camada nativa, vários
segundos e uma caixa de diálogo depois.

A regra diz respeito aos *serviços*, não ao JavaPOS. Um fornecedor que distribuísse um serviço
multiplataforma funcionaria aqui, e alargar a regra é uma alteração apenas a `supportsJavaPos(String)` — que
é privado ao pacote e testado contra cadeias de `os.name`, para que ambas as respostas fiquem cobertas em
qualquer máquina que compile.

### Simbologias

`Symbologies` faz corresponder todos os **78** tipos de dados de leitura `SCAN_SDT_*` do JavaPOS a nomes de
apresentação, e ao `DataCarrierType` do núcleo onde os dois vocabulários coincidem. O JavaPOS é o mais fino
dos dois — separa UPC-A de EAN-13, e Data Matrix simples de GS1 DataMatrix, onde o AIM agrupa uma família
inteira sob um único caráter de código.

Um tipo fora da tabela recebe mesmo assim um nome utilizável, encontrado por reflexão sobre `ScannerConst`,
porque os serviços dos fornecedores comunicam de facto valores fora da lista da especificação. Um teste
percorre `ScannerConst` e falha se algum tipo declarado não tiver nome — o que apanha também constantes que
um futuro JavaPOS acrescente.

Algumas correspondências de suporte ficam deliberadamente em `UNKNOWN` em vez de adivinhadas: o 2 de 5
simples (o AIM separa `]R`/`]S` pelo número de barras de início/fim, que o JavaPOS não comunica), os códigos
postais de quatro estados e TLC-39.

---

## Localização

As etiquetas vivem num ficheiro de propriedades por idioma em
`src/main/resources/i18n/ui_<CODE>.properties`, com os mesmos códigos que os catálogos de erros do núcleo
(`EN`, `FR`, `PCM`, …). Existem os 35 e os 35 têm um conjunto de chaves idêntico. Os ficheiros são lidos
como **UTF-8**, e não como o ISO-8859-1 que `Properties` pressupõe, para que as traduções fiquem guardadas de
forma legível e não como escapes `\uXXXX`.

**O recurso é por chave, não por ficheiro** — é isso que torna útil um idioma parcialmente traduzido logo
que chega a sua primeira chave. O inglês é o contrato: todas as chaves têm de existir nele.

| Camada | Chaves | Cobertura |
|---|---|---|
| Vocabulário partilhado do relatório | 31 | todos os 35 idiomas |
| Moldura do desktop — menus, botões, compositor, opções, linha de estado, paginação do relatório, estado do leitor, textos das caixas de diálogo | 114 | todos os 35 idiomas |

Seis dos termos partilhados (`generated`, `status`, `dlRole`, `interpretations`, `message`, `level`) não são
usados pelo relatório desta aplicação.
São mantidos de propósito; o cabeçalho do ficheiro di-lo.

### O argumento `--language`

`GaiaDesktopApp.languageOverride()` lê `--language=…` através do próprio `getParameters().getNamed()` do
JavaFX, resolve-o com `Languages.fromArgument` — código de catálogo, nome do enum ou etiqueta BCP 47, sem
distinguir maiúsculas — e passa-o a `MainView`.

**A substituição vale para a execução, não para o ficheiro.** `MainView` guarda em `savedLanguage` o idioma
que carregou e escreve de volta *esse*, até que `languageChosenInSession` indique que o utilizador escolheu
um idioma por si; a partir daí a sua escolha persiste normalmente. Essa separação é todo o desenho: um
perfil de quiosque ou um atalho de demonstração pode abrir em espanhol sem reescrever aquilo que o dono da
máquina abre amanhã, enquanto uma escolha real feita no painel de opções se mantém.

Um valor que não nomeia nenhum catálogo é registado com os códigos aceites e ignorado. Recusar arrancar por
causa de um erro de escrita num atalho seria a falha pior, e `Languages.fromArgument` devolve `null` em vez
de lançar uma exceção pela mesma razão.

### Adicionar um idioma

Não é preciso alterar código.

1. Edite `ui_<CODE>.properties` — todos os idiomas já têm um ficheiro.
2. Traduza os valores. Deixe no lugar os marcadores `{0}` / `{1}`.
3. Compile. Dois testes fazem cumprir o contrato: um falha se o código pedir uma chave que o inglês não
   define, o outro se o conjunto de chaves de algum catálogo se afastar do inglês.

O primeiro teste percorre as **fontes** à procura de literais de chave em vez de usar reflexão sobre as
classes, porque as chaves só existem como literais de cadeia nos pontos de chamada. É por isso que uma
mensagem nova tem de ser construída com a chave escrita em linha — `strings.format("st.scannerError", …)` —
e não procurada através de uma variável, ou o teste não a consegue ver.

### O que deliberadamente não é traduzido

- os nomes de campo concisos em letra monoespaçada ao lado dos valores em bruto — `format`, `position`,
  `length`, `raw` e `part` do cartão de elemento, `AI`, `sep`, `pos` e `len` da fita, e as quatro células
  que localizam um erro (`AI`, `position`, `level`, `stage`), cujos valores são de qualquer forma nomes de
  enum não traduzidos;
- os nomes das entradas de exemplo, que nomeiam construções GS1, e os nomes das simbologias, que são nomes
  próprios;
- o parágrafo de correção de `GE-C084` — prosa normativa, onde uma meia tradução é pior do que o inglês;
- a salvaguarda de AI desconhecido de `ElementBuilder`, inalcançável a partir da interface: o editor verifica
  ele próprio os AI desconhecidos e comunica-os a partir do catálogo;
- as linhas de registo.

Resta uma lacuna: `ScanSource.label()` fornece `manual` / `scanner` / `file` / `built` em inglês à linha do
histórico e ao cabeçalho do relatório. Fechá-la exige mais do que uma chave — `HistoryCell` não tem
`UiStrings`, e a fábrica de células da lista é instalada uma única vez, pelo que as células também teriam de
ser atualizadas numa mudança de idioma.

---

## Formatos do estado guardado

Dois ficheiros em `~/.gaia-desktop/`, mantidos à parte porque mudam a ritmos diferentes — um registo de
capturas corrompido ou editado à mão não deve custar ao utilizador as suas definições. Altere a pasta com
`-Dgaia.desktop.home=…`, que é como os testes ficam fora da pasta pessoal real.

**`options.properties`** — escrito quando muda uma opção, e à saída.

| Chave | Significado |
|---|---|
| `parse.mode`, `parse.language` | Profundidade do processamento e idioma das mensagens |
| `date.endian`, `date.separator`, `date.month`, `date.year` | Apresentação das datas |
| `check.skipRequires`, `check.skipExcludes` | Exceções às verificações estruturais |
| `ui.theme` | `LIGHT` ou `DARK` |
| `ui.optionsPane` | Painel aberto? |
| `ui.scannerDevice` | Último nome lógico JavaPOS |
| `ui.mode` | `PARSE`, `BUILD` ou `COMBINE` |
| `update.check` | Se a verificação diária de atualizações corre (predefinição `true`) |
| `update.lastCheck` | Quando correu pela última vez, em ISO-8601; ausente ou ilegível significa que há uma verificação em falta |
| `update.skipVersion` | Uma release para a qual o utilizador escolheu *Ignorar esta versão*; não volta a ser oferecida automaticamente |

Um ficheiro em falta ou danificado nunca é fatal: cada valor recorre ao seu valor de recurso
individualmente, pelo que uma linha má só custa essa linha. No primeiro arranque, sem ficheiro algum, as
opções são inicializadas a partir das definições regionais da máquina por `SystemDefaults` e não a partir
das predefinições do GAIA — o formato de data curto da região dá a ordem dos componentes, o separador, e se
o mês e o ano são abreviados.

Nunca passe um null a `Properties.setProperty`: lança uma exceção, e `persistOptions()` corre a partir de
handlers de eventos da interface, onde uma exceção escapa para o thread FX. É uma falha real que esta
aplicação já teve.

**`captures.tsv`** — escrito quando o histórico muda, e à saída. As mais recentes primeiro, com um máximo de
**500** entradas.

```
captured(ISO)              <TAB> source  <TAB> symbology   <TAB> input
2026-08-09T14:22:05.123    SCANNER       Data Matrix       0109506000134352\x1D10LOT-001
```

Só se guarda o que não pode ser recalculado. O `ParseResult` **não** é serializado — é reobtido voltando a
analisar a entrada ao carregar, pelo que um histórico reposto reflete as opções em vigor *agora*. As
entradas contêm frequentemente FNC1 e tudo o mais que um leitor emita, pelo que o campo de entrada é escapado
(`\\`, `\t`, `\n`, `\r`, e `\xNN` para tudo o resto abaixo de `0x20`) e o ficheiro é lido e escrito como
ISO-8859-1, byte a byte.

Os ficheiros escritos antes de a data ser registada contêm só uma hora; continuam a carregar, datados de
hoje, porque a data real nunca foi anotada.

---

## Testes

```bash
(cd gaia-desktop && mvn test)
```

198 testes, deliberadamente **independentes do toolkit JavaFX**, o que os mantém rápidos e sem interface
gráfica. Cobrem o gerador do relatório, a lógica de composição e as pesquisas de AI, a conversão de `{GS}`,
as predefinições derivadas da região, ambos os armazéns (incluindo uma ida e volta com `0x1D` e ficheiros
danificados de propósito), a tabela de simbologias, a regra de plataforma do leitor, o stub de versão e os
catálogos.

Três merecem ser conhecidos porque defendem contratos em vez de funções:

| Teste | Falha quando |
|---|---|
| `CatalogueCoverageTest` | O código pede uma chave que `ui_EN.properties` não define, ou algum dos 35 ficheiros se afasta do conjunto de chaves inglês |
| `SymbologiesTest` | Alguma constante `SCAN_SDT_*` declarada pelo JavaPOS não tem nome de apresentação — incluindo uma que uma atualização do JavaPOS acrescente |
| `ScannerServiceTest` | A regra de plataforma muda de resposta para Windows ou para qualquer outro sistema |

`UiStrings` é deliberadamente tolerante em execução — uma chave desconhecida aparece como a própria chave em
vez de lançar uma exceção a meio da paginação —, e é precisamente por isso que o teste do catálogo tem de
existir.

**Não coberto:** tudo o que precise de um toolkit em funcionamento — o comportamento do foco, a mudança de
modo, a edição de tabelas, a mudança de idioma, e ambas as falhas descritas em [Threads](#threads). Foram
verificadas a manobrar a janela real, mas uma regressão nelas não faria falhar a compilação. Acrescentar
TestFX ou Monocle fecharia essa lacuna.

---

## Resolução de problemas de compilação

| Sintoma | Causa e solução |
|---|---|
| `package tools.pantheum.gaia does not exist` | O núcleo não está instalado. `(cd gaia && mvn install -DskipTests)` |
| *"JavaFX runtime components are missing"* | A `Main-Class` do jar tem de ser `Launcher`, não `GaiaDesktopApp` |
| *"Unsupported JavaFX configuration: classes were loaded from unnamed module"* no arranque | Esperado e inofensivo — o JavaFX nota que está no class path. É exatamente por isso que `Launcher` existe |
| O jar não arranca, `NoClassDefFoundError` | O jar predefinido só leva as classes deste módulo; `dist/lib/` tem de estar ao lado. Use `-Pfat` para um único ficheiro autónomo |
| `Artifact has not been packaged yet … MDEP-187` no Eclipse | Resolução do espaço de trabalho do m2e. Veja a nota em [Empacotamento](#empacotamento) |
| Aparecem ficheiros `… 2.properties` duplicados numa compilação | O projeto está numa pasta sincronizada com o iCloud e a sincronização criou cópias em conflito a meio da compilação. Faça sempre `mvn clean package`; melhor ainda, exclua `target/` da sincronização |
| Um item de menu mostra uma chave como `act.hri` | Essa chave falta em `ui_EN.properties`. A compilação devia tê-lo detetado — veja [Testes](#testes) |

---

## Limitações conhecidas e trabalho em aberto

- **Não há testes de interface sem ecrã.** Veja [Testes](#testes).
- **Artefactos específicos de cada plataforma.** Ambos os empacotamentos incluem os nativos do JavaFX de uma
  só plataforma; compile um por destino com `-Dfx.platform` (qualquer máquina pode compilá-los todos).
  Nenhum inclui um ambiente de execução Java — para isso é preciso `jpackage`, que só compila para o sistema
  operativo em que corre.
- **A entrada pelo leitor é só para Windows.** Veja [A barreira de plataforma](#a-barreira-de-plataforma).
- **`ScanSource.label()` não está localizado.** Veja [O que deliberadamente não é
  traduzido](#o-que-deliberadamente-não-é-traduzido).
- **`UiState.mode` é uma `String`** (`"PARSE"` / `"BUILD"`) enquanto `Theme`, ao lado, é um enum, pelo que o
  literal aparece em vários sítios. Um enum eliminá-los-ia.
- **O destino Digital Link limita-se ao domínio e ao prefixo de caminho.** `BuilderDigitalLinkConfig`
  suporta também parâmetros de consulta adicionais e um fragmento; o editor não os disponibiliza.
- **Os pares de AI mutuamente exclusivos (`GE-S006`) não são assinalados no editor** como acontece com os AI
  desconhecidos e duplicados — a composição simplesmente falha.
- **O catálogo de erros do núcleo duplica as aspas** (`value ''09506000134350''`), o que aparece tal e qual
  nas falhas de composição e no relatório. Parece um escape de aspas do `MessageFormat` que não é desfeito; a
  correção cabe ao núcleo.
