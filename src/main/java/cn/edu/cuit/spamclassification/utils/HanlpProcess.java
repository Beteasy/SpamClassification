package cn.edu.cuit.spamclassification.utils;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName HanlpProcess
 * @Description TODO
 * @Author 21971
 * @Date 2021/1/28 10:48
 */
public class HanlpProcess {
//    public static ArrayList<String> cutWords(ArrayList<String> bodyList){
//        /**
//         * @ClassName cutWords
//         * @Description TODO
//         * @Author 21971
//         * @param bodyList
//         * @Date 2021/1/28 11:11
//         * 这种方式是吧所有邮件分词后的结果放到一起
//         */
//        ArrayList<String> wordsList = new ArrayList<>();
//        for (String body: bodyList){
//            //NLP分词
//            //System.out.println(body);
//            List<Term> words = NLPTokenizer.segment(body);
//            //System.out.println(words);
//            //
//            for (Term term:words){
//                String word = term.word;
//                wordsList.add(word);
//            }
//        }
//        return wordsList;
//    }
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
    for (String body: bodyList){
        //NLP分词
//        System.out.println("body"+body);
        //分词后就会多出空格来了------------------实际上没多出空格，只是显示出来感觉多了
        List<Term> words = NLPTokenizer.segment(body);
//        System.out.println("words"+words);
        ArrayList<String> eachMail = new ArrayList<>();
        for (Term term:words){
            String w = term.word;
//            System.out.println(w);
            //这里不起作用，在processfile中进行处理
//            System.out.println("before trim:"+w+"----len:"+w.length());
//            w.replaceAll("\\s*","");
//            System.out.println(w.length());
//            System.out.println("after trim:"+w+"----len:"+w.length());
            eachMail.add(w);
            
        }
//        System.out.println("eachMail"+eachMail);
        wordsList.add(eachMail);
    }
    return wordsList;
}
}
