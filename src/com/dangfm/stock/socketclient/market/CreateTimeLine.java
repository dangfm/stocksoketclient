package com.dangfm.stock.socketclient.market;

import com.dangfm.stock.socketclient.Config;
import com.dangfm.stock.socketclient.utils.FN;
import com.dangfm.stock.socketclient.utils.RedisCls;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Set;

/**
 * 实时分钟线和分时线生成
 */
public class CreateTimeLine extends Thread {
    public static final Logger logger = LoggerFactory.getLogger(CreateMinuteKline.class);
    // 保存的路径
    private static String klinePath = Config.klineSavePath.isEmpty()? FN.getProjectPath():Config.klineSavePath;

    /**
     * 本地redis
     */
    private static RedisCls localRedis = new RedisCls();
    private static RedisCls redis = new RedisCls();

    public CreateTimeLine(){

    }

    @Override
    public void run() {
        super.run();

        while (true) {

            if (Config.isConnected) {
                break;
            }

            try {
                sleep(1000);
            } catch (InterruptedException e) {
                logger.error(e.toString());
            }


        }

//        logger.info("开始生成分钟线分时线进程");

        // 先初始化所有分钟点位置
//        initAllPoint();
//        logger.info("分钟线线程加载初始化完成");
        // 接下来就是计算每个点的数据
        while (true){
            if (FN.isStopTime("29 09")) {
//                long t = System.currentTimeMillis();
                createAll();
//                t = System.currentTimeMillis() - t;
//                logger.info("生成分钟线分时线耗时" + t);
            }else{
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (FN.isStopTime("10 15")) {
                logger.info("生成分钟线分时线进程15:10结束");
                return;
            }

            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void initAllPoint(){
        redis.select(Config.redisDB_minKlineData);
        Set s = localRedis.redis(Config.redisDB_Search).keys("*");
        localRedis.select(Config.redisDB_market);
        Object[] list = s.toArray();
        logger.info("分钟线线程加载初始化开始"+list.length);
        for (int i=0;i<list.length;i++) {
            try {
                String key = (String) list[i];
                if (key.indexOf("|") >= 0) {
                    String[] a = key.split("\\|");
                    if (a.length > 0) {
                        key = a[0];
//                        initOnePoint(key);
//                        System.out.println(key+" "+i+"/"+list.length);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.toString());
            }
        }
    }


    /**
     * 初始化所有分钟点
     * @param code
     */
    private void initOnePoint(String code){
        String key = code+"_today";
        long startTime = FN.getTimeMillis("09:30:00");
        for (int i=0;i<=330;i++){
            if (i<=120 || i>210){
                String filed = FN.getDateWithFormat("HHmm",new Date(startTime));
                String value = "";
                if (!filed.isEmpty()) {
                    // 如果当前有了最新的就不要替换了
                    String v = redis.getRedis().hget(key,filed);
//                    logger.info(v);
                    if (v!=null) {
                        if (v.indexOf(",")>0) {
                            String[] vs = v.split(",");
                            // 拿到这个数据的日期
                            String date = vs[0];
                            // 拿实时行情的日期
                            JSONObject obj = FN.getRealtime(code,localRedis);
                            if (obj!=null) {
                                try {
//                                    logger.info(obj.toString());
                                    if (obj.has("lastDate")) {
                                        String datetime = obj.getString("lastDate");
                                        datetime = datetime.replace("-", "");
                                        if (Integer.parseInt(date) == Integer.parseInt(datetime)) {
                                            // 是最新实时分钟行情就保留
                                            value = v;
//                                        logger.info("日期相同："+v);
                                        }
                                    }
                                } catch (JSONException e) {
                                    logger.error(e.toString());
                                }
                            }
                            obj = null;

                        }
                    }
                    v = null;
                    redis.getRedis().hset(key, filed, value);

                }
                filed = null;
                value = null;
            }
            startTime += 60 * 1000;
        }
    }


    private void createAll(){
        redis.select(Config.redisDB_minKlineData);
        localRedis.select(Config.redisDB_market);
        JSONObject objlist = Config.realtimeStockDatas;
//        logger.debug("有多少个股票啊："+objlist.length());
        if (objlist.length()<=0) return;
        JSONArray list = objlist.names();

        if (list.length()<=0) return;

        for (int i=0;i<list.length();i++) {
            try {
                String key = list.getString(i);
//                logger.info(key);
                if (key==null) continue;
                if (key.startsWith("sz") || key.startsWith("sh")) {
                    // 先看看有没有最新的数据
                    String json = localRedis.getValue(key);
                    if (json != null) {
                        JSONObject obj = null;
                        try {
                            obj = new JSONObject(json);
                            if (obj != null) {
                                String hKey = key+"_today";
                                if (obj.has("lastTime")) {
                                    String lastTime = obj.getString("lastTime");
                                    if (lastTime.contains(":")) {
                                        String[] times = lastTime.split(":");
                                        lastTime = times[0] + times[1];
                                        String filed = lastTime;
                                        // 拿当前分钟的历史数据
                                        String oneStr = redis.getRedis().hget(hKey, filed);
                                        // 开高低收
                                        double open, high, low, close, volumn, volumnPrice, price,startVolumn;
                                        int date;
                                        String time = lastTime;
                                        date = Integer.parseInt(obj.getString("lastDate").replace("-", ""));
                                        price = obj.getDouble("price");
                                        volumn = obj.getDouble("volumn");
                                        volumnPrice = obj.getDouble("volumnPrice");
                                        startVolumn = volumn;
                                        open = price;
                                        high = price;
                                        low = price;
                                        close = price;

                                        if (oneStr != null) {
                                            if (!oneStr.isEmpty()) {
                                                String[] one = oneStr.split(",");
                                                if (one.length >= 9) {
                                                    int olddate = Integer.parseInt(one[0]);
                                                    // 看看时间是不是当天的
                                                    if (olddate == date) {
                                                        open = Double.parseDouble(one[2]);
                                                        high = Double.parseDouble(one[3]) > price ? Double.parseDouble(one[3]) : price;
                                                        low = Double.parseDouble(one[4]) < price ? Double.parseDouble(one[4]) : price;
                                                        close = price;
                                                        startVolumn = Double.parseDouble(one[8]);
                                                    } else {
                                                        // 不是当天的时间就清理掉
                                                    }
                                                }
                                                one = null;
                                            }
                                        }

                                        String value = date + "," + time + "," + open + "," + high + "," + low + "," + close + "," + volumn + "," + volumnPrice + "," + startVolumn;

                                        if (!filed.isEmpty()) {
//                                            logger.info(hKey+filed+value);
                                            // 更新这一分钟
                                            redis.getRedis().hset(hKey, filed, value);
                                        }
                                        value = null;
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            logger.error(e.toString());
                        }
                        obj = null;
                    }
                    json = null;
                }
                key = null;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.toString());
            }
        }
        list = null;
        objlist = null;
    }

    private void createOnePointKline(){

    }
}
