package com.tencent.PluginDemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.tencent.PluginSDK.PluginProxyActivity;
import com.tencent.PluginSDK.PluginStatic;
import com.tencent.PluginSDK.PluginUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity {

    private Button mButton1;
    private Button mButton2;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initView();
    }

    private void initView() {
        mButton1 = (Button)findViewById(R.id.button1);
        mButton2 = (Button)findViewById(R.id.button2);
        mButton1.setOnClickListener(mOnclick);
        mButton2.setOnClickListener(mOnclick);
    }

    private void startPlugin(String pluginName){
        PluginUtils.installPlugin(MainActivity.this, pluginName);

        Intent intent = new Intent(MainActivity.this, PluginProxyActivity.class);
        intent.putExtra(PluginStatic.PARAM_PLUGIN_NAME, pluginName);
        startActivity(intent);
    }

    View.OnClickListener mOnclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.button1:
                    startPlugin("Plugin1.apk");
                    break;
                case R.id.button2:
                    startPlugin("Plugin2.apk");
                    break;
                default:
                    break;
            }
        }
    };
}
