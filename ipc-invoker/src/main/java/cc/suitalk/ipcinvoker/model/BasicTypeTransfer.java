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

package cc.suitalk.ipcinvoker.model;

import android.os.Parcel;

import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.extension.BaseTypeTransfer;

/**
 * Created by albieliang on 2018/5/8.
 */

public class BasicTypeTransfer implements BaseTypeTransfer {

    private static final int TYPE_UNDEFINED = 0;
    private static final int TYPE_INT = 1;
    private static final int TYPE_FLOAT = 2;
    private static final int TYPE_BOOLEAN = 3;
    private static final int TYPE_STRING = 4;
    private static final int TYPE_BYTE = 5;
    private static final int TYPE_LONG = 6;
    private static final int TYPE_DOUBLE = 7;

    @Override
    public boolean canTransfer(Object o) {
        if (o instanceof Integer) {
            return true;
        } else if (o instanceof Float) {
            return true;
        } else if (o instanceof Boolean) {
            return true;
        } else if (o instanceof String) {
            return true;
        } else if (o instanceof Byte) {
            return true;
        } else if (o instanceof Long) {
            return true;
        } else if (o instanceof Double) {
            return true;
        }
        return false;
    }

    @Override
    public void writeToParcel(@NonNull Object o, @NonNull Parcel dest) {
        if (o instanceof Integer) {
            dest.writeInt(TYPE_INT);
            dest.writeInt((Integer) o);
        } else if (o instanceof Float) {
            dest.writeInt(TYPE_FLOAT);
            dest.writeFloat((Float) o);
        } else if (o instanceof Boolean) {
            dest.writeInt(TYPE_BOOLEAN);
            dest.writeInt(((Boolean) o) ? 1 : 0);
        } else if (o instanceof String) {
            dest.writeInt(TYPE_STRING);
            dest.writeString((String) o);
        } else if (o instanceof Byte) {
            dest.writeInt(TYPE_BYTE);
            dest.writeByte((Byte) o);
        } else if (o instanceof Long) {
            dest.writeInt(TYPE_LONG);
            dest.writeLong((Long) o);
        } else if (o instanceof Double) {
            dest.writeInt(TYPE_DOUBLE);
            dest.writeDouble((Double) o);
        } else {
            dest.writeInt(TYPE_UNDEFINED);
        }
    }

    @Override
    public Object readFromParcel(@NonNull Parcel in) {
        final int type = in.readInt();
        switch (type) {
            case TYPE_STRING: {
                return in.readString();
            }
            case TYPE_INT: {
                return in.readInt();
            }
            case TYPE_FLOAT: {
                return in.readFloat();
            }
            case TYPE_BOOLEAN: {
                return in.readInt() == 1;
            }
            case TYPE_LONG: {
                return in.readLong();
            }
            case TYPE_BYTE: {
                return in.readByte();
            }
            case TYPE_DOUBLE: {
                return in.readDouble();
            }
            case TYPE_UNDEFINED:
            default: {
            }
        }
        return null;
    }
}
