package com.esen.jdbc.pool;

import java.io.PrintStream;
import java.text.SimpleDateFormat;


/**
 * 记录连接池日志；
 * 日志分5个级别：DEBUG,INFO,WARN,ERROR,FATAL ；
 * 对应数值是 0,1,2,3,4;
 * DEBUG: 记录的最详细，包含INFO级别，记录每个执行的sql，耗时，对sql的传值等；
 * INFO:  包含WARN日志，记录连接池状态，比如，创建连接池，什么时候获取连接，什么时候关闭连接，关闭连接池信息；
 * WARN： 包含ERROR日志，记录每次获取连接的堆栈，用于检查连接有没有关闭（回收）；
 *        记录每次创建statement堆栈，用于判断关闭连接前是否关闭创建的statement;
 *        记录嵌套获取连接警告；
 * ERROR: 包含FATAL日志，记录从连接池获取连接超时的异常；记录从连接池无法获取连接的异常等；
 *        记录sql执行出错异常；
 * FATAL: 记录创建连接异常（比如数据库没有启动等）；
 *        连接没有关闭提示，这里不记录该连接的堆栈，要找到它，请使用WARN级别；
 * <0表示不记录任何日志，>4表示FATAL级别；
 * 
 * 为了兼容旧设置：
 * isDeBug=ture  表示 LOG_LEVER_WARN 级次；
 *        =false 表示 LOG_LEVER_ERROR 级次；
 *        
 * 每个连接池都拥有一个此类的实例，互不干扰；
 * 但是，每个连接池内部，有获取连接的资源的并发，因此此类需要考虑并发操作；
 * @author dw
 */
public class JdbcLogger {
  public static final String[] LOGLEVERSTR={"DEBUG","INFO","WARN","ERROR","FATAL"};
  public static final int LOG_LEVER_DEBUG = 0;
  public static final int LOG_LEVER_INFO = 1;
  public static final int LOG_LEVER_WARN = 2;
  public static final int LOG_LEVER_ERROR = 3;
  public static final int LOG_LEVER_FATAL = 4;
  
  /**
   * 用于打印信息对其；
   */
  private static final String[] printLOGLEVERSTR={"DEBUG","INFO ","WARN ","ERROR","FATAL"};
  private int loglev;
  private PrintStream out;
  private SimpleDateFormat df;
  /**
   * 此日志的名称；
   * 这里主要指连接池的名字；
   */
  private String name;
  
  public JdbcLogger(){
    /**
     * 用于格式化日志开头的时间；
     */
    df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  }
  /**
   * lev<0  表示不记录日志；
   * lev>=4 表示最高级别
   * @param lev
   */
  public synchronized void setLogLever(int lev){
    this.loglev = lev;
  }
  /**
   * 设置日志级别，levstr可以是数值，也可以是：DEBUG,INFO,WARN,ERROR,FATAL 级别名称；
   * levstr为空表示0级别（DEBUG级别）；
   * levstr无法确定级别，则表示不记录日志，即-1级别；
   * @param levstr
   */
  public synchronized void setLogLever(String levstr){
    if(levstr==null||levstr.trim().length()==0){
      return;
    }
    int lev = -1;
    levstr = levstr.trim();
    try{
      lev = Integer.parseInt(levstr);
      if (lev < 0) {
    	  lev = 0;
      } else if (lev >= LOGLEVERSTR.length) {
    	  lev = LOGLEVERSTR.length - 1;
      }
    }catch(NumberFormatException nx){
      //非数值
      for(int i=0;i<LOGLEVERSTR.length;i++){
        if(levstr.equalsIgnoreCase(LOGLEVERSTR[i])){
          lev = i;
          break;
        }
      }
      /*
       * bug:ESENBI-2501: modify by liujin 2014.11.13
       * logLevel 的值配置有误时，无法在  LOGLEVERSTR 中找到对应字符串，会出异常，
       * 配置参数不正确时，用默认值处理。
       * 
       */
      if (lev == -1) {
      	lev = LOG_LEVER_ERROR;
      }
    }
    
    this.loglev = lev;
  }
  public synchronized int getLogLever(){
    return loglev;
  }
  public synchronized String getLogLeverStr(){
    if(loglev<0)
      return null;
    if(loglev>=LOGLEVERSTR.length)
      return LOGLEVERSTR[LOGLEVERSTR.length-1];
    return LOGLEVERSTR[loglev];
  }
  /**
   * 设置日志记录的流，如果out==null，则记录到标准输出控制台；
   * @param out
   */
  public synchronized void setLogWriter(PrintStream out){
    this.out = out;
  }
  public synchronized PrintStream getLogWriter(){
    if(this.out==null){
      return System.out;
    }
    return this.out;
  }
  /**
   * 设置名称；
   * 这里主要指连接池的名称；
   * @param name
   */
  public synchronized void setName(String name){
    this.name = name;
  }
  private void printTitle(PrintStream out, int lev){
    out.print('[');
    out.print(printLOGLEVERSTR[lev>=LOGLEVERSTR.length?LOGLEVERSTR.length-1:lev]);
    out.print(']');
    out.print(df.format(new java.util.Date()));
    if(name!=null&&name.length()>0){
      out.print('(');
      out.print(name);
      out.print(')');
    }
  }
  
  /**
   * 记录制定级次的日志；
   * @param v
   * @param lev
   */
  public synchronized void log(String v,int lev){
    if(canLogLevel(lev)){
      if(v==null||v.length()==0)
        return;
      PrintStream out = this.getLogWriter();
      printTitle(out, lev);
      out.print(' ');
      out.println(v);
    }
  }
  /**
   * 判断是否允许记录指定级次的日志；
   * 设置的日志级次loglev>LOG_LEVER_FATAL ,当做LOG_LEVER_FATAL处理；
   * @param lev
   * @return
   */
  public synchronized boolean canLogLevel(int lev){
    return loglev>=0&&loglev<=lev||lev==LOG_LEVER_FATAL&&loglev>lev;
  }
  /**
   * 记录debug级别日志；
   * @param v
   */
  public void debug(String v) {
    log(v,JdbcLogger.LOG_LEVER_DEBUG);
  }
  
  /**
   * 记录info级别日志；
   * @param v
   */
  public void info(String v) {
    log(v,JdbcLogger.LOG_LEVER_INFO);
  }
  /**
   * 记录warn级别日志；
   * @param v
   */
  public void warn(String v) {
    log(v,JdbcLogger.LOG_LEVER_WARN);
  }
  /**
   * 记录error级别日志；
   * @param v
   */
  public void error(String v) {
    log(v,JdbcLogger.LOG_LEVER_ERROR);
  }
  /**
   * 记录fatal级别日志；
   * @param v
   */
  public void fatal(String v) {
    log(v,JdbcLogger.LOG_LEVER_FATAL);
  }
  /**
   * 关闭连接池时调用；
   */
  public void close(){
    if(out!=null && out!=System.out)
      out.close();
  }
}
