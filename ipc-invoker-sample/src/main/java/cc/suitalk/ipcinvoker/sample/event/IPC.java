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

package cc.suitalk.ipcinvoker.sample.event;

import cc.suitalk.ipcinvoker.IPCInvokeClient;
import cc.suitalk.ipcinvoker.sample.service.MainProcessIPCService;
import cc.suitalk.ipcinvoker.sample.service.PushProcessIPCService;
import cc.suitalk.ipcinvoker.sample.service.SupportProcessIPCService;

/**
 * Created by albieliang on 2017/6/18.
 */

public class IPC {

    private static final IPCInvokeClient sMainProcessClient = new IPCInvokeClient(MainProcessIPCService.PROCESS_NAME);
    private static final IPCInvokeClient sSupportProcessClient = new IPCInvokeClient(SupportProcessIPCService.PROCESS_NAME);
    private static final IPCInvokeClient sPushProcessClient = new IPCInvokeClient(PushProcessIPCService.PROCESS_NAME);

    public static IPCInvokeClient getMainIPCClient() {
        return sMainProcessClient;
    }

    public static IPCInvokeClient getSupportIPCClient() {
        return sSupportProcessClient;
    }

    public static IPCInvokeClient getPushIPCClient() {
        return sPushProcessClient;
    }
}
