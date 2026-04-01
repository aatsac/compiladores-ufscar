package br.ufscar.dc.compiladores.semantico;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import java.io.PrintWriter;

/**
 * Listener de erros sintáticos para o compilador LA.
 *
 * Intercepta os erros gerados pelo LAParser (ANTLR) e os formata
 * conforme a especificação do trabalho:
 *
 *   Linha N: erro sintatico proximo a LEXEMA
 *
 * Apenas o PRIMEIRO erro sintático é reportado; após isso, a compilação
 * deve encerrar com "Fim da compilacao".
 */
public class LASyntaxErrorListener extends BaseErrorListener {

    private final PrintWriter writer;

    /** true se pelo menos um erro sintático foi detectado. */
    private boolean temErro = false;

    public LASyntaxErrorListener(PrintWriter writer) {
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

        // Só reporta o primeiro erro sintático
        if (temErro) return;
        temErro = true;

        // Obtém o lexema do token que causou o erro
        String lexema;
        if (offendingSymbol instanceof Token) {
            Token token = (Token) offendingSymbol;
            if (token.getType() == Token.EOF) {
                lexema = "EOF";
            } else {
                lexema = token.getText();
            }
        } else {
            lexema = msg;
        }

        writer.println("Linha " + line + ": erro sintatico proximo a " + lexema);
    }

    public boolean hasError() {
        return temErro;
    }
}
