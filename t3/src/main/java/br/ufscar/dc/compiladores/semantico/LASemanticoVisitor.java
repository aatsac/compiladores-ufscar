package br.ufscar.dc.compiladores.semantico;

import org.antlr.v4.runtime.Token;
import java.io.PrintWriter;

/**
 * Visitor semântico da linguagem LA.
 *
 * Percorre a árvore sintática gerada pelo ANTLR e realiza as seguintes
 * verificações semânticas:
 *
 *  1. Identificador já declarado no mesmo escopo (duplicado)
 *  2. Tipo não declarado (uso de tipo inexistente)
 *  3. Identificador não declarado (uso antes de declarar)
 *  4. Atribuição incompatível com o tipo declarado
 *
 * Ao encontrar um erro, NÃO interrompe a execução — continua verificando
 * o restante do programa e acumulando todas as mensagens de erro.
 */
public class LASemanticoVisitor extends LABaseVisitor<String> {

    // Constantes de tipo interno
    private static final String TIPO_INTEIRO    = "inteiro";
    private static final String TIPO_REAL       = "real";
    private static final String TIPO_LITERAL    = "literal";
    private static final String TIPO_LOGICO     = "logico";
    private static final String TIPO_INDEFINIDO = "tipo_indefinido";

    private final TabelaDeSimbolos tabela;
    private final PrintWriter writer;

    public LASemanticoVisitor(PrintWriter writer) {
        this.tabela = new TabelaDeSimbolos();
        this.writer = writer;
    }

    // -----------------------------------------------------------------------
    // Métodos auxiliares de emissão de erros
    // -----------------------------------------------------------------------

    private void erroJaDeclarado(Token token) {
        writer.println("Linha " + token.getLine()
                + ": identificador " + token.getText() + " ja declarado anteriormente");
    }

    private void erroTipoNaoDeclarado(Token token, String nomeTipo) {
        writer.println("Linha " + token.getLine()
                + ": tipo " + nomeTipo + " nao declarado");
    }

    private void erroNaoDeclarado(Token token) {
        writer.println("Linha " + token.getLine()
                + ": identificador " + token.getText() + " nao declarado");
    }

    private void erroAtribuicaoIncompativel(Token token) {
        writer.println("Linha " + token.getLine()
                + ": atribuicao nao compativel para " + token.getText());
    }

    // -----------------------------------------------------------------------
    // Resolução de tipos
    // -----------------------------------------------------------------------

    /**
     * Resolve o tipo de um tipo_estendido.
     * Verifica se um tipo definido pelo usuário foi declarado.
     */
    private String resolverTipoEstendido(LAParser.Tipo_estendidoContext ctx) {
        LAParser.Tipo_basico_identContext tbi = ctx.tipo_basico_ident();
        if (tbi.tipo_basico() != null) {
            return tbi.tipo_basico().getText();
        }
        // Tipo definido pelo usuário (IDENT)
        String nomeIdent = tbi.IDENT().getText();
        EntradaTabelaDeSimbolos entrada = tabela.buscar(nomeIdent);
        if (entrada == null || entrada.getCategoria() != EntradaTabelaDeSimbolos.Categoria.TIPO) {
            erroTipoNaoDeclarado(tbi.IDENT().getSymbol(), nomeIdent);
            return TIPO_INDEFINIDO;
        }
        return nomeIdent;
    }

    /**
     * Resolve o tipo de um nó tipo (registro ou tipo_estendido).
     */
    private String resolverTipo(LAParser.TipoContext ctx) {
        if (ctx.registro() != null) {
            return "registro";
        }
        return resolverTipoEstendido(ctx.tipo_estendido());
    }

    /**
     * Verifica compatibilidade de tipos para atribuição:
     *  - numérico ← numérico (inteiro/real intercambiáveis)
     *  - literal  ← literal
     *  - logico   ← logico
     *  - mesmo tipo definido pelo usuário
     *  - ponteiro ← endereço (^tipo ← ^tipo)
     */
    private boolean tiposCompativeis(String tipoEsq, String tipoDir) {
        if (tipoEsq == null || tipoDir == null) return false;
        if (tipoEsq.equals(TIPO_INDEFINIDO) || tipoDir.equals(TIPO_INDEFINIDO)) return false;

        boolean esqNum = tipoEsq.equals(TIPO_INTEIRO) || tipoEsq.equals(TIPO_REAL);
        boolean dirNum = tipoDir.equals(TIPO_INTEIRO) || tipoDir.equals(TIPO_REAL);
        if (esqNum && dirNum) return true;

        return tipoEsq.equals(tipoDir);
    }

