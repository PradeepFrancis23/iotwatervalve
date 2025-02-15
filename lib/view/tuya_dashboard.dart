
import 'package:flutter/material.dart';
import 'package:smartwatervalve/service/native_services.dart';

class TuyaDashboard extends StatelessWidget {
  final NativeServices nativeServices = NativeServices();

  TuyaDashboard({super.key});

  @override
  Widget build(BuildContext context) {

    return Scaffold(
      appBar: AppBar(
        title: Text('Tuya Smart Dashboard'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('No devices added', style: TextStyle(fontSize: 18)),
            SizedBox(height: 20),
            SizedBox(height: 10),
            ElevatedButton(
              onPressed: nativeServices.searchDevices,
              child: Text('Add Device'),
            ),
          ],
        ),
      ),
    );
  }
}
