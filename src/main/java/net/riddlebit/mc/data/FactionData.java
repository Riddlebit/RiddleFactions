package net.riddlebit.mc.data;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@Entity(value = "factions", noClassnameStored = true)
public class FactionData {

    public FactionData() {
        players = new ArrayList<>();
        ownedChunks = new ArrayList<>();
    }

    public FactionData(PlayerData playerData) {
        players = new ArrayList<>();
        players.add(playerData);
    }

    @Id
    private ObjectId id;

    public String name;

    @Reference
    public List<PlayerData> players;

    @Embedded
    public List<ChunkData> ownedChunks;

}
