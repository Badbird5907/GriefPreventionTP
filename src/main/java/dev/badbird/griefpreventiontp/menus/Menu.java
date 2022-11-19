package dev.badbird.griefpreventiontp.menus;

import dev.badbird.griefpreventiontp.object.config.MenuConfig;
import org.bukkit.entity.HumanEntity;

public abstract class Menu {
    public abstract void open(HumanEntity player);

    public abstract MenuConfig.Menu getMenuType();
}
