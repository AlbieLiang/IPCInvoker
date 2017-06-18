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

/**
 * Created by albieliang on 2017/5/14.
 */

public class IPCInvokeSample_InvokeByTypeWithData {

    public static IPCSampleData invokeIPCLogic(String id, int debugType, int pkgVersion) {
        IPCRemoteInvoke_PrintWithData o = new IPCRemoteInvoke_PrintWithData();
        o.id = id;
        o.debugType = debugType;
        o.version = pkgVersion;
        return IPCInvoker.invokeSync(MainProcessIPCService.PROCESS_NAME, o, IPCRemoteInvoke_PrintWithData.class);
    }


    private static class IPCRemoteInvoke_PrintWithData implements IPCRemoteSyncInvoke<IPCRemoteInvoke_PrintWithData, IPCSampleData>, Parcelable {

        private String id;
        private int debugType;
        private int version;

        @Override
        public IPCSampleData invoke(IPCRemoteInvoke_PrintWithData data) {
            IPCSampleData result = new IPCSampleData();
            result.result = String.format("id:%s|type:%s|version:%s", data.id, data.debugType, data.version);
            return result;
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

        public static final Creator<IPCRemoteInvoke_PrintWithData> CREATOR = new Creator<IPCRemoteInvoke_PrintWithData>() {
            @Override
            public IPCRemoteInvoke_PrintWithData createFromParcel(Parcel in) {
                IPCRemoteInvoke_PrintWithData o = new IPCRemoteInvoke_PrintWithData();
                o.id = in.readString();
                o.debugType = in.readInt();
                o.version = in.readInt();
                return o;
            }

            @Override
            public IPCRemoteInvoke_PrintWithData[] newArray(int size) {
                return new IPCRemoteInvoke_PrintWithData[size];
            }
        };

    }
}
