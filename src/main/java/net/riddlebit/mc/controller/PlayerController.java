package net.riddlebit.mc.controller;

import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.ChunkData;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

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

    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Chunk chunk = block.getChunk();
        Player player = event.getPlayer();

        FactionData factionData = plugin.factionController.getFactionForPlayer(player);
        ChunkData chunkData = new ChunkData(chunk.getX(), chunk.getZ());
        FactionData chunkFactionData = plugin.factionController.getChunkOwner(chunkData);

        if (chunkFactionData != null && (factionData == null || !factionData.equals(chunkFactionData))) {
            // Player is not in this faction -> cancel block placement
            event.setCancelled(true);
        } else {
            if (player.getWorld() == Bukkit.getWorlds().get(0)) {
                if (plugin.treasureController.addTreasure(block)) {
                    player.sendMessage("Treasure placed!");
                }
            }
        }

    }

    public void onPlayerBreakBlock(BlockBreakEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Player player = event.getPlayer();

        FactionData factionData = plugin.factionController.getFactionForPlayer(player);
        ChunkData chunkData = new ChunkData(chunk.getX(), chunk.getZ());
        FactionData chunkFactionData = plugin.factionController.getChunkOwner(chunkData);

        if (chunkFactionData != null && (factionData == null || !factionData.equals(chunkFactionData))) {
            // Player is not in this faction -> cancel block break
            event.setCancelled(true);
        } else {
            if (player.getWorld() == Bukkit.getWorlds().get(0)) {
                if (plugin.treasureController.removeTreasure(block)) {
                    player.sendMessage("Treasure removed!");
                }
            }
        }
    }

    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerData playerData = dataManager.getPlayerData(player);

        float reputation = playerData.reputation;
        playerData.reputation = reputation * 0.5f;
        float lostReputation = reputation - playerData.reputation;
        player.sendMessage("You lost " + (int) lostReputation + " reputation...");

        Player killer = player.getKiller();
        if (killer != null) {
            PlayerData killerPlayerData = dataManager.getPlayerData(killer);
            float gainedReputation = lostReputation * 0.5f;
            killerPlayerData.reputation += gainedReputation;
            killer.sendMessage("You gained " + (int) gainedReputation + " reputation!");
        }
    }

}
