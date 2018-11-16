package cc.suitalk.ipcinvoker.model;

import android.os.Parcel;

import java.util.LinkedList;
import java.util.List;

import cc.suitalk.ipcinvoker.extension.BaseTypeTransfer;
import cc.suitalk.ipcinvoker.extension.ObjectTypeTransfer;
import cc.suitalk.ipcinvoker.tools.Log;

/**
 * Created by albieliang on 2018/11/16.
 */

public class CollectionTypeTransfer implements BaseTypeTransfer {

    private static final String TAG = "IPC.CollectionTypeTransfer";

    @Override
    public boolean canTransfer(Object o) {
        return o instanceof List;
    }

    @Override
    public void writeToParcel(Object o, Parcel dest) {
        final List list = (List) o;
        dest.writeInt(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object element = list.get(i);
            BaseTypeTransfer transfer = ObjectTypeTransfer.getTypeTransfer(element);
            if (transfer == null) {
                Log.i(TAG, "writeToParcel, transfer(%s) not found", element.getClass().getName());
                continue;
            }
            dest.writeString(transfer.getClass().getName());
            ObjectTypeTransfer.writeToParcel(element, dest);
        }
    }

    @Override
    public Object readFromParcel(Parcel in) {
        final List list = new LinkedList();
        final int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String transferClass = in.readString();
            list.add(ObjectTypeTransfer.readFromParcel(transferClass, in));
        }
        return list;
    }
}
