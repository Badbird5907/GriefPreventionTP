package dev.badbird.griefpreventiontp.commands.impl;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.menus.ManageClaimMenu;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.badbird5907.blib.command.Command;
import net.octopvp.commander.annotation.JoinStrings;
import net.octopvp.commander.annotation.Name;
import net.octopvp.commander.annotation.Required;
import net.octopvp.commander.annotation.Sender;
import net.octopvp.commander.bukkit.annotation.PlayerOnly;
import org.bukkit.entity.Player;

public class ManageClaimsCommand {
    @Command(name = "manageclaim", description = "Manage claims")
    @PlayerOnly
    public void execute(@Sender Player sender, @Required @Name("name") @JoinStrings String name) {
        ClaimInfo claim;
        Claim c;
        try {
            long claimId = Long.parseLong(name);
            c = GriefPrevention.instance.dataStore.getClaim(claimId);
            if (c == null) {
                MessageManager.sendMessage(sender, "messages.claim-not-found");
                return;
            }
            claim = GriefPreventionTP.getInstance().getClaimManager().fromClaim(c);
        } catch (NumberFormatException e) {
            claim = GriefPreventionTP.getInstance().getClaimManager().getClaims(sender.getUniqueId()).stream().filter(cl -> cl.getName().equalsIgnoreCase(name)).findFirst().orElse(null); // Prioritize private claims over public claims
            if (claim == null) {
                claim = GriefPreventionTP.getInstance().getClaimManager().getAllPublicClaims().stream().filter(cl -> cl.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                if (claim == null) {
                    MessageManager.sendMessage(sender, "messages.claim-not-found");
                    return;
                }
            }
            c = claim.getClaim();
        }
        boolean canManage = sender.hasPermission("gptp.staff") ||
                GriefPreventionTP.getInstance().getPermissionsManager()
                        .hasClaimPermission(sender, c);
        if (!canManage) {
            MessageManager.sendMessage(sender, "messages.no-permission");
            return;
        }
        new ManageClaimMenu(claim, null).open(sender);
    }
}
