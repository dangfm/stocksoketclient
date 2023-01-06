# 大师兄股票实时行情客户端

客户端采用JAVA语言开发，支持Linux|Windows多平台运行，主要用来分发股市Level1实时行情。大师兄实时行情工具集成了股市实时行情，五档，财务数据以及板块涨跌幅榜，个股涨跌幅榜，日K、周K、月K、1分钟、5分钟、15分钟、30分钟、60分钟K线历史数据于一体，采用Redis缓存数据库进行存储，快速便捷。数据使用也非常方便，不用复杂的计算，基本都是拿来就用，任何人都无须股市知识的情况下灵活使用。现在推出公测版，因为服务器资源有限，视情况切断试用通道，理解万岁！！！

目前只支持A股行情，实时行情延时3秒左右

<img src="http://www.gjl.info/images/225137_503db3cf_450618.png" width=320>

## 环境要求
1、JAVA JDK 1.8及以上，JDK下载地址：

http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

2、安装Reids环境

下载地址https://redis.io/

3、硬件要求：

CPU 四核 8G内存及以上

## 配置文件
配置文件路径：

项目根目录/config.properties

配置文件内容：

      #本地redis

     redisServer = 127.0.0.1

     redisPort = 6379

     redisPassword = 123456

     #路由 不可修改

     socketServer = http://127.0.0.1:8111/v1/

     #是否采集K线历史数据

     isCollectionHistoryKline = false

     #是否采集分钟K线历史数据

     isCollectionHistoryMinKline = false

     #k线本地保存地址 为空默认为程序运行目录

     klineSavePath =

     #注：第一次运行会自动注册并保存以下信息，如果IDE运行会每次被覆盖掉，请用命令行运行，运行后保存好config文件

     appId=1234

     appKey=daf57274235c4792ba0929ca12345678

     appSecret=356d73a37a2344cd91d369d8b12345678

     appPassword=12345678


## 运行命令
启动：

     java -jar /data/stocksocketclient/stocksocketclient.jar

重启：

     java -jar /data/stocksocketclient/stocksocketclient.jar 1

同步所有K线历史数据：

     java -jar /data/stocksocketclient/stocksocketclient.jar 3


## 数据结构

运行初始化后即可在redis找到实时行情数据，具体操作步骤如下：

### 1、获取某个股票实时行情数据：

     select 0

     get sh000001

     #返回json字符串：


     {
         "buy_1":0,
         "buy_2":0,
         "buy_3":0,
         "buy_4":0,
         "buy_5":0,
         "buy_1_s":530787800,
         "buy_2_s":0,
         "buy_3_s":0,
         "buy_4_s":0,
         "buy_5_s":0,
         "code":"sh000001",
         "closePrice":3154.65,
         "cityNetRate":0,
         "circulationValue":0,
         "highPrice":3156.73,
         "isStop":0,
         "lastDate":"2018-05-25",
         "lastTime":"15:01:03",
         "lowPrice":3131.07,
         "name":"上证指数",
         "openPrice":3148.41,
         "price":3141.3,
         "peRatio":0,
         "swing":0,
         "sell_1":0,
         "sell_2":0,
         "sell_3":0,
         "sell_4":0,
         "sell_5":0,
         "sell_1_s":621543700,
         "sell_2_s":0,
         "sell_3_s":0,
         "sell_4_s":0,
         "sell_5_s":0,
         "type":1,
         "totalValue":0,
         "turnoverRate":0,
         "volumn":12861084000,
         "volumnPrice":166554042368
     }

     #字段说明： 
     name : 股票名称
     code : 股票代码
     type : 股票类型 0=普通A股 1=指数
     openPrice : 开盘价格
     closePrice : 昨日收盘价
     price : 当前价格
     highPrice : 最高价
     lowPrice : 最低价
     volumn : 成交量（股）
     volumnPrice : 成交额
     totalValue ：总市值
     circulationValue ：流通市值
     cityNetRate ：市净率
     peRatio ：市盈率
     swing ： 振幅
     turnoverRate ：换手率
     buy_1 : 买一价
     buy_2 : 买二价
     buy_3 : 买三价
     buy_4 : 买四价
     buy_5 : 买五价
     buy_1_s : 买一委托量（股）
     buy_2_s : 买二委托量
     buy_3_s : 买三委托量
     buy_4_s : 买四委托量
     buy_5_s : 买五委托量
     sell_1 : 卖一价
     sell_2 : 卖二价
     sell_3 : 卖三价
     sell_4 : 卖四价
     sell_5 : 卖五价
     sell_1_s : 卖一委托量
     sell_2_s : 卖二委托量
     sell_3_s : 卖三委托量
     sell_4_s : 卖四委托量
     sell_5_s : 卖五委托量
     lastDate : 最后更新日期
     lastTime : 最后更新时间


