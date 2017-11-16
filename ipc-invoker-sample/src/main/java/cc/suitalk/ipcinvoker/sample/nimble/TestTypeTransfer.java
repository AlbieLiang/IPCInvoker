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

package cc.suitalk.ipcinvoker.sample.nimble;

import android.os.Parcel;

import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.extension.BaseTypeTransfer;

/**
 * Created by albieliang on 2017/7/6.
 */

public class TestTypeTransfer implements BaseTypeTransfer {
    @Override
    public boolean canTransfer(Object o) {
        return o instanceof TestType;
    }

    @Override
    public void writeToParcel(@NonNull Object o, Parcel dest) {
        TestType testTypeObj = (TestType) o;
        dest.writeString(testTypeObj.key);
        dest.writeString(testTypeObj.value);
    }

    @Override
    public Object readFromParcel(Parcel in) {
        TestType testTypeObj = new TestType();
        testTypeObj.key = in.readString();
        testTypeObj.value = in.readString();
        return testTypeObj;
    }
}
