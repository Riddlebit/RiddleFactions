package net.riddlebit.mc.controller;

import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.*;
import net.riddlebit.mc.data.store.RFDatastore;
import net.riddlebit.mc.data.store.RFJsonStore;
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

    private RFDatastore datastore;

    public DataManager(RiddleFactions plugin) {
        this.plugin = plugin;
        players = new HashMap<>();
        factions = new HashMap<>();
        treasures = new HashSet<>();
        invites = new HashSet<>();

        String dataStoreType = plugin.config.getString("datastore");
        switch (dataStoreType.trim().toLowerCase()) {
            case "json":
                datastore = new RFJsonStore(plugin.getDataFolder());
                break;
            default:
                plugin.getLogger().severe("Invalid datastore in config: " + dataStoreType);
        }

        if (datastore == null) {
            plugin.getLogger().severe("Datastore is null!");
        }

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
        datastore.savePlayerData(playerData);
    }

    public void addFactionData(FactionData factionData) {
        factions.put(factionData.name, factionData);
        datastore.saveFactionData(factionData);
    }

    public void deleteFaction(FactionData factionData) {
        factions.remove(factionData.name);
        datastore.deleteFactionData(factionData);
    }

    public void addTreasureData(TreasureData treasureData) {
        treasures.add(treasureData);
        datastore.saveTreasureData(treasureData);
    }

    public void removeTreasureData(TreasureData treasureData) {
        treasures.remove(treasureData);
        datastore.deleteTreasure(treasureData);
    }

    private void loadPlayers() {
        List<PlayerData> loadedPlayers = datastore.loadPlayers();
        if (loadedPlayers != null && loadedPlayers.size() > 0) {
            for (PlayerData playerData : loadedPlayers) {
                players.put(playerData.uuid, playerData);
            }
        }
    }

    private void loadFactions() {
        List<FactionData> loadedFactions = datastore.loadFactions();
        if (loadedFactions != null && loadedFactions.size() > 0) {
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
        List<TreasureData> loadedTreasures = datastore.loadTreasures();
        if (loadedTreasures != null) {
            treasures.addAll(loadedTreasures);
        }
    }

    public void save() {
        plugin.getLogger().info("Saving to database");
        for (FactionData factionData : factions.values()) {
            datastore.saveFactionData(factionData);
        }
        for (PlayerData playerData : players.values()) {
            datastore.savePlayerData(playerData);
        }
        for (TreasureData treasureData : treasures) {
            datastore.saveTreasureData(treasureData);
        }
    }

}
