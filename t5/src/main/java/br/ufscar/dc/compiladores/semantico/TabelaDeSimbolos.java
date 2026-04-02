package br.ufscar.dc.compiladores.semantico;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tabela de símbolos da linguagem LA.
 *
 * Implementa um modelo de escopos aninhados (pilha de escopos):
 *  - O escopo global é criado ao iniciar a compilação
 *  - Cada subprograma (procedimento/função) cria um novo escopo ao entrar
 *    e o destrói ao sair
 *
 * Cada escopo é um mapa de nome → EntradaTabelaDeSimbolos, preservando
 * a ordem de inserção (LinkedHashMap).
 */
public class TabelaDeSimbolos {

    /**
     * Representa um escopo (tabela de símbolos de um bloco).
     */
    public class Escopo {

        /** Mapa de identificador → símbolo neste escopo. */
        private final Map<String, EntradaTabelaDeSimbolos> simbolos = new LinkedHashMap<>();

        /**
         * Adiciona um símbolo ao escopo atual.
         *
         * @param entrada símbolo a ser inserido
         */
        public void adicionar(EntradaTabelaDeSimbolos entrada) {
            simbolos.put(entrada.getNome(), entrada);
        }

        /**
         * Verifica se um identificador já existe neste escopo.
         *
         * @param nome identificador a verificar
         * @return true se já declarado no escopo atual
         */
        public boolean existe(String nome) {
            return simbolos.containsKey(nome);
        }

        /**
         * Busca um símbolo pelo nome neste escopo.
         *
         * @param nome identificador
         * @return a entrada correspondente, ou null se não encontrada
         */
        public EntradaTabelaDeSimbolos buscar(String nome) {
            return simbolos.get(nome);
        }
    }

    /** Pilha de escopos ativos (índice 0 = global, último = mais interno). */
    private final List<Escopo> escopos = new ArrayList<>();

    /**
     * Cria a tabela de símbolos com o escopo global já aberto.
     */
    public TabelaDeSimbolos() {
        criarEscopo(); // escopo global
    }

    /** Cria (empilha) um novo escopo. */
    public void criarEscopo() {
        escopos.add(new Escopo());
    }

    /** Destrói (desempilha) o escopo mais interno. */
    public void destruirEscopo() {
        if (escopos.size() > 1) {
            escopos.remove(escopos.size() - 1);
        }
    }

    /**
     * Adiciona um símbolo no escopo mais interno (atual).
     *
     * @param entrada símbolo a ser inserido
     */
    public void adicionar(EntradaTabelaDeSimbolos entrada) {
        escopoAtual().adicionar(entrada);
    }

    /**
     * Verifica se um identificador já está declarado no escopo atual (não busca em escopos externos).
     * Usado para detectar declarações duplicadas no mesmo escopo.
     *
     * @param nome identificador
     * @return true se já declarado no escopo atual
     */
        public boolean existeNoEscopoAtual(String nome) {
        return escopoAtual().existe(nome);
    }

    /**
     * Busca um símbolo em todos os escopos, do mais interno para o mais externo.
     * Retorna o primeiro encontrado.
     *
     * @param nome identificador
     * @return a entrada encontrada, ou null se não declarado em nenhum escopo
     */
    public EntradaTabelaDeSimbolos buscar(String nome) {
        for (int i = escopos.size() - 1; i >= 0; i--) {
            EntradaTabelaDeSimbolos entrada = escopos.get(i).buscar(nome);
            if (entrada != null) return entrada;
        }
        return null;
    }

    /**
     * Busca todos os campos de um tipo/variável registrado como "prefixo.campo".
     * Usado para expandir campos de registros ao declarar variáveis de um tipo composto.
     *
     * Ex: buscarCampos("tVinho") retorna as entradas "tVinho.nome", "tVinho.preco", etc.
     *
     * @param prefixo nome do tipo ou variável cujos campos se deseja buscar
     * @return lista de entradas cujo nome começa com "prefixo."
     */
    public List<EntradaTabelaDeSimbolos> buscarCampos(String prefixo) {
        List<EntradaTabelaDeSimbolos> campos = new ArrayList<>();
        String prefixoPonto = prefixo + ".";
        // Busca em todos os escopos, do mais externo ao mais interno
        for (Escopo escopo : escopos) {
            for (EntradaTabelaDeSimbolos entrada : escopo.simbolos.values()) {
                if (entrada.getNome().startsWith(prefixoPonto)) {
                    campos.add(entrada);
                }
            }
        }
        return campos;
    }

    /** @return o escopo mais interno (ativo no momento) */
    private Escopo escopoAtual() {
        return escopos.get(escopos.size() - 1);
    }
}
