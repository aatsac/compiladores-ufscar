package br.ufscar.dc.compiladores.semantico;

import br.ufscar.dc.compiladores.semantico.LAParser.*;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.Token;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Visitor gerador de código C para a linguagem LA — T5.
 *
 * Percorre a árvore sintática e produz código C equivalente ao programa LA.
 *
 * Mapeamento principal LA → C:
 *  - inteiro  → int
 *  - real     → float
 *  - literal  → char[80]
 *  - logico   → int  (0=falso, 1=verdadeiro)
 *  - ^tipo    → tipo*
 *  - registro → struct { ... }
 *  - tipo reg → typedef struct { ... } nome
 *  - vetor[n] → tipo nome[n]
 *  - constante → #define
 *  - leia     → scanf / gets
 *  - escreva  → printf
 *  - se/entao/senao → if/else
 *  - enquanto → while
 *  - para     → for
 *  - faca/ate → do/while
 *  - caso     → switch/case
 *  - procedimento → void func(params)
 *  - funcao   → tipo func(params)
 *  - retorne  → return
 *  - e/ou/nao → &&/||/!
 *  - =/<>     → ==/!=
 *  - <- (atrib) → =
 *  - &ident   → &ident
 *  - ^ident   → *ident
 */
public class LAGeradorVisitor extends LABaseVisitor<String> {

    /** Writer para o código C gerado. */
    private final PrintWriter out;

    /** Nível de indentação atual (1 tab por nível). */
    private int nivel = 1;

    /**
     * Tabela de símbolos usada para consultar tipos durante a geração.
     * Necessária para determinar o formato printf/scanf correto.
     */
    private final TabelaDeSimbolos tabela;

    public LAGeradorVisitor(PrintWriter out) {
        this.out   = out;
        this.tabela = new TabelaDeSimbolos();
    }

    // -----------------------------------------------------------------------
    // Utilitários de indentação e emissão
    // -----------------------------------------------------------------------

