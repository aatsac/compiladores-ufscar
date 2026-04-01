package br.ufscar.dc.compiladores.semantico;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma entrada na tabela de símbolos da linguagem LA.
 *
 * Cada símbolo possui:
 *  - nome:       o identificador (ex: "idade", "soma", "Pessoa")
 *  - categoria:  o que o símbolo representa (variável, constante, tipo, etc.)
 *  - tipo:       o tipo associado ao símbolo (inteiro, real, literal, etc.)
 *  - parametros: lista de tipos dos parâmetros (apenas para PROCEDIMENTO e FUNCAO)
 *                usada para verificar compatibilidade em chamadas (T4)
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
     * Lista de tipos dos parâmetros formais do subprograma.
     * Vazia para variáveis, constantes e tipos.
     * Para subprogramas, contém os tipos na ordem de declaração.
     */
    private final List<String> parametros;

    /**
     * Constrói uma entrada de símbolo simples (variável, constante, tipo).
     */
    public EntradaTabelaDeSimbolos(String nome, Categoria categoria, String tipo) {
        this.nome       = nome;
        this.categoria  = categoria;
        this.tipo       = tipo;
        this.parametros = new ArrayList<>();
    }

    /**
     * Constrói uma entrada de subprograma (procedimento ou função)
     * com lista de tipos dos parâmetros formais.
     *
     * @param nome       nome do subprograma
     * @param categoria  PROCEDIMENTO ou FUNCAO
     * @param tipo       tipo de retorno (para função) ou "procedimento"
     * @param parametros lista dos tipos de cada parâmetro formal, na ordem
     */
    public EntradaTabelaDeSimbolos(String nome, Categoria categoria, String tipo,
                                   List<String> parametros) {
        this.nome       = nome;
        this.categoria  = categoria;
        this.tipo       = tipo;
        this.parametros = new ArrayList<>(parametros);
    }

    public String getNome()              { return nome; }
    public Categoria getCategoria()      { return categoria; }
    public String getTipo()              { return tipo; }
    public List<String> getParametros()  { return parametros; }
}
