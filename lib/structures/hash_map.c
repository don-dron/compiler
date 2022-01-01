#include <structures/hash_map.h>

void hash_map_print(struct hash_map *map, void (*print_node)(struct hash_map_node *node)) {
    for (int i = 0; i < 16; i++) {
        printf("bucket %d: ", i);

        struct hash_map_node *current = map->buckets[i];

        if (current == NULL) {
            printf("NULL");
        } else {
            print_node(current);
        }
        while (current != NULL) {
            current = current->next;
            printf(" -> ");

            if (current == NULL) {
                printf("NULL");
            } else {
                print_node(current);
            }
        }

        printf("\n");
    }
}

void hash_map_init(
        struct hash_map *map,
        int (*cmp)(struct hash_map_node *a, struct hash_map_node *b),
        int (*hash_function)(struct hash_map_node *node)) {
    map->cmp = cmp;
    map->hash_function = hash_function;
    map->size = 0;
    memset(map->buckets, 0, sizeof(struct hash_map_node *) * 16);
}

struct hash_map_node *hash_map_insert(
        struct hash_map *map,
        struct hash_map_node *node) {
    int hash = map->hash_function(node);
    struct hash_map_node *current = map->buckets[hash % 16];
    struct hash_map_node *prev = NULL;

    node->next = NULL;

    if (current == NULL) {
        map->size++;
        map->buckets[hash % 16] = node;
        return NULL;
    }

    while (1) {
        if (current == NULL) {
            map->size++;
            prev->next = node;
            return NULL;
        }

        int result = map->cmp(current, node);
        if (result < 0) {
            prev = current;
            current = current->next;
        } else if (result > 0) {
            node->next = current;

            if (prev != NULL) {
                prev->next = node;
            } else {
                map->buckets[hash % 16] = node;
            }
            map->size++;
            return NULL;
        } else {
            node->next = current->next;

            if (prev != NULL) {
                prev->next = node;
            } else {
                map->buckets[hash % 16] = node;
            }
            return current;
        }
    }
}

struct hash_map_node *hash_map_find(
        struct hash_map *map,
        struct hash_map_node *node) {
    int hash = map->hash_function(node);
    struct hash_map_node *current = map->buckets[hash % 16];

    node->next = NULL;

    while (1) {
        if (current == NULL) {
            return NULL;
        }

        int result = map->cmp(current, node);
        if (result < 0) {
            current = current->next;
        } else if (result > 0) {
            return NULL;
        } else {
            return current;
        }
    }
}

struct hash_map_node *hash_map_remove(
        struct hash_map *map,
        struct hash_map_node *node) {
    int hash = map->hash_function(node);
    struct hash_map_node *current = map->buckets[hash % 16];
    struct hash_map_node *prev = NULL;

    if (current == NULL) {
        return NULL;
    }

    node->next = NULL;

    while (1) {
        if (current == NULL) {
            return NULL;
        }

        int result = map->cmp(current, node);
        if (result < 0) {
            prev = current;
            current = current->next;
        } else if (result > 0) {
            return NULL;
        } else {
            if (prev != NULL) {
                prev->next = current->next;
            } else {
                map->buckets[hash % 16] = current->next;
            }
            map->size--;
            return current;
        }
    }
}

void hash_map_free(struct hash_map *map) {
    free(map->buckets);
}