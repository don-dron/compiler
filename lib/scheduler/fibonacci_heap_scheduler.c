#include <scheduler/fibonacci_heap_scheduler.h>

static int cmp(struct fib_heap_data*lhs, struct fib_heap_data*rhs)
{
    fiber *first = (fiber *)lhs;
    fiber *second = (fiber *)rhs;

    if (first->level < second->level)
    {
        return 1;
    }
    else if (first->level > second->level)
    {
        return -1;
    }
    else
    {
        if (first->vruntime < second->vruntime)
        {
            return -1;
        }
        else if (first->vruntime > second->vruntime)
        {
            return 1;
        }
        else
        {
            if (((unsigned long)first) > ((unsigned long)second))
            {
                return -1;
            }
            else if (((unsigned long)first) < ((unsigned long)second))
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    }
}

int create_scheduler_manager(scheduler *sched)
{
    sched->manager = (scheduler_manager *)malloc(sizeof(scheduler_manager));
    scheduler_manager *manager = sched->manager;
    init_spinlock(&manager->lock);

    manager->heap = fibheap_init(cmp);
    asm volatile("mfence" ::
                     : "memory");
    return 0;
}

fiber *get_from_pool()
{
    scheduler* current_scheduler = get_current_scheduler();
    struct fib_heap *heap = current_scheduler->manager->heap;

    lock_spinlock(&current_scheduler->manager->lock);
    fiber * node = (fiber*)fibheap_extract(heap);
    unlock_spinlock(&current_scheduler->manager->lock);
    asm volatile("mfence" ::
                     : "memory");

    if (node != NULL)
    {      
        return node;
    }
    else
    {
        return NULL;
    }
}

void return_to_pool(scheduler *sched, fiber *fib)
{
    asm volatile("mfence" ::
                     : "memory");

    lock_spinlock(&sched->manager->lock);
    fibheap_insert(sched->manager->heap, (struct fib_heap_data*)fib);
    unlock_spinlock(&sched->manager->lock);
    asm volatile("mfence" ::
                     : "memory");
}

int free_scheduler_manager(scheduler *sched)
{
    scheduler_manager *manager = sched->manager;

    free(manager->heap);

    free(sched->manager);
    return 0;
}