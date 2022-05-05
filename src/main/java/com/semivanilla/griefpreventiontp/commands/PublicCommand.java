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
import org.bukkit.entity.Player;

public class PublicCommand extends BaseCommand {
    @Command(name = "public", aliases = {"private"}, playerOnly = true, description = "Toggle the public/private status of the claim you're standing in", cooldown = 15)
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
        ClaimInfo ci = GriefPreventionTP.getInstance().getClaimManager().fromClaim(claim);
        ci.setPublic(!ci.isPublic());
        if (ci.isPublic()) MessageManager.sendMessage(player, "messages.public-on");
        else MessageManager.sendMessage(player, "messages.public-off");
        ci.save();
        return CommandResult.SUCCESS;
    }
}
