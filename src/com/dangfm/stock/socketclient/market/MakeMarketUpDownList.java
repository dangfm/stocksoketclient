package com.dangfm.stock.socketclient.market;

import com.dangfm.stock.socketclient.Config;
import com.dangfm.stock.socketclient.utils.FN;
import com.dangfm.stock.socketclient.utils.RedisCls;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 计算行业涨幅,概念涨幅,地区涨幅
 * 通过股票分类取对应股票的缓存数据计算
 * 开盘 9:10到9:25 涨幅榜都是不显示涨幅和个股的 涨幅显示0.00%,个股显示-,0 0%
 * Created by dangfm on 16/5/7.
 */
public class MakeMarketUpDownList extends Thread{
    private static final Logger logger = LoggerFactory.getLogger(MakeMarketUpDownList.class);
    /**
     * 行业分类列表
     */
    private static JSONArray hangyeList = new JSONArray();
    // 行业涨幅redis缓存key
    private static String redisKey_Hangye = Config.redisKey_Hangye;
    // 概念分类列表
    private static JSONArray gainianList = new JSONArray();
    // 概念涨幅redis缓存key
    private static String redisKey_Gainian = Config.redisKey_Gainian;
    // 地区分类列表
    private static JSONArray diquList = new JSONArray();
    // 地区涨幅redis缓存key
    private static String redisKey_Diqu = Config.redisKey_Diqu;

    // 股票列表
    private static JSONArray stockList = new JSONArray();
    /**
     * 本地redis
     */
    private static RedisCls localRedis = new RedisCls();
    private static RedisCls redis = new RedisCls();

    /**
     * 线程内部停止时间 linux定时任务写法
     */
    private static String stopTime = "30 15"; // 15点10分

    private static Set allStocks = null;

    /**
     * 每次启动运行一遍
     */
    public MakeMarketUpDownList(){

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



//        logger.info("开始计算行情涨跌幅线程");
        while (true){
            if (Config.isConnected) {
                localRedis.select(Config.redisDB_stocks);
                allStocks = localRedis.getRedis().keys("*");

                // 一次计算
                taskOnetime();

                // 如果周六周天停止运行
                if (FN.getWeekOfDate(new Date(System.currentTimeMillis())).equals("6") ||
                        FN.getWeekOfDate(new Date(System.currentTimeMillis())).equals("7")) {
                    logger.info("行情涨跌幅周六日停止运行");
                    return;
                }

                // 这里做停止时间判断
                if (FN.isStopTime(stopTime)) {
                    logger.info("行情涨跌幅15点30分线程内部停止");
                    return;
                }
            }else{
                logger.info("客户端还未连接,行情涨跌幅线程无法计算");
            }

            try {
                sleep(3000);
            } catch (InterruptedException e) {
                logger.error(e.toString());
            }

        }


    }

    /**
     * 单独运行一次
     */
    public void taskOnetime(){

        loadHangyeList();
        loadGainianList();
        loadQuyuList();

        // 计算个股涨跌幅
        stocksOrderUpList("");
        stocksOrderUpList("turnoverRate");
        stocksOrderUpList("swing");
        stocksOrderUpList("totalValue");
        stocksOrderUpList("circulationValue");

        // 计算行业涨幅列表
        createHangyeUpList();
        // 计算概念涨幅列表
        createGainianUpList();
        // 计算地区涨幅列表
        createDiyuUpList();
//        logger.info("行情涨跌幅线程一次");
    }

    /**
     * 加载行情分类列表
     */
    public void loadHangyeList(){
        try {
            localRedis.select(Config.redisDB_upDown);
            String list = localRedis.getValue(Config.redisKey_Hangye_tables);
            hangyeList = new JSONArray(list);

        } catch (JSONException e) {
            logger.error(e.toString());
        }
    }

    /**
     * 加载概念分类列表
     */
    public void loadGainianList(){
        try {
            localRedis.select(Config.redisDB_upDown);
            String list = localRedis.getValue(Config.redisKey_Gainian_tables);
            gainianList = new JSONArray(list);

        } catch (JSONException e) {
            logger.error(e.toString());
        }
    }

