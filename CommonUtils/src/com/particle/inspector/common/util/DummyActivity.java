package com.particle.inspector.common.util;

import com.particle.inspector.common.utils.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

// A dummy activity for turn off screen light
public class DummyActivity extends Activity 
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dummy);
        
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.screenBrightness = 0.f;
    }
}
