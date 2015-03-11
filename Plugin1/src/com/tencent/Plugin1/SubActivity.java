package com.tencent.Plugin1;

import android.os.Bundle;

/**
 * Created by jamie on 14-6-4.
 */
public class SubActivity extends BaseActivity implements ITest {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_activity);
    }

    /* (non-Javadoc)
     * @see com.tencent.Plugin1.ITest#getMoney()
     */
    @Override
    public String getMoney() {
        // TODO Auto-generated method stub
        return "subactivity";
    }
}
