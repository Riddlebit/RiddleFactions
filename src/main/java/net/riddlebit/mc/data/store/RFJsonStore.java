package net.riddlebit.mc.data.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import net.riddlebit.mc.data.FactionData;
import net.riddlebit.mc.data.PlayerData;
import net.riddlebit.mc.data.TreasureData;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RFJsonStore implements RFDatastore {

    private Gson gson;
    private File playersFile, factionsFile, treasuresFile;

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
            return gson.fromJson(reader, new TypeToken<List<FactionData>>(){}.getType());
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
        try {
            List<FactionData> factions = loadFactions();
            if (factions == null) {
                factions = new ArrayList<>();
            }
            factions.remove(factionData);
            factions.add(factionData);
            FileWriter writer = new FileWriter(factionsFile, false);
            writer.write(gson.toJson(factions));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save faction: " + e);
        }
    }

    @Override
    public void deleteFactionData(FactionData factionData) {
        try {
            List<FactionData> factions = loadFactions();
            if (factions == null) {
                factions = new ArrayList<>();
            }
            factions.remove(factionData);
            FileWriter writer = new FileWriter(factionsFile, false);
            writer.write(gson.toJson(factions));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to delete faction: " + e);
        }
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

}
