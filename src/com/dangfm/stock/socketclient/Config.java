package com.dangfm.stock.socketclient;

import com.dangfm.stock.socketclient.utils.FN;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 配置类
 * Created by dangfm on 16/3/19.
 */
public class Config {
    // 配置文件名
    public static final String configFileName = "config.properties"; // 本地测试
//    public static final String configFileName = "ForlightClientConfig.properties"; // 为明服务器

    // 本地 redis 服务器
    public static final String redisServer = FN.getProperty("redisServer");
    public static final int redisPort = Integer.parseInt(FN.getProperty("redisPort"));
    public static final String redisPassword = FN.getProperty("redisPassword");

    // 服务端口号
    public static String serverPort = "";
    public static String serverIP = "";

    // 开发者信息
    public static String appKey = FN.getProperty("appKey");
    public static String appSecret = FN.getProperty("appSecret");
    // 登陆信息
    public static String appId = FN.getProperty("appId");
    public static String appPassword = FN.getProperty("appPassword");

    // 服务器IP地址
    public static String socketServer = FN.getProperty("socketServer");
    public static String socketServer_kline = "/kline.php";
    public static String socketServer_Ip = "/socketserverip.php";

    // k线缓存地址
    public static final String klineSavePath = FN.getProperty("klineSavePath");
    /**
     * 是否采集历史日K数据
     */
    public static final boolean isCollectionHistoryKline = Boolean.parseBoolean(FN.getProperty("isCollectionHistoryKline"));
    /**
     * 是否采集历史分钟线数据
     */
    public static final boolean isCollectionHistoryMinKline = Boolean.parseBoolean(FN.getProperty("isCollectionHistoryMinKline"));

    // JAVA 安装路径
    public static final String javaHome = System.getProperty("java.home");
    public static final String osName = System.getProperty("os.name");
    public static final String appPath = FN.getProjectPath();


    // 行业，概念，地域涨跌幅
    // 行业涨幅redis缓存key
    public static String redisKey_Hangye = "Hangye_UpListKey";
    // 概念涨幅redis缓存key
    public static String redisKey_Gainian = "Gainian_UpListKey";
    // 地区涨幅redis缓存key
    public static String redisKey_Diqu = "Diqu_UpListKey";
    // 行业表
    public static String redisKey_Hangye_tables = "Hangye_Tables";
    // 概念表
    public static String redisKey_Gainian_tables = "Gainian_Tables";
    // 地区表
    public static String redisKey_Diqu_tables = "Diqu_Tables";
    // 个股涨跌幅缓存key
    public static String redisKey_Stock_UpdownList = "Stock_UpdownList";

    // 保存推送过来的实时每笔动态数据
    public static JSONObject realtimeStockDatas = new JSONObject();



    public static int redisDB_market = 0;           // 实时行情
    public static int redisDB_kLineData = 1;        // k线原始数据专用
    public static int redisDB_Search = 2;           // 搜索专用
    public static int redisDB_simulationBuy = 3;    // 模拟交易买入队列
    public static int getRedisDB_simulationSell = 4;// 模拟交易卖出队列
    public static int getRedisDB_simulationAccount = 5;// 模拟交易账户队列
    public static int redisDB_kLineHoufuquan = 7;   // k线后复权专用
    public static int redisDB_kLineQianfuquan = 6;  // k线前复权专用
    public static int redisDB_upDown = 8;           // 涨跌幅
    public static int redisDB_stocks = 9;           // 保存股票数据
    public static int redisDB_marketzip = 10;       // 压缩过的实时行情数据
    public static int redisDB_cache = 11;           // 接口核心库缓存的区域
    public static int redisDB_minKlineData = 12;           // 当日分钟线数据
    public static int redisDB_otherWode = 15;       // 存储涨幅大于9%跌幅小于-%9的股票, key=sh000006 value=10(每天减1,0的时候删除)
    public static int redisDB_socketServerReceiveDB = 11;  // socket服务用来存储客户端发送过来的最后请求数据

    public static String smtp_emal = "stocksync@163.com";
    public static String smtp_pass = "dong261811252";
    public static String smtp_server = "smtp.163.com";
    public static int smtp_port = 25;
    public static String adminEmail = "dangfm@qq.com";



    // 是否断开继续连接
    public static boolean isreConnected = true;
    // 是否连接上
    public static boolean isConnected = false;

    // 请求不能为空
    public static final int error_request_noempty = 100001;
    // 请求不合法
    public static final int error_request_noauthorized = 100002;
    // 请求未包含任何合法参数
    public static final int error_request_noparams = 100003;
    // 访问的IP("+nbcIp+")不合法,请添加IP白名单
    public static final int error_request_ipauthorized = 100004;
    // 客户端合法验证失败
    public static final int error_client_noauthorized = 100005;
    // 客户端过期了，需要续费
    public static final int error_client_expired = 100006;
    // 服务器错误
    public static final int error_serviceerror = 100007;
    // 用户名密码不能为空
    public static final int error_user_empty = 100008;
    // 请求不能为空
    public static final int error_user_noauthorized = 100009;


    // 发送数据的换行符
    public static final String writeEnd = "######\r\n######";
    // 协议
    // 注册
    public static String protocol_register = "protocol_register";
    // 合法性验证协议
    public static String protocol_checkclient = "protocol_checkclient";
    // 登陆协议
    public static String protocol_checklogin = "protocol_checklogin";
    // 心跳
    public static String protocol_heart = "protocol_heart";
    // 发送实时行情的key键名数据
    public static String protocol_json_names = "protocol_json_names";
    // 发送实时行情数据
    public static String protocol_realtime = "protocol_realtime";
    // 发送当天日K数据
    public static String protocol_todayKline = "protocol_todayKline";
    // 发送当天日K数据 前复权
    public static String protocol_todayKline_before = "protocol_todayKline_before";
    // 发送当天日K数据 后复权
    public static String protocol_todayKline_after = "protocol_todayKline_after";
    // 发送当天日1分钟数据
    public static String protocol_today1MinuteKline = "protocol_today1MinuteKline";
    // 发送当天日1分钟数据 前复权
    public static String protocol_today1MinuteKline_before = "protocol_today1MinuteKline_before";
    // 发送当天日1分钟数据 后复权
    public static String protocol_today1MinuteKline_after = "protocol_today1MinuteKline_after";
    // 发送当天日5分钟数据
    public static String protocol_today5MinuteKline = "protocol_today5MinuteKline";
    // 发送当天日5分钟数据 前复权
    public static String protocol_today5MinuteKline_before = "protocol_today5MinuteKline_before";
    // 发送当天日5分钟数据 后复权
    public static String protocol_today5MinuteKline_after = "protocol_today5MinuteKline_after";
    // 发送股票分类表
    public static String protocol_allstocks = "protocol_allstocks";
    // 发送股票搜索表
    public static String protocol_searchstocks = "protocol_searchstocks";
    // 发送股票行业分类表
    public static String protocol_hangye_tables = "protocol_hangye_tables";
    // 发送股票概念分类表
    public static String protocol_gainian_tables = "protocol_gainian_tables";
    // 发送股票地区分类表
    public static String protocol_diqu_tables = "protocol_diqu_tables";
}
