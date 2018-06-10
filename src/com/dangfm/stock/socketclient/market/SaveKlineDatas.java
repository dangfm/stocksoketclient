package com.dangfm.stock.socketclient.market;

import com.dangfm.stock.socketclient.Config;
import com.dangfm.stock.socketclient.server.StockResposeDatas;
import com.dangfm.stock.socketclient.utils.FN;
import com.dangfm.stock.socketclient.utils.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 保存K线数据
 */
public class SaveKlineDatas {
    public static final Logger logger = LoggerFactory.getLogger(StockResposeDatas.class);

    private static ReentrantLock lock = new ReentrantLock();
    // 保存的路径
    private static String klinePath = Config.klineSavePath.isEmpty()? FN.getProjectPath():Config.klineSavePath;

    /**
     * 保存当天1分钟历史K线数据
     * @param code      股票代码
     * @param value     保存的分钟线数据
     * @param fuquan    复权类型 datas before after
     */
    public static void saveMinuteKline(String code,String value,String fuquan){
        String[] list = value.split("\r\n");
//        logger.info("接收到当天1分钟历史数据:"+code+" = "+list.length);
        // 首先拿到日期
        if (list.length>0){
            String rows = list[0];
            if (!rows.isEmpty()) {
                if (rows.contains(",")) {
                    String date = rows.split(",")[0];
                    // 生产日期文件夹
                    String year = date.substring(0,4);
                    String month = date.substring(4,6);
                    String day = date.substring(6,8);
                    String path = klinePath+"/1min/"+fuquan+"/"+year+"/"+month+day+"/";
                    FileHelper.makeDirs(path);
                    String fileName = path+code.toUpperCase()+".txt";
                    // 保存文件
                    FileHelper.createFile(fileName,value);
                    logger.info("保存当天1分钟历史数据:"+code+"["+fuquan+"]"+"："+fileName);
                }
            }
        }
    }

    /**
     * 保存当天日K历史数据
     * @param code      股票代码
     * @param value     保存的k线数据
     * @param fuquan    复权类型 datas before after
     */
    public static void saveDayKline(String code,String value,String fuquan){
        String[] list = value.split("\r\n");
//        logger.info("接收到当天日K历史数据:"+code+" = "+list.length);
        // 首先拿到日期
        if (list.length>0){
            String rows = list[0];
            if (!rows.isEmpty()) {
                if (rows.contains(",")) {
                    String date = rows.split(",")[0];
                    // 生产日期文件夹
                    String year = date.substring(0,4);
                    String month = date.substring(4,6);
                    String day = date.substring(6,8);
                    String path = klinePath+"/day/"+fuquan+"/";
                    FileHelper.makeDirs(path);
                    String fileName = path+code.toUpperCase()+".txt";
                    // 先拿出旧的文件
                    String content = FileHelper.readToString(fileName);
                    if (!content.isEmpty()) {
                        // 检查是否存在当天数据
                        String[] oldList = content.split("\r\n");
                        if (oldList.length>0){
                            String lastRow = oldList[oldList.length-1];
                            if (lastRow.contains(",")){
                                String newDate = lastRow.split(",")[0];
                                // 如果拿到的最新日期比之前的都大，那么就加在最后一个下面
                                if (Integer.parseInt(date)>Integer.parseInt(newDate)){
                                    content += value;
                                    // 保存文件
                                    FileHelper.createFile(fileName,content);
//                                    logger.info("更新保存当天日K历史数据:"+code+"["+fuquan+"]"+"："+fileName);
                                    return;
                                }
                            }
                        }
                    }else {
                        // 保存文件
                        FileHelper.createFile(fileName,value);
//                        logger.info("首次保存当天日K历史数据:"+code+"["+fuquan+"]"+"："+fileName);
                        return;
                    }


                }
            }
        }

        logger.info("保存当天日K历史数据:"+code+"["+fuquan+"]"+"完成");
    }



}
