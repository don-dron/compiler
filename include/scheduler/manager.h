#pragma once

#include <root.h>
#include <structures/hash_map.h>
#include <pthread.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/syscall.h>

int get_current_thread_id();

struct pthread_node {
    struct hash_map_node core;
    int thread_id;
    void *ptr;
};

int pthread_cmp_node(struct hash_map_node *a, struct hash_map_node *b);

int pthread_hash_node(struct hash_map_node *node);

struct fibers_pool_manager {
    struct hash_map current_fiber;
    struct hash_map current_coroutine;
    struct hash_map current_scheduler;
};

extern struct fibers_pool_manager fpl_manager;