    /**
     * 加载地域分类列表
     */
    public void loadQuyuList(){
        try {
            localRedis.select(Config.redisDB_upDown);
            String list = localRedis.getValue(Config.redisKey_Diqu_tables);
            diquList = new JSONArray(list);

        } catch (JSONException e) {
            logger.error(e.toString());
        }
    }

    /**
     * 查询板块下的股票
     * @param typeId
     * @return
     */
    public JSONArray loadStocksWithTypeId(String typeId){
        JSONArray stocklist = new JSONArray();
        try {
            localRedis.select(Config.redisDB_stocks);
            Set s = localRedis.getRedis().keys("*,"+typeId+",*");
            Object[] list = s.toArray();
            for (int i=0;i<list.length;i++)
                try {
                    String key = (String) list[i];
                    // "sz002003:tree:0,1,2,20 typeIds:,20,21,31,102,154,187,196,202,299,350,"
                    String value = key;
                    if (value.indexOf(":tree:") > 0) {
                        String[] a = value.split(":tree:");
                        if (a.length > 1) {
                            String code = a[0];
                            String type = "";
                            if (code.length() > 2) {
                                type = code.substring(0, 2);
                            }
                            String endStr = a[1];
                            if (endStr.indexOf("typeIds:") > 0) {
                                a = endStr.split("typeIds:");
                                if (a.length > 1) {
                                    String tree = a[0];
                                    tree = tree.replace(" ", "");
                                    String typeIds = a[1];
                                    JSONObject obj = new JSONObject();
                                    obj.put("type", type);
                                    obj.put("code", code);
                                    obj.put("typeTree", tree);
                                    obj.put("typeIds", typeIds);

                                    stocklist.put(obj);

                                    obj = null;
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    logger.error(e.toString());
                }

        } catch (Exception e) {
            logger.error(e.toString());
        }

        return stocklist;
    }
    /**
     * 计算行业涨幅排行列表
     */
    public void createHangyeUpList(){
        JSONArray list = createUpList(hangyeList);
//        logger.info(hangyeList.toString());
        if (list.length()>0) {
            localRedis.select(Config.redisDB_upDown);
            localRedis.setValue(redisKey_Hangye, list.toString());
            // 同步到公共redis
        }
        //System.out.println(list.toString());
    }

    /**
     * 计算概念涨幅排行列表
     */
    public void createGainianUpList(){
        JSONArray list = createUpList(gainianList);
        if (list.length()>0) {
            localRedis.select(Config.redisDB_upDown);
            localRedis.setValue(redisKey_Gainian, list.toString());
        }
        //System.out.println(list.toString());
        System.gc();
    }

    /**
     * 计算概念涨幅排行列表
     */
    public void createDiyuUpList(){

        JSONArray list = createUpList(diquList);
        if (list.length()>0) {
            localRedis.select(Config.redisDB_upDown);
            localRedis.setValue(redisKey_Diqu, list.toString());
        }
        //System.out.println(list.toString());
        System.gc();
    }



    public JSONArray createUpList(JSONArray list){
        // 遍历所有行业股票
        JSONArray upList = new JSONArray();
        for (int i = 0; i < list.length(); i++) {
            try {
                JSONObject obj = list.getJSONObject(i);
                JSONObject newObj = new JSONObject();
                String title = obj.getString("title");
                String typeId = obj.getString("id");
                newObj.put("title",title);
                newObj.put("id",typeId);

                // 计算这个分类的平均涨幅
                JSONObject valueObj = createStocksEverageValue(typeId);
                if (valueObj==null) continue;
                double counts = valueObj.getDouble("changeRate");
                String code = valueObj.getString("code");
                newObj.put("rate", counts);
                newObj.put("code", code);
                double change = 0;
                double changeRate = 0;
                String stockName = "";
                // 计算当前股票的涨跌幅
                String json = redis.getValue(code);
//                logger.info(json);
                if (json!=null){
                    JSONObject jsonObject = new JSONObject(json);
                    double price = jsonObject.getDouble("price");
                    double closePrice = jsonObject.getDouble("closePrice");
                    double openPrice = jsonObject.getDouble("openPrice");
                    int type = 0;
                    int isStop = 0;
                    if (jsonObject.has("isStop")){
                        isStop = jsonObject.getInt("isStop");
                    }
                    if (jsonObject.has("type")){
                        type = jsonObject.getInt("type");
                    }
                    if (price>0 && closePrice>0 && type<=0 &&  openPrice>0 && isStop<=0){
                        change = price - closePrice;
                        changeRate = change / closePrice * 100;
                        stockName = jsonObject.getString("name");
                        
                        newObj.put("change",price);
                        newObj.put("changeRate", changeRate);
                        newObj.put("name", stockName);
//                        logger.info(stockName + " 行业涨幅为" + counts + " " + obj.toString());
                        upList.put(newObj);
                    }


                    jsonObject = null;
                }

                obj = null;
                valueObj = null;
                code = null;
                title = null;
                newObj = null;

            } catch (JSONException e) {
                logger.error(e.toString());
            }
        }
        // 排序
        JSONArray newlist = orderUpList(upList);
        upList = null;
        System.gc();
        return newlist;
    }



    /**
     * 负责排序,按涨幅倒叙排列
     * @param list
     * @return
     */
    public JSONArray orderUpList(JSONArray list){
        //JSONArray newList = new JSONArray();
        for (int i = list.length() - 1; i > 0; --i)
        {
            for (int j = 0; j < i; ++j)
            {
                try {
                    // 下一个对象
                    JSONObject nextObj = list.getJSONObject(j+1);
                    // 当前对象
                    JSONObject nowObj = list.getJSONObject(j);
                    double nextChangeRate = nextObj.getDouble("rate");
                    double nowChangeRate = nowObj.getDouble("rate");
                    // 如果下个对象比当前对象大,就调换位置
                    if (nextChangeRate < nowChangeRate)
                    {
                        list.put(j,nextObj);
                        list.put(j+1,nowObj);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
        JSONArray newList = new JSONArray();
        for (int i = list.length() - 1; i > 0; --i){
            try {
                newList.put(list.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        return newList;
    }

    /**
     * 统计股票列表的平均涨幅
     * @param typeId 股票类型ID
     * @return
     */
    public JSONObject createStocksEverageValue(String typeId){
        JSONObject obj = new JSONObject();
        JSONArray list = loadStocksWithTypeId(typeId);
//        logger.info(list.toString());
        // 板块总市值
        double totalValues = totalMoney(list);
        int j = 0; // 有效股票总数
        double counts = 0;// 总的涨跌幅值
        double priceCount = 0;
        double closePriceCount = 0;
        String maxcode = "";// 行业涨幅最高的股票代码
        double maxChangeRate = -1000;
        for (int i = 0; i < list.length(); i++) {
            try {
                String codeJson = list.getString(i);
                // 拿redis数据
                JSONObject codeObject = new JSONObject(codeJson);
                String code = codeObject.getString("code");
                // 拿redis数据
                String json = redis.getValue(code);
                if (json!=null && json.substring(0,1).equals("{")){
                    JSONObject jsonObject = new JSONObject(json);
                    // 价格不能为0
                    double price = jsonObject.getDouble("price");
                    double closePrice = jsonObject.getDouble("closePrice");
                    double openPrice = jsonObject.getDouble("openPrice");
                    double totalValue = 0;
                    if (!jsonObject.getString("circulationValue").isEmpty()) totalValue = jsonObject.getDouble("circulationValue");
                    int type = 0;
                    int isStop = 0;
                    if (jsonObject.has("isStop")){
                        isStop = jsonObject.getInt("isStop");
                    }
                    if (jsonObject.has("type")){
                        type = jsonObject.getInt("type");
                    }
                    if (price>0 && closePrice>0 && type<=0 &&  openPrice>0 && isStop<=0){
                        double quanzhong = totalValue/totalValues;
                        double changeRate = (price - closePrice) / closePrice ;
                        if (changeRate>maxChangeRate){
                            maxcode = code;
                            maxChangeRate = changeRate;
                        }
                        priceCount += price * quanzhong;
                        closePriceCount += closePrice * quanzhong;

                        counts += changeRate * quanzhong;
                        j ++;
                    }
                    jsonObject = null;
                }
                code = null;
                json = null;
            } catch (JSONException e) {
                try {
                    logger.info(list.getString(i));
                } catch (JSONException e1) {
                    logger.error(e.toString());
                }
                logger.error(e.toString());
            }

        }

        if (j>0) {
            Double changeRateAvreage = (priceCount - closePriceCount) / closePriceCount * 100;
            if (changeRateAvreage.isInfinite() || changeRateAvreage.isNaN() || closePriceCount<=0) changeRateAvreage = 0.00;
            try {
                obj.put("changeRate", changeRateAvreage.doubleValue());
                obj.put("code", maxcode);
            } catch (JSONException e) {
                logger.error(e.toString());
            }
        }else{
            obj = null;
        }
        return obj;
    }


    public double totalMoney(JSONArray list){
        double total = 0;
        for (int i = 0; i < list.length(); i++) {

            try {
                String codeJson = list.getString(i);
                // 拿redis数据
                JSONObject codeObject = new JSONObject(codeJson);
                String code = codeObject.getString("code");

                String json = redis.getValue(code);
//                logger.info(code+"="+json);
                if (json != null && json.substring(0,1).equals("{")) {
//                    logger.info(code);
                    JSONObject jsonObject = new JSONObject(json);
                    // 价格不能为0
                    double totalValue = 0;
                    if (!jsonObject.getString("circulationValue").isEmpty()) totalValue = jsonObject.getDouble("circulationValue");
                    double price = jsonObject.getDouble("price");
                    double closePrice = jsonObject.getDouble("closePrice");
                    double openPrice = jsonObject.getDouble("openPrice");
                    int isStop = 0;
                    if (jsonObject.has("isStop")){
                        isStop = jsonObject.getInt("isStop");
                    }
                    int type = 0;
                    if (jsonObject.has("type")){
                        type = jsonObject.getInt("type");
                    }
                    if (price>0 && closePrice>0 && type<=0 &&  openPrice>0 && isStop<=0) {
                        total += totalValue;
//                        logger.info(code+"="+total);
                    }

                }
            } catch (JSONException e) {
                try {
                    logger.info(list.getString(i));
                } catch (JSONException e1) {
                    logger.error(e.toString());
                }
                logger.error(e.toString());

            }
        }
        return total;
    }

    /**
     * 个股涨跌幅排序,按涨幅倒叙排列
     * @return
     */
    public JSONObject stocksOrderUpList(String key){

        Set s = allStocks;
        Object[] list = s.toArray();
        //JSONArray newList = new JSONArray();
        long t = System.currentTimeMillis();
//        logger.info("开始计算个股涨跌幅，总数："+list.length);
        Map<String, Double> map = new TreeMap<String, Double>();
        for (int i = 0; i <list.length; i++)
        {
            try {
                String objstr = (String)list[i];
                if (objstr!=null) {
                    if (!objstr.isEmpty()) {
                        JSONObject nowObj = getStocksCode(objstr);
//                        logger.info(objstr);
                        if (nowObj.length()>0) {
                            double nowChangeRate = getStockChangeRate(nowObj,key);
                            String code = nowObj.getString("code");
                            if (code.indexOf("sh") >= 0 || code.indexOf("sz") >= 0) {
                                map.put(code, nowChangeRate);
//                                logger.info(code+"="+nowChangeRate);
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                logger.error(e.toString());
            }
        }

//        logger.info("map="+map.size());

        JSONObject newList = new JSONObject();
        String listStr = "";
        Map<String, Double> resultMap = sortMapByValue(map);    //按Key进行排序
        for (Map.Entry<String, Double> entry : resultMap.entrySet()) {
            String code = entry.getKey();
            double rate = entry.getValue();
            if (listStr.isEmpty())
                listStr += "{\""+code+"\":"+rate+"";
            else
                listStr += ",\""+code+"\":"+rate+"";


        }
        listStr = listStr + "}";
        JSONObject obj = new JSONObject(resultMap);


//        logger.info(obj.toString());

        localRedis.select(Config.redisDB_upDown);
        if (key.isEmpty())
            localRedis.setValue(Config.redisKey_Stock_UpdownList,listStr);
        else{
            localRedis.setValue(Config.redisKey_Stock_UpdownList+"_"+key,listStr);
        }

//        logger.info("计算个股涨跌幅完成用时："+(System.currentTimeMillis()-t)+" ms");
//        try {
//            sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return newList;
    }


    /**
     * 根据股票表数据拿到股票实时行情对象
     * @param lines
     * @return
     */
    private JSONObject getStocksCode(String lines){
        JSONObject result = new JSONObject();
        if(lines!=null){
            if(!lines.isEmpty() && !lines.equals(null)){
                if (lines.indexOf(":")>0){
                    String[] a = lines.split(":");
                    String code = a[0];
                    if (code.startsWith("sh") || code.startsWith("sz")){
                        String codenumber = code.substring(2);
                        String json = redis.getValue(code);
                        if (json != null) {
                            if (!json.isEmpty()) {
                                if (json.substring(0, 1).equals("{")) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(json);
                                        int isStop = 0;
                                        if (jsonObject.has("isStop")) {
                                            isStop = jsonObject.getInt("isStop");
                                        }
                                        int type = 0;
                                        if (jsonObject.has("type")) {
                                            type = jsonObject.getInt("type");
                                        }
                                        double closePrice = jsonObject.getDouble("closePrice");
                                        double openPrice = jsonObject.getDouble("openPrice");
                                        // 个股和停牌股不挤入内
                                        if (closePrice>0 && openPrice>0 && type == 0 && isStop <= 0 && !codenumber.startsWith("90") && !codenumber.startsWith("20")) {
                                            result = jsonObject;
                                        }
                                    } catch (JSONException e) {
                                        logger.error(e.toString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 给实时行情对象返回涨跌幅
     * @param jsonObject
     * @return
     */
    private double getStockChangeRate(JSONObject jsonObject,String key){
        double rate = 0;
        try {
            if (jsonObject!=null){
                double price = jsonObject.getDouble("price");
                double closePrice = jsonObject.getDouble("closePrice");
                double openPrice = jsonObject.getDouble("openPrice");
                if (price>0 && closePrice>0 && openPrice>0){
                    if (key.isEmpty())
                        rate = (price - closePrice) / closePrice * 100;
                    else{
                        if (jsonObject.has(key))
                            rate = jsonObject.getDouble(key);
                    }
                }
            }
        } catch (JSONException e) {
            logger.error(e.toString());
        }
        return rate;
    }

    /**
     * 使用 Map按key进行排序
     * @param map
     * @return
     */
    public static Map<String, Double> sortMapByKey(Map<String, Double> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, Double> sortMap = new TreeMap<String, Double>(new MapKeyComparator());
        sortMap.putAll(map);
        return sortMap;
    }

    /**
     * 使用 Map按value进行排序
     * @param map
     * @return
     */
    public static Map<String, Double> sortMapByValue(Map<String, Double> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        List<Map.Entry<String, Double>> entryList = new ArrayList<Map.Entry<String, Double>>(map.entrySet());
        Collections.sort(entryList, new MapValueComparator());
        Iterator<Map.Entry<String, Double>> iter = entryList.iterator();
        Map.Entry<String, Double> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }
}

//比较器类
class MapKeyComparator implements Comparator<String> {
    public int compare(String str1, String str2) {
//        System.out.println(str1+"=="+str2);
        return str1.compareTo(str2);
    }
}

//比较器类
class MapValueComparator implements Comparator<Map.Entry<String, Double>> {
    public int compare(Map.Entry<String, Double> me1, Map.Entry<String, Double> me2) {
//        System.out.println(me2.getValue()+"=="+me1.getValue()+"======"+me2.getValue().compareTo(me1.getValue()));
        return me2.getValue().compareTo(me1.getValue());
    }
}
