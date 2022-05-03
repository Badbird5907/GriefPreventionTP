package com.semivanilla.griefpreventiontp.data.impl;

import com.semivanilla.griefpreventiontp.GriefPreventionTp;
import com.semivanilla.griefpreventiontp.data.StorageProvider;
import com.semivanilla.griefpreventiontp.object.PlayerData;

import java.io.File;

public class FlatFileStorageProvider implements StorageProvider {
    private File folder;
    @Override
    public void init(GriefPreventionTp plugin) {
        folder = new File(plugin.getDataFolder(), "data");
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    @Override
    public void save(PlayerData data) {
        File dataFile = new File(folder, data.getName() + ".json");

    }

    @Override
    public PlayerData getData(String playerName) {
        return null;
    }
}
