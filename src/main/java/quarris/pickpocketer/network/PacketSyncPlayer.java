package quarris.pickpocketer.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSyncPlayer implements IMessage, IMessageHandler<PacketSyncPlayer, IMessage> {

    public long stolenTime;

    public PacketSyncPlayer() {
    }

    public PacketSyncPlayer(long stolenTime) {
        this.stolenTime = stolenTime;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.stolenTime);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.stolenTime = buf.readLong();
    }

    @Override
    public IMessage onMessage(PacketSyncPlayer message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Minecraft.getMinecraft().player.getEntityData().setLong("PP:StolenTime", message.stolenTime);
        });
        return null;
    }
}
