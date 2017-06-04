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

import android.os.Looper;

import cc.suitalk.ipcinvoker.BuildConfig;
import cc.suitalk.ipcinvoker.tools.log.ILogPrinter;
import cc.suitalk.ipcinvoker.tools.log.ILogWriter;

/**
 * A default LogPrinter. Invoked {@link Log#setLogPrinter(ILogPrinter)} to set a
 * {@link ILogPrinter} for the Log tools.
 *
 * @author AlbieLiang
 * @see Log#setLogPrinter(ILogPrinter)
 */
public class DefaultLogPrinter implements ILogPrinter {

    private boolean mWriterStarted;

    public DefaultLogPrinter() {
    }

    @Override
    public void printLog(int priority, String tag, String format, Object... args) {
        if (priority < getPriority()) {
            return;
        }
        String msg = String.format(format, args);
        android.util.Log.println(priority, tag, msg);
    }

    @Override
    public boolean isLoggable(String tag, int priority) {
        return android.util.Log.isLoggable(tag, priority);
    }

    @Override
    public int getPriority() {
        int priority = Log.VERBOSE;
        if (!BuildConfig.DEBUG) {
            priority = Log.INFO;
        }
        return priority;
    }

    @Override
    public ILogWriter getLogWriter() {
        return null;
    }
}