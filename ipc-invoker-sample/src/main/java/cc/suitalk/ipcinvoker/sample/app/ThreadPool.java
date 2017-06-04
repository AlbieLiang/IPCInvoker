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

package cc.suitalk.ipcinvoker.sample.app;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by albieliang on 2017/6/3.
 */

public class ThreadPool {

    Handler mHandler;

    private static ThreadPool sImpl;

    private static ThreadPool getImpl() {
        if (sImpl == null) {
            synchronized (ThreadPool.class) {
                if (sImpl == null) {
                    sImpl = new ThreadPool();
                }
            }
        }
        return sImpl;
    }

    private ThreadPool() {
        HandlerThread thread = new HandlerThread("IPCInvoker-Sample#WorkerThread");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    public static void post(Runnable runnable) {
        getImpl().mHandler.post(runnable);
    }

}
