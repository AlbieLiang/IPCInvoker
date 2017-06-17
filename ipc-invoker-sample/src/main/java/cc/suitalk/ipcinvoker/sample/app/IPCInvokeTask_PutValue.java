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

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;

import cc.suitalk.ipcinvoker.IPCInvokeLogic;
import cc.suitalk.ipcinvoker.IPCRemoteAsyncInvoke;
import cc.suitalk.ipcinvoker.IPCRemoteInvokeCallback;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/6/3.
 */

public class IPCInvokeTask_PutValue implements Parcelable, IPCRemoteAsyncInvoke<IPCInvokeTask_PutValue, Bundle> {

    private static final String TAG = "IPCInvokerSample.IPCInvokeTask_PutValue";

    public static final String PID = "pid";
    public static final String PROCESS_NAME = "processName";
    public static final String RESULT_SIZE = "size";
    public static final String RESULT = "result";

    String key;
    String value;

    @Override
    public void invoke(IPCInvokeTask_PutValue data, IPCRemoteInvokeCallback<Bundle> callback) {
        String key = data.key;
        String value = data.value;
        DataCenter.getImpl().putString(key, value);
        Bundle result = new Bundle();
        result.putInt(PID, android.os.Process.myPid());
        result.putString(PROCESS_NAME, IPCInvokeLogic.getCurrentProcessName());
        result.putInt(RESULT_SIZE, DataCenter.getImpl().getMap().size());

        result.putString(RESULT, String.format("pid : %s\nprocess : %s\nresultSize : %s",
                result.getInt(PID), result.getString(PROCESS_NAME), result.getInt(RESULT_SIZE)));

        Log.i(TAG, "putValue, result : %s", result);
        Looper looper = Looper.myLooper();
        (new Handler(looper)).post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "test post message to Looper(%s).", Looper.myLooper().hashCode());
            }
        });
        callback.onCallback(result);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(value);
    }

    public static final Creator<IPCInvokeTask_PutValue> CREATOR = new Creator<IPCInvokeTask_PutValue>() {
        @Override
        public IPCInvokeTask_PutValue createFromParcel(Parcel in) {
            IPCInvokeTask_PutValue o = new IPCInvokeTask_PutValue();
            o.key = in.readString();
            o.value = in.readString();
            return o;
        }

        @Override
        public IPCInvokeTask_PutValue[] newArray(int size) {
            return new IPCInvokeTask_PutValue[size];
        }
    };

}