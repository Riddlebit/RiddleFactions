package net.riddlebit.mc.controller;

import dev.morphia.query.Query;
import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerController {

    private RiddleFactions plugin;

    private HashMap<UUID, PlayerData> players;

    public PlayerController(RiddleFactions plugin) {
        this.plugin = plugin;
        players = new HashMap<>();
    }

    public PlayerData addPlayer(Player player) {
        if (players.containsKey(player.getUniqueId())) {
            return players.get(player.getUniqueId());
        }

        Query<PlayerData> query = plugin.datastore.createQuery(PlayerData.class);
        PlayerData playerData = query.field("uuid").equal(player.getUniqueId().toString()).first();
        if (playerData == null) {
            playerData = new PlayerData();
            playerData.uuid = player.getUniqueId().toString();
            playerData.name = player.getDisplayName();
            playerData.reputation = 0;
            plugin.datastore.save(playerData);
        }

        players.put(player.getUniqueId(), playerData);

        return playerData;
    }

    public PlayerData getPlayer(Player player) {
        return players.get(player.getUniqueId());
    }

}
