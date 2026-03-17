package com.cheersai.nexus.security;

import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

import static com.cheersai.nexus.security.ConvertUtils.getHexString;

public class SM4Utils {

    /**
     * SM4加密
     *
     * @param str 参数
     * @param key 随机码
     */
    public static String EncryptStr(String str, String key) throws Exception {
        if (str == null || key == null) {
            return "0";
        }

        int length = str.length();
        str = str + "\0".repeat((16 - length % 16));

        byte[] context = new SM4Context().EncryptByte(str.getBytes(), key.getBytes());
        if (context == null) {
            return "1";
        }
        System.out.println(getHexString(context));
        return getHexString(context);
    }

    /**
     * 解密
     *
     * @param cipherStrings 密文
     * @param keyStr        随机码
     */
    public static String DecryptStr(String cipherStrings, String keyStr) {
        String result = "";
        SM4Context aa = new SM4Context();
        byte[] dd;

        byte[] cipherText = Hex.decode(cipherStrings);
        byte[] keyBytes = keyStr.getBytes();

        try {

            System.out.println();
            System.out.println("It's a new version.");
            System.out.println();

            dd = aa.DecryptStrByte(cipherText, keyBytes);

            result = new String(dd, StandardCharsets.UTF_8);
            System.out.println("result=" + result);
            char[] charArray = result.toCharArray();

            System.out.print("charArray:");
            System.out.println(charArray);
            System.out.println("Length:" + charArray.length);

            if (charArray.length > 0) {
                for (int i = charArray.length - 1; i > -1 && charArray[i] <= 16; i--) {
                    charArray[i] = ' ';
                    System.out.println(charArray);
                }
            }

            return String.valueOf(charArray).trim();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
