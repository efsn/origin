package org.codeyn.util;

import org.codeyn.util.i18n.I18N;
import org.codeyn.util.yn.StrUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * 根据Comparator排序的List对象
 * 调用add(o)方法时，根据二分查找算法，将新的节点插入指定位置
 * 由于该OrderedList内部的对象已经排序,所以OrderedList不支持向指定序号插入对象.
 */
public class OrderedList extends ArrayList implements Cloneable {
    /**
     * 自定义比较器
     */
    private static final Comparator DEFAULT = new Comparator() {
        public int compare(Object o1, Object o2) {
            return StrUtil.compareObject(o1, o2);
        }
    };
    /**
     * 比较器对象
     */
    private Comparator comp;
    public OrderedList() {
        this(DEFAULT);
    }

    public OrderedList(Comparator comp) {
        super();
        setComparator(comp);
    }

    public OrderedList(int size, Comparator comp) {
        super(size);
        setComparator(comp);
    }

    public OrderedList(int size) {
        this(size, DEFAULT);
    }

    public OrderedList(Collection c) {
        this(c, DEFAULT);
    }

    public OrderedList(Collection c, Comparator comp) {
        this(c.size(), comp);
        this.addAll(c);
    }

    /**
     * 克隆OrderedList对象
     */
    public Object clone() {
        OrderedList clone = (OrderedList) super.clone();
        clone.comp = this.comp;
        return clone;
    }

    /**
     * 定义比较器
     *
     * @param comp 比较器对象
     */
    private void setComparator(Comparator comp) {
        if (comp == null)
            //throw new RuntimeException("Comparator不能为空！");
            throw new RuntimeException(I18N.getString("com.esen.util.orderedlist.exp1", "Comparator不能为空！"));
        this.comp = comp;
    }

    /**
     * 插入一个对象，返回当前对象插入的位置
     *
     * @param obj
     * @return 当返回值小于0时, 表示该节点已经存在.插入操作不成功.
     */
    public int insert(Object obj) {
        int r = -1;
        int size = size();
        if (size == 0) {
            r = 0;
        } else {
            /* 先比较最后一个代码 */
            Object obj2 = get(size - 1);
            int c = comp.compare(obj, obj2);
            if (c == 0) {
                return -1;
            }
            //如果比最后一个代码要大，则这截追加到最后一个
            else if (c > 0) {
                r = size;
            } else {
                //取第0个代码进行比较
                obj2 = get(0);
                c = comp.compare(obj, obj2);
                if (c == 0) {
                    return -1;
                } else if (c < 0) {
                    r = 0;
                } else {
                    int b = 0;
                    int l = size - 1;
                    while (b <= l) {
                        r = (l + b) >> 1;
                        obj2 = get(r);
                        c = comp.compare(obj, obj2);
                        if (c == 0)
                            return -1;
                        else if (c > 0) {
                            b = r + 1;
                        } else {
                            l = r - 1;
                        }
                    }
                    obj2 = get(r);
                    r = comp.compare(obj, obj2) > 0 ? r + 1 : r;
                }
            }
        }
        if (r == size) {
            super.add(obj);
        } else {
            super.add(r, obj);
        }
        return r;
    }

    /**
     * 添加一个对象，如果对象不存在返回true，存在返回false
     *
     * @param obj 添加的对象
     */
    public boolean add(Object obj) {
        return insert(obj) > -1;
    }

    /**
     * 添加指定的集合,集合为null或不含有元素返回null
     *
     * @param arg0 添加的集合
     */
    public boolean addAll(Collection arg0) {
        if (arg0 == null || arg0.isEmpty()) {
            return false;
        }
        Iterator it = arg0.iterator();
        boolean r = false;
        while (it.hasNext()) {
            r = this.add(it.next()) || r;
        }
        return r;
    }

    /**
     * 利用二分查找法，查找指定元素的在集合中的位置,元素不存在则返回-1
     *
     * @param elem 查找的元素
     */
    public int indexOf(Object elem) {
        int high = size() - 1, low = 0, mid, cmp;
        Object midVal;
        while (low <= high) {
            mid = (low + high) >> 1;
            midVal = get(mid);
            cmp = comp.compare(midVal, elem);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1); // key not found.
    }

    /**
     * 利用二分查找法，查找指定元素的在集合中的最终位置,元素不存在则返回-1
     *
     * @param elem 查找的元素
     */
    public int lastIndexOf(Object elem) {
        return indexOf(elem);
    }

    /**
     * 移除指定对象
     *
     * @param o 指定移除的元素
     */
    public boolean remove(Object o) {
        int i = indexOf(o);
        return i > -1 && remove(i) != null;
    }

    public void add(int index, Object element) {
        //throw new RuntimeException("OrderedList不支持此方法!");
        throw new RuntimeException(I18N.getString("com.esen.util.orderedlist.exp2", "OrderedList不支持此方法!"));
    }

    public boolean addAll(int index, Collection c) {
        //throw new RuntimeException("OrderedList不支持此方法!");
        throw new RuntimeException(I18N.getString("com.esen.util.orderedlist.exp2", "OrderedList不支持此方法!"));
    }

    public Object set(int index, Object element) {
        //throw new RuntimeException("OrderedList不支持此方法!");
        throw new RuntimeException(I18N.getString("com.esen.util.orderedlist.exp2", "OrderedList不支持此方法!"));
    }

}
