package com.semivanilla.griefpreventiontp.commands;

import com.semivanilla.griefpreventiontp.GriefPreventionTP;
import com.semivanilla.griefpreventiontp.manager.MessageManager;
import com.semivanilla.griefpreventiontp.object.ClaimInfo;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.badbird5907.blib.command.BaseCommand;
import net.badbird5907.blib.command.Command;
import net.badbird5907.blib.command.CommandResult;
import net.badbird5907.blib.command.Sender;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

public class RenameCommand extends BaseCommand {
    @Command(name = "rename", playerOnly = true, cooldown = 30)
    public CommandResult execute(Sender sender, String[] args) {
        Player player = sender.getPlayer();
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, playerData.lastClaim);
        if (claim == null) {
            MessageManager.sendMessage(player, "messages.must-be-standing-in-claim");
            return CommandResult.SUCCESS;
        }
        if (claim.getOwnerID() != player.getUniqueId()) {
            MessageManager.sendMessage(player, "messages.no-permission");
            return CommandResult.SUCCESS;
        }
        String name = StringUtils.join(args, " ");
        if (name.length() > GriefPreventionTP.getInstance().getConfig().getInt("max-claim-name-length")) {
            MessageManager.sendMessage(player, "messages.name-too-long");
            return CommandResult.SUCCESS;
        }
        ClaimInfo ci = GriefPreventionTP.getInstance().getClaimManager().fromClaim(claim);
        ci.setName(name);
        MessageManager.sendMessage(player, "messages.updated-name", name);
        return CommandResult.SUCCESS;
    }
}
