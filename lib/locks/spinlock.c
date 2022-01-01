#include <locks/spinlock.h>

void init_spinlock(spinlock *spin_lock)
{
    spin_lock->lock = 0;
}

void lock_spinlock(spinlock *spin_lock)
{
    while (__atomic_exchange_n(&spin_lock->lock, 1, __ATOMIC_SEQ_CST))
    {
        while (__atomic_load_n(&spin_lock->lock, __ATOMIC_SEQ_CST))
        {
            usleep(2);
        }
    }
}

void unlock_spinlock(spinlock *spin_lock)
{
    __atomic_store_n(&spin_lock->lock, 0, __ATOMIC_SEQ_CST);
}

int try_lock_spinlock(spinlock *spin_lock)
{
    return !__atomic_exchange_n(&spin_lock->lock, 1, __ATOMIC_SEQ_CST);
}