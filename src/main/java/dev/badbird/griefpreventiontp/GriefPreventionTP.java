package dev.badbird.griefpreventiontp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.badbird.griefpreventiontp.commands.impl.*;
import dev.badbird.griefpreventiontp.commands.provider.PlayerDataProvider;
import dev.badbird.griefpreventiontp.data.StorageProvider;
import dev.badbird.griefpreventiontp.data.impl.FlatFileStorageProvider;
import dev.badbird.griefpreventiontp.listener.ClaimListener;
import dev.badbird.griefpreventiontp.manager.MenuManager;
import dev.badbird.griefpreventiontp.manager.PermissionsManager;
import dev.badbird.griefpreventiontp.manager.TPClaimManager;
import dev.badbird.griefpreventiontp.manager.TeleportManager;
import dev.badbird.griefpreventiontp.api.IconWrapper;
import dev.badbird.griefpreventiontp.util.AdventureUtil;
import dev.badbird.griefpreventiontp.util.IconWrapperSerializer;
import dev.badbird.griefpreventiontp.util.Metrics;
import lombok.Getter;
import lombok.Setter;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.badbird5907.blib.bLib;
import net.badbird5907.blib.spigotmc.UpdateChecker;
import net.badbird5907.blib.util.Tasks;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.permission.Permission;
import net.octopvp.commander.Commander;
import net.octopvp.commander.bukkit.BukkitCommander;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

@Getter
public final class GriefPreventionTP extends JavaPlugin {
    @Getter
    private static GriefPreventionTP instance;

    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(IconWrapper.class, new IconWrapperSerializer())
            .setPrettyPrinting().create();

    private StorageProvider storageProvider;

    @Setter
    private boolean disabled = false;
    @Setter
    private String disabledReason = "";
    private TPClaimManager claimManager;
    private TeleportManager teleportManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final ConversationFactory conversationFactory = new ConversationFactory(this);
    private PermissionsManager permissionsManager;
    private Commander commander;
    private UpdateChecker updateChecker = null;
    private boolean updateAvailable = false;
    private String newVersion = "";
    private Permission vaultPermissions;
    private boolean useVault = false;
    @Getter
    private static String USER = "%%__USER__%%", RESOURCE = "%%__RESOURCE__%%", NONCE = "%%__NONCE__%%";
    @Getter
    private static List<IconWrapper> allowedIcons;

    @Override
    public void onLoad() {
        getLogger().info("GriefPreventionTP v" + getDescription().getVersion() + " By Badbird5907");

        instance = this;
    }

    @Override
    public void onEnable() {
        instance = this;
        bLib.create(this);
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
        this.permissionsManager = new PermissionsManager(this);
        try {
            Metrics metrics = new Metrics(this, 15417);
        } catch (Exception e) {
            // weird error
        }

        AdventureUtil.init();

        if (getConfig().getBoolean("update-check")) {
            updateChecker = new UpdateChecker(102521);
            updateChecker.getVersion(version -> {
                if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
                    updateAvailable = true;
                    newVersion = version;
                    getLogger().info("There a new update available! Download at https://s.badbird.dev/gptp?ref=console");
                }
                //just dont say anything
            });
        }

        commander = BukkitCommander.getCommander(this)
                .registerProvider(PlayerData.class, new PlayerDataProvider())
                .registerDependency(GriefPreventionTP.class, this)
                .registerDependency(TPClaimManager.class, claimManager)
                .registerDependency(TeleportManager.class, teleportManager)
                .registerDependency(StorageProvider.class, storageProvider)
                .registerDependency(MiniMessage.class, miniMessage)
                .registerDependency(ConversationFactory.class, conversationFactory)
                .registerDependency(PermissionsManager.class, permissionsManager)
                .register(new ClaimsCommand(),
                        new GPTPCommand(),
                        new RenameCommand(),
                        new SetSpawnCommand(),
                        new TPCommand(),
                        new ManageClaimsCommand()
                );
        if (getConfig().getBoolean("enable-public"))
            commander.register(new PublicCommand());

