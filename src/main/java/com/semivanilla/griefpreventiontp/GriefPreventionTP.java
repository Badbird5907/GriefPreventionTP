package com.semivanilla.griefpreventiontp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.semivanilla.griefpreventiontp.data.StorageProvider;
import com.semivanilla.griefpreventiontp.data.impl.FlatFileStorageProvider;
import com.semivanilla.griefpreventiontp.manager.TPClaimManager;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class GriefPreventionTP extends JavaPlugin {
    @Getter
    private static GriefPreventionTP instance;

    @Getter
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    private StorageProvider storageProvider;

    @Getter @Setter
    private boolean disabled = false;
    @Getter @Setter
    private String disabledReason = "";

    @Getter
    private TPClaimManager claimManager;

    @Getter
    private MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        instance = this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
        this.storageProvider = new FlatFileStorageProvider();
        this.storageProvider.init(this);

        this.claimManager = new TPClaimManager();
        this.claimManager.init();
    }

    @Override
    public void onDisable() {
        if (this.storageProvider != null) {
            this.storageProvider.disable(this);
        }
    }
}
