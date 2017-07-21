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

public class IPCDouble implements Parcelable {

    public double value;

    public IPCDouble() {

    }

    public IPCDouble(double value) {
        this.value = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(value);
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof IPCDouble) {
            IPCDouble o = (IPCDouble) obj;
            return value == o.value;
        }
        if (obj instanceof Double) {
            return obj.equals(value);
        }
        return false;
    }

    public static final Creator<IPCDouble> CREATOR = new Creator<IPCDouble>() {
        @Override
        public IPCDouble createFromParcel(Parcel in) {
            IPCDouble o = new IPCDouble();
            o.value = in.readDouble();
            return o;
        }

        @Override
        public IPCDouble[] newArray(int size) {
            return new IPCDouble[size];
        }
    };
}
