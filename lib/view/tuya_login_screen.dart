import 'package:flutter/material.dart';
import 'package:smartwatervalve/view/tuya_dashboard.dart';

import '../service/native_services.dart';
// Redirect to main dashboard

class TuyaLoginScreen extends StatefulWidget {
  const TuyaLoginScreen({super.key});

  @override
  _TuyaLoginScreen createState() => _TuyaLoginScreen();
}

class _TuyaLoginScreen extends State<TuyaLoginScreen> {
  final NativeServices _tuyaService = NativeServices();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  final String _countryCode = "91"; // Default country code (India)

  String _statusMessage = "";

  void _registerUser() async {
    String email = _emailController.text.trim();
    String password = _passwordController.text.trim();
    if (email.isEmpty || password.isEmpty) {
      setState(() => _statusMessage = "Please enter email and password.");
      return;
    }

    setState(() => _statusMessage = "Registering...");
    String result =
        await _tuyaService.registerUser(_countryCode, email, password);
    setState(() => _statusMessage = result);

    if (result.contains("successful")) {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => TuyaLoginScreen()),
      );
      // _loginUser();
    }
  }

  void _loginUser() async {
    String email = _emailController.text.trim();
    String password = _passwordController.text.trim();

    setState(() => _statusMessage = "Logging in...");
    String result = await _tuyaService.loginUser(_countryCode, email, password);
    setState(() => _statusMessage = result);

    if (result.contains("successful")) {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => TuyaDashboard()),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Tuya Smart Login")),
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(_statusMessage, style: TextStyle(color: Colors.red)),
            TextField(
              controller: _emailController,
              decoration: InputDecoration(labelText: "Email"),
            ),
            TextField(
              controller: _passwordController,
              decoration: InputDecoration(labelText: "Password"),
              obscureText: true,
            ),
            SizedBox(height: 20),
            ElevatedButton(
              onPressed: _registerUser,
              child: Text("Register"),
            ),
            SizedBox(height: 10),
            ElevatedButton(
              onPressed: _loginUser,
              child: Text("Login"),
            ),
          ],
        ),
      ),
    );
  }
}
