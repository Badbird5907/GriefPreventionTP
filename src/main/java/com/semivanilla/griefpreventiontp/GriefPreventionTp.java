package com.semivanilla.griefpreventiontp;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class GriefPreventionTp extends JavaPlugin {
    @Getter
    private static GriefPreventionTp instance;

    @Override
    public void onEnable() {
        instance = this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
    }
}
