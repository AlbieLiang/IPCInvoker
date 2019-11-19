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

import junit.framework.Assert;

import cc.suitalk.ipcinvoker.annotation.AnyThread;
import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.annotation.WorkerThread;
import cc.suitalk.ipcinvoker.event.IPCEventBus;
import cc.suitalk.ipcinvoker.event.IPCObserver;
import cc.suitalk.ipcinvoker.restore.IPCObserverRestorer;
import cc.suitalk.ipcinvoker.type.IPCVoid;

/**
 * Created by albieliang on 2017/6/18.
 */

public class IPCInvokeClient {

    private static final String TOKEN = "Token";
    private static final String EVENT = "Event";

    private String mProcess;

    public IPCInvokeClient(String process) {
        this.mProcess = process;
    }

    @AnyThread
    public <T extends IPCAsyncInvokeTask<InputType, ResultType>, InputType extends Parcelable, ResultType extends Parcelable>
            boolean invokeAsync(InputType data, @NonNull Class<T> taskClass, final IPCInvokeCallback<ResultType> callback) {
        return IPCInvoker.invokeAsync(mProcess, data, taskClass, callback);
    }

    @WorkerThread
    public <T extends IPCSyncInvokeTask<InputType, ResultType>, InputType extends Parcelable, ResultType extends Parcelable>
            ResultType invokeSync(InputType data, @NonNull Class<T> taskClass) {
        return IPCInvoker.invokeSync(mProcess, data, taskClass);
    }

    @AnyThread
    public boolean registerIPCObserver(final String event, @NonNull final IPCObserver observer) {
        if (event == null || event.length() == 0 || observer == null) {
            return false;
        }
        Bundle data = new Bundle();
        data.putString(TOKEN, buildToken(observer));
        data.putString(EVENT, event);
        IPCInvoker.invokeAsync(mProcess, data, IPCInvokeTask_RegisterIPCObserver.class, observer);
        // save observer and event after application startup
        IPCInvoker.invokeAsync(mProcess, null, IPCTestAsyncTask.class, new IPCInvokeCallback<IPCVoid>() {
            @Override
            public void onCallback(IPCVoid data) {
                // save register
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        IPCObserverRestorer.addIPCObserver(mProcess, event, observer);
                    }
                });
            }
        });
        return true;
    }

    @AnyThread
    public boolean unregisterIPCObserver(final String event, @NonNull final IPCObserver observer) {
        if (event == null || event.length() == 0 || observer == null) {
            return false;
        }
        Bundle data = new Bundle();
        data.putString(TOKEN, buildToken(observer));
        data.putString(EVENT, event);
        IPCInvoker.invokeAsync(mProcess, data, IPCInvokeTask_UnregisterIPCObserver.class, new IPCInvokeCallback<Bundle>() {
            @Override
            public void onCallback(Bundle data) {
                // remove register
                ThreadPool.post(new Runnable() {
                    @Override
                    public void run() {
                        IPCObserverRestorer.removeIPCObserver(mProcess, event, observer);
                    }
                });
            }
        });
        return true;
    }

    public static String buildToken(@NonNull Object o) {
        return "Token#IPCObserver#" + o.hashCode();
    }

    private static class IPCInvokeTask_RegisterIPCObserver implements IPCAsyncInvokeTask<Bundle, Bundle> {

        @Override
        public void invoke(Bundle data, final IPCInvokeCallback<Bundle> callback) {
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

    private static class IPCInvokeTask_UnregisterIPCObserver implements IPCAsyncInvokeTask<Bundle, Bundle> {

        @Override
        public void invoke(Bundle data, final IPCInvokeCallback<Bundle> callback) {
            final String token = data.getString(TOKEN);
            final String event = data.getString(EVENT);
            IPCEventBus.getImpl().unregisterIPCObserver(event, new IPCObserverProxy(token) {
                @Override
                public void onCallback(Bundle data) {
                }
            });
            if (callback != null) {
                callback.onCallback(null);
            }
        }
    }

    private static class IPCTestAsyncTask implements IPCAsyncInvokeTask<IPCVoid, IPCVoid> {
        @Override
        public void invoke(IPCVoid data, final IPCInvokeCallback<IPCVoid> callback) {
            callback.onCallback(null);
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
