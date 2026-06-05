package huangdihd.xinbot;

import lombok.Getter;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSwingPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundUseItemOnPacket;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.movement.Movement;

public class UseItemOnMovement extends Movement {

    @Getter
    private final Vector3i target;

    @Getter
    private final Direction direction;

    public UseItemOnMovement(Vector3i target, Direction direction) {
        this.target = target;
        this.direction = direction;
    }

    @Override
    public void init() {
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
    public void onTick() {
    }

    @Override
    public long getTime() {
        return 50;
    }

    @Override
    public void onStop() {
    }
}
