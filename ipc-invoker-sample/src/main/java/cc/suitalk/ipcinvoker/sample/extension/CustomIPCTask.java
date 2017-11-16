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

package cc.suitalk.ipcinvoker.sample.extension;

import cc.suitalk.ipcinvoker.IPCRemoteInvokeCallback;
import cc.suitalk.ipcinvoker.extension.annotation.IPCAsyncInvokeCallback;
import cc.suitalk.ipcinvoker.extension.annotation.IPCAsyncInvokeMethod;
import cc.suitalk.ipcinvoker.extension.annotation.IPCInvokeTaskManager;
import cc.suitalk.ipcinvoker.extension.annotation.IPCSyncInvokeMethod;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/10/26.
 */

@IPCInvokeTaskManager(process = "cc.suitalk.ipcinvoker.sample")
public class CustomIPCTask {

    private static final String TAG = "IPCSample.CustomIPCTask";

    public CustomIPCTask() {
        init();
    }

    @IPCAsyncInvokeMethod
    public void showLoading(int id, String name, InputData data, @IPCAsyncInvokeCallback IPCRemoteInvokeCallback<ResultData> callback) {
        Log.i(TAG, "invokeAsync(id : %s, data : %s, name : %s)", id, data, name);
    }

    @IPCAsyncInvokeMethod
    public void hideLoading(int id) {
        Log.d(TAG, "hideLoading(id : %s)", id);
    }

    @IPCSyncInvokeMethod
    public String getName(int id) {
        Log.d(TAG, "getName(id : %s)", id);
        return "todo";
    }

    public void init() {
        Log.d(TAG, "init(hashCode : %s)", hashCode());
    }

    public static class InputData {
        public String name;
    }

    public static class ResultData {
        public String result;
    }
}
