import 'package:emodulnotifier/data/model/ModuleSettings.dart';
import 'package:emodulnotifier/data/model/ModuleWithSettings.dart';
import 'package:emodulnotifier/data/remote/EmoduleApi.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import '../App.dart';
import '../data/model/Module.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  bool loading = false;
  List<ModuleWithSettings> modules = [];
  var app = App();
  var prefs = App().preferences;

  @override
  void initState() {
    _load();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Padding(
          padding: EdgeInsets.symmetric(vertical: 25, horizontal: 12),
          // child: TextButton(onPressed: () {showN()}, child: Text("Pokaz powiadomienie")),
          child: loading? _buildProgressIndicator() : _buildBody(),
        ),
      ),
    );
  }

  Widget _buildProgressIndicator() {
    return CircularProgressIndicator();
  }

  Widget _buildBody() {
    return ListView(
      shrinkWrap: true,
      children: List.generate(modules.length, (index) {
        return _buildModuleWidget(modules[index]);
      }),
    );
  }

  Widget _buildModuleWidget(ModuleWithSettings m) {
    return Column(
      children: [
        Text(m.module.name, style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text("Notify about low fuel supply"),
            Switch(value: m.settings.fuelEmptyNotificationsEnabled, onChanged: (value) {
              m.settings.fuelEmptyNotificationsEnabled = value;
              app.databaseService.updateModuleSettings(m.settings);
              setState(() {});
              if(value) {
                app.workManagerService.registerFuelEmptyNotificationTaskFor(m.settings);
              }else {
                app.workManagerService.cancelFuelEmptyNotificationTaskFor(m.settings);
              }
            })
          ],
        ),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text("Activate parallel pumps"),
            Switch(value: m.settings.pumpActivationScheduleEnabled, onChanged: (value) {
              m.settings.pumpActivationScheduleEnabled = value;
              app.databaseService.updateModuleSettings(m.settings);
              setState(() {});
              if(value) {
                app.workManagerService.registerPumpActivationTaskFor(m.settings);
              }else {
                app.workManagerService.cancelPumpActivationTaskFor(m.settings);
              }
            })
          ],
        ),
        Opacity(
          opacity: m.settings.pumpActivationScheduleEnabled? 1 : 0.5,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              TextButton(onPressed: () async {
                if(!m.settings.pumpActivationScheduleEnabled) {
                  return;
                }
                TimeOfDay? timeOfDay = await showTimePicker(context: context, initialTime: m.settings.pumpActivationTime);
                if(timeOfDay != null) {
                  m.settings.pumpActivationTime = timeOfDay;
                  app.databaseService.updateModuleSettings(m.settings);
                  setState(() {});
                }
                app.workManagerService.registerPumpActivationTaskFor(m.settings);
              }, child: Text(timeOfDay2String(m.settings.pumpActivationTime)))
            ],
          ),
        ),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text("Activate summer pump mode"),
            Switch(value: m.settings.pumpShutdownScheduleEnabled, onChanged: (value) {
              m.settings.pumpShutdownScheduleEnabled = value;
              app.databaseService.updateModuleSettings(m.settings);
              setState(() {});
              if(value) {
                app.workManagerService.registerPumpShutdownTaskFor(m.settings);
              }else {
                app.workManagerService.cancelPumpShutdownTaskFor(m.settings);
              }
            })
          ],
        ),
        Opacity(
          opacity: m.settings.pumpShutdownScheduleEnabled? 1 : 0.5,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              TextButton(onPressed: () async {
                if(!m.settings.pumpShutdownScheduleEnabled) {
                  return;
                }
                TimeOfDay? timeOfDay = await showTimePicker(context: context, initialTime: m.settings.pumpShutdownTime);
                if(timeOfDay != null) {
                  m.settings.pumpShutdownTime = timeOfDay;
                  app.databaseService.updateModuleSettings(m.settings);
                  setState(() {});
                }
                app.workManagerService.registerPumpShutdownTaskFor(m.settings);
              }, child: Text(timeOfDay2String(m.settings.pumpShutdownTime)))
            ],
          ),
        )
      ],
    );
  }

  _load() {
    if(loading) {
      return;
    }
    loading = true;
    () async {
      List<Module> modulesFromApi = [];
      try {
        modulesFromApi = await fetch(context, () =>
            EmoduleApi().getModules(prefs.getString("token") ?? "",
                prefs.getString("userId") ?? ""));
      }catch(_){}

      List<ModuleSettings> moduleSettings = await app.databaseService.moduleSettings();
      List<ModuleWithSettings> modulesWithSettings = [];

      for(ModuleSettings moduleSettings in moduleSettings) {
        int moduleIndex = modulesFromApi.indexWhere((e) => e.id == moduleSettings.moduleId);
        if(moduleIndex < 0) {
          app.databaseService.deleteModuleSettings(moduleSettings.id);
          continue;
        }
        Module module = modulesFromApi[moduleIndex];
        modulesFromApi.removeAt(moduleIndex);
        modulesWithSettings.add(ModuleWithSettings(module, moduleSettings));
      }
      for(Module module in modulesFromApi) {
        ModuleSettings moduleSettings = ModuleSettings(
            moduleId: module.id,
            fuelEmptyNotificationsEnabled: false,
            pumpActivationScheduleEnabled: false,
            pumpShutdownScheduleEnabled: false,
        );
        app.databaseService.insertModuleSettings(moduleSettings);
        modulesWithSettings.add(ModuleWithSettings(module, moduleSettings));
      }
      setState(() {
        modules = modulesWithSettings;
        loading = false;
      });
    }.call();
  }

  String timeOfDay2String(TimeOfDay t) {
    String hour = t.hour <= 9? "0${t.hour}" : t.hour.toString();
    String minute = t.minute <= 9? "0${t.minute}" : t.minute.toString();
    return "$hour:$minute";
  }
}