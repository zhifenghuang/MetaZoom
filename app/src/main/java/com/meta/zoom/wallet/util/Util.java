package com.meta.zoom.wallet.util;

/**
 * 常用工具类
 *
 * @author rainking
 */
public final class Util {

    /**
     * 判断是否非空
     *
     * @param str 内容
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}
