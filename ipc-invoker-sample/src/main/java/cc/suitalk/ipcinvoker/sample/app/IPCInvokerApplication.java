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

package cc.suitalk.ipcinvoker.sample.app;

import android.app.Application;

import cc.suitalk.ipcinvoker.IPCInvoker;
import cc.suitalk.ipcinvoker.model.IPCInvokerInitDelegate;
import cc.suitalk.ipcinvoker.model.IPCInvokerInitializer;
import cc.suitalk.ipcinvoker.sample.service.PushProcessIPCService;
import cc.suitalk.ipcinvoker.sample.service.MainProcessIPCService;
import cc.suitalk.ipcinvoker.sample.service.SupportProcessIPCService;
import cc.suitalk.ipcinvoker.tools.DefaultLogPrinter;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/5/28.
 */

public class IPCInvokerApplication extends Application {

    private static final String TAG = "IPCInvokerSample.IPCInvokerApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        // Set UncaughtExceptionHandler
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.e(TAG, "uncaughtException : %s", android.util.Log.getStackTraceString(e));
            }
        });
        // Initialize IPCInvoker
        IPCInvoker.setup(this, new IPCInvokerInitDelegate() {
            @Override
            public void onAttachServiceInfo(IPCInvokerInitializer initializer) {
                initializer.addIPCService(MainProcessIPCService.PROCESS_NAME, MainProcessIPCService.class);
                initializer.addIPCService(SupportProcessIPCService.PROCESS_NAME, SupportProcessIPCService.class);
                initializer.addIPCService(PushProcessIPCService.PROCESS_NAME, PushProcessIPCService.class);
            }
        });
    }
}
