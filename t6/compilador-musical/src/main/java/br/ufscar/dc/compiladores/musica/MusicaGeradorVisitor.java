package br.ufscar.dc.compiladores.musica;

import br.ufscar.dc.compiladores.musica.MusicaParser.*;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gerador de código C para a linguagem MusicaLA.
 *
 * Converte a descrição musical em código C que toca as notas via
 * síntese senoidal usando o comando 'play' do pacote sox:
 *   play -n synth DURACAO sine FREQUENCIA
 *
 * Cada nota é mapeada para sua frequência em Hz (afinação igual temperada,
 * LA4 = 440 Hz). As durações são calculadas a partir do BPM declarado.
 *
 * O código C gerado usa stdio.h e stdlib.h, é compilável com GCC e
 * requer sox instalado no sistema (sudo apt install sox).
 */
public class MusicaGeradorVisitor extends MusicaBaseVisitor<Void> {

    private final PrintWriter out;

    // -----------------------------------------------------------------------
    // Tabela de frequências: nome da nota → frequência em Hz
    // Baseada em afinação igual temperada, LA4 = 440 Hz
    // -----------------------------------------------------------------------
    private static final Map<String, Double> FREQUENCIAS = new LinkedHashMap<>();

    static {
        // Oitava 1
        FREQUENCIAS.put("DO1",   32.70); FREQUENCIAS.put("DO#1",  34.65);
        FREQUENCIAS.put("REb1",  34.65); FREQUENCIAS.put("RE1",   36.71);
        FREQUENCIAS.put("RE#1",  38.89); FREQUENCIAS.put("MIb1",  38.89);
        FREQUENCIAS.put("MI1",   41.20); FREQUENCIAS.put("FA1",   43.65);
        FREQUENCIAS.put("FA#1",  46.25); FREQUENCIAS.put("SOLb1", 46.25);
        FREQUENCIAS.put("SOL1",  49.00); FREQUENCIAS.put("SOL#1", 51.91);
        FREQUENCIAS.put("LAb1",  51.91); FREQUENCIAS.put("LA1",   55.00);
        FREQUENCIAS.put("LA#1",  58.27); FREQUENCIAS.put("SIb1",  58.27);
        FREQUENCIAS.put("SI1",   61.74);
        // Oitava 2
        FREQUENCIAS.put("DO2",   65.41); FREQUENCIAS.put("DO#2",  69.30);
        FREQUENCIAS.put("REb2",  69.30); FREQUENCIAS.put("RE2",   73.42);
        FREQUENCIAS.put("RE#2",  77.78); FREQUENCIAS.put("MIb2",  77.78);
        FREQUENCIAS.put("MI2",   82.41); FREQUENCIAS.put("FA2",   87.31);
        FREQUENCIAS.put("FA#2",  92.50); FREQUENCIAS.put("SOLb2", 92.50);
        FREQUENCIAS.put("SOL2",  98.00); FREQUENCIAS.put("SOL#2",103.83);
        FREQUENCIAS.put("LAb2", 103.83); FREQUENCIAS.put("LA2",  110.00);
        FREQUENCIAS.put("LA#2", 116.54); FREQUENCIAS.put("SIb2", 116.54);
        FREQUENCIAS.put("SI2",  123.47);
        // Oitava 3
        FREQUENCIAS.put("DO3",  130.81); FREQUENCIAS.put("DO#3", 138.59);
        FREQUENCIAS.put("REb3", 138.59); FREQUENCIAS.put("RE3",  146.83);
        FREQUENCIAS.put("RE#3", 155.56); FREQUENCIAS.put("MIb3", 155.56);
        FREQUENCIAS.put("MI3",  164.81); FREQUENCIAS.put("FA3",  174.61);
        FREQUENCIAS.put("FA#3", 185.00); FREQUENCIAS.put("SOLb3",185.00);
        FREQUENCIAS.put("SOL3", 196.00); FREQUENCIAS.put("SOL#3",207.65);
        FREQUENCIAS.put("LAb3", 207.65); FREQUENCIAS.put("LA3",  220.00);
        FREQUENCIAS.put("LA#3", 233.08); FREQUENCIAS.put("SIb3", 233.08);
        FREQUENCIAS.put("SI3",  246.94);
        // Oitava 4 (oitava central)
        FREQUENCIAS.put("DO4",  261.63); FREQUENCIAS.put("DO#4", 277.18);
        FREQUENCIAS.put("REb4", 277.18); FREQUENCIAS.put("RE4",  293.66);
        FREQUENCIAS.put("RE#4", 311.13); FREQUENCIAS.put("MIb4", 311.13);
        FREQUENCIAS.put("MI4",  329.63); FREQUENCIAS.put("FA4",  349.23);
        FREQUENCIAS.put("FA#4", 369.99); FREQUENCIAS.put("SOLb4",369.99);
        FREQUENCIAS.put("SOL4", 392.00); FREQUENCIAS.put("SOL#4",415.30);
        FREQUENCIAS.put("LAb4", 415.30); FREQUENCIAS.put("LA4",  440.00);
        FREQUENCIAS.put("LA#4", 466.16); FREQUENCIAS.put("SIb4", 466.16);
        FREQUENCIAS.put("SI4",  493.88);
        // Oitava 5
        FREQUENCIAS.put("DO5",  523.25); FREQUENCIAS.put("DO#5", 554.37);
        FREQUENCIAS.put("REb5", 554.37); FREQUENCIAS.put("RE5",  587.33);
        FREQUENCIAS.put("RE#5", 622.25); FREQUENCIAS.put("MIb5", 622.25);
        FREQUENCIAS.put("MI5",  659.25); FREQUENCIAS.put("FA5",  698.46);
        FREQUENCIAS.put("FA#5", 739.99); FREQUENCIAS.put("SOLb5",739.99);
        FREQUENCIAS.put("SOL5", 783.99); FREQUENCIAS.put("SOL#5",830.61);
        FREQUENCIAS.put("LAb5", 830.61); FREQUENCIAS.put("LA5",  880.00);
        FREQUENCIAS.put("LA#5", 932.33); FREQUENCIAS.put("SIb5", 932.33);
        FREQUENCIAS.put("SI5",  987.77);
        // Oitava 6
        FREQUENCIAS.put("DO6", 1046.50); FREQUENCIAS.put("RE6", 1174.66);
        FREQUENCIAS.put("MI6", 1318.51); FREQUENCIAS.put("FA6", 1396.91);
        FREQUENCIAS.put("SOL6",1567.98); FREQUENCIAS.put("LA6", 1760.00);
        FREQUENCIAS.put("SI6", 1975.53);
    }

