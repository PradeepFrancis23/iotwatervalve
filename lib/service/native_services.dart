import 'package:flutter/services.dart';

class NativeServices {
  static const MethodChannel _channel =
      MethodChannel('com.example.smartwatervalve');

  // Register a new user
  Future<String> registerUser(
      String countryCode, String email, String password) async {
    try {
      final String result = await _channel.invokeMethod('registerUser', {
        "countryCode": countryCode,
        "email": email,
        "password": password,
      });
      return result;
    } on PlatformException catch (e) {
      return "Error: ${e.message}";
    }
  }

  // login user
  Future<String> loginUser(
      String countryCode, String email, String password) async {
    try {
      final String result = await _channel.invokeMethod('loginUser', {
        "countryCode": countryCode,
        "email": email,
        "password": password,
      });
      return result;
    } on PlatformException catch (e) {
      return "Error: ${e.message}";
    }
  }

  Future<void> searchDevices() async {
    try {
      final String result = await _channel.invokeMethod('searchDevices');
      print("Device Search Result: $result");
    } on PlatformException catch (e) {
      print("Failed to search: '${e.message}'.");
    }
  }

  // Future<void> addWaterValve() async {
  //   try {
  //     final String result = await _channel.invokeMethod('addWaterValve');
  //     print("Add Water Valve Result: $result");
  //   } on PlatformException catch (e) {
  //     print("Failed to add valve: '${e.message}'.");
  //   }
  // }

  // Future<void> addLeakageSensor() async {
  //   try {
  //     final String result = await _channel.invokeMethod('addLeakageSensor');
  //     print("Add Leakage Sensor Result: $result");
  //   } on PlatformException catch (e) {
  //     print("Failed to add sensor: '${e.message}'.");
  //   }
  // }
}
