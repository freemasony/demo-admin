package com.demo.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.util.SafeEncoder;

import java.util.*;

/**
* Created by a on 15-5-27.
 * author  futao
*/
public class JedisUtil

{
    private static Logger logger = LoggerFactory.getLogger(JedisUtil.class);

    private static JedisUtil jedisTypeUtil = new JedisUtil();

    private JedisPoolConfig config;
    private String mutli;

    public void setConfig(JedisPoolConfig config)
    {
        this.config = config;
    }

    public void setMutli(String mutli)
    {
        this.mutli = mutli;
    }




    //存放各个实例   的ip、host 以及和type的对应关系
    private Map<String,String[]> getRedisAddressPort(){
        String[] hosts = StringUtil.split(mutli, ",");
        if(hosts==null){
            return null;
        }
        Map<String,String[]> redisAddressPort = new HashMap<String, String[]>();
        for(String host:hosts){
            String []info = StringUtil.split(host,":");
            redisAddressPort.put(info[0],new String[]{info[1],info[2]});
        }
        return redisAddressPort;
    }



    // 每个jedis实例的pool都放在map里，和类型type对应。通过type值得到对应的pool
    private static volatile Map<String,JedisPool> mapJedisPool  = null;



    private JedisUtil(){
        this.KEYS = new Keys();
        this.STRINGS = new Strings();
        this.HASH = new Hash();
        this.LISTS = new Lists();
        this.SETS = new Sets();
        this.SORTSET = new SortSet();
    }


    /**
     * 初始化连接池的时候，保证单例
     * @return
     */
    public JedisUtil getInstance(){

        if(mapJedisPool==null){
            synchronized (JedisUtil.class){
                if(mapJedisPool==null){
                    initJedisPool();
                }
            }
        }
        return jedisTypeUtil;
    }


    /**
     * 获取连接池.
     * @return 连接池实例
     */
    private JedisPool getJedisPool(String type) {
        if(mapJedisPool!=null){
            return mapJedisPool.get(type);
        }
        return null;
    }

    /**
     * 获取redis配置文件
     * @return
     */
    private JedisPoolConfig getJedisPoolConfig(){
        return config;
    }

