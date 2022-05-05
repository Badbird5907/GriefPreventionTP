package com.semivanilla.griefpreventiontp.manager;

import com.semivanilla.griefpreventiontp.GriefPreventionTP;
import com.semivanilla.griefpreventiontp.object.ClaimInfo;
import lombok.Getter;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TPClaimManager {
    private ConcurrentHashMap<UUID, CopyOnWriteArrayList<ClaimInfo>> claims = new ConcurrentHashMap<>();
    private Set<ClaimInfo> publicClaims = ConcurrentHashMap.newKeySet();

    private Set<ClaimInfo> allClaims = ConcurrentHashMap.newKeySet(); //Holds references to all claims

    public void init() {
        load();
    }

    public void stop() {
        save();
    }

    public void load() {
        allClaims.clear();
        publicClaims.clear();
        claims.clear();
        allClaims.addAll(GriefPreventionTP.getInstance().getStorageProvider().getClaims());
        sortClaims();
    }

    public void sortClaims() {
        claims.clear();
        publicClaims.clear();
        for (ClaimInfo claim : allClaims) {
            if (claim.isPublic()) {
                publicClaims.add(claim);
            }
            claims.computeIfAbsent(claim.getOwner(), k -> new CopyOnWriteArrayList<>()).add(claim);
        }
    }

    public void save() {
        GriefPreventionTP.getInstance().getStorageProvider().saveClaims(allClaims);
    }

    public List<ClaimInfo> getClaims(UUID owner) {
        return allClaims.stream().filter(claim -> {
            Claim c = claim.getClaim();
            return claim.getOwner().equals(owner) || c.hasExplicitPermission(owner, ClaimPermission.Access) || c.hasExplicitPermission(owner, ClaimPermission.Build);
        }).collect(CopyOnWriteArrayList::new, CopyOnWriteArrayList::add, CopyOnWriteArrayList::addAll);
    }

    public List<ClaimInfo> getAllPublicClaims() {
        return publicClaims.stream().collect(CopyOnWriteArrayList::new, CopyOnWriteArrayList::add, CopyOnWriteArrayList::addAll);
    }

    public ClaimInfo fromClaim(Claim claim) {
        ClaimInfo claimInfo = allClaims.stream().filter(c -> c.getClaim().equals(claim)).findFirst().orElse(null);
        if (claimInfo == null) {
            claimInfo = new ClaimInfo(claim.getID(), claim.getOwnerID());
            allClaims.add(claimInfo);
            save();
        }
        return claimInfo;
    }

    public Set<ClaimInfo> getAllClaims() {
        return allClaims;
    }

    public void updatePublic(ClaimInfo claimInfo){
        if (claimInfo.isPublic()) {
            publicClaims.add(claimInfo);
        } else {
            publicClaims.remove(claimInfo);
        }
    }
}
