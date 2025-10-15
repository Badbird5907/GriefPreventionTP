package dev.badbird.griefpreventiontp.api;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.manager.MessageManager;
import dev.badbird.griefpreventiontp.manager.TeleportManager;
import lombok.Getter;
import lombok.Setter;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.badbird5907.blib.util.Logger;
import net.badbird5907.blib.util.StoredLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
@Setter
public class ClaimInfo {
    private final long claimID;
    private UUID owner;
    private StoredLocation spawn;
    private boolean isPublic;
    private String name, ownerName;
    private IconWrapper icon = null;

    private int playerClaimCount = -1;

    public ClaimInfo(long claimID, UUID owner) {
        this.claimID = claimID;
        this.owner = owner;
        this.ownerName = Bukkit.getOfflinePlayer(owner).getName();
        this.playerClaimCount = -1;
        int claimCount = getPlayerClaimCount();
        this.name = "Unnamed (" + claimCount + ")";
        this.playerClaimCount = claimCount;
    }

    public int getPlayerClaimCount() {
        if (playerClaimCount == -1) {
            playerClaimCount = GriefPrevention.instance.dataStore.getPlayerData(owner).getClaims().size();
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

    public void checkValid() {
        Claim claim = getClaim();
        if (claim == null) {
            return; //Claim has been deleted
        }
        if (getSpawn() == null) {
            Logger.info("Claim %1 owned by %2 has an invalid spawn location (not set)! Resetting...", getName(), getOwnerName());
            setSpawn(new StoredLocation(ClaimInfo.getDefaultLocation(claim)));
            Player owner = Bukkit.getPlayer(getOwner());
            if (owner != null) {
                MessageManager.sendMessage(owner, "messages.invalid-claim", getName());
            }
        }else {
            Location spawn = getSpawn().getLocation();
            if (GriefPrevention.instance.dataStore.getClaimAt(spawn, false, claim) == null) {
                Logger.info("Claim %1 owned by %2 has an invalid spawn location (outside of claim)! Resetting...", getName(), getOwnerName());
                setSpawn(new StoredLocation(ClaimInfo.getDefaultLocation(claim)));
                Player owner = Bukkit.getPlayer(getOwner());
                if (owner != null) {
                    MessageManager.sendMessage(owner, "messages.invalid-claim", getName());
                }
            }
        }

    }

    public static Location getDefaultLocation(Claim claim) {
        Location l = claim.getGreaterBoundaryCorner().clone().add(claim.getLesserBoundaryCorner().clone()).multiply(0.5);
        // set Y value to the highest block
        World w = l.getWorld();
        Location top = l.clone();
        top.setY(w.getHighestBlockYAt(l) + 1.5);
        if (w.getEnvironment() == World.Environment.NETHER) { // handle bedrock roof
            // recursively loop down until we find a safe location
            Location loc = top.clone();
            while (loc.getBlockY() > 1 && !TeleportManager.isSafeLocation(loc)) {
                loc.setY(loc.getBlockY() - 2);
            }
            if (loc.getBlockY() <= 1) {
                Logger.warn("Could not find safe location for claim " + claim.getID() + " in nether, using top of roof");
                return top;
            }
            return loc;
        } else {
            return top;
        }
    }

    public IconWrapper getIcon() {
        if (icon != null) {
            if (GriefPreventionTP.getAllowedIcons().stream().noneMatch(icn -> icon.equals(icn))) {
                return icon = null;
            }
        }
        return icon;
    }
}
