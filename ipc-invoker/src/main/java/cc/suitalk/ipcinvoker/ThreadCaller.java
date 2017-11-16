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

/**
 * Created by albieliang on 2017/6/8.
 */

public class ThreadCaller {

    private static volatile ThreadCaller sImpl;

    private HandlerThread mHandlerThread;

    private Handler mHandler;
    private Handler mUiThreadHandler;

    private ThreadPool mThreadPool;

    private static ThreadCaller getImpl() {
        if (sImpl == null) {
            synchronized (ThreadCaller.class) {
                if (sImpl == null) {
                    sImpl = new ThreadCaller();
                }
            }
        }
        return sImpl;
    }

    private ThreadCaller() {
        mHandlerThread = new HandlerThread("ThreadCaller#Worker-" + hashCode());
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mUiThreadHandler = new Handler(Looper.getMainLooper());
        mThreadPool = ThreadPool.newInstance();
    }

    public static HandlerThread getWorkerThread() {
        return getImpl().mHandlerThread;
    }

    /**
     * Post task to the worker thread. That only one worker thread in the ThreadCaller.
     *
     * @param r Runnable task
     * @return true post task success, otherwise false.
     *
     * @see #execute(Runnable)
     * @see #postDelayed(Runnable, long)
     */
    public static boolean post(Runnable r) {
        return getImpl().mHandler.post(r);
    }

    public static boolean postDelayed(Runnable r, long delayMillis) {
        return getImpl().mHandler.postDelayed(r, delayMillis);
    }

    public static boolean post(boolean runOnUiThread, Runnable r) {
        if (runOnUiThread) {
            return getImpl().mUiThreadHandler.post(r);
        }
        return getImpl().mHandler.post(r);
    }

    public static boolean postDelayed(boolean runOnUiThread, Runnable r, long delayMillis) {
        if (runOnUiThread) {
            return getImpl().mUiThreadHandler.postDelayed(r, delayMillis);
        }
        return getImpl().mHandler.postDelayed(r, delayMillis);
    }

    public static void removeCallbacks(Runnable r) {
        getImpl().mHandler.removeCallbacks(r);
    }

    public static void removeCallbacks(boolean runOnUiThread, Runnable r) {
        if (runOnUiThread) {
            getImpl().mUiThreadHandler.removeCallbacks(r);
            return;
        }
        getImpl().mHandler.removeCallbacks(r);
    }

    /**
     * Execute task on ThreadPool.
     *
     * @param r runnable task
     * @return true the task was post into the queue, otherwise false.
     */
    public static boolean execute(Runnable r) {
        if (r == null) {
            return false;
        }
        getImpl().mThreadPool.mExecutorService.execute(r);
        return true;
    }
}
