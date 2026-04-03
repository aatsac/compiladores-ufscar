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
    /* Musica: "Super Mario Bros" | BPM: 200 | Repeticoes: 1 */
    tocar(659, 150); /* MI5 */
    tocar(659, 150); /* MI5 */
    tocar(0, 150); /* pausa */
    tocar(659, 150); /* MI5 */
    tocar(0, 150); /* pausa */
    tocar(523, 150); /* DO5 */
    tocar(659, 150); /* MI5 */
    tocar(0, 150); /* pausa */
    tocar(784, 300); /* SOL5 */
    tocar(0, 300); /* pausa */
    tocar(392, 300); /* SOL4 */
    tocar(0, 300); /* pausa */
    tocar(523, 300); /* DO5 */
    tocar(0, 150); /* pausa */
    tocar(392, 300); /* SOL4 */
    tocar(0, 150); /* pausa */
    tocar(330, 300); /* MI4 */
    tocar(0, 150); /* pausa */
    tocar(440, 150); /* LA4 */
    tocar(0, 150); /* pausa */
    tocar(494, 150); /* SI4 */
    tocar(0, 150); /* pausa */
    tocar(466, 150); /* LA#4 */
    tocar(440, 150); /* LA4 */
    tocar(392, 150); /* SOL4 */
    tocar(659, 150); /* MI5 */
    tocar(784, 150); /* SOL5 */
    tocar(880, 150); /* LA5 */
    tocar(698, 150); /* FA5 */
    tocar(784, 150); /* SOL5 */
    tocar(0, 150); /* pausa */
    tocar(659, 150); /* MI5 */
    tocar(523, 150); /* DO5 */
    tocar(587, 150); /* RE5 */
    tocar(494, 300); /* SI4 */
    tocar(0, 300); /* pausa */
    tocar(523, 300); /* DO5 */
    tocar(0, 150); /* pausa */
    tocar(392, 300); /* SOL4 */
    tocar(0, 150); /* pausa */
    tocar(330, 300); /* MI4 */
    tocar(0, 150); /* pausa */
    tocar(440, 150); /* LA4 */
    tocar(0, 150); /* pausa */
    tocar(494, 150); /* SI4 */
    tocar(0, 150); /* pausa */
    tocar(466, 150); /* LA#4 */
    tocar(440, 150); /* LA4 */
    tocar(392, 150); /* SOL4 */
    tocar(659, 150); /* MI5 */
    tocar(784, 150); /* SOL5 */
    tocar(880, 150); /* LA5 */
    tocar(698, 150); /* FA5 */
    tocar(784, 150); /* SOL5 */
    tocar(0, 150); /* pausa */
    tocar(659, 150); /* MI5 */
    tocar(523, 150); /* DO5 */
    tocar(587, 150); /* RE5 */
    tocar(494, 300); /* SI4 */
    tocar(0, 300); /* pausa */

    return 0;
}
