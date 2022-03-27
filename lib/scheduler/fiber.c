#include <scheduler/fiber.h>

//thread_local fiber *volatile current_fiber = NULL;
list history;
unsigned long volatile id = 0;

static unsigned long generate_id() {
    return inc(&id);
}

fiber *get_current_fiber() {
    struct pthread_node to_find;
    to_find.thread_id = get_current_thread_id();
    asm volatile("mfence"::
    : "memory");

    struct pthread_node *node = ((struct pthread_node *) hash_map_find(&fpl_manager.current_fiber, &to_find.core));

    asm volatile("mfence"::
    : "memory");
    if (node == NULL) {
        return NULL;
    }

    return (fiber *) node->ptr;
}

static void fiber_trampoline() {
    fiber *temp = get_current_fiber();

    temp->state = RUNNING;

    // Unlock after lock in run_task
//    unlock_spinlock(&temp->lock);

     temp->routine(temp->args);

    // Lock for swtich context, unlocked in run_task
//    lock_spinlock(&temp->lock);

    if (temp->state == RUNNING) {
        temp->state = TERMINATED;

        // To run task
#if FIBER_STAT
        update_fiber_history(temp);
#endif
        switch_context(&temp->context, &temp->external_context);

        // Unreachable
        printf("Wrong state 2 \n");
        exit(1);
    } else {
        printf("Wrong state 3 \n");
        exit(1);
    }
}

fiber *create_fiber(fiber_routine routine, void *args) {
    asm volatile("mfence"::
    : "memory");
    fiber *new_fiber = (fiber *) malloc(sizeof(fiber));

    new_fiber->id = generate_id();
    new_fiber->routine = routine;
    new_fiber->state = STARTING;
    new_fiber->parent = get_current_fiber();
    new_fiber->args = args;
    new_fiber->level = 0;

#if FIBER_STAT
    new_fiber->last = (history_node *)malloc(sizeof(history_node));
    new_fiber->last->fiber_state = STARTING;
    new_fiber->last->start = clock();
    new_fiber->last->prev = NULL;
    new_fiber->last->next = NULL;
#endif

    new_fiber->vruntime = 0;

//    init_spinlock(&new_fiber->lock);

    setup_trampoline(new_fiber);

    asm volatile("mfence":: :"memory");
    return new_fiber;
}

void free_fiber(fiber *fiber) {
    free(fiber->context.stack);

#if FIBER_STAT
    save_fiber_history(fiber);
#endif
}

void setup_trampoline(fiber *new_fiber) {
    // Allocate memory for stack and context
    void *start = malloc(STACK_SIZE);
//            mmap(/*addr=*/0, /*length=*/STACK_SIZE,
//            /*prot=*/PROT_READ | PROT_WRITE,
//            /*flags=*/MAP_PRIVATE | 0x20,
//            /*fd=*/-1, /*offset=*/0);

    //int ret =
    mprotect(/*addr=*/(void *) ((size_t) start + pages_to_bytes(4)),
            /*len=*/pages_to_bytes(4),
            /*prot=*/PROT_NONE);

    stack_builder stackBuilder;
    // Set top stack address
    //
    // Programm heap
    //
    // 0xfffff
    //   ^
    //   |
    //   |
    //   ^
    //   |------------------ top = start + stack_size - 1   - Because stack grows downward
    //   |                                                                  |
    //   |                   Coroutine stack memory.                        |
    //   |                                                                  |
    //   |------------------ start                                          v
    //   ^
    //   |
    //   |
    //   0
    stackBuilder.top = (char *) ((size_t) start + STACK_SIZE - 1);

    // Machine word size, usually 8 bytes
    stackBuilder.word_size = sizeof(void *);

    // For rbp register(shift 16 bytes)
    align_next_push(&stackBuilder, 16);
    // Allocate stack on allocated memory
    allocate(&stackBuilder, sizeof(stack_saved_context));

    stack_saved_context *saved_context = (stack_saved_context *) stackBuilder.top;

    // Rsp - stack pointer - new stack saved context - pointer to top allocated stack.
    // Rip - instruction pointer to trampoline function, after switch context automatically executed trampoline function.

    saved_context->rip = (void *) fiber_trampoline;
    new_fiber->context.rsp = saved_context;

    // Save allocated memory pointer
    new_fiber->context.stack = start;
}

void save_current_fiber(fiber *fib) {
    struct pthread_node *next = (struct pthread_node *) malloc(sizeof(struct pthread_node));
    next->ptr = fib;
    next->thread_id = get_current_thread_id();

    struct hash_map_node *node = hash_map_insert(
            &fpl_manager.current_fiber,
            &next->core
    );

    if (node != NULL) {
        free(node);
    }
}


void delete_current_fiber() {
    struct pthread_node next;
    next.thread_id = get_current_thread_id();

    struct hash_map_node *node = hash_map_remove(
            &fpl_manager.current_fiber,
            &next.core
    );

    if (node != NULL) {
        free(node);
    }
}