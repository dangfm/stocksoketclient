package com.dangfm.stock.socketclient.utils;

import com.dangfm.stock.socketclient.server.StockResposeDatas;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.dangfm.stock.socketclient.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

/**
 * 通用工具
 * Created by dangfm on 16/4/2.
 */
public class FN {
    public static final Logger logger = LoggerFactory.getLogger(FN.class);
    public static ImageIcon getImageIcon(String path, int width, int height) {
        if (width == 0 || height == 0) {
            return new ImageIcon(path);
        }
        ImageIcon icon = new ImageIcon(path);
        icon.setImage(icon.getImage().getScaledInstance(width, height,
                Image.SCALE_DEFAULT));
        return icon;
    }

    public final static String MD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 到指定时间是否停止
     * @param taskTime 停止时间 Linux定时任务写法 如 "0 1" 表示1点0分
     * @return
     */
    public static boolean isStopTime(String taskTime){
        boolean stop = false;
        String[] a = taskTime.split(" ");
        int m = Integer.parseInt(a[0]);
        int h = 0;
        if (a.length>0)
            h = Integer.parseInt(a[1]);
        int nowH = Integer.parseInt(new SimpleDateFormat("HH").format(System.currentTimeMillis()));
        int nowM = Integer.parseInt(new SimpleDateFormat("mm").format(System.currentTimeMillis()));
        if (nowH==h && nowM>m){
            stop = true;
        }
        if (nowH>h){
            stop = true;
        }
        return stop;
    }

    /**
     * 获取指定时间对应的毫秒数
     * @param time "HH:mm:ss"
     * @return
     */
    public static long getTimeMillis(String time) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
            Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
            return curDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Date strToDate(String time,String format) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(format);
            Date curDate = dateFormat.parse(time);
            return curDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取日期字符串表示
     * @return "MMdd"
     */
    public static String getDateStr() {
        DateFormat dayFormat = new SimpleDateFormat("MMdd");
        String d = dayFormat.format(new Date());
        return d;
    }

    /**
     * 获取日期字符串表示
     * @param format 格式化规则 如:yy-MM-dd
     * @return 日期字符串
     */
    public static String getDateWithFormat(String format,Date date) {
        if (date==null) date = new Date();
        DateFormat dayFormat = new SimpleDateFormat(format);
        String d = dayFormat.format(date);
        return d;
    }
    /**
     * 获取当前日期是星期几<br>
     *
     * @param dt
     * @return 当前日期是星期几
     */
    public static String getWeekOfDate(Date dt) {
        String[] weekDays = {"7", "1", "2", "3", "4", "5", "6"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);

        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;

        return weekDays[w];
    }

