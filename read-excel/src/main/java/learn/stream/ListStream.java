package learn.stream;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class ListStream {
    public static void main(String[] args) {
        List<String> str1 = Arrays.asList("a", "a", "a", "a", "b");
        boolean aa = str1.stream().anyMatch(str -> str.equals("a"));
        boolean bb = str1.stream().allMatch(str -> str.equals("a"));
        boolean cc = str1.stream().noneMatch(str -> str.equals("a"));
        long count = str1.stream().filter(str -> str.equals("a")).count();
        System.out.println(aa);    // TRUE
        System.out.println(bb);    // FALSE
        System.out.println(cc);    // FALSE
        System.out.println(count); // 4

        List<String> str2 = Arrays.asList("b", "b", "c", "c", "b");
        boolean aa2 = str2.stream().anyMatch(str -> str.equals("a"));
        boolean bb2 = str2.stream().allMatch(str -> !str.equals("a"));
        boolean cc2 = str2.stream().noneMatch(str -> str.equals("a"));
        long count2 = str2.stream().filter(str -> str.equals("a")).count();
        System.out.println(aa2);    // TRUE
        System.out.println(bb2);    // FALSE
        System.out.println(cc2);    // FALSE
        System.out.println(count2); // 4
    }
}
