#include <stdlib.h>

int main() {
    int i_1 = 0;
    int i_2 = 0;
    int i_3 = 0;
    int size_1 = 13;
    int size_2 = 17;
    int size_3 = 19;

    int *** b = malloc(sizeof(int**) * size_1);

    while (i_1 < size_1) {
        i_2 = 0;
        b[i_1] = malloc(sizeof(int*) * size_2);
        while(i_2 < size_2) {
             i_3 = 0;
              b[i_2] = malloc(sizeof(int) * size_3);
              while(i_3 < size_3){
                i_3++;
              }
            i_2++;
        }
        i_1++;
    }

    return i_1;
}