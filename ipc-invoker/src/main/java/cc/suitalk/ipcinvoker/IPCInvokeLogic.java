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

import android.app.ActivityManager;
import android.content.Context;

import junit.framework.Assert;

/**
 * Created by albieliang on 2017/5/13.
 */

public class IPCInvokeLogic {

    private static Context sContext;
    private static String sCurrentProcessName;

    static void setContext(Context context) {
        sContext = context;
    }

    public static Context getContext() {
        Assert.assertNotNull("IPCInvoker not initialize.", sContext);
        return sContext;
    }

    public static boolean isCurrentProcess(String process) {
        return process != null && process.equals(getCurrentProcessName());
    }

    public static String getCurrentProcessName() {
        if (sCurrentProcessName == null || sCurrentProcessName.length() == 0) {
            sCurrentProcessName = getProcessName(sContext, android.os.Process.myPid());
        }
        return sCurrentProcessName;
    }

    public static String getProcessName(Context context, int pid) {
        if (context == null) {
            return null;
        }
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
}
