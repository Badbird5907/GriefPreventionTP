package com.semivanilla.griefpreventiontp.listener;

import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClaimListener implements Listener {
    @EventHandler
    public void onClaim(ClaimEvent event) {

    }
    @EventHandler
    public void onUnclaim(ClaimDeletedEvent event) {

    }
}
