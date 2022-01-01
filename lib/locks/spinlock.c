#include <locks/spinlock.h>

void init_spinlock(spinlock *spin_lock) {
    spin_lock->lock = 0;
}

void lock_spinlock(spinlock *spin_lock) {
    asm volatile("mfence"::: "memory");
    while (__atomic_exchange_n(&spin_lock->lock, 1, __ATOMIC_SEQ_CST)) {
        while (__atomic_load_n(&spin_lock->lock, __ATOMIC_SEQ_CST)) {
            usleep(1);
        }
    }
    asm volatile("mfence"::: "memory");
}

void unlock_spinlock(spinlock *spin_lock) {
    asm volatile("mfence"::: "memory");
    __atomic_store_n(&spin_lock->lock, 0, __ATOMIC_SEQ_CST);
    asm volatile("mfence"::: "memory");
}

int try_lock_spinlock(spinlock *spin_lock) {
    return !__atomic_exchange_n(&spin_lock->lock, 1, __ATOMIC_SEQ_CST);
}