package huangdihd.xinbot;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlayerBaseConfig {
    private List<ButtonLocation> locations = new ArrayList<>();

    public PlayerBaseConfig() {
    }

    public PlayerBaseConfig(List<ButtonLocation> locations) {
        this.locations = locations;
    }

    public List<ButtonLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<ButtonLocation> locations) {
        this.locations = locations;
    }

    public ButtonLocation getLocation(String number) {
        for (ButtonLocation location : locations) {
            if (location.getNumber().equals(number)) {
                return location;
            }
        }
        return null;
    }

    public static class BaseConfig {
        private Map<String, PlayerBaseConfig> players = new LinkedHashMap<>();
        @SerializedName("return")
        private ReturnConfig returnConfig = new ReturnConfig();

        public Map<String, PlayerBaseConfig> getPlayers() {
            return players;
        }

        public void setPlayers(Map<String, PlayerBaseConfig> players) {
            this.players = players;
        }

        public ReturnConfig getReturnConfig() {
            return returnConfig;
        }

        public void setReturnConfig(ReturnConfig returnConfig) {
            this.returnConfig = returnConfig;
        }
    }

    public static class ReturnConfig {
        private boolean enabled = false;
        private ReturnLocation location = new ReturnLocation();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public ReturnLocation getLocation() {
            return location;
        }

        public void setLocation(ReturnLocation location) {
            this.location = location;
        }
    }

    public static class ReturnLocation {
        private int x = 0;
        private int y = 60;
        private int z = 0;

        public ReturnLocation() {
        }

        public ReturnLocation(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getZ() {
            return z;
        }

        public void setZ(int z) {
            this.z = z;
        }
    }
}
