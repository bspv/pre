package com.bazzi.tests.util;

import com.bazzi.core.util.BlockQueue;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

class TestBlockQueue {

    @Test
    void testQueue() throws InterruptedException {
        final BlockQueue bq = new BlockQueue();
        CountDownLatch latch = new CountDownLatch(27);
        new Thread(() -> {
            try {
                for (int i = 0; i < 15; i++) {
                    System.out.println("put(" + i + ")");
                    bq.put(i);
                    latch.countDown();
                }
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }).start();

        new Thread(() -> {
            try {
                for (int i = 0; i < 12; i++) {
                    System.out.println("take---(" + bq.take() + ")");
                    latch.countDown();
                }
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }).start();

        latch.await();
        assertThat(bq.take()).isEqualTo(12);
    }

}
