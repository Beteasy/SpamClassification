package cn.edu.cuit.spamclassification.excutor;

import cn.edu.cuit.spamclassification.utils.HanlpProcess;
import cn.edu.cuit.spamclassification.utils.MyTFIDF;
import cn.edu.cuit.spamclassification.utils.ProcessFile;
import cn.edu.cuit.spamclassification.utils.RemoveStopWords;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.SparkSession;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @ClassName SpamTrain_TFIDF
 * @Description TODO
 * @Author 21971
 * @Date 2021/2/21 11:11
 */
public class SpamTrain_TFIDF_spark {
    //the path of index file
    //fowling two paths need to be modified if run on linux
    public static final String FULL_PATH = "E:\\FinalProject\\datasets\\trec06c\\full\\index_train";
    public static final String DATA_PRE_PATH = "E:\\FinalProject\\datasets\\trec06c";
    public static final String MODEL_PATH = "E:\\FinalProject\\models\\withTFIDF";
    public static final String TOP200_PATH  = "E:\\FinalProject\\datasets\\trec06c\\spam_java.txt";
    //the number of spam used for training
    public static final Integer SPAM_NUM_TRAIN = 3000;
    //the number of ham used for training
    public static final Integer HAM_NUM_TRAIN = 3000;
    //the number of features
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

        /*****************将数据写入文件，后面再读成Dataset**************************/



        /********************新增内容：TF-IDF******************************/
        Map<String, Float> hamTF = MyTFIDF.tfCalculate(keyHamWords);
        Map<String, Float> hamTFIDF = MyTFIDF.tfIdfCalculate(hamTF, keyHamWords, HAM_NUM_TRAIN);
        Map<String, Float> spamTF = MyTFIDF.tfCalculate(keySpamWords);
        Map<String, Float> spamTFIDF = MyTFIDF.tfIdfCalculate(spamTF, keySpamWords, SPAM_NUM_TRAIN);
        System.out.println(spamTF);
        System.out.println(spamTFIDF);


