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

import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.RestrictTo;

import cc.suitalk.ipcinvoker.aidl.AIDL_IPCInvokeBridge;
import cc.suitalk.ipcinvoker.aidl.AIDL_IPCInvokeCallback;
import cc.suitalk.ipcinvoker.annotation.AnyThread;
import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.annotation.WorkerThread;
import cc.suitalk.ipcinvoker.exception.OnExceptionObserver;
import cc.suitalk.ipcinvoker.recycle.ObjectRecycler;
import cc.suitalk.ipcinvoker.recycle.Recyclable;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2018/5/7.
 */

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class IPCTaskExecutor {

    private static final String TAG = "IPC.IPCTaskExecutor";

    /**
     * Async invoke, it can be invoked on any thread.
     *
     * @param process      remote service process name
     * @param data         data for remote process invoked, it must be a {@link Parcelable}
     * @param taskClass    remote invoke logic task class
     * @param callback     callback on current process after IPC invoked finished and initiative callback.
     * @param extInfo      {@link IPCTaskExtInfo} for current IPC invoked.
     * @param <T>          the class implements {@link IPCAsyncInvokeTask} interface
     * @param <InputType>  the class extends {@link Parcelable}
     * @param <ResultType> the class extends {@link Parcelable}
     * @return true if cross-process invoke has been initiated, false otherwise.
     */
    @AnyThread
    public static <T extends IPCAsyncInvokeTask<InputType, ResultType>, InputType extends Parcelable, ResultType extends Parcelable>
            boolean invokeAsync(final String process, final InputType data, @NonNull final Class<T> taskClass,
                                final IPCInvokeCallback<ResultType> callback, @NonNull final IPCTaskExtInfo<ResultType> extInfo) {
        if (process == null || process.length() == 0) {
            if (extInfo.hasDefaultResult() && callback != null) {
                callback.onCallback(extInfo.getDefaultResult());
                return true;
            }
            Log.e(TAG, "invokeAsync failed, process is null or nil.");
            return false;
        }
        if (taskClass == null) {
            if (extInfo.hasDefaultResult() && callback != null) {
                callback.onCallback(extInfo.getDefaultResult());
                return true;
            }
            Log.e(TAG, "invokeAsync failed, taskClass is null(process : %s).", process);
            return false;
        }
        return ThreadCaller.execute(new Runnable() {
            @Override
            public void run() {
                if (IPCInvokeLogic.isCurrentProcess(process)) {
                    IPCAsyncInvokeTask task = ObjectStore.get(taskClass, IPCAsyncInvokeTask.class);
                    if (task == null) {
                        if (extInfo.hasDefaultResult() && callback != null) {
                            callback.onCallback(extInfo.getDefaultResult());
                            return;
                        }
                        Log.e(TAG, "invokeAsync failed, newInstance(%s) return null.", taskClass);
                        return;
                    }
                    task.invoke(data, callback);
                    return;
                }
                AIDL_IPCInvokeBridge bridge = IPCBridgeManager.getImpl().getIPCBridge(process, extInfo);
                if (bridge == null) {
                    if (extInfo.hasDefaultResult() && callback != null) {
                        callback.onCallback(extInfo.getDefaultResult());
                        return;
                    }
                    Log.e(TAG, "invokeAsync failed, get bridge is null by process(%s).", process);
                    return;
                }
                try {
                    AIDL_IPCInvokeCallback invokeCallback = null;
                    if (callback != null) {
                        invokeCallback = new IPCInvokeCallbackWrapper(process, callback);
                    }
                    bridge.invokeAsync(buildBundle(data, taskClass), taskClass.getName(), invokeCallback);
                    return;
                } catch (Exception e) {
                    Log.d(TAG, "invokeAsync failed, ipc invoke error : %s", e);
                    final OnExceptionObserver onExceptionObserver = extInfo.getOnExceptionObserver();
                    if (onExceptionObserver != null) {
                        onExceptionObserver.onExceptionOccur(e);
                    }
                    GlobalExceptionManager.dispatchException(e);
                }
                if (extInfo.hasDefaultResult() && callback != null) {
                    callback.onCallback(extInfo.getDefaultResult());
                }
            }
        });
    }

    /**
     * Sync invoke, it must be invoked on WorkerThread or make sure the connection is established before invoked.
     *
     * Call {@link IPCInvoker#connectRemoteService(String)} to pre-connect remote Service.
     *
     * @param process       remote service process name
     * @param data          data for remote process invoked, it must be a {@link Parcelable}
     * @param taskClass     remote invoke logic task class
     * @param extInfo       {@link IPCTaskExtInfo} for current IPC invoked
     * @param <T>           the class implements {@link IPCSyncInvokeTask} interface
     * @param <InputType>   the class extends {@link Parcelable}
     * @param <ResultType>  the class extends {@link Parcelable}
     * @return the cross-process invoke result.
     */
    @WorkerThread
    public static <T extends IPCSyncInvokeTask<InputType, ResultType>, InputType extends Parcelable, ResultType extends Parcelable>
            ResultType invokeSync(String process, InputType data, @NonNull Class<T> taskClass, @NonNull IPCTaskExtInfo<ResultType> extInfo) {
        if (process == null || process.length() == 0) {
            Log.e(TAG, "invokeSync failed, process is null or nil.");
            return extInfo.getDefaultResult();
        }
        if (taskClass == null) {
            Log.e(TAG, "invokeSync failed, taskClass is null(process : %s).", process);
            return extInfo.getDefaultResult();
        }
        if (IPCInvokeLogic.isCurrentProcess(process)) {
            IPCSyncInvokeTask task = ObjectStore.get(taskClass, IPCSyncInvokeTask.class);
            if (task == null) {
                Log.e(TAG, "get IPC task by class '%s', got null.", taskClass);
                return extInfo.getDefaultResult();
            }
            return (ResultType) task.invoke(data);
        }
        AIDL_IPCInvokeBridge bridge = IPCBridgeManager.getImpl().getIPCBridge(process, extInfo);
        if (bridge == null) {
            Log.e(TAG, "invokeSync failed, get bridge is null by process(%s).", process);
            return extInfo.getDefaultResult();
        }
        try {
            Bundle resData = bridge.invokeSync(buildBundle(data, taskClass), taskClass.getName());
            if (resData == null) {
                return extInfo.getDefaultResult();
            }
            resData.setClassLoader(IPCInvoker.class.getClassLoader());
            return (ResultType) resData.getParcelable(BaseIPCService.INNER_KEY_REMOTE_TASK_RESULT_DATA);
        } catch (Exception e) {
            Log.d(TAG, "invokeSync failed, ipc invoke error : %s", e);
            final OnExceptionObserver onExceptionObserver = extInfo.getOnExceptionObserver();
            if (onExceptionObserver != null) {
                onExceptionObserver.onExceptionOccur(e);
            }
            GlobalExceptionManager.dispatchException(e);
        }
        return extInfo.getDefaultResult();
    }

    private static Bundle buildBundle(Parcelable parcelable, Class<?> taskClass) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BaseIPCService.INNER_KEY_REMOTE_TASK_DATA, parcelable);
