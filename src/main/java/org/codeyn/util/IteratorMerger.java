package org.codeyn.util;

import java.util.Iterator;

public class IteratorMerger implements Iterator {
    private Iterator[] itts;
    private int index;

    public IteratorMerger(Iterator[] itts) {
        this.itts = itts;
        index = 0;
    }

    public boolean hasNext() {
        boolean b = itts[index].hasNext();
        return b;
    }

    public Object next() {
        return itts[index].next();
    }

    public void remove() {
        itts[index].remove();
    }
}