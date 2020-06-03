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

import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.event.IPCDispatcher;
import cc.suitalk.ipcinvoker.event.IPCObservable;
import cc.suitalk.ipcinvoker.tools.SafeConcurrentHashMap;

/**
 * Created by albieliang on 2017/6/18.
 */

public class MObservable {

    private static final Map<String, IPCObservable<?>> sMap = new SafeConcurrentHashMap<>();

    public static <T extends IPCDispatcher<?>> IPCObservable get(@NonNull String process, @NonNull Class<T> clazz) {
        String key = genKey(process, clazz);
        IPCObservable<?> observable = sMap.get(key);
        if (observable == null) {
            observable = new IPCObservable(process, clazz);
            sMap.put(key, observable);
        }
        return observable;
    }

    private static String genKey(@NonNull String process, @NonNull Class<?> clazz) {
        return String.format("%s#%s", process, clazz.getName());
    }
}
