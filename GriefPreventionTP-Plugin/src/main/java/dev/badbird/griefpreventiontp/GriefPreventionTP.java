package dev.badbird.griefpreventiontp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.badbird.griefpreventiontp.commands.impl.*;
import dev.badbird.griefpreventiontp.commands.provider.PlayerDataProvider;
import dev.badbird.griefpreventiontp.data.StorageProvider;
import dev.badbird.griefpreventiontp.data.impl.FlatFileStorageProvider;
import dev.badbird.griefpreventiontp.listener.ClaimListener;
import dev.badbird.griefpreventiontp.manager.PermissionsManager;
import dev.badbird.griefpreventiontp.manager.TPClaimManager;
import dev.badbird.griefpreventiontp.manager.TeleportManager;
import dev.badbird.griefpreventiontp.util.AdventureUtil;
import dev.badbird.griefpreventiontp.util.Metrics;
import lombok.Getter;
import lombok.Setter;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.badbird5907.blib.bLib;
import net.badbird5907.blib.spigotmc.UpdateChecker;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.permission.Permission;
import net.octopvp.commander.Commander;
import net.octopvp.commander.bukkit.BukkitCommander;
import org.bukkit.Bukkit;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

public final class GriefPreventionTP extends JavaPlugin {
    @Getter
    private static GriefPreventionTP instance;

    @Getter
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    private StorageProvider storageProvider;

    @Getter
    @Setter
    private boolean disabled = false;
    @Getter
    @Setter
    private String disabledReason = "";

    @Getter
    private TPClaimManager claimManager;

    @Getter
    private TeleportManager teleportManager;

    @Getter
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Getter
    private final ConversationFactory conversationFactory = new ConversationFactory(this);

    @Getter
    private PermissionsManager permissionsManager;

    @Getter
    private Commander commander;

    @Getter
    private UpdateChecker updateChecker = null;

    @Getter
    private boolean updateAvailable = false;

    @Getter
    private String newVersion = "";

    @Getter
    private Permission vaultPermissions;

    @Getter
    private boolean useVault = false;

    @Getter
    private static String USER = "%%__USER__%%", RESOURCE = "%%__RESOURCE__%%", NONCE = "%%__NONCE__%%";

    @Override
    public void onLoad() {
        boolean bruh = !getDescription().getName().equals("GriefPreventionTP") || !getDescription().getWebsite().equals("https://badbird.dev");
        if (getDescription().getAuthors().size() < 1) bruh = true;
        else if (!getDescription().getAuthors().get(0).equals("Badbird5907")) bruh = true;
        if (bruh) {
            getLogger().severe("Please do not modify the plugin! To receive help, join the support server @ https://discord.badbird.dev/");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("GriefPreventionTP v" + getDescription().getVersion() + " By Badbird5907, Licensed to User: " + USER + " | ID: " + NONCE);

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
                        new TPCommand());
        if (getConfig().getBoolean("enable-public"))
            commander.register(new PublicCommand());

        this.storageProvider = new FlatFileStorageProvider();
        this.storageProvider.init(this);

        this.claimManager = new TPClaimManager();
        this.claimManager.init();

        this.teleportManager = new TeleportManager(this);


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
