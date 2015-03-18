package com.esen.jdbc.orm.cache;

public interface ORMCacheListener {
  void afterClearCache(String connName, String entityName);
}
