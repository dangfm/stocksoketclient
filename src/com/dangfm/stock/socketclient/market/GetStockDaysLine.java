package com.dangfm.stock.socketclient.market;

import com.dangfm.stock.socketclient.Config;
import com.dangfm.stock.socketclient.utils.FN;
import com.dangfm.stock.socketclient.utils.FileHelper;
import com.dangfm.stock.socketclient.utils.RedisCls;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;

/**
 * 采集日K
 * 每天凌晨和清晨采集一遍
 *
 * Created by dangfm on 16/4/20.
 */
public class GetStockDaysLine extends Thread{
    public static final Logger logger = LoggerFactory.getLogger(GetStockDaysLine.class);
    // 出错等待时间
    private static int weittime = 10; // 10次
    // 超时十次跳过
    private static int loopTime = 0;
    // 保存的路径
    private static String klinePath = Config.klineSavePath.isEmpty()? FN.getProjectPath():Config.klineSavePath;

    /**
     * 本地redis
     */
    private static RedisCls localRedis = new RedisCls();
    private static RedisCls redis = new RedisCls();

    public GetStockDaysLine(){


    }

    @Override
    public void run() {
        super.run();
        if (Config.isCollectionHistoryKline) {
//        // 如果周六周天停止运行
            if (FN.getWeekOfDate(new Date(System.currentTimeMillis())).equals("6") ||
                    FN.getWeekOfDate(new Date(System.currentTimeMillis())).equals("7")) {
                logger.info("日k采集周六日不运行");
//            return;
            }
            getMarket();
        }else{
            logger.info("未开启采集K线开关，如需采集历史K线数据请设置配置文件 isCollectionHistoryKline=true");
        }

    }

    /**
     * 采集日k数据入口方法
     * 集成采集所有沪深股市的日k数据,包括历史数据和最新数据
     */
    public void getMarket(){

        while (true) {
            if (Config.isCollectionHistoryKline) {
                // 同步k线数据
                getMarket_Kline("day");
            }
            if (Config.isCollectionHistoryMinKline) {
                getMarket_Kline("min1");
            }
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            getMarket_Kline("min5");
            if (FN.isStopTime("00 9")) return;
        }

    }



    /**
     * 同步所有日K数据 包括原始数据,复权数据
     * @param
     */
    public void getMarket_Kline(String cycle){
        Set s = localRedis.redis(Config.redisDB_Search).keys("*");
        logger.info("开始同步原始k线"+cycle+"数据" + s.size());
        Object[] list = s.toArray();
        for (int i=0;i<list.length;i++){
            try {
                String key = (String) list[i];
                if (key.indexOf("|") >= 0) {
                    String[] a = key.split("\\|");
                    //System.out.println(a[0]);
                    if (a.length > 0) {
                        key = a[0];
                        String path = klinePath+"/"+cycle+"/after/";
                        String fileName = path+key.toUpperCase()+".txt";
                        String klineData = FileHelper.readToString(fileName);
                        String lastDate = "";
                        // 检查有没有最新的k线数据
                        boolean isExit = checkDataIsNewDate(klineData,key,i);
                        if (isExit) continue;

                        int pageSize = 100000;
                        if (cycle.equals("min1")){
                            if (!klineData.isEmpty()) pageSize = 240;
                        }

                        if (!isExit) {
                            String params = "code=" + key + "&cycle="+cycle+"&fq=data&page=1&pageSize="+pageSize;
                            long t = System.currentTimeMillis();
                            String token = FN.MD5(params+t+Config.appKey+Config.appSecret);
                            params = params + "&t="+t+"&app_key="+Config.appKey+"&token="+token;
                            String url = Config.socketServer+Config.socketServer_kline + "?"+params;
                            loopTime = 0;
                            String value = getContent(url);
                            if (!value.isEmpty()) {
                                // 如果是1分钟，检查拿到的是数据是否是最新日期的
                                if (cycle.equals("min1")&&!klineData.isEmpty()){
                                    if (!checkDataIsNewDate(value,key,i)){
                                        continue;
                                    }else{
                                        // 合并增量
                                        value = klineData.replace("]","")+","+value.replace("[","");
                                    }
                                }
                                path = klinePath+"/"+cycle+"/data/";
                                fileName = path+key.toUpperCase()+".txt";
                                FileHelper.makeDirs(path);
                                // 保存到本地
                                FileHelper.createFile(fileName,value);
                                logger.info(i + "/" + list.length + " " + key + " k线"+cycle+"原始数据同步完成-外网");

                            }

                            sleep(1000);

                            params = "code=" + key + "&cycle="+cycle+"&fq=before&page=1&pageSize="+pageSize;
                            t = System.currentTimeMillis();
                            token = FN.MD5(params+t+Config.appKey+Config.appSecret);
                            params = params + "&t="+t+"&app_key="+Config.appKey+"&token="+token;
                            url = Config.socketServer+Config.socketServer_kline + "?"+params;
                            loopTime = 0;
                            value = getContent(url);
                            if (!value.isEmpty()) {
                                // 如果是1分钟，检查拿到的是数据是否是最新日期的
                                if (cycle.equals("min1")&&!klineData.isEmpty()){
                                    if (!checkDataIsNewDate(value,key,i)){
                                        continue;
                                    }else{
                                        // 合并增量
                                        value = klineData.replace("]","")+","+value.replace("[","");
                                    }
                                }
                                path = klinePath+"/"+cycle+"/before/";
                                fileName = path+key.toUpperCase()+".txt";
                                FileHelper.makeDirs(path);
                                // 保存到本地
                                FileHelper.createFile(fileName,value);
                                logger.info(i + "/" + list.length + " " + key + " k线"+cycle+"前复权数据同步完成-外网");

                            }

                            sleep(1000);

                            params = "code=" + key + "&cycle="+cycle+"&fq=after&page=1&pageSize="+pageSize;
                            t = System.currentTimeMillis();
                            token = FN.MD5(params+t+Config.appKey+Config.appSecret);
                            params = params + "&t="+t+"&app_key="+Config.appKey+"&token="+token;
                            url = Config.socketServer+Config.socketServer_kline + "?"+params;
                            loopTime = 0;
                            value = getContent(url);
                            if (!value.isEmpty()) {
                                // 如果是1分钟，检查拿到的是数据是否是最新日期的
                                if (cycle.equals("min1")&&!klineData.isEmpty()){
                                    if (!checkDataIsNewDate(value,key,i)){
                                        continue;
                                    }else{
                                        // 合并增量
                                        value = klineData.replace("]","")+","+value.replace("[","");
                                    }
                                }
                                path = klinePath+"/"+cycle+"/after/";
                                fileName = path+key.toUpperCase()+".txt";
                                FileHelper.makeDirs(path);
                                // 保存到本地
                                FileHelper.createFile(fileName,value);
                                logger.info(i + "/" + list.length + " " + key + " k线"+cycle+"后复权数据同步完成-外网");

                            }
                            value = null;
                        }

                    }
                }


            }catch (Exception e){
                e.printStackTrace();
            }

            try {
                sleep(1000);
            } catch (InterruptedException e) {

            }

        }
        System.gc();
    }

