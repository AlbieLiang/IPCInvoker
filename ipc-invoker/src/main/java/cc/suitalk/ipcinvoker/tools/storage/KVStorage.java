package cc.suitalk.ipcinvoker.tools.storage;

import android.content.Context;

import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.annotation.Nullable;

public abstract class KVStorage {
    private volatile static KVStorage INSTANCE;
    private Context context;

    public static void set(@NonNull KVStorage instance) {
        KVStorage.INSTANCE = instance;
    }

    @NonNull
    public static KVStorage get() {
        return INSTANCE;
    }

    public void setContext(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    protected Context getContext() {
        return context;
    }

    public abstract boolean putString(@NonNull String key, @NonNull String value);

    public abstract String getString(@NonNull String key, @Nullable String defaultValue);

}
