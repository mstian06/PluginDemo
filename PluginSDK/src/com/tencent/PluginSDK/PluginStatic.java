package com.tencent.PluginSDK;

import android.content.Context;
import android.content.pm.PackageInfo;
import dalvik.system.DexClassLoader;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jamie on 14-6-3.
 */
public class PluginStatic {
    public static final String PARAM_IS_IN_PLUGIN = "is_in_plugin";
    public static final String PARAM_PLUGIN_NAME = "plugin_name";
    public static final String PARAM_LAUNCH_ACTIVITY = "launch_activity";
    public static final String PARAM_PLUGIN_PATH = "plugin_path";
    public static final String PARAM_LAUNCH_SERVICE = "launch_service";

    static final HashMap<String, DexClassLoader> sClassLoaderMap = new HashMap<String, DexClassLoader>();
    static final ConcurrentHashMap<String, PackageInfo> sPackageInfoMap = new ConcurrentHashMap<String, PackageInfo>();

    static synchronized ClassLoader getOrCreateClassLoaderByPath(Context c, String pluginName, String apkFilePath) throws Exception {

        DexClassLoader dexClassLoader = PluginStatic.sClassLoaderMap.get(pluginName);
        if (dexClassLoader == null) {
            String optimizedDexOutputPath = c.getDir("odex", Context.MODE_PRIVATE).getAbsolutePath();
            dexClassLoader = new DexClassLoader(apkFilePath, optimizedDexOutputPath, null, c.getClassLoader());
            PluginStatic.sClassLoaderMap.put(pluginName, dexClassLoader);
        }
        return dexClassLoader;
    }

    static synchronized ClassLoader getClassLoader(String pluginID) {
        DexClassLoader dexClassLoader = PluginStatic.sClassLoaderMap.get(pluginID);
        return dexClassLoader;
    }
}
