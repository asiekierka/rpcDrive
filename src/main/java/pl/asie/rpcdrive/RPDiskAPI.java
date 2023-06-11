package pl.asie.rpcdrive;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.DimensionManager;

import java.io.File;
import java.util.Random;
import java.util.regex.Pattern;

public final class RPDiskAPI {
    public static final int SERNO_LENGTH = 16;
    private static final Random SERNO_RANDOM = new Random();
    private static final Pattern VALID_SERNO = Pattern.compile("^[0-9A-Fa-f]+$");
    private static final char[] HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    // Avoid conflict with item damage-derived values. 11 zeroes (16 - 5)
    private static final String TOO_MANY_ZEROES = "00000000000";

    private RPDiskAPI() {

    }

    public static String generateSerNo(boolean forbidExistingFiles) {
        // Prevent an infinite loop.
        for (int attempts = 0; attempts < 1000000; attempts++) {
            StringBuilder sernoBuilder = new StringBuilder();
            for (int i = 0; i < SERNO_LENGTH; i++) {
                sernoBuilder.append(HEX_CHARS[SERNO_RANDOM.nextInt(HEX_CHARS.length)]);
            }
            String serno = sernoBuilder.toString();
            if (serno.startsWith(TOO_MANY_ZEROES)) {
                continue;
            }
            if (forbidExistingFiles) {
                File file = openFileFromSerNo(serno);
                if (file != null && file.exists()) {
                    continue;
                }
            }
            return serno;
        }
        throw new RuntimeException("exhausted serial number generation attempts");
    }

    public static String validateSerNoAndWarn(String serno) {
        if (!VALID_SERNO.matcher(serno).find()) {
            System.err.println("Blocked potentially invalid RP2 disk serno: " + serno);
            return null;
        }
        return serno;
    }

    public static File openFileFromSerNo(String serno) {
        if (serno == null) {
            return null;
        }
        serno = validateSerNoAndWarn(serno);
        if (serno == null) {
            return null;
        } else {
            File dir = new File(DimensionManager.getCurrentSaveRootDirectory(), "redpower");
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdir();
            }
            return new File(dir, "disk_" + serno + ".img");
        }
    }

    public static RPDisk getRPDisk(IInventory inventory, int slot) {
        try {
            if (inventory == null || inventory.getSizeInventory() <= slot) {
                return null;
            }

            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == null || stack.getItem() == null) {
                return null;
            }

            String stackName = stack.getItemName();
            if ("item.disk".equals(stackName)) {
                return new RPDiskWritableInventory(inventory, slot);
            } else if ("item.disk.forth".equals(stackName)) {
                return new RPDiskResourceInventory(inventory, slot, "eloraam/control/redforth.img");
            } else if ("item.disk.forthxp".equals(stackName)) {
                return new RPDiskResourceInventory(inventory, slot, "eloraam/control/redforthxp.img");
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
