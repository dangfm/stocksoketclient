package com.dangfm.stock.socketclient.market;

import com.dangfm.stock.socketclient.Config;
import com.dangfm.stock.socketclient.utils.HttpWebCollecter;
import com.dangfm.stock.socketclient.utils.RedisCls;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 分钟线k线生成器
 * 主要是根据一分钟的k线生成5分15分30分60分钟k历史k线
 * Created by dangfm on 17/3/4.
 */
public class CreateMinuteKline extends Thread {
    public static final Logger logger = LoggerFactory.getLogger(CreateMinuteKline.class);
    // k线缓存地址
    private String cachePath = Config.klineSavePath;
    private static RedisCls localRedis = new RedisCls();
    // 限制保存多少根历史K线
    private int maxKlineCount = 1000; // 最多保存一千根历史K线
    @Override
    public void run() {
        super.run();
//        logger.info("开始生成分钟K线");

        getAllStocks();

    }

    /**
     * 同步行情数据过来一次
     *
     */
    public void getAllStocks(){
        Set s = localRedis.redis(Config.redisDB_Search).keys("*");
//        logger.info("开始采集公司资料"+s.size());
        Object[] list = s.toArray();
        for (int i=0;i<list.length;i++){
            String key = (String) list[i];
            if (key.indexOf("|")>=0) {
                String[] a = key.split("\\|");
                //logger.info(a[0]);
                if (a.length > 0) {
                    key = a[0];
                    createMinuteKlineWithCode(key);
                    System.out.println("" + key + " 分钟线生成完成" + i + "/" + list.length);
                }
            }
        }
        // getAllStocks_A();

    }


    private void createMinuteKlineWithCode(String code){
        // 首先获取今天一分钟原始数据
        JSONArray minuteData = getOldMinuteData(code, 1);
//        logger.info("拿到原始数据"+minuteData.toString());
        // 将一分钟转换成其他分钟数据
        // 5分钟
        JSONArray minute5Data = returnMinuteData(minuteData,code,5);
        // 15分钟
        JSONArray minute15Data = returnMinuteData(minuteData,code,15);
        // 30分钟
        JSONArray minute30Data = returnMinuteData(minuteData, code, 30);
        // 60分钟
        JSONArray minute60Data = returnMinuteData(minuteData, code, 60);

        // 开始存储数据
        saveMinuteData(minute5Data,code,5);
        saveMinuteData(minute15Data,code,15);
        saveMinuteData(minute30Data,code,30);
        saveMinuteData(minute60Data,code,60);

    }

    private void saveMinuteData(JSONArray data,String code,int minute){
        if (data==null) return;
        String newString = "";
        // 存数据前要把旧数据取出来
        JSONArray oldMinuteData = getOldMinuteData(code,minute);
        if (oldMinuteData.length()>maxKlineCount){
            // 超过一千,就去除一部分

        }
        if (oldMinuteData!=null && oldMinuteData.length()>0) {
            // 把数据拼起来然后一起存进去
            try {
                // 看看最新的日期
                JSONArray last = oldMinuteData.getJSONArray(oldMinuteData.length()-1);
                String lastDateTime = last.getString(0);
                lastDateTime = lastDateTime.substring(0,8);
                JSONArray first = data.getJSONArray(0);
                String firstDateTime = first.getString(0);
                firstDateTime = firstDateTime.substring(0,8);
                if (!lastDateTime.equals(firstDateTime)){
                    for (int i = 0; i < data.length(); i++) {
                        JSONArray line = data.getJSONArray(i);
                        oldMinuteData.put(line);
                        line = null;
                    }
                }else{
                    System.out.println("生成" + minute + "分钟k线的日期已经是最新的,无需生成");
                    return;
                }

                newString = oldMinuteData.toString();
            } catch (JSONException e) {
                logger.error(e.toString());
            }


        }else{
            // 直接存储
            newString = data.toString();

        }
        if (!newString.isEmpty()){
            if (newString.length()>10) {
                // 存储
                String savePath = cachePath + "" + minute + "min/" + code.toUpperCase() + ".txt";
                try {
                    HttpWebCollecter.saveFile(savePath, newString);
                    System.out.println("生成" + minute+"分钟k线成功");
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }
        }
    }

    /**
     * 获取k线历史数据
     * @param code
     * @param minute
     * @return
     */
    private JSONArray getOldMinuteData(String code,int minute){
        String minCachePath = cachePath+""+minute+"min/"+code.toUpperCase()+".txt";
        System.out.println(minute+"分钟原始数据路径"+minCachePath);
        String result = HttpWebCollecter.readFile(minCachePath);
        JSONArray line = new JSONArray();
        try {
            if (result!=null) {
                if (!result.isEmpty()) {
                    // 转换成json数组
                    line = new JSONArray(result);
                }
            }
        } catch (JSONException e) {
            logger.error(e.toString());
        }
        // 获取k线数据
        return line;
    }

    /**
     * 返回分钟k线
     * @param daysdatas     原始一分钟k线数据
     * @param code          股票代码
     * @param minute        返回的分钟类型 5=5分钟 15=15分钟,以此类推
     * @return
     */
    private JSONArray returnMinuteData(JSONArray daysdatas,String code,int minute){
        JSONArray newa = new JSONArray();
        double mHeightPrice = 0.0;
        double mLowPrice = 100000000;
        double mVolumn = 0.0;
        double mVolumnPrice = 0.0;
        double mClosePrice = 0.0;
        double mOpenPrice = 0.0;
        String mStartDate = "";
        double mYestodayClosePrice = 0;
        for (int i = 0; i < daysdatas.length(); i++) {
            try {
                JSONArray rs = daysdatas.getJSONArray(i);
                String datetime = rs.getString(0);
                String minstr = datetime.substring(10,12);
                int min = Integer.parseInt(minstr);
                int fenzhong = Integer.parseInt(datetime.substring(8,12));
                if ((min%minute==0 && i>1) || (fenzhong==1030 && minute==60) || (fenzhong==1130 && minute==60)) {
                    if (fenzhong==1000 && minute==60) {

                    }else if (fenzhong==1100 && minute==60) {

                    }else {
                        JSONArray line = new JSONArray();
                        line.put(datetime);
                        line.put(mYestodayClosePrice);
                        line.put(mOpenPrice);
                        line.put(mHeightPrice);
                        line.put(mLowPrice);
                        line.put(mClosePrice);
                        line.put(mVolumn);
                        line.put(mVolumnPrice);

                        // 添加进数组
                        newa.put(line);
                        // 恢复初始化
                        mHeightPrice = 0;
                        mLowPrice = 100000000;
                        mVolumn = 0;
                        mVolumnPrice = 0;
                        mClosePrice = 0;
                        mOpenPrice = 0;
                        mYestodayClosePrice = 0;
                    }
                }
                if (rs.getDouble(3)>mHeightPrice) {
                    mHeightPrice = rs.getDouble(3);
                }
                if (rs.getDouble(4)<mLowPrice){
                    mLowPrice = rs.getDouble(4);
                }
                mVolumn += rs.getDouble(6);
                mVolumnPrice += rs.getDouble(7);
                mClosePrice = rs.getDouble(2);
                if (mOpenPrice<=0) {
                    mOpenPrice = rs.getDouble(5);
                    mYestodayClosePrice = rs.getDouble(5);
                }
            } catch (JSONException e) {
                logger.error(e.toString());
            }

        }


        return newa;
    }
}
