#include <stdlib.h>

int main() {
    int i = 0;
    int j = 0;
    int res = 0;
    while (i < 4){
        while (j < 4){
            res = res + 1;
            j = j + 1;
        }
        i = i + 1;
    }

    return res;
}