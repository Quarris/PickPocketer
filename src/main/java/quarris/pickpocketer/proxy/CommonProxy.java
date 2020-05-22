package quarris.pickpocketer.proxy;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

public class CommonProxy {

    public Vec3d getEntityLook(EntityLivingBase entity) {
        return entity.getLookVec();
    }
}
