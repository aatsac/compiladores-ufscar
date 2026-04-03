#!/bin/bash
# =============================================================
# Testa se as melodias geram o código C esperado
# =============================================================

COMPILADOR="../compilador-musical/target/compilador-musica.jar"

ENTRADA="entrada"
SAIDA="saida"
ESPERADA="saida_esperada"

mkdir -p "$SAIDA"

PASSOU=0
FALHOU=0
TOTAL=0

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "======================================"
echo " Teste de geração das melodias"
echo "======================================"
echo ""

for arquivo in "$ENTRADA"/*.musica; do
    nome=$(basename "$arquivo" .musica)

    gerado="$SAIDA/$nome.c"
    esperado="$ESPERADA/$nome.c"

    TOTAL=$((TOTAL + 1))

    # gera código C
    java -jar "$COMPILADOR" "$arquivo" "$gerado" 2>/dev/null

    # verifica se existe saída esperada
    if [ ! -f "$esperado" ]; then
        echo -e "${RED}❌ FALHOU${NC} — $nome (arquivo esperado não encontrado)"
        FALHOU=$((FALHOU + 1))
        continue
    fi

    # compara ignorando CRLF
    if diff <(tr -d '\r' < "$gerado") \
            <(tr -d '\r' < "$esperado") >/dev/null; then
        echo -e "${GREEN}✅ PASSOU${NC} — $nome"
        PASSOU=$((PASSOU + 1))
    else
        echo -e "${RED}❌ FALHOU${NC} — $nome"
        echo "Diferenças:"
        diff "$esperado" "$gerado"
        FALHOU=$((FALHOU + 1))
    fi
done

echo ""
echo "======================================"
echo " Resultado: $PASSOU/$TOTAL passaram"
echo "======================================"

[ $FALHOU -eq 0 ] && exit 0 || exit 1