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

/**
 * Created by albieliang on 2017/5/20.
 */

class IPCInvokerThreadPool {

    private static IPCInvokerThreadPool sThreadPool;
    
    private Handler mHandler;
    
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
        HandlerThread thread = new HandlerThread("IPCThreadPool#" + hashCode());
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }
    
    public static boolean post(Runnable run) {
        if (run == null) {
            return false;
        }
        return getImpl().getHandler().post(run);
    }
    
    public static boolean postDelayed(Runnable run, long delayMillis) {
        if (run == null) {
            return false;
        }
        return getImpl().getHandler().postDelayed(run, delayMillis);
    }
    
    private Handler getHandler() {
        // TODO: 2017/5/20 albielaing, add strategy here.
        return mHandler;
    }
}
