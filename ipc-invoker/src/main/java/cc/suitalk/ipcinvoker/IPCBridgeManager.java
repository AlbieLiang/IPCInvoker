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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import cc.suitalk.ipcinvoker.aidl.AIDL_IPCInvokeBridge;
import cc.suitalk.ipcinvoker.model.IPCInvokerInitializer;
import cc.suitalk.ipcinvoker.tools.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by albieliang on 2017/5/14.
 */

class IPCBridgeManager implements IPCInvokerInitializer {

    private static final String TAG = "IPC.IPCBridgeManager";

    private static IPCBridgeManager sInstance;

    private Map<String, Class<?>> mServiceClassMap;
    private Handler mHandler;

    private Map<String, IPCBridgeWrapper> mBridgeMap;

    public static IPCBridgeManager getImpl() {
        if (sInstance == null) {
            synchronized (IPCBridgeManager.class) {
                if (sInstance == null) {
                    sInstance = new IPCBridgeManager();
                }
            }
        }
        return sInstance;
    }

    private Class<?> getServiceClass(String process) {
        return mServiceClassMap.get(process);
    }

    private IPCBridgeManager() {
        mServiceClassMap = new HashMap<>();
        mBridgeMap = new ConcurrentHashMap<>();
        //
        HandlerThread thread = new HandlerThread("IPCBridgeThread#" + hashCode());
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    @WorkerThread
    public AIDL_IPCInvokeBridge getIPCBridge(@NonNull final String process) {
        IPCBridgeWrapper bridgeWrapper = mBridgeMap.get(process);
        if (bridgeWrapper == null) {
            bridgeWrapper = new IPCBridgeWrapper();
            synchronized (mBridgeMap) {
                mBridgeMap.put(process, bridgeWrapper);
            }
            synchronized (bridgeWrapper) {
                bridgeWrapper.isConnecting = true;
            }
            final Context context = IPCInvokeLogic.getContext();
            final IPCBridgeWrapper bw = bridgeWrapper;
            bw.serviceConnection = new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    bw.bridge = AIDL_IPCInvokeBridge.Stub.asInterface(service);
                    synchronized (bw) {
                        bw.isConnecting = false;
                        bw.notifyAll();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mBridgeMap.remove(process);
                    bw.bridge = null;
                    synchronized (bw) {
                        bw.isConnecting = false;
                    }
                    bw.serviceConnection = null;
                }
            };
            final Intent intent = new Intent(context, getServiceClass(process));
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    context.bindService(intent, bw.serviceConnection, Context.BIND_AUTO_CREATE);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!bw.isConnecting) {
                                return;
                            }
                            // Prevent deadlocks
                            synchronized (bw) {
                                if (!bw.isConnecting) {
                                    return;
                                }
                                bw.isConnecting = false;
                                bw.notifyAll();
                            }
                            synchronized (mBridgeMap) {
                                mBridgeMap.remove(process);
                            }
                        }
                    }, 10 * 1000);
                }
            });
            try {
                synchronized (bw) {
                    if (bw.isConnecting) {
                        bw.wait();
                    }
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "%s", e);
                synchronized (mBridgeMap) {
                    mBridgeMap.remove(process);
                }
                return null;
            } finally {
                synchronized (bw) {
                    bw.isConnecting = false;
                }
            }
        } else {
            if (bridgeWrapper.isConnecting) {
                try {
                    synchronized (bridgeWrapper) {
                        if (bridgeWrapper.isConnecting) {
                            bridgeWrapper.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "%s", e);
                }
            }
        }
        return bridgeWrapper.bridge;
    }

    @WorkerThread
    public boolean hasIPCBridge(@NonNull final String process) {
        if (IPCInvokeLogic.isCurrentProcess(process)) {
            Log.i(TAG, "the same process(%s), do not need to build IPCBridge.", process);
            return false;
        }
        return mBridgeMap.get(process) != null;
    }

    @WorkerThread
    public void prepareIPCBridge(@NonNull final String process) {
        if (IPCInvokeLogic.isCurrentProcess(process)) {
            Log.i(TAG, "the same process(%s), do not need to build IPCBridge.", process);
            return;
        }
        getIPCBridge(process);
    }

    @WorkerThread
    public void releaseIPCBridge(@NonNull final String process) {
        if (IPCInvokeLogic.isCurrentProcess(process)) {
            Log.i(TAG, "the same process(%s), do not need to release IPCBridge.", process);
            return;
        }
        final IPCBridgeWrapper bridgeWrapper;
        synchronized (mBridgeMap) {
            bridgeWrapper = mBridgeMap.get(process);
        }
        if (bridgeWrapper == null) {
            Log.i(TAG, "releaseIPCBridge(%s) failed, IPCBridgeWrapper is null.", process);
            return;
        }
        if (bridgeWrapper.isConnecting) {
            synchronized (bridgeWrapper) {
                bridgeWrapper.isConnecting = false;
                bridgeWrapper.notifyAll();
            }
        }
        if (bridgeWrapper.serviceConnection == null) {
            Log.i(TAG, "releaseIPCBridge(%s) failed, ServiceConnection is null.", process);
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (bridgeWrapper.serviceConnection == null) {
                    Log.i(TAG, "releaseIPCBridge(%s) failed, ServiceConnection is null.", process);
                    return;
                }
                IPCInvokeLogic.getContext().unbindService(bridgeWrapper.serviceConnection);
                synchronized (mBridgeMap) {
                    mBridgeMap.remove(process);
                }
                synchronized (bridgeWrapper) {
                    bridgeWrapper.bridge = null;
                    bridgeWrapper.isConnecting = false;
                    bridgeWrapper.serviceConnection = null;
                }
            }
        });
    }

    @Override
    public <T extends BaseIPCService> void addIPCService(String processName, Class<T> service) {
        mServiceClassMap.put(processName, service);
    }

    private static class IPCBridgeWrapper {
        AIDL_IPCInvokeBridge bridge;
        ServiceConnection serviceConnection;
        volatile boolean isConnecting;
    }
}
