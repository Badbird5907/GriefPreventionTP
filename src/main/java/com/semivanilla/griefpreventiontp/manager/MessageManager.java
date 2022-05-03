package com.semivanilla.griefpreventiontp.manager;

import com.semivanilla.griefpreventiontp.GriefPreventionTP;
import net.badbird5907.blib.utils.StringUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class MessageManager {
    public static void sendMessage(Player player, String key, String... placeholders) {
        String raw = GriefPreventionTP.getInstance().getConfig().getString(key);
        String msg = StringUtils.replacePlaceholders(raw, placeholders);
        Component component = GriefPreventionTP.getInstance().getMiniMessage().deserialize(msg);
        player.sendMessage(component);
    }
}
