package dev.badbird.griefpreventiontp.data.impl;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.ClaimInfo;
import dev.badbird.griefpreventiontp.data.StorageProvider;
import dev.badbird.griefpreventiontp.object.FilterOptions;
import lombok.SneakyThrows;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

public class H2StorageProvider implements StorageProvider {
    private Connection connection;

    @SneakyThrows
    @Override
    public void init(GriefPreventionTP plugin) {

    }
    @SneakyThrows
    public void connect() {
        if (connection != null) {
            if (connection.isClosed()) {
                connection = createConnection();
                Bukkit.getLogger().info("Reconnected to H2 database");
            }
        } else {
            connection = createConnection();
            Bukkit.getLogger().info("Connected to H2 database");
        }
    }

    @SneakyThrows
    private Connection createConnection() {
        Class.forName("org.h2.Driver");
        File file = new File(GriefPreventionTP.getInstance().getDataFolder(), "claims.db");
        return DriverManager.getConnection("jdbc:h2:" + file.getAbsolutePath());
    }

    public void initTable(String table, List<String> columns) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ").append(table).append(" (");
        for (String column : columns) {
            builder.append(column).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(");");
        String sql = builder.toString();
        execute(sql);
    }

    @SneakyThrows
    private ResultSet execute(String sql) {
        connect();
        return connection.createStatement().executeQuery(sql);
    }

    @Override
    public void disable(GriefPreventionTP plugin) {

    }

    @Override
    public void saveClaims(Collection<ClaimInfo> claims) {

    }

    @Override
    public void saveClaim(ClaimInfo claim) {

    }

    @Override
    public Collection<ClaimInfo> getAllClaims() {
        return null;
    }

    @Override
    public ClaimInfo getClaim(long claimId) {
        return null;
    }

    @Override
    public ClaimInfo fromClaim(Claim claim) {
        return null;
    }

    @Override
    public Collection<ClaimInfo> getPublicClaims() {
        return null;
    }

    @Override
    public int getTotalClaims(FilterOptions options) {
        return 0;
    }

    @Override
    public Collection<ClaimInfo> getClaims(FilterOptions options, int max, int page) {
        return null;
    }
}
