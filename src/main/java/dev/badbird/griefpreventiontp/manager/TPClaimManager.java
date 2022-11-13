package dev.badbird.griefpreventiontp.manager;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.badbird5907.blib.objects.tuple.Pair;
import net.milkbowl.vault.economy.Economy;
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

    private List<Pair<String, Integer>> maxPublic = new ArrayList<>(),
            createCost = new ArrayList<>(); // List because we want to sort it
    private boolean enableMaxPublic = false, enableVaultIntegration = false, enablePublicCost = false;

    private Object vaultEconomy;

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
        enableMaxPublic = GriefPreventionTP.getInstance().getConfig().getBoolean("max-public.enable", false);
        enableVaultIntegration = GriefPreventionTP.getInstance().getConfig().getBoolean("vault-integration.enable", false);
        enablePublicCost = GriefPreventionTP.getInstance().getConfig().getBoolean("vault-integration.public-claim-cost", false);
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
        if (enableVaultIntegration) {
            boolean vaultEnabled = GriefPreventionTP.getInstance().getServer().getPluginManager().isPluginEnabled("Vault");
            if (!vaultEnabled) {
                GriefPreventionTP.getInstance().getLogger().warning("Vault is not enabled, but vault-integration is enabled in the config! Vault integration will not work.");
                enableVaultIntegration = false;
                return;
            }
        }
        if (enableVaultIntegration && enablePublicCost) {
            Map<String, Object> map = GriefPreventionTP.getInstance().getConfig().getConfigurationSection("vault-integration.public-claim-cost.groups").getValues(false);
            for (Map.Entry<String, Object> stringObjectEntry : map.entrySet()) {
                String k = stringObjectEntry.getKey();
                Object v = stringObjectEntry.getValue();
                int i = -1;
                if (v instanceof String) {
                    i = Integer.parseInt((String) v);
                } else if (v instanceof Integer) {
                    i = (Integer) v;
                }
                createCost.add(new Pair<>(k, i));
            }

            // sort by biggest first, consider -1 largest
            // largest should be considered first when looping through this list
            createCost.sort((o1, o2) -> {
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

            vaultEconomy = GriefPreventionTP.getInstance().getServer().getServicesManager().getRegistration(Economy.class).getProvider();
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
            return GriefPreventionTP.getInstance().getPermissionsManager().canTeleportToClaim(owner, c);
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

    public int canMakePublic(Player player) { //We may need to optimize this
        if (GriefPreventionTP.getInstance().isUseVault() && enableMaxPublic) {
            if (player.hasPermission("gptp.bypass-public")) return 0;
            Permission permission = GriefPreventionTP.getInstance().getVaultPermissions();
            List<Pair<String, Integer>> maxPublic = new ArrayList<>();
            for (Pair<String, Integer> pair : this.maxPublic) {
                if (permission.playerInGroup(player, pair.getValue0())) {
                    maxPublic.add(pair);
                }
            }
            if (maxPublic.size() == 0) {
                return 0;
            }
            AtomicInteger count = new AtomicInteger();
            getClaims(player.getUniqueId()).forEach(claimInfo -> {
                if (claimInfo.isPublic()) {
                    count.getAndIncrement();
                }
            });
            for (Pair<String, Integer> pair : maxPublic) {
                if (pair.getValue1() < 0) {
                    return 0;
                }
                if (count.get() < pair.getValue1()) {
                    return 0;
                }
            }
            return 1;
        }
        if (GriefPreventionTP.getInstance().isUseVault() && enablePublicCost) {
            if (player.hasPermission("gptp.bypass-public")) return 0;
            int cost = getCostToMakePublic(player);
            if (cost <= 0) {
                return 0;
            }
            if (playerHasEnough(player, cost)) {
                return 0;
            }
            return 2;
        }
        return 0;
    }
    public int getCostToMakePublic(Player player) {
        if (GriefPreventionTP.getInstance().isUseVault() && vaultEconomy != null && enablePublicCost) {
            if (player.hasPermission("gptp.bypass-cost")) return 0;
            Permission permission = GriefPreventionTP.getInstance().getVaultPermissions();
            for (Pair<String, Integer> pair : createCost) {
                if (permission.playerInGroup(player, pair.getValue0())) {
                    return pair.getValue1();
                }
            }
        }
        return 0;
    }

    public boolean playerHasEnough(Player player, int cost) {
        if (GriefPreventionTP.getInstance().isUseVault() && vaultEconomy != null && cost > 0) {
            Economy economy = (Economy) vaultEconomy;
            return economy.getBalance(player) >= cost;
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

    public Object getVaultEconomy() {
        return vaultEconomy;
    }
}
