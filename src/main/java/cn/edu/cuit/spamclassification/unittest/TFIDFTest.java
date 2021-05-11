package cn.edu.cuit.spamclassification.unittest;

import cn.edu.cuit.spamclassification.utils.MyTFIDF;
import cn.edu.cuit.spamclassification.utils.TFIDFSUB;
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
        mail1.add("公司");
        mail1.add("强大");
        mail1.add("美丽");
        mail2.add("公司");
        mail2.add("山河");
        mail2.add("美丽");
        mail3.add("公司");
        mail3.add("熊猫");
        mail3.add("可爱");
        keySpamWords.add(mail1);
        keySpamWords.add(mail2);
        keySpamWords.add(mail3);
        ArrayList<ArrayList<String>> keyHamWords = new ArrayList<ArrayList<String>>();
        ArrayList<String> hammail1 = new ArrayList<String>();
        ArrayList<String> hammail2 = new ArrayList<String>();
        ArrayList<String> hammail3 = new ArrayList<String>();
        hammail1.add("公司");
        hammail1.add("发票");
        hammail1.add("美丽");
        hammail2.add("彩票");
        hammail2.add("山河");
        hammail2.add("中奖");
        hammail3.add("赚钱");
        hammail3.add("富裕");
        hammail3.add("合法");
        keyHamWords.add(hammail1);
        keyHamWords.add(hammail2);
        keyHamWords.add(hammail3);
        System.out.println("计算TF");
        Map<String, Integer> spamtf = TFIDFSUB.tfCalculate(keySpamWords);
        Map<String, Integer> hamtf = TFIDFSUB.tfCalculate(keyHamWords);
        System.out.println("优化前TF");
        System.out.println(spamtf);
        System.out.println(hamtf);
        System.out.println("优化后TF");
        TFIDFSUB.TF_SUB(hamtf,spamtf);
        System.out.println(spamtf);
        System.out.println(hamtf);
        System.out.println("计算IDF");
        Map<String, Float> spamIDF = TFIDFSUB.IDFCalculate(spamtf, keySpamWords, 3);
        Map<String, Float> hamIDF = TFIDFSUB.IDFCalculate(hamtf, keyHamWords, 3);
        System.out.println("优化前IDF");
        System.out.println(spamIDF);
        System.out.println(hamIDF);
        System.out.println("优化后IDF");
        TFIDFSUB.IDFSub(hamIDF,spamIDF);
        System.out.println(spamIDF);
        System.out.println(hamIDF);
        Map<String, Float> hamTFIDF = TFIDFSUB.TFIDFCalculate(hamtf, hamIDF);
        Map<String, Float> spamTFIDF = TFIDFSUB.TFIDFCalculate(spamtf, spamIDF);
        System.out.println("最终TDF");
        System.out.println(spamTFIDF);
        System.out.println(hamTFIDF);


//        List<Map.Entry<String, Float>> hamList = new ArrayList<Map.Entry<String, Float>>(tfidf.entrySet());
//        Collections.sort(hamList, new Comparator<Map.Entry<String, Float>>() {
//            @Override
//            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
//                return o2.getValue().compareTo(o1.getValue());
//            }
//        });
//
//        for(Map.Entry<String,Float> m : hamList){
//            System.out.println(m.getKey()+"="+m.getValue());
//        }
    }
}
