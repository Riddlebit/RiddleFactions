package net.riddlebit.mc.data.store;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.PlayerData;
import net.riddlebit.mc.data.TreasureData;

import java.util.List;

public class RFMongoStore implements RFDatastore {

    private Datastore datastore;

    public RFMongoStore(String dbHost, String dbName) {
        Morphia morphia = new Morphia();
        morphia.map(PlayerData.class);
        morphia.map(FactionData.class);
        morphia.map(TreasureData.class);
        datastore = morphia.createDatastore(new MongoClient(dbHost), dbName);
        datastore.ensureIndexes();
    }

    @Override
    public List<PlayerData> loadPlayers() {
        Query<PlayerData> query = datastore.createQuery(PlayerData.class);
        return query.find().toList();
    }

    @Override
    public List<FactionData> loadFactions() {
        Query<FactionData> query = datastore.createQuery(FactionData.class);
        return query.find().toList();
    }

    @Override
    public List<TreasureData> loadTreasures() {
        Query<TreasureData> query = datastore.createQuery(TreasureData.class);
        return query.find().toList();
    }

    @Override
    public void savePlayerData(PlayerData playerData) {
        datastore.save(playerData);
    }

    @Override
    public void saveFactionData(FactionData factionData) {
        datastore.save(factionData);
    }

    @Override
    public void deleteFactionData(FactionData factionData) {
        datastore.delete(factionData);
    }

    @Override
    public void saveTreasureData(TreasureData treasureData) {
        datastore.save(treasureData);
    }

    @Override
    public void deleteTreasure(TreasureData treasureData) {
        datastore.delete(treasureData);
    }

}
