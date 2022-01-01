#pragma once

#include <stdlib.h>
#include <assert.h>

typedef int (*splay_tree_cmp)(void *left, void *right);

typedef struct splay_node {
    struct splay_node *parent, *left, *right;
    void *value;
} splay_node;

typedef struct splay_tree {
    splay_node *root;
    splay_tree_cmp comp;
    int size;
} splay_tree;

splay_tree *splay_tree_new_tree(splay_tree_cmp comp);

splay_node *splay_tree_insert(splay_tree *tree, void *value);

splay_node *splay_tree_find(splay_tree *tree, void *value);

splay_node *splay_tree_first(splay_tree *tree);

splay_node *splay_tree_next(splay_node *node);

splay_node *splay_tree_last(splay_tree *tree);

void *splay_tree_contents(splay_tree *tree);

void splay_tree_delete(splay_tree *tree, void *value);

void splay_tree_delete_hint(splay_tree *tree, splay_node *node);

#if DEBUG
int check_node_sanity(splay_node *x, void *floor, void *ceil, comparator comp);
void check_sanity(splay_tree *tree);
#endif