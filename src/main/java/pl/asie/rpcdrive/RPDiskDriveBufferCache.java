package pl.asie.rpcdrive;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;

import java.util.WeakHashMap;

public final class RPDiskDriveBufferCache {
    public static final RPDiskDriveBufferCache INSTANCE = new RPDiskDriveBufferCache();
    private final WeakHashMap<World, WeakHashMap<Chunk, WeakHashMap<TileEntity, byte[]>>> diskDriveBuffer = new WeakHashMap<World, WeakHashMap<Chunk, WeakHashMap<TileEntity, byte[]>>>();

    public byte[] getOrCreateBuffer(World world, TileEntity parent, int len) {
        WeakHashMap<Chunk, WeakHashMap<TileEntity, byte[]>> map = diskDriveBuffer.get(world);
        if (map == null) {
            map = new WeakHashMap<Chunk, WeakHashMap<TileEntity, byte[]>>();
            diskDriveBuffer.put(world, map);
        }
        Chunk ch = world.getChunkFromBlockCoords(parent.xCoord, parent.zCoord);
        WeakHashMap<TileEntity, byte[]> map1 = map.get(ch);
        if (map1 == null) {
            map1 = new WeakHashMap<TileEntity, byte[]>();
            map.put(ch, map1);
        }
        byte[] buffer = map1.get(parent);
        if (buffer == null || buffer.length != len) {
            buffer = new byte[len];
            map1.put(parent, buffer);
        }
        return buffer;
    }

    @ForgeSubscribe
    public void onChunkUnload(ChunkEvent.Unload event) {
        WeakHashMap<Chunk, WeakHashMap<TileEntity, byte[]>> map = diskDriveBuffer.get(event.world);
        if (map != null) {
            map.remove(event.getChunk());
        }
    }
}
