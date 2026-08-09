package com.bazzi.core.util;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class DataQueueStrategy {
    private DataQueueStrategy() {
    }


    // volatile 保证 init 构建完成的环形链表对其他线程可见；构建过程在局部变量完成，最后一次性发布
    private static volatile RingNode cur;
    private static final LoadingCache<String, String> cache = Caffeine.newBuilder().recordStats().expireAfterAccess(1, TimeUnit.MINUTES)
            .build(k -> getAndNext());

    public static void init(Set<String> keys) {
        if (cur != null)
            return;
        synchronized (DataQueueStrategy.class) {
            if (cur != null)
                return;
            if (keys == null || keys.isEmpty())
                throw new IllegalArgumentException("keys不能为空");
            RingNode first = null;
            RingNode prev = null;
            for (String key : keys) {
                RingNode ringNode = new RingNode(key);
                if (first == null)
                    first = ringNode;
                else
                    prev.next = ringNode;
                prev = ringNode;
            }
            prev.next = first;
            cur = first;
        }
    }

    public static String poll(String deviceID, String filename) {
        String signature = md5(deviceID, filename);
        return cache.get(signature);
    }

    private static synchronized String getAndNext() {
        if (cur == null)
            throw new IllegalStateException("请先调用init初始化");
        String key = cur.val;
        cur = cur.next;
        return key;
    }

    private static String md5(String deviceID, String filename) {
        return DigestUtils.md5Hex(deviceID.concat(filename)).toUpperCase();
    }

    static class RingNode {
        private final String val;
        private RingNode next;

        public RingNode(String val) {
            this.val = val;
        }
    }
}
