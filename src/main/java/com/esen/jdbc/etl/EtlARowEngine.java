package com.esen.jdbc.etl;

import com.esen.jdbc.etl.impl.EtlARowEngineImpl;
import com.esen.util.exp.Expression;

/**
 * 数据抽取接口，负责复制一行数据，使用者可以先创建此对象，然后为每行源数据调用etlARow方法
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>武汉新连线科技有限公司</p>
 * @author yk
 * @version 5.0
 */
public abstract class EtlARowEngine {
  /**
   * 写一行的数据，如果返回true表示数据符合导入条件并设置了到dest中去了，如果返回false表示这条数据忽略
   */
  public abstract boolean etlARow(EtlDataSrc src, EtlDataDest dest);
  
  public abstract boolean canDataEtl(EtlDataSrc src);

  public static EtlARowEngine createInstance(EtlDefine def,
      EtlDataMeta srcMeta, EtlDataMeta destMeta) {
    EtlARowEngineImpl engine = new EtlARowEngineImpl(def, srcMeta, destMeta);
    engine.compile();
    return engine;
  }
}