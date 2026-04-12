package huangdihd.xinbot;

import lombok.Getter;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerRotPacket;
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
    private final org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction direction;

    public UseItemOnMovement(Vector3i target, org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction direction) {
        this.target = target;
        this.direction = direction;
    }

    @Override
    public void init() {
        Vector3d headPos = MovementSync.Instance.getHeadPosition();
        
        double targetX = this.target.getX() + 0.5;
        double targetY = this.target.getY() + 0.5;
        double targetZ = this.target.getZ() + 0.5;

        double dx = targetX - headPos.x;
        double dy = targetY - headPos.y;
        double dz = targetZ - headPos.z;
        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance > 6.0) {
            // Use MovementSync instance logger as Bot.Instance might not have a public logger getter
            MovementSync.Instance.getLogger().error("Distance to button is too far: " + distance);
            return;
        }

        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distanceXZ));

        // Update MS internal state for rotation
        MovementSync.Instance.yaw.set(yaw);
        MovementSync.Instance.pitch.set(pitch);

        // 1. Rotation Packet (Instant head turn)
        // 1.21.11 constructor per user instructions (onGround, horizontalCollision, yaw, pitch)
        // We use ServerboundMovePlayerRotPacket for the 4-parameter version
        Bot.Instance.getSession().send(new ServerboundMovePlayerRotPacket(
                MovementSync.Instance.onGround.get(),
                false, // horizontalCollision
                yaw,
                pitch
        ));

        // 2. Swing Packet
        Bot.Instance.getSession().send(new ServerboundSwingPacket(
                Hand.MAIN_HAND
        ));

        // 3. Use Item On Packet
        Bot.Instance.getSession().send(new ServerboundUseItemOnPacket(
                this.target,
                this.direction,
                Hand.MAIN_HAND,
                0.5f, 0.5f, 0.5f,
                false, // insideBlock
                false, // isHitWorldBorder
                BackToTheBase.Instance.getAndIncrementSequence()
        ));
    }

    @Override
    public void onTick() {
    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public void onStop() {
    }
}
