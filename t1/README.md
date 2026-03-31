# T1 — Analisador Léxico

## Descrição

Implementação do analisador léxico da **Linguagem Algorítmica (LA)**, desenvolvida pelo prof. Jander Moreira no DC/UFSCar.

O analisador lê um programa-fonte em LA e produz a lista de tokens reconhecidos. Em caso de erro léxico, a execução é interrompida e a mensagem de erro é gravada na saída.

---

## Pré-requisitos

| Ferramenta   | Versão mínima | Download |
|--------------|---------------|----------|
| Java JDK     | 11            | https://adoptium.net |
| Apache Maven | 3.6           | https://maven.apache.org/download.cgi |

Verifique as versões instaladas:

```bash
java -version
mvn -version
```

---

## Como compilar

Dentro da pasta `t1/`:

```bash
mvn package
```

O Maven compila o projeto e gera o JAR executável em `target/compilador-la.jar`.

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

Cada token reconhecido é impresso em uma linha no formato:

```
<'lexema','TIPO'>    ← tokens genéricos: IDENT, NUM_INT, NUM_REAL, CADEIA
<'lexema','lexema'>  ← palavras reservadas e símbolos
```

**Exemplo de entrada:**
```
algoritmo
    declare
        idade: inteiro
    leia(idade)
fim_algoritmo
```

**Saída produzida:**
```
<'algoritmo','algoritmo'>
<'declare','declare'>
<'idade',IDENT>
<':',':'>
<'inteiro','inteiro'>
<'leia','leia'>
<'(','('>
<'idade',IDENT>
<')',')'>
<'fim_algoritmo','fim_algoritmo'>
```

### Erros léxicos

| Tipo de erro             | Formato da mensagem                      |
|--------------------------|------------------------------------------|
| Símbolo inválido         | `Linha N: X - simbolo nao identificado`  |
| Comentário não fechado   | `Linha N: comentario nao fechado`        |
| Cadeia não fechada       | `Linha N: cadeia literal nao fechada`    |

Ao encontrar um erro, os tokens reconhecidos até aquele ponto são impressos normalmente, seguidos da mensagem de erro. A execução é interrompida.

---

## Tokens reconhecidos

### Palavras reservadas

`algoritmo` `fim_algoritmo` `declare` `constante` `tipo` `procedimento` `fim_procedimento` `funcao` `fim_funcao` `retorne` `var` `registro` `fim_registro` `leia` `escreva` `se` `entao` `fim_se` `senao` `caso` `seja` `fim_caso` `para` `ate` `fim_para` `enquanto` `faca` `fim_enquanto` `inteiro` `real` `literal` `logico` `verdadeiro` `falso` `nao` `e` `ou`

### Tokens genéricos

| Token     | Descrição             | Exemplo         |
|-----------|-----------------------|-----------------|
| `IDENT`   | Identificador         | `nome`, `x1`    |
| `NUM_INT` | Número inteiro        | `42`, `100`     |
| `NUM_REAL` | Número real          | `3.14`, `0.5`   |
| `CADEIA`  | String entre aspas    | `"olá mundo"`   |

### Símbolos especiais

`,` `;` `:` `.` `..` `(` `)` `[` `]` `<-` `<` `>` `<=` `>=` `<>` `=` `+` `-` `*` `/` `%` `^` `&`

---

## Estrutura do projeto

```
t1/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── br/ufscar/dc/compiladores/lexico/
                ├── Principal.java        # Ponto de entrada (main)
                ├── AnalisadorLexico.java # Implementação do AFD léxico
                ├── Token.java            # Representa um token reconhecido
                ├── TipoToken.java        # Enum com todos os tipos de token
                └── ErroLexico.java       # Exceção para erros léxicos
```