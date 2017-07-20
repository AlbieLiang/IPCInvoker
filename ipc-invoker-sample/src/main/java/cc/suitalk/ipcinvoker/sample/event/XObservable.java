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

package cc.suitalk.ipcinvoker.sample.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.extension.event.XIPCDispatcher;
import cc.suitalk.ipcinvoker.extension.event.XIPCObservable;

/**
 * Created by albieliang on 2017/6/18.
 */

public class XObservable {

    private static Map<String, XIPCObservable> sMap = new ConcurrentHashMap<>();

    public static <T extends XIPCDispatcher> XIPCObservable get(@NonNull String process, @NonNull Class<T> clazz) {
        String key = genKey(process, clazz);
        XIPCObservable observable = sMap.get(key);
        if (observable == null) {
            observable = new XIPCObservable(process, clazz);
            sMap.put(key, observable);
        }
        return observable;
    }

    private static String genKey(@NonNull String process, @NonNull Class<?> clazz) {
        return String.format("%s#%s", process, clazz.getName());
    }
}
