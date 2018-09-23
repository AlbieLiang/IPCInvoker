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

import android.content.ServiceConnection;
import android.os.Parcel;
import android.os.Parcelable;

import cc.suitalk.ipcinvoker.exception.OnExceptionObservable;
import cc.suitalk.ipcinvoker.exception.OnExceptionObserver;
import cc.suitalk.ipcinvoker.extension.BaseTypeTransfer;
import cc.suitalk.ipcinvoker.extension.ObjectTypeTransfer;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2018/5/3.
 */

public class IPCTask {

    private String process;
    private long timeout;
    private boolean hasTimeout;
    private OnExceptionObserver onExceptionObserver;
    private ServiceConnection serviceConnection;

    private IPCTask() {
    }

    public static IPCTask create(String process) {
        IPCTask task = new IPCTask();
        task.process = process;
        return task;
    }

    public IPCTask timeout(long timeoutMillis) {
        this.timeout = timeoutMillis;
        this.hasTimeout = true;
        return this;
    }

    public IPCTask onExceptionObserver(OnExceptionObserver onExceptionObserver) {
        this.onExceptionObserver = onExceptionObserver;
        return this;
    }

    public IPCTask serviceConnection(ServiceConnection serviceConnection) {
        this.serviceConnection = serviceConnection;
        return this;
    }

    public <InputType, ResultType> Async<InputType, ResultType> async(Class<? extends IPCAsyncInvokeTask<InputType, ResultType>> clazz) {
        return new Async(this, clazz);
    }

    public <InputType, ResultType> Sync<InputType, ResultType> sync(Class<? extends IPCSyncInvokeTask<InputType, ResultType>> clazz) {
        return new Sync(this, clazz);
    }

    public static final class Async<InputType, ResultType> {

        private static final String TAG = "IPCTask.Async";

        private InputType data;
        private ResultType defaultResult;
        private Class<IPCAsyncInvokeTask<InputType, ResultType>> taskClass;
        private IPCInvokeCallback<ResultType> callback;

        private boolean hasDefaultResult;

        private IPCTask task;

        Async(IPCTask task, Class<IPCAsyncInvokeTask<InputType, ResultType>> taskClass) {
            this.task = task;
            this.taskClass = taskClass;
        }

        public Async<InputType, ResultType> callback(IPCInvokeCallback<ResultType> callback) {
            this.callback = callback;
            return this;
        }

        public Async<InputType, ResultType> defaultResult(ResultType defaultResult) {
            this.defaultResult = defaultResult;
            this.hasDefaultResult = true;
            return this;
        }

        public Async<InputType, ResultType> data(InputType data) {
            this.data = data;
            return this;
        }

        public boolean invoke() {
            if (task == null) {
                if (callback != null && hasDefaultResult) {
                    Log.d(TAG, "task is null, callback with defaultResult");
                    callback.onCallback(defaultResult);
                    return true;
                }
                Log.w(TAG, "invoke failed, task is null");
                return false;
            }
            if (taskClass == null) {
                if (callback != null && hasDefaultResult) {
                    Log.d(TAG, "taskClass is null, callback with defaultResult");
                    callback.onCallback(defaultResult);
                    return true;
                }
                Log.w(TAG, "invoke failed, taskClass is null");
                return false;
            }
            String process = task.process;
            if (process == null || process.length() == 0) {
                process = IPCInvokeLogic.getCurrentProcessName();
            }
            IPCTaskExtInfo extInfo = new IPCTaskExtInfo();
            if (hasDefaultResult) {
                extInfo.setDefaultResult(new WrapperParcelable(null, defaultResult));
            }
            if (task.hasTimeout) {
                extInfo.setTimeout(task.timeout);
            }
            extInfo.setServiceConnection(task.serviceConnection);
            extInfo.setOnExceptionObserver(task.onExceptionObserver);
            IPCTaskExecutor.invokeAsync(process, new WrapperParcelable(taskClass.getName(), data),
                    IPCAsyncInvokeTaskProxy.class, new IPCInvokeCallback<WrapperParcelable>() {
                        @Override
                        public void onCallback(WrapperParcelable data) {
                            if (callback != null) {
                                ResultType result = null;
                                if (data == null) {
                                    Log.w(TAG, "async invoke callback error, wrapper parcelable data is null!");
                                    if (hasDefaultResult) {
                                        result = defaultResult;
                                    }
                                } else {
                                    result = (ResultType) data.getTarget();
                                }
                                callback.onCallback(result);
                            }
                        }
                    }, extInfo);
            return true;
        }
    }

    public static final class Sync<InputType, ResultType> {

        private static final String TAG = "IPCTask.Sync";

        private InputType data;
        private ResultType defaultResult;
        private Class<IPCSyncInvokeTask<InputType, ResultType>> taskClass;

        private boolean hasDefaultResult;

        private IPCTask task;

        Sync(IPCTask task, Class<IPCSyncInvokeTask<InputType, ResultType>> taskClass) {
            this.task = task;
            this.taskClass = taskClass;
        }

        public Sync<InputType, ResultType> defaultResult(ResultType defaultResult) {
            this.defaultResult = defaultResult;
            this.hasDefaultResult = true;
            return this;
        }

        public Sync<InputType, ResultType> data(InputType data) {
            this.data = data;
            return this;
        }

