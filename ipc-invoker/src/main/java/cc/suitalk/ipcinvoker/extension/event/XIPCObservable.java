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

package cc.suitalk.ipcinvoker.extension.event;

import android.os.Bundle;

import junit.framework.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cc.suitalk.ipcinvoker.IPCInvokeClient;
import cc.suitalk.ipcinvoker.annotation.AnyThread;
import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.event.IPCObserver;
import cc.suitalk.ipcinvoker.reflect.ReflectUtil;

/**
 * Created by albieliang on 2017/7/20.
 */

public class XIPCObservable<InputType> {

    static final String INNER_KEY_DATA = "__inner_key_data";

    private final String process;
    private final String event;
    private final IPCInvokeClient mClient;
    private final Map<XIPCObserver<InputType>, IPCObserver> mMap;

    public <T extends XIPCDispatcher<InputType>> XIPCObservable(String process, Class<T> dispatcherClass) {
        Assert.assertNotNull(process);
        Assert.assertNotNull(dispatcherClass);
        Class<?> tClass = ReflectUtil.getActualTypeArgument(dispatcherClass);
        Assert.assertNotNull(tClass);
        this.process = process;
        this.event = genKey(dispatcherClass, tClass);
        mClient = new IPCInvokeClient(process);
        mMap = new ConcurrentHashMap<>();
    }

    @AnyThread
    public boolean registerIPCObserver(@NonNull final XIPCObserver<InputType> o) {
        if (o == null) {
            return false;
        }
        IPCObserver observer = new IPCObserver() {
            @Override
            public void onCallback(Bundle data) {
                WrapperParcelable parcelable = data.getParcelable(INNER_KEY_DATA);
                o.onCallback((InputType) parcelable.getTarget());
            }

            @Override
            public int hashCode() {
                return o.hashCode();
            }
        };
        boolean r = mClient.registerIPCObserver(event, observer);
        if (r) {
            mMap.put(o, observer);
        }
        return r;
    }

    @AnyThread
    public boolean unregisterIPCObserver(@NonNull XIPCObserver<InputType> o) {
        if (o == null) {
            return false;
        }
        IPCObserver observer = mMap.remove(o);
        if (observer == null) {
            return false;
        }
        return mClient.unregisterIPCObserver(event, observer);
    }

    public String getProcess() {
        return process;
    }

    public static String genKey(Class<?> dispatcherClass, Class<?> inputDataClass) {
        return (new StringBuilder()).append(dispatcherClass.getName()).append("#").append(inputDataClass.getName()).toString();
    }
}
