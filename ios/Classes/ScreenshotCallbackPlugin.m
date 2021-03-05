#import "ScreenshotCallbackPlugin.h"
#if __has_include(<screenshot_callback/screenshot_callback-Swift.h>)
#import <screenshot_callback/screenshot_callback-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "screenshot_callback-Swift.h"
#endif

@implementation ScreenshotCallbackPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftScreenshotCallbackPlugin registerWithRegistrar:registrar];
}
@end
