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

package cc.suitalk.ipcinvoker.tools;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by albieliang on 2020/6/4.
 *
 * @param <K>
 * @param <V>
 */
public class SafeConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

    @Override
    public V get(Object key) {
        return key == null ? null : super.get(key);
    }

    @Override
    public V put(K key, V value) {
        if (key == null) {
            return null;
        }
        if (value == null) {
            return super.remove(key);
        }
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return key == null ? null : super.remove(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return value == null ? false : super.containsValue(value);
    }
}