package com.dangfm.stock.socketclient.user;

import com.dangfm.stock.socketclient.Config;
import com.dangfm.stock.socketclient.server.StockClientThread;
import com.dangfm.stock.socketclient.utils.FN;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xsocket.connection.INonBlockingConnection;
import org.xsocket.connection.NonBlockingConnection;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public final class UserHelper {
    public static final Logger logger = LoggerFactory.getLogger(UserHelper.class);
    /**
     * 启动注册
     */
    public static void register(String msg,INonBlockingConnection nbc){
        if (msg.startsWith("{")) {
            JSONObject subObj = null;
            try {
                subObj = new JSONObject(msg);
                String app_key = subObj.getString("app_key");
                String app_secret = subObj.getString("app_secret");
                String app_id = subObj.getString("app_id");
                String app_password = subObj.getString("app_password");

//
                FN.setProperty("appKey",app_key);
                FN.setProperty("appSecret",app_secret);
                FN.setProperty("appId",app_id);
                FN.setProperty("appPassword",app_password);

                Config.appKey=app_key;
                Config.appSecret=app_secret;
                Config.appId=app_id;
                Config.appPassword=app_password;
                sleep(1000);

                login(nbc);

            } catch (JSONException e) {
                logger.error(e.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public static void login(INonBlockingConnection nbc){
        JSONObject obj = new JSONObject();
        try {
            if (Config.appId!=null && Config.appPassword!=null) {
                obj.put("app_id", Config.appId);
                obj.put("app_password", FN.MD5(Config.appPassword));
                String v = FN.dataForWrite(Config.protocol_checklogin,obj);
//                logger.info(v);
                nbc.write(v);
            }else{
                String v = FN.dataForWrite(Config.protocol_checkclient,obj);
//                logger.info(v);
                nbc.write(v);
            }

        } catch (JSONException e) {
            logger.error(e.toString());
        } catch (ClosedChannelException e) {
            logger.error(e.toString());
        } catch (IOException e) {
            logger.error(e.toString());
        }

    }

}
