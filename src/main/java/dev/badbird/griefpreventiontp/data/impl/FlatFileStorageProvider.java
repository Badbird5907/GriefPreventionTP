package dev.badbird.griefpreventiontp.data.impl;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.data.StorageProvider;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.badbird5907.blib.util.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FlatFileStorageProvider implements StorageProvider {
    private static final int VERSION = 1;
    private File folder;

    private File metaFile;

    @SneakyThrows
    @Override
    public void init(GriefPreventionTP plugin) {
        Logger.info("Initializing FlatFileStorageProvider");
        Logger.error("It is not recommended to use this storage provider, as it is not very efficient, and can cause lag issues at large claim counts. Please use a database instead.");
        folder = new File(plugin.getDataFolder(), "data");
        if (!folder.exists()) {
            folder.mkdir();
        }
        metaFile = new File(folder, "meta.json");
        JsonObject meta = new JsonObject();
        File claimsFile = new File(GriefPreventionTP.getInstance().getDataFolder(), "claims.json");
        if (metaFile.exists()) {
            try {
                meta = GriefPreventionTP.getInstance().getGson().fromJson(new String(Files.readAllBytes(metaFile.toPath())), JsonObject.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                metaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            meta.addProperty("version", VERSION);
            Files.write(metaFile.toPath(), ("{\"version\":" + VERSION + "}").getBytes());
        }

        if (meta.get("version").getAsInt() != VERSION) {
            Logger.warn("Data version is outdated! Attempting to migrate data...");
            migrateData(meta.get("version").getAsInt());
        }
        if (claimsFile.exists()) {
            Logger.warn("Old claims.json file found! Attempting to migrate data... This may take a while.");
            migrateClaims();
        }

        boolean bruh = !GriefPreventionTP.getInstance().getDescription().getName().equals("GriefPreventionTP") || !GriefPreventionTP.getInstance().getDescription().getWebsite().equals("https://badbird.dev");
        if (GriefPreventionTP.getInstance().getDescription().getAuthors().size() < 1) bruh = true;
        else if (!GriefPreventionTP.getInstance().getDescription().getAuthors().get(0).equals("Badbird5907"))
            bruh = true;
        if (bruh) {
            Logger.error("Please do not modify the plugin! To receive help, join the support server @ https://discord.badbird.dev/");
            GriefPreventionTP.getInstance().getServer().getPluginManager().disablePlugin(GriefPreventionTP.getInstance());
        }
    }

    @SneakyThrows
    private void migrateClaims() {
        // Migrate claims.json to every claim having its own file
        File claimsFile = new File(GriefPreventionTP.getInstance().getDataFolder(), "claims.json");
        Collection<ClaimInfo> claims = GriefPreventionTP.getInstance().getGson().fromJson(new String(Files.readAllBytes(claimsFile.toPath())), new TypeToken<Collection<ClaimInfo>>() {
        }.getType());
        for (ClaimInfo claim : claims) {
            File claimFile = new File(folder, claim.getClaimID() + ".json");
            if (!claimFile.exists()) {
                claimFile.createNewFile();
            }
            Files.write(claimFile.toPath(), GriefPreventionTP.getInstance().getGson().toJson(claim).getBytes());
        }
        Logger.info("Migrated claims.json to individual claim files!");
        Logger.info("Backing up claims.json, just in case...");
        File backup = new File(GriefPreventionTP.getInstance().getDataFolder(), "claims" + System.currentTimeMillis() + ".json.bak");
        Files.copy(claimsFile.toPath(), backup.toPath());
        Logger.info("Backed up claims.json to " + backup.getName());
        Logger.info("Deleting claims.json...");
        claimsFile.delete();
        Logger.info("Deleted claims.json!");
    }

    private void migrateData(int version) {

    }

    @Override
    public void disable(GriefPreventionTP plugin) {

    }

    @Override
    public void saveClaims(Collection<ClaimInfo> claims) {
        for (ClaimInfo claim : claims) {
            saveClaim(claim);
        }
    }

    @Override
    public void saveClaim(ClaimInfo claim) {
        File claimFile = new File(folder, claim.getClaimID() + ".json");
        if (!claimFile.exists()) {
            try {
                claimFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.write(claimFile.toPath(), GriefPreventionTP.getInstance().getGson().toJson(claim).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<ClaimInfo> getAllClaims() {
        List<ClaimInfo> claims = new ArrayList<>();
        for (Claim claim : GriefPrevention.instance.dataStore.getClaims()) {
            ClaimInfo claimInfo = fromClaim(claim);
            if (claimInfo != null) {
                claims.add(claimInfo);
            }
        }
        return claims;
    }

    @Override
    public ClaimInfo getClaim(long claimId) {
        File file = new File(folder, claimId + ".json");
        if (!file.exists()) {
            return null;
        }
        try {
            String contents = new String(Files.readAllBytes(file.toPath()));
            return GriefPreventionTP.getInstance().getGson().fromJson(contents, ClaimInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Claim claim = GriefPrevention.instance.dataStore.getClaim(claimId);
        return new ClaimInfo(claimId, claim.getOwnerID());
    }

    @Override
    public ClaimInfo fromClaim(Claim claim) {
        return getClaim(claim.getID());
    }

    @Override
    public Collection<ClaimInfo> getPublicClaims() {
        List<ClaimInfo> claims = new ArrayList<>();
        for (ClaimInfo allClaim : getAllClaims()) {
            if (allClaim.isPublic())
                claims.add(allClaim);
        }
        return claims;
    }

}
