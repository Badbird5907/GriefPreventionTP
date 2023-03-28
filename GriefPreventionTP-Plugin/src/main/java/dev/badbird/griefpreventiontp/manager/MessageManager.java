package dev.badbird.griefpreventiontp.manager;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.util.AdventureUtil;
import net.badbird5907.blib.utils.StringUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class MessageManager {
    public static void sendMessage(Player player, String key, Object... placeholders) {
        AdventureUtil.sendMessage(player, getComponent(key, placeholders));
    }

    public static Component getComponent(String key, Object... placeholders) {
        String raw = GriefPreventionTP.getInstance().getConfig().getString(key);
        String msg = (placeholders == null || placeholders.length == 0) ? raw : StringUtils.replacePlaceholders(raw, placeholders);
        assert msg != null;
        return GriefPreventionTP.getInstance().getMiniMessage().deserialize(msg);
    }
}
