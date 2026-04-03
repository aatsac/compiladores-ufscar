#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Toca uma nota usando sox (play) do sistema (Linux). */
/* Instale com: sudo apt install sox */
void tocar(int freq, int dur_ms) {
    char cmd[256];
    double dur_s = dur_ms / 1000.0;
    if (freq == 0) {
        /* Pausa: aguarda sem som */
        sprintf(cmd, "sleep %0.3f", dur_s);
    } else {
        sprintf(cmd, "play -n synth %0.3f sine %d > /dev/null 2>&1", dur_s, freq);
    }
    system(cmd);
}

int main() {
    /* Musica: "Brilha Brilha Estrelinha" | BPM: 110 | Repeticoes: 1 */
    tocar(262, 545); /* DO4 */
    tocar(262, 545); /* DO4 */
    tocar(392, 545); /* SOL4 */
    tocar(392, 545); /* SOL4 */
    tocar(440, 545); /* LA4 */
    tocar(440, 545); /* LA4 */
    tocar(392, 1090); /* SOL4 */
    tocar(349, 545); /* FA4 */
    tocar(349, 545); /* FA4 */
    tocar(330, 545); /* MI4 */
    tocar(330, 545); /* MI4 */
    tocar(294, 545); /* RE4 */
    tocar(294, 545); /* RE4 */
    tocar(262, 1090); /* DO4 */
    tocar(392, 545); /* SOL4 */
    tocar(392, 545); /* SOL4 */
    tocar(349, 545); /* FA4 */
    tocar(349, 545); /* FA4 */
    tocar(330, 545); /* MI4 */
    tocar(330, 545); /* MI4 */
    tocar(294, 1090); /* RE4 */
    tocar(392, 545); /* SOL4 */
    tocar(392, 545); /* SOL4 */
    tocar(349, 545); /* FA4 */
    tocar(349, 545); /* FA4 */
    tocar(330, 545); /* MI4 */
    tocar(330, 545); /* MI4 */
    tocar(294, 1090); /* RE4 */
    tocar(262, 545); /* DO4 */
    tocar(262, 545); /* DO4 */
    tocar(392, 545); /* SOL4 */
    tocar(392, 545); /* SOL4 */
    tocar(440, 545); /* LA4 */
    tocar(440, 545); /* LA4 */
    tocar(392, 1090); /* SOL4 */
    tocar(349, 545); /* FA4 */
    tocar(349, 545); /* FA4 */
    tocar(330, 545); /* MI4 */
    tocar(330, 545); /* MI4 */
    tocar(294, 545); /* RE4 */
    tocar(294, 545); /* RE4 */
    tocar(262, 2180); /* DO4 */

    return 0;
}
