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

package cc.suitalk.ipcinvoker.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/5/20.
 */

public class ReflectUtil {

    private static final String TAG = "IPC.ReflectUtil";

    public static <T> T newInstance(String clazz, Class<?> parentClass) {
        if (clazz == null || clazz.length() == 0) {
            Log.e(TAG, "newInstance failed, class is null or nil.");
            return null;
        }
        if (parentClass == null) {
            Log.e(TAG, "newInstance failed, parent class is null.");
            return null;
        }
        try {
            Class<?> c = Class.forName(clazz);
            if (!parentClass.isAssignableFrom(c)) {
                Log.e(TAG, "%s isAssignableFrom %s return false", parentClass, c);
                return null;
            }
            Constructor constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "reflect error : %s", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "reflect error : %s", e);
        } catch (InstantiationException e) {
            Log.e(TAG, "reflect error : %s", e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "reflect error : %s", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "reflect error : %s", e);
        }
        return null;
    }

    public static <T> T newInstance(Class<?> clazz, Class<?> parentClass) {
        if (clazz == null) {
            Log.e(TAG, "newInstance failed, class is null or nil.");
            return null;
        }
        if (parentClass == null) {
            Log.e(TAG, "newInstance failed, parent class is null.");
            return null;
        }
        try {
            Class<?> c = clazz;
            if (!parentClass.isAssignableFrom(c)) {
                Log.e(TAG, "%s isAssignableFrom %s return false", parentClass, c);
                return null;
            }
            Constructor constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "reflect error : %s", e);
        } catch (InstantiationException e) {
            Log.e(TAG, "reflect error : %s", e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "reflect error : %s", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "reflect error : %s", e);
        }
        return null;
    }

    public static <T> T newInstance(Class<?> clazz) {
        if (clazz == null) {
            Log.e(TAG, "newInstance failed, class is null or nil.");
            return null;
        }
        try {
            Class<?> c = clazz;
            Constructor constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "reflect error : %s", e);
        } catch (InstantiationException e) {
            Log.e(TAG, "reflect error : %s", e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "reflect error : %s", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "reflect error : %s", e);
        }
        return null;
    }
}
