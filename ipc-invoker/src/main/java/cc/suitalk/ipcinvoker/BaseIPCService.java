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

import cc.suitalk.ipcinvoker.aidl.AIDL_IPCInvokeBridge;
import cc.suitalk.ipcinvoker.aidl.AIDL_IPCInvokeCallback;
import cc.suitalk.ipcinvoker.annotation.Nullable;
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
            IPCAsyncInvokeTask task = ObjectStore.get(clazz, IPCAsyncInvokeTask.class);
            if (task == null) {
                Log.e(TAG, "invokeAsync failed, can not newInstance by class %s.", clazz);
                return;
            }
            final IPCAsyncInvokeTask finalTask = task;
            if (data != null) {
                data.setClassLoader(BaseIPCService.class.getClassLoader());
            }
            ThreadPool.post(new Runnable() {
                @Override
                public void run() {
                    finalTask.invoke(data, new IPCInvokeCallback() {
                        @Override
                        public void onCallback(Bundle data) {
                            if (callback != null) {
                                try {
                                    if (data != null) {
                                        data.setClassLoader(BaseIPCService.class.getClassLoader());
                                    }
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
            IPCSyncInvokeTask task = ObjectStore.get(clazz, IPCSyncInvokeTask.class);
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
        Log.i(TAG, "onBind(%s), killSelf(%s)", intent, mNeedKillSelf);
        if (mNeedKillSelf) {
            Log.i(TAG, "need to kill self, return null Binder object.");
            return null;
        }
        IPCServiceManager.getImpl().put(IPCInvokeLogic.getCurrentProcessName(), this);
        mHasConnectting = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind(%s)", intent);
        boolean r = super.onUnbind(intent);
        mHasConnectting = false;
//        tryToKillSelf();
        return r;
    }

    public abstract String getProcessName();

    public void killSelf() {
        tryToKillSelf(true);
    }

    public void abortKillSelf() {
        Log.i(TAG, "abortKillSelf(%s)", getProcessName());
        mNeedKillSelf = false;
    }

    public void tryToKillSelf() {
        tryToKillSelf(false);
    }

    private void tryToKillSelf(boolean force) {
        if (mHasConnectting && !force) {
            Log.i(TAG, "abort kill self(%s), the service was connected by other process.", getProcessName());
            return;
        }
        Log.i(TAG, "kill self(%s)", getProcessName());
        mNeedKillSelf = true;
        IPCBridgeManager.getImpl().lockCreateBridge(true);
        IPCBridgeManager.getImpl().releaseAllIPCBridge();
        stopSelf();
        IPCServiceManager.getImpl().remove(getProcessName());
        ThreadPool.postDelayed(new Runnable() {
            @Override
            public void run() {
                Process.killProcess(Process.myPid());
            }
        }, 2 * 1000);
    }
}
