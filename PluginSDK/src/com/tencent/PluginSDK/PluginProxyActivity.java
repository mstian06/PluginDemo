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
import android.os.Environment;
import android.widget.Toast;

import dalvik.system.DexClassLoader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        
        // [tms] 测试代码，测试从sd卡load apk调用的情况，打开需要检查路径是否正确。
//        testToast();
//        testToast2();
    }
    
    private void testToast() {
        String path = Environment.getExternalStorageDirectory() + "/";
        String filename = "TestB.apk";
        String optimizedDirectory = path + File.separator + "dex_temp"  ;
        // 核心是这里， 通过getDir来获取一个File对象，然后在获取到getAbsolutePath, 传递给DexClassLoader 即可
        File file = getDir("dex", 0) ;
        
//        String path = Environment.getExternalStorageDirectory() + "/";
//        String filename = "TestB.apk";
//        DexClassLoader classLoader = new DexClassLoader(path + filename, path,
//                null, getClassLoader());
        
        DexClassLoader classLoader = new DexClassLoader(path + filename, file.getAbsolutePath(),
                null, getClassLoader());

        try {
            Class mLoadClass = classLoader.loadClass("com.example.testb.TestBActivity");
            Constructor constructor = mLoadClass.getConstructor(new Class[] {});
            Object TestBActivity = constructor.newInstance(new Object[] {});
            
            Method getMoney = mLoadClass.getMethod("getMoney", null);
            getMoney.setAccessible(true);
            Object money = getMoney.invoke(TestBActivity, null);
            Toast.makeText(this, money.toString(), Toast.LENGTH_LONG).show();
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    private void testToast2() {

        String path = Environment.getExternalStorageDirectory() + "/";
        String filename = "PluginDemo_1.apk";
        String optimizedDirectory = path + File.separator + "dex_temp"  ;
        // 核心是这里， 通过getDir来获取一个File对象，然后在获取到getAbsolutePath, 传递给DexClassLoader 即可
        File file = getDir("dex", 0) ;
        
//        String path = Environment.getExternalStorageDirectory() + "/";
//        String filename = "TestB.apk";
//        DexClassLoader classLoader = new DexClassLoader(path + filename, path,
//                null, getClassLoader());
        
        DexClassLoader classLoader = new DexClassLoader(path + filename, file.getAbsolutePath(),
                null, getClassLoader());

        try {
            Class mLoadClass = classLoader.loadClass("com.tencent.Plugin1.SubActivity");
            Constructor constructor = mLoadClass.getConstructor(new Class[] {});
            Object TestBActivity = constructor.newInstance(new Object[] {});
            
            Method getMoney = mLoadClass.getMethod("getMoney", null);
            getMoney.setAccessible(true);
            Object money = getMoney.invoke(TestBActivity, null);
            Toast.makeText(this, money.toString(), Toast.LENGTH_LONG).show();
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
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
        
        // [tms] 测试代码，测试从sd卡路径load apk，打开这段代码需要检查测试路径是否正确。
//        try {
//            String path = Environment.getExternalStorageDirectory() + "/";
//            String filename = "Plugin1.apk";
//            String optimizedDirectory = path + File.separator + "dex_temp"  ;
//            // 核心是这里， 通过getDir来获取一个File对象，然后在获取到getAbsolutePath, 传递给DexClassLoader 即可
//            File file = getDir("dex", 0) ;
//            DexClassLoader classLoader2 = new DexClassLoader(path + filename, file.getAbsolutePath(),
//                    null, getClassLoader());
//            
//            Class mLoadClass = classLoader2.loadClass("com.tencent.Plugin1.MainActivity");
//            Constructor constructor = mLoadClass.getConstructor(new Class[] {});
//            Object TestBActivity = constructor.newInstance(new Object[] {});
//            
//            Method getMoney = mLoadClass.getMethod("getMoney", null);
//            getMoney.setAccessible(true);
//            Object money = getMoney.invoke(TestBActivity, null);
//            Toast.makeText(this, money.toString(), Toast.LENGTH_LONG).show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
