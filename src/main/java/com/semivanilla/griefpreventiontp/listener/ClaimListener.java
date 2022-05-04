package com.semivanilla.griefpreventiontp.listener;

import com.semivanilla.griefpreventiontp.GriefPreventionTP;
import com.semivanilla.griefpreventiontp.manager.MessageManager;
import com.semivanilla.griefpreventiontp.object.ClaimInfo;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.events.*;
import net.badbird5907.blib.util.CC;
import net.badbird5907.blib.util.StoredLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClaimListener implements Listener {
    @EventHandler
    public void onClaim(ClaimCreatedEvent event) {
        ClaimInfo claimInfo = GriefPreventionTP.getInstance().getClaimManager().fromClaim(event.getClaim());
        //find the center of the claim
        Location l = event.getClaim().getGreaterBoundaryCorner().clone().add(event.getClaim().getLesserBoundaryCorner().clone()).multiply(0.5);
        //set Y value to the highest block
        l.setY(l.getWorld().getHighestBlockYAt(l) + 1.5);
        StoredLocation storedLocation = new StoredLocation(l);
        claimInfo.setCenter(storedLocation);
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
    public void onTrustChanged(TrustChangedEvent e) {

    }

    @EventHandler
    public void onUnclaim(ClaimDeletedEvent event) {
        GriefPreventionTP.getInstance().getClaimManager().getAllClaims().removeIf(c -> c.getClaimID() == event.getClaim().getID());
        GriefPreventionTP.getInstance().getClaimManager().save();
    }

    @EventHandler
    public void onResize(ClaimResizeEvent e) {

    }
}
