package com.esen.jdbc.etl.data;

import java.io.Serializable;

public class IrptData implements Serializable {
  public static final int DATASTATUS_WAIT = -1; //需要等待

  public static final int DATASTATUS_READY = 1;//数据已经准备好

  public static final int DATASTATUS_OVER = 0; //数据已经完毕

  public static final int DATASTATUS_ABORT = 2; //因为异常而终止

  public static final String STR_DATASTATUS_WAIT = "wait";

  public static final String STR_DATASTATUS_READY = "ready";//数据已经准备好

  public static final String STR_DATASTATUS_OVER = "over"; //数据已经完毕

  public static final String STR_DATASTATUS_ABORT = "abort"; //因为异常而终止

  private int status = DATASTATUS_WAIT;//状态

  private Object data;//数据

  private String msg;//信息

  private int allcount;//数据个数

  private String id;//id

  private long overtime = -1;

  public IrptData(Object data, int status, String msg, String id, int allcount,
      long overtime) {
    this.data = data;
    this.status = status;
    this.msg = msg;
    this.id = id;
    this.allcount = allcount;
    this.overtime = overtime;
  }

  public void setData(Object data) {
    this.data = data;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public int getStatus() {
    return this.status;
  }

  public Object getData() {
    return this.data;
  }

  public void setMessage(String msg) {
    this.msg = msg;
  }

  public String getMessage() {
    return this.msg;
  }

  public void setAllCount(int allcount) {
    this.allcount = allcount;
  }

  public int getAllCount() {
    return this.allcount;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return this.id;
  }

  public void setOverTime(long overtime) {
    this.overtime = overtime;
  }

  public long getOverTime() {
    return this.overtime;
  }
}