    private String indent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nivel; i++) sb.append("\t");
        return sb.toString();
    }

    private void emit(String linha) {
        out.println(indent() + linha);
    }

    private void emitRaw(String s) {
        out.print(s);
    }

    // -----------------------------------------------------------------------
    // Mapeamento de tipos LA → C
    // -----------------------------------------------------------------------

    /**
     * Converte um tipo LA para a declaração C correspondente.
     *
     * @param tipoLA  string do tipo LA (inteiro, real, literal, logico, ^tipo, nome_tipo)
     * @param nome    nome da variável (necessário para arrays: "char nome[80]")
     * @return declaração C completa (ex: "int x", "char x[80]", "float* p")
     */
    private String tipoParaC(String tipoLA, String nome) {
        switch (tipoLA) {
            case "inteiro": return "int " + nome;
            case "real":    return "float " + nome;
            case "literal": return "char " + nome + "[80]";
            case "logico":  return "int " + nome;
            default:
                if (tipoLA.startsWith("^")) {
                    // Ponteiro: ^inteiro → int* nome
                    String base = tipoLA.substring(1);
                    return tipoCBase(base) + "* " + nome;
                }
                // Tipo definido pelo usuário (typedef)
                return tipoLA + " " + nome;
        }
    }

    /** Retorna apenas o tipo C base sem o nome da variável. */
    private String tipoCBase(String tipoLA) {
        switch (tipoLA) {
            case "inteiro": return "int";
            case "real":    return "float";
            case "literal": return "char*";
            case "logico":  return "int";
            default:        return tipoLA;
        }
    }

    /**
     * Retorna o especificador de formato printf/scanf para um tipo LA.
     */
    private String formatoTipo(String tipoLA) {
        switch (tipoLA) {
            case "inteiro": return "%d";
            case "real":    return "%f";
            case "literal": return "%s";
            case "logico":  return "%d";
            default:        return "%s";
        }
    }

    /**
     * Infere o tipo de uma expressão para determinar o formato correto.
     * Percorre a expressão e retorna "inteiro", "real", "literal" ou "logico".
     */
    /**
     * Infere o tipo de uma expressão para escolher o formato printf correto.
     *
     * Regras de inferência (percorre a árvore buscando o tipo dominante):
     *  - NUM_REAL → real
     *  - CADEIA   → literal
     *  - verdadeiro/falso → logico
     *  - operador relacional (=, <>, >, <, >=, <=) → logico
     *  - identificador → consulta tabela de símbolos
     *  - real em qualquer operando → real prevalece
     *  - literal em qualquer operando → literal prevalece
     *  - default → inteiro
     */
    private String inferirTipoExpressao(LAParser.ExpressaoContext ctx) {
        // Operador lógico 'ou' → logico
        if (ctx.termo_logico().size() > 1) return "logico";
        return inferirTipoTermoLogico(ctx.termo_logico(0));
    }

    private String inferirTipoTermoLogico(LAParser.Termo_logicoContext ctx) {
        // Operador lógico 'e' → logico
        if (ctx.fator_logico().size() > 1) return "logico";
        return inferirTipoFatorLogico(ctx.fator_logico(0));
    }

    private String inferirTipoFatorLogico(LAParser.Fator_logicoContext ctx) {
        // 'nao' → logico
        if (ctx.getChild(0).getText().equals("nao")) return "logico";
        return inferirTipoParcela_logica(ctx.parcela_logica());
    }

    private String inferirTipoParcela_logica(LAParser.Parcela_logicaContext ctx) {
        if (ctx.getText().equals("verdadeiro") || ctx.getText().equals("falso")) return "logico";
        return inferirTipoExpRelacional(ctx.exp_relacional());
    }

    private String inferirTipoExpRelacional(LAParser.Exp_relacionalContext ctx) {
        // Operador relacional → logico
        if (ctx.exp_aritmetica().size() > 1) return "logico";
        return inferirTipoExpAritmetica(ctx.exp_aritmetica(0));
    }

    private String inferirTipoExpAritmetica(LAParser.Exp_aritmeticaContext ctx) {
        String tipo = inferirTipoTermo(ctx.termo(0));
        for (int i = 1; i < ctx.termo().size(); i++) {
            tipo = promoverTipo(tipo, inferirTipoTermo(ctx.termo(i)));
        }
        return tipo;
    }

    private String inferirTipoTermo(LAParser.TermoContext ctx) {
        String tipo = inferirTipoFator(ctx.fator(0));
        for (int i = 1; i < ctx.fator().size(); i++) {
            tipo = promoverTipo(tipo, inferirTipoFator(ctx.fator(i)));
        }
        return tipo;
    }

    private String inferirTipoFator(LAParser.FatorContext ctx) {
        String tipo = inferirTipoParcela(ctx.parcela(0));
        for (int i = 1; i < ctx.parcela().size(); i++) {
            tipo = promoverTipo(tipo, inferirTipoParcela(ctx.parcela(i)));
        }
        return tipo;
    }

    private String inferirTipoParcela(LAParser.ParcelaContext ctx) {
        if (ctx.parcela_unario() != null) return inferirTipoParcelaUnario(ctx.parcela_unario());
        return inferirTipoParcelaNaoUnario(ctx.parcela_nao_unario());
    }

    private String inferirTipoParcelaUnario(LAParser.Parcela_unarioContext ctx) {
        // Número inteiro
        if (ctx.NUM_INT() != null) return "inteiro";
        // Número real
        if (ctx.NUM_REAL() != null) return "real";

        // Subexpressão: ( expressao )
        if (ctx.IDENT() == null && ctx.expressao() != null && !ctx.expressao().isEmpty()) {
            return inferirTipoExpressao(ctx.expressao(0));
        }

        // Chamada de função: IDENT ( args ) — retorna tipo da função
        if (ctx.IDENT() != null && ctx.expressao() != null && !ctx.expressao().isEmpty()) {
            EntradaTabelaDeSimbolos e = tabela.buscar(ctx.IDENT().getText());
            if (e != null) return e.getTipo();
            return "inteiro";
        }

        // Identificador simples ou campo de registro — consulta tabela
        if (ctx.identificador() != null) {
            return inferirTipoIdentificador(ctx.identificador());
        }

        return "inteiro";
    }

    private String inferirTipoParcelaNaoUnario(LAParser.Parcela_nao_unarioContext ctx) {
        // CADEIA → literal
        if (ctx.CADEIA() != null) return "literal";
        // &ident → ponteiro (tratado como inteiro para printf)
        return "inteiro";
    }

    /**
     * Resolve o tipo de um identificador consultando a tabela de símbolos.
     * Suporta campos de registro (reg.campo) e vetores (v[i]).
     */
    private String inferirTipoIdentificador(LAParser.IdentificadorContext ctx) {
        String nomeBase = ctx.IDENT(0).getText();
        // Campo de registro: reg.campo
        if (ctx.IDENT().size() > 1) {
            String nomeCampo = nomeBase + "." + ctx.IDENT(1).getText();
            EntradaTabelaDeSimbolos campo = tabela.buscar(nomeCampo);
            if (campo != null) return campo.getTipo();
        }
        EntradaTabelaDeSimbolos e = tabela.buscar(nomeBase);
        if (e != null) return e.getTipo();
        return "inteiro";
    }

    /** Promoção de tipos: real > inteiro; literal domina em concatenações. */
    private String promoverTipo(String t1, String t2) {
        if ("real".equals(t1) || "real".equals(t2)) return "real";
        if ("literal".equals(t1) || "literal".equals(t2)) return "literal";
        if ("logico".equals(t1) || "logico".equals(t2)) return "logico";
        return t1 != null ? t1 : "inteiro";
    }

    // -----------------------------------------------------------------------
    // VISITOR: Programa
    // -----------------------------------------------------------------------

    /**
     * Ponto de entrada: emite os includes, processa declarações globais
     * (subprogramas), depois o main com o corpo do algoritmo.
     */
    @Override
    public String visitPrograma(LAParser.ProgramaContext ctx) {
        out.println("#include <stdio.h>");
        out.println("#include <stdlib.h>");
        out.println("#include <string.h>");
        out.println();

        // Processa declarações globais (constantes, tipos, procedimentos, funções)
        // que ficam FORA do main
        for (LAParser.Decl_local_globalContext dlg : ctx.declaracoes().decl_local_global()) {
            if (dlg.declaracao_local() != null) {
                gerarDeclaracaoGlobal(dlg.declaracao_local());
            } else if (dlg.declaracao_global() != null) {
                gerarSubprograma(dlg.declaracao_global());
            }
        }

        // Gera o main
        out.println("int main() {");
        nivel = 1;

        // Declarações locais dentro do corpo
        for (LAParser.Declaracao_localContext dl : ctx.corpo().declaracao_local()) {
            gerarDeclaracaoLocal(dl);
        }

        // Comandos do corpo
        for (LAParser.CmdContext cmd : ctx.corpo().cmd()) {
            visit(cmd);
        }

        emit("return 0;");
        out.println("}");
        return null;
    }

    // -----------------------------------------------------------------------
    // Declarações globais (fora do main): constantes, tipos, subprogramas
    // -----------------------------------------------------------------------

    /**
     * Gera declarações globais:
     *  - constante → #define NOME VALOR
     *  - tipo (registro) → typedef struct { ... } nome;
     */
    private void gerarDeclaracaoGlobal(LAParser.Declaracao_localContext ctx) {
        if (ctx.getChild(0).getText().equals("constante")) {
            // constante NOME : tipo = valor → #define NOME valor
            String nome  = ctx.IDENT().getText();
            String valor = ctx.valor_constante().getText();
            out.println("#define " + nome + " " + valor);
            out.println();
            // Registra na tabela para uso posterior
            tabela.adicionar(new EntradaTabelaDeSimbolos(nome,
                    EntradaTabelaDeSimbolos.Categoria.CONSTANTE,
                    ctx.tipo_basico().getText()));

        } else if (ctx.getChild(0).getText().equals("tipo")) {
            // tipo NOME : registro ... fim_registro → typedef struct { ... } NOME;
            String nome = ctx.IDENT().getText();
            if (ctx.tipo().registro() != null) {
                out.println("typedef struct {");
                for (LAParser.VariavelContext v : ctx.tipo().registro().variavel()) {
                    gerarCamposRegistro(v, 1);
                }
                out.println("} " + nome + ";");
                out.println();
                // Registra o tipo e seus campos
                tabela.adicionar(new EntradaTabelaDeSimbolos(nome,
                        EntradaTabelaDeSimbolos.Categoria.TIPO, "registro"));
                registrarCamposNaTabela(nome, ctx.tipo().registro());
            }
        }
        // declare variavel global: não há no LA, ignora
    }

    /**
     * Gera subprograma (procedimento ou função) fora do main.
     */
    private void gerarSubprograma(LAParser.Declaracao_globalContext ctx) {
        boolean ehFuncao = ctx.getChild(0).getText().equals("funcao");
        String nome = ctx.IDENT().getText();

        // Registra subprograma na tabela de símbolos
        List<String> tiposParams = new ArrayList<>();
        if (ctx.parametros() != null) {
            for (LAParser.ParametroContext p : ctx.parametros().parametro()) {
                String tipoParam = resolverTipoEstendido(p.tipo_estendido());
                for (int i = 0; i < p.identificador().size(); i++) {
                    tiposParams.add(tipoParam);
                }
            }
        }
        String tipoRetorno = ehFuncao ? resolverTipoEstendido(ctx.tipo_estendido()) : "procedimento";
        tabela.adicionar(new EntradaTabelaDeSimbolos(nome,
                ehFuncao ? EntradaTabelaDeSimbolos.Categoria.FUNCAO
                         : EntradaTabelaDeSimbolos.Categoria.PROCEDIMENTO,
                tipoRetorno, tiposParams));

        // Cabeçalho: void nome(params) ou tipo nome(params)
        String retC = ehFuncao ? tipoCBase(resolverTipoEstendido(ctx.tipo_estendido())) : "void";
        StringBuilder header = new StringBuilder(retC + " " + nome + "(");
        if (ctx.parametros() != null) {
            List<String> params = new ArrayList<>();
            for (LAParser.ParametroContext p : ctx.parametros().parametro()) {
                String tipoP = resolverTipoEstendido(p.tipo_estendido());
                boolean isVar = p.getChild(0).getText().equals("var");
                for (LAParser.IdentificadorContext id : p.identificador()) {
                    String nomeP = id.IDENT(0).getText();
                    String decl;
                    if ("literal".equals(tipoP)) {
                        // literal passado por referência: char* nome
                        decl = "char* " + nomeP;
                    } else if (tipoP.startsWith("^")) {
                        decl = tipoCBase(tipoP.substring(1)) + "* " + nomeP;
                    } else if (isVar) {
                        decl = tipoCBase(tipoP) + "* " + nomeP;
                    } else {
                        decl = tipoCBase(tipoP) + " " + nomeP;
                    }
                    params.add(decl);
                }
            }
            header.append(String.join(", ", params));
        }
        header.append(")");
        out.println(header + "{");

        // Corpo do subprograma
        tabela.criarEscopo();
        if (ctx.parametros() != null) {
            for (LAParser.ParametroContext p : ctx.parametros().parametro()) {
                String tipoP = resolverTipoEstendido(p.tipo_estendido());
                for (LAParser.IdentificadorContext id : p.identificador()) {
                    tabela.adicionar(new EntradaTabelaDeSimbolos(
                            id.IDENT(0).getText(),
                            EntradaTabelaDeSimbolos.Categoria.VARIAVEL, tipoP));
                    expandirCamposTipo(id.IDENT(0).getText(), tipoP);
                }
            }
        }
        nivel = 1;
        for (LAParser.Declaracao_localContext dl : ctx.declaracao_local()) {
            gerarDeclaracaoLocal(dl);
        }
        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visit(cmd);
        }
        tabela.destruirEscopo();

        out.println("}");
        out.println();
    }

    // -----------------------------------------------------------------------
    // Declarações locais (dentro de main ou subprogramas)
    // -----------------------------------------------------------------------

    /**
     * Gera declaração local: variável, constante ou tipo.
     */
    private void gerarDeclaracaoLocal(LAParser.Declaracao_localContext ctx) {
        if (ctx.variavel() != null) {
            gerarVariavel(ctx.variavel());

        } else if (ctx.getChild(0).getText().equals("constante")) {
            String nome  = ctx.IDENT().getText();
            String valor = ctx.valor_constante().getText();
            // Constante local → #define (antes do main, idealmente, mas aqui geramos local)
            // Como não podemos fazer #define dentro de função em C padrão, usamos const
            emit("const int " + nome + " = " + valor + ";");
            tabela.adicionar(new EntradaTabelaDeSimbolos(nome,
                    EntradaTabelaDeSimbolos.Categoria.CONSTANTE,
                    ctx.tipo_basico().getText()));

        } else if (ctx.getChild(0).getText().equals("tipo")) {
            String nome = ctx.IDENT().getText();
            if (ctx.tipo().registro() != null) {
                emit("typedef struct {");
                nivel++;
                for (LAParser.VariavelContext v : ctx.tipo().registro().variavel()) {
                    gerarCamposRegistro(v, nivel);
                }
                nivel--;
                emit("} " + nome + ";");
                tabela.adicionar(new EntradaTabelaDeSimbolos(nome,
                        EntradaTabelaDeSimbolos.Categoria.TIPO, "registro"));
                registrarCamposNaTabela(nome, ctx.tipo().registro());
            }
        }
    }

    /**
     * Gera declaração de variável(eis).
     * Exemplos:
     *  - x: inteiro → int x;
     *  - x,y: real  → float x, y; (ou duas linhas)
     *  - s: literal → char s[80];
     *  - p: ^inteiro → int* p;
     *  - v[5]: inteiro → int v[5];
     *  - r: registro ... → struct { ... } r;
     *  - r: treg → treg r;
     */
    private void gerarVariavel(LAParser.VariavelContext ctx) {
        LAParser.TipoContext tipoCtx = ctx.tipo();
        String tipoLA = resolverTipoSimples(tipoCtx);

        if (tipoCtx.registro() != null) {
            // Registro anônimo inline
            emit("struct {");
            nivel++;
            for (LAParser.VariavelContext v : tipoCtx.registro().variavel()) {
                gerarCamposRegistro(v, nivel);
            }
            nivel--;
            // Gera os nomes das variáveis
            List<String> nomes = new ArrayList<>();
            for (LAParser.IdentificadorContext id : ctx.identificador()) {
                String nome = id.IDENT(0).getText();
                nomes.add(nome);
                tabela.adicionar(new EntradaTabelaDeSimbolos(nome,
                        EntradaTabelaDeSimbolos.Categoria.VARIAVEL, "registro"));
                registrarCamposNaTabela(nome, tipoCtx.registro());
            }
            emit("} " + String.join(", ", nomes) + ";");
            return;
        }

        // Tipo simples, ponteiro ou tipo definido
        List<String> decls = new ArrayList<>();
        for (LAParser.IdentificadorContext id : ctx.identificador()) {
            String nome = id.IDENT(0).getText();
            String nomeComDim = nome;

            // Vetor: ident[n]
            LAParser.DimensaoContext dim = id.dimensao();
            if (dim != null && !dim.exp_aritmetica().isEmpty()) {
                nomeComDim = nome + "[" + gerarExpAritmetica(dim.exp_aritmetica(0)) + "]";
            }

            // Registra na tabela
            tabela.adicionar(new EntradaTabelaDeSimbolos(nome,
                    EntradaTabelaDeSimbolos.Categoria.VARIAVEL, tipoLA));
            expandirCamposTipo(nome, tipoLA);

            // Gera a declaração C
            if ("literal".equals(tipoLA)) {
                if (dim != null && !dim.exp_aritmetica().isEmpty()) {
                    decls.add("char " + nomeComDim + "[80]");
                } else {
                    decls.add("char " + nome + "[80]");
                }
            } else if (tipoLA.startsWith("^")) {
                String base = tipoLA.substring(1);
                decls.add(tipoCBase(base) + "* " + nome);
            } else {
                if (dim != null && !dim.exp_aritmetica().isEmpty()) {
                    decls.add(tipoCBase(tipoLA) + " " + nomeComDim);
                } else {
                    decls.add(tipoCBase(tipoLA) + " " + nome);
                }
            }
        }

        // Agrupa múltiplas variáveis do mesmo tipo em uma linha quando possível
        if (!decls.isEmpty()) {
            if (decls.size() == 1) {
                emit(decls.get(0) + ";");
            } else {
                // Verifica se pode agrupar (mesmo tipo, sem char[80])
                boolean podeAgrupar = !tipoLA.equals("literal");
                if (podeAgrupar) {
                    // Extrai só os nomes (o tipo é o mesmo)
                    List<String> nomesOnly = new ArrayList<>();
                    for (LAParser.IdentificadorContext id : ctx.identificador()) {
                        String nome = id.IDENT(0).getText();
                        LAParser.DimensaoContext dim2 = id.dimensao();
                        if (dim2 != null && !dim2.exp_aritmetica().isEmpty()) {
                            nomesOnly.add(nome + "[" + gerarExpAritmetica(dim2.exp_aritmetica(0)) + "]");
                        } else {
                            nomesOnly.add(nome);
                        }
                    }
                    emit(tipoCBase(tipoLA) + " " + String.join(", ", nomesOnly) + ";");
                } else {
                    for (String d : decls) emit(d + ";");
                }
            }
        }
    }

    /**
     * Gera os campos de um registro como declarações C.
     */
    private void gerarCamposRegistro(LAParser.VariavelContext ctx, int nivelLocal) {
        String tipo = resolverTipoSimples(ctx.tipo());
        List<String> nomes = new ArrayList<>();
        for (LAParser.IdentificadorContext id : ctx.identificador()) {
            nomes.add(id.IDENT(0).getText());
        }
        String indent = "";
        for (int i = 0; i < nivelLocal; i++) indent += "\t";

        if ("literal".equals(tipo)) {
            for (String n : nomes) out.println(indent + "char " + n + "[80];");
        } else {
            out.println(indent + tipoCBase(tipo) + " " + String.join(", ", nomes) + ";");
        }
    }

    // -----------------------------------------------------------------------
    // VISITOR: Comandos
    // -----------------------------------------------------------------------

    @Override
    public String visitCmd(LAParser.CmdContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * leia(ident, ...) → scanf("%d", &x) ou gets(x) para literal
     */
    @Override
    public String visitCmdLeia(LAParser.CmdLeiaContext ctx) {
        for (LAParser.IdentificadorContext id : ctx.identificador()) {
            String nome = gerarIdentificador(id);
            String nomeBase = id.IDENT(0).getText();
            String tipo = resolverTipoIdent(id);

            if ("literal".equals(tipo)) {
                emit("gets(" + nome + ");");
            } else {
                emit("scanf(\"" + formatoTipo(tipo) + "\",&" + nome + ");");
            }
        }
        return null;
    }

    /**
     * escreva(expr, ...) → printf(...)
     * Agrupa múltiplas expressões num único printf quando possível,
     * ou gera um printf por expressão.
     */
    @Override
    public String visitCmdEscreva(LAParser.CmdEscrevaContext ctx) {
        for (LAParser.ExpressaoContext expr : ctx.expressao()) {
            String tipo = inferirTipoExpressao(expr);
            String exprC = gerarExpressao(expr);

            // Cadeias literais: printf("texto") sem formato extra
            if (expr.getText().startsWith("\"")) {
                // Remove aspas externas do texto
                String texto = exprC;
                emit("printf(" + texto + ");");
            } else {
                emit("printf(\"" + formatoTipo(tipo) + "\"," + exprC + ");");
            }
        }
        return null;
    }

    /**
     * se expressao entao {cmd} [senao {cmd}] fim_se → if (...) { } [else { }]
     */
    @Override
    public String visitCmdSe(LAParser.CmdSeContext ctx) {
        String cond = gerarExpressao(ctx.expressao());
        emit("if (" + cond + ") {");
        nivel++;

        // O ANTLR coloca todos os cmds numa lista; a primeira metade é o 'entao',
        // a segunda é o 'senao' (se houver). Precisamos separar pelo token 'senao'.
        List<LAParser.CmdContext> cmds = ctx.cmd();
        int idxSenao = encontrarSenao(ctx);

        List<LAParser.CmdContext> cmdsThen = idxSenao >= 0 ? cmds.subList(0, idxSenao) : cmds;
        List<LAParser.CmdContext> cmdsElse = idxSenao >= 0 ? cmds.subList(idxSenao, cmds.size()) : new ArrayList<>();

        for (LAParser.CmdContext c : cmdsThen) visit(c);
        nivel--;

        if (!cmdsElse.isEmpty()) {
            emit("}");
            emit("else {");
            nivel++;
            for (LAParser.CmdContext c : cmdsElse) visit(c);
            nivel--;
        }
        emit("}");
        return null;
    }

    /**
     * Encontra o índice do primeiro comando após o 'senao' na lista de cmds do CmdSe.
     * O ANTLR coloca os cmds de then e else na mesma lista; usamos os tokens filhos
     * para encontrar a posição do 'senao'.
     */
    private int encontrarSenao(LAParser.CmdSeContext ctx) {
        // Conta quantos cmds pertencem ao bloco 'entao' contando tokens
        int cmdIdx = 0;
        boolean encontrouSenao = false;
        int cmdCount = 0;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            String texto = ctx.getChild(i).getText();
            if (texto.equals("entao")) continue;
            if (texto.equals("senao")) {
                encontrouSenao = true;
                cmdIdx = cmdCount;
                break;
            }
            if (ctx.getChild(i) instanceof LAParser.CmdContext) {
                cmdCount++;
            }
        }
        return encontrouSenao ? cmdIdx : -1;
    }

    /**
     * caso exp_aritmetica seja selecao [senao {cmd}] fim_caso → switch (...) { ... }
     *
     * Intervalos (a..b) são expandidos em casos individuais.
     */
    @Override
    public String visitCmdCaso(LAParser.CmdCasoContext ctx) {
        String expr = gerarExpAritmetica(ctx.exp_aritmetica());
        emit("switch (" + expr + ") {");
        nivel++;

        for (LAParser.Item_selecaoContext item : ctx.selecao().item_selecao()) {
            // Gera um 'case' para cada número/intervalo
            for (LAParser.Numero_intervaloContext ni : item.constantes().numero_intervalo()) {
                gerarCasesIntervalo(ni);
            }
            // Comandos do item
            nivel++;
            for (LAParser.CmdContext c : item.cmd()) visit(c);
            emit("break;");
            nivel--;
        }

        // Senao → default
        if (!ctx.cmd().isEmpty()) {
            nivel--;
            emit("default:");
            nivel++;
            for (LAParser.CmdContext c : ctx.cmd()) visit(c);
            nivel--;
            nivel++;
        }

        nivel--;
        emit("}");
        return null;
    }

    /**
     * Gera os labels case para um intervalo (simples ou a..b).
     * Intervalos são expandidos: 3..5 → case 3: case 4: case 5:
     */
    private void gerarCasesIntervalo(LAParser.Numero_intervaloContext ctx) {
        List<TerminalNode> nums = ctx.NUM_INT();
        String sinal1 = ctx.op_unario() != null && !ctx.op_unario().isEmpty()
                ? ctx.op_unario(0).getText() : "";

        if (nums.size() == 1) {
            emit("case " + sinal1 + nums.get(0).getText() + ":");
        } else {
            // Intervalo a..b: expande
            int inicio = Integer.parseInt(sinal1 + nums.get(0).getText());
            String sinal2 = ctx.op_unario().size() > 1 ? ctx.op_unario(1).getText() : "";
            int fim = Integer.parseInt(sinal2 + nums.get(1).getText());
            for (int v = inicio; v <= fim; v++) {
                emit("case " + v + ":");
            }
        }
    }

    /**
     * para IDENT <- exp ate exp faca {cmd} fim_para → for (i=ini; i<=fim; i++) { }
     */
    @Override
    public String visitCmdPara(LAParser.CmdParaContext ctx) {
        String var  = ctx.IDENT().getText();
        String ini  = gerarExpAritmetica(ctx.exp_aritmetica(0));
        String fim  = gerarExpAritmetica(ctx.exp_aritmetica(1));
        emit("for (" + var + " = " + ini + "; " + var + " <= " + fim + "; " + var + "++) {");
        nivel++;
        for (LAParser.CmdContext c : ctx.cmd()) visit(c);
        nivel--;
        emit("}");
        return null;
    }

    /**
     * enquanto expressao faca {cmd} fim_enquanto → while (...) { }
     */
    @Override
    public String visitCmdEnquanto(LAParser.CmdEnquantoContext ctx) {
        String cond = gerarExpressao(ctx.expressao());
        emit("while (" + cond + ") {");
        nivel++;
        for (LAParser.CmdContext c : ctx.cmd()) visit(c);
        nivel--;
        emit("}");
        return null;
    }

    /**
     * faca {cmd} ate expressao → do { } while (cond);
     */
    @Override
    public String visitCmdFaca(LAParser.CmdFacaContext ctx) {
        emit("do {");
        nivel++;
        for (LAParser.CmdContext c : ctx.cmd()) visit(c);
        nivel--;
        String cond = gerarExpressao(ctx.expressao());
        emit("} while (" + cond + ");");
        return null;
    }

    /**
     * [^] identificador <- expressao → nome = expr;
     * Para literal: strcpy(nome, expr);
     */
    @Override
    public String visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        boolean temCirc = ctx.getStart().getText().equals("^");
        LAParser.IdentificadorContext id = ctx.identificador();
        String nomeLA = gerarIdentificador(id);
        String nomeC  = temCirc ? "*" + nomeLA : nomeLA;

        String tipo = resolverTipoIdent(id);
        String exprC = gerarExpressao(ctx.expressao());

        if ("literal".equals(tipo) && !temCirc) {
            emit("strcpy(" + nomeC + "," + exprC + ");");
        } else {
            emit(nomeC + " = " + exprC + ";");
        }
        return null;
    }

    /**
     * IDENT(expressao, ...) → IDENT(expr, ...);
     */
    @Override
    public String visitCmdChamada(LAParser.CmdChamadaContext ctx) {
        String nome = ctx.IDENT().getText();
        List<String> args = new ArrayList<>();
        for (LAParser.ExpressaoContext e : ctx.expressao()) {
            args.add(gerarExpressao(e));
        }
        emit(nome + "(" + String.join(", ", args) + ");");
        return null;
    }

    /**
     * retorne expressao → return expr;
     */
    @Override
    public String visitCmdRetorne(LAParser.CmdRetorneContext ctx) {
        emit("return " + gerarExpressao(ctx.expressao()) + ";");
        return null;
    }

    // -----------------------------------------------------------------------
    // Geração de expressões → String C
    // -----------------------------------------------------------------------

    private String gerarExpressao(LAParser.ExpressaoContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(gerarTermoLogico(ctx.termo_logico(0)));
        for (int i = 1; i < ctx.termo_logico().size(); i++) {
            sb.append(" || ").append(gerarTermoLogico(ctx.termo_logico(i)));
        }
        return sb.toString();
    }

    private String gerarTermoLogico(LAParser.Termo_logicoContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(gerarFatorLogico(ctx.fator_logico(0)));
        for (int i = 1; i < ctx.fator_logico().size(); i++) {
            sb.append(" && ").append(gerarFatorLogico(ctx.fator_logico(i)));
        }
        return sb.toString();
    }

    private String gerarFatorLogico(LAParser.Fator_logicoContext ctx) {
        if (ctx.getChild(0).getText().equals("nao")) {
            return "!(" + gerarParcelaLogica(ctx.parcela_logica()) + ")";
        }
        return gerarParcelaLogica(ctx.parcela_logica());
    }

    private String gerarParcelaLogica(LAParser.Parcela_logicaContext ctx) {
        if (ctx.getText().equals("verdadeiro")) return "1";
        if (ctx.getText().equals("falso"))      return "0";
        return gerarExpRelacional(ctx.exp_relacional());
    }

    private String gerarExpRelacional(LAParser.Exp_relacionalContext ctx) {
        String esq = gerarExpAritmetica(ctx.exp_aritmetica(0));
        if (ctx.exp_aritmetica().size() > 1) {
            String op  = ctx.op_relacional().getText();
            String dir = gerarExpAritmetica(ctx.exp_aritmetica(1));
            // Mapeia operadores LA → C
            switch (op) {
                case "=":  op = "=="; break;
                case "<>": op = "!="; break;
            }
            return esq + " " + op + " " + dir;
        }
        return esq;
    }

    private String gerarExpAritmetica(LAParser.Exp_aritmeticaContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(gerarTermo(ctx.termo(0)));
        for (int i = 0; i < ctx.op1().size(); i++) {
            sb.append(ctx.op1(i).getText());
            sb.append(gerarTermo(ctx.termo(i + 1)));
        }
        return sb.toString();
    }

    private String gerarTermo(LAParser.TermoContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(gerarFator(ctx.fator(0)));
        for (int i = 0; i < ctx.op2().size(); i++) {
            sb.append(ctx.op2(i).getText());
            sb.append(gerarFator(ctx.fator(i + 1)));
        }
        return sb.toString();
    }

    private String gerarFator(LAParser.FatorContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(gerarParcela(ctx.parcela(0)));
        for (int i = 0; i < ctx.op3().size(); i++) {
            sb.append(ctx.op3(i).getText());
            sb.append(gerarParcela(ctx.parcela(i + 1)));
        }
        return sb.toString();
    }

    private String gerarParcela(LAParser.ParcelaContext ctx) {
        if (ctx.parcela_unario() != null) {
            String unario = "";
            if (ctx.op_unario() != null) unario = ctx.op_unario().getText();
            return unario + gerarParcelaUnario(ctx.parcela_unario());
        }
        return gerarParcelaNaoUnario(ctx.parcela_nao_unario());
    }

    private String gerarParcelaUnario(LAParser.Parcela_unarioContext ctx) {
        // NUM_INT e NUM_REAL são tokens terminais singulares na gramática
        if (ctx.NUM_INT() != null) return ctx.NUM_INT().getText();
        if (ctx.NUM_REAL() != null) return ctx.NUM_REAL().getText();

        // Subexpressão: ( expressao )
        if (ctx.IDENT() == null && ctx.expressao() != null && !ctx.expressao().isEmpty()) {
            return "(" + gerarExpressao(ctx.expressao(0)) + ")";
        }

        // Chamada de função: IDENT ( args )
        if (ctx.IDENT() != null && ctx.expressao() != null && !ctx.expressao().isEmpty()) {
            List<String> args = new ArrayList<>();
            for (LAParser.ExpressaoContext e : ctx.expressao()) args.add(gerarExpressao(e));
            return ctx.IDENT().getText() + "(" + String.join(", ", args) + ")";
        }

        // Identificador (com possível ^ de desreferência)
        if (ctx.identificador() != null) {
            boolean temCirc = ctx.getChild(0).getText().equals("^");
            String nome = gerarIdentificador(ctx.identificador());
            return temCirc ? "*" + nome : nome;
        }

        return "";
    }

    private String gerarParcelaNaoUnario(LAParser.Parcela_nao_unarioContext ctx) {
        if (ctx.CADEIA() != null) return ctx.CADEIA().getText();
        if (ctx.identificador() != null) {
            return "&" + gerarIdentificador(ctx.identificador());
        }
        return "";
    }

    /**
     * Gera o nome C de um identificador, incluindo campo de registro e índice de vetor.
     * Ex: ponto1.x → ponto1.x
     *     vetor[i] → vetor[i]
     */
    private String gerarIdentificador(LAParser.IdentificadorContext ctx) {
        StringBuilder sb = new StringBuilder(ctx.IDENT(0).getText());
        // Campos de registro
        for (int i = 1; i < ctx.IDENT().size(); i++) {
            sb.append(".").append(ctx.IDENT(i).getText());
        }
        // Índice de vetor
        LAParser.DimensaoContext dim = ctx.dimensao();
        if (dim != null && !dim.exp_aritmetica().isEmpty()) {
            sb.append("[").append(gerarExpAritmetica(dim.exp_aritmetica(0))).append("]");
        }
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Utilitários de resolução de tipos
    // -----------------------------------------------------------------------

    private String resolverTipoEstendido(LAParser.Tipo_estendidoContext ctx) {
        LAParser.Tipo_basico_identContext tbi = ctx.tipo_basico_ident();
        String base = tbi.tipo_basico() != null ? tbi.tipo_basico().getText() : tbi.IDENT().getText();
        boolean ehPonteiro = !ctx.getText().startsWith(base);
        return ehPonteiro ? "^" + base : base;
    }

    private String resolverTipoSimples(LAParser.TipoContext ctx) {
        if (ctx.registro() != null) return "registro";
        return resolverTipoEstendido(ctx.tipo_estendido());
    }

    /**
     * Resolve o tipo de um identificador consultando a tabela de símbolos.
     * Suporta campos de registro (ponto1.x) e vetores.
     */
    private String resolverTipoIdent(LAParser.IdentificadorContext ctx) {
        String nomeBase = ctx.IDENT(0).getText();
        if (ctx.IDENT().size() > 1) {
            String campo = nomeBase + "." + ctx.IDENT(1).getText();
            EntradaTabelaDeSimbolos e = tabela.buscar(campo);
            if (e != null) return e.getTipo();
        }
        EntradaTabelaDeSimbolos e = tabela.buscar(nomeBase);
        return e != null ? e.getTipo() : "inteiro";
    }

    /**
     * Registra os campos de um registro na tabela com prefixo "dono.campo".
     */
    private void registrarCamposNaTabela(String prefixo, LAParser.RegistroContext ctx) {
        for (LAParser.VariavelContext v : ctx.variavel()) {
            String tipo = resolverTipoSimples(v.tipo());
            for (LAParser.IdentificadorContext id : v.identificador()) {
                String nomeCampo = prefixo + "." + id.IDENT(0).getText();
                if (!tabela.existeNoEscopoAtual(nomeCampo)) {
                    tabela.adicionar(new EntradaTabelaDeSimbolos(nomeCampo,
                            EntradaTabelaDeSimbolos.Categoria.VARIAVEL, tipo));
                }
            }
        }
    }

    /**
     * Expande os campos de um tipo definido pelo usuário para uma variável.
     * Ex: vinho: tVinho → registra vinho.nome, vinho.preco, etc.
     */
    private void expandirCamposTipo(String nomeVar, String nomeTipo) {
        List<EntradaTabelaDeSimbolos> campos = tabela.buscarCampos(nomeTipo);
        for (EntradaTabelaDeSimbolos campo : campos) {
            String sufixo = campo.getNome().substring(nomeTipo.length());
            String nomeCampoVar = nomeVar + sufixo;
            if (!tabela.existeNoEscopoAtual(nomeCampoVar)) {
                tabela.adicionar(new EntradaTabelaDeSimbolos(nomeCampoVar,
                        EntradaTabelaDeSimbolos.Categoria.VARIAVEL, campo.getTipo()));
            }
        }
    }
}
