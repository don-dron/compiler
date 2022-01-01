#include <scheduler/coroutine.h>

//thread_local coroutine *current_coroutine = NULL;

static void trampoline() {
    coroutine *current_coroutine = get_current_coroutine();

    current_coroutine->routine(current_coroutine->args);

    current_coroutine->complete = 1;

    suspend();
}

static int setup(coroutine *coroutine_, void (*trampoline)(void *)) {
    // Allocate memory for stack and context
    void *start = mmap(/*addr=*/0, /*length=*/STACK_SIZE,
            /*prot=*/PROT_READ | PROT_WRITE,
            /*flags=*/MAP_PRIVATE | 0x20,
            /*fd=*/-1, /*offset=*/0);

    int ret = mprotect(/*addr=*/(void *) ((size_t) start + pages_to_bytes(4)),
            /*len=*/pages_to_bytes(4),
            /*prot=*/PROT_NONE);

    if (ret) {
        munmap(start, STACK_SIZE);
        return ret;
    }

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

    // Machine word size, usually 8 bytes(x86)
    stackBuilder.word_size = sizeof(void *);

    // For rbp register(shift 16 bytes)
    align_next_push(&stackBuilder, 16);
    // Allocate stack on allocated memory
    allocate(&stackBuilder, sizeof(stack_saved_context));

    stack_saved_context *saved_context = (stack_saved_context *) stackBuilder.top;

    // Rsp - stack pointer - new stack saved context - pointer to top allocated stack.
    // Rip - instruction pointer to trampoline function, after switch context automatically executed trampoline function.

    saved_context->rip = (void *) trampoline;
    coroutine_->routine_context.rsp = saved_context;

    // Save allocated memory pointer
    coroutine_->routine_context.stack = start;

    return 0;
}

int create_coroutine(coroutine *new_coroutine, void (*routine)(), void *args) {
    new_coroutine->routine = routine;
    new_coroutine->complete = 0;
    new_coroutine->args = args;

    int ret = setup(new_coroutine, trampoline);

    if (ret) {
        return ret;
    }

    new_coroutine->external_routine = get_current_coroutine();
    return 0;
}

void suspend() {
    coroutine *next_coroutine = get_current_coroutine();
    save_current_coroutine(next_coroutine->external_routine);
    switch_to_caller(next_coroutine);
}

void resume(coroutine *coroutine_by_resume) {
    if (coroutine_by_resume->complete) {
        return;
    }

    save_current_coroutine(coroutine_by_resume);

    switch_context(&coroutine_by_resume->caller_context, &coroutine_by_resume->routine_context);
}

coroutine *get_current_coroutine() {
    struct pthread_node to_find;
    to_find.thread_id = get_current_thread_id();

    struct pthread_node *node = ((struct pthread_node *) hash_map_find(&fpl_manager.current_coroutine,
                                                                       &to_find.core));

    if (node == NULL) {
        return NULL;
    }

    return (coroutine *) node->ptr;
}

void switch_to_caller(coroutine *coroutine_) {
    switch_context(&coroutine_->routine_context, &coroutine_->caller_context);
}

int free_coroutine(coroutine *coroutine_) {
    munmap(coroutine_->routine_context.stack, STACK_SIZE);
    return 0;
}