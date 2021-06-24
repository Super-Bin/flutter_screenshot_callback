import Flutter
import UIKit

public class SwiftFlutterScreenshotCallbackPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "screenshot_callback", binaryMessenger: registrar.messenger())
    NotificationCenter.default.addObserver(forName: UIApplication.userDidTakeScreenshotNotification, object: nil, queue: OperationQueue.main) { (noti: Notification) in
        let path = screenshot();
        print(path);
        channel.invokeMethod("screenshotCallback", arguments: path);
    }
    
    let instance = SwiftFlutterScreenshotCallbackPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
    static func screenshot() -> String{
//        let screenRect = UIScreen.main.bounds
//        UIGraphicsBeginImageContext(screenRect.size)
//        let ctx:CGContext = UIGraphicsGetCurrentContext()!
//        UIApplication.shared.keyWindow?.layer.render(in: ctx)
//        let image = UIGraphicsGetImageFromCurrentImageContext()
//        UIGraphicsEndImageContext();
        let image = UIApplication.shared.getScreenshot()
        guard let imageData = image?.pngData() as NSData? else { return"" }
        let documentPath = NSTemporaryDirectory();
        let path = documentPath + "screen_shot.png";
        if imageData.write(toFile: path, atomically: true) {
            print("保存成功")
            return path;
        } else {
            print("保存失败")
            return "";
        }
    
    }
}

extension UIApplication {
    func getScreenshot() -> UIImage? {
        guard let window = keyWindow else { return nil }
        let bounds = UIScreen.main.bounds
        UIGraphicsBeginImageContextWithOptions(bounds.size, false, 0)
        window.drawHierarchy(in: bounds, afterScreenUpdates: true)
        guard let image = UIGraphicsGetImageFromCurrentImageContext() else { return nil }
        UIGraphicsEndImageContext()
        return image
    }
}

