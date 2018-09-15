package com.dangfm.stock.socketclient;
import com.dangfm.stock.socketclient.market.CreateTimeLine;
import com.dangfm.stock.socketclient.market.GetStockDaysLine;
import com.dangfm.stock.socketclient.market.MakeMarketUpDownList;
import com.dangfm.stock.socketclient.server.StockClientThread;
import com.dangfm.stock.socketclient.utils.FN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.Thread.sleep;

public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);
    /**
     * 定时任务运行池
     */
    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1000);

    public static void main(String[] args) {

        // 首先拿到societ服务器IP
        FN.getServerIp();

        if(!FN.reStart()){
            // windows 会检查端口是否占用，占用的就禁止启动了
            logger.info("****************大师兄行情客户端已经启动！！！！！******************");
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }else {



            int args0 = 0;
            if (args.length > 0) {
                String n = args[0];
                args0 = Integer.parseInt(n);
            }
//            logger.info("JAVA HOME=" + Config.javaHome + "");
//            logger.info("OS Name=" + Config.osName + "");
//            logger.info("Me Path=" + Config.appPath + "");
//            logger.info("当前最大可用内存:" + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " M"); //最大可用内存，对应-Xmx

            // 初始化
            String autoStartTime = "0";

            FN.createExecutor("7:00:00", 0, new StockClientThread(), true,executor);
            /**
             * 行情涨跌幅
             */
            FN.createExecutor("9:25:00", 0, new MakeMarketUpDownList(), args0 == 1 ? true : false,executor);
            /**
             * 生成分钟线分时线
             */
            FN.createExecutor("9:00:00", 0, new CreateTimeLine(), args0 == 1 ? true : false,executor);
            /**
             * 同步K线数据 参数2是全量更新K线，周六日默认都是全量更新的
             */
            FN.createExecutor("20:00:00", 0, new GetStockDaysLine(args0==2), (args0 == 3 || args0==2) ? true : false,executor);
            FN.createExecutor("7:00:00", 0, new GetStockDaysLine(false), false,executor);

            // 生成历史k线
//        createExecutor("15:10:00", 0, new CreateMinuteKline(), args0 == 2 ? true : false);
        }

        // 每天早上六点自动退出 linux定时任务会在凌晨6点30分开始启动
        FN.createExecutor("6:0:0", 0, new Thread() {
            @Override
            public void run() {
                System.out.println("程序自动退出");
                System.exit(0);
            }
        }, false,executor);

    }




}
