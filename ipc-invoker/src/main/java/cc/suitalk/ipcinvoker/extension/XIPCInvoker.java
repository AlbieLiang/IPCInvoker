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
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import cc.suitalk.ipcinvoker.IPCInvoker;
import cc.suitalk.ipcinvoker.IPCRemoteAsyncInvoke;
import cc.suitalk.ipcinvoker.IPCRemoteInvokeCallback;
import cc.suitalk.ipcinvoker.IPCRemoteSyncInvoke;
import cc.suitalk.ipcinvoker.ThreadCaller;
import cc.suitalk.ipcinvoker.reflect.ReflectUtil;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/7/6.
 */

public class XIPCInvoker {

    private static final String TAG = "IPC.XIPCInvoker";

    /**
     *
     * @param process
     * @param data
     * @param taskClass
     * @param callback
     * @param <T>
     * @param <InputType>
     * @param <ResultType>
     */
    public static <T extends IPCRemoteAsyncInvoke<InputType, ResultType>, InputType, ResultType>
            void invokeAsync(final String process, final InputType data, @NonNull final Class<T> taskClass, final IPCRemoteInvokeCallback<ResultType> callback) {
        ThreadCaller.post(new Runnable() {
            @Override
            public void run() {
                boolean r = IPCInvoker.invokeAsync(process, new WrapperParcelable(taskClass.getName(), data),
                        IPCAsyncInvokeTaskProxy.class, new IPCRemoteInvokeCallback<WrapperParcelable>() {
                            @Override
                            public void onCallback(WrapperParcelable data) {
                                if (callback != null) {
                                    callback.onCallback((ResultType) data.getTarget());
                                }
                            }
                        });
                Log.d(TAG, "IPCInvoker.invokeAsync return : %s", r);
            }
        });
    }

    /**
     *
     * @param process
     * @param data
     * @param taskClass
     * @param <T>
     * @param <InputType>
     * @param <ResultType>
     * @return
     */
    @WorkerThread
    public static <T extends IPCRemoteSyncInvoke<InputType, ResultType>, InputType, ResultType>
            ResultType invokeSync(String process, InputType data, @NonNull Class<T> taskClass) {
        WrapperParcelable parcelable = IPCInvoker.invokeSync(process, new WrapperParcelable(taskClass.getName(), data), IPCSyncInvokeTaskProxy.class);
        return (ResultType) parcelable.getTarget();
    }

    private static class IPCSyncInvokeTaskProxy implements IPCRemoteSyncInvoke<WrapperParcelable, WrapperParcelable> {

        @Override
        public WrapperParcelable invoke(WrapperParcelable data) {
            Object remoteData = data.getTarget();
            String clazz = data.getTaskClass();
            if (clazz == null || clazz.length() == 0) {
                Log.e(TAG, "proxy SyncInvoke failed, class is null or nil.");
                return new WrapperParcelable(null, null);
            }
            IPCRemoteSyncInvoke task = ReflectUtil.newInstance(clazz, IPCRemoteSyncInvoke.class);
            if (task == null) {
                Log.w(TAG, "proxy SyncInvoke failed, newInstance(%s) return null.", clazz);
                return new WrapperParcelable(null, null);
            }
            return new WrapperParcelable(null, task.invoke(remoteData));
        }
    }

    private static class IPCAsyncInvokeTaskProxy implements IPCRemoteAsyncInvoke<WrapperParcelable, WrapperParcelable> {

        private static final String TAG = "IPC.IPCAsyncInvokeTaskProxy";

        @Override
        public void invoke(WrapperParcelable data, final IPCRemoteInvokeCallback<WrapperParcelable> callback) {
            Object remoteData = data.getTarget();
            String clazz = data.getTaskClass();
            if (clazz == null || clazz.length() == 0) {
                Log.e(TAG, "proxy AsyncInvoke failed, class is null or nil.");
                return;
            }
            IPCRemoteAsyncInvoke task = ReflectUtil.newInstance(clazz, IPCRemoteAsyncInvoke.class);
            if (task == null) {
                Log.w(TAG, "proxy AsyncInvoke failed, newInstance(%s) return null.", clazz);
                return;
            }
            task.invoke(remoteData, new IPCRemoteInvokeCallback() {
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
                    ObjectTypeTransfer.writeToParcel(target, dest);
                    return;
                }
            }
            dest.writeInt(HAS_DATA);
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
