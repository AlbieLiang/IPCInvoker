package cc.suitalk.ipcinvoker;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2018/9/21.
 */

class ServiceConnectionManager {

    private static final String TAG = "IPC.ServiceConnectionManager";

    private static final Map<String, List<ServiceConnection>> sMap = new HashMap<>();

    public static boolean registerServiceConnection(@NonNull String processName, @NonNull ServiceConnection serviceConnection) {
        if (TextUtils.isEmpty(processName) || serviceConnection == null) {
            Log.w(TAG, "registerServiceConnection failed, processName(%s), or serviceConnection(%s) is null.",
                    processName, serviceConnection);
            return false;
        }
        List<ServiceConnection> list;
        synchronized (sMap) {
            list = sMap.get(processName);
            if (list == null) {
                list = new LinkedList<>();
                sMap.put(processName, list);
            }
        }
        synchronized (list) {
            if (list.contains(serviceConnection)) {
                return false;
            }
            return list.add(serviceConnection);
        }
    }

    public static boolean unregisterServiceConnection(@NonNull String processName, @NonNull ServiceConnection serviceConnection) {
        if (TextUtils.isEmpty(processName) || serviceConnection == null) {
            Log.w(TAG, "unregisterServiceConnection failed, processName(%s), or serviceConnection(%s) is null.",
                    processName, serviceConnection);
            return false;
        }
        List<ServiceConnection> list;
        synchronized (sMap) {
            list = sMap.get(processName);
            if (list == null) {
                return false;
            }
        }
        synchronized (list) {
            return list.remove(serviceConnection);
        }
    }

    static void dispatchOnServiceConnected(String processName, ComponentName name, IBinder service) {
        Log.i(TAG, "dispatchOnServiceConnected(pn : %s)", processName);
        List<ServiceConnection> list;
        synchronized (sMap) {
            list = sMap.get(processName);
            if (list == null || list.isEmpty()) {
                return;
            }
        }
        List<ServiceConnection> tempList;
        synchronized (list) {
            tempList = new LinkedList<>(list);
        }
        for (ServiceConnection sc : tempList) {
            sc.onServiceConnected(name, service);
        }
    }

    static void dispatchOnServiceDisconnected(String processName, ComponentName name) {
        Log.i(TAG, "dispatchOnServiceDisconnected(pn : %s)", processName);
        List<ServiceConnection> list;
        synchronized (sMap) {
            list = sMap.get(processName);
            if (list == null || list.isEmpty()) {
                return;
            }
        }
        List<ServiceConnection> tempList;
        synchronized (list) {
            tempList = new LinkedList<>(list);
        }
        for (ServiceConnection sc : tempList) {
            sc.onServiceDisconnected(name);
        }
    }
}
