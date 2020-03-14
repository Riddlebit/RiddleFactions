package net.riddlebit.mc.controller;

import dev.morphia.query.Query;
import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.Invite;
import net.riddlebit.mc.data.PlayerData;
import org.bukkit.entity.Player;

import java.util.*;

public class DataManager {

    public RiddleFactions plugin;

    public Map<String, PlayerData> players;
    public Map<String, FactionData> factions;
    public Set<Invite> invites;

    public DataManager(RiddleFactions plugin) {
        this.plugin = plugin;
        players = new HashMap<>();
        factions = new HashMap<>();
        invites = new HashSet<>();

        // Load from database
        loadPlayers();
        loadFactions();
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

    public void save() {
        plugin.getLogger().info("Saving to database");
        for (FactionData factionData : factions.values()) {
            plugin.datastore.save(factionData);
        }
        for (PlayerData playerData : players.values()) {
            plugin.datastore.save(playerData);
        }
    }

    public void deleteFaction(FactionData factionData) {
        factions.remove(factionData.name);
        plugin.datastore.delete(factionData);
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

}
