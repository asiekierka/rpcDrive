package pl.asie.rpcdrive;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class RPDiskWritableInventory extends RPDiskGenericInventory {
    public RPDiskWritableInventory(IInventory inventory, int slot) {
        super(inventory, slot);
    }

    @Override
    public boolean hasData() {
        ItemStack stack = getStack();
        return stack != null && stack.getTagCompound() != null && stack.getTagCompound().hasKey("serno");
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public void eraseContents() {
        if (hasData()) {
            getStack().getTagCompound().removeTag("serno");
            if (getStack().getTagCompound().hasNoTags()) {
                getStack().setTagCompound(null);
            }
            markStackChanged();
        }
    }

    @Override
    public String getSerialNumber() {
        if (hasData()) {
            return getStack().getTagCompound().getString("serno");
        } else {
            return null;
        }
    }

    @Override
    public String getLabel() {
        ItemStack stack = getStack();
        if (stack != null && stack.getTagCompound() != null && stack.getTagCompound().hasKey("display")) {
            NBTTagCompound displayTag = stack.getTagCompound().getCompoundTag("display");
            if (displayTag.hasKey("Name")) {
                return displayTag.getString("Name");
            }
        }
        return null;
    }

    @Override
    public boolean setLabel(String label) {
        ItemStack stack = getStack();
        if (stack != null) {
            if (label == null || label.length() == 0) {
                if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("display")) {
                    NBTTagCompound displayTag = stack.getTagCompound().getCompoundTag("display");
                    displayTag.removeTag("Name");
                    if (displayTag.hasNoTags()) {
                        stack.getTagCompound().removeTag("display");
                        if (stack.getTagCompound().hasNoTags()) {
                            stack.setTagCompound(null);
                        }
                    }
                }
            } else {
                stack.setItemName(label);
            }
            return true;
        }
        return false;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        File path = getPath(false);
        if (path != null) {
            return new FileInputStream(path);
        } else {
            return null;
        }
    }

    @Override
    public File getPath(boolean create) throws IOException {
        String label = getSerialNumber();
        if (label == null && create) {
            ItemStack stack = getStack();
            if (stack.getTagCompound() == null) {
                stack.setTagCompound(new NBTTagCompound());
            }
            stack.getTagCompound().setString("serno", RPDiskAPI.generateSerNo(true));
            label = getSerialNumber();
            markStackChanged();
        }
        return label == null ? null : RPDiskAPI.openFileFromSerNo(label);
    }
}
