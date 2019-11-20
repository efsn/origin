package org.codeyn.util;

import org.codeyn.util.i18n.I18N;
import org.codeyn.util.random.SecurityRandom;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>武汉新连线科技有限公司</p>
 *
 * @author yk
 * @version 5.0
 */

public class MathUtil {

    /**
     * 该变量用于表示在进行归并排序时最小的数组划分大小。归并排序中当子数组的大小大于该值时，
     * 算法将数组分成两个较小的部分然后递归的进行排序；当排序的数组大小小于该值时，算法直接采用
     * 简单的选择排序或者插入排序对数组进行排序。
     */
    private static final int SORT_SIZE = 8;

    /**
     * 计算浮点数组元素的和
     *
     * @param values 需要计算元素和的浮点数组
     * @param size   参与计算元素和的元素个数
     * @return 指定个数的浮点数组元素的和;若参数为null,则返回{@link Double#NaN}
     */
    public final static double sum(double[] values, int size) {
        if (values == null) {
            return Double.NaN;
        }

        if (size == 0) {
            return 0;
        }
        double r = 0;
        for (int i = 0; i < size; i++) {
            double v = values[i];
            if (!Double.isNaN(v)) {
                r += v;
            }
        }
        return r;
    }

    /**
     * 计算浮点数组元素的最小值
     *
     * @param values 需要计算元素最小值的浮点数组,数组中的{@link Double#NaN}元素将不参与计算最小值
     * @param size   参与计算最小值的元素个数
     * @return 指定个数的浮点数组元素的最小值;若size==0或者指定的数组元素中非{@link Double#NaN}元素的
     * 个数为零,则返回{@link Double#NaN}
     */
    public final static double min(double[] values, int size) {
        if (values == null || size == 0) {
            return Double.NaN;
        }

        double r = Double.POSITIVE_INFINITY;
        for (int i = 0; i < size; i++) {
            double v = values[i];
            if (!Double.isNaN(v) && r > v) {
                r = v;
            }
        }
        return r == Double.POSITIVE_INFINITY ? Double.NaN : r;
    }

