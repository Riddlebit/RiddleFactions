package net.riddlebit.mc.controller;

import dev.morphia.query.Query;
import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.*;
import org.bukkit.entity.Player;

import java.util.*;

public class DataManager {

    public RiddleFactions plugin;

    public Map<String, PlayerData> players;
    public Map<String, FactionData> factions;
    public Set<TreasureData> treasures;
    public Set<Invite> invites;

    public DataManager(RiddleFactions plugin) {
        this.plugin = plugin;
        players = new HashMap<>();
        factions = new HashMap<>();
        treasures = new HashSet<>();
        invites = new HashSet<>();

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
        plugin.datastore.save(playerData);
    }

    public void addFactionData(FactionData factionData) {
        factions.put(factionData.name, factionData);
        plugin.datastore.save(factionData);
    }

    public void deleteFaction(FactionData factionData) {
        factions.remove(factionData.name);
        plugin.datastore.delete(factionData);
    }

    public void addTreasureData(TreasureData treasureData) {
        treasures.add(treasureData);
        plugin.datastore.save(treasureData);
    }

    public void removeTreasureData(TreasureData treasureData) {
        treasures.remove(treasureData);
        plugin.datastore.delete(treasureData);
    }

    private void loadPlayers() {
        Query<PlayerData> query = plugin.datastore.createQuery(PlayerData.class);
        List<PlayerData> loadedPlayers = query.find().toList();
        if (loadedPlayers.size() > 0) {
            for (PlayerData playerData : loadedPlayers) {
                players.put(playerData.uuid, playerData);
            }
        }
    }

    private void loadFactions() {
        Query<FactionData> query = plugin.datastore.createQuery(FactionData.class);
        List<FactionData> loadedFactions = query.find().toList();
        if (loadedFactions.size() > 0) {
            for (FactionData factionData : loadedFactions) {
                factions.put(factionData.name, factionData);
                for (PlayerData playerData : factionData.players) {
                    players.put(playerData.uuid, playerData);
                }
            }
        }
    }

    private void loadTreasures() {
        Query<TreasureData> query = plugin.datastore.createQuery(TreasureData.class);
        List<TreasureData> loadedTreasures = query.find().toList();
        for (TreasureData treasureData : loadedTreasures) {
            treasures.add(treasureData);
        }
    }

    public void save() {
        plugin.getLogger().info("Saving to database");
        for (FactionData factionData : factions.values()) {
            plugin.datastore.save(factionData);
        }
        for (PlayerData playerData : players.values()) {
            plugin.datastore.save(playerData);
        }
        for (TreasureData treasureData : treasures) {
            plugin.datastore.save(treasureData);
        }
    }

}
