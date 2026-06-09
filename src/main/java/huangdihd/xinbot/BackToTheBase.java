package huangdihd.xinbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class BackToTheBase implements Plugin {
    private final Logger logger = LoggerFactory.getLogger(BackToTheBase.class.getSimpleName());
    public static BackToTheBase INSTANCE;
    public Map<String, PlayerBaseConfig> playerConfigs = new LinkedHashMap<>();
    public static final String config_name = "base_config.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
                writeDefaultConfig(configFile);
            } catch (IOException e) {
                getLogger().error("Failed to create config file", e);
                System.exit(-1);
            }
        }
        if (configFile.isFile()) {
            try {
                loadConfig(configFile, false);
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

    private void writeDefaultConfig(File configFile) throws IOException {
        Map<String, PlayerBaseConfig> defaultConfig = new LinkedHashMap<>();
        ButtonLocation defaultLocation = new ButtonLocation("1", 0, 60, 0);
        defaultConfig.put("example_name", new PlayerBaseConfig(
                List.of(defaultLocation),
                false,
                new ReturnLocation(defaultLocation.getX(), defaultLocation.getY(), defaultLocation.getZ())
        ));
        writeConfig(configFile, defaultConfig);
    }

    public synchronized boolean reloadConfig() {
        File configFile = new File(config_name);
        if (!configFile.isFile()) {
            getLogger().error("Cannot reload {} because the file does not exist or is not a file. Keeping previous config.", config_name);
            return false;
        }

        try {
            if (loadConfig(configFile, true)) {
                getLogger().info("Reloaded {}", config_name);
                return true;
            }
            getLogger().warn("Reload of {} failed validation. Keeping previous config.", config_name);
        } catch (IOException e) {
            getLogger().error("Failed to reload {}. Keeping previous config.", config_name, e);
        }
        return false;
    }

    private boolean loadConfig(File configFile, boolean runtimeReload) throws IOException {
        JsonObject root;
        try (Reader reader = new FileReader(configFile)) {
            JsonElement parsed = gson.fromJson(reader, JsonElement.class);
            if (parsed == null || !parsed.isJsonObject()) {
                getLogger().error("Config root must be a JSON object. {}.", runtimeReload ? "Keeping previous config" : "No player configs loaded");
                if (!runtimeReload) {
                    playerConfigs = new LinkedHashMap<>();
                }
                return false;
            }
            root = parsed.getAsJsonObject();
        } catch (JsonParseException e) {
            getLogger().error("Config file is not valid JSON. {}.", runtimeReload ? "Keeping previous config" : "No player configs loaded", e);
            if (!runtimeReload) {
                playerConfigs = new LinkedHashMap<>();
            }
            return false;
        }

        boolean changed = false;
        boolean invalid = false;
        Map<String, PlayerBaseConfig> loadedConfigs = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String playerName = entry.getKey();
            if (!entry.getValue().isJsonObject()) {
                getLogger().error("Config entry for {} must be an object.", playerName);
                changed = true;
                invalid = true;
                continue;
            }

            ConfigResult result = parsePlayerConfig(playerName, entry.getValue().getAsJsonObject());
            changed |= result.changed;
            invalid |= result.invalid;
            if (result.config != null) {
                loadedConfigs.put(playerName, result.config);
            }
        }

        if (runtimeReload && invalid) {
            return false;
        }

        playerConfigs = loadedConfigs;
        if (changed && !runtimeReload) {
            writeConfig(configFile, playerConfigs);
            getLogger().info("Saved migrated/validated {}", config_name);
        }
        return true;
    }

    private ConfigResult parsePlayerConfig(String playerName, JsonObject obj) {
        if (hasCoordinate(obj)) {
            int x = getInt(obj, "x");
            int y = getInt(obj, "y");
            int z = getInt(obj, "z");
            ButtonLocation location = new ButtonLocation("1", x, y, z);
            getLogger().info("Migrating old config format for {}", playerName);
            return new ConfigResult(new PlayerBaseConfig(
                    List.of(location),
                    false,
                    new ReturnLocation(x, y, z)
            ), true);
        }

        if (!obj.has("locations") || !obj.get("locations").isJsonArray()) {
            getLogger().error("Config entry for {} must contain old x/y/z fields or a locations array. Skipping.", playerName);
            return new ConfigResult(null, true, true);
        }

        boolean changed = false;
        boolean invalid = false;
        JsonArray locationsJson = obj.getAsJsonArray("locations");
        List<ButtonLocation> locations = new ArrayList<>();
        Set<String> seenNumbers = new HashSet<>();
        for (JsonElement locationElement : locationsJson) {
            if (!locationElement.isJsonObject()) {
                getLogger().error("Invalid location under {}: location must be an object. Skipping.", playerName);
                changed = true;
                invalid = true;
                continue;
            }
            JsonObject locationObj = locationElement.getAsJsonObject();
            if (!locationObj.has("number") && locationsJson.size() == 1) {
                changed = true;
            }
            String number = getLocationNumber(playerName, locationObj, locationsJson.size());
            if (number == null) {
                changed = true;
                invalid = true;
                continue;
            }
            if (!isPositiveInteger(number)) {
                getLogger().error("Invalid location number {} under {}. Number must be a positive integer starting from 1. Skipping.", number, playerName);
                changed = true;
                invalid = true;
                continue;
            }
            if (seenNumbers.contains(number)) {
                getLogger().warn("Duplicate location number {} under {}. Keeping the first one.", number, playerName);
                changed = true;
                invalid = true;
                continue;
            }
            if (!hasCoordinate(locationObj)) {
                getLogger().error("Location {} under {} must contain x, y, and z. Skipping.", number, playerName);
                changed = true;
                invalid = true;
                continue;
            }

            seenNumbers.add(number);
            locations.add(new ButtonLocation(
                    number,
                    getInt(locationObj, "x"),
                    getInt(locationObj, "y"),
                    getInt(locationObj, "z")
            ));
        }

        if (locations.isEmpty()) {
            getLogger().error("Config entry for {} has no valid locations. Skipping.", playerName);
            return new ConfigResult(null, true, true);
        }

        ConfigValue<Boolean> returnAfterUseResult = getBoolean(playerName, obj, "returnAfterUse");
        changed |= returnAfterUseResult.changed;
        invalid |= returnAfterUseResult.invalid;

        ReturnLocationResult returnLocationResult = parseReturnLocation(playerName, obj);
        changed |= returnLocationResult.changed;
        invalid |= returnLocationResult.invalid;

        boolean returnAfterUse = returnAfterUseResult.value;
        ReturnLocation returnLocation = returnLocationResult.location;
        if (returnLocation == null) {
            ButtonLocation defaultLocation = findDefaultLocation(locations);
            if (defaultLocation == null) {
                defaultLocation = locations.get(0);
                getLogger().warn("{} has no location number 1. returnLocation defaults to the first valid location.", playerName);
            }
            returnLocation = new ReturnLocation(defaultLocation.getX(), defaultLocation.getY(), defaultLocation.getZ());
            changed = true;
        }

        if (!obj.has("returnAfterUse") || !obj.has("returnLocation")) {
            changed = true;
        }

        return new ConfigResult(new PlayerBaseConfig(locations, returnAfterUse, returnLocation), changed, invalid);
    }

    private String getLocationNumber(String playerName, JsonObject locationObj, int locationCount) {
        if (!locationObj.has("number")) {
            if (locationCount == 1) {
                getLogger().warn("Location under {} is missing number. Defaulting it to 1.", playerName);
                return "1";
            }
            getLogger().error("A location under {} is missing number. Multiple locations require explicit numbers.", playerName);
            return null;
        }
        if (!locationObj.get("number").isJsonPrimitive()) {
            getLogger().error("Location number under {} must be a positive integer string. Skipping.", playerName);
            return null;
        }
        return locationObj.get("number").getAsString();
    }

    private ReturnLocationResult parseReturnLocation(String playerName, JsonObject obj) {
        if (!obj.has("returnLocation")) {
            return new ReturnLocationResult(null, false, false);
        }
        JsonElement element = obj.get("returnLocation");
        if (!element.isJsonObject() || !hasCoordinate(element.getAsJsonObject())) {
            getLogger().warn("returnLocation for {} is invalid. It will be defaulted on startup if possible.", playerName);
            return new ReturnLocationResult(null, true, true);
        }
        JsonObject returnObj = element.getAsJsonObject();
        return new ReturnLocationResult(new ReturnLocation(
                getInt(returnObj, "x"),
                getInt(returnObj, "y"),
                getInt(returnObj, "z")
        ), false, false);
    }

    private ButtonLocation findDefaultLocation(List<ButtonLocation> locations) {
        for (ButtonLocation location : locations) {
            if ("1".equals(location.getNumber())) {
                return location;
            }
        }
        return null;
    }

    private boolean hasCoordinate(JsonObject obj) {
        return getInt(obj, "x") != null && getInt(obj, "y") != null && getInt(obj, "z") != null;
    }

    private Integer getInt(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (NumberFormatException | UnsupportedOperationException | IllegalStateException e) {
            return null;
        }
    }

    private ConfigValue<Boolean> getBoolean(String playerName, JsonObject obj, String key) {
        if (!obj.has(key)) {
            return new ConfigValue<>(false, false, false);
        }
        JsonElement element = obj.get(key);
        if (!element.isJsonPrimitive()) {
            getLogger().warn("{} for {} must be true or false. Defaulting to false.", key, playerName);
            return new ConfigValue<>(false, true, true);
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isBoolean()) {
            getLogger().warn("{} for {} must be true or false. Defaulting to false.", key, playerName);
            return new ConfigValue<>(false, true, true);
        }
        return new ConfigValue<>(primitive.getAsBoolean(), false, false);
    }

    private boolean isPositiveInteger(String number) {
        return number != null && number.matches("[1-9][0-9]*");
    }

    private void writeConfig(File configFile, Map<String, PlayerBaseConfig> config) throws IOException {
        try (Writer writer = new FileWriter(configFile)) {
            gson.toJson(config, writer);
        }
    }

    private static class ConfigResult {
        private final PlayerBaseConfig config;
        private final boolean changed;
        private final boolean invalid;

        private ConfigResult(PlayerBaseConfig config, boolean changed) {
            this(config, changed, false);
        }

        private ConfigResult(PlayerBaseConfig config, boolean changed, boolean invalid) {
            this.config = config;
            this.changed = changed;
            this.invalid = invalid;
        }
    }

    private static class ConfigValue<T> {
        private final T value;
        private final boolean changed;
        private final boolean invalid;

        private ConfigValue(T value, boolean changed, boolean invalid) {
            this.value = value;
            this.changed = changed;
            this.invalid = invalid;
        }
    }

    private static class ReturnLocationResult {
        private final ReturnLocation location;
        private final boolean changed;
        private final boolean invalid;

        private ReturnLocationResult(ReturnLocation location, boolean changed, boolean invalid) {
            this.location = location;
            this.changed = changed;
            this.invalid = invalid;
        }
    }
}
