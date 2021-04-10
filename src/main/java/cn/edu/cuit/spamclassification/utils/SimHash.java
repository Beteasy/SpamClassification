package cn.edu.cuit.spamclassification.utils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @ClassName SimHash
 * @Description TODO
 * @Author 21971
 * @Date 2021/2/24 12:30
 */
public class SimHash {
    private String tokens;
    public BigInteger intSimHash;
    public String strSimHash;
    private int hashbits = 64; // simhash code的位数
    private ArrayList<String> tokenList;

    public SimHash(String tokens) {
        this.tokens = tokens;
        this.intSimHash = this.simHash();
    }

    public SimHash(String tokens, int hashbits) {
        this.tokens = tokens;
        this.hashbits = hashbits;
        this.intSimHash = this.simHash();
    }
    public SimHash(ArrayList<String> tokenList, int hashbits) {
        this.tokenList = tokenList;
        this.hashbits = hashbits;
        this.intSimHash = this.simHash();
    }
    // 获得tokens的 simhash值 整数形式 和 字符串形式
    public BigInteger simHash() {
        // 初始化一个64维的特征向量
        final int[] v = new int[this.hashbits];

        /*************my code**************/
        for (String token:this.tokenList){
            //每一个特征词
            final BigInteger t = this.hash(token);
//            System.out.println("token = "+token+"\thash = "+t);

            for (int i = 0; i < this.hashbits; i++) {
                BigInteger bitmask = new BigInteger("1").shiftLeft(i);
//                System.out.println("bitmask = "+bitmask);
                // 3、建立一个长度为64的整数数组(假设要生成64位的数字指纹,也可以是其它数字),
                // 对每一个分词hash后的数列进行判断,如果是1000...1,那么数组的第一位和末尾一位加1,
                // 中间的62位减一,也就是说,逢1加1,逢0减1.一直到把所有的分词hash数列全部判断完毕.
                if (t.and(bitmask).signum() != 0) {
                    // 这里是计算整个文档的所有特征的向量和
                    // 这里实际使用中需要 +- 权重，比如词频，而不是简单的 +1/-1，
                    v[i] += 1;
                } else {
                    v[i] -= 1;
                }
            }
//            System.out.printf("v = ");
//            for (int i:v){
//                System.out.printf("%d,",i);
//            }
//            System.out.println("");
        }


        // 根据simhash的原理，特征向量中大于0的部分，对应于hash值的那一位是1
        BigInteger fingerprint = new BigInteger("0");
        final StringBuffer simHashBuffer = new StringBuffer();
        for (int i = 0; i < this.hashbits; i++) {
            if (v[i] >= 0) {
                fingerprint = fingerprint.add(new BigInteger("1").shiftLeft(i));
                simHashBuffer.append("1");
            } else {
                simHashBuffer.append("0");
            }
        }
        this.strSimHash = simHashBuffer.toString();
        System.out.println("输出simhash值:"+this.strSimHash + " length "
                + this.strSimHash.length());
        return fingerprint;
    }

    // 这是什么hash算法？？？？能否替换成 sha-1
    private BigInteger hash(String source) {
        if (source == null || source.length() == 0) {
            return new BigInteger("0");
        } else {
            char[] sourceArray = source.toCharArray();
            BigInteger x = BigInteger.valueOf(((long) sourceArray[0]) << 7);
            BigInteger m = new BigInteger("1000003"); // 大素数
            BigInteger mask = new BigInteger("2").pow(this.hashbits).subtract(
                    new BigInteger("1"));
            for (char item : sourceArray) {
                BigInteger temp = BigInteger.valueOf((long) item);
                x = x.multiply(m).xor(temp).and(mask);
            }
            x = x.xor(new BigInteger(String.valueOf(source.length())));
            if (x.equals(new BigInteger("-1"))) {
                x = new BigInteger("-2");
            }
            return x;
        }
    }

    public int hammingDistance(SimHash other) {
        BigInteger x = this.intSimHash.xor(other.intSimHash);
        int tot = 0;

        // 统计二进制表示的x中1的个数
        // 这是个经典的算法，n&(n-1)可以每次消去最右边的1（最右边可能是一个域）
        // 我们想想，一个二进制数减去1，那么，从最后那个1（包括那个1）后面的数字全都反了，
        // 对吧，然后，n&(n-1)就相当于把后面的数字清0，
        // 我们看n能做多少次这样的操作就OK了。
        while (x.signum() != 0) {
            tot += 1;
            x = x.and(x.subtract(new BigInteger("1")));
        }
        return tot;
    }

    public int getDistance(String str1, String str2) {
        int distance;
        if (str1.length() != str2.length()) {
            distance = -1;
        } else {
            distance = 0;
            for (int i = 0; i < str1.length(); i++) {
                if (str1.charAt(i) != str2.charAt(i)) {
                    distance++;
                }
            }
        }
        return distance;
    }

    public List subByDistance(SimHash simHash, int distance) {
        // 分成几组来检查
        int numEach = this.hashbits / (distance + 1);
        List characters = new ArrayList();

        StringBuffer buffer = new StringBuffer();

        int k = 0;
        for (int i = 0; i < this.intSimHash.bitLength(); i++) {
            // 当且仅当设置了指定的位时，返回 true
            boolean sr = simHash.intSimHash.testBit(i);

            if (sr) {
                buffer.append("1");
            } else {
                buffer.append("0");
            }

            if ((i + 1) % numEach == 0) {
                // 将二进制转为BigInteger
                BigInteger eachValue = new BigInteger(buffer.toString(), 2);
                System.out.println("----" + eachValue);
                buffer.delete(0, buffer.length());
                characters.add(eachValue);
            }
        }

        return characters;
    }

//    public static void main(String[] args) {
////        String s = "This is a test string for testing";
//        ArrayList<String> list1 = new ArrayList<>();
//        list1.add("This");
//        list1.add("is");
//        list1.add("a");
//        list1.add("test");
//        list1.add("string");
//        list1.add("for");
//        list1.add("testing");
//        SimHash hash1 = new SimHash(list1, 64);
//        System.out.println(hash1.intSimHash + "  "
//                + hash1.intSimHash.bitCount());
//
//        // hash1.subByDistance(hash1, 3);
//
////        s = "This is a test string for";
//        ArrayList<String> list2 = new ArrayList<>();
//        list2.add("This");
//        list2.add("is");
//        list2.add("a");
//        list2.add("test");
//        list2.add("string");
//        list2.add("for");
//        list2.add("testing");
//        SimHash hash2 = new SimHash(list2, 64);
//        System.out.println(hash2.intSimHash + "  "
//                + hash2.intSimHash.bitCount());
//        // hash1.subByDistance(hash2, 3);
////        s = "This is a test string for testing yu";
//        ArrayList<String> list3 = new ArrayList<>();
//        list3.add("This");
//        list3.add("is");
//        list3.add("a");
//        list3.add("test");
//        list3.add("string");
//        list3.add("for");
//        list3.add("testing");
//        list3.add("yu");
//        SimHash hash3 = new SimHash(list3, 64);
//        System.out.println(hash3.intSimHash + "  "
//                + hash3.intSimHash.bitCount());
//        // hash1.subByDistance(hash3, 3);
//
//        System.out.println("============================");
//        // int dis = hash1.getDistance(hash1.strSimHash, hash2.strSimHash);
//
//        System.out.println(list1+"和"+list2+"之间的海明距离 = "+hash1.hammingDistance(hash2));
//
//        // int dis2 = hash1.getDistance(hash1.strSimHash, hash3.strSimHash);
//
////        System.out.println(hash1.hammingDistance(hash3));
//    }


}
