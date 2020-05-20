package quarris.pickpocketer;

import net.minecraft.init.Blocks;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = PickPocketer.MODID, name = PickPocketer.NAME, version = PickPocketer.VERSION)
public class PickPocketer {
    public static final String MODID = "pickpocketer";
    public static final String NAME = "Pick Pocketer";
    public static final String VERSION = "0.1";

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        NetworkRegistry.INSTANCE.registerGuiHandler(MODID, new ModGuiHandler());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

    }


}
