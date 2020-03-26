package net.riddlebit.mc.data.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import net.riddlebit.mc.data.ChunkData;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.PlayerData;
import net.riddlebit.mc.data.TreasureData;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RFJsonStore implements RFDatastore {

    private Gson gson;
    private File playersFile, factionsFile, treasuresFile;

    // Class used for saving faction data to json
    private class JsonFactionData {
        @Expose public String name;
        @Expose public List<String> players;
        @Expose public List<ChunkData> ownedChunks;
        public JsonFactionData() {
            players = new ArrayList<>();
            ownedChunks = new ArrayList<>();
        }
    }

    public RFJsonStore(File dataFolder) {
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        playersFile = new File(dataFolder, "players.json");
        factionsFile = new File(dataFolder, "factions.json");
        treasuresFile = new File(dataFolder, "treasures.json");
        try {
            playersFile.createNewFile();
            factionsFile.createNewFile();
            treasuresFile.createNewFile();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to create new file: " + e);
        }
    }

    @Override
    public List<PlayerData> loadPlayers() {
        try {
            JsonReader reader = new JsonReader(new FileReader(playersFile));
            return gson.fromJson(reader, new TypeToken<List<PlayerData>>(){}.getType());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to load players: " + e);
        }
        return null;
    }

    @Override
    public List<FactionData> loadFactions() {
        try {
            JsonReader reader = new JsonReader(new FileReader(factionsFile));
            List<JsonFactionData> jsonFactions = gson.fromJson(reader, new TypeToken<List<JsonFactionData>>(){}.getType());
            if (jsonFactions == null) return null;
            List<FactionData> factions = new ArrayList<>();
            for (JsonFactionData jsonFactionData : jsonFactions) {
                FactionData factionData = new FactionData();
                factionData.name = jsonFactionData.name;
                for (PlayerData playerData : loadPlayers()) {
                    if (jsonFactionData.players.contains(playerData.uuid)) {
                        factionData.players.add(playerData);
                    }
                }
                factionData.ownedChunks = jsonFactionData.ownedChunks;
                factions.add(factionData);
            }
            return factions;
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to load factions: " + e);
        }
        return null;
    }

    @Override
    public List<TreasureData> loadTreasures() {
        try {
            JsonReader reader = new JsonReader(new FileReader(treasuresFile));
            return gson.fromJson(reader, new TypeToken<List<TreasureData>>(){}.getType());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to load treasures: " + e);
        }
        return null;
    }

    @Override
    public void savePlayerData(PlayerData playerData) {
        try {
            List<PlayerData> players = loadPlayers();
            if (players == null) {
                players = new ArrayList<>();
            }
            players.remove(playerData);
            players.add(playerData);
            FileWriter writer = new FileWriter(playersFile, false);
            writer.write(gson.toJson(players));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save player: " + e);
        }
    }

    @Override
    public void saveFactionData(FactionData factionData) {
        List<FactionData> factions = loadFactions();
        if (factions == null) {
            factions = new ArrayList<>();
        }
        factions.remove(factionData);
        factions.add(factionData);
        writeFactionsToFile(factions);
    }

    @Override
    public void deleteFactionData(FactionData factionData) {
        List<FactionData> factions = loadFactions();
        if (factions == null) {
            factions = new ArrayList<>();
        }
        factions.remove(factionData);
        writeFactionsToFile(factions);
    }

    @Override
    public void saveTreasureData(TreasureData treasureData) {
        try {
            List<TreasureData> treasures = loadTreasures();
            if (treasures == null) {
                treasures = new ArrayList<>();
            }
            treasures.remove(treasureData);
            treasures.add(treasureData);
            FileWriter writer = new FileWriter(treasuresFile,false);
            writer.write(gson.toJson(treasures));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save treasure: " + e);
        }
    }

    @Override
    public void deleteTreasure(TreasureData treasureData) {
        try {
            List<TreasureData> treasures = loadTreasures();
            if (treasures == null) {
                treasures = new ArrayList<>();
            }
            treasures.remove(treasureData);
            FileWriter writer = new FileWriter(treasuresFile, false);
            writer.write(gson.toJson(treasures));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to delete treasure: " + e);
        }
    }

    private void writeFactionsToFile(Collection<FactionData> factions) {
        try {
            List<JsonFactionData> jsonFactions = new ArrayList<>();
            for (FactionData faction : factions) {
                JsonFactionData jsonFactionData = new JsonFactionData();
                jsonFactionData.name = faction.name;
                for (PlayerData playerData : faction.players) {
                    jsonFactionData.players.add(playerData.uuid);
                }
                jsonFactionData.ownedChunks = faction.ownedChunks;
                jsonFactions.add(jsonFactionData);
            }
            FileWriter writer = new FileWriter(factionsFile, false);
            writer.write(gson.toJson(jsonFactions));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to write factions to file: " + e);
        }
    }

}
