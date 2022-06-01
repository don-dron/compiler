#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <pthread.h>
#include <time.h>
#include <sys/time.h>
#include <scheduler/context.h>
#include <structures/list.h>
#include <scheduler/local_queues_with_steal_scheduler.h>
#include <root.h>

scheduler *sched;

long __create_fiber(void (*fiber_routine)(void)) {
    fiber *fib = spawn(sched, (void (*)(void *)) fiber_routine, NULL);
    publish_fiber(sched, fib);
    return (long) fib;
}

void __sleep(unsigned int millis) {
    sleep_for(millis);
}

void __join(long fib) {
    join((fiber *) fib);
}

void __yield(void) {
    yield();
}

void __delete_fiber(long fib) {
    unpublish_fiber(sched, (fiber *) fib);
}

int __cas(long addr, long exp, long next) {
    return __atomic_compare_exchange_n((long*)(addr+8), &exp, next, 0, __ATOMIC_SEQ_CST,  __ATOMIC_SEQ_CST);
}

long __exchange(long addr, long next) {
    return __atomic_exchange_n((long*)(addr+8), next,  __ATOMIC_SEQ_CST);
}

long __load(long addr) {
    return __atomic_load_n((long*)(addr+8),  __ATOMIC_SEQ_CST);
}

void __store(long addr, long next) {
    __atomic_store_n((long*)(addr+8), next,  __ATOMIC_SEQ_CST);
}


int main() {
    sched = (scheduler *) malloc(sizeof(scheduler));
    new_scheduler(sched, 8);
    run_scheduler(sched);

    spawn(sched, lang_main, NULL);

    shutdown(sched);
    terminate_scheduler(sched);
    free(sched);
}

