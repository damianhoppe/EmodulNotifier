import 'package:emodulnotifier/pages/HomePage.dart';
import 'package:emodulnotifier/pages/LoginPage.dart';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

import 'App.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  App app = App();
  await app.init();
  if(await Permission.notification.isDenied) {
    await Permission.notification.request();
  }
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'EmodulNotifier',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: App().preferences.containsKey("token")? const HomePage() : const LoginPage(),
    );
  }
}