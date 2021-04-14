import 'dart:async';

import 'package:flutter/services.dart';

/// @FileName: flutter_screenshot_callback.dart
/// @Author: zhaozhebin
/// @Date: 2021/3/5
/// @Description:
/// 两个Flutter调用原生方法：开始监听、停止监听
/// 两个原生调用Flutter方法：截图成功回调、没有权限回调
class ScreenshotCallback {
  static const MethodChannel _channel =
      const MethodChannel('screenshot_callback');

  /// Flutter调用原生方法
  static const String FLUTTER_START_SCREENSHOT = "startListenScreenshot";
  static const String FLUTTER_STOP_SCREENSHOT = "stopListenScreenshot";

  /// 原生调用Flutter的方法
  static const String NATIVE_SCREENSHOT_CALLBACK = "screenshotCallback";
  static const String NATIVE_DENIED_PERMISSION = "deniedPermission";

  IScreenshotCallback _iScreenshotCallback;

  List<IScreenshotCallback> onCallbacks = <IScreenshotCallback>[];

  ScreenshotCallback() {
    _channel.setMethodCallHandler(methodCallHandler);
  }

  Future<dynamic> methodCallHandler(MethodCall call) async {
    /// 同样也是根据方法名分发不同的函数
    switch (call.method) {
      case NATIVE_SCREENSHOT_CALLBACK:
        {
          String path = call.arguments;
          print("截图回调，path是：$path");
          _iScreenshotCallback?.screenshotCallback(path);
        }
        break;
      case NATIVE_DENIED_PERMISSION:
        {
          print("截图回调，没有获取权限");
          _iScreenshotCallback?.deniedPermission();
        }
        break;
    }
    // return returnResult();
  }

  void setInterfaceScreenshotCallback(IScreenshotCallback iScreenshotCallback){
    _iScreenshotCallback = iScreenshotCallback;
  }

  void startScreenshot() async =>
    await _channel.invokeMethod(FLUTTER_START_SCREENSHOT);

  void stopScreenshot() async =>
      await _channel.invokeMethod(FLUTTER_STOP_SCREENSHOT);


}

abstract class IScreenshotCallback {
  screenshotCallback(String data);
  deniedPermission();
}