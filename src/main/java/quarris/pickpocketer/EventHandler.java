package quarris.pickpocketer;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("ALL")
@Mod.EventBusSubscriber(modid = PickPocketer.MODID)
public class EventHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getTarget() instanceof EntityLivingBase) {
            EntityPlayer player = event.getEntityPlayer();
            EntityLivingBase target = (EntityLivingBase) event.getTarget();
            if (player.isSneaking() && isHiddenFrom(player, target) && isBehind(player, target)) {
            }
        }
    }

    private static boolean isHiddenFrom(EntityPlayer player, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            return entity.canEntityBeSeen(player);
        } else if (entity instanceof EntityLiving) {
            return ((EntityLiving) entity).getAttackTarget() != player;
        } else
            return false;
    }

    private static boolean isBehind(EntityPlayer player, EntityLivingBase entity) {
        Vec3d directionToPlayer = entity.getPositionVector().subtract(player.getPositionVector());
        return entity.getLookVec().dotProduct(directionToPlayer) > 0;
    }

}
