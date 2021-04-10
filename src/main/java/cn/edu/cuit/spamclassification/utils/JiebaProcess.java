package cn.edu.cuit.spamclassification.utils;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;
import com.huaban.analysis.jieba.JiebaSegmenter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName JiebaProcess
 * @Description TODO
 * @Author 21971
 * @Date 2021/2/5 16:18
 */
public class JiebaProcess {
    public static ArrayList<ArrayList<String>> cutWords(ArrayList<String> bodyList){
        /**
         * @ClassName cutWords
         * @Description TODO
         * @Author 21971
         * @param bodyList
         * @Date 2021/1/28 11:11
         * 这种方式是吧每个邮件分词后的结果单独放，最后返回结果相当于是一个二维数组，每一维都是一个ArrayList<String>
         */
        ArrayList<ArrayList<String>> wordsList = new ArrayList<ArrayList<String>>();
        JiebaSegmenter segmenter = new JiebaSegmenter();
        for (String body: bodyList){
            //jieba分词
            System.out.println("body"+body);
            List<String> words = segmenter.sentenceProcess(body);
            System.out.println("words:"+words);

//            ArrayList<String> eachMail = new ArrayList<>();
//            for (String str:words){
//                String w = term.word;
////            System.out.println(w);
//                //这里不起作用，在processfile中进行处理
////            System.out.println("before trim:"+w+"----len:"+w.length());
////            w.replaceAll("\\s*","");
////            System.out.println(w.length());
////            System.out.println("after trim:"+w+"----len:"+w.length());
//                eachMail.add(w);
//            }
//            System.out.println("eachMail"+eachMail);
//            wordsList.add(eachMail);
        }
        return wordsList;
    }
}
