package dev.badbird.griefpreventiontp.data;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import me.ryanhamshire.GriefPrevention.Claim;

import java.util.Collection;

public interface StorageProvider {
    void init(GriefPreventionTP plugin);

    void disable(GriefPreventionTP plugin);

    void saveClaims(Collection<ClaimInfo> claims);

    void saveClaim(ClaimInfo claim);

    Collection<ClaimInfo> getAllClaims();

    ClaimInfo getClaim(long claimId);

    ClaimInfo fromClaim(Claim claim);

    Collection<ClaimInfo> getPublicClaims();
}
