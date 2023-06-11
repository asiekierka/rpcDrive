package pl.asie.rpcdrive;

import com.google.common.io.ByteStreams;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IHostedPeripheral;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.Arrays;

public class PeripheralRPDiskDrive implements IHostedPeripheral {
    private static final int BYTES_PER_SECTOR = 128;
    private static final int MAX_SECTOR_COUNT = 2048;
    private final WeakReference<TileEntity> parentRef;

    public PeripheralRPDiskDrive(TileEntity parent) {
        this.parentRef = new WeakReference<TileEntity>(parent);
    }

    @Override
    public void update() {

    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {

    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {

    }

    @Override
    public String getType() {
        return "rp_drive";
    }

    @Override
    public String[] getMethodNames() {
        return new String[] {
                "getBufferByte",
                "setBufferByte",
                "isPresent",
                "hasData",
                "isReadable",
                "isWritable",
                "getSerialNumber",
                "getBytesPerSector",
                "getSectorCount",
                "getSectorSize",
                "readSector",
                "writeSector",
                "erase",
                "initialize",
                "getLabel",
                "setLabel"
        };
    }

    private String checkLabel(Object[] args, int index) throws Exception {
        if (args.length <= index || !(args[index] instanceof String)) {
            throw new Exception("label not provided");
        }
        return (String) args[index];
    }

    private int checkSectorIndex(RPDisk disk, Object[] args, int index) throws Exception {
        if (args.length <= index || !(args[index] instanceof Number)) {
            throw new Exception("sector not provided");
        }
        int sector = ((Number) args[index]).intValue() - 1;
        if (sector < 0 || sector >= getSectorSize(disk)) {
            throw new Exception("sector out of range");
        }
        return sector;
    }

    private int checkBufferIndex(Object[] args, int index) throws Exception {
        if (args.length <= index || !(args[index] instanceof Number)) {
            throw new Exception("index not provided");
        }
        int pos = ((Number) args[index]).intValue() - 1;
        if (pos < 0 || pos >= BYTES_PER_SECTOR) {
            throw new Exception("index out of range");
        }
        return pos;
    }

    private byte checkBufferValue(Object[] args, int index) throws Exception {
        if (args.length <= index || !(args[index] instanceof Number)) {
            throw new Exception("index not provided");
        }
        return (byte) (((Number) args[index]).intValue() & 0xFF);
    }

    private byte[] checkSectorData(Object[] args, int index) throws Exception {
        if (args.length <= index) {
            throw new Exception("data not provided");
        }
        byte[] data = null;
        if (args[index] instanceof byte[]) {
            data = (byte[]) args[index];
        } else if (args[index] instanceof String) {
            String dataStr = (String) args[index];
            data = new byte[dataStr.length()];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (dataStr.charAt(i) & 0xFF);
            }
        }
        if (data == null) {
            throw new Exception("data not provided");
        }
        if (data.length > BYTES_PER_SECTOR) {
            throw new Exception("data too big");
        }
        if (data.length < BYTES_PER_SECTOR) {
            byte[] newData = new byte[BYTES_PER_SECTOR];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
        return data;
    }

    private int getSectorCount(RPDisk disk) throws Exception {
        if (disk.isWritable()) {
            // writable disk
            try {
                File path = disk.getPath(false);
                return Utils.inUnitsRoundedUp((int) (path != null ? path.length() : 0L), BYTES_PER_SECTOR);
            } catch (IOException e) {
                return 0;
            }
        } else {
            try {
                // readable disk, calculate size
                return Utils.inUnitsRoundedUp(Utils.getInputStreamActualSizeAndClose(disk.openInputStream()), BYTES_PER_SECTOR);
            } catch (IOException e) {
                throw new Exception("read error");
            }
        }
    }

    private int getSectorSize(RPDisk disk) throws Exception {
        if (disk.isWritable()) {
            // writable disk
            return MAX_SECTOR_COUNT;
        } else {
            try {
                // readable disk, calculate size
                return Utils.inUnitsRoundedUp(Utils.getInputStreamActualSizeAndClose(disk.openInputStream()), BYTES_PER_SECTOR);
            } catch (IOException e) {
                throw new Exception("read error");
            }
        }
    }

    private boolean isWritable(RPDisk disk) {
        try {
            if (disk.isWritable()) {
                File path = disk.getPath(false);
                return path == null || !path.exists() || path.canWrite();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Object[] callMethod(IComputerAccess computer, int methodId, Object[] args) throws Exception {
        TileEntity parent = this.parentRef.get();
        IInventory inventory = (IInventory) parent;
        RPDisk disk = RPDiskAPI.getRPDisk(inventory, 0);
        if (disk == null && methodId >= 6) {
            throw new Exception("disk not present");
        }
        byte[] buffer = ((methodId < 2 || methodId == 10 || methodId == 11) && parent != null)
                ? RPDiskDriveBufferCache.INSTANCE.getOrCreateBuffer(parent.worldObj, parent, BYTES_PER_SECTOR)
                : null;

        switch (methodId) {
            case 0: /* getBufferByte */
                return new Object[]{ buffer == null ? 0 : (((int) buffer[checkBufferIndex(args, 0)]) & 0xFF) };
            case 1: { /* setBufferByte */
                int index = checkBufferIndex(args, 0);
                byte value = checkBufferValue(args, 1);
                if (buffer != null) {
                    buffer[index] = value;
                }
                return new Object[]{};
            }
            case 2: /* isPresent */
                return new Object[]{inventory != null && inventory.getSizeInventory() >= 1 && inventory.getStackInSlot(0) != null};
            case 3: /* hasData */
                return new Object[]{disk != null && disk.hasData()};
            case 4: /* isReadable */
                return new Object[]{disk != null};
            case 5: /* isWritable */
                return new Object[]{disk != null && isWritable(disk)};
            case 6: /* getSerialNumber */
                return new Object[]{disk.getSerialNumber()};
            case 7: /* getBytesPerSector */
                return new Object[]{BYTES_PER_SECTOR};
            case 8: /* getSectorCount */
                return new Object[]{getSectorCount(disk)};
            case 9: /* getSectorSize */
                return new Object[]{getSectorSize(disk)};
            case 10: { /* readSector */
                int sector = checkSectorIndex(disk, args, 0);
                if (buffer == null) {
                    throw new Exception("read error");
                }
                InputStream stream = disk.openInputStream();
                if (stream != null) {
                    try {
                        while (sector >= 0) {
                            sector--;
                            try {
                                Arrays.fill(buffer, (byte) 0);
                                ByteStreams.readFully(stream, buffer);
                            } catch (EOFException e) {
                                break;
                            }
                        }
                        if (sector >= 0) {
                            Arrays.fill(buffer, (byte) 0);
                        }
                        return new Object[]{true};
                    } catch (Exception e) {
                        throw new Exception("read error");
                    } finally {
                        stream.close();
                    }
                } else if (!disk.isWritable()) {
                    throw new Exception("read error");
                } else {
                    // special case: writable disk that wasn't written to yet
                    Arrays.fill(buffer, (byte) 0);
                    return new Object[]{true};
                }
                // unreachable
            }
            case 11: { /* writeSector */
                int sector = checkSectorIndex(disk, args, 0);
                if (buffer == null) {
                    throw new Exception("write error");
                }
                File path = disk.getPath(true);
                if (path != null && isWritable(disk)) {
                    RandomAccessFile file = null;
                    try {
                        file = new RandomAccessFile(path, "rw");
                        file.seek((long) sector * BYTES_PER_SECTOR);
                        file.write(buffer);
                        file.close();
                    } catch (Exception e) {
                        throw new Exception("write error");
                    } finally {
                        if (file != null) {
                            try {
                                file.close();
                            } catch (Exception e) {
                                // pass
                            }
                        }
                    }
                    return new Object[]{ true };
                } else {
                    throw new Exception("write error");
                }
                // unreachable
            }
            case 12: { /* erase */
                if (!disk.isWritable()) {
                    throw new Exception("cannot erase read-only disk");
                }
                if (ModRpcDrive.DELETE_ERASED_DISKS) {
                    File oldPath = disk.getPath(false);
                    if (oldPath != null && oldPath.exists()) {
                        oldPath.delete();
                    }
                }
                disk.eraseContents();
                return new Object[]{ true };
            }
            case 13: { /* initialize */
                if (!disk.isWritable()) {
                    throw new Exception("cannot initialize read-only disk");
                }
                File oldPath = disk.getPath(false);
                if (oldPath != null) {
                    throw new Exception("already initialized");
                }
                File newPath = disk.getPath(true);
                return new Object[]{ newPath != null };
            }
            case 14: { /* getLabel */
                String label = disk.getLabel();
                return new Object[]{label != null ? label : ""};
            }
            case 15: { /* setLabel */
                return new Object[]{disk.setLabel(checkLabel(args, 0))};
            }
            default:
                throw new Exception("unreachable");
        }
    }

    @Override
    public boolean canAttachToSide(int i) {
        return true;
    }

    @Override
    public void attach(IComputerAccess iComputerAccess) {

    }

    @Override
    public void detach(IComputerAccess iComputerAccess) {

    }
}
