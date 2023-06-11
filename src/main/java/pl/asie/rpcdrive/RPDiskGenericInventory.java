package pl.asie.rpcdrive;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

abstract class RPDiskGenericInventory extends RPDiskGenericItem {
    private final IInventory inventory;
    private final int slot;

    public RPDiskGenericInventory(IInventory inventory, int slot) {
        this.inventory = inventory;
        this.slot = slot;
    }

    @Override
    protected ItemStack getStack() {
        return this.inventory.getStackInSlot(this.slot);
    }

    @Override
    protected void markStackChanged() {
        this.inventory.setInventorySlotContents(this.slot, getStack());
    }

}
