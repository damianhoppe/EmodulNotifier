import 'dart:convert';

import 'package:emodulnotifier/data/model/User.dart';
import 'package:emodulnotifier/pages/LoginPage.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:http/http.dart' as http;

import '../model/Module.dart';

const int PUMP_PARALLEL_PUMPS = 2;
const int PUMP_SUMMER_MODE = 3;

class EmoduleApi {
  static final EmoduleApi instance = EmoduleApi._internal();

  EmoduleApi._internal();

  factory EmoduleApi() => instance;

  Future<User> login(String login, String password) async {
    Uri uri = Uri.parse("https://emodul.eu/api/v1/authentication");
    var body = "{\"username\": \"$login\", \"password\": \"$password\"}";
    var response = await http.post(uri, body: body);

    if(response.statusCode == 401) {
      throw UnAuthenticatedException();
    }
    if(response.statusCode != 200) {
      throw Exception("Http error: ${response.statusCode}");
    }
    var decodedResponse = jsonDecode(utf8.decode(response.bodyBytes)) as Map;
    bool authenticated = decodedResponse["authenticated"] as bool;
    if(!authenticated) {
      throw UnAuthenticatedException();
    }
    String userId = decodedResponse["user_id"].toString();
    String token = decodedResponse["token"] as String;
    return User(userId, token);
  }

  Future<List<Module>> getModules(String token, String userId) async {
    if(token.isEmpty || userId.isEmpty) {
      throw UnAuthenticatedException();
    }
    Uri uri = Uri.parse("https://emodul.eu/api/v1/users/$userId/modules");
    var response = await http.get(uri, headers: {"Authorization": "Bearer $token"});
    if(response.statusCode == 401) {
      throw UnAuthenticatedException();
    }
    if (response.statusCode != 200) {
      throw Exception("Http error: ${response.statusCode}");
    }
    var modulesList = jsonDecode(utf8.decode(response.bodyBytes)) as List<dynamic>;
    List<Module> modules = [];
    for (var element in modulesList) {
      Module module;
      try {
        module = Module(element["udid"], element["name"]);
      }catch(_){ continue; }
      modules.add(module);
    }
    return modules;
  }

  Future<String> getModuleDetails(String token, String userId, String moduleId) async {
    if(token.isEmpty || userId.isEmpty) {
      throw UnAuthenticatedException();
    }
    assert(moduleId.isNotEmpty);
    Uri uri = Uri.parse("https://emodul.eu/api/v1/users/$userId/modules/$moduleId");
    var response = await http.get(uri, headers: {"Authorization": "Bearer $token"});
    if(response.statusCode == 401) {
      throw UnAuthenticatedException();
    }
    if (response.statusCode != 200) {
      throw Exception("Http error: ${response.statusCode}");
    }
    return utf8.decode(response.bodyBytes);
  }

  Future<int> getFuelSupply(String token, String userId, String moduleId) async {
    String jsonTextModuleDetails = await getModuleDetails(token, userId, moduleId);
    var moduleDetails = jsonDecode(jsonTextModuleDetails);
    var tiles = moduleDetails["tiles"];
    dynamic fuelSupplyTile;
    for(dynamic tile in tiles) {
      if(tile["id"] == 4060) {
        fuelSupplyTile = tile;
        break;
      }
    }
    return fuelSupplyTile["params"]["percentage"];
  }

  Future<void> setPumpMode(String token, String userId, String moduleId, int mode) async {
    if(token.isEmpty || userId.isEmpty) {
      throw UnAuthenticatedException();
    }
    assert(moduleId.isNotEmpty);
    Uri uri = Uri.parse("https://emodul.eu/api/v1/users/$userId/modules/$moduleId/menu/MU/ido/2006");
    var response = await http.post(uri, headers: {"Authorization": "Bearer $token"}, body: "{\"value\":$mode}");
    if(response.statusCode == 401) {
      throw UnAuthenticatedException();
    }
    if (response.statusCode != 200) {
      throw Exception("Http error: ${response.statusCode}");
    }
  }
}

class UnAuthenticatedException implements Exception {}

Future<T> fetch<T>(BuildContext context, Future<T> Function() function) async {
  try {
    return await function.call();
  }on UnAuthenticatedException catch(_) {
    Navigator.pushAndRemoveUntil(
        context,
        MaterialPageRoute(builder: (context) => const LoginPage()),
        (r) => false
    );
    rethrow;
  }catch(e) {
    rethrow;
  }
}