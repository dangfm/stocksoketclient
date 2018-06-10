package com.dangfm.stock.socketclient.market;

import com.dangfm.stock.socketclient.Config;
import com.dangfm.stock.socketclient.utils.FN;
import com.dangfm.stock.socketclient.utils.HttpWebCollecter;
import com.dangfm.stock.socketclient.utils.RedisCls;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 初始化一些数据，如股票分类，板块分类等
 */
public class InitAllStockDatas extends Thread{
    public static final Logger logger = LoggerFactory.getLogger(InitAllStockDatas.class);
    private static RedisCls redisCls = new RedisCls();
    private static RedisCls localRedis = new RedisCls();

    public InitAllStockDatas(){

    }

    @Override
    public void run() {
        super.run();
        logger.info("开始初始化数据...");
        int i = 0;
        while (true){
            if (i>10) break;
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i ++;
        }

        // 初始化板块
        initStockTypes();
        // 初始化好股票
        initStocks();
        // 初始化所有搜索库股票
        initSearchStocks();
        // 初始化所有股票实时行情
        initStockRealtimeQuotes();
    }

    /**
     * 初始化请求股票板块分类
     * http://kline.api.dashixiong.cn/V1/stocktypes.php
     */
    private void initStockTypes(){
        long t = System.currentTimeMillis();
        String params = "";
        String token = FN.MD5(params+t+Config.appKey+Config.appSecret);
        params = params + "t="+t+"&app_key="+Config.appKey+"&token="+token;
        String url = Config.socketServer+"/stocktypes.php?"+params;
//        logger.info("开始初始化所有股票板块分类数据"+url);
        String html = HttpWebCollecter.getWebContent(url);
        if (html!=null){
            if (!html.isEmpty()){
                if (html.startsWith("{")){
                    try {
                        JSONObject obj = new JSONObject(html);
                        if (obj!=null){
                            JSONObject data = obj.getJSONObject("data");
                            if (data!=null){
                                JSONArray hangye = data.getJSONArray("hangye");
                                JSONArray gainian = data.getJSONArray("gainian");
                                JSONArray diqu = data.getJSONArray("diqu");
                                if (hangye!=null) {
                                    if (hangye.length() > 0)
                                        SaveAllStocks.saveAllStockType_hangye(hangye);
                                }
                                if (gainian!=null) {
                                    if (gainian.length() > 0)
                                        SaveAllStocks.saveAllStockType_gainian(gainian);
                                }
                                if (diqu!=null) {
                                    if (diqu.length() > 0)
                                        SaveAllStocks.saveAllStockType_diqu(diqu);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        logger.error(e.toString());
                    }

                }
            }
        }
        logger.info("初始化所有股票板块分类数据-完成，用时："+(System.currentTimeMillis()-t)+"ms");
    }

    /**
     * 初始化请求股票数据
     * http://kline.api.dashixiong.cn/V1/stocktypes.php
     */
    private void initStocks(){
        long t = System.currentTimeMillis();
        String params = "";
        String token = FN.MD5(params+t+Config.appKey+Config.appSecret);
        params = params + "t="+t+"&app_key="+Config.appKey+"&token="+token;
        String url = Config.socketServer+"/stocks.php?"+params;
//        logger.info("开始初始化所有股票数据"+url);
        String html = HttpWebCollecter.getWebContent(url);
        if (html!=null){
            if (!html.isEmpty()){
                if (html.startsWith("{")){
                    try {
                        JSONObject obj = new JSONObject(html);
                        if (obj!=null){
                            JSONArray data = obj.getJSONArray("data");
                            if (data!=null){
                                for (int i = 0; i < data.length(); i++) {
                                    String rows = data.getString(i);
                                    String key = rows;
                                    if (rows.indexOf(":tree:")>0){
                                        String[] a = rows.split(":tree:");
                                        if (a.length>0){
                                            String code = a[0];
                                            rows = a[1];
                                            if (rows.indexOf("typeIds:")>0){
                                                a = rows.split("typeIds:");
                                                if (a.length>0){
                                                    String tree = a[0];
                                                    String typeIds = a[1];
                                                    String type = "0";
                                                    if (code.startsWith("sh000") || code.startsWith("sz399")){
                                                        type = "1";
                                                    }

                                                    // sh600169:tree:0,666666 typeIds:,666666,6,67,130,192,201,2...	;
                                                    JSONObject item = new JSONObject();
                                                    item.put("code",code);
                                                    item.put("typeTree",tree);
                                                    item.put("typeIds",typeIds);
                                                    item.put("type",type);
                                                    item.put("isStop","0");
                                                    String value = item.toString();
                                                    item = null;

                                                    Set ss = redisCls.redis(Config.redisDB_stocks).keys(code + "*");
                                                    if (ss.size() > 0) {
                                                        Object[] lists = ss.toArray();
                                                        for (int j = 0; j < lists.length; j++) {
                                                            // 这样可以避免中文名称不同而重复
                                                            String keys = (String) lists[j];
                                                            redisCls.redis(Config.redisDB_stocks).del(keys);
                                                        }
                                                    }
                                                    redisCls.redis(Config.redisDB_stocks).set(key,value);


                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        logger.error(e.toString());
                    }

                }
            }
        }
        logger.info("初始化所有股票数据-完成，用时："+(System.currentTimeMillis()-t)+"ms");
    }

    /**
     * 初始化请求股票搜索库数据
     * http://kline.api.dashixiong.cn/V1/search.php
     */
    private void initSearchStocks(){
        long t = System.currentTimeMillis();
        String params = "";
        String token = FN.MD5(params+t+Config.appKey+Config.appSecret);
        params = params + "t="+t+"&app_key="+Config.appKey+"&token="+token;
        String url = Config.socketServer+"/search.php?"+params;
//        logger.info("开始初始化所有股票搜索库数据"+url);
        String html = HttpWebCollecter.getWebContent(url);
        if (html!=null){
            if (!html.isEmpty()){
                if (html.startsWith("{")){
                    try {
                        JSONObject obj = new JSONObject(html);
                        if (obj!=null){
                            JSONArray data = obj.getJSONArray("data");
                            if (data!=null){
                                for (int i = 0; i < data.length(); i++) {
                                    String rows = data.getString(i);

                                    if (rows.indexOf("|")>0) {
//                                        logger.info(rows);
                                        String[] a = rows.split("\\|");
                                        String code = a[0];
                                        if (code.startsWith("sh") || code.startsWith("sz")) {
                                            Set ss = redisCls.redis(Config.redisDB_Search).keys(code + "*");
                                            if (ss.size() > 0) {
                                                Object[] lists = ss.toArray();
                                                for (int j = 0; j < lists.length; j++) {
                                                    // 这样可以避免中文名称不同而重复
                                                    String keys = (String) lists[j];
                                                    redisCls.redis(Config.redisDB_Search).del(keys);
                                                }
                                            }
                                            redisCls.redis(Config.redisDB_Search).set(rows, rows);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        logger.error(e.toString());
                    }

                }
            }
        }
        logger.info("初始化所有股票搜索库数据-完成，用时："+(System.currentTimeMillis()-t)+"ms");
    }

    /**
     * 初始化请求股票实时行情数据
     * http://kline.api.dashixiong.cn/V1/search.php
     */
    private void initStockRealtimeQuotes(){
        long t = System.currentTimeMillis();
        String params = "";
        String token = FN.MD5(params+t+Config.appKey+Config.appSecret);
        params = params + "t="+t+"&app_key="+Config.appKey+"&token="+token;
        String url = Config.socketServer+"/allstockquotes.php?"+params;
//        logger.info("开始初始化所有股票搜索库数据"+url);
        String html = HttpWebCollecter.getWebContent(url);
        if (html!=null){
            if (!html.isEmpty()){
                if (html.startsWith("{")){
                    try {
                        JSONObject obj = new JSONObject(html);
                        if (obj!=null){
                            JSONObject data = obj.getJSONObject("data");
                            if (data!=null){
                                String names = data.getString("names");
                                names = names.replace("[","");
                                names = names.replace("]","");
                                JSONArray namekeys = new JSONArray("["+names+"]");
                                JSONArray datas = data.getJSONArray("datas");
                                for (int i = 0; i < datas.length(); i++) {
                                    String jsonStr = datas.getString(i);
//                                    logger.debug(jsonStr);
                                    JSONObject shObj = FN.stoObj(jsonStr, namekeys);
                                    if (shObj.length()>0) {
                                        /**
                                         * 这里写入redis
                                         */
                                        if(shObj.has("code")) {
                                            String code = shObj.getString("code");
                                            if (code.startsWith("sh") || code.startsWith("sz")) {
                                                localRedis.setValue(code, shObj.toString());
                                            }

                                        }
                                    }
                                    shObj = null;
                                    jsonStr = null;
                                }
                                namekeys = null;
                                names = null;
                            }
                            data = null;
                        }
                        obj = null;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        logger.error(e.toString());
                    }

                }
            }
        }
        html = null;
        logger.info("初始化所有股票实时行情数据-完成，用时："+(System.currentTimeMillis()-t)+"ms");
    }
}


