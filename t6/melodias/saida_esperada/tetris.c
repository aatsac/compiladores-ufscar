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
    /* Musica: "Tetris" | BPM: 160 | Repeticoes: 1 */
    tocar(659, 375); /* MI5 */
    tocar(494, 187); /* SI4 */
    tocar(523, 187); /* DO5 */
    tocar(587, 375); /* RE5 */
    tocar(523, 187); /* DO5 */
    tocar(494, 187); /* SI4 */
    tocar(440, 375); /* LA4 */
    tocar(440, 187); /* LA4 */
    tocar(523, 187); /* DO5 */
    tocar(659, 375); /* MI5 */
    tocar(587, 187); /* RE5 */
    tocar(523, 187); /* DO5 */
    tocar(494, 375); /* SI4 */
    tocar(494, 187); /* SI4 */
    tocar(523, 187); /* DO5 */
    tocar(587, 375); /* RE5 */
    tocar(659, 375); /* MI5 */
    tocar(523, 375); /* DO5 */
    tocar(440, 375); /* LA4 */
    tocar(440, 750); /* LA4 */
    tocar(0, 187); /* pausa */
    tocar(587, 375); /* RE5 */
    tocar(698, 187); /* FA5 */
    tocar(880, 375); /* LA5 */
    tocar(784, 187); /* SOL5 */
    tocar(698, 187); /* FA5 */
    tocar(659, 375); /* MI5 */
    tocar(659, 187); /* MI5 */
    tocar(523, 187); /* DO5 */
    tocar(659, 375); /* MI5 */
    tocar(587, 187); /* RE5 */
    tocar(523, 187); /* DO5 */
    tocar(494, 375); /* SI4 */
    tocar(494, 187); /* SI4 */
    tocar(523, 187); /* DO5 */
    tocar(587, 375); /* RE5 */
    tocar(659, 375); /* MI5 */
    tocar(523, 375); /* DO5 */
    tocar(440, 375); /* LA4 */
    tocar(440, 750); /* LA4 */
    tocar(659, 375); /* MI5 */
    tocar(494, 187); /* SI4 */
    tocar(523, 187); /* DO5 */
    tocar(587, 375); /* RE5 */
    tocar(523, 187); /* DO5 */
    tocar(494, 187); /* SI4 */
    tocar(440, 375); /* LA4 */
    tocar(440, 187); /* LA4 */
    tocar(523, 187); /* DO5 */
    tocar(659, 375); /* MI5 */
    tocar(587, 187); /* RE5 */
    tocar(523, 187); /* DO5 */
    tocar(494, 375); /* SI4 */
    tocar(494, 187); /* SI4 */
    tocar(523, 187); /* DO5 */
    tocar(587, 375); /* RE5 */
    tocar(659, 375); /* MI5 */
    tocar(523, 375); /* DO5 */
    tocar(440, 375); /* LA4 */
    tocar(440, 1500); /* LA4 */

    return 0;
}
