package net.riddlebit.mc.data;

import com.google.gson.annotations.Expose;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

import java.util.Objects;

@Entity(value = "treasures", noClassnameStored = true)
public class TreasureData {

    public TreasureData() {
       id = new ObjectId();
    }

    public TreasureData(String blockType, int x, int y, int z) {
        id = new ObjectId();
        this.blockType = blockType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Id
    private ObjectId id;

    @Expose
    public String blockType;

    @Expose
    public int x, y, z;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreasureData that = (TreasureData) o;
        return x == that.x &&
                y == that.y &&
                z == that.z &&
                blockType.equals(that.blockType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockType, x, y, z);
    }
}
