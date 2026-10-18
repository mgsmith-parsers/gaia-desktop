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
6. [O relatório](#o-relatório)
   - [Exportar](#exportar)
7. [Opções de análise](#opções-de-análise)
   - [Primeiro arranque](#primeiro-arranque)
8. [Idiomas](#idiomas)
9. [O que é memorizado](#o-que-é-memorizado)
10. [Atalhos de teclado](#atalhos-de-teclado)
11. [Resolução de problemas](#resolução-de-problemas)
12. [Limites](#limites)

---

## O que é

O GAIA Desktop faz duas coisas com dados GS1:

- **Analisar** — escreva, cole, abra ou leia com um leitor uma cadeia de elementos GS1 ou um URI
  Digital Link e leia o resultado como um relatório completo: o que significa cada identificador
  de aplicação (AI), o que está errado e porquê;
- **Compor** — juntar pares AI / valor numa cadeia de elementos ou num URI Digital Link.

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

> O esquema mantém as etiquetas em inglês, tal como todos os blocos de código desta
> documentação. No ecrã verá *Analisar* / *Compor*, *Capturas*, *Guardar relatório*, *Opções*,
> *leitor: desligado* e *Acerca*.

**No macOS e no Linux não existe a linha do leitor**: não há botão nem seletor de dispositivo na
barra de ferramentas, não há menu *Leitor* e não aparece *leitor: desligado* na barra de estado.
Consulte [Leitura com leitor](#leitura-com-leitor).

O seletor **Analisar ⇄ Compor** muda apenas a faixa por baixo da barra de ferramentas e os
controlos de captura. O histórico, o relatório, o painel de opções e as ações de exportação
servem os dois modos, porque uma composição bem-sucedida passa a ser simplesmente mais uma
captura.

O painel de opções está **fechado no primeiro arranque**. O modo, a visibilidade do painel, o
tema e o último leitor usado são memorizados entre execuções.

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
indicá-lo, e uma leitura que chegue com o editor aberto é capturada na mesma.

Se um dispositivo não ligar, consulte [Resolução de problemas](#resolução-de-problemas).

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
| `options.properties` | Todas as opções de análise, o tema, se o painel de opções está aberto, o último leitor usado e o modo em que a janela ficou |
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

---

## Resolução de problemas

| Sintoma | Causa e solução |
|---|---|
| A aplicação não arranca, ou indica `NoClassDefFoundError` | A pasta `lib/` tem de estar ao lado do JAR. Mantenha-os juntos, ou use uma distribuição autónoma |
| *«Unsupported JavaFX configuration…»* na consola ao arrancar | Esperado e inofensivo — o conjunto de ferramentas apenas regista como foi carregado. A aplicação funciona normalmente |
| Um separador que colou parece desaparecer | É o esperado: é reescrito como o token visível `{GS}` e o analisador continua a receber o carácter verdadeiro. Consulte [o token {GS}](#a-barra-de-entrada-e-o-token-gs) |
| Não há botão *Ligar leitor* | Está no macOS ou no Linux, onde os controladores do leitor não existem. Consulte [Leitura com leitor](#leitura-com-leitor) |
| O leitor não liga | Verifique se o nome lógico no seletor corresponde ao seu modelo, se o leitor está ligado e se nenhuma outra aplicação o está a ocupar |
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
