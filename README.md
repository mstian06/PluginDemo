Android插件框架的一种实现，plugin编译成普通的apk文件，主程序可以在不安装插件apk的情况下启动插件中的Activity以及访问插件中的资源。

具体的实现原理参考：[基于Proxy思想的Android插件框架](http://zjmdp.github.io/2014/07/22/a-plugin-framework-for-android/)



说明：
---
1. Plugin1, Plugin2可以作为单独的apk运行，但必须将PluginSDK生成的jar包打进apk内。
2. Plugin1, Plugin2作为插件，PluginHost作为宿主能调起Plugin1, Plugin2两个apak，则需要满足下面条件：
Plugin1, Plugin2使用PluginSDK.jar编译，但不要打包进apk内。详细可见.classpath文件。
```
作为引用，只编译使用，不打进apk内
<classpathentry kind="lib" path="D:/Code/Code_github/PluginDemo/PluginSDK/bin/pluginsdk.jar"/>
打进apk内
<classpathentry exported="true" kind="lib" path="D:/Code/Code_github/PluginDemo/PluginSDK/bin/pluginsdk.jar"/>
在eclipse中，properties->Java Build Path->Order and Export，在引用的jar包内选中即是打包进apk内，否则不打包进apk内[测试Android Dependencies的这种引用方式不勾选也会打包进apk内]。
```
将编译好的Plugin1.apk, Plugin2.apk拷贝到PluginHost代码下assert目录内，代码内调用使用。</p>
可以参见PluginSDK工程里PluginUtils#installPlugin()函数，会先将assert目录下插件包copy到/data/data/com.tencent.PluginDemo/app_plugins目录下。


调试log
---
0. DexClassloader在新Android 新api版本下
```
dexClassLoader = new DexClassLoader(apkFilePath, optimizedDexOutputPath, null, c.getClassLoader());
optimizedDexOutputPath只能是data/data目录下，否则会安全检查通不过，导致dexload失败。
```
1. 宿主调用插件，不能包含重复的jar包。
```
调试的时候出现错误log，如下:
02-26 15:47:01.801: W/dalvikvm(8053): Class resolved by unexpected DEX: Lcom/tencent/Plugin1/BaseActivity;(0x4301dbc0):0x75d54000 ref [Lcom/tencent/PluginSDK/BasePluginActivity;] Lcom/tencent/PluginSDK/BasePluginActivity;(0x42ecca38):0x751c0000
02-26 15:47:01.801: W/dalvikvm(8053): (Lcom/tencent/Plugin1/BaseActivity; had used a different Lcom/tencent/PluginSDK/BasePluginActivity; during pre-verification)
02-26 15:47:01.801: W/dalvikvm(8053): Unable to resolve superclass of Lcom/tencent/Plugin1/BaseActivity; (47)
02-26 15:47:01.801: W/dalvikvm(8053): Link of class 'Lcom/tencent/Plugin1/BaseActivity;' failed
02-26 15:47:01.801: W/dalvikvm(8053): Unable to resolve superclass of Lcom/tencent/Plugin1/SubActivity; (31)
02-26 15:47:01.801: W/dalvikvm(8053): Link of class 'Lcom/tencent/Plugin1/SubActivity;' failed
02-26 15:47:01.801: W/System.err(8053): java.lang.ClassNotFoundException: Didn't find class "com.tencent.Plugin1.SubActivity" on path: DexPathList[[zip file "/storage/emulated/0/PluginDemo_1.apk"],nativeLibraryDirectories=[/vendor/lib, /system/lib]]
02-26 15:47:01.801: W/System.err(8053): 	at dalvik.system.BaseDexClassLoader.findClass(BaseDexClassLoader.java:56)
02-26 15:47:01.801: W/System.err(8053): 	at java.lang.ClassLoader.loadClass(ClassLoader.java:497)
02-26 15:47:01.801: W/System.err(8053): 	at java.lang.ClassLoader.loadClass(ClassLoader.java:457)
02-26 15:47:01.801: W/System.err(8053): 	at com.tencent.PluginSDK.PluginProxyActivity.testToast2(PluginProxyActivity.java:117)
02-26 15:47:01.801: W/System.err(8053): 	at com.tencent.PluginSDK.PluginProxyActivity.onCreate(PluginProxyActivity.java:55)
02-26 15:47:01.801: W/System.err(8053): 	at android.app.Activity.performCreate(Activity.java:5249)
02-26 15:47:01.801: W/System.err(8053): 	at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1087)
02-26 15:47:01.801: W/System.err(8053): 	at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2154)
02-26 15:47:01.801: W/System.err(8053): 	at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2239)
02-26 15:47:01.801: W/System.err(8053): 	at android.app.ActivityThread.access$800(ActivityThread.java:141)
02-26 15:47:01.801: W/System.err(8053): 	at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1202)
02-26 15:47:01.801: W/System.err(8053): 	at android.os.Handler.dispatchMessage(Handler.java:102)
02-26 15:47:01.801: W/System.err(8053): 	at android.os.Looper.loop(Looper.java:136)
02-26 15:47:01.801: W/System.err(8053): 	at android.app.ActivityThread.main(ActivityThread.java:5047)
02-26 15:47:01.801: W/System.err(8053): 	at java.lang.reflect.Method.invokeNative(Native Method)
02-26 15:47:01.801: W/System.err(8053): 	at java.lang.reflect.Method.invoke(Method.java:515)
02-26 15:47:01.801: W/System.err(8053): 	at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:806)
02-26 15:47:01.801: W/System.err(8053): 	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:622)
02-26 15:47:01.801: W/System.err(8053): 	at dalvik.system.NativeStart.main(Native Method)
解决办法：
Class resolved by unexpected DEX;
http://stackoverflow.com/questions/22193668/class-resolved-by-unexpected-dex
The code is above (in my question). Assume that class A (in A.apk) invokes a remote method in class B (class B is in external B.apk file). Hence, class A is loader class and class B is loaded class. In my previous work, class B uses Gson library (gson.jar is exported and added in B's classpath) and class A also uses same Gson library (gson.jar is also added in A's classpath). That introduces the error: "had used a different Lcom/google/gson/Gson; during pre-verification". The solution is that, remove gson.jar in A.apk and used in B.apk only. Hope this will help you. –  Richard Le Jun 5 '14 at 10:02
But if remove gson.jar in A.apk, It will compile error. –  Trinea Jun 6 '14 at 1:59
Try to modify your code so that A.apk does not use gson.jar –  Richard Le Jun 6 '14 at 15:15
原因分析：
插件里和宿主里都包含了jar包文件，导致出现的错误，classloader的时候不能重复加载jar包文件。编译的时候，让插件不包含jar文件编译（private不勾选），这样使能够在宿主里运行，单独运行插件apk则需要将jar打包进apk。详细见代码。。。。
```

参考资料
---
```
农民伯伯
Android动态加载jar/dex
http://www.cnblogs.com/over140/archive/2011/11/23/2259367.html
Android应用开发提高系列（4）——Android动态加载（上）——加载未安装APK中的类
http://www.cnblogs.com/over140/archive/2012/03/29/2423116.html
android DexClassLoader动态加载技术详解
http://www.2cto.com/kf/201408/327363.html
Android App 如何动态加载类
http://blog.csdn.net/quaful/article/details/6096951 
基于Proxy思想的Android插件框架
http://www.tuicool.com/articles/mUZ3qq
代码： https://github.com/mstian06/PluginDemo[原作者Jamie的github上代码已删，这是从其他地方clone的]
```