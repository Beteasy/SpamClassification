package cn.edu.cuit.spamclassification.excutor;

import cn.edu.cuit.spamclassification.utils.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.sources.In;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @ClassName SpamTrain_TFIDF
 * @Description TODO
 * @Author 21971
 * @Date 2021/2/21 11:11
 */
public class MySpamTrain_TFIDF_SUB {
    //the path of index file
    //fowling two paths need to be modified if run on linux
    public static final String FULL_PATH = "E:\\FinalProject\\datasets\\trec06c\\full\\index_train";
    public static final String DATA_PRE_PATH = "E:\\FinalProject\\datasets\\trec06c";
    public static final String MODEL_PATH = "E:\\FinalProject\\models\\SpamTrain_TFIDF_SUB";
    public static final String TOP200_PATH  = "E:\\FinalProject\\datasets\\trec06c\\spam_java.txt";
    //the number of spam used for training
    public static final Integer SPAM_NUM_TRAIN = 3000;
    //the number of ham used for training
    public static final Integer HAM_NUM_TRAIN = 3000;
    //the number of features
    public static final Integer FEATURE_NUM = 100;
//    public static final Integer TOTAL_FEATURE = 2*FEATURE_NUM;



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
                System.out.println("???????????????"+(k++)+"????????????"+key+"\t???????????????"+value);
                path = key.replace("..",DATA_PRE_PATH);
                //remove this sentence when run on linux
                path = path.replace("/", "\\");
                String mail = null;
                mail = ProcessFile.readFile(path);
                spamMailList.add(mail);
            }
            if(value.equals("ham") && (hamNum++)<HAM_NUM_TRAIN) {
                System.out.println("???????????????"+(k++)+"????????????"+key+"\t???????????????"+value);
                path = key.replace("..",DATA_PRE_PATH);
                //remove this sentence when run on linux
                path = path.replace("/", "\\");
                String mail = null;
                mail = ProcessFile.readFile(path);
                hamMailList.add(mail);

            }
        }


        System.out.println("**********************????????????********************");
        ArrayList<ArrayList<String>> spamWordsList = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> hamWordsList = new ArrayList<ArrayList<String>>();
        spamWordsList = HanlpProcess.cutWords(spamMailList);
        hamWordsList = HanlpProcess.cutWords(hamMailList);

        /*****************remove stop words????????????*******************************/
        System.out.println("**********************?????????????????????********************");
        ArrayList<ArrayList<String>> keySpamWords = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> keyHamWords = new ArrayList<ArrayList<String>>();
        keySpamWords = RemoveStopWords.getKeyWordsList(spamWordsList);
        keyHamWords = RemoveStopWords.getKeyWordsList(hamWordsList);


        /*****************???????????????????????????????????????Dataset**************************/



        /********************???????????????TF-IDF******************************/
        System.out.println("**********************????????????TF*******************");
        Map<String, Integer> hamTF = TFIDFSUB.tfCalculate(keyHamWords);
        Map<String, Integer> spamTF = TFIDFSUB.tfCalculate(keySpamWords);
        Map<String, Float> hamTFIDF = new HashMap<>();
        Map<String, Float> spamTFIDF = new HashMap<>();
        TFIDFSUB.TF_SUB(hamTF,spamTF);
        System.out.println("**********************????????????IDF*******************");
        Map<String, Float> hamIDF = TFIDFSUB.IDFCalculate(hamTF, hamWordsList, HAM_NUM_TRAIN);
        Map<String, Float> spamIDF = TFIDFSUB.IDFCalculate(spamTF, spamWordsList, SPAM_NUM_TRAIN);
        TFIDFSUB.IDFSub(hamIDF,spamIDF);
        hamTFIDF = TFIDFSUB.TFIDFCalculate(hamTF,hamIDF);
        spamTFIDF = TFIDFSUB.TFIDFCalculate(spamTF,spamIDF);



        /***********************??????TOP100**************************/
        /***
         * TF-IDF????????????
         * ??????value??????????????????
         * ?????????100?????????
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
        //???100???TF-IDF???????????????
        for(Map.Entry<String,Float> m : hamList){
            hamTop100.add(m.getKey());
            if (hamTop100.size()==FEATURE_NUM){
                break;
            }
        }
        for(Map.Entry<String,Float> m : spamList){
            spamTop100.add(m.getKey());
            if (spamTop100.size()==FEATURE_NUM){
                break;
            }
        }

        // ??????TOP100????????????????????????TOP200
        System.out.println("**********************????????????TOP200********************");
        ArrayList<String> allKeyWord = new ArrayList<>();
        String keyWordStr = "";
        for (String word:spamTop100){
            allKeyWord.add(word);
            //??????????????????????????????
            keyWordStr += word + ",";
        }
        for (String word:hamTop100){
            allKeyWord.add(word);
            //??????????????????????????????
            keyWordStr += word + ",";
        }

        //???top200???????????????????????????
        System.out.println("**********************?????????TOP200********************");
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(TOP200_PATH);
            printWriter.write(keyWordStr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            printWriter.close();
        }

        //??????????????????????????????????????????????????????
        System.out.println("**********************??????????????????????????????????????????????????????********************");
        ArrayList<LabeledPoint> spamLabledPointList = getTrainData(keySpamWords, allKeyWord, 1.0);
        ArrayList<LabeledPoint> hamLabledPointList = getTrainData(keyHamWords, allKeyWord, 0.0);
        spamLabledPointList.addAll(hamLabledPointList);
        // ????????????????????????rdd
        RDD<LabeledPoint> trainRDD = jsc.parallelize(spamLabledPointList).rdd();
        // ????????????????????????
        System.out.println("**********************????????????********************");
        NaiveBayesModel model = NaiveBayes.train(trainRDD);
        // ???????????????
        System.out.println("**********************???????????????********************");
        model.save(jsc.sc(),MODEL_PATH);
    }
    /**
     * ????????????????????????????????????????????????????????????
     * @param
     * @param keyWords
     * @param type
     * @return
     */
    public static ArrayList<LabeledPoint> getTrainData(ArrayList<ArrayList<String>> wordList, ArrayList<String> keyWords, double type){
        // ???????????????????????????
        int i = 1;
        ArrayList<LabeledPoint> featureBox = new ArrayList<LabeledPoint>();
        for(ArrayList<String> emailMeta:wordList){
            System.out.println("********????????????"+i+"?????????????????????*********");
            // ??????????????????
            double[] mapTrain = new double[2*FEATURE_NUM];
            int mapIndex = 0;
            for(String key:keyWords){
                if(emailMeta.contains(key)){
                    mapTrain[mapIndex] = 1.0;
                }
                mapIndex ++;
            }
            System.out.println("###########??????mapTrain??????##################");
            System.out.println(mapTrain);
            featureBox.add(new LabeledPoint(type, Vectors.dense(mapTrain)));
        }
        System.out.println("###########??????featureBox??????##################");
        System.out.println(featureBox);
        return featureBox;
    }
}
