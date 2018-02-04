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

package cc.suitalk.ipcinvoker.sample.sync;

import cc.suitalk.ipcinvoker.IPCInvoker;
import cc.suitalk.ipcinvoker.IPCRemoteSyncInvoke;
import cc.suitalk.ipcinvoker.sample.service.PushProcessIPCService;
import cc.suitalk.ipcinvoker.tools.Log;
import cc.suitalk.ipcinvoker.type.IPCLong;
import cc.suitalk.ipcinvoker.type.IPCString;

/**
 * Created by albieliang on 2017/5/30.
 */

public class IPCInvokeSample_InvokeSync {
    private static final String TAG = "InvokerSync";

    public static void invokeSync() {
        IPCString result = IPCInvoker.invokeSync(PushProcessIPCService.PROCESS_NAME,
                new IPCLong(System.nanoTime()), IPCRemoteInvoke_BuildString.class);
        Log.i(TAG, "invoke result : %s", result);
    }
    private static class IPCRemoteInvoke_BuildString implements IPCRemoteSyncInvoke<IPCLong, IPCString> {
        @Override
        public IPCString invoke(IPCLong data) {
            String msg = String.format("data:%s|curPid:%s", data, android.os.Process.myPid());
            Log.i(TAG, "build String : %s", msg);
            return new IPCString(msg);
        }
    }
}
