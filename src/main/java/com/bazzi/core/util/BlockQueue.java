package com.bazzi.core.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockQueue {
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    private final Object[] items = new Object[10];
    private int putIdx;
    private int takeIdx;
    private int count;

    public void put(Object obj) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length)
                notFull.await();
            items[putIdx] = obj;
            if (++putIdx == items.length)
                putIdx = 0;
            ++count;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public Object take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0)
                notEmpty.await();
            Object obj = items[takeIdx];
            if (++takeIdx == items.length)
                takeIdx = 0;
            --count;
            notFull.signal();
            return obj;
        } finally {
            lock.unlock();
        }
    }

}
