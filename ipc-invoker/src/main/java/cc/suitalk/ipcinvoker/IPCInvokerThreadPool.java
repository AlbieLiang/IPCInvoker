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
import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/5/20.
 */

class IPCInvokerThreadPool {

    private static final String TAG = "IPC.IPCInvokerThreadPool";

    private static final int DEFAULT_CORE_POOL_SIZE = 3;

    private static IPCInvokerThreadPool sThreadPool;
    
    private Handler mHandler;
    private ExecutorService mExecutorService;
    private int mCorePoolSize = DEFAULT_CORE_POOL_SIZE;
    
    private static IPCInvokerThreadPool getImpl() {
        if (sThreadPool == null) {
            synchronized (IPCInvokerThreadPool.class) {
                if (sThreadPool == null) {
                    sThreadPool = new IPCInvokerThreadPool();
                }
            }
        }
        return sThreadPool;
    }
    
    private IPCInvokerThreadPool() {
        HandlerThread thread = new HandlerThread("IPCInvokerThreadPool#WorkerThread-" + hashCode());
        thread.start();
        mHandler = new Handler(thread.getLooper());
        mExecutorService = Executors.newScheduledThreadPool(mCorePoolSize, new ThreadFactory() {

            int index = 0;
            @Override
            public Thread newThread(@NonNull final Runnable r) {
                String name = "IPCInvokerThreadPool#Thread-" + (index++);
                HandlerThread thread = new HandlerThread(name) {
                    @Override
                    protected void onLooperPrepared() {
                        r.run();
                    }
                };
                Log.i(TAG, "newThread(thread : %s)", name);
                return thread;
            }
        });
        Log.i(TAG, "initialize IPCInvoker ThreadPool(hashCode : %s)", hashCode());
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
}
