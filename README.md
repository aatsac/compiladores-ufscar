# Compilador LA — Construção de Compiladores

Disciplina: **Construção de Compiladores** — DC/UFSCar  
Professor: Daniel Lucrédio

---

## Membros do grupo

> Adriano Tavares | RA: 811389.

---

## Descrição

Este repositório contém a implementação incremental de compiladores desenvolvidos na disciplina **Construção de Compiladores**.

Os cinco primeiros trabalhos implementam um compilador completo para a **Linguagem Algorítmica (LA)**, desenvolvida pelo prof. Jander Moreira no DC/UFSCar.

Como extensão criativa do projeto, o **T6** introduz a **MusicaLA**, uma linguagem declarativa em português para descrição de melodias, compilada para código C com síntese sonora via `sox`.

O compilador foi construído em seis trabalhos, cada um adicionando uma nova fase ou aplicação do pipeline:

| Trabalho | Fase | Descrição |
|----------|---|---|
| T1 | Análise Léxica | Reconhece tokens e reporta erros léxicos |
| T2 | Análise Sintática | Valida a estrutura gramatical do programa |
| T3 | Análise Semântica (Parte 1) | Verifica tipos, declarações e atribuições |
| T4 | Análise Semântica (Parte 2) | Adiciona verificação de chamadas e ponteiros |
| T5 | Geração de Código | Produz código C compilável equivalente ao programa |
| T6 | DSL Musical + Geração de Áudio | Compila melodias em português para C executável com `sox` |

---

## Estrutura do repositório

```text
/
├── README.md   ← este arquivo
├── t1/         ← Analisador Léxico
├── t2/         ← Analisador Sintático
├── t3/         ← Analisador Semântico (Parte 1)
├── t4/         ← Analisador Semântico (Parte 2)
├── t5/         ← Gerador de Código C
└── t6/         ← MusicaLA: compilador de melodias
```

Cada pasta contém seu próprio `README.md` com instruções detalhadas de compilação e execução.

---

## Pré-requisitos gerais

| Ferramenta   | Versão mínima | Download |
|--------------|---------------|----------|
| Java JDK     | 11 | https://adoptium.net |
| Apache Maven | 3.6 | https://maven.apache.org/download.cgi |
| GCC          | qualquer | Linux/macOS: nativo; Windows: https://www.mingw-w64.org |
| sox          | qualquer | Linux: `sudo apt install sox` |

> O **ANTLR4** é baixado automaticamente pelo Maven.

---

## Como compilar e executar cada trabalho

Todos os trabalhos seguem o mesmo padrão de build e execução. Acesse a pasta do trabalho desejado e execute:

```bash
mvn package
java -jar target/compilador-la.jar <arquivo-entrada> <arquivo-saida>
```

> No **T6**, o nome do JAR e os arquivos de entrada/saída mudam conforme o README específico do projeto MusicaLA.

Consulte o `README.md` de cada pasta para exemplos e detalhes específicos.

---

## Linguagem LA — Visão geral

A **Linguagem Algorítmica (LA)** é um pseudo-código estruturado com sintaxe em português, voltado ao ensino de algoritmos. Suporta:

- Tipos primitivos: `inteiro`, `real`, `literal`, `logico`
- Tipos compostos: registros (`registro ... fim_registro`) e vetores (`ident[n]`)
- Ponteiros: `^tipo` e operador de endereço `&`
- Estruturas de controle: `se/entao/senao`, `enquanto/faca`, `para/ate/faca`, `faca/ate`, `caso/seja`
- Subprogramas: `procedimento` e `funcao`
- Entrada/saída: `leia` e `escreva`

---

## T6 — MusicaLA

O **T6** expande os conceitos da disciplina para uma **DSL (Domain Specific Language)** musical.

A linguagem permite descrever melodias em português com elementos como:

- `nota`
- `pausa`
- `sequencia`
- `bpm`
- `repeticoes`
- `instrumento`

Exemplo:

```text
musica "Ode a Alegria"
  bpm 120
  nota MI4 quarto
  nota FA4 quarto
  nota SOL4 meio
fim_musica
```

O código gerado é convertido em C e executado com síntese senoidal usando `sox`.

Esse trabalho demonstra aplicação prática de:

- análise léxica
- parsing com ANTLR4
- visitor pattern
- verificação semântica
- geração de código
- design de DSL

---

## Pipeline do compilador

```text
Código-fonte
    │
    ▼
┌─────────────────┐
│ Análise Léxica  │
└────────┬────────┘
         │
         ▼
┌──────────────────────┐
│ Análise Sintática    │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ Análise Semântica    │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ Geração de Código C  │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ Execução / Saída     │
└──────────────────────┘
```

> Esse pipeline é compartilhado entre a LA (T1–T5) e a DSL musical do T6.

---

## Tecnologias utilizadas

- **Java 11+**
- **ANTLR 4.13.1** — gerador de parsers
- **Maven 3.6+** — build e gerenciamento de dependências
- **GCC** — compilação do código C gerado
- **sox** — síntese e reprodução de áudio no T6

