package quarris.pickpocketer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class Helper {

    public static EntityLivingBase getLookingAtEntity(EntityLivingBase self, World world, Vec3d start, Vec3d direction, double distance) {
        Vec3d end = start.add(direction.scale(distance));

        double minDist = distance * distance;
        EntityLivingBase entity = null;

        List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(start, end).grow(0.3), e -> e != self);

        for (EntityLivingBase check : entities) {
            AxisAlignedBB axisalignedbb = check.getEntityBoundingBox();
            RayTraceResult trace = axisalignedbb.calculateIntercept(start, end);
            if (trace != null) {
                double dist = start.squareDistanceTo(trace.hitVec);
                if (dist < minDist) {
                    entity = check;
                    minDist = dist;
                }
            }
        }
        return entity;
    }
}
