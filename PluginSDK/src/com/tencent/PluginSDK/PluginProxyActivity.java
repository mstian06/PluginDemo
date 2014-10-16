package com.tencent.PluginSDK;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import dalvik.system.DexClassLoader;

import java.io.File;

/**
 * Created by jamie on 14-6-3.
 */
public class PluginProxyActivity extends Activity {
    IPluginActivity mPluginActivity;
    String mPluginApkFilePath;
    String mLaunchActivity;
    private String mPluginName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if(bundle == null){
            return;
        }
        mPluginName = bundle.getString(PluginStatic.PARAM_PLUGIN_NAME);
        mLaunchActivity = bundle.getString(PluginStatic.PARAM_LAUNCH_ACTIVITY);
        File pluginFile = PluginUtils.getInstallPath(PluginProxyActivity.this, mPluginName);
        if(!pluginFile.exists()){
            return;
        }
        mPluginApkFilePath = pluginFile.getAbsolutePath();
        try {
            initPlugin();
            super.onCreate(savedInstanceState);
            mPluginActivity.IOnCreate(savedInstanceState);
        } catch (Exception e) {
            mPluginActivity = null;
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mPluginActivity != null){
            mPluginActivity.IOnResume();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mPluginActivity != null) {
            mPluginActivity.IOnStart();
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if(mPluginActivity != null) {
            mPluginActivity.IOnRestart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mPluginActivity != null) {
            mPluginActivity.IOnStop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mPluginActivity != null) {
            mPluginActivity.IOnPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPluginActivity != null) {
            mPluginActivity.IOnDestroy();
        }
    }

    private void initPlugin() throws Exception {
        PackageInfo packageInfo;
        try {
            PackageManager pm = getPackageManager();
            packageInfo = pm.getPackageArchiveInfo(mPluginApkFilePath, PackageManager.GET_ACTIVITIES);
        } catch (Exception e) {
            throw e;
        }

        if (mLaunchActivity == null || mLaunchActivity.length() == 0) {
            mLaunchActivity = packageInfo.activities[0].name;
        }

//        String optimizedDexOutputPath = getDir("odex", Context.MODE_PRIVATE).getAbsolutePath();
        ClassLoader classLoader = PluginStatic.getOrCreateClassLoaderByPath(this, mPluginName, mPluginApkFilePath);

        if (mLaunchActivity == null || mLaunchActivity.length() == 0) {
            if (packageInfo == null || (packageInfo.activities == null) || (packageInfo.activities.length == 0)) {
                throw new ClassNotFoundException("Launch Activity not found");
            }
            mLaunchActivity = packageInfo.activities[0].name;
        }
        Class<?> mClassLaunchActivity = (Class<?>) classLoader.loadClass(mLaunchActivity);

        getIntent().setExtrasClassLoader(classLoader);
        mPluginActivity = (IPluginActivity) mClassLaunchActivity.newInstance();
        mPluginActivity.IInit(mPluginApkFilePath, this, classLoader, packageInfo);
    }


    protected Class<? extends PluginProxyActivity> getProxyActivity(String pluginActivityName) {
        return getClass();
    }

    protected  Class<? extends  PluginProxyService> getProxyService(String pluginServiceName){
        return PluginProxyService.class;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        boolean pluginActivity = intent.getBooleanExtra(PluginStatic.PARAM_IS_IN_PLUGIN, false);
        if (pluginActivity) {
            String launchActivity = null;
            ComponentName componentName = intent.getComponent();
            if(null != componentName) {
                launchActivity = componentName.getClassName();
            }
            intent.putExtra(PluginStatic.PARAM_IS_IN_PLUGIN, false);
            if (launchActivity != null && launchActivity.length() > 0) {
                Intent pluginIntent = new Intent(this, getProxyActivity(launchActivity));

                pluginIntent.putExtra(PluginStatic.PARAM_PLUGIN_NAME, mPluginName);
                pluginIntent.putExtra(PluginStatic.PARAM_PLUGIN_PATH, mPluginApkFilePath);
                pluginIntent.putExtra(PluginStatic.PARAM_LAUNCH_ACTIVITY, launchActivity);
                startActivityForResult(pluginIntent, requestCode);
            }
        } else {
			super.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        boolean pluginService = service.getBooleanExtra(PluginStatic.PARAM_IS_IN_PLUGIN, false);
        if (pluginService) {
            String serviceName = null;
            ComponentName componentName = service.getComponent();
            if (null != componentName) {
                serviceName = componentName.getClassName();
            }
            Intent intent = new Intent(this, getProxyService(serviceName));
            intent.putExtra(PluginStatic.PARAM_IS_IN_PLUGIN, false);
            intent.putExtra(PluginStatic.PARAM_PLUGIN_NAME, mPluginName);
            intent.putExtra(PluginStatic.PARAM_LAUNCH_SERVICE, serviceName);
            return super.bindService(intent, conn, flags);
        }else{
            return super.bindService(service, conn, flags);
        }
    }

}
