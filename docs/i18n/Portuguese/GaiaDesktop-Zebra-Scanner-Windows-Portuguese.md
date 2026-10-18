# Configuração de um leitor Zebra — GAIA Desktop para Windows

Como instalar e configurar um leitor de códigos de barras Zebra para que o GAIA Desktop leia
diretamente os códigos GS1: cada leitura é analisada e acrescentada ao histórico, com o prefixo
AIM Code ID e cada separador FNC1 intactos. Versão original:
[GaiaDesktop-Zebra-Scanner-Windows.md](../../GaiaDesktop-Zebra-Scanner-Windows.md).

O GAIA Desktop foi desenvolvido e testado com um leitor de imagem portátil **Zebra DS8108** ligado
por USB. Os passos aplicam-se a qualquer leitor 2D Zebra suportado pelo Scanner SDK da Zebra; os
códigos de barras de configuração e os nomes de menu de um modelo específico estão no respetivo
Quick Reference Guide ou Product Reference Guide.

> **Apenas Windows.** Os controladores dos leitores Zebra só existem para Windows, por isso em
> macOS ou Linux o GAIA Desktop não cria nenhum controlo de leitor. Tudo o resto da aplicação
> funciona em qualquer plataforma.

---

## Índice

1. [O que é preciso](#1-o-que-é-preciso)
2. [Instalar o Java](#2-instalar-o-java)
3. [Instalar o Zebra Scanner SDK](#3-instalar-o-zebra-scanner-sdk)
4. [Instalar o 123Scan](#4-instalar-o-123scan)
5. [Atualizar o firmware do leitor Zebra](#5-atualizar-o-firmware-do-leitor-zebra)
6. [Definir o modo USB do leitor Zebra](#6-definir-o-modo-usb-do-leitor-zebra)
7. [Configurar o leitor Zebra para GS1](#7-configurar-o-leitor-zebra-para-gs1)
8. [Instalar e iniciar o GAIA Desktop](#8-instalar-e-iniciar-o-gaia-desktop)
9. [Ligar o leitor](#9-ligar-o-leitor)
10. [Referência de configuração](#10-referência-de-configuração)
11. [Resolução de problemas](#11-resolução-de-problemas)
12. [Leitores 2D Zebra suportados](#12-leitores-2d-zebra-suportados)

---

## 1. O que é preciso

| | |
|---|---|
| **Leitor** | Um leitor 2D Zebra, com o respetivo cabo USB — ver [Leitores 2D Zebra suportados](#12-leitores-2d-zebra-suportados) |
| **Computador** | Windows 10 ou 11, 64 bits |
| **Java** | Java 21 ou mais recente, **64 bits** |
| **Zebra Scanner SDK para Windows** | **64 bits**, do site de suporte da Zebra — fornece o controlador e os serviços JavaPOS que o GAIA Desktop utiliza |
| **Zebra 123Scan** | O utilitário de configuração gratuito da Zebra, do site de suporte — serve para atualizar o firmware do leitor e verificar as definições |
| **GAIA Desktop** | `gaia-desktop-1.0.0-win.zip` |
| **Opcional** | O **Product Reference Guide** (PRG) do leitor, que contém os códigos de barras de configuração |

Como as peças se encaixam:

```
Zebra scanner ──USB──► Zebra CoreScanner driver ──► Zebra JavaPOS services ──► GAIA Desktop
                       (installed by the SDK)        (in the SDK's JPOS\bin)    (run.bat puts them on
                                                                                 the class path)
```

O GAIA Desktop inclui a própria API JavaPOS e a configuração que dá nome ao leitor (`jpos.xml`,
dentro de `gaia-desktop.jar`). O SDK da Zebra fornece o resto: o controlador que comunica com o
leitor e as classes de serviço da Zebra que ficam entre o JavaPOS e esse controlador.

---

## 2. Instalar o Java

O GAIA Desktop precisa do **Java 21 ou mais recente**, na versão de **64 bits**, para corresponder
aos controladores Zebra de 64 bits.

1. Instale um JDK ou JRE Java 21 para Windows x64 — por exemplo o Eclipse Temurin, de
   adoptium.net. Deixe o instalador definir `JAVA_HOME`, se o propuser.
2. Verifique numa linha de comandos:

   ```
   java -version
   ```

   A primeira linha deve mostrar a versão 21 ou posterior; um Java de 64 bits mostra também
   `64-Bit Server VM`.

O `run.bat` procura o Java por esta ordem: uma pasta `jdk*` ao lado de `run.bat`, depois
`JAVA_HOME` e depois `java` no `PATH`. Recusa qualquer versão anterior à 21, com uma mensagem a
dizê-lo.

---

## 3. Instalar o Zebra Scanner SDK

1. Transfira o **Zebra Scanner SDK para Windows (64 bits)** do site de suporte da Zebra.
2. Execute o instalador. Se oferecer uma escolha de componentes, certifique-se de que o componente
   **JPOS** (JavaPOS) está incluído.
3. Mantenha a localização de instalação predefinida. O GAIA Desktop procura a parte JavaPOS do SDK
   em:

   ```
   C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   e espera os jar de serviço da Zebra (`JposServiceScanner.jar`, `JposServiceJniScanner.jar`, …)
   e as bibliotecas nativas (`CSJPOSScanner64.dll`, …) na respetiva pasta `bin\`. Se o SDK estiver
   instalado noutro local, ver [ZEBRA_HOME](#zebra_home).
4. **Reinicie o Windows** depois de instalar. O controlador CoreScanner do SDK corre em segundo
   plano e tem de estar ativo antes de o GAIA Desktop conseguir chegar ao leitor; reiniciar é a
   forma mais simples de o garantir.

---

## 4. Instalar o 123Scan

O 123Scan é o utilitário gratuito da Zebra para configurar os seus leitores. É usado no passo
seguinte para atualizar o firmware do leitor e, depois, é a forma mais simples de verificar e
alterar as definições do leitor.

1. Transfira o **123Scan** do site de suporte da Zebra e execute o instalador.
2. Ligue o leitor Zebra e inicie o 123Scan. Deve encontrar o leitor e mostrar o modelo, o número
   de série e a versão atual do firmware. Se não o fizer, experimente outra porta USB ou outro cabo
   antes de continuar — o GAIA Desktop não verá um leitor que o 123Scan não vê.

---

## 5. Atualizar o firmware do leitor Zebra

Atualize o firmware do leitor para a versão mais recente antes de o configurar. As atualizações de
firmware corrigem problemas de descodificação e mantêm o leitor em sintonia com o controlador Zebra
instalado pelo SDK.

1. Feche o **GAIA Desktop** e qualquer outro programa que possa estar a usar o leitor: o 123Scan
   precisa do leitor só para si durante a atualização.
2. No 123Scan, com o leitor ligado, escolha a opção de atualização de firmware. O 123Scan compara o
   firmware do leitor com a versão mais recente publicada pela Zebra e propõe transferi-la e
   instalá-la. Se o computador não tiver acesso à Internet, transfira o firmware do leitor a partir
   da respetiva página de suporte no site da Zebra e carregue o ficheiro no 123Scan.
3. Inicie a atualização e **mantenha o leitor ligado até o 123Scan indicar que terminou**. Demora
   vários minutos; o leitor reinicia no fim. Desligá-lo a meio pode deixá-lo inutilizável.
4. Verifique que o 123Scan mostra agora a nova versão do firmware.

Uma atualização pode repor algumas definições nos valores predefinidos; é por isso que o leitor é
configurado nos dois passos seguintes, depois do firmware.

---

## 6. Definir o modo USB do leitor Zebra

É o passo mais esquecido. **De fábrica, um leitor Zebra comporta-se como um teclado USB** (modo
USB HID Keyboard): «escreve» cada código de barras onde estiver o foco. Nesse modo, *Ligar leitor*
do GAIA Desktop não o consegue encontrar, e tudo o que é escrito num campo de texto perde os
separadores FNC1, o que muda o significado de uma cadeia de elementos GS1.

Mude o leitor para um modo com que o controlador Zebra comunique diretamente:

| Modo USB do leitor | Nome do dispositivo no GAIA Desktop | Notas |
|---|---|---|
| **IBM Hand-held USB** | `ZebraUSBHandHeld`, ou `ZebraAllScanners` | Recomendado |
| SNAPI (com ou sem imagem) | `ZebraScannerSNAPI`, ou `ZebraAllScanners` | O protocolo USB próprio da Zebra; também funciona |
| IBM Table-top USB | `ZebraUSBTableTop`, ou `ZebraAllScanners` | Pensado para leitores de balcão |
| USB OPOS (Hand-held) | `ZebraUSBOPOS`, ou `ZebraAllScanners` | |
| USB HID Keyboard (predefinição de fábrica) | — | **Não liga** |

`ZebraAllScanners`, o nome de dispositivo predefinido do GAIA Desktop, aceita um leitor em
qualquer um dos modos suportados, por isso, definido o modo, a predefinição costuma bastar.

Para mudar o modo, faça uma de duas coisas:

- **leia o código de barras de configuração** do modo — *IBM Hand-held USB*, por exemplo — no
  Quick Reference Guide ou no Product Reference Guide do leitor (secção USB Host Types / USB Device
  Type); o leitor emite um sinal sonoro a confirmar. Ou
- **use o 123Scan** ([passo 4](#4-instalar-o-123scan)): ligue o leitor, abra a configuração e defina o
  tipo de anfitrião USB.

Depois de mudar o modo, o Windows volta a detetar o leitor como outro tipo de dispositivo; aguarde
alguns segundos antes de ligar.

Para repor mais tarde o leitor em modo teclado — para outro software — leia o código de barras de
configuração *USB Keyboard (HID)*, ou reponha as predefinições de fábrica.

---

## 7. Configurar o leitor Zebra para GS1

O GAIA Desktop analisa melhor quando o leitor lhe diz que tipo de código de barras leu.

### Transmitir o AIM Code ID

Defina **Transmit Code ID Character** como **AIM Code ID Character**, com o código de barras de
configuração do Product Reference Guide (User Preferences) ou no 123Scan. Cada leitura começa então
com o seu identificador de simbologia — `]C1` para GS1-128, `]d2` para GS1 DataMatrix, `]Q3` para
GS1 QR Code, `]e0` para GS1 DataBar —, que o GAIA Desktop usa para identificar o suporte de dados e
aplicar-lhe as respetivas regras.

Sem ele, o GAIA Desktop analisa na mesma a cadeia de elementos e obtém a simbologia da própria
leitura, mas o relatório não tem AIM Code ID para mostrar, e um código de barras simples como um
EAN-13 distingue-se menos facilmente de uma cadeia de elementos GS1.

### Verificar as simbologias

Certifique-se de que os tipos de código de barras com que trabalha estão **ativados**: normalmente
GS1-128, GS1 DataMatrix, GS1 DataBar, GS1 QR Code e EAN/UPC. A maioria vem ativada por
predefinição nos leitores 2D Zebra; o 123Scan mostra as definições atuais, e o Product Reference
Guide tem um código de barras para ativar cada um.

### Deixar desativadas as funções de análise GS1 da Zebra

O firmware recente acrescenta funções GS1 que trabalham no próprio leitor — **Label Parse+**,
**GS1 Digital Link** e **GS1 Retail POS parsing**. Deixe-as **desativadas**, como vêm de fábrica:
reformatam ou encurtam o que o leitor envia, e o GAIA Desktop precisa dos dados em bruto para os
analisar e validar. Ver [Leitores 2D Zebra suportados](#12-leitores-2d-zebra-suportados) para saber
o que cada uma faz.

### Não acrescentar prefixos nem sufixos

Não acrescente prefixo, sufixo nem tecla Enter à saída do leitor. O GAIA Desktop recebe cada
leitura como uma mensagem completa através do JavaPOS e não precisa de nenhum deles; os caracteres
a mais iriam parar à cadeia de elementos.

---

## 8. Instalar e iniciar o GAIA Desktop

1. Extraia `gaia-desktop-1.0.0-win.zip`, por exemplo para `C:\Tools\`. Contém uma única pasta:

   ```
   gaia-desktop-1.0.0-win\
   ├── docs\
   │   └── GaiaDesktop-Zebra-Scanner-Windows.md      this guide
   ├── gaia-desktop.jar
   ├── lib\
   ├── readme.html                                   index of every guide
   └── run.bat
   ```

2. Ligue o leitor.
3. Faça duplo clique em **`run.bat`**. Abre-se uma janela de consola ao lado da aplicação, que fica
   aberta: o estado do leitor e os erros JavaPOS são indicados ali.
4. Leia a primeira linha da consola. Diz o que o `run.bat` encontrou:

   ```
   run.bat: Java 21.0.8 from JAVA_HOME; JavaFX from lib\; JavaPOS from lib\; Zebra services from C:\Program Files\Zebra Technologies\Barcode Scanners\Scanner SDK\JPOS\
   ```

   Se terminar em **`Zebra services from none (Zebra Scanner SDK not installed)`**, o SDK não foi
   encontrado na localização predefinida — instale-o
   ([passo 3](#3-instalar-o-zebra-scanner-sdk)) ou defina [ZEBRA_HOME](#zebra_home). A aplicação
   inicia na mesma, mas o leitor não consegue ligar.

---

## 9. Ligar o leitor

1. Na barra de ferramentas, o **seletor de dispositivo** lista os nomes de leitor que o GAIA
   Desktop conhece. Deixe-o em **`ZebraAllScanners`**, ou escolha o nome para o modo USB do leitor
   na tabela do [passo 6](#6-definir-o-modo-usb-do-leitor-zebra).
2. Clique em **Ligar leitor**, ou prima **Ctrl+K** (também no menu **Leitor**).
3. A barra de estado passa a **`leitor: ZebraAllScanners`** (ou o nome escolhido), e a
   linha de estado indica *Leitor ligado — ZebraAllScanners*. O botão mostra agora
   **Desligar leitor**.
4. Leia um código de barras GS1. Aparece no topo do histórico de capturas e o respetivo relatório
   abre-se. Com o AIM Code ID ativo, o relatório indica o suporte de dados — *GS1-128*,
   *GS1 DataMatrix* — e a cadeia de elementos mantém cada separador FNC1.

O seletor de dispositivo memoriza a escolha entre sessões. O leitor fica ligado até o desligar ou
fechar a aplicação. As leituras também funcionam nos modos **Compor** e
**Combinar**: em Combinar, cada leitura passa a ser um dos códigos de barras a
combinar.

Enquanto o GAIA Desktop tem o leitor, os outros programas não o podem usar, e vice-versa: feche o
123Scan e qualquer outro software de ponto de venda ou de leitura antes de ligar.

---

## 10. Referência de configuração

### Nomes de dispositivo

O seletor de dispositivo é preenchido a partir do `jpos.xml` incluído em `gaia-desktop.jar`. As
suas entradas de leitor:

| Nome do dispositivo | Tipo de leitor | Usar com |
|---|---|---|
| `ZebraAllScanners` | qualquer | **Predefinido.** Qualquer modo USB suportado |
| `ZebraAllScanners114` | qualquer | A versão JavaPOS 1.14 do mesmo serviço |
| `ZebraUSBHandHeld` | IBM Hand-held USB | Um leitor em modo IBM Hand-held USB |
| `ZebraUSBTableTop` | IBM Table-top USB | Modo de balcão |
| `ZebraUSBOPOS` | USB OPOS | Modo USB OPOS |
| `ZebraScannerSNAPI` | SNAPI | Modo SNAPI |
| `ZebraScannerUSB` | qualquer USB | Qualquer modo USB |
| `ZebraScannerSerialSSI` | série SSI | Leitores com cabo série (não USB) |
| `ZebraScannerSerialNIXMODB` | série NIXMODB | Leitores com cabo série |
| `ZebraVDI` | VDI | Leitores redirecionados para um ambiente de trabalho virtual |

### ZEBRA_HOME

Se o SDK da Zebra estiver instalado noutro local que não o predefinido, defina `ZEBRA_HOME` para a
respetiva pasta `JPOS` antes de iniciar o `run.bat` — por exemplo num pequeno ficheiro batch ao
lado:

```bat
@echo off
set "ZEBRA_HOME=D:\Zebra\Scanner SDK\JPOS\"
call "%~dp0run.bat" %*
```

O `run.bat` vai então buscar os jar de serviço da Zebra a `%ZEBRA_HOME%bin` e acrescenta essa
pasta ao `PATH` para as DLL da Zebra.

### Um jpos.xml próprio

Para acrescentar um nome de dispositivo ou alterar as definições de uma entrada, copie `jpos.xml`
para fora de `gaia-desktop.jar` (é um zip; qualquer ferramenta de extração o abre), edite a cópia e
indique-a ao GAIA Desktop:

```bat
set GAIA_DESKTOP_OPTS=-Djpos.config.populatorFileURL=file:///C:/Tools/jpos.xml
run.bat
```

O seletor de dispositivo lista então os leitores desse ficheiro. Com mais de um leitor Zebra
ligado, uma entrada pode ser associada a um deles através da propriedade `ScannerModel` ou
`SerialNumber`, como na entrada `ZebraAllScanners`.

### Outras opções

| Definição | Como |
|---|---|
| Idioma da aplicação | `run.bat --language=FR` (só para esta sessão), ou o Painel de opções |
| Opções Java adicionais | `set GAIA_DESKTOP_OPTS=…` antes de `run.bat` |
| Um Java incluído | coloque uma pasta `jdk-21…` ao lado de `run.bat`; é usada antes de `JAVA_HOME` |

---

## 11. Resolução de problemas

Os erros aparecem numa caixa de diálogo ao ligar, na janela de consola e no ficheiro de registo
`logs\gaia-desktop.log`, na pasta a partir da qual o `run.bat` foi iniciado (a pasta do GAIA
Desktop, quando se faz duplo clique em `run.bat`). O número entre parênteses retos no registo é o
código de erro JavaPOS.

| O que vê | Causa provável | O que fazer |
|---|---|---|
| Não existe o botão *Ligar leitor* | Não está a correr em Windows | A leitura só existe em Windows |
| O 123Scan não encontra o leitor | Cabo, porta USB, ou outro programa está a usar o leitor | Experimente outro cabo ou porta; feche o GAIA Desktop e outro software de leitura. O GAIA Desktop não verá um leitor que o 123Scan não vê |
| Consola: `Zebra services from none (Zebra Scanner SDK not installed)` | O SDK não está na localização predefinida | Instale o SDK de 64 bits ([passo 3](#3-instalar-o-zebra-scanner-sdk)), ou defina [ZEBRA_HOME](#zebra_home) |
| Erro **104**: *Could not connect to service with logicalName = …: …SymScannerSvc112Factory* | Os jar de serviço da Zebra não foram encontrados | Como acima — verifique a linha `Zebra services from` da consola |
| Erro **109**: *Service does not exist in loaded JCL registry* | O nome do dispositivo não está na configuração do leitor, ou o `javaPOS114.jar` do SDK foi parar ao class path | Escolha um nome no seletor de dispositivo; não acrescente `javaPOS114.jar`; se usa um `jpos.xml` próprio, verifique que o nome está lá |
| Erro **107** (sem hardware), ou a ligação falha com o leitor ligado | O leitor ainda está em modo teclado USB, ou o controlador CoreScanner não está a correr | Defina o modo USB ([passo 6](#6-definir-o-modo-usb-do-leitor-zebra)); reinicie o Windows depois de instalar o SDK; experimente outra porta USB |
| Erro **112** (tempo esgotado) ou **102** (reclamado) ao ligar | Outro programa tem o leitor | Feche o 123Scan e qualquer outro software de leitura ou de ponto de venda e volte a ligar |
| `UnsatisfiedLinkError` … `CSJPOSScanner64` na consola | As DLL da Zebra não podem ser carregadas — muitas vezes um SDK de 32 bits com um Java de 64 bits | Instale o SDK de 64 bits; verifique que `java -version` indica uma VM de 64 bits |
| As leituras são escritas na barra de entrada em vez de chegarem como capturas | O leitor está em modo teclado USB | Defina o modo USB ([passo 6](#6-definir-o-modo-usb-do-leitor-zebra)) |
| Ligado, mas as leituras não chegam | Nome de dispositivo errado para o modo do leitor, ou o leitor foi desligado e voltado a ligar durante a ligação | Desligue, escolha `ZebraAllScanners` e volte a ligar |
| O relatório não mostra AIM Code ID nem suporte de dados | A transmissão do AIM Code ID está desativada | Ative *Transmit Code ID Character → AIM Code ID* ([passo 7](#7-configurar-o-leitor-zebra-para-gs1)) |
| Uma cadeia de elementos perdeu os separadores, ou os AI aparecem colados | Lida em modo teclado, ou escrita à mão sem `{GS}` | Use um modo USB JavaPOS; ao escrever, insira os separadores com o botão `{GS}` |
| Um tipo de código de barras nunca é lido | Essa simbologia está desativada no leitor | Ative-a ([passo 7](#7-configurar-o-leitor-zebra-para-gs1)) |

Se um problema persistir, guarde em conjunto a linha `run.bat:` da consola, o texto da caixa de
erro e `logs\gaia-desktop.log`: entre os três mostram que Java, que serviços Zebra e que nome de
dispositivo foram usados, e onde exatamente a ligação falhou.

---

## 12. Leitores 2D Zebra suportados

O GAIA Desktop precisa de um leitor capaz de ler os códigos de barras em que os dados GS1 são
impressos — GS1-128, GS1 DataBar, EAN/UPC e, para a maioria dos usos, os códigos 2D:
**GS1 DataMatrix** e **GS1 QR Code**, que é também a forma habitual de imprimir um URI **GS1
Digital Link**. Isso significa um **leitor de imagem 2D** Zebra: um leitor linear (1D) não consegue
ler de todo códigos DataMatrix nem QR.

O leitor não precisa de entender por si os Application Identifiers GS1 nem o Digital Link.
**O GAIA Desktop analisa ambos**: recebe a leitura tal como o leitor a leu — com AIM Code ID e
separadores FNC1 — e divide-a em AI, ou converte ele próprio um URI Digital Link nos seus AI.

### Testado

| Leitor | Tipo | Estado |
|---|---|---|
| **DS8108** | Leitor de imagem 2D portátil com cabo | **Desenvolvido e testado** com o GAIA Desktop |

### Devem funcionar

São leitores de imagem 2D Zebra que, como o DS8108, funcionam através do Scanner SDK e dos serviços
JavaPOS da Zebra. **Não foram testados** com o GAIA Desktop; configure cada um como neste guia, com
o respetivo Quick Reference Guide ou Product Reference Guide para os códigos de barras de
configuração.

| Família | Modelos (exemplos) |
|---|---|
| DS81x8 | DS8108 (com cabo), DS8178 (sem fios) |
| DS22x8 | DS2208 (com cabo), DS2278 (sem fios) — uso geral |
| DS36x8 | DS3608 (com cabo), DS3678 (sem fios) — robustos, incluindo as variantes KD e XR |
| DS46x8 | DS4608 (com cabo), DS4678 (sem fios) |
| DS9308 / DS9908 | Leitores de apresentação e mãos-livres |
| MP7000 / MP7200 | Leitores biópticos multiplano e leitores-balança |
| CS6080 | Leitor companheiro sem fios |

Um leitor sem fios comunica com a sua base, e é a base que se liga ao computador: ligue a base por
USB e trate o par como um único leitor. Leitores de outras famílias Zebra também podem funcionar, se
o Scanner SDK da Zebra os suportar num dos modos USB do [passo 6](#6-definir-o-modo-usb-do-leitor-zebra).

### Funções GS1 integradas nos leitores Zebra

Para além de ler os códigos de barras, alguns leitores de imagem 2D Zebra conseguem processar dados
GS1 no próprio leitor, configurados através do 123Scan:

| Função | O que faz | Para o GAIA Desktop |
|---|---|---|
| **Label Parse+** | Analisa os AI de uma etiqueta GS1 no leitor e envia campos reformatados | **Deixar desativada** — o GAIA Desktop deixaria de ver os AI, os separadores FNC1 e o AIM Code ID |
| **GS1 Digital Link** | Para produtos de retalho marcados com um EAN/UPC 1D e um código Digital Link 2D: escolhe qual enviar | **Deixar desativada** (predefinição) |
| **GS1 Retail POS parsing** | Lê códigos de barras 2D de retalho em sintaxe de URI Digital Link ou de cadeia de elementos | **Deixar desativada** |

A definição **GS1 Digital Link** tem três modos. **Mode 1 — GTIN only**, o modo com que arranca
assim que é ativada, envia apenas o GTIN, venha de que código de barras vier, pelo que um lote, uma
data de validade ou um número de série do Digital Link nunca chegariam ao GAIA Desktop.
**Mode 2 — Prioritize 2D** envia o código 2D se for visto dentro do tempo de prioridade (200 ms por
predefinição), caso contrário o 1D. **Mode 3 — Corroborate 1D and 2D GTINs Match** envia o 1D, e
também o 2D quando o GTIN coincide. Se a definição tiver de estar ativa para outro software, use o
Mode 2, não o Mode 1. No DS8108 são os parâmetros 2373 (ativo/inativo), 2374 (modo) e 2491 (tempo
limite).

Estas funções requerem o **123Scan versão 6 ou mais recente** e um firmware que as inclua. A Zebra
tem vindo a acrescentar suporte para GS1 Digital Link por atualização de firmware desde abril de
2025; as datas seguintes vêm de um revendedor Zebra, por isso verifique as notas de versão do
firmware de cada modelo:

| Família de modelos | Firmware com GS1 Digital Link publicado |
|---|---|
| DS81x8 | 16 de abril de 2025 |
| MP7000 | 21 de abril de 2025 |
| DS9908 | 6 de junho de 2025 |
| SP72 / SM72 | 11 de julho de 2025 |
| DS46x8 | 28 de julho de 2025 |
| MP7200 (Rev-C), DS5502 | 30 de julho de 2025 |
| DS9308 | 29 de agosto de 2025 |
| DS22x8, DS1000 | 5 de setembro de 2025 (só Ásia-Pacífico) |
| DS36x8 (incluindo KD e XR), MS47 | 30 de setembro de 2025 |
| PL5000, SE4107 | 15 de outubro de 2025 |
| CS6080 | 31 de dezembro de 2025 |

Nenhuma destas funções é necessária para ler códigos GS1 Digital Link no GAIA Desktop: com elas
desativadas, um leitor 2D Zebra com o firmware atual lê um código QR Digital Link como qualquer
outro, e o GAIA Desktop converte o URI nos seus AI.

Fontes: Zebra, *DS8108 Product Reference Guide*, MN-002926-11EN Rev A (agosto de 2025), capítulo 12
«GS1 Digital Link Parameters» e capítulo Data Parsing; Zebra, *123Scan release notes*;
Barcode Factory, *Upgrade Zebra Scanners for GS1 Digital Link* (datas de firmware).