    // -----------------------------------------------------------------------
    // VISITOR: Declarações locais
    // -----------------------------------------------------------------------

    /**
     * Processa declarações locais:
     *  - declare variavel
     *  - constante IDENT : tipo_basico = valor_constante
     *  - tipo IDENT : tipo
     */
    @Override
    public String visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        if (ctx.variavel() != null) {
            visitVariavelDeclaracao(ctx.variavel());

        } else if (ctx.getChild(0).getText().equals("constante")) {
            Token ident = ctx.IDENT().getSymbol();
            if (tabela.existeNoEscopoAtual(ident.getText())) {
                erroJaDeclarado(ident);
            } else {
                tabela.adicionar(new EntradaTabelaDeSimbolos(
                        ident.getText(),
                        EntradaTabelaDeSimbolos.Categoria.CONSTANTE,
                        ctx.tipo_basico().getText()));
            }

        } else if (ctx.getChild(0).getText().equals("tipo")) {
            Token ident = ctx.IDENT().getSymbol();
            if (tabela.existeNoEscopoAtual(ident.getText())) {
                erroJaDeclarado(ident);
            } else {
                tabela.adicionar(new EntradaTabelaDeSimbolos(
                        ident.getText(),
                        EntradaTabelaDeSimbolos.Categoria.TIPO,
                        resolverTipo(ctx.tipo())));
            }
        }
        return null;
    }

    /**
     * Processa a declaração de uma variável (lista de identificadores : tipo).
     * Registra cada identificador na tabela verificando duplicatas.
     */
    private void visitVariavelDeclaracao(LAParser.VariavelContext ctx) {
        String tipoResolvido = resolverTipo(ctx.tipo());

        for (LAParser.IdentificadorContext identCtx : ctx.identificador()) {
            Token ident = identCtx.IDENT(0).getSymbol();
            if (tabela.existeNoEscopoAtual(ident.getText())) {
                erroJaDeclarado(ident);
            } else {
                tabela.adicionar(new EntradaTabelaDeSimbolos(
                        ident.getText(),
                        EntradaTabelaDeSimbolos.Categoria.VARIAVEL,
                        tipoResolvido));
            }
        }
    }

    // -----------------------------------------------------------------------
    // VISITOR: Declarações globais (procedimentos e funções)
    // -----------------------------------------------------------------------

    /**
     * Processa procedimento ou função:
     *  - Registra no escopo externo
     *  - Cria escopo interno para parâmetros e corpo
     *  - Destrói o escopo ao sair
     */
    @Override
    public String visitDeclaracao_global(LAParser.Declaracao_globalContext ctx) {
        Token ident = ctx.IDENT().getSymbol();
        boolean ehFuncao = ctx.getChild(0).getText().equals("funcao");

        String tipoRetorno = ehFuncao
                ? resolverTipoEstendido(ctx.tipo_estendido())
                : "procedimento";

        if (tabela.existeNoEscopoAtual(ident.getText())) {
            erroJaDeclarado(ident);
        } else {
            tabela.adicionar(new EntradaTabelaDeSimbolos(
                    ident.getText(),
                    ehFuncao ? EntradaTabelaDeSimbolos.Categoria.FUNCAO
                             : EntradaTabelaDeSimbolos.Categoria.PROCEDIMENTO,
                    tipoRetorno));
        }

        // Cria escopo interno para o corpo do subprograma
        tabela.criarEscopo();

        // Registra parâmetros no novo escopo
        if (ctx.parametros() != null) {
            for (LAParser.ParametroContext param : ctx.parametros().parametro()) {
                String tipoParam = resolverTipoEstendido(param.tipo_estendido());
                for (LAParser.IdentificadorContext identParam : param.identificador()) {
                    Token tParam = identParam.IDENT(0).getSymbol();
                    if (tabela.existeNoEscopoAtual(tParam.getText())) {
                        erroJaDeclarado(tParam);
                    } else {
                        tabela.adicionar(new EntradaTabelaDeSimbolos(
                                tParam.getText(),
                                EntradaTabelaDeSimbolos.Categoria.VARIAVEL,
                                tipoParam));
                    }
                }
            }
        }

        // Visita declarações locais e comandos do corpo
        for (LAParser.Declaracao_localContext dl : ctx.declaracao_local()) {
            visitDeclaracao_local(dl);
        }
        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visit(cmd);
        }

        tabela.destruirEscopo();
        return null;
    }

    // -----------------------------------------------------------------------
    // VISITOR: Comandos
    // -----------------------------------------------------------------------

    /**
     * leia(^? identificador, ...) — verifica se cada identificador foi declarado.
     */
    @Override
    public String visitCmdLeia(LAParser.CmdLeiaContext ctx) {
        for (LAParser.IdentificadorContext identCtx : ctx.identificador()) {
            Token ident = identCtx.IDENT(0).getSymbol();
            if (tabela.buscar(ident.getText()) == null) {
                erroNaoDeclarado(ident);
            }
        }
        return null;
    }

    /**
     * escreva(expressao, ...) — visita cada expressão para detectar
     * identificadores não declarados dentro delas.
     */
    @Override
    public String visitCmdEscreva(LAParser.CmdEscrevaContext ctx) {
        for (LAParser.ExpressaoContext expr : ctx.expressao()) {
            visit(expr);
        }
        return null;
    }

    /**
     * identificador <- expressao — verifica declaração e compatibilidade de tipos.
     */
    @Override
    public String visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        LAParser.IdentificadorContext identCtx = ctx.identificador();
        Token ident = identCtx.IDENT(0).getSymbol();

        EntradaTabelaDeSimbolos entrada = tabela.buscar(ident.getText());
        if (entrada == null) {
            erroNaoDeclarado(ident);
            // Visita a expressão mesmo assim para detectar outros erros
            visit(ctx.expressao());
            return null;
        }

        String tipoDir = visit(ctx.expressao());

        if (!tiposCompativeis(entrada.getTipo(), tipoDir)) {
            erroAtribuicaoIncompativel(ident);
        }

        return null;
    }

    /**
     * se expressao entao {cmd} [senao {cmd}] fim_se
     */
    @Override
    public String visitCmdSe(LAParser.CmdSeContext ctx) {
        visit(ctx.expressao());
        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visit(cmd);
        }
        return null;
    }

    /**
     * enquanto expressao faca {cmd} fim_enquanto
     */
    @Override
    public String visitCmdEnquanto(LAParser.CmdEnquantoContext ctx) {
        visit(ctx.expressao());
        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visit(cmd);
        }
        return null;
    }

    /**
     * para IDENT <- exp ate exp faca {cmd} fim_para
     */
    @Override
    public String visitCmdPara(LAParser.CmdParaContext ctx) {
        Token ident = ctx.IDENT().getSymbol();
        if (tabela.buscar(ident.getText()) == null) {
            erroNaoDeclarado(ident);
        }
        for (LAParser.Exp_aritmeticaContext exp : ctx.exp_aritmetica()) {
            visit(exp);
        }
        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visit(cmd);
        }
        return null;
    }

    /**
     * faca {cmd} ate expressao
     */
    @Override
    public String visitCmdFaca(LAParser.CmdFacaContext ctx) {
        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visit(cmd);
        }
        visit(ctx.expressao());
        return null;
    }

    /**
     * caso exp_aritmetica seja selecao [senao {cmd}] fim_caso
     */
    @Override
    public String visitCmdCaso(LAParser.CmdCasoContext ctx) {
        visit(ctx.exp_aritmetica());
        visit(ctx.selecao());
        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visit(cmd);
        }
        return null;
    }

    /**
     * IDENT(expressao, ...) — chamada de procedimento
     */
    @Override
    public String visitCmdChamada(LAParser.CmdChamadaContext ctx) {
        Token ident = ctx.IDENT().getSymbol();
        if (tabela.buscar(ident.getText()) == null) {
            erroNaoDeclarado(ident);
        }
        for (LAParser.ExpressaoContext expr : ctx.expressao()) {
            visit(expr);
        }
        return null;
    }

    /**
     * retorne expressao
     */
    @Override
    public String visitCmdRetorne(LAParser.CmdRetorneContext ctx) {
        return visit(ctx.expressao());
    }

    // -----------------------------------------------------------------------
    // VISITOR: Expressões — retornam o tipo inferido
    // -----------------------------------------------------------------------

    /**
     * expressao → termo_logico {ou termo_logico}
     */
    @Override
    public String visitExpressao(LAParser.ExpressaoContext ctx) {
        String tipo = visit(ctx.termo_logico(0));
        for (int i = 1; i < ctx.termo_logico().size(); i++) {
            String t = visit(ctx.termo_logico(i));
            if (!tiposCompativeis(tipo, t)) tipo = TIPO_INDEFINIDO;
        }
        return tipo;
    }

    /**
     * termo_logico → fator_logico {e fator_logico}
     */
    @Override
    public String visitTermo_logico(LAParser.Termo_logicoContext ctx) {
        String tipo = visit(ctx.fator_logico(0));
        for (int i = 1; i < ctx.fator_logico().size(); i++) {
            String t = visit(ctx.fator_logico(i));
            if (!tiposCompativeis(tipo, t)) tipo = TIPO_INDEFINIDO;
        }
        return tipo;
    }

    /**
     * fator_logico → [nao] parcela_logica
     */
    @Override
    public String visitFator_logico(LAParser.Fator_logicoContext ctx) {
        return visit(ctx.parcela_logica());
    }

    /**
     * parcela_logica → (verdadeiro | falso) | exp_relacional
     */
    @Override
    public String visitParcela_logica(LAParser.Parcela_logicaContext ctx) {
        if (ctx.exp_relacional() != null) {
            return visit(ctx.exp_relacional());
        }
        return TIPO_LOGICO;
    }

    /**
     * exp_relacional → exp_aritmetica [op_relacional exp_aritmetica]
     * Resultado é logico se há operador relacional, senão propaga o tipo aritmético.
     */
    @Override
    public String visitExp_relacional(LAParser.Exp_relacionalContext ctx) {
        String tipo = visit(ctx.exp_aritmetica(0));
        if (ctx.exp_aritmetica().size() > 1) {
            visit(ctx.exp_aritmetica(1));
            return TIPO_LOGICO;
        }
        return tipo;
    }

    /**
     * exp_aritmetica → termo {op1 termo}
     * Propaga o tipo: real prevalece sobre inteiro; literal + qualquer != literal = indefinido.
     */
    @Override
    public String visitExp_aritmetica(LAParser.Exp_aritmeticaContext ctx) {
        String tipo = visit(ctx.termo(0));
        for (int i = 1; i < ctx.termo().size(); i++) {
            String t = visit(ctx.termo(i));
            tipo = combinarTipos(tipo, t);
        }
        return tipo;
    }

    /**
     * termo → fator {op2 fator}
     */
    @Override
    public String visitTermo(LAParser.TermoContext ctx) {
        String tipo = visit(ctx.fator(0));
        for (int i = 1; i < ctx.fator().size(); i++) {
            String t = visit(ctx.fator(i));
            tipo = combinarTipos(tipo, t);
        }
        return tipo;
    }

    /**
     * fator → parcela {op3 parcela}
     */
    @Override
    public String visitFator(LAParser.FatorContext ctx) {
        String tipo = visit(ctx.parcela(0));
        for (int i = 1; i < ctx.parcela().size(); i++) {
            String t = visit(ctx.parcela(i));
            tipo = combinarTipos(tipo, t);
        }
        return tipo;
    }

    /**
     * Combina dois tipos em uma operação aritmética.
     * - real + inteiro = real
     * - inteiro + inteiro = inteiro
     * - literal + literal = literal (concatenação)
     * - qualquer outro mix = indefinido
     */
    private String combinarTipos(String t1, String t2) {
        if (t1 == null || t2 == null) return TIPO_INDEFINIDO;
        if (t1.equals(TIPO_INDEFINIDO) || t2.equals(TIPO_INDEFINIDO)) return TIPO_INDEFINIDO;

        // Numérico
        boolean n1 = t1.equals(TIPO_INTEIRO) || t1.equals(TIPO_REAL);
        boolean n2 = t2.equals(TIPO_INTEIRO) || t2.equals(TIPO_REAL);
        if (n1 && n2) {
            return (t1.equals(TIPO_REAL) || t2.equals(TIPO_REAL)) ? TIPO_REAL : TIPO_INTEIRO;
        }

        // Literal + literal (concatenação com +)
        if (t1.equals(TIPO_LITERAL) && t2.equals(TIPO_LITERAL)) return TIPO_LITERAL;

        // Tipos incompatíveis
        return TIPO_INDEFINIDO;
    }

    /**
     * parcela → [op_unario] parcela_unario | parcela_nao_unario
     */
    @Override
    public String visitParcela(LAParser.ParcelaContext ctx) {
        if (ctx.parcela_unario() != null) {
            return visit(ctx.parcela_unario());
        }
        return visit(ctx.parcela_nao_unario());
    }

    /**
     * parcela_unario → [^] identificador
     *                | IDENT ( expressao {, expressao} )  ← chamada de função
     *                | NUM_INT
     *                | NUM_REAL
     *                | ( expressao )
     */
    @Override
    public String visitParcela_unario(LAParser.Parcela_unarioContext ctx) {
        // Número inteiro ou real
        if (ctx.NUM_INT() != null) return TIPO_INTEIRO;
        if (ctx.NUM_REAL() != null) return TIPO_REAL;

        // Subexpressão entre parênteses: ( expressao )
        // Gramática: '(' expressao ')' — IDENT é null nesse caso
        if (ctx.IDENT() == null && ctx.expressao() != null && !ctx.expressao().isEmpty()) {
            return visit(ctx.expressao(0));
        }

        // Chamada de função: IDENT ( expressao {, expressao} )
        if (ctx.IDENT() != null && ctx.expressao() != null && !ctx.expressao().isEmpty()) {
            Token ident = ctx.IDENT().getSymbol();
            EntradaTabelaDeSimbolos entrada = tabela.buscar(ident.getText());
            if (entrada == null) {
                erroNaoDeclarado(ident);
                return TIPO_INDEFINIDO;
            }
            for (LAParser.ExpressaoContext expr : ctx.expressao()) {
                visit(expr);
            }
            return entrada.getTipo();
        }

        // Identificador simples (com possível ^ de desreferência)
        if (ctx.identificador() != null) {
            Token ident = ctx.identificador().IDENT(0).getSymbol();
            EntradaTabelaDeSimbolos entrada = tabela.buscar(ident.getText());
            if (entrada == null) {
                erroNaoDeclarado(ident);
                return TIPO_INDEFINIDO;
            }
            return entrada.getTipo();
        }

        return TIPO_INDEFINIDO;
    }

    /**
     * parcela_nao_unario → & identificador | CADEIA
     */
    @Override
    public String visitParcela_nao_unario(LAParser.Parcela_nao_unarioContext ctx) {
        if (ctx.CADEIA() != null) return TIPO_LITERAL;

        if (ctx.identificador() != null) {
            Token ident = ctx.identificador().IDENT(0).getSymbol();
            EntradaTabelaDeSimbolos entrada = tabela.buscar(ident.getText());
            if (entrada == null) {
                erroNaoDeclarado(ident);
                return TIPO_INDEFINIDO;
            }
            return "^" + entrada.getTipo();
        }

        return TIPO_INDEFINIDO;
    }

    // -----------------------------------------------------------------------
    // VISITOR: Seleção (caso/seja)
    // -----------------------------------------------------------------------

    @Override
    public String visitSelecao(LAParser.SelecaoContext ctx) {
        for (LAParser.Item_selecaoContext item : ctx.item_selecao()) {
            visit(item);
        }
        return null;
    }

    @Override
    public String visitItem_selecao(LAParser.Item_selecaoContext ctx) {
        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visit(cmd);
        }
        return null;
    }
}
