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

import android.app.Application;

import junit.framework.Assert;

import cc.suitalk.ipcinvoker.activate.IPCInvokerInitDelegate;
import cc.suitalk.ipcinvoker.activate.TypeTransferInitializer;
import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.extension.BaseTypeTransfer;
import cc.suitalk.ipcinvoker.extension.ObjectTypeTransfer;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/7/8.
 */

public class IPCInvokerBoot {

    private static final String TAG = "IPC.IPCInvokerBoot";

    public static void setup(@NonNull Application application, @NonNull IPCInvokerInitDelegate delegate) {
        Assert.assertNotNull(application);
        IPCInvokeLogic.setContext(application);
        delegate.onAddTypeTransfer(new TypeTransferInitializer() {
            @Override
            public void addTypeTransfer(@NonNull BaseTypeTransfer transfer) {
                ObjectTypeTransfer.addTypeTransfer(transfer);
            }
        });
        delegate.onAttachServiceInfo(IPCBridgeManager.getImpl());
        Log.i(TAG, "setup IPCInvoker(process : %s, application : %s)", IPCInvokeLogic.getCurrentProcessName(), application.hashCode());
    }

    /**
     * Invoke this method to pre-connect Remote Service to improve the performance of the first time IPC invoke.
     *
     * @param process remote service process name
     */
    public static void connectRemoteService(@NonNull final String process) {
        if (hasConnectedRemoteService(process)) {
            return;
        }
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                IPCBridgeManager.getImpl().prepareIPCBridge(process);
            }
        });
    }

    /**
     * Invoke this method to disconnect the connection between current process and remote process to release resource.
     *
     * @param process remote service process name
     */
    public static void disconnectRemoteService(@NonNull final String process) {
        if (hasConnectedRemoteService(process)) {
            return;
        }
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                IPCBridgeManager.getImpl().releaseIPCBridge(process);
            }
        });
    }

    public static boolean hasConnectedRemoteService(@NonNull String process) {
        return IPCBridgeManager.getImpl().hasIPCBridge(process);
    }

    /**
     * Invoke this method to disconnect all of the connections between current process and remote process to release resource.
     */
    public static void disconnectAllRemoteService() {
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                IPCBridgeManager.getImpl().releaseAllIPCBridge();
            }
        });
    }

}
