package com.common.lib.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PrefUtil {
    public static void putString(Context context, String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);
    }

    public static void putStringSet(Context context, String key, Set<String> value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(key, value).apply();
    }

    public static Set<String> getStringSet(Context context, String key, Set<String> defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(key, defaultValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
    }

    public static void putFloat(Context context, String key, Float value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat(key, value).apply();
    }

    public static Float getFloat(Context context, String key, Float defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat(key, defaultValue);
    }

    public static void putLong(Context context, String key, Long value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value).apply();
    }

    public static Long getLong(Context context, String key, Long defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defaultValue);
    }

    public static void putInt(Context context, String key, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).apply();
    }

    public static int getInt(Context context, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue);
    }

    public static boolean contains(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).contains(key);
    }

    public static void remove(Context context, String key) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(key).apply();
    }

    public static File getDefaultSharedPreferencesFile(Context context) {
        return new File(context.getFilesDir().getParentFile() + File.separator + "shared_prefs",
                context.getPackageName() + "_preferences.xml");
    }

    public static String getDSPAll(Context context) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, ?> entry : PreferenceManager.getDefaultSharedPreferences(context).getAll().entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();
            if (value instanceof String) {
                try {
                    jsonObject.put(key, value);
                } catch (JSONException e) {
                }
            } else if (value instanceof Set) {
                try {
                    jsonObject.put(key, value); // EditorImpl.putStringSet already creates a copy of the set
                } catch (JSONException e) {
                }
            } else if (value instanceof Integer) {
                try {
                    jsonObject.put(key, value);
                } catch (JSONException e) {
                }
            } else if (value instanceof Long) {
                try {
                    jsonObject.put(key, value);
                } catch (JSONException e) {
                }
            } else if (value instanceof Float) {
                try {
                    jsonObject.put(key, value);
                } catch (JSONException e) {
                }
            } else if (value instanceof Boolean) {
                try {
                    jsonObject.put(key, value);
                } catch (JSONException e) {
                }
            }
        }
        return jsonObject.toString();
    }

    public static void putDSPAll(Context context, String prefJsonString) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor toEditor = sharedPreferences.edit();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(prefJsonString);
        } catch (JSONException e) {
            toEditor.clear();
            toEditor.apply();
            return;
        }

        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = null;
            try {
                value = jsonObject.get(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (value instanceof String) {
                toEditor.putString(key, ((String) value));
            } else if (value instanceof Set) {
                toEditor.putStringSet(key, (Set<String>) value); // EditorImpl.putStringSet already creates a copy of the set
            } else if (value instanceof Integer) {
                toEditor.putInt(key, (Integer) value);
            } else if (value instanceof Long) {
                toEditor.putLong(key, (Long) value);
            } else if (value instanceof Float) {
                toEditor.putFloat(key, (Float) value);
            } else if (value instanceof Boolean) {
                toEditor.putBoolean(key, (Boolean) value);
            }
        }
        toEditor.apply();
    }

}
