package net.riddlebit.mc.controller;

import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.ChunkData;
import net.riddlebit.mc.data.ChunkType;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

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
            boolean cancelEvent = true;
            for (Material material : plugin.treasureController.getAllTreasureMaterials()) {
                // Intruders can break treasures!
                if (block.getType().equals(material)) {
                    cancelEvent = false;
                }
            }
            event.setCancelled(cancelEvent);
        }
        plugin.treasureController.removeTreasure(block);
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

    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getPlayer().getWorld() != Bukkit.getWorlds().get(0)) return;
        if (event.getTo() == null) return;
        Player player = event.getPlayer();

        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();

        ChunkData fromChunkData = new ChunkData(fromChunk.getX(), fromChunk.getZ());
        ChunkData toChunkData = new ChunkData(toChunk.getX(), toChunk.getZ());

        ChunkType fromChunkType = plugin.factionController.getChunkType(fromChunkData);
        ChunkType toChunkType = plugin.factionController.getChunkType(toChunkData);

        if (fromChunkType != toChunkType) {
            switch (toChunkType) {
                case CLAIMED:
                    FactionData factionData = plugin.factionController.getChunkOwner(toChunkData);
                    player.sendMessage("Entering " + factionData.name + " territory!");
                    break;
                case BORDER:
                    factionData = plugin.factionController.chunkBordersToFaction(toChunkData);
                    if (fromChunkType == ChunkType.WILDERNESS) {
                        player.sendMessage("Approaching " + factionData.name + " territory!");
                    } else {
                        player.sendMessage("Leaving " + factionData.name + " territory");
                    }
                    break;
                case WILDERNESS:
                    player.sendMessage("Entering wilderness");
            }
        }
    }

    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;

        Chunk chunk = block.getChunk();
        Player player = event.getPlayer();

        FactionData factionData = plugin.factionController.getFactionForPlayer(player);
        ChunkData chunkData = new ChunkData(chunk.getX(), chunk.getZ());
        FactionData chunkFactionData = plugin.factionController.getChunkOwner(chunkData);

        if (chunkFactionData != null && (factionData == null || !factionData.equals(chunkFactionData))) {
            // Player is not in this faction -> cancel block break
            switch (block.getType()) {
                case OAK_DOOR:
                case BIRCH_DOOR:
                case ACACIA_DOOR:
                case JUNGLE_DOOR:
                case SPRUCE_DOOR:
                case DARK_OAK_DOOR:
                case OAK_TRAPDOOR:
                case BIRCH_TRAPDOOR:
                case ACACIA_TRAPDOOR:
                case JUNGLE_TRAPDOOR:
                case SPRUCE_TRAPDOOR:
                case DARK_OAK_TRAPDOOR:
                case OAK_FENCE_GATE:
                case BIRCH_FENCE_GATE:
                case ACACIA_FENCE_GATE:
                case JUNGLE_FENCE_GATE:
                case SPRUCE_FENCE_GATE:
                case DARK_OAK_FENCE_GATE:
                    event.setCancelled(true);
            }
        }
    }

}
