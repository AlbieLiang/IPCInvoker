/*
 *  Copyright (C) 2017-present Albie Liang. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package cc.suitalk.ipcinvoker;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import cc.suitalk.ipcinvoker.activate.ExecutorServiceCreator;
import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.reflect.ReflectStaticFieldSmith;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/5/20.
 */

class ThreadPool {

    private static final String TAG = "IPC.ThreadPool";

    private static volatile ThreadPool sThreadPool;

    private static ExecutorServiceCreator sCreator = new ExecutorServiceCreatorImpl();

    private static Thread.UncaughtExceptionHandler sUncaughtExceptionHandler = new UncaughtExceptionHandlerImpl();

    private Handler mHandler;

    ExecutorService mExecutorService;

    private static ThreadPool getImpl() {
        if (sThreadPool == null) {
            synchronized (ThreadPool.class) {
                if (sThreadPool == null) {
                    sThreadPool = new ThreadPool();
                }
            }
        }
        return sThreadPool;
    }

    static ThreadPool newInstance() {
        return new ThreadPool();
    }

    static void setExecutorServiceCreator(ExecutorServiceCreator creator) {
        if (creator == null) {
            return;
        }
        sCreator = creator;
    }

    private ThreadPool() {
        mHandler = createHandler();
        mExecutorService = sCreator.create();
        Log.i(TAG, "initialize IPCInvoker ThreadPool(hashCode : %s)", hashCode());
    }

    private Handler createHandler() {
        final HandlerThread handlerThread = new HandlerThread("ThreadPool#WorkerThread-" + hashCode());
        handlerThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                sUncaughtExceptionHandler.uncaughtException(t, e);
                Log.i(TAG, "uncaughtException occurred, create a new HandlerThread.");
                mHandler = createHandler();
            }
        });
        handlerThread.start();
        Log.i(TAG, "createHandlerThread(id : %d)", handlerThread.getThreadId());
        return new Handler(handlerThread.getLooper());
    }
    
    public static boolean post(Runnable run) {
        if (run == null) {
            return false;
        }
        getImpl().mExecutorService.execute(run);
        return true;
    }
    
    public static boolean postDelayed(Runnable run, long delayMillis) {
        if (run == null) {
            return false;
        }
        return getImpl().mHandler.postDelayed(run, delayMillis);
    }

    private static class ExecutorServiceCreatorImpl implements ExecutorServiceCreator {

        private static final String TAG = "IPC.ExecutorServiceCreatorImpl";

        private static final int DEFAULT_CORE_POOL_SIZE = 3;

        private int mCorePoolSize = DEFAULT_CORE_POOL_SIZE;

        HandlerThread mHandlerThread;

        ExecutorServiceCreatorImpl() {
            mHandlerThread = createHandlerThread();
        }

        @Override
        public ExecutorService create() {
            ThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(mCorePoolSize, new ThreadFactory() {

                int index = 0;

                @Override
                public Thread newThread(@NonNull final Runnable r) {
                    String name = "ThreadPool#Thread-" + (index++);
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ThreadLocal<Looper> tl = new ReflectStaticFieldSmith<ThreadLocal<Looper>>(Looper.class, "sThreadLocal").getWithoutThrow();
                            if (tl != null && tl.get() == null) {
                                Log.d(TAG, "create a new Looper ThreadLocal variable.");
                                tl.set(mHandlerThread.getLooper());
                            } else {
                                Log.d(TAG, "ThreadLocal Looper variable is null or has set.(%s)", tl);
                            }
                            r.run();
                        }
                    }, name);
                    thread.setUncaughtExceptionHandler(sUncaughtExceptionHandler);
                    Log.i(TAG, "newThread(thread : %s)", name);
                    return thread;
                }
            });
            executor.setMaximumPoolSize((int) (mCorePoolSize * 1.5));
            executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    Log.w(TAG, "on rejectedExecution(r : %s)", r);
                }
            });
            return executor;
        }

        private HandlerThread createHandlerThread() {
            final HandlerThread handlerThread = new HandlerThread("ThreadPool#InnerWorkerThread-" + hashCode());
            handlerThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    sUncaughtExceptionHandler.uncaughtException(t, e);
                    Log.i(TAG, "uncaughtException occurred, create a new HandlerThread.");
                    mHandlerThread = createHandlerThread();
                }
            });
            handlerThread.start();
            Log.i(TAG, "createHandlerThread(id : %d)", handlerThread.getThreadId());
            return handlerThread;
        }

    }

    private static class UncaughtExceptionHandlerImpl implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Log.e(TAG, "uncaughtException(thread : %d), %s", t.getId(), e);
        }
    }
}
