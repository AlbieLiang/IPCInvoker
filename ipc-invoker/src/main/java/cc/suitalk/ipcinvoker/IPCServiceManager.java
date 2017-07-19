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

package cc.suitalk.ipcinvoker;

import java.util.HashMap;
import java.util.Map;

import cc.suitalk.ipcinvoker.annotation.NonNull;

/**
 * Created by albieliang on 2017/5/20.
 */

class IPCServiceManager {

    private static IPCServiceManager sMgr;

    private Map<String, BaseIPCService> mIPCServiceMap;

    private IPCServiceManager() {
        mIPCServiceMap = new HashMap<>();
    }

    public static IPCServiceManager getImpl() {
        if (sMgr == null) {
            synchronized (IPCServiceManager.class) {
                if (sMgr == null) {
                    sMgr = new IPCServiceManager();
                }
            }
        }
        return sMgr;
    }

    public boolean put(@NonNull String process, @NonNull BaseIPCService service) {
        if (process == null || process.length() == 0 || service == null) {
            return false;
        }
        mIPCServiceMap.put(process, service);
        return true;
    }

    public BaseIPCService remove(@NonNull String process) {
        return mIPCServiceMap.remove(process);
    }

    public BaseIPCService get(@NonNull String process) {
        return mIPCServiceMap.get(process);
    }
}