    public MusicaGeradorVisitor(PrintWriter out) {
        this.out = out;
    }

    // -----------------------------------------------------------------------
    // Visita do programa completo
    // -----------------------------------------------------------------------

    @Override
    public Void visitPrograma(ProgramaContext ctx) {
        // Cabeçalho do arquivo C gerado
        out.println("#include <stdio.h>");
        out.println("#include <stdlib.h>");
        out.println("#include <string.h>");
        out.println();
        // Função auxiliar para tocar uma nota via sox (play) do sistema
        out.println("/* Toca uma nota usando sox (play) do sistema (Linux). */");
        out.println("/* Instale com: sudo apt install sox */");
        out.println("void tocar(int freq, int dur_ms) {");
        out.println("    char cmd[256];");
        out.println("    double dur_s = dur_ms / 1000.0;");
        out.println("    if (freq == 0) {");
        out.println("        /* Pausa: aguarda sem som */");
        out.println("        sprintf(cmd, \"sleep %0.3f\", dur_s);");
        out.println("    } else {");
        out.println("        sprintf(cmd, \"play -n synth %0.3f sine %d > /dev/null 2>&1\", dur_s, freq);");
        out.println("    }");
        out.println("    system(cmd);");
        out.println("}");
        out.println();
        out.println("int main() {");

        // Visita cada bloco de música
        for (Bloco_musicaContext bloco : ctx.bloco_musica()) {
            visitBloco_musica(bloco);
        }

        out.println("    return 0;");
        out.println("}");
        return null;
    }

    // -----------------------------------------------------------------------
    // Visita de bloco de música
    // -----------------------------------------------------------------------

