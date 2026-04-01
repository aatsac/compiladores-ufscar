package br.ufscar.dc.compiladores.semantico;

/**
 * Representa uma entrada na tabela de símbolos da linguagem LA.
 *
 * Cada símbolo possui:
 *  - nome:       o identificador (ex: "idade", "soma", "Pessoa")
 *  - categoria:  o que o símbolo representa (variável, constante, tipo, etc.)
 *  - tipo:       o tipo associado ao símbolo (inteiro, real, literal, etc.)
 */
public class EntradaTabelaDeSimbolos {

    /**
     * Categorias possíveis de um símbolo na linguagem LA.
     */
    public enum Categoria {
        VARIAVEL,
        CONSTANTE,
        TIPO,
        PROCEDIMENTO,
        FUNCAO
    }

    private final String nome;
    private final Categoria categoria;
    private final String tipo;

    /**
     * Constrói uma entrada da tabela de símbolos.
     *
     * @param nome      identificador do símbolo
     * @param categoria categoria do símbolo
     * @param tipo      tipo do símbolo (pode ser "inteiro", "real", nome de tipo, etc.)
     */
    public EntradaTabelaDeSimbolos(String nome, Categoria categoria, String tipo) {
        this.nome      = nome;
        this.categoria = categoria;
        this.tipo      = tipo;
    }

    public String getNome()          { return nome; }
    public Categoria getCategoria()  { return categoria; }
    public String getTipo()          { return tipo; }
}
