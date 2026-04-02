package br.ufscar.dc.compiladores.semantico;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.io.PrintWriter;

/**
 * Listener de erros léxicos para o compilador LA.
 *
 * Intercepta os erros gerados pelo LALexer (ANTLR) e os formata
 * conforme a especificação do trabalho.
 *
 * Formatos de saída:
 *   Símbolo inválido:        "Linha N: X - simbolo nao identificado"
 *   Cadeia não fechada:      "Linha N: cadeia literal nao fechada"
 *   Comentário não fechado:  "Linha N: comentario nao fechado"
 *
 * O ANTLR não distingue nativamente cadeia/comentário não fechados via
 * BaseErrorListener; esses casos são capturados por tokens de erro
 * definidos na gramática ou via verificação da mensagem.
 */
public class LALexicalErrorListener extends BaseErrorListener {

    private final PrintWriter writer;

    /** true se pelo menos um erro léxico foi detectado. */
    private boolean temErro = false;

    public LALexicalErrorListener(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e) {

        temErro = true;

        // Extrai o símbolo problemático da mensagem padrão do ANTLR:
        // "token recognition error at: 'X'"
        String simbolo = extrairSimbolo(msg);

        // Verifica se é cadeia não fechada (começa com aspas mas não fecha na linha)
        if (simbolo.startsWith("\"")) {
            writer.println("Linha " + line + ": cadeia literal nao fechada");

        // Verifica se é comentário não fechado (começa com '{' mas não fecha na linha)
        } else if (simbolo.startsWith("{")) {
            writer.println("Linha " + line + ": comentario nao fechado");

        } else {
            // Símbolo não reconhecido
            writer.println("Linha " + line + ": " + simbolo + " - simbolo nao identificado");
        }
    }

    /**
     * Extrai o símbolo da mensagem padrão do ANTLR.
     * Formato típico: "token recognition error at: 'X'"
     *
     * @param msg mensagem gerada pelo ANTLR
     * @return o símbolo problemático
     */
    private String extrairSimbolo(String msg) {
        int inicio = msg.indexOf('\'');
        int fim    = msg.lastIndexOf('\'');
        if (inicio >= 0 && fim > inicio) {
            return msg.substring(inicio + 1, fim);
        }
        return msg;
    }

    public boolean hasError() {
        return temErro;
    }
}
