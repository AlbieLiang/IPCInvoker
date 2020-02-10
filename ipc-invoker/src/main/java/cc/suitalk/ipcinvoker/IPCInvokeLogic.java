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
import android.text.TextUtils;

import org.junit.Assert;

import java.io.FileInputStream;
import java.util.List;

import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2017/5/13.
 */

public class IPCInvokeLogic {

    private static final String TAG = "IPC.IPCInvokeLogic";

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
        if (context != null) {
            try {
                ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                if (mActivityManager != null) {
                    List<ActivityManager.RunningAppProcessInfo> list = mActivityManager.getRunningAppProcesses();
                    if (list != null && !list.isEmpty()) {
                        for (ActivityManager.RunningAppProcessInfo appProcess : list) {
                            if (appProcess.pid == pid) {
                                return appProcess.processName;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "get running process by system error: %s", android.util.Log.getStackTraceString(e));
            }
        }
        byte[] b = new byte[128];
        FileInputStream in = null;
        try {
            in = new FileInputStream("/proc/" + pid + "/cmdline");
            int len = in.read(b);
            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    if (b[i] > 128 || b[i] <= 0) {
                        len = i;
                        break;
                    }
                }
                return new String(b, 0, len);
            }
        } catch (Exception e) {
            Log.e(TAG, "get running process error : %s", android.util.Log.getStackTraceString(e));
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     *  判断进程是否存活
     */
    public static boolean isProcessLive(@NonNull Context context, @NonNull String processName) {
        if (TextUtils.isEmpty(processName)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return false;
        }
        List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
        if (list == null) {
            return false;
        }
        for(ActivityManager.RunningAppProcessInfo info : list){
            if(processName.equals(info.processName)){
                return true;
            }
        }
        return false;
    }
}
