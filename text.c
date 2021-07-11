#include <stdlib.h>
struct point_t {
    int x;
    int y;
};

int main() {
    struct point_t p;

    struct point_t *t = (struct point_t *)malloc(sizeof(struct point_t));
    t->x = 1;
    int c = t->x;

    return 0;
}