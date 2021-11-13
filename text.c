#include <stdlib.h>

static void dest1(void* p) {
}
static void dest2(void* p) {
}
static void dest3(void* p) {
}
static void dest4(void* p){
}

void (*c[4])(void*) = {dest1, dest2, dest3, dest4};


int main() {
    c[1](0);
    return 0;
}