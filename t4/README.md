# T4 — Analisador Semântico (Parte 2)

## Descrição

Extensão do analisador semântico da **Linguagem Algorítmica (LA)**, desenvolvida pelo prof. Jander Moreira no DC/UFSCar.

Inclui todas as verificações do T3, acrescidas de:
- Declaração duplicada envolvendo ponteiros, registros e funções
- Identificador não declarado envolvendo ponteiros, registros e funções
- Incompatibilidade de argumentos em chamadas de procedimentos e funções
- Atribuição incompatível envolvendo ponteiros e registros
- Uso do comando `retorne` fora de uma função

Não interrompe ao primeiro erro — reporta todos os erros do programa antes de encerrar.

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

Dentro da pasta `t4/`:

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

### Programa sem erros

```
Fim da compilacao
```

### Programa com erros semânticos

Todos os erros são reportados antes do `Fim da compilacao`:

```
Linha 7: tipo inteir nao declarado
Linha 11: identificador idades nao declarado
Fim da compilacao
```

### Erros léxicos e sintáticos (herdados do T1 e T2)

Quando ocorrem, a execução é interrompida imediatamente:

| Tipo de erro           | Formato da mensagem                     |
|------------------------|-----------------------------------------|
| Símbolo inválido       | `Linha N: X - simbolo nao identificado` |
| Comentário não fechado | `Linha N: comentario nao fechado`       |
| Cadeia não fechada     | `Linha N: cadeia literal nao fechada`   |
| Erro sintático         | `Linha N: erro sintatico proximo a X`   |

---

## Erros semânticos detectados

### Herdados do T3

| Tipo de erro | Formato da mensagem |
|---|---|
| Identificador já declarado | `Linha N: identificador X ja declarado anteriormente` |
| Tipo não declarado | `Linha N: tipo X nao declarado` |
| Identificador não declarado | `Linha N: identificador X nao declarado` |
| Atribuição incompatível | `Linha N: atribuicao nao compativel para X` |

### Novos no T4

| Tipo de erro | Formato da mensagem |
|---|---|
| Argumentos incompatíveis em chamada | `Linha N: incompatibilidade de parametros na chamada de X` |
| `retorne` fora de função | `Linha N: comando retorne nao permitido nesse escopo` |

---

## Regras de compatibilidade

### Atribuição

| Lado esquerdo      | Lado direito aceito         |
|--------------------|-----------------------------|
| `inteiro`          | `inteiro` ou `real`         |
| `real`             | `inteiro` ou `real`         |
| `literal`          | `literal`                   |
| `logico`           | `logico`                    |
| registro (tipo X)  | registro (mesmo tipo X)     |
| ponteiro (`^tipo`) | endereço (`&ident`)         |

### Passagem de parâmetros (mais restrita que atribuição)

| Parâmetro formal   | Argumento real aceito    |
|--------------------|--------------------------|
| `inteiro`          | `inteiro` (apenas)       |
| `real`             | `real` (apenas)          |
| `literal`          | `literal`                |
| `logico`           | `logico`                 |
| registro (tipo X)  | registro (mesmo tipo X)  |
| ponteiro (`^tipo`) | endereço (`&ident`)      |

> Nota: para parâmetros, `inteiro` e `real` **não** são intercambiáveis.

---

## Estrutura do projeto

```
t4/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── antlr4/
        │   └── br/ufscar/dc/compiladores/semantico/
        │       └── LA.g4                        # Gramática oficial da linguagem LA
        └── java/
            └── br/ufscar/dc/compiladores/semantico/
                ├── Principal.java               # Ponto de entrada (main)
                ├── LALexicalErrorListener.java  # Tratamento de erros léxicos
                ├── LASyntaxErrorListener.java   # Tratamento de erros sintáticos
                ├── LASemanticoVisitor.java       # Visitor de análise semântica
                ├── TabelaDeSimbolos.java         # Tabela de símbolos com escopos
                └── EntradaTabelaDeSimbolos.java  # Entrada da tabela (nome, tipo, categoria, parâmetros)
```

> As classes `LALexer.java` e `LAParser.java` são **geradas automaticamente** pelo ANTLR4 durante o build e não estão no repositório.

---

## Tecnologias utilizadas

- **Java 11+**
- **ANTLR 4.13.1** — gerador de parsers a partir da gramática `LA.g4`
- **Maven 3.6+** — build e gerenciamento de dependências