    private boolean checkDataIsNewDate(String data,String code,int i){
        boolean isExit = false;
        // 先看看有没有最新的数据
        String json = redis.getValue(code);
        if (json!=null) {
            JSONObject obj = null;
            try {
                obj = new JSONObject(json);
                if (obj != null) {
                    String lastDate = obj.getString("lastDate");
                    lastDate = lastDate.replace("-","");
                    // 读取股票k线所有数据
                    // 先拿出旧的文件

                    if (!data.isEmpty()) {
                        if (data.startsWith("[")) {
                            JSONArray datas = new JSONArray(data);
                            // 查找最新K线日期
                            int k = 0;
                            for (int j = datas.length() - 1; j >= 0; j--) {
                                String lineStr = datas.getString(j);
                                if (lineStr.indexOf(",") > 0) {
                                    String[] line = lineStr.split(",");
//                                            logger.info(line);
                                    if (line.length > 0) {
                                        String lineDate = line[0];
                                        if (!lineDate.isEmpty()) {
                                            if (Integer.parseInt(lineDate) >= Integer.parseInt(lastDate)) {
                                                // 已经有最新的K线数据了，不需要再采集
                                                logger.info(i+"/"+code+"最新K数据已存在" + lineStr);
                                                isExit = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                k++;
                                if (k > 5) break;
                            }
                            datas = null;
                        }
                    }

                }
                obj = null;
            }catch (JSONException e) {
                logger.error(e.toString());
            }
        }
        json = null;
        return isExit;
    }



    /**
     * 采集K线内容回来
     * @param urlStr
     * @return
     */
    public String getContent(String urlStr){
        StringBuffer sb = new StringBuffer();
        try{
//            logger.info("请求K线:"+urlStr+"");
            java.net.URL url = new java.net.URL(urlStr);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while((line = in.readLine()) != null){
                sb.append(line);
            }
            in.close();
            if (sb.toString().startsWith("{")) {
                JSONObject obj = new JSONObject(sb.toString());
                if (obj != null) {
                    boolean success = obj.getBoolean("success");
                    if (success){
                        JSONArray data = obj.getJSONArray("data");
                        return data.toString();
                    }else {
                        logger.error("请求K线出错" + obj.toString());
                        return "";
                    }
                }else{
                    logger.error("请求K线出错" + obj.toString());
                    return "";
                }
            }
            url = null;
        }catch(Exception e)  {
            //  Report  any  errors  that  arise
            //sb.append(e.toString());
            logger.error("请求K线出错"+loopTime+"次");
            loopTime ++;
            if (loopTime<weittime) {
                try {
                    sleep(3000);
                    return getContent(urlStr);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }


        }
        return  sb.toString();
    }




}
