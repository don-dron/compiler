#include <scheduler/manager.h>

struct fibers_pool_manager fpl_manager = {
        .current_fiber={
                .size=0,
                .hash_function=pthread_hash_node,
                .cmp=pthread_cmp_node,
                .buckets={
                        NULL, NULL, NULL, NULL,
                        NULL, NULL, NULL, NULL,
                        NULL, NULL, NULL, NULL,
                        NULL, NULL, NULL, NULL}
        },
        .current_coroutine={
                .size=0,
                .hash_function=pthread_hash_node,
                .cmp=pthread_cmp_node,
                .buckets={
                        NULL, NULL, NULL, NULL,
                        NULL, NULL, NULL, NULL,
                        NULL, NULL, NULL, NULL,
                        NULL, NULL, NULL, NULL}
        },
        .current_scheduler={
                .size=0,
                .hash_function=pthread_hash_node,
                .cmp=pthread_cmp_node,
                .buckets={
                        NULL, NULL, NULL, NULL,
                        NULL, NULL, NULL, NULL,
                        NULL, NULL, NULL, NULL,
                        NULL, NULL, NULL, NULL}
        }
};

int get_current_thread_id() {
    uint64_t tid;
    pthread_threadid_np(NULL, &tid);
    return tid;
}

int pthread_cmp_node(struct hash_map_node *a, struct hash_map_node *b) {
    return ((struct pthread_node *) a)->thread_id - ((struct pthread_node *) b)->thread_id;
}

int pthread_hash_node(struct hash_map_node *node) {
    return ((struct pthread_node *) node)->thread_id;
}