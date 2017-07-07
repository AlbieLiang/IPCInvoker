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

package cc.suitalk.ipcinvoker.type;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by albieliang on 2017/6/26.
 */

public class IPCByte implements Parcelable {

    public byte value;

    public IPCByte() {

    }

    public IPCByte(byte value) {
        this.value = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(value);
    }

    public static final Creator<IPCByte> CREATOR = new Creator<IPCByte>() {
        @Override
        public IPCByte createFromParcel(Parcel in) {
            IPCByte o = new IPCByte();
            o.value = in.readByte();
            return o;
        }

        @Override
        public IPCByte[] newArray(int size) {
            return new IPCByte[size];
        }
    };
}
