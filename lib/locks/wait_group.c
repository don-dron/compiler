#include <locks/wait_group.h>

wait_group init() {
    wait_group group;
    group.count = 0;
    return group;
}

void add(wait_group *group) {
    inc(&group->count);
}

void done(wait_group *group) {
    dec(&group->count);
}

void wait_block(wait_group *group) {
    while (group->count) {
        usleep(2);
    }
}