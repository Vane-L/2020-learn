package learn.stream;


import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * @Author: wenhongliang
 */
public class Concurrent {
    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(Integer.MAX_VALUE / 1024 / 1024);
        System.out.println(10 * 1024 * 1024 / 8);
        System.out.println(Integer.MAX_VALUE - (10 * 1024 * 1024 / 8));

        System.out.println("中文".getBytes(StandardCharsets.UTF_8).length);
        System.out.println("中文".getBytes("GBK").length);

        String str = new String("ABC") + "ABC";
        System.out.println(str.intern() == str);
        String str2 = "ABCABC"; //在常量池创建了"ABCABC"字面量实例，str2指向该实例
        String str1 = new String("ABC") + "ABC"; //在堆中得到一个合并的String("ABCABC")对象，str1指向它
        System.out.println(str1.intern() == str1); //intern()方法在常量池能找到"ABCABC"常量对象，直接返回它的引用，也就是str2，所以str1.intern() != str1
        System.out.println(str1.intern() == str2); //str1.intern()和str2指向同一个对象
        System.out.println(str1 == str2); // str1和str2指向不同对象
        
    }
}
