package org.codeyn.util.progress;

import java.io.Serializable;

import org.codeyn.util.exception.CancelException;

/**
 * 此类是一个进度信息接口，用于在执行较耗时的大型任务时通知外部执行进度和相关的执行日志
 * 
 * @author yk
 */

public interface IProgress extends Serializable{
  /**
   * msg描述了正在作什么
   * @param msg
   */
  public void setMessage(String msg);

  /**
   * log表示详细信息
   * @param log
   */
  public void addLog(String log);
  
  /**
   * 在日志信息前加上时间后，再添加log
   */
  public void addLogWithTime(String log);
  
  /**
   * 修改最后一条添加的日志，如果没有一条日志，那么添加一条
   */
  public void setLastLog(String log);
  
  public void setLastLogWithTime(String log);
  
  //取得最后一条日志
  public String getLastLog();
  
  /**
   * 下面这些方法方便外边对象遍历日志信息，有利于服务器端向客户端增量发送日志信息
   */
  public String getLog(int i);
  
  public String getLogs();
  
  public int getLogCount();

  /**
   * 设置进度的最小，最大和步长
   * @param min
   * @param max
   * @param step
   */
  public void setProgress(int min, int max, int step);

  /**
   * 设置进度的位置
   * @param p
   */
  public void setPosition(int p);

  /**
   * 前进st步
   * @param st
   */
  public void step(int st);

  /**
   * 前进一步
   */
  public void stepit();

  /**
   * 判断是否已经取消了。
   * @return
   */
  public boolean isCancel();
  
  /**
   * 设置是否取消，msg是取消的原因；
   * @param value
   * @param msg
   */
  public void setCancel(boolean value,String msg);

  /**
   * 检查是否已经取消了，如果取消了则触发异常CancelException
   * @return
   * @throws CancelException
   */
  public void checkCancel()throws CancelException;

  /**
   * 处理异常
   * @param e Exception
   */
  public void showException(Exception e);
  
  /**
   * 返回进度条的步进
   * @return
   */
  public int getStep();

  /**
   * 进度条起始位
   * @return
   */
  public int getMin();

  /**
   * 进度条结束位
   * @return
   */
  public int getMax();

  /**
   * 进度条的当前位置
   * @return
   */
  public int getPosition();

  /**
   * 当前正在做什么
   * @return
   */
  public String getMessage();

  /**
   * 设置已经完成
   */
  public void setFinished();

  /**
   * 是否已经结束
   * @return
   */
  public boolean isFinshed();
}
