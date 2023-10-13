import 'package:flutter/material.dart';

import 'Module.dart';

class ModuleSettings {
  int id = 0;
  String moduleId;
  bool fuelEmptyNotificationsEnabled;
  bool pumpActivationScheduleEnabled;
  TimeOfDay pumpActivationTime;
  bool pumpShutdownScheduleEnabled;
  TimeOfDay pumpShutdownTime;

  ModuleSettings({
    required this.moduleId,
    required this.fuelEmptyNotificationsEnabled,
    required this.pumpActivationScheduleEnabled,
    required this.pumpShutdownScheduleEnabled,
    this.pumpActivationTime = const TimeOfDay(hour: 9, minute: 0),
    this.pumpShutdownTime = const TimeOfDay(hour: 21, minute: 0)
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'moduleId': moduleId,
      'fuelEmptyNotificationsEnabled': fuelEmptyNotificationsEnabled? 1 : 0,
      'pumpActivationScheduleEnabled': pumpActivationScheduleEnabled? 1 : 0,
      'pumpActivationTime': "${pumpActivationTime.hour}:${pumpActivationTime.minute}",
      'pumpShutdownScheduleEnabled': pumpShutdownScheduleEnabled? 1 : 0,
      'pumpShutdownTime': "${pumpShutdownTime.hour}:${pumpShutdownTime.minute}",
    };
  }
}