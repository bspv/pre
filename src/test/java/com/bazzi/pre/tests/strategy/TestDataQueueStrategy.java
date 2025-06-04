package com.bazzi.pre.tests.strategy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TestDataQueueStrategy {
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("DataQueue-0", 0);
        map.put("DataQueue-1", 1);
        map.put("DataQueue-2", 2);
        map.put("DataQueue-3", 3);
        map.put("DataQueue-4", 4);
        map.put("DataQueue-5", 5);
//        map.put("DataQueue-6", 6);
//        map.put("DataQueue-7", 7);
//        map.put("DataQueue-8", 8);
//        map.put("DataQueue-9", 9);

        DataQueueStrategy.init(map.keySet());

        Random random = new Random();
        File file = new File("C:\\Users\\hhxd1\\Desktop\\log_20");
        File[] files = file.listFiles();
        assert files != null;
        for (File f : files) {
            String device = String.valueOf(random.nextDouble()).substring(2, 10);
            System.out.println("d = " + device + " , f = " + f.getName() + " , target = " + DataQueueStrategy.poll(device, f.getName()));
        }
    }
}
