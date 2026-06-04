package huangdihd.xinbot;

import java.util.ArrayList;
import java.util.List;

public class PlayerBaseConfig {
    private List<ButtonLocation> locations = new ArrayList<>();
    private boolean returnAfterUse = false;
    private ReturnLocation returnLocation;

    public PlayerBaseConfig(List<ButtonLocation> locations, boolean returnAfterUse, ReturnLocation returnLocation) {
        this.locations = locations;
        this.returnAfterUse = returnAfterUse;
        this.returnLocation = returnLocation;
    }

    public List<ButtonLocation> getLocations() {
        return locations;
    }

    public boolean isReturnAfterUse() {
        return returnAfterUse;
    }

    public ReturnLocation getReturnLocation() {
        return returnLocation;
    }

    public ButtonLocation getLocation(String number) {
        for (ButtonLocation location : locations) {
            if (location.getNumber().equals(number)) {
                return location;
            }
        }
        return null;
    }
}
