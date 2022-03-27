#include <scheduler/scheduler.h>

#if INTERRUPT_ENABLED

#define INTERVAL 50
#define MAX_TIME 1000

#endif

//thread_local unsigned long thread_id = 127;
//thread_local scheduler *current_scheduler = NULL;

//
//    Handler threads - pthreads . Each pthread gets task - fiber from pull and run him.
//    After yield,sleep,terminate - cooperative methods or interrupt - preemption methods
//    handler returns for scheduling.
//
//    Detail for one thread:
//
//              scheduling          scheduling .etc
//         (gets fiber from pull                                              scheduler
//              and run him)                                                   _______
//              _______           __________ .....                            ^       |
//              ^      |          ^                                           |       |
//              |      V          |                                           |       V
//  -after run----------*---------*--------------------*----------------------------------------------------->  work time
//                      |         ^                    |  work in          ^          |                   |
//                      V_________|  yield()           |  fiber is long    |          |                   ^
//                       work in     sleep_for()       V--------           |          |                   |
//   useful work *-----*    fiber    `end of fiber`            |           |          |                   |
//                      |         |                                        |          |                   ^ returns to fiber context
//                                                             |           |          |                   |         for handler context
//                      |         |                                        |          V                   |
//                                                             |   switch context    switch context       |
//                      |         |                                 to scheduler      to signal handler   |
//                      *         *                            |           ^          |                   |
//          switch to fiber      switch to scheduler                       |          |                   |
//                                                             |           |          |                   |
//                                                                    signal handler  |**************** > |
//            cooperative mechanizm                            |           |                         ^
//                                                                         |                         | remove additional stack frame
//                                                             |           |                         |          for handler
//                                              it's kernel                ^                        KERNEL
//                                                work         |           |
//                                      |******************* >             |
//                                      |                      |         new stack frame(created by kernel)
//                                      |                      ******* > for handler(in userspace) - we have additional
//                                      |                                stack frame above fiber frame
//                                      |
//
//                                   KERNEL **************** < *********   ^
//                                                                         |
//                                                                 send signal from additional thread
//

typedef struct free_node {
  list_node node;
  fiber *fib;
} free_node;

static void delete_fiber(list_node *node) {
    free_node *fiber_node = (free_node *) node;
    free_fiber(fiber_node->fib);
    free(fiber_node->fib);
}

static inline long clock_to_microseconds(long time) {
    // Magic constant
    return time * 4;
}

static inline long sub_time(clock_t end, clock_t start) {
    // Returns time delta
    long delta = clock_to_microseconds((long) (end - start));
    return delta;
}

void save_current_scheduler(scheduler *sched) {
    struct pthread_node *next = (struct pthread_node *) malloc(sizeof(struct pthread_node));
    next->ptr = sched;
    next->thread_id = get_current_thread_id();

    struct hash_map_node *node = hash_map_insert(
        &fpl_manager.current_scheduler,
        &next->core
    );

    if (node != NULL) {
        free(node);
    }
}

scheduler *get_current_scheduler() {
    struct pthread_node to_find;
    to_find.thread_id = get_current_thread_id();

    struct pthread_node *node = ((struct pthread_node *) hash_map_find(&fpl_manager.current_scheduler, &to_find.core));

    if (node == NULL) {
        return NULL;
    }

    return (scheduler *) node->ptr;
}

