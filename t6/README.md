# T6 — MusicaLA: Compilador de Melodias

## Descrição

**MusicaLA** é uma DSL (Domain Specific Language) declarativa para descrever melodias musicais em português. O compilador valida léxico, sintaxe e semântica, e gera código C que toca as notas via síntese senoidal com `sox`.

**Exemplo de programa:**
```
musica "Ode a Alegria"
  bpm 120
  instrumento piano

  nota MI4 quarto
  nota FA4 quarto
  nota SOL4 meio
fim_musica
```

---

## Pré-requisitos

| Ferramenta   | Versão mínima | Download |
|--------------|---------------|----------|
| Java JDK     | 21            | https://adoptium.net |
| Apache Maven | 3.6           | https://maven.apache.org/download.cgi |
| GCC          | qualquer      | Nativo no Linux/macOS; Windows: https://www.mingw-w64.org |
| sox          | qualquer      | Linux: `sudo apt install sox` |

> O ANTLR4 é baixado automaticamente pelo Maven.

Verifique as versões instaladas:

```bash
java -version
mvn -version
gcc --version
sox --version
```

---

## Como compilar o compilador

Dentro da pasta `t6/compilador-musical/`:

```bash
cd t6/compilador-musical
mvn package
```

Gera o JAR em:

```
t6/compilador-musical/target/compilador-musica.jar
```

---

## Como usar

**Passo 1:** Escreva sua melodia em um arquivo `.musica`.

**Passo 2:** Compile para código C:

```bash
java -jar t6/compilador-musical/target/compilador-musica.jar entrada.musica saida.c
```

**Passo 3:** Compile e execute o código C gerado:

```bash
gcc saida.c -o musica
./musica
```

---

## Rodando os testes automáticos (casos de teste)

Verifica erros léxicos, sintáticos e semânticos:

```bash
cd t6/casos-teste-t6
chmod +x testar.sh
./testar.sh
```

O script compila automaticamente o JAR via `mvn package` em `../compilador-musical`, testa todos os arquivos em `entrada/`, compara com `saida_esperada/` e verifica se os casos válidos geram C compilável.

---

## Executando as melodias de exemplo

### Tocar todas as melodias

Compila, linka e executa cada melodia em sequência:

```bash
cd t6/melodias
chmod +x executar_todas.sh
./executar_todas.sh
```

Lê todos os `.musica` de `entrada/`, gera os `.c` em `saida/`, compila com GCC e toca cada música com `sox`.

### Verificar geração de código das melodias

Compara o código C gerado com o código C esperado armazenado em `saida_esperada/`:

```bash
cd t6/melodias
chmod +x testar_melodias.sh
./testar_melodias.sh
```

Para cada melodia em `entrada/`, o script gera o `.c` em `saida/` e compara com o arquivo correspondente em `saida_esperada/`, reportando diferenças caso existam.

---

## Melodias de exemplo disponíveis

| Arquivo | Melodia | BPM |
|---------|---------|-----|
| `parabens.musica` | Parabéns pra Você | 100 |
| `brilha_estrelinha.musica` | Brilha Brilha Estrelinha | 110 |
| `ode_alegria.musica` | Ode à Alegria — Beethoven | 120 |
| `fur_elise.musica` | Für Elise — Beethoven | 140 |
| `jingle_bells.musica` | Jingle Bells | 160 |
| `tetris.musica` | Tetris (Korobeiniki) | 160 |
| `piratas_caribe.musica` | Piratas do Caribe | 160 |
| `super_mario.musica` | Super Mario Bros | 200 |
| `marcha_imperial.musica` | Marcha Imperial — Star Wars | 100 |

---

## Sintaxe da linguagem

### Estrutura geral

```
musica "Título"
  [bpm NUMERO]
  [instrumento NOME]
  [repeticoes NUMERO]
  (notas, pausas e sequências)
fim_musica
```

Múltiplos blocos podem existir no mesmo arquivo e são tocados em sequência.

### Notas e pausas

```
nota NOME_NOTA DURACAO
pausa DURACAO
```

**Notas:** `DO`, `RE`, `MI`, `FA`, `SOL`, `LA`, `SI`  
**Acidentes:** `#` (sustenido) ou `b` (bemol) — ex: `DO#4`, `SIb3`  
**Oitavas:** `1` a `8`

### Durações

| Duração    | Valor        |
|------------|--------------|
| `inteiro`  | 4 × quarto   |
| `meio`     | 2 × quarto   |
| `quarto`   | 1 × quarto   |
| `colcheia` | 1/2 × quarto |
| `semi`     | 1/4 × quarto |

### Sequência nomeada

```
sequencia "nome"
  nota DO4 quarto
fim_sequencia
```

### Configurações

| Configuração        | Intervalo válido | Padrão |
|---------------------|------------------|--------|
| `bpm NUMERO`        | 20–300           | 120    |
| `instrumento NOME`  | qualquer ident   | —      |
| `repeticoes NUMERO` | >= 1             | 1      |

### Comentários

```
// comentário de linha
```

---

## Verificações semânticas

| Erro | Mensagem |
|------|----------|
| BPM fora do intervalo | `Linha N: bpm X fora do intervalo valido (20-300)` |
| Repetições inválidas  | `Linha N: repeticoes X invalido (deve ser >= 1)` |
| Configuração duplicada | `Linha N: configuracao X ja declarada nesta musica` |
| Música sem notas      | `Linha N: musica "X" nao contem notas ou pausas` |

Erros semânticos não interrompem a análise — todos são reportados antes do `Fim da compilacao`.

---

## Estrutura do projeto

```
t6/
├── README.md
├── compilador-musical/          # Código-fonte do compilador
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── antlr4/
│           │   └── br/ufscar/dc/compiladores/musica/
│           │       └── Musica.g4              # Gramática da linguagem MusicaLA
│           └── java/
│               └── br/ufscar/dc/compiladores/musica/
│                   ├── Principal.java                  # Ponto de entrada
│                   ├── MusicaLexicalErrorListener.java # Erros léxicos
│                   ├── MusicaSyntaxErrorListener.java  # Erros sintáticos
│                   ├── MusicaSemanticoVisitor.java      # Análise semântica
│                   └── MusicaGeradorVisitor.java        # Geração de código C
├── casos-teste-t6/              # Testes automáticos do compilador
│   ├── entrada/                 # Arquivos .musica de entrada
│   ├── saida_esperada/          # Saídas esperadas dos casos de erro
│   └── testar.sh                # Script de teste automático
└── melodias/                    # Melodias de exemplo
    ├── entrada/                 # Arquivos .musica das melodias
    ├── saida/                   # Código C e executáveis gerados
    ├── saida_esperada/          # Código C esperado para comparação
    ├── executar_todas.sh        # Compila e toca todas as melodias
    └── testar_melodias.sh       # Verifica geração de código das melodias
```

> `MusicaLexer.java`, `MusicaParser.java` e `MusicaBaseVisitor.java` são **gerados automaticamente** pelo ANTLR4 durante o build.

---

## Tecnologias utilizadas

- **Java 21**
- **ANTLR 4.13.1**
- **Maven 3.6+**
- **GCC**
- **sox** — síntese e reprodução de áudio via onda senoidal (`play -n synth`)
