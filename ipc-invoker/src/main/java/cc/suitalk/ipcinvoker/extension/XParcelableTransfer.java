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

import cc.suitalk.ipcinvoker.reflect.ReflectUtil;

/**
 * Created by albieliang on 2017/7/9.
 */

public class XParcelableTransfer implements BaseTypeTransfer {

    @Override
    public boolean canTransfer(Object o) {
        return o instanceof XParcelable;
    }

    @Override
    public void writeToParcel(Object o, Parcel dest) {
        XParcelable parcelable = (XParcelable) o;
        dest.writeString(parcelable.getClass().getName());
        parcelable.writeToParcel(dest);
    }

    @Override
    public Object readFromParcel(Parcel in) {
        String dataClass = in.readString();
        XParcelable parcelable = ReflectUtil.newInstance(dataClass, XParcelable.class);
        parcelable.readFromParcel(in);
        return parcelable;
    }
}
