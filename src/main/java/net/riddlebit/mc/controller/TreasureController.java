package net.riddlebit.mc.controller;

import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.ChunkData;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.TreasureData;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreasureController {

    private RiddleFactions plugin;
    private DataManager dataManager;

    private Map<Material, Integer> treasureMap;

    public TreasureController(RiddleFactions plugin) {
        this.plugin = plugin;
        dataManager = plugin.dataManager;
        treasureMap = new HashMap<>();
        treasureMap.put(Material.DIAMOND_BLOCK, 5);
        treasureMap.put(Material.EMERALD_BLOCK, 5);
        treasureMap.put(Material.LAPIS_BLOCK, 3);
    }

    public boolean addTreasure(Block block) {
        TreasureData treasureData = getTreasureDataFromBlock(block);
        if (treasureData != null) {
            dataManager.addTreasureData(treasureData);
            return true;
        }
        return false;
    }

    public boolean removeTreasure(Block block) {
        TreasureData treasureData = getTreasureDataFromBlock(block);
        if (treasureData != null) {
            dataManager.removeTreasureData(treasureData);
            return true;
        }
        return false;
    }

    public TreasureData getTreasureDataFromBlock(Block block) {
        if (!treasureMap.containsKey(block.getType())) return null;
        for (TreasureData treasureData : dataManager.treasures) {
            if (block.getX() == treasureData.x && block.getY() == treasureData.y && block.getZ() == treasureData.z) {
                return treasureData;
            }
        }
        String blockType = block.getType().toString();
        return new TreasureData(blockType, block.getX(), block.getY(), block.getZ());
    }

    public boolean isTreasureOwnedByFaction(TreasureData treasureData, FactionData factionData) {
        List<ChunkData> ownedChunks = factionData.ownedChunks;
        for (ChunkData chunkData : ownedChunks) {
            // Is treasure within this chunk?
            if (treasureData.x >> 4 == chunkData.x && treasureData.z >> 4 == chunkData.z) {
                return true;
            }
        }
        return false;
    }

    public List<TreasureData> getAllTreasuresInFaction(FactionData factionData) {
        List<TreasureData> treasures = new ArrayList<>();
        for (TreasureData treasureData : dataManager.treasures) {
            // Is this treasure owned by this faction?
            if (isTreasureOwnedByFaction(treasureData, factionData)) {
                treasures.add(treasureData);
            }
        }
        return treasures;
    }

    public int getTreasureReputation(TreasureData treasureData) {
        Material material = Material.valueOf(treasureData.blockType);
        return treasureMap.getOrDefault(material, 0);
    }

}
