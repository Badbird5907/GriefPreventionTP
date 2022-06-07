package dev.badbird.griefpreventiontp.commands.impl;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.object.ClaimInfo;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.badbird5907.blib.util.StoredLocation;
import net.octopvp.commander.annotation.Command;
import net.octopvp.commander.annotation.Cooldown;
import net.octopvp.commander.annotation.Sender;
import net.octopvp.commander.bukkit.annotation.PlayerOnly;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetSpawnCommand {
    @Command(name = "setspawn")
    @PlayerOnly
    @Cooldown(15)
    public void execute(@Sender Player player) {
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, playerData.lastClaim);
        if (claim == null) {
            MessageManager.sendMessage(player, "messages.must-be-standing-in-claim");
            return;
        }
        if (claim.getOwnerID() != player.getUniqueId() && !claim.hasExplicitPermission(player, ClaimPermission.Manage)) {
            MessageManager.sendMessage(player, "messages.no-permission");
            return;
        }
        Location loc = player.getLocation();
        //check if the location is in the claim
        if(!claim.contains(loc,true,
                false //Don't know what excludeSubdivisions is, but i'll leave it false
        )){
            MessageManager.sendMessage(player,"messages.must-be-standing-in-claim");
            return;
        }

        ClaimInfo ci = GriefPreventionTP.getInstance().getClaimManager().fromClaim(claim);
        ci.setSpawn(new StoredLocation(loc));
        ci.save();
        MessageManager.sendMessage(player, "messages.updated-spawn", ci.getSpawn().getX(), ci.getSpawn().getY(), ci.getSpawn().getZ());
        return;
    }
}
