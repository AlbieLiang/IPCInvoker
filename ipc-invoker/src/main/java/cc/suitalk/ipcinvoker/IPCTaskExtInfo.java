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

import cc.suitalk.ipcinvoker.exception.OnExceptionObserver;

/**
 * Created by albieliang on 2018/5/8.
 */

class IPCTaskExtInfo<ResultType> {

    public static final IPCTaskExtInfo DEFAULT = new IPCTaskExtInfo();

    public static final long DEFAULT_TIMEOUT = 10 * 1000;

    private long timeoutMillis = DEFAULT_TIMEOUT;
    private ResultType defaultResult;
    private boolean hasDefaultResult;
    private OnExceptionObserver onExceptionObserver;
    private ServiceConnection serviceConnection;

    public long getTimeout() {
        return timeoutMillis;
    }

    public IPCTaskExtInfo<ResultType> setTimeout(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public ResultType getDefaultResult() {
        return defaultResult;
    }

    public IPCTaskExtInfo<ResultType> setDefaultResult(ResultType defaultResult) {
        this.defaultResult = defaultResult;
        this.hasDefaultResult = true;
        return this;
    }

    public boolean hasDefaultResult() {
        return hasDefaultResult;
    }

    public OnExceptionObserver getOnExceptionObserver() {
        return onExceptionObserver;
    }

    public IPCTaskExtInfo<ResultType> setOnExceptionObserver(OnExceptionObserver onExceptionObserver) {
        this.onExceptionObserver = onExceptionObserver;
        return this;
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public IPCTaskExtInfo<ResultType> setServiceConnection(ServiceConnection serviceConnection) {
        this.serviceConnection = serviceConnection;
        return this;
    }
}