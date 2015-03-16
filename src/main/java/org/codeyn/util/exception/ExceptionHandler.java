package org.codeyn.util.exception;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.codeyn.util.yn.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 提供一个处理异常的通用工具类
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public final class ExceptionHandler{
    
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    private ExceptionHandler(){
    }

    static private _ExceptionHandler handle = null;

    /**
     * 打印当前线程的堆栈信息
     */
    static public void printCurrentStackTrace(String msg){
        try {
            throw new RuntimeException(msg);
        } catch (Exception ex) {
            String s = StrUtil.exception2str(ex);
            s = StrUtil.ensureNotStartWith(s, "java.lang.RuntimeException:")
                    .trim();
            System.out.println(s);
        }
    }

    /**
     * 异常处理
     * 
     * @param ex
     */
    synchronized static public void handleException(Exception ex){
        if (handle != null) {
            handle.handleException(ex);
        }
    }

    /**
     * 设置异常处理的接口，返回原来的接口。
     * 
     * @param value
     * @return
     */
    synchronized static public _ExceptionHandler setExceptionHandler(
            _ExceptionHandler value){
        _ExceptionHandler old = handle;
        handle = value;
        return old;
    }

    /**
     * 获得异常处理接口。
     * 
     * @return
     */
    synchronized static public _ExceptionHandler getExceptionHandler(){
        return handle;
    }

    /**
     * @deprecated 使用支持国际化的异常抛出方式 抛出一个新的异常，异常的堆栈信息于给定的ex一致，但message是指定的msg
     * @param ex
     * @param msg
     * @throws java.lang.Exception
     */
    static public void rethrow(Exception ex, String msg) throws Exception{
        Exception e = new Exception(msg, ex);
        e.setStackTrace(ex.getStackTrace());
        throw e;
    }

    /**
     * 抛出一个新的异常，异常的堆栈信息于给定的ex一致，但message是指定的msg
     * 
     * @param ex
     * @param key
     *            异常信息的资源key
     * @param defaultValue
     *            异常信息的默认值
     * @param params
     *            参数
     * @throws Exception
     *             重新构造的异常
     */
    static public void rethrow(Exception ex, String key, String defaultValue,
            Object[] params) throws Exception{
        Exception e = new Exception4I18N(key, defaultValue, ex);
        e.setStackTrace(ex.getStackTrace());
        throw e;
    }

    static public void rethrow(Exception ex, String key, String defaultValue)
            throws Exception{
        rethrow(ex, key, defaultValue, null);
    }

    /**
     * @deprecated 使用支持国际化的异常抛出方式 抛出一个新的异常，异常的堆栈信息于给定的ex一致，但message是指定的msg
     * @param ex
     * @param msg
     * @throws java.lang.Exception
     */
    static public void rethrowRuntimeException(Throwable ex, String msg){
        if (ex == null) {
            throwRuntimeException(msg);
        }
        if (msg == null) {
            /**
             * 20080928 msg为空时：以前的做法是msg为空时取ex.getMessage。当ex是RuntimeException时，
             * 这样处理会导致重复包装RuntimeException。
             * 现在直接就调用rethrowRuntimeException(Throwable ex)处理。
             */
            rethrowRuntimeException(ex);
        }
        /**
         * 由于系统中有很多地方rethrowRuntimeException的异常又被上层的函数再次rethrowRuntimeException，
         * 有时达到4~5此之多， 导致同一个异常在控制台可能输出4~5边，故不应该在这里log异常，而应该在需要的地方自己处理。
         */
        // log.warn(msg, ex);

        /**
         * 只有ex是RuntimeException的子类时才这样处理，如果ex是RuntimeException，那么走其他流程，因为
         * 由于ex可能是RuntimeException的子类，而我们没法设置ex的message，所以只能尽量用反射机制实现
         * 这个做法都是保证此函数再次抛出的异常和ex尽量类型一致，这样方便外部对异常的处理
         * 先试图用反射方法修改Throwable类内部的一个私有变量，如果修改成功，那么还是直接throw ex
         */
        if (ex instanceof RuntimeException
                && ex.getClass() != RuntimeException.class) {
            if (msg.equals(ex.getMessage())) throw (RuntimeException) ex;
            RuntimeException throwex = null;
            try {
                Field detailMessageField = Throwable.class
                        .getDeclaredField("detailMessage");
                detailMessageField.setAccessible(true);
                detailMessageField.set(ex, msg);
                throwex = (RuntimeException) ex;
                throwex.setStackTrace(ex.getStackTrace());
            } catch (Throwable ccex) {
                throwex = null;
            }
            if (throwex != null) {
                throw throwex;
            }
        }

        RuntimeException e = new RuntimeException(msg, ex);
        e.setStackTrace(ex.getStackTrace());
        throw e;
    }

    /**
     * 抛出一个新的Runtime异常，异常的堆栈信息于给定的ex一致，但message是指定的msg
     * 
     * @param ex
     * @param key
     *            异常信息的key
     * @param defaultValue
     *            异常信息的默认文字
     * @param params
     *            异常信息的格式化参数
     */
    static public void rethrowRuntimeException(Throwable ex, String key,
            String defaultValue, Object[] params){
        if (ex == null) {
            throwRuntimeException(key, defaultValue, params);
        }
        if (StrUtil.isNull(key)) {
            /**
             * 20080928 msg为空时：以前的做法是msg为空时取ex.getMessage。当ex是RuntimeException时，
             * 这样处理会导致重复包装RuntimeException。
             * 现在直接就调用rethrowRuntimeException(Throwable ex)处理。
             */
            rethrowRuntimeException(ex);
        }
        /**
         * 由于系统中有很多地方rethrowRuntimeException的异常又被上层的函数再次rethrowRuntimeException，
         * 有时达到4~5此之多， 导致同一个异常在控制台可能输出4~5边，故不应该在这里log异常，而应该在需要的地方自己处理。
         */
        // log.warn(msg, ex);

        RuntimeException e = new RuntimeException4I18N(key, defaultValue,
                params, ex);
        e.setStackTrace(ex.getStackTrace());
        throw e;
    }

    static public void rethrowRuntimeException(Throwable ex, String key,
            String defaultValue){
        rethrowRuntimeException(ex, key, defaultValue, null);
    }

    static public void rethrowRuntimeException(Throwable ex){
        /**
         * 20110317 ISSUE:BI-4300
         * 当主题域或主题集对应的数据库连接池连不上时为何提示“java.lang.RuntimeException”？
         * 原因：在执行反射操作时如果被调用的函数抛出了异常，jdk会自动把它包装成InvocationTargetException。
         * 本函数又会把InvocationTargetException包装成RuntimeException，这样导致真正的异常被掩埋。
         * 解决办法：遇到这种异常时，应该直接抛出原有异常。
         */
        if (ex instanceof InvocationTargetException) {
            ex = ((InvocationTargetException) ex).getTargetException();
        }

        /**
         * 由于系统中有很多地方rethrowRuntimeException的异常又被上层的函数再次rethrowRuntimeException，
         * 有时达到4~5此之多， 导致同一个异常在控制台可能输出4~5边，故不应该在这里log异常，而应该在需要的地方自己处理。
         */
        // log.warn(ex.getMessage(),ex);
        if (ex instanceof RuntimeException) throw (RuntimeException) ex;

        /*
         * 如果抛出的是可国际化的异常，再抛runtime异常时，保留国际化信息
         */
        if (ex instanceof Exception4I18N) {
            Exception4I18N e = (Exception4I18N) ex;
            RuntimeException4I18N e18n = new RuntimeException4I18N(
                    e.getMessageKey(), e.getDefaultValue(), e.getParams());
            e18n.setStackTrace(e.getStackTrace());
            throw e18n;
        }

        RuntimeException e = new RuntimeException(ex.getMessage(), ex);
        e.setStackTrace(ex.getStackTrace());
        throw e;
    }

    /**
     * @deprecated 使用可以国际化的异常 抛出一个运行时异常
     * @param msg
     */
    public static void throwRuntimeException(String msg){
        /**
         * 由于系统中有很多地方rethrowRuntimeException的异常又被上层的函数再次rethrowRuntimeException，
         * 有时达到4~5此之多， 导致同一个异常在控制台可能输出4~5边，故不应该在这里log异常，而应该在需要的地方自己处理。
         */
        // log.warn(msg);
        throw new RuntimeException(msg);
    }

    public static void throwRuntimeException(String key, String defaultValue,
            Object[] params){
        /**
         * 由于系统中有很多地方rethrowRuntimeException的异常又被上层的函数再次rethrowRuntimeException，
         * 有时达到4~5此之多， 导致同一个异常在控制台可能输出4~5边，故不应该在这里log异常，而应该在需要的地方自己处理。
         */
        // log.warn(msg);
        throw new RuntimeException4I18N(key, defaultValue, params);
    }

    public static void throwRuntimeException(String key, String defaultValue){
        /**
         * 由于系统中有很多地方rethrowRuntimeException的异常又被上层的函数再次rethrowRuntimeException，
         * 有时达到4~5此之多， 导致同一个异常在控制台可能输出4~5边，故不应该在这里log异常，而应该在需要的地方自己处理。
         */
        // log.warn(msg);
        throw new RuntimeException4I18N(key, defaultValue);
    }

    /**
     * 返回一个异常的根异常
     * 
     * @param ex
     * @return
     */
    static public Throwable getExceptionRoot(Throwable ex){
        Throwable r = ex;
        while (r != null) {
            if (r.getCause() == null) {
                return r;
            }
            r = r.getCause();
        }
        return ex;
    }
}
