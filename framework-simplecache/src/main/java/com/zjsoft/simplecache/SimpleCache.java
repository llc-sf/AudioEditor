package com.zjsoft.simplecache;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.zjsoft.simplecache.cache.DiskCache;
import com.zjsoft.simplecache.cache.MemoryCache;


/**
 * User: robotinthesun
 * 主体类
 */
public final class SimpleCache {
    //名字
    private  String shareName;
    //缓存大小
    private  int cacheSize;
    private  DiskCache diskCache = null;
    private  MemoryCache memoryCache = null;


    /**
     * 初始化
     */
    public SimpleCache(String shareName, int cacheSize, String path) {
        shareName = shareName;
        cacheSize = cacheSize;

        memoryCache = new MemoryCache();
        diskCache = new DiskCache(cacheSize, path, 1);

    }

    /**
     * 设置int类型的值
     *
     * @param key   键
     * @param value 值
     */
    public void putInt(String key, int value) {
        memoryCache.put(key, new Integer(value));
        diskCache.put(key, new Integer(value));

    }

    /**
     * 设置long类型的值
     *
     * @param key   键
     * @param value 值
     */
    public  void putLong(String key, long value) {
        memoryCache.put(key, new Long(value));
        diskCache.put(key, new Long(value));
    }

    /**
     * 设置boolean类型的值
     *
     * @param key   键
     * @param value 值
     */
    public void putBoolean(String key, boolean value) {
        memoryCache.put(key, new Boolean(value));
        diskCache.put(key, new Boolean(value));
    }


    /**
     * 设置String类型的值
     *
     * @param key   键
     * @param value 值
     */
    public void putString(String key, String value) {
        memoryCache.put(key, value);
        diskCache.put(key, value);
    }

    /**
     * 设置float类型的值
     *
     * @param key   键
     * @param value 值
     */
    public void putFloat(String key, float value) {
        memoryCache.put(key, new Float(value));
        diskCache.put(key, new Float(value));
    }

    /**
     * 设置double类型的值
     *
     * @param key   键
     * @param value 值
     */
    public void putDouble(String key, double value) {
        memoryCache.put(key, new Double(value));
        diskCache.put(key, new Double(value));
    }

    /**
     * 设置Object类型的值
     *
     * @param key   键
     * @param value 值
     */
    public void putObject(String key, Object value) {
        memoryCache.put(key, value);
        diskCache.put(key, value);

    }

    /**
     * 设置bytes类型的值
     *
     * @param key   键
     * @param bytes 值
     */
    public void putBytes(String key, byte[] bytes) {
        memoryCache.put(key, bytes);
        diskCache.put(key, bytes);

    }

    /**
     * 设置Bitmap类型的值
     *
     * @param key   键
     * @param value 值
     */
    public void putBitmap(String key, Bitmap value) {
        memoryCache.put(key, value);
        diskCache.put(key, value);

    }


    /**
     * 得到int类型的值
     *
     * @param key     键
     * @param defalut 默认值
     * @return int值
     */
    public int getInt(String key, int defalut) {
        Integer value = memoryCache.getInt(key);
        if (value == null) {
            value = diskCache.getInt(key);

            if (value == null) {
                return defalut;
            } else {
                memoryCache.put(key,value);
                return value.intValue();
            }
        } else {
            return value.intValue();
        }

    }

    /**
     * 得到long类型的值
     *
     * @param key     键
     * @param defalut 默认值
     * @return long值
     */
    public long getLong(String key, long defalut) {
        Long value = memoryCache.getLong(key);
        if (value == null) {
            value = diskCache.getLong(key);

            if (value != null) {
                memoryCache.put(key,value);
                return value.longValue();
            }
            else
                return defalut;
        } else {
            return value.longValue();
        }
    }

    /**
     * 得到String类型的值
     *
     * @param key 键
     * @return String值
     */
    public String getString(String key) {
        String value = memoryCache.getString(key);
        if (value == null) {
            value = diskCache.getString(key);
            if (value!=null)
                memoryCache.put(key,value);
            return value;
        } else {
            return value;
        }
    }

    /**
     * 得到boolean类型的值
     *
     * @param key     键
     * @param defalut 默认值
     * @return boolean值
     */
    public boolean getBoolean(String key, boolean defalut) {
        Boolean value = memoryCache.getBoolean(key);
        if (value == null) {
            value = diskCache.getBoolean(key);
            if (value == null)
                return defalut;
            memoryCache.put(key,value);
            return value.booleanValue();
        } else {
            return value.booleanValue();
        }
    }

    /**
     * 得到double类型的值
     *
     * @param key     键
     * @param defalut 默认值
     * @return double值
     */
    public double getDouble(String key, double defalut) {
        Double value = memoryCache.getDouble(key);
        if (value == null) {
            value = diskCache.getDouble(key);

            if (value == null)
                return defalut;
            memoryCache.put(key,value);
            return value.doubleValue();
        } else {
            return value.doubleValue();
        }
    }

