# T6 — MusicaLA: Compilador de Melodias

## Descrição

**MusicaLA** é uma linguagem declarativa para descrever melodias musicais em português. O compilador lê um arquivo `.musica`, valida léxico, sintaxe e semântica, e gera código C que toca as notas via síntese senoidal com `sox` (`play`).

**Exemplo de programa:**
```text
musica "Ode a Alegria"
  bpm 120
  instrumento piano

  nota MI4 quarto
  nota MI4 quarto
  nota FA4 quarto
  nota SOL4 quarto
  nota MI4 meio
fim_musica
```

---

## Membros do grupo

> Preencha aqui os nomes e RAs dos integrantes do grupo.

---

## Pré-requisitos

| Ferramenta   | Versão mínima | Download |
|--------------|---------------|----------|
| Java JDK     | 11            | https://adoptium.net |
| Apache Maven | 3.6           | https://maven.apache.org/download.cgi |
| GCC          | qualquer      | Nativo no Linux/macOS; Windows: https://www.mingw-w64.org |
| sox          | qualquer      | Linux: `sudo apt install sox` |

> O ANTLR4 é baixado automaticamente pelo Maven.

---

## Como compilar o compilador

```bash
cd t6/compilador-musical
mvn package
```

Isso gera:

```text
compilador-musical/target/compilador-musica.jar
```

---

## Como usar

**Passo 1:** Escreva sua melodia em um arquivo `.musica`.

**Passo 2:** Compile para código C:

```bash
cd t6/compilador-musical
java -jar target/compilador-musica.jar ../melodias/entrada/minha_musica.musica ../melodias/saida/saida.c
```

**Passo 3:** Compile e execute o código C gerado:

```bash
gcc ../melodias/saida/saida.c -o musica
./musica
```

> No Linux, o programa usa `sox` (comando `play`) para sintetizar as notas via onda senoidal.

---

## Sintaxe da linguagem

### Estrutura geral

```text
musica "Título da Música"
  [bpm NUMERO]
  [instrumento NOME]
  [repeticoes NUMERO]

  (notas, pausas e sequências)

fim_musica
```

Múltiplos blocos `musica ... fim_musica` podem existir no mesmo arquivo e são tocados em sequência.

### Notas

```text
nota NOME_NOTA DURACAO
```

**Notas disponíveis:** `DO`, `RE`, `MI`, `FA`, `SOL`, `LA`, `SI`  
**Acidentes:** `#` (sustenido) ou `b` (bemol) — ex: `DO#`, `SIb`  
**Oitavas:** `1` a `8` — ex: `DO4`, `LA#5`, `MIb3`

### Durações

| Duração    | Valor        | Descrição |
|------------|--------------|-----------|
| `inteiro`  | 4 × quarto   | Nota inteira |
| `meio`     | 2 × quarto   | Meia nota |
| `quarto`   | 1 × quarto   | Semínima |
| `colcheia` | 1/2 × quarto | Colcheia |
| `semi`     | 1/4 × quarto | Semicolcheia |

### Pausa

```text
pausa DURACAO
```

### Sequência nomeada

```text
sequencia "nome da frase"
  nota DO4 quarto
  nota RE4 quarto
fim_sequencia
```

### Configurações

| Configuração | Descrição | Padrão |
|---|---|---|
| `bpm NUMERO` | Andamento em batidas por minuto (20–300) | 120 |
| `instrumento NOME` | Nome do instrumento (informativo) | — |
| `repeticoes NUMERO` | Quantas vezes repetir o bloco (>= 1) | 1 |

### Comentários

```text
// Este é um comentário de linha
```

---

## Exemplos completos

### 1. Parabéns

```text
musica "Parabens"
  bpm 100
  instrumento piano

  nota SOL4 colcheia
  nota SOL4 semi
  nota LA4 quarto
  nota SOL4 quarto
  nota DO5 quarto
  nota SI4 meio
  pausa quarto
fim_musica
```

### 2. Dormir com repetições

```text
musica "Brilha Brilha Estrelinha"
  bpm 110
  repeticoes 2

  sequencia "verso"
    nota DO4 quarto
    nota DO4 quarto
    nota SOL4 quarto
    nota SOL4 quarto
    nota LA4 quarto
    nota LA4 quarto
    nota SOL4 meio
  fim_sequencia

fim_musica
```

### 3. Escala cromática

```text
musica "Escala"
  bpm 140

  nota DO4 colcheia
  nota DO#4 colcheia
  nota RE4 colcheia
  nota RE#4 colcheia
  nota MI4 colcheia
  nota FA4 colcheia
  nota FA#4 colcheia
  nota SOL4 colcheia
fim_musica
```

---

## Verificações semânticas

O compilador detecta 4 tipos de erros semânticos:

| Erro | Mensagem |
|---|---|
| BPM fora do intervalo (20–300) | `Linha N: bpm X fora do intervalo valido (20-300)` |
| Repetições inválidas (< 1) | `Linha N: repeticoes X invalido (deve ser >= 1)` |
| Configuração duplicada | `Linha N: configuracao X ja declarada nesta musica` |
| Música sem notas ou pausas | `Linha N: musica "X" nao contem notas ou pausas` |

Erros semânticos **não interrompem** a análise — todos são reportados antes do `Fim da compilacao`.

---

## Rodando os testes automaticamente

```bash
cd t6/casos-teste-t6
chmod +x testar.sh
./testar.sh
```

O script:

- entra em `../compilador-musical`
- executa `mvn package`
- usa o JAR gerado
- testa todos os arquivos em `entrada/`
- compara com `saida_esperada/`
- verifica se os válidos geram C compilável

---

## Executando todas as melodias

```bash
cd t6/melodias
chmod +x executar_todas.sh
./executar_todas.sh
```

Esse script:

- lê todos os `.musica` em `entrada/`
- gera os `.c` em `saida/`
- compila com `gcc`
- executa automaticamente para tocar as músicas

---

## Estrutura do projeto

```text
t6/
├── readme.md
├── compilador-musical/
│   ├── pom.xml
│   ├── src/
│   └── target/
├── casos-teste-t6/
│   ├── entrada/
│   ├── saida_esperada/
│   └── testar.sh
└── melodias/
    ├── entrada/
    ├── saida/
    └── executar_todas.sh
```

---

## Tecnologias utilizadas

- **Java 11+**
- **ANTLR4**
- **Maven**
- **GCC**
- **sox (`play`)**