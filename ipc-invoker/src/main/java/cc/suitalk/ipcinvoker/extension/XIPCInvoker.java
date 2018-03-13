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

package cc.suitalk.ipcinvoker.extension;

import android.os.Parcel;
import android.os.Parcelable;

import cc.suitalk.ipcinvoker.IPCInvokeCallback;
import cc.suitalk.ipcinvoker.IPCInvoker;
import cc.suitalk.ipcinvoker.IPCAsyncInvokeTask;
import cc.suitalk.ipcinvoker.IPCSyncInvokeTask;
import cc.suitalk.ipcinvoker.ObjectStore;
import cc.suitalk.ipcinvoker.annotation.AnyThread;
import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.annotation.WorkerThread;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/7/6.
 */

public class XIPCInvoker {

    private static final String TAG = "IPC.XIPCInvoker";

    /**
     * Async invoke, it can be invoked on any thread.
     *
     * @param process      remote service process name
     * @param data         data for remote process invoked, it must be a {@link Parcelable}
     * @param taskClass    remote invoke logic task class
     * @param callback     callback on current process after IPC invoked finished and initiative callback.
     * @param <T>          the class implements {@link IPCAsyncInvokeTask} interface
     * @param <InputType>  the class extends {@link Parcelable}
     * @param <ResultType> the class extends {@link Parcelable}
     */
    @AnyThread
    public static <T extends IPCAsyncInvokeTask<InputType, ResultType>, InputType, ResultType>
            void invokeAsync(final String process, final InputType data, @NonNull final Class<T> taskClass, final IPCInvokeCallback<ResultType> callback) {
        IPCInvoker.invokeAsync(process, new WrapperParcelable(taskClass.getName(), data),
                IPCAsyncInvokeTaskProxy.class, new IPCInvokeCallback<WrapperParcelable>() {
                    @Override
                    public void onCallback(WrapperParcelable data) {
                        if (callback != null) {
                            if (data == null) {
                                Log.w(TAG, "async invoke callback error, wrapper parcelable data is null!");
                                callback.onCallback(null);
                                return;
                            }
                            callback.onCallback((ResultType) data.getTarget());
                        }
                    }
                });
    }

    /**
     * Sync invoke, it must be invoked on WorkerThread or make sure the connection is established before invoked.
     *
     * Call {@link cc.suitalk.ipcinvoker.IPCInvokerBoot#connectRemoteService(String)} to pre-connect remote Service.
     *
     * @param process      remote service process name
     * @param data         data for remote process invoked, it must be a {@link Parcelable}
     * @param taskClass    remote invoke logic task class
     * @param <T>          the class implements {@link IPCSyncInvokeTask} interface
     * @param <InputType>  the class extends {@link Parcelable}
     * @param <ResultType> the class extends {@link Parcelable}
     * @return the cross-process invoke result.
     */
    @WorkerThread
    public static <T extends IPCSyncInvokeTask<InputType, ResultType>, InputType, ResultType>
            ResultType invokeSync(String process, InputType data, @NonNull Class<T> taskClass) {
        WrapperParcelable parcelable = IPCInvoker.invokeSync(process, new WrapperParcelable(taskClass.getName(), data), IPCSyncInvokeTaskProxy.class);
        if (parcelable == null) {
            Log.w(TAG, "sync invoke error, wrapper parcelable data is null!");
            return null;
        }
        return (ResultType) parcelable.getTarget();
    }

    private static class IPCSyncInvokeTaskProxy implements IPCSyncInvokeTask<WrapperParcelable, WrapperParcelable> {

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
            task.invoke(remoteData, new IPCInvokeCallback() {
                @Override
                public void onCallback(Object data) {
                    if (callback != null) {
                        callback.onCallback(new WrapperParcelable(null, data));
                    }
                }
            });
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
