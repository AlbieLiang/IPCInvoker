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

package cc.suitalk.ipcinvoker.sample.exception;

import cc.suitalk.ipcinvoker.IPCAsyncInvokeTask;
import cc.suitalk.ipcinvoker.IPCInvokeCallback;
import cc.suitalk.ipcinvoker.ThreadCaller;
import cc.suitalk.ipcinvoker.exception.OnExceptionObservable;
import cc.suitalk.ipcinvoker.exception.OnExceptionObserver;
import cc.suitalk.ipcinvoker.tools.Log;
import cc.suitalk.ipcinvoker.type.IPCBoolean;
import cc.suitalk.ipcinvoker.type.IPCString;

/**
 * Created by albieliang on 2018/4/17.
 */

public class TestAsyncInvokeCallbackException implements IPCAsyncInvokeTask<IPCString, IPCBoolean> {

    private static final String TAG = "IPCSample.TestAsyncInvokeCallbackException";

    @Override
    public void invoke(IPCString data, final IPCInvokeCallback<IPCBoolean> callback) {
        ((OnExceptionObservable) callback).registerObserver(new OnExceptionObserver() {
            @Override
            public void onExceptionOccur(Exception e) {
                Log.e(TAG, "onExceptionOccur(%s)", android.util.Log.getStackTraceString(e));
                ((OnExceptionObservable) callback).unregisterObserver(this);
            }
        });
        ThreadCaller.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onCallback(new IPCBoolean(false));
            }
        }, 2000);
    }
}
