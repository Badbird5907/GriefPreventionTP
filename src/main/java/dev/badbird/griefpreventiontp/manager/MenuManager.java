package dev.badbird.griefpreventiontp.manager;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import lombok.Getter;
import net.badbird5907.blib.util.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuManager {
    @Getter
    private static final MenuManager instance = new MenuManager();
    private Map<String, FileConfiguration> menus = new HashMap<>();
    public void init() {
        menus.clear();
        File dataFolder = new File(GriefPreventionTP.getInstance().getDataFolder(), "menus");
        if (!dataFolder.exists()) {
            Logger.info("Creating menus folder");
            dataFolder.mkdirs();
        }
        /*
        for (File file : dataFolder.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                menus.put(file.getName().replace(".yml", ""), config);
            }
        }
         */
        String[] files = {
          "claims", "confirm", "manage-claim"
        };
        for (String file : files) {
            File f = new File(dataFolder, file + ".yml");
            if (!f.exists()) {
                GriefPreventionTP.getInstance().saveResource("menus/" + file + ".yml", false);
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(f);
            menus.put(file, config);
            Logger.info("Loaded menu " + file);
        }
        menus.put("", GriefPreventionTP.getInstance().getConfig());
        Logger.info("Loaded " + menus.size() + " menus");
    }
    public static FileConfiguration getMenu(String name) {
        return instance.menus.get(name);
    }
    public static String getString(String menu, String path, String def) {
        return getMenu(menu).getString(path, def);
    }
    public static boolean getBoolean(String menu, String path, boolean def) {
        return getMenu(menu).getBoolean(path, def);
    }
}
