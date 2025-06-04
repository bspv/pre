package com.bazzi.pre.tests.strategy;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class DataQueueStrategy {

    private static RingNode cur = new RingNode();
    private static final LoadingCache<String, String> cache = Caffeine.newBuilder().recordStats().expireAfterAccess(1, TimeUnit.MINUTES)
            .build(k -> getAndNext());

    public static void init(Set<String> keys) {
        if (cur.val == null) {
            synchronized (DataQueueStrategy.class) {
                if (cur.val == null) {
                    RingNode first = null;
                    for (String key : keys) {
                        RingNode ringNode = new RingNode(key);
                        cur.next = ringNode;
                        cur = ringNode;

                        if (first == null)
                            first = ringNode;
                    }
                    cur.next = first;
                    cur = first;
                }
            }
        }
    }

    public static String poll(String deviceID, String filename) {
        String signature = md5(deviceID, filename);
        return cache.get(signature);
    }

    private static synchronized String getAndNext() {
        String key = cur.val;
        cur = cur.next;
        return key;
    }

    private static String md5(String deviceID, String filename) {
        return DigestUtils.md5Hex(deviceID.concat(filename)).toUpperCase();
    }

    static class RingNode {
        private volatile String val;
        private RingNode next;

        public RingNode() {
        }

        public RingNode(String val) {
            this.val = val;
        }
    }
}
