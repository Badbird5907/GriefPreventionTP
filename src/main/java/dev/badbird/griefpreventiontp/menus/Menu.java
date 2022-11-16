package dev.badbird.griefpreventiontp.menus;

import dev.badbird.griefpreventiontp.object.config.MenuConfig;
import org.bukkit.entity.Player;

public abstract class Menu {
    public abstract void open(Player player);

    public abstract MenuConfig.Menu getMenuType();
}
