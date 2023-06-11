package pl.asie.rpcdrive;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

abstract class RPDiskGenericItem implements RPDisk {
    protected abstract ItemStack getStack();

    protected abstract void markStackChanged();

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
            markStackChanged();
            return true;
        }
        return false;
    }
}