static void run_task(fiber *routine) {
    scheduler *current_scheduler = get_current_scheduler();
    // If not locking, a context switch can be called during another
    // context switch in the same thread - it's ub and crushed stack frame
    // This lock will unlocked in fiber body.
//    lock_spinlock(&routine->lock);

    fiber *current_fiber = routine;
    save_current_fiber(current_fiber);
    fiber *temp = current_fiber;

    if (current_fiber->state == SLEEPING) {
        // Checks time for wake up
        if (sub_time(current_fiber->wakeup, clock()) > 0) {
            // Wake up failed, sleep yet
            return_to_pool(current_scheduler, current_fiber);
            delete_current_fiber();
//            current_fiber = NULL;
//            unlock_spinlock(&temp->lock);
            return;
        } else {
            // Wake up success, run
            current_fiber->state = RUNNABLE;
        }
    }

    if (current_fiber->state == STARTING || current_fiber->state == RUNNABLE) {
#if DEBUG
        printf("[IN_FIBER ] To fiber %ld %ld %d\n", get_current_thread_id(), current_fiber->id, current_fiber->state);
#endif

        current_fiber->state = RUNNING;

        // Save start fiber time
        current_fiber->start = clock();

        // Returns to fiber
        //
        // * ~ Magic ~ *
        //
#if FIBER_STAT
        update_fiber_history(current_fiber);
#endif

        switch_context(&current_fiber->external_context, &current_fiber->context);

        clock_t diff = clock() - current_fiber->start;
        current_fiber->vruntime += (unsigned long long) diff;
        //
        // Returns to scheduler
#if DEBUG
        printf("[OUT_FIBER] Out fiber %ld %ld %d\n", get_current_thread_id(), current_fiber->id, current_fiber->state);
#endif

        // If not terminated - returns to pull
        if (current_fiber->state != TERMINATED) {
            // Returns to pull
            return_to_pool(current_scheduler, current_fiber);
//            current_fiber = NULL;

            delete_current_fiber();

            // This unlock is unlocked fiber body lock
//            unlock_spinlock(&temp->lock);
        } else {
//            current_fiber = NULL;

            delete_current_fiber();
            inc((unsigned long *) &current_scheduler->end_count);

//            free_node *node = (free_node *) malloc(sizeof(free_node));
//            node->fib = temp;
//            list_push_front(&current_scheduler->garbage, (list_node *) node);
//            unlock_spinlock(&temp->lock);

            free_fiber(temp);
            free(temp);
        }
    } else {
        printf("[ERROR] Run task wrong state  %d\n", current_fiber->state);
        exit(1);
    }
}

static inline void scheduler_pause() {
    scheduler *current_scheduler = get_current_scheduler();
    // Loop if not running and go out if terminate
    while (!current_scheduler->threads_running) {
        if (current_scheduler->terminate) {
            return;
        }

        usleep(10);
    }
}

static void schedule() {
    scheduler *current_scheduler = get_current_scheduler();
    while (1) {
        // If scheduler not run - block
        scheduler_pause();

#if THREAD_STAT
        update_thread_history(SCHEDULE);
#endif

        if (current_scheduler->terminate) {
            return;
        }

        // Gets fiber from pull
        fiber *fib = get_from_pool();

        if (fib) {
            // Run task

#if THREAD_STAT
            update_thread_history(WORK);
#endif
            run_task(fib);

#if THREAD_STAT
            update_thread_history(SCHEDULE);
#endif
        } else {
#if THREAD_STAT
            update_thread_history(SLEEP_THREAD);
#endif
            // Sleep if work queue is empty
            usleep(1000);
        }
    }
}

static void insert_fiber(scheduler *sched, fiber *fib) {
    inc((unsigned long *) &sched->count);
    return_to_pool(sched, fib);
}

#if INTERRUPT_ENABLED

static void *signal_thread_func(void *args)
{
    current_scheduler = (scheduler *)args;
    fiber ***fibers = current_scheduler->current_fibers;
    for (; !current_scheduler->terminate;)
    {
        // If scheduler not run - block
        scheduler_pause();

        if (current_scheduler->terminate)
        {
            return NULL;
        }

        // Wake up after interval

        for (size_t i = 0; i < current_scheduler->threads; i++)
        {
            // Send signals to handlers

            fiber **ptr_to_ptr = fibers[i];

            if (!ptr_to_ptr)
            {
                continue;
            }

            fiber *temp = *ptr_to_ptr;

            if (!temp || temp != *(current_scheduler->current_fibers[i]) || temp->state != RUNNING)
            {
                continue;
            }

            long delta = sub_time(clock(), temp->start);
            if (delta > MAX_TIME)
            {
                pthread_kill(current_scheduler->threads_pool[i], SIGALRM);
            }
            else
            {
                inc(&interrupt_failed_count);
            }
        }

        usleep(INTERVAL);
    }

    return NULL;
}

