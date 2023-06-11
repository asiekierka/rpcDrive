package pl.asie.rpcdrive;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

class RPDiskResourceInventory extends RPDiskGenericInventory {
    private final String path;
    private Boolean hasData;

    public RPDiskResourceInventory(IInventory inv, int slot, String path) {
        super(inv, slot);
        this.path = path;
    }

    @Override
    public boolean hasData() {
        if (hasData == null) {
            hasData = RPDiskResourceInventory.class.getClassLoader().getResource(path) != null;
        }
        return hasData;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public void eraseContents() {

    }

    @Override
    public String getSerialNumber() {
        ItemStack stack = getStack();
        return String.format("%016d", stack != null ? stack.getItemDamage() : 0);
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return RPDiskResourceInventory.class.getClassLoader().getResourceAsStream(path);
    }

    @Override
    public File getPath(boolean create) throws IOException {
        return null;
    }
}
