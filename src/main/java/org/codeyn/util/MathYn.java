package org.codeyn.util;

public class MathYn{

    public static double roundFloat(double d, int dec){
        if (dec <= 0) {
            return Math.round(d);
        }
        double dd = Math.round(d * (Math.pow(10, dec)));
        return dd / Math.round(Math.pow(10, dec));
    }

    /**
     * 值是否满足逻辑运算符
     */
    public static boolean isValueSatisfyLogicOperator(double value, String lOp){
        boolean result = false;
        if (lOp.equals("=")) {
            result = value == 0;
        } else if (lOp.equals(">")) {
            result = value > 0;
        } else if (lOp.equals("<")) {
            result = value < 0;
        } else if (lOp.equals(">=")) {
            result = value >= 0;
        } else if (lOp.equals("<=")) {
            result = value <= 0;
        }
        return result;
    }

    /**
     * 公式两边的值是否满足逻辑运算符
     */
    public static boolean isValueSatisfyLogicOperator(double leftValue,
            double rightValue, String lOp){
        double sub = leftValue - rightValue;
        return isValueSatisfyLogicOperator(sub, lOp);
    }

}