package net.riddlebit.mc.data;

import java.util.Objects;

public class ChunkData {

    public ChunkData(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int x;
    public int z;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkData chunkData = (ChunkData) o;
        return x == chunkData.x &&
                z == chunkData.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
