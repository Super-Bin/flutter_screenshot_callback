#import "FlutterScreenshotCallbackPlugin.h"
#if __has_include(<flutter_screenshot_callback/flutter_screenshot_callback-Swift.h>)
#import <flutter_screenshot_callback/flutter_screenshot_callback-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_screenshot_callback-Swift.h"
#endif

@implementation FlutterScreenshotCallbackPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    [FlutterScreenshotCallbackPlugin registerWithRegistrar:registrar];
}
@end
