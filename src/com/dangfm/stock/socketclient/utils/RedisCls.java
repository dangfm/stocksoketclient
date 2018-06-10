package com.dangfm.stock.socketclient.utils;

import com.dangfm.stock.socketclient.Config;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Redis
 * Created by dangfm on 17/2/3.
 */
public class RedisCls {
    private Logger logger = LoggerFactory.getLogger(RedisCls.class);
    private ReentrantLock lock = new ReentrantLock();
    public String redisServer = Config.redisServer;
    //private static String redisServer = "127.0.0.1";
    public int redisPort = Config.redisPort;
    public String redisPassword = Config.redisPassword;
    private Jedis redis = null;
    private Jedis[] allRedis = new Jedis[100];

    public RedisCls(){
        init();
    }

    public RedisCls(String server, int port, String pass){
        redisServer = server;
        redisPassword = pass;
        redisPort = port;
        init();
    }

    private void init(){
        if (redis==null) {
            // 连接redis
            try{
                redis = new Jedis(redisServer, redisPort);
                if (!redisPassword.isEmpty()) {
//                    System.out.println("redis:"+redisPassword);
                    redis.auth(redisPassword);
                }
                if (isConnect()) {
//                    logger.info("redis:" + redisServer + ":" + redisPort + " connected!");
                }
            }catch (Exception e){
                redis.close();
                redis = null;
                logger.error("redis:" + redisServer + ":" + redisPort + " unconnected!");
                logger.error(e.toString());
            }

        }
    }

    private void restart(){

    }

    public Boolean isConnect(){
        if (redis!=null){
            return true;
        }
        String connect_status="";
        try {
            connect_status = redis.ping();
            if (connect_status.equals("PONG")) {
                return true;
            }
        }catch (Exception e){
            logger.error("redis:" + redisServer + ":" + redisPort + " ping = "+connect_status);
            logger.error(e.toString());
        }
        // 如果redis无法连接，启动重连

        return false;
    }

    public void disconnect(){
        if (redis!=null)
            redis.disconnect();
    }

    public void destroy(){
        // 销毁
        logger.info("销毁redis：" + redisServer + ":" + redisPort);
        if (redis!=null){
            redis.disconnect();
            redis.close();
            redis = null;
        }
    }


    public Jedis getRedis() {
        return redis;
    }

    public Jedis redis(int databaseNumber){
        try {
            if (isConnect()) {
                lock.lock();
                redis.select(databaseNumber);
                lock.unlock();
            }
        }catch (JedisException e){
            lock.unlock();
            logger.error(e.toString());
        }
        return redis;
    }

    public void setValue(String key, String value) {


        try {
            if (isConnect()) {
                lock.lock();
                if (value != null) {
                    String r = redis.set(key, value);
                }
                // System.out.println(r);
                lock.unlock();
            }
        }catch (JedisException e){
            lock.unlock();
            logger.error(e.toString());
        }

        //redis.lpush(key,value);
        //redis.set(key, value);
    }

    public String getValue(Object key) {
        String value = null;
        try {

            if (isConnect()) {
                lock.lock();
                value = redis.get((String) key);
                lock.unlock();
            }
        }catch (JedisException e){
            lock.unlock();
            logger.error(e.toString());
        }

//        try{
//
//        }catch (Exception e){
//            System.out.println("出错了");
//            lock.unlock();
//        }
//        finally {
//            lock.unlock();
//        }


        return value;
    }

    /**
     * 获取某个股票的实时行情对象
     * @param code
     * @return
     */
    public JSONObject getStockPrices(String code){
        JSONObject obj=null;
        String json = getValue(code);
        if (json!=null){
            try {
                obj = new JSONObject(json);

            } catch (JSONException e) {
                logger.error(e.toString());
            }
        }

        return obj;
    }

    /**
     * 切换数据库
     *
     * @param num
     */
    public void select(int num) {
        if (isConnect()) {
            lock.lock();
            String value = null;
            redis.select(num);
            lock.unlock();
        }
    }
}