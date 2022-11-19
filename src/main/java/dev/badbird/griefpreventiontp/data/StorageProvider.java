package dev.badbird.griefpreventiontp.data;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import dev.badbird.griefpreventiontp.object.FilterOptions;
import me.ryanhamshire.GriefPrevention.Claim;

import java.util.Collection;

public interface StorageProvider {
    void init(GriefPreventionTP plugin);

    void disable(GriefPreventionTP plugin);

    void saveClaims(Collection<ClaimInfo> claims);

    void saveClaim(ClaimInfo claim);

    void deleteClaim(long id);

    Collection<ClaimInfo> getAllClaims();

    ClaimInfo getClaim(long claimId);

    ClaimInfo fromClaim(Claim claim);

    Collection<ClaimInfo> getPublicClaims();

    int getTotalClaims(FilterOptions options);

    /**
     * Get claims with filter
     * @param options filter options
     * @param max Max claims to return
     * @param page Page to return
     * @return
     */
    Collection<ClaimInfo> getClaims(FilterOptions options, int max, int page);
}
