package com.liang.string;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
public class StringProblem {
    public static void main(String[] args) {
        StringProblem sp = new StringProblem();
        System.out.println(sp.balancedStringSplit("LLLLRRRR"));
    }

    //输入：s = "RLRRLLRLRL"
    //输出：4
    //解释：s 可以分割为 "RL", "RRLL", "RL", "RL", 每个子字符串中都包含相同数量的 'L' 和 'R'。
    public int balancedStringSplit(String s) {
        int count = 0;
        int res = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == 'L') {
                count++;
            } else {
                count--;
            }
            if (count == 0) {
                res++;
            }
        }
        return res;
    }

    //输入：paths = [["B","C"],["D","B"],["C","A"]]
    //输出："A"
    //解释：所有可能的线路是：
    //"D" -> "B" -> "C" -> "A". 
    //"B" -> "C" -> "A". 
    //"C" -> "A". 
    //"A". 
    //显然，旅行终点站是 "A" 。
    public String destCity(List<List<String>> paths) {
        Map<String, String> map = new HashMap<>();
        for (List<String> list : paths) {
            map.put(list.get(0), list.get(1));
        }
        for (String s : map.keySet()) {
            if (map.get(map.get(s)) == null)
                return map.get(s);
        }
        return null;
    }

}
