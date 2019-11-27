package cc.suitalk.ipcinvoker.tools.storage;

import android.content.Context;
import android.content.SharedPreferences;

import cc.suitalk.ipcinvoker.annotation.NonNull;
import cc.suitalk.ipcinvoker.annotation.Nullable;

public class DefaultKVStorage extends KVStorage {
    private static final String KEY_DEFAULT_KV_STORAGE = "default_kv_storage";

    public DefaultKVStorage(@NonNull Context context) {
        setContext(context);
    }

    @Override
    public boolean putString(@NonNull String key, @NonNull String value) {
        SharedPreferences sp = getContext().getSharedPreferences(KEY_DEFAULT_KV_STORAGE, Context.MODE_PRIVATE);
        if (sp == null) {
            return false;
        }
        return sp.edit().putString(key, value).commit();
    }

    @Override
    public String getString(@NonNull String key, @Nullable String defaultValue) {
        SharedPreferences sp = getContext().getSharedPreferences(KEY_DEFAULT_KV_STORAGE, Context.MODE_PRIVATE);
        if (sp == null) {
            return defaultValue;
        }
        return sp.getString(key, defaultValue);
    }
}
