package com.hycan.idn.adapter.biz;

import org.junit.Test;

/**
 * @author shichongying
 * @datetime 2023年 03月 04日 14:03
 */
public class StringFormatTest {

    private static final String PWD_URI = "/api/v1/mqtt/encrypt/%s";

    @Test
    public void test() {
        String str = String.format(PWD_URI, "12345");
        System.out.println(str);
    }

    @Test
    public void test2() {
        String str = "123";
        Object obj = str;
        System.out.println("abc" + obj + "def");
    }
}
