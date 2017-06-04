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

import android.os.Bundle;

import cc.suitalk.ipcinvoker.IPCInvoker;
import cc.suitalk.ipcinvoker.IPCRemoteSyncInvoke;
import cc.suitalk.ipcinvoker.sample.IPCData;
import cc.suitalk.ipcinvoker.sample.service.PushProcessIPCService;

/**
 * Created by albieliang on 2017/5/30.
 */

public class IPCInvokeSample_InvokeByType {

    public static IPCData invokeIPCLogic(String id, int debugType, int pkgVersion) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putInt("type", debugType);
        bundle.putInt("version", 0);
        return IPCInvoker.invokeSync(PushProcessIPCService.PROCESS_NAME, bundle, IPCRemoteInvoke_PrintSomething.class);
    }

    private static class IPCRemoteInvoke_PrintSomething implements IPCRemoteSyncInvoke<Bundle, IPCData> {

        @Override
        public IPCData invoke(Bundle data) {
            IPCData result = new IPCData();
            result.result = data.getString("id") + ":" + data.getInt("type") + ":" + data.getInt("version");
            return result;
        }
    }
}
