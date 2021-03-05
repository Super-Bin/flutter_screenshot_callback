package com.flutter.screenshot_callback;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * ScreenshotCallbackPlugin
 */
public class ScreenshotCallbackPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, ScreenShotListenManager.OnScreenShotListener {
  private static final String TAG = "ScreenshotCallback";
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private ScreenShotListenManager mScreenShotListenManager;
    private Activity mActivity;

    // Flutter调用方法
    private static final String NATIVE_START_SCREENSHOT = "startListenScreenshot";
    private static final String NATIVE_STOP_SCREENSHOT = "stopListenScreenshot";

    // native调用flutter方法
    private static final String NATIVE_SCREENSHOT_CALLBACK = "screenshotCallback";
    private static final String NATIVE_DENIED_PERMISSION = "deniedPermission";

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "screenshot_callback");
        channel.setMethodCallHandler(this);
        Log.i(TAG, "onAttachedToEngine");
    }


    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        HashMap<String, String> mapArgs = call.arguments();
        Log.i(TAG, "截图方法onMethodCall call.method =  " + call.method);
        switch (call.method) {
            case NATIVE_START_SCREENSHOT:
                mScreenShotListenManager.startListen();
                break;
            case NATIVE_STOP_SCREENSHOT:
                mScreenShotListenManager.stopListen();
                break;
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        Log.i(TAG, "onAttachedToActivity");
        mActivity = binding.getActivity();
        mScreenShotListenManager = new ScreenShotListenManager(mActivity);
        mScreenShotListenManager.setListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {
//    mActivity = null;
    }

    @Override
    public void onShot(String imagePath) {
        Log.i(TAG, "获取截图成功 = " + imagePath);
        channel.invokeMethod(NATIVE_SCREENSHOT_CALLBACK, imagePath);
    }

    @Override
    public void onScreenCapturedWithDeniedPermission() {
        Log.i(TAG, "没有权限");
        channel.invokeMethod(NATIVE_DENIED_PERMISSION, null);
    }
}
