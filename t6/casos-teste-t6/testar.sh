#!/bin/bash
# =============================================================
# Script de teste automático do compilador MusicaLA (T6)
# =============================================================

COMPILADOR="../compilador-musical/target/compilador-musica.jar"
ENTRADA="entrada"
ESPERADA="saida_esperada"
TMP="/tmp/musica_test"

mkdir -p "$TMP"

PASSOU=0
FALHOU=0
TOTAL=0

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "======================================"
echo " Testes do Compilador MusicaLA (T6)"
echo "======================================"
echo ""

for entrada in "$ENTRADA"/*.musica; do
    nome=$(basename "$entrada" .musica)
    saida_produzida="$TMP/$nome.saida"
    saida_esperada="$ESPERADA/$nome.txt"
    codigo_c="$TMP/$nome.c"
    executavel="$TMP/$nome.out"

    TOTAL=$((TOTAL + 1))

    # executa compilador
    java -jar "$COMPILADOR" "$entrada" "$saida_produzida" 2>/dev/null

    # ---------------------------------------------------------
    # Caso de erro esperado
    # ---------------------------------------------------------
    if [ -f "$saida_esperada" ]; then
        if diff <(tr -d '\r' < "$saida_produzida") \
                <(tr -d '\r' < "$saida_esperada") >/dev/null; then

            echo -e "${GREEN}✅ PASSOU${NC} — $nome"
            PASSOU=$((PASSOU + 1))
        else
            echo -e "${RED}❌ FALHOU${NC} — $nome"
            echo "   Esperado:"
            sed 's/^/     /' "$saida_esperada"
            echo "   Obtido:"
            sed 's/^/     /' "$saida_produzida"
            FALHOU=$((FALHOU + 1))
        fi

    # ---------------------------------------------------------
    # Caso válido → deve gerar C compilável
    # ---------------------------------------------------------
    else
        if grep -q "int main()" "$saida_produzida"; then
            cp "$saida_produzida" "$codigo_c"

            if gcc "$codigo_c" -o "$executavel" 2>/dev/null; then
                echo -e "${GREEN}✅ PASSOU${NC} — $nome (C compilável)"
                PASSOU=$((PASSOU + 1))
            else
                echo -e "${RED}❌ FALHOU${NC} — $nome (gcc falhou)"
                echo "   Erro GCC:"
                gcc "$codigo_c" -o "$executavel" 2>&1 | sed 's/^/     /'
                FALHOU=$((FALHOU + 1))
            fi
        else
            echo -e "${RED}❌ FALHOU${NC} — $nome (não gerou C válido)"
            echo "   Saída:"
            sed 's/^/     /' "$saida_produzida"
            FALHOU=$((FALHOU + 1))
        fi
    fi
done

echo ""
echo "======================================"
echo " Resultado: $PASSOU/$TOTAL passaram"
echo "======================================"

[ $FALHOU -eq 0 ] && exit 0 || exit 1