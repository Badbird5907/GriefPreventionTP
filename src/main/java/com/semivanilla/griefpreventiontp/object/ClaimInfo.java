package com.semivanilla.griefpreventiontp.object;

import com.semivanilla.griefpreventiontp.GriefPreventionTP;
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
    private String name,ownerName;

    public ClaimInfo(long claimID, UUID owner) {
        this.claimID = claimID;
        this.owner = owner;
        this.name = "Unnamed (" + (GriefPrevention.instance.dataStore.getPlayerData(owner).getClaims().size() + 1) + ")";
        this.ownerName = Bukkit.getOfflinePlayer(owner).getName();
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
