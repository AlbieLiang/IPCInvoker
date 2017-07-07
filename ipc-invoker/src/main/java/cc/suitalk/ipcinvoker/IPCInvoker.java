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
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import cc.suitalk.ipcinvoker.aidl.AIDL_IPCInvokeBridge;
import cc.suitalk.ipcinvoker.aidl.AIDL_IPCInvokeCallback;
import cc.suitalk.ipcinvoker.reflect.ReflectUtil;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/5/13.
 */

public class IPCInvoker {

    private static final String TAG = "IPC.IPCInvoker";

    private static final String INNER_KEY_REMOTE_TASK_CLASS = "__remote_task_class";
    private static final String INNER_KEY_REMOTE_TASK_DATA = "__remote_task_data";
    private static final String INNER_KEY_REMOTE_TASK_RESULT_DATA = "__remote_task_result_data";

    /**
     * Async invoke, it must be invoke on WorkerThread.
     *
     * @param process   remote service process name
     * @param data      data for remote process invoked
     * @param taskClass remote invoke logic task class
     * @param callback  callback on current process after IPC invoked finished and initiative callback.
     * @param <T>       the class implements {@link IPCAsyncInvokeTask} interface
     * @return true if cross-process invoke has been initiated, false otherwise.
     *
     * @see #invokeAsync(String, Parcelable, Class, IPCRemoteInvokeCallback)
     */
    @WorkerThread
    public static <T extends IPCAsyncInvokeTask> boolean invokeAsync(
            @NonNull String process, Bundle data, @NonNull Class<T> taskClass, final IPCInvokeCallback callback) {
        if (process == null || process.length() == 0) {
            Log.e(TAG, "invokeAsync failed, process is null or nil.");
            return false;
        }
        if (taskClass == null) {
            Log.e(TAG, "invokeAsync failed, task is null(process : %s).", process);
            return false;
        }
        if (IPCInvokeLogic.isCurrentProcess(process)) {
            IPCAsyncInvokeTask task = ReflectUtil.newInstance(taskClass, IPCAsyncInvokeTask.class);
            if (task == null) {
                Log.e(TAG, "invokeAsync failed, newInstance(%s) return null.", taskClass);
                return false;
            }
            task.invoke(data, callback);
            return true;
        }
        AIDL_IPCInvokeBridge bridge = IPCBridgeManager.getImpl().getIPCBridge(process);
        if (bridge == null) {
            Log.e(TAG, "invokeAsync failed, get bridge is null by process(%s).", process);
            return false;
        }
        try {
            AIDL_IPCInvokeCallback invokeCallback = null;
            if (callback != null) {
                invokeCallback = new AIDL_IPCInvokeCallback.Stub() {
                    @Override
                    public void onCallback(Bundle data) throws RemoteException {
                        if (data != null) {
                            data.setClassLoader(IPCInvoker.class.getClassLoader());
                        }
                        callback.onCallback(data);
                    }
                };
            }
            bridge.invokeAsync(data, taskClass.getName(), invokeCallback);
            return true;
        } catch (RemoteException e) {
            Log.d(TAG, "invokeAsync failed, ipc invoke error : %s", e);
        }
        return false;
    }

