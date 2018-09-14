# 大师兄股票实时行情客户端

客户端采用JAVA语言开发，支持Linux|Windows多平台运行，主要用来分发股市Level1实时行情。大师兄实时行情工具集成了股市实时行情，五档，财务数据以及板块涨跌幅榜，个股涨跌幅榜，日K、周K、月K、1分钟、5分钟、15分钟、30分钟、60分钟K线历史数据于一体，采用Redis缓存数据库进行存储，快速便捷。数据使用也非常方便，不用复杂的计算，基本都是拿来就用，任何人都无须股市知识的情况下灵活使用。现在推出公测版，因为服务器资源有限，视情况切断试用通道，理解万岁！！！

目前只支持A股行情，实时行情延时3秒左右

 基于此客户端的PHP版API接口下载地址：https://github.com/dangfm/KLineApi
 

# 环境要求：
JAVA JDK 1.8及以上，JDK下载地址：http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

安装Reids环境 下载地址https://redis.io/

CPU 双核 4G内存及以上
 

# 配置config.properties：

#本地redis

redisServer = 127.0.0.1

redisPort = 6379

redisPassword = 123456

#路由 不可修改

socketServer = http://114.55.140.140:8111/v1/
 
#是否采集K线历史数据

isCollectionHistoryKline = false

#是否采集分钟K线历史数据

isCollectionHistoryMinKline = false

#k线本地保存地址 为空默认为程序运行目录

klineSavePath =


# 运行：

启动：

java -jar /data/stocksocketclient/stocksocketclient.jar 
 
重启： 

java -jar /data/stocksocketclient/stocksocketclient.jar 1 
 
同步所有K线历史数据： 

java -jar /data/stocksocketclient/stocksocketclient.jar 3
 

# 数据使用方式：

#运行初始化后即可在redis找到实时行情数据，具体操作步骤如下：

#获取某个股票实时行情数据：

select 0

get sh000001
 
#返回json字符串：

{"buy_1":0,"buy_2":0,"buy_3":0,"buy_4":0,"buy_5":0,"buy_1_s":530787800,"buy_2_s":0,"buy_3_s":0,"buy_4_s":0,"buy_5_s":0,"code":"sh000001","closePrice":3154.65,"cityNetRate":0,"circulationValue":0,"highPrice":3156.73,"isStop":0,"lastDate":"2018-05-25","lastTime":"15:01:03","lowPrice":3131.07,"name":"\u4e0a\u8bc1\u6307\u6570","openPrice":3148.41,"price":3141.3,"peRatio":0,"swing":0,"sell_1":0,"sell_2":0,"sell_3":0,"sell_4":0,"sell_5":0,"sell_1_s":621543700,"sell_2_s":0,"sell_3_s":0,"sell_4_s":0,"sell_5_s":0,"type":1,"totalValue":0,"turnoverRate":0,"volumn":12861084000,"volumnPrice":166554042368}
 
#获取股票搜索库

select 2

keys *

搜索某个关键词股票

keys *xxx*
 
 
#板块涨幅榜

select 8

#行业涨幅

get Hangye_UpListKey

#概念涨幅

get Gainian_UpListKey

#地域涨幅

get Diqu_UpListKey

#个股涨幅榜

get Stock_UpdownList

#换手率榜

get Stock_UpdownList_turnoverRate

#振幅榜

get Stock_UpdownList_swing

#总市值排行榜

get Stock_UpdownList_totalValue

#流通市值排行

get Stock_UpdownList_circulationValue

#除权数据

hgetall ExRight_Tables

hget ExRight_Tables 600000


#财务数据

hgetall Financial_Tables

hget Financial_Tables 600000

 

# 下载地址

官方更新地址：

https://github.com/dangfm/stocksoketclient/

QQ交流群：大师兄股票实时行情

群   号：789599606



仅供学习研究使用，请勿用于商业用途！
