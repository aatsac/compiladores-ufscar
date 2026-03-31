package br.ufscar.dc.compiladores.lexico;

/**
 * Exceção lançada quando o analisador léxico encontra um erro no código-fonte.
 *
 * Tipos de erros léxicos tratados:
 *  - Símbolo não reconhecido (caractere inválido)
 *  - Comentário não fechado na mesma linha (comentário sem '}')
 *  - Cadeia não fechada na mesma linha (string sem '"' de fechamento)
 */
public class ErroLexico extends Exception {

    /** Linha do arquivo-fonte onde o erro ocorreu. */
    private final int linha;

    /** Símbolo ou trecho que causou o erro. */
    private final String simbolo;

    /**
     * Constrói um erro léxico com a mensagem, linha e símbolo problemático.
     *
     * @param mensagem descrição do tipo de erro
     * @param linha    número da linha onde o erro ocorreu
     * @param simbolo  símbolo ou trecho causador do erro
     */
    public ErroLexico(String mensagem, int linha, String simbolo) {
        super(mensagem);
        this.linha   = linha;
        this.simbolo = simbolo;
    }

    /** @return número da linha onde o erro ocorreu */
    public int getLinha() { return linha; }

    /** @return símbolo ou trecho que causou o erro */
    public String getSimbolo() { return simbolo; }

    /**
     * Retorna a mensagem de erro no formato exigido pela especificação.
     *
     * Símbolo não identificado: "Linha N: X - simbolo nao identificado"
     * Demais erros (cadeia, comentário): "Linha N: descricao do erro"
     */
    public String getMensagemFormatada() {
        if (getMessage().equals("simbolo nao identificado")) {
            return "Linha " + linha + ": " + simbolo + " - " + getMessage();
        }
        return "Linha " + linha + ": " + getMessage();
    }
}
