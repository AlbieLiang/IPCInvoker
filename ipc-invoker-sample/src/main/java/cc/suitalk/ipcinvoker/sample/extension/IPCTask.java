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

import android.os.Bundle;

import cc.suitalk.ipcinvoker.IPCInvokeLogic;
import cc.suitalk.ipcinvoker.IPCRemoteInvokeCallback;
import cc.suitalk.ipcinvoker.annotation.Singleton;
import cc.suitalk.ipcinvoker.extension.annotation.IPCAsyncInvokeCallback;
import cc.suitalk.ipcinvoker.extension.annotation.IPCAsyncInvokeMethod;
import cc.suitalk.ipcinvoker.extension.annotation.IPCInvokeTask;
import cc.suitalk.ipcinvoker.extension.annotation.IPCSyncInvokeMethod;
import cc.suitalk.ipcinvoker.sample.app.model.DataCenter;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/10/26.
 */

@IPCInvokeTask(process = "cc.suitalk.ipcinvoker.sample:support")
public class IPCTask {

    private static final String TAG = "IPCSample.IPCTask";

    public IPCTask() {
        init();
    }

    @IPCAsyncInvokeMethod
    public void showLoading(@IPCAsyncInvokeCallback IPCRemoteInvokeCallback<ResultData> callback, InputData data) {
        Log.i(TAG, "invokeAsync(data : %s)", data);
    }

    @IPCAsyncInvokeMethod
    public void setValue(String key, String value, @IPCAsyncInvokeCallback IPCRemoteInvokeCallback<Bundle> callback) {
        Log.d(TAG, "setValue(key : %s, value : %s)", key, value);
        DataCenter.getImpl().putString(key, value);
        Bundle result = new Bundle();
        String resultStr = String.format("pid : %s\nprocess : %s\nresultSize : %s",
                android.os.Process.myPid(), IPCInvokeLogic.getCurrentProcessName(), DataCenter.getImpl().getMap().size());
        result.putString("result", resultStr);
        if (callback != null) {
            callback.onCallback(result);
        }
    }

    @IPCSyncInvokeMethod
    public String getValue(String key) {
        String value = DataCenter.getImpl().getString(key);
        Log.d(TAG, "getValue(key : %s, value : %s)", key, value);
        return value;
    }

    public void init() {
        Log.d(TAG, "init(hashCode : %s)", hashCode());
    }

    @Singleton
    public static class InputData {
        public String name;
    }

    public static class ResultData {
        public String result;
    }
}
