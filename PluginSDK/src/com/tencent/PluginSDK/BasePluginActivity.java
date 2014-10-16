package com.tencent.PluginSDK;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.*;

import java.lang.reflect.Field;

/**
 * Created by jamie on 14-6-3.
 */
public class BasePluginActivity extends Activity implements IPluginActivity {

    private boolean mIsRunInPlugin;
    private ClassLoader mDexClassLoader;
    private Activity mOutActivity;
    private String mApkFilePath;
    private PackageInfo mPackageInfo;
    private PluginContext mContext;
    private View mContentView;
    private Activity mActivity;
    private boolean mFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (mIsRunInPlugin) {
            mActivity = mOutActivity;
        } else {
            super.onCreate(savedInstanceState);
            mActivity = this;
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        if (mIsRunInPlugin) {
            mContentView = LayoutInflater.from(mContext).inflate(layoutResID, null);
            mActivity.setContentView(mContentView);
        } else {
            super.setContentView(layoutResID);
        }
    }

    @Override
    public void setContentView(View view) {
        if (mIsRunInPlugin) {
            mContentView = view;
            mActivity.setContentView(mContentView);
        } else {
            super.setContentView(view);
        }
    }

    @Override
    public View findViewById(int id) {
        if (mIsRunInPlugin && mContentView != null) {
            View v = mContentView.findViewById(id);
            if (null == v) {
                v = super.findViewById(id);
            }
            return v;
        } else {
            return super.findViewById(id);
        }
    }

    @Override
    public void IOnCreate(Bundle savedInstanceState) {
        onCreate(savedInstanceState);
    }

    @Override
    public void IOnResume() {
        onResume();
    }

    @Override
    public void IOnStart() {
        onStart();
    }

    @Override
    public void IOnPause() {
        onPause();
    }

    @Override
    public void IOnStop() {
        onStop();
    }

    @Override
    public void IOnDestroy() {
        onDestroy();
    }

    @Override
    public void IOnRestart() {
        onRestart();
    }

    @Override
    public void IInit(String path, Activity context, ClassLoader classLoader, PackageInfo packageInfo) {
        mIsRunInPlugin = true;
        mDexClassLoader = classLoader;
        mOutActivity = context;
        mApkFilePath = path;
        mPackageInfo = packageInfo;

        mContext = new PluginContext(context, 0, mApkFilePath, mDexClassLoader);
        attachBaseContext(mContext);
    }

    @Override
    protected void onResume() {
        if (mIsRunInPlugin) {
            return;
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mIsRunInPlugin) {
            return;
        }
        super.onPause();

    }

    @Override
    protected void onStart() {
        if (mIsRunInPlugin) {
            return;
        }
        super.onStart();
    }

    @Override
    protected void onRestart() {
        if (mIsRunInPlugin) {
            return;
        }
        super.onRestart();
    }

    @Override
    protected void onStop() {
        if (mIsRunInPlugin) {
            return;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mIsRunInPlugin) {
            mDexClassLoader = null;
            return;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mIsRunInPlugin) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mIsRunInPlugin) {
            return false;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        if (mIsRunInPlugin) {
            return false;
        }
        return super.onKeyMultiple(keyCode, repeatCount, event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (mIsRunInPlugin) {
            return;
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void openOptionsMenu() {
        if (mIsRunInPlugin) {
            mOutActivity.openOptionsMenu();
        } else {
            super.openOptionsMenu();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mIsRunInPlugin) {
            return false;
        } else {
            return super.onTouchEvent(e);
        }
    }

    @Override
    public Context getApplicationContext() {
        if (mIsRunInPlugin) {
            return mOutActivity.getApplicationContext();
        } else {
            return super.getApplicationContext();
        }
    }

    @Override
    public void finish() {
        if (mIsRunInPlugin) {
            int resultCode = Activity.RESULT_CANCELED;
            Intent data = null;
            synchronized (this) {
                Field field;
                try {
                    field = Activity.class.getDeclaredField("mResultCode");
                    field.setAccessible(true);
                    resultCode = (Integer) field.get(this);
                    field = Activity.class.getDeclaredField("mResultData");
                    field.setAccessible(true);
                    data = (Intent) field.get(this);
                } catch (Exception e) {
                }
            }
            mOutActivity.setResult(resultCode, data);
            mOutActivity.finish();
            mFinished = true;
        } else {
            super.finish();
        }
    }

    @Override
    public boolean isFinishing() {
        if (mIsRunInPlugin) {
            return mFinished;
        } else {
            return super.isFinishing();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mIsRunInPlugin) {
            return;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        if (mContext != null) {
            return LayoutInflater.from(mContext);
        } else {
            return LayoutInflater.from(mActivity);
        }
    }

    @Override
    public void overridePendingTransition(int enterAnim, int exitAnim) {
        if (mIsRunInPlugin) {
            mActivity.overridePendingTransition(enterAnim, exitAnim);
        } else {
            super.overridePendingTransition(enterAnim, exitAnim);
        }
    }

    @Override
    public Object getSystemService(String name) {
        if (WINDOW_SERVICE.equals(name) || SEARCH_SERVICE.equals(name)/* || LAYOUT_INFLATER_SERVICE.equals(name)*/) {
            if (mIsRunInPlugin) {
                return mOutActivity.getSystemService(name);
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
    public WindowManager getWindowManager() {
        if (mIsRunInPlugin) {
            return mOutActivity.getWindowManager();
        } else {
            return super.getWindowManager();
        }
    }

    @Override
    public int getChangingConfigurations() {
        if (mIsRunInPlugin) {
            return mOutActivity.getChangingConfigurations();
        } else {
            return super.getChangingConfigurations();
        }
    }

    @Override
    public Window getWindow() {
        if (mIsRunInPlugin) {
            return mOutActivity.getWindow();
        } else {
            return super.getWindow();
        }
    }

    @Override
    public void setTheme(int resid) {
        if (mIsRunInPlugin) {
            mOutActivity.setTheme(resid);
        } else {
            super.setTheme(resid);
        }
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        super.setRequestedOrientation(requestedOrientation);
    }

    @Override
    public String getPackageName() {
        if (mIsRunInPlugin) {
            return mPackageInfo.packageName;
        } else {
            return super.getPackageName();
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
    public void startActivityForResult(Intent intent, int requestCode) {
        if (mIsRunInPlugin) {
            intent.putExtra(PluginStatic.PARAM_IS_IN_PLUGIN, true);
            mActivity.startActivityForResult(intent, requestCode);
        } else {
            super.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public ComponentName startService(Intent service) {
        if(mIsRunInPlugin){
            service.putExtra(PluginStatic.PARAM_IS_IN_PLUGIN, true);
            return mActivity.startService(service);
        }else{
            return super.startService(service);
        }
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        if(mIsRunInPlugin){
            service.putExtra(PluginStatic.PARAM_IS_IN_PLUGIN, true);
            return mActivity.bindService(service, conn, flags);
        }else{
            return super.bindService(service, conn, flags);
        }
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        if(mIsRunInPlugin){
            mActivity.unbindService(conn);
        }else{
            super.unbindService(conn);
        }
    }
}
