package dev.badbird.griefpreventiontp.manager;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
@Setter
public class PermissionsManager {
    private boolean claimsPerm, publicPerm, renamePerm, setSpawnPerm;

    public PermissionsManager(GriefPreventionTP plugin) {
        reload(plugin);
    }

    public void reload(GriefPreventionTP plugin) {
        FileConfiguration config = plugin.getConfig();

        this.claimsPerm = config.getBoolean("commands.claims.permission.enabled");
        this.publicPerm = config.getBoolean("commands.claims.public.enabled");
        this.renamePerm = config.getBoolean("commands.claims.rename.enabled");
        this.setSpawnPerm = config.getBoolean("commands.claims.setspawn.enabled");
    }
}
