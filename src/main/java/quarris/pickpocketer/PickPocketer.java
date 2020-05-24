package quarris.pickpocketer;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quarris.pickpocketer.config.ModConfig;
import quarris.pickpocketer.network.PacketHandler;
import quarris.pickpocketer.proxy.CommonProxy;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

@Mod(modid = PickPocketer.MODID, name = PickPocketer.NAME, version = PickPocketer.VERSION)
public class PickPocketer {
    public static final String MODID = "pickpocketer";
    public static final String NAME = "Pick Pocketer";
    public static final String VERSION = "0.1";

    public static final Logger LOGGER = LogManager.getLogger();

    @SidedProxy(clientSide = "quarris.pickpocketer.proxy.ClientProxy", serverSide = "quarris.pickpocketer.proxy.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(MODID, new ModGuiHandler());
        ModConfig.sync();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PacketHandler.init();
        ModConfig.sync();
        CapabilityManager.INSTANCE.register(CapabilityMobSteal.class, new Capability.IStorage<CapabilityMobSteal>() {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<CapabilityMobSteal> capability, CapabilityMobSteal instance, EnumFacing side) {
                return null;
            }

            @Override
            public void readNBT(Capability<CapabilityMobSteal> capability, CapabilityMobSteal instance, EnumFacing side, NBTBase nbt) {

            }
        }, () -> null);
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID)) {
            ConfigManager.sync(MODID, Config.Type.INSTANCE);
            ModConfig.sync();
        }
    }
}
