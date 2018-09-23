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
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcelable;

import junit.framework.Assert;

import cc.suitalk.ipcinvoker.activate.Debuggable;
import cc.suitalk.ipcinvoker.activate.ExecutorServiceCreator;
import cc.suitalk.ipcinvoker.activate.IPCInvokerInitDelegate;
import cc.suitalk.ipcinvoker.activate.IPCInvokerInitializer;
import cc.suitalk.ipcinvoker.activate.ThreadCreator;
import cc.suitalk.ipcinvoker.activate.TypeTransferInitializer;
import cc.suitalk.ipcinvoker.annotation.AnyThread;
import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.annotation.WorkerThread;
import cc.suitalk.ipcinvoker.extension.BaseTypeTransfer;
import cc.suitalk.ipcinvoker.extension.ObjectTypeTransfer;
import cc.suitalk.ipcinvoker.tools.Log;
import cc.suitalk.ipcinvoker.tools.log.ILogPrinter;

/**
 * Created by albieliang on 2017/5/13.
 */

public class IPCInvoker {

    private static final String TAG = "IPC.IPCInvoker";

    public static void setup(@NonNull Application application, @NonNull IPCInvokerInitDelegate delegate) {
        Assert.assertNotNull(application);
        IPCInvokeLogic.setContext(application);
        final IPCInvokerInitializer initializer = new IPCInvokerInitializer() {
            @Override
            public <T extends BaseIPCService> void addIPCService(String processName, Class<T> service) {
                IPCBridgeManager.getImpl().addIPCService(processName, service);
            }

            @Override
            public void setLogPrinter(ILogPrinter printer) {
                Log.setLogPrinter(printer);
            }

            @Override
            public void setExecutorServiceCreator(ExecutorServiceCreator creator) {
                ThreadPool.setExecutorServiceCreator(creator);
            }

            @Override
            public void setThreadCreator(ThreadCreator creator) {
                ThreadPool.setThreadCreator(creator);
            }

            @Override
            public void setDebugger(Debuggable debugger) {
                Debugger.setDebuggable(debugger);
            }
        };
        delegate.onInitialize(initializer);
        delegate.onAddTypeTransfer(new TypeTransferInitializer() {
            @Override
            public void addTypeTransfer(@NonNull BaseTypeTransfer transfer) {
                ObjectTypeTransfer.addTypeTransfer(transfer);
            }
        });
        delegate.onAttachServiceInfo(initializer);
        Log.i(TAG, "setup IPCInvoker(process : %s, application : %s)", IPCInvokeLogic.getCurrentProcessName(), application.hashCode());
    }

    /**
     * Async invoke, it can be invoked on any thread.
     *
     * @param process      remote service process name
     * @param data         data for remote process invoked, it must be a {@link Parcelable}
     * @param taskClass    remote invoke logic task class
     * @param callback     callback on current process after IPC invoked finished and initiative callback.
     * @param <T>          the class implements {@link IPCAsyncInvokeTask} interface
     * @param <InputType>  the class extends {@link Parcelable}
     * @param <ResultType> the class extends {@link Parcelable}
     * @return true if cross-process invoke has been initiated, false otherwise.
     */
    @AnyThread
    public static <T extends IPCAsyncInvokeTask<InputType, ResultType>, InputType extends Parcelable, ResultType extends Parcelable>
    boolean invokeAsync(final String process, final InputType data, @NonNull final Class<T> taskClass, final IPCInvokeCallback<ResultType> callback) {
        return IPCTaskExecutor.invokeAsync(process, data, taskClass, callback, IPCTaskExtInfo.DEFAULT);
    }

