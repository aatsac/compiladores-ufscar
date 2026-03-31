// ============================================================
// Gramática da Linguagem Algorítmica (LA) para ANTLR4
// Disciplina: Construção de Compiladores - DC/UFSCar
// Autor da linguagem LA: prof. Jander Moreira
// Autores da gramática: Prof. Daniel Lucrédio e Profa. Helena de Medeiros Caseli
//
// Esta gramática foi transcrita fielmente do documento oficial (ago/2018).
// ============================================================

grammar LA;

// ============================================================
// REGRA INICIAL
// programa → declaracoes "algoritmo" corpo "fim_algoritmo"
// ============================================================
programa
    : declaracoes 'algoritmo' corpo 'fim_algoritmo' EOF
    ;

// ============================================================
// DECLARAÇÕES
// declaracoes → {decl_local_global}
// ============================================================
declaracoes
    : decl_local_global*
    ;

// decl_local_global → declaracao_local | declaracao_global
decl_local_global
    : declaracao_local
    | declaracao_global
    ;

// declaracao_local → "declare" variavel
//                  | "constante" IDENT ":" tipo_basico "=" valor_constante
//                  | "tipo" IDENT ":" tipo
declaracao_local
    : 'declare' variavel
    | 'constante' IDENT ':' tipo_basico '=' valor_constante
    | 'tipo' IDENT ':' tipo
    ;

// ============================================================
// VARIÁVEL E IDENTIFICADOR
// variavel → identificador {"," identificador} ":" tipo
// identificador → IDENT {"." IDENT} dimensao
// dimensao → {"[" exp_aritmetica "]"}
// ============================================================
variavel
    : identificador (',' identificador)* ':' tipo
    ;

identificador
    : IDENT ('.' IDENT)* dimensao
    ;

dimensao
    : ('[' exp_aritmetica ']')*
    ;

// ============================================================
// TIPOS
// tipo → registro | tipo_estendido
// tipo_basico → "literal" | "inteiro" | "real" | "logico"
// tipo_basico_ident → tipo_basico | IDENT
// tipo_estendido → ["^"] tipo_basico_ident
// ============================================================
tipo
    : registro
    | tipo_estendido
    ;

tipo_basico
    : 'literal'
    | 'inteiro'
    | 'real'
    | 'logico'
    ;

tipo_basico_ident
    : tipo_basico
    | IDENT
    ;

tipo_estendido
    : '^'? tipo_basico_ident
    ;

// valor_constante → CADEIA | NUM_INT | NUM_REAL | "verdadeiro" | "falso"
valor_constante
    : CADEIA
    | NUM_INT
    | NUM_REAL
    | 'verdadeiro'
    | 'falso'
    ;

// registro → "registro" {variavel} "fim_registro"
registro
    : 'registro' variavel+ 'fim_registro'
    ;

// ============================================================
// DECLARAÇÕES GLOBAIS (subprogramas)
// declaracao_global → "procedimento" IDENT "(" [parametros] ")"
//                         {declaracao_local} {cmd} "fim_procedimento"
//                   | "funcao" IDENT "(" [parametros] ")" ":" tipo_estendido
//                         {declaracao_local} {cmd} "fim_funcao"
// ============================================================
declaracao_global
    : 'procedimento' IDENT '(' parametros? ')' declaracao_local* cmd* 'fim_procedimento'
    | 'funcao'       IDENT '(' parametros? ')' ':' tipo_estendido declaracao_local* cmd* 'fim_funcao'
    ;

// parametro → ["var"] identificador {"," identificador} ":" tipo_estendido
// parametros → parametro {"," parametro}
parametro
    : 'var'? identificador (',' identificador)* ':' tipo_estendido
    ;

parametros
    : parametro (',' parametro)*
    ;

// ============================================================
// CORPO
// corpo → {declaracao_local} {cmd}
// ============================================================
corpo
    : declaracao_local* cmd*
    ;

// ============================================================
// COMANDOS
// cmd → cmdLeia | cmdEscreva | cmdSe | cmdCaso | cmdPara
//     | cmdEnquanto | cmdFaca | cmdAtribuicao | cmdChamada | cmdRetorne
// ============================================================
cmd
    : cmdLeia
    | cmdEscreva
    | cmdSe
    | cmdCaso
    | cmdPara
    | cmdEnquanto
    | cmdFaca
    | cmdAtribuicao
    | cmdChamada
    | cmdRetorne
    ;

