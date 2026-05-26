package huangdihd.xinbot;

import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction;
import org.joml.Vector3d;
import xin.bbtt.Block.BlockState;
import xin.bbtt.MovementSync;
import xin.bbtt.pathfinding.PathfindingContext;
import xin.bbtt.pathfinding.PathfindingContextBuilder;
import xin.bbtt.world.World;
import xin.bbtt.mcbot.Bot;
import xin.bbtt.mcbot.event.EventHandler;
import xin.bbtt.mcbot.event.Listener;
import xin.bbtt.mcbot.events.PrivateChatEvent;
import xin.bbtt.movements.LookAtMovement;
import xin.bbtt.movements.PathMovement;
import xin.bbtt.pathfinding.DStarLite;
import xin.bbtt.pathfinding.Node;

import java.util.List;

public class OnPrivateChat implements Listener {

    PathfindingContext pathfindingContext = new PathfindingContextBuilder(MovementSync.INSTANCE.getWorld()).addWalk().build();

    @EventHandler
    public void onBack(PrivateChatEvent event) {
        if (!BackToTheBase.INSTANCE.buttons.containsKey(event.getSender().getName())) return;
        if (!event.getMessage().startsWith("back")) return;
        
        Vector3i positionInt = BackToTheBase.INSTANCE.buttons.get(event.getSender().getName());
        Vector3d positionDouble = new Vector3d(positionInt.getX(), positionInt.getY(), positionInt.getZ()).add(new Vector3d(.5, .5, .5));
        
        if (!Bot.INSTANCE.getPluginManager().isPluginLoaded("MovementSync")) return;
        if (!(Bot.INSTANCE.getPluginManager().getPlugin("MovementSync").getPlugin() instanceof MovementSync movementSync)) return;
        
        World world = MovementSync.INSTANCE.getWorld();
        BlockState blockState = world.getBlockStateAt(positionDouble);
        if (!blockState.blockName().endsWith("button")) {
            BackToTheBase.INSTANCE.getLogger().error("Target block is not a button!");
            return;
        }

        Direction direction = Direction.valueOf(blockState.getProperty("facing").toUpperCase());
        xin.bbtt.world.Direction msDirection = xin.bbtt.world.Direction.valueOf(direction.name());
        
        // Directly add -0.5 * direction vector to point exactly at the button's face
        positionDouble.sub(msDirection.getVector(0.5));

        Vector3d currentPos = MovementSync.INSTANCE.position.get();

        // Check if the bot is already close enough to click the button (within ~4 blocks reach distance)
        double eyeY = currentPos.y + 1.62;
        Vector3d eyePos = new Vector3d(currentPos.x, eyeY, currentPos.z);
        double currentDistToButtonSq = Math.pow(currentPos.x - positionDouble.x, 2) + 
                                       Math.pow(eyeY - positionDouble.y, 2) + 
                                       Math.pow(currentPos.z - positionDouble.z, 2);

        if (currentDistToButtonSq <= 16.0 && world.canSee(eyePos, positionDouble)) {
            movementSync.getMovementController().addMovement(new LookAtMovement(positionDouble));
            movementSync.getMovementController().addMovement(new UseItemOnMovement(positionInt, direction));
            Bot.INSTANCE.sendChatMessage("已经触发" + event.getSender().getName() + "的滞留珍珠 by BackToTheBase");
            return;
        }

        Node start = new Node((int)Math.floor(currentPos.x), (int)Math.floor(currentPos.y), (int)Math.floor(currentPos.z));
        
        // Find a safe standing block near the button (radius 1~3.4), closest to the bot
        Node goal = null;
        double minBotDistSq = Double.MAX_VALUE;

        int targetY = (int) Math.floor(currentPos.y);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int cx = positionInt.getX() + dx;
                int cz = positionInt.getZ() + dz;
                int dy = targetY - positionInt.getY();

                if (!world.getBlockStateAt(new Vector3d(cx, targetY - 1, cz)).isSolid()) continue;
                if (!world.getBlockStateAt(new Vector3d(cx, targetY, cz)).isPassable()) continue;
                if (!world.getBlockStateAt(new Vector3d(cx, targetY + 1, cz)).isPassable())continue;

                double distToButtonSq = dx * dx + dy * dy + dz * dz;
                // Minimum distance of 1 (don't stand exactly inside the button) and max of 12 (approx radius 3.4)
                if (distToButtonSq >= 1 && distToButtonSq <= 12) {
                    // Verify line of sight from the candidate standing position
                    Vector3d standEyePos = new Vector3d(cx + 0.5, targetY + 1.62, cz + 0.5);
                    if (world.canSee(standEyePos, positionDouble)) {
                        double distToBotSq = Math.pow(cx - currentPos.x, 2) + Math.pow(targetY - currentPos.y, 2) + Math.pow(cz - currentPos.z, 2);
                        if (distToBotSq < minBotDistSq) {
                            minBotDistSq = distToBotSq;
                            goal = new Node(cx, targetY, cz);
                        }
                    }
                }
            }
        }

        if (goal == null) {
            BackToTheBase.INSTANCE.getLogger().error("Could not find a safe block to stand on near the button.");
            return;
        }

        DStarLite pathfinder = new DStarLite(start, goal, pathfindingContext);
        List<Node> path = pathfinder.findPath(5000);

        if (path != null && path.size() > 1) {
            MovementSync.INSTANCE.setActiveGoal(new org.joml.Vector3i(goal.x, goal.y, goal.z));
            movementSync.getMovementController().addMovement(new PathMovement(path));
        } else if (start.equals(goal)) {
            BackToTheBase.INSTANCE.getLogger().error("Could not find a path to the button.");
            return;
        }

        movementSync.getMovementController().addMovement(new LookAtMovement(positionDouble));
        movementSync.getMovementController().addMovement(new UseItemOnMovement(positionInt, direction));
        Bot.INSTANCE.sendChatMessage("已经触发" + event.getSender().getName() + "的滞留珍珠 by BackToTheBase");
    }
}
