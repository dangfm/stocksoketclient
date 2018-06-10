package com.dangfm.stock.socketclient.server;

import com.dangfm.stock.socketclient.Config;
import com.dangfm.stock.socketclient.utils.FN;
import com.dangfm.stock.socketclient.utils.ZipUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.connection.*;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * socket服务器事件处理类
 * Created by dangfm on 17/2/2.
 */
public class StockClientHandle implements IDataHandler, IConnectHandler,
        IDisconnectHandler,IConnectionTimeoutHandler {

    public static final Logger logger = LoggerFactory.getLogger(StockClientHandle.class);

    private static ReentrantLock lock = new ReentrantLock();

    /**
     * 连接的成功时的操作
     */
    @Override
    public boolean onConnect(INonBlockingConnection nbc) throws IOException,
            BufferUnderflowException, MaxReadSizeExceededException {
        String  remoteName=nbc.getRemoteAddress().getHostName();
        logger.info("客户端启动成功连接行情服务器:" + Config.serverIP+":"+ Config.serverPort);
//        nbc.setEncoding("utf-8");
        Config.isConnected = true;
        return true;
    }
    /**
     * 连接断开时的操作
     */
    @Override
    public boolean onDisconnect(INonBlockingConnection nbc) throws IOException {
        // TODO Auto-generated method stub
        String  remoteName=nbc.getRemoteAddress().getHostName();
        logger.info("客户端断开行情服务器:" + Config.serverIP+":"+ Config.serverPort);
        Config.isConnected = false;
        return true;
    }

    @Override
    public boolean onConnectionTimeout(INonBlockingConnection iNonBlockingConnection) throws IOException {
        String  remoteName=iNonBlockingConnection.getRemoteAddress().getHostName();
        logger.info("客户端连接行情服务器超时:" + Config.serverIP+":"+ Config.serverPort);
        Config.isConnected = false;
        return false;
    }

    /**
     *
     * 接收到数据库时候的处理
     */
    @Override
    public boolean onData(INonBlockingConnection nbc) throws IOException,
            BufferUnderflowException, ClosedChannelException,
            MaxReadSizeExceededException {

//        byte[] byteBuffers= nbc.readBytesByDelimiter(Config.writeEnd);
        String str = nbc.readStringByDelimiter(Config.writeEnd);
//        str = new String(str.getBytes("ISO-8859-1","utf-8"));
        /**
         * 解析数据
         */
        StockResposeDatas.StockResposeDatas(str,nbc);
        str = null;
//        byteBuffers = null;
        nbc.flush();
        System.gc();
        return true;
    }



}
