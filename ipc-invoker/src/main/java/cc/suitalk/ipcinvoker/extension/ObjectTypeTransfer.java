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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import cc.suitalk.ipcinvoker.tools.SafeConcurrentHashMap;

/**
 * Created by albieliang on 2017/7/6.
 */

public class ObjectTypeTransfer {

    private static final List<BaseTypeTransfer> sTransferList = new CopyOnWriteArrayList<>();
    private static final Map<String, BaseTypeTransfer> sTransferMap = new SafeConcurrentHashMap<>();

    public static BaseTypeTransfer getTypeTransfer(String transferClass) {
        return sTransferMap.get(transferClass);
    }

    public static BaseTypeTransfer getTypeTransfer(Class<?> transferClass) {
        return sTransferMap.get(transferClass.getName());
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

    public static Object readFromParcel(String transferClass, Parcel in) {
        BaseTypeTransfer transfer = getTypeTransfer(transferClass);
        if (transfer != null) {
            return transfer.readFromParcel(in);
        }
        return null;
    }

    public static synchronized void addTypeTransfer(BaseTypeTransfer transfer) {
        if (transfer == null || sTransferList.contains(transfer)) {
            return;
        }
        sTransferMap.put(transfer.getClass().getName(), transfer);
        sTransferList.add(transfer);
    }

    public static synchronized void addTypeTransfer(BaseTypeTransfer... transfers) {
        if (transfers == null || transfers.length == 0) {
            return;
        }
        final List<BaseTypeTransfer> temp = new ArrayList<>(transfers.length);
        for (BaseTypeTransfer t : transfers) {
            if (t == null || sTransferList.contains(t)) {
                continue;
            }
            sTransferMap.put(t.getClass().getName(), t);
            temp.add(t);
        }
        sTransferList.addAll(temp);
    }
}
