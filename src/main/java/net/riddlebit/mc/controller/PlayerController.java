package net.riddlebit.mc.controller;

import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.PlayerData;
import org.bukkit.entity.Player;

public class PlayerController {

    private RiddleFactions plugin;
    private DataManager dataManager;

    public PlayerController(RiddleFactions plugin) {
        this.plugin = plugin;
        dataManager = plugin.dataManager;
    }

    public PlayerData addPlayer(Player player) {
        PlayerData playerData = dataManager.getPlayerData(player);
        if (playerData == null) {
            playerData = new PlayerData();
            playerData.uuid = player.getUniqueId().toString();
            playerData.name = player.getDisplayName();
            playerData.reputation = 0;
            dataManager.addPlayerData(playerData);
        }
        return playerData;
    }

}
