# T2 — Analisador Sintático

## Descrição

Implementação do analisador sintático da **Linguagem Algorítmica (LA)**, desenvolvida pelo prof. Jander Moreira no DC/UFSCar.

O analisador utiliza o **ANTLR4** para geração automática do Lexer e Parser a partir da gramática oficial da linguagem LA. Erros léxicos (do T1) continuam sendo detectados corretamente. Em caso de erro sintático, a linha e o lexema causador são reportados.

---

## Pré-requisitos

| Ferramenta   | Versão mínima | Download |
|--------------|---------------|----------|
| Java JDK     | 11            | https://adoptium.net |
| Apache Maven | 3.6           | https://maven.apache.org/download.cgi |

> O ANTLR4 é baixado automaticamente pelo Maven — não é necessário instalá-lo separadamente.

Verifique as versões instaladas:

```bash
java -version
mvn -version
```

---

## Como compilar

Dentro da pasta `t2/`:

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
java -jar target/compilador-la.jar entrada.txt saida.txt
```

**Exemplo (Windows):**
```cmd
java -jar target\compilador-la.jar C:\testes\entrada.txt C:\temp\saida.txt
```

---

## Formato da saída

### Programa válido

```
Fim da compilacao
```

### Erro sintático

```
Linha N: erro sintatico proximo a LEXEMA
Fim da compilacao
```

### Erros léxicos (herdados do T1)

| Tipo de erro           | Formato da mensagem                     |
|------------------------|-----------------------------------------|
| Símbolo inválido       | `Linha N: X - simbolo nao identificado` |
| Comentário não fechado | `Linha N: comentario nao fechado`       |
| Cadeia não fechada     | `Linha N: cadeia literal nao fechada`   |

Em todos os casos de erro, a saída é encerrada com `Fim da compilacao`.

---

## Estrutura do projeto

```
t2/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── antlr4/
        │   └── br/ufscar/dc/compiladores/sintatico/
        │       └── LA.g4                        # Gramática oficial da linguagem LA
        └── java/
            └── br/ufscar/dc/compiladores/sintatico/
                ├── Principal.java               # Ponto de entrada (main)
                ├── LALexicalErrorListener.java  # Tratamento de erros léxicos
                └── LASyntaxErrorListener.java   # Tratamento de erros sintáticos
```

> As classes `LALexer.java` e `LAParser.java` são **geradas automaticamente** pelo ANTLR4 durante o build e não estão no repositório.

---

## Tecnologias utilizadas

- **Java 11+**
- **ANTLR 4.13.1** — gerador de parsers a partir da gramática `LA.g4`
- **Maven 3.6+** — build e gerenciamento de dependências