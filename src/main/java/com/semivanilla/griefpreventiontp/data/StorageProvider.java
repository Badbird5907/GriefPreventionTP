package com.semivanilla.griefpreventiontp.data;

import com.semivanilla.griefpreventiontp.GriefPreventionTP;
import com.semivanilla.griefpreventiontp.object.PlayerData;

public interface StorageProvider {
    void init(GriefPreventionTP plugin);

    void save(PlayerData data);

    PlayerData getData(String playerName);
}
