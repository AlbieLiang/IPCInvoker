package cc.suitalk.ipcinvoker.restore;

import android.os.Bundle;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cc.suitalk.ipcinvoker.IPCInvokeClient;
import cc.suitalk.ipcinvoker.IPCInvokeLogic;
import cc.suitalk.ipcinvoker.IPCInvoker;
import cc.suitalk.ipcinvoker.IPCSyncInvokeTask;
import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.event.IPCObserver;
import cc.suitalk.ipcinvoker.tools.Log;
import cc.suitalk.ipcinvoker.tools.storage.KVStorage;
import cc.suitalk.ipcinvoker.type.IPCVoid;

public class IPCObserverRestorer {
    private static final String TAG = "IPCInvoker.IPCObserverRestorer";
    // key: event,  value:observer
    private static final Map<String, Set<IPCObserver>> eventObserverMap = new HashMap<>();
    private static final String KEY_EVENT_PROCESS_LIST = "event_process_list_";
    private static final String KEY_EVENT = "event";
    private static final String KEY_PROCESS = "process";

    /**
     * 1. store event and observer in register process
     * 2. save event and process to local file
     * @param event
     * @param observer
     */
    public synchronized static void addIPCObserver(@NonNull String targetProcess, @NonNull String event, @NonNull IPCObserver observer) {
        Set<IPCObserver> observers = eventObserverMap.get(event);
        if (observers == null) {
            observers = new HashSet<>();
            eventObserverMap.put(event, observers);
        }
        observers.add(observer);

        // save process and event to local file
        String processName = IPCInvokeLogic.getCurrentProcessName();
        if (TextUtils.isEmpty(processName)) {
            return;
        }
        Set<EventProcess> set = getEventProcessSet(targetProcess);
        set.add(new EventProcess(event, processName));
        setEventProcessList(targetProcess, set);
    }

    /**
     * 1. remove event and observer in register process
     * 2. save event and process from local file
     * @param event
     * @param observer
     */
    public synchronized static void removeIPCObserver(@NonNull String targetProcess, @NonNull String event, @NonNull IPCObserver observer) {
        Set<IPCObserver> observers = eventObserverMap.get(event);
        if (observers == null) {
            return;
        }
        observers.remove(observer);
        if (observers.isEmpty()) {
            eventObserverMap.remove(event);
        }
        // save process and event to local file
        String processName = IPCInvokeLogic.getCurrentProcessName();
        if (TextUtils.isEmpty(processName)) {
            return;
        }
        Set<EventProcess> set = getEventProcessSet(targetProcess);
        set.remove(new EventProcess(event, processName));
        setEventProcessList(targetProcess, set);
    }

    @NonNull
    private static Set<EventProcess> getEventProcessSet(@NonNull String targetProcess) {
        String json = KVStorage.get().getString(KEY_EVENT_PROCESS_LIST + targetProcess, null);
        if (TextUtils.isEmpty(json)) {
            return Collections.emptySet();
        }
        try {
            JSONArray jsonArray = new JSONArray(json);
            Set<EventProcess> set = new HashSet<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                if (jsonObject == null) {
                    continue;
                }
                String event = jsonObject.optString(KEY_EVENT);
                String process = jsonObject.optString(KEY_PROCESS);
                if (TextUtils.isEmpty(event) || TextUtils.isEmpty(process)) {
                    continue;
                }
                set.add(new EventProcess(event, process));
            }
            return set;
        } catch (JSONException e) {
            Log.w(TAG, "getEventProcessSet, %s", android.util.Log.getStackTraceString(e));
            return Collections.emptySet();
        }
    }

    private static boolean setEventProcessList(@NonNull String targetProcess, @NonNull Set<EventProcess> set) {
        JSONArray jsonArray = new JSONArray();
        for (EventProcess eventProcess : set) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(KEY_EVENT, eventProcess.event);
                jsonObject.put(KEY_PROCESS, eventProcess.process);
            } catch (JSONException e) {
                Log.w(TAG, "setEventProcessList, put event(%s), process(%s) failed", eventProcess.event, eventProcess.process);
                continue;
            }
            jsonArray.put(jsonObject);
        }
//        Log.d(TAG, "setEventProcessList, process:%s, data:%s", targetProcess, jsonArray.toString());
        return KVStorage.get().putString(KEY_EVENT_PROCESS_LIST + targetProcess, jsonArray.toString());
    }

    /**
     * 1. get all event and process from local file. then clear it.
     * 2. check the status of process
     * 3. if process live, restore observer
     */
    public static void restore() {
        String processName = IPCInvokeLogic.getCurrentProcessName();
        if (TextUtils.isEmpty(processName)) {
            return;
        }
        // 1.
        Set<EventProcess> eventProcessSet = getEventProcessSet(processName);
//        Log.i(TAG, "restore, %s", eventProcessSet.toString());
        setEventProcessList(processName, Collections.<EventProcess>emptySet());
        for (EventProcess eventProcess : eventProcessSet) {
            // 2.
            boolean processLive = IPCInvokeLogic.isProcessLive(IPCInvokeLogic.getContext(), eventProcess.process);
            if (!processLive) {
                continue;
            }
            // 3
            Bundle bundle = new Bundle();
            bundle.putString(KEY_EVENT, eventProcess.event);
            bundle.putString(KEY_PROCESS, processName);
            IPCInvoker.invokeSync(eventProcess.process, bundle, IPCRestoreObserverSyncTask.class);
        }
    }

    /**
     * 1. get cached ipcObservers by event
     * 2. register observer for each
     */
    private static class IPCRestoreObserverSyncTask implements IPCSyncInvokeTask<Bundle, IPCVoid> {
        @Override
        public IPCVoid invoke(Bundle bundle) {
            if (bundle == null) {
                return null;
            }
            String event = bundle.getString(KEY_EVENT);
            String process = bundle.getString(KEY_PROCESS);
            if (TextUtils.isEmpty(event) || TextUtils.isEmpty(process)) {
                Log.w(TAG, "IPCRestoreObserverSyncTask, event or process is empty");
                return null;
            }

            synchronized (IPCObserverRestorer.class) {
                Set<IPCObserver> ipcObservers = eventObserverMap.get(event);
                if (ipcObservers == null) {
                    return null;
                }
                Log.i(TAG, "IPCRestoreObserverSyncTask, restore %d observer in process(%s) when process(%s) start",
                        ipcObservers.size(), IPCInvokeLogic.getCurrentProcessName(), process);
                IPCInvokeClient client = new IPCInvokeClient(process);
                for (IPCObserver observer: ipcObservers) {
                    client.registerIPCObserver(event, observer);
                }
            }
            return null;
        }
    }

    private static class EventProcess {
        private String event;
        private String process;

        public EventProcess(String event, String process) {
            this.event = event;
            this.process = process;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EventProcess that = (EventProcess) o;
            return equals(event, that.event) &&
                    equals(process, that.process);
        }

        @Override
        public int hashCode() {
            return hash(event, process);
        }

        private static boolean equals(Object a, Object b) {
            return (a == b) || (a != null && a.equals(b));
        }

        private static int hash(Object... values) {
            return Arrays.hashCode(values);
        }
    }

}
