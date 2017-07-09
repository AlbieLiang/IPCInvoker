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

package cc.suitalk.ipcinvoker.extension;

import android.os.Parcel;
import android.os.Parcelable;

import cc.suitalk.ipcinvoker.ObjectStore;

/**
 * Created by albieliang on 2017/7/1.
 */

public class XParcelableWrapper implements Parcelable {

    private static final int NO_DATA = 0;
    private static final int HAS_DATA = 1;

    public XParcelable target;

    private XParcelableWrapper() {

    }

    public XParcelableWrapper(XParcelable parcelable) {
        this.target = parcelable;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (target != null) {
            dest.writeInt(HAS_DATA);
            dest.writeString(target.getClass().getName());
            target.writeToParcel(dest);
        } else {
            dest.writeInt(HAS_DATA);
        }
    }

    void readFromParcel(Parcel in) {
        int hasData = in.readInt();
        if (hasData == HAS_DATA) {
            String dataClass = in.readString();
            if (target == null) {
                target = ObjectStore.newInstance(dataClass, XParcelable.class);
            }
            target.readFromParcel(in);
        }
    }

    XParcelable getTarget() {
        return target;
    }

    public static final Creator<XParcelableWrapper> CREATOR = new Creator<XParcelableWrapper>() {
        @Override
        public XParcelableWrapper createFromParcel(Parcel in) {
            XParcelableWrapper o = new XParcelableWrapper();
            o.readFromParcel(in);
            return o;
        }

        @Override
        public XParcelableWrapper[] newArray(int size) {
            return new XParcelableWrapper[size];
        }
    };
}
