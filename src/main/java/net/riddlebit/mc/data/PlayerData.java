package net.riddlebit.mc.data;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.Objects;

@Entity(value = "players", noClassnameStored = true)
public class PlayerData {

    @Id
    private ObjectId id;

    public String uuid;

    public String name;

    public float reputation;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerData that = (PlayerData) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
