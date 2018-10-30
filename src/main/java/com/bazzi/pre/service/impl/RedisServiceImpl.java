package com.bazzi.pre.service.impl;

import com.bazzi.pre.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisServiceImpl implements RedisService {
    private static final long DEFAULT_SLEEP_TIME = 100L;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public long ttl(String key) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null)
            throw new NullPointerException("ttl fail");
        return ttl;
    }

    public Set<String> keys(String keyPattern) {
        return redisTemplate.keys(keyPattern);
    }

    public boolean exists(String key) {
        Boolean b = redisTemplate.hasKey(key);
        return b != null ? b : false;
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean expire(String key, int seconds) {
        Boolean b = redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
        return b != null ? b : false;
    }

    public boolean expireAt(String key, long timestamp) {
        Boolean b = redisTemplate.expireAt(key, new Date(timestamp));
        return b != null ? b : false;
    }


    public String type(String key) {
        return Objects.requireNonNull(redisTemplate.type(key)).code();
    }

    public boolean persist(String key) {
        Boolean b = redisTemplate.persist(key);
        return b != null ? b : false;
    }

    public <T> void set(String key, T val) {
        redisTemplate.opsForValue().set(key, val);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public <T> boolean setNx(String key, T val) {
        Boolean b = redisTemplate.opsForValue().setIfAbsent(key, val);
        return b != null ? b : false;
    }

    public <T> void setEx(String key, T val, int expireTime) {
        redisTemplate.opsForValue().set(key, val, expireTime, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    public <T> T getSet(String key, T val) {
        return (T) redisTemplate.opsForValue().getAndSet(key, val);
    }


    public long increment(String key, long num) {
        Long l = redisTemplate.opsForValue().increment(key, num);
        if (l == null)
            throw new NullPointerException("increment fail");
        return l;
    }

    public long hDel(String key, Object field) {
        return redisTemplate.opsForHash().delete(key, field);
    }

    public boolean hExists(String key, Object field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    public Object hGet(String key, Object field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Set<Object> hKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    public long hLen(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    public List<Object> hMultiGet(String key, Object... fields) {
        List<Object> f = Arrays.asList(fields);
        return redisTemplate.opsForHash().multiGet(key, f);
    }

    public void hMultiSet(String key, Map<Object, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public void hSet(String key, Object field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public boolean hSetNx(String key, Object field, Object value) {
        return redisTemplate.opsForHash().putIfAbsent(key, field, value);
    }

    public List<Object> hValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }


    public long sAdd(String key, Object... member) {
        Long l = redisTemplate.opsForSet().add(key, member);
        return l == null ? -1 : l;
    }

    public long sCard(String key) {
        Long l = redisTemplate.opsForSet().size(key);
        return l == null ? -1 : l;
    }

    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public boolean sIsMember(String key, Object obj) {
        Boolean b = redisTemplate.opsForSet().isMember(key, obj);
        return b != null ? b : false;
    }

    public Object sPop(String key) {
        return redisTemplate.opsForSet().pop(key);
    }

    public long sRem(String key, Object... member) {
        Long l = redisTemplate.opsForSet().remove(key, member);
        return l == null ? -1 : l;
    }

    public Object sRandMember(String key) {
        return redisTemplate.opsForSet().randomMember(key);
    }

    public long lLen(String key) {
        Long l = redisTemplate.opsForList().size(key);
        return l == null ? -1 : l;
    }

    public void lSet(String key, long index, Object value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    public Object lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    public Object lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    public Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    public long lPush(String key, Object value) {
        Long l = redisTemplate.opsForList().leftPush(key, value);
        return l == null ? -1 : l;
    }

    public long rPush(String key, Object value) {
        Long l = redisTemplate.opsForList().rightPush(key, value);
        return l == null ? -1 : l;
    }

    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public long lRem(String key, int c, Object value) {
        Long l = redisTemplate.opsForList().remove(key, c, value);
        return l == null ? -1 : l;
    }

    public void lTrim(String key, int start, int end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

    public boolean lock(String key, String value, long expireTime) {
        String lua = "return redis.call('set', KEYS[1], ARGV[1], 'NX', 'PX', ARGV[2])";
        RedisScript<String> redisScript = new DefaultRedisScript<>(lua, String.class);
        String result = redisTemplate.execute(redisScript, new StringRedisSerializer(),
                new StringRedisSerializer(), Collections.singletonList(key), value, String.valueOf(expireTime));
        return "OK".equals(result);
    }

    public boolean lockForBlock(String key, String value, long expireTime) {
        while (!lock(key, value, expireTime)) {
            try {
                Thread.sleep(DEFAULT_SLEEP_TIME);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        return true;
    }

    public boolean lockForBlock(String key, String value, long expireTime, int blockTime, TimeUnit unit) {
        return lockForBlock(key, value, expireTime, blockTime, unit, DEFAULT_SLEEP_TIME);
    }

    public boolean lockForBlock(String key, String value, long expireTime, int blockTime, TimeUnit unit, long sleepTime) {
        sleepTime = sleepTime > 0 ? sleepTime : DEFAULT_SLEEP_TIME;
        long loopTime = unit.toMillis(blockTime);
        long current = 0L;
        while (!lock(key, value, expireTime)) {
            if (current > loopTime) {
                return false;
            }
            try {
                Thread.sleep(sleepTime);
                current += sleepTime;
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        return true;
    }

    public boolean releaseLock(String key, String value) {
        String lua = "if redis.call('get',KEYS[1]) == ARGV[1] then " +
                " return redis.call('del',KEYS[1]) " +
                " else " +
                " return 0 " +
                " end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(lua, Long.class);
        Long result = redisTemplate.execute(redisScript, new StringRedisSerializer(),
                new Jackson2JsonRedisSerializer<>(Long.class), Collections.singletonList(key), value);
        return result != null && 1 == result.intValue();
    }

}
