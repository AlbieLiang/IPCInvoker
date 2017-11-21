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

package cc.suitalk.ipcinvoker.sample.sync;

import android.os.Parcel;
import android.os.Parcelable;

import cc.suitalk.ipcinvoker.IPCInvoker;
import cc.suitalk.ipcinvoker.IPCRemoteSyncInvoke;
import cc.suitalk.ipcinvoker.sample.IPCSampleData;
import cc.suitalk.ipcinvoker.sample.service.MainProcessIPCService;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/5/14.
 */

public class IPCInvokeSample_InvokeByTypeWithData {

    private static final String TAG = "IPCInvokerSample.IPCInvokeSample_InvokeByTypeWithData";

    public static void invokeSync() {
        IPCRemoteInvoke_BuildStringWithData o = new IPCRemoteInvoke_BuildStringWithData();
        o.name = "AlbieLiang";
        o.pid = android.os.Process.myPid();
        o.timestamp = System.currentTimeMillis();
        IPCSampleData result = IPCInvoker.invokeSync(MainProcessIPCService.PROCESS_NAME, o, IPCRemoteInvoke_BuildStringWithData.class);
        Log.i(TAG, "invoke result : %s", result);
    }

    private static class IPCRemoteInvoke_BuildStringWithData implements IPCRemoteSyncInvoke<IPCRemoteInvoke_BuildStringWithData, IPCSampleData>, Parcelable {

        private String name;
        private int pid;
        private long timestamp;

        @Override
        public IPCSampleData invoke(IPCRemoteInvoke_BuildStringWithData data) {
            IPCSampleData result = new IPCSampleData();
            result.result = String.format("name:%s|pid:%s|timestamp:%s", data.name, data.pid, data.timestamp);
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeInt(pid);
            dest.writeLong(timestamp);
        }

        public static final Creator<IPCRemoteInvoke_BuildStringWithData> CREATOR = new Creator<IPCRemoteInvoke_BuildStringWithData>() {
            @Override
            public IPCRemoteInvoke_BuildStringWithData createFromParcel(Parcel in) {
                IPCRemoteInvoke_BuildStringWithData o = new IPCRemoteInvoke_BuildStringWithData();
                o.name = in.readString();
                o.pid = in.readInt();
                o.timestamp = in.readLong();
                return o;
            }

            @Override
            public IPCRemoteInvoke_BuildStringWithData[] newArray(int size) {
                return new IPCRemoteInvoke_BuildStringWithData[size];
            }
        };

    }
}
