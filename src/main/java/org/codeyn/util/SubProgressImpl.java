package org.codeyn.util;

import org.codeyn.util.progress.IProgress;
import org.codeyn.util.progress.ProgressDefault;


/**
 * 本进度可以设置一个它依附的进度：
 * 1、本进度的日志会加入到依附的进度中，但是依附的进度的日志不加入到本进度。
 * 2、停止本进度时，不会影响依附的进度，但是停止依附的进度时，会停止本进度。
 *
 * @author xh
 */
public class SubProgressImpl extends ProgressDefault {
    protected IProgress owner;

    public SubProgressImpl(IProgress owner, String nm) {
        super(nm);
        this.owner = owner;
    }

    public synchronized void addLog(String log) {
        super.addLog(log);
        if (owner != null) owner.addLog(log);
    }

    public synchronized boolean isCancel() {
        return super.isCancel() || (owner != null && owner.isCancel());
    }

    public void setLastLog(String log) {
        super.setLastLog(log);
        if (owner != null) owner.setLastLog(log);
    }

    public synchronized void setMessage(String msg) {
        super.setMessage(msg);
        if (owner != null) owner.setMessage(msg);
    }

    public void showException(Exception e) {
        super.showException(e);
        if (owner != null) owner.showException(e);
    }
}
