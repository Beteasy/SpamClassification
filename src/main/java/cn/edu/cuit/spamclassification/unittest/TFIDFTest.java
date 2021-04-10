package cn.edu.cuit.spamclassification.unittest;

import cn.edu.cuit.spamclassification.utils.MyTFIDF;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * @ClassName TFIDFTest
 * @Description TODO
 * @Author 21971
 * @Date 2021/4/8 15:30
 */
public class TFIDFTest {

    @Test
    void tfIDFTest(){
        ArrayList<ArrayList<String>> keySpamWords = new ArrayList<ArrayList<String>>();
        ArrayList<String> mail1 = new ArrayList<String>();
        ArrayList<String> mail2 = new ArrayList<String>();
        ArrayList<String> mail3 = new ArrayList<String>();
        mail1.add("中国");
        mail1.add("强大");
        mail1.add("美丽");
        mail2.add("中国");
        mail2.add("山河");
        mail2.add("美丽");
        mail3.add("中国");
        mail3.add("熊猫");
        mail3.add("可爱");
        keySpamWords.add(mail1);
        keySpamWords.add(mail2);
        keySpamWords.add(mail3);
        Map<String, Float> tf = MyTFIDF.tfCalculate(keySpamWords);
        Map<String, Float> tfidf = MyTFIDF.tfIdfCalculate(tf, keySpamWords, 3);
        System.out.println(tf);
        System.out.println(tfidf);

        List<Map.Entry<String, Float>> hamList = new ArrayList<Map.Entry<String, Float>>(tfidf.entrySet());
        Collections.sort(hamList, new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        for(Map.Entry<String,Float> m : hamList){
            System.out.println(m.getKey()+"="+m.getValue());
        }
    }
}
