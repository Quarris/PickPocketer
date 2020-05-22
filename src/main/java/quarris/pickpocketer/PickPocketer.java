package quarris.pickpocketer;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;
import quarris.pickpocketer.network.PacketHandler;
import quarris.pickpocketer.proxy.CommonProxy;

@Mod(modid = PickPocketer.MODID, name = PickPocketer.NAME, version = PickPocketer.VERSION)
public class PickPocketer {
    public static final String MODID = "pickpocketer";
    public static final String NAME = "Pick Pocketer";
    public static final String VERSION = "0.1";
    @SidedProxy(clientSide = "quarris.pickpocketer.proxy.ClientProxy", serverSide = "quarris.pickpocketer.proxy.CommonProxy")
    public static CommonProxy proxy;
    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        NetworkRegistry.INSTANCE.registerGuiHandler(MODID, new ModGuiHandler());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PacketHandler.init();
    }


}
