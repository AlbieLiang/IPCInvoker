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
import cc.suitalk.ipcinvoker.IPCSyncInvokeTask;
import cc.suitalk.ipcinvoker.ObjectStore;
import cc.suitalk.ipcinvoker.annotation.Singleton;
import cc.suitalk.ipcinvoker.extension.ParameterHelper;
import cc.suitalk.ipcinvoker.extension.annotation.IPCAsyncInvokeMethod;
import cc.suitalk.ipcinvoker.extension.annotation.IPCSyncInvokeMethod;

import cc.suitalk.ipcinvoker.sample.extension.CustomIPCTask;
import cc.suitalk.ipcinvoker.sample.extension.CustomIPCTask.InputData;

import cc.suitalk.ipcinvoker.sample.extension.CustomIPCTask.ResultData;

import cc.suitalk.ipcinvoker.tools.Log;

/**
 *
 * Created by ArbitraryGen on 2017-12-20 00:11:53.
 *
 */
public class CustomIPCTask$AG {

    private static final String TAG = "AG.CustomIPCTask$AG";

    public static final String PROCESS = "cc.suitalk.ipcinvoker.sample";

    public static final Class<?> TARGET_CLASS = CustomIPCTask.class;

    private static final String KEY_INVOKE_METHOD = "__invoke_method";


    private static final int SYNC_INVOKE_METHOD_getName = 0;

    private static final int ASYNC_INVOKE_METHOD_showLoading = 1;

    private static final int ASYNC_INVOKE_METHOD_hideLoading = 2;



    @IPCSyncInvokeMethod
    public static String getName(final int id) {
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

    @IPCAsyncInvokeMethod
    public static void showLoading(final int id, final String name, final InputData data, final IPCInvokeCallback<ResultData> callback) {
        Log.d(TAG, "showLoading(id : %s, name : %s, data : %s, callback : %s)", id, name, data, callback);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_INVOKE_METHOD, ASYNC_INVOKE_METHOD_showLoading);
        // Parameter

        ParameterHelper.put(bundle, "id", id);
        ParameterHelper.put(bundle, "name", name);
        ParameterHelper.put(bundle, "data", data);
        // Callback
        IPCInvokeCallback<Bundle> __callback = null;

        if (callback != null) {
            __callback = new IPCInvokeCallback<Bundle>() {

                @Override
                public void onCallback(Bundle data) {
                    if (callback != null) {
                        ResultData result = ParameterHelper.get(data, "__result");
                        callback.onCallback(result);
                    }
                }
            };
        }

        IPCInvoker.invokeAsync(PROCESS, bundle, IPCAsyncInvokeTaskImpl.class, __callback);

    }

    @IPCAsyncInvokeMethod
    public static void hideLoading(final int id) {
        Log.d(TAG, "hideLoading(id : %s)", id);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_INVOKE_METHOD, ASYNC_INVOKE_METHOD_hideLoading);
        // Parameter

        ParameterHelper.put(bundle, "id", id);
        // Callback
        IPCInvokeCallback __callback = null;

        IPCInvoker.invokeAsync(PROCESS, bundle, IPCAsyncInvokeTaskImpl.class, __callback);

    }


    // Get singleton target object
    private static CustomIPCTask getTarget() {
        CustomIPCTask task = ObjectStore.get(TARGET_CLASS);
        if (task == null) {
            synchronized (CustomIPCTask$AG.class) {
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
    private static final class IPCAsyncInvokeTaskImpl implements IPCAsyncInvokeTask<Bundle, Bundle> {

        @Override
        public void invoke(Bundle __data, final IPCInvokeCallback<Bundle> __callback) {
            int invokeMethod = __data.getInt(KEY_INVOKE_METHOD);
            switch (invokeMethod) {

                case ASYNC_INVOKE_METHOD_showLoading: {
                    // Get parameters

                    final int id = ParameterHelper.get(__data, "id");
                    final String name = ParameterHelper.get(__data, "name");
                    final InputData data = ParameterHelper.get(__data, "data");
                    // Obtain target task
                    final CustomIPCTask __task = getTarget();

                    // Create callback proxy
                    IPCInvokeCallback __callbackProxy = null;
                    if (__callback != null) {
                        __callbackProxy = new IPCInvokeCallback() {
                            @Override
                            public void onCallback(Object data) {
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
    private static final class IPCSyncInvokeTaskImpl implements IPCSyncInvokeTask<Bundle, Bundle> {

        @Override
        public Bundle invoke(Bundle __data) {
            int invokeMethod = __data.getInt(KEY_INVOKE_METHOD);
            switch (invokeMethod) {

                case SYNC_INVOKE_METHOD_getName: {
                    // Get parameters

                    final int id = ParameterHelper.get(__data, "id");
                    // Obtain target task
                    final CustomIPCTask __task = getTarget();
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
