import 'package:emodulnotifier/data/remote/EmoduleApi.dart';
import 'package:emodulnotifier/pages/HomePage.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import '../App.dart';
import '../data/model/User.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final TextEditingController loginFieldController = TextEditingController();
  final TextEditingController passwordFieldController = TextEditingController();
  LoginConnectionState loginConnectionState = LoginConnectionState.NONE;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Padding(
          padding: EdgeInsets.symmetric(vertical: 25, horizontal: 12),
          child: ListView(
            shrinkWrap: true,
            children: [
              const Text(
                'Welcome',
                style: TextStyle(fontSize: 28),
              ),
              SizedBox(height: 28,),
              Text(loginConnectionState==LoginConnectionState.ERROR? "Incorrent login or password!" : "",
                style: TextStyle(fontSize: 16, color: Colors.redAccent),
              ),
              SizedBox(height: 4,),
              _textInput(context, hintText: "Login", controller: loginFieldController),
              _textInput(context, hintText: "Password", password: true, controller: passwordFieldController),
              SizedBox(height: 16,),
              Align(
                alignment: Alignment.centerRight,
                child: FilledButton(
                  child: AnimatedSize(
                    duration: Duration(milliseconds: 200),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text("Login"),
                        loginConnectionState==LoginConnectionState.CONNECTION? Padding(
                          padding: EdgeInsets.only(left: 12),
                          child: SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2,),
                          ),
                        ) : Container(),
                      ],
                    ),
                  ),
                  onPressed: () => _tryLogin()
                ),
              )
            ],
          ),
        ),
      ),
    );
  }

  _tryLogin() async {
    if(loginConnectionState == LoginConnectionState.CONNECTION)
      return;
    loginConnectionState = LoginConnectionState.CONNECTION;
    setState(() {});
    User user;
    try {
      user = await EmoduleApi().login(loginFieldController.text, passwordFieldController.text);
    }catch(e) {
      setState(() {
        loginConnectionState = LoginConnectionState.ERROR;
      });
      return;
    }
    App().preferences.setString("token", user.token);
    App().preferences.setString("userId", user.userId);
    setState(() {
      loginConnectionState = LoginConnectionState.NONE;
    });
    Navigator.pushAndRemoveUntil(
        context,
        MaterialPageRoute(builder: (context) => const HomePage()),
        (r) => false
    );
  }
}

Widget _textInput(BuildContext context, {required String hintText, TextEditingController? controller, bool password = false}) {
  return Padding(
    padding: EdgeInsets.symmetric(vertical: 6, horizontal: 0),
    child: TextField(
      controller: controller,
      keyboardType: password? TextInputType.visiblePassword : TextInputType.name,
      obscureText: password,
      decoration: InputDecoration(
          border: UnderlineInputBorder(),
          hintText: hintText
      ),
    ),
  );
}

enum LoginConnectionState {
  NONE, CONNECTION, ERROR;
}