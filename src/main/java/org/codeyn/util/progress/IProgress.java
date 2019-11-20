package org.codeyn.util.progress;

import org.codeyn.util.exception.CancelException;

import java.io.Serializable;

/**
 * 此类是一个进度信息接口，用于在执行较耗时的大型任务时通知外部执行进度和相关的执行日志
 *
 * @author yk
 */

public interface IProgress extends Serializable {
    /**
     * log表示详细信息
     *
     * @param log
     */
    void addLog(String log);

    /**
     * 在日志信息前加上时间后，再添加log
     */
    void addLogWithTime(String log);

    void setLastLogWithTime(String log);

    //取得最后一条日志
    String getLastLog();

    /**
     * 修改最后一条添加的日志，如果没有一条日志，那么添加一条
     */
    void setLastLog(String log);

    /**
     * 下面这些方法方便外边对象遍历日志信息，有利于服务器端向客户端增量发送日志信息
     */
    String getLog(int i);

    String getLogs();

    int getLogCount();

    /**
     * 设置进度的最小，最大和步长
     *
     * @param min
     * @param max
     * @param step
     */
    void setProgress(int min, int max, int step);

    /**
     * 前进st步
     *
     * @param st
     */
    void step(int st);

    /**
     * 前进一步
     */
    void stepit();

    /**
     * 判断是否已经取消了。
     *
     * @return
     */
    boolean isCancel();

    /**
     * 设置是否取消，msg是取消的原因；
     *
     * @param value
     * @param msg
     */
    void setCancel(boolean value, String msg);

    /**
     * 检查是否已经取消了，如果取消了则触发异常CancelException
     *
     * @return
     * @throws CancelException
     */
    void checkCancel() throws CancelException;

    /**
     * 处理异常
     *
     * @param e Exception
     */
    void showException(Exception e);

    /**
     * 返回进度条的步进
     *
     * @return
     */
    int getStep();

    /**
     * 进度条起始位
     *
     * @return
     */
    int getMin();

    /**
     * 进度条结束位
     *
     * @return
     */
    int getMax();

    /**
     * 进度条的当前位置
     *
     * @return
     */
    int getPosition();

    /**
     * 设置进度的位置
     *
     * @param p
     */
    void setPosition(int p);

    /**
     * 当前正在做什么
     *
     * @return
     */
    String getMessage();

    /**
     * msg描述了正在作什么
     *
     * @param msg
     */
    void setMessage(String msg);

    /**
     * 设置已经完成
     */
    void setFinished();

    /**
     * 是否已经结束
     *
     * @return
     */
    boolean isFinshed();
}
