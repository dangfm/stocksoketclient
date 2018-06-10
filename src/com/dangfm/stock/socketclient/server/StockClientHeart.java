package com.dangfm.stock.socketclient.server;

import com.dangfm.stock.socketclient.Config;
import com.dangfm.stock.socketclient.utils.FN;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xsocket.connection.INonBlockingConnection;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class StockClientHeart extends Thread {
    public static final Logger logger = LoggerFactory.getLogger(StockClientHeart.class);
    private static int loopTime = 30000; // 10秒一次

    private static INonBlockingConnection nbc;

    private boolean isStart = true;

    /**
     * 定时任务运行池
     */
    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);


    public static void StockClientHeart(INonBlockingConnection nbcs){
        nbc = nbcs;
//        logger.info("******启动心跳******");
        FN.createExecutor("0", loopTime, heart(), true,executor);
    }


    private static Runnable heart(){
        return new Runnable(){
            @Override
            public void run() {
                if(nbc.isOpen()){
                    try {
                        nbc.write(FN.dataForWrite(Config.protocol_heart,new JSONObject()));
//                        logger.info("******心跳******");
                    } catch (IOException e) {
                        logger.info(e.toString());
                    }
                }
            }
        };
    }
}
