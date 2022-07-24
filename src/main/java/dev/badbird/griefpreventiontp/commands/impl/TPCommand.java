package dev.badbird.griefpreventiontp.commands.impl;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.manager.PermissionsManager;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.octopvp.commander.annotation.*;
import net.octopvp.commander.bukkit.annotation.PlayerOnly;
import net.octopvp.commander.exception.NoPermissionException;
import org.bukkit.entity.Player;

public class TPCommand {
    @Command(name = "claimtp", description = "Teleport to a claim")
    @PlayerOnly
    public void execute(@Sender Player sender, @Dependency PermissionsManager permissionsManager, @Required @Name("name") @JoinStrings String name) {
        if (permissionsManager.isClaimTPPerm() && !sender.hasPermission("gptp.command.claimtp")) throw new NoPermissionException();
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

        if (!permissionsManager.canTeleportToClaim(sender.getUniqueId(), c) && !claim.isPublic()) {
            MessageManager.sendMessage(sender, "messages.no-permission");
            return;
        }

        if (claim.getSpawn() == null) {
            MessageManager.sendMessage(sender, "messages.no-spawn-set");
            return;
        }

        GriefPreventionTP.getInstance().getTeleportManager().teleport(sender, claim.getSpawn().getLocation());
    }
}
