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
        this.publicPerm = config.getBoolean("commands.public.permission.enabled");
        this.privatePerm = config.getBoolean("commands.private.permission.enabled");
        this.renamePerm = config.getBoolean("commands.rename.permission.enabled");
        this.setSpawnPerm = config.getBoolean("commands.setspawn.permission.enabled");
        this.claimTPPerm = config.getBoolean("commands.claimtp.permission.enabled");
    }

    public boolean hasClaimPermission(Player player, Claim claim) {
        if (claim.getOwnerID().equals(player.getUniqueId())) return true;
        return allowManager && claim.hasExplicitPermission(player, ClaimPermission.Manage);
    }

    public boolean canTeleportToClaim(UUID player, Claim c) {
        return c.getOwnerID().equals(player) ||
                c.hasExplicitPermission(player, ClaimPermission.Access) ||
                c.hasExplicitPermission(player, ClaimPermission.Build) ||
                c.hasExplicitPermission(player, ClaimPermission.Manage);
    }
}
