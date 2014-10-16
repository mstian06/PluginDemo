package com.tencent.PluginSDK;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;

/**
 * Created by jamie on 14-6-3.
 */
public interface IPluginActivity {
    public void IOnCreate(Bundle savedInstanceState);

    public void IOnResume();

    public void IOnStart();

    public void IOnPause();

    public void IOnStop();

    public void IOnDestroy();

    public void IOnRestart();

    public void IInit(String path, Activity context, ClassLoader classLoader, PackageInfo packageInfo);
}
