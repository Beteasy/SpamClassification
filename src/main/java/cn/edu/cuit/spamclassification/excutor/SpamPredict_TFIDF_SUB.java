package cn.edu.cuit.spamclassification.excutor;

import cn.edu.cuit.spamclassification.utils.HanlpProcess;
import cn.edu.cuit.spamclassification.utils.ProcessFile;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.rdd.RDD;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @ClassName SpamPredict
 * @Description TODO 进行邮件预测——使用TF-IDF作差的优化方法
 * @Author 21971
 * @Date 2021/2/12 14:19
 */
public class SpamPredict_TFIDF_SUB {

    public static final String FULL_PATH = "E:\\FinalProject\\datasets\\trec06c\\full\\index_pre";
    public static final String DATA_PRE_PATH = "E:\\FinalProject\\datasets\\trec06c";
//    public static final String MODEL_PATH = "E:\\FinalProject\\models\\withTFIDF";
//public static final String MODEL_PATH = "E:\\FinalProject\\models\\frequency";
public static final String MODEL_PATH = "E:\\FinalProject\\models\\SpamTrain_TFIDF_SUB";
    public static final String TOP200_PATH  = "E:\\FinalProject\\datasets\\trec06c\\spam_java.txt";
    //the number of spam used for predicting
    public static final Integer SPAM_NUM_PREDICT = 3000;
    //the number of ham used for predicting
    public static final Integer HAM_NUM_PREDICT = 3000;
    //the number of features
    public static final Integer FEATURE_NUM = 100;


