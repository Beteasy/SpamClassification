package cn.edu.cuit.spamclassification.unittest;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import java.util.ArrayList;

import java.util.function.Function;

/**
 * @ClassName DatasetTest
 * @Description TODO
 * @Author 21971
 * @Date 2021/2/21 21:56
 */
public class DatasetTest {
    public static void main(String[] args) {
        ArrayList<ArrayList<String>> keyWords = new ArrayList<>();
        ArrayList<String> oneList = new ArrayList<>();
        oneList.add("孔子");
        oneList.add("老总");
        oneList.add("发票");
        keyWords.add(oneList);
        ArrayList<String> twoList = new ArrayList<>();
        twoList.add("连接");
        twoList.add("点击");
        twoList.add("优惠");
        keyWords.add(twoList);
        //创建spark配置对象
        SparkConf conf = new SparkConf().setAppName("spam").setMaster("local[*]");
        //创建JavaSpark上下文对象
        JavaSparkContext javaSparkContext = new JavaSparkContext(conf);
        SparkSession sparkSession = SparkSession.builder().getOrCreate();
        JavaRDD<ArrayList<String>> arrayListJavaRDD = javaSparkContext.parallelize(keyWords);
        Seq<ArrayList<String>> tmpSeq = JavaConverters.asScalaIteratorConverter(keyWords.iterator()).asScala().toSeq();
//        sparkSession.createDataFrame();



//        Dataset dataset1 = new Dataset<>();
//        for(ArrayList<String> list:keyWords){
//            Dataset<String> dataset = sparkSession.createDataset(list, Encoders.STRING());
//        }


        HashingTF hashingTF = new HashingTF();
        /*********为什么是2000**************/
        hashingTF.setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(2000);
//        Dataset<Row> rowDataset = hashingTF.transform();
//        rowDataset.show();
    }
}
