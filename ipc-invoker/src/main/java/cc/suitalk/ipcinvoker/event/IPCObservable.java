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

package cc.suitalk.ipcinvoker.event;

import android.os.Bundle;

import cc.suitalk.ipcinvoker.tools.Assert;

import java.util.Map;

import cc.suitalk.ipcinvoker.annotation.AnyThread;
import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.annotation.Nullable;
import cc.suitalk.ipcinvoker.inner.InnerIPCObservable;
import cc.suitalk.ipcinvoker.inner.InnerIPCObserver;
import cc.suitalk.ipcinvoker.reflect.ReflectUtil;
import cc.suitalk.ipcinvoker.tools.SafeConcurrentHashMap;

/**
 * Created by albieliang on 2017/7/20.
 */

public class IPCObservable<InputType> {

    static final String INNER_KEY_DATA = "__inner_key_data";

    private final String mEvent;

    private final Ext<InputType> mExt;

    public static <T> Ext<T> create(@NonNull String process) {
        return new Ext<>(process);
    }

    public <T extends IPCDispatcher<InputType>> IPCObservable(String process, Class<T> dispatcherClass) {
        Assert.assertNotNull(process);
        Assert.assertNotNull(dispatcherClass);
        Class<?> tClass = ReflectUtil.getActualTypeArgument(dispatcherClass);
        Assert.assertNotNull(tClass);
        this.mEvent = genKey(dispatcherClass, tClass);
        mExt = create(process);
    }

    @AnyThread
    public boolean registerIPCObserver(@NonNull final IPCObserver<InputType> o) {
        return mExt.registerIPCObserver(mEvent, o);
    }

    @AnyThread
    public boolean unregisterIPCObserver(@NonNull IPCObserver<InputType> o) {
        return mExt.unregisterIPCObserver(mEvent, o);
    }

    public String getProcess() {
        return mExt.mProcess;
    }

    public static String genKey(@NonNull Class<?> dispatcherClass, @Nullable Class<?> inputDataClass) {
        return (new StringBuilder()).append(dispatcherClass.getName()).append("#").append(inputDataClass == null ? "" : inputDataClass.getName()).toString();
    }

    /**
     *
     * @param <InputType>
     */
    public static final class Ext<InputType> {

        private final String mProcess;
        private final Map<IPCObserver<InputType>, InnerIPCObserver> mMap;

        private InnerIPCObservable mInnerIPCObservable;

        private <T extends IPCDispatcher<InputType>> Ext(String process) {
            Assert.assertNotNull(process);
            this.mProcess = process;
            mInnerIPCObservable = new InnerIPCObservable(mProcess);
            mMap = new SafeConcurrentHashMap<>();
        }

        @AnyThread
        public boolean registerIPCObserver(@NonNull Class<IPCDispatcher<InputType>> dispatcherClass, @NonNull final IPCObserver<InputType> o) {
            if (dispatcherClass == null || o == null || mMap.containsKey(o)) {
                return false;
            }
            Class<?> tClass = ReflectUtil.getActualTypeArgument(dispatcherClass);
            return registerIPCObserver(genKey(dispatcherClass, tClass), o);
        }

        @AnyThread
        public boolean unregisterIPCObserver(@NonNull Class<IPCDispatcher<InputType>> dispatcherClass, @NonNull IPCObserver<InputType> o) {
            if (dispatcherClass == null || o == null) {
                return false;
            }
            Class<?> tClass = ReflectUtil.getActualTypeArgument(dispatcherClass);
            return unregisterIPCObserver(genKey(dispatcherClass, tClass), o);
        }

        @AnyThread
        private boolean registerIPCObserver(@NonNull String event, @NonNull final IPCObserver<InputType> o) {
            if (event == null || event.length() == 0 || o == null || mMap.containsKey(o)) {
                return false;
            }
            final InnerIPCObserver observer = new InnerIPCObserver() {
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
            boolean r = mInnerIPCObservable.registerIPCObserver(event, observer);
            if (r) {
                mMap.put(o, observer);
            }
            return r;
        }

        @AnyThread
        private boolean unregisterIPCObserver(@NonNull String event, @NonNull IPCObserver<InputType> o) {
            if (event == null || event.length() == 0 || o == null) {
                return false;
            }
            final InnerIPCObserver observer = mMap.remove(o);
            if (observer == null) {
                return false;
            }
            return mInnerIPCObservable.unregisterIPCObserver(event, observer);
        }
    }
}
