package org.codeyn.util;

/**
 * <p>Title: 变长double[]的实现</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class NumberList {
  private double[] nums;

  private int size;

  public NumberList(int initlength) {
    if (initlength < 0)
      throw new IllegalArgumentException("Illegal Capacity: " + initlength);
    nums = new double[initlength];
    size = 0;
  }

  public NumberList() {
    this(10);
  }

  public boolean add(double value) {
    ensureCapacity(size + 1);
    nums[size++] = value;
    return true;
  }

  public double get(int index) {
    RangeCheck(index);
    return nums[index];
  }

  public double remove(int index) {
    RangeCheck(index);

    double oldValue = nums[index];

    int numMoved = size - index - 1;
    if (numMoved > 0)
      System.arraycopy(nums, index + 1, nums, index, numMoved);
    nums[--size] = 0;

    return oldValue;
  }

  private void RangeCheck(int index) {
    if (index >= size)
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
  }

  private void ensureCapacity(int minCapacity) {
    int oldCapacity = nums.length;
    if (minCapacity > oldCapacity) {
      double oldData[] = nums;
      int newCapacity = (oldCapacity * 3) / 2 + 1;
      if (newCapacity < minCapacity)
        newCapacity = minCapacity;
      nums = new double[newCapacity];
      System.arraycopy(oldData, 0, nums, 0, size);
    }
  }

  public boolean contains(double value) {
    for (int i = 0; i < size; i++) {
      if(value == nums[i]){
        return true;
      }
    }
    return false;
  }

  public double[] toArray() {
    double[] num = new double[size];
    for (int i = 0; i < size; i++) {
      num[i] = nums[i];
    }
    return num;
  }

  public int[] toIntArray() {
    int[] num = new int[size];
    for (int i = 0; i < size; i++) {
      num[i] =(int) Math.round(nums[i]);
    }
    return num;
  }

  public void add(long values[]) {
    for (int i = 0; i < values.length; i++) {
      this.add(values[i]);
    }
  }
  
  public void add(int values[]) {
    for (int i = 0; i < values.length; i++) {
      this.add(values[i]);
    }
  }

  
  public int getSize() {
    //System.out.println(nums.length);
    return size;
  }

  public void clear() {
    //nums = new double[10];
    size = 0;
  }

  public boolean isEmpty() {
    return size == 0;
  }
}