    public static void textCheck(String str){
        //创建spark配置对象
        SparkConf conf = new SparkConf().setAppName("spam").setMaster("local[*]");
        //创建JavaSpark上下文对象
        JavaSparkContext javaSparkContext = new JavaSparkContext(conf);
        //获取持久化的Top200字符串
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader bufferedReader = null;
        ArrayList<String> keyString = new ArrayList<>();
        InputStreamReader inputStreamReader = null;
        FileInputStream fileInputStream = null;
        File file = null;
        String result = null;
        try {
            file = new File(TOP200_PATH);
            fileInputStream = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(fileInputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            String tmp = "";
            while ((tmp = bufferedReader.readLine()) != null) {
                stringBuffer.append(tmp);
                }
            bufferedReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
        }
        result = stringBuffer.toString();
        List<String> keyList = Arrays.asList(result.split(","));
        ArrayList strList = new ArrayList<String>();
        strList.add(str);
        System.out.println(keyList);
        System.out.println(strList);

        //通过源数据列表与TOP200列表数据对比生成特征值列表
        ArrayList<double[]> strNumList = getCharacter(strList,keyList);
        for (double d: strNumList.get(0)){
            System.out.println(d);
        }
//        System.out.println(strNumList.get(0).toString());
        Vector testVec = Vectors.dense(strNumList.get(0));
        //加载持久化的训练模型
        NaiveBayesModel naiveBayesModel = NaiveBayesModel.load(javaSparkContext.sc(), MODEL_PATH);
        //通过模型对每一封email特征值列表进行预测
        double predict = naiveBayesModel.predict(testVec);
        System.out.println("预测内容："+str);
        System.out.println("预测值:"+predict);
        if (predict == 1.0){
            System.out.println("该邮件为垃圾邮件");
        }else {
            System.out.println("该邮件为正常邮件");
        }


    }

    /**
     * @MethodName fileCheck
     * @Description TODO   对已知数据进行预测，并进行算法的评价
     *                      现存问题：1.将文件名和邮件进行配对——他将垃圾邮件和正常邮件分别放在两个不同的目录下，我不这样做，看怎么处理
     * @Author 21971
     * @param
     * @Date 2021/2/16 12:01
     */
    public static void fileCheck(){
        //创建spark配置对象
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("spamPredict");
        //创建spark上下文对象
        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);
        //获取垃圾邮件和正常邮件，并放入列表中
        /*****************read the type and path of all emails*******************************/
        Map<String, String> typeAndPathMap = ProcessFile.getTypeAndPath(FULL_PATH);


        /**********************read spam as will as ham and put them into spamMailList and hanMailList*****************/
        Integer spamNum = 0;
        Integer hamNum  = 0;
        int i = 0;
        int j = 0;
        int k = 1;
        ArrayList<String> spamMailList  = new ArrayList<>();
        ArrayList<String> hamMailList   = new ArrayList<>();
        String[] spamNameList = new String[SPAM_NUM_PREDICT];
        String[] hamNameList = new String[HAM_NUM_PREDICT];
        String[] fileNameList = new String[SPAM_NUM_PREDICT+HAM_NUM_PREDICT];
        for (Map.Entry<String,String> entry: typeAndPathMap.entrySet()){
            //key是文件相对路径
            String key = entry.getKey();
            //value是文件的类型
            String value = entry.getValue();
            String path = null;
            //put spam into spamMailList
            if (value.equals("spam") && spamNum<SPAM_NUM_PREDICT){
//            if (value.equals("spam")){
                spamNum++;
                System.out.println("正在读取第"+(k++)+"封邮件："+key+"\t该邮件为："+value);
                spamNameList[i++] = key;
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
            if(value.equals("ham") && hamNum<HAM_NUM_PREDICT) {
//            if(value.equals("ham")) {
                hamNum++;
                System.out.println("正在读取第"+(k++)+"封邮件："+key+"\t该邮件为："+value);
//                System.out.println("正在读取第"+k+"封邮件："+key+"\t该邮件为："+value);
                hamNameList[j++] = key;
                path = key.replace("..",DATA_PRE_PATH);
                //remove this sentence when run on linux
                path = path.replace("/", "\\");
//                System.out.println(path);
                String mail = null;
                mail = ProcessFile.readFile(path);
                hamMailList.add(mail);
            }
            if (hamNum>HAM_NUM_PREDICT&&spamNum>SPAM_NUM_PREDICT){
                break;
            }

        }
        System.arraycopy(spamNameList,0,fileNameList,0,spamNameList.length);
        System.arraycopy(hamNameList,0,fileNameList,spamNameList.length,hamNameList.length);
//        System.out.println("************spamMailList***************\n");
//        for (String mail:spamMailList){
//            System.out.println(mail);
//        }
//        System.out.println("************hamMailList***************\n");
//        for (String mail:hamMailList){
//            System.out.println(mail);
//        }
//        System.out.println("************fileNameList***************\n");
//        for (String name:fileNameList){
//            System.out.println(name);
//        }




        //获取持久化的Top200字符串
        System.out.println("*************************正在加载TOP200************************");
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader bufferedReader = null;
        ArrayList<String> keyString = new ArrayList<>();
        InputStreamReader inputStreamReader = null;
        FileInputStream fileInputStream = null;
        File file = null;
        String result = null;
        try {
            file = new File(TOP200_PATH);
            fileInputStream = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(fileInputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            String tmp = "";
            while ((tmp = bufferedReader.readLine()) != null) {
                stringBuffer.append(tmp);
            }
            bufferedReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
        }
        result = stringBuffer.toString();
        //对top转化为列表
        List<String> keyList = Arrays.asList(result.split(","));
        //通过元数据列表与TOP200列表数据对比生成特征值列表
        System.out.println("*********************正在生成特征值列表*********************");
        ArrayList<double[]> spamNumList = getCharacter(spamMailList, keyList);
        ArrayList<double[]> hamNumList = getCharacter(hamMailList, keyList);
        //创建LabeledPoint列表装载所有的测试数据单元
        System.out.println("************************正在装载测试数据**********************");
        List<LabeledPoint> labeledPoints = new ArrayList<>();
        //垃圾邮件装载
        for (double[] arr:spamNumList){
            labeledPoints.add(new LabeledPoint(1.0,Vectors.dense(arr)));
        }
        //正常邮件装载
        for (double[] arr:hamNumList){
            //这里写0.0还是2.0
//            labeledPoints.add(new LabeledPoint(2.0,Vectors.dense(arr)));
            labeledPoints.add(new LabeledPoint(0.0,Vectors.dense(arr)));
        }
        //将LabeledPoint装华为RDD
        System.out.println("********************正在将LabeledPoint转化为RDD**********************");
        /************************这个转化了咋没用到********************************/
        RDD<LabeledPoint> labeledPointRDD = javaSparkContext.parallelize(labeledPoints).rdd();
        //加载持久化的训练模型
        System.out.println("********************正在加载模型************************");
        NaiveBayesModel naiveBayesModel = NaiveBayesModel.load(javaSparkContext.sc(), MODEL_PATH);
        //通过模型对每一封email特征值列表进行预测
        int index = 0;
        int wrong = 0;
        int TP = 0;
        int FP = 0;
        int TN = 0;
        int FN = 0;
        for (LabeledPoint labeledPoint:labeledPoints){
            double predictValue = naiveBayesModel.predict(labeledPoint.features());
            System.out.println("预测文件名为："+fileNameList[index++]);
            System.out.println("准确值:"+labeledPoint.label()+"\t预测值:"+predictValue);
            if (labeledPoint.label() == 1.0 && predictValue == 1.0){
                //TP
                TP++;
            }else if (labeledPoint.label() == 1.0 && predictValue == 0.0){
                FP++;
            }else if (labeledPoint.label() == 0.0 && predictValue == 0.0){
                TN++;
            }else if (labeledPoint.label() == 0.0 && predictValue == 1.0){
                FN++;
            }
            String type = "";
            //这里把1.0改成了2.0
            if (labeledPoint.label() == 0.0){
                type = "正常邮件";
            }else {
                type = "垃圾邮件";
            }
            if (labeledPoint.label() == predictValue){
                System.out.println("该邮件为:"+type+"\t预测准确");
            }else {
                System.out.println("该邮件为:"+type+"\t预测错误");
                wrong++;
            }
        }
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        System.out.println("======================");
        System.out.println("预测完毕！一共预测" + labeledPoints.size() + "封邮件！\r\n" +
                "预测准确：" + (labeledPoints.size() - wrong) + "封邮件！\r\n" +
                "预测错误：" + wrong + "封邮件！\r\n" +
                "预测准确率为：" + decimalFormat.format((double)(labeledPoints.size() - wrong) / labeledPoints.size()) + "！\r\n"
//                "预测错误率为：" + decimalFormat.format((double)wrong / labeledPoints.size()) + "！"
        );
        System.out.println("TP = "+TP);
        System.out.println("FP = "+FP);
        System.out.println("TN = "+TN);
        System.out.println("FN = "+FN);
        System.out.println("查准率 = "+decimalFormat.format(((double)TP/(TP+FP)) ));
        System.out.println("召回率 = "+decimalFormat.format(((double)TP/(TP+FN))));
        double precision = (double)TP/(TP+FP);
        double recall = (double)TP/(TP+FN);
        double F1 = (2*precision*recall)/(precision+recall);
        System.out.println("F1 = "+F1);
        System.out.println("==========程序结束============");


    }



    private static ArrayList<double[]> getCharacter(ArrayList<String> strList, List<String> keyList) {
        //实例化装载容器
        ArrayList<double[]> characterNumList = new ArrayList<double[]>();
        //对每一条源数据进行分词对比，生成特征值数组，然后装载进列表容器
        /*********************这里刚开始弄错了，分词不该在循环体里面*****************************************/
//        for (int i=0; i<strList.size(); i++){
//            System.out.println("********正在对第"+(i+1)+"封邮件生成特征值列表*********");
//            double[] keyNum = new double[200];
//            ArrayList<ArrayList<String>> cutList = HanlpProcess.cutWords(strList);
//            for (String s: cutList.get(0)){
////                System.out.println(s);
//                if (keyList.contains(s)){
//                    int index = keyList.indexOf(s);
//                    keyNum[index] = 1.0;
//                }
//            }
//            characterNumList.add(keyNum);
//        }
        /****************************更正*************************************************/
        ArrayList<ArrayList<String>> cutList = HanlpProcess.cutWords(strList);
        int i = 0;
        for (ArrayList<String> eachMail:cutList){
            System.out.println("********正在对第"+(i++)+"封邮件生成特征值列表*********");
            System.out.println(eachMail);
            double[] keyNum = new double[2*FEATURE_NUM];
            for (String s:eachMail){
                if (keyList.contains(s)){
                    int index = keyList.indexOf(s);
                    keyNum[index] = 1.0;
                }
            }
            characterNumList.add(keyNum);

        }
        return characterNumList;
    }
}
