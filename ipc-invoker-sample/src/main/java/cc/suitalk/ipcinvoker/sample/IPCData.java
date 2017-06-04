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

package cc.suitalk.ipcinvoker.sample;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by albieliang on 2017/5/14.
 */

public class IPCData implements Parcelable {

    public String result;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(result);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IPCData> CREATOR = new Creator<IPCData>() {
        @Override
        public IPCData createFromParcel(Parcel in) {
            IPCData o = new IPCData();
            o.result = in.readString();
            return o;
        }

        @Override
        public IPCData[] newArray(int size) {
            return new IPCData[size];
        }
    };
}
