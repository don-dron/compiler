#pragma once

#include <stdlib.h>
#include <stdio.h>

struct fib_heap_data
{
    int key;
};

struct fib_heap_node
{
    struct fib_heap_data *data;
    unsigned int degree;
    char marked;
    struct fib_heap_node *parent;
    struct fib_heap_node *child;
    struct fib_heap_node *left;
    struct fib_heap_node *right;
};

struct fib_heap
{
    struct fib_heap_node *the_one;
    struct fib_heap_node **cons_array;
    int (*compr)(struct fib_heap_data *, struct fib_heap_data *);
    unsigned int total_nodes;
};

struct fib_heap *fibheap_init(int (*compr)(struct fib_heap_data *, struct fib_heap_data *));

struct fib_heap_node *fibheap_insert(struct fib_heap *H, struct fib_heap_data *d);

struct fib_heap_data *fibheap_read(struct fib_heap *H);

struct fib_heap_data *fibheap_extract(struct fib_heap *H);

struct fib_heap *fibheap_union(struct fib_heap *a, struct fib_heap *b);

void fibheap_increase(struct fib_heap *H, struct fib_heap_node *node);

void fibheap_decrease(struct fib_heap *H, struct fib_heap_node *node);

void fibheap_delete(struct fib_heap *H, struct fib_heap_node *node);

void fibheap_destroy(struct fib_heap *H);