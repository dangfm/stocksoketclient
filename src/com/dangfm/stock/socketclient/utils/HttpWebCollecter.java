package com.dangfm.stock.socketclient.utils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpWebCollecter {

    protected final static String USER_AGENT = "Mozilla/5.0";

    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param pathUrl
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String pathUrl, String param) {
        String result = "";
        String url = pathUrl;
        HttpPost post = new HttpPost(url);
        post.setHeader("user-Agent", USER_AGENT);
        StringEntity se = new StringEntity(param, "UTF-8");
        post.setEntity(se);
        // Send the post request and get the response
        HttpResponse response = null;
        try {
            HttpClient httpclient = HttpClients.createDefault();
            response = httpclient.execute(post);
            int status = response.getStatusLine().getStatusCode();
            System.out.println("Response Code : " + status);
            BufferedReader rd = null;
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            if (status == 200) {
                System.out.println("Notification sent successfully.");
            } else {
                System.out.println("Failed to send the notification!");
            }
            result = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return result;
    }


    /**
     * 网页抓取方法
     *
     * @param urlString
     *            要抓取的url地址
     * @param charset
     *            网页编码方式
     * @param timeout
     *            超时时间
     * @return 抓取的网页内容
     * @throws IOException
     *             抓取异常
     */
    public static String GetWebContent(String urlString, final String charset,
            int timeout) throws IOException {
        if (urlString == null || urlString.length() == 0) {
            return "";
        }
        urlString = (urlString.startsWith("http://") || urlString
                .startsWith("https://")) ? urlString : ("http://" + urlString)
                .intern();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Pragma", "no-cache");
        conn.setRequestProperty("Cache-Control", "no-cache");
 
        int temp = Integer.parseInt(Math.round(Math.random()
                * (UserAgent.length - 1))
                + "");
        //conn.setRequestProperty("user-Agent", UserAgent[temp]); // 模拟手机系统
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");// 只接受text/html类型，当然也可以接受图片,pdf,*/*任意，就是tomcat/conf/web里面定义那些
        conn.setConnectTimeout(timeout);
        try {
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "";
            }
        } catch (Exception e) {
            try {
                System.out.println(e.getMessage());
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return "";
        }
        InputStream input = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input,
                charset));
        String line = null;
        StringBuffer sb = new StringBuffer("");
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\r\n");
        }
        if (reader != null) {
            reader.close();
        }
        if (conn != null) {
            conn.disconnect();
        }
        return sb.toString();
    }
 
    public static String[] UserAgent = {
            "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.2",
            "Mozilla/5.0 (iPad; U; CPU OS 3_2_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B500 Safari/531.21.11",
            "Mozilla/5.0 (SymbianOS/9.4; Series60/5.0 NokiaN97-1/20.0.019; Profile/MIDP-2.1 Configuration/CLDC-1.1) AppleWebKit/525 (KHTML, like Gecko) BrowserNG/7.1.18121",
            "Nokia5700AP23.01/SymbianOS/9.1 Series60/3.0",
            "UCWEB7.0.2.37/28/998", "NOKIA5700/UCWEB7.0.2.37/28/977",
            "Openwave/UCWEB7.0.2.37/28/978",
            "Mozilla/4.0 (compatible; MSIE 6.0; ) Opera/UCWEB7.0.2.37/28/989" 
            };

    public static String getWebContent(String domain){
        // System.out.println("开始读取内容...("+domain+")");
        StringBuffer sb = new StringBuffer();
        try{
            URL url = new URL(domain);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while((line = in.readLine()) != null){
                sb.append(line);
            }
            in.close();
        }catch(Exception e)  {
            //  Report  any  errors  that  arise
//            sb.append(e.toString());
            System.err.println(e);
            System.err.println("Usage: java HttpClient <URL> [<filename>]");
        }
        return  sb.toString();
    }

    /**
     * 下载文件或图片到本地存储
     * @param urlString
     * @param filename
     * @param savePath
     * @throws Exception
     */
    public static String download(String urlString, String filename,String savePath) throws Exception {
        // 构造URL
        URL url = new URL(urlString);
        // 打开连接
        URLConnection con = url.openConnection();
        //设置请求超时为5s
        con.setConnectTimeout(5*1000);
        // 输入流
        InputStream is = con.getInputStream();

        // 1K的数据缓冲
        byte[] bs = new byte[1024];
        // 读取到的数据长度
        int len;
        // 输出的文件流
        File sf=new File(savePath);
        //判断目标文件所在的目录是否存在
//        if(!sf.getParentFile().exists()) {
//            //如果目标文件所在的目录不存在，则创建父目录
//            System.out.println("目标文件所在目录不存在，准备创建它！");
//            if(!sf.getParentFile().mkdirs()) {
//                System.out.println("创建目标文件所在目录失败！");
//                return null;
//            }
//        }
        if(!sf.exists()){
            sf.mkdirs();

        }
        sf.setWritable(true,false);
        try {
            OutputStream os = new FileOutputStream(sf.getPath()+"/"+filename);
            // 开始读取
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
            // 完毕，关闭所有链接
            //System.out.println(sf.getPath() + "/" + filename);
            os.close();
            is.close();
            return filename;
        }catch (Exception e){
            System.out.println(e);
            is.close();
            return "";
        }


        //return filename;
    }
    public static final Pattern PATTERN = Pattern.compile("<img\\s+(?:[^>]*)src\\s*=\\s*([^>]+)",Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);

    public static List<String> getImgSrc(String html){
        Matcher matcher = PATTERN.matcher(html);
        List<String> list = new ArrayList();
        while (matcher.find())   {
            String group = matcher.group(1);
            if(group == null){
                continue;
            }
            // 这里可能还需要更复杂的判断,用以处理src="...."内的一些转义符
            if(group.startsWith("'")){
                list.add(group.substring(1,group.indexOf("'",1)));
            }else if (group.startsWith("\"")){
                list.add(group.substring(1,group.indexOf("\"",1)));
            }else{
                list.add(group.split("\\s")[0]);
            }
        }
        return list;
    }

