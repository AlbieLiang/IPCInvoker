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

package cc.suitalk.ipcinvoker.activate;

import cc.suitalk.ipcinvoker.BaseIPCService;
import cc.suitalk.ipcinvoker.tools.log.ILogPrinter;

/**
 * Created by albieliang on 2017/5/28.
 */

public interface IPCInvokerInitializer {

    <T extends BaseIPCService> void addIPCService(String processName, Class<T> service);

    void setLogPrinter(ILogPrinter printer);

    void setExecutorServiceCreator(ExecutorServiceCreator creator);

    void setThreadCreator(ThreadCreator creator);

    void setDebugger(Debuggable debugger);

    /**
     * @see #{Context.BindServiceFlags}
     * @param flags
     */
    void setBindServiceFlags(int flags);
}
