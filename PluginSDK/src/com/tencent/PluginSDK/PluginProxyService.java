package com.tencent.PluginSDK;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;

import java.io.File;
import java.io.IOException;

public class PluginProxyService extends Service {
	
	private IPluginService mPluginService;

	private String mPluginName;
	private String mPluginFilePath;
	
	private String mLaunchService;
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		boolean ret = startPluginIfNeccessary(intent);
		if(ret && null != mPluginService) {
			mPluginService.IOnStart(intent, startId);
		}
	}

	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int res = super.onStartCommand(intent, flags, startId);
		boolean ret = startPluginIfNeccessary(intent);
		if(ret && null != mPluginService) {
			res = mPluginService.IOnStartCommand(intent, flags, startId);
		}
		return Service.START_REDELIVER_INTENT;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		boolean res = super.onUnbind(intent);
		if(null != mPluginService) {
			res = mPluginService.IOnUnbind(intent);
		}
		return res;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(null != mPluginService) {
			mPluginService.IOnDestroy();
			mPluginService = null;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		IBinder res = null;
		if(null == mPluginService) {
			startPluginIfNeccessary(intent);
		}
		if(null != mPluginService) {
			res = mPluginService.IOnBind(intent);
		}
		return res;
	}

	protected boolean startPluginIfNeccessary(Intent intent) {
		if(null == intent) {
			return false;
		}
		
		String pluginName = intent.getStringExtra(PluginStatic.PARAM_PLUGIN_NAME);
		String launchService = intent.getStringExtra(PluginStatic.PARAM_LAUNCH_SERVICE);
        File file = PluginUtils.getInstallPath(this, pluginName);
        try {
            mPluginFilePath = file.getCanonicalPath();
        } catch (IOException e) {
            e = null;
        }

        if(null != mPluginService) {
			if(mPluginName.equals(pluginName) && mLaunchService.equals(launchService)) {
				//already init the same service
				return true;
			} else {
				//error arguments
				return false;
			}
		}
		
		mPluginName = pluginName;
		mLaunchService = launchService;

		ClassLoader classLoader = PluginStatic.getClassLoader(mPluginName);
		if(null != classLoader) {
			intent.setExtrasClassLoader(classLoader);
		}
		
		String errInfo = null;
		if (mPluginName == null || mPluginName.length() == 0) {
			errInfo = "Plugin name is wrong";
		} else {
			File f = new File(mPluginFilePath);
			if (!f.exists() && !f.isFile()) {
				errInfo = "Plugin File Not Found!";
			} else {
				try {
					errInfo = initPlugin();
					if(null == errInfo) {
						mPluginService.IOnCreate();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return true;
	}
	
	private String initPlugin() throws Exception {

		PackageInfo packageInfo = PluginStatic.sPackageInfoMap.get(mPluginFilePath);
		if (packageInfo == null) {
            try {
                PackageManager pm = getPackageManager();
                packageInfo = pm.getPackageArchiveInfo(mPluginFilePath, PackageManager.GET_ACTIVITIES);
            } catch(Exception e) {
                e = null;
            }

			if (packageInfo == null) {
				return "Get Package Info Failed!";
			}
			PluginStatic.sPackageInfoMap.put(mPluginFilePath, packageInfo);
		}

		ClassLoader classLoader = PluginStatic.getOrCreateClassLoaderByPath(this, mPluginName, mPluginFilePath);
		
		Class<?> pluginServiceClass = (Class<?>) classLoader.loadClass(mLaunchService);
		mPluginService = (IPluginService) pluginServiceClass.newInstance();
		
		mPluginService.IInit(mPluginFilePath, this, classLoader, packageInfo);

		return null;
	}
}
