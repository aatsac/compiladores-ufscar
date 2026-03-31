package br.ufscar.dc.compiladores.lexico;

/**
 * Enumeração de todos os tipos de tokens reconhecidos pelo analisador léxico da linguagem LA.
 *
 * Os tokens são divididos em:
 *  - Palavras reservadas: identificadores com significado especial na linguagem
 *  - Símbolos especiais: operadores, pontuação e delimitadores
 *  - Tokens genéricos: IDENT (identificador), NUM_INT, NUM_REAL, CADEIA
 */
public enum TipoToken {

    // -----------------------------------------------------------------------
    // Palavras reservadas da linguagem LA
    // -----------------------------------------------------------------------
    ALGORITMO("algoritmo"),
    FIM_ALGORITMO("fim_algoritmo"),
    DECLARE("declare"),
    LEIA("leia"),
    ESCREVA("escreva"),
    ENQUANTO("enquanto"),
    FACA("faça"),
    FIM_ENQUANTO("fim_enquanto"),
    PARA("para"),
    ATE("até"),
    FIM_PARA("fim_para"),
    SE("se"),
    ENTAO("então"),
    FIM_SE("fim_se"),
    SENAO("senão"),
    CASO("caso"),
    SEA("seja"),
    FIM_CASO("fim_caso"),
    PROCEDIMENTO("procedimento"),
    FIM_PROCEDIMENTO("fim_procedimento"),
    FUNCAO("função"),
    FIM_FUNCAO("fim_função"),
    RETORNE("retorne"),
    TIPO("tipo"),
    VAR("var"),
    CONSTANTE("constante"),
    VERDADEIRO("verdadeiro"),
    FALSO("falso"),
    NAO("não"),
    E("e"),
    OU("ou"),
    // Tipos primitivos
    INTEIRO("inteiro"),
    REAL("real"),
    LITERAL("literal"),
    LOGICO("lógico"),
    // Comando de registro
    REGISTRO("registro"),
    FIM_REGISTRO("fim_registro"),

    // -----------------------------------------------------------------------
    // Símbolos especiais (operadores, pontuação, delimitadores)
    // -----------------------------------------------------------------------
    VIRGULA(","),
    PONTO_E_VIRGULA(";"),
    DOIS_PONTOS(":"),
    PONTO("."),
    ABRE_PAREN("("),
    FECHA_PAREN(")"),
    ABRE_COLCHETE("["),
    FECHA_COLCHETE("]"),
    ABRE_CHAVE("{"),
    FECHA_CHAVE("}"),
    ATRIBUICAO("<-"),
    MENOR("<"),
    MAIOR(">"),
    MENOR_IGUAL("<="),
    MAIOR_IGUAL(">="),
    DIFERENTE("<>"),
    IGUAL("="),
    MAIS("+"),
    MENOS("-"),
    ASTERISCO("*"),
    BARRA("/"),
    PORCENTAGEM("%"),
    CIRCUNFLEXO("^"),
    PONTO_PONTO(".."),
    AMPERSAND("&"),      // Operador de endereço (ponteiro)

    // -----------------------------------------------------------------------
    // Tokens genéricos (não têm lexema fixo)
    // -----------------------------------------------------------------------
    IDENT(null),      // Identificador de variável, procedimento, função, etc.
    NUM_INT(null),    // Número inteiro
    NUM_REAL(null),   // Número real (ponto flutuante)
    CADEIA(null);     // Cadeia de caracteres (string entre aspas duplas)

    /** Lexema fixo associado ao token (null para tokens genéricos). */
    public final String lexema;

    TipoToken(String lexema) {
        this.lexema = lexema;
    }
}
