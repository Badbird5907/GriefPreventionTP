package dev.badbird.griefpreventiontp.commands.impl;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import dev.badbird.griefpreventiontp.manager.PermissionsManager;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.octopvp.commander.annotation.*;
import net.octopvp.commander.bukkit.annotation.PlayerOnly;
import net.octopvp.commander.exception.NoPermissionException;
import org.bukkit.entity.Player;

public class RenameCommand {
    @Command(name = "rename", description = "Rename the claim you're standing in")
    @PlayerOnly
    @Cooldown(3)
    public void rename(@Sender Player sender, @JoinStrings @Required @Name("name") String name, @Dependency PermissionsManager permissionsManager) {
        if (permissionsManager.isClaimsPerm() && !sender.hasPermission("gptp.command.rename")) throw new NoPermissionException();
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(sender.getUniqueId());
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(sender.getLocation(), true, playerData.lastClaim);
        if (claim == null) {
            MessageManager.sendMessage(sender, "messages.must-be-standing-in-claim");
            return;
        }
        //if (claim.getOwnerID() != sender.getUniqueId() && !claim.hasExplicitPermission(sender, ClaimPermission.Manage)) {
        if (!permissionsManager.hasClaimPermission(sender, claim)) {
            MessageManager.sendMessage(sender, "messages.no-permission");
            return;
        }
        //String fullName = String.join(" ", name);
        String fullName = name;
        if (fullName.length() > GriefPreventionTP.getInstance().getConfig().getInt("max-claim-name-length")) {
            MessageManager.sendMessage(sender, "messages.name-too-long");
            return;
        }
        ClaimInfo ci = GriefPreventionTP.getInstance().getClaimManager().fromClaim(claim);
        ci.setName(fullName);
        ci.save();
        MessageManager.sendMessage(sender, "messages.updated-name", fullName);
    }
}