### 2、获取股票搜索库

     select 2

     keys *

     搜索某个关键词股票

     keys xxx

     返回结果：

     #示例
     #股票代码|拼音简称|股票名称|类型（0：个股，1：指数）
     sh000108|380xf|380消费|1
     sh600019|bggf|宝钢股份|0
     sh000010|sz180|上证180|1
     sh000122|nyzt|农业主题|1
     sh000145|yszy|优势资源|1
     sh000101|5nxy|5年信用|1
     sz300016|blyy|北陆药业|0
     sz000013|*stsha|*ST石化A|0


### 3、板块涨幅榜


     select 8

     3.1、行业涨幅

     get Hangye_UpListKey

     返回数据：


     [
       {
         "code": "sz002617",
         "rate": 2.869056714423142,
         "change": 4.46,
         "name": "露笑科技",
         "id": "10000024",
         "title": "有色金属",
         "changeRate": 10.123456790123461
       },
       {
         "code": "sh600983",
         "rate": 2.0594379730205024,
         "change": 5.82,
         "name": "惠而浦",
         "id": "10000012",
         "title": "家电行业",
         "changeRate": 2.826855123674914
       },

     ]

     #字段说明：
     id : 行业编号
     title : 行业名称
     rate ：行业涨幅
     code ：股票代码
     name ：股票名称
     change ：股票涨跌额
     changeRate ：股票涨跌幅百分比

#### 3.2、概念涨幅

     get Gainian_UpListKey

     返回数据：


     [
       {
         "code": "sz002237",
         "rate": 4.225965075707311,
         "change": 17.21,
         "name": "恒邦股份",
         "id": "1111111",
         "title": "黄金概念",
         "changeRate": 6.894409937888195
       },
       {
         "code": "sz002554",
         "rate": 2.6858786703475914,
         "change": 3.66,
         "name": "惠博普",
         "id": "11111165",
         "title": "可燃冰",
         "changeRate": 5.475504322766569
       },

     ]

     #字段说明：
     id : 概念编号
     title : 概念名称
     rate ：概念涨幅
     code ：股票代码
     name ：股票名称
     change ：股票涨跌额
     changeRate ：股票涨跌幅百分比

#### 3.3、地域涨幅

     get Diqu_UpListKey

     返回数据：


     [
       {
         "code": "sz000975",
         "rate": 1.8884066689198225,
         "change": 13.42,
         "name": "银泰资源",
         "id": "2222222",
         "title": "内蒙古自治区",
         "changeRate": 6.847133757961779
       },
       {
         "code": "sh600759",
         "rate": 1.400824359077736,
         "change": 3.65,
         "name": "洲际油气",
         "id": "2222228",
         "title": "海南省",
         "changeRate": 7.988165680473373
       },
       {
         "code": "sz002237",
         "rate": 1.3713239408760203,
         "change": 17.27,
         "name": "恒邦股份",
         "id": "2222224",
         "title": "山东省",
         "changeRate": 7.2670807453416035
       },

     ]

     #字段说明：
     id : 地域编号
     title : 地域名称
     rate ：地域涨幅
     code ：股票代码
     name ：股票名称
     change ：股票涨跌额
     changeRate ：股票涨跌幅百分比
     