    /**
     * 计算浮点数组元素的最大值
     *
     * @param values 需要计算元素最大值的浮点数组,数组中的{@link Double#NaN}元素将不参与计算最大值
     * @param size   参与计算最大值的元素个数
     * @return 指定个数的浮点数组元素的最大值;若size==0或者指定的数组元素中非{@link Double#NaN}元素的
     * 个数为零,则返回{@link Double#NaN}
     */
    public final static double max(double[] values, int size) {
        if (values == null || size == 0) {
            return Double.NaN;
        }

        double r = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < size; i++) {
            double v = values[i];
            if (!Double.isNaN(v) && r < v) {
                r = v;
            }
        }
        return r == Double.NEGATIVE_INFINITY ? Double.NaN : r;
    }

    /**
     * 获取一个数组中绝对值最大的元素的绝对值
     */
    public final static double amax(double[] values) {
        if (values == null) {
            return Double.NaN;
        }

        double amax = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < values.length; i++) {
            double v = Math.abs(values[i]);
            if (!Double.isNaN(v) && amax < v) {
                amax = v;
            }
        }

        return amax == Double.NEGATIVE_INFINITY ? Double.NaN : amax;
    }

    /**
     * 获取一个二维数组中绝对值最大的元素的绝对值
     */
    public final static double amax(double[][] values) {

        if (values == null) {
            return 0;
        }

        double amax = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < values.length; i++) {
            double v = amax(values[i]);
            if (!Double.isNaN(v) && amax < v) {
                amax = v;
            }
        }

        return amax == Double.NEGATIVE_INFINITY ? Double.NaN : amax;
    }

    /**
     * 计算浮点数组元素的平均值
     *
     * @param values    需要计算元素平均的浮点数组
     * @param size      参与计算平均值的元素个数
     * @param ignoreNan 值为{@link Double#NaN}是否参与计算元素计数标记;当其值为{@link <code>true</code>}时，忽略
     *                  值为{@link Double#NaN}的数组元素，否则，值为{@link Double#NaN}的元素将参与元素计数
     * @return 指定个数的浮点数组元素的平均值;若size==0或者指定的数组元素中非{@link Double#NaN}元素的
     * 个数为零,则返回{@link Double#NaN}
     */
    public final static double avg(double[] values, int size, boolean ignoreNan) {
        if (values == null || size == 0) {
            return Double.NaN;
        }
        int cnt = 0;
        double sum = 0;
        for (int i = 0; i < size; i++) {
            double v = values[i];
            if (!Double.isNaN(v)) {
                cnt++;
                sum += v;
            } else if (!ignoreNan) {
                cnt++;
            }
        }
        return cnt == 0 ? Double.NaN : sum / cnt;
    }

    /**
     * 标准差
     * if(_N(x)=1,0,sqrt(_S(power(x - _A(x),2))/(_N(x)-1)))
     */
    public final static double stddev(double[] values, int size, boolean ignoreNan) {
        if (size <= 1) {
            return Double.NaN;
        }
        double ss = 0;
        double ss1 = 0;
        int cnt = 0;
        for (int i = 0; i < size; i++) {
            double v = values[i];
            if (!Double.isNaN(v)) {
                ss += v * v;
                ss1 += v;
                cnt++;
            } else if (!ignoreNan) {
                cnt++;
            }
        }
        if (cnt <= 1) {
            return Double.NaN;
        }
        return Math.sqrt((cnt * ss - ss1 * ss1) / (cnt * (cnt - 1)));
        // 下面的方法是等价的。
        //    if (size <= 1) {
        //      return Double.NaN;
        //    }
        //    double avg = avg(values, size, ignoreNan);
        //    if (Double.isNaN(avg))
        //      return avg;
        //
        //    double ss = 0;
        //    int cnt = 0;
        //    for (int i = 0; i < size; i++) {
        //      double v = values[i];
        //      if (!Double.isNaN(v)) {
        //        ss += (v - avg) * (v - avg);
        //        cnt++;
        //      }
        //      else if (!ignoreNan) {
        //        cnt++;
        //      }
        //    }
        //    if (cnt <= 1) {
        //      return Double.NaN;
        //    }
        //    return Math.sqrt(ss / (cnt - 1));
    }

    /*
     * 计算总体标准差
     *
     * @param values 需要计算总体标准差的数据
     * @param size 参与计算总体标准差的数据个数
     * @param ignoreNan 在计算过程中值为NaN的数据是否参与计算数据个数
     * @return 给定数据的总体标准差
     */
    public final static double stdevp(double[] values, int size, boolean ignoreNan) {

        if (size == 0 || values == null) {
            return Double.NaN;
        }

        double ss = 0;
        double ss1 = 0;
        int cnt = 0;
        for (int i = 0; i < size; i++) {
            double v = values[i];
            if (!Double.isNaN(v)) {
                ss += v * v;
                ss1 += v;
                cnt++;
            } else if (!ignoreNan) {
                cnt++;
            }
        }

        if (cnt == 0) {
            return Double.NaN;
        }

        return Math.sqrt(ss / cnt - (ss1 / cnt) * (ss1 / cnt));
    }

    /**
     * /**
     * 平均偏差
     * if(_N()=0,null,_S(abs(zb-_A(zb)))/_N())
     */
    public final static double avedev(double[] values, int size, boolean ignoreNan) {
        if (size <= 0) {
            return Double.NaN;
        }
        double avg = avg(values, size, ignoreNan);
        if (Double.isNaN(avg))
            return avg;

        double ss = 0;
        int cnt = 0;
        for (int i = 0; i < size; i++) {
            double v = values[i];
            if (!Double.isNaN(v)) {
                ss += Math.abs(v - avg);
                cnt++;
            } else if (!ignoreNan) {
                ss += Math.abs(avg);
                cnt++;
            }
        }
        if (cnt == 0) {
            return Double.NaN;
        }
        return ss / cnt;
    }

    /**
     * 相关系数
     * (_S(x*y)-_S(x)*_S(y)/_N(x*y))/(sqrt(_S(x*x)-_S(x)*_S(x)/_N(x))*sqrt(_S(y*y)-_S(y)*_S(y)/_N(y)))
     * IF((sqrt(_S(x*x)-_S(x)*_S(x)/_N(x))*sqrt(_S(y*y)-_S(y)*_S(y)/_N(y)))*_N(x)*_N(y)*_N(x*y)=0,0,(_S(x*y)-_S(x)*_S(y)/_N(x*y))/(sqrt(_S(x*x)-_S(x)*_S(x)/_N(x))*sqrt(_S(y*y)-_S(y)*_S(y)/_N(y))))
     *
     * @deprecated 该函数的第二个参数和第四个参数重复了，建议使用函数{@link #corr(double[], double[], int)}代替该方法
     */
    public final static double correl(double[] values1, int size1, double[] values2, int size2) {
        if (values1 == null || values2 == null ||
                values1.length < size1 || values2.length < size2 || size1 <= 0 || size1 != size2) {
            return Double.NaN;
        }
        double sumxy = 0;
        double sumx = 0;
        double sumy = 0;
        double sumxx = 0;
        double sumyy = 0;
        int cnt = 0;
        for (int i = 0; i < size1; i++) {
            double x = values1[i];
            double y = values2[i];
            if (Double.isNaN(x) || Double.isNaN(y)) {
                continue;
            }
            sumxy += x * y;
            sumx += x;
            sumy += y;
            sumyy += y * y;
            sumxx += x * x;
            cnt++;
        }

        double fenmu = Math.sqrt(sumxx - sumx * sumx / cnt) * Math.sqrt(sumyy - sumy * sumy / cnt);
        if (fenmu == 0) {
            return Double.NaN;
        }
        return (sumxy - sumx * sumy / cnt) / fenmu;
    }

    /**
     * 计算两个序列的相关系数
     *
     * @param values1 计算相关系数的第一个序列
     * @param values2 计算相关系数的第二个序列
     * @param length  参与计算相关系数的数据个数
     * @return 序列的相关系数
     */
    public final static double corr(double[] values1, double[] values2, int length) {

        if (values1 == null || values2 == null || length < 0 || values1.length < length || values2.length < length) {
            return Double.NaN;
        }

        return correl(values1, length, values2, length);
    }

    /**
     * 类似excel中的var函数，计算基于给定样本的方差。
     */
    public static double var(double[] values, int size, boolean ignoreNan) {
        if (values == null || size <= 0) {
            return Double.NaN;
        }
        double ss = 0, s = 0;
        int cnt = 0;
        for (int i = 0; i < size; i++) {
            double v = values[i];
            if (!Double.isNaN(v)) {
                ss += v * v;
                s += v;
                cnt++;
            } else if (!ignoreNan) {
                cnt++;
            }
        }

        if (cnt <= 1)
            return Double.NaN;

        return (cnt * ss - s * s) / (cnt * (cnt - 1));
    }

