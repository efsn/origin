package org.codeyn.util.progress;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import java.util.ArrayList;

import org.codeyn.util.exception.CancelException;
import org.codeyn.util.exception.ExceptionHandler;
import org.codeyn.util.i18n.I18N;
import org.codeyn.util.yn.StrUtil;

public class ProgressDefault implements IProgress{
    private static final long serialVersionUID = -105778650059739864L;
    private String name;
    private boolean printLog;

    /**
     * 记录最后一次更新日志的时间，因为每次客户端中服务器去日志信息是很耗时的，记录下
     */
    private long last_update_log_time;

    /**
     * 设置是否打印日志信息 true打印日志信息到控制台，反之不打印
     * 
     * @param printLog
     */
    public void setPrintLog(boolean printLog){
        this.printLog = printLog;
    }

    /**
     * 构造方法，默认不打印日志信息到控制台
     */
    public ProgressDefault(){
        printLog = false;
    }

    /**
     * 构造函数，默认不打印日志信息到控制台，设置日志信息的名称
     * 
     * @param nm
     */
    public ProgressDefault(String nm){
        this();
        name = nm;
    }

    private String msg;

    /**
     * msg描述了正在做什么
     */
    public synchronized void setMessage(String msg){
        this.msg = msg;
    }

    private ArrayList logs = new ArrayList();

    /**
     * 增加日志信息
     */
    public synchronized void addLog(String log){
        last_update_log_time = System.currentTimeMillis();
        logs.add(log);
        if (printLog) {
            System.out.println(log);
        }
    }

    /**
     * 修改最后一条添加的日志，如果没有一条日志，那么添加一条
     */
    public void setLastLog(String log){
        last_update_log_time = System.currentTimeMillis();
        if (logs.size() > 0) {
            logs.set(logs.size() - 1, log);
        } else {
            logs.add(log);
        }
        if (printLog) {
            System.out.println(log);
        }
    }

    /**
     * 设置最后一条添加的日志的时间
     */
    public void setLastLogWithTime(String log){
        this.setLastLog(StrUtil.formatNowDateTime() + " " + log);
    }

    /**
     * 得到指定位置的日志信息
     */
    public synchronized String getLog(int i){
        return (String) logs.get(i);
    }

    /**
     * 将所有log用回车换行链接起来组成一个字符串返回
     */
    public synchronized String getLogs(){
        int initlen = 5;
        for (int i = 0; i < logs.size(); i++) {
            String s = (String) logs.get(i);
            initlen += 2 + (s != null ? s.length() : 0);
        }

        StringBuffer sb = new StringBuffer(initlen);
        for (int i = 0; i < logs.size(); i++) {
            sb.append((String) logs.get(i));
            sb.append("\r\n");
        }
        return sb.toString();
    }

    /**
     * 得到日志信息的个数
     */
    public synchronized int getLogCount(){
        return logs.size();
    }

    int imin, imax, istep, ipos;

    /**
     * 设置进度的最小，最大和步长
     */
    public synchronized void setProgress(int min, int max, int step){
        this.imin = min;
        this.imax = max;
        this.istep = step;
    }

    /**
     * 设置进度的位置
     */
    public synchronized void setPosition(int p){
        this.ipos = p;
    }

    /**
     * 进度前进st步
     */
    public synchronized void step(int st){
        ipos += st * istep;
    }

    /**
     * 进度前进一步
     */
    public synchronized void stepit(){
        step(1);
    }

    private boolean canceled = false, finished = false;
    private String cancelMsg;

    /**
     * 检查是否取消
     */
    public synchronized boolean isCancel(){
        return canceled;
    }

    /**
     * 检查是否已经取消了，如果取消了则触发异常CancelException
     */
    public synchronized void checkCancel() throws CancelException{
        if (isCancel()) {
            throw new CancelException(cancelMsg);
        }
        /*
         * try { Thread.sleep(1); } catch (InterruptedException ex) { throw new
         * CancelException("线程被中止!"); }
         */
        Thread.yield();
        if (Thread.currentThread().isInterrupted()) {
            // throw new CancelException("线程被中止!");
            throw new CancelException(I18N.getString(
                    "com.esen.util.progressdefault.exp", "线程被中止!"));
        }
    }

    /**
     * 设置是否取消，msg是取消的原因；
     */
    public synchronized void setCancel(boolean value, String msg){
        canceled = value;
        this.cancelMsg = msg;
    }

    /**
     * 设置是否取消
     * 
     * @param value
     *            为true
     */
    public synchronized void setCancel(boolean value){
        setCancel(value, null);
    }

    /**
     * 得到描述信息
     * 
     * @return
     */
    public synchronized String getMessage(){
        return msg;
    }

    /**
     * 得到进度的最小值
     * 
     * @return
     */
    public synchronized int getMin(){
        return imin;
    }

    /**
     * 得到进度的最大值
     * 
     * @return
     */
    public synchronized int getMax(){
        return imax;
    }

    /**
     * 得到进度的步长
     * 
     * @return
     */
    public synchronized int getPosition(){
        return ipos;
    }

    /**
     * 处理异常 showException
     *
     * @param e
     *            Exception
     */
    public void showException(Exception e){
        ExceptionHandler.rethrowRuntimeException(e);
    }

    /**
     * 添加日志信息的时间
     */
    public void addLogWithTime(String log){
        this.addLog(StrUtil.formatNowDateTime() + " " + log);
    }

    /**
     * 得到最后一条日志信息
     */
    public String getLastLog(){
        if (this.logs != null && logs.size() > 0) {
            return (String) logs.get(logs.size() - 1);
        }
        return null;
    }

    /**
     * 得到日志信息的最后更新时间
     * 
     * @return
     */
    public long getLast_update_log_time(){
        return last_update_log_time;
    }

    /**
     * 得到日志信息的名称
     * 
     * @return
     */
    public String getName(){
        return name;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public int getStep(){
        return istep;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setFinished(){
        finished = true;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public boolean isFinshed(){
        return finished;
    }

}
