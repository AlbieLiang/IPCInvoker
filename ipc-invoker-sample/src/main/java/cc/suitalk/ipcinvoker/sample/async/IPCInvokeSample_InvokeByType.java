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
import cc.suitalk.ipcinvoker.sample.service.PushProcessIPCService;
import cc.suitalk.ipcinvoker.tools.Log;
import cc.suitalk.ipcinvoker.type.IPCString;

/**
 * Created by albieliang on 2017/5/30.
 */

public class IPCInvokeSample_InvokeByType {

    private static final String TAG = "IPCInvokerSample.IPCInvokeSample_InvokeByType";

    public static void invokeAsync() {
        Bundle bundle = new Bundle();
        bundle.putString("name", "AlbieLiang");
        bundle.putInt("pid", android.os.Process.myPid());
        IPCInvoker.invokeAsync(PushProcessIPCService.PROCESS_NAME, bundle, IPCRemoteInvoke_PrintSomething.class, new IPCInvokeCallback<IPCString>() {
            @Override
            public void onCallback(IPCString data) {
                Log.i(TAG, "onCallback : %s", data.value);
            }
        });
    }

    private static class IPCRemoteInvoke_PrintSomething implements IPCAsyncInvokeTask<Bundle, IPCString> {

        @Override
        public void invoke(Bundle data, IPCInvokeCallback<IPCString> callback) {
            String result = String.format("name:%s|fromPid:%s|curPid:%s", data.getString("name"), data.getInt("pid"), android.os.Process.myPid());
            callback.onCallback(new IPCString(result));
        }
    }
}