//    /***
//     * 获取ImageSrc地址
//     *
//     * @param listImageUrl
//     * @return
//     */
//    public static List<String> getImageSrc(List<String> listImageUrl,String imgTag) {
//        List<String> listImgSrc = new ArrayList<String>();
//        for (String image : listImageUrl) {
//            Matcher matcher = Pattern.compile(imgTag).matcher(image);
//            while (matcher.find()) {
//                listImgSrc.add(matcher.group().substring(0, matcher.group().length() - 1));
//            }
//        }
//        return listImgSrc;
//    }


    public static String readFile(String filePath){
        BufferedReader br = null;
        String value = "";
        try {
            File sf=new File(filePath);
            //判断目标文件所在的目录是否存在
            if(!sf.getParentFile().exists()) {
                //如果目标文件所在的目录不存在，则创建父目录
                //System.out.println("目标文件所在目录不存在，准备创建它！");
                if(!sf.getParentFile().mkdirs()) {
                    System.out.println("创建目标文件所在目录失败！"+filePath);
                    //return null;
                }
            }
            if (sf.exists()) {
                br = new BufferedReader(new FileReader(filePath));
                StringBuffer sb = new StringBuffer();
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                value = sb.toString();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    /**
     * 保存文件内容到本地存储
     * @param savePath
     * @param datas
     * @throws Exception
     */
    public static void saveFile(String savePath,String datas) throws Exception {
        // 输出的文件流
        File sf=new File(savePath);
        //判断目标文件所在的目录是否存在
        if(!sf.getParentFile().exists()) {
            //如果目标文件所在的目录不存在，则创建父目录
            //System.out.println("目标文件所在目录不存在，准备创建它！");
            if(!sf.getParentFile().mkdirs()) {
                System.out.println("创建目标文件所在目录失败！"+savePath);
                //return null;
            }
        }
        if(!sf.exists()){
            //sf.mkdirs();
            sf.createNewFile();
        }
        sf.setWritable(true,false);
        try {
            FileWriter fw = new FileWriter(sf.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(datas);
            bw.close();
        }catch (Exception e){
            System.out.println(e);
        }


        //return filename;
    }
}
