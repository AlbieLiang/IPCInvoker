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

package cc.suitalk.ipcinvoker.recycle;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2018/3/15.
 */

public class ObjectRecycler {

    private static final String TAG = "IPC.ObjectRecycler";

    private static final Set<Object> sObjectSet = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, Set<Recyclable>> sMap = new HashMap<>();

    public static void keepRef(Object o) {
        sObjectSet.add(o);
    }

    public static void releaseRef(Object o) {
        sObjectSet.remove(o);
    }

    public static boolean addIntoSet(String setId, Recyclable o) {
        if (setId == null || setId.length() == 0 || o == null) {
            return false;
        }
        Set<Recyclable> set;
        synchronized (sMap) {
            set = sMap.get(setId);
            if (set == null) {
                set = new HashSet<>();
                sMap.put(setId, set);
            }
        }
        Log.d(TAG, "addIntoSet(%s)", setId);
        synchronized (set) {
            return set.add(o);
        }
    }

    public static boolean removeFromSet(String setId, Recyclable o) {
        if (setId == null || setId.length() == 0 || o == null) {
            return false;
        }
        Set<Recyclable> set;
        synchronized (sMap) {
            set = sMap.get(setId);
        }
        if (set == null) {
            return false;
        }
        Log.d(TAG, "removeFromSet(%s)", setId);
        synchronized (set) {
            return set.remove(o);
        }
    }

    public static void recycleAll(String setId) {
        if (setId == null || setId.length() == 0) {
            return;
        }
        Set<Recyclable> set;
        synchronized (sMap) {
            set = sMap.remove(setId);
        }
        if (set == null) {
            return;
        }
        Log.d(TAG, "recycleAll(%s)", setId);
        synchronized (set) {
            for (Recyclable o : set) {
                Log.d(TAG, "recycle(%s)", o.hashCode());
                o.recycle();
            }
            set.clear();
        }
    }
}
