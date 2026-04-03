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
    /* Musica: "Marcha Imperial" | BPM: 100 | Repeticoes: 1 */
    tocar(220, 600); /* LA3 */
    tocar(220, 600); /* LA3 */
    tocar(220, 600); /* LA3 */
    tocar(175, 300); /* FA3 */
    tocar(262, 150); /* DO4 */
    tocar(220, 600); /* LA3 */
    tocar(175, 300); /* FA3 */
    tocar(262, 150); /* DO4 */
    tocar(220, 1200); /* LA3 */
    tocar(330, 600); /* MI4 */
    tocar(330, 600); /* MI4 */
    tocar(330, 600); /* MI4 */
    tocar(349, 300); /* FA4 */
    tocar(262, 150); /* DO4 */
    tocar(220, 600); /* LA3 */
    tocar(175, 300); /* FA3 */
    tocar(262, 150); /* DO4 */
    tocar(220, 1200); /* LA3 */
    tocar(440, 600); /* LA4 */
    tocar(220, 300); /* LA3 */
    tocar(220, 150); /* LA3 */
    tocar(440, 600); /* LA4 */
    tocar(415, 300); /* SOL#4 */
    tocar(392, 150); /* SOL4 */
    tocar(370, 150); /* FA#4 */
    tocar(349, 150); /* FA4 */
    tocar(370, 300); /* FA#4 */
    tocar(0, 300); /* pausa */
    tocar(233, 300); /* LA#3 */
    tocar(294, 600); /* RE4 */
    tocar(277, 300); /* DO#4 */
    tocar(262, 150); /* DO4 */
    tocar(247, 150); /* SI3 */
    tocar(247, 150); /* SI3 */
    tocar(262, 300); /* DO4 */
    tocar(0, 300); /* pausa */
    tocar(175, 300); /* FA3 */
    tocar(208, 600); /* SOL#3 */
    tocar(175, 300); /* FA3 */
    tocar(220, 150); /* LA3 */
    tocar(262, 600); /* DO4 */
    tocar(220, 300); /* LA3 */
    tocar(262, 150); /* DO4 */
    tocar(330, 1200); /* MI4 */
    tocar(440, 600); /* LA4 */
    tocar(220, 300); /* LA3 */
    tocar(220, 150); /* LA3 */
    tocar(440, 600); /* LA4 */
    tocar(415, 300); /* SOL#4 */
    tocar(392, 150); /* SOL4 */
    tocar(370, 150); /* FA#4 */
    tocar(349, 150); /* FA4 */
    tocar(370, 300); /* FA#4 */
    tocar(0, 300); /* pausa */
    tocar(233, 300); /* LA#3 */
    tocar(294, 600); /* RE4 */
    tocar(277, 300); /* DO#4 */
    tocar(262, 150); /* DO4 */
    tocar(247, 150); /* SI3 */
    tocar(262, 300); /* DO4 */
    tocar(175, 300); /* FA3 */
    tocar(220, 600); /* LA3 */
    tocar(175, 300); /* FA3 */
    tocar(262, 150); /* DO4 */
    tocar(220, 2400); /* LA3 */

    return 0;
}
