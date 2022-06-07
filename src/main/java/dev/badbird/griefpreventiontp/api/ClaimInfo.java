package dev.badbird.griefpreventiontp.api;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import lombok.Getter;
import lombok.Setter;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.badbird5907.blib.util.StoredLocation;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@Getter
@Setter
public class ClaimInfo {
    private final long claimID;
    private UUID owner;
    private StoredLocation spawn; //TODO rename this to spawnLocation
    private boolean isPublic;
    private String name, ownerName;

    private int playerClaimCount = -1;

    public ClaimInfo(long claimID, UUID owner) {
        this.claimID = claimID;
        this.owner = owner;
        this.ownerName = Bukkit.getOfflinePlayer(owner).getName();
        int claimCount = GriefPrevention.instance.dataStore.getPlayerData(owner).getClaims().indexOf(getClaim()) + 1;
        this.name = "Unnamed (" + claimCount + ")";
        playerClaimCount = claimCount;
    }

    public int getPlayerClaimCount() {
        if (playerClaimCount == -1) {
            playerClaimCount = GriefPrevention.instance.dataStore.getPlayerData(owner).getClaims().indexOf(getClaim()) + 1;
            if (playerClaimCount == 0) {
                playerClaimCount = 1;
            }
        }
        return playerClaimCount;
    }

    public Claim getClaim() {
        Claim c = GriefPrevention.instance.dataStore.getClaim(claimID);
        if (c != null)
            this.owner = c.getOwnerID();
        return c;
    }

    public ClaimInfo save() {
        GriefPreventionTP.getInstance().getClaimManager().save();
        return this;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
        GriefPreventionTP.getInstance().getClaimManager().updatePublic(this);
    }

    public String getOwnerName() {
        if (ownerName == null) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(owner);
            ownerName = op.getName();
        }
        return ownerName;
    }
}
