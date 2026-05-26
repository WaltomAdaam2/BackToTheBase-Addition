package huangdihd.xinbot;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.cloudburstmc.math.vector.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

@Getter
public class BackToTheBase implements Plugin {
    private final Logger logger = LoggerFactory.getLogger(BackToTheBase.class.getSimpleName());
    public static BackToTheBase INSTANCE;
    public Map<String, Vector3i> buttons;
    public static final String config_name = "base_config.json";

    @Override
    public void onLoad() {
        INSTANCE = this;

        File configFile = new File(config_name);
        if (!configFile.exists()) {
            try {
                if (!configFile.createNewFile()) {
                    getLogger().error("Failed to create config file");
                    System.exit(-1);
                }
                try (FileWriter writer = new FileWriter(configFile)) {
                    writer.write("{}");
                }
            } catch (IOException e) {
                getLogger().error("Failed to create config file", e);
                System.exit(-1);
            }
        }
        if (configFile.isFile()) {
            try {
                Gson gson = new GsonBuilder().registerTypeAdapter(Vector3i.class, (JsonDeserializer<Vector3i>) (json, typeOfT, context) -> {
                    JsonObject obj = json.getAsJsonObject();
                    return Vector3i.from(
                            obj.get("x").getAsInt(),
                            obj.get("y").getAsInt(),
                            obj.get("z").getAsInt()
                    );
                }).create();
                Type type = new TypeToken<Map<String, Vector3i>>() {}.getType();
                FileReader reader = new FileReader(configFile);
                buttons = gson.fromJson(reader, type);
                reader.close();
            } catch (IOException e) {
                getLogger().error("Failed to read config file", e);
                System.exit(-1);
            }
        }
    }

    @Override
    public void onUnload() {

    }

    @Override
    public void onEnable() {
        Bot.INSTANCE.getPluginManager().events().registerEvents(new OnPrivateChat(), this);
    }

    @Override
    public void onDisable() {

    }
}
