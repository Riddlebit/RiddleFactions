package net.riddlebit.mc.controller;

import dev.morphia.query.Query;
import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.PlayerData;
import org.bukkit.entity.Player;

import java.util.*;

public class FactionController {

    private RiddleFactions plugin;

    private HashMap<String, FactionData> factions;

    public FactionController(RiddleFactions plugin) {
        this.plugin = plugin;
        factions = new HashMap<>();

        // Load factions from database
        Query query = plugin.datastore.createQuery(FactionData.class);
        List<FactionData> loadedFactions = query.find().toList();
        if (loadedFactions.size() > 0) {
            for (FactionData factionData : loadedFactions) {
                factions.put(factionData.name, factionData);
            }
        }

    }

    public boolean createFaction(String factionName, Player player) {

        if (isPlayerInFaction(player)) {
            player.sendMessage("You're already in a faction...");
            return true;
        }

        if (factions.containsKey(factionName)) {
            player.sendMessage("A faction with that name already exists");
            return true;
        }

        // Create the new faction
        PlayerData playerData = plugin.playerController.getPlayer(player);
        FactionData factionData = new FactionData(playerData);
        factionData.name = factionName;
        factions.put(factionName, factionData);
        plugin.datastore.save(factionData);

        player.sendMessage(factionName + " was created!");
        return true;
    }

    public boolean isPlayerInFaction(Player player) {
        PlayerData playerData = plugin.playerController.getPlayer(player);
        for (FactionData factionData : factions.values()) {
            if (factionData.players.contains(playerData)) return true;
        }
        return false;
    }

}
