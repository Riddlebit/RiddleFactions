package net.riddlebit.mc.controller;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import java.util.*;

public class DataManager {

    public RiddleFactions plugin;

    public Map<String, PlayerData> players;
    public Map<String, FactionData> factions;
    public Set<TreasureData> treasures;
    public Set<Invite> invites;

    private Datastore datastore;

    public DataManager(RiddleFactions plugin) {
        this.plugin = plugin;
        players = new HashMap<>();
        factions = new HashMap<>();
        treasures = new HashSet<>();
        invites = new HashSet<>();

        // Database stuff
        Morphia morphia = new Morphia();
        morphia.map(PlayerData.class);
        morphia.map(FactionData.class);

        String dbHost = plugin.config.getString("db-host");
        String dbName = plugin.config.getString("db-name");
        datastore = morphia.createDatastore(new MongoClient(dbHost), dbName);
        datastore.ensureIndexes();

        // Load from database
        loadPlayers();
        loadFactions();
        loadTreasures();
    }

    public PlayerData getPlayerData(Player player) {
        return players.get(player.getUniqueId().toString());
    }

    public void addPlayerData(PlayerData playerData) {
        players.put(playerData.uuid, playerData);
        datastore.save(playerData);
    }

    public void addFactionData(FactionData factionData) {
        factions.put(factionData.name, factionData);
        datastore.save(factionData);
    }

    public void deleteFaction(FactionData factionData) {
        factions.remove(factionData.name);
        datastore.delete(factionData);
    }

    public void addTreasureData(TreasureData treasureData) {
        treasures.add(treasureData);
        datastore.save(treasureData);
    }

    public void removeTreasureData(TreasureData treasureData) {
        treasures.remove(treasureData);
        datastore.delete(treasureData);
    }

    private void loadPlayers() {
        Query<PlayerData> query = datastore.createQuery(PlayerData.class);
        List<PlayerData> loadedPlayers = query.find().toList();
        if (loadedPlayers.size() > 0) {
            for (PlayerData playerData : loadedPlayers) {
                players.put(playerData.uuid, playerData);
            }
        }
    }

    private void loadFactions() {
        Query<FactionData> query = datastore.createQuery(FactionData.class);
        List<FactionData> loadedFactions = query.find().toList();
        if (loadedFactions.size() > 0) {
            for (FactionData factionData : loadedFactions) {
                factions.put(factionData.name, factionData);
                for (PlayerData playerData : factionData.players) {
                    players.put(playerData.uuid, playerData);
                }
                factionData.bossBar = plugin.getServer().createBossBar(factionData.name, BarColor.WHITE, BarStyle.SOLID);
            }
        }
    }

    private void loadTreasures() {
        Query<TreasureData> query = datastore.createQuery(TreasureData.class);
        List<TreasureData> loadedTreasures = query.find().toList();
        treasures.addAll(loadedTreasures);
    }

    public void save() {
        plugin.getLogger().info("Saving to database");
        for (FactionData factionData : factions.values()) {
            datastore.save(factionData);
        }
        for (PlayerData playerData : players.values()) {
            datastore.save(playerData);
        }
        for (TreasureData treasureData : treasures) {
            datastore.save(treasureData);
        }
    }

}