static void handler(int signo)
{
    // Handler for SIGALRM signals
    fiber *temp = current_fiber;
    if (temp && try_lock_spinlock(&temp->lock))
    {
        if (temp == current_fiber && temp->state == RUNNING)
        {
#if DEBUG
            printf("[INTERRUPT] Interrupt fiber in scheduler thread with tid = %ld  with id = %ld  with last slice %ld delta\n", get_current_thread_id(), temp->id, delta);
#endif
            inc(&interrupt_count);
            temp->state = RUNNABLE;

            // Interrupt
#if FIBER_STAT
            update_fiber_history(temp);
#endif
            switch_context(&temp->context, &temp->external_context);

            // Unlock fiber body lock
            unlock_spinlock(&temp->lock);
        }
        else
        {
            inc(&interrupt_failed_count);
            unlock_spinlock(&temp->lock);
        }
    }
    else
    {
        inc(&interrupt_failed_count);
    }
}

#endif

static void *run_fibers_handler(void *arg) {
    unsigned long thread_number = ((unsigned long *) arg)[0];
    scheduler *sched = (scheduler *) ((unsigned long *) arg)[1];

    // Setting params to TLS

    scheduler *current_scheduler = sched;
    save_current_scheduler(sched);
//    current_scheduler->current_fibers[thread_number] = (fiber **)&current_fiber;

#if THREAD_STAT
    current_scheduler->threads_histories[thread_number] = (history_node *)malloc(sizeof(history_node));
    current_scheduler->threads_histories[thread_number]->start = clock();
    current_scheduler->threads_histories[thread_number]->thread_state = SLEEP_THREAD;
    current_scheduler->threads_histories[thread_number]->prev = NULL;
    current_scheduler->threads_histories[thread_number]->next = NULL;
#endif

    free(arg);

    // Run schedule
    schedule();
    return NULL;
}

int new_default_scheduler(scheduler *sched) {
    return new_scheduler(sched, (unsigned int) sysconf(_SC_NPROCESSORS_ONLN) - 1);
}

int new_scheduler(scheduler *sched, unsigned int using_threads) {
    // Init block
    size_t threads = using_threads;
    srand((unsigned int) time(NULL));

    sched->threads_pool = (pthread_t *) malloc(sizeof(pthread_t) * threads);
    sched->count = 0;
    sched->end_count = 0;
    sched->terminate = 0;
    sched->threads_running = 0;
    sched->threads = threads;
//    sched->current_fibers = (fiber ***)malloc(sizeof(fiber **) * threads);
//    memset(sched->current_fibers, 0, sizeof(fiber **) * threads);
    create_list(&sched->garbage);

    asm volatile("mfence"::
    : "memory");
#if INTERRUPT_ENABLED
    // Sets signals parameters for interrupting fibers

    // mask SIGALRM in all threads by default
    sigemptyset(&(sched->sigact.sa_mask));
    sigaddset(&(sched->sigact.sa_mask), SIGALRM);

    // we need a signal handler.
    // The default is to call abort() and
    // setting SIG_IGN might cause the signal
    // to not be delivered at all.

    memset(&sched->sigact, 0, sizeof(sched->sigact));
    sched->sigact.sa_handler = handler;
    sigaction(SIGALRM, &sched->sigact, NULL);

    // Create thread for sending signals to handlers-threads
    pthread_create(&sched->signal_thread, 0, signal_thread_func, sched);

#if REALTIME
    pthread_setschedprio(sched->signal_thread, 99);
#endif

#endif

#if THREAD_STAT
    sched->threads_histories = (history_node **)malloc(threads * sizeof(history_node *));
#endif

    asm volatile("mfence"::
    : "memory");
    // Create manager for scheduler
    create_scheduler_manager(sched);

    asm volatile("mfence"::
    : "memory");
    // Create handlers-threads
    for (unsigned long index = 0; index < threads; index++) {
        pthread_attr_t attr;
        pthread_attr_init(&attr);
        unsigned long *args = (unsigned long *) malloc(sizeof(unsigned long) * 2);
        args[0] = index;
        args[1] = (unsigned long) sched;
        pthread_create(&sched->threads_pool[index], &attr, run_fibers_handler, (void *) args);

#if REALTIME
        pthread_setschedprio(sched->threads_pool[index], 99);
#endif
    }

    return 0;
}

