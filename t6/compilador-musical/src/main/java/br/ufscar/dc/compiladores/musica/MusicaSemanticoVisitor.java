package br.ufscar.dc.compiladores.musica;

import br.ufscar.dc.compiladores.musica.MusicaParser.*;
import org.antlr.v4.runtime.Token;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * Analisador semântico da linguagem MusicaLA.
 *
 * Realiza quatro verificações além da gramática:
 *
 *  1. BPM fora do intervalo válido (deve ser 20–300 BPM).
 *     BPM muito baixo ou muito alto não faz sentido musical.
 *
 *  2. Número de repetições inválido (deve ser >= 1).
 *     Repetir 0 vezes é sem sentido.
 *
 *  3. Configuração duplicada no mesmo bloco de música.
 *     Declarar 'bpm' ou 'instrumento' duas vezes no mesmo bloco é ambíguo.
 *
 *  4. Bloco de música sem nenhum comando (nota ou pausa).
 *     Uma música sem notas é inválida.
 *
 * Mensagens de erro:
 *   "Linha N: bpm X fora do intervalo valido (20-300)"
 *   "Linha N: repeticoes X invalido (deve ser >= 1)"
 *   "Linha N: configuracao X ja declarada nesta musica"
 *   "Linha N: musica X nao contem notas ou pausas"
 *
 * Não interrompe ao primeiro erro — reporta todos.
 */
public class MusicaSemanticoVisitor extends MusicaBaseVisitor<Void> {

    private final PrintWriter writer;
    private boolean temErro = false;

    public MusicaSemanticoVisitor(PrintWriter writer) {
        this.writer = writer;
    }

    public boolean hasError() { return temErro; }

    // -----------------------------------------------------------------------
    // Verificação de bloco de música
    // -----------------------------------------------------------------------

    @Override
    public Void visitBloco_musica(Bloco_musicaContext ctx) {
        // Conjunto de configurações já declaradas neste bloco
        Set<String> configsDeclaradas = new HashSet<>();

        // Verifica cada configuração
        for (ConfiguracaoContext cfg : ctx.configuracao()) {

            if (cfg.cmd_bpm() != null) {
                // Verificação 3: BPM duplicado
                if (configsDeclaradas.contains("bpm")) {
                    Token t = cfg.cmd_bpm().getStart();
                    erroConfigDuplicada(t, "bpm");
                } else {
                    configsDeclaradas.add("bpm");
                }

                // Verificação 1: BPM no intervalo 20–300
                int bpm = Integer.parseInt(cfg.cmd_bpm().NUM_INT().getText());
                if (bpm < 20 || bpm > 300) {
                    Token t = cfg.cmd_bpm().NUM_INT().getSymbol();
                    erroBpmInvalido(t, bpm);
                }
            }

            if (cfg.cmd_instrumento() != null) {
                // Verificação 3: instrumento duplicado
                if (configsDeclaradas.contains("instrumento")) {
                    Token t = cfg.cmd_instrumento().getStart();
                    erroConfigDuplicada(t, "instrumento");
                } else {
                    configsDeclaradas.add("instrumento");
                }
            }

            if (cfg.cmd_repeticoes() != null) {
                // Verificação 3: repeticoes duplicado
                if (configsDeclaradas.contains("repeticoes")) {
                    Token t = cfg.cmd_repeticoes().getStart();
                    erroConfigDuplicada(t, "repeticoes");
                } else {
                    configsDeclaradas.add("repeticoes");
                }

                // Verificação 2: repetições >= 1
                int rep = Integer.parseInt(cfg.cmd_repeticoes().NUM_INT().getText());
                if (rep < 1) {
                    Token t = cfg.cmd_repeticoes().NUM_INT().getSymbol();
                    erroRepeticoesInvalido(t, rep);
                }
            }
        }

        // Verificação 4: bloco deve ter pelo menos um comando
        if (!temComandos(ctx)) {
            Token t = ctx.CADEIA().getSymbol();
            String titulo = t.getText();
            erroMusicaSemNotas(t, titulo);
        }

        // Continua visitando filhos (sequências aninhadas)
        return visitChildren(ctx);
    }

    // -----------------------------------------------------------------------
    // Utilitários
    // -----------------------------------------------------------------------

    /**
     * Verifica recursivamente se um bloco contém pelo menos um cmd_nota ou cmd_pausa.
     */
    private boolean temComandos(Bloco_musicaContext ctx) {
        for (ComandoContext cmd : ctx.comando()) {
            if (cmd.cmd_nota() != null || cmd.cmd_pausa() != null) return true;
            if (cmd.cmd_sequencia() != null) {
                for (ComandoContext inner : cmd.cmd_sequencia().comando()) {
                    if (inner.cmd_nota() != null || inner.cmd_pausa() != null) return true;
                }
            }
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Emissão de erros
    // -----------------------------------------------------------------------

    private void erroBpmInvalido(Token t, int bpm) {
        temErro = true;
        writer.println("Linha " + t.getLine() + ": bpm " + bpm
                + " fora do intervalo valido (20-300)");
    }

    private void erroRepeticoesInvalido(Token t, int rep) {
        temErro = true;
        writer.println("Linha " + t.getLine() + ": repeticoes " + rep
                + " invalido (deve ser >= 1)");
    }

    private void erroConfigDuplicada(Token t, String config) {
        temErro = true;
        writer.println("Linha " + t.getLine() + ": configuracao " + config
                + " ja declarada nesta musica");
    }

    private void erroMusicaSemNotas(Token t, String titulo) {
        temErro = true;
        writer.println("Linha " + t.getLine() + ": musica " + titulo
                + " nao contem notas ou pausas");
    }
}
