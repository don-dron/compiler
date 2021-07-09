#include <stdlib.h>

void main() {
    int *a = malloc(2);
    int c = 101010;
    a[c] = 2;
    int b = a[c];
}