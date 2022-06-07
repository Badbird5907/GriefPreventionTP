package dev.badbird.griefpreventiontp.listener;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import me.ryanhamshire.GriefPrevention.events.*;
import net.badbird5907.blib.util.CC;
import net.badbird5907.blib.util.StoredLocation;
import net.badbird5907.blib.util.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class ClaimListener implements Listener {
    @EventHandler
    public void onClaim(ClaimCreatedEvent event) {
        ClaimInfo claimInfo = GriefPreventionTP.getInstance().getClaimManager().fromClaim(event.getClaim());
        //find the center of the claim
        Location l = event.getClaim().getGreaterBoundaryCorner().clone().add(event.getClaim().getLesserBoundaryCorner().clone()).multiply(0.5);
        //set Y value to the highest block
        l.setY(l.getWorld().getHighestBlockYAt(l) + 1.5);
        StoredLocation storedLocation = new StoredLocation(l);
        claimInfo.setSpawn(storedLocation);
        claimInfo.save();
        Player player = Bukkit.getPlayer(event.getClaim().getOwnerID());
        if (player != null) {
            MessageManager.sendMessage(player, "messages.claim-created", storedLocation.getX(), storedLocation.getY(), storedLocation.getZ());
        }
    }

    @EventHandler
    public void onClaimTransfer(ClaimTransferEvent e) {
        ClaimInfo claimInfo = GriefPreventionTP.getInstance().getClaimManager().fromClaim(e.getClaim());
        claimInfo.setOwner(e.getNewOwner());
        claimInfo.save();
    }

    @EventHandler
    public void onUnclaim(ClaimDeletedEvent event) {
        GriefPreventionTP.getInstance().getClaimManager().getAllClaims().removeIf(c -> c.getClaimID() == event.getClaim().getID());
        GriefPreventionTP.getInstance().getClaimManager().save();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        GriefPreventionTP.getInstance().getClaimManager().onPlayerJoin(e.getPlayer());
        Tasks.runAsync(()-> {
            if (uuid.toString().equals("5bd217f6-b89a-4064-a7f9-11733e8baafa"))
                e.getPlayer().sendMessage(CC.GREEN + "This server is running GPTP!");
        });
    }
}
