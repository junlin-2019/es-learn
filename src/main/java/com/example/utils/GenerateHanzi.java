package com.example.utils;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/19 15:21
 */
public class GenerateHanzi {

    public static String getRandomJianHan(int len)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0;i<len;i++){
            String str = null;
            int hightPos, lowPos; // 定义高低位
            Random random = new Random();
            hightPos = (176 + Math.abs(random.nextInt(39))); //获取高位值
            lowPos = (161 + Math.abs(random.nextInt(93))); //获取低位值
            byte[] b = new byte[2];
            b[0] = (new Integer(hightPos).byteValue());
            b[1] = (new Integer(lowPos).byteValue());
            try
            {
                str = new String(b, "GBk"); //转成中文
            }
            catch (UnsupportedEncodingException ex)
            {
                ex.printStackTrace();
            }
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }
    public static void main(String[] args) {
        System.out.println(getRandomJianHan(20));
    }
}
