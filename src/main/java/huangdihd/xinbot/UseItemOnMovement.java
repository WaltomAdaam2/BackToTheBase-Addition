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
        Bot.INSTANCE.getSession().send(new ServerboundSwingPacket(
                Hand.MAIN_HAND
        ));

        Bot.INSTANCE.getSession().send(new ServerboundUseItemOnPacket(
                this.target,
                this.direction,
                Hand.MAIN_HAND,
                0.5f, 0.5f, 0.5f,
                false, // insideBlock
                false, // isHitWorldBorder
                Bot.INSTANCE.getAndIncreaseSequence()
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
