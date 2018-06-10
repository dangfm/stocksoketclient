package com.dangfm.stock.socketclient.server;

import com.dangfm.stock.socketclient.Config;
import com.dangfm.stock.socketclient.market.InitAllStockDatas;
import com.dangfm.stock.socketclient.market.SaveAllStocks;
import com.dangfm.stock.socketclient.market.SaveKlineDatas;
import com.dangfm.stock.socketclient.user.UserHelper;
import com.dangfm.stock.socketclient.utils.FN;
import com.dangfm.stock.socketclient.utils.FileHelper;
import com.dangfm.stock.socketclient.utils.RedisCls;
import com.dangfm.stock.socketclient.utils.ZipUtils;
import com.sun.org.apache.xml.internal.security.Init;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xsocket.connection.INonBlockingConnection;
import redis.clients.jedis.Protocol;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class StockResposeDatas {
    public static final Logger logger = LoggerFactory.getLogger(StockResposeDatas.class);

    private static ReentrantLock lock = new ReentrantLock();

    /**
    保存接收到的数据
     */
    private static String datas;
    /**
     * 保存拿到的键值信息
     */
    private static JSONArray names = new JSONArray();
    /**
     * 保存拿到的股票行情
     */
    private static JSONObject stockRealTimes = new JSONObject();

    private static INonBlockingConnection nbc;

    /**
     * 本地redis
     */
    private static RedisCls redisCls = new RedisCls();

    /**
     * 解析类
     * @param data
     */
    public static void StockResposeDatas(String data, INonBlockingConnection nbcs){
        nbc = nbcs;
        if (Config.isConnected) {
            // 先url解码
            if (data.length()>0) {
                try {
                    datas = data;
                    float size = datas.getBytes().length / 1024;
                    // 解压一下
                    String str = ZipUtils.unzip(datas);

                    float afterSize = str.getBytes().length / 1024;
//                    logger.info("接收到数据，原大小:" + size + "k 解压后:" + afterSize + "k" + " ");
                    if (str.startsWith("{")) {
                        JSONObject obj = new JSONObject(str);
                        // 协议和数据
                        if (obj.has("protocol") && obj.has("data")) {
                            // 协议名称
                            String protocol = obj.getString("protocol");
                            // 数据
                            String dataStr = obj.getString("data");
                            dataStr = URLDecoder.decode(dataStr,"UTF-8");

//                            logger.info(dataStr);
                            /**
                             * 解析注册
                             */
                            if (protocol.equals(Config.protocol_register)) displayRegister(dataStr);
                            /**
                             * 解析登陆验证
                             */
                            if (protocol.equals(Config.protocol_checklogin)) displayCheckLogin(dataStr);
                            /**
                             * 解析实时行情数据键值数据
                             */
                            if (protocol.equals(Config.protocol_json_names)) displayJsonNames(dataStr);

                            /**
                             * 解析实时行情数据
                             */
                            if (protocol.equals(Config.protocol_realtime)) displayRealtime(dataStr);
                            /**
                             * 解析股票行业板块
                             */
                            if (protocol.equals(Config.protocol_hangye_tables)) displayAllStockTypes_hangye(dataStr);
                            /**
                             * 解析股票概念板块
                             */
                            if (protocol.equals(Config.protocol_gainian_tables)) displayAllStockTypes_gainian(dataStr);
                            /**
                             * 解析股票地区板块
                             */
                            if (protocol.equals(Config.protocol_diqu_tables)) displayAllStockTypes_diqu(dataStr);
                            /**
                             * 解析股票分类表
                             */
                            if (protocol.equals(Config.protocol_allstocks)) displayAllStocks(dataStr);
                            /**
                             * 解析股票搜索表
                             */
                            if (protocol.equals(Config.protocol_searchstocks)) displaySearchStocks(dataStr);

                            /**
                             * 解析当天日k历史行情数据
                             */
                            if (protocol.equals(Config.protocol_todayKline)) displayTodayKlineData(dataStr);
                            if (protocol.equals(Config.protocol_todayKline_before)) displayTodayKlineData_before(dataStr);
                            if (protocol.equals(Config.protocol_todayKline_after)) displayTodayKlineData_after(dataStr);

                            /**
                             * 解析当天1分钟历史行情数据
                             */
                            if (protocol.equals(Config.protocol_today1MinuteKline)) displayToday1MinuteKlineData(dataStr);
                            if (protocol.equals(Config.protocol_today1MinuteKline_before)) displayToday1MinuteKlineData_before(dataStr);
                            if (protocol.equals(Config.protocol_today1MinuteKline_after)) displayToday1MinuteKlineData_after(dataStr);
                        }


                        obj = null;

                    }
                } catch (JSONException e) {
                    logger.error(e.toString());
                } catch (UnsupportedEncodingException e) {
                    logger.error(e.toString());
                }
            }
        }
    }

    /**
     * 解析注册
     * @param data
     */
    private static void displayRegister(String data){
        if (data.length()>0) {
            try {
                if (data.startsWith("{")) {
                    JSONObject obj = new JSONObject(data);
                    int error = obj.getInt("error");
                    String msg = obj.getString("msg");
                    logger.info(msg);
                    if (error > 0) {
                        // 注册失败

                    }else{
                        // 注册成功会发送这个app_key app_secret
                        UserHelper.register(msg,nbc);

                    }
                }
            } catch (JSONException e) {
                logger.error(e.toString());
            }
        }

    }

    /**
     * 解析登陆验证
     * @param data
     */
    private static void displayCheckLogin(String data){
        if (data.length()>0) {
            try {
                if (data.startsWith("{")) {
                    JSONObject obj = new JSONObject(data);
                    int error = obj.getInt("error");
                    String msg = obj.getString("msg");
                    logger.info(msg);
                    if (error > 0) {
                        Config.isreConnected = false;
                    }else{
                        // 登陆成功
                        StockClientHeart.StockClientHeart(nbc);
                        // 初始化数据
                        InitAllStockDatas initAllStockDatas = new InitAllStockDatas();
                        initAllStockDatas.start();
                    }
                }
            } catch (JSONException e) {
                logger.error(e.toString());
            }
        }

    }

    /**
     * 解析键值信息
     * @param data
     */
    private static void displayJsonNames(String data){
        try {
            names.put(0,new JSONArray(data));

        } catch (JSONException e) {
            logger.error(e.toString());
        }
    }

    /**
     * 解析实时行情
     * @param str
     */
    private static void displayRealtime(String str){
            try {
                JSONObject obj = new JSONObject(str);
                String stockCode = "";
                JSONArray namekeys = names.getJSONArray(0);
                if (obj!=null) {
                    String lastTime = "";
                    String key = obj.names().getString(0);
                    if (obj.has(key)) {
                        String sh000001_obj = null;
                        sh000001_obj = obj.getString(key);
                        JSONObject shObj = FN.stoObj(sh000001_obj, namekeys);
                        if (shObj.length()>0) {
                            lastTime = shObj.getString("lastTime");
                            stockCode = shObj.getString("code");
//                            logger.info("接收到键值"+ namekeys);
//                            logger.info(shObj.toString());
                        }
                    }



                    // 写入redis
                    JSONArray keys = obj.names();
                    for (int i=0;i<keys.length();i++){
                        String code = keys.getString(i);
                        if (obj.has(code)) {
                            String jsonStr = obj.getString(code);

                            JSONObject shObj = FN.stoObj(jsonStr, namekeys);
                            if (shObj.length()>0) {
                                stockRealTimes.put(code, shObj);
                                /**
                                 * 这里写入redis
                                 */
                                redisCls.setValue(code, shObj.toString());
                            }

                        }
                    }
                    // 共享给其他服务用
                    Config.realtimeStockDatas = obj;
//                    logger.info(obj.toString());
                    logger.info("接收到实时行情"+keys.length()+"笔 "+stockCode+"最新时间：" + lastTime+" 当前时间："+FN.getDateWithFormat("HH:mm:ss",new Date())+" 总共"+stockRealTimes.length()+"个股票");

                }
                obj = null;
            } catch (JSONException e) {
                logger.error(e.toString());
            }

    }

    /**
     * 解析当天日K历史数据
     * @param data
     */
    private static void displayTodayKlineData(String data){
        try {
            JSONObject obj = new JSONObject(data);
            String code = obj.names().getString(0);
            String value = obj.getString(code);

            SaveKlineDatas.saveDayKline(code,value,"datas");

        } catch (JSONException e) {
            logger.error(e.toString());
        }
    }
    private static void displayTodayKlineData_before(String data){
        try {
            JSONObject obj = new JSONObject(data);
            String code = obj.names().getString(0);
            String value = obj.getString(code);

            SaveKlineDatas.saveDayKline(code,value,"before");

        } catch (JSONException e) {
            logger.error(e.toString());
        }
    }
    private static void displayTodayKlineData_after(String data){
        try {
            JSONObject obj = new JSONObject(data);
            String code = obj.names().getString(0);
            String value = obj.getString(code);

            SaveKlineDatas.saveDayKline(code,value,"after");

        } catch (JSONException e) {
            logger.error(e.toString());
        }
    }

    /**
     * 解析当天1分钟历史数据
     * @param data
     */
    private static void displayToday1MinuteKlineData(String data){
        try {
            JSONObject obj = new JSONObject(data);
            String code = obj.names().getString(0);
            String value = obj.getString(code);

            SaveKlineDatas.saveMinuteKline(code,value,"datas");

        } catch (JSONException e) {
            logger.error(e.toString());
        }

    }
    private static void displayToday1MinuteKlineData_before(String data){
        try {
            JSONObject obj = new JSONObject(data);
            String code = obj.names().getString(0);
            String value = obj.getString(code);
            SaveKlineDatas.saveMinuteKline(code,value,"before");
        } catch (JSONException e) {
            logger.error(e.toString());
        }
    }
    private static void displayToday1MinuteKlineData_after(String data){
        try {
            JSONObject obj = new JSONObject(data);
            String code = obj.names().getString(0);
            String value = obj.getString(code);
            SaveKlineDatas.saveMinuteKline(code,value,"after");
        } catch (JSONException e) {
            logger.error(e.toString());
        }
    }

    /**
     * 解析股票板块表 行业板块
     * @param data
     */
    private static void displayAllStockTypes_hangye(String data){
        try {
            JSONArray obj = new JSONArray(data);
            SaveAllStocks.saveAllStockType_hangye(obj);
        } catch (JSONException e) {
            logger.error(e.toString());
        }
    }

    /**
     * 解析股票板块表 概念板块
     * @param data
     */
    private static void displayAllStockTypes_gainian(String data){
        try {
            JSONArray obj = new JSONArray(data);
            SaveAllStocks.saveAllStockType_gainian(obj);
        } catch (JSONException e) {
            logger.error(e.toString());
        }
    }

    /**
     * 解析股票板块表 地区板块
     * @param data
     */
    private static void displayAllStockTypes_diqu(String data){
        try {
            JSONArray obj = new JSONArray(data);
            SaveAllStocks.saveAllStockType_diqu(obj);
        } catch (JSONException e) {
            logger.error(e.toString());
        }
    }

    /**
     * 解析股票分类表
     * @param data
     */
    private static void displayAllStocks(String data){
        try {
            JSONObject obj = new JSONObject(data);
            String key = obj.names().getString(0);
            String value = obj.getString(key);
            SaveAllStocks.saveAllStocks(key,value);
        } catch (JSONException e) {
            logger.error(e.toString());
        }
    }

    /**
     * 解析股票搜索表
     * @param data
     */
    private static void displaySearchStocks(String data){
        try {
            JSONObject obj = new JSONObject(data);
            String key = obj.names().getString(0);
            String value = obj.getString(key);
            SaveAllStocks.saveSearchStocks(key,value);
        } catch (JSONException e) {
            logger.error(e.toString());
        }
    }
}
