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
import cc.suitalk.ipcinvoker.IPCSyncInvokeTask;
import cc.suitalk.ipcinvoker.sample.IPCSampleData;
import cc.suitalk.ipcinvoker.sample.service.MainProcessIPCService;

/**
 * Created by albieliang on 2017/5/14.
 */

public class IPCInvokeSample_InvokeWithBundle {

    private static final String INNER_KEY_RESULT = "__result";

    public static Bundle invokeIPCLogic(String id, int debugType, int pkgVersion) {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putInt("type", debugType);
        bundle.putInt("version", 0);
        Bundle result = IPCInvoker.invokeSync(MainProcessIPCService.PROCESS_NAME, bundle, IPCInvokeTask_CheckWxaPkg.class);
        return result.getParcelable(INNER_KEY_RESULT);
    }

    private static class IPCInvokeTask_CheckWxaPkg implements IPCSyncInvokeTask {
        @Override
        public Bundle invoke(Bundle data) {
            String id = data.getString("id");
            int type = data.getInt("type");
            int version = data.getInt("version");
            IPCSampleData result = new IPCSampleData();
            result.result = String.format("id:%s|type:%s|version:%s", id, type, version);
            // Add remote logic here
            Bundle bundle = new Bundle();
            bundle.putParcelable(INNER_KEY_RESULT, result);
            return bundle;
        }
    }

}