    /**
     * Async invoke, it must be invoke on WorkerThread.
     *
     * @param process      remote service process name
     * @param data         data for remote process invoked, it must be a {@link Parcelable}
     * @param taskClass    remote invoke logic task class
     * @param callback     callback on current process after IPC invoked finished and initiative callback.
     * @param <T>          the class implements {@link IPCRemoteAsyncInvoke} interface
     * @param <InputType>  the class extends {@link Parcelable}
     * @param <ResultType> the class extends {@link Parcelable}
     * @return true if cross-process invoke has been initiated, false otherwise.
     *
     * @see #invokeAsync(String, Bundle, Class, IPCInvokeCallback)
     */
    @WorkerThread
    public static <T extends IPCRemoteAsyncInvoke<InputType, ResultType>, InputType extends Parcelable, ResultType extends Parcelable>
            boolean invokeAsync(String process, InputType data, @NonNull Class<T> taskClass, final IPCRemoteInvokeCallback<ResultType> callback) {
        if (process == null || process.length() == 0) {
            Log.e(TAG, "invokeAsync failed, process is null or nil.");
            return false;
        }
        if (taskClass == null) {
            Log.e(TAG, "invokeAsync failed, taskClass is null(process : %s).", process);
            return false;
        }
        if (IPCInvokeLogic.isCurrentProcess(process)) {
            (new IPCAsyncInvokeTaskProxy()).invoke(buildBundle(data, taskClass), new IPCInvokeCallback() {
                @Override
                public void onCallback(Bundle data) {
                    if (callback != null) {
                        data.setClassLoader(IPCInvoker.class.getClassLoader());
                        callback.onCallback((ResultType) data.getParcelable(INNER_KEY_REMOTE_TASK_RESULT_DATA));
                    }
                }
            });
            return true;
        }
        AIDL_IPCInvokeBridge bridge = IPCBridgeManager.getImpl().getIPCBridge(process);
        if (bridge == null) {
            Log.e(TAG, "invokeAsync failed, get bridge is null by process(%s).", process);
            return false;
        }
        try {
            AIDL_IPCInvokeCallback invokeCallback = null;
            if (callback != null) {
                invokeCallback = new AIDL_IPCInvokeCallback.Stub() {
                    @Override
                    public void onCallback(Bundle data) throws RemoteException {
                        data.setClassLoader(IPCInvoker.class.getClassLoader());
                        callback.onCallback((ResultType) data.getParcelable(INNER_KEY_REMOTE_TASK_RESULT_DATA));
                    }
                };
            }
            bridge.invokeAsync(buildBundle(data, taskClass), IPCAsyncInvokeTaskProxy.class.getName(), invokeCallback);
            return true;
        } catch (RemoteException e) {
            Log.d(TAG, "invokeAsync failed, ipc invoke error : %s", e);
        }
        return false;
    }

    /**
     * Sync invoke, it must be invoked on WorkerThread.
     *
     * @param process   remote service process name
     * @param data      data for remote process invoked
     * @param taskClass remote invoke logic task class
     * @param <T>       the class implements {@link IPCSyncInvokeTask} interface
     * @return the cross-process invoke result.
     *
     * @see #invokeSync(String, Parcelable, Class)
     */
    @WorkerThread
    public static <T extends IPCSyncInvokeTask> Bundle invokeSync(@NonNull String process, Bundle data, @NonNull Class<T> taskClass) {
        if (process == null || process.length() == 0) {
            Log.e(TAG, "invokeSync failed, process is null or nil.");
            return null;
        }
        if (taskClass == null) {
            Log.e(TAG, "invokeSync failed, taskClass is null(process : %s).", process);
            return null;
        }
        if (IPCInvokeLogic.isCurrentProcess(process)) {
            IPCSyncInvokeTask task = ReflectUtil.newInstance(taskClass, IPCSyncInvokeTask.class);
            if (task == null) {
                Log.e(TAG, "invokeSync failed, newInstance(%s) return null.", taskClass);
                return null;
            }
            return task.invoke(data);
        }
        AIDL_IPCInvokeBridge bridge = IPCBridgeManager.getImpl().getIPCBridge(process);
        if (bridge == null) {
            Log.e(TAG, "invokeSync failed, get bridge is null by process(%s).", process);
            return null;
        }
        try {
            Bundle result = bridge.invokeSync(data, taskClass.getName());
            if (result != null) {
                result.setClassLoader(IPCInvoker.class.getClassLoader());
            }
            return result;
        } catch (RemoteException e) {
            Log.d(TAG, "invokeSync failed, ipc invoke error : %s", e);
        }
        return null;
    }

