package dev.badbird.griefpreventiontp.commands.impl;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.octopvp.commander.annotation.*;
import net.octopvp.commander.bukkit.annotation.PlayerOnly;
import org.bukkit.entity.Player;

public class RenameCommand {
    @Command(name = "rename")
    @PlayerOnly
    @Cooldown(15)
    public void execute(@Sender Player sender, @Sender PlayerData playerData, @JoinStrings @Required @Name("name") String name) {
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(sender.getLocation(), true, playerData.lastClaim);
        if (claim == null) {
            MessageManager.sendMessage(sender, "messages.must-be-standing-in-claim");
            return;
        }
        if (claim.getOwnerID() != sender.getUniqueId() && !claim.hasExplicitPermission(sender, ClaimPermission.Manage)) {
            MessageManager.sendMessage(sender, "messages.no-permission");
            return;
        }
        if (name.length() > GriefPreventionTP.getInstance().getConfig().getInt("max-claim-name-length")) {
            MessageManager.sendMessage(sender, "messages.name-too-long");
            return;
        }
        ClaimInfo ci = GriefPreventionTP.getInstance().getClaimManager().fromClaim(claim);
        ci.setName(name);
        MessageManager.sendMessage(sender, "messages.updated-name", name);
    }
}
