package cn.edu.cuit.spamclassification.reference;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.feature.IDF;
import org.apache.spark.mllib.feature.IDFModel;
import org.apache.spark.mllib.linalg.Vector;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName TFIDF1
 * @Description TODO
 * @Author 21971
 * @Date 2021/4/4 11:08
 */
public class TFIDF1 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf();
        conf.setAppName("WordCounter").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        final HashingTF hashingTF = new HashingTF();

        JavaRDD<String> text = sc.textFile("E:\\FinalProject\\datasets\\trec06c\\hamTop100.txt");
        System.out.println(text);
        JavaRDD<Vector> tf = text.map(new Function<String, Vector>() {

            @Override
            public Vector call(String v1) throws Exception {
                return hashingTF.transform(Arrays.asList(v1.split(" ")));
            }
        });
        System.out.println(tf.collect());
        IDFModel idf = new IDF().fit(tf);

        JavaRDD<Vector> tfIdf = idf.transform(tf);

        List<Vector> list = tfIdf.collect();
        System.out.println(list);

    }
}
