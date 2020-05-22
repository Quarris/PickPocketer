package quarris.pickpocketer.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

public class ClientProxy extends CommonProxy {

    @Override
    public Vec3d getEntityLook(EntityLivingBase entity) {
        return entity.getLook(Minecraft.getMinecraft().getRenderPartialTicks());
    }
}
