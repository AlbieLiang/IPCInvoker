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

import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;

import junit.framework.Assert;

import cc.suitalk.ipcinvoker.IPCInvokeClient;

/**
 * Created by albieliang on 2017/6/18.
 */

public class IPCObservable {

    private String process;
    private String event;
    private IPCInvokeClient mClient;

    public <T extends IPCDispatcher> IPCObservable(String process, Class<T> clazz) {
        Assert.assertNotNull(process);
        Assert.assertNotNull(clazz);
        this.process = process;
        this.event = clazz.getName();
        mClient = new IPCInvokeClient(process);
    }

    @AnyThread
    public boolean registerIPCObserver(@NonNull IPCObserver o) {
        return mClient.registerIPCObserver(event, o);
    }

    @AnyThread
    public boolean unregisterIPCObserver(@NonNull IPCObserver o) {
        return mClient.unregisterIPCObserver(event, o);
    }

    public String getProcess() {
        return process;
    }
}
