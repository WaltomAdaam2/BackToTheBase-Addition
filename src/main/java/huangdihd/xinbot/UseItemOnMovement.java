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
    private final float hitX;
    private final float hitY;
    private final float hitZ;

    public UseItemOnMovement(Vector3i target, Direction direction) {
        this(target, direction, 0.5f, 0.5f, 0.5f);
    }

    public UseItemOnMovement(Vector3i target, Direction direction, float hitX, float hitY, float hitZ) {
        this.target = target;
        this.direction = direction;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
    }

    @Override
    public void init() {
        Bot.INSTANCE.getSession().send(new ServerboundUseItemOnPacket(
                this.target,
                this.direction,
                Hand.MAIN_HAND,
                hitX, hitY, hitZ,
                false, // insideBlock
                false, // isHitWorldBorder
                Bot.INSTANCE.getAndIncreaseSequence()
        ));

        Bot.INSTANCE.getSession().send(new ServerboundSwingPacket(
                Hand.MAIN_HAND
        ));
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
