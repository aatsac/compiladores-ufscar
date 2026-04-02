package br.ufscar.dc.compiladores.sintatico;

import org.antlr.v4.runtime.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Ponto de entrada do Analisador Sintático da Linguagem LA (T2).
 *
 * Uso em linha de comando:
 *   java -jar compilador-la.jar <arquivo-entrada> <arquivo-saida>
 *
 * Fluxo de execução:
 *  1. Lê o arquivo de entrada
 *  2. Cria o LALexer (ANTLR) com o listener léxico customizado
 *  3. Se houver erro léxico → escreve o erro + "Fim da compilacao" e encerra
 *  4. Cria o LAParser com o listener sintático customizado
 *  5. Executa o parse a partir da regra inicial "programa"
 *  6. Se houver erro sintático → o listener já escreveu a mensagem
 *  7. Escreve "Fim da compilacao" no arquivo de saída
 */
public class Principal {

    public static void main(String[] args) {

        // -------------------------------------------------------------------
        // Validação dos argumentos de linha de comando
        // -------------------------------------------------------------------
        if (args.length < 2) {
            System.err.println("Uso: java -jar compilador-la.jar <entrada> <saida>");
            System.exit(1);
        }

        String caminhoEntrada = args[0];
        String caminhoSaida   = args[1];

        // -------------------------------------------------------------------
        // Leitura do arquivo de entrada
        // -------------------------------------------------------------------
        String codigoFonte;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(caminhoEntrada));
            codigoFonte = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo de entrada: " + e.getMessage());
            System.exit(1);
            return;
        }

        // -------------------------------------------------------------------
        // Análise léxica e sintática com ANTLR
        // -------------------------------------------------------------------
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(caminhoSaida), StandardCharsets.UTF_8))) {

            // Cria o stream de caracteres a partir do código-fonte
            CharStream charStream = CharStreams.fromString(codigoFonte);

            // ----------------------------------------------------------------
            // FASE 1: Análise Léxica
            // ----------------------------------------------------------------
            LALexer lexer = new LALexer(charStream);

            // Remove o listener padrão (que imprime na stderr) e adiciona o customizado
            lexer.removeErrorListeners();
            LALexicalErrorListener lexicalErrorListener = new LALexicalErrorListener(writer);
            lexer.addErrorListener(lexicalErrorListener);

            // Tokeniza o código-fonte
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            // Se houve erro léxico, encerra com "Fim da compilacao"
            if (lexicalErrorListener.hasError()) {
                writer.println("Fim da compilacao");
                return;
            }

            // ----------------------------------------------------------------
            // FASE 2: Análise Sintática
            // ----------------------------------------------------------------
            LAParser parser = new LAParser(tokens);

            // Remove o listener padrão e adiciona o customizado
            parser.removeErrorListeners();
            LASyntaxErrorListener syntaxErrorListener = new LASyntaxErrorListener(writer);
            parser.addErrorListener(syntaxErrorListener);

            // Executa o parse a partir da regra inicial "programa"
            // O método gerado pelo ANTLR para a regra 'programa' é parser.programa()
            parser.programa();

            // Escreve o marcador de fim de compilação (sempre, com ou sem erros)
            writer.println("Fim da compilacao");

        } catch (IOException e) {
            System.err.println("Erro ao gravar o arquivo de saída: " + e.getMessage());
            System.exit(1);
        }
    }
}
