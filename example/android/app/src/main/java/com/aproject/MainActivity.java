package com.aproject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.facebook.react.LifecycleState;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.NotThreadSafeBridgeIdleDebugListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.react.uimanager.AppRegistry;

import com.evaluator.react.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity implements DefaultHardwareBackBtnHandler {

    private ReactInstanceManager mReactInstanceManager;
    private TextView flavor_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setBundleAssetName("index.android.bundle")
                .setJSMainModuleName("index.android")
                .addPackage(new MainReactPackage())
                .addPackage(new RNMEPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();

        try {
            Method method = getReactContextInitialize();
            method.setAccessible(true);
            method.invoke(mReactInstanceManager);
            method.setAccessible(false);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);

        flavor_text = (TextView) findViewById(R.id.flavor_text);
    }

    private Method getReactContextInitialize() {
        Method method;
        try {
            // RN 14
            method = mReactInstanceManager.getClass().getDeclaredMethod("initializeReactContext");
        } catch (NoSuchMethodException e) {
            method = null;
        }

        if (method == null) {
            try {
                // RN 15
                method = mReactInstanceManager.getClass().getDeclaredMethod("createReactContextInBackground");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return method;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && mReactInstanceManager != null) {
            mReactInstanceManager.showDevOptionsDialog();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void invokeDefaultOnBackPressed() {
      super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onPause();
        }

        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onResume(this);
        }

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
            new IntentFilter("loaded"));
    }

    private void updateFlavorText(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                flavor_text.setText(message);
            }
        });
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcast
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Map<String, Object> value = (Map<String, Object>) intent.getSerializableExtra(RNMEvaluator.EXTRA_MAP);
            Log.d("received", "Got message: " + value.get("message"));

            final StringBuilder sb = new StringBuilder((String) value.get("message"));

            updateFlavorText(sb.toString());

            ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();

            // Perform action on click
            Integer[] args = new Integer[2];
            args[0] = new Integer(2);
            args[1] = new Integer(2);
            RNMEvaluator.callSyncFunction(reactContext, "Math.pow", args, new RNMEvaluator.EvaluatorCallback() {
                @Override
                public void invoke(String error, Object returnValue) {
                    if (error != null) {
                        sb.append('\n' + "Error occured: " + error);
                        Log.e("MAIN", "Error occured: " + error);
                    } else {
                        sb.append('\n' + "Function returned: " + returnValue);
                        Log.i("MAIN", "Function returned: " + returnValue);
                    }

                    updateFlavorText(sb.toString());
                }
            });
        }
    };

    public void onClick(View v) {
        ReactContext context = mReactInstanceManager.getCurrentReactContext();
        if (context == null) {
            Log.e("MAIN", "React Context is not ready!");
            return;
        }

        RNMEvaluator.callSyncFunction(context, "AProject.load", null, null);
    }
}
