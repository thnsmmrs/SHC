#include <stdio.h>
#include <stdlib.h>

#include <stdint.h>

uint64_t main() {
    uint64_t len;
    len = 100;
    uint8_t *buf;
    buf = malloc(len + 1);
    uint8_t *val;
    val = buf;
    while ((val < buf + len)) {
        *val = 65;
        val = val + 1;
    }
    *val = 0;
    puts(buf);
    free(buf);
    return 0;
}