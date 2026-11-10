import dev.rice.module.impl.PlayerEspModule;
import dev.rice.module.RecoveredUiLayout;
import dev.rice.setting.*;
import dev.rice.config.SettingCodec;
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Verify the actual module and GUI match the OpenB4 settings definition. */
public final class OpenB4EspCheck {
    public static void main(String[] args) throws Exception {
        PlayerEspModule module = new PlayerEspModule();
        JsonArray settings = null;
        try (Reader reader = Files.newBufferedReader(Paths.get(args[0]))) {
            for (JsonElement element : new JsonParser().parse(reader).getAsJsonArray()) {
                JsonObject row = element.getAsJsonObject();
                if (row.has("name") && !row.get("name").isJsonNull() && row.get("name").getAsString().equals("PlayerESP")) settings = row.getAsJsonArray("settings");
            }
        }
        require(settings != null && settings.size() == module.getSettings().size(), "setting count mismatch");
        List<RecoveredUiLayout.Control> controls = RecoveredUiLayout.controls(module);
        require(controls.size() == settings.size(), "missing GUI controls");
        int index = 0;
        for (JsonElement element : settings) {
            JsonObject expected = element.getAsJsonObject();
            Setting<?> setting = module.getSettings().get(index);
            require(setting.getLabel().equals(expected.get("name").getAsString()), "setting label/order mismatch");
            require(controls.get(index++).setting == setting, "GUI order mismatch");
            if (setting instanceof ChoiceSetting) {
                ChoiceSetting choice = (ChoiceSetting) setting;
                require(choice.index() == expected.get("value").getAsInt(), "type default mismatch");
                List<String> labels = new ArrayList<>();
                for (JsonElement option : expected.getAsJsonArray("options")) labels.add(option.getAsString());
                require(labels.equals(choice.getOptions()), "mode list mismatch");
                for (int i = 0; i < labels.size(); i++) {
                    choice.set(i);
                    JsonElement encoded = SettingCodec.encode(choice);
                    choice.set((i + 1) % labels.size());
                    SettingCodec.decode(choice, encoded);
                    require(choice.index() == i, "mode roundtrip failed");
                }
            } else if (setting instanceof NumberSetting) {
                NumberSetting number = (NumberSetting) setting;
                require(number.doubleValue() == expected.get("value").getAsDouble(), "RGB default mismatch");
                require(number.getMinimum() == 0 && number.getMaximum() == 255 && number.getStep() == 1, "RGB bounds mismatch");
            } else {
                require(((BooleanSetting) setting).isEnabled() == expected.get("value").getAsBoolean(), "toggle default mismatch");
            }
            String before = setting.format();
            SettingCodec.decode(setting, SettingCodec.encode(setting));
            require(before.equals(setting.format()), "setting roundtrip failed");
        }
        require(module.findSetting("mode") == null && module.findSetting("hide_friendlies") == null, "old ESP options remain");
        System.out.println("PASS: all 11 OpenB4 settings/defaults, 8 modes, GUI controls and persistence");
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
