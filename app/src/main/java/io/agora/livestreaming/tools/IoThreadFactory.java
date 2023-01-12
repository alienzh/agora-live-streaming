package io.agora.livestreaming.tools;

import android.os.Process;

import java.util.concurrent.ThreadFactory;

/**
 *  @author create by zhangwei03
 *
 * the factory to use when the executor creates a new thread
 */
public class IoThreadFactory implements ThreadFactory {
    private final int mThreadPriority;

    public IoThreadFactory(int threadPriority) {
        mThreadPriority = threadPriority;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Runnable wrapperRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(mThreadPriority);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                runnable.run();
            }
        };
        return new Thread(wrapperRunnable);
    }
}
