package br.ufscar.dc.compiladores.musica;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Ponto de entrada do Compilador MusicaLA — T6.
 *
 * Uso em linha de comando:
 *   java -jar compilador-musica.jar <arquivo-entrada> <arquivo-saida>
 *
 * Pipeline:
 *  1. Análise léxica  → erro: grava mensagem + "Fim da compilacao"
 *  2. Análise sintática → erro: grava mensagem + "Fim da compilacao"
 *  3. Análise semântica → erros: grava todos + "Fim da compilacao"
 *  4. Geração de código C → grava código no arquivo de saída
 *
 * Se não houver erros, a saída contém código C compilável com GCC.
 * O código C gerado usa sox ('play') para sintetizar as notas via onda senoidal.
 */
public class Principal {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java -jar compilador-musica.jar <entrada> <saida>");
            System.exit(1);
        }

        String caminhoEntrada = args[0];
        String caminhoSaida   = args[1];

        // Leitura do arquivo de entrada em UTF-8
        String fonte;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(caminhoEntrada));
            fonte = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo de entrada: " + e.getMessage());
            System.exit(1);
            return;
        }

        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(caminhoSaida), StandardCharsets.UTF_8))) {

            CharStream charStream = CharStreams.fromString(fonte);

            // ------------------------------------------------------------------
            // FASE 1: Análise Léxica
            // ------------------------------------------------------------------
            MusicaLexer lexer = new MusicaLexer(charStream);
            lexer.removeErrorListeners();
            MusicaLexicalErrorListener lexicalListener = new MusicaLexicalErrorListener(writer);
            lexer.addErrorListener(lexicalListener);

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            if (lexicalListener.hasError()) {
                writer.println("Fim da compilacao");
                return;
            }

            // ------------------------------------------------------------------
            // FASE 2: Análise Sintática
            // ------------------------------------------------------------------
            MusicaParser parser = new MusicaParser(tokens);
            parser.removeErrorListeners();
            MusicaSyntaxErrorListener syntaxListener = new MusicaSyntaxErrorListener(writer);
            parser.addErrorListener(syntaxListener);

            ParseTree tree = parser.programa();

            if (syntaxListener.hasError()) {
                writer.println("Fim da compilacao");
                return;
            }

            // ------------------------------------------------------------------
            // FASE 3: Análise Semântica
            // ------------------------------------------------------------------
            MusicaSemanticoVisitor semantico = new MusicaSemanticoVisitor(writer);
            semantico.visit(tree);

            if (semantico.hasError()) {
                writer.println("Fim da compilacao");
                return;
            }

            // ------------------------------------------------------------------
            // FASE 4: Geração de Código C
            // ------------------------------------------------------------------
            MusicaGeradorVisitor gerador = new MusicaGeradorVisitor(writer);
            gerador.visit(tree);

        } catch (IOException e) {
            System.err.println("Erro ao gravar saída: " + e.getMessage());
            System.exit(1);
        }
    }
}