    /**
     * 字符串编码转换的实现方法
     * @param str  待转换编码的字符串
     * @param newCharset 目标编码
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String changeCharset(String str, String newCharset) {
        if (str != null) {
            //用默认字符编码解码字符串。
            byte[] bs = str.getBytes();
            //用新的字符编码生成字符串
            try {
                return new String(bs, newCharset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    /**
     * 字符串编码转换的实现方法
     * @param str  待转换编码的字符串
     * @param oldCharset 原编码
     * @param newCharset 目标编码
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String changeCharset(String str, String oldCharset, String newCharset){
        if (str != null) {
            //用旧的字符编码解码字符串。解码可能会出现异常。
            byte[] bs = new byte[0];
            try {
                bs = str.getBytes(oldCharset);
                //用新的字符编码生成字符串
                return new String(bs, newCharset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        return null;
    }


    /**
     * 数据库结果集转成json字符串
     * @param rs
     * @return
     */
    public static String resultSetToJson(ResultSet rs)
    {
        // json数组
        JSONArray array = new JSONArray();
        // 获取列数
        ResultSetMetaData metaData = null;
        try {
            metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            // 遍历ResultSet中的每条数据
            while (rs.next()) {
                JSONObject jsonObj = new JSONObject();

                // 遍历每一列
                for (int i = 1; i <= columnCount; i++) {
                    String columnName =metaData.getColumnLabel(i);
                    String value = rs.getString(columnName);
                    jsonObj.put(columnName, value);
                }
                array.put(jsonObj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return array.toString();
    }

    public static String pinYin(String s) {
        if (s==null) return "";
        if (s.length()<=0) return s;

        char [] hanzhi=new char[s.length()];
        for(int i=0;i<s.length();i++){
            hanzhi[i]=s.charAt(i);
        }

        char [] t1 =hanzhi;
        String[] t2 = new String[s.length()];
        /**
         * 设置输出格式
         */
        net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
        t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        t3.setVCharType(HanyuPinyinVCharType.WITH_V);

        int t0=t1.length;
        String py = "";
        try {
            for (int i=0;i<t0;i++){
                t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);
                if (t2!=null)
                    py=py+t2[0].charAt(0);//获取首字母
                else
                    py=py+t1[i];
            }
        }
        catch (BadHanyuPinyinOutputFormatCombination e1) {
            e1.printStackTrace();
        }

        return py.trim().toLowerCase();
    }

    // 压缩
    public static String zip(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes());
        gzip.close();
        return out.toString("ISO-8859-1");
    }

    // 解压缩
    public static String unzip(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
        GZIPInputStream gunzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n;
        while ((n = gunzip.read(buffer))>= 0) {
            out.write(buffer, 0, n);
        }
        // toString()使用平台默认编码，也可以显式的指定如toString(&quot;GBK&quot;)
        return out.toString();
    }

    public static String getProperty(String key){

        Properties p = new Properties();
        try {
//            System.out.println(FN.getProjectPath()+"/"+Config.configFileName);
            InputStream inputStream = new FileInputStream(new File(FN.getProjectPath()+"/"+Config.configFileName));
//            InputStream inputStream = new FN().getClass().getClassLoader().getResourceAsStream(Config.configFileName);

            p.load(inputStream);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
//        System.out.println(p.getProperty(key));
        String value = p.getProperty(key);
        if (value==null) value = "";
        return value;

    }

    public static void setProperty(String key,String value){

        Properties p = new Properties();
        try {
//            System.out.println(FN.getProjectPath()+"/"+Config.configFileName);
            String fileName = FN.getProjectPath()+"/"+Config.configFileName;
            InputStream inputStream = new FileInputStream(new File(fileName));
//            InputStream inputStream = new FN().getClass().getClassLoader().getResourceAsStream(Config.configFileName);

            p.load(inputStream);
            p.setProperty(key,value);
            OutputStream outputFile = new FileOutputStream(fileName);
            p.store(outputFile, "");
            outputFile.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
//        System.out.println(p.getProperty(key));




    }

    public static void sendEmail(String toEmail,String subject,String content){

        try {
            System.out.println("配置邮件");
            sendMail(Config.smtp_emal,Config.smtp_emal,Config.smtp_pass,toEmail,subject,content);
//            SimpleEmail email = new SimpleEmail();
//            email.setHostName(Config.smtp_server);//邮件服务器
////            email.setSmtpPort(Config.smtp_port);
//            email.setAuthentication(Config.smtp_emal, Config.smtp_pass);//smtp认证的用户名和密码
//            System.out.println("发送邮件");
//            email.addTo(toEmail, "admin");//收信者
//            email.setFrom(Config.smtp_emal, Config.configFileName);//发信者
//            email.setSubject(subject);//标题
//            email.setCharset("UTF-8");//编码格式
//            email.setMsg(content);//内容
//            System.out.println("发送...");
//            email.send();//发送

            System.out.println("发送邮件成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }

    }

    public static void sendMail(String fromMail, String user, String password,
                                String toMail,
                                String mailTitle,
                                String mailContent) throws Exception {
        toMail = fromMail;
        Properties props = new Properties(); //可以加载一个配置文件
        // 使用smtp：简单邮件传输协议
        props.put("mail.smtp.host", "smtp.163.com");//存储发送邮件服务器的信息
        props.put("mail.smtp.auth", "true");//同时通过验证

        Session session = Session.getInstance(props);//根据属性新建一个邮件会话
        session.setDebug(true); //有他会打印一些调试信息。

        MimeMessage message = new MimeMessage(session);//由邮件会话新建一个消息对象
        message.setFrom(new InternetAddress(fromMail));//设置发件人的地址
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toMail));//设置收件人,并设置其接收类型为TO
        message.setSubject(Config.configFileName+mailTitle);//设置标题
        //设置信件内容
        message.setText(mailContent); //发送 纯文本 邮件 todo
//        message.setContent(mailContent, "text/html;charset=gbk"); //发送HTML邮件，内容样式比较丰富
        message.setSentDate(new Date());//设置发信时间
        message.saveChanges();//存储邮件信息

        //发送邮件
        Transport transport = session.getTransport("smtp");
//        Transport transport = session.getTransport();
        transport.connect(user, password);
        transport.sendMessage(message, message.getAllRecipients());//发送邮件,其中第二个参数是所有已设好的收件人地址
        transport.close();
    }

    public static String getProjectPath() {

        java.net.URL url = FN.class .getProtectionDomain().getCodeSource().getLocation();

        String filePath = null ;

        try {

            filePath = java.net.URLDecoder.decode (url.getPath(), "utf-8");

        } catch (Exception e) {

            e.printStackTrace();

        }

        if (filePath.endsWith(".jar"))
            filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
//        System.out.println(filePath);
        File file = new File(filePath);

        filePath = file.getAbsolutePath();

        return filePath;

    }

    /**
     * 计算两个日期之间相差的天数
     * @param smdate 较小的时间
     * @param bdate  较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    public static int daysBetween(Date smdate,Date bdate) throws ParseException
    {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        smdate=sdf.parse(sdf.format(smdate));
        bdate=sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days=(time2-time1)/(1000*3600*24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     *字符串的日期格式的计算
     */
    public static int daysBetween(String smdate,String bdate,String format) throws ParseException{
        if (format.isEmpty()) format = "yyyy-MM-dd";
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(smdate));
        long time1 = cal.getTimeInMillis();
        cal.setTime(sdf.parse(bdate));
        long time2 = cal.getTimeInMillis();
        long between_days=(time2-time1)/(1000*3600*24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("-?[0-9]+.?[0-9]+");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }

    public static JSONObject stoObj(String value,JSONArray names){
        String[] v = value.split(",");
        JSONObject obj = new JSONObject();
        String[] ns = sortWithKeys(names.toString());
        for (int i=0;i<v.length;i++){
            String f = v[i];
            try {
                if (i<ns.length) {
                    String key = ns[i];
                    obj.put(key, f);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    public static final String getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
//        System.out.println(runtimeMXBean.getName());
        return runtimeMXBean.getName().split("@")[0];
    }
    // 重启杀掉同伴
    public static boolean reStart(){
        if (System.getProperty("os.name").contains("Windows")){
            Runtime runtime = Runtime.getRuntime();
            try {
                String cmdStr = "netstat -aon|findstr \"" +Config.serverIP+":"+Config.serverPort+"\"";
                String[] cmd = new String[] { "cmd.exe", "/C", cmdStr};
//                System.out.println(cmd);
                BufferedReader br = new BufferedReader(new InputStreamReader(runtime.exec(cmd).getInputStream()));
                //StringBuffer b = new StringBuffer();
                String line=null;
                StringBuffer b=new StringBuffer();
                int i=0;
                while ((line=br.readLine())!=null) {
                    if (line.indexOf(Config.serverIP+":"+Config.serverPort)>0){
                        System.out.println(line+"="+Config.serverIP+":"+Config.serverPort);
                        b.append(line + "\n");
                        i++;
                    }
                }

                if (i>0){
//                    System.out.println("同伴进程：\n"+b.toString());
                    // 同伴在运行，无法启动
                    return false;
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }else {
            Process process = null;
            List<String> processList = new ArrayList<String>();
            try {
                process = Runtime.getRuntime().exec("ps -aux");
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = "";
                while ((line = input.readLine()) != null) {
                    processList.add(line);
                }
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (String line : processList) {
                if (line.contains(FN.getProjectPath())) {
                    // 杀掉之前的同伴
                    line = line.replace("root", "");
                    line = line.trim();
                    line = line.replace(" ", ",");
//                    System.out.println(line);
                    String[] s = line.split(",");

                    String id = s[0];
//                    System.out.println("同伴进程ID：" + id + " 我的ID：" + FN.getProcessID());
                    try {
                        if (!FN.getProcessID().equals(id))
                            process = Runtime.getRuntime().exec("kill " + id);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        return true;
    }

    /**
     * 生成定时任务
     * @param startTime	 HH:mm:ss
     * @param periode   下次运行时间毫秒数  24 * 60 * 60 * 1000 表示一天运行一次
     * @param task
     */
    public static void createExecutor(String startTime,long periode,Runnable task,boolean isFollowMainStart,ScheduledExecutorService executor){
        // 表明启动就开始运行
        if (startTime.equals("0")){
            // 一分钟后运行
            startTime = new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()+6*1000);
        }
        long oneDay = 24 * 60 * 60 * 1000;
        if (periode>0) oneDay = periode;
        long initDelay  = FN.getTimeMillis(startTime) - System.currentTimeMillis();

        if (isFollowMainStart){
            initDelay = 1000;

        }else{
            initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;
        }

        executor.scheduleAtFixedRate(
                task,
                initDelay,
                oneDay,
                TimeUnit.MILLISECONDS);
    }


    /**
     * 字符串数组排序
     * @param keys key1,key2,key3....
     * @return
     */
    public static String[] sortWithKeys(String keys){
        keys = keys.replace("\"","");
        keys = keys.replace("[","");
        keys = keys.replace("]","");
        String []str = keys.split(",");
        arraySort(str);
//        System.out.println(stringWithArray(str));
        return str;
    }

    /**
     * 数组转成字符串
     * @param array String[]
     * @return
     */
    public static String stringWithArray(String[] array){
        String value = "";
        for (int i=0;i<array.length;i++){
            if (value.isEmpty())
                value = array[i];
            else
                value += ","+array[i];
        }
        return value;
    }

    public static void arraySort(String[] array){
        // 从此一个位开始循环数组
        for (int i = 0; i < array.length; i++) {
            // 从第i+1为开始循环数组
            for (int j = i + 1; j < array.length; j++) {
                String stri = array[i].substring(0,1);
                String strj = array[j].substring(0,1);
                int leni = array[i].length();
                int lenj = array[j].length();
                if (stri.compareTo(strj)>0) {
                    String tem = array[i];
                    array[i] = array[j];
                    array[j] = tem;
                }else if(stri.compareTo(strj)==0){
                    // 如果相等就比较长度
                    if (leni>lenj){
                        String tem = array[i];
                        array[i] = array[j];
                        array[j] = tem;
                    }else if(leni==lenj){
//                        System.out.println("长度相等："+array[i]+"=="+array[j]);
                        // 如果相同就按照查找
                        char[] ci = array[i].toCharArray();
                        char[] cj = array[j].toCharArray();
                        for (int k=0;k<ci.length;k++){

                            if (ci[k]>cj[k]){
//                                System.out.println("比较字母："+ci[k]+">"+cj[k]);
                                String tem = array[i];
                                array[i] = array[j];
                                array[j] = tem;
                                break;
                            }else if(ci[k]<cj[k]){
                                break;
                            }
                        }

                    }
                }
            }
        }
    }

    // 包装发送的数据
    public static String dataForWrite(String protocol,JSONObject data){
        String v = "";
        JSONObject obj = new JSONObject();
        long t = System.currentTimeMillis();
        String dataStr = data.toString();
        String token = FN.MD5(dataStr+t+Config.appKey+Config.appSecret);
        try {
            dataStr = URLEncoder.encode(dataStr,"UTF-8");
            obj.put("protocol",protocol);
            obj.put("data",dataStr);
            obj.put("app_key",Config.appKey==null?"":Config.appKey);
            obj.put("t",t);
            obj.put("token",token);
            v = ZipUtils.zip(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        v += Config.writeEnd;
        return v;
    }

    public static void getServerIp(){

        if (Config.serverIP.isEmpty() || Config.serverIP==null) {
            System.out.println("初始化请求行情服务器地址："+Config.socketServer+Config.socketServer_Ip);
            String serverInfo = null;
            try {
                serverInfo = HttpWebCollecter.GetWebContent(Config.socketServer+Config.socketServer_Ip,"utf-8",5000);

                //System.out.println(serverInfo);
                if (serverInfo != null) {
                    if (!serverInfo.isEmpty()) {
                        if (serverInfo.indexOf(":") >= 0) {
                            serverInfo = serverInfo.trim();
                            serverInfo = serverInfo.replace("\n","");
                            serverInfo = serverInfo.replace("\r","");
                            String[] s = serverInfo.split(":");
                            String server = s[0];
                            String port = s[1];
                            logger.info("IP:"+server + " PORT:"+port);
                            Config.serverPort = port;
                            Config.serverIP = server;
                            return;

                        }
                    }
                }

            } catch (IOException e) {
                logger.error(e.toString());
            }


            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 继续请求
            getServerIp();
        }

    }

    /**
     * 获取K线网关
     */
    public static String getKlineServerIp(){
        String urlStr = Config.socketServer+Config.klineServer_Ip;
        StringBuffer sb = new StringBuffer();
        String klineserverip = null;
        try{
//            logger.info("请求K线:"+urlStr+"");
            sb.append(HttpWebCollecter.getWebContent(urlStr));
            if (sb.toString().indexOf(".")>0) {
                String ip = sb.toString();
                ip = ip.trim();
                if (ip.startsWith("http")) {
                    klineserverip = ip;
                }
            }

        }catch(Exception e)  {
            //  Report  any  errors  that  arise
            //sb.append(e.toString());
            logger.error("请求K线网关出错"+urlStr);
            try {
                sleep(3000);

            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

        }

        if (klineserverip==null){
            return getKlineServerIp();
        }

        return klineserverip;
    }

    /**
     * 获取实时行情对象
     * @param code
     * @param redis
     * @return
     */
    public static JSONObject getRealtime(String code,RedisCls redis){
        JSONObject obj = new JSONObject();
        String jsonstr = redis.getValue(code);
        if (jsonstr!=null){
            if (!jsonstr.isEmpty()){
                if (jsonstr.startsWith("{")){
                    try {
                        obj = new JSONObject(jsonstr);
                    } catch (JSONException e) {
//                        e.printStackTrace();

                    }
                }
            }
        }
        return obj;
    }

}
