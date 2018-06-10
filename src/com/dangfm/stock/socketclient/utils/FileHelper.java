package com.dangfm.stock.socketclient.utils;

import java.io.*;

/**
 * 文件操作
 * Created by dangfm on 17/6/10.
 */
public class FileHelper {
    /**
     * 创建文件
     * @param fileName  文件名称
     * @param filecontent   文件内容
     * @return  是否创建成功，成功则返回true
     */
    public static boolean createFile(String fileName,String filecontent){
        Boolean bool = false;
        File file = new File(fileName);
        try {
            //如果文件不存在，则创建新的文件
            if(!file.exists()){
                file.createNewFile();
                bool = true;
//                System.out.println("success create file,the file is "+fileName);
                //创建文件成功后，写入内容到文件里
                writeFileContent(fileName, filecontent,true);
            }else{
                bool = true;
//                System.out.println("success update file,the file is "+fileName);
                //创建文件成功后，写入内容到文件里
                writeFileContent(fileName, filecontent, false);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bool;
    }

    /**
     * 向文件中写入内容
     * @param filepath 文件路径与名称
     * @param newstr  写入的内容
     * @return
     * @throws IOException
     */
    public static boolean writeFileContent(String filepath,String newstr,boolean isNew) throws IOException {
        Boolean bool = false;
        String filein = newstr+"\r\n";//新写入的行，换行
        String temp  = "";

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        FileOutputStream fos  = null;
        PrintWriter pw = null;
        try {
            File file = new File(filepath);//文件路径(包括文件名称)
            //将文件读入输入流
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            StringBuffer buffer = new StringBuffer();

            //文件原有内容
            if (isNew) {
                for (int i = 0; (temp = br.readLine()) != null; i++) {
                    buffer.append(temp);
                    // 行与行之间的分隔符 相当于“\n”
                    buffer = buffer.append(System.getProperty("line.separator"));
                }
            }
            buffer.append(filein);



            fos = new FileOutputStream(file);
            pw = new PrintWriter(fos);
            pw.write(buffer.toString().toCharArray());
            pw.flush();
            bool = true;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }finally {
            //不要忘记关闭
            if (pw != null) {
                pw.close();
            }
            if (fos != null) {
                fos.close();
            }
            if (br != null) {
                br.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
        return bool;
    }

    /**
     * 删除文件
     * @param fileName 文件名称
     * @return
     */
    public static boolean delFile(String fileName){
        Boolean bool = false;
        File file  = new File(fileName);
        try {
            if(file.exists()){
                file.delete();
                bool = true;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return bool;
    }

    public static boolean makeDirs(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        File folder = new File(filePath);
        return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
    }

    public static String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        byte[] filecontent = null;
        if(file.exists()) {
            Long filelength = file.length();
            filecontent = new byte[filelength.intValue()];
            try {
                FileInputStream in = new FileInputStream(file);
                in.read(filecontent);
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
//            System.out.println("文件不存在：" + fileName);
            return "";
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }
}