//        bundle.putString(BaseIPCService.INNER_KEY_REMOTE_TASK_CLASS, taskClass.getName());
        return bundle;
    }

    private static class IPCInvokeCallbackWrapper extends AIDL_IPCInvokeCallback.Stub implements Recyclable {

        private static final String TAG = "IPC.IPCInvokeCallbackWrapper";

        String mProcess;
        IPCInvokeCallback mCallback;

        IPCInvokeCallbackWrapper(@NonNull String process, @NonNull IPCInvokeCallback callback) {
            this.mCallback = callback;
            this.mProcess = process;
            ObjectRecycler.addIntoSet(process, this);
            Log.i(TAG, "keep ref of callback(%s)", callback.hashCode());
        }

        @Override
        public void onCallback(Bundle data) throws RemoteException {
            try {
                final IPCInvokeCallback callback = this.mCallback;
                if (callback == null) {
                    Log.w(TAG, "callback failed, ref has been release");
                    return;
                }
                if (data == null) {
                    callback.onCallback(null);
                    return;
                }
                data.setClassLoader(IPCInvoker.class.getClassLoader());
                boolean releaseRef = data.getBoolean(BaseIPCService.INNER_KEY_COMMAND_RELEASE_REF);
                if (releaseRef) {
                    Log.d(TAG, "release ref of callback(%s)", callback.hashCode());
                    recycle();
                    return;
                }
                callback.onCallback(data.getParcelable(BaseIPCService.INNER_KEY_REMOTE_TASK_RESULT_DATA));
            } catch (Exception e) {
                // TODO: 2020-05-26 albieliang 
                Log.e(TAG, "onCallback error, %s", android.util.Log.getStackTraceString(e));
                GlobalExceptionManager.dispatchException(e);
            }
        }

        @Override
        public void recycle() {
            mCallback = null;
            ObjectRecycler.removeFromSet(mProcess, this);
        }

        @Override
        protected void finalize() throws Throwable {
            recycle();
            Log.d(TAG, "finalize(%s)", hashCode());
            super.finalize();
        }
    }

}
