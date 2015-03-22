package org.codeyn.util;

import java.text.Collator;
import java.util.Comparator;

/**
 * 支持中文按拼音排序 支持null的处理 升序时NULL排序到最后 isAscNull 默认为false即升序时NULL排序到最前 降序时NULL排序到最前
 * isDescNull 默认为false即降序时NULL排序到最后
 */

public class AdvSortComparator implements Comparator{

    protected boolean asc;// 是否升序。默认降序

    protected boolean isAscNullLast;

    protected boolean isDescNullFirst;

    protected boolean isNullEqualBlank;

    private Collator localstrcmp;

    public AdvSortComparator(){
        super();
    }

    public final boolean isAsc(){
        return asc;
    }

    public final void setAsc(boolean asc){
        this.asc = asc;
    }

    public final boolean isAscNullLast(){
        return isAscNullLast;
    }

    public final void setAscNullLast(boolean isAscNull){
        this.isAscNullLast = isAscNull;
    }

    public final boolean isDescNullFirst(){
        return isDescNullFirst;
    }

    public final void setDescNullFirst(boolean isDescNull){
        this.isDescNullFirst = isDescNull;
    }

    public boolean isNullEqualBlank(){
        return isNullEqualBlank;
    }

    public void setNullEqualBlank(boolean isNullEqualBlank){
        this.isNullEqualBlank = isNullEqualBlank;
    }

    public int compare(Object a, Object b){
        Comparable sza = (Comparable) a;
        Comparable szb = (Comparable) b;
        if (sza == szb) return 0;
        boolean nulla = isNull(sza);
        boolean nullb = isNull(szb);
        if (nulla && nullb) return 0;
        if (nulla && !nullb) {
            if (asc && isAscNullLast) return 1;
            if (!asc && isDescNullFirst) return -1;
            return asc ? -1 : 1;
        }
        if (!nulla && nullb) {
            if (asc && isAscNullLast) return -1;
            if (!asc && isDescNullFirst) return 1;
            return asc ? 1 : -1;
        }
        int r;
        if (sza instanceof String) {
            r = getLocalStrCompare().compare(sza, szb);
        } else {
            r = sza.compareTo(szb);
        }
        if (!asc) {
            r = -r;
        }
        return r;
    }

    private Collator getLocalStrCompare(){
        if (this.localstrcmp == null)
            this.localstrcmp = java.text.Collator
                    .getInstance(java.util.Locale.CHINA);
        return this.localstrcmp;
    }

    private boolean isNull(Comparable o){
        if (o == null) return true;
        if (o instanceof Double)
            return ((Double) o).isNaN();
        else if (o instanceof String) {
            return isNullEqualBlank() && ((String) o).length() == 0;
        }

        return false;
    }

}
