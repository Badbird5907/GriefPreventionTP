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
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
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
        
        // Handle Nether dimensions specially to avoid spawning on the bedrock ceiling
        if (l.getWorld().getEnvironment() == World.Environment.NETHER) {
            // Start from Y=120 (below bedrock ceiling) and search downward for a safe spawn
            l.setY(120);
            Location safeLoc = findSafeNetherLocation(l);
            if (safeLoc != null) {
                return safeLoc;
            }
            // Fallback to a reasonable height if no safe location found
            l.setY(64);
            return l;
        }
        
        // For Overworld and End, use the highest block
        l.setY(l.getWorld().getHighestBlockYAt(l) + 1.5);
        return l;
    }
    
    /**
     * Finds a safe spawn location in the Nether by searching downward
     * @param start The starting location to search from
     * @return A safe location with solid ground and air above, or null if none found
     */
    private static Location findSafeNetherLocation(Location start) {
        World world = start.getWorld();
        int x = start.getBlockX();
        int z = start.getBlockZ();
        
        // Search downward from starting Y to bedrock floor (Y=5)
        for (int y = start.getBlockY(); y > 5; y--) {
            Block block = world.getBlockAt(x, y, z);
            Block above1 = world.getBlockAt(x, y + 1, z);
            Block above2 = world.getBlockAt(x, y + 2, z);
            
            // Check if this is a safe spawn: solid block with 2 air blocks above
            if (block.getType().isSolid() && 
                !block.getType().equals(Material.LAVA) &&
                (above1.getType().isAir() || above1.getType().equals(Material.CAVE_AIR)) &&
                (above2.getType().isAir() || above2.getType().equals(Material.CAVE_AIR))) {
                
                Location safeLoc = new Location(world, x + 0.5, y + 1.5, z + 0.5);
                return safeLoc;
            }
        }
        
        return null;
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
