package com.meta.zoom.wallet.util;


/**
 * 底层算法
 *
 * @author rainking
 */
public class Arithmetic {


    /**
     * byte数组转16进制
     *
     * @param data byte[]
     * @return String
     */
    public static String encodeHex(byte[] data) {
        char[] ts = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters from the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = ts[(0xF0 & data[i]) >>> 4];
            out[j++] = ts[0x0F & data[i]];
        }
        return new String(out);
    }
}
