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

package cc.suitalk.ipcinvoker.sample.extension.gencode;

import android.os.Bundle;

import cc.suitalk.ipcinvoker.IPCAsyncInvokeTask;
import cc.suitalk.ipcinvoker.IPCInvokeCallback;
import cc.suitalk.ipcinvoker.IPCInvoker;
import cc.suitalk.ipcinvoker.IPCRemoteInvokeCallback;
import cc.suitalk.ipcinvoker.IPCSyncInvokeTask;
import cc.suitalk.ipcinvoker.ObjectStore;
import cc.suitalk.ipcinvoker.annotation.Singleton;
import cc.suitalk.ipcinvoker.extension.ParameterHelper;
import cc.suitalk.ipcinvoker.extension.annotation.IPCAsyncInvokeCallback;
import cc.suitalk.ipcinvoker.extension.annotation.IPCAsyncInvokeMethod;
import cc.suitalk.ipcinvoker.extension.annotation.IPCSyncInvokeMethod;
import cc.suitalk.ipcinvoker.sample.extension.CustomIPCTask;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/10/26.
 */

public class CustomIPCTask$Proxy {

    private static final String TAG = "IPCSample.CustomIPCTask";

    public static final String PROCESS = "cc.suitalk.ipcinvoker.sample:support";

    public static final Class<?> TARGET_CLASS = CustomIPCTask.class;

    private static final String KEY_INVOKE_METHOD = "__invoke_method";

    private static final int ASYNC_INVOKE_METHOD_showLoading = 0;
    private static final int ASYNC_INVOKE_METHOD_hideLoading = 1;
    private static final int SYNC_INVOKE_METHOD_getName = 2;

    @IPCAsyncInvokeMethod
    public void showLoading(int id, String name, CustomIPCTask.InputData data, @IPCAsyncInvokeCallback final IPCRemoteInvokeCallback<CustomIPCTask.ResultData> callback) {
        Log.d(TAG, "showLoading(id : %s, name : %s, data : %s)", id, name, data);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_INVOKE_METHOD, ASYNC_INVOKE_METHOD_showLoading);
        // Parameter
        ParameterHelper.put(bundle, "id", id);
        ParameterHelper.put(bundle, "name", name);
        ParameterHelper.put(bundle, "data", data);
        // Callback
        IPCInvokeCallback __callback = null;
        if (callback != null) {
            __callback = new IPCInvokeCallback() {

                @Override
                public void onCallback(Bundle data) {
                    if (callback != null) {
                        CustomIPCTask.ResultData result = ParameterHelper.get(data, "__result");
                        callback.onCallback(result);
                    }
                }
            };
        }
        IPCInvoker.invokeAsync(PROCESS, bundle, IPCAsyncInvokeTaskImpl.class, __callback);
    }

    @IPCAsyncInvokeMethod
    public void hideLoading(int id) {
        Log.d(TAG, "hideLoading(id : %s)", id);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_INVOKE_METHOD, ASYNC_INVOKE_METHOD_hideLoading);
        // Parameter
        ParameterHelper.put(bundle, "id", id);
        // Callback
        IPCInvokeCallback __callback = null;
        IPCInvoker.invokeAsync(PROCESS, bundle, IPCAsyncInvokeTaskImpl.class, __callback);
    }

    @IPCSyncInvokeMethod
    public String getName(int id) {
        Log.d(TAG, "getName(id : %s)", id);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_INVOKE_METHOD, SYNC_INVOKE_METHOD_getName);
        // Parameter
        ParameterHelper.put(bundle, "id", id);
        Bundle result = IPCInvoker.invokeSync(PROCESS, bundle, IPCSyncInvokeTaskImpl.class);
        if (result == null) {
            // Get width fallback
            return null;
        }
        return ParameterHelper.get(result, "result");
    }

    // Get singleton target object
    private static CustomIPCTask getTarget() {
        CustomIPCTask task = ObjectStore.get(TARGET_CLASS);
        if (task == null) {
            synchronized (CustomIPCTask$Proxy.class) {
                task = ObjectStore.get(TARGET_CLASS);
                if (task == null) {
                    task = new CustomIPCTask();
                    ObjectStore.put(task);
                }
            }
        }
        return task;
    }

    @Singleton
    private static final class IPCAsyncInvokeTaskImpl implements IPCAsyncInvokeTask {

        @Override
        public void invoke(Bundle __data, final IPCInvokeCallback __callback) {
            int invokeMethod = __data.getInt(KEY_INVOKE_METHOD);
            switch (invokeMethod) {
                case ASYNC_INVOKE_METHOD_showLoading: {
                    // Get parameters
                    final int id = ParameterHelper.get(__data, "id");
                    final String name = ParameterHelper.get(__data, "name");
                    final CustomIPCTask.InputData data = ParameterHelper.get(__data, "data");
                    // Obtain target task
                    final CustomIPCTask __task = getTarget();
                    // Create callback proxy
                    IPCRemoteInvokeCallback __callbackProxy = null;
                    if (__callback != null) {
                        __callbackProxy = new IPCRemoteInvokeCallback<CustomIPCTask.ResultData>() {
                            @Override
                            public void onCallback(CustomIPCTask.ResultData data) {
                                Bundle result = new Bundle();
                                ParameterHelper.put(result, "__result", data);
                                __callback.onCallback(result);
                            }
                        };
                    }
                    // Invoke method
                    __task.showLoading(id, name, data, __callbackProxy);
                    break;
                }
                case ASYNC_INVOKE_METHOD_hideLoading: {
                    // Get parameters
                    final int id = ParameterHelper.get(__data, "id");
                    // Obtain target task
                    final CustomIPCTask __task = getTarget();
                    // Invoke method
                    __task.hideLoading(id);
                    break;
                }
            }
        }
    }

    @Singleton
    private static final class IPCSyncInvokeTaskImpl implements IPCSyncInvokeTask {

        @Override
        public Bundle invoke(Bundle data) {
            int invokeMethod = data.getInt(KEY_INVOKE_METHOD);
            switch (invokeMethod) {
                case SYNC_INVOKE_METHOD_getName: {
                    // Get parameters
                    final int id = data.getInt("id");
                    // Obtain target task
                    CustomIPCTask __task = getTarget();
                    // Invoke method
                    String result = __task.getName(id);
                    // Create result
                    Bundle bundle = new Bundle();
                    ParameterHelper.put(bundle, "result", result);
                    return bundle;
                }
            }
            return null;
        }
    }
}
