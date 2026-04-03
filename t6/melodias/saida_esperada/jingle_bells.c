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
    /* Musica: "Jingle Bells" | BPM: 160 | Repeticoes: 1 */
    /* Sequencia: "refrao" */
    tocar(330, 375); /* MI4 */
    tocar(330, 375); /* MI4 */
    tocar(330, 750); /* MI4 */
    tocar(330, 375); /* MI4 */
    tocar(330, 375); /* MI4 */
    tocar(330, 750); /* MI4 */
    tocar(330, 375); /* MI4 */
    tocar(392, 375); /* SOL4 */
    tocar(262, 375); /* DO4 */
    tocar(294, 375); /* RE4 */
    tocar(330, 1500); /* MI4 */
    tocar(349, 375); /* FA4 */
    tocar(349, 375); /* FA4 */
    tocar(349, 375); /* FA4 */
    tocar(349, 375); /* FA4 */
    tocar(349, 375); /* FA4 */
    tocar(330, 375); /* MI4 */
    tocar(330, 375); /* MI4 */
    tocar(330, 93); /* MI4 */
    tocar(330, 93); /* MI4 */
    tocar(330, 375); /* MI4 */
    tocar(294, 375); /* RE4 */
    tocar(294, 375); /* RE4 */
    tocar(330, 375); /* MI4 */
    tocar(294, 750); /* RE4 */
    tocar(392, 750); /* SOL4 */
    tocar(262, 375); /* DO4 */
    tocar(262, 375); /* DO4 */
    tocar(294, 375); /* RE4 */
    tocar(262, 375); /* DO4 */
    tocar(196, 750); /* SOL3 */
    tocar(0, 750); /* pausa */
    tocar(262, 375); /* DO4 */
    tocar(262, 375); /* DO4 */
    tocar(294, 375); /* RE4 */
    tocar(262, 375); /* DO4 */
    tocar(220, 750); /* LA3 */
    tocar(0, 750); /* pausa */
    tocar(262, 375); /* DO4 */
    tocar(262, 375); /* DO4 */
    tocar(523, 375); /* DO5 */
    tocar(440, 375); /* LA4 */
    tocar(349, 375); /* FA4 */
    tocar(294, 375); /* RE4 */
    tocar(392, 1500); /* SOL4 */
    tocar(247, 375); /* SI3 */
    tocar(247, 375); /* SI3 */
    tocar(220, 375); /* LA3 */
    tocar(196, 375); /* SOL3 */
    tocar(175, 375); /* FA3 */
    tocar(294, 375); /* RE4 */
    tocar(262, 1500); /* DO4 */

    return 0;
}
