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

import android.os.Parcelable;

import cc.suitalk.ipcinvoker.IPCInvokeCallback;
import cc.suitalk.ipcinvoker.IPCAsyncInvokeTask;
import cc.suitalk.ipcinvoker.IPCSyncInvokeTask;
import cc.suitalk.ipcinvoker.annotation.AnyThread;
import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.annotation.WorkerThread;
import cc.suitalk.ipcinvoker.IPCTask;

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
     * @param <InputType>  the class extends {@link Parcelable} or has extended a {@link BaseTypeTransfer} for this InputType
     * @param <ResultType> the class extends {@link Parcelable} or has extended a {@link BaseTypeTransfer} for this ResultType
     */
    @AnyThread
    public static <T extends IPCAsyncInvokeTask<InputType, ResultType>, InputType, ResultType>
            void invokeAsync(final String process, final InputType data, @NonNull final Class<T> taskClass, final IPCInvokeCallback<ResultType> callback) {
        IPCTask.create(process).async(taskClass).data(data).callback(callback).invoke();
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
     * @param <InputType>  the class extends {@link Parcelable} or has extended a {@link BaseTypeTransfer} for this InputType
     * @param <ResultType> the class extends {@link Parcelable} or has extended a {@link BaseTypeTransfer} for this ResultType
     * @return the cross-process invoke result.
     */
    @WorkerThread
    public static <T extends IPCSyncInvokeTask<InputType, ResultType>, InputType, ResultType>
            ResultType invokeSync(String process, InputType data, @NonNull Class<T> taskClass) {
        return IPCTask.create(process).sync(taskClass).data(data).invoke();
    }
}