    /**
     * Sync invoke, it must be invoked on WorkerThread.
     *
     * @param process      remote service process name
     * @param data         data for remote process invoked, it must be a {@link Parcelable}
     * @param taskClass    remote invoke logic task class
     * @param <T>          the class implements {@link IPCRemoteSyncInvoke} interface
     * @param <InputType>  the class extends {@link Parcelable}
     * @param <ResultType> the class extends {@link Parcelable}
     * @return the cross-process invoke result.
     *
     * @see #invokeSync(String, Bundle, Class)
     */
    @WorkerThread
    public static <T extends IPCRemoteSyncInvoke<InputType, ResultType>, InputType extends Parcelable, ResultType extends Parcelable>
            ResultType invokeSync(String process, InputType data, @NonNull Class<T> taskClass) {
        if (process == null || process.length() == 0) {
            Log.e(TAG, "invokeSync failed, process is null or nil.");
            return null;
        }
        if (taskClass == null) {
            Log.e(TAG, "invokeSync failed, taskClass is null(process : %s).", process);
            return null;
        }
        if (IPCInvokeLogic.isCurrentProcess(process)) {
            Bundle resData = (new IPCSyncInvokeTaskProxy()).invoke(buildBundle(data, taskClass));
            if (resData == null) {
                return null;
            }
            resData.setClassLoader(IPCInvoker.class.getClassLoader());
            return (ResultType) resData.getParcelable(INNER_KEY_REMOTE_TASK_RESULT_DATA);
        }
        AIDL_IPCInvokeBridge bridge = IPCBridgeManager.getImpl().getIPCBridge(process);
        if (bridge == null) {
            Log.e(TAG, "invokeSync failed, get bridge is null by process(%s).", process);
            return null;
        }
        try {
            Bundle resData = bridge.invokeSync(buildBundle(data, taskClass), IPCSyncInvokeTaskProxy.class.getName());
            if (resData == null) {
                return null;
            }
            resData.setClassLoader(IPCInvoker.class.getClassLoader());
            return (ResultType) resData.getParcelable(INNER_KEY_REMOTE_TASK_RESULT_DATA);
        } catch (RemoteException e) {
            Log.d(TAG, "invokeSync failed, ipc invoke error : %s", e);
        }
        return null;
    }

    private static Bundle buildBundle(Parcelable parcelable, Class<?> taskClass) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INNER_KEY_REMOTE_TASK_DATA, parcelable);
        bundle.putString(INNER_KEY_REMOTE_TASK_CLASS, taskClass.getName());
        return bundle;
    }

    private static class IPCSyncInvokeTaskProxy implements IPCSyncInvokeTask {

        @Override
        public Bundle invoke(Bundle data) {
            Parcelable remoteData = data.getParcelable(INNER_KEY_REMOTE_TASK_DATA);
            String clazz = data.getString(INNER_KEY_REMOTE_TASK_CLASS);
            if (clazz == null || clazz.length() == 0) {
                Log.e(TAG, "proxy SyncInvoke failed, class is null or nil.");
                return null;
            }
            IPCRemoteSyncInvoke<Parcelable, Parcelable> task = ReflectUtil.newInstance(clazz, IPCRemoteSyncInvoke.class);
            if (task == null) {
                Log.w(TAG, "proxy SyncInvoke failed, newInstance(%s) return null.", clazz);
                return null;
            }
            Bundle resData = new Bundle();
            resData.putParcelable(INNER_KEY_REMOTE_TASK_RESULT_DATA, task.invoke(remoteData));
            return resData;
        }
    }

    private static class IPCAsyncInvokeTaskProxy implements IPCAsyncInvokeTask {

        @Override
        public void invoke(Bundle data, final IPCInvokeCallback callback) {
            Parcelable remoteData = data.getParcelable(INNER_KEY_REMOTE_TASK_DATA);
            String clazz = data.getString(INNER_KEY_REMOTE_TASK_CLASS);
            if (clazz == null || clazz.length() == 0) {
                Log.e(TAG, "proxy AsyncInvoke failed, class is null or nil.");
                return;
            }
            IPCRemoteAsyncInvoke task = ReflectUtil.newInstance(clazz, IPCRemoteAsyncInvoke.class);
            if (task == null) {
                Log.w(TAG, "proxy AsyncInvoke failed, newInstance(%s) return null.", clazz);
                return;
            }
            task.invoke(remoteData, new IPCRemoteInvokeCallback<Parcelable>() {
                @Override
                public void onCallback(Parcelable data) {
                    if (callback != null) {
                        Bundle resData = new Bundle();
                        resData.putParcelable(INNER_KEY_REMOTE_TASK_RESULT_DATA, data);
                        callback.onCallback(resData);
                    }
                }
            });
        }
    }
}
