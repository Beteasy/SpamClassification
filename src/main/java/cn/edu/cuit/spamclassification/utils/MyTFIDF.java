package cn.edu.cuit.spamclassification.utils;

import org.apache.spark.sql.sources.In;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName MyTF
 * @Description TODO
 * @Author 21971
 * @Date 2021/4/5 15:58
 */
public class MyTFIDF {
    /**
     * @MethodName tfCalulate
     * @Description TODO   传入去除停用词后的分词列表，统计关键词在该类别中的TF-IDF
     * @Author 21971
     * @param keyWords
     * @Date 2021/4/8 14:38
     */
    public static Map<String, Float> tfCalculate(ArrayList<ArrayList<String>> keyWords){

        //统计该类别所有关键词数量
        int totalWords = 0;
        //用来做词频统计，词频没有归一化
        HashMap<String,Integer> dict = new HashMap<>();
        //归一化后的词频统计
        HashMap<String, Float> tf = new HashMap<>();

        //统计词频
        for (ArrayList<String> oneMail:keyWords){
            for (String word:oneMail){
                totalWords++;
                if (dict.containsKey(word)){
                    dict.put(word,dict.get(word)+1);
                }else {
                    dict.put(word,1);
                }
            }
        }

        //对词频进行归一化处理
        for (Map.Entry<String,Integer> entry:dict.entrySet()){
            float normalized = (float)entry.getValue()/totalWords;
            tf.put(entry.getKey(),normalized);
        }
        return tf;
    }

    /**
     * @MethodName tfIdfCalculate
     * @Description TODO   计算关键词的TF-IDF值
     * @Author 21971
     * @param tf:计算出的TF值
     *          keyWords:邮件列表，用于判断关键词存在于那些邮件中
     *          totalMails:邮件总数
     * @Date 2021/4/8 15:03
     */
    public static Map<String, Float> tfIdfCalculate(Map<String, Float> tf, ArrayList<ArrayList<String>> keyWords, Integer totalMails){
        //存放计算出关键词的TF-IDF值
        HashMap<String, Float> tfIdf = new HashMap<>();

        //计算IDF值
        for (String key:tf.keySet()){
            //包含该关键词的文档数
            int containMailNum = 0;
            for (ArrayList<String> oneMail:keyWords){
                if (oneMail.contains(key)){
                    containMailNum++;
                }
            }
            float idf = (float) Math.log(Double.valueOf(totalMails+1)/(containMailNum+1));
            tfIdf.put(key,idf*tf.get(key));
        }
        return tfIdf;
    }
}