void run_scheduler(scheduler *sched) {
    sched->threads_running = 1;
}

fiber *spawn(scheduler *sched, fiber_routine routine, void *args) {
    asm volatile("mfence"::
    : "memory");
    fiber *fib = create_fiber(routine, args);
    fib->sched = sched;
    asm volatile("mfence"::
    : "memory");
    insert_fiber(sched, fib);
    asm volatile("mfence"::
    : "memory");
    return fib;
}

void sleep_for(unsigned long duration) {
    fiber *current_fiber = get_current_fiber();
    if (!current_fiber) {
        printf("[ERROR] Out of fiber\n");
        exit(1);
    }

    // This lock will be unlocked in run_task function after switch context

//    lock_spinlock(&current_fiber->lock);

    fiber *temp = current_fiber;

    // Sets time for wake up
    temp->wakeup = clock();
    temp->wakeup += clock_to_microseconds((long) duration);

    if (temp->state == RUNNING) {
        temp->state = SLEEPING;

        // Return to run_task function

#if FIBER_STAT
        update_fiber_history(temp);
#endif
        switch_context(&temp->context, &temp->external_context);

        // This unlock unlocking lock locked in run_task function
//        unlock_spinlock(&temp->lock);
    } else {
        printf("[ERROR] Sleep wrong state %d\n", temp->state);
        exit(1);
    }
}

fiber *submit(fiber_routine routine, void *args) {
    scheduler *current_scheduler = get_current_scheduler();
    fiber *current_fiber = get_current_fiber();
    asm volatile("mfence"::
    : "memory");
    fiber *temp = current_fiber;
    if (temp == NULL) {
        printf("[ERROR] Submit out of fiber\n");
        exit(1);
    }

    // Lock for create new fiber - block context switching
//    lock_spinlock(&temp->lock);
    fiber *fib = create_fiber(routine, args);
    fib->external_context = temp->context;
    fib->sched = current_scheduler;
    fib->level = current_fiber->level + 1;
    asm volatile("mfence"::
    : "memory");
    insert_fiber(current_scheduler, fib);
//    unlock_spinlock(&temp->lock);
    asm volatile("mfence"::
    : "memory");

    return fib;
}

void join(fiber *fib) {
    asm volatile("mfence"::
    : "memory");
    fiber *temp = get_current_fiber();
    while (1) {
        asm volatile("mfence"::
        : "memory");
        if (fib->state != TERMINATED) {
            if (temp) {
                // If we in fiber
                sleep_for(200);
            } else {
                // If we not in fiber
                usleep(200);
            }
        } else {
            break;
        }
    }
}

void shutdown(scheduler *sched) {
    asm volatile("mfence"::
    : "memory");
    while (sched->count != sched->end_count) {
//      printf("%ld %ld\n", sched->count, sched->end_count);
        asm volatile("mfence"::
        : "memory");
        // Sleep if failed
        usleep(200);
    }
    sched->threads_running = 0;
}

void yield() {
    fiber *current_fiber = get_current_fiber();

    if (current_fiber == NULL) {
        printf("ERROR current fiber is NULL\n");
        return;
    }

    // This lock will be unlocked in run_task function after switch context
//    lock_spinlock(&current_fiber->lock);
    fiber *temp = current_fiber;

    if (temp->state == RUNNING) {
        temp->state = RUNNABLE;

        // Returns to run_task function
#if FIBER_STAT
        update_fiber_history(temp);
#endif
        switch_context(&temp->context, &temp->external_context);

        // This unlock unlock lock locked in run_task function before switch context
//        unlock_spinlock(&temp->lock);
    } else {
        printf("[ERROR] Yield wrong state %d\n", temp->state);
        exit(1);
    }
}

