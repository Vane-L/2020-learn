package com;

/**
 * @Author: wenhongliang
 */
public class VersionCompareUtil {
    /**
     * 比较版本大小
     * <p>
     * 说明：支n位基础版本号+1位子版本号
     * 示例：1.0.2>1.0.1 , 1.0.1.1>1.0.1
     *
     * @param version1 版本1
     * @param version2 版本2
     * @return 0:相同 1:version1大于version2 -1:version1小于version2
     */
    public static int compareVersion(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0; //版本相同
        }
        String[] v1Array = version1.split("\\.");
        String[] v2Array = version2.split("\\.");

        int v1Len = v1Array.length;
        int v2Len = v2Array.length;
        int baseLen = Math.min(v1Len, v2Len);      //基础版本号位数（取长度小的）

        for (int i = 0; i < baseLen; i++) {       //基础版本号比较
            if (!v1Array[i].equals(v2Array[i])) {  //同位版本号相同
                return Integer.parseInt(v1Array[i]) - Integer.parseInt(v2Array[i]);
            }
        }
        //基础版本相同，再比较子版本号
        if (v1Len != v2Len) {
            return v1Len - v2Len;
        } else {
            //基础版本相同，无子版本号
            return 0;
        }
    }
}
