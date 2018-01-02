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

import junit.framework.Assert;

import cc.suitalk.ipcinvoker.IPCInvokeClient;
import cc.suitalk.ipcinvoker.annotation.AnyThread;
import cc.suitalk.ipcinvoker.annotation.NonNull;

/**
 * Created by albieliang on 2017/6/18.
 */

public class IPCObservable {

    private String mProcess;
    private String mEvent;
    private IPCInvokeClient mClient;

    public <T extends IPCDispatcher> IPCObservable(String process, Class<T> dispatcherClass) {
        Assert.assertNotNull(process);
        Assert.assertNotNull(dispatcherClass);
        this.mProcess = process;
        this.mEvent = dispatcherClass.getName();
        this.mClient = new IPCInvokeClient(process);
    }

    @AnyThread
    public boolean registerIPCObserver(@NonNull IPCObserver o) {
        return mClient.registerIPCObserver(mEvent, o);
    }

    @AnyThread
    public boolean unregisterIPCObserver(@NonNull IPCObserver o) {
        return mClient.unregisterIPCObserver(mEvent, o);
    }

    public String getProcess() {
        return mProcess;
    }
}
