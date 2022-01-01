#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <pthread.h>
#include <time.h>
#include <sys/time.h>
#include <scheduler/context.h>
#include <structures/list.h>
#include <scheduler/rb_tree_scheduler.h>
#include <root.h>

scheduler* sched;

long __create_fiber(void (*fiber_routine)(void))
{
    return (long)spawn(sched, (void(*)(void*))fiber_routine, NULL);
}

void __sleep(unsigned int millis) {
    sleep_for(millis);
}

void __join(long fib) {
    join((fiber *)fib);
}

void __yield(void) {
    yield();
}

int main() {
    sched = (scheduler *)malloc(sizeof(scheduler));
    new_scheduler(sched, 4);
    run_scheduler(sched);

    spawn(sched, lang_main, NULL);

    shutdown(sched);
    terminate_scheduler(sched);
    free(sched);
}

