package huangdihd.xinbot;

import org.cloudburstmc.math.vector.Vector3i;

public class ReturnLocation {
    private int x;
    private int y;
    private int z;

    public ReturnLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
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