    /**
     * 得到float类型的值
     *
     * @param key     键
     * @param defalut 默认值
     * @return float值
     */
    public float getFloat(String key, float defalut) {
        Float value = memoryCache.getFloat(key);
        if (value == null) {
            value = diskCache.getFloat(key);
            if (value == null)
                return defalut;
            memoryCache.put(key,value);
            return value.floatValue();
        } else {
            return value.floatValue();
        }
    }

    /**
     * 得到object类型的值
     *
     * @param key 键
     * @return object值
     */
    public Object getObject(String key) {
        Object object = memoryCache.getObject(key);
        if (object == null) {
            object = diskCache.getObject(key);
            if (object!=null)
                memoryCache.put(key,object);
            return object;
        } else {
            return object;
        }
    }

    /**
     * 得到bytes类型的值
     *
     * @param key 键
     * @return bytes值
     */

    public byte[] getBytes(String key) {
        byte[] bytes = memoryCache.getBytes(key);
        if (bytes == null) {
            bytes = diskCache.getBytes(key);
        }
        if (bytes!=null)
            memoryCache.put(key,bytes);
        return bytes;
    }

    /**
     * 得到Bitmap类型的值
     *
     * @param key 键
     * @return bitmap值
     */
    public Bitmap getBitmap(String key) {
        Bitmap bitmap = memoryCache.getBitmap(key);
        if (bitmap == null) {
            bitmap = diskCache.getBitmap(key);

        }
        if (bitmap!=null)
            memoryCache.put(key,bitmap);

        return bitmap;
    }


    /**
     * 清楚数据
     */
    public void clearData() {
        memoryCache.clear();
        diskCache.clear();

    }

    /**
     * 清除指定的数据
     *
     * @param key 键
     */
    public void remove(String key) {
        diskCache.remove(key);

        memoryCache.remove(key);
    }


    //异步方法

    /**
     * 异步设置Bitmap类型的值
     *
     * @param key      键
     * @param value    值
     * @param callback 回调函数
     */
    public void putBitmapAsync(final String key, final Bitmap value, final Callback callback) {
        new AsyncTask<Void, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                putBitmap(key, value);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPreExecute();
                if (callback != null) {
                    callback.apply();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 异步设置String类型的值
     *
     * @param key      键
     * @param value    值
     * @param callback 回调函数
     */
    public void putStringAsync(final String key, final String value, final Callback callback) {
        new AsyncTask<Void, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                putString(key, value);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPreExecute();
                if (callback != null) {
                    callback.apply();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    /**
     * 异步设置Object类型的值
     *
     * @param key      键
     * @param value    值
     * @param callback 回调函数
     */
    public void putObjectAsync(final String key, final Object value, final Callback callback) {
        new AsyncTask<Void, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                putObject(key, value);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPreExecute();
                if (callback != null) {
                    callback.apply();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    /**
     * 异步设置long类型的值
     *
     * @param key      键
     * @param value    值
     * @param callback 回调函数
     */
    public void putLongAsync(final String key, final long value, final Callback callback) {
        new AsyncTask<Void, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                putLong(key, value);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPreExecute();
                if (callback != null) {
                    callback.apply();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 异步设置int类型的值
     *
     * @param key      键
     * @param value    值
     * @param callback 回调函数
     */
    public void putIntAsync(final String key, final int value, final Callback callback) {

        new AsyncTask<Void, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                putInt(key, value);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPreExecute();
                if (callback != null) {
                    callback.apply();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    /**
     * 异步设置double类型的值
     *
     * @param key      键
     * @param value    值
     * @param callback 回调函数
     */
    public void putDoubleAsync(final String key, final double value, final Callback callback) {
        new AsyncTask<Void, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                putDouble(key, value);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPreExecute();
                if (callback != null) {
                    callback.apply();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 异步设置float类型的值
     *
     * @param key      键
     * @param value    值
     * @param callback 回调函数
     */
    public void putFloatAsync(final String key, final float value, final Callback callback) {
        new AsyncTask<Void, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                putFloat(key, value);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPreExecute();
                if (callback != null) {
                    callback.apply();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 异步设置byte[]类型的值
     *
     * @param key      键
     * @param value    值
     * @param callback 回调函数
     */
    public void putBytesAsync(final String key, final byte[] value, final Callback callback) {
        new AsyncTask<Void, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                putBytes(key, value);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPreExecute();
                if (callback != null) {
                    callback.apply();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 异步设置boolean类型的值
     *
     * @param key      键
     * @param value    值
     * @param callback 回调函数
     */
    public void putBooleanAsync(final String key, final boolean value, final Callback callback) {
        new AsyncTask<Void, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                putBoolean(key, value);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPreExecute();
                if (callback != null) {

                    callback.apply();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 回调接口
     */
    public interface Callback {
        void apply();
    }


}
