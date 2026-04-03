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
    /* Musica: "Ode a Alegria" | BPM: 120 | Repeticoes: 1 */
    tocar(330, 500); /* MI4 */
    tocar(330, 500); /* MI4 */
    tocar(349, 500); /* FA4 */
    tocar(392, 500); /* SOL4 */
    tocar(392, 500); /* SOL4 */
    tocar(349, 500); /* FA4 */
    tocar(330, 500); /* MI4 */
    tocar(294, 500); /* RE4 */
    tocar(262, 500); /* DO4 */
    tocar(262, 500); /* DO4 */
    tocar(294, 500); /* RE4 */
    tocar(330, 500); /* MI4 */
    tocar(330, 1000); /* MI4 */
    tocar(294, 125); /* RE4 */
    tocar(294, 1000); /* RE4 */
    tocar(330, 500); /* MI4 */
    tocar(330, 500); /* MI4 */
    tocar(349, 500); /* FA4 */
    tocar(392, 500); /* SOL4 */
    tocar(392, 500); /* SOL4 */
    tocar(349, 500); /* FA4 */
    tocar(330, 500); /* MI4 */
    tocar(294, 500); /* RE4 */
    tocar(262, 500); /* DO4 */
    tocar(262, 500); /* DO4 */
    tocar(294, 500); /* RE4 */
    tocar(330, 500); /* MI4 */
    tocar(294, 1000); /* RE4 */
    tocar(262, 125); /* DO4 */
    tocar(262, 2000); /* DO4 */
    tocar(294, 500); /* RE4 */
    tocar(294, 500); /* RE4 */
    tocar(330, 500); /* MI4 */
    tocar(262, 500); /* DO4 */
    tocar(294, 500); /* RE4 */
    tocar(330, 125); /* MI4 */
    tocar(349, 125); /* FA4 */
    tocar(330, 500); /* MI4 */
    tocar(262, 500); /* DO4 */
    tocar(294, 500); /* RE4 */
    tocar(330, 125); /* MI4 */
    tocar(349, 125); /* FA4 */
    tocar(330, 500); /* MI4 */
    tocar(294, 500); /* RE4 */
    tocar(262, 500); /* DO4 */
    tocar(294, 1000); /* RE4 */
    tocar(0, 500); /* pausa */
    tocar(330, 500); /* MI4 */
    tocar(330, 500); /* MI4 */
    tocar(349, 500); /* FA4 */
    tocar(392, 500); /* SOL4 */
    tocar(392, 500); /* SOL4 */
    tocar(349, 500); /* FA4 */
    tocar(330, 500); /* MI4 */
    tocar(294, 500); /* RE4 */
    tocar(262, 500); /* DO4 */
    tocar(262, 500); /* DO4 */
    tocar(294, 500); /* RE4 */
    tocar(330, 500); /* MI4 */
    tocar(294, 1000); /* RE4 */
    tocar(262, 125); /* DO4 */
    tocar(262, 2000); /* DO4 */

    return 0;
}
