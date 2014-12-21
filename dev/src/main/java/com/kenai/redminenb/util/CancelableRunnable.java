
package com.kenai.redminenb.util;

import org.openide.util.Cancellable;

public class CancelableRunnable implements Runnable, Cancellable {
    private Thread runningThread;
    protected boolean canceled = false;
    
    @Override
    public void run() {
        runningThread = Thread.currentThread();
    }

    @Override
    public boolean cancel() {
        canceled = true;
        runningThread.interrupt();
        return true;
    }
    
}