int free_fibers(scheduler *sched) {
    return free_list(&sched->garbage, delete_fiber);
}

int terminate_scheduler(scheduler *sched) {
    asm volatile("mfence"::
    : "memory");
    // Shutdown before terminate
    shutdown(sched);

    sched->terminate = 1;
    delete_current_scheduler();

#if INTERRUPT_ENABLED
    // Join threads
    pthread_join(sched->signal_thread, NULL);
#endif

    for (size_t i = 0; i < sched->threads; i++) {
        pthread_join(sched->threads_pool[i], NULL);
    }

    free(sched->threads_pool);

#if THREAD_STAT
    save_thread_history(sched);
#endif

    // Scheduler manager teminate
    free_scheduler_manager(sched);

//    free(sched->current_fibers);
    free_fibers(sched);

    return 0;
}

void create_history() {
    create_list(&history);
}

#if FIBER_STAT
void update_fiber_history(fiber *fiber)
{
    fiber->last->next = (history_node *)malloc(sizeof(history_node));
    fiber->last->next->fiber_state = fiber->state;
    fiber->last->next->start = clock();
    fiber->last->next->next = NULL;
    fiber->last->next->prev = fiber->last;
    fiber->last = fiber->last->next;
}

void save_fiber_history(fiber *fiber)
{
    history_save *save = (history_save *)malloc(sizeof(history_save));
    save->tail = fiber->last;
    list_push_back(&history, &save->node);
}
#endif

#if THREAD_STAT
void update_thread_history(thread_state state)
{
    history_node *last = current_scheduler->threads_histories[get_current_thread_id()];

    last->next = (history_node *)malloc(sizeof(history_node));
    last->next->thread_state = state;
    last->next->start = clock();
    last->next->next = NULL;
    last->next->prev = last;
    current_scheduler->threads_histories[get_current_thread_id()] = last->next;
}

void save_thread_history(scheduler *sched)
{
    for (unsigned long i = 0; i < sched->threads; i++)
    {
        history_save *save = (history_save *)malloc(sizeof(history_save));
        save->tail = sched->threads_histories[i];
        list_push_back(&history, &save->node);
    }
}
#endif

static void print_item_history(int num, history_node *node) {
    history_node *tail = node;
    history_node *head;

    while (tail->prev) {
        tail = tail->prev;
    }

    head = tail;

    while (head) {
#if FIBER_STAT
        printf("%d %d %ld\n", num, head->fiber_state, head->start);
#endif

#if THREAD_STAT
        if (head->prev != NULL && (head->prev->start + 100 > head->start || head->prev->thread_state == head->thread_state))
        {
            head->prev->next = head->next;

            if (head->next != NULL)
            {
                head->next->prev = head->prev;
            }

            head = head->next;
            continue;
        }

        printf("%d %d %ld\n", num, head->thread_state, head->start);
#endif
        head = head->next;
    }
}

void print_history() {
    printf("Number State Start\n");
    history_save *head = (history_save *) history.start;

    int count = 0;
    while (head) {
        print_item_history(count, head->tail);
        head = (history_save *) (head->node.next);
        count++;
    }
}

static void free_callback(list_node *node) {
    if (node) {
        history_save *save = (history_save *) node;
        history_node *tail = save->tail;
        history_node *curr = tail;

        while (curr) {
            history_node *tmp = curr->prev;
            free(curr);
            curr = tmp;
        }
    }
}

void free_history() {
    free_list(&history, free_callback);
}

void delete_current_scheduler() {
    struct pthread_node next;
    next.thread_id = get_current_thread_id();

    struct hash_map_node *node = hash_map_remove(
        &fpl_manager.current_scheduler,
        &next.core
    );

    if (node != NULL) {
        free(node);
    }
}