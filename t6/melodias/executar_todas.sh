#!/bin/bash

COMPILADOR="../compilador-musical/target/compilador-musica.jar"
PASTA_ENTRADA="entrada"
PASTA_SAIDA="saida"

mkdir -p "$PASTA_SAIDA"

echo "🎵 Compilando e tocando melodias..."
echo

for arquivo in "$PASTA_ENTRADA"/*.musica; do
    nome=$(basename "$arquivo" .musica)
    arquivo_c="$PASTA_SAIDA/${nome}.c"
    executavel="$PASTA_SAIDA/$nome"

    echo "======================================"
    echo "🎼 Música: $nome"
    echo "======================================"

    # gera código C
    java -jar "$COMPILADOR" "$arquivo" "$arquivo_c"

    if [ $? -ne 0 ]; then
        echo "❌ Erro ao gerar C para $nome"
        continue
    fi

    # compila C
    gcc "$arquivo_c" -o "$executavel"

    if [ $? -ne 0 ]; then
        echo "❌ Erro no gcc para $nome"
        continue
    fi

    # executa
    echo "▶ Tocando..."
    "$executavel"

    echo "✅ Fim de $nome"
    echo
    sleep 1
done