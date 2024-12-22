package dev.badbird.griefpreventiontp.object;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

@RequiredArgsConstructor
public class TeleportRunnable extends BukkitRunnable {
    private final UUID uuid;
    private final Location to, from;
    private final int cost;

    private int countdown = GriefPreventionTP.getInstance().getConfig().getInt("teleport.warmup.seconds"); //TODO implement cooldowns


    @Override
    public void run() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            cancel();
            return;
        }
        if (hasMoved()) {
            cancel();
            MessageManager.sendMessage(player, "messages.moved");
            return;
        }
        if (countdown <= 0) {
            if (cost > 0 && !GriefPreventionTP.getInstance().getClaimManager().playerHasEnough(player, cost)) {
                MessageManager.sendMessage(player, "messages.not-enough-money.tp");
                return;
            }
            player.teleport(to);
            MessageManager.sendMessage(player, "messages.teleported");
            cancel();
            return;
        }
        countdown--;
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        GriefPreventionTP.getInstance().getTeleportManager().getRunnableMap().remove(uuid);
        super.cancel();
    }

    public boolean hasMoved() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return false;
        if (player.getLocation().getWorld() != from.getWorld()) return true;
        return player.getLocation().distance(from) > 0.5;
        /*

        int x = (int) player.getLocation().getX();
        int y = (int) player.getLocation().getY();
        int z = (int) player.getLocation().getZ();
        int fromX = (int) from.getX();
        int fromY = (int) from.getY();
        int fromZ = (int) from.getZ();

        //return Math.abs(x - fromX) > 1 || Math.abs(y - fromY) > 1 || Math.abs(z - fromZ) > 1; //lol im overthinking this
        return x != fromX || y != fromY || z != fromZ;
         */
    }
}
