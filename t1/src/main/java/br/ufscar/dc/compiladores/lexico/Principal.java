package br.ufscar.dc.compiladores.lexico;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Ponto de entrada principal do analisador léxico da linguagem LA.
 *
 * Uso em linha de comando:
 *   java -jar compilador.jar <arquivo-entrada> <arquivo-saida>
 *
 * O programa lê o código-fonte do arquivo de entrada, executa a análise léxica
 * e grava os tokens (ou mensagens de erro) no arquivo de saída.
 *
 * Formato da saída (tokens reconhecidos):
 *   <'lexema','TIPO'>        — para IDENT, NUM_INT, NUM_REAL, CADEIA
 *   <'lexema','lexema'>      — para palavras reservadas e símbolos
 *
 * Formato da saída (erro léxico):
 *   Linha N: simbolo - descricao do erro
 */
public class Principal {

    public static void main(String[] args) {

        // -------------------------------------------------------------------
        // Validação dos argumentos de linha de comando
        // -------------------------------------------------------------------
        if (args.length < 2) {
            System.err.println("Uso: java -jar compilador.jar <arquivo-entrada> <arquivo-saida>");
            System.exit(1);
        }

        String caminhoEntrada = args[0];
        String caminhoSaida   = args[1];

        // -------------------------------------------------------------------
        // Leitura do arquivo de entrada
        // -------------------------------------------------------------------
        String codigoFonte;
        try {
            // Lê todo o conteúdo do arquivo usando UTF-8
            byte[] bytes = Files.readAllBytes(Paths.get(caminhoEntrada));
            codigoFonte = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo de entrada: " + e.getMessage());
            System.exit(1);
            return; // necessário para o compilador entender que codigoFonte será inicializado
        }

        // -------------------------------------------------------------------
        // Análise léxica e escrita da saída
        // -------------------------------------------------------------------
        AnalisadorLexico analisador = new AnalisadorLexico(codigoFonte);

        try (PrintWriter escritor = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(caminhoSaida), StandardCharsets.UTF_8))) {

            Token token;
            // Lê tokens enquanto houver conteúdo no fonte
            while ((token = analisador.proximoToken()) != null) {
                escritor.println(token.toString());
            }

        } catch (ErroLexico e) {
            // Erro léxico: grava a mensagem formatada no arquivo de saída e encerra
            try (PrintWriter escritorErro = new PrintWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(caminhoSaida, true), StandardCharsets.UTF_8))) {
                escritorErro.println(e.getMensagemFormatada());
            } catch (IOException ioEx) {
                System.err.println("Erro ao gravar a saída: " + ioEx.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Erro ao gravar o arquivo de saída: " + e.getMessage());
            System.exit(1);
        }
    }
}
