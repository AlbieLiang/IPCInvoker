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

import java.lang.reflect.Field;

import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Reflect a static field of the given class.
 * 
 * @author AlbieLiang
 *
 * @param <FieldType>
 */
public class ReflectStaticFieldSmith<FieldType> {
	
	private static final String TAG = "SDK.ReflectStaticFieldSmith";
	private Class<?> mClazz;
	private String mFieldName;

	private boolean mInited;
	private Field mField;

	public ReflectStaticFieldSmith(Class<?> clazz, String fieldName) {
		if (clazz == null || fieldName == null || fieldName.length() == 0) {
			throw new IllegalArgumentException("Both of invoker and fieldName can not be null or nil.");
		}
		this.mClazz = clazz;
		this.mFieldName = fieldName;
	}

	private synchronized void prepare() {
		if (mInited) {
			return;
		}
		Class<?> clazz = mClazz;
		while (clazz != null) {
			try {
				Field f = clazz.getDeclaredField(mFieldName);
				f.setAccessible(true);
				mField = f;
				break;
			} catch (Exception e) {
			}
			clazz = clazz.getSuperclass();
		}
		mInited = true;
	}

	public synchronized FieldType get() throws NoSuchFieldException, IllegalAccessException,
			IllegalArgumentException {
		return get(false);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized FieldType get(boolean ignoreFieldNoExist)
			throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException {
		prepare();
		if (mField == null) {
			if (!ignoreFieldNoExist) {
				throw new NoSuchFieldException();
			}
			Log.w(TAG, "Field %s is no exists.", mFieldName);
			return null;
		}
		FieldType fieldVal = null;
		try {
			fieldVal = (FieldType) mField.get(null);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("unable to cast object");
		}
		return fieldVal;
	}

	public synchronized FieldType getWithoutThrow() {
		FieldType fieldVal = null;
		try {
			fieldVal = get(true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return fieldVal;
	}
	
	public synchronized void set(FieldType val) throws NoSuchFieldException, IllegalAccessException,
			IllegalArgumentException {
		set(val, false);
	}
	
	public synchronized boolean set(FieldType val, boolean ignoreFieldNoExist)
			throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException {
		prepare();
		if (mField == null) {
			if (!ignoreFieldNoExist) {
				throw new NoSuchFieldException("Method " + mFieldName + " is not exists.");
			}
			Log.w(TAG, "Field %s is no exists.", mFieldName);
			return false;
		}
		mField.set(null, val);
		return true;
	}

	public synchronized boolean setWithoutThrow(FieldType val) {
		boolean result = false;
		try {
			result = set(val, true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return result;
	}
	
}
