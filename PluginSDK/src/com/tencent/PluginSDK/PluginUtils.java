package com.tencent.PluginSDK;

import android.content.Context;

import java.io.*;

/**
 * Created by jamie on 14-6-4.
 */
public class PluginUtils {

    private static final String PLUGIN_PATH = "plugins";
    public static File getInstallPath(Context context, String pluginID) {
        File pluginDir = getPluginPath(context);
        if (pluginDir == null) {
            return null;
        }
        int suffixBegin = pluginID.lastIndexOf('.');
        if (suffixBegin != -1 && !pluginID.substring(suffixBegin).equalsIgnoreCase(".apk")) {
            pluginID = pluginID.substring(0, suffixBegin) + ".apk";
        } else if (suffixBegin == -1) {
            pluginID = pluginID + ".apk";
        }
        return new File(pluginDir, pluginID);
    }

    public static File getPluginPath(Context context) {
        return context.getDir(PLUGIN_PATH, Context.MODE_PRIVATE);
    }

    public static void installPlugin(Context context, String pluginPath){
        File pluginFile = new File(context.getDir(PLUGIN_PATH, Context.MODE_PRIVATE), pluginPath);
        if(pluginFile.exists()){
            return;
        }

        BufferedInputStream bis;
        OutputStream dexWriter;

        final int BUF_SIZE = 8 * 1024;
        try {
            bis = new BufferedInputStream(context.getAssets().open(pluginFile.getName()));
            dexWriter = new BufferedOutputStream(
                    new FileOutputStream(pluginFile));
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

