// ============================================================
// Gramática da Linguagem Musical (MusicaLA) para ANTLR4
// Disciplina: Construção de Compiladores - DC/UFSCar
// Prof. Daniel Lucrédio
//
// Permite descrever melodias com notas, durações, BPM e
// pausas. O compilador gera código C que toca a melodia
// usando frequências de áudio via beep do sistema.
//
// Exemplo de programa válido:
//
//   musica "Parabéns"
//     bpm 120
//     instrumento piano
//     nota DO4 quarto
//     nota RE4 quarto
//     pausa quarto
//   fim_musica
// ============================================================

grammar Musica;

// ============================================================
// REGRA INICIAL
// Um programa é uma sequência de um ou mais blocos de música.
// ============================================================
programa
    : bloco_musica+ EOF
    ;

// ============================================================
// BLOCO DE MÚSICA
// Cada bloco tem um título, configurações opcionais e comandos.
// ============================================================
bloco_musica
    : 'musica' CADEIA
      configuracao*
      comando*
      'fim_musica'
    ;

// ============================================================
// CONFIGURAÇÕES
// Parâmetros opcionais do bloco musical.
// ============================================================
configuracao
    : cmd_bpm
    | cmd_instrumento
    | cmd_repeticoes
    ;

// bpm N — define o andamento (batidas por minuto)
cmd_bpm
    : 'bpm' NUM_INT
    ;

// instrumento NOME — define o instrumento (informativo)
cmd_instrumento
    : 'instrumento' IDENT
    ;

// repeticoes N — quantas vezes repetir a melodia
cmd_repeticoes
    : 'repeticoes' NUM_INT
    ;

// ============================================================
// COMANDOS
// Sequência de notas e pausas que formam a melodia.
// ============================================================
comando
    : cmd_nota
    | cmd_pausa
    | cmd_sequencia
    ;

// nota NOME_NOTA DURACAO — toca uma nota por uma duração
// Exemplos: nota DO4 quarto, nota LA#5 meio
cmd_nota
    : 'nota' NOME_NOTA duracao
    ;

// pausa DURACAO — silêncio por uma duração
cmd_pausa
    : 'pausa' duracao
    ;

// sequencia "nome" { comandos } — grupo nomeado de notas (para repetição)
cmd_sequencia
    : 'sequencia' CADEIA
      comando+
      'fim_sequencia'
    ;

// ============================================================
// DURAÇÃO
// Valores de duração possíveis para uma nota ou pausa.
// ============================================================
duracao
    : 'inteiro'     // 1 compasso inteiro
    | 'meio'        // 1/2 compasso
    | 'quarto'      // 1/4 compasso
    | 'colcheia'    // 1/8 compasso
    | 'semi'        // 1/16 compasso
    ;

// ============================================================
// REGRAS LÉXICAS
// ============================================================

// Nota musical: nome da nota + oitava (ex: DO4, LA#3, SIb5)
// Notas: DO, RE, MI, FA, SOL, LA, SI
// Acidentes: # (sustenido) ou b (bemol)
// Oitava: 1 a 8
NOME_NOTA
    : ('DO' | 'RE' | 'MI' | 'FA' | 'SOL' | 'LA' | 'SI')
      ('#' | 'b')?
      [1-8]
    ;

// Palavras reservadas (devem vir antes de IDENT)
MUSICA        : 'musica' ;
FIM_MUSICA    : 'fim_musica' ;
BPM           : 'bpm' ;
INSTRUMENTO   : 'instrumento' ;
REPETICOES    : 'repeticoes' ;
NOTA          : 'nota' ;
PAUSA         : 'pausa' ;
SEQUENCIA     : 'sequencia' ;
FIM_SEQUENCIA : 'fim_sequencia' ;
INTEIRO_DUR   : 'inteiro' ;
MEIO          : 'meio' ;
QUARTO        : 'quarto' ;
COLCHEIA      : 'colcheia' ;
SEMI          : 'semi' ;

// Identificador: nome de instrumento, etc.
IDENT : [a-zA-Z_][a-zA-Z0-9_]* ;

// Número inteiro positivo (BPM, repetições)
NUM_INT : [0-9]+ ;

// Cadeia de caracteres entre aspas duplas (título da música)
CADEIA : '"' ~["\n\r]* '"' ;

// Comentários iniciados com // são ignorados
COMENTARIO : '//' ~[\n\r]* -> skip ;

// Espaços e quebras de linha são ignorados
WS : [ \t\r\n]+ -> skip ;
