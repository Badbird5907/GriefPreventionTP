package dev.badbird.griefpreventiontp.data.impl;

import com.google.gson.reflect.TypeToken;
import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.data.StorageProvider;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import net.badbird5907.blib.util.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

public class FlatFileStorageProvider implements StorageProvider {
    private File folder;

    @Override
    public void init(GriefPreventionTP plugin) {
        folder = new File(plugin.getDataFolder(), "data");
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    @Override
    public void disable(GriefPreventionTP plugin) {

    }
    @Override
    public void saveClaims(Collection<ClaimInfo> claims) {
        File claimsFile = new File(GriefPreventionTP.getInstance().getDataFolder(), "claims.json");
        if (!claimsFile.exists()) {
            try {
                claimsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            ArrayList<ClaimInfo> claimInfos = new ArrayList<>(claims);
            PrintStream ps = new PrintStream(claimsFile);
            ps.print(GriefPreventionTP.getInstance().getGson().toJson(claimInfos));
            ps.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<ClaimInfo> getClaims() {
        File claimsFile = new File(GriefPreventionTP.getInstance().getDataFolder(), "claims.json");
        if (!claimsFile.exists()) {
            return new ArrayList<>();
        }
        try {
            String contents = new String(Files.readAllBytes(claimsFile.toPath()));
            ArrayList<ClaimInfo> claimInfos = GriefPreventionTP.getInstance().getGson().fromJson(contents, new TypeToken<ArrayList<ClaimInfo>>() {
            }.getType());
            return claimInfos;
        } catch (IOException e) {
            e.printStackTrace();
        }
        GriefPreventionTP.getInstance().setDisabled(true);
        GriefPreventionTP.getInstance().setDisabledReason("Failed to load claims");
        return new ArrayList<>();
    }

}
