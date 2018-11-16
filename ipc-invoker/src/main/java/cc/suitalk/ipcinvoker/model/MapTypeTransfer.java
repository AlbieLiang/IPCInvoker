package cc.suitalk.ipcinvoker.model;

import android.os.Parcel;

import java.util.HashMap;
import java.util.Map;

import cc.suitalk.ipcinvoker.extension.BaseTypeTransfer;
import cc.suitalk.ipcinvoker.extension.ObjectTypeTransfer;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2018/11/16.
 */

public class MapTypeTransfer implements BaseTypeTransfer {

    private static final String TAG = "IPC.CollectionTypeTransfer";

    @Override
    public boolean canTransfer(Object o) {
        return o instanceof Map;
    }

    @Override
    public void writeToParcel(Object o, Parcel dest) {
        final Map<?, ?> map = (Map) o;
        dest.writeInt(map.size());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            // write key
            Object key = entry.getKey();
            BaseTypeTransfer keyTransfer = ObjectTypeTransfer.getTypeTransfer(key);
            if (keyTransfer == null) {
                Log.i(TAG, "writeToParcel, keyTransfer(%s) not found", key != null ? key.getClass().getName() : null);
                continue;
            }
            dest.writeString(keyTransfer.getClass().getName());
            ObjectTypeTransfer.writeToParcel(key, dest);
            // Write value
            Object value = entry.getValue();
            BaseTypeTransfer valueTransfer = ObjectTypeTransfer.getTypeTransfer(value);
            if (valueTransfer == null) {
                Log.i(TAG, "writeToParcel, valueTransfer(%s) not found", value != null ? value.getClass().getName() : null);
                continue;
            }
            dest.writeString(valueTransfer.getClass().getName());
            ObjectTypeTransfer.writeToParcel(value, dest);
        }
    }

    @Override
    public Object readFromParcel(Parcel in) {
        final Map map = new HashMap<>();
        final int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String keyTransferClass = in.readString();
            Object key = ObjectTypeTransfer.readFromParcel(keyTransferClass, in);

            String valueTransferClass = in.readString();
            Object value = ObjectTypeTransfer.readFromParcel(valueTransferClass, in);

            map.put(key, value);
        }
        return map;
    }
}
