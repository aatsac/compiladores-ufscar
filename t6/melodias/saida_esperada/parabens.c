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
    /* Musica: "Parabens pra Voce" | BPM: 100 | Repeticoes: 1 */
    tocar(392, 300); /* SOL4 */
    tocar(392, 150); /* SOL4 */
    tocar(440, 600); /* LA4 */
    tocar(392, 600); /* SOL4 */
    tocar(523, 600); /* DO5 */
    tocar(494, 1200); /* SI4 */
    tocar(392, 300); /* SOL4 */
    tocar(392, 150); /* SOL4 */
    tocar(440, 600); /* LA4 */
    tocar(392, 600); /* SOL4 */
    tocar(587, 600); /* RE5 */
    tocar(523, 1200); /* DO5 */
    tocar(392, 300); /* SOL4 */
    tocar(392, 150); /* SOL4 */
    tocar(784, 600); /* SOL5 */
    tocar(659, 600); /* MI5 */
    tocar(523, 600); /* DO5 */
    tocar(494, 600); /* SI4 */
    tocar(440, 1200); /* LA4 */
    tocar(698, 300); /* FA5 */
    tocar(698, 150); /* FA5 */
    tocar(659, 600); /* MI5 */
    tocar(523, 600); /* DO5 */
    tocar(587, 600); /* RE5 */
    tocar(523, 2400); /* DO5 */

    return 0;
}
