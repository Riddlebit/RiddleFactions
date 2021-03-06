package net.riddlebit.mc.controller;

import net.riddlebit.mc.RFChat;
import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

public class FactionController {

    private RiddleFactions plugin;
    private DataManager dataManager;

    private float reputationBaseRate;

    public FactionController(RiddleFactions plugin) {
        this.plugin = plugin;
        dataManager = plugin.dataManager;
        reputationBaseRate = (float) plugin.config.getDouble("reputation-base-rate");
    }

    public boolean createFaction(String factionName, Player player) {
        if (isPlayerInFaction(player)) {
            RFChat.toPlayer(player, "You're already in a faction...");
            return true;
        }

        if (isFactionNameTaken(factionName)) {
            RFChat.toPlayer(player, "A faction with that name already exists...");
            return true;
        }

        // Create the new faction
        PlayerData playerData = dataManager.getPlayerData(player);
        FactionData factionData = new FactionData(playerData);
        factionData.name = factionName;
        factionData.bossBar = plugin.getServer().createBossBar(factionData.name, BarColor.WHITE, BarStyle.SOLID);
        dataManager.addFactionData(factionData);

        RFChat.toPlayer(player, factionName + " was created!");
        RFChat.broadcast(player.getDisplayName() + " created the faction " + factionName + "!");
        return true;
    }

    public boolean inviteToFaction(Player inviter, Player invitee) {
        if (!isPlayerInFaction(inviter)) {
            RFChat.toPlayer(inviter, "You must be in a faction...");
            return true;
        }

        FactionData factionData = getFactionForPlayer(inviter);
        PlayerData inviterData = dataManager.getPlayerData(inviter);
        PlayerData inviteeData = dataManager.getPlayerData(invitee);

        if (factionData == null || inviterData == null || inviteeData == null) {
            return false;
        }

        if (inviterData.equals(inviteeData)) {
            RFChat.toPlayer(inviter, "You can't invite yourself...");
            return true;
        }

        Invite invite = new Invite(factionData, inviterData, inviteeData);
        dataManager.invites.add(invite);

        RFChat.toPlayer(inviter, invitee.getDisplayName() + " was invited to your faction");
        RFChat.toPlayer(invitee, "You have been invited to " + factionData.name);
        return true;
    }

