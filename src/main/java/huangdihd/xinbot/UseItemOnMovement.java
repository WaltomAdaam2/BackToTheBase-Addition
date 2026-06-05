package huangdihd.xinbot;

import lombok.Getter;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSwingPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundUseItemOnPacket;
import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.movement.Movement;

public class UseItemOnMovement extends Movement {

    @Getter
    private final Vector3i target;

    @Getter
    private final Direction direction;

    private final Vector3d lookTarget;
    private int ticks = 0;
    private boolean used = false;

    public UseItemOnMovement(Vector3i target, Direction direction) {
        this(target, direction, null);
    }

    public UseItemOnMovement(Vector3i target, Direction direction, Vector3d lookTarget) {
        this.target = target;
        this.direction = direction;
        this.lookTarget = lookTarget;
    }

    @Override
    public void init() {
        if (lookTarget != null) {
            sendLookPacket(lookTarget);
        }
    }

    @Override
    public void onTick() {
        if (used) {
            setFinished(true);
            return;
        }

        ticks++;

        // Wait about one movement tick after sending the rotation packet.
        // This gives the server time to receive the new yaw/pitch before the right click packet.
        if (ticks < 2) {
            return;
        }

        Bot.INSTANCE.getSession().send(new ServerboundUseItemOnPacket(
                this.target,
                this.direction,
                Hand.MAIN_HAND,
                hitX(this.direction),
                hitY(this.direction),
                hitZ(this.direction),
                false,
                false,
                Bot.INSTANCE.getAndIncreaseSequence()
        ));

        Bot.INSTANCE.getSession().send(new ServerboundSwingPacket(
                Hand.MAIN_HAND
        ));

        used = true;
        setFinished(true);
    }

    private void sendLookPacket(Vector3d target) {
        Vector3d headPos = MovementSync.INSTANCE.getHeadPosition();
        Vector3d diff = new Vector3d(target).sub(headPos);

        double distanceXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float targetYaw = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
        float targetPitch = (float) Math.toDegrees(Math.atan2(-diff.y, distanceXZ));

        MovementSync.INSTANCE.yaw.set(targetYaw);
        MovementSync.INSTANCE.pitch.set(targetPitch);

        Vector3d pos = MovementSync.INSTANCE.position.get();

        Bot.INSTANCE.getSession().send(new ServerboundMovePlayerPosRotPacket(
                MovementSync.INSTANCE.onGround.get(),
                false,
                pos.x,
                pos.y,
                pos.z,
                targetYaw,
                targetPitch
        ));
    }

    private float hitX(Direction direction) {
        return switch (direction) {
            case WEST -> 0.0f;
            case EAST -> 1.0f;
            default -> 0.5f;
        };
    }

    private float hitY(Direction direction) {
        return switch (direction) {
            case DOWN -> 0.0f;
            case UP -> 1.0f;
            default -> 0.5f;
        };
    }

    private float hitZ(Direction direction) {
        return switch (direction) {
            case NORTH -> 0.0f;
            case SOUTH -> 1.0f;
            default -> 0.5f;
        };
    }

    @Override
    public long getTime() {
        return 1000;
    }

    @Override
    public void onStop() {
    }
}
