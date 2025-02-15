import 'package:flutter/material.dart';
import 'dart:async';

import 'package:smartwatervalve/view/tuya_login_screen.dart';

class SplashScreen extends StatefulWidget {
  @override
  _SplashScreenState createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    Timer(Duration(seconds: 3), () {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => TuyaLoginScreen()),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Text("TUYA",style: TextStyle(color: Colors.black,fontWeight: FontWeight.bold,fontSize: 50),), // Your logo here
      ),
    );
  }
}