    /**
     * Sync invoke, it must be invoked on WorkerThread or make sure the connection is established before invoked.
     *
     * Call {@link IPCInvoker#connectRemoteService(String)} to pre-connect remote Service.
     *
     * @param process      remote service process name
     * @param data         data for remote process invoked, it must be a {@link Parcelable}
     * @param taskClass    remote invoke logic task class
     * @param <T>          the class implements {@link IPCSyncInvokeTask} interface
     * @param <InputType>  the class extends {@link Parcelable}
     * @param <ResultType> the class extends {@link Parcelable}
     * @return the cross-process invoke result.
     */
    @WorkerThread
    public static <T extends IPCSyncInvokeTask<InputType, ResultType>, InputType extends Parcelable, ResultType extends Parcelable>
    ResultType invokeSync(String process, InputType data, @NonNull Class<T> taskClass) {
        return (ResultType) IPCTaskExecutor.invokeSync(process, data, taskClass, IPCTaskExtInfo.DEFAULT);
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
     * Invoke this method to pre-connect Remote Service to improve the performance of the first time IPC invoke.
     *
     * @param process remote service process name
     * @param callback callback for connect remote service
     */
    public static void connectRemoteService(@NonNull final String process, final OnConnectRemoteServiceCallback callback) {
        if (IPCInvokeLogic.isCurrentProcess(process) || hasConnectedRemoteService(process)) {
            if (callback != null) {
                callback.onConnectCallback(true);
            }
            return;
        }
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                final boolean success = IPCBridgeManager.getImpl().getIPCBridge(process, IPCTaskExtInfo.DEFAULT) != null;
                if (callback != null) {
                    callback.onConnectCallback(success);
                }
            }
        });
    }

    /**
     * Invoke this method to disconnect the connection between current process and remote process to release resource.
     *
     * @param process remote service process name
     */
    public static void disconnectRemoteService(@NonNull final String process) {
        if (!hasConnectedRemoteService(process)) {
            return;
        }
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                IPCBridgeManager.getImpl().releaseIPCBridge(process);
            }
        });
    }

    /**
     * Invoke this method to disconnect the connection between current process and remote process to release resource.
     *
     * You must check whether the connection is exists or not by {@link #hasConnectedRemoteService(String)} before.
     *
     * @param callback
     * @param process remote service process name
     * @return true: in disconnectRemoteService process, false: otherwise
     */
    public static boolean disconnectRemoteService(@NonNull final String process, final OnDisconnectRemoteServiceCallback callback) {
        if (!hasConnectedRemoteService(process)) {
            return false;
        }
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                boolean r = false;
                if (hasConnectedRemoteService(process)) {
                    final ServiceConnection sc = new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {
                            ServiceConnectionManager.unregisterServiceConnection(process, this);
                            if (callback != null) {
                                callback.onDisconnectCallback();
                            }
                        }
                    };
                    ServiceConnectionManager.registerServiceConnection(process, sc);
                    r = IPCBridgeManager.getImpl().releaseIPCBridge(process);
                    if (!r) {
                        ServiceConnectionManager.unregisterServiceConnection(process, sc);
                    }
                }
                if (callback != null && !r) {
                    callback.onDisconnectCallback();
                }
            }
        });
        return true;
    }

    /**
     *
     * @param process
     * @return
     */
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

    /**
     * Register the {@link ServiceConnection} by processName.
     *
     * @param processName
     * @param serviceConnection
     * @return
     */
    public static boolean registerServiceConnection(@NonNull String processName, @NonNull ServiceConnection serviceConnection) {
        return ServiceConnectionManager.registerServiceConnection(processName, serviceConnection);
    }

    /**
     *
     * @param processName
     * @param serviceConnection
     * @return
     */
    public static boolean unregisterServiceConnection(@NonNull String processName, @NonNull ServiceConnection serviceConnection) {
        return ServiceConnectionManager.unregisterServiceConnection(processName, serviceConnection);
    }

    /**
     * See {@link #connectRemoteService(String, OnConnectRemoteServiceCallback)}
     */
    public interface OnConnectRemoteServiceCallback {
        void onConnectCallback(boolean success);
    }

    /**
     * See {@link #disconnectRemoteService(String, OnDisconnectRemoteServiceCallback)}
     */
    public interface OnDisconnectRemoteServiceCallback {
        void onDisconnectCallback();
    }

}
