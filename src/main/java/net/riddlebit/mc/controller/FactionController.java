package net.riddlebit.mc.controller;

import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.*;

public class FactionController {

    private RiddleFactions plugin;
    private DataManager dataManager;

    public FactionController(RiddleFactions plugin) {
        this.plugin = plugin;
        dataManager = plugin.dataManager;
    }

    public boolean createFaction(String factionName, Player player) {
        if (isPlayerInFaction(player)) {
            player.sendMessage("You're already in a faction...");
            return true;
        }

        if (dataManager.factions.containsKey(factionName)) {
            player.sendMessage("A faction with that name already exists");
            return true;
        }

        // Create the new faction
        PlayerData playerData = dataManager.getPlayerData(player);
        FactionData factionData = new FactionData(playerData);
        factionData.name = factionName;
        dataManager.addFactionData(factionData);

        player.sendMessage(factionName + " was created!");
        return true;
    }

    public boolean inviteToFaction(Player inviter, Player invitee) {
        if (!isPlayerInFaction(inviter)) {
            inviter.sendMessage("You must be in a faction...");
            return true;
        }

        FactionData factionData = getFactionForPlayer(inviter);
        PlayerData inviterData = dataManager.getPlayerData(inviter);
        PlayerData inviteeData = dataManager.getPlayerData(invitee);

        if (factionData == null || inviterData == null || inviteeData == null) {
            return false;
        }

        if (inviterData.equals(inviteeData)) {
            inviter.sendMessage("You cannot invite yourself...");
            return true;
        }

        Invite invite = new Invite(factionData, inviterData, inviteeData);
        dataManager.invites.add(invite);

        inviter.sendMessage(invitee.getDisplayName() + " was invited to your faction");
        invitee.sendMessage("You have been invited to " + factionData.name);
        return true;
    }

    public boolean joinFaction(String factionName, Player player) {
        PlayerData playerData = dataManager.getPlayerData(player);

        Invite invite = null;
        for (Invite i : dataManager.invites) {
            if (i.factionData.name.equals(factionName) && i.invitee.equals(playerData)) {
                invite = i;
            }
        }

        if (invite == null) {
            player.sendMessage("No invite found :(");
            return true;
        }

        dataManager.invites.remove(invite);

        // Remove player from current faction
        if (isPlayerInFaction(player)) {
            FactionData currentFactionData = getFactionForPlayer(player);
            currentFactionData.players.remove(playerData);
            if (currentFactionData.players.size() > 0) {
                dataManager.save();
            } else {
                dataManager.deleteFaction(currentFactionData);
            }
        }

        // Reset player reputation
        playerData.reputation = 0;
        dataManager.save();

        // Add player to new faction
        FactionData factionData = invite.factionData;
        factionData.players.add(playerData);
        dataManager.save();

        player.sendMessage("You have joined " + factionData.name);
        for (Player factionPlayer : getOnlinePlayersInFaction(factionData)) {
            if (factionPlayer != player) {
                factionPlayer.sendMessage(player.getDisplayName() + " has joined your faction!");
            }
        }
        return true;
    }

    public boolean leaveFaction(Player player) {
        if (!isPlayerInFaction(player)) {
            player.sendMessage("You're not in a faction...");
            return true;
        }

        FactionData factionData = getFactionForPlayer(player);
        PlayerData playerData = dataManager.getPlayerData(player);

        // Remove player from faction
        factionData.players.remove(playerData);
        if (factionData.players.size() > 0) {
            // Broadcast to other faction members
            for (Player factionPlayer : getOnlinePlayersInFaction(factionData)) {
                factionPlayer.sendMessage(player.getDisplayName() + " left your faction!");
            }
            dataManager.save();
        } else {
            // Delete faction if there are no players left
            dataManager.deleteFaction(factionData);
        }

        // Reset player reputation
        playerData.reputation = 0;
        dataManager.save();

        player.sendMessage("You left " + factionData.name);
        return true;
    }

    public boolean claimChunk(Player player) {
        if (!isPlayerInFaction(player)) {
            player.sendMessage("You're not in a faction...");
            return true;
        }

        if (player.getWorld() != Bukkit.getWorlds().get(0)) {
            player.sendMessage("You cannot claim a chunk here...");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();
        ChunkData chunkData = new ChunkData(chunk.getX(), chunk.getZ());
        FactionData factionData = getFactionForPlayer(player);

        // Check if this chunk is claimed
        if (isChunkOwnedByFaction(chunkData)) {
            player.sendMessage("This chunk is already claimed...");
            return true;
        }

        // Check if border chunks are claimed
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                int currentX = chunk.getX()+x;
                int currentZ = chunk.getZ()+z;
                ChunkData currentChunkData = new ChunkData(currentX, currentZ);
                FactionData chunkFactionData = getChunkOwner(currentChunkData);
                if (chunkFactionData != null && !chunkFactionData.equals(factionData)) {
                    player.sendMessage("Too close to another faction!");
                    return true;
                }
            }
        }

