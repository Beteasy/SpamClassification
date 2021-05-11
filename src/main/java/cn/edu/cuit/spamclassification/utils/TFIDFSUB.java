package cn.edu.cuit.spamclassification.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName TFIDFSUB
 * @Description TODO  参照论文基于TF*IDF的垃圾邮件过滤特征选择改进算法实现的TF-IDF改进算法
 *                    主要思想是将每个特征词在每一类邮件中的词频先进行相减，在取对数，对特征词在两类邮件中都
 *                    大量出现的情况做一个优化（两类邮件中都大量出现说明区分度不好）
 * @Author 21971
 * @Date 2021/5/2 12:56
 */

public class TFIDFSUB {
    private Map<String, Integer> TF_ORIGIN;
    private Map<String, Float> TF_NORMALIZED;

    public Map<String, Integer> getTF_ORIGIN() {
        return TF_ORIGIN;
    }

    public void setTF_ORIGIN(Map<String, Integer> TF_ORIGIN) {
        this.TF_ORIGIN = TF_ORIGIN;
    }

    public Map<String, Float> getTF_NORMALIZED() {
        return TF_NORMALIZED;
    }

    public void setTF_NORMALIZED(Map<String, Float> TF_NORMALIZED) {
        this.TF_NORMALIZED = TF_NORMALIZED;
    }

    public static Map<String, Integer> tfCalculate(ArrayList<ArrayList<String>> keyWords) {
        //统计该类别所有关键词数量
        int totalWords = 0;
        //用来做词频统计，词频没有归一化
        HashMap<String, Integer> dict = new HashMap<>();
        //归一化后的词频统计
        HashMap<String, Float> tf = new HashMap<>();
        //统计词频
        for (ArrayList<String> oneMail : keyWords) {
            for (String word : oneMail) {
                totalWords++;
                if (dict.containsKey(word)) {
                    dict.put(word, dict.get(word) + 1);
                } else {
                    dict.put(word, 1);
                }
            }
        }
//        this.setTF_ORIGIN(dict);
        return dict;
    }

    public static Map<String, Float> tfFormat(Map<String, Integer> TF_ORIGIN){
        /**
         * @MethodName tfFormat
         * @Description TODO   将词频取对数，降低高词频词组对权重的影响
         *              TODO   思考下，和用总词组数进行归一化，那个效果更好
         * @Author 21971
         * @param TF_ORIGIN
         */
        Map<String, Float> tf = new HashMap<>();

        for (Map.Entry<String,Integer> entry:TF_ORIGIN.entrySet()){
            float normalized = (float) (Math.log(entry.getValue()+1)+1);
            tf.put(entry.getKey(),normalized);
        }
        return tf;
    }

    public static void TF_SUB(Map<String, Integer> hamTF, Map<String, Integer> spamTF){
        /**
         * @MethodName TF_SUB
         * @Description TODO   将两类邮件中存在的共同的特征，将其词频进行相减
         * @Author 21971
         * @param hamTF
         * @param spamTF
         * @Date 2021/5/3 10:47
         */
        for (String key: hamTF.keySet()){
            if (spamTF.containsKey(key)){
                Integer num1 = hamTF.get(key);
                Integer num2 = spamTF.get(key);
                hamTF.put(key,Math.abs(num1-num2));
                spamTF.put(key,Math.abs(num1-num2));
            }
        }
    }

    public static Map<String, Float> IDFCalculate(Map<String, Integer> tf, ArrayList<ArrayList<String>> keyWords, Integer totalMails){
        //存放计算出关键词的IDF值
        HashMap<String, Float> IDFMap = new HashMap<>();
        //计算IDF值
//        int i=1;
        for (String key:tf.keySet()){
            //包含该关键词的文档数
            int containMailNum = 0;
//            System.out.println("判断第"+(i++)+"个关键词,共"+tf.size()+"个关键词");
            for (ArrayList<String> oneMail:keyWords){
                if (oneMail.contains(key)){
                    containMailNum++;
                }
            }
//          计算IDF，做了平滑处理
            float idf = (float) Math.log(Double.valueOf(totalMails+1)/(containMailNum+1));
            System.out.println("log("+(totalMails+1)+"/"+(containMailNum+1)+")="+idf);
//            System.out.println("他的idf是"+idf);
            IDFMap.put(key,idf);
        }
        return IDFMap;
    }

    public static void IDFSub(Map<String, Float> hamIDF, Map<String, Float> spamIDF){
        for (String key: hamIDF.keySet()){
            if (spamIDF.containsKey(key)){
                Float num1 = hamIDF.get(key);
                Float num2 = spamIDF.get(key);
                hamIDF.put(key,Math.abs(num1-num2));
                spamIDF.put(key,Math.abs(num1-num2));
            }
        }
    }
    public static Map<String, Float> TFIDFCalculate(Map<String, Integer> TFMap, Map<String, Float> IDFMap){
        Map<String, Float> TFIDF = new HashMap<>();
        for (String key: TFMap.keySet()){
            //对词频取对数
            Float tf = (float) (Math.log(TFMap.get(key)+1)+1);
            Float idf = IDFMap.get(key);
            TFIDF.put(key, tf*idf);
        }
        return TFIDF;
    }
}
