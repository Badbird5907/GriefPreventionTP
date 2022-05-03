package com.semivanilla.griefpreventiontp.listener;

import com.semivanilla.griefpreventiontp.object.ClaimInfo;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.events.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClaimListener implements Listener {
    @EventHandler
    public void onClaim(ClaimEvent event) {
        ClaimInfo claimInfo = new ClaimInfo(event.getClaim().getID(),event.getClaim().getOwnerID());

    }
    @EventHandler
    public void onClaimTransfer(ClaimTransferEvent e){

    }
    @EventHandler
    public void onTrustChanged(TrustChangedEvent e) {

    }

    @EventHandler
    public void onUnclaim(ClaimDeletedEvent event) {

    }
    @EventHandler
    public void onResize(ClaimResizeEvent e){

    }
}
