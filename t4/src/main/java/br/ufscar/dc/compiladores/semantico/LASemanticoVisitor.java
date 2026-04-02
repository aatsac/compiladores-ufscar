package br.ufscar.dc.compiladores.semantico;

import org.antlr.v4.runtime.Token;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Visitor semântico da linguagem LA — T4.
 *
 * Verifica os seguintes erros semânticos:
 *  1. Identificador já declarado no mesmo escopo
 *  2. Tipo não declarado
 *  3. Identificador não declarado (incluindo campos de registro e índices de vetor)
 *  4. Atribuição incompatível com o tipo declarado (incluindo ponteiros e registros)
 *  5. Incompatibilidade de argumentos em chamadas (número e tipo)
 *  6. Uso de 'retorne' fora de função
 *
 * Não interrompe ao primeiro erro — reporta todos os erros encontrados.
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

    /**
     * Flag: indica se o escopo atual permite 'retorne'.
     * true somente dentro de funções (não em procedimentos ou escopo global).
     */
    private boolean dentroFuncao = false;

    public LASemanticoVisitor(PrintWriter writer) {
        this.tabela = new TabelaDeSimbolos();
        this.writer = writer;
    }

    // -----------------------------------------------------------------------
    // Emissão de erros
    // -----------------------------------------------------------------------

    private void erroJaDeclarado(Token token) {
        writer.println("Linha " + token.getLine()
                + ": identificador " + token.getText() + " ja declarado anteriormente");
    }

    private void erroTipoNaoDeclarado(Token token, String nomeTipo) {
        writer.println("Linha " + token.getLine()
                + ": tipo " + nomeTipo + " nao declarado");
    }

    private void erroNaoDeclarado(Token token, String nome) {
        writer.println("Linha " + token.getLine()
                + ": identificador " + nome + " nao declarado");
    }

    private void erroIncompatibilidadeParametros(Token token) {
        writer.println("Linha " + token.getLine()
                + ": incompatibilidade de parametros na chamada de " + token.getText());
    }

    private void erroRetorneNaoPermitido(Token token) {
        writer.println("Linha " + token.getLine()
                + ": comando retorne nao permitido nesse escopo");
    }

    // -----------------------------------------------------------------------
    // Resolução de tipos
    // -----------------------------------------------------------------------

    /**
     * Resolve o tipo de um tipo_estendido.
     * Propaga o prefixo '^' para indicar tipo ponteiro.
     */
    private String resolverTipoEstendido(LAParser.Tipo_estendidoContext ctx) {
        LAParser.Tipo_basico_identContext tbi = ctx.tipo_basico_ident();
        String base;

        if (tbi.tipo_basico() != null) {
            base = tbi.tipo_basico().getText();
        } else {
            String nomeIdent = tbi.IDENT().getText();
            EntradaTabelaDeSimbolos entrada = tabela.buscar(nomeIdent);
            if (entrada == null || entrada.getCategoria() != EntradaTabelaDeSimbolos.Categoria.TIPO) {
                erroTipoNaoDeclarado(tbi.IDENT().getSymbol(), nomeIdent);
                return TIPO_INDEFINIDO;
            }
            base = nomeIdent;
        }

        boolean ehPonteiro = !ctx.getText().startsWith(base);
        return ehPonteiro ? "^" + base : base;
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
     * Verifica compatibilidade para ATRIBUIÇÃO:
     *  - numérico ← numérico (inteiro/real intercambiáveis)
     *  - literal ← literal
     *  - logico ← logico
     *  - mesmo tipo de registro/ponteiro
     */
    private boolean tiposCompativeis(String tipoEsq, String tipoDir) {
        if (tipoEsq == null || tipoDir == null) return false;
        if (tipoEsq.equals(TIPO_INDEFINIDO) || tipoDir.equals(TIPO_INDEFINIDO)) return false;

        boolean esqNum = tipoEsq.equals(TIPO_INTEIRO) || tipoEsq.equals(TIPO_REAL);
        boolean dirNum = tipoDir.equals(TIPO_INTEIRO) || tipoDir.equals(TIPO_REAL);
        if (esqNum && dirNum) return true;

        return tipoEsq.equals(tipoDir);
    }

    /**
     * Verifica compatibilidade para PARÂMETROS (mais estrita):
     *  - inteiro → inteiro (apenas)
     *  - real → real (apenas)
     *  - demais: mesmo tipo exato
     */
    private boolean tiposCompativeisParametro(String tipoFormal, String tipoReal) {
        if (tipoFormal == null || tipoReal == null) return false;
        if (tipoFormal.equals(TIPO_INDEFINIDO) || tipoReal.equals(TIPO_INDEFINIDO)) return false;
        return tipoFormal.equals(tipoReal);
    }

    /**
     * Combina tipos em operações aritméticas.
     * real prevalece sobre inteiro; literal+literal=literal; outros = indefinido.
     */
    private String combinarTipos(String t1, String t2) {
        if (t1 == null || t2 == null) return TIPO_INDEFINIDO;
        if (t1.equals(TIPO_INDEFINIDO) || t2.equals(TIPO_INDEFINIDO)) return TIPO_INDEFINIDO;

        boolean n1 = t1.equals(TIPO_INTEIRO) || t1.equals(TIPO_REAL);
        boolean n2 = t2.equals(TIPO_INTEIRO) || t2.equals(TIPO_REAL);
        if (n1 && n2) {
            return (t1.equals(TIPO_REAL) || t2.equals(TIPO_REAL)) ? TIPO_REAL : TIPO_INTEIRO;
        }
        if (t1.equals(TIPO_LITERAL) && t2.equals(TIPO_LITERAL)) return TIPO_LITERAL;
        return TIPO_INDEFINIDO;
    }

    // -----------------------------------------------------------------------
    // Resolução de identificador composto (ident.campo, ident[i])
    // -----------------------------------------------------------------------

    /**
     * Monta o nome completo de um identificador composto (ex: "ponto1.x", "valor[0]").
     * Usado para mensagens de erro mais precisas.
     */
    private String nomeCompleto(LAParser.IdentificadorContext ctx) {
        StringBuilder sb = new StringBuilder(ctx.IDENT(0).getText());
        for (int i = 1; i < ctx.IDENT().size(); i++) {
            sb.append(".").append(ctx.IDENT(i).getText());
        }
        return sb.toString();
    }

    /**
     * Resolve o tipo de um identificador composto.
     * Para identificadores simples (sem '.'), busca diretamente na tabela.
     * Para identificadores com campo (ex: ponto1.x), retorna o tipo do campo.
     * Para vetores (ex: valor[i]), retorna o tipo do elemento.
     *
     * Emite erro se o identificador ou campo não estiver declarado.
     *
     * @param ctx   contexto do identificador
     * @param token token para localização do erro (linha)
     * @return tipo resolvido, ou TIPO_INDEFINIDO se não declarado
     */
    private String resolverTipoIdentificador(LAParser.IdentificadorContext ctx, Token token) {
        String nomeBase = ctx.IDENT(0).getText();
        EntradaTabelaDeSimbolos entrada = tabela.buscar(nomeBase);

        if (entrada == null) {
            erroNaoDeclarado(token, nomeCompleto(ctx));
            return TIPO_INDEFINIDO;
        }

        // Identificador com campo de registro (ex: ponto1.x)
        if (ctx.IDENT().size() > 1) {
            String nomeCampo = ctx.IDENT(1).getText();
            // Busca a tabela de campos deste registro
            String nomeCampoCompleto = nomeBase + "." + nomeCampo;
            EntradaTabelaDeSimbolos campo = tabela.buscar(nomeCampoCompleto);
            if (campo == null) {
                erroNaoDeclarado(token, nomeCampoCompleto);
                return TIPO_INDEFINIDO;
            }
            return campo.getTipo();
        }

        // Vetor: o tipo é o mesmo da variável (índice não muda o tipo)
        return entrada.getTipo();
    }

    // -----------------------------------------------------------------------
    // VISITOR: Declarações locais
    // -----------------------------------------------------------------------

    /**
     * Processa declarações locais:
     *  - declare variavel
     *  - constante IDENT : tipo_basico = valor_constante
     *  - tipo IDENT : tipo (com registro — registra também os campos)
     */
    @Override
    public String visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        if (ctx.variavel() != null) {
            visitVariavelDeclaracao(ctx.variavel(), null);

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
                String tipoResolvido = resolverTipo(ctx.tipo());
                tabela.adicionar(new EntradaTabelaDeSimbolos(
                        ident.getText(),
                        EntradaTabelaDeSimbolos.Categoria.TIPO,
                        tipoResolvido));
                // Se o tipo é um registro, registra os campos na tabela
                if (ctx.tipo().registro() != null) {
                    registrarCamposRegistro(ident.getText(), ctx.tipo().registro());
                }
            }
        }
        return null;
    }

    /**
     * Processa a declaração de variável.
     * Se 'prefixo' for fornecido, os campos são registrados como "prefixo.campo"
     * (usado para registros: ex. ponto1.x, ponto1.y).
     */
    private void visitVariavelDeclaracao(LAParser.VariavelContext ctx, String prefixo) {
        String tipoResolvido = resolverTipo(ctx.tipo());

        for (LAParser.IdentificadorContext identCtx : ctx.identificador()) {
            Token ident = identCtx.IDENT(0).getSymbol();
            String nomeRegistro = prefixo != null
                    ? prefixo + "." + ident.getText()
                    : ident.getText();

            if (tabela.existeNoEscopoAtual(nomeRegistro)) {
                erroJaDeclarado(ident);
            } else {
                tabela.adicionar(new EntradaTabelaDeSimbolos(
                        nomeRegistro,
                        EntradaTabelaDeSimbolos.Categoria.VARIAVEL,
                        tipoResolvido));
            }

            // Se o tipo é um registro, registra os campos como "nomeRegistro.campo"
            if (ctx.tipo().registro() != null) {
                registrarCamposRegistro(nomeRegistro, ctx.tipo().registro());
            }

            // Se o tipo é um tipo definido pelo usuário (registro), expande os campos
            if (ctx.tipo().tipo_estendido() != null
                    && ctx.tipo().tipo_estendido().tipo_basico_ident() != null
                    && ctx.tipo().tipo_estendido().tipo_basico_ident().IDENT() != null) {
                String nomeTipo = ctx.tipo().tipo_estendido().tipo_basico_ident().IDENT().getText();
                expandirCamposTipo(nomeRegistro, nomeTipo);
            }
        }
    }

    /**
     * Registra os campos de um registro na tabela com o prefixo do dono.
     * Ex: para "ponto1" do tipo registro{x,y: real}, registra "ponto1.x" e "ponto1.y".
     */
    private void registrarCamposRegistro(String prefixo, LAParser.RegistroContext ctx) {
        for (LAParser.VariavelContext varCtx : ctx.variavel()) {
            String tipoCampo = resolverTipo(varCtx.tipo());
            for (LAParser.IdentificadorContext identCtx : varCtx.identificador()) {
                String nomeCampo = prefixo + "." + identCtx.IDENT(0).getText();
                if (!tabela.existeNoEscopoAtual(nomeCampo)) {
                    tabela.adicionar(new EntradaTabelaDeSimbolos(
                            nomeCampo,
                            EntradaTabelaDeSimbolos.Categoria.VARIAVEL,
                            tipoCampo));
                }
            }
        }
    }

    /**
     * Expande os campos de um tipo definido pelo usuário para uma variável.
     * Ex: se "tVinho" tem campos {nome: literal, preco: real},
     * e declaramos "vinho: tVinho", registra "vinho.nome" e "vinho.preco".
     */
    private void expandirCamposTipo(String nomeVar, String nomeTipo) {
        EntradaTabelaDeSimbolos entradaTipo = tabela.buscar(nomeTipo);
        if (entradaTipo == null) return;

        // Busca todos os campos do tipo na tabela (registrados como "nomeTipo.campo")
        // Percorre a tabela procurando entradas que comecem com "nomeTipo."
        List<EntradaTabelaDeSimbolos> campos = tabela.buscarCampos(nomeTipo);
        for (EntradaTabelaDeSimbolos campo : campos) {
            // Remove o prefixo do tipo e substitui pelo prefixo da variável
            String sufixo = campo.getNome().substring(nomeTipo.length()); // ".campo"
            String nomeCampoVar = nomeVar + sufixo;
            if (!tabela.existeNoEscopoAtual(nomeCampoVar)) {
                tabela.adicionar(new EntradaTabelaDeSimbolos(
                        nomeCampoVar,
                        EntradaTabelaDeSimbolos.Categoria.VARIAVEL,
                        campo.getTipo()));
            }
        }
    }

    // -----------------------------------------------------------------------
    // VISITOR: Declarações globais (procedimentos e funções)
    // -----------------------------------------------------------------------

    /**
     * Processa procedimento ou função:
     *  - Coleta parâmetros formais
     *  - Registra no escopo externo com lista de parâmetros
     *  - Cria escopo interno, visita corpo e destrói escopo
     *  - Controla flag dentroFuncao para verificação do 'retorne'
     */
    @Override
    public String visitDeclaracao_global(LAParser.Declaracao_globalContext ctx) {
        Token ident = ctx.IDENT().getSymbol();
        boolean ehFuncao = ctx.getChild(0).getText().equals("funcao");

        String tipoRetorno = ehFuncao
                ? resolverTipoEstendido(ctx.tipo_estendido())
                : "procedimento";

        // Coleta tipos dos parâmetros formais
        List<String> tiposParametros = new ArrayList<>();
        if (ctx.parametros() != null) {
            for (LAParser.ParametroContext param : ctx.parametros().parametro()) {
                String tipoParam = resolverTipoEstendido(param.tipo_estendido());
                for (int i = 0; i < param.identificador().size(); i++) {
                    tiposParametros.add(tipoParam);
                }
            }
        }

        // Registra no escopo externo
        if (tabela.existeNoEscopoAtual(ident.getText())) {
            erroJaDeclarado(ident);
        } else {
            tabela.adicionar(new EntradaTabelaDeSimbolos(
                    ident.getText(),
                    ehFuncao ? EntradaTabelaDeSimbolos.Categoria.FUNCAO
                             : EntradaTabelaDeSimbolos.Categoria.PROCEDIMENTO,
                    tipoRetorno,
                    tiposParametros));
        }

        // Cria escopo interno
        tabela.criarEscopo();
        boolean dentroFuncaoAnterior = dentroFuncao;
        dentroFuncao = ehFuncao;

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
                        // Se o parâmetro é de tipo registro, expande os campos
                        if (ctx.parametros() != null) {
                            String tipoParamStr = resolverTipoEstendido(param.tipo_estendido());
                            expandirCamposTipo(tParam.getText(), tipoParamStr);
                        }
                    }
                }
            }
        }

        for (LAParser.Declaracao_localContext dl : ctx.declaracao_local()) {
            visitDeclaracao_local(dl);
        }
        for (LAParser.CmdContext cmd : ctx.cmd()) {
            visit(cmd);
        }

        dentroFuncao = dentroFuncaoAnterior;
        tabela.destruirEscopo();
        return null;
    }

    // -----------------------------------------------------------------------
    // VISITOR: Comandos
    // -----------------------------------------------------------------------

    @Override
    public String visitCmdLeia(LAParser.CmdLeiaContext ctx) {
        for (LAParser.IdentificadorContext identCtx : ctx.identificador()) {
            Token t = identCtx.IDENT(0).getSymbol();
            resolverTipoIdentificador(identCtx, t);
        }
        return null;
    }

    @Override
    public String visitCmdEscreva(LAParser.CmdEscrevaContext ctx) {
        for (LAParser.ExpressaoContext expr : ctx.expressao()) {
            visit(expr);
        }
        return null;
    }

    /**
     * Atribuição: [^] identificador <- expressao
     *
     * Casos especiais:
     *  - ^ponteiro <- expr: desreferência, tipo esperado = tipo base do ponteiro
     *  - registro.campo <- expr: verifica o tipo do campo
     *  - vetor[i] <- expr: verifica o tipo do elemento
     */
    @Override
    public String visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        LAParser.IdentificadorContext identCtx = ctx.identificador();
        Token tokenInicio = ctx.getStart();
        boolean temCircunflexo = tokenInicio.getText().equals("^");

        // Prefixo "^" incluído no nome da mensagem quando é desreferência (ex: "^ponteiro")
        String prefixoCirc = temCircunflexo ? "^" : "";

        // Monta o nome completo para a mensagem de erro
        // dimensao() é singular na gramática — ANTLR gera método simples, não lista
        String nomeCompleto = prefixoCirc + nomeCompleto(identCtx);
        LAParser.DimensaoContext dim = identCtx.dimensao();
        if (dim != null
                && dim.exp_aritmetica() != null
                && !dim.exp_aritmetica().isEmpty()) {
            nomeCompleto = prefixoCirc + identCtx.IDENT(0).getText()
                    + "[" + dim.exp_aritmetica(0).getText() + "]";
        }

        Token identToken = identCtx.IDENT(0).getSymbol();
        String nomeBase = identCtx.IDENT(0).getText();

        // Verifica se o identificador base foi declarado
        EntradaTabelaDeSimbolos entrada = tabela.buscar(nomeBase);
        if (entrada == null) {
            erroNaoDeclarado(identToken, nomeCompleto);
            visit(ctx.expressao());
            return null;
        }

        // Resolve o tipo do lado esquerdo
        String tipoEsq;
        if (identCtx.IDENT().size() > 1) {
            // Campo de registro: verifica se o campo existe
            String nomeCampo = nomeBase + "." + identCtx.IDENT(1).getText();
            EntradaTabelaDeSimbolos campo = tabela.buscar(nomeCampo);
            if (campo == null) {
                erroNaoDeclarado(identToken, nomeCompleto);
                visit(ctx.expressao());
                return null;
            }
            tipoEsq = campo.getTipo();
        } else if (temCircunflexo) {
            // ^ponteiro: desreferência — tipo esperado é o tipo base do ponteiro
            String tipo = entrada.getTipo();
            tipoEsq = tipo.startsWith("^") ? tipo.substring(1) : tipo;
        } else {
            tipoEsq = entrada.getTipo();
        }

        String tipoDir = visit(ctx.expressao());

        if (!tiposCompativeis(tipoEsq, tipoDir)) {
            Token tokenErro = identCtx.IDENT(0).getSymbol();
            writer.println("Linha " + tokenErro.getLine()
                    + ": atribuicao nao compativel para " + nomeCompleto);
        }

        return null;
    }

    /**
     * Chamada de procedimento: IDENT(expressao, ...)
     * Verifica declaração e compatibilidade dos argumentos.
     */
    @Override
    public String visitCmdChamada(LAParser.CmdChamadaContext ctx) {
        Token ident = ctx.IDENT().getSymbol();
        EntradaTabelaDeSimbolos entrada = tabela.buscar(ident.getText());

        if (entrada == null) {
            erroNaoDeclarado(ident, ident.getText());
            for (LAParser.ExpressaoContext expr : ctx.expressao()) visit(expr);
            return null;
        }

        List<String> tiposArgs = new ArrayList<>();
        for (LAParser.ExpressaoContext expr : ctx.expressao()) {
            tiposArgs.add(visit(expr));
        }
        verificarParametros(ident, entrada.getParametros(), tiposArgs);
        return null;
    }

    @Override
    public String visitCmdSe(LAParser.CmdSeContext ctx) {
        visit(ctx.expressao());
        for (LAParser.CmdContext cmd : ctx.cmd()) visit(cmd);
        return null;
    }

    @Override
    public String visitCmdEnquanto(LAParser.CmdEnquantoContext ctx) {
        visit(ctx.expressao());
        for (LAParser.CmdContext cmd : ctx.cmd()) visit(cmd);
        return null;
    }

    @Override
    public String visitCmdPara(LAParser.CmdParaContext ctx) {
        Token ident = ctx.IDENT().getSymbol();
        if (tabela.buscar(ident.getText()) == null) {
            erroNaoDeclarado(ident, ident.getText());
        }
        for (LAParser.Exp_aritmeticaContext exp : ctx.exp_aritmetica()) visit(exp);
        for (LAParser.CmdContext cmd : ctx.cmd()) visit(cmd);
        return null;
    }

    @Override
    public String visitCmdFaca(LAParser.CmdFacaContext ctx) {
        for (LAParser.CmdContext cmd : ctx.cmd()) visit(cmd);
        visit(ctx.expressao());
        return null;
    }

    @Override
    public String visitCmdCaso(LAParser.CmdCasoContext ctx) {
        visit(ctx.exp_aritmetica());
        visit(ctx.selecao());
        for (LAParser.CmdContext cmd : ctx.cmd()) visit(cmd);
        return null;
    }

    /**
     * retorne expressao — erro se não estiver dentro de uma função.
     */
    @Override
    public String visitCmdRetorne(LAParser.CmdRetorneContext ctx) {
        if (!dentroFuncao) {
            erroRetorneNaoPermitido(ctx.getStart());
        }
        return visit(ctx.expressao());
    }

    // -----------------------------------------------------------------------
    // Verificação de parâmetros
    // -----------------------------------------------------------------------

    /**
     * Verifica número e tipos dos argumentos contra os parâmetros formais.
     * Para parâmetros, inteiro e real NÃO são intercambiáveis.
     */
    private void verificarParametros(Token ident, List<String> formais, List<String> reais) {
        if (formais.size() != reais.size()) {
            erroIncompatibilidadeParametros(ident);
            return;
        }
        for (int i = 0; i < formais.size(); i++) {
            if (!tiposCompativeisParametro(formais.get(i), reais.get(i))) {
                erroIncompatibilidadeParametros(ident);
                return;
            }
        }
    }

    // -----------------------------------------------------------------------
    // VISITOR: Expressões
    // -----------------------------------------------------------------------

    @Override
    public String visitExpressao(LAParser.ExpressaoContext ctx) {
        String tipo = visit(ctx.termo_logico(0));
        for (int i = 1; i < ctx.termo_logico().size(); i++) {
            String t = visit(ctx.termo_logico(i));
            if (!tiposCompativeis(tipo, t)) tipo = TIPO_INDEFINIDO;
        }
        return tipo;
    }

    @Override
    public String visitTermo_logico(LAParser.Termo_logicoContext ctx) {
        String tipo = visit(ctx.fator_logico(0));
        for (int i = 1; i < ctx.fator_logico().size(); i++) {
            String t = visit(ctx.fator_logico(i));
            if (!tiposCompativeis(tipo, t)) tipo = TIPO_INDEFINIDO;
        }
        return tipo;
    }

    @Override
    public String visitFator_logico(LAParser.Fator_logicoContext ctx) {
        return visit(ctx.parcela_logica());
    }

    @Override
    public String visitParcela_logica(LAParser.Parcela_logicaContext ctx) {
        if (ctx.exp_relacional() != null) return visit(ctx.exp_relacional());
        return TIPO_LOGICO;
    }

    @Override
    public String visitExp_relacional(LAParser.Exp_relacionalContext ctx) {
        String tipo = visit(ctx.exp_aritmetica(0));
        if (ctx.exp_aritmetica().size() > 1) {
            visit(ctx.exp_aritmetica(1));
            return TIPO_LOGICO;
        }
        return tipo;
    }

    @Override
    public String visitExp_aritmetica(LAParser.Exp_aritmeticaContext ctx) {
        String tipo = visit(ctx.termo(0));
        for (int i = 1; i < ctx.termo().size(); i++) {
            tipo = combinarTipos(tipo, visit(ctx.termo(i)));
        }
        return tipo;
    }

    @Override
    public String visitTermo(LAParser.TermoContext ctx) {
        String tipo = visit(ctx.fator(0));
        for (int i = 1; i < ctx.fator().size(); i++) {
            tipo = combinarTipos(tipo, visit(ctx.fator(i)));
        }
        return tipo;
    }

    @Override
    public String visitFator(LAParser.FatorContext ctx) {
        String tipo = visit(ctx.parcela(0));
        for (int i = 1; i < ctx.parcela().size(); i++) {
            tipo = combinarTipos(tipo, visit(ctx.parcela(i)));
        }
        return tipo;
    }

    @Override
    public String visitParcela(LAParser.ParcelaContext ctx) {
        if (ctx.parcela_unario() != null) return visit(ctx.parcela_unario());
        return visit(ctx.parcela_nao_unario());
    }

    /**
     * parcela_unario → [^] identificador
     *                | IDENT ( expressao {, expressao} )
     *                | NUM_INT | NUM_REAL
     *                | ( expressao )
     */
    @Override
    public String visitParcela_unario(LAParser.Parcela_unarioContext ctx) {
        if (ctx.NUM_INT() != null) return TIPO_INTEIRO;
        if (ctx.NUM_REAL() != null) return TIPO_REAL;

        // Subexpressão: ( expressao )
        if (ctx.IDENT() == null && ctx.expressao() != null && !ctx.expressao().isEmpty()) {
            return visit(ctx.expressao(0));
        }

        // Chamada de função em expressão: IDENT ( expressao {, expressao} )
        if (ctx.IDENT() != null && ctx.expressao() != null && !ctx.expressao().isEmpty()) {
            Token ident = ctx.IDENT().getSymbol();
            EntradaTabelaDeSimbolos entrada = tabela.buscar(ident.getText());
            if (entrada == null) {
                erroNaoDeclarado(ident, ident.getText());
                return TIPO_INDEFINIDO;
            }
            List<String> tiposArgs = new ArrayList<>();
            for (LAParser.ExpressaoContext expr : ctx.expressao()) {
                tiposArgs.add(visit(expr));
            }
            verificarParametros(ident, entrada.getParametros(), tiposArgs);
            return entrada.getTipo();
        }

        // Identificador simples ou com campo de registro
        if (ctx.identificador() != null) {
            LAParser.IdentificadorContext identCtx = ctx.identificador();
            Token ident = identCtx.IDENT(0).getSymbol();
            String tipo = resolverTipoIdentificador(identCtx, ident);

            // Se tem '^', desreferencia o ponteiro
            boolean temCircunflexo = ctx.getChild(0).getText().equals("^");
            if (temCircunflexo && tipo.startsWith("^")) {
                return tipo.substring(1);
            }
            return tipo;
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
                erroNaoDeclarado(ident, ident.getText());
                return TIPO_INDEFINIDO;
            }
            return "^" + entrada.getTipo();
        }
        return TIPO_INDEFINIDO;
    }

    // -----------------------------------------------------------------------
    // VISITOR: Seleção
    // -----------------------------------------------------------------------

    @Override
    public String visitSelecao(LAParser.SelecaoContext ctx) {
        for (LAParser.Item_selecaoContext item : ctx.item_selecao()) visit(item);
        return null;
    }

    @Override
    public String visitItem_selecao(LAParser.Item_selecaoContext ctx) {
        for (LAParser.CmdContext cmd : ctx.cmd()) visit(cmd);
        return null;
    }
}
