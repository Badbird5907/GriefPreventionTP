package dev.badbird.griefpreventiontp.data;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.object.ClaimInfo;

import java.util.Collection;

public interface StorageProvider {
    void init(GriefPreventionTP plugin);

    void disable(GriefPreventionTP plugin);

    void saveClaims(Collection<ClaimInfo> claims);

    Collection<ClaimInfo> getClaims();
}
