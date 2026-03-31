package br.ufscar.dc.compiladores.lexico;

/**
 * Representa um token reconhecido pelo analisador léxico.
 *
 * Um token é composto por:
 *  - lexema: a sequência de caracteres original do código-fonte
 *  - tipo:   a categoria léxica a que pertence (ex: IDENT, NUM_INT, CADEIA, etc.)
 *  - linha:  número da linha onde o token foi encontrado (útil para mensagens de erro)
 */
public class Token {

    /** Sequência de caracteres original extraída do código-fonte. */
    private final String lexema;

    /** Categoria léxica do token. */
    private final TipoToken tipo;

    /** Linha do arquivo-fonte onde este token foi encontrado. */
    private final int linha;

    /**
     * Constrói um token com lexema, tipo e número de linha.
     *
     * @param lexema  texto original do token
     * @param tipo    categoria léxica
     * @param linha   número da linha no código-fonte
     */
    public Token(String lexema, TipoToken tipo, int linha) {
        this.lexema = lexema;
        this.tipo   = tipo;
        this.linha  = linha;
    }

    /** @return o lexema do token */
    public String getLexema() { return lexema; }

    /** @return o tipo do token */
    public TipoToken getTipo() { return tipo; }

    /** @return a linha onde o token foi encontrado */
    public int getLinha() { return linha; }

    /**
     * Retorna a representação textual do token no formato exigido pela especificação:
     *  <'lexema','TIPO'>  — para tokens genéricos (IDENT, NUM_INT, NUM_REAL, CADEIA)
     *  <'lexema','lexema'> — para palavras reservadas e símbolos (tipo == lexema)
     */
    /**
     * Retorna a representação textual do token no formato exigido pela especificação:
     *  <'lexema',TIPO>    — tokens genéricos (IDENT, NUM_INT, NUM_REAL, CADEIA): tipo SEM aspas
     *  <'lexema','lexema'> — palavras reservadas e símbolos: tipo COM aspas (igual ao lexema)
     */
    @Override
    public String toString() {
        // Tokens genéricos: tipo aparece SEM aspas simples
        if (tipo == TipoToken.IDENT
                || tipo == TipoToken.NUM_INT
                || tipo == TipoToken.NUM_REAL
                || tipo == TipoToken.CADEIA) {
            return "<'" + lexema + "'," + tipo.name() + ">";
        }
        // Palavras reservadas e símbolos: tipo aparece COM aspas simples (igual ao lexema)
        return "<'" + lexema + "','" + lexema + "'>";
    }
}
