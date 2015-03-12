package com.example.testa;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import dalvik.system.DexClassLoader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestAActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
}