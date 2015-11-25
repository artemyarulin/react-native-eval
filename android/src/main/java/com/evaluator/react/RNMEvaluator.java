/**
 *  RNMEvaluator.java
 *  react-native-eval
 *
 *  Created by Andy Prock on 9/28/15.
 */

package com.evaluator.react;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * The NativeModule.
 */
public final class RNMEvaluator extends ReactContextBaseJavaModule {
    private static final String TAG = "RNMEvaluator";

    public static final String EXTRA_STRING = "com.evaluator.extra.STRING";
    public static final String EXTRA_ARRAY  = "com.evaluator.extra.ARRAY";
    public static final String EXTRA_MAP    = "com.evaluator.extra.MAP";
    public static final String EXTRA_INT    = "com.evaluator.extra.INT";
    public static final String EXTRA_DOUBLE = "com.evaluator.extra.DOUBLE";
    public static final String EXTRA_BOOL   = "com.evaluator.extra.BOOL";

    private static final String VALUE_KEY   = "_value";

    private static Map callbacks = new HashMap();

    private boolean mShuttingDown = false;

    public RNMEvaluator(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public void initialize() {
        mShuttingDown = false;
    }

    @Override
    public void onCatalystInstanceDestroy() {
        mShuttingDown = true;
        callbacks.clear();
    }

    /**
     * This broadcasts the given event via the LocalBroadcastManager
     *
     * @param event the name of the event to be broadcast.
     * @param value a {@link ReadableMap} containing the actual data to be
     * broadcast. The data is wrapped in the VALUE_KEY key of this map, and
     * is converted to a standard Java object or primitive, and put into the
     * Intent.
     */
    @ReactMethod
    public void emit(final String event, final ReadableMap value) {
        Intent intent = new Intent(event);

        if (value != null && value.hasKey(VALUE_KEY)); {
            Object data = ConversionUtil.toObject(value, VALUE_KEY);
            if (data instanceof HashMap) {
                intent.putExtra(EXTRA_MAP, (HashMap) data);
            } else if (data instanceof ArrayList) {
                intent.putExtra(EXTRA_ARRAY, (ArrayList) data);
            } else if (data instanceof String) {
                intent.putExtra(EXTRA_STRING, (String) data);
            } else if (data instanceof Integer) {
                intent.putExtra(EXTRA_INT, (Integer) data);
            } else if (data instanceof Double) {
                intent.putExtra(EXTRA_DOUBLE, (Double) data);
            } else if (data instanceof Boolean) {
                intent.putExtra(EXTRA_BOOL, (Boolean) data);
            } else {
                throw new IllegalArgumentException("Could not convert object with type: " + data.getClass().getCanonicalName() + ".");
            }
        }

        LocalBroadcastManager.getInstance(getReactApplicationContext()).sendBroadcast(intent);
    }

    /**
     * This calls the completion callback for the function that was invoked.
     *
     * @param callId the String identifying the call.
     * @param error a String representing the possible error.
     * @param value a {@link ReadableMap} containing the actual data to be
     * returned.  The data is wrapped in the VALUE_KEY key of this map, and
     * is converted to a standard Java object or primitive, and put into the
     * Intent.
     */
    @ReactMethod
    public void functionCallCompleted(final String callId, final String error, final ReadableMap returnValue) {
        if (!callbacks.containsKey(callId)) {
            return;
        }

        EvaluatorCallback cb = (EvaluatorCallback) callbacks.get(callId);
        cb.invoke(error, returnValue.hasKey(VALUE_KEY) ? ConversionUtil.toObject(returnValue, VALUE_KEY) : null);
        callbacks.remove(callId);
    }

    /**
     * Invokes a javascript function via RNMEvaluator.callSyncFunction (blocking)
     */
    public static void callSyncFunction(ReactContext context, String name, Object[] args, EvaluatorCallback cb) {
        RNMEvaluator.callFunction(context, name, args, cb, "RNMEvaluator.callSyncFunction");
    }

    /**
     * Invokes a javascript function via RNMEvaluator.callAsyncFunction (non-blocking)
     */
    public static void callAsyncFunction(ReactContext context, String name, Object[] args, EvaluatorCallback cb) {
        RNMEvaluator.callFunction(context, name, args, cb, "RNMEvaluator.callAsyncFunction");
    }

    /**
     * Marshalls a function call to the javascript layer, via our NativeModule.
     *
     * @param context The context needed to execute this in.
     * @param name The function to execute. e.g. "Math.Pow"
     * @param args The arguments to pass to the function, or null.
     * @param cb The completion callback for the result, or null.
     * @param event The name of the event that our NativeModule is listening for.
     */
    private static void callFunction(ReactContext context, String name, @Nullable Object[] args, @Nullable  EvaluatorCallback cb, String event) {
        String callId = UUID.randomUUID().toString();

        if (null != cb) {
            callbacks.put(callId, cb);
        }

        WritableArray arguments = args != null ? Arguments.fromJavaArgs(args) : Arguments.createArray();
        if (arguments.size() == 0) {
            arguments.pushNull();
        }

        WritableMap eventParams = Arguments.createMap();
        eventParams.putString("name", name);
        eventParams.putArray("args", arguments);
        eventParams.putString("callId", callId);

        // TODO: move to AppEventEmitter once App events are supported on android.
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event, eventParams);
    }

    /**
     * Callback interface.
     */
    public interface EvaluatorCallback {
        void invoke(String error, Object returnValue);
    }
}