//  /**
//   * 四舍五入，保留指定小数位数。
//   * 20090107 如果浮点数是NaN，则返回NaN。注意不判断NaN时，本函数返回0.
//   * 20090525 如果浮点数是无穷大，则返回无穷大。注意如果不判断无期大，本函数返回9E16。
//   * @param x
//   * @param declen 小数位数
//   * @return
//   */
//  public static double round(double d, int decLen){
//    if (Double.isNaN(d))
//      return Double.NaN;
//    if (Double.isInfinite(d))
//      return d;
//    
//		/**
//		 * 参照excel标准
//		 * MathUtil.round(-0.5)=-1
//		 * MathUtil.round(-1.5,0)=-2
//		 * MathUtil.round(-1.4,0)=-1
//		 * MathUtil.round(123,-1)=120
//		 * MathUtil.round(-123,-1)=-120
//		 * MathUtil.round(125,-1)=130
//		 * MathUtil.round(-125,-1)=-130
//		 * 
//		 */
//  
//
//    /**
//     * BI-6333
//     * easyolap，显示数据不正确
//     * 在数据显示中小数位改成2 ，数据能正常显示 
//     * 
//     * 解决：原方法在处理大数字大精度的时候，会出现超出Long.MAX_VALUE的情况，导致数据出错。
//     * 如：MathUtil.round(274290863842.08,10)，会返回long的最大值
//     * 修改为使用BigDecimal来对大于Long.MAX_VALUE的进行精度运算
//     * 20120314 by baochl
//     * 
//     * 
//     */
//    if (d < 0) {
//		if (decLen < 0) {
//			double p = Math.pow(10.0, -decLen);
//			return -Math.round(-d / p) * p;
//		}
//		double p = Math.pow(10.0, decLen);
//		double r = -d * p;
//		if(r > Long.MAX_VALUE){
//			return round_BigDecimal(d, decLen);
//		}
//		return -Math.round(r) / p;
//	}
//	else {
//		if (decLen < 0) {
//			double p = Math.pow(10.0, -decLen);
//			return Math.round(d / p) * p;
//		}
//		double p = Math.pow(10.0, decLen);
//		double r = d * p;
//		if(r > Long.MAX_VALUE){
//			return round_BigDecimal(d, decLen);
//		}
//		return Math.round(r) / p;
//	}
//    
//    
//  }

    /**
     * 采样同Excel一样的舍入函数算法。
     *
     * @param d
     * @param decLen
     * @return
     */
    public static double round(double d, int decLen) {
        if (Double.isNaN(d))
            return Double.NaN;
        if (Double.isInfinite(d))
            return d;
        return _excelRound(d, decLen);
    }

    /**
     * 同Excel一样算法的Round函数。
     * 之前的MathUtil.round()方法算出来的结果跟excel有出入。
     * 另外StrFunc.round()方法超级大数round后会变成long最大值。
     * 现在统一改为用excel round算法。
     * 此算法参考链接: http://stackoverflow.com/questions/6930786/how-does-excel-successfully-rounds-floating-numbers-even-though-they-are-impreci
     * 相关JIRA: BI-6470, BI-6679,
     */
    //Round the same way Excel does.
    //Dealing with nonsense such as nplaces=400 is an exercise left to the reader.
    private static double _excelRound(final double d, int nplaces) {
        boolean is_neg = false;
        double x = d;
        // Excel uses symmetric arithmetic round: Round away from zero.
        // The algorithm will be easier if we only deal with positive numbers.
        if (x < 0.0) {
            is_neg = true;
            x = -x;
        }

        // Construct the nearest rounded values and the nasty corner case.
        // Note: We really do not want an optimizing compiler to put the corner
        // case in an extended double precision register. Hence the volatile.
        double round_down, round_up;
        double corner_case;
        if (nplaces < 0) {
            double scale = _pow10(-nplaces);
            round_down = Math.floor(x / scale);
            corner_case = (round_down + 0.5) * scale;
            round_up = (round_down + 1.0) * scale;
            round_down *= scale;
        } else {
            double scale = _pow10(nplaces);
            round_down = Math.floor(x * scale);
            corner_case = (round_down + 0.5) / scale;
            round_up = (round_down + 1.0) / scale;
            round_down /= scale;
        }

        // Round by comparing to the corner case.
        x = (x < corner_case) ? round_down : round_up;

        /************************
         * BEGIN: 修正精度误差 . BI-7579, by chxb 2012/12/13
         * 由于round算法的基本思路是将原数放大10^nplaces次方，会造成算出来的数在超过16位精度时多出误差，需要额外去除这个误差
         */
        int intLen = (int) (Math.log(d) / Math.log(10) + 1);//求整数部分长度
        double derror = x - d;//实际误差
        //当整数位数intLen+nplaces>=16时，会多出精度误差，要消除它
        x = (intLen + nplaces >= 16 && x != d) ? x - derror : x;
        /************************/

        // Correct the sign if needed.
        if (is_neg)
            x = -x;

        return x;
    }

    //Compute 10 to some positive integral power.
    //Dealing with overflow (exponent > 308) is an exercise left to the reader.
    private static double _pow10(int exponent) {
        double result = 1.0;
        double base = 10.0;
        while (exponent > 0) {
            if ((exponent & 1) != 0)
                result *= base;
            exponent >>= 1;
            base *= base;
        }
        return result;
    }


    private static double round_BigDecimal(double d, int decLen) {
        BigDecimal bigD = new BigDecimal(d);
        if (decLen < 0) {
            double tempValue = bigD.movePointLeft(-decLen).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
            bigD = (new BigDecimal(tempValue)).movePointRight(-decLen);
        } else {
            bigD = bigD.setScale(decLen, BigDecimal.ROUND_HALF_UP);
        }
        return bigD.doubleValue();
    }

    /**
     * Returns the integer nearest to "value" parameter
     *
     * @param value double
     * @return int
     */
    public static int round(double value) {
        return (int) round(value, 0);
    }

    /**
     * 数据按指定位数进行截尾处理
     *
     * @param value 需要进行截尾处理的数据
     * @param len   指定的需要做截尾处理的小数位
     * @return 截尾处理后的数据
     */
    public final static double trunc(double value, int len) {

        if (Double.isNaN(value)) {
            return Double.NaN;
        }

        if (Double.isInfinite(value)) {
            return value;
        }

        double p = Math.pow(10, len);
        if (value > 0) {
            return (Math.floor(value * p) / p);
        } else if (value < 0) {
            return -(Math.floor(-value * p) / p);
        } else {
            return 0;
        }
    }

    /**
     * 等价于jdk1.5的Math.log10。
     * 2011-9-7 修改MathUtil.log10的实现，使结果更精确，将返回值类型修改为double
     *
     * @param length
     * @return
     */
    public static double log10(double length) {
        //Math.log(10)==2.302585092994046
        //return (int) (Math.log(length) / 2.302585092994046);计算有错误

        if (Double.isNaN(length) || length <= 0) {
            return Double.NaN;
        }

        final double delta = length * 1.0e-14;
        double log_right = Math.log(length) / 2.302585092994046 + 1;
        double log_left = Math.log(length) / 2.302585092994046 - 1;
        int count = 1;
        do {
            double log10 = (log_right + log_left) / 2.0;
            if (Math.pow(10, log10) > length) {
                log_right = log10;
            } else {
                log_left = log10;
            }
            count++;
        }
        while (count < 100 && Math.pow(10, log_right) - Math.pow(10, log_left) > delta);
        return (log_right + log_left) / 2;
    }

    /**
     * 返回一个数组的分位数，例如：中位数（p=0.5），四分之一分位数（p=0.25），四分之三分位数（p=0.75）。
     *
     * @param data 需要计算分位数的数组
     * @param p    需要计算的分位点
     * @return 返回指定的分位数
     */
    public static double quantile(double[] data, double p) {

        double[] x = data;
        if (x == null || x.length == 0 || Double.isNaN(p) || p < 0 || p > 1) {
            return Double.NaN;
        }

        x = data.clone();
        sort(x);//将数组元素从小到大进行排序

        int n = x.length;

        double pos = (n + 1) * p;

        if (pos >= n) {
            return x[n - 1];
        }
        if (pos <= 1) {
            return x[0];
        }

        int left = (int) Math.floor(pos);
        int right = (int) Math.ceil(pos);


        if (left == right) {
            return x[left - 1];
        } else {
            return x[left - 1] * (right - pos) + x[right - 1] * (pos - left);
        }
    }

    /**
     * 计算给定数据的中位数
     *
     * @param data 需要计算中位数的数据数组
     * @return 数组数据的中位数。特殊情况包括：如果传入的参数为null，
     * 则返回Double.NaN。
     */
    public static double median(double[] data) {
        return quantile(data, 0.5);
    }

    /**
     * 返回给定数据从大到小排列的前几个数据的索引，不会改变原始数组的内容
     *
     * @param data 需要计算排序之后前几个索引的数据数组
     * @param k    需要返回的索引的个数
     * @return 数据排序之后排在前K位的索引。特殊情况包括：如果传入的数据数组
     * 为null，或者k小于0，则返回一个长度为0的索引数组；如果k大于数组的长度，
     * 则返回一个长度为给定数组长度的索引数组。
     */
    public static int[] top(double[] data, int k) {

        if (data == null || k < 0) {
            return new int[0];
        }

        int[] index = index(data, null);

        if (k == data.length) {
            return index;
        }

        int[] ri = new int[k];
        System.arraycopy(index, 0, ri, 0, k);
        return ri;
    }

    /**
     * 对给定的数组进行排序（从大到小的顺序），元素值为Double.NaN的元素将排在元素值不为Double.NaN的元素后面
     *
     * @param data 需要进行排序的数组。该方法执行结束后，data的值被修改为排序之后的值。如果该
     *             参数为null，则不进行任何操作。
     */
    public static void sort(double[] data) {

        if (data == null) {
            return;
        }

        Arrays.sort(data);
    }

    /**
     * 归并排序算法（按降序排列），仅供内部使用；该算法会改变输入的索引元素顺序为排好序的顺序
     */
    private static void msort(double[] data, int begin, int len, int[] index) {

        int end = begin + len;
        int item = 0;
        int j = 0;

        if (len <= SORT_SIZE) {
            for (int i = begin + 1; i < end; i++) {
                if (Double.isNaN(data[index[i]])) {
                    continue;
                }

                item = index[i];
                for (j = i - 1; j >= begin; j--) {
                    if (Double.isNaN(data[index[j]]) || data[index[j]] < data[item]) {
                        index[j + 1] = index[j];
                        continue;
                    }
                    break;
                }

                index[j + 1] = item;
            }

            return;
        }

        int[] ti = new int[len];
        int mid = len / 2;
        msort(data, begin, mid, index);//将数组分成两个部分分别进行排序
        msort(data, begin + mid, len - mid, index);

        /**
         * 将两个排好序的子索引合并为按降序排列的索引
         */
        for (int i = 0, xi = 0, li = begin, ri = begin + mid; i < len; i++) {

            if (li < begin + mid && ri < end) {
                if (Double.isNaN(data[index[ri]])) {
                    ti[xi++] = index[li++];
                    continue;
                }

                if (Double.isNaN(data[index[li]])) {
                    ti[xi++] = index[ri++];
                    continue;
                }

                if (data[index[ri]] > data[index[li]]) {
                    ti[xi++] = index[ri++];
                } else {
                    ti[xi++] = index[li++];
                }
                continue;
            }

            if (li == begin + mid) {
                System.arraycopy(index, ri, ti, xi, len - xi);
                break;
            }

            if (ri == end) {
                System.arraycopy(index, li, ti, xi, len - xi);
            }
        }

        System.arraycopy(ti, 0, index, begin, len);
    }

    /**
     * 计算给定数组排序后（从大到小的顺序，Double.NaN元素排在非Double.NaN元素后面）的索引，该方法不会改变传入的数组数组
     *
     * @param data  需要计算索引的数组
     * @param index 用于保存索引的整型数组。
     * @return 所有元素的索引。特殊情况包括：如果传入的index数组为null，则返回新生产的索引数组；
     * 如果传入的需要计算索引的数据为null，则返回null；
     */
    public static int[] index(double[] data, int[] index) {
        if (data == null) {
            return null;
        }

        if (index == null || index.length != data.length) {
            index = new int[data.length];
        }

        for (int i = 0; i < index.length; i++) {
            index[i] = i;
        }

        msort(data, 0, data.length, index);
        return index;
    }

    /**
     * 对给定的数组进行排序（从大到小的顺序），元素值为Double.NaN的元素将排在元素值不为Double.NaN的元素后面
     *
     * @param data 需要进行排序的数组。该方法执行结束后，data的值被修改为排序之后的值。如果该
     *             参数为null，则不进行任何操作。
     */
    public static void sort(int[] data) {

        if (data == null) {
            return;
        }

        Arrays.sort(data);
    }

    /**
     * 归并排序算法（按降序排列），仅供内部使用；该算法会改变输入的索引元素顺序为排好序的顺序
     */
    private static void msort(int[] data, int begin, int len, int[] index) {

        int end = begin + len;
        int item = 0;
        int j = 0;

        if (len <= SORT_SIZE) {
            for (int i = begin + 1; i < end; i++) {
                item = index[i];
                for (j = i - 1; j >= begin; j--) {
                    if (data[index[j]] < data[item]) {
                        index[j + 1] = index[j];
                        continue;
                    }
                    break;
                }

                index[j + 1] = item;
            }

            return;
        }

        int[] ti = new int[len];
        int mid = len / 2;
        msort(data, begin, mid, index);//将数组分成两个部分分别进行排序
        msort(data, begin + mid, len - mid, index);

        /**
         * 将两个排好序的子索引合并为按降序排列的索引
         */
        for (int i = 0, xi = 0, li = begin, ri = begin + mid; i < len; i++) {

            if (li < begin + mid && ri < end) {

                if (data[index[ri]] > data[index[li]]) {
                    ti[xi++] = index[ri++];
                } else {
                    ti[xi++] = index[li++];
                }
                continue;
            }

            if (li == begin + mid) {
                System.arraycopy(index, ri, ti, xi, len - xi);
                break;
            }

            if (ri == end) {
                System.arraycopy(index, li, ti, xi, len - xi);
            }
        }

        System.arraycopy(ti, 0, index, begin, len);
    }

    /**
     * 计算给定数组排序后（从大到小的顺序）的索引，该方法不会改变传入的数组数组
     *
     * @param data  需要计算索引的数组
     * @param index 用于保存索引的整型数组。
     * @return 所有元素的索引。特殊情况包括：如果传入的index数组为null，则返回新生产的索引数组；
     * 如果传入的需要计算索引的数据为null，则返回null；
     */
    public static int[] index(int[] data, int[] index) {
        if (data == null) {
            return null;
        }

        if (index == null || index.length != data.length) {
            index = new int[data.length];
        }

        for (int i = 0; i < index.length; i++) {
            index[i] = i;
        }

        msort(data, 0, data.length, index);
        return index;
    }

    /**
     * 计算给定数组中每个元素在排序之后的秩序（从大到小的顺序，Double.NaN元素排在非Double.NaN元素后面）
     *
     * @param data 需要计算秩序的数组
     * @return 所有元素的秩序数组
     */
    public static int[] rank(double[] data) {

        if (data == null) {
            return null;
        }

        int[] index = index(data, null);
        int n = index.length;
        int[] rank = new int[n];

        for (int i = 0; i < n; i++) {
            rank[index[i]] = i;
        }

        return rank;
    }

    /**
     * 将数组中的元素按相反的顺序从新排列
     *
     * @param data 需要重新排列的数组。如果参数为null则不进行任何操作，该方法直接返回
     */
    public static void reverse(int[] data) {

        if (data == null || data.length < 2) {
            return;
        }

        int n = data.length;
        for (int i = 0; i < n / 2; i++) {
            int it = data[i];
            data[i] = data[n - 1 - i];
            data[n - 1 - i] = it;
        }
    }

    /**
     * 将数组中的元素按相反的顺序从新排列
     *
     * @param data 需要重新排列的数组。如果参数为null则不进行任何操作，该方法直接返回
     */
    public static void reverse(double[] data) {

        if (data == null || data.length < 2) {
            return;
        }

        int n = data.length;
        for (int i = 0; i < n / 2; i++) {
            double it = data[i];
            data[i] = data[n - 1 - i];
            data[n - 1 - i] = it;
        }
    }

    /**
     * 计算给定数组所有元素的平方和
     *
     * @param data 需要计算平方和的数组
     * @return 数组所有元素的和。特殊情况包括：当参数为null是返回Double.NaN；当数组中包含的非缺失数据个数为0时返回0.
     */
    public static double ssq(double[] data) {

        double[] x = data;
        if (x == null) {
            return Double.NaN;
        }

        double ssq = 0.0;
        for (int i = 0, len = x.length; i < len; i++) {
            if (Double.isNaN(x[i])) {
                continue;
            }
            ssq += x[i] * x[i];
        }

        return ssq;
    }

    /**
     * 计算给定数据中所有非缺失元素的个数
     *
     * @param data 需要计算非缺失元素个数的数组
     * @return 数组中非缺失元素的个数。特殊情况包括：如果传入的参数为null，返回0.
     */
    public static int count(double[] data) {

        int n = 0;

        double[] x = data;
        if (x == null) {
            return 0;
        }

        for (int i = 0, len = x.length; i < len; i++) {
            if (Double.isNaN(x[i])) {
                continue;
            }
            n++;
        }

        return n;
    }

    /**
     * 对给定的数组进行对数变换，并返回对数变换之后的结果，该方法不会改变传入的参数。
     *
     * @param data 需要进行对数变化的数据
     * @return 返回新生成的经过对数变换之后的结果。特殊情况包括：如果传入的参数为null，则返回
     * 长度为0的浮点型数组；如果传入的数组中包含非正浮点数或者Double.NaN，
     * 则对应的对数变换之后的结果为Double.NaN。
     */
    public static double[] tlog(double[] data) {

        double[] x = data;
        if (x == null) {
            return new double[0];
        }

        x = new double[data.length];
        for (int i = 0, len = data.length; i < len; i++) {
            x[i] = Math.log(data[i]);
        }

        return x;
    }

    /**
     * 对给定的数据数组进行指数变换,并返回指数变换之后的结果
     *
     * @param data 需要进行指数变换的数据数组
     * @return 进行指数变换之后的数据数组
     */
    public static double[] texp(double[] data) {

        if (data == null) {
            return new double[0];
        }

        double[] x = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            x[i] = Math.exp(data[i]);
        }

        return x;
    }

    /**
     * 将给定的数据等比例地变换到指定的区间[a, b]中，数组的所有元素经过变换之后会位于区间[a,b]之间。
     *
     * @param data 需要进行比例变换的数据数组
     * @param a    变换之后的区间左端点
     * @param b    变换之后的区间有端点, b应该大于a
     * @return 新生成的变换了之后的数组。特殊情况包括：如果传入的data参数为null，或者任一区间端点为
     * Double.NaN，则直接返回传入的data参数做为变换结果，不做任何变换；
     */
    public static double[] tscale(double[] data, double a, double b) {

        if (data == null) {
            return null;
        }

        double max = max(data, data.length);
        double min = min(data, data.length);
        double range = max - min;

        if (Double.isNaN(max) || Double.isNaN(min)) {
            return data;
        }

        if (Double.isNaN(a) || Double.isNaN(b) || b <= a) {
            return data;
        }

        int n = data.length;
        double[] r = new double[n];
        if (range == 0) {
            for (int i = 0; i < n; i++) {
                r[i] = (Double.isNaN(data[i]) ? Double.NaN : b);
            }
            return r;
        }

        for (int i = 0; i < n; i++) {
            r[i] = (data[i] - min) / range * (b - a) + a;
        }

        return r;
    }

    /**
     * 计算两个数组的内积:两个数组对应元素相乘并相加得到的结果,将忽略数组中为NaN的元素
     *
     * @param x 计算内积的第一个数据数组
     * @param y 计算内积的第二个数据数组
     * @return 数据的内积, 如果传入的参数中包含空指针或者两个数组的长度不相等, 则返回NaN
     */
    public static double dot(double[] x, double[] y) {
        if (x == null || y == null || x.length != y.length) {
            return Double.NaN;
        }

        double dot = 0;
        for (int i = 0; i < x.length; i++) {
            if (Double.isNaN(x[i]) || Double.isNaN(y[i])) {
                continue;
            }
            dot += x[i] * y[i];
        }

        return dot;
    }

    /**
     * 一元线性回归分析预测
     *
     * @param ys 目标变量的历史观察数据
     * @param xs 影响因素的历史观察数据
     * @param x  需要用来线性回归分析预测的新的影响因素指标数据，该数组的长度就是需要进行预测数据的数目，
     *           数组中每一个数据都对应一个利用一元线性回归分析得到的预测结果
     * @return 利用新的影响因素指标数据预测得到的预测结果数组，该数组的长度跟参数提供的新的影响因素指标数据数组长度相等
     * @throws NullPointerException     如果传入的参数中存在空指针，将抛出该异常信息
     * @throws IllegalArgumentException 如果第二个参数和第三个参数数组的长度不相等，将抛出该异常信息
     */
    public static double[] ls(double[] x, double[] ys, double[] xs) throws NullPointerException, IllegalArgumentException {

        if (x == null || ys == null || xs == null) {
            //throw new NullPointerException("传入的参数中包含空指针");
            throw new NullPointerException(I18N.getString("com.esen.util.MathUtil.1", "传入的参数中包含空指针"));
        }

        if (ys.length != xs.length) {
            //throw new IllegalArgumentException("传入的目标变量的历史观察数据数组长度和影响因素的历史观测数据长度不等，无法进行计算");
            throw new IllegalArgumentException(I18N.getString("com.esen.util.MathUtil.2", "传入的目标变量的历史观察数据数组长度和影响因素的历史观测数据长度不等，无法进行计算 "));
        }

        int len = ys.length;
        double[] yy = new double[len];
        double[] xx = new double[len];
        int n = 0;
        for (int i = 0; i < len; i++) {
            if (Double.isNaN(ys[i]) || Double.isNaN(xs[i])) {
                continue;
            }

            yy[n] = ys[i];
            xx[n] = xs[i];
            n++;
        }

        double[] ys1 = new double[n];
        double[] xs1 = new double[n];
        System.arraycopy(yy, 0, ys1, 0, n);
        System.arraycopy(xx, 0, xs1, 0, n);

        double yhat = avg(ys1, n, true);
        double xhat = avg(xs1, n, true);
        double beta = covar(ys1, xs1) / var(xs1, n, true);
        double alpha = yhat - beta * xhat;
        double[] result = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = alpha + beta * x[i];
        }

        return result;
    }

    /**
     * 计算两个指标数组的协方差
     *
     * @param x 计算协方差的第一个数据数组
     * @param y 计算协方差的第二个数据数组
     * @return 两个数组的协方差
     * @throws NullPointerException     如果参数中包含空指针,将抛出该异常信息
     * @throws IllegalArgumentException 如果传入的两个数组的长度不相等,将抛出该异常信息
     */
    public static double covar(double[] x, double[] y) throws NullPointerException, IllegalArgumentException {

        if (x == null || y == null) {
            //throw new NullPointerException("参数中包含空指针");
            throw new NullPointerException(I18N.getString("com.esen.util.MathUtil.3", "参数中包含空指针"));
        }

        if (x.length != y.length) {
            //throw new IllegalArgumentException("两个数组的长度不相等");
            throw new IllegalArgumentException(I18N.getString("com.esen.util.MathUtil.4", "两个数组的长度不相等"));
        }

        int len = x.length;
        double[] xx = new double[len];
        double[] yy = new double[len];
        int n = 0;
        for (int i = 0; i < len; i++) {
            if (Double.isNaN(x[i]) || Double.isNaN(y[i])) {
                continue;
            }

            xx[n] = x[i];
            yy[n] = y[i];
            n++;
        }

        if (n < 2) {
            return Double.NaN;
        }

        double xhat = avg(xx, n, true);
        double yhat = avg(yy, n, true);

        double covar = 0.0;
        for (int i = 0; i < n; i++) {
            covar += (xx[i] - xhat) * (yy[i] - yhat);
        }

        return covar / (n - 1);
    }

    /**
     * 一元对数线性回归预测
     *
     * @param ys 目标变量的历史观察数据
     * @param xs 影响因素的历史观察数据
     * @param x  需要用来对数线性回归分析预测的新的影响因素指标，该数组的长度就是需要进行预测数据的数目，
     *           数组中每一个数据都对应一个利用一元对数线性回归分析得到的预测结果
     * @return 利用新的影响因素指标数据预测得到的预测结果数组，该数组的长度跟参数提供的新的影响因素指标数据数组长度相等
     * @throws NullPointerException     如果传入的参数中存在空指针，将抛出该异常信息
     * @throws IllegalArgumentException 如果第二个参数和第三个参数数组的长度不相等，将抛出该异常信息
     */
    public static double[] lnls(double[] x, double[] ys, double[] xs) throws NullPointerException,
            IllegalArgumentException {

        if (x == null || ys == null || xs == null) {
            //throw new NullPointerException("传入的参数中包含空指针");
            throw new NullPointerException(I18N.getString("com.esen.util.MathUtil.5", "传入的参数中包含空指针"));
        }

        if (ys.length != xs.length) {
            //throw new IllegalArgumentException("传入的目标变量的历史观察数据数组长度和影响因素的历史观测数据长度不等，无法进行计算");
            throw new IllegalArgumentException(I18N.getString("com.esen.util.MathUtil.6", "传入的目标变量的历史观察数据数组长度和影响因素的历史观测数据长度不等，无法进行计算 "));
        }

        return texp(ls(tlog(x), tlog(ys), tlog(xs)));
    }

    /**
     * 偏相关系数
     *
     * @param x 需要计算偏相关系数的第一个指标数据
     * @param y 需要计算偏相关系数的第二个指标数据
     * @param z 计算偏相关系数时使用的控制指标数据
     * @return 指标x和指标y的偏相关系数
     * @throws NullPointerException     如果传入的参数中存在空指针，将抛出该异常信息
     * @throws IllegalArgumentException 如果传入的三个参数数组的长度不相等，将抛出该异常信息
     */
    public static double pcorr(double[] x, double[] y, double[] z) throws NullPointerException, IllegalArgumentException {

        if (x == null || y == null || z == null) {
            //throw new NullPointerException("传入的参数中包含空指针,无法进行计算");
            throw new NullPointerException(I18N.getString("com.esen.util.MathUtil.7", "传入的参数中包含空指针,无法进行计算"));
        }

        int len = x.length;
        if (y.length != len || z.length != len) {
            //throw new NullPointerException("传入的数据指标的长度不相等,无法计算相关系数");
            throw new NullPointerException(I18N.getString("com.esen.util.MathUtil.8", "传入的数据指标的长度不相等,无法计算相关系数"));
        }

        double[] yy = ls(z, y, z);
        double[] xx = ls(x, y, x);

        return corr(yy, xx, len);
    }

    /**
     * 计算一个数组偏差的平方和
     *
     * @param x 需要计算偏差平方和的数据
     * @return 一组数据的偏差平方和;如果数据中包含NaN,将被忽略.
     * @throws NullPointerException 如果参数为空指针，将抛出该异常信息
     */
    public static double devsq(double[] x) throws NullPointerException {

        if (x == null) {
            //throw new NullPointerException("传入的参数为空指针,无法进行计算");
            throw new NullPointerException(I18N.getString("com.esen.util.MathUtil.9", "传入的参数为空指针,无法进行计算 "));
        }

        double xhat = avg(x, x.length, true);

        if (Double.isNaN(xhat)) {
            return Double.NaN;
        }

        double devsq = 0;
        for (int i = 0; i < x.length; i++) {
            if (Double.isNaN(x[i])) {
                continue;
            }

            devsq += (x[i] - xhat) * (x[i] - xhat);
        }

        return devsq;
    }

    /**
     * 计算一列数据的几何平均值
     *
     * @param x 需要计算几何平均值的数据数组
     * @return 一组数据的几何平均值, 数据中包含的NaN以及非正数的数据在计算时将被忽略;
     * 当传入的数组中包含的正数个数为0时，返回NaN;
     * @throws NullPointerException 如果参数为空指针，将抛出该异常信息
     */
    public static double gmean(double[] x) throws NullPointerException {

        if (x == null) {
            //throw new NullPointerException("传入的数据参数为空指针,无法进行计算");
            throw new NullPointerException(I18N.getString("com.esen.util.MathUtil.10", "传入的数据参数为空指针,无法进行计算"));
        }

        double n = 0;
        double prod = 1;
        for (int i = 0; i < x.length; i++) {
            if (Double.isNaN(x[i]) || x[i] <= 0) {
                continue;
            }

            prod *= x[i];
            n = n + 1.0;
        }

        if (n == 0) {
            return Double.NaN;
        }

        return Math.pow(prod, 1.0 / n);
    }

    /**
     * 获取数据中最大的k个数据
     *
     * @param x 需要获取最大的K个数据的数组
     * @param k 需要获取的最大的数据的数目，该参数的值为正数，并且不能大于传入的数组的长度，否则将抛出异常{@link IllegalArgumentException}信息
     * @return 原始数据中最大的K个数据，结果中数据将从大到小排列
     * @throws NullPointerException     如果传入的数据数组为空指针，将抛出该异常信息
     * @throws IllegalArgumentException 如果第二个参数不在合法的范围内，将抛出该异常信息，详细情况说明请参考第二个参数的说明
     */
    public static double[] large(double[] x, int k) throws NullPointerException, IllegalArgumentException {
        //TODO
        return null;
    }

    /**
     * 获取数据中最小的k个数据
     *
     * @param x 需要获取最小的K个数据的数组
     * @param k 需要获取的最小的数据的数目，该参数的值为正数，并且不能大于传入的数组的长度，否则将抛出异常{@link IllegalArgumentException}信息
     * @return 原始数据中最小的K个数据，结果中数据将从小到大排列
     * @throws NullPointerException     如果传入的数据数组为空指针，将抛出该异常信息
     * @throws IllegalArgumentException 如果第二个参数不在合法的范围内，将抛出该异常信息，详细情况说明请参考第二个参数的说明
     */
    public static double[] small(double[] x, int k) throws NullPointerException, IllegalArgumentException {
        //TODO
        return null;
    }

    /**
     * 计算一组数据样本在给定总体均值情况下的标准差
     *
     * @param values 需要计算标准差的样本数据
     * @param mean   给定的样本数据的总体均值
     * @return 样本数据的标准差
     */
    public static double stdevm(double[] values, double mean) {

        if (values == null || Double.isNaN(mean)) {
            return Double.NaN;
        }

        double sum_sq = 0;
        int n = 0;
        for (int k = 0; k < values.length; k++) {
            if (Double.isNaN(values[k])) {
                continue;
            }

            n++;
            sum_sq += (values[k] - mean) * (values[k] - mean);
        }

        if (n == 0) {
            return Double.NaN;
        }

        return Math.sqrt(sum_sq / n);
    }

    //安全检查修改
    public static double random() {
        return SecurityRandom.getInstance().nextDouble();
    }
}
