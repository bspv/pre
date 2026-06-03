package com.bazzi.tests.util;

import com.bazzi.core.util.BlockQueue;
import org.junit.jupiter.api.Test;

class TestBlockQueue {
	
	@Test
	void testQueue() throws InterruptedException {
		final BlockQueue bq = new BlockQueue();
		new Thread(() -> {
            try {
                for (int i = 0; i < 15; i++) {
                    System.out.println("put(" + i + ")");
                    bq.put(i);
                }
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }).start();

		new Thread(() -> {
            try {
                for (int i = 0; i < 12; i++) {
                    System.out.println("take---(" + bq.take() + ")");
                }
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }).start();

		Thread.sleep(5000);
	}

}
