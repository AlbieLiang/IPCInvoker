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

package cc.suitalk.ipcinvoker.sample.app.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by albieliang on 2017/6/3.
 */

public class DataCenter {

    private Map<String, Object> mMap;

    private static DataCenter sImpl;

    public static DataCenter getImpl() {
        if (sImpl == null) {
            synchronized (DataCenter.class) {
                if (sImpl == null) {
                    sImpl = new DataCenter();
                }
            }
        }
        return sImpl;
    }

    private DataCenter() {
        mMap = new HashMap<>();
    }

    public Map<String, Object> getMap() {
        return mMap;
    }

    public String getString(String key) {
        if (key == null || key.length() == 0) {
            return null;
        }
        Object o = mMap.get(key);
        if (o instanceof String) {
            return (String) o;
        }
        return null;
    }

    public void putString(String key, String value) {
        if (key == null || key.length() == 0) {
            return;
        }
        mMap.put(key, value);
    }
}
