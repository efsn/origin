package org.codeyn.util;

import java.util.Comparator;

public class Comparators<T> implements Comparator<T> {

    private Comparator<T>[] cs;

    public Comparators(Comparator<T>[] comparators) {
        this.cs = comparators;
    }

    public int compare(T arg0, T arg1) {
        for (int i = 0; i < cs.length; i++) {
            int c = cs[i].compare(arg0, arg1);
            if (c != 0) return c;
        }
        return 0;
    }
}
