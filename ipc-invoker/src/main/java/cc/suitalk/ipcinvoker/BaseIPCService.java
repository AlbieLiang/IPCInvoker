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
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;

import java.util.LinkedList;
import java.util.List;

import cc.suitalk.ipcinvoker.aidl.AIDL_IPCInvokeBridge;
import cc.suitalk.ipcinvoker.aidl.AIDL_IPCInvokeCallback;
import cc.suitalk.ipcinvoker.annotation.Nullable;
import cc.suitalk.ipcinvoker.exception.OnExceptionObservable;
import cc.suitalk.ipcinvoker.exception.OnExceptionObserver;
import cc.suitalk.ipcinvoker.recycle.ObjectRecycler;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/5/13.
 */

public abstract class BaseIPCService extends Service {

    private static final String TAG = "IPC.BaseIPCService";

    protected static final String INNER_KEY_REMOTE_TASK_DATA = "__remote_task_data";
    protected static final String INNER_KEY_REMOTE_TASK_RESULT_DATA = "__remote_task_result_data";
    protected static final String INNER_KEY_COMMAND_RELEASE_REF = "__command_release_ref";

    private volatile boolean mNeedKillSelf;

    private volatile boolean mHasConnectting;

    private AIDL_IPCInvokeBridge.Stub mBinder = new AIDL_IPCInvokeBridge.Stub() {

        @Override
        public void invokeAsync(final Bundle data, final String clazz, final AIDL_IPCInvokeCallback callback) throws RemoteException {
            if (clazz == null || clazz.length() == 0) {
                Log.e(TAG, "invokeAsync failed, class is null or nil.");
                return;
            }
            if (data == null) {
                Log.e(TAG, "invokeAsync failed, data is null.");
                return;
            }
            data.setClassLoader(BaseIPCService.class.getClassLoader());
            final Parcelable remoteData = data.getParcelable(INNER_KEY_REMOTE_TASK_DATA);
            IPCAsyncInvokeTask task = ObjectStore.get(clazz, IPCAsyncInvokeTask.class);
            if (task == null) {
                Log.e(TAG, "invokeAsync failed, can not newInstance by class %s.", clazz);
                return;
            }
            final IPCAsyncInvokeTask finalTask = task;
            ThreadPool.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        finalTask.invoke(remoteData, new IPCInvokeCallbackProxy(callback));
                    } catch (Exception e) {
                        // TODO: 2020-05-26 albieliang
                        Log.e(TAG, "invokeAsync error, %s'", android.util.Log.getStackTraceString(e));
                    }
                }
            });
            return;
        }

        @Override
        public Bundle invokeSync(Bundle data, String clazz) throws RemoteException {
            if (clazz == null || clazz.length() == 0) {
                Log.e(TAG, "invokeSync failed, class is null or nil.");
                return null;
            }
            if (data == null) {
                Log.e(TAG, "invokeSync failed, data is null.");
                return null;
            }
            IPCSyncInvokeTask<Parcelable, Parcelable> task = ObjectStore.get(clazz, IPCSyncInvokeTask.class);
            if (task == null) {
                Log.e(TAG, "invokeSync failed, can not newInstance by class %s.", clazz);
                return null;
            }
            data.setClassLoader(BaseIPCService.class.getClassLoader());
            final Parcelable remoteData = data.getParcelable(INNER_KEY_REMOTE_TASK_DATA);
            Bundle bundle = new Bundle();
            Parcelable result = null;
            try {
                result = task.invoke(remoteData);
            } catch (Exception e) {
                // TODO: 2020-05-26 albieliang
                Log.e(TAG, "invokeSync error, %s'", android.util.Log.getStackTraceString(e));
            }
            bundle.putParcelable(INNER_KEY_REMOTE_TASK_RESULT_DATA, result);
            return bundle;
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
        IPCServiceManager.getImpl().put(getProcessName(), this);
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

    private static class IPCInvokeCallbackProxy implements IPCInvokeCallback<Parcelable>, OnExceptionObservable {

        private static final String TAG = "IPC.IPCInvokeCallbackProxy";

        AIDL_IPCInvokeCallback callback;
        final List<OnExceptionObserver> observableList = new LinkedList<>();


        public IPCInvokeCallbackProxy(AIDL_IPCInvokeCallback callback) {
            this.callback = callback;
            if (callback != null) {
                Log.d(TAG, "keep ref of callback(%s)", callback.hashCode());
                ObjectRecycler.keepRef(callback);
            }
        }

        @Override
        public void onCallback(Parcelable data) {
            if (callback == null) {
                return;
            }
            Log.d(TAG, "onCallback(%s)", callback.hashCode());
            try {
                Bundle resData = new Bundle();
                resData.putParcelable(INNER_KEY_REMOTE_TASK_RESULT_DATA, data);
                callback.onCallback(resData);
            } catch (RemoteException e) {
                Log.e(TAG, "%s", android.util.Log.getStackTraceString(e));
                List<OnExceptionObserver> list = new LinkedList<>();
                synchronized (observableList) {
                    if (!observableList.isEmpty()) {
                        list.addAll(observableList);
                    }
                }
                for (OnExceptionObserver observable : list) {
                    observable.onExceptionOccur(e);
                }
            }
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                Log.d(TAG, "finalize(%s)", hashCode());
                if (callback != null) {
                    Log.d(TAG, "finalize, release callback(%s)", callback.hashCode());
                    ThreadPool.post(new ReleaseRefRunnable(callback));
                    callback = null;
                }
            } finally {
                super.finalize();
            }
        }

        @Override
        public void registerObserver(OnExceptionObserver observer) {
            if (observer == null) {
                return;
            }
            synchronized (observableList) {
                if (observableList.contains(observer)) {
                    return;
                }
                observableList.add(observer);
            }
        }

        @Override
        public void unregisterObserver(OnExceptionObserver observer) {
            if (observer == null) {
                return;
            }
            synchronized (observableList) {
                observableList.remove(observer);
            }
        }

        private static class ReleaseRefRunnable implements Runnable {

            private static final Bundle sReleaseRef = new Bundle();

            static {
                sReleaseRef.putBoolean(INNER_KEY_COMMAND_RELEASE_REF, true);
            }

            AIDL_IPCInvokeCallback callback;

            ReleaseRefRunnable(AIDL_IPCInvokeCallback callback) {
                this.callback = callback;
            }

            @Override
            public void run() {
                try {
                    Log.i(TAG, "notify release ref of callback(%s).", callback.hashCode());
                    callback.onCallback(sReleaseRef);
                    ObjectRecycler.releaseRef(callback);
                    callback = null;
                } catch (RemoteException e) {
                    Log.e(TAG, "notify release ref error, %s", android.util.Log.getStackTraceString(e));
                } catch (Exception e) {
                    Log.e(TAG, "notify release ref error, %s\n %s", e.getMessage(), android.util.Log.getStackTraceString(e));
                }
            }
        }
    }
}
