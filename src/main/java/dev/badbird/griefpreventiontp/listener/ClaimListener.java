package dev.badbird.griefpreventiontp.listener;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
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
        Location l = ClaimInfo.getDefaultLocation(event.getClaim());

        StoredLocation storedLocation = new StoredLocation(l);
        claimInfo.setSpawn(storedLocation);
        claimInfo.save();
        Player player = Bukkit.getPlayer(event.getClaim().getOwnerID());
        if (player != null) {
            MessageManager.sendMessage(player, "messages.claim-created", storedLocation.getX(), storedLocation.getY(), storedLocation.getZ());
        }
    }

    @EventHandler
    public void onClaimTransfer(ClaimTransferEvent event) {
        ClaimInfo claimInfo = GriefPreventionTP.getInstance().getClaimManager().fromClaim(event.getClaim());
        claimInfo.setOwner(event.getNewOwner());
        claimInfo.save();
        GriefPreventionTP.getInstance().getClaimManager().updateClaims(claimInfo.getOwner());
    }

    @EventHandler
    public void onUnclaim(ClaimDeletedEvent event) {
        GriefPreventionTP.getInstance().getClaimManager().getAllClaims().removeIf(c -> c.getClaimID() == event.getClaim().getID());
        GriefPreventionTP.getInstance().getClaimManager().save();
        GriefPreventionTP.getInstance().getClaimManager().updateClaims(event.getClaim().getOwnerID());
    }

    @EventHandler
    public void onClaimResize(ClaimResizeEvent event) {
        Claim to = event.getTo();
        Claim from = event.getFrom();
        ClaimInfo claimInfo = GriefPreventionTP.getInstance().getClaimManager().fromClaim(to);
        claimInfo.checkValid();

        //System.out.println("Claim " + (claim == claim2) + " | " + claim.getID() + " | " + claim2.getID());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if ((event.getPlayer().hasPermission("gptp.staff") || event.getPlayer().isOp()) && (GriefPreventionTP.getInstance().getConfig().getBoolean("update-check") && GriefPreventionTP.getInstance().getUpdateChecker() != null)) {
            if (GriefPreventionTP.getInstance().isUpdateAvailable()){
                event.getPlayer().sendMessage(CC.translate("&7[&bGriefPreventionTP&7] &aThere is a update available! Your current version is: " + CC.B + GriefPreventionTP.getInstance().getDescription().getVersion() + CC.R + CC.GREEN + " and the new version is: " + CC.B + GriefPreventionTP.getInstance().getNewVersion() + CC.R + CC.GREEN + ".\nDownload @ https://badbird5907.xyz/gptp?ref=server"));
            }
        }
        UUID uuid = event.getPlayer().getUniqueId();
        GriefPreventionTP.getInstance().getClaimManager().onPlayerJoin(event.getPlayer());
        Tasks.runAsync(()-> {
            if (uuid.toString().equals("5bd217f6-b89a-4064-a7f9-11733e8baafa"))
                event.getPlayer().sendMessage(CC.GREEN + "This server is running GPTP!");
        });
    }
}
