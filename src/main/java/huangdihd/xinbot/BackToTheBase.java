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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class BackToTheBase implements Plugin {
    private final Logger logger = LoggerFactory.getLogger(BackToTheBase.class.getSimpleName());
    public static BackToTheBase INSTANCE;
    public PlayerBaseConfig.BaseConfig baseConfig = new PlayerBaseConfig.BaseConfig();
    public Map<String, PlayerBaseConfig> playerConfigs = baseConfig.getPlayers();
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
        PlayerBaseConfig.BaseConfig defaultConfig = new PlayerBaseConfig.BaseConfig();
        Map<String, PlayerBaseConfig> players = new LinkedHashMap<>();
        players.put("example_name", new PlayerBaseConfig(List.of(new ButtonLocation("1", 0, 60, 0))));
        defaultConfig.setPlayers(players);
        defaultConfig.setReturnConfig(new PlayerBaseConfig.ReturnConfig());
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
                    setLoadedConfig(new PlayerBaseConfig.BaseConfig());
                }
                return false;
            }
            root = parsed.getAsJsonObject();
        } catch (JsonParseException e) {
            getLogger().error("Config file is not valid JSON. {}.", runtimeReload ? "Keeping previous config" : "No player configs loaded", e);
            if (!runtimeReload) {
                setLoadedConfig(new PlayerBaseConfig.BaseConfig());
            }
            return false;
        }

        LoadResult result = root.has("players")
                ? parseReadmeConfig(root)
                : parseLegacyConfig(root, runtimeReload);

        if (result == null || (runtimeReload && result.invalid)) {
            return false;
        }

        setLoadedConfig(result.config);
        if (result.changed && !runtimeReload) {
            writeConfig(configFile, baseConfig);
            getLogger().info("Saved migrated/validated {}", config_name);
        }
        return !result.invalid;
    }

    private LoadResult parseReadmeConfig(JsonObject root) {
        boolean changed = false;
        boolean invalid = false;

        JsonElement playersElement = root.get("players");
        if (playersElement == null || !playersElement.isJsonObject()) {
            getLogger().error("Config field players must be an object.");
            return new LoadResult(new PlayerBaseConfig.BaseConfig(), true, true);
        }

        Map<String, PlayerBaseConfig> players = new LinkedHashMap<>();
        JsonObject playersRoot = playersElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : playersRoot.entrySet()) {
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
                players.put(playerName, result.config);
            }
        }

        ReturnConfigResult returnResult = parseGlobalReturnConfig(root);
        changed |= returnResult.changed;
        invalid |= returnResult.invalid;

        PlayerBaseConfig.BaseConfig config = new PlayerBaseConfig.BaseConfig();
        config.setPlayers(players);
        config.setReturnConfig(returnResult.config);
        return new LoadResult(config, changed, invalid);
    }

    private LoadResult parseLegacyConfig(JsonObject root, boolean runtimeReload) {
        boolean changed = true;
        boolean invalid = false;
        Map<String, PlayerBaseConfig> players = new LinkedHashMap<>();
        LegacyReturnSelection legacyReturnSelection = new LegacyReturnSelection();

        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String playerName = entry.getKey();
            if (!entry.getValue().isJsonObject()) {
                getLogger().error("Config entry for {} must be an object.", playerName);
                invalid = true;
                continue;
            }

            JsonObject playerObj = entry.getValue().getAsJsonObject();
            if (hasCoordinate(playerObj)) {
                int x = getInt(playerObj, "x");
                int y = getInt(playerObj, "y");
                int z = getInt(playerObj, "z");
                getLogger().info("Migrating old simple config format for {}", playerName);
                players.put(playerName, new PlayerBaseConfig(List.of(new ButtonLocation("1", x, y, z))));
                legacyReturnSelection.acceptSimpleLocation(new PlayerBaseConfig.ReturnLocation(x, y, z));
                continue;
            }

            ConfigResult result = parsePlayerConfig(playerName, playerObj);
            changed |= result.changed;
            invalid |= result.invalid;
            if (result.config != null) {
                players.put(playerName, result.config);
            }

            ReturnConfigResult legacyReturnResult = parseLegacyReturnConfig(playerName, playerObj);
            invalid |= legacyReturnResult.invalid;
            if (legacyReturnResult.config != null) {
                legacyReturnSelection.acceptIntermediateConfig(playerName, legacyReturnResult.config);
            }
        }

        PlayerBaseConfig.BaseConfig config = new PlayerBaseConfig.BaseConfig();
        config.setPlayers(players);
        config.setReturnConfig(legacyReturnSelection.config);
        return new LoadResult(config, changed, invalid);
    }

    private ConfigResult parsePlayerConfig(String playerName, JsonObject obj) {
        if (!obj.has("locations") || !obj.get("locations").isJsonArray()) {
            getLogger().error("Config entry for {} must contain a locations array. Skipping.", playerName);
            return new ConfigResult(null, true, true);
        }

        boolean changed = false;
        boolean invalid = false;
        JsonArray locationsJson = obj.getAsJsonArray("locations");
        List<ButtonLocation> locations = new ArrayList<>();
        Set<String> seenNumbers = new HashSet<>();
        for (int i = 0; i < locationsJson.size(); i++) {
            JsonElement locationElement = locationsJson.get(i);
            if (!locationElement.isJsonObject()) {
                getLogger().error("Invalid location under {}: location must be an object. Skipping.", playerName);
                changed = true;
                invalid = true;
                continue;
            }
            JsonObject locationObj = locationElement.getAsJsonObject();
            ConfigValue<String> numberResult = getLocationNumber(playerName, locationObj, i);
            changed |= numberResult.changed;
            invalid |= numberResult.invalid;
            String number = numberResult.value;
            if (number == null) {
                changed = true;
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

        if (obj.has("returnAfterUse") || obj.has("returnLocation")) {
            changed = true;
        }
        return new ConfigResult(new PlayerBaseConfig(locations), changed, invalid);
    }

    private ReturnConfigResult parseGlobalReturnConfig(JsonObject root) {
        if (!root.has("return")) {
            getLogger().error("Config field return must be an object.");
            return new ReturnConfigResult(new PlayerBaseConfig.ReturnConfig(), true, true);
        }

        JsonElement returnElement = root.get("return");
        if (!returnElement.isJsonObject()) {
            getLogger().error("Config field return must be an object.");
            return new ReturnConfigResult(new PlayerBaseConfig.ReturnConfig(), true, true);
        }

        JsonObject returnObj = returnElement.getAsJsonObject();
        ConfigValue<Boolean> enabledResult = getBoolean("return", returnObj, "enabled", false, true);
        ReturnLocationResult locationResult = parseReturnLocation("return.location", returnObj.get("location"), true);

        PlayerBaseConfig.ReturnConfig returnConfig = new PlayerBaseConfig.ReturnConfig();
        returnConfig.setEnabled(enabledResult.value);
        if (locationResult.location != null) {
            returnConfig.setLocation(locationResult.location);
        }

        boolean changed = enabledResult.changed || locationResult.changed;
        boolean invalid = enabledResult.invalid || locationResult.invalid;
        return new ReturnConfigResult(returnConfig, changed, invalid);
    }

    private ReturnConfigResult parseLegacyReturnConfig(String playerName, JsonObject obj) {
        if (!obj.has("returnAfterUse") && !obj.has("returnLocation")) {
            return new ReturnConfigResult(null, false, false);
        }

        ConfigValue<Boolean> returnAfterUseResult = getBoolean(playerName, obj, "returnAfterUse", false, false);
        ReturnLocationResult returnLocationResult = parseReturnLocation("returnLocation for " + playerName, obj.get("returnLocation"), false);
        boolean invalid = returnAfterUseResult.invalid || returnLocationResult.invalid;

        if (returnLocationResult.location == null) {
            return new ReturnConfigResult(null, true, invalid);
        }

        PlayerBaseConfig.ReturnConfig returnConfig = new PlayerBaseConfig.ReturnConfig();
        returnConfig.setEnabled(returnAfterUseResult.value);
        returnConfig.setLocation(returnLocationResult.location);
        return new ReturnConfigResult(returnConfig, true, invalid);
    }

    private ConfigValue<String> getLocationNumber(String playerName, JsonObject locationObj, int index) {
        if (locationObj.has("number")) {
            if (!locationObj.get("number").isJsonPrimitive()) {
                getLogger().error("Location number under {} must be a positive integer string. Skipping.", playerName);
                return new ConfigValue<>(null, true, true);
            }
            return new ConfigValue<>(locationObj.get("number").getAsString(), false, false);
        }
        if (locationObj.has("priority")) {
            if (locationObj.get("priority").isJsonPrimitive()) {
                String priority = locationObj.get("priority").getAsString();
                if (isPositiveInteger(priority)) {
                    getLogger().warn("Location under {} is missing number. Using priority {} as number.", playerName, priority);
                    return new ConfigValue<>(priority, true, false);
                }
            }
            getLogger().error("Location priority under {} must be a positive integer. Skipping.", playerName);
            return new ConfigValue<>(null, true, true);
        }
        String number = String.valueOf(index + 1);
        getLogger().warn("Location under {} is missing number. Defaulting it to {}.", playerName, number);
        return new ConfigValue<>(number, true, false);
    }

    private ReturnLocationResult parseReturnLocation(String label, JsonElement element, boolean required) {
        if (element == null) {
            if (required) {
                getLogger().error("{} is required.", label);
            }
            return new ReturnLocationResult(null, true, required);
        }
        if (!element.isJsonObject() || !hasCoordinate(element.getAsJsonObject())) {
            getLogger().error("{} must be an object with x, y, and z coordinates.", label);
            return new ReturnLocationResult(null, true, true);
        }
        JsonObject returnObj = element.getAsJsonObject();
        return new ReturnLocationResult(new PlayerBaseConfig.ReturnLocation(
                getInt(returnObj, "x"),
                getInt(returnObj, "y"),
                getInt(returnObj, "z")
        ), false, false);
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

    private ConfigValue<Boolean> getBoolean(String owner, JsonObject obj, String key, boolean defaultValue, boolean required) {
        if (!obj.has(key)) {
            if (required) {
                getLogger().error("{} for {} is required.", key, owner);
            }
            return new ConfigValue<>(defaultValue, true, required);
        }
        JsonElement element = obj.get(key);
        if (!element.isJsonPrimitive()) {
            getLogger().warn("{} for {} must be true or false. Defaulting to {}.", key, owner, defaultValue);
            return new ConfigValue<>(defaultValue, true, true);
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isBoolean()) {
            getLogger().warn("{} for {} must be true or false. Defaulting to {}.", key, owner, defaultValue);
            return new ConfigValue<>(defaultValue, true, true);
        }
        return new ConfigValue<>(primitive.getAsBoolean(), false, false);
    }

    private boolean isPositiveInteger(String number) {
        return number != null && number.matches("[1-9][0-9]*");
    }

    private void setLoadedConfig(PlayerBaseConfig.BaseConfig config) {
        baseConfig = config;
        playerConfigs = config.getPlayers();
    }

    private void writeConfig(File configFile, PlayerBaseConfig.BaseConfig config) throws IOException {
        try (Writer writer = new FileWriter(configFile)) {
            gson.toJson(config, writer);
        }
    }

    private static class LoadResult {
        private final PlayerBaseConfig.BaseConfig config;
        private final boolean changed;
        private final boolean invalid;

        private LoadResult(PlayerBaseConfig.BaseConfig config, boolean changed, boolean invalid) {
            this.config = config;
            this.changed = changed;
            this.invalid = invalid;
        }
    }

    private static class ConfigResult {
        private final PlayerBaseConfig config;
        private final boolean changed;
        private final boolean invalid;

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
        private final PlayerBaseConfig.ReturnLocation location;
        private final boolean changed;
        private final boolean invalid;

        private ReturnLocationResult(PlayerBaseConfig.ReturnLocation location, boolean changed, boolean invalid) {
            this.location = location;
            this.changed = changed;
            this.invalid = invalid;
        }
    }

    private static class ReturnConfigResult {
        private final PlayerBaseConfig.ReturnConfig config;
        private final boolean changed;
        private final boolean invalid;

        private ReturnConfigResult(PlayerBaseConfig.ReturnConfig config, boolean changed, boolean invalid) {
            this.config = config;
            this.changed = changed;
            this.invalid = invalid;
        }
    }

    private class LegacyReturnSelection {
        private final PlayerBaseConfig.ReturnConfig config = new PlayerBaseConfig.ReturnConfig();
        private boolean selected;

        private void acceptSimpleLocation(PlayerBaseConfig.ReturnLocation location) {
            if (!selected) {
                config.setEnabled(false);
                config.setLocation(location);
                selected = true;
            }
        }

        private void acceptIntermediateConfig(String playerName, PlayerBaseConfig.ReturnConfig candidate) {
            if (!selected) {
                config.setEnabled(candidate.isEnabled());
                config.setLocation(candidate.getLocation());
                selected = true;
                return;
            }
            if (!sameReturnConfig(config, candidate)) {
                getLogger().warn(
                        "Multiple legacy player return configs were found. README format only supports one global return setting; ignoring return config for {}.",
                        playerName
                );
            }
        }

        private boolean sameReturnConfig(PlayerBaseConfig.ReturnConfig first, PlayerBaseConfig.ReturnConfig second) {
            PlayerBaseConfig.ReturnLocation firstLocation = first.getLocation();
            PlayerBaseConfig.ReturnLocation secondLocation = second.getLocation();
            return first.isEnabled() == second.isEnabled()
                    && firstLocation.getX() == secondLocation.getX()
                    && firstLocation.getY() == secondLocation.getY()
                    && firstLocation.getZ() == secondLocation.getZ();
        }
    }
}
