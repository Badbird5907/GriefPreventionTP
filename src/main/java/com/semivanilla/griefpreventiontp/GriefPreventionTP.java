package com.semivanilla.griefpreventiontp;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class GriefPreventionTP extends JavaPlugin {
    @Getter
    private static GriefPreventionTP instance;

    @Override
    public void onEnable() {
        instance = this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
    }
}