        // Check reputation
        int ownedChunksCount = factionData.ownedChunks.size();
        if (!factionData.canAffordChunkCount(ownedChunksCount+1)) {
            player.sendMessage("Your faction does not have enough reputation...");
            return true;
        }

        factionData.ownedChunks.add(chunkData);
        dataManager.save();

        player.sendMessage("You claimed this chunk!");
        return true;
    }

    public boolean clearChunk(Player player) {
        if (!isPlayerInFaction(player)) {
            player.sendMessage("You're not in a faction...");
            return true;
        }

        if (player.getWorld() != Bukkit.getWorlds().get(0)) {
            player.sendMessage("You cannot clear chunks here...");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();
        ChunkData chunkData = new ChunkData(chunk.getX(), chunk.getZ());
        FactionData factionData = getFactionForPlayer(player);
        FactionData factionChunkData = getChunkOwner(chunkData);

        if (factionChunkData == null) {
            player.sendMessage("This is not a claimed chunk...");
            return true;
        }

        if (!factionChunkData.equals(factionData)) {
            int currentChunkCount = factionChunkData.ownedChunks.size();
            if (factionChunkData.canAffordChunkCount(currentChunkCount)) {
                player.sendMessage("No, you cannot do that");
                return true;
            }
            if (!factionChunkData.canChunkBeCleared(chunkData)) {
                player.sendMessage("This chunk does not border wilderness...");
                return true;
            }
        }

        factionChunkData.ownedChunks.remove(chunkData);
        dataManager.save();

        player.sendMessage("Chunk cleared");
        return true;
    }

    public boolean isChunkOwnedByFaction(ChunkData chunkData) {
        for (FactionData factionData : dataManager.factions.values()) {
            for (ChunkData ownedChunkData : factionData.ownedChunks) {
                if (ownedChunkData.equals(chunkData)) {
                    return true;
                }
            }
        }
        return false;
    }

    public FactionData getChunkOwner(ChunkData chunkData) {
        for (FactionData factionData : dataManager.factions.values()) {
            for (ChunkData ownedChunkData : factionData.ownedChunks) {
                if (ownedChunkData.equals(chunkData)) {
                    return factionData;
                }
            }
        }
        return null;
    }

    public boolean isChunkBorder(ChunkData chunkData) {
        if (isChunkOwnedByFaction(chunkData)) return false;
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                int currentX = chunkData.x + x;
                int currentZ = chunkData.z + z;
                ChunkData currentChunkData = new ChunkData(currentX, currentZ);
                if (isChunkOwnedByFaction(currentChunkData)) return true;
            }
        }
        return false;
    }

    public FactionData chunkBordersToFaction(ChunkData chunkData) {
        if (!isChunkBorder(chunkData)) return null;
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                int currentX = chunkData.x + x;
                int currentZ = chunkData.z + z;
                ChunkData currentChunkData = new ChunkData(currentX, currentZ);
                FactionData factionData = getChunkOwner(currentChunkData);
                if (factionData != null) return factionData;
            }
        }
        return null;
    }

    public boolean isPlayerInFaction(Player player) {
        return getFactionForPlayer(player) != null;
    }

    public FactionData getFactionForPlayer(Player player) {
        PlayerData playerData = dataManager.getPlayerData(player);
        for (FactionData factionData : dataManager.factions.values()) {
            if (factionData.players.contains(playerData)) {
                return factionData;
            }
        }
        return null;
    }

    public List<Player> getOnlinePlayersInFaction(FactionData factionData) {
        List<Player> players = new ArrayList<>();
        for (PlayerData playerData : factionData.players) {
            Player player = Bukkit.getPlayer(UUID.fromString(playerData.uuid));
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }
        return players;
    }

    public List<Player> getAlivePlayersInFaction(FactionData factionData) {
        List<Player> alivePlayers = new ArrayList<>();
        for (Player player : getOnlinePlayersInFaction(factionData)) {
            if (!player.isDead()) {
                alivePlayers.add(player);
            }
        }
        return alivePlayers;
    }

    public float getReputationRateForFaction(FactionData factionData) {
        float reputationRate = 100f;
        for (TreasureData treasureData : plugin.treasureController.getAllTreasuresInFaction(factionData)) {
            reputationRate += plugin.treasureController.getTreasureReputation(treasureData);
        }
        return reputationRate;
    }

    public ChunkType getChunkType(ChunkData chunkData) {
        if (isChunkOwnedByFaction(chunkData)) return ChunkType.CLAIMED;
        if (isChunkBorder(chunkData)) return ChunkType.BORDER;
        return ChunkType.WILDERNESS;
    }

    public void updateReputation() {
        for (FactionData factionData : dataManager.factions.values()) {

            int playerCount = getAlivePlayersInFaction(factionData).size();

            if (playerCount > 0) {
                float reputationRate = getReputationRateForFaction(factionData);
                float reputationPerPlayer = reputationRate / playerCount / 3600;
                for (PlayerData playerData : factionData.players) {
                    if (!playerData.isDead()) {
                        playerData.reputation += reputationPerPlayer;
                    }
                }
            }
        }
    }

}
