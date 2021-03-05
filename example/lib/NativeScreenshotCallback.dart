import 'package:screenshot_callback/screenshot_callback.dart';

class NativeScreenshotCallback implements IScreenshotCallback{
  @override
  deniedPermission() {
    print("没有权限");
  }

  @override
  screenshotCallback(String data) {
    print("获取截图文件路径 = $data");
  }

}