// cmdLeia → "leia" "(" ["^"] identificador {"," ["^"] identificador} ")"
cmdLeia
    : 'leia' '(' '^'? identificador (',' '^'? identificador)* ')'
    ;

// cmdEscreva → "escreva" "(" expressao {"," expressao} ")"
cmdEscreva
    : 'escreva' '(' expressao (',' expressao)* ')'
    ;

// cmdSe → "se" expressao "entao" {cmd} ["senao" {cmd}] "fim_se"
cmdSe
    : 'se' expressao 'entao' cmd* ('senao' cmd*)? 'fim_se'
    ;

// cmdCaso → "caso" exp_aritmetica "seja" selecao ["senao" {cmd}] "fim_caso"
cmdCaso
    : 'caso' exp_aritmetica 'seja' selecao ('senao' cmd*)? 'fim_caso'
    ;

// cmdPara → "para" IDENT "<-" exp_aritmetica "ate" exp_aritmetica "faca" {cmd} "fim_para"
cmdPara
    : 'para' IDENT '<-' exp_aritmetica 'ate' exp_aritmetica 'faca' cmd* 'fim_para'
    ;

// cmdEnquanto → "enquanto" expressao "faca" {cmd} "fim_enquanto"
cmdEnquanto
    : 'enquanto' expressao 'faca' cmd* 'fim_enquanto'
    ;

// cmdFaca → "faca" {cmd} "ate" expressao
cmdFaca
    : 'faca' cmd* 'ate' expressao
    ;

// cmdAtribuicao → ["^"] identificador "<-" expressao
cmdAtribuicao
    : '^'? identificador '<-' expressao
    ;

// cmdChamada → IDENT "(" expressao {"," expressao} ")"
cmdChamada
    : IDENT '(' expressao (',' expressao)* ')'
    ;

// cmdRetorne → "retorne" expressao
cmdRetorne
    : 'retorne' expressao
    ;

// ============================================================
// SELEÇÃO (estrutura do caso/seja)
// selecao → {item_selecao}
// item_selecao → constantes ":" {cmd}
// constantes → numero_intervalo {"," numero_intervalo}
// numero_intervalo → [op_unario] NUM_INT [".." [op_unario] NUM_INT]
// ============================================================
selecao
    : item_selecao+
    ;

item_selecao
    : constantes ':' cmd*
    ;

constantes
    : numero_intervalo (',' numero_intervalo)*
    ;

numero_intervalo
    : op_unario? NUM_INT ('..' op_unario? NUM_INT)?
    ;

op_unario
    : '-'
    ;

// ============================================================
// EXPRESSÕES ARITMÉTICAS
// exp_aritmetica → termo {op1 termo}
// termo → fator {op2 fator}
// fator → parcela {op3 parcela}
// op1 → "+" | "-"
// op2 → "*" | "/"
// op3 → "%"
// ============================================================
exp_aritmetica
    : termo (op1 termo)*
    ;

termo
    : fator (op2 fator)*
    ;

fator
    : parcela (op3 parcela)*
    ;

op1 : '+' | '-' ;
op2 : '*' | '/' ;
op3 : '%' ;

// parcela → [op_unario] parcela_unario | parcela_nao_unario
//
// parcela_unario → ["^"] identificador
//                | IDENT "(" expressao {"," expressao} ")"
//                | NUM_INT
//                | NUM_REAL
//                | "(" expressao ")"
//
// parcela_nao_unario → "&" identificador | CADEIA
parcela
    : op_unario? parcela_unario
    | parcela_nao_unario
    ;

parcela_unario
    : '^'? identificador
    | IDENT '(' expressao (',' expressao)* ')'
    | NUM_INT
    | NUM_REAL
    | '(' expressao ')'
    ;

parcela_nao_unario
    : '&' identificador
    | CADEIA
    ;

// ============================================================
// EXPRESSÕES RELACIONAIS E LÓGICAS
// exp_relacional → exp_aritmetica [op_relacional exp_aritmetica]
// op_relacional → "=" | "<>" | ">=" | "<=" | ">" | "<"
// expressao → termo_logico {op_logico_1 termo_logico}
// termo_logico → fator_logico {op_logico_2 fator_logico}
// fator_logico → ["nao"] parcela_logica
// parcela_logica → ("verdadeiro" | "falso") | exp_relacional
// op_logico_1 → "ou"
// op_logico_2 → "e"
// ============================================================
exp_relacional
    : exp_aritmetica (op_relacional exp_aritmetica)?
    ;

