# screenshot_callback

A new Flutter plugin.

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter, view our
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

## Android需要获取存储权限才能正常使用

## 需要真机才能验证截图

## 更新flutter2，支持Null safety

# Flutter中使用注意
如果在flutter中重写 didChangeAppLifecycleState，其中在三星手机上，侧面截图功能会执行
resumed生命周期。
```
case AppLifecycleState.resumed: // 应用程序可见，前台
        _screenshotCallback.startScreenshot();
        break;
      case AppLifecycleState.paused: // 应用程序不可见，后台
        _screenshotCallback.stopScreenshot();
        break;
```

# 上传命令
有一个警告没关系
flutter packages pub publish --dry-run --server=https://pub.dartlang.org -v
flutter packages pub publish --server=https://pub.dartlang.org