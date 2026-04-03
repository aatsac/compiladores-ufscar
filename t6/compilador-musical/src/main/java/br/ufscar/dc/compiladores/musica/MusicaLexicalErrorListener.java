package br.ufscar.dc.compiladores.musica;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import java.io.PrintWriter;

/**
 * Captura erros léxicos do MusicaLexer e os formata para o arquivo de saída.
 *
 * Formato: "Linha N: simbolo X nao reconhecido"
 */
public class MusicaLexicalErrorListener extends BaseErrorListener {

    private final PrintWriter writer;
    private boolean temErro = false;

    public MusicaLexicalErrorListener(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        temErro = true;
        // Extrai o símbolo da mensagem do ANTLR: "token recognition error at: 'X'"
        String simbolo = extrairSimbolo(msg);
        writer.println("Linha " + line + ": simbolo " + simbolo + " nao reconhecido");
    }

    private String extrairSimbolo(String msg) {
        int inicio = msg.indexOf('\'');
        int fim    = msg.lastIndexOf('\'');
        if (inicio >= 0 && fim > inicio) return msg.substring(inicio + 1, fim);
        return msg;
    }

    public boolean hasError() { return temErro; }
}
