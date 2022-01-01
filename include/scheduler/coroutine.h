#pragma once

#include <sys/mman.h>
#include <stdio.h>
#include <stdlib.h>
#include <scheduler/manager.h>

#include <scheduler/context.h>

struct coroutine;

typedef struct coroutine {
    void *args;

    void (*routine)(void *);

    execution_context routine_context;
    execution_context caller_context;
    struct coroutine *external_routine;
    volatile int complete;
} coroutine;

//extern thread_local coroutine *current_coroutine;

int create_coroutine(coroutine *new_coroutine, void (*routine)(void *), void *args);

void suspend(void);

void resume(coroutine *coro);

void switch_to_caller(coroutine *coroutine);

int free_coroutine(coroutine *coroutine);

coroutine *get_current_coroutine();

void save_current_coroutine(coroutine *coro) {
    struct pthread_node *next = (struct pthread_node *) malloc(sizeof(struct pthread_node));
    next->ptr = coro;
    next->thread_id = get_current_thread_id();

    struct hash_map_node *node = hash_map_insert(
            &fpl_manager.current_coroutine,
            &next->core
    );

    if (node != NULL) {
        free(node);
    }
}