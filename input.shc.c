#include <stdio.h>
#include <stdint.h>

uint64_t main() {
    uint64_t len;
    len = 100;
    uint8_t *buf;
    buf = malloc(len);
    uint8_t *val;
    val = buf;
    uint64_t i;
    i = 0;
    while ((i < len)) {
        val = val + 1;
        *val = 65;
        i = i + 1;
    }
    puts(buf);
    free(buf);
    return 0;
}