package cn.edu.cuit.spamclassification.excutor;

import cn.edu.cuit.spamclassification.utils.HanlpProcess;
import cn.edu.cuit.spamclassification.utils.MyTFIDF;
import cn.edu.cuit.spamclassification.utils.ProcessFile;
import cn.edu.cuit.spamclassification.utils.RemoveStopWords;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.rdd.RDD;
import org.omg.CORBA.PUBLIC_MEMBER;
import scala.Tuple2;
import sun.security.pkcs11.P11Util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @ClassName Excutor
 * @Description TODO
 * @Author 21971
 * @Date 2021/1/30 15:33
 */
public class SpamTrain_Simhash {
    //the path of index file
    //fowling two paths need to be modified if run on linux
    public static final String FULL_PATH = "E:\\FinalProject\\datasets\\trec06c\\full\\index_train";
    public static final String DATA_PRE_PATH = "E:\\FinalProject\\datasets\\trec06c";
    public static final String MODEL_PATH = "E:\\FinalProject\\models\\WithSimhash";
    public static final String TOP200_PATH  = "E:\\FinalProject\\datasets\\trec06c\\spam_java.txt";
    public static final String SPAM1OO_PATH = "E:\\FinalProject\\datasets\\trec06c\\spamTop100.txt";
    public static final String HAM100_PATH  = "E:\\FinalProject\\datasets\\trec06c\\hamTop100.txt";
    //the number of spam used for training
    public static final Integer SPAM_NUM_TRAIN = 3000;
    //the number of ham used for training
    public static final Integer HAM_NUM_TRAIN = 3000;
    public static final Integer FEATURE_NUM = 100;






