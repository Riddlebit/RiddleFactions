package net.riddlebit.mc.controller;

import net.riddlebit.mc.RFChat;
import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.ChunkData;
import net.riddlebit.mc.data.ChunkType;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

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
        updateBossBarForPlayer(player);
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
                    RFChat.toPlayer(player, "Treasure placed!");
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
        player.sendMessage("You lost " + ChatColor.RED + (int) lostReputation + ChatColor.RESET + " reputation...");

        Player killer = player.getKiller();
        if (killer != null) {
            PlayerData killerPlayerData = dataManager.getPlayerData(killer);
            float gainedReputation = lostReputation * 0.5f;
            killerPlayerData.reputation += gainedReputation;
            killer.sendMessage("You gained " + ChatColor.GREEN + (int) gainedReputation + ChatColor.RESET + " reputation!");
        }

        // Set death message
        String deathMessage = ChatColor.RED + "-> ";
        deathMessage += ChatColor.GRAY + event.getDeathMessage();
        deathMessage += " and lost " + ChatColor.RED + (int) lostReputation + ChatColor.GRAY + " reputation...";
        event.setDeathMessage(deathMessage);
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
            updateBossBarForPlayer(player, toChunkData);
        }
    }

    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getPlayer().getWorld() != Bukkit.getWorlds().get(0)) return;
        if (event.getTo() == null) return;
        Player player = event.getPlayer();
        Chunk toChunk = event.getTo().getChunk();
        ChunkData toChunkData = new ChunkData(toChunk.getX(), toChunk.getZ());
        updateBossBarForPlayer(player, toChunkData);
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
            // Player is not in this faction -> cancel interaction with certain blocks

            BlockData blockData = block.getBlockData();

            if (blockData instanceof Door || blockData instanceof TrapDoor || blockData instanceof Gate)
                event.setCancelled(true);
        }
    }

    public void onPlayerBucketEvent(PlayerBucketEvent event) {
        Block block = event.getBlockClicked();
        Chunk chunk = block.getChunk();
        Player player = event.getPlayer();

        FactionData factionData = plugin.factionController.getFactionForPlayer(player);
        ChunkData chunkData = new ChunkData(chunk.getX(), chunk.getZ());
        FactionData chunkFactionData = plugin.factionController.getChunkOwner(chunkData);

        if (chunkFactionData != null && (factionData == null || !factionData.equals(chunkFactionData))) {
            // Player is not in this faction -> cancel bucket event
            event.setCancelled(true);
        }
    }

    public ChunkData getChunkDataForPlayer(Player player) {
        if (player.getWorld() != Bukkit.getWorlds().get(0)) return null;
        Chunk chunk = player.getLocation().getChunk();
        return new ChunkData(chunk.getX(), chunk.getZ());
    }

    public void resetBossBarForPlayer(Player player) {
        for (FactionData factionData : dataManager.factions.values()) {
            factionData.bossBar.removePlayer(player);
        }
    }

    public void updateBossBarForPlayer(Player player, ChunkData chunkData) {
        FactionData factionData = plugin.factionController.getChunkOwner(chunkData);
        resetBossBarForPlayer(player);
        if (factionData != null) {
            factionData.bossBar.addPlayer(player);
        }
    }

    public void updateBossBarForPlayer(Player player) {
        ChunkData chunkData = getChunkDataForPlayer(player);
        updateBossBarForPlayer(player, chunkData);
    }

}
