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
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import junit.framework.Assert;

import cc.suitalk.ipcinvoker.event.IPCEventBus;
import cc.suitalk.ipcinvoker.event.IPCObserver;

/**
 * Created by albieliang on 2017/6/18.
 */

public class IPCInvokeClient {

    public static final String TOKEN = "Token";
    public static final String EVENT = "Event";

    private String process;

    public IPCInvokeClient(String process) {
        this.process = process;
    }

    @WorkerThread
    public <T extends IPCAsyncInvokeTask> boolean invokeAsync(Bundle data, @NonNull Class<T> taskClass, final IPCInvokeCallback callback) {
        return IPCInvoker.invokeAsync(process, data, taskClass, callback);
    }

    @WorkerThread
    public <T extends IPCRemoteAsyncInvoke<InputType, ResultType>, InputType extends Parcelable, ResultType extends Parcelable>
            boolean invokeAsync(InputType data, @NonNull Class<T> taskClass, final IPCRemoteInvokeCallback<ResultType> callback) {
        return IPCInvoker.invokeAsync(process, data, taskClass, callback);
    }

    @WorkerThread
    public <T extends IPCSyncInvokeTask> Bundle invokeSync(Bundle data, @NonNull Class<T> taskClass) {
        return IPCInvoker.invokeSync(process, data, taskClass);
    }

    @WorkerThread
    public <T extends IPCRemoteSyncInvoke<InputType, ResultType>, InputType extends Parcelable, ResultType extends Parcelable>
            ResultType invokeSync(InputType data, @NonNull Class<T> taskClass) {
        return IPCInvoker.invokeSync(process, data, taskClass);
    }

    public boolean registerIPCObserver(String event, @NonNull IPCObserver observer) {
        if (event == null || event.length() == 0 || observer == null) {
            return false;
        }
        Bundle data = new Bundle();
        data.putString(TOKEN, buildToken(observer));
        data.putString(EVENT, event);
        IPCInvoker.invokeAsync(process, data, IPCInvokeTask_RegisterIPCObserver.class, observer);
        return true;
    }

    public boolean unregisterIPCObserver(String event, @NonNull IPCObserver observer) {
        if (event == null || event.length() == 0 || observer == null) {
            return false;
        }
        Bundle data = new Bundle();
        data.putString(TOKEN, buildToken(observer));
        data.putString(EVENT, event);
        IPCInvoker.invokeAsync(process, data, IPCInvokeTask_UnregisterIPCObserver.class, observer);
        return true;
    }

    public static String buildToken(@NonNull Object o) {
        return "Token#IPCObserver#" + o.hashCode();
    }

    private static class IPCInvokeTask_RegisterIPCObserver implements IPCAsyncInvokeTask {

        @Override
        public void invoke(Bundle data, final IPCInvokeCallback callback) {
            final String token = data.getString(TOKEN);
            final String event = data.getString(EVENT);
            IPCEventBus.getImpl().registerIPCObserver(event, new IPCObserverProxy(token) {
                @Override
                public void onCallback(Bundle data) {
                    callback.onCallback(data);
                }
            });
        }
    }

    private static class IPCInvokeTask_UnregisterIPCObserver implements IPCAsyncInvokeTask {

        @Override
        public void invoke(Bundle data, final IPCInvokeCallback callback) {
            final String token = data.getString(TOKEN);
            final String event = data.getString(EVENT);
            IPCEventBus.getImpl().unregisterIPCObserver(event, new IPCObserverProxy(token) {
                @Override
                public void onCallback(Bundle data) {
                    callback.onCallback(data);
                }
            });
        }
    }

    private static abstract class IPCObserverProxy implements IPCObserver {

        String token;

        IPCObserverProxy(String token) {
            this.token = token;
            Assert.assertNotNull(token);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof IPCObserverProxy)) {
                return false;
            }
            return token.equals(((IPCObserverProxy) obj).token);
        }
    }
}
