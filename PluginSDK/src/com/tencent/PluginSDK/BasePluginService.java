package com.tencent.PluginSDK;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.IBinder;

@SuppressLint("NewApi")
public abstract class BasePluginService extends Service implements IPluginService {
	
	protected boolean mIsRunInPlugin;
	
	protected ClassLoader mDexClassLoader;
	protected Service mOutService;
	protected String mApkFilePath;
	protected PackageInfo mPackageInfo;
	
	private Context mContext;
	
	@Override
	public void IOnCreate() { 
		onCreate();
	}

	@Override
	public void IOnStart(Intent intent, int startId) {
		onStart(intent, startId);
	}

	@Override
	public int IOnStartCommand(Intent intent, int flags, int startId) {
		return onStartCommand(intent, flags, startId);
	}

	public IBinder IOnBind(Intent intent) {
		return onBind(intent);
	}
	
	@Override
	public boolean IOnUnbind(Intent intent) {
		return onUnbind(intent);
	}

	@Override
	public void IOnDestroy() {
		onDestroy();
	}
	
	@Override
	public Object getSystemService(String name) {
		if (WINDOW_SERVICE.equals(name) || SEARCH_SERVICE.equals(name)) {
			if (mIsRunInPlugin) {
				return mOutService.getSystemService(name);
			} else {
				return super.getSystemService(name);
			}
		}
		if (mContext != null) {
			return mContext.getSystemService(name);
		} else {
			return super.getSystemService(name);
		}
	}
	
	@Override
	public String getPackageName() {
		if (mIsRunInPlugin) {
			return mPackageInfo.packageName;
		} else {
			return super.getPackageName();
		}
	}
	
	public PackageInfo getPackageInfo() {
		if (mIsRunInPlugin) {
			return mPackageInfo;
		} else {
			return null; // TODO
		}
	}
	
	@Override
	public ApplicationInfo getApplicationInfo() {
		if (mIsRunInPlugin) {
			return mPackageInfo.applicationInfo;
		} else {
			return super.getApplicationInfo();
		}
	}
	
	@Override
	public void IInit(String apkPath, Service context, ClassLoader loader, PackageInfo packageInfo) {
		mIsRunInPlugin = true;
		
		mApkFilePath = apkPath;
		mOutService = context;
		mDexClassLoader = loader;
		mPackageInfo = packageInfo;

        mContext = new PluginContext(context, 0, mApkFilePath, mDexClassLoader);

		attachBaseContext(mContext);
	}

}
