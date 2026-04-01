package br.ufscar.dc.compiladores.semantico;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Ponto de entrada do Compilador LA — Fase 3: Análise Semântica (T3).
 *
 * Uso em linha de comando:
 *   java -jar compilador-la.jar <arquivo-entrada> <arquivo-saida>
 *
 * Pipeline de execução:
 *  1. Lê o arquivo de entrada
 *  2. Análise léxica (LALexer gerado pelo ANTLR)
 *     → Erro léxico: grava mensagem + "Fim da compilacao" e encerra
 *  3. Análise sintática (LAParser gerado pelo ANTLR)
 *     → Erro sintático: grava mensagem + "Fim da compilacao" e encerra
 *  4. Análise semântica (LASemanticoVisitor)
 *     → Grava todos os erros encontrados (não interrompe ao primeiro)
 *  5. Grava "Fim da compilacao"
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
            System.err.println("Erro ao ler o arquivo de entrada: " + e.getMessage());
            System.exit(1);
            return;
        }

        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(caminhoSaida), StandardCharsets.UTF_8))) {

            CharStream charStream = CharStreams.fromString(codigoFonte);

            // FASE 1: Análise Léxica
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

            // FASE 2: Análise Sintática
            LAParser parser = new LAParser(tokens);
            parser.removeErrorListeners();
            LASyntaxErrorListener syntaxListener = new LASyntaxErrorListener(writer);
            parser.addErrorListener(syntaxListener);

            ParseTree tree = parser.programa();

            if (syntaxListener.hasError()) {
                writer.println("Fim da compilacao");
                return;
            }

            // FASE 3: Análise Semântica
            // Não interrompe ao primeiro erro — reporta todos os erros encontrados
            LASemanticoVisitor semantico = new LASemanticoVisitor(writer);
            semantico.visit(tree);

            writer.println("Fim da compilacao");

        } catch (IOException e) {
            System.err.println("Erro ao gravar o arquivo de saída: " + e.getMessage());
            System.exit(1);
        }
    }
}
