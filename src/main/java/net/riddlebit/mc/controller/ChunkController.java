package net.riddlebit.mc.controller;

import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.ChunkData;
import net.riddlebit.mc.data.FactionData;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;

public class ChunkController {

    private RiddleFactions plugin;
    private DataManager dataManager;

    private int spawnRadius, coreRadius;
    private float chunkBaseCost, chunkCostMultiplier;

    public ChunkController(RiddleFactions plugin) {
        this.plugin = plugin;
        dataManager = plugin.dataManager;
        spawnRadius = plugin.config.getInt("spawn-radius");
        coreRadius = plugin.config.getInt("core-radius");
        chunkBaseCost = (float) plugin.config.getDouble("chunk-base-cost");
        chunkCostMultiplier = (float) plugin.config.getDouble("chunk-cost-multiplier");
    }

    public ChunkData getSpawnChunkData() {
        Chunk spawnChunk = Bukkit.getWorlds().get(0).getSpawnLocation().getChunk();
        return new ChunkData(spawnChunk.getX(), spawnChunk.getZ());
    }

    public int getChunkDistanceFromSpawn(ChunkData chunkData) {
        ChunkData spawnChunk = getSpawnChunkData();
        int dx = Math.abs(chunkData.x - spawnChunk.x);
        int dy = Math.abs(chunkData.z - spawnChunk.z);
        return Math.max(dx, dy);
    }

    public boolean isChunkWithinRadius(ChunkData chunkData, int radius) {
        int distanceFromSpawn = getChunkDistanceFromSpawn(chunkData);
        return distanceFromSpawn < radius;
    }

    public boolean isChunkWithinSpawnRadius(ChunkData chunkData) {
        return isChunkWithinRadius(chunkData, spawnRadius);
    }

    public boolean isChunkWithinCoreRadius(ChunkData chunkData) {
        return isChunkWithinRadius(chunkData, coreRadius);
    }

    public float getChunkReputationCost(ChunkData chunkData) {
        if (isChunkWithinCoreRadius(chunkData)) {
            return chunkBaseCost;
        } else {
            int distanceFromSpawn = getChunkDistanceFromSpawn(chunkData);
            int distanceFromCore = distanceFromSpawn - coreRadius;
            return chunkBaseCost + chunkCostMultiplier * distanceFromCore;
        }
    }

    public boolean isFactionSustainable(FactionData factionData) {
        float totalReputationCost = 0;
        for (ChunkData chunkData : factionData.ownedChunks) {
            totalReputationCost += getChunkReputationCost(chunkData);
        }
        return factionData.getReputation() >= totalReputationCost;
    }

    public boolean canFactionAffordChunk(FactionData factionData, ChunkData chunkData) {
        if (!isFactionSustainable(factionData)) return false;
        List<ChunkData> chunksToCheck = new ArrayList<>(factionData.ownedChunks);
        chunksToCheck.add(chunkData);
        float totalReputationCost = 0;
        for (ChunkData chunkToCheck : chunksToCheck) {
            totalReputationCost += getChunkReputationCost(chunkToCheck);
        }
        return factionData.getReputation() >= totalReputationCost;
    }

}
