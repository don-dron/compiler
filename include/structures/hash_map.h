#pragma once

#include <root.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

struct hash_map_node {
    struct hash_map_node *next;
};

struct hash_map {
    int size;

    int (*hash_function)(struct hash_map_node *node);

    int (*cmp)(struct hash_map_node *a, struct hash_map_node *b);

    struct hash_map_node *buckets[16];
};

void hash_map_init(
        struct hash_map *map,
        int (*cmp)(struct hash_map_node *a, struct hash_map_node *b),
        int (*hash_function)(struct hash_map_node *node));

struct hash_map_node *hash_map_insert(struct hash_map *map, struct hash_map_node *node);

struct hash_map_node *hash_map_find(struct hash_map *map, struct hash_map_node *node);

struct hash_map_node *hash_map_remove(struct hash_map *map, struct hash_map_node *node);

void hash_map_print(struct hash_map *map, void (*print_node)(struct hash_map_node *node));

void hash_map_free(struct hash_map *map);
