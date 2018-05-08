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
import android.os.HandlerThread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import cc.suitalk.ipcinvoker.IPCInvokerBoot;
import cc.suitalk.ipcinvoker.activate.Debuggable;
import cc.suitalk.ipcinvoker.activate.DefaultInitDelegate;
import cc.suitalk.ipcinvoker.activate.ExecutorServiceCreator;
import cc.suitalk.ipcinvoker.activate.IPCInvokerInitializer;
import cc.suitalk.ipcinvoker.activate.ThreadCreator;
import cc.suitalk.ipcinvoker.activate.TypeTransferInitializer;
import cc.suitalk.ipcinvoker.sample.BuildConfig;
import cc.suitalk.ipcinvoker.sample.nimble.TestTypeTransfer;
import cc.suitalk.ipcinvoker.sample.service.PushProcessIPCService;
import cc.suitalk.ipcinvoker.sample.service.MainProcessIPCService;
import cc.suitalk.ipcinvoker.sample.service.SupportProcessIPCService;
import cc.suitalk.ipcinvoker.tools.DefaultLogPrinter;
import cc.suitalk.ipcinvoker.tools.Log;
import cc.suitalk.ipcinvoker.tools.log.ILogPrinter;

/**
 * Created by albieliang on 2017/5/28.
 */

public class IPCInvokerApplication extends Application {

    private static final String TAG = "IPCInvokerSample.IPCInvokerApplication";

    private static final ILogPrinter sLogPrinter = new DefaultLogPrinter();

    @Override
    public void onCreate() {
        super.onCreate();
        // Set UncaughtExceptionHandler
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.e(TAG, "uncaughtException : %s", android.util.Log.getStackTraceString(e));
                System.exit(0);
            }
        });
        // Initialize IPCInvoker
        IPCInvokerBoot.setup(this, new DefaultInitDelegate() {

            @Override
            public void onInitialize(IPCInvokerInitializer initializer) {
                initializer.setLogPrinter(sLogPrinter);
//                initializer.setExecutorServiceCreator(new ExecutorServiceCreator() {
//                    @Override
//                    public ExecutorService create() {
//                        return new ScheduledThreadPoolExecutor(5);
//                    }
//                });
                initializer.setDebugger(new Debuggable() {
                    @Override
                    public boolean isDebug() {
                        return BuildConfig.DEBUG;
                    }
                });
                initializer.setThreadCreator(new ThreadCreator() {
                    @Override
                    public Thread createThread(Runnable run, String name) {
                        return new Thread(run, name);
                    }

                    @Override
                    public HandlerThread createHandlerThread(String name) {
                        return new HandlerThread(name);
                    }
                });
            }

            @Override
            public void onAttachServiceInfo(IPCInvokerInitializer initializer) {
                initializer.addIPCService(MainProcessIPCService.PROCESS_NAME, MainProcessIPCService.class);
                initializer.addIPCService(SupportProcessIPCService.PROCESS_NAME, SupportProcessIPCService.class);
                initializer.addIPCService(PushProcessIPCService.PROCESS_NAME, PushProcessIPCService.class);
            }

            @Override
            public void onAddTypeTransfer(TypeTransferInitializer initializer) {
                super.onAddTypeTransfer(initializer);
                initializer.addTypeTransfer(new TestTypeTransfer());
            }
        });
    }
}
