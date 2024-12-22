package dev.badbird.griefpreventiontp.manager;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.object.TeleportRunnable;
import net.badbird5907.blib.objects.tuple.Pair;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TeleportManager implements Listener {
    private Map<UUID, TeleportRunnable> runnableMap = new ConcurrentHashMap<>();
    private List<Pair<String, Integer>> tpCost = new CopyOnWriteArrayList<>();

    private boolean enableTPCost = false;

    public TeleportManager(GriefPreventionTP plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        enableTPCost = GriefPreventionTP.getInstance().getConfig().getBoolean("vault-integration.tp-cost.enabled", false);

        if (enableTPCost) {
            Map<String, Object> map = GriefPreventionTP.getInstance().getConfig().getConfigurationSection("vault-integration.tp-cost.groups").getValues(false);
            for (Map.Entry<String, Object> stringObjectEntry : map.entrySet()) {
                String k = stringObjectEntry.getKey();
                Object v = stringObjectEntry.getValue();
                int i = -1;
                if (v instanceof String) {
                    i = Integer.parseInt((String) v);
                } else if (v instanceof Integer) {
                    i = (Integer) v;
                }
                tpCost.add(new Pair<>(k, i));
            }
        }
    }

    public void teleport(Player player, Location loc) {
        if (GriefPreventionTP.getInstance().getConfig().getBoolean("teleport.check-tp-location.enabled", true) && !isSafeLocation(loc)) {
            MessageManager.sendMessage(player, "teleport.check-tp-location.message");
            return;
        }
        int cost = getTPCost(player);
        if (cost > 0 && !GriefPreventionTP.getInstance().getClaimManager().playerHasEnough(player, cost)) {
            MessageManager.sendMessage(player, "messages.not-enough-money.tp");
            return;
        }
        if (!GriefPreventionTP.getInstance().getConfig().getBoolean("teleport.warmup.enabled")) {
            if (cost > 0 && !GriefPreventionTP.getInstance().getClaimManager().withdrawPlayer(player, cost)) {
                MessageManager.sendMessage(player, "messages.not-enough-money.tp");
                return;
            }
            player.teleport(loc);
            return;
        }
        if (runnableMap.containsKey(player.getUniqueId())) {
            MessageManager.sendMessage(player, "messages.already-teleporting");
            return;
        }
        if (player.hasPermission("gptp.bypass.warmup")) {
            MessageManager.sendMessage(player, "messages.teleported");
            player.teleport(loc);
            return;
        }
        TeleportRunnable runnable = new TeleportRunnable(player.getUniqueId(), loc, player.getLocation(), cost);
        runnableMap.put(player.getUniqueId(), runnable);
        MessageManager.sendMessage(player, "messages.teleporting");
        runnable.runTaskTimer(GriefPreventionTP.getInstance(), 0, 20);
    }

    public boolean isSafeLocation(Location location) {
        Block under = location.clone().subtract(0, 1, 0).getBlock();
        return under.isSolid() && !under.isLiquid() && isBlockSafe(location) && isBlockSafe(location.clone().add(0, 1, 0));
    }

    private boolean isBlockSafe(Location location) {
        return !location.getBlock().isSolid() && !location.getBlock().isLiquid();
    }

    public int getTPCost(Player player) {
        if (!GriefPreventionTP.getInstance().isUseVault() || !enableTPCost) return 0;
        if (player.hasPermission("gptp.bypass.cost.tp") || player.hasPermission("gptp.bypass.cost")) return 0;
        Permission permission = GriefPreventionTP.getInstance().getVaultPermissions();
        for (Pair<String, Integer> pair : tpCost) {
            if (permission.playerInGroup(player, pair.getValue0())) {
                return pair.getValue1();
            }
        }
        return 0;
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
