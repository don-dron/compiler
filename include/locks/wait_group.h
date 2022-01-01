#pragma once

#include <locks/wait_group.h>
#include <locks/atomics.h>

typedef struct wait_group {
    unsigned long count;
} wait_group;

wait_group init(void);

void add(wait_group *group);

void done(wait_group *group);

void wait_block(wait_group *group);
