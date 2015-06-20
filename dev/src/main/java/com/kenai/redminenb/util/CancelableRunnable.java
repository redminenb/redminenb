
package com.kenai.redminenb.util;

import org.openide.util.Cancellable;

public abstract class CancelableRunnable implements Runnable, Cancellable {
    private volatile Thread runningThread;
    
    @Override
    public final void run() {
        runningThread = Thread.currentThread();
        try {
            guardedRun();
        } catch (RuntimeException ex) {
            throw ex;
        }
        runningThread = null;
    }
    
    protected abstract void guardedRun();

    @Override
    public boolean cancel() {
        Thread thread = runningThread;
        if(thread != null) {
            thread.interrupt();
        }
        return true;
    }
    
}
