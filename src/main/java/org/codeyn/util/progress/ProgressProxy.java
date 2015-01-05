package org.codeyn.util.progress;

import org.codeyn.util.exception.CancelException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */


public class ProgressProxy
    implements IProgress {
  private IProgress i;
  public ProgressProxy(IProgress i) {
    this.i = i;
  }

  public void setMessage(String msg) {
    if (i != null) {
      i.setMessage(msg);
    }
  }

  public void addLog(String log) {
    if (i != null) {
      i.addLog(log);
    }
  }
  
  public void setLastLog(String log) {
    if (i != null) {
      i.setLastLog(log);
    }
  }

  public void setProgress(int min, int max, int step) {
    if (i != null) {
      i.setProgress(min, max, step);
    }
  }

  public void setPosition(int p) {
    if (i != null) {
      i.setPosition(p);
    }
  }

  public void step(int st) {
    if (i != null) {
      i.step(st);
    }
  }

  public void stepit() {
    if (i != null) {
      i.stepit();
    }
  }

  public boolean isCancel() {
    if (i != null) {
      return i.isCancel();
    }
    else {
      return false;
    }
  }

  public void checkCancel() throws CancelException {
    if (i != null) {
      i.checkCancel();
    }
  }

  /**
   * showException
   *
   * @param e Exception
   */
  public void showException(Exception e) {
    if (i != null) {
      i.showException(e);
    }

  }

  public String getLogs() {
    if (i != null) {
      return i.getLogs();
    }
    return null;
  }

  public void addLogWithTime(String log) {
    if (i != null) {
      i.addLogWithTime(log);
    }
  }

	public String getLastLog() {
		if (i != null) {
      return i.getLastLog();
    }
		
		return null;
	}

  public int getLogCount() {
    if (i != null) {
      return i.getLogCount();
    }
    return 0;
  }

  public String getLog(int i) {
    if (this.i != null) {
      return this.i.getLog(i);
    }
    return null;
  }

  public void setLastLogWithTime(String log) {
    if (this.i != null) {
      this.i.setLastLogWithTime(log);
    }
  }

public void setCancel(boolean value,String msg) {
	if(this.i!=null){
		this.i.setCancel(value,msg);
	}
	
}

  public int getStep() {
    return this.i.getStep();
  }

  public int getMin() {
    return this.i.getMin();
  }

  public int getMax() {
    return this.i.getMax();
  }

  public int getPosition() {
    return this.i.getPosition();
  }

  public String getMessage() {
    return this.i.getMessage();
  }

  public void setFinished() {
    this.i.setFinished();
  }

  public boolean isFinshed() {
    return this.i.isFinshed();
  }

}
