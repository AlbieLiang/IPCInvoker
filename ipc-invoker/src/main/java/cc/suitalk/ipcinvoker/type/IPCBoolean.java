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

public class IPCBoolean implements Parcelable {

    public boolean value;

    public IPCBoolean() {

    }

    public IPCBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(value ? 1 : 0);
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this || value == obj) {
            return true;
        }
        if (obj instanceof IPCBoolean) {
            IPCBoolean o = (IPCBoolean) obj;
            return value == o.value;
        }
        if (obj instanceof Boolean) {
            return value == obj || obj != null && obj.equals(value);
        }
        return false;
    }

    public static final Creator<IPCBoolean> CREATOR = new Creator<IPCBoolean>() {
        @Override
        public IPCBoolean createFromParcel(Parcel in) {
            IPCBoolean o = new IPCBoolean();
            o.value = in.readInt() == 1;
            return o;
        }

        @Override
        public IPCBoolean[] newArray(int size) {
            return new IPCBoolean[size];
        }
    };
}
