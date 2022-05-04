package com.semivanilla.griefpreventiontp.manager;

import com.semivanilla.griefpreventiontp.GriefPreventionTP;
import com.semivanilla.griefpreventiontp.object.TeleportRunnable;
import net.badbird5907.blib.util.StoredLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager implements Listener {
    private Map<UUID, TeleportRunnable> runnableMap = new ConcurrentHashMap<>();

    public TeleportManager(GriefPreventionTP plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void teleport(Player player, Location loc) {
        if (!GriefPreventionTP.getInstance().getConfig().getBoolean("teleport.warmup.enabled")) {
            player.teleport(loc);
            return;
        }
        TeleportRunnable runnable = new TeleportRunnable(player.getUniqueId(), loc, player.getLocation());
        runnableMap.put(player.getUniqueId(), runnable);
        MessageManager.sendMessage(player, "messages.teleporting");
        runnable.runTaskTimer(GriefPreventionTP.getInstance(), 0, 20);
    }

    public boolean cancelTeleport(UUID uuid) {
        TeleportRunnable runnable = runnableMap.remove(uuid);
        if (runnable != null) {
            runnable.cancel();
            return true;
        }
        return false;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (cancelTeleport(player.getUniqueId())) {
            MessageManager.sendMessage(player, "messages.damaged");
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        cancelTeleport(event.getPlayer().getUniqueId());
    }

    public Map<UUID, TeleportRunnable> getRunnableMap() {
        return runnableMap;
    }

    /*
    //Don't use this, it can cause lag, instead we'll use the teleport runnable and check if the player is still in the same location
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        cancelTeleport(event.getPlayer().getUniqueId());
    }
     */
}
