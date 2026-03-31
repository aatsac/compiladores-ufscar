package br.ufscar.dc.compiladores.lexico;

import java.util.HashMap;
import java.util.Map;

/**
 * Analisador léxico da linguagem LA (Linguagem Algorítmica).
 *
 * Responsável por ler o código-fonte caractere a caractere e produzir
 * uma sequência de tokens. Implementa um autômato finito determinístico (AFD)
 * para reconhecer os padrões léxicos da linguagem.
 *
 * Padrões reconhecidos:
 *  - Palavras reservadas (algoritmo, declare, leia, etc.)
 *  - Identificadores: letra seguida de letras, dígitos ou '_'
 *  - Números inteiros: sequência de dígitos
 *  - Números reais: inteiro seguido de '.' e mais dígitos
 *  - Cadeias: texto entre aspas duplas (na mesma linha)
 *  - Comentários: texto entre '{' e '}' (ignorados; devem estar na mesma linha)
 *  - Símbolos especiais: operadores, pontuação e delimitadores
 *
 * Erros léxicos reportados:
 *  - Símbolo não identificado
 *  - Cadeia não fechada na mesma linha
 *  - Comentário não fechado na mesma linha
 */
public class AnalisadorLexico {

    // -----------------------------------------------------------------------
    // Tabela de palavras reservadas: lexema → TipoToken
    // -----------------------------------------------------------------------
    private static final Map<String, TipoToken> PALAVRAS_RESERVADAS = new HashMap<>();

    static {
        // Registra cada palavra reservada mapeando seu lexema ao tipo correspondente
        PALAVRAS_RESERVADAS.put("algoritmo",       TipoToken.ALGORITMO);
        PALAVRAS_RESERVADAS.put("fim_algoritmo",   TipoToken.FIM_ALGORITMO);
        PALAVRAS_RESERVADAS.put("declare",         TipoToken.DECLARE);
        PALAVRAS_RESERVADAS.put("leia",            TipoToken.LEIA);
        PALAVRAS_RESERVADAS.put("escreva",         TipoToken.ESCREVA);
        PALAVRAS_RESERVADAS.put("enquanto",        TipoToken.ENQUANTO);
        PALAVRAS_RESERVADAS.put("faça",            TipoToken.FACA);
        PALAVRAS_RESERVADAS.put("faca",            TipoToken.FACA);
        PALAVRAS_RESERVADAS.put("fim_enquanto",    TipoToken.FIM_ENQUANTO);
        PALAVRAS_RESERVADAS.put("para",            TipoToken.PARA);
        PALAVRAS_RESERVADAS.put("até",             TipoToken.ATE);
        PALAVRAS_RESERVADAS.put("ate",             TipoToken.ATE);
        PALAVRAS_RESERVADAS.put("fim_para",        TipoToken.FIM_PARA);
        PALAVRAS_RESERVADAS.put("se",              TipoToken.SE);
        PALAVRAS_RESERVADAS.put("então",           TipoToken.ENTAO);
        PALAVRAS_RESERVADAS.put("entao",           TipoToken.ENTAO);
        PALAVRAS_RESERVADAS.put("fim_se",          TipoToken.FIM_SE);
        PALAVRAS_RESERVADAS.put("senão",           TipoToken.SENAO);
        PALAVRAS_RESERVADAS.put("senao",           TipoToken.SENAO);
        PALAVRAS_RESERVADAS.put("caso",            TipoToken.CASO);
        PALAVRAS_RESERVADAS.put("seja",            TipoToken.SEA);
        PALAVRAS_RESERVADAS.put("fim_caso",        TipoToken.FIM_CASO);
        PALAVRAS_RESERVADAS.put("procedimento",    TipoToken.PROCEDIMENTO);
        PALAVRAS_RESERVADAS.put("fim_procedimento",TipoToken.FIM_PROCEDIMENTO);
        PALAVRAS_RESERVADAS.put("função",          TipoToken.FUNCAO);
        PALAVRAS_RESERVADAS.put("funcao",          TipoToken.FUNCAO);
        PALAVRAS_RESERVADAS.put("fim_função",      TipoToken.FIM_FUNCAO);
        PALAVRAS_RESERVADAS.put("fim_funcao",      TipoToken.FIM_FUNCAO);
        PALAVRAS_RESERVADAS.put("retorne",         TipoToken.RETORNE);
        PALAVRAS_RESERVADAS.put("tipo",            TipoToken.TIPO);
        PALAVRAS_RESERVADAS.put("var",             TipoToken.VAR);
        PALAVRAS_RESERVADAS.put("constante",       TipoToken.CONSTANTE);
        PALAVRAS_RESERVADAS.put("verdadeiro",      TipoToken.VERDADEIRO);
        PALAVRAS_RESERVADAS.put("falso",           TipoToken.FALSO);
        PALAVRAS_RESERVADAS.put("não",             TipoToken.NAO);
        PALAVRAS_RESERVADAS.put("nao",             TipoToken.NAO);
        PALAVRAS_RESERVADAS.put("e",               TipoToken.E);
        PALAVRAS_RESERVADAS.put("ou",              TipoToken.OU);
        PALAVRAS_RESERVADAS.put("inteiro",         TipoToken.INTEIRO);
        PALAVRAS_RESERVADAS.put("real",            TipoToken.REAL);
        PALAVRAS_RESERVADAS.put("literal",         TipoToken.LITERAL);
        PALAVRAS_RESERVADAS.put("lógico",          TipoToken.LOGICO);
        PALAVRAS_RESERVADAS.put("logico",          TipoToken.LOGICO);
        PALAVRAS_RESERVADAS.put("registro",        TipoToken.REGISTRO);
        PALAVRAS_RESERVADAS.put("fim_registro",    TipoToken.FIM_REGISTRO);
    }

