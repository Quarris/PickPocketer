package quarris.pickpocketer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityRegistry;

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

    public static boolean isEntityInArray(String[] array, Entity entity) {
        for (String name : array) {
            if (entity instanceof EntityPlayer) {
                if (name.equals("minecraft:player")) {
                    return true;
                }
                continue;
            }

            if (EntityRegistry.getEntry(entity.getClass()).getRegistryName().toString().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
