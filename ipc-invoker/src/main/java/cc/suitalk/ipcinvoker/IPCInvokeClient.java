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

import android.os.Parcelable;

import cc.suitalk.ipcinvoker.annotation.AnyThread;
import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.annotation.WorkerThread;
import cc.suitalk.ipcinvoker.event.IPCDispatcher;
import cc.suitalk.ipcinvoker.event.IPCObservable;
import cc.suitalk.ipcinvoker.event.IPCObserver;

/**
 * Created by albieliang on 2017/6/18.
 */

public class IPCInvokeClient {

    private String mProcess;
    private IPCObservable.Ext mIPCObservableExt;

    public IPCInvokeClient(String process) {
        this.mProcess = process;
        this.mIPCObservableExt = IPCObservable.create(process);
    }

    @AnyThread
    public <T extends IPCAsyncInvokeTask<InputType, ResultType>, InputType extends Parcelable, ResultType extends Parcelable>
            boolean invokeAsync(InputType data, @NonNull Class<T> taskClass, final IPCInvokeCallback<ResultType> callback) {
        return IPCInvoker.invokeAsync(mProcess, data, taskClass, callback);
    }

    @AnyThread
    public <T extends IPCAsyncInvokeTask<InputType, ResultType>, InputType, ResultType>
            boolean invokeAsync(InputType data, @NonNull Class<T> taskClass, final IPCInvokeCallback<ResultType> callback) {
        return IPCTask.create(mProcess).async(taskClass).data(data).callback(callback).invoke();
    }

    @WorkerThread
    public <T extends IPCSyncInvokeTask<InputType, ResultType>, InputType extends Parcelable, ResultType extends Parcelable>
            ResultType invokeSync(InputType data, @NonNull Class<T> taskClass) {
        return IPCInvoker.invokeSync(mProcess, data, taskClass);
    }

    @WorkerThread
    public <T extends IPCSyncInvokeTask<InputType, ResultType>, InputType, ResultType>
            ResultType invokeSync(InputType data, @NonNull Class<T> taskClass) {
        return IPCTask.create(mProcess).sync(taskClass).data(data).invoke();
    }

    @AnyThread
    public <T> boolean registerIPCObserver(@NonNull final Class<? extends IPCDispatcher<T>> dispatcherClass, @NonNull final IPCObserver<T> o) {
        return mIPCObservableExt.registerIPCObserver(dispatcherClass, o);
    }

    @AnyThread
    public <T> boolean unregisterIPCObserver(@NonNull final Class<? extends IPCDispatcher<T>> dispatcherClass, @NonNull final IPCObserver o) {
        return mIPCObservableExt.unregisterIPCObserver(dispatcherClass, o);
    }

}
