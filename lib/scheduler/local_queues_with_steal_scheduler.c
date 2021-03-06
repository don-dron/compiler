#include <scheduler/local_queues_with_steal_scheduler.h>

static inline fiber_node *steal_task(list *queue, unsigned long thread_number)
{
    fiber_node *stolen = NULL;
    size_t threads = get_current_scheduler()->threads;
    unsigned long current = thread_number;
    while (1)
    {
        current += 1;
        current %= threads;
        stolen = (fiber_node *)list_pop_back(queue);
        if (stolen != 0 || current == thread_number)
        {
            return stolen;
        }
    }
}

int create_scheduler_manager(scheduler *sched)
{
    sched->manager = (scheduler_manager *)malloc(sizeof(scheduler_manager));
    scheduler_manager *manager = sched->manager;

    manager->queues = (list **)calloc(sizeof(list *), sched->threads);

    for (unsigned long index = 0; index < sched->threads; index++)
    {
        manager->queues[index] = (list *)malloc(sizeof(list));
        create_list(manager->queues[index]);
    }

    return 0;
}

fiber *get_from_pool()
{
    scheduler * scheduler = get_current_scheduler();
    list *queue = scheduler->manager->queues[rand() % scheduler->threads];
    fiber_node *node = (fiber_node *)list_pop_front(queue);

    if (node)
    {
        fiber *res = node->fib;
        free(node);
        return res;
    }
    return NULL;
}

void return_to_pool(scheduler *sched, fiber *fib)
{
    fiber_node *fib_node = (fiber_node *)malloc(sizeof(fiber_node));
    fib_node->fib = fib;
    list_push_back(sched->manager->queues[fib->id % sched->threads], &fib_node->lst_node);
}

int free_scheduler_manager(scheduler *sched)
{
    scheduler_manager *manager = sched->manager;
    for (size_t i = 0; i < sched->threads; i++)
    {
        if (manager->queues[i]->size != 0)
        {
            printf("Panic\n");
            abort();
        }
        free(manager->queues[i]);
    }

    free(manager->queues);

    free(sched->manager);
    return 0;
}