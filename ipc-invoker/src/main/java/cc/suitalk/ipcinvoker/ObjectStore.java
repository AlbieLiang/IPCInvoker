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

package cc.suitalk.ipcinvoker;

import android.support.annotation.RestrictTo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.extension.Singleton;
import cc.suitalk.ipcinvoker.reflect.ReflectUtil;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/7/9.
 */

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ObjectStore {

    private static final String TAG = "IPC.ObjectStore";

    private static final Map<String, Singleton> sMap = new ConcurrentHashMap<>();

    public static <T> T get(@NonNull String clazz, @NonNull Class<?> parentClass) {
        try {
            Class<?> c = Class.forName(clazz);
            if (!parentClass.isAssignableFrom(c)) {
                Log.e(TAG, "%s isAssignableFrom %s return false", parentClass, c);
                return null;
            }
            if (c.isAnnotationPresent(cc.suitalk.ipcinvoker.annotation.Singleton.class)) {
                Singleton o = sMap.get(clazz);
                if (o == null) {
                    o = new Singleton(c);
                    sMap.put(clazz, o);
                }
                return (T) o.get();
            }
            return ReflectUtil.newInstance(clazz, parentClass);
        } catch (Exception e) {
        }
        return null;
    }

    public static <T> T get(@NonNull Class<?> clazz, @NonNull Class<?> parentClass) {
        try {
            if (!parentClass.isAssignableFrom(clazz)) {
                Log.e(TAG, "%s isAssignableFrom %s return false", parentClass, clazz);
                return null;
            }
            if (clazz.isAnnotationPresent(cc.suitalk.ipcinvoker.annotation.Singleton.class)) {
                String className = clazz.getName();
                Singleton o = sMap.get(className);
                if (o == null) {
                    o = new Singleton(clazz);
                    sMap.put(className, o);
                }
                return (T) o.get();
            }
            return ReflectUtil.newInstance(clazz, parentClass);
        } catch (Exception e) {
        }
        return null;
    }

    public static void put(@NonNull Object o) {
        if (o == null) {
            return;
        }
        Class<?> clazz = o.getClass();
        if (!clazz.isAnnotationPresent(cc.suitalk.ipcinvoker.annotation.Singleton.class)) {
            Log.w(TAG, "put failed, the class(%s).isAnnotationPresent(Singleton.class) return false", clazz);
            return;
        }
        sMap.put(clazz.getName(), new Singleton(o));
    }

    public static <T> T get(@NonNull Class<?> clazz) {
        try {
            if (clazz.isAnnotationPresent(cc.suitalk.ipcinvoker.annotation.Singleton.class)) {
                String className = clazz.getName();
                Singleton o = sMap.get(className);
                if (o == null) {
                    o = new Singleton(clazz);
                    sMap.put(className, o);
                }
                return (T) o.get();
            }
            return ReflectUtil.newInstance(clazz);
        } catch (Exception e) {
        }
        return null;
    }

    public static <T> T newInstance(String clazz, Class<?> parentClass) {
        return ReflectUtil.newInstance(clazz, parentClass);
    }

    public static <T> T newInstance(Class<?> clazz, Class<?> parentClass) {
        return ReflectUtil.newInstance(clazz, parentClass);
    }
}