    // -----------------------------------------------------------------------
    // Estado interno do analisador
    // -----------------------------------------------------------------------

    /** Código-fonte completo como string. */
    private final String fonte;

    /** Posição atual do cursor no código-fonte. */
    private int pos;

    /** Número da linha atual (começa em 1). */
    private int linha;

    /**
     * Constrói o analisador léxico com o código-fonte a ser analisado.
     *
     * @param fonte conteúdo completo do arquivo-fonte
     */
    public AnalisadorLexico(String fonte) {
        this.fonte = fonte;
        this.pos   = 0;
        this.linha = 1;
    }

    // -----------------------------------------------------------------------
    // Métodos auxiliares de navegação no código-fonte
    // -----------------------------------------------------------------------

    /** @return caractere na posição atual, ou '\0' se fim do arquivo */
    private char atual() {
        return pos < fonte.length() ? fonte.charAt(pos) : '\0';
    }

    /** @return caractere na próxima posição (lookahead de 1), ou '\0' se fim do arquivo */
    private char proximo() {
        return (pos + 1) < fonte.length() ? fonte.charAt(pos + 1) : '\0';
    }

    /** Avança o cursor uma posição, atualizando o contador de linha. */
    private void avancar() {
        if (pos < fonte.length()) {
            if (fonte.charAt(pos) == '\n') linha++;
            pos++;
        }
    }

    // -----------------------------------------------------------------------
    // Métodos de classificação de caracteres
    // -----------------------------------------------------------------------

    /** @return true se o caractere é uma letra (incluindo acentuadas) ou '_' */
    private boolean isLetraOuUnderscore(char c) {
        return Character.isLetter(c) || c == '_';
    }

