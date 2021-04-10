package cn.edu.cuit.spamclassification.utils;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName ProcessFile
 * @Description TODO   
 * @Author 21971
 * @Date 2021/1/25 17:58
 */
public class ProcessFile {

    public static Map<String,String> getTypeAndPath(String fullPath){
        /**
         * @MethodName getTypeAndPath
         * @Description TODO   get the path and type of each email
         * @Author 21971
         * @param fullPath the path of file index
         * @Date 2021/1/30 12:06
         */
        Map<String,String> typeAndPathMap = new HashMap<>();
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        String[] typeAndPath = null;
        String str = null;
        try {
            fileReader = new FileReader(fullPath);
            bufferedReader = new BufferedReader(fileReader);
//            int i =1;
            while ((str = bufferedReader.readLine()) != null){
                //split the type and path user whitespace
//                System.out.println(str);
                typeAndPath = str.split(" ");
                //index 0:type, index 1:path
                typeAndPathMap.put(typeAndPath[1], typeAndPath[0]);
//                if (i++ == 10){
//                    break;
//                }
            }
//            System.out.println(typeAndPathMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return typeAndPathMap;
    }



//    public static ArrayList<String> readFiles(String path){
//        /**
//         * @MethodName readFiles
//         * @Description TODO   read emails and put them into mailList
//         *                      if you read spam, pass the path of spam
//         *                      if you read ham, pass the path of ham
//         * @Author 21971
//         * @param path
//         * @Date 2021/1/30 12:39
//         */
//        ArrayList<String> mailList = new ArrayList<>();
//        try {
//            File file = new File(path);
//            File[] files = file.listFiles();
//            if (files != null) {
//                for (File f:files){
//                    //读取每个文件的内容，组成一个字符串，放到list中
//                    FileInputStream fileInputStream = new FileInputStream(f.getPath());
//                    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "GBK");
//                    String str = "";
//                    //byte[] buff = new byte[1024];
//                    char[] buff = new char[1024];
//                    int len = 0;
////                    while ((len = fileInputStream.read(buff)) != -1){
////                        str += new String(buff,0,len);
////                    }
//                    while ((len = inputStreamReader.read(buff)) != -1){
//                        str += new String(buff);
//                    }
//                    //System.out.println("**************************全文************************\n"+str);
////                    if (str.equals(new String(str.getBytes("GBK"), "GBK"))) {      //判断是不是GB2312
////                        str = new String(str.getBytes("GBK"), StandardCharsets.UTF_8);
////                    }else if (str.equals(new String(str.getBytes("UTF-8"), "UTF-8"))){
////                        str = new String(str.getBytes(StandardCharsets.UTF_8),StandardCharsets.UTF_8);
////                    }
//                    mailList.add(str);
//
//                }
//            }else{
//                System.out.println("File not found!");
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return mailList;
//    }


    public static String readFile(String filePath) {
        /**
         * @MethodName readFile
         * @Description TODO   read one email and return as a string
         *                     if you read spam, pass the path of spam
         *                     if you read ham, pass the path of ham
         * @Author 21971
         * @param filePath
         * @Date 2021/1/30 12:42
         */
        StringBuffer str = new StringBuffer();
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        FileInputStream fileInputStream = null;
        File file = null;
        String result = null;
        try {
            file = new File(filePath);
            fileInputStream = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(fileInputStream, "GBK");
            bufferedReader = new BufferedReader(inputStreamReader);
            String tmp = "";
            int flag = 0;
            while ((tmp = bufferedReader.readLine()) != null) {
                if (flag == 1) {
//                    System.out.println(tmp);
                    str.append(tmp);
//                    System.out.println(str.toString());
                }
                if (tmp.equals("") || tmp.length() == 0) {
                    flag = 1;
                }
            }
            bufferedReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        result = str.toString().replaceAll("\\s*","");
//        System.out.println(result);
        return result;
    }


//    public static ArrayList<String> getBody(ArrayList<String> mailList){
//        /**
//         * @MethodName getBody
//         * @Description TODO   获取邮件正文内容
//         * @Author 21971
//         * @param mailList：获取到的全部邮件的完整内容列表
//         * @Date 2021/1/26 15:43
//         */
//        ArrayList<String> bodyList =  new ArrayList<>();
//        int i=1;
//        for (String mail: mailList){
//            //只匹配出现的第一个换行
//            String[] str = mail.split("\n\n",2);
//            //System.out.println("第"+i+"次打印:"+str[1]);
//            //str[1]才是正文内
//            //System.out.println("**************************body************************\n"+str[1]);
//            bodyList.add(str[1]);
//        }
//        return bodyList;
//    }

}
