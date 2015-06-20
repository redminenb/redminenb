
package com.kenai.redminenb.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class CancelableRunnableWrapper extends CancelableRunnable {
    private Runnable backingRunnable;

    public CancelableRunnableWrapper() {
    }
    
    public Runnable getBackingRunnable() {
        return backingRunnable;
    }

    public void setBackingRunnable(Runnable backingRunnable) {
        this.backingRunnable = backingRunnable;
    }

    @Override
    @SuppressFBWarnings(value="RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE",
            justification = "The null check should be not necessary, but a developer could invoke this without having checked it in an development run.")
    protected final void guardedRun() {
        assert backingRunnable != null;
        if(backingRunnable != null) {
            backingRunnable.run();
        }
    }
}
