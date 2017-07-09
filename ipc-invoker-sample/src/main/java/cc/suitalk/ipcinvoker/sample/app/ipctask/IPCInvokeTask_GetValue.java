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

package cc.suitalk.ipcinvoker.sample.app.ipctask;

import android.os.Bundle;

import cc.suitalk.ipcinvoker.IPCRemoteSyncInvoke;
import cc.suitalk.ipcinvoker.IPCSyncInvokeTask;
import cc.suitalk.ipcinvoker.sample.app.model.DataCenter;
import cc.suitalk.ipcinvoker.tools.Log;


/**
 * Created by albieliang on 2017/6/3.
 */

public class IPCInvokeTask_GetValue implements IPCRemoteSyncInvoke<Bundle, Bundle> {

    private static final String TAG = "IPCInvokerSample.IPCInvokeTask_GetValue";

    public static final String KEY = "key";
    public static final String VALUE = "value";

    @Override
    public Bundle invoke(Bundle data) {
        String key = data.getString(KEY);
        Bundle result = new Bundle();
        result.putString(VALUE, DataCenter.getImpl().getString(key));

        Log.i(TAG, "getValue, result : %s", result);

        return result;
    }
}