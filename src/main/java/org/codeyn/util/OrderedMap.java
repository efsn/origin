package org.codeyn.util;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Title: 按加入的先后顺序遍历的HashMap</p>
 * 
 * 存在频繁的删除操作时，特慢。
 */

public class OrderedMap extends HashMap implements Serializable {
  private List orderedKeys = new LinkedList();

  public OrderedMap() {
    super();
  }
  
  public OrderedMap(int initialCapacity) {
    super(initialCapacity);
  }
  /**
   * 得到集合的key值的set集合
   */
  public Set keySet() {
    return new OrderedSet(orderedKeys);
  }
  /**
   * 检索指定key值元素的位置
   * @param key 指定的key值
   * @return 元素的索引位置
   */
  public int indexOf(Object key) {
    return orderedKeys.indexOf(key);
  }
  /**
   * 将指定的键值对插入集合中
   * @param key 键值
   * @param value 键值对应的value值
   * @return 返回旧的键值对应的值
   */
  public Object put(Object key, Object value) {
    /**
     * 使用Map的方式判断key是否存在，效率要高于List
     */
    if (!super.containsKey(key)) {
      orderedKeys.add(key);
    }
    return super.put(key, value);
  }
  /**
   * 将指定的map插入到集合中
   * @param m 指定的map
   */
  public void putAll(Map m){
    if(m.size() == 0) return;
    for (Iterator i = m.entrySet().iterator(); i.hasNext(); ) {
      Map.Entry e = (Map.Entry)i.next();
      this.put(e.getKey(), e.getValue());
  }
  }
  /**
   * 得到集合指定位置上的元素的value值
   * @param i 元素在集合中的位置
   * @return 返回i位置上的元素的value值
   */
  public Object get(int i) {
    Object key = orderedKeys.get(i);
    return super.get(key);
  }
  /**
   * 得到集合指定位置上的元素的key值
   * @param i 元素在集合中的位置
   * @return 返回i位置上的元素的key值
   */
  public Object getKey(int i){
    return orderedKeys.get(i);
  }
  /**
   * 清空集合中的内容
   */
  public void clear() {
    super.clear();
    orderedKeys.clear();
  }
  /**
   * 移除指定键值在集合中的键值对
   * @param key 指定的key值
   * @return 返回key值对应的value值
   */
  public Object remove(Object key) {
    if (super.containsKey(key)) {
      orderedKeys.remove(key);
    }
    return super.remove(key);
  }
  /**
   * 得到集合的所有value值
   */
  public Collection values() {
    Iterator i = orderedKeys.iterator();
    if (!i.hasNext()) {
      return null;
    }

    List values = new ArrayList();
    while (i.hasNext()) {
      values.add(this.get(i.next()));
    }
    return (Collection) values;
  }
}

class OrderedSet
    extends AbstractSet {

  private List backedList = new LinkedList();

  public OrderedSet() {}

  public OrderedSet(Collection c) {
    Iterator i = c.iterator();

    while (i.hasNext()) {
      add(i.next());
    }
  }

  public Iterator iterator() {
    return backedList.iterator();
  }
  /**
   * 得到集合的大小
   */
  public int size() {
    return backedList.size();
  }
  /**
   * 向集合中添加元素，如果添加的元素已经存在，返回false，否则返回true
   * @param obj 要添加的对象
   */
  public boolean add(Object obj) {
    int index = backedList.indexOf(obj);

    if (index == -1) {
      return backedList.add(obj);
    }
    else {
      backedList.set(index, obj);
      return false;
    }
  }
}
