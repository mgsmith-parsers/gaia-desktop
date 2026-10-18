# GAIA Desktop — Guia do utilizador

Como usar a aplicação. Para a compilar, alterar ou acrescentar um idioma, consulte o
[guia do programador](../../GaiaDesktop-Developer.md) (em inglês). Documento original:
[GaiaDesktop-User.md](../../GaiaDesktop-User.md).

## Índice

1. [O que é](#o-que-é)
2. [Executar](#executar)
3. [A janela](#a-janela)
4. [Modo Analisar](#modo-analisar)
   - [A barra de entrada e o token {GS}](#a-barra-de-entrada-e-o-token-gs)
   - [Leitura com leitor](#leitura-com-leitor)
   - [Abrir um ficheiro](#abrir-um-ficheiro)
   - [O histórico de capturas](#o-histórico-de-capturas)
5. [Modo Compor](#modo-compor)
   - [O editor de AI](#o-editor-de-ai)
   - [O que o editor recusa](#o-que-o-editor-recusa)
   - [Cadeia de elementos ou Digital Link](#cadeia-de-elementos-ou-digital-link)
   - [Editar uma captura existente](#editar-uma-captura-existente)
6. [Modo Combinar](#modo-combinar)
   - [A faixa Combinar](#a-faixa-combinar)
   - [O que Combinar recusa](#o-que-combinar-recusa)
   - [Duplicados e conflitos](#duplicados-e-conflitos)
   - [O relatório de combinação](#o-relatório-de-combinação)
7. [O relatório](#o-relatório)
   - [Exportar](#exportar)
8. [Opções de análise](#opções-de-análise)
   - [Primeiro arranque](#primeiro-arranque)
9. [Idiomas](#idiomas)
10. [O que é memorizado](#o-que-é-memorizado)
11. [Atalhos de teclado](#atalhos-de-teclado)
12. [Resolução de problemas](#resolução-de-problemas)
13. [Limites](#limites)

---

## O que é

O GAIA Desktop faz três coisas com dados GS1:

- **Analisar** — escreva, cole, abra ou leia com um leitor uma cadeia de elementos GS1 ou um URI
  Digital Link e leia o resultado como um relatório completo: o que significa cada identificador
  de aplicação (AI), o que está errado e porquê;
- **Compor** — juntar pares AI / valor numa cadeia de elementos ou num URI Digital Link;
- **Combinar** — juntar até três códigos de barras GS1-128 de uma mesma etiqueta na
  única cadeia de elementos que um GS1 DataMatrix transportaria, verificá-la como um todo e
  desenhar o símbolo.

Tudo o que a aplicação sabe sobre GS1 — cada definição de AI, cada regra de validação, cada
mensagem de erro e cada descrição localizada — vem do motor GAIA que está por baixo. A aplicação
não acrescenta regras próprias: aceita exatamente o que o motor aceita.

---

## Executar

```bash
java -jar gaia-desktop.jar
```

O ficheiro JAR precisa da pasta `lib/` distribuída ao seu lado — mantenha os dois juntos, ou a
aplicação não arranca. (Também é possível compilar um JAR autónomo; consulte o
[guia do programador](../../GaiaDesktop-Developer.md#packaging).)

É preciso ter instalado **Java 21 ou posterior**. A distribuição é específica de um sistema
operativo e de uma arquitetura de processador: use o JAR compilado para a sua máquina.

Para abrir a aplicação num idioma específico, sem alterar o idioma guardado:

```bash
java -jar gaia-desktop.jar --language=FR
```

O valor pode ser o código do catálogo (`FR`, `PCM`, `ARZ`), o nome completo (`FRENCH`,
`WU-CHINESE`) ou uma etiqueta de idioma (`fr`, `zh-HK` para cantonês, `ar-EG` para árabe
egípcio) — maiúsculas e minúsculas são indiferentes. A indicação vale apenas para essa
execução: o idioma guardado fica intacto, pelo que um atalho criado para um colega não pode
alterar o idioma em que a sua própria cópia abre. Escolher um idioma *dentro* da aplicação é uma
escolha verdadeira e fica guardada como sempre. Um valor que não designa idioma nenhum é
ignorado, com uma nota na consola, e a aplicação abre no idioma guardado em vez de se recusar a
arrancar.

No arranque, a consola imprime uma linha com a versão da aplicação, a data de compilação e a
versão do motor. É essa a linha a citar num relatório de erro.

---

## A janela

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

> O esquema mantém as etiquetas em inglês, tal como todos os blocos de código desta
> documentação. No ecrã verá *Analisar* / *Compor* / *Combinar*, *Capturas*, *Guardar relatório*, *Opções*,
> *leitor: desligado* e *Acerca*.

**No macOS e no Linux não existe a linha do leitor**: não há botão nem seletor de dispositivo na
barra de ferramentas, não há menu *Leitor* e não aparece *leitor: desligado* na barra de estado.
Consulte [Leitura com leitor](#leitura-com-leitor).

O seletor **Analisar ⇄ Compor** muda apenas a faixa por baixo da barra de ferramentas e os
controlos de captura. O histórico, o relatório, o painel de opções e as ações de exportação
servem os dois modos, porque uma composição bem-sucedida passa a ser simplesmente mais uma
captura. O modo **Combinar** tem outra disposição: consulte [Modo Combinar](#modo-combinar).

O painel de opções está **fechado no primeiro arranque**. O modo, a visibilidade do painel, o
tema e o último leitor usado são memorizados entre execuções.

**Novas versões.** Alguns segundos depois de arrancar, no máximo uma vez por dia, a aplicação
pergunta ao GitHub se foi publicada uma versão mais recente do GAIA Desktop. Se foi, aparece na
barra de estado uma ligação — *O GAIA Desktop 1.1.0 está disponível* — que abre a página da versão no
seu navegador; nada é transferido nem instalado por si. *Ignorar esta versão* oculta o aviso até
uma versão posterior. A verificação é silenciosa quando não há nada de novo ou quando não
consegue chegar ao GitHub. *Ajuda → Procurar atualizações…* verifica de imediato e diz o
que encontrou; *Ajuda → Procurar atualizações automaticamente* desliga e volta a ligar a verificação
diária.

---

## Modo Analisar

### A barra de entrada e o token {GS}

A barra aceita uma cadeia de elementos, um URI GS1 Digital Link ou uma leitura em bruto com o
seu prefixo AIM Code ID (`]d2…`). <kbd>Enter</kbd> analisa.

Os AI de comprimento variável são separados por **FNC1** — o carácter de controlo invisível
`0x1D`. Um campo de texto não consegue guardá-lo: seria eliminado em silêncio, e
`01…` + FNC1 + `10LOT` chegaria como `01…10LOT`, uma cadeia de elementos com outro significado.

Por isso a aplicação mostra o separador como o token visível **`{GS}`** e converte-o de volta a
caminho do analisador:

- o **botão `{GS}`** insere o token onde estiver o cursor;
- **colar** caracteres separadores verdadeiros — pelo teclado, pelo menu de contexto ou
  arrastando — transforma-os automaticamente em `{GS}`, pelo que nada se perde;
- o token é escrito da mesma forma que o relatório o escreve, portanto um valor copiado de um
  relatório pode ser colado de volta tal como está.

A captura, o relatório e o histórico guardado contêm todos o separador **verdadeiro**. O token é
apenas o que vê e escreve.

### Leitura com leitor

> **Apenas Windows.** A Zebra fornece os controladores do leitor só para Windows, pelo que no
> macOS e no Linux a aplicação não constrói qualquer controlo de leitura — não há botão *Ligar
> leitor*, nem seletor de dispositivo, nem menu *Leitor*, nem estado do leitor na barra de
> estado. Nada fica esbatido, porque não existe definição nem equipamento que o pudesse ativar.
> Tudo o resto, incluindo abrir um ficheiro de leituras guardadas, funciona em qualquer sistema.

*Ligar leitor* abre o dispositivo indicado no seletor; `ZebraAllScanners` é o valor
predefinido e serve para a maioria dos modelos. Cada leitura é analisada e acrescentada ao
histórico automaticamente.

Os dados do leitor são lidos byte a byte, pelo que o prefixo AIM Code ID e cada separador FNC1
chegam ao analisador exatamente como o leitor os enviou.

No modo Compor os controlos do leitor ficam ocultos, mas um leitor ligado continua a funcionar:
*Leitor → Ligar / Desligar* (<kbd>⌘K</kbd>) continua a alterná-lo, a barra de estado continua a
indicá-lo, e uma leitura que chegue com o editor aberto é capturada na mesma. No modo Combinar os controlos mantêm-se, e cada leitura é acrescentada à combinação em
vez de ao histórico.

A primeira configuração de um leitor — o Zebra Scanner SDK, o modo USB do leitor e as
definições GS1 — é explicada passo a passo no
[guia de configuração do leitor Zebra](GaiaDesktop-Zebra-Scanner-Windows-Portuguese.md), incluído em cada zip em `docs/`. Se um dispositivo não ligar, consulte [Resolução de problemas](#resolução-de-problemas).

### Abrir um ficheiro

*Ficheiro → Abrir ficheiro de texto…* analisa **cada linha não vazia como uma captura
independente**, que é a forma rápida de tratar um lote de leituras que alguém lhe enviou. O
ficheiro é lido byte a byte, pelo que os separadores em bruto lá dentro sobrevivem.

### O histórico de capturas

As mais recentes primeiro. Cada linha mostra um ponto de válido / inválido, um resumo de uma
linha e ainda a hora da captura, a origem e quantos AI contém. A data só aparece nas capturas de
um dia anterior; as linhas de hoje mostram apenas a hora.

O resumo é o **URI Digital Link** no caso de um Digital Link, e o **HRI** nos restantes: numa
cadeia de elementos, `(01)09506000134352 (17)261231` lê-se melhor do que
`01095060001343521726123110…`.

Clique com o botão direito numa captura para copiar a entrada ou o HRI, devolvê-la à barra de
entrada, abri-la no editor, removê-la ou limpar o histórico.

---

## Modo Compor

### O editor de AI

Uma tabela de pares AI / valor. Escreva um código de AI ou escolha entre **todos os AI que o
motor conhece** na lista *Adicionar um AI…*; as colunas **Título**, **Formato** e **Descrição**
preenchem-se à medida que escreve, no idioma escolhido. O seletor não consegue oferecer um AI
que a composição viesse a recusar.

A tabela mantém sempre **uma linha vazia no fim**: escreva nela e aparece a seguinte, pelo que
uma cadeia com vários AI nunca precisa de *Adicionar linha*.

A edição comporta-se como uma tabela deve comportar-se: <kbd>Enter</kbd>, <kbd>Tab</kbd>,
<kbd>Shift</kbd>+<kbd>Tab</kbd> e um clique fora **mantêm** o que escreveu; <kbd>Esc</kbd>
descarta. <kbd>Tab</kbd> passa de AI para Valor e depois para a linha seguinte, saltando as
colunas que não pode editar.

### O que o editor recusa

| | O que acontece |
|---|---|
| Um AI que o motor não reconhece | A linha fica vermelha enquanto escreve; a composição é recusada com uma explicação |
| O mesmo AI duas vezes, a partir do seletor | Não é acrescentado — uma mensagem nomeia-o e é selecionada a linha que já o tem |
| O mesmo AI duas vezes, escrito à mão | Ambas as linhas ficam marcadas; a composição é recusada com o erro `GE-S004` |
| Nada para compor | Uma mensagem; não é criada nenhuma captura |

Um AI só pode aparecer uma vez numa cadeia de elementos — daí o seletor recusar logo, em vez de
deixar tentar uma composição condenada a falhar.

### Cadeia de elementos ou Digital Link

**Compor cadeia de elementos** junta as linhas, inserindo o separador FNC1 onde a norma o exige.
**Compor Digital Link** produz um URI; expanda *Destino Digital Link* para o dirigir ao seu
próprio resolvedor em vez do canónico `id.gs1.org`:

| Campo | Efeito | Vazio significa |
|---|---|---|
| Domínio | O anfitrião do resolvedor, p. ex. `example.com` | `id.gs1.org` |
| Prefixo de caminho | Um segmento antes do caminho dos AI, p. ex. `/products` | sem prefixo |

Em caso de **êxito**, o resultado comporta-se tal como uma leitura: é analisado, acrescentado ao
histórico, apresentado como relatório e guardado. A composição valida analisando a sua própria
saída, pelo que tudo o que devolve é seguramente válido.

Em caso de **falha** nada é capturado. O motivo fica por baixo dos botões de composição, uma
linha por problema, cada uma com o erro, o AI a que pertence e a explicação do próprio motor:

```
GE-C003  (01)  Check digit validation failed for AI (01) value ''09506000134350''
```

> As aspas duplicadas nessa mensagem vêm do catálogo de erros do motor, não desta aplicação.

### Editar uma captura existente

Clique com o botão direito numa captura → **Compor** carrega os seus AI no editor. Altere um
valor, componha de novo, e o resultado surge como uma captura nova — a original fica intacta.

---

## Modo Combinar

As etiquetas mais antigas repartem muitas vezes os seus dados por **vários códigos de barras
GS1-128** — o GTIN num, a data de validade e o lote noutro — porque um único código de barras
linear não consegue conter tudo. Combinar recebe até **três** desses códigos de barras,
junta-os na única cadeia de elementos que um **GS1 DataMatrix** transportaria e verifica o
resultado como um todo. Quando é válido, a aplicação desenha o símbolo DataMatrix.

Nenhum desses códigos de barras precisa de estar completo por si só. Um número de lote sem o seu
GTIN falha sozinho com `GE-S005`, mas isso é normal num código de barras de um conjunto, por isso
Combinar verifica-o na combinação.

### A faixa Combinar

Ao mudar para **Combinar**, a barra de entrada é substituída por uma faixa ao longo da
parte inferior da janela. O histórico de capturas sai de cena, porque as leituras do modo
Combinar não são capturas e nunca entram nele. A área do relatório mostra em vez disso o
relatório de combinação:

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

Há duas formas de introduzir um código de barras:

- **Lendo-o.** No Windows, os controlos do leitor ficam na barra de ferramentas no modo
  Combinar, e cada leitura vai diretamente para a combinação em vez do histórico.
- **Escrevendo-o ou colando-o** no campo *Código de barras* e premindo <kbd>Enter</kbd>
  ou **Adicionar**. Escreva o FNC1 como **`{GS}`**, tal como na barra de entrada. Um código
  de barras escrito sem AIM Code ID é tratado como GS1-128.

| Botão | O que faz |
|---|---|
| **Adicionar** | Acrescenta o código de barras do campo |
| **Remover o último** | Retira o código de barras mais recente (indisponível quando não há nenhum) |
| **Repor** | Retira todos os códigos de barras, limpa o campo e qualquer recusa, e recomeça |

A barra de estado diz sempre quantos códigos de barras estão presentes (*2 de 3 códigos de barras GS1-128*),
ou porque foi recusado o último.

### O que Combinar recusa

Um código de barras recusado não é acrescentado. O relatório e a barra de estado dizem ambos
porquê:

| Código de barras | Recusado porque |
|---|---|
| Um campo vazio | Não há nada para acrescentar |
| Um quarto código de barras | Uma etiqueta combina-se a partir de três, no máximo. Retire o último, ou reponha |
| Outra simbologia, pelo seu AIM Code ID | Só o GS1-128 é combinado, por isso um GS1 DataMatrix (`]d2`) ou um QR Code é recusado, com o seu nome |
| AI que não se conseguem ler | Um erro de sintaxe; são mostrados o código de erro e a mensagem do motor |
| Nenhum AI | Não há nada para combinar |
| Um já lido | A mesma cadeia de elementos duas vezes, por exemplo o mesmo código de barras lido de novo; o relatório diz que código de barras anterior repete |

Combinar só recusa o que não consegue usar de todo. Tudo o resto (um dígito de controlo
errado, uma data inválida, um AI acompanhante em falta) é aceite e avaliado na combinação.

### Duplicados e conflitos

Os AI são combinados pela ordem em que foram lidos. Quando dois códigos de barras levam o mesmo
AI:

- **com o mesmo valor**, é mantido uma vez e listado em *Duplicados — mantidos uma vez*, com os códigos
  de barras em que foi lido;
- **com valores diferentes**, é um **conflito**. O veredito diz *CONFLITO*, os
  valores em desacordo são listados código de barras a código de barras, e **nada é combinado**
  até que seja resolvido. Retire o código de barras em falta, ou reponha.

### O relatório de combinação

De cima para baixo:

1. **O veredito**: *sem leituras*, *CONFLITO*, válido ou inválido, e quantos
   códigos de barras estão presentes.
2. **As leituras**: cada código de barras tal como chegou, a sua simbologia e a cadeia de
   elementos que resta depois de retirado o AIM Code ID. Um problema que só a combinação pode
   decidir é mostrado no código de barras, marcado *avaliado na combinação*.
3. **Duplicados** e **conflitos**, quando os há.
4. **Combinação**: a cadeia de elementos, o seu HRI e o seu comprimento; depois, a
   entrada combinada como fita etiquetada e um cartão por AI, ambos dispostos como no
   [relatório de análise](#o-relatório). Cada erro e cada aviso sobre a combinação é depois listado
   em *Validação*.
5. **O símbolo GS1 DataMatrix**, com o seu HRI por baixo. Só é desenhado quando a combinação é
   válida. Caso contrário, o relatório diz porque não foi desenhado, para que um símbolo inválido
   não possa ser impresso por engano.

Por exemplo, uma etiqueta com dois códigos de barras:

| Código de barras | Contém | Sozinho |
|---|---|---|
| `]C10109506000134352` | (01) GTIN | válido |
| `]C11726123110LOT-ABC` | (17) data de validade, (10) lote | `GE-S005` para ambos os AI, avaliado na combinação |

Combinam-se em `01095060001343521726123110LOT-ABC`, HRI
`(01)09506000134352 (17)261231 (10)LOT-ABC`, que é válido, por isso o símbolo é desenhado. O AI
`17` tem comprimento fixo, por isso não é preciso um FNC1 depois dele. Combinar insere
um sempre que a norma o exige.

**Guardar relatório** exporta o relatório de combinação em HTML ou PDF, tal como um relatório de
análise (consulte [Exportar](#exportar)), assim que houver um código de barras. O nome do
ficheiro HTML começa por `gaia-combine-`.

O relatório segue o idioma e o tema escolhidos. Mudar uma opção de análise ou o idioma volta a
verificar os códigos de barras já presentes. Também sobrevivem a uma passagem para
Analisar ou Compor e regresso, mas não a um reinício: os códigos de barras do modo
Combinar não são guardados.

---

## O relatório

A captura selecionada é apresentada como um relatório completo: um banner de veredito, a cadeia
de elementos decomposta numa «fita» etiquetada, os metadados da análise, um cartão por AI com as
suas interpretações, e os erros e avisos com orientação para os corrigir.

Num **Digital Link**, cada cartão de AI leva ainda o seu papel estrutural — **chave primária**,
**qualificador de chave** ou **atributo de dados** — com código de cores pela ordem em que
surgem no URI. Os papéis só são mostrados num Digital Link: numa cadeia de elementos simples, um
AI não tem posição no URI para descrever.

O relatório segue o idioma e o tema que escolheu.

### Exportar

| | |
|---|---|
| **Guardar relatório → Como HTML…** (<kbd>⌘S</kbd>) | Escreve exatamente o que está no ecrã: um único ficheiro autónomo que abre em qualquer navegador |
| **Guardar relatório → Como PDF…** (<kbd>⌘P</kbd>) | Imprime o relatório através da caixa de impressão do sistema — escolha aí um destino PDF |

O PDF passa pela caixa de impressão porque é aí que se escolhem o destino e o nome do ficheiro:
**PDF ▾ → Guardar como PDF** no macOS, **Microsoft Print to PDF** no Windows, **Imprimir para
ficheiro** no Linux. As páginas são paginadas e o texto continua selecionável.

A impressão e o PDF usam sempre a paleta clara, mesmo com a aplicação em tema escuro, e mantêm
intactas as etiquetas coloridas dos AI e o banner de veredito.

Se a sua máquina não tiver impressora nem destino PDF configurado, a aplicação avisa e
encaminha-o para a exportação HTML, que se imprime a partir de qualquer navegador.

---

## Opções de análise

O painel da direita reúne todas as opções do motor. Alterar qualquer uma **volta a analisar
imediatamente a captura selecionada**, pelo que o painel serve também para perguntar «como
ficaria esta entrada com estas definições?», e não apenas para influenciar a leitura seguinte.

| Opção | Valores |
|---|---|
| Modo de análise | `SYNTAX`, `CONTENT`, `DATA_CARRIER`, `INTERPRETATION` — até onde levar a análise |
| Idioma | 35 catálogos de mensagens |
| Ordem | `LITTLE` (dd/mm/aaaa), `MIDDLE` (mm/dd/aaaa), `BIG` (aaaa/mm/dd) |
| Separador | `SLASH`, `HYPHEN`, `PERIOD` |
| Mês | `TWO_DIGIT` (12), `THREE_LETTER` (DEC) |
| Ano | `FOUR_DIGIT` (2026), `TWO_DIGIT` (26) |
| Ignorar a verificação «requires» | Deixa de recusar um AI a que falta o companheiro obrigatório (`GE-S005`) |
| Ignorar a verificação «excludes» | Deixa de recusar as combinações de AI que a norma proíbe (`GE-S006`) |

*Repor predefinições*, no fundo do painel, restaura os valores do próprio motor.

### Primeiro arranque

Enquanto não houver nada guardado, a aplicação vai buscar as definições às **definições
regionais da sua máquina**, em vez das predefinições do motor:

- o **idioma** vem da configuração regional do sistema, recuando para inglês quando não existe
  catálogo;
- as **opções de data** são lidas do formato de data curta da sua configuração regional: a ordem
  dos componentes, o separador e se o mês e o ano são abreviados.

> **Nota.** Uma configuração regional que escreve datas curtas com o ano em dois dígitos começa
> aqui também com dois dígitos: coincide com o resto do sistema, mas difere da predefinição do
> motor, que é de quatro. Altere no painel e a escolha mantém-se.

---

## Idiomas

Escolher um idioma muda **toda a aplicação** — menus, botões, editor, painel de opções, linha de
estado e relatório —, não apenas as mensagens de erro. Os idiomas escritos da direita para a
esquerda invertem a janela e o relatório.

Estão disponíveis os 35 idiomas, com duas camadas de cobertura:

| | Cobertura |
|---|---|
| Relatórios de análise e mensagens de erro do motor | **os 35 idiomas** |
| Menus, botões e etiquetas da própria aplicação | **os 35 idiomas** |

Os idiomas da direita para a esquerda — árabe, árabe egípcio, persa e urdu — invertem a janela e
o relatório desde o momento em que a aplicação abre.

O idioma é memorizado entre execuções. Para abrir noutro idioma apenas uma vez, consulte
[`--language`](#executar).

Algumas coisas ficam propositadamente por traduzir em todos os idiomas: os nomes de campo
abreviados impressos junto aos valores em bruto (`format`, `position`, `raw`, `len` …), que são
identificadores e não palavras; os nomes dos exemplos e das simbologias, que são termos GS1 e do
setor; e um longo parágrafo normativo, onde uma tradução parcial induziria em erro.

---

## O que é memorizado

Dois ficheiros em `.gaia-desktop`, dentro da sua pasta pessoal:

| Ficheiro | Contém |
|---|---|
| `options.properties` | Todas as opções de análise, o tema, se o painel de opções está aberto, o último leitor usado, o modo em que a janela ficou, e as definições da verificação de atualizações — ligada ou desligada, quando correu pela última vez e qualquer versão que tenha escolhido ignorar |
| `captures.tsv` | O histórico de capturas, as mais recentes primeiro, limitado a 500 entradas |

As opções são escritas sempre que altera uma; o histórico sempre que muda. Ambos são reescritos
ao sair.

Apagar qualquer um dos ficheiros repõe essa parte e mais nada — estão separados para que um
histórico danificado não lhe custe as definições. Um ficheiro estragado por edição manual também
não é fatal: cada definição recua individualmente para o seu valor predefinido, pelo que uma
linha errada só custa essa linha.

O histórico guarda a entrada, não a análise. Esta é refeita ao carregar, o que significa que uma
captura restaurada reflete as opções em vigor **agora**, e não as que vigoravam quando foi
capturada.

Os códigos de barras do modo Combinar não são guardados: depois de um reinício,
Combinar começa vazio.

---

## Atalhos de teclado

| | |
|---|---|
| <kbd>Enter</kbd> | Analisar a barra de entrada |
| <kbd>⌘O</kbd> | Abrir um ficheiro de texto |
| <kbd>⌘S</kbd> | Guardar o relatório como HTML |
| <kbd>⌘P</kbd> | Guardar o relatório como PDF (caixa de impressão do sistema) |
| <kbd>⌘G</kbd> | Inserir o token separador FNC1 |
| <kbd>⌘K</kbd> | Ligar / desligar o leitor (apenas Windows) |
| <kbd>⌘D</kbd> | Alternar o tema escuro |
| <kbd>⌘Q</kbd> | Sair |

No editor: <kbd>Tab</kbd> / <kbd>Shift</kbd>+<kbd>Tab</kbd> movem entre células editáveis,
<kbd>Enter</kbd> confirma, <kbd>Esc</kbd> cancela.

No modo Combinar: <kbd>Enter</kbd> no campo *Código de barras* acrescenta o
código de barras. Aí, escreva o FNC1 como `{GS}`; <kbd>⌘G</kbd> pertence à barra de entrada do
modo Analisar.

---

## Resolução de problemas

| Sintoma | Causa e solução |
|---|---|
| A aplicação não arranca, ou indica `NoClassDefFoundError` | A pasta `lib/` tem de estar ao lado do JAR. Mantenha-os juntos, ou use uma distribuição autónoma |
| *«Unsupported JavaFX configuration…»* na consola ao arrancar | Esperado e inofensivo — o conjunto de ferramentas apenas regista como foi carregado. A aplicação funciona normalmente |
| Um separador que colou parece desaparecer | É o esperado: é reescrito como o token visível `{GS}` e o analisador continua a receber o carácter verdadeiro. Consulte [o token {GS}](#a-barra-de-entrada-e-o-token-gs) |
| Não há botão *Ligar leitor* | Está no macOS ou no Linux, onde os controladores do leitor não existem. Consulte [Leitura com leitor](#leitura-com-leitor) |
| O leitor não liga | Verifique se o nome lógico no seletor corresponde ao seu modelo, se o leitor está ligado e se nenhuma outra aplicação o está a ocupar. O [guia de configuração do leitor Zebra](GaiaDesktop-Zebra-Scanner-Windows-Portuguese.md#11-resolução-de-problemas) lista cada código de erro. |
| A exportação para PDF diz que não há destino | A sua máquina não tem impressora nem destino PDF configurado. Acrescente um, ou exporte em HTML e imprima a partir de um navegador |
| Um item de menu mostra algo como `act.hri` | Falta uma etiqueta — comunique, por favor; é um defeito de compilação, não uma definição |

---

## Limites

- **A leitura é só para Windows**, porque os controladores também são.
- **O PDF passa pela caixa de impressão** e não por uma de gravação.
- **Os destinos Digital Link aceitam um domínio e um prefixo de caminho**, mas não parâmetros de
  consulta adicionais nem um fragmento.
- **Um AI só pode aparecer uma vez** numa cadeia de elementos — é a regra GS1, não um limite da
  aplicação.
- **Combinar só aceita códigos de barras GS1-128, três no máximo**, e não os guarda
  entre execuções.
