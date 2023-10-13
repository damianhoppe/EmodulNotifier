import 'package:emodulnotifier/services/DatabsaeService.dart';
import 'package:emodulnotifier/services/NotificationService.dart';
import 'package:emodulnotifier/services/WorkManagerService.dart';
import 'package:shared_preferences/shared_preferences.dart';

void showN() {
  App().notificationService.showLocalNotification(id: 0, title: "Title", body: "Body", payload: "Payload");
}

class App {
  static final App _appInstance = App._internal();

  App._internal();

  factory App() {
    return _appInstance;
  }

  late final SharedPreferences preferences;
  final NotificationService notificationService = NotificationService();
  final DatabaseService databaseService = DatabaseService();
  final WorkmanagerService workManagerService = WorkmanagerService();

  init() async {
    preferences = await SharedPreferences.getInstance();
    await databaseService.init();

    await notificationService.initializePlatformNotifications();
    workManagerService.init();
  }
}