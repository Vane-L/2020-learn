package com.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author: wenhongliang
 */
public class CountOccurrenceOfWords {
    public static void main(String[] args) {
        sortByWord("Good morning. Have a good class. Have a good visit. Have fun!");
        sortByCount("Good morning. Have a good class. Have a good visit. Have fun!");
    }

    public static void sortByWord(String text) {
        TreeMap<String, Integer> map = new TreeMap<>();
        String[] words = text.split("[ \n\t\r.,;:!?()]");
        for (String word : words) {
            String key = word.toLowerCase();
            map.put(key, map.getOrDefault(key, 0) + 1);
        }
        for (Map.Entry entry : map.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    public static void sortByCount(String text) {
        HashMap<String, Integer> map1 = new HashMap<>();
        TreeMap<Integer, String> map2 = new TreeMap<>();
        String[] words = text.split("[ \n\t\r.,;:!?()]");
        for (String word : words) {
            String key = word.toLowerCase();
            map1.put(key, map1.getOrDefault(key, 0) + 1);
        }
        for (Map.Entry<String, Integer> entry : map1.entrySet()) {
            map2.put(entry.getValue(), entry.getKey());
        }

        for (Map.Entry entry : map2.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}
