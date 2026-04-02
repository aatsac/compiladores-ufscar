# T5 — Gerador de Código C

## Descrição

Implementação do gerador de código C da **Linguagem Algorítmica (LA)**, desenvolvida pelo prof. Jander Moreira no DC/UFSCar.

Combina todas as fases anteriores (léxica, sintática, semântica) em um único executável. Se o programa de entrada não contiver erros, a saída é código C válido e compilável com GCC. Se houver erros, a saída contém as mensagens de erro das fases anteriores.

---

## Pré-requisitos

| Ferramenta   | Versão mínima | Download |
|--------------|---------------|----------|
| Java JDK     | 11            | https://adoptium.net |
| Apache Maven | 3.6           | https://maven.apache.org/download.cgi |
| GCC          | qualquer      | Já presente no Linux/macOS; Windows: https://www.mingw-w64.org |

> O ANTLR4 é baixado automaticamente pelo Maven — não é necessário instalá-lo separadamente.

Verifique as versões instaladas:

```bash
java -version
mvn -version
gcc --version
```

---

## Como compilar

Dentro da pasta `t5/`:

```bash
mvn package
```

O Maven irá automaticamente:
1. Baixar a dependência do ANTLR4
2. Gerar `LALexer.java` e `LAParser.java` a partir de `LA.g4`
3. Compilar todos os fontes Java
4. Gerar o JAR executável em `target/compilador-la.jar`

---

## Como executar

```bash
java -jar target/compilador-la.jar <arquivo-entrada> <arquivo-saida>
```

**Exemplo (Linux/macOS):**
```bash
java -jar target/compilador-la.jar programa.alg saida.c
gcc saida.c -o programa
./programa
```

**Exemplo (Windows):**
```cmd
java -jar target\compilador-la.jar programa.alg saida.c
gcc saida.c -o programa.exe
programa.exe
```

---

## Comportamento da saída

| Situação         | Conteúdo do arquivo de saída              |
|------------------|-------------------------------------------|
| Programa válido  | Código C compilável (sem mensagens)       |
| Erro léxico      | Mensagem de erro + `Fim da compilacao`    |
| Erro sintático   | Mensagem de erro + `Fim da compilacao`    |
| Erro semântico   | Todas as mensagens + `Fim da compilacao`  |

---

## Mapeamento LA → C

| LA                          | C                            |
|-----------------------------|------------------------------|
| `inteiro`                   | `int`                        |
| `real`                      | `float`                      |
| `literal`                   | `char[80]`                   |
| `logico`                    | `int`                        |
| `^tipo`                     | `tipo*`                      |
| `constante X: tipo = val`   | `#define X val`              |
| `tipo T: registro ...`      | `typedef struct { ... } T;`  |
| `registro` inline           | `struct { ... }`             |
| `leia(x)` inteiro/real      | `scanf("%d/%f", &x)`         |
| `leia(s)` literal           | `gets(s)`                    |
| `escreva(x)`                | `printf("%formato", x)`      |
| `se/entao/senao`            | `if/else`                    |
| `enquanto/faca`             | `while`                      |
| `para/ate/faca`             | `for`                        |
| `faca/ate`                  | `do/while`                   |
| `caso/seja`                 | `switch/case`                |
| `procedimento`              | `void func(params)`          |
| `funcao`                    | `tipo func(params)`          |
| `retorne`                   | `return`                     |
| `e` / `ou` / `nao`         | `&&` / `\|\|` / `!`          |
| `=` / `<>`                  | `==` / `!=`                  |
| `<-`                        | `=`                          |
| `&ident`                    | `&ident`                     |
| `^ident`                    | `*ident`                     |

---

## Estrutura do projeto

```
t5/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── antlr4/
        │   └── br/ufscar/dc/compiladores/semantico/
        │       └── LA.g4                        # Gramática oficial da linguagem LA
        └── java/
            └── br/ufscar/dc/compiladores/semantico/
                ├── Principal.java               # Ponto de entrada — pipeline completo
                ├── LALexicalErrorListener.java  # Tratamento de erros léxicos
                ├── LASyntaxErrorListener.java   # Tratamento de erros sintáticos
                ├── LASemanticoVisitor.java       # Visitor de análise semântica
                ├── LAGeradorVisitor.java         # Visitor de geração de código C
                ├── TabelaDeSimbolos.java         # Tabela de símbolos com escopos
                └── EntradaTabelaDeSimbolos.java  # Entrada da tabela (nome, tipo, categoria, parâmetros)
```

> As classes `LALexer.java` e `LAParser.java` são **geradas automaticamente** pelo ANTLR4 durante o build e não estão no repositório.

---

## Tecnologias utilizadas

- **Java 11+**
- **ANTLR 4.13.1** — gerador de parsers a partir da gramática `LA.g4`
- **Maven 3.6+** — build e gerenciamento de dependências
- **GCC** — para compilar o código C gerado
