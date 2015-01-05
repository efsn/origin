package org.codeyn.util;

public final class DoubleArray{
    
    private double[] values;
    private int size;

    public DoubleArray(int initsize){
        values = new double[initsize];
        size = 0;
    }

    public DoubleArray(){
        this(10);
    }

    public void add(double v){
        ensureCapacity(size + 1);
        values[size++] = v;
    }

    public void add(double[] dd){
        if (dd != null) {
            int l = dd.length;
            ensureCapacity(size + l);
            System.arraycopy(dd, 0, values, size, l);
            size += l;
        }
    }

    public void add(int index, double v){
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                    + size);

        ensureCapacity(size + 1); // Increments modCount!!
        System.arraycopy(values, index, values, index + 1, size - index);
        values[index] = v;
        size++;
    }

    private void RangeCheck(int index){
        if (index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
                    + size);
    }

    public double remove(int index){
        RangeCheck(index);

        double oldValue = values[index];

        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(values, index + 1, values, index, numMoved);
        return oldValue;
    }

    public int size(){
        return size;
    }

    public double sum(){
        if (size == 0) {
            return Double.NaN;
        }
        double r = 0;
        for (int i = 0; i < size; i++) {
            double v = values[i];
            if (!Double.isNaN(v)) r += v;
        }
        return r;
    }

    public double avg(boolean ignoreNan){
        return MathUtil.avg(values, size, ignoreNan);
    }

    public int count(boolean ignoreNan){
        if (!ignoreNan) {
            return size;
        }
        int r = 0;
        for (int i = 0; i < size; i++) {
            if (!Double.isNaN(values[i])) r++;
        }
        return r;
    }

    /**
     * 标准差 if(_N(x)=1,0,sqrt(_S(power(x - _A(x),2))/(_N(x)-1)))
     */
    public double stddev(boolean ignoreNan){
        return MathUtil.stddev(values, size, ignoreNan);
    }

    /**
     * 平均偏差 if(_N()=0,null,_S(abs(zb-_A(zb)))/_N())
     */
    public double avedev(boolean ignoreNan){
        return MathUtil.avedev(values, size, ignoreNan);
    }

    public double var(boolean ignoreNan){
        return MathUtil.var(values, size, ignoreNan);
    }

    public double get(int index){
        RangeCheck(index);
        return values[index];
    }

    public double set(int index, double element){
        RangeCheck(index);

        double oldValue = values[index];
        values[index] = element;
        return oldValue;
    }

    private void ensureCapacity(int minCapacity){
        int oldCapacity = values.length;
        if (minCapacity > oldCapacity) {
            double oldData[] = values;
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity) newCapacity = minCapacity;
            values = new double[newCapacity];
            System.arraycopy(oldData, 0, values, 0, size);
        }
    }

    public double[] toArray(){
        double[] r = new double[this.size];
        System.arraycopy(values, 0, r, 0, size);
        return r;
    }

}
