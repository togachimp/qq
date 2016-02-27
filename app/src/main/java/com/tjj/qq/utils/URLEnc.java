package com.tjj.qq.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class URLEnc {

    public static String encode(String rawStr) {

        String encStr = "";
        try {
            encStr = URLEncoder.encode(rawStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encStr;
    }

    public static String decode(String encStr) {

        try {
            String decStr = URLDecoder.decode(encStr, "UTF-8");
            return decStr;
        } catch (UnsupportedEncodingException e) {
            return encStr;
        }
    }
}
