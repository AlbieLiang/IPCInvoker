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

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import cc.suitalk.ipcinvoker.aidl.AIDL_IPCInvokeBridge;
import cc.suitalk.ipcinvoker.aidl.AIDL_IPCInvokeCallback;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/5/13.
 */

public abstract class BaseIPCService extends Service {

    private static final String TAG = "IPC.BaseIPCService";

    private volatile boolean mNeedKillSelf;

    private volatile boolean mHasConnectting;

    private AIDL_IPCInvokeBridge.Stub mBinder = new AIDL_IPCInvokeBridge.Stub() {

        @Override
        public void invokeAsync(final Bundle data, final String clazz, final AIDL_IPCInvokeCallback callback) throws RemoteException {
            if (clazz == null || clazz.length() == 0) {
                Log.e(TAG, "invokeAsync failed, class is null or nil.");
                return;
            }
            IPCAsyncInvokeTask task = IPCReflectUtil.newInstance(clazz, IPCAsyncInvokeTask.class);
            if (task == null) {
                Log.e(TAG, "invokeAsync failed, can not newInstance by class %s.", clazz);
                return;
            }
            final IPCAsyncInvokeTask finalTask = task;
            if (data != null) {
                data.setClassLoader(BaseIPCService.class.getClassLoader());
            }
            IPCInvokerThreadPool.post(new Runnable() {
                @Override
                public void run() {
                    finalTask.invoke(data, new IPCInvokeCallback() {
                        @Override
                        public void onCallback(Bundle data) {
                            if (callback != null) {
                                try {
                                    callback.onCallback(data);
                                } catch (RemoteException e) {
                                    Log.e(TAG, "%s", e);
                                }
                            }
                        }
                    });
                }
            });
            return;
        }

        @Override
        public Bundle invokeSync(Bundle data, String clazz) throws RemoteException {
            if (clazz == null || clazz.length() == 0) {
                Log.e(TAG, "invokeAsync failed, class is null or nil.");
                return null;
            }
            IPCSyncInvokeTask task = IPCReflectUtil.newInstance(clazz, IPCSyncInvokeTask.class);
            if (task == null) {
                Log.e(TAG, "invokeSync failed, can not newInstance by class %s.", clazz);
                return null;
            }
            if (data != null) {
                data.setClassLoader(BaseIPCService.class.getClassLoader());
            }
            return task.invoke(data);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind(%s)", intent);
        IPCServiceManager.getImpl().put(IPCInvokeLogic.getCurrentProcessName(), this);
        mHasConnectting = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind(%s)", intent);
        boolean r = super.onUnbind(intent);
        mHasConnectting = false;
        tryToKillSelf();
        return r;
    }

    public abstract String getProcessName();

    public void killSelf() {
        mNeedKillSelf = true;
        tryToKillSelf();
    }

    public void abortKillSelf() {
        mNeedKillSelf = false;
    }

    private void tryToKillSelf() {
        if (mHasConnectting || !mNeedKillSelf) {
            return;
        }
        stopSelf();
        IPCServiceManager.getImpl().remove(getProcessName());
        IPCInvokerThreadPool.postDelayed(new Runnable() {
            @Override
            public void run() {
                Process.killProcess(Process.myPid());
            }
        }, 2 * 1000);
    }
}
