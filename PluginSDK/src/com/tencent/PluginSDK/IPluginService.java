package com.tencent.PluginSDK;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.IBinder;

public interface IPluginService {
	public void IInit(String apkPath, Service context, ClassLoader loader, PackageInfo packageInfo);

	public void IOnCreate();

	public void IOnStart(Intent intent, int startId);

	public int IOnStartCommand(Intent intent, int flags, int startId);

	public IBinder IOnBind(Intent intent);

	public boolean IOnUnbind(Intent intent);

	public void IOnDestroy();
}