    public static void main(String[] args) {
        ArrayList<String> spamMailList  = new ArrayList<>();
        ArrayList<String> hamMailList   = new ArrayList<>();
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("spam");
        JavaSparkContext jsc = new JavaSparkContext(conf);


        /*****************read the type and path of all emails*******************************/
        Map<String, String> typeAndPathMap = ProcessFile.getTypeAndPath(FULL_PATH);


        /**********************read spam as will as ham and put them into spamMailList and hanMailList*****************/
        Integer spamNum = 0;
        Integer hamNum  = 0;
        int k = 1;
        for (Map.Entry<String,String> entry: typeAndPathMap.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            String path = null;
            //put spam into spamMailList
            if (value.equals("spam") && (spamNum++)<SPAM_NUM_TRAIN){
                System.out.println("正在读取第"+(k++)+"封邮件："+key+"\t该邮件为："+value);
                path = key.replace("..",DATA_PRE_PATH);
                //remove this sentence when run on linux
                path = path.replace("/", "\\");
//                System.out.println(path);
                String mail = null;
                mail = ProcessFile.readFile(path);
                //去掉所有空格和换行------------在processFile中去掉了，这里不用管了
//                mail.replaceAll(" ","");
//                mail.replaceAll("\n","");
//                System.out.println("去掉空格后的字符串：\n"+mail);
                spamMailList.add(mail);
            }
            if(value.equals("ham") && (hamNum++)<HAM_NUM_TRAIN) {
                System.out.println("正在读取第"+(k++)+"封邮件："+key+"\t该邮件为："+value);
                path = key.replace("..",DATA_PRE_PATH);
                //remove this sentence when run on linux
                path = path.replace("/", "\\");
//                System.out.println(path);
                String mail = null;
                mail = ProcessFile.readFile(path);
                hamMailList.add(mail);

            }
        }
        //这里检查前面去除空格没问题
//        System.out.println(spamMailList);
//        System.out.println(hamMailList);

        /*****************split words*******************************/
//        for (String mail:spamMailList){
//
//            wordsList = HanlpProcess.cutWords(mail);
//        }
        /********************配套旧版分词************************/
//        ArrayList<String> spamWordsList = new ArrayList<>();
//        ArrayList<String> hamWordsList = new ArrayList<>();
        System.out.println("**********************正在分词********************");
        ArrayList<ArrayList<String>> spamWordsList = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> hamWordsList = new ArrayList<ArrayList<String>>();
        spamWordsList = HanlpProcess.cutWords(spamMailList);
//        System.out.println(spamWordsList.contains("推广"));
        hamWordsList = HanlpProcess.cutWords(hamMailList);
//        System.out.println("####################split words################################");
//        for (ArrayList<String> list:spamWordsList){
//            System.out.println(list);
//        }
//        for (ArrayList<String> list:hamWordsList){
//            System.out.println(list);
//        }
//        System.out.println(hamWordsList);




        /*****************remove stop words——旧版*******************************/
//        ArrayList<String> keySpamWords = new ArrayList<>();
//        ArrayList<String> keyHamWords = new ArrayList<>();
//        keySpamWords = RemoveStopWords.getKeyWordsList(spamWordsList);
//        keyHamWords = RemoveStopWords.getKeyWordsList(hamWordsList);
        /*****************remove stop words——新版*******************************/
        System.out.println("**********************正在去除停用词********************");
        ArrayList<ArrayList<String>> keySpamWords = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> keyHamWords = new ArrayList<ArrayList<String>>();
        keySpamWords = RemoveStopWords.getKeyWordsList(spamWordsList);
        keyHamWords = RemoveStopWords.getKeyWordsList(hamWordsList);
//        System.out.println("####################remove stop words################################");
//        System.out.println(keyHamWords);
//        System.out.println(keySpamWords);


        // 对分词列表进行词频统计获取TOP100数据
        System.out.println("**********************获取TOP100********************");
        List<String> spamTop100 = getTop100(keySpamWords);
        List<String> hamTop100 = getTop100(keyHamWords);


        //对垃圾邮件特征词进行持久化，用于simhash判断
        String keySpamStr = "";
        for (String word:spamTop100){
            keySpamStr += word +",";
        }
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(SPAM1OO_PATH);
            printWriter.write(keySpamStr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            printWriter.close();
        }

        //对正常邮件特征词进行持久化，用于simhash判断
        String keyHamStr = "";
        for (String word:hamTop100){
            keyHamStr += word +",";
        }
        try {
            printWriter = new PrintWriter(HAM100_PATH);
            printWriter.write(keyHamStr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            printWriter.close();
        }

        // 合并TOP100数据得到特征数据TOP200----------------------------为什么呢？？？？
        System.out.println("**********************合并得到TOP200********************");
        ArrayList<String> allKeyWord = new ArrayList<>();
        String keyWordStr = "";
        for (String word:spamTop100){
            allKeyWord.add(word);
            //每个词之间用空格分离
            keyWordStr += word + ",";
        }
        for (String word:hamTop100){
            allKeyWord.add(word);
            //每个词之间用空格分离
            keyWordStr += word + ",";
        }

        //对top200数据进行本地持久化
        System.out.println("**********************持久化TOP200********************");
//        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(TOP200_PATH);
            printWriter.write(keyWordStr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            printWriter.close();
        }

        //对元数据列表进行特征匹配生成训练数据
        System.out.println("**********************对元数据列表进行特征匹配生成训练数据********************");
        ArrayList<LabeledPoint> spamLabledPointList = getTrainData(keySpamWords, allKeyWord, 1.0);

        //这里和后面预测返回结果是否有关
//        ArrayList<LabeledPoint> hamLabledPointList = getTrainData(keyHamWords, allKeyWord, 2.0);
        ArrayList<LabeledPoint> hamLabledPointList = getTrainData(keyHamWords, allKeyWord, 0.0);
//        System.out.println(spamLabledPointList);
//        System.out.println(hamLabledPointList);
        spamLabledPointList.addAll(hamLabledPointList);
        // 将训练数据转化为rdd
        RDD<LabeledPoint> trainRDD = jsc.parallelize(spamLabledPointList).rdd();
        // 数据训练生成模型
        System.out.println("**********************训练模型********************");
        NaiveBayesModel model = NaiveBayes.train(trainRDD);
        // 持久化模型
//        File file = new File("E:\\FinalProject\\datasets\\trec06c\\model\\model_java");
//        if(file.list().length == 0){
//            model.save(jsc.sc(),"E:\\FinalProject\\datasets\\trec06c\\model\\model_java");
//        }
        System.out.println("**********************持久化模型********************");
        model.save(jsc.sc(),MODEL_PATH);

    }

    /*****************get Top100 keywords******************************/
    public static List<String> getTop100(ArrayList<ArrayList<String>> keywordList){
        Map<String, Float> tf = MyTFIDF.tfCalculate(keywordList);
        List<Map.Entry<String, Float>> list = new ArrayList<Map.Entry<String, Float>>(tf.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        System.out.println(list);
        List<String> listTop100 = new ArrayList<>();
        //选100个TF最大的出来
        for(Map.Entry<String,Float> m : list){
//            System.out.println(m.getKey()+"="+m.getValue());
            listTop100.add(m.getKey());
            if (listTop100.size()==FEATURE_NUM){
                break;
            }
        }
        return listTop100;
    }

    /**
     * 通过分词列表与关键词列表获取训练映射数据
     * @param
     * @param keyWords
     * @param type
     * @return
     */
    public static ArrayList<LabeledPoint> getTrainData(ArrayList<ArrayList<String>> wordList, ArrayList<String> keyWords, double type){
        // 初始化向量标签容器
        int i = 1;
        ArrayList<LabeledPoint> featureBox = new ArrayList<LabeledPoint>();
        for(ArrayList<String> emailMeta:wordList){
            System.out.println("********正在对第"+i+"封邮件映射转化*********");
            // 对比映射转化
            double[] mapTrain = new double[200];
            int mapIndex = 0;
            for(String key:keyWords){
                if(emailMeta.contains(key)){
                    mapTrain[mapIndex] = 1.0;
                }
                mapIndex ++;
            }
            System.out.println("###########输出mapTrain格式##################");
            System.out.println(mapTrain);
            featureBox.add(new LabeledPoint(type, Vectors.dense(mapTrain)));
        }
        System.out.println("###########输出featureBox格式##################");
        System.out.println(featureBox);
        return featureBox;
    }
}
