package com.zjsoft.simplecache;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class SharedPreferenceV2 implements SharedPreferences, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SharedPreferenceV2";

    private ConcurrentHashMap<String, Object> cache;
    private final SharedPreferences preferences;

    private final ExecutorService service = SharedPreferenceV2Executors.getExecutors();

    public SharedPreferenceV2(SharedPreferences sharedPreferences) {
        preferences = sharedPreferences;
        cache = new ConcurrentHashMap<String, Object>() {
            @Nullable
            @Override
            public Object put(@Nullable String key, @Nullable Object value) {
                //如果key为null或者value为null的情况下，会报空指针异常。
                if (key != null && value != null) {
                    return super.put(key, value);
                } else {
                    return value;
                }
            }
        };
        cache.putAll(preferences.getAll());
    }

    @Override
    public Map<String, ?> getAll() {
        return cache;
    }

    @Nullable
    @Override
    public String getString(String s, @Nullable String s1) {
        if (cache.containsKey(s)) {
            return (String) cache.get(s);
        }

        String value = preferences.getString(s, s1);
        cache.put(s, value);
        return value;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String s, @Nullable Set<String> set) {
        if (cache.containsKey(s)) {
            return (Set<String>) cache.get(s);
        }

        Set<String> value = preferences.getStringSet(s, set);
        cache.put(s, value);
        return value;
    }

    @Override
    public int getInt(String s, int i) {
        if (cache.containsKey(s)) {
            return ((Integer) cache.get(s)).intValue();
        }

        int value = preferences.getInt(s, i);
        cache.put(s, value);
        return value;
    }

    @Override
    public long getLong(String s, long l) {
        if (cache.containsKey(s)) {
            return ((Long) cache.get(s)).longValue();
        }

        long value = preferences.getLong(s, l);
        cache.put(s, value);
        return value;
    }

    @Override
    public float getFloat(String s, float v) {
        if (cache.containsKey(s)) {
            return ((Float) cache.get(s)).floatValue();
        }

        float value = preferences.getFloat(s, v);
        cache.put(s, value);
        return value;
    }

    @Override
    public boolean getBoolean(String s, boolean b) {
        if (cache.containsKey(s)) {
            return ((Boolean) cache.get(s)).booleanValue();
        }

        boolean value = preferences.getBoolean(s, b);
        cache.put(s, value);
        return value;
    }

    @Override
    public boolean contains(String s) {
        if (cache.containsKey(s)) {
            return true;
        }

        return preferences.contains(s);
    }

    @Override
    public Editor edit() {
        return new EditorV2(cache, preferences.edit(), service);
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        cache.putAll(sharedPreferences.getAll());
//        printPreference(cache);
    }


    private static void printPreference(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("\n");
        }

        Log.e(TAG, sb.toString());
    }

    private static class EditorV2 implements Editor {
        private final ConcurrentHashMap<String, Object> editorCache;
        private final ConcurrentHashMap<String, Object> cache;
        private final Editor editor;
        private final ExecutorService execute;

        public EditorV2(ConcurrentHashMap<String, Object> cache, Editor editor, ExecutorService executorService) {
            editorCache = new ConcurrentHashMap<>();
            this.cache = cache;
            this.editor = editor;
            this.execute = executorService;
        }

        @Override
        public Editor putString(String s, @Nullable String s1) {
            editorCache.put(s, s1);
            editor.putString(s, s1);
            return this;
        }

        @Override
        public Editor putStringSet(String s, @Nullable Set<String> set) {
            editorCache.put(s, set);
            editor.putStringSet(s, set);
            return this;
        }

        @Override
        public Editor putInt(String s, int i) {
            editorCache.put(s, i);
            editor.putInt(s, i);
            return this;
        }

        @Override
        public Editor putLong(String s, long l) {
            editorCache.put(s, l);
            editor.putLong(s, l);
            return this;
        }

        @Override
        public Editor putFloat(String s, float v) {
            editorCache.put(s, v);
            editor.putFloat(s, v);
            return this;
        }

        @Override
        public Editor putBoolean(String s, boolean b) {
            editorCache.put(s, b);
            editor.putBoolean(s, b);
            return this;
        }

        @Override
        public Editor remove(String s) {
            cache.remove(s);
            editor.remove(s);
            return this;
        }

        @Override
        public Editor clear() {
            cache.clear();
            editor.clear();
            return this;
        }

        @Override
        public boolean commit() {
            Log.e(TAG, "commit shared preference");
            cache.putAll(editorCache);
//            printPreference(cache);
            return editor.commit();
        }

        @Override
        public void apply() {
            Log.e(TAG, "apply shared preference");
            cache.putAll(editorCache);
//            printPreference(cache);
            execute.execute(() -> {
                Log.e(TAG, "commit shared preference in background");
                //commit方法是线程安全的，所以用single和多线程池无差别
                editor.commit();
            });
        }
    }
}
