package net.riddlebit.mc.data;

import com.google.gson.annotations.Expose;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity(value = "factions", noClassnameStored = true)
public class FactionData {

    public FactionData() {
        id = new ObjectId();
        players = new ArrayList<>();
        ownedChunks = new ArrayList<>();
    }

    public FactionData(PlayerData playerData) {
        id = new ObjectId();
        players = new ArrayList<>();
        players.add(playerData);
        ownedChunks = new ArrayList<>();
    }

    @Id
    private ObjectId id;

    @Expose
    public String name;

    @Reference
    @Expose
    public List<PlayerData> players;

    @Embedded
    @Expose
    public List<ChunkData> ownedChunks;

    @Transient
    public BossBar bossBar;

    public float getReputation() {
        float reputation = 0;
        for (PlayerData playerData : players) {
            reputation += playerData.reputation;
        }
        return reputation;
    }

    public boolean isOwnerOfChunk(ChunkData chunkData) {
        return ownedChunks.contains(chunkData);
    }

    public boolean canChunkBeCleared(ChunkData chunkData) {
        List<ChunkData> chunksToTest = new ArrayList<>();
        chunksToTest.add(new ChunkData(chunkData.x-1, chunkData.z));
        chunksToTest.add(new ChunkData(chunkData.x+1, chunkData.z));
        chunksToTest.add(new ChunkData(chunkData.x, chunkData.z-1));
        chunksToTest.add(new ChunkData(chunkData.x, chunkData.z+1));
        for (ChunkData chunkToTest : chunksToTest) {
            if (!isOwnerOfChunk(chunkToTest)) {
                return true;
            }
        }
        return false;
    }

    public List<Player> getOnlinePlayersInFaction() {
        List<Player> onlinePlayers = new ArrayList<>();
        for (PlayerData playerData : players) {
            Player player = Bukkit.getPlayer(UUID.fromString(playerData.uuid));
            if (player != null && player.isOnline()) {
                onlinePlayers.add(player);
            }
        }
        return onlinePlayers;
    }

    public List<Player> getAlivePlayersInFaction() {
        List<Player> alivePlayers = new ArrayList<>();
        for (Player player : getOnlinePlayersInFaction()) {
            if (!player.isDead()) {
                alivePlayers.add(player);
            }
        }
        return alivePlayers;
    }

    public List<String> getPlayersInFaction() {
        List<String> playersInFaction = new ArrayList<>();
        for (PlayerData playerData : players) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerData.uuid));

            playersInFaction.add(player.getPlayer().getDisplayName());
        }
        return playersInFaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactionData that = (FactionData) o;
        return name.equalsIgnoreCase(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
