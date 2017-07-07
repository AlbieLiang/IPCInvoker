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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by albieliang on 2017/7/6.
 */

public class ObjectTypeTransfer {

    private static List<BaseTypeTransfer> sTransferList = new LinkedList<>();
    private static Map<String, BaseTypeTransfer> sTransferMap = new HashMap<>();

    public static BaseTypeTransfer getTypeTransfer(String transferClass) {
        return sTransferMap.get(transferClass);
    }

    public static BaseTypeTransfer getTypeTransfer(Object o) {
        for (BaseTypeTransfer transfer : sTransferList) {
            if (transfer.canTransfer(o)) {
                return transfer;
            }
        }
        return null;
    }

    public static void writeToParcel(Object o, Parcel dest) {
        BaseTypeTransfer transfer = getTypeTransfer(o);
        if (transfer != null) {
            transfer.writeToParcel(o, dest);
        }
    }

    public static Object readFromParcel(String transferClass, Parcel dest) {
        BaseTypeTransfer transfer = getTypeTransfer(transferClass);
        if (transfer != null) {
            return transfer.readFromParcel(dest);
        }
        return null;
    }

    public static void addTypeTransfer(BaseTypeTransfer transfer) {
        if (transfer == null || sTransferList.contains(transfer)) {
            return;
        }
        sTransferMap.put(transfer.getClass().getName(), transfer);
        sTransferList.add(transfer);
    }
}