#### 3.4、个股涨幅榜

     get Stock_UpdownList

     返回数据：

     {
       "sz002617": 10.123456790123461,
       "sz300019": 10.069930069930066,
       "sz300697": 10.027855153203353,
       "sz300206": 10.02747252747252,
       "sh601698": 10.023866348448685,
       "sz300788": 10.01405481377371,
       "sh601236": 9.954751131221721,
       "sz300162": 7.812499999999996,
       "sh601808": 7.104984093319193,
       "sz002177": 6.434782608695653,
     }

     #字段说明
     股票代码 ：涨跌百分比
     
     
#### 3.5、换手率榜

     get Stock_UpdownList_turnoverRate

     返回数据：

     {
       "sh603867": 33.23,
       "sz300554": 30.26,
       "sz300717": 12.98,
       "sh603738": 12.64,
       "sz300697": 12.42,
       "sz002865": 12.02,
       "sh603283": 11.57,
     }
     #股票代码：换手率百分比
     
     
#### 3.6、振幅榜

     get Stock_UpdownList_swing

     返回数据：

     {
       "sh600078": 12.18,
       "sz300700": 11.84,
       "sz300385": 11.78,
       "sh603937": 11.06,
       "sh600592": 10.83,
     }
     #股票代码：振幅百分比


#### 3.7、总市值排行榜

     get Stock_UpdownList_totalValue

     返回数据:

     {
       "sh601398": 14990.44,
       "sh600519": 12342.39,
       "sh601857": 10832.59,
       "sh601288": 10556.59,
       "sh601318": 9574.99,
     }
     #股票代码：市值（单位：亿）


#### 3.8、流通市值排行

     get Stock_UpdownList_circulationValue

     返回数据:

     {
       "sh601398": 14990.44,
       "sh600519": 12342.39,
       "sh601857": 10832.59,
       "sh601288": 10556.59,
       "sh601318": 9574.99,
     }
     #股票代码：市值（单位：亿）


### 4、除权数据

     hgetall ExRight_Tables

     hget ExRight_Tables 600000

     返回数据：

     [
         {
             "code":"600000",
             "datetime":"2000-07-06",
             "give":0,
             "pei":0,
             "peiPrice":0,
             "profile":0.15000000596046448
         },
         {
             "code":"600000",
             "datetime":"2002-08-22",
             "give":0.5,
             "pei":0,
             "peiPrice":0,
             "profile":0.20000000298023224
         }
     ]


     #字段含义：
     code:股票代码
     datetime：日期
     give：每股送
     pei：每股配
     peiPrice:配股价
     profile：每股红利

### 5、财务数据

     hgetall Financial_Tables

     hget Financial_Tables 600000

     返回数据：

     {
         "code":"600000",
         "datetime":"20180830",
         "ZGB":"2935208.00",
         "GJG":"180199.00",
         "LTAG":"2810376.50",
         "ZZC":"6091758592.0",
         "LDZC":"0.0",
         "GDZC":"31543000.0",
         "JZC":"440855008.0",
         "ZYSY":"82256000.0",
         "ZYLY":"0.0",
         "YYLY":"34164000.0",
         "SHLY":"28900000.0",
         "JLY":"28569000.0",
         "DY":" 16",
         "HY":" 1",
         "ZBNB":"6",
         "SSDATE":"19991110"
     }

     #字段说明：
     code：股票代码
     datetime：日期
     ZGB:总股本
     GJG:国家股
     LTAG:流通A股
     ZZC:总资产
     LDZC：流动资产
     GDZC：固定资产
     JZC：净资产
     ZYSY：营业收入
     ZYLY：主营利润
     YYLY：营业利润
     SHLY：税后利润
     JLY：净利润
     DY：地域
     HY：行业
     ZBNB：资料月份 9 代表三季报 12代表年报
     SSDATE：上市日期




## 使用文档：

http://www.gjl.info/quotes/
 

## 下载地址

官方更新地址：

https://github.com/dangfm/stocksoketclient/

接口DEMO：https://github.com/dangfm/KLineApi

移动端DEMO：https://app.download.hemyun.com/cloudstrategy/

QQ交流群：大师兄股票实时行情

群   号：789599606



仅供学习研究使用，请勿用于商业用途！