    /** @return true se o caractere é uma letra, dígito ou '_' */
    private boolean isAlfanumericoOuUnderscore(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    // -----------------------------------------------------------------------
    // Método principal: próximo token
    // -----------------------------------------------------------------------

    /**
     * Lê e retorna o próximo token do código-fonte.
     * Retorna null quando o fim do arquivo é atingido.
     *
     * @return próximo Token, ou null se fim do arquivo
     * @throws ErroLexico se um erro léxico for encontrado
     */
    public Token proximoToken() throws ErroLexico {
        // Pula espaços em branco e comentários
        pularEspacosEComentarios();

        // Fim do arquivo: não há mais tokens
        if (pos >= fonte.length()) {
            return null;
        }

        char c = atual();
        int linhaToken = linha; // guarda a linha onde o token começa

        // ---------------------------------------------------------------
        // Identificadores e palavras reservadas
        // Padrão: começa com letra ou '_', seguido de letras, dígitos ou '_'
        // ---------------------------------------------------------------
        if (isLetraOuUnderscore(c)) {
            return lerIdentificadorOuPalavraReservada(linhaToken);
        }

        // ---------------------------------------------------------------
        // Números inteiros e reais
        // Padrão: dígitos opcionalmente seguidos de '.' e mais dígitos
        // ---------------------------------------------------------------
        if (Character.isDigit(c)) {
            return lerNumero(linhaToken);
        }

        // ---------------------------------------------------------------
        // Cadeias de caracteres
        // Padrão: texto entre aspas duplas, na mesma linha
        // ---------------------------------------------------------------
        if (c == '"') {
            return lerCadeia(linhaToken);
        }

        // ---------------------------------------------------------------
        // Símbolos especiais (operadores, pontuação, delimitadores)
        // ---------------------------------------------------------------
        return lerSimbolo(linhaToken);
    }

    // -----------------------------------------------------------------------
    // Pula espaços em branco e comentários { ... }
    // -----------------------------------------------------------------------

    /**
     * Avança o cursor sobre espaços em branco e comentários entre chaves {}.
     * Comentários devem ser fechados na mesma linha; caso contrário, lança ErroLexico.
     *
     * @throws ErroLexico se um comentário não for fechado na mesma linha
     */
    private void pularEspacosEComentarios() throws ErroLexico {
        while (pos < fonte.length()) {
            char c = atual();

            // Espaço em branco: apenas avança
            if (Character.isWhitespace(c)) {
                avancar();

            // Início de comentário
            } else if (c == '{') {
                int linhaInicio = linha;
                avancar(); // consome '{'

                // Percorre o comentário até encontrar '}'
                while (pos < fonte.length() && atual() != '}') {
                    // Comentário não pode ultrapassar a linha onde foi aberto
                    if (atual() == '\n') {
                        throw new ErroLexico(
                            "comentario nao fechado",
                            linhaInicio,
                            "{"
                        );
                    }
                    avancar();
                }

                // Fim do arquivo sem fechar o comentário
                if (pos >= fonte.length()) {
                    throw new ErroLexico(
                        "comentario nao fechado",
                        linhaInicio,
                        "{"
                    );
                }

                avancar(); // consome '}'

            } else {
                // Não é espaço nem comentário: para o loop
                break;
            }
        }
    }

    // -----------------------------------------------------------------------
    // Reconhece identificadores e palavras reservadas
    // -----------------------------------------------------------------------

    /**
     * Lê um identificador ou palavra reservada a partir da posição atual.
     * Padrão: [letra|_][letra|dígito|_]*
     *
     * Após ler o lexema, verifica na tabela de palavras reservadas se é
     * uma palavra reservada; caso contrário, classifica como IDENT.
     *
     * @param linhaToken linha onde o token começa
     * @return Token do tipo IDENT ou palavra reservada
     */
    private Token lerIdentificadorOuPalavraReservada(int linhaToken) {
        StringBuilder sb = new StringBuilder();

        // Consome letras, dígitos e '_'
        while (pos < fonte.length() && isAlfanumericoOuUnderscore(atual())) {
            sb.append(atual());
            avancar();
        }

        String lexema = sb.toString();

        // Verifica se é uma palavra reservada
        TipoToken tipo = PALAVRAS_RESERVADAS.getOrDefault(lexema, TipoToken.IDENT);

        return new Token(lexema, tipo, linhaToken);
    }

    // -----------------------------------------------------------------------
    // Reconhece números inteiros e reais
    // -----------------------------------------------------------------------

    /**
     * Lê um número inteiro ou real a partir da posição atual.
     * Padrão inteiro: [0-9]+
     * Padrão real:    [0-9]+ '.' [0-9]+
     *
     * @param linhaToken linha onde o token começa
     * @return Token do tipo NUM_INT ou NUM_REAL
     */
    private Token lerNumero(int linhaToken) {
        StringBuilder sb = new StringBuilder();

        // Consome a parte inteira
        while (pos < fonte.length() && Character.isDigit(atual())) {
            sb.append(atual());
            avancar();
        }

        // Verifica se há parte decimal: '.' seguido de dígito
        if (atual() == '.' && Character.isDigit(proximo())) {
            sb.append('.'); // consome o '.'
            avancar();

            // Consome os dígitos da parte decimal
            while (pos < fonte.length() && Character.isDigit(atual())) {
                sb.append(atual());
                avancar();
            }

            return new Token(sb.toString(), TipoToken.NUM_REAL, linhaToken);
        }

        return new Token(sb.toString(), TipoToken.NUM_INT, linhaToken);
    }

    // -----------------------------------------------------------------------
    // Reconhece cadeias de caracteres
    // -----------------------------------------------------------------------

    /**
     * Lê uma cadeia de caracteres entre aspas duplas.
     * A cadeia deve começar e terminar na mesma linha.
     *
     * Padrão: '"' [qualquer caractere exceto '"' e '\n']* '"'
     *
     * @param linhaToken linha onde a cadeia começa
     * @return Token do tipo CADEIA com o lexema incluindo as aspas
     * @throws ErroLexico se a cadeia não for fechada na mesma linha
     */
    private Token lerCadeia(int linhaToken) throws ErroLexico {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        avancar(); // consome a aspas de abertura

        // Lê o conteúdo da cadeia até encontrar '"' ou fim de linha
        while (pos < fonte.length() && atual() != '"') {
            if (atual() == '\n') {
                throw new ErroLexico(
                    "cadeia literal nao fechada",
                    linhaToken,
                    sb.toString()
                );
            }
            sb.append(atual());
            avancar();
        }

        // Fim do arquivo sem fechar a cadeia
        if (pos >= fonte.length()) {
            throw new ErroLexico(
                "cadeia literal nao fechada",
                linhaToken,
                sb.toString()
            );
        }

        sb.append('"');
        avancar(); // consome a aspas de fechamento

        return new Token(sb.toString(), TipoToken.CADEIA, linhaToken);
    }

    // -----------------------------------------------------------------------
    // Reconhece símbolos especiais
    // -----------------------------------------------------------------------

    /**
     * Lê um símbolo especial (operador, pontuação ou delimitador).
     * Alguns símbolos são compostos de dois caracteres (ex: <-, <=, >=, <>, ..).
     *
     * @param linhaToken linha onde o símbolo começa
     * @return Token do tipo correspondente ao símbolo
     * @throws ErroLexico se o caractere não pertencer à linguagem
     */
    private Token lerSimbolo(int linhaToken) throws ErroLexico {
        char c = atual();

        // Símbolo '<': pode ser '<', '<-', '<=' ou '<>'
        if (c == '<') {
            avancar();
            if (atual() == '-') { avancar(); return new Token("<-", TipoToken.ATRIBUICAO,   linhaToken); }
            if (atual() == '=') { avancar(); return new Token("<=", TipoToken.MENOR_IGUAL,  linhaToken); }
            if (atual() == '>') { avancar(); return new Token("<>", TipoToken.DIFERENTE,    linhaToken); }
            return new Token("<", TipoToken.MENOR, linhaToken);
        }

        // Símbolo '>': pode ser '>' ou '>='
        if (c == '>') {
            avancar();
            if (atual() == '=') { avancar(); return new Token(">=", TipoToken.MAIOR_IGUAL, linhaToken); }
            return new Token(">", TipoToken.MAIOR, linhaToken);
        }

        // Símbolo '.': pode ser '.' (ponto simples) ou '..' (intervalo)
        if (c == '.') {
            avancar();
            if (atual() == '.') { avancar(); return new Token("..", TipoToken.PONTO_PONTO, linhaToken); }
            return new Token(".", TipoToken.PONTO, linhaToken);
        }

        // Símbolos de um único caractere
        avancar();
        switch (c) {
            case ',': return new Token(",", TipoToken.VIRGULA,          linhaToken);
            case ';': return new Token(";", TipoToken.PONTO_E_VIRGULA,  linhaToken);
            case ':': return new Token(":", TipoToken.DOIS_PONTOS,      linhaToken);
            case '(': return new Token("(", TipoToken.ABRE_PAREN,       linhaToken);
            case ')': return new Token(")", TipoToken.FECHA_PAREN,      linhaToken);
            case '[': return new Token("[", TipoToken.ABRE_COLCHETE,    linhaToken);
            case ']': return new Token("]", TipoToken.FECHA_COLCHETE,   linhaToken);
            case '=': return new Token("=", TipoToken.IGUAL,            linhaToken);
            case '+': return new Token("+", TipoToken.MAIS,             linhaToken);
            case '-': return new Token("-", TipoToken.MENOS,            linhaToken);
            case '*': return new Token("*", TipoToken.ASTERISCO,        linhaToken);
            case '/': return new Token("/", TipoToken.BARRA,            linhaToken);
            case '%': return new Token("%", TipoToken.PORCENTAGEM,      linhaToken);
            case '^': return new Token("^", TipoToken.CIRCUNFLEXO,      linhaToken);
            case '&': return new Token("&", TipoToken.AMPERSAND,        linhaToken);
            default:
                // Caractere não pertence à linguagem LA
                throw new ErroLexico(
                    "simbolo nao identificado",
                    linhaToken,
                    String.valueOf(c)
                );
        }
    }
}