    @Override
    public Void visitBloco_musica(Bloco_musicaContext ctx) {
        // Extrai configurações do bloco
        int bpm         = 120;  // padrão
        int repeticoes  = 1;
        String titulo   = ctx.CADEIA().getText();

        for (ConfiguracaoContext cfg : ctx.configuracao()) {
            if (cfg.cmd_bpm() != null) {
                bpm = Integer.parseInt(cfg.cmd_bpm().NUM_INT().getText());
            }
            if (cfg.cmd_repeticoes() != null) {
                repeticoes = Integer.parseInt(cfg.cmd_repeticoes().NUM_INT().getText());
            }
        }

        // Duração do tempo (quarto de compasso) em milissegundos
        // 1 batida = 60000 / BPM ms; um "quarto" = 1 batida
        int durQuarto = 60000 / bpm;

        // Comentário indicando o início do bloco
        out.println("    /* Musica: " + titulo + " | BPM: " + bpm
                + " | Repeticoes: " + repeticoes + " */");

        // Loop de repetições
        if (repeticoes > 1) {
            out.println("    for (int _rep = 0; _rep < " + repeticoes + "; _rep++) {");
        }

        // Gera os comandos do bloco
        for (ComandoContext cmd : ctx.comando()) {
            gerarComando(cmd, durQuarto);
        }

        if (repeticoes > 1) {
            out.println("    }");
        }
        out.println();
        return null;
    }

    // -----------------------------------------------------------------------
    // Geração de comandos
    // -----------------------------------------------------------------------

    private void gerarComando(ComandoContext ctx, int durQuarto) {
        if (ctx.cmd_nota() != null) {
            gerarNota(ctx.cmd_nota(), durQuarto);
        } else if (ctx.cmd_pausa() != null) {
            gerarPausa(ctx.cmd_pausa(), durQuarto);
        } else if (ctx.cmd_sequencia() != null) {
            out.println("    /* Sequencia: "
                    + ctx.cmd_sequencia().CADEIA().getText() + " */");
            for (ComandoContext inner : ctx.cmd_sequencia().comando()) {
                gerarComando(inner, durQuarto);
            }
        }
    }

    /**
     * Gera chamada tocar(frequencia, duracao_ms) para uma nota.
     * Exemplo: nota DO4 quarto → tocar(262, 500);
     */
    private void gerarNota(Cmd_notaContext ctx, int durQuarto) {
        String nomeLa   = ctx.NOME_NOTA().getText();
        int    durMs    = calcularDuracao(ctx.duracao(), durQuarto);
        int    freq     = (int) Math.round(obterFrequencia(nomeLa));
        out.println("    tocar(" + freq + ", " + durMs + "); /* " + nomeLa + " */");
    }

    /**
     * Gera chamada tocar(0, duracao_ms) para uma pausa (frequência 0 = silêncio).
     */
    private void gerarPausa(Cmd_pausaContext ctx, int durQuarto) {
        int durMs = calcularDuracao(ctx.duracao(), durQuarto);
        out.println("    tocar(0, " + durMs + "); /* pausa */");
    }

    // -----------------------------------------------------------------------
    // Utilitários
    // -----------------------------------------------------------------------

    /**
     * Calcula a duração em milissegundos a partir do valor de duração
     * e do tempo de um quarto de compasso.
     *
     * inteiro  = 4 × quarto
     * meio     = 2 × quarto
     * quarto   = 1 × quarto
     * colcheia = 0.5 × quarto
     * semi     = 0.25 × quarto
     */
    private int calcularDuracao(DuracaoContext ctx, int durQuarto) {
        String dur = ctx.getStart().getText();
        switch (dur) {
            case "inteiro":  return durQuarto * 4;
            case "meio":     return durQuarto * 2;
            case "quarto":   return durQuarto;
            case "colcheia": return durQuarto / 2;
            case "semi":     return durQuarto / 4;
            default:         return durQuarto;
        }
    }

    /**
     * Obtém a frequência em Hz para uma nota musical.
     * Retorna 440.0 (LA4) como fallback se a nota não for encontrada.
     */
    private double obterFrequencia(String nota) {
        return FREQUENCIAS.getOrDefault(nota, 440.0);
    }
}
