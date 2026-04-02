package br.ufscar.dc.compiladores.semantico;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Ponto de entrada do Compilador LA — T5: Gerador de Código C.
 *
 * Uso em linha de comando:
 *   java -jar compilador-la.jar <arquivo-entrada> <arquivo-saida>
 *
 * Pipeline:
 *  1. Análise léxica  → erro léxico:  grava erro + "Fim da compilacao"
 *  2. Análise sintática → erro sint.: grava erro + "Fim da compilacao"
 *  3. Análise semântica → erros sem.: grava todos + "Fim da compilacao"
 *  4. Geração de código → grava código C no arquivo de saída
 *
 * Se houver qualquer erro nas fases 1-3, a geração de código NÃO ocorre.
 * Se não houver erros, a saída contém apenas o código C gerado (sem "Fim da compilacao").
 */
public class Principal {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("Uso: java -jar compilador-la.jar <entrada> <saida>");
            System.exit(1);
        }

        String caminhoEntrada = args[0];
        String caminhoSaida   = args[1];

        String codigoFonte;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(caminhoEntrada));
            codigoFonte = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo de entrada: " + e.getMessage());
            System.exit(1);
            return;
        }

        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(caminhoSaida), StandardCharsets.UTF_8))) {

            CharStream charStream = CharStreams.fromString(codigoFonte);

            // ------------------------------------------------------------------
            // FASE 1: Análise Léxica
            // ------------------------------------------------------------------
            LALexer lexer = new LALexer(charStream);
            lexer.removeErrorListeners();
            LALexicalErrorListener lexicalListener = new LALexicalErrorListener(writer);
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
            LAParser parser = new LAParser(tokens);
            parser.removeErrorListeners();
            LASyntaxErrorListener syntaxListener = new LASyntaxErrorListener(writer);
            parser.addErrorListener(syntaxListener);

            ParseTree tree = parser.programa();

            if (syntaxListener.hasError()) {
                writer.println("Fim da compilacao");
                return;
            }

            // ------------------------------------------------------------------
            // FASE 3: Análise Semântica
            // ------------------------------------------------------------------
            LASemanticoVisitor semantico = new LASemanticoVisitor(writer);
            semantico.visit(tree);

            if (semantico.hasError()) {
                writer.println("Fim da compilacao");
                return;
            }

            // ------------------------------------------------------------------
            // FASE 4: Geração de Código C
            // Só chegamos aqui se não houve nenhum erro nas fases anteriores.
            // ------------------------------------------------------------------
            LAGeradorVisitor gerador = new LAGeradorVisitor(writer);
            gerador.visit(tree);

        } catch (IOException e) {
            System.err.println("Erro ao gravar saída: " + e.getMessage());
            System.exit(1);
        }
    }
}
