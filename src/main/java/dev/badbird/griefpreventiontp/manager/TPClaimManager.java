package dev.badbird.griefpreventiontp.manager;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.badbird5907.blib.objects.tuple.Pair;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class TPClaimManager {
    private ConcurrentHashMap<UUID, CopyOnWriteArrayList<ClaimInfo>> claims = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<ClaimInfo> publicClaims = new CopyOnWriteArrayList<>();

    private CopyOnWriteArrayList<ClaimInfo> allClaims = new CopyOnWriteArrayList<>(); //Holds references to all claims

    private List<Pair<String, Integer>> maxPublic = new ArrayList<>();
    boolean enableMaxPublic = false;

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
        enableMaxPublic = GriefPreventionTP.getInstance().getConfig().getBoolean("max-public.enable");
        if (enableMaxPublic) {
            Map<String, Object> map = GriefPreventionTP.getInstance().getConfig().getConfigurationSection("max-public.rules").getValues(false);
            for (Map.Entry<String, Object> stringObjectEntry : map.entrySet()) {
                String k = stringObjectEntry.getKey();
                Object v = stringObjectEntry.getValue();
                int i = -1;
                if (v instanceof String) {
                    i = Integer.parseInt((String) v);
                } else if (v instanceof Integer) {
                    i = (Integer) v;
                }
                maxPublic.add(new Pair<>(k, i));
            }

            // sort by biggest first, consider -1 largest
            // largest should be considered first when looping through this list
            maxPublic.sort((o1, o2) -> {
                int i1 = o1.getValue1();
                int i2 = o2.getValue1();

                if (i1 == -1) {
                    return -1;
                } else if (i2 == -1) {
                    return 1;
                } else {
                    return i1 < i2 ? 1 : -1;
                }
            });
        }
        //for (Pair<String, Integer> stringIntegerPair : maxPublic) {
        //    System.out.println(stringIntegerPair.getValue0() + " | " + stringIntegerPair.getValue1());
        //}
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
        allClaims.removeIf(claim -> claim.getClaim() == null);
        return allClaims.stream().filter(claim -> {
            Claim c = claim.getClaim();
            return claim.getOwner().equals(owner) || c.hasExplicitPermission(owner, ClaimPermission.Access) || c.hasExplicitPermission(owner, ClaimPermission.Build);
        }).collect(CopyOnWriteArrayList::new, CopyOnWriteArrayList::add, CopyOnWriteArrayList::addAll);
    }

    public List<ClaimInfo> getAllPublicClaims() {
        publicClaims.removeIf(claim -> claim.getClaim() == null);
        return publicClaims.stream().collect(CopyOnWriteArrayList::new, CopyOnWriteArrayList::add, CopyOnWriteArrayList::addAll);
    }

    public ClaimInfo fromClaim(Claim claim) {
        ClaimInfo claimInfo = allClaims.stream().filter(c -> Objects.equals(c.getClaim().getID(), claim.getID())).findFirst().orElse(null);
        if (claimInfo == null) {
            claimInfo = new ClaimInfo(claim.getID(), claim.getOwnerID());
            if (claimInfo.getPlayerClaimCount() == 0) {
                claimInfo.setPlayerClaimCount((int) (GriefPrevention.instance.dataStore.getPlayerData(claim.getOwnerID()).getClaims().stream().filter(c -> c.getID() != claim.getID()).count() + 1));
                claimInfo.setName("Unnamed (" + claimInfo.getPlayerClaimCount() + ")");
            }
            allClaims.add(claimInfo);
            save();
        }
        return claimInfo;
    }

    public CopyOnWriteArrayList<ClaimInfo> getAllClaims() {
        return allClaims;
    }

    public void updatePublic(ClaimInfo claimInfo) {
        if (claimInfo.isPublic()) {
            publicClaims.add(claimInfo);
        } else {
            publicClaims.remove(claimInfo);
        }
    }

    public void onPlayerJoin(Player player) {
        for (Claim claim : GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId()).getClaims()) {
            fromClaim(claim); //just to make sure its in the list
        }
    }

    public boolean canMakePublic(Player player) { //We may need to optimize this
        if (GriefPreventionTP.getInstance().isUseVault() && enableMaxPublic) {
            if (player.hasPermission("gptp.bypass-public")) return true;
            Permission permission = GriefPreventionTP.getInstance().getVaultPermissions();
            List<Pair<String, Integer>> maxPublic = new ArrayList<>();
            for (Pair<String, Integer> pair : this.maxPublic) {
                if (permission.playerInGroup(player, pair.getValue0())) {
                    maxPublic.add(pair);
                }
            }
            if (maxPublic.size() == 0) {
                return true;
            }
            AtomicInteger count = new AtomicInteger();
            getClaims(player.getUniqueId()).forEach(claimInfo -> {
                if (claimInfo.isPublic()) {
                    count.getAndIncrement();
                }
            });
            for (Pair<String, Integer> pair : maxPublic) {
                if (pair.getValue1() < 0) {
                    return true;
                }
                if (count.get() < pair.getValue1()) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public void updateClaims(UUID owner) {
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(owner);
        for (ClaimInfo claim : getClaims(owner)) {
            claim.setPlayerClaimCount(playerData.getClaims().indexOf(claim.getClaim()) + 1);
            if (claim.getName().startsWith("Unnamed (") && claim.getName().endsWith(")")) {
                claim.setName("Unnamed (" + claim.getPlayerClaimCount() + ")");
            }
        }
    }
}
