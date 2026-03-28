package com.cheersai.nexus.common.security;

import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

import static com.cheersai.nexus.common.security.ConvertUtils.getHexString;

public class SM4Utils {

    /**
     * SM4加密
     *
     * @param str 参数
     * @param key 随机码
     */
    public static String EncryptStr(String str, String key) throws Exception {
        if (str == null || key == null) {
            throw new IllegalArgumentException("密文或密钥不能为空");
        }

        int length = str.length();
        str = str + "\0".repeat((16 - length % 16));

        byte[] context = new SM4Context().EncryptByte(str.getBytes(), key.getBytes());
        if (context == null) {
            throw new RuntimeException("SM4加密失败");
        }
        return getHexString(context);
    }

    /**
     * 解密
     *
     * @param cipherStrings 密文
     * @param keyStr        随机码
     */
    public static String DecryptStr(String cipherStrings, String keyStr) {
        SM4Context sm4Context = new SM4Context();

        byte[] cipherText = Hex.decode(cipherStrings);
        byte[] keyBytes = keyStr.getBytes();

        try {
            byte[] decrypted = sm4Context.DecryptStrByte(cipherText, keyBytes);

            String result = new String(decrypted, StandardCharsets.UTF_8);
            char[] charArray = result.toCharArray();

            // 去除末尾的 null 字符 padding
            if (charArray.length > 0) {
                for (int i = charArray.length - 1; i > -1 && charArray[i] <= 16; i--) {
                    charArray[i] = ' ';
                }
            }

            return String.valueOf(charArray).trim();
        } catch (Exception e) {
            throw new RuntimeException("SM4解密失败: " + e.getMessage(), e);
        }
    }
}
