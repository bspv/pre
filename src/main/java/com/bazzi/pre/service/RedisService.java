package com.bazzi.pre.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface RedisService {

	//key

	/**
	 * 剩余生存时间，单位秒
	 *
	 * @param key key
	 * @return 剩余生存时间
	 */
	long ttl(String key);

	/**
	 * 检查给定 key 是否存在
	 *
	 * @param key key
	 * @return 存在true，反之false
	 */
	boolean exists(String key);

	/**
	 * 删除给定的key
	 *
	 * @param key key
	 */
	void delete(String key);

	/**
	 * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
	 *
	 * @param key     key
	 * @param seconds 过期时间，单位秒
	 * @return 成功true，反之false
	 */
	boolean expire(String key, int seconds);

	/**
	 * 设置key的过期时间，UNIX时间戳是key过期的时间
	 *
	 * @param key       key
	 * @param timestamp 时间戳，毫秒
	 * @return 成功true，反之false
	 */
	boolean expireAt(String key, long timestamp);

	/**
	 * 返回 key 所储存的值的类型
	 *
	 * @param key key
	 * @return 值的类型
	 */
	String type(String key);

	/**
	 * 移除给定 key 的生存时间
	 *
	 * @param key key
	 * @return 成功true，反之false
	 */
	boolean persist(String key);

	//String

	/**
	 * 给key设置val值
	 *
	 * @param key key
	 * @param val 值
	 * @param <T> 值的类型
	 */
	<T> void set(String key, T val);

	/**
	 * 获取key对象的值
	 *
	 * @param key key
	 * @return 值
	 */
	Object get(String key);

	/**
	 * 只有在key不存在时设置key的值
	 *
	 * @param key key
	 * @param val 值
	 * @param <T> 值的类型
	 * @return 成功true，反之false
	 */
	<T> boolean setNx(String key, T val);

	/**
	 * 将值val关联到 key ，并将key的过期时间设为 seconds (以秒为单位)
	 *
	 * @param key        key
	 * @param val        值
	 * @param expireTime 过期时间
	 * @param <T>        值的类型
	 */
	<T> void setEx(String key, T val, int expireTime);

	/**
	 * 将给定key的值设为val ，并返回key的旧值
	 *
	 * @param key key
	 * @param val 值
	 * @param <T> 值的类型
	 * @return 旧值
	 */
	<T> T getSet(String key, T val);

	/**
	 * key对应的value加上number，并返回操作之后的结果
	 *
	 * @param key    key
	 * @param number 数值
	 * @return 操作之后的值
	 */
	long increment(String key, long number);

	//hash

	/**
	 * 删除哈希表field字段
	 *
	 * @param key   key
	 * @param field 属性
	 * @return 被成功移除的域的数量，不包括被忽略的域
	 */
	long hDel(String key, Object field);

	/**
	 * 查看哈希表 key 中，给定域 field 是否存在
	 *
	 * @param key   key
	 * @param field 属性
	 * @return 存在true，反之false
	 */
	boolean hExists(String key, Object field);

	/**
	 * 返回哈希表 key 中给定域 field 的值
	 *
	 * @param key   key
	 * @param field 属性
	 * @return 值
	 */
	Object hGet(String key, Object field);

	/**
	 * 返回哈希表 key 中，所有的域和值
	 *
	 * @param key key
	 * @return 整个hash表
	 */
	Map<Object, Object> hGetAll(String key);

	/**
	 * 返回哈希表 key 中的所有域
	 *
	 * @param key key
	 * @return 所有域
	 */
	Set<Object> hKeys(String key);

	/**
	 * 返回哈希表 key 中域的数量
	 *
	 * @param key key
	 * @return 域的数量
	 */
	long hLen(String key);

	/**
	 * 返回哈希表 key 中，一个或多个给定域的值
	 *
	 * @param key    key
	 * @param fields 属性
	 * @return 一个或多个给定域的值
	 */
	List<Object> hMultiGet(String key, Object... fields);

	/**
	 * 同时将多个 field-value (域-值)对设置到哈希表 key 中。
	 *
	 * @param key key
	 * @param map 要设置的域和值
	 */
	void hMultiSet(String key, Map<Object, Object> map);

	/**
	 * 将哈希表 key 中的域 field 的值设为 value
	 *
	 * @param key   key
	 * @param field 属性
	 * @param value 值
	 */
	void hSet(String key, Object field, Object value);

	/**
	 * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在
	 *
	 * @param key   key
	 * @param field 属性
	 * @param value 值
	 * @return 成功true，反之false
	 */
	boolean hSetNx(String key, Object field, Object value);

	/**
	 * 返回哈希表 key 中所有域的值
	 *
	 * @param key key
	 * @return 所有域的值
	 */
	List<Object> hValues(String key);

	//set

	/**
	 * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略
	 *
	 * @param key    key
	 * @param member 元素
	 * @return 被添加到集合中的新元素的数量，不包括被忽略的元素
	 */
	long sAdd(String key, Object... member);

	/**
	 * 返回集合 key 的基数
	 *
	 * @param key key
	 * @return 集合的基数。key不存在时，返回 0 。
	 */
	long sCard(String key);

	/**
	 * 返回集合 key 中的所有成员。
	 *
	 * @param key key
	 * @return 集合中的所有成员
	 */
	Set<Object> sMembers(String key);

	/**
	 * 判断 obj 元素是否集合 key 的成员
	 *
	 * @param key key
	 * @param obj 元素
	 * @return 是返回true，反之false
	 */
	boolean sIsMember(String key, Object obj);

	/**
	 * 移除并返回集合中的一个随机元素
	 *
	 * @param key key
	 * @return 集合中的一个随机元素
	 */
	Object sPop(String key);

	/**
	 * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略
	 *
	 * @param key    key
	 * @param member 元素
	 * @return 被成功移除的元素的数量，不包括被忽略的元素
	 */
	long sRem(String key, Object... member);

	/**
	 * 返回集合中的一个随机元素
	 *
	 * @param key key
	 * @return 集合中的一个随机元素
	 */
	Object sRandMember(String key);

	//list

	/**
	 * 返回列表 key 的长度
	 *
	 * @param key key
	 * @return 列表长度
	 */
	long lLen(String key);

	/**
	 * 将列表 key 下标为 index 的元素的值设置为 value
	 *
	 * @param key   key
	 * @param index 下标
	 * @param value 值
	 */
	void lSet(String key, long index, Object value);

	/**
	 * 返回列表 key 中，下标为 index 的元素
	 *
	 * @param key   key
	 * @param index 下标
	 * @return 下标对应的元素
	 */
	Object lIndex(String key, long index);

	/**
	 * 移除并返回列表 key 的头元素
	 *
	 * @param key key
	 * @return 头元素
	 */
	Object lPop(String key);

	/**
	 * 移除并返回列表 key 的尾元素
	 *
	 * @param key key
	 * @return 尾元素
	 */
	Object rPop(String key);

	/**
	 * 将value插入到列表key的表头
	 *
	 * @param key   key
	 * @param value 值
	 * @return 执行命令后，列表的长度
	 */
	long lPush(String key, Object value);

	/**
	 * 将value插入到列表key的表尾(最右边)
	 *
	 * @param key   key
	 * @param value 值
	 * @return 执行命令后，列表的长度
	 */
	long rPush(String key, Object value);

	/**
	 * 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 stop 指定
	 *
	 * @param key   key
	 * @param start 起始下标
	 * @param end   结束下标
	 * @return 一个列表，包含指定区间内的元素
	 */
	List<Object> lRange(String key, long start, long end);

	/**
	 * 根据参数c的值，移除列表中与参数 value相等的元素
	 *
	 * @param key   key
	 * @param c     个数
	 * @param value 值
	 * @return 被移除元素的数量
	 */
	long lRem(String key, int c, Object value);

	/**
	 * 让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除
	 *
	 * @param key   key
	 * @param start 起始下标
	 * @param end   结束下标
	 */
	void lTrim(String key, int start, int end);

	//Distributed lock

	/**
	 * redis锁，非阻塞
	 *
	 * @param key        锁的key值
	 * @param value      标识锁拥有者的value值
	 * @param expireTime 锁过期时间，单位：毫秒
	 * @return 加锁成功true，失败false
	 */
	boolean lock(String key, String value, long expireTime);

	/**
	 * redis锁，一直阻塞
	 *
	 * @param key        锁的key值
	 * @param value      标识锁拥有者的value值
	 * @param expireTime 锁过期时间，单位：毫秒
	 * @return 加锁成功true，失败false
	 */
	boolean lockForBlock(String key, String value, long expireTime);

	/**
	 * redis锁，阻塞blockTime时间
	 *
	 * @param key        锁的key值
	 * @param value      标识锁拥有者的value值
	 * @param expireTime 锁过期时间，单位：毫秒
	 * @param blockTime  阻塞时间
	 * @param unit       阻塞单位
	 * @return 加锁成功true，失败false
	 */
	boolean lockForBlock(String key, String value, long expireTime, int blockTime, TimeUnit unit);

	/**
	 * redis锁，阻塞blockTime时间，每次间隔sleepTime获取锁
	 *
	 * @param key        锁的key值
	 * @param value      标识锁拥有者的value值
	 * @param expireTime 锁过期时间，单位：毫秒
	 * @param blockTime  阻塞时间
	 * @param unit       阻塞单位
	 * @param sleepTime  休眠时间，单位:ms
	 * @return 加锁成功true，失败false
	 */
	boolean lockForBlock(String key, String value, long expireTime, int blockTime, TimeUnit unit, long sleepTime);

	/**
	 * 删除redis锁
	 *
	 * @param key   锁的key值
	 * @param value 标识锁拥有者的value值
	 * @return 删除成功true，失败false
	 */
	boolean releaseLock(String key, String value);

}
