package dev.badbird.griefpreventiontp.commands.impl;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.manager.PermissionsManager;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.milkbowl.vault.economy.Economy;
import net.octopvp.commander.annotation.Command;
import net.octopvp.commander.annotation.Cooldown;
import net.octopvp.commander.annotation.Dependency;
import net.octopvp.commander.annotation.Sender;
import net.octopvp.commander.bukkit.annotation.PlayerOnly;
import net.octopvp.commander.exception.NoPermissionException;
import org.bukkit.entity.Player;

public class PublicCommand {
    @Command(name = "public", aliases = {"private"}, description = "Toggle the public/private status of the claim you're standing in")
    @Cooldown(3)
    @PlayerOnly
    public void execute(@Sender Player sender, @Dependency PermissionsManager permissionsManager, @Dependency GriefPreventionTP plugin) {
        if (!plugin.getConfig().getBoolean("enable-public")) {
            MessageManager.sendMessage(sender, "messages.public-disabled");
            return;
        }
        if (permissionsManager.isClaimsPerm() && !sender.hasPermission("gptp.command.public"))
            throw new NoPermissionException();
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
        ClaimInfo ci = GriefPreventionTP.getInstance().getClaimManager().fromClaim(claim);
        int cost = 0;
        if (!ci.isPublic()) {
            int canMakePublic = GriefPreventionTP.getInstance().getClaimManager().canMakePublic(sender);  // 0: can make public, 1: max public reached, 2: not enough money
            if (canMakePublic == 1) {
                MessageManager.sendMessage(sender, "messages.max-public-exceeded");
                return;
            } else if (canMakePublic == 2) {
                MessageManager.sendMessage(sender, "messages.not-enough-money");
                return;
            }
            cost = GriefPreventionTP.getInstance().getClaimManager().getCostToMakePublic(sender.getPlayer());
            if (cost > 0) {
                Economy economy = (Economy) GriefPreventionTP.getInstance().getClaimManager().getVaultEconomy();
                economy.withdrawPlayer(sender, cost);
            }
            ci.setPublic(true);
            MessageManager.sendMessage(sender, "messages.public-on", cost);
        } else {
            ci.setPublic(false);
            MessageManager.sendMessage(sender, "messages.public-off");
        }
        ci.save();
    }
}
