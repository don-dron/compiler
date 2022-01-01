#pragma once

#include <stdlib.h>
#include <limits.h>

#define NOT_IN_HEAP UINT_MAX

struct thin_heap_node
{
    struct thin_heap_node *parent;
    struct thin_heap_node *next;
    struct thin_heap_node *child;

    unsigned int degree;
    struct thin_heap_node **ref;
};

typedef int (*heap_prio_t)(struct thin_heap_node *a, struct thin_heap_node *b);
typedef void (*swap_f)(struct thin_heap_node *a, struct thin_heap_node *b);

struct thin_heap
{
    struct thin_heap_node *head;
    struct thin_heap_node *min;
    heap_prio_t prio;
    swap_f swp;
};

void heap_init(struct thin_heap *thin_heap, heap_prio_t prio, swap_f swp);

void heap_insert(struct thin_heap *thin_heap,
                 struct thin_heap_node *node);

void heap_union(struct thin_heap *target, struct thin_heap *addition);

struct thin_heap_node *heap_peek(struct thin_heap *thin_heap);

struct thin_heap_node *heap_take(struct thin_heap *thin_heap);

void heap_decrease(struct thin_heap *thin_heap,
                   struct thin_heap_node *node);

void heap_delete(struct thin_heap *thin_heap,
                 struct thin_heap_node *node);