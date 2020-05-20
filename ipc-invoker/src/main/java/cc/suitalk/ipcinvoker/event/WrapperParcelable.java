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

package cc.suitalk.ipcinvoker.event;

import android.os.Parcel;
import android.os.Parcelable;

import cc.suitalk.ipcinvoker.extension.BaseTypeTransfer;
import cc.suitalk.ipcinvoker.extension.ObjectTypeTransfer;

/**
 * Created by albieliang on 2017/7/20.
 */

class WrapperParcelable implements Parcelable {

    private static final int NO_DATA = 0;
    private static final int HAS_DATA = 1;

    Object target;

    private WrapperParcelable() {

    }

    WrapperParcelable(Object o) {
        this.target = o;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (target != null) {
            BaseTypeTransfer transfer = ObjectTypeTransfer.getTypeTransfer(target);
            if (transfer != null) {
                dest.writeInt(HAS_DATA);
                dest.writeString(transfer.getClass().getName());
                transfer.writeToParcel(target, dest);
                return;
            }
        }
        dest.writeInt(HAS_DATA);
    }

    void readFromParcel(Parcel in) {
        int hasData = in.readInt();
        if (hasData == HAS_DATA) {
            String transferClass = in.readString();
            target = ObjectTypeTransfer.readFromParcel(transferClass, in);
        }
    }

    Object getTarget() {
        return target;
    }

    public static final Creator<WrapperParcelable> CREATOR = new Creator<WrapperParcelable>() {
        @Override
        public WrapperParcelable createFromParcel(Parcel in) {
            WrapperParcelable o = new WrapperParcelable();
            o.readFromParcel(in);
            return o;
        }

        @Override
        public WrapperParcelable[] newArray(int size) {
            return new WrapperParcelable[size];
        }
    };
}