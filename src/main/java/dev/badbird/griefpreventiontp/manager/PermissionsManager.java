package dev.badbird.griefpreventiontp.manager;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import lombok.Getter;
import lombok.Setter;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
@Setter
public class PermissionsManager {
    private boolean claimsPerm, publicPerm, renamePerm, setSpawnPerm,
    allowManager, claimTPPerm, privatePerm;

    public PermissionsManager(GriefPreventionTP plugin) {
        reload(plugin);
    }

    public void reload(GriefPreventionTP plugin) {
        FileConfiguration config = plugin.getConfig();

        this.claimsPerm = config.getBoolean("commands.claims.permission.enabled");
        this.publicPerm = config.getBoolean("commands.claims.public.enabled");
        this.renamePerm = config.getBoolean("commands.claims.rename.enabled");
        this.setSpawnPerm = config.getBoolean("commands.claims.setspawn.enabled");
        this.claimTPPerm = config.getBoolean("commands.claims.teleport.enabled");
        this.allowManager = config.getBoolean("permissions.allow-manager");
    }

    public boolean hasClaimPermission(Player player, Claim claim) {
        if (claim.getOwnerID().equals(player.getUniqueId())) return true;
        return allowManager && claim.hasExplicitPermission(player, ClaimPermission.Manage);
    }

    public boolean canTeleportToClaim(UUID player, Claim c) {
        return c.ownerID.equals(player) || c.hasExplicitPermission(player, ClaimPermission.Access) || c.hasExplicitPermission(player, ClaimPermission.Build);
    }
}
