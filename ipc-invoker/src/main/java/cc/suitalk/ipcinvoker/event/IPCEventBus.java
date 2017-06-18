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

import android.os.Bundle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by albieliang on 2017/6/18.
 */

public class IPCEventBus {

    private static IPCEventBus sImpl;

    Map<String, List<IPCObserver>> mMap;

    public static IPCEventBus getImpl() {
        if (sImpl == null) {
            synchronized (IPCEventBus.class) {
                if (sImpl == null) {
                    sImpl = new IPCEventBus();
                }
            }
        }
        return sImpl;
    }

    private IPCEventBus() {
        mMap = new ConcurrentHashMap<>();
    }

    public boolean dispatch(String event, IPCData data) {
        if (event == null || event.length() == 0 || data == null) {
            return false;
        }
        List<IPCObserver> list = mMap.get(event);
        if (list == null || list.isEmpty()) {
            return true;
        }
        List<IPCObserver> tempList;
        synchronized (list) {
            tempList = new ArrayList<>(list);
        }
        Bundle bundle = data.toBundle();
        for (IPCObserver observer : tempList) {
            // TODO: 2017/6/19 albieliang
            observer.onCallback(bundle);
        }
        return true;
    }

    public boolean registerIPCObserver(String event, IPCObserver o) {
        if (event == null || event.length() == 0 || o == null) {
            return false;
        }
        List<IPCObserver> list = mMap.get(event);
        if (list == null) {
            list = new LinkedList<>();
            mMap.put(event, list);
        }
        if (list.contains(o)) {
            return false;
        }
        boolean r;
        synchronized (list) {
            r = list.add(o);
        }
        return r;
    }

    public boolean unregisterIPCObserver(String event, IPCObserver o) {
        if (event == null || event.length() == 0 || o == null) {
            return false;
        }
        boolean r = false;
        List<IPCObserver> list = mMap.get(event);
        if (list != null) {
            synchronized (list) {
                r = list.remove(o);
            }
            if (list.isEmpty()) {
                mMap.remove(event);
            }
        }
        return r;
    }
}
