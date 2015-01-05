package org.codeyn.util;

import java.util.HashMap;
import java.util.Map;

import org.codeyn.util.i18n.I18N;

/**
 * 用于在运行时保存一个全局的key与value的对应表
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>武汉新连线科技有限公司</p>
 * @author yk
 * @version 5.0
 */

public final class RuntimeSysProperty {
  private static final Map ppt = new HashMap();

  public synchronized static final Object get(Object nm, Object def) {
    if (ppt.containsKey(nm)) {
      return ppt.get(nm);
    }
    else {
      return def;
    }
  }

  public synchronized static final Object get(Object nm) {
    return ppt.get(nm);
  }

  public synchronized static final Object get(Object nm,
      boolean throwifnotexists) {
    if (ppt.containsKey(nm)) {
      return ppt.get(nm);
    }
    else {
      if (throwifnotexists) {
        //throw new RuntimeException(nm + "在RuntimeSysProperty中不存在！");
        throw new RuntimeException(I18N.getString("com.esen.util.runtimesysproperty.exp", "{0}在RuntimeSysProperty中不存在！",new Object[]{nm}));
      }
      return null;
    }
  }

  public synchronized static final Object set(Object nm, Object value) {
    return ppt.put(nm, value);
  }
}
