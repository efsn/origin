package org.codeyn.util;

import java.security.SecureRandom;

/**
 * 生成一个唯一流水号字符串的对象,替代GUID对象
 * GUID在高并发的大批量生成的时候会有异常,
 * 这个生成UUID与jdk1.5的算法完全一致.但存在下面亮点区别:
 * 1. jdk1.5生成的uuid是带'-'连接符的.
 * 2. jdk1.5生成randomUUID()的时候,有部分冗余代码.如下
 * <pre>
 *
 * SecureRandom ng = numberGenerator;
 * if (ng == null) {
 * numberGenerator = ng = new SecureRandom();
 * }
 *
 * byte[] randomBytes = new byte[16];
 * ng.nextBytes(randomBytes);
 * randomBytes[6]  &= 0x0f;  /* clear version        *
 * randomBytes[6]  |= 0x40;  /* set to version 4     *
 * randomBytes[8]  &= 0x3f;  /* clear variant        *
 * randomBytes[8]  |= 0x80;  /* set to IETF variant  *
 * UUID result = new UUID(randomBytes);  <-- 这里创建了一次对象,但没有任何地方使用
 * return new UUID(randomBytes);
 *
 *    <pre>
 *
 * @version:
 * @author:
 */
public class UNID {

    private static volatile SecureRandom numberGenerator = null;

    /**
     * Returns val represented by the specified number of hex digits.
     */
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    public static final String randomID() {
        /**
         * @see UUID#randomUUID
         * 不用JDK的 方法是因为,jdk1.5的这个方法里面创建了两次重复的UUID对象.
         * <pre>
         * 	...
        UUID result = new UUID(randomBytes);  <-- 这里创建了一次对象,没用到
        return new UUID(randomBytes);

         * <pre>
         */
        SecureRandom ng = numberGenerator;
        if (ng == null) {
            numberGenerator = ng = new SecureRandom();
        }

        byte[] data = new byte[16];
        ng.nextBytes(data);
        data[6] &= 0x0f; /* clear version        */
        data[6] |= 0x40; /* set to version 4     */
        data[8] &= 0x3f; /* clear variant        */
        data[8] |= 0x80; /* set to IETF variant  */
        long msb = 0;
        long lsb = 0;
        assert data.length == 16;
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);

        long mostSigBits = msb;
        long leastSigBits = lsb;

        return (digits(mostSigBits >> 32, 8) + digits(mostSigBits >> 16, 4) + digits(mostSigBits, 4)
                + digits(leastSigBits >> 48, 4) + digits(leastSigBits, 12));
    }

}
