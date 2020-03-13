package net.riddlebit.mc.data;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

@Entity(value = "players", noClassnameStored = true)
public class PlayerData {

    @Id
    private ObjectId id;

    public String uuid;

    public String name;

    public float reputation;

}
