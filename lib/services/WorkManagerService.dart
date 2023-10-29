import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:workmanager/workmanager.dart';

import '../App.dart';
import '../data/model/ModuleSettings.dart';
import '../data/remote/EmoduleApi.dart';

const String fuelEmptyNotificationTaskName = "FuelEmptyNotificationTask";
const String pumpActivationTaskName = "PumpActivationTask";
const String pumpShutdownTaskName = "PumpShutdownTask";

const String KEY_MODULE_SETTINGS_ID = "moduleSettingsId";
const String KEY_MODULE_ID = "moduleId";

enum TaskType {
  FuelCheck, PumpActivation, PumpShutdown;
}

TaskType? taskTypeFrom(String taskTypeName) {
  if(taskTypeName.startsWith(fuelEmptyNotificationTaskName)) {
    return TaskType.FuelCheck;
  }
  if(taskTypeName.startsWith(pumpActivationTaskName)) {
    return TaskType.PumpActivation;
  }
  if(taskTypeName.startsWith(pumpShutdownTaskName)) {
    return TaskType.PumpShutdown;
  }
  return null;
}

Duration calcDelay(TimeOfDay t1, TimeOfDay t2) {
  int diff = toMinutes(t2) - toMinutes(t1);
  if(diff < 0) {
    diff = 24*60 + diff;
  }
  return Duration(minutes: diff);
}

int toMinutes(TimeOfDay time) {
  return time.hour * 60 + time.minute;
}

@pragma('vm:entry-point')
void callbackDispatcher() {
  Workmanager().executeTask((task, inputData) async {
    try {
      int moduleSettingsId = inputData![KEY_MODULE_SETTINGS_ID];
      String moduleId = inputData![KEY_MODULE_ID];
      TaskType? taskType = taskTypeFrom(task);
      int notificationId = moduleSettingsId * 2;

      EmoduleApi api = EmoduleApi();
      SharedPreferences prefs = await SharedPreferences.getInstance();
      String token = prefs.getString("token")!;
      String userId = prefs.getString("userId")!;

      bool skipShowNotification = false;
      String title = "";
      String body = "";

      switch (taskType) {
        case TaskType.FuelCheck:
          int fuelSupply = await api.getFuelSupply(token, userId, moduleId);
          title = "Checking the fuel supply";
          if (fuelSupply < 20) {
            body = "Critical - $fuelSupply%";
          } else if (fuelSupply < 30) {
            body = "Be vigilant - $fuelSupply%";
          } else if (fuelSupply < 50) {
            body = "It's falling - $fuelSupply%";
          } else {
            body = "OK - $fuelSupply%";
          }
          if (fuelSupply > 30) {
            skipShowNotification = true;
          }
          break;
        case TaskType.PumpActivation:
          notificationId += 1;
          title = "Pump mode changed";
          try {
            await api.setPumpMode(token, userId, moduleId, PUMP_PARALLEL_PUMPS);
          } catch (_) {
            body = "Parallel pumps mode activation failed";
            break;
          }
          body = "Parallel pumps mode activated";
          break;
        case TaskType.PumpShutdown:
          notificationId += 1;
          title = "Pump mode changed";
          try {
            await api.setPumpMode(token, userId, moduleId, PUMP_SUMMER_MODE);
          } catch (_) {
            body = "Summer pump mode activation failed";
            break;
          }
          title = "Pump mode changed";
          body = "Summer pump mode activated";
          break;
        default:
          break;
      }

      if(!skipShowNotification) {
        await App().notificationService.showLocalNotification(
            id: notificationId,
            title: title,
            body: body,
            payload: ""
        );
      }
    }catch(_) {}
    return Future.value(true);
  });
}

class WorkmanagerService {

  init() async {
    Workmanager().initialize(
        callbackDispatcher,
        isInDebugMode: false
    );
    var modules = await App().databaseService.moduleSettings();
    for(ModuleSettings moduleSettings in modules) {
      if(moduleSettings.fuelEmptyNotificationsEnabled) {
        registerFuelEmptyNotificationTaskFor(moduleSettings);
      }
      if(moduleSettings.pumpActivationScheduleEnabled) {
        registerPumpActivationTaskFor(moduleSettings);
      }
      if(moduleSettings.pumpShutdownScheduleEnabled) {
        registerPumpShutdownTaskFor(moduleSettings);
      }
    }
  }

  String generateTaskName(List<String> taskNames) => taskNames.join("-");

  void registerFuelEmptyNotificationTaskFor(ModuleSettings moduleSettings) {
    String taskName = generateTaskName([fuelEmptyNotificationTaskName, moduleSettings.moduleId]);
    Workmanager().registerPeriodicTask(taskName, taskName,
      frequency: const Duration(hours: 1),
      existingWorkPolicy: ExistingWorkPolicy.keep,
      inputData: {KEY_MODULE_SETTINGS_ID: moduleSettings.id, KEY_MODULE_ID: moduleSettings.moduleId},
      constraints: Constraints(networkType: NetworkType.connected),
    );
  }

  void cancelFuelEmptyNotificationTaskFor(ModuleSettings moduleSettings) {
    Workmanager().cancelByUniqueName(generateTaskName([fuelEmptyNotificationTaskName, moduleSettings.moduleId]));
  }

  void registerPumpActivationTaskFor(ModuleSettings moduleSettings) {
    String taskName = generateTaskName([pumpActivationTaskName, moduleSettings.moduleId]);
    Workmanager().registerPeriodicTask(taskName, taskName,
      frequency: const Duration(days: 1),
      initialDelay: calcDelay(TimeOfDay.now(), moduleSettings.pumpActivationTime),
      existingWorkPolicy: ExistingWorkPolicy.update,
      inputData: {KEY_MODULE_SETTINGS_ID: moduleSettings.id, KEY_MODULE_ID: moduleSettings.moduleId},
      constraints: Constraints(networkType: NetworkType.connected),
    );
  }

  void cancelPumpActivationTaskFor(ModuleSettings moduleSettings) {
    Workmanager().cancelByUniqueName(generateTaskName([pumpActivationTaskName, moduleSettings.moduleId]));
  }

  void registerPumpShutdownTaskFor(ModuleSettings moduleSettings) {
    String taskName = generateTaskName([pumpShutdownTaskName, moduleSettings.moduleId]);
    Workmanager().registerPeriodicTask(taskName, taskName,
      frequency: const Duration(days: 1),
      initialDelay: calcDelay(TimeOfDay.now(), moduleSettings.pumpShutdownTime),
      existingWorkPolicy: ExistingWorkPolicy.update,
      inputData: {KEY_MODULE_SETTINGS_ID: moduleSettings.id, KEY_MODULE_ID: moduleSettings.moduleId},
      constraints: Constraints(networkType: NetworkType.connected),
    );
  }

  void cancelPumpShutdownTaskFor(ModuleSettings moduleSettings) {
    Workmanager().cancelByUniqueName(generateTaskName([pumpShutdownTaskName, moduleSettings.moduleId]));
  }
}