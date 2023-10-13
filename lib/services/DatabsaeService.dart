import 'package:flutter/material.dart';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

import '../data/model/ModuleSettings.dart';

TimeOfDay parse(String value) {
  List<String> values = value.split(":");
  if(values.length < 2) {
    return TimeOfDay(hour: 0, minute: 0);
  }
  try {
    int hour = int.parse(values[0]);
    int minute = int.parse(values[1]);
    return TimeOfDay(hour: hour, minute: minute);
  }catch(_){
    return TimeOfDay(hour: 0, minute: 0);
  }
}

class DatabaseService {
  late final Database database;

  init() async {
    database = await openDatabase(
        join(await getDatabasesPath(), 'database.db'),
      onCreate: (db, version) {
        return db.execute(
          'CREATE TABLE module_settings(id INTEGER PRIMARY KEY AUTOINCREMENT, moduleId TEXT, fuelEmptyNotificationsEnabled INTEGER, pumpActivationScheduleEnabled INTEGER, pumpActivationTime TEXT, pumpShutdownScheduleEnabled INTEGER, pumpShutdownTime TEXT)',
        );
      },
      version: 1,
    );
  }

  Future<List<ModuleSettings>> moduleSettings() async {
    final List<Map<String, dynamic>> maps = await database.query('module_settings');

    return List.generate(maps.length, (i) {
      return ModuleSettings(
        moduleId: maps[i]['moduleId'],
        fuelEmptyNotificationsEnabled: maps[i]['fuelEmptyNotificationsEnabled']==1,
        pumpActivationScheduleEnabled: maps[i]['pumpActivationScheduleEnabled']==1,
        pumpShutdownScheduleEnabled: maps[i]['pumpShutdownScheduleEnabled']==1,
        pumpActivationTime: parse(maps[i]['pumpActivationTime']),
        pumpShutdownTime: parse(maps[i]['pumpShutdownTime']),
      );
    });
  }

  void deleteModuleSettings(int id) async {
    database.rawDelete("DELETE FROM module_settings WHERE id == $id");
  }

  Future<void> insertModuleSettings(ModuleSettings moduleSettings) async {
    int id = await database.insert("module_settings", moduleSettings.toMap());
    moduleSettings.id = id;
  }

  Future<void> updateModuleSettings(ModuleSettings moduleSettings) async {
    await database.update("module_settings", moduleSettings.toMap());
  }
}