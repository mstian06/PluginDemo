package com.tencent.PluginSDK;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;

import java.lang.reflect.Method;

class PluginContext extends ContextThemeWrapper {

	private AssetManager mAsset;
	private Resources mResources;
	private Theme mTheme;
	private int mThemeResId;
	private ClassLoader mClassLoader;
	private Context mOutContext;

	private AssetManager getSelfAssets(String apkPath) {
		AssetManager instance = null;
		try {
			instance = AssetManager.class.newInstance();
			Method addAssetPathMethod = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
			addAssetPathMethod.invoke(instance, apkPath);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return instance;
	}

	private Resources getSelfRes(Context ctx, AssetManager selfAsset) {
		DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
		Configuration con = ctx.getResources().getConfiguration();
		return new Resources(selfAsset, metrics, con);
	}

	private Theme getSelfTheme(Resources selfResources) {
		Theme theme = selfResources.newTheme();
		mThemeResId = getInnerRIdValue("com.android.internal.R.style.Theme");
		theme.applyStyle(mThemeResId, true);
		return theme;
	}
	
	

	private int getInnerRIdValue(String rStrnig) {
		int value = -1;
		try {
			int rindex = rStrnig.indexOf(".R.");
			String Rpath = rStrnig.substring(0, rindex + 2);
			int fieldIndex = rStrnig.lastIndexOf(".");
			String fieldName = rStrnig.substring(fieldIndex + 1, rStrnig.length());
			rStrnig = rStrnig.substring(0, fieldIndex);
			String type = rStrnig.substring(rStrnig.lastIndexOf(".") + 1, rStrnig.length());
			String className = Rpath + "$" + type;

			Class<?> cls = Class.forName(className);
			value = cls.getDeclaredField(fieldName).getInt(null);

		} catch (Throwable e) {
			e.printStackTrace();
		}
		return value;
	}


	public PluginContext(Context base, int themeres, String apkPath, ClassLoader classLoader) {
		super(base, themeres);
		mClassLoader = classLoader;
        mAsset = getSelfAssets(apkPath);
        mResources = getSelfRes(base, mAsset);
		mTheme = getSelfTheme(mResources);
		mOutContext = base;
	}

	@Override
	public Resources getResources() {
		return mResources;
	}

	@Override
	public AssetManager getAssets() {
		return mAsset;
	}

	@Override
	public Theme getTheme() {
		return mTheme;
	}

	@Override
	public ClassLoader getClassLoader() {
		if (mClassLoader != null) {
			return mClassLoader;
		}
		return super.getClassLoader();
	}

	public void setClassLoader(ClassLoader classLoader) {
		mClassLoader = classLoader;
	}

}
