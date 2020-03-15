package net.riddlebit.mc.data;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public String name;

    @Reference
    public List<PlayerData> players;

    @Embedded
    public List<ChunkData> ownedChunks;

    public float getReputation() {
        float reputation = 0;
        for (PlayerData playerData : players) {
            reputation += playerData.reputation;
        }
        return reputation;
    }

    public boolean isChunkOwnedByFaction(ChunkData chunkData) {
        return ownedChunks.contains(chunkData);
    }

    public boolean canChunkBeCleared(ChunkData chunkData) {
        List<ChunkData> chunksToTest = new ArrayList<>();
        chunksToTest.add(new ChunkData(chunkData.x-1, chunkData.z));
        chunksToTest.add(new ChunkData(chunkData.x+1, chunkData.z));
        chunksToTest.add(new ChunkData(chunkData.x, chunkData.z-1));
        chunksToTest.add(new ChunkData(chunkData.x, chunkData.z+1));
        for (ChunkData chunkToTest : chunksToTest) {
            if (!isChunkOwnedByFaction(chunkToTest)) {
                return true;
            }
        }
        return false;
    }

    public boolean canAffordChunkCount(int chunkCount) {
        float cost = 100f * chunkCount;
        if (chunkCount > 9) {
            cost += 10f * Math.floor((((chunkCount - 9) * (chunkCount - 8)) / 2f));
        }
        return cost <= getReputation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactionData that = (FactionData) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