op_relacional
    : '='
    | '<>'
    | '>='
    | '<='
    | '>'
    | '<'
    ;

expressao
    : termo_logico (op_logico_1 termo_logico)*
    ;

termo_logico
    : fator_logico (op_logico_2 fator_logico)*
    ;

fator_logico
    : 'nao'? parcela_logica
    ;

parcela_logica
    : ('verdadeiro' | 'falso')
    | exp_relacional
    ;

op_logico_1 : 'ou' ;
op_logico_2 : 'e' ;

// ============================================================
// REGRAS LÉXICAS
// Tokens reconhecidos pelo Lexer do ANTLR.
// Palavras reservadas devem vir ANTES de IDENT.
// ============================================================

// Palavras reservadas
ALGORITMO        : 'algoritmo' ;
FIM_ALGORITMO    : 'fim_algoritmo' ;
DECLARE          : 'declare' ;
CONSTANTE_KW     : 'constante' ;
TIPO_KW          : 'tipo' ;
PROCEDIMENTO     : 'procedimento' ;
FIM_PROCEDIMENTO : 'fim_procedimento' ;
FUNCAO           : 'funcao' ;
FIM_FUNCAO       : 'fim_funcao' ;
RETORNE          : 'retorne' ;
VAR_KW           : 'var' ;
REGISTRO_KW      : 'registro' ;
FIM_REGISTRO     : 'fim_registro' ;
LEIA             : 'leia' ;
ESCREVA          : 'escreva' ;
SE               : 'se' ;
ENTAO            : 'entao' ;
FIM_SE           : 'fim_se' ;
SENAO            : 'senao' ;
CASO             : 'caso' ;
SEJA             : 'seja' ;
FIM_CASO         : 'fim_caso' ;
PARA             : 'para' ;
ATE              : 'ate' ;
FIM_PARA         : 'fim_para' ;
ENQUANTO         : 'enquanto' ;
FACA             : 'faca' ;
FIM_ENQUANTO     : 'fim_enquanto' ;
LITERAL_KW       : 'literal' ;
INTEIRO_KW       : 'inteiro' ;
REAL_KW          : 'real' ;
LOGICO_KW        : 'logico' ;
VERDADEIRO       : 'verdadeiro' ;
FALSO            : 'falso' ;
NAO              : 'nao' ;
E_KW             : 'e' ;
OU               : 'ou' ;

// Identificador: letra (incluindo acentuadas) ou '_', seguido de letras, dígitos ou '_'
IDENT : (LETRA | '_') (LETRA | DIGITO | '_')* ;

// Números inteiros e reais
NUM_INT  : DIGITO+ ;
NUM_REAL : DIGITO+ '.' DIGITO+ ;

// Cadeia de caracteres entre aspas duplas (deve estar na mesma linha)
CADEIA : '"' ~["\n\r]* '"' ;

// Símbolos compostos (devem vir antes dos simples correspondentes)
ATRIBUICAO  : '<-' ;
MENOR_IGUAL : '<=' ;
MAIOR_IGUAL : '>=' ;
DIFERENTE   : '<>' ;
PONTO_PONTO : '..' ;

// Símbolos simples
VIRGULA     : ',' ;
PONTO_VIRG  : ';' ;
DOIS_PONTOS : ':' ;
PONTO       : '.' ;
ABRE_PAR    : '(' ;
FECHA_PAR   : ')' ;
ABRE_COL    : '[' ;
FECHA_COL   : ']' ;
MENOR       : '<' ;
MAIOR       : '>' ;
IGUAL       : '=' ;
MAIS        : '+' ;
MENOS       : '-' ;
ASTERISCO   : '*' ;
BARRA       : '/' ;
PORCENTAGEM : '%' ;
CIRCUNFLEXO : '^' ;
AMPERSAND   : '&' ;

// Comentários entre { } são ignorados (devem estar na mesma linha)
COMENTARIO : '{' ~[}\n\r]* '}' -> skip ;

// Espaços em branco são ignorados
WS : [ \t\r\n]+ -> skip ;

// Fragmentos auxiliares para composição de outros tokens
fragment LETRA  : [a-zA-ZÀ-ÿ] ;
fragment DIGITO : [0-9] ;
