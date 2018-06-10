package com.dangfm.stock.socketclient.server;

import com.dangfm.stock.socketclient.Config;
import com.dangfm.stock.socketclient.user.UserHelper;
import com.dangfm.stock.socketclient.utils.FN;
import com.dangfm.stock.socketclient.utils.HttpWebCollecter;
import com.sun.corba.se.impl.oa.poa.POAImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xsocket.connection.INonBlockingConnection;
import org.xsocket.connection.NonBlockingConnection;

import java.io.IOException;
import java.net.UnknownHostException;
/**
 * Socket服务器主线程
 * Created by dangfm on 17/2/2.
 */
public class StockClientThread extends Thread{
    private String PORT= Config.serverPort;
    private String SERVER= Config.serverIP;
    public static final Logger logger = LoggerFactory.getLogger(StockClientThread.class);

    public void run() {

        INonBlockingConnection nbc = startNbc();

        while (true){

            if (nbc==null){
//                logger.info("...");
                nbc = startNbc();
            }else if (!nbc.isOpen()){
//                logger.info(".");
                nbc = startNbc();
            }else{
//                try {
//                    if (nbc.isOpen()) {
////                        nbc.write(valueForClient("check"));
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
            try {
                if (!Config.isreConnected) {
                    // 由于验证失败等断开链接，需要更长时间才能再次链接
                    sleep(60*1000);
                }else {
                    sleep(10000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }




    private INonBlockingConnection startNbc(){
        if (SERVER.isEmpty() || SERVER==null) {
            return null;
        }

        try {

            //建立handler
            //采用非阻塞式的连接
            INonBlockingConnection nbc = new NonBlockingConnection(SERVER, Integer.parseInt(PORT), new StockClientHandle());

            //采用阻塞式的连接
            //IBlockingConnection bc = new BlockingConnection("localhost", PORT);
            //一个非阻塞的连接是很容易就变成一个阻塞连接
//            IBlockingConnection bc = new BlockingConnection(nbc);
            //设置编码格式
            nbc.setEncoding("UTF-8");
//            nbc.setIdleTimeoutMillis(30000);
//            nbc.setConnectionTimeoutMillis(10000);
            //设置是否自动清空缓存
//            nbc.setAutoflush(true);
//            nbc.setMaxReadBufferThreshold(2000000000);


            // 登录
//            nbc.write("login:"+token+":"+t);

//            JSONObject obj = new JSONObject();
//            obj.put("app_id",Config.appId);
//            obj.put("app_password",FN.MD5(Config.appPassword));
//            nbc.write(FN.dataForWrite(Config.protocol_checklogin,obj));
            UserHelper.login(nbc);
//            //向客户端读取数据的信息
//            byte[] byteBuffers= bc.readBytesByDelimiter("|", "UTF-8");
//            //打印服务器端信息
//            logger.info(new String(byteBuffers));
            //将信息清除缓存，写入服务器端
//            bc.flush();
//            bc.close();
            return nbc;
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
//            logger.error(e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
//            logger.error(e.getMessage());
        }

        return null;


    }




}
