package com.android.serialportlibrary.util;

public class StringUtils {
    public static String[] split(String str) {
        int m = str.length() / 2;
        if (m * 2 < str.length()) {
            m++;
        }
        String[] strs = new String[m];
        int j = 0;
        for (int i = 0; i < str.length(); i++) {
            if (i % 2 == 0) {//每隔两个dao
                strs[j] = "" + str.charAt(i);
            } else {
                strs[j] = strs[j] + "" + str.charAt(i);//将字符加上两个空格
                j++;
            }
        }
        return strs;
    }

}