    /**
     * 每个实例获取连接池
     * @return
     */
    public  void initJedisPool(){

        Map<String,String[]> redisAddressPort = getRedisAddressPort();

        if(redisAddressPort!=null&&redisAddressPort.size()>0){

            mapJedisPool = new HashMap<String, JedisPool>();

            JedisPool pool = null;

            JedisPoolConfig config = getJedisPoolConfig();

            for(String type:redisAddressPort.keySet()){
                try{
                    /**
                     * 设置构造JedisPool的超时时间为5秒
                     */
                    pool = new JedisPool(config, redisAddressPort.get(type)[0],Integer.parseInt(redisAddressPort.get(type)[1]),5000);
                    mapJedisPool.put(type, pool);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据key中type类型获取Redis实例.
     * @return
     */
    public Jedis getJedisByType(String type) {
        Jedis jedis  = null;

            try{
                JedisPool jedisPool = getJedisPool(type);
                jedis = jedisPool.getResource();

            } catch (Exception e) {
                // 销毁对象
                JedisPool jedisPool = getJedisPool(type);
                jedisPool.returnBrokenResource(jedis);
                e.printStackTrace();
            }
        return jedis;
    }

    /**
     * 按key查找对应的jedis实例
     * @param key 缓存key
     * @return
     */
    public Jedis getJedisByKey(String key){
        String type = KeyToType(key);
        if(StringUtil.isEmpty(type)){
            return null;
        }
        return getJedisByType(type);

    }

    /**
     * 缓存key解析出type
     * @param key
     * @return
     */
    public String KeyToType(String key){
        if(StringUtil.isEmpty(key)){
            return null;
        }
        String type = StringUtil.split(key,":")[0];

        return type;
    }


    /**
     * 释放连接
     * @param jedis
     * @param key
     */

    public  void returnJedis(Jedis jedis, String key){
        String type = KeyToType(key);
        if(jedis!=null){
            JedisPool jedisPool = getJedisPool(type);
            if(jedisPool!=null){
                getJedisPool(type).returnResource(jedis);
            }
        }
    }

    public String MD5Key(String key){
        return com.cfs.common.util.crypto.MD5.md5(key);
    }






    /**
     * 缓存生存时间
     */
    private final int expire = 60000;
    /**
     * 操作Key的方法
     */
    public Keys KEYS;
    /**
     * 对存储结构为String类型的操作
     */
    public Strings STRINGS;
    /**
     * 对存储结构为List类型的操作
     */
    public Lists LISTS;
    /**
     * 对存储结构为Set类型的操作
     */
    public Sets SETS;
    /**
     * 对存储结构为HashMap类型的操作
     */
    public Hash HASH;
    /**
     * 对存储结构为Set(排序的)类型的操作
     */
    public SortSet SORTSET;







    /**
     * 设置过期时间
     *
     * @author ruan 2013-4-11
     */
    public void expire(String key, int seconds)
    {
        if (seconds <= 0)
        {
            return;
        }
        Jedis jedis = getJedisByKey(key);
        if (jedis == null)
        {
            return;
        }
        try {
            jedis.expire(key, seconds);
        }catch (Exception e){
            logger.error(e.getMessage());
        }finally {
            returnJedis(jedis,key);
        }
    }

    /**
     * 设置默认过期时间
     *
     * @author ruan 2013-4-11
     */
    public void expire(String key)
    {
        expire(key, expire);
    }






    //*******************************************Keys*******************************************//
    public class Keys
    {


        /**
         * 设置key的过期时间，以秒为单位
         *
         * @param seconds ,已秒为单位
         * @return 影响的记录数
         */
        public Long expired(String key, int seconds)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long count =0L;
            try {
                count = jedis.expire(key, seconds);
            }catch (Exception e){
                logger.error("设置缓存过期异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return count;
        }

        /**
         * 设置key的过期时间,它是距历元（即格林威治标准时间 1970 年 1 月 1 日的 00:00:00，格里高利历）的偏移量。
         *
         * @param timestamp ,已秒为单位
         * @return 影响的记录数
         */
        public Long expireAt(String key, long timestamp)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long count = 0L;
            try {
                count = jedis.expireAt(key, timestamp);
            }catch (Exception e){
                logger.error("设置缓存过期异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return count;
        }

        /**
         * 查询key的过期时间
         *
         * @return 以秒为单位的时间表示
         */
        public Long ttl(String key)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long len = 0L;
            try {
                len = jedis.ttl(key);
            }catch (Exception e){
                logger.error(e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return len;
        }

        /**
         * 取消对key过期时间的设置
         *
         * @return 影响的记录数
         */
        public Long persist(String key)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long count = 0L;
            try {
                count = jedis.persist(key);
            }catch (Exception e){
                logger.error(e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return count;
        }

        /**
         * 删除key对应的记录
         *
         * @return 删除的记录数
         */
        public Long del(String key)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long count = 0L;
            try {
                count = jedis.del(key);
            }catch (Exception e){
                logger.error("删除key对应的记录，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return count;
        }




        /**
         * 判断key是否存在
         *
         * @return boolean
         */
        public Boolean exists(String key)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return false;
            }
            boolean exis = false;
            try {
                exis = jedis.exists(key);
            }catch (Exception e){
                logger.error("判断key是否存在异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return exis;
        }


        /**
         * 对List,Set,SortSet进行排序,如果集合数据较大应避免使用这个方法
         *
         * @return List<String> 集合的全部记录 *
         */
        public List<String> sort(String key)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            List<String> list = null;
            try {
                list = jedis.sort(key);
            }catch (Exception e){
                logger.error("对List,Set,SortSet进行排序异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return list;
        }

        /**
         * 对List,Set,SortSet进行排序或limit
         *
         * @param parame 定义排序类型或limit的起止位置.
         * @return List<String> 全部或部分记录 *
         */
        public List<String> sort(String key, SortingParams parame)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            List<String> list = null;
            try {
                list = jedis.sort(key, parame);
            }catch (Exception e){
                logger.error("对List,Set,SortSet进行排序或limit异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return list;
        }

        /**
         * 返回指定key存储的类型
         *
         * @return String string|list|set|zset|hash *
         */
        public String type(String key)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String type = null;
            try {
                type = jedis.type(key);
            }catch (Exception e){
                logger.error("返回指定key存储的类型异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return type;
        }
    }

    //*******************************************Sets*******************************************//
    public class Sets
    {

        /**
         * 向Set添加一条记录，如果member已存在返回0,否则返回1
         *
         * @return 操作码, 0或1
         */
        public Long sadd(String key, String member)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long s = 0L;
            try {
                 s = jedis.sadd(key, member);
            }catch (Exception e){
                logger.error("向Set添加一条记录，如果member已存在返回0,否则返回1异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }


        public Long saddAll(String key, String... members)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long s = 0L;
            try {
                s = jedis.sadd(key, members);
            }catch (Exception e){
                logger.error("向Set添加多条记录异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }


        /**
         * 获取给定key中元素个数
         *
         * @return 元素个数
         */
        public Long scard(String key)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long len = 0L;
            try {
                len = jedis.scard(key);
            }catch (Exception e){
                logger.error("获取给定key中元素个数异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return len;
        }


        /**
         * 确定一个给定的值是否存在
         *
         * @param member 要判断的值
         * @return 存在返回1，不存在返回0 *
         */
        public Boolean sismember(String key, String member)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return false;
            }
            boolean s =false;
            try {
                s = jedis.sismember(key, member);
            }catch (Exception e){
                logger.error("确定一个给定的值是否存在异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }

        /**
         * 返回集合中的所有成员
         *
         * @return 成员集合
         */
        public Set<String> smembers(String key)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Set<String> set = null;
            try {
                set = jedis.smembers(key);
            }catch (Exception e){
                logger.error("返回集合中的所有成员异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return set;
        }


        /**
         * 从集合中删除成员
         *
         * @return 被删除的成员
         */
        public String spop(String key)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String s = null;
            try {
                s = jedis.spop(key);
            }catch (Exception e){
                logger.error("从集合中删除成员异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }

        /**
         * 从集合中删除指定成员
         *
         * @param member 要删除的成员
         * @return 状态码，成功返回1，成员不存在返回0
         */
        public Long srem(String key, String member)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long s =0L;
            try {
                s = jedis.srem(key, member);
            }catch (Exception e){
                logger.error("从集合中删除指定成员异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }
    }

    //*******************************************SortSet*******************************************//
    public class SortSet
    {

        /**
         * 向集合中增加一条记录,如果这个值已存在，这个值对应的权重将被置为新的权重
         *
         * @param score  权重
         * @param member 要加入的值，
         * @return 状态码 1成功，0已存在member的值
         */
        public Long zadd(String key, double score, String member)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long s=0L;
            try {
                s = jedis.zadd(key, score, member);
            }catch (Exception e){
                logger.error("向集合中增加一条记录,如果这个值已存在，这个值对应的权重将被置为新的权重异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }

        /**
         * 只有当这个key存在，才往里面添加
         * @param
         * @param score
         * @param member
         * @return
         */
        public Long existKeyAndZadd(String key, double score, String member)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            if(!jedis.exists(key)){

                returnJedis(jedis,key);
                return 0L;
            }

            Long s=0L;
            try {
                s = jedis.zadd(key, score, member);
            }catch (Exception e){
                logger.error("只有当这个key存在，才往里面添加异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }

        public Long zadd(String key, Map<String, Double> scoreMembers)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long s=0L;
            try {
                s = jedis.zadd(key, scoreMembers);
            }catch (Exception e){
                logger.error("sortset添加多个元素异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }

        /**
         * 获取集合中元素的数量
         *
         * @return 如果返回0则集合不存在
         */
        public Long zcard(String key)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long len=0L;
            try {
                len = jedis.zcard(key);
            }catch (Exception e){
                logger.error("获取集合中元素的数量异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return len;
        }

        /**
         * 获取指定权重区间内集合的数量
         *
         * @param min 最小排序位置
         * @param max 最大排序位置
         */
        public Long zcount(String key, double min, double max)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long len=0L;
            try {
                len =jedis.zcount(key, min, max);
            }catch (Exception e){
                logger.error("获取指定权重区间内集合的数量异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return len;
        }

        /**
         * 获得set的长度
         */
        public Long zlength(String key)
        {
            Long len = (long) 0;
            Set<String> set = zrange(key, 0, -1);
            len = (long) set.size();
            return len;
        }

        /**
         * 权重增加给定值，如果给定的member已存在
         *
         * @param score  要增的权重
         * @param member 要插入的值
         * @return 增后的权重
         */
        public Double zincrby(String key, double score, String member)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            double s=0;
            try {
                s = jedis.zincrby(key, score, member);
            }catch (Exception e){
                logger.error("权重增加给定值，如果给定的member已存在异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }

            return s;
        }

        /**
         * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
         *
         * @param start 开始位置(包含)
         * @param end   结束位置(包含)
         * @return Set<String>
         */
        public Set<String> zrange(String key, int start, int end)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Set<String> set=null;
            try {
               set = jedis.zrange(key, start, end);
            }catch (Exception e){
                logger.error("返回指定位置的集合元素,0为第一个元素，-1为最后一个元素异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return set;
        }

        /**
         * 返回指定权重区间的元素集合
         *
         * @param min 上限权重
         * @param max 下限权重
         * @return Set<String>
         */
        public Set<String> zrangeByScore(String key, double min, double max)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Set<String> set=null;
            try {
                set = jedis.zrangeByScore(key, min, max);
            }catch (Exception e){
                logger.error("返回指定权重区间的元素集合异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return set;
        }


        /**
         * 返回指定权重区间的指定位置，指定数量的元素集合 , 正序
         * @param key
         * @param min
         * @param max
         * @param offset
         * @param count
         * @return
         */
        public Set<String> zrangeByScore(String key, double min, double max,
                                         int offset, int count){
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Set<String> set=null;
            try {
                set =  jedis.zrangeByScore(key, min, max,offset,count);
            }catch (Exception e){
                logger.error("返回指定权重区间的指定位置，指定数量的元素集合 , 正序异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return set;
        }

        /**
         * 返回指定权重区间的指定位置，指定数量的元素集合 倒序
         * @param key
         * @param min
         * @param max
         * @param offset
         * @param count
         * @return
         */
        public Set<String> zrevrangeByScore(String key, double max, double min,
                                            int offset, int count){
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }

            Set<String> set=null;
            try {
                set =  jedis.zrevrangeByScore(key, max, min, offset, count);
            }catch (Exception e){
                logger.error("返回指定权重区间的指定位置，指定数量的元素集合 倒序异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return set;
        }

        /**
         * 获取指定值在集合中的位置，集合排序从低到高
         *
         * @return Long 位置
         * @see
         */
        public Long zrank(String key, String member)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long index =0L;

            try {
                index = jedis.zrank(key, member);
            }catch (Exception e){
                logger.error("获取指定值在集合中的位置，集合排序从低到高异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return index;
        }

        /**
         * 获取指定值在集合中的位置，集合排序从高到低
         *
         * @return Long 位置
         * @see
         */
        public Long zrevrank(String key, String member)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long index =0L;

            try {
                index = jedis.zrevrank(key, member);
            }catch (Exception e){
                logger.error("获取指定值在集合中的位置，集合排序从高到低异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }

            return index;
        }

        /**
         * 从集合中删除成员
         *
         * @return 返回1成功
         */
        public Long zrem(String key, String member)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long s = 0L;
            try {
                s = jedis.zrem(key, member);
            }catch (Exception e){
                logger.error("从集合中删除成员异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }

        /**
         * 删除
         */
        public Long zrem(String key)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long s = 0L;
            try {
                s = jedis.zrem(key);
            }catch (Exception e){
                logger.error("删除key异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }

        /**
         * 删除给定位置区间的元素
         *
         * @param start 开始区间，从0开始(包含)
         * @param end   结束区间,-1为最后一个元素(包含)
         * @return 删除的数量
         */
        public Long zremrangeByRank(String key, int start, int end)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long s = 0L;
            try {
                s = jedis.zremrangeByRank(key, start, end);
            }catch (Exception e){
                logger.error("删除给定位置区间的元素异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }

        /**
         * 删除给定权重区间的元素
         *
         * @param min 下限权重(包含)
         * @param max 上限权重(包含)
         * @return 删除的数量
         */
        public Long zremrangeByScore(String key, double min, double max)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long s = 0L;
            try {
                s = jedis.zremrangeByScore(key, min, max);
            }catch (Exception e){
                logger.error("删除给定权重区间的元素异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }

        /**
         * 获取给定区间的元素，原始按照权重由高到低排序
         *
         * @return Set<String>
         */
        public Set<String> zrevrange(String key, int start, int end)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Set<String> set = null;
            try {
                set = jedis.zrevrange(key, start, end);
            }catch (Exception e){
                logger.error("获取给定区间的元素，原始按照权重由高到低排序异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return set;
        }

        /**
         * 获取给定值在集合中的权重
         *
         * @return double 权重
         */
        public Double zscore(String key, String memebr)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Double score = null;
            try {
                score = jedis.zscore(key, memebr);
            }catch (Exception e){
                logger.error("获取给定值在集合中的权重异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            if (score != null)
            {
                return score;
            }
            return null;
        }
    }

    //*******************************************Hash*******************************************//
    public class Hash
    {

        /**
         * 从hash中删除指定的存储
         *
         * @param fieid 存储的名字
         * @return 状态码，1成功，0失败
         */
        public Long hdel(String key, String fieid)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long s = 0L;
            try {
                s = jedis.hdel(key, fieid);
            }catch (Exception e){
                logger.error("从hash中删除指定的存储异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }

        public Long hdel(String key)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long s = 0L;
            try {
                s = jedis.del(key);
            }catch (Exception e){
                logger.error("从hash中删除异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }

        /**
         * 测试hash中指定的存储是否存在
         *
         * @param fieid 存储的名字
         * @return 1存在，0不存在
         */
        public Boolean hexists(String key, String fieid)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return false;
            }
            boolean s = false;
            try {
                s = jedis.hexists(key, fieid);
            }catch (Exception e){
                logger.error("hash中指定的存储是否存在异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }

        /**
         * 返回hash中指定存储位置的值
         *
         * @param fieid 存储的名字
         * @return 存储对应的值
         */
        public String hget(String key, String fieid)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String s  = null;
            try {
                s = jedis.hget(key, fieid);
            }catch (Exception e){
                logger.error("返回hash中指定存储位置的值异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }


        /**
         * 以Map的形式返回hash中的存储和值
         *
         * @return Map<Strinig,String>
         */
        public Map<String, String> hgetAll(String key)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Map<String, String> map = null;
            try {
                map = jedis.hgetAll(key);
            }catch (Exception e){
                logger.error("以Map的形式返回hash中的存储和值异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return map;
        }

        /**
         * 添加一个对应关系
         *
         * @return 状态码 1成功，0失败，fieid已存在将更新，也返回0 *
         */
        public Long hset(String key, String fieid, String value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long s = 0L;
            try {
                s = jedis.hset(key, fieid, value);
            }catch (Exception e){
                logger.error("添加一个对应关系异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }


        /**
         * 添加对应关系，只有在field不存在时才执行
         *
         * @return 状态码 1成功，0失败fieid已存 *
         */
        public Long hsetnx(String key, String fieid, String value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long s = 0L;
            try {
                s = jedis.hsetnx(key, fieid, value);
            }catch (Exception e){
                logger.error("添加对应关系，只有在field不存在时才执行异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }

        /**
         * 获取hash中value的集合
         *
         * @return List<String>
         */
        public List<String> hvals(String key)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            List<String> list = null;
            try {
                list = jedis.hvals(key);
            }catch (Exception e){
                logger.error("获取hash中value的集合异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return list;
        }

        /**
         * 在指定的存储位置加上指定的数字，存储位置的值必须可转为数字类型
         *
         * @param fieid 存储位置
         * @param value 要增加的值,可以是负数
         * @return 增加指定数字后，存储位置的值
         */
        public Long hincrby(String key, String fieid, Long value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long s = 0L;
            try {
                s = jedis.hincrBy(key, fieid, value);
            }catch (Exception e){
                logger.error("在指定的存储位置加上指定的数字，存储位置的值必须可转为数字类型异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }

        /**
         * 返回指定hash中的所有存储名字,类似Map中的keySet方法
         *
         * @return Set<String> 存储名称的集合
         */
        public Set<String> hkeys(String key)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Set<String> set = null;
            try {
                set = jedis.hkeys(key);
            }catch (Exception e){
                logger.error("返回指定hash中的所有存储名字,类似Map中的keySet方法异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return set;
        }

        /**
         * 获取hash中存储的个数，类似Map中size方法
         *
         * @return Long 存储的个数
         */
        public Long hlen(String key)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long len = 0L;
            try {
                len = jedis.hlen(key);
            }catch (Exception e){
                logger.error("获取hash中存储的个数异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return len;
        }

        /**
         * 根据多个key，获取对应的value，返回List,如果指定的key不存在,List对应位置为null
         *
         * @param fieids 存储位置
         * @return List<String>
         */
        public List<String> hmget(String key, String fieids)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            List<String> list = null;
            try {
                list = jedis.hmget(key, fieids);
            }catch (Exception e){
                logger.error("据多个key，获取对应的value，返回List,如果指定的key不存在,List对应位置为null异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return list;
        }



        /**
         * 添加对应关系，如果对应关系已存在，则覆盖
         *
         * @param map 对应关系
         * @return 状态，成功返回OK
         */
        public String hmset(String key, Map<String, String> map)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String s = null;
            try {
                s = jedis.hmset(key, map);
            }catch (Exception e){
                logger.error("据多个key，获取对应的value，返回List,如果指定的key不存在,List对应位置为null异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return s;
        }
    }


    //*******************************************Strings*******************************************//
    public class Strings
    {

        public <T> T getObject(String key, Class<T> c)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String value =null;
            try {
                value = jedis.get(key);
            }catch (Exception e){
                logger.error("String 获取Object异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }

            return JsonSerializeUtil.jsonToObject(value, c);

        }

        public <T> List<T> getObjectList(String key, Class<T> c)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String value =null;
            try {
                value = jedis.get(key);
            }catch (Exception e){
                logger.error("String 获取Object异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }

            return JsonSerializeUtil.jsonToLinkList(value, c);

        }

        public <T> String setObject(String key, T c)
        {
            return set(key, JsonSerializeUtil.objectToJson(c));
        }

        public <T> String setExObject(String key, T c,int seconds)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String str = null;
            try {
                str = jedis.setex(SafeEncoder.encode(key), seconds, SafeEncoder.encode(JsonSerializeUtil.objectToJson(c)));
            }catch (Exception e){
                logger.error("String 添加一个Object异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return str;
        }


        /**
         * 根据key获取记录
         *
         * @return 值
         */
        public String get(String key)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String value = null;
            try {
                value = jedis.get(key);
            }catch (Exception e){
                logger.error("String 获取一个String值异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return value;
        }


        /**
         * 添加有过期时间的记录
         *
         * @param seconds 过期时间，以秒为单位
         * @return String 操作状态
         */
        public String setEx(String key, int seconds, String value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String str = null;
            try {
                str = jedis.setex(key, seconds, value);
            }catch (Exception e){
                logger.error("String 添加有过期时间的记录异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return str;
        }

        /**
         * 当key不存在的时候添加value值，并且添加过期时间，用户redis分布式锁。
         * @param key
         * @param value
         * @param expireTime
         * @return 成功返回1 如果此key有值 返回0
         */
        public Long setnxExpire(String key, String value,int expireTime)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long str = null;
            try {
                str = jedis.setnx(key, value);//这个方法是给redis的分布式锁用的，一定要用事务，保证setnx和expire的操作原子性，如果过期时间执行失败了，那么就会发生死锁的情况。
                if(str.longValue()==0){ //str=0表示这个key已经有值，那么就不能再执行下面的更新过期时间的操作了。因为这样的话，过期时间就不准了。会变得比自己需要的过期时间长。
                    return 0L;
                }
                if(jedis.expire(key,expireTime)==null){
                    jedis.del(key);
                }
            }catch (Exception e){
                logger.error("添加一条记录，仅当给定的key不存在时才插入异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return str;
        }


        /**
         * 添加一条记录，仅当给定的key不存在时才插入
         *
         * @return Long 状态码，1插入成功且key不存在，0未插入，key存在
         */
        public Long setnx(String key, String value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long str = null;
            try {
                str = jedis.setnx(key, value);
            }catch (Exception e){
                logger.error("添加一条记录，仅当给定的key不存在时才插入异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return str;
        }



        /**
         * 添加记录,如果记录已存在将覆盖原有的value
         *
         * @return 状态码
         */
        public String set(String key, String value)
        {
            return set(key, SafeEncoder.encode(value));
        }

        /**
         * 添加记录,如果记录已存在将覆盖原有的value
         *
         * @return 状态码
         */
        public String set(String key, byte[] value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String status = null;
            try {
                status = jedis.set(SafeEncoder.encode(key), value);
            }catch (Exception e){
                logger.error("添加记录,如果记录已存在将覆盖原有的value异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return status;
        }

        /**
         * 从指定位置开始插入数据，插入的数据会覆盖指定位置以后的数据<br/> 例:String str1="123456789";<br/> 对str1操作后setRange(key,4,0000)，str1="123400009";
         *
         * @return Long value的长度
         */
        public Long setRange(String key, long offset, String value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long len = null;
            try {
                len = jedis.setrange(key, offset, value);
            }catch (Exception e){
                logger.error("从指定位置开始插入数据，插入的数据会覆盖指定位置以后的数据异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return len;
        }

        /**
         * 在指定的key中追加value
         *
         * @return Long 追加后value的长度 *
         */
        public Long append(String key, String value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long len = null;
            try {
                len = jedis.append(key, value);
            }catch (Exception e){
                logger.error("在指定的key中追加value异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return len;
        }

        /**
         * 将key对应的value减去指定的值，只有value可以转为数字时该方法才可用
         *
         * @param number 要减去的值
         * @return Long 减指定值后的值
         */
        public Long decrBy(String key, long number)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long len = null;
            try {
                len = jedis.decrBy(key, number);
            }catch (Exception e){
                logger.error("将key对应的value减去指定的值，只有value可以转为数字时该方法才可用异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return len;
        }

        /**
         * <b>可以作为获取唯一id的方法</b><br/> 将key对应的value加上指定的值，只有value可以转为数字时该方法才可用
         *
         * @param number 要减去的值
         * @return Long 相加后的值
         */
        public Long incrBy(String key, long number)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long len = null;
            try {
                if (jedis.exists(key))
                {
                    len = jedis.incrBy(key, number);
                }
            }catch (Exception e){
                logger.error("将key对应的value加上指定的值，只有value可以转为数字时该方法才可用异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return len;
        }

        public Long incr(String key, long number)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long len = jedis.incrBy(key, number);
            returnJedis(jedis,key);
            return len;
        }

        /**
         * 对指定key对应的value进行截取
         *
         * @param startOffset 开始位置(包含)
         * @param endOffset   结束位置(包含)
         * @return String 截取的值
         */
        public String getrange(String key, long startOffset, long endOffset)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String value = null;
            try {
                value = jedis.getrange(key, startOffset, endOffset);
            }catch (Exception e){
                logger.error("对指定key对应的value进行截取异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return value;
        }

        /**
         * 获取并设置指定key对应的value<br/> 如果key存在返回之前的value,否则返回null
         *
         * @return String 原始value或null
         */
        public String getSet(String key, String value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String str = null;
            try {
                str = jedis.getSet(key, value);
            }catch (Exception e){
                logger.error("获取并设置指定key对应的value异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return str;
        }

        /**
         * 批量获取记录,如果指定的key不存在返回List的对应位置将是null
         *
         * @return List<String> 值得集合
         */
//        		public List<String> mget(String keys)
//        		{
//        			Jedis jedis = getJedisByKey(key); if(jedis==null){ return ; }
//        			List<String> str = jedis.mget(keys);
//        			returnJedis(jedis,key);
//        			return str;
//        		}

        /**
         * 批量存储记录
         *
         * @param keysvalues 例:keysvalues="key1","value1","key2","value2";
         * @return String 状态码
         */
//        		public String mset(String keysvalues)
//        		{
//        			Jedis jedis = getJedisByKey(key); if(jedis==null){ return ; }
//        			String str = jedis.mset(keysvalues);
//        			returnJedis(jedis,key);
//        			return str;
//        		}

        /**
         * 获取key对应的值的长度
         *
         * @return value值得长度
         */
        public Long strlen(String key)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long len = null;
            try {
                len = jedis.strlen(key);
            }catch (Exception e){
                logger.error("获取key对应的值的长度异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return len;
        }
    }


    //*******************************************Lists*******************************************//
    public class Lists
    {

        /**
         * List长度
         *
         * @return 长度
         */
//        public Long llen(String key)
//        {
//            return llen(SafeEncoder.encode(key));
//        }

        /**
         * List长度
         *
         * @return 长度
         */
        public Long llen(String key)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long count = null;
            try {
                count = jedis.llen(SafeEncoder.encode(key));
            }catch (Exception e){
                logger.error("获取List长度异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return count;
        }

        /**
         * 覆盖操作,将覆盖List中指定位置的值
         *
         * @param index 位置
         * @param value 值
         * @return 状态码
         */
        public String lset(String key, int index, String value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String status = null;
            try {
                status = jedis.lset(SafeEncoder.encode(key), index, SafeEncoder.encode(value));
            }catch (Exception e){
                logger.error("覆盖操作,将覆盖List中指定位置的值异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return status;
        }

        /**
         * 覆盖操作,将覆盖List中指定位置的值
         *
         * @param index 位置
         * @param value 值
         * @return 状态码
         */
//        public String lset(String key, int index, String value)
//        {
//            return lset(SafeEncoder.encode(key), index, SafeEncoder.encode(value));
//        }

        /**
         * 在value的相对位置插入记录
         *
         * @param where 前面插入或后面插入
         * @param pivot 相对位置的内容
         * @param value 插入的内容
         * @return 记录总数
         */
//        public Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value)
//        {
//            return linsert(SafeEncoder.encode(key), where, SafeEncoder.encode(pivot), SafeEncoder.encode(value));
//        }

        /**
         * 在指定位置插入记录
         *
         * @param where 前面插入或后面插入
         * @param pivot 相对位置的内容
         * @param value 插入的内容
         * @return 记录总数
         */
        public Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long count = null;
            try {
                count = jedis.linsert(SafeEncoder.encode(key), where, SafeEncoder.encode(pivot), SafeEncoder.encode(value));
            }catch (Exception e){
                logger.error("在指定位置插入记录异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return count;
        }

        /**
         * 获取List中指定位置的值
         *
         * @param index 位置
         * @return 值 *
         */
//        public String lindex(String key, int index)
//        {
//            return SafeEncoder.encode(lindex(SafeEncoder.encode(key), index));
//        }

        /**
         * 获取List中指定位置的值
         *
         * @param index 位置
         * @return 值 *
         */
        public String lindex(String key, int index)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String result = null;
            try {
                byte[] value = jedis.lindex(SafeEncoder.encode(key), index);
                result = SafeEncoder.encode(value);
            }catch (Exception e){
                logger.error("获取List中指定位置的值异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return result;
        }

        /**
         * 将List中的第一条记录移出List
         *
         * @return 移出的记录
         */
//        public String lpop(String key)
//        {
//            return SafeEncoder.encode(lpop(SafeEncoder.encode(key)));
//        }

        /**
         * 将List中的第一条记录移出List
         *
         * @return 移出的记录
         */
        public String lpop(String key)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String result = null;
            try {
                byte[] value = jedis.lpop(SafeEncoder.encode(key));
                result = SafeEncoder.encode(value);
            }catch (Exception e){
                logger.error("将List中的第一条记录移出List异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return result;
        }

        /**
         * 将List中最后第一条记录移出List
         *
         * @return 移出的记录
         */
        public String rpop(String key)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String value = null;
            try {
                value = jedis.rpop(key);
            }catch (Exception e){
                logger.error("将List中最后第一条记录移出List异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return value;
        }


        /**
         * 向List尾部追加记录
         *
         * @return 记录总数
         */
//        public Long lpush(String key, String value)
//        {
//            return lpush(SafeEncoder.encode(key), SafeEncoder.encode(value));
//        }

        /**
         * 向List中追加记录
         *
         * @return 记录总数
         */
        public Long lpush(String key, String value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long count = null;
            try {
                count = jedis.lpush(SafeEncoder.encode(key), SafeEncoder.encode(value));
            }catch (Exception e){
                logger.error("向List尾部追加记录异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return count;
        }

        /**
         * 向List头部追加记录
         *
         * @return 记录总数
         */
        public Long rpush(String key, String... value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long count = null;
            try {
                count = jedis.rpush(key, value);
            }catch (Exception e){
                logger.error("将List中的第一条记录移出List异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return count;
        }

        /**
         * 向List尾部追加记录
         *
         * @return 记录总数
         */
        public Long lpush(String key, String... value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long count = null;
            try {
                count = jedis.lpush(key, value);
            }catch (Exception e){
                logger.error("向List尾部追加记录异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return count;
        }

        /**
         * 向List头部追加记录
         *
         * @return 记录总数
         */
        public Long rpush(String key, String value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long count = null;
            try {
                count = jedis.rpush(key, value);
            }catch (Exception e){
                logger.error("向List头部追加记录异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return count;
        }

        /**
         * 向List头部追加记录
         *
         * @return 记录总数
         */
//        public Long rpush(byte[] key, byte[] value)
//        {
//            Jedis jedis = getJedisByKey(key);
//            if (jedis == null)
//            {
//                return null;
//            }
//            Long count = jedis.rpush(key, value);
//            returnJedis(jedis,key);
//            return count;
//        }




        /**
         * 向List尾部追加记录
         *
         * @return 记录总数
         */
        public <T> Long rpushObject(String key, List<T> value)
        {
            String[] values = new String[value.size()];
            for (int i = 0; i < value.size(); i++)
            {
                values[i] = JsonSerializeUtil.objectToJson(value.get(i));
            }
            return rpush(key, values);
        }

        /**
         * 向List头部追加记录
         *
         * @return 记录总数
         */
        public <T> Long lpushObject(String key, List<T> value)
        {
            String[] values = new String[value.size()];
            for (int i = 0; i < value.size(); i++)
            {
                values[i] = JsonSerializeUtil.objectToJson(value.get(i));
            }
            return lpush(key, values);
        }

        /**
         * 向List头部追加记录
         *
         * @return 记录总数
         */
        public <T> Long rpushObject(String key, T value)
        {
            return rpush(key, JsonSerializeUtil.objectToJson(value));
        }

        /**
         * 向List中追加记录
         *
         * @return 记录总数
         */
        public <T> Long lpushObject(String key, T value)
        {
            return lpush(key, JsonSerializeUtil.objectToJson(value));
        }

        /**
         * 向List头部追加多个记录
         *
         * @return 记录总数
         */
        public <T> Long rpushObjectAll(String key, List<T> value)
        {
            long i = 0;
            for (T t : value)
            {
                try
                {
                    long m = rpush(key, JsonSerializeUtil.objectToJson(t));
                    i = i + m;
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return i;
        }

        /**
         * 向List中追加多个记录
         *
         * @return 记录总数
         */
        public <T> Long lpushObjectAll(String key, List<T> value)
        {
            long i = 0;
            for (T t : value)
            {
                try
                {
                    long m = lpush(key, JsonSerializeUtil.objectToJson(t));
                    i = i + m;
                } catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            return i;
        }


        /**
         * 获取指定范围的记录，可以做为分页使用
         *
         * @return List
         */
        public <T> List<T> lrangeObject(String key, long start, long end, Class<T> c)
        {
            List<T> list = null;
            try
            {
                List<String> str = lrange(key, start, end);
                if (str != null && !str.isEmpty())
                {
                    list = new ArrayList<T>();
                    for (String s : str)
                    {
                        T o = JsonSerializeUtil.jsonToObject(s, c);
                        if (o != null)
                        {
                            list.add(o);
                        }
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return list;
        }


        /**
         * 获取指定范围的记录，可以做为分页使用
         *
         * @return List
         */
        public List<String> lrange(String key, long start, long end)
        {

            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            List<String> list = null;
            try {
                list = jedis.lrange(key, start, end);
            }catch (Exception e){
                logger.error("获取指定范围的记录，可以做为分页使用异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return list;
        }

//        /**
//         * 获取指定范围的记录，可以做为分页使用
//         *
//         * @param end 如果为负数，则尾部开始计算
//         * @return List
//         */
//        public List<byte[]> lrange(byte[] key, int start, int end)
//        {
//
//            Jedis jedis = getJedisByKey(key);
//            if (jedis == null)
//            {
//                return null;
//            }
//            List<byte[]> list = jedis.lrange(key, start, end);
//            returnJedis(jedis,key);
//            return list;
//        }

        /**
         * 删除List中c条记录，被删除的记录值为value
         *
         * @param c     要删除的数量，如果为负数则从List的尾部检查并删除符合的记录
         * @param value 要匹配的值
         * @return 删除后的List中的记录数
         */
        public Long lrem(String key, int c, String value)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            Long count = null;
            try {
                count = jedis.lrem(SafeEncoder.encode(key), c, SafeEncoder.encode(value));
            }catch (Exception e){
                logger.error("删除List中c条记录，被删除的记录值为value异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return count;
        }

        /**
         * 删除List中c条记录，被删除的记录值为value
         *
         * @param c     要删除的数量，如果为负数则从List的尾部检查并删除符合的记录
         * @param value 要匹配的值
         * @return 删除后的List中的记录数
         */
//        public Long lrem(String key, int c, String value)
//        {
//            return lrem(SafeEncoder.encode(key), c, SafeEncoder.encode(value));
//        }

        /**
         * 算是删除吧，只保留start与end之间的记录
         *
         * @param start 记录的开始位置(0表示第一条记录)
         * @param end   记录的结束位置（如果为-1则表示最后一个，-2，-3以此类推）
         * @return 执行状态码
         */
        public String ltrim(String key, int start, int end)
        {
            Jedis jedis = getJedisByKey(key);
            if (jedis == null)
            {
                return null;
            }
            String str = null;
            try {
                str = jedis.ltrim(SafeEncoder.encode(key), start, end);
            }catch (Exception e){
                logger.error("算是删除吧，只保留start与end之间的记录异常，key:"+key+";"+e.getMessage());
            }finally {
                returnJedis(jedis,key);
            }
            return str;
        }

        /**
         * 算是删除吧，只保留start与end之间的记录
         *
         * @param start 记录的开始位置(0表示第一条记录)
         * @param end   记录的结束位置（如果为-1则表示最后一个，-2，-3以此类推）
         * @return 执行状态码
         */
//        public String ltrim(String key, int start, int end)
//        {
//            return ltrim(SafeEncoder.encode(key), start, end);
//        }
    }

}
