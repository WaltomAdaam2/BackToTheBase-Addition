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
    private boolean rotated = false;
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
    }

    @Override
    public void onTick() {
        if (used) {
            setFinished(true);
            return;
        }
        if (lookTarget != null && !rotated) {
            sendRotation();
            rotated = true;
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

    private void sendRotation() {
        Vector3d headPosition = MovementSync.INSTANCE.getHeadPosition();
        Vector3d delta = new Vector3d(lookTarget).sub(headPosition);
        double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-delta.y, horizontalDistance));
        Vector3d position = MovementSync.INSTANCE.position.get();
        boolean onGround = MovementSync.INSTANCE.onGround.get();

        MovementSync.INSTANCE.yaw.set(yaw);
        MovementSync.INSTANCE.pitch.set(pitch);
        Bot.INSTANCE.getSession().send(new ServerboundMovePlayerPosRotPacket(
                onGround,
                false,
                position.x,
                position.y,
                position.z,
                yaw,
                pitch
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