        /***********************获取TOP100**************************/
        /***
         * TF-IDF初始无序
         * 按照value对其进行排序
         * 选取前100个特征
         */
        List<Map.Entry<String, Float>> hamList = new ArrayList<Map.Entry<String, Float>>(hamTFIDF.entrySet());
        Collections.sort(hamList, new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        List<Map.Entry<String, Float>> spamList = new ArrayList<Map.Entry<String, Float>>(spamTFIDF.entrySet());
        Collections.sort(spamList, new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        List<String> spamTop100 = new ArrayList<>();
        List<String> hamTop100 = new ArrayList<>();
        //选100个TF-IDF最大的出来
        for(Map.Entry<String,Float> m : hamList){
//            System.out.println(m.getKey()+"="+m.getValue());
            hamTop100.add(m.getKey());
            if (hamTop100.size()==FEATURE_NUM){
                break;
            }
        }
        for(Map.Entry<String,Float> m : spamList){
//            System.out.println(m.getKey()+"="+m.getValue());
            spamTop100.add(m.getKey());
            if (spamTop100.size()==FEATURE_NUM){
                break;
            }
        }
//        System.out.println(hamTop100);
//        System.out.println(spamTop100);



        // 对分词列表进行词频统计获取TOP100数据
//        System.out.println("**********************获取TOP100********************");
//        List<String> spamTop100 = getTop100(keySpamWords, jsc);
//        List<String> hamTop100 = getTop100(keyHamWords, jsc);
        /*****************************************************/
//        System.out.println("/*******************top100**********************************/");
//        System.out.println("spamTop100"+spamTop100);
////        System.out.println(spamTop100.remove("的"));
//        System.out.println("hamTop100"+hamTop100);

        // 合并TOP100数据得到特征数据TOP200
        System.out.println("**********************合并得到TOP200********************");
        ArrayList<String> allKeyWord = new ArrayList<>();
        String keyWordStr = "";
        for (String word:spamTop100){
            allKeyWord.add(word);
            //每个词之间用逗号分离
            keyWordStr += word + ",";
        }
        for (String word:hamTop100){
            allKeyWord.add(word);
            //每个词之间用逗号分离
            keyWordStr += word + ",";
        }

        //对top200数据进行本地持久化
        System.out.println("**********************持久化TOP200********************");
        PrintWriter printWriter = null;
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
//        String modelPath = "E:\\FinalProject\\datasets\\trec06c\\model";
        model.save(jsc.sc(),MODEL_PATH);
    }

    /*****************get Top100 keywords******************************/
//    public static List<String> getTop100(ArrayList<ArrayList<String>> keywordList, JavaSparkContext jsc){
//        //获取top15
//        ArrayList<List<Tuple2<String,Integer>>> top15List = new ArrayList<List<Tuple2<String,Integer>>>();
//        for (ArrayList<String> s: keywordList){
//            //将email分词列表转化为javaRDD
//            //每一个RDD都是一封邮件
//            JavaRDD<String> emailRDD = jsc.parallelize(s);
//            //映射为元组
//            //聚合
//            //降序排列
//            //获取top15热词列表
//            List<Tuple2<String, Integer>> metaList  = emailRDD.mapToPair(new PairFunction<String, String, Integer>() {
//                @Override
//                public Tuple2<String, Integer> call(String s) throws Exception {
//                    return new Tuple2<String,Integer>(s,1);
//                }
//            }).reduceByKey(new Function2<Integer, Integer, Integer>() {
//                @Override
//                public Integer call(Integer integer, Integer integer2) throws Exception {
//                    return integer+integer2;
//                }
//            }).mapToPair(new PairFunction<Tuple2<String, Integer>, Integer, String>() {
//                @Override
//                public Tuple2<Integer, String> call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
//                    return new Tuple2<Integer,String>(stringIntegerTuple2._2(),stringIntegerTuple2._1());
//                }
//            }).sortByKey(false).mapToPair(new PairFunction<Tuple2<Integer, String>, String, Integer>() {
//                @Override
//                public Tuple2<String, Integer> call(Tuple2<Integer, String> integerStringTuple2) throws Exception {
//                    return new Tuple2<String,Integer>(integerStringTuple2._2(), integerStringTuple2._1());
//                }
//            }).take(15);
//            top15List.add(metaList);
//        }
//        //汇总top15为top100
//        ArrayList<Tuple2<String,Integer>> allTuple = new ArrayList<Tuple2<String,Integer>>();
//        for (List<Tuple2<String,Integer>> list1: top15List){
//            allTuple.addAll(list1);
//        }
//
//        //将所有元组列表转化为javaRDD
//        JavaRDD<Tuple2<String,Integer>> allJRDD = jsc.parallelize(allTuple);
//        // 映射
//        // 聚合
//        // 降序
//        // 获取TOP100热词列表
//        List<String> top100List = allJRDD.mapToPair(new PairFunction<Tuple2<String, Integer>, String, Integer>() {
//            @Override
//            public Tuple2<String, Integer> call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
//                return new Tuple2<String,Integer>(stringIntegerTuple2._1(),stringIntegerTuple2._2());
//            }
//        }).reduceByKey(new Function2<Integer, Integer, Integer>() {
//            @Override
//            public Integer call(Integer integer, Integer integer2) throws Exception {
//                return integer+integer2;
//            }
//        }).mapToPair(new PairFunction<Tuple2<String, Integer>, Integer, String>() {
//            @Override
//            public Tuple2<Integer, String> call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
//                return new Tuple2<Integer,String>(stringIntegerTuple2._2(),stringIntegerTuple2._1());
//            }
//        }).sortByKey(false).map(new Function<Tuple2<Integer, String>, String>() {
//            @Override
//            public String call(Tuple2<Integer, String> integerStringTuple2) throws Exception {
//                return integerStringTuple2._2();
//            }
//        }).take(100);
//
//        return top100List;
//    }

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
            double[] mapTrain = new double[2*FEATURE_NUM];
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
