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

package cc.suitalk.ipcinvoker.sample.async;

import android.os.Parcel;
import android.os.Parcelable;

import cc.suitalk.ipcinvoker.IPCInvoker;
import cc.suitalk.ipcinvoker.IPCRemoteAsyncInvoke;
import cc.suitalk.ipcinvoker.IPCRemoteInvokeCallback;
import cc.suitalk.ipcinvoker.sample.IPCData;
import cc.suitalk.ipcinvoker.sample.service.MainProcessIPCService;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/5/30.
 */

public class IPCInvokeSample_InvokeByTypeWithData {

    private static final String TAG = "IPCInvokerSample.IPCInvokeSample_InvokeByTypeWithData";

    public static void invokeIPCLogic(String id, int debugType, int pkgVersion, final IPCRemoteInvokeCallback<IPCData> callback) {
        IPCRemoteInvoke_PrintWithData o = new IPCRemoteInvoke_PrintWithData();
        o.id = id;
        o.debugType = debugType;
        o.version = pkgVersion;
        IPCInvoker.invokeAsync(MainProcessIPCService.PROCESS_NAME, o, IPCRemoteInvoke_PrintWithData.class, new IPCRemoteInvokeCallback<IPCData>() {
            @Override
            public void onCallback(IPCData data) {
                Log.i(TAG, "onCallback : %s", data.result);
                if (callback != null) {
                    callback.onCallback(data);
                }
            }
        });
    }


    private static class IPCRemoteInvoke_PrintWithData implements IPCRemoteAsyncInvoke<IPCRemoteInvoke_PrintWithData, IPCData>, Parcelable {

        private String id;
        private int debugType;
        private int version;

        @Override
        public void invoke(IPCRemoteInvoke_PrintWithData data, IPCRemoteInvokeCallback<IPCData> callback) {
            IPCData result = new IPCData();
            result.result = String.format("id:%s|type:%s|version:%s", data.id, data.debugType, data.version);
            callback.onCallback(result);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeInt(debugType);
            dest.writeInt(version);
        }

        public static final Creator<IPCRemoteInvoke_PrintWithData> CREATOR = new Creator<IPCInvokeSample_InvokeByTypeWithData.IPCRemoteInvoke_PrintWithData>() {
            @Override
            public IPCInvokeSample_InvokeByTypeWithData.IPCRemoteInvoke_PrintWithData createFromParcel(Parcel in) {
                IPCInvokeSample_InvokeByTypeWithData.IPCRemoteInvoke_PrintWithData o = new IPCInvokeSample_InvokeByTypeWithData.IPCRemoteInvoke_PrintWithData();
                o.id = in.readString();
                o.debugType = in.readInt();
                o.version = in.readInt();
                return o;
            }

            @Override
            public IPCInvokeSample_InvokeByTypeWithData.IPCRemoteInvoke_PrintWithData[] newArray(int size) {
                return new IPCInvokeSample_InvokeByTypeWithData.IPCRemoteInvoke_PrintWithData[size];
            }
        };
    }
}
