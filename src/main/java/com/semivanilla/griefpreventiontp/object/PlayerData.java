package com.semivanilla.griefpreventiontp.object;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
@Setter
public class PlayerData {
    private final UUID uuid;
    private String name;


    public PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    public PlayerData(UUID uuid) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        this.uuid = uuid;
        this.name = op.getName();
    }

}
