package org.codeyn.util;

import java.io.Serializable;
import java.util.Random;

/**
 * <p>
 * Generate a guid object, almost none of duplicate, length is always 32
 * </p>
 * 
 * @author Codeyn
 * @version 1.0
 */

public final class GUID implements Serializable{

    private static final long serialVersionUID = 3826200632731090939L;
    
    private final static byte[] RANDOMCHARS = {'Y', 'A', 'U', '8', 'K', 'V',
            'C', 'U', '6', 'D', 'N', 'E', 'I', 'F', 'L', 'J', 'U', 'K', 'M',
            'W', 'L', 'T', '4', 'I', 'N', '2', 'O', 'P', 'X', 'S', 'T', '7',
            'U', '1', 'Q', 'Y', '0', 'C', 'S', 'Z', '3', 'M', 'R', '5', 'B',
            '9'};
    private final static int[] INDEXS = {9, 17, 0, 18, 27, 3, 8, 25, 13, 11, 6,
            14, 19, 28, 30, 31, 21, 26, 15, 1, 2, 29, 16, 12, 22, 10, 7, 23,
            20, 24, 4, 5};
    
    private String guid;
    private static long counter = 1;
    private static int[] localAddr;
    
    private static String getLocalMachineCode(){
        String r = null;
        try {
            r = MacAddress.getMacAddress();
            if (r != null)
                r = r.replaceAll("-", "").replaceAll(":", "").trim();
            return r;
        } catch (Exception ex) {
            return r;
        }
    }

    static {
        long l = System.currentTimeMillis();
        String lm = getLocalMachineCode();
        if (lm == null || lm.length() == 0) {
            lm = Long.toHexString(System.currentTimeMillis());
        }
        localAddr = new int[12];
        for (int i = 0; i < 12; i++) {
            char c;
            if (i < lm.length()) {
                c = lm.charAt(i);
            } else {
                Thread.yield();
                c = (char) (System.currentTimeMillis() - l);
            }
            localAddr[i] = c;
        }
    }

    private static synchronized long getNextSerial(){
        return counter++;
    }

    static synchronized void setNextSerial(long value){
        counter = value;
    }

    public GUID(){
        this.guid = makeGuid(null);
    }

    public GUID(String id){
        this.guid = makeGuid(id);
    }

    public static final String makeGuid(){
        return makeGuid(null);
    }

    public static final String makeGuid(String id){
        byte[] buf = new byte[32];
        long ns = getNextSerial();
        long l = System.currentTimeMillis();
        int rcl = RANDOMCHARS.length;
        Random rd = new Random(l + ns + ((id != null) ? id.hashCode() : 3423));
        int st = getRandomAbsInt(rd);
        int idx = getRandomAbsInt(rd);
        // Local machine code
        for (int i = 0; i < 12; i++) {
            int b = (int) (localAddr[i] + i);
            idx = (int) ((b + idx) % rcl);
            buf[INDEXS[i]] = RANDOMCHARS[idx];
        }

        // System time
        idx = getRandomAbsInt(rd);
        for (int i = 0; i < 8; i++) {
            long b = ((l >> (i * 8)) & 0xFF);
            idx = (int) ((b + idx) % rcl);
            buf[INDEXS[i + 12]] = RANDOMCHARS[idx];
        }

        // Serial number
        idx = (int) ns;
        for (int i = 0; i < 8; i++) {
            int b = (int) (((ns) >> (i * 8)) & 0xFF);
            b = (b == 0) ? getRandomAbsInt(rd) : b + idx;
            idx = (int) ((b) % rcl);
            buf[INDEXS[i + 20]] = RANDOMCHARS[idx];
        }

        // user tags
        for (int i = 0; i < 4; i++) {
            int b;
            if (id == null || id.length() == 0) {
                b = getRandomAbsInt(rd);
            } else {
                st = getRandomAbsInt(rd);
                b = id.charAt(st % id.length());
            }
            idx = (int) ((b + idx) % rcl);
            buf[INDEXS[i + 28]] = RANDOMCHARS[idx];
        }

        return new String(buf);
    }

    private static int getRandomAbsInt(Random rd){
        /**
         * Bug: Bad attempt to compute absolute value of signed 32-bit random
         * integer Pattern id: RV_ABSOLUTE_VALUE_OF_RANDOM_INT, type: RV,
         * category: CORRECTNESS This code generates a random signed integer and
         * then computes the absolute value of that random integer. If the
         * number returned by the random number generator is Integer.MIN_VALUE,
         * then the result will be negative as well (since
         * Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE).
         */
        int r = rd.nextInt() + 1;
        r = Math.abs(r);// Math.abs(Integer.MIN_VALUE+1)==2147483647
        return r;
    }

    public int hashCode(){
        return guid.hashCode();
    }

    public boolean equals(Object obj){
        if (this == obj) {
            return true;
        }
        if (obj instanceof GUID) {
            return guid.equals(((GUID) obj).guid);
        }
        return false;
    }

    public String toString(){
        return guid;
    }
}