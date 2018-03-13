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

package cc.suitalk.ipcinvoker.sample.async;

import android.os.Bundle;

import cc.suitalk.ipcinvoker.IPCAsyncInvokeTask;
import cc.suitalk.ipcinvoker.IPCInvokeCallback;
import cc.suitalk.ipcinvoker.IPCInvoker;
import cc.suitalk.ipcinvoker.sample.service.MainProcessIPCService;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/5/14.
 */

public class IPCInvokeSample_InvokeWithBundle {

    private static final String TAG = "IPCInvokerSample.IPCInvokeSample_InvokeWithBundle";

    private static final String INNER_KEY_RESULT = "__result";

    public static void invokeAsync() {
        Bundle bundle = new Bundle();
        bundle.putString("name", "AlbieLiang");
        bundle.putInt("pid", android.os.Process.myPid());
        bundle.putInt("version", 0);
        IPCInvoker.invokeAsync(MainProcessIPCService.PROCESS_NAME, bundle, IPCInvokeTask_doSomething.class, new IPCInvokeCallback<Bundle>() {
            @Override
            public void onCallback(Bundle data) {
                Log.i(TAG, "onCallback : %s", data.getString(INNER_KEY_RESULT));
            }

        });
    }

    private static class IPCInvokeTask_doSomething implements IPCAsyncInvokeTask<Bundle, Bundle> {

        @Override
        public void invoke(Bundle data, IPCInvokeCallback<Bundle> callback) {
            String name = data.getString("name");
            int pid = data.getInt("type");
            Bundle bundle = new Bundle();
            bundle.putString(INNER_KEY_RESULT, String.format("name:%s|fromPid:%s|curPid:%s", name, pid, android.os.Process.myPid()));
            callback.onCallback(bundle);
        }
    }
}
