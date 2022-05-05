package com.semivanilla.griefpreventiontp.commands;

import com.semivanilla.griefpreventiontp.GriefPreventionTP;
import com.semivanilla.griefpreventiontp.manager.MessageManager;
import com.semivanilla.griefpreventiontp.object.ClaimInfo;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.badbird5907.blib.command.BaseCommand;
import net.badbird5907.blib.command.Command;
import net.badbird5907.blib.command.CommandResult;
import net.badbird5907.blib.command.Sender;
import net.badbird5907.blib.util.StoredLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetSpawnCommand extends BaseCommand {
    @Command(name = "setspawn", playerOnly = true,cooldown = 30)
    public CommandResult execute(Sender sender, String[] args) {
        Player player = sender.getPlayer();
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, playerData.lastClaim);
        if (claim == null) {
            MessageManager.sendMessage(player, "messages.must-be-standing-in-claim");
            return CommandResult.SUCCESS;
        }
        if (claim.getOwnerID() != player.getUniqueId() && !claim.hasExplicitPermission(player, ClaimPermission.Manage)) {
            MessageManager.sendMessage(player, "messages.no-permission");
            return CommandResult.SUCCESS;
        }
        Location loc = player.getLocation();
        //check if the location is in the claim
        if(!claim.contains(loc,true,
                false //Don't know what excludeSubdivisions is, but i'll leave it false
        )){
            MessageManager.sendMessage(player,"messages.must-be-standing-in-claim");
            return CommandResult.SUCCESS;
        }

        ClaimInfo ci = GriefPreventionTP.getInstance().getClaimManager().fromClaim(claim);
        ci.setSpawn(new StoredLocation(loc));
        ci.save();
        MessageManager.sendMessage(player, "messages.updated-spawn", ci.getSpawn().getX(), ci.getSpawn().getY(), ci.getSpawn().getZ());
        return CommandResult.SUCCESS;
    }
}
