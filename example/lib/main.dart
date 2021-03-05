import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:screenshot_callback/screenshot_callback.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _imagePath = 'Unknown';
  ScreenshotCallback _screenshotCallback;

  @override
  void initState() {
    super.initState();
    initCallback();
  }

  void initCallback(){
    _screenshotCallback = ScreenshotCallback();
    _screenshotCallback.startScreenshot();
  }

  @override
  void dispose() {
    // TODO: implement dispose
    super.dispose();
    _screenshotCallback.stopScreenshot();
  }


  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Image path: $_imagePath\n'),
        ),
      ),
    );
  }
}
