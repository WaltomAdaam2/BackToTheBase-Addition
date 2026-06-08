package huangdihd.xinbot;

import org.cloudburstmc.math.vector.Vector3i;

public class ButtonLocation {
    private String number;
    private int x;
    private int y;
    private int z;

    public ButtonLocation(String number, int x, int y, int z) {
        this.number = number;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getNumber() {
        return number;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Vector3i toVector3i() {
        return Vector3i.from(x, y, z);
    }
}
