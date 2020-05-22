package quarris.pickpocketer.network;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import quarris.pickpocketer.PickPocketer;

public class PacketHandler {

    public static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(PickPocketer.MODID);

    public static void init() {
        INSTANCE.registerMessage(PacketSyncPlayer.class, PacketSyncPlayer.class, 0, Side.CLIENT);
    }

    public static void sendToAllTracking(IMessage packet, Entity entity) {
        INSTANCE.sendToAllTracking(packet, entity);
    }

}