package pl.asie.rpcdrive;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import dan200.computer.api.ComputerCraftAPI;
import dan200.computer.api.IHostedPeripheral;
import dan200.computer.api.IPeripheralHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mod(modid = "rpcdrive", version = "0.1.0")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class ModRpcDrive {
    public static final String LUA_EXTRACTED_PREFIX = "mods/rpcDrive/lua/";
    public static boolean DELETE_ERASED_DISKS;
    private Thread extractThread;

    @Mod.PreInit
    public void preInit(FMLPreInitializationEvent event) {
        boolean extractCCFiles = true;

        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        {
            Property prop = config.get("general", "deleteErasedDisks", true);
            prop.comment = "If true, erasing an RP2 disk will also delete its file. If your server relies on NBT-duplicated item stacks, you might want to set this to false.";
            DELETE_ERASED_DISKS = prop.getBoolean(true);
        }
        {
            Property prop = config.get("general", "extractComputerCraftFiles", extractCCFiles);
            prop.comment = "If true, built-in ComputerCraft software is extracted to mods/ComputerCraft/lua/rom/programs/rpc on every launch.";
            extractCCFiles = prop.getBoolean(extractCCFiles);
        }
        config.save();

        if (extractCCFiles) {
            List<String> ccFiles = new ArrayList<String>();
            ccFiles.add("drive");

            extractThread = new Thread(new ModFilesExtractingRunnable(
                    "pl/asie/rpcdrive/lua/rom/programs/rpc/",
                    ccFiles,
                    new File(event.getModConfigurationDirectory().getParentFile(), "mods/ComputerCraft/lua/rom/programs/rpc")
            ));
            extractThread.start();
        }
    }

    @Mod.Init
    public void init(FMLInitializationEvent event) {
        if (Loader.isModLoaded("ComputerCraft")) {
            try {
                final Class diskDriveClass = Class.forName("com.eloraam.redpower.control.TileDiskDrive");
                ComputerCraftAPI.registerExternalPeripheral(
                        diskDriveClass, new IPeripheralHandler() {
                            @Override
                            @SuppressWarnings("unchecked")
                            public IHostedPeripheral getPeripheral(TileEntity tileEntity) {
                                if (diskDriveClass.isAssignableFrom(tileEntity.getClass())) {
                                    return new PeripheralRPDiskDrive(tileEntity);
                                } else {
                                    return null;
                                }
                            }
                        }
                );
                MinecraftForge.EVENT_BUS.register(RPDiskDriveBufferCache.INSTANCE);
            } catch (ClassNotFoundException e) {
                // pass
            } catch (Exception e) {
                System.err.println("Could not initialize RP2<->CC disk drive integration!");
                e.printStackTrace();
            }
        }
    }

    @Mod.PostInit
    public void postInit(FMLPostInitializationEvent event) {
        try {
            extractThread.join();
        } catch (InterruptedException e) {
            // pass
        }
    }
}
