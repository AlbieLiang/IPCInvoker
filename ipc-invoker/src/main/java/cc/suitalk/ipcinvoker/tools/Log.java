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

import cc.suitalk.ipcinvoker.tools.log.ILogPrinter;

/**
 * A Log tools. Invoked {@link #setLogPrinter(ILogPrinter)} to set a
 * {@link ILogPrinter} to do a real Log action.
 * 
 * @author AlbieLiang
 * 
 */
public class Log {

    public static final int VERBOSE = android.util.Log.VERBOSE;
    public static final int DEBUG = android.util.Log.DEBUG;
    public static final int INFO = android.util.Log.INFO;
    public static final int WARN = android.util.Log.WARN;
    public static final int ERROR = android.util.Log.ERROR;
    public static final int ASSERT = android.util.Log.ASSERT;
	
	private static ILogPrinter sLogPrinter = new DefaultLogPrinter();

	public static boolean setLogPrinter(ILogPrinter printer) {
		if (printer == null) {
			return false;
		}
		sLogPrinter = printer;
		return true;
	}

	public static void i(String tag, String format, Object... args) {
		sLogPrinter.printLog(INFO, tag, format, args);
	}

	public static void e(String tag, String format, Object... args) {
		sLogPrinter.printLog(ERROR, tag, format, args);
	}

	public static void w(String tag, String format, Object... args) {
		sLogPrinter.printLog(WARN, tag, format, args);
	}

	public static void d(String tag, String format, Object... args) {
		sLogPrinter.printLog(DEBUG, tag, format, args);
	}

	public static void v(String tag, String format, Object... args) {
		sLogPrinter.printLog(VERBOSE, tag, format, args);
	}

	public static void printLog(int priority, String tag, String format, Object... args) {
		sLogPrinter.printLog(priority, tag, format, args);
	}
	
	public static String formatTag(Object o) {
		if (o == null) {
			return null;
		}
		return formatTag(o.getClass());
	}

	public static String formatTag(Class<?> clazz) {
		if (clazz == null) {
			return null;
		}
		return clazz.getSimpleName();
	}
}
