package com.dangfm.stock.socketclient.market;

import com.dangfm.stock.socketclient.Config;
import com.dangfm.stock.socketclient.server.StockResposeDatas;
import com.dangfm.stock.socketclient.utils.FN;
import com.dangfm.stock.socketclient.utils.RedisCls;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class SaveAllStocks {
    public static final Logger logger = LoggerFactory.getLogger(SaveAllStocks.class);

    private static ReentrantLock lock = new ReentrantLock();
    /**
     * 本地redis
     */
    private static RedisCls redisCls = new RedisCls();
    private static RedisCls redisSearchCls = new RedisCls();
    private static RedisCls redisBlockCls = new RedisCls();


    /**
     * 保存行业板块表到redis
     * @param datas
     */
    public static void saveAllStockType_hangye(JSONArray datas){
        redisBlockCls.select(Config.redisDB_upDown);
        redisBlockCls.setValue(Config.redisKey_Hangye_tables,datas.toString());
    }

    /**
     * 保存概念板块表到redis
     * @param datas
     */
    public static void saveAllStockType_gainian(JSONArray datas){
        redisBlockCls.select(Config.redisDB_upDown);
        redisBlockCls.setValue(Config.redisKey_Gainian_tables,datas.toString());
    }

    /**
     * 保存地域板块表到redis
     * @param datas
     */
    public static void saveAllStockType_diqu(JSONArray datas){
        redisBlockCls.select(Config.redisDB_upDown);
        redisBlockCls.setValue(Config.redisKey_Diqu_tables,datas.toString());
    }

    /**
     * 保存股票分类表
     * @param key
     * @param value
     */
    public static void saveAllStocks(String key,String value){
        // 查询本地key
        if (!key.isEmpty() && !value.isEmpty()) {
//            logger.info("接收保存股票分类表："+key+"="+value);
            // 拿到股票代码
            try {
                JSONObject obj = new JSONObject(value);
                if (obj!=null) {
                    String code = obj.getString("code");
                    Set ss = redisCls.redis(Config.redisDB_stocks).keys(code + "*");
                    if (ss.size() > 0) {
                        Object[] lists = ss.toArray();
                        for (int j = 0; j < lists.length; j++) {
                            // 这样可以避免中文名称不同而重复
                            String keys = (String) lists[j];
                            redisCls.redis(Config.redisDB_stocks).del(keys);
//                            logger.info("股票数据更新:" + code + "===" + keys);
                        }
                    }
                    redisCls.redis(Config.redisDB_stocks).set(key,value);
//                    logger.debug("接收保存股票分类表："+key);
                }

            } catch (JSONException e) {
                logger.error(e.toString());
            }

        }

    }

    /**
     * 保存股票搜索表
     * @param key
     * @param value
     */
    public static void saveSearchStocks(String key,String value){
        // 查询本地key
        if (!key.isEmpty() && !value.isEmpty()) {
            // 拿到股票代码
            try {
                JSONObject obj = new JSONObject(value);
                if (obj!=null) {
                    String code = obj.getString("code");
                    Set ss = redisSearchCls.redis(Config.redisDB_Search).keys(code + "*");
                    if (ss.size() > 0) {
                        Object[] lists = ss.toArray();
                        for (int j = 0; j < lists.length; j++) {
                            // 这样可以避免中文名称不同而重复
                            String keys = (String) lists[j];
                            redisSearchCls.redis(Config.redisDB_Search).del(keys);
//                        System.out.println("搜索key:" + code + "===" + keys);
                        }
                    }
                    redisSearchCls.redis(Config.redisDB_Search).set(key,value);
//                    logger.debug("接收保存股票搜索表："+key);
                }

            } catch (JSONException e) {
                logger.error(e.toString());
            }

        }

    }
}
