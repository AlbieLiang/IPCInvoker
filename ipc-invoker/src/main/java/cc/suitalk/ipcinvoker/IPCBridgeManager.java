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
import android.os.Looper;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import cc.suitalk.ipcinvoker.aidl.AIDL_IPCInvokeBridge;
import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.annotation.WorkerThread;
import cc.suitalk.ipcinvoker.exception.OnExceptionObserver;
import cc.suitalk.ipcinvoker.exception.RemoteServiceNotConnectedException;
import cc.suitalk.ipcinvoker.recycle.DeathRecipientImpl;
import cc.suitalk.ipcinvoker.recycle.ObjectRecycler;
import cc.suitalk.ipcinvoker.tools.Log;
import cc.suitalk.ipcinvoker.tools.SafeConcurrentHashMap;

/**
 * Created by albieliang on 2017/5/14.
 */

class IPCBridgeManager {

    private static final String TAG = "IPC.IPCBridgeManager";

    private static volatile IPCBridgeManager sInstance;

    private Map<String, Class<?>> mServiceClassMap;
    private Handler mHandler;

    private Map<String, IPCBridgeWrapper> mBridgeMap;

    private volatile boolean mLockCreateBridge;
    private int mBindServiceFlags = Context.BIND_AUTO_CREATE | Context.BIND_WAIVE_PRIORITY;

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
        mBridgeMap = new SafeConcurrentHashMap<>();
        //
        HandlerThread thread = new HandlerThread("IPCBridgeThread#" + hashCode());
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    @WorkerThread
    public AIDL_IPCInvokeBridge getIPCBridge(@NonNull final String process, @NonNull IPCTaskExtInfo extInfo) {
        IPCBridgeWrapper bridgeWrapper;
        synchronized (mBridgeMap) {
            bridgeWrapper = mBridgeMap.get(process);
            Log.d(TAG, "getIPCBridge(%s), getFromMap(bw : %s)", process, bridgeWrapper != null ? bridgeWrapper.hashCode() : null);
            if (bridgeWrapper != null) {
                try {
                    bridgeWrapper.latch.await();
                } catch (InterruptedException e) {
                    Log.e(TAG, "getIPCBridge, latch.await() error, %s", e);
                }
                return bridgeWrapper.bridge;
            }
            bridgeWrapper = new IPCBridgeWrapper();
            mBridgeMap.put(process, bridgeWrapper);
        }
        if (mLockCreateBridge) {
            Log.i(TAG, "build IPCBridge(process : %s) failed, locked.", process);
            return null;
        }
        if (Looper.getMainLooper() == Looper.myLooper()) {
            RemoteServiceNotConnectedException e = new RemoteServiceNotConnectedException("can not invoke on main-thread, the remote service not connected.");
            Log.w(TAG, "getIPCBridge failed, can not create bridge on Main thread. exception : %s", android.util.Log.getStackTraceString(e));
            if (Debugger.isDebug()) {
                throw e;
            }
            return null;
        }
        Class<?> serviceClass = getServiceClass(process);
        if (serviceClass == null) {
            Log.w(TAG, "getServiceClass by '%s', got null.", process);
            return null;
        }
        final Context context = IPCInvokeLogic.getContext();
        final IPCBridgeWrapper bw = bridgeWrapper;
        final OnExceptionObserver onExceptionObserver = extInfo.getOnExceptionObserver();
        final ServiceConnection serviceConnection = extInfo.getServiceConnection();

        final ServiceConnection sc = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (service == null) {
                    Log.i(TAG, "onServiceConnected(%s), but service is null", bw.hashCode());
                    BindServiceExecutor.unbindService(context, this);
                    synchronized (mBridgeMap) {
                        mBridgeMap.remove(process);
                    }
                    synchronized (bw) {
                        bw.serviceConnection = null;
                        bw.bridge = null;
                    }
                } else {
                    Log.i(TAG, "onServiceConnected(%s)", bw.hashCode());
                    synchronized (bw) {
                        bw.bridge = AIDL_IPCInvokeBridge.Stub.asInterface(service);
                    }
                    try {
                        service.linkToDeath(new DeathRecipientImpl(process), 0);
                    } catch (RemoteException e) {
                        Log.e(TAG, "binder register linkToDeath listener error, %s", android.util.Log.getStackTraceString(e));
                    }
                }
                if (bw.connectTimeoutRunnable != null) {
                    mHandler.removeCallbacks(bw.connectTimeoutRunnable);
                }
                synchronized (bw) {
                    bw.connectTimeoutRunnable = null;
                }
                bw.latch.countDown();
                if (serviceConnection != null) {
                    serviceConnection.onServiceConnected(name, service);
                }
                ServiceConnectionManager.dispatchOnServiceConnected(process, name, service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.i(TAG, "onServiceDisconnected(%s)", bw.hashCode());
                BindServiceExecutor.unbindService(context, this);
                final IPCBridgeWrapper bridgeWrapper;
                synchronized (mBridgeMap) {
                    bridgeWrapper = mBridgeMap.remove(process);
                }
                if (bridgeWrapper == null) {
                    Log.i(TAG, "onServiceDisconnected(%s), IPCBridgeWrapper is null.", process);
                    return;
                }
                if (bridgeWrapper != bw) {
                    Log.i(TAG, "onServiceDisconnected(%s), IPCBridgeWrapper(pbw : %s, cbw : %s) has expired, skip."
                            , bw.hashCode(), bridgeWrapper.hashCode(), process);
                    return;
                }
                bw.latch.countDown();
                bridgeWrapper.latch.countDown();
                synchronized (bridgeWrapper) {
                    bridgeWrapper.bridge = null;
                    bridgeWrapper.serviceConnection = null;
                }
                ObjectRecycler.recycleAll(process);
                if (serviceConnection != null) {
                    serviceConnection.onServiceDisconnected(name);
                }
                ServiceConnectionManager.dispatchOnServiceDisconnected(process, name);
            }
        };
        synchronized (bw) {
            bw.serviceConnection = sc;
        }
        try {
            final Intent intent = new Intent(context, serviceClass);
            Log.i(TAG, "bindService(bw : %s, tid : %s, intent : %s)", bw.hashCode(), Thread.currentThread().getId(), intent);
            BindServiceExecutor.bindService(context, intent, sc, mBindServiceFlags);
            bw.connectTimeoutRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "on connect timeout(bw : %s, tid : %s, latchCount : %d)", bw.hashCode(), Thread.currentThread().getId(), bw.latch.getCount());
                    if (bw.latch.getCount() == 0) {
                        return;
                    }
                    bw.latch.countDown();
                    // Prevent deadlocks
                    synchronized (bw) {
                        bw.connectTimeoutRunnable = null;
                    }
                    synchronized (mBridgeMap) {
                        mBridgeMap.remove(process);
                    }
                }
            };
            mHandler.postDelayed(bw.connectTimeoutRunnable, extInfo.getTimeout());
            bw.latch.await();
            if (bw.connectTimeoutRunnable != null) {
                mHandler.removeCallbacks(bw.connectTimeoutRunnable);
                bw.connectTimeoutRunnable = null;
            }
        } catch (InterruptedException | SecurityException e) {
            Log.e(TAG, "bindService error : %s", android.util.Log.getStackTraceString(e));
            synchronized (mBridgeMap) {
                mBridgeMap.remove(process);
            }
            if (onExceptionObserver != null) {
                onExceptionObserver.onExceptionOccur(e);
            }
            GlobalExceptionManager.dispatchException(e);
            return null;
        } finally {
        }
        return bridgeWrapper.bridge;
    }

    @WorkerThread
    public boolean hasIPCBridge(@NonNull final String process) {
        if (IPCInvokeLogic.isCurrentProcess(process)) {
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
        getIPCBridge(process, IPCTaskExtInfo.DEFAULT);
    }

    /**
     * Release IPC Bridge.
     *
     * @param process
     * @return true: in releaseIPCBridge process, false: otherwise.
     */
    @WorkerThread
    public boolean releaseIPCBridge(@NonNull final String process) {
        if (IPCInvokeLogic.isCurrentProcess(process)) {
            Log.i(TAG, "the same process(%s), do not need to release IPCBridge.", process);
            return false;
        }
        final IPCBridgeWrapper bridgeWrapper;
        synchronized (mBridgeMap) {
            bridgeWrapper = mBridgeMap.get(process);
        }
        if (bridgeWrapper == null) {
            Log.i(TAG, "releaseIPCBridge(%s) failed, IPCBridgeWrapper is null.", process);
            return false;
        }
        bridgeWrapper.latch.countDown();
        synchronized (bridgeWrapper) {
            if (bridgeWrapper.serviceConnection == null) {
                Log.i(TAG, "releaseIPCBridge(%s) failed, ServiceConnection is null.", process);
                return false;
            }
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ServiceConnection sc;
                synchronized (bridgeWrapper) {
                    sc = bridgeWrapper.serviceConnection;
                }
                if (sc == null) {
                    Log.i(TAG, "releaseIPCBridge(%s) failed, ServiceConnection is null.", process);
                    return;
                }
                BindServiceExecutor.unbindService(IPCInvokeLogic.getContext(), sc);
                synchronized (mBridgeMap) {
                    mBridgeMap.remove(process);
                }
                synchronized (bridgeWrapper) {
                    bridgeWrapper.bridge = null;
                    bridgeWrapper.serviceConnection = null;
                }
            }
        });
        return true;
    }

    public synchronized void lockCreateBridge(boolean lock) {
        mLockCreateBridge = lock;
    }

    public void releaseAllIPCBridge() {
        Log.i(TAG, "releaseAllIPCBridge");
        if (mBridgeMap.isEmpty()) {
            return;
        }
        Set<String> keySet = null;
        synchronized (mBridgeMap) {
            if (mBridgeMap.isEmpty()) {
                return;
            }
            keySet = new HashSet<>(mBridgeMap.keySet());
        }
        if (keySet == null || keySet.isEmpty()) {
            return;
        }
        for (String process : keySet) {
            releaseIPCBridge(process);
        }
    }

    public <T extends BaseIPCService> void addIPCService(String processName, Class<T> service) {
        mServiceClassMap.put(processName, service);
    }

    public void setBindServiceFlags(int bindServiceFlags) {
        this.mBindServiceFlags = bindServiceFlags;
    }

    private static class IPCBridgeWrapper {
        AIDL_IPCInvokeBridge bridge;
        ServiceConnection serviceConnection;
        Runnable connectTimeoutRunnable;
        final CountDownLatch latch = new CountDownLatch(1);
    }
}