    public boolean joinFaction(String factionName, Player player) {
        PlayerData playerData = dataManager.getPlayerData(player);

        Invite invite = null;
        for (Invite i : dataManager.invites) {
            if (i.factionData.name.equalsIgnoreCase(factionName) && i.invitee.equals(playerData)) {
                invite = i;
            }
        }

        if (invite == null) {
            RFChat.toPlayer(player, "No invite found :(");
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

        RFChat.toPlayer(player, "You have joined " + factionData.name);
        for (Player factionPlayer : factionData.getOnlinePlayersInFaction()) {
            if (factionPlayer != player) {
                RFChat.toPlayer(factionPlayer, player.getDisplayName() + " has joined your faction!");
            }
        }
        return true;
    }

    public boolean leaveFaction(String factionName, Player player) {
        if (!isPlayerInFaction(player)) {
            RFChat.toPlayer(player, "You're not in a faction...");
            return true;
        }

        FactionData factionData = getFactionForPlayer(player);
        PlayerData playerData = dataManager.getPlayerData(player);

        // Player must specify faction name as a safety check
        if (!factionData.name.equalsIgnoreCase(factionName)) {
            RFChat.toPlayer(player, "Please specify the name of your faction...");
            return true;
        }

        // Remove player from faction
        factionData.players.remove(playerData);
        if (factionData.players.size() > 0) {
            // Broadcast to other faction members
            for (Player factionPlayer : factionData.getOnlinePlayersInFaction()) {
                RFChat.toPlayer(factionPlayer, player.getDisplayName() + " left your faction!");
            }
            dataManager.save();
        } else {
            // Delete faction if there are no players left
            for (Player p : Bukkit.getOnlinePlayers()) {
                factionData.bossBar.removePlayer(player);
            }
            dataManager.deleteFaction(factionData);
        }

        // Reset player reputation
        playerData.reputation = 0;
        dataManager.save();

        RFChat.toPlayer(player, "You left " + factionData.name);
        return true;
    }

    public boolean claimChunk(Player player) {
        if (!isPlayerInFaction(player)) {
            RFChat.toPlayer(player, "You're not in a faction...");
            return true;
        }

        if (player.getWorld() != Bukkit.getWorlds().get(0)) {
            RFChat.toPlayer(player, "You can't claim a chunk here...");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();
        ChunkData chunkData = new ChunkData(chunk.getX(), chunk.getZ());
        FactionData factionData = getFactionForPlayer(player);

        // Check if this chunk is claimed
        if (isChunkOwnedByFaction(chunkData)) {
            RFChat.toPlayer(player, "This chunk is already claimed...");
            return true;
        }

        // Check if this chunk is a spawn chunk
        if (plugin.chunkController.isChunkWithinSpawnRadius(chunkData)) {
            RFChat.toPlayer(player, "This is a spawn chunk. You can't claim it!");
            return true;
        }

        // Check if border chunks are claimed
        FactionData borderFactionData = chunkBordersToFaction(chunkData);
        if (borderFactionData != null && !borderFactionData.equals(factionData)) {
            RFChat.toPlayer(player, "This chunk is too close to another faction!");
            return true;
        }

        // Check reputation
        if (!plugin.chunkController.canFactionAffordChunk(factionData, chunkData)) {
            RFChat.toPlayer(player, "Your faction does not have enough reputation to claim this chunk...");
            return true;
        }

        factionData.ownedChunks.add(chunkData);
        dataManager.save();

        RFChat.toPlayer(player, "You claimed this chunk!");
        plugin.playerController.updateBossBarForPlayer(player);
        return true;
    }

    public boolean clearChunk(Player player) {
        if (!isPlayerInFaction(player)) {
            RFChat.toPlayer(player, "You're not in a faction...");
            return true;
        }

        if (player.getWorld() != Bukkit.getWorlds().get(0)) {
            RFChat.toPlayer(player, "You can't clear chunks here...");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();
        ChunkData chunkData = new ChunkData(chunk.getX(), chunk.getZ());
        FactionData factionData = getFactionForPlayer(player);
        FactionData chunkFactionData = getChunkOwner(chunkData);

        if (chunkFactionData == null) {
            RFChat.toPlayer(player, "This is not a claimed chunk...");
            return true;
        }

        if (!chunkFactionData.equals(factionData)) {
            if (plugin.chunkController.isFactionSustainable(chunkFactionData)) {
                RFChat.toPlayer(player, "You can't clear this chunk. Faction is sustainable!");
                return true;
            }
            if (!chunkFactionData.canChunkBeCleared(chunkData)) {
                RFChat.toPlayer(player, "This chunk does not border wilderness...");
                return true;
            }
        }

        chunkFactionData.ownedChunks.remove(chunkData);
        dataManager.save();

        if (plugin.factionController.getFactionForPlayer(player).equals(chunkFactionData)) {
            RFChat.toPlayer(player, "Chunk cleared!");
        } else {
            RFChat.broadcast(player.getDisplayName() + " just cleared a chunk from " + chunkFactionData.name + "!");
        }

        plugin.playerController.updateBossBarForPlayer(player);
        return true;
    }

    public boolean isFactionNameTaken(String factionName) {
        for (FactionData factionData : dataManager.factions.values()) {
            if (factionName.equalsIgnoreCase(factionData.name)) {
                return true;
            }
        }
        return false;
    }

    public boolean isChunkOwnedByFaction(ChunkData chunkData) {
        for (FactionData factionData : dataManager.factions.values()) {
            if (factionData.isOwnerOfChunk(chunkData)) return true;
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

    public float getReputationRateForFaction(FactionData factionData) {
        float reputationRate = reputationBaseRate;
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

            int playerCount = factionData.getAlivePlayersInFaction().size();

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
