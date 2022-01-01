#include <scheduler/thin_heap_scheduler.h>

static int cmp(const void *lhs, const void *rhs)
{
    fiber_node *first = (fiber_node *)lhs;
    fiber_node *second = (fiber_node *)rhs;

    if (first->fib->level < second->fib->level)
    {
        return 0;
    }
    else if (first->fib->level > second->fib->level)
    {
        return 1;
    }
    else
    {
        if (first->fib->vruntime < second->fib->vruntime)
        {
            return 0;
        }
        else if (first->fib->vruntime > second->fib->vruntime)
        {
            return 1;
        }
        else
        {
            return ((unsigned long)first) > ((unsigned long)second);
        }
    }
}

static void swp(struct thin_heap_node *first, struct thin_heap_node *second)
{
    fiber_node *f = (fiber_node *)first;
    fiber_node *s = (fiber_node *)second;
    fiber *temp;

    temp = f->fib;
    f->fib = s->fib;
    s->fib = temp;
}

int create_scheduler_manager(scheduler *sched)
{
    sched->manager = (scheduler_manager *)malloc(sizeof(scheduler_manager));
    scheduler_manager *manager = sched->manager;
    init_spinlock(&manager->lock);

    manager->heap = (struct thin_heap *)malloc(sizeof(struct thin_heap));
    heap_init(manager->heap, (heap_prio_t)cmp, (swap_f)swp);
    asm volatile("mfence" ::
                     : "memory");
    return 0;
}

fiber *get_from_pool()
{
    scheduler* current_scheduler = get_current_scheduler();
    asm volatile("mfence" ::
                     : "memory");
    struct thin_heap *heap = current_scheduler->manager->heap;

    lock_spinlock(&current_scheduler->manager->lock);
    fiber_node *node = (fiber_node *)heap_take(heap);
    unlock_spinlock(&current_scheduler->manager->lock);
    asm volatile("mfence" ::
                     : "memory");

    if (node)
    {
        fiber *res = node->fib;
        free(node);
        return res;
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
    fiber_node *fib_node = (fiber_node *)malloc(sizeof(fiber_node));
    fib_node->fib = fib;
    asm volatile("mfence" ::
                     : "memory");

    lock_spinlock(&sched->manager->lock);
    heap_insert(sched->manager->heap, (struct thin_heap_node *)fib_node);
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