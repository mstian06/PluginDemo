package com.tencent.Plugin1;

import android.app.Activity;
import android.os.Bundle;

public class TestBActivity extends Activity implements ITest {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public String getMoney() {
        return "1234566";
    }

}