        public ResultType invoke() {
            if (task == null) {
                if (!hasDefaultResult) {
                    Log.w(TAG, "invoke failed, task is null");
                }
                return defaultResult;
            }
            if (taskClass == null) {
                if (!hasDefaultResult) {
                    Log.w(TAG, "invoke failed, taskClass is null");
                }
                return defaultResult;
            }
            String process = task.process;
            if (process == null || process.length() == 0) {
                process = IPCInvokeLogic.getCurrentProcessName();
            }
            final IPCTaskExtInfo<WrapperParcelable> extInfo = new IPCTaskExtInfo<>();
            if (hasDefaultResult) {
                extInfo.setDefaultResult(new WrapperParcelable(null, defaultResult));
            }
            if (task.hasTimeout) {
                extInfo.setTimeout(task.timeout);
            }

            WrapperParcelable parcelable = IPCTaskExecutor.invokeSync(process, new WrapperParcelable(taskClass.getName(), data),
                    IPCSyncInvokeTaskProxy.class, extInfo);
            ResultType result = null;
            if (parcelable == null) {
                Log.w(TAG, "sync invoke error, wrapper parcelable data is null!");
                if (hasDefaultResult) {
                    result = defaultResult;
                }
            } else {
                result = (ResultType) parcelable.getTarget();
            }
            return result;
        }
    }

    private static class IPCSyncInvokeTaskProxy implements IPCSyncInvokeTask<WrapperParcelable, WrapperParcelable> {

        private static final String TAG = "IPC.IPCSyncInvokeTaskProxy";

        @Override
        public WrapperParcelable invoke(WrapperParcelable data) {
            Object remoteData = data.getTarget();
            String clazz = data.getTaskClass();
            if (clazz == null || clazz.length() == 0) {
                Log.e(TAG, "proxy SyncInvoke failed, class is null or nil.");
                return new WrapperParcelable(null, null);
            }
            IPCSyncInvokeTask task = ObjectStore.get(clazz, IPCSyncInvokeTask.class);
            if (task == null) {
                Log.w(TAG, "proxy SyncInvoke failed, newInstance(%s) return null.", clazz);
                return new WrapperParcelable(null, null);
            }
            return new WrapperParcelable(null, task.invoke(remoteData));
        }
    }

    private static class IPCAsyncInvokeTaskProxy implements IPCAsyncInvokeTask<WrapperParcelable, WrapperParcelable> {

        private static final String TAG = "IPC.IPCAsyncInvokeTaskProxy";

        @Override
        public void invoke(WrapperParcelable data, final IPCInvokeCallback<WrapperParcelable> callback) {
            Object remoteData = data.getTarget();
            String clazz = data.getTaskClass();
            if (clazz == null || clazz.length() == 0) {
                Log.e(TAG, "proxy AsyncInvoke failed, class is null or nil.");
                return;
            }
            IPCAsyncInvokeTask task = ObjectStore.get(clazz, IPCAsyncInvokeTask.class);
            if (task == null) {
                Log.w(TAG, "proxy AsyncInvoke failed, newInstance(%s) return null.", clazz);
                return;
            }
            task.invoke(remoteData, new IPCInvokeCallbackProxy(callback));
        }
    }

    private static class IPCInvokeCallbackProxy implements IPCInvokeCallback, OnExceptionObservable {

        IPCInvokeCallback<WrapperParcelable> callback;
        OnExceptionObservable onExceptionObservable;

        IPCInvokeCallbackProxy(IPCInvokeCallback<WrapperParcelable> callback) {
            this.callback = callback;
            if (callback instanceof OnExceptionObservable) {
                this.onExceptionObservable = (OnExceptionObservable) callback;
            }
        }

        @Override
        public void onCallback(Object data) {
            if (callback != null) {
                callback.onCallback(new WrapperParcelable(null, data));
            }
        }

        @Override
        public void registerObserver(OnExceptionObserver observer) {
            if (onExceptionObservable == null) {
                return;
            }
            onExceptionObservable.registerObserver(observer);
        }

        @Override
        public void unregisterObserver(OnExceptionObserver observer) {
            if (onExceptionObservable == null) {
                return;
            }
            ((OnExceptionObservable) callback).unregisterObserver(observer);
        }
    }

    private static class WrapperParcelable implements Parcelable {

        private static final int NO_DATA = 0;
        private static final int HAS_DATA = 1;

        String taskClass;
        Object target;

        private WrapperParcelable() {
        }

        public WrapperParcelable(String taskClass, Object o) {
            this.taskClass = taskClass;
            this.target = o;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(taskClass);
            if (target != null) {
                BaseTypeTransfer transfer = ObjectTypeTransfer.getTypeTransfer(target);
                if (transfer != null) {
                    dest.writeInt(HAS_DATA);
                    dest.writeString(transfer.getClass().getName());
                    transfer.writeToParcel(target, dest);
                    return;
                }
            }
            dest.writeInt(NO_DATA);
        }

        void readFromParcel(Parcel in) {
            taskClass = in.readString();
            int hasData = in.readInt();
            if (hasData == HAS_DATA) {
                String transferClass = in.readString();
                target = ObjectTypeTransfer.readFromParcel(transferClass, in);
            }
        }

        Object getTarget() {
            return target;
        }

        String getTaskClass() {
            return this.taskClass;
        }

        public static final Creator<WrapperParcelable> CREATOR = new Creator<WrapperParcelable>() {
            @Override
            public WrapperParcelable createFromParcel(Parcel in) {
                WrapperParcelable o = new WrapperParcelable();
                o.readFromParcel(in);
                return o;
            }

            @Override
            public WrapperParcelable[] newArray(int size) {
                return new WrapperParcelable[size];
            }
        };
    }
}
