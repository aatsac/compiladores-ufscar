package br.ufscar.dc.compiladores.musica;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import java.io.PrintWriter;

/**
 * Captura erros sintáticos do MusicaParser e os formata para o arquivo de saída.
 *
 * Formato: "Linha N: erro sintatico proximo a LEXEMA"
 *
 * Apenas o primeiro erro sintático é reportado, pois erros em cascata
 * geralmente são consequência do primeiro.
 */
public class MusicaSyntaxErrorListener extends BaseErrorListener {

    private final PrintWriter writer;
    private boolean temErro = false;

    public MusicaSyntaxErrorListener(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        if (temErro) return; // reporta apenas o primeiro
        temErro = true;

        String lexema = "EOF";
        if (offendingSymbol instanceof Token) {
            Token t = (Token) offendingSymbol;
            lexema = t.getType() == Token.EOF ? "EOF" : t.getText();
        }
        writer.println("Linha " + line + ": erro sintatico proximo a " + lexema);
    }

    public boolean hasError() { return temErro; }
}
