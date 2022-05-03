package com.semivanilla.griefpreventiontp.data;

import com.semivanilla.griefpreventiontp.GriefPreventionTp;
import com.semivanilla.griefpreventiontp.object.PlayerData;

public interface StorageProvider {
    void init(GriefPreventionTp plugin);

    void save(PlayerData data);

    PlayerData getData(String playerName);
}
