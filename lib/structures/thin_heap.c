#include <structures/thin_heap.h>

#define NOT_IN_HEAP UINT_MAX

void heap_init(struct thin_heap *thin_heap, heap_prio_t prio,swap_f swp)
{
    thin_heap->head = NULL;
    thin_heap->min = NULL;
    thin_heap->prio = prio;
    thin_heap->swp = swp;
}

static void __heap_link(struct thin_heap_node *root,
                               struct thin_heap_node *child)
{
    child->parent = root;
    child->next = root->child;
    root->child = child;
    root->degree++;
}

static struct thin_heap_node *__heap_merge(struct thin_heap_node *a,
                                             struct thin_heap_node *b)
{
    struct thin_heap_node *head = NULL;
    struct thin_heap_node **pos = &head;

    while (a && b)
    {
        if (a->degree < b->degree)
        {
            *pos = a;
            a = a->next;
        }
        else
        {
            *pos = b;
            b = b->next;
        }
        pos = &(*pos)->next;
    }
    if (a)
        *pos = a;
    else
        *pos = b;
    return head;
}

static struct thin_heap_node *__heap_reverse(struct thin_heap_node *h)
{
    struct thin_heap_node *tail = NULL;
    struct thin_heap_node *next;

    if (!h)
        return h;

    h->parent = NULL;
    while (h->next)
    {
        next = h->next;
        h->next = tail;
        tail = h;
        h = next;
        h->parent = NULL;
    }
    h->next = tail;
    return h;
}

static void __heap_min(struct thin_heap *thin_heap, struct thin_heap_node **prev, struct thin_heap_node **node)
{
    heap_prio_t higher_prio = thin_heap->prio;
    struct thin_heap_node *_prev, *cur;
    *prev = NULL;

    if (!thin_heap->head)
    {
        *node = NULL;
        return;
    }

    *node = thin_heap->head;
    _prev = thin_heap->head;
    cur = thin_heap->head->next;
    while (cur)
    {
        if (higher_prio(cur, *node))
        {
            *node = cur;
            *prev = _prev;
        }
        _prev = cur;
        cur = cur->next;
    }
}

static void __heap_union(struct thin_heap *thin_heap,
                                struct thin_heap_node *h2)
{
    heap_prio_t higher_prio = thin_heap->prio;
    struct thin_heap_node *h1;
    struct thin_heap_node *prev, *x, *next;
    if (!h2)
        return;
    h1 = thin_heap->head;
    if (!h1)
    {
        thin_heap->head = h2;
        return;
    }
    h1 = __heap_merge(h1, h2);
    prev = NULL;
    x = h1;
    next = x->next;
    while (next)
    {
        if (x->degree != next->degree ||
            (next->next && next->next->degree == x->degree))
        {
            /* nothing to do, advance */
            prev = x;
            x = next;
        }
        else if (higher_prio(x, next))
        {
            /* x becomes the root of next */
            x->next = next->next;
            __heap_link(x, next);
        }
        else
        {
            /* next becomes the root of x */
            if (prev)
                prev->next = next;
            else
                h1 = next;
            __heap_link(next, x);
            x = next;
        }
        next = x->next;
    }
    thin_heap->head = h1;
}

static struct thin_heap_node *__heap_extract_min(struct thin_heap *thin_heap)
{
    struct thin_heap_node *prev, *node;
    __heap_min(thin_heap, &prev, &node);
    if (!node)
        return NULL;
    if (prev)
        prev->next = node->next;
    else
        thin_heap->head = node->next;
    __heap_union(thin_heap, __heap_reverse(node->child));
    return node;
}

/* insert (and reinitialize) a node into the thin_heap */
void heap_insert(struct thin_heap *thin_heap, struct thin_heap_node *node)
{
    heap_prio_t higher_prio = thin_heap->prio;
    struct thin_heap_node *min;
    node->child = NULL;
    node->parent = NULL;
    node->next = NULL;
    node->degree = 0;
    if (thin_heap->min && higher_prio(node, thin_heap->min))
    {
        /* swap min cache */
        min = thin_heap->min;
        min->child = NULL;
        min->parent = NULL;
        min->next = NULL;
        min->degree = 0;
        __heap_union(thin_heap, min);
        thin_heap->min = node;
    }
    else
        __heap_union(thin_heap, node);
}

static void __uncache_min(struct thin_heap *thin_heap)
{
    struct thin_heap_node *min;
    if (thin_heap->min)
    {
        min = thin_heap->min;
        thin_heap->min = NULL;
        heap_insert(thin_heap, min);
    }
}

/* merge addition into target */
void heap_union(struct thin_heap *target, struct thin_heap *addition)
{
    /* first insert any cached minima, if necessary */
    __uncache_min(target);
    __uncache_min(addition);
    __heap_union(target, addition->head);
    /* this is a destructive merge */
    addition->head = NULL;
}

struct thin_heap_node *heap_peek(struct thin_heap *thin_heap)
{
    if (!thin_heap->min)
        thin_heap->min = __heap_extract_min(thin_heap);
    return thin_heap->min;
}

struct thin_heap_node *heap_take(struct thin_heap *thin_heap)
{
    struct thin_heap_node *node;
    if (!thin_heap->min)
        thin_heap->min = __heap_extract_min(thin_heap);
    node = thin_heap->min;
    thin_heap->min = NULL;
    if (node)
        node->degree = NOT_IN_HEAP;
    return node;
}

void heap_decrease(struct thin_heap *thin_heap,
                                 struct thin_heap_node *node)
{
    heap_prio_t higher_prio = thin_heap->prio;
    struct thin_heap_node *parent;
    struct thin_heap_node **tmp_ref;

    /* node's priority was decreased, we need to update its position */
    if (!node->ref)
        return;
    if (thin_heap->min != node)
    {
        if (thin_heap->min && higher_prio(node, thin_heap->min))
            __uncache_min(thin_heap);
        /* bubble up */
        parent = node->parent;
        while (parent && higher_prio(node, parent))
        {
            /* swap parent and node */
            thin_heap->swp(node,parent);
            /* swap references */
            if (parent->ref)
                *(parent->ref) = node;
            *(node->ref) = parent;
            tmp_ref = parent->ref;
            parent->ref = node->ref;
            node->ref = tmp_ref;
            /* step up */
            node = parent;
            parent = node->parent;
        }
    }
}

void heap_delete(struct thin_heap *thin_heap,
                               struct thin_heap_node *node)
{
    struct thin_heap_node *parent, *prev, *pos;
    struct thin_heap_node **tmp_ref;

    if (!node->ref) /* can only delete if we have a reference */
        return;
    if (thin_heap->min != node)
    {
        /* bubble up */
        parent = node->parent;
        while (parent)
        {
            /* swap parent and node */
            thin_heap->swp(node,parent);
            /* swap references */
            if (parent->ref)
                *(parent->ref) = node;
            *(node->ref) = parent;
            tmp_ref = parent->ref;
            parent->ref = node->ref;
            node->ref = tmp_ref;
            /* step up */
            node = parent;
            parent = node->parent;
        }
        /* now delete:
		 * first find prev */
        prev = NULL;
        pos = thin_heap->head;
        while (pos != node)
        {
            prev = pos;
            pos = pos->next;
        }
        /* we have prev, now remove node */
        if (prev)
            prev->next = node->next;
        else
            thin_heap->head = node->next;
        __heap_union(thin_heap, __heap_reverse(node->child));
    }
    else
        thin_heap->min = NULL;
    node->degree = NOT_IN_HEAP;
}