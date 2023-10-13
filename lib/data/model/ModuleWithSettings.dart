import 'Module.dart';
import 'ModuleSettings.dart';

class ModuleWithSettings {

    Module module;
    ModuleSettings settings;

    ModuleWithSettings(this.module, this.settings);

    Map<String, dynamic> toMap() {
        return settings.toMap();
    }
}