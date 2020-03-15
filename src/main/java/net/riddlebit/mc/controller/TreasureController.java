package net.riddlebit.mc.controller;

import net.riddlebit.mc.RiddleFactions;
import net.riddlebit.mc.data.TreasureData;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;
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

}
