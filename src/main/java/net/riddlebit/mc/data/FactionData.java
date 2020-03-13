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
        players = new ArrayList<>();
        ownedChunks = new ArrayList<>();
    }

    public FactionData(PlayerData playerData) {
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
