package huangdihd.xinbot;

import org.cloudburstmc.math.vector.Vector3i;
import org.joml.Vector3d;
import xin.bbtt.MovementSync;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.event.EventHandler;
import xin.bbtt.mcbot.event.Listener;
import xin.bbtt.mcbot.events.PrivateChatEvent;
import xin.bbtt.movements.LookAtMovement;

public class OnPrivateChat implements Listener {

    @EventHandler
    public void onBack(PrivateChatEvent event) {
        if (!BackToTheBase.INSTANCE.buttons.containsKey(event.getSender().getName())) return;
        if (!event.getMessage().startsWith("back")) return;
        Button button = BackToTheBase.INSTANCE.buttons.get(event.getSender().getName());
        Vector3i positionInt = Vector3i.from(button.x, button.y, button.z);
        Vector3d positionDouble = new Vector3d(button.x, button.y, button.z).add(new Vector3d(.5, .5, .5));
        if (!Bot.INSTANCE.getPluginManager().isPluginLoaded("MovementSync")) return;
        if (!(Bot.INSTANCE.getPluginManager().getPlugin("MovementSync").getPlugin() instanceof MovementSync movementSync)) return;
        movementSync.getMovementController().addMovement(new LookAtMovement(positionDouble));
        movementSync.getMovementController().addMovement(new UseItemOnMovement(positionInt, button.direction));
        Bot.INSTANCE.sendChatMessage("已经触发" + event.getSender().getName() + "的滞留珍珠 by BackToTheBase");
    }
}
