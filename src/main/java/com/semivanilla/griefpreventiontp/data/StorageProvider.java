package com.semivanilla.griefpreventiontp.data;

import com.semivanilla.griefpreventiontp.GriefPreventionTP;
import com.semivanilla.griefpreventiontp.object.ClaimInfo;
import com.semivanilla.griefpreventiontp.object.PlayerData;

import java.util.Collection;
import java.util.UUID;

public interface StorageProvider {
    void init(GriefPreventionTP plugin);

    void disable(GriefPreventionTP plugin);

    void save(PlayerData data);

    PlayerData getData(String playerName);

    PlayerData getData(UUID uuid);

    void saveClaims(Collection<ClaimInfo> claims);

    Collection<ClaimInfo> getClaims();
}
