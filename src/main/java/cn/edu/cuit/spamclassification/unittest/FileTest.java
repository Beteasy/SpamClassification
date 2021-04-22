package cn.edu.cuit.spamclassification.unittest;

import cn.edu.cuit.spamclassification.excutor.MySpamTrainSVMWithSGD;
import cn.edu.cuit.spamclassification.excutor.SpamPredict;
import cn.edu.cuit.spamclassification.excutor.SpamPredictSVMWithSGD;
import cn.edu.cuit.spamclassification.excutor.SpamPredict_Simhash;
import cn.edu.cuit.spamclassification.utils.HanlpProcess;
import cn.edu.cuit.spamclassification.utils.ProcessFile;
import cn.edu.cuit.spamclassification.utils.RemoveStopWords;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @ClassName Test
 * @Description TODO
 * @Author 21971
 * @Date 2021/1/26 15:01
 */
public class FileTest {
    @Test
    void readFileTest(){
//        ArrayList<String> mailList = ProcessFile.readFiles("E:\\FinalProject\\datasets\\trec06c\\data\\000");
//        ArrayList<String> mailList = ProcessFile.readFiles("C:\\Users\\21971\\Desktop\\test");
//        ArrayList<String> mailList = ProcessFile.readFile("E:\\FinalProject\\datasets\\trec06c\\data\\000");
//        System.out.println(mailList);
//        ArrayList<String> bodyList = ProcessFile.getBody(mailList);
//        ArrayList<String> wordList = HanlpProcess.cutWords(bodyList);
//        try {
//            ArrayList<String> keyWordsList = RemoveStopWords.getKeyWordsList(wordList);
//            System.out.println(keyWordsList);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



//        //test function-> ProcessFile.getTypeAndPath
//        String fullPath = "E:\\FinalProject\\datasets\\trec06c\\full\\index";
//        Map<String, String> typeAndPathMap = ProcessFile.getTypeAndPath(fullPath);
//        Iterator<Map.Entry<String,String>> it = typeAndPathMap.entrySet().iterator();
//        System.out.println(typeAndPathMap.get("../data/000/000"));



        //test function: ProcessFile.readFile


    }


    @Test
    void preTest(){
        String email = "正规發剽【驗证后付款】有保障。\n" +
                "                   \n" +
                "\n" +
                "           電话微信：1987-4764-060  陈\n" +
                "                         \n" +
                "                        酒店住宿、餐饮、建材等等都有\n" +
                "\n" +
                " \n" +
                "\n" +
                " \n" +
                "\n" +
                "a touch of her fan on my cheek; could I not understand！ Was I still such a child， Must I be broken more harshly in to learn to give place! That was all.\n" +
                "還如一夢中彭游\n" +
                "能解连环";
        SpamPredict.textCheck(email);
    }


    @Test
    void typeAndPathTest(){
        Map<String, String> typeAndPath = ProcessFile.getTypeAndPath("E:\\FinalProject\\datasets\\trec06c\\full\\index");
        for (Map.Entry<String,String> entry:typeAndPath.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key);
        }
//        ProcessFile.getTypeAndPath("E:\\FinalProject\\datasets\\trec06c\\full\\index");
    }


    @Test
    void fileCheckTest(){
        SpamPredictSVMWithSGD.fileCheck();
    }

    @Test
    void simhashTest(){
        SpamPredict_Simhash.fileCheck();
    }


}