        // allowedIcons = getConfig().getStringList("icons").stream().map(Material::valueOf).toList();
        allowedIcons = new ArrayList<>();
        if (getConfig().isList("icons")) {
            List<?> iconsList = getConfig().getList("icons");
            // getLogger().info("Loading icons");
            for (Object obj : iconsList) {
                // getLogger().info(" - Found obj: " + obj.getClass().getName());
                if (obj instanceof LinkedHashMap) {
                    LinkedHashMap<?,?> map1 = (LinkedHashMap<?,?>) obj;
                    LinkedHashMap<?,?> map = (LinkedHashMap<?, ?>) map1.get("head");
                    // map.keySet().forEach(key -> getLogger().info("  - Found key: " + key + " | " + key.getClass().getName() + " | " + map.get(key) + " | " + map.get(key).getClass().getName()));
                    if (map.get("texture") != null) {
                        String texture = (String) map.get("texture");
                        // getLogger().info("  - Found texture: " + texture);
                        if (texture.equals("e...")) {
                            continue;
                        }
                        String id = (String) map.get("id");
                        String name = (String) map.get("name");
                        if (name ==  null) name = "";
                        // getLogger().info("  - Found id: " + id);
                        // getLogger().info("  - Found name: " + name);
                        allowedIcons.add(new IconWrapper(new IconWrapper.CustomIcon(id, name, texture)));
                    }
                } else if (obj instanceof String) {
                    Material material = Material.getMaterial((String) obj);
                    if (material != null) {
                        // getLogger().info("  - Found material: " + material);
                        allowedIcons.add(new IconWrapper(material));
                    } else {
                        getLogger().warning("Invalid material: " + obj);
                    }
                }
            }
        }
        getLogger().info("Loaded " + allowedIcons.size() + " icons.");

        this.storageProvider = new FlatFileStorageProvider();
        this.storageProvider.init(this);

        this.claimManager = new TPClaimManager();
        this.claimManager.init();

        this.teleportManager = new TeleportManager(this);

        MenuManager.getInstance().init();


        Listener[] listeners = {
                new ClaimListener()
        };

        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            vaultPermissions = rsp.getProvider();
            useVault = true;
        }

        /*
        Tasks.runAsyncTimer(() -> {
            for (ClaimInfo claim : claimManager.getAllClaims()) {
                if (claim.getSpawn() != null) {
                    StoredLocation loc = claim.getSpawn().center();
                    //Location l = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
                    //spawn particle in a circle around the center
                    for (double i = 0; i < Math.PI * 2; i += Math.PI / 20) {
                        Location particleLoc = new Location(loc.getWorld(), loc.getX() + Math.cos(i), loc.getY(), loc.getZ() + Math.sin(i));
                        //normalize the location to the center of the block
                        particleLoc.getWorld().spawnParticle(Particle.REDSTONE, particleLoc, 1, 0, 0, 0, 1, new Particle.DustOptions(Color.RED, 1F));
                    }
                    //   for (int i = 0; i < 360; i++) {
                    //     double angle = i;
                    //     double x = loc.getX() + Math.cos(Math.toRadians(angle)) * 0.5;
                    //     double z = loc.getZ() + Math.sin(Math.toRadians(angle)) * 0.5;
                    //     Location particleLoc = new Location(loc.getWorld(), x, loc.getY(), z);
                    //
                    //     new ParticleBuilder(Particle.REDSTONE)
                    //             .color(Color.RED)
                    //             .count(1)
                    //             .offset(x > 0 ? 0.5 : -0.5,0,z > 0 ? 0.5 : -0.5)
                    //             .count(0)
                    //             .location(particleLoc)
                    //             .spawn();
                    //     //l.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, p, p.get(0), l.getX(), l.getY(), l.getZ(), 10, 0d, 0d, 0d, 0d, null);
                    //     //l.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, p, p.get(0), particleLoc.getX(), particleLoc.getY(), particleLoc.getZ(), 1, 0.5, 0.5, 0.5, 0.1, null);
                    // }
                }
            }
        }, 20l, 10l);
         */
        getLogger().info("Please ignore any warnings about deprecated listeners. It is a bug (https://github.com/TechFortress/GriefPrevention/issues/1791#issuecomment-1312809553)");

        boolean disableMsg = Boolean.getBoolean("gptp.disableSponsorMsg");
        if (!disableMsg) {
            if (USER.startsWith("%%")) {
                Tasks.runLater(() -> {
                    getLogger().severe("------------------------------------------------");
                    getLogger().severe("Please consider supporting me by purchasing the plugin at https://qrt.badbird.dev/gptp?ref=csp");
                    getLogger().severe("Or by sponsoring me at https://sponsor.badbird.dev/");
                    getLogger().severe("To disable this message in the future, include -Dgptp.disableSponsorMsg=true before the -jar flag in your startup script.");
                    getLogger().severe("------------------------------------------------");
                }, 80);
            } else {
                getLogger().info("Thank you for purchasing GriefPreventionTP! <3");
            }
        }
    }

    @Override
    public void onDisable() {
        if (claimManager != null) {
            claimManager.stop();
        }

        if (this.storageProvider != null) {
            this.storageProvider.disable(this);
        }

        AdventureUtil.adventure().close();
    }

    @Override
    public @NotNull Logger getLogger() {
        return super.getLogger();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (permissionsManager != null)
            permissionsManager.reload(this);
    }
}
