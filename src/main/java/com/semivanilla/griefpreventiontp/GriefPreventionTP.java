package com.semivanilla.griefpreventiontp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.semivanilla.griefpreventiontp.data.StorageProvider;
import com.semivanilla.griefpreventiontp.data.impl.FlatFileStorageProvider;
import com.semivanilla.griefpreventiontp.listener.ClaimListener;
import com.semivanilla.griefpreventiontp.manager.TPClaimManager;
import com.semivanilla.griefpreventiontp.manager.TeleportManager;
import com.semivanilla.griefpreventiontp.object.ClaimInfo;
import lombok.Getter;
import lombok.Setter;
import net.badbird5907.blib.bLib;
import net.badbird5907.blib.util.StoredLocation;
import net.badbird5907.blib.util.Tasks;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

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
    private MiniMessage miniMessage = MiniMessage.miniMessage();

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

        bLib.getCommandFramework().registerCommandsInPackage("com.semivanilla.griefpreventiontp.commands");

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
    }

    @Override
    public void onDisable() {
        if (claimManager != null) {
            claimManager.stop();
        }

        if (this.storageProvider != null) {
            this.storageProvider.disable(this);
        }
    }
}
