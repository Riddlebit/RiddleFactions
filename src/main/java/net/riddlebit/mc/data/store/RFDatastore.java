package net.riddlebit.mc.data.store;

import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.PlayerData;
import net.riddlebit.mc.data.TreasureData;

import java.util.List;

public interface RFDatastore {

    List<PlayerData> loadPlayers();
    List<FactionData> loadFactions();
    List<TreasureData> loadTreasures();

    void savePlayerData(PlayerData playerData);

    void saveFactionData(FactionData factionData);
    void deleteFactionData(FactionData factionData);

    void saveTreasureData(TreasureData treasureData);
    void deleteTreasure(TreasureData treasureData);

}
