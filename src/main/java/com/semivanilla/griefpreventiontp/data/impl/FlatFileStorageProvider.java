package com.semivanilla.griefpreventiontp.data.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.semivanilla.griefpreventiontp.GriefPreventionTP;
import com.semivanilla.griefpreventiontp.data.StorageProvider;
import com.semivanilla.griefpreventiontp.object.ClaimInfo;
import com.semivanilla.griefpreventiontp.object.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

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
    public void save(PlayerData data) {
        File dataFile = new File(folder, data.getUuid() + ".json");
        JsonObject json = new JsonObject();
        json.addProperty("uuid", data.getUuid() + "");
        json.addProperty("name", data.getName());
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            PrintStream ps = new PrintStream(dataFile);
            ps.print(GriefPreventionTP.getInstance().getGson());
            ps.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerData getData(String playerName) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
        return getData(op.getUniqueId());
    }

    @Override
    public PlayerData getData(UUID uuid) {
        File dataFile = new File(folder, uuid + ".json");
        if (!dataFile.exists()) {
            return null;
        }
        try {
            String contents = new String(Files.readAllBytes(dataFile.toPath()));
            JsonObject json = JsonParser.parseString(contents).getAsJsonObject();
            return new PlayerData(UUID.fromString(json.get("uuid").getAsString())).load(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
