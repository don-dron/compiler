#pragma once

#include <scheduler/manager.h>
#include <time.h>

#include <scheduler/context.h>
#include <locks/atomics.h>
#include <locks/spinlock.h>
#include <structures/list.h>

enum fiber_state {
    STARTING,
    RUNNABLE,
    RUNNING,
    SLEEPING,
    TERMINATED
};

enum thread_state {
    SCHEDULE,
    WORK,
    SLEEP_THREAD
};

typedef struct scheduler scheduler;
typedef enum thread_state thread_state;
typedef enum fiber_state fiber_state;
typedef struct fiber fiber;

typedef struct history_node {
    struct history_node *prev;
    struct history_node *next;
#if FIBER_STAT
    fiber_state fiber_state;
#endif

#if THREAD_STAT
    thread_state thread_state;
#endif
    clock_t start;
} history_node;

typedef struct history_save {
    list_node node;
    history_node *tail;
} history_save;

extern list history;

void create_history(void);

void print_history(void);

void free_history(void);

#if FIBER_STAT
void update_fiber_history(fiber *fiber);
void save_fiber_history(fiber *fiber);
#endif

#if THREAD_STAT
void update_thread_history(thread_state state);
void save_thread_history(scheduler* sched);
#endif

typedef struct history_node history_node;

typedef void (*fiber_routine)(void *);

typedef struct fiber {
    void *args;
    execution_context external_context;
    execution_context context;
    fiber_state state;
    fiber_routine routine;
    struct fiber *parent;
    unsigned long id;
    clock_t start;
    clock_t wakeup;
    struct spinlock lock;
    struct scheduler *sched;
    unsigned long long vruntime;
    int level;

#if FIBER_STAT
    history_node *last;
#endif

} fiber;

//extern thread_local fiber * volatile current_fiber;
extern unsigned long volatile id;

fiber *create_fiber(fiber_routine routine, void *args);

void free_fiber(fiber *fiber);

void setup_trampoline(fiber *fiber);

fiber *get_current_fiber();

void save_current_fiber(fiber *fib);

void delete_current_fiber();