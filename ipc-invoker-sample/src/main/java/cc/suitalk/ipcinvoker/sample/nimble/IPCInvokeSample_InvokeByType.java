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

package cc.suitalk.ipcinvoker.sample.nimble;

import cc.suitalk.ipcinvoker.IPCAsyncInvokeTask;
import cc.suitalk.ipcinvoker.IPCInvokeCallback;
import cc.suitalk.ipcinvoker.IPCTask;
import cc.suitalk.ipcinvoker.sample.service.PushProcessIPCService;
import cc.suitalk.ipcinvoker.tools.Log;
import cc.suitalk.ipcinvoker.type.IPCString;

/**
 * Created by albieliang on 2017/7/6.
 */

public class IPCInvokeSample_InvokeByType {

    public static final String TAG = "IPCSample.IPCInvokeSample_InvokeByType";

    public static void invokeIPCLogic() {
        TestType data = new TestType();
        data.key = "wx-developer";
        data.value = "AlbieLiang";
        IPCTask.create(PushProcessIPCService.PROCESS_NAME)
                .async(IPCRemoteInvoke_PrintSomething.class)
                .data(data)
                .callback(true, new IPCInvokeCallback<IPCString>() {

                    @Override
                    public void onCallback(IPCString data) {
                        Log.i(TAG, "result : %s", data.value);
                    }
                }).invoke();
    }

    private static class IPCRemoteInvoke_PrintSomething implements IPCAsyncInvokeTask<TestType, IPCString> {

        @Override
        public void invoke(TestType data, IPCInvokeCallback<IPCString> callback) {
            callback.onCallback(new IPCString(data.key + ":" + data.value));
        }
    }
